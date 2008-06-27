package org.redcross.sar.map.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEventStack;
import org.redcross.sar.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.EventType;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.util.mso.Selector;

import com.esri.arcgis.carto.ILayerGeneralProperties;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IGeoDataset;
import com.esri.arcgis.geodatabase.IQueryFilter;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IPersistVariant;
import com.esri.arcgis.system.ITrackCancel;
import com.esri.arcgis.system.IUID;
import com.esri.arcgis.system.IVariantStream;

public abstract class AbstractMsoFeatureLayer implements IMsoFeatureLayer, IGeoDataset,
	IPersistVariant, ILayerGeneralProperties, IMsoUpdateListenerIf {

	protected String name = null;
	protected MsoClassCode classCode = null;
	protected LayerCode layerCode = null;
	protected IEnvelope extent = null;
	protected ISpatialReference srs = null;
	protected IFeatureClass featureClass = null;
	protected boolean isCached = false;
	protected boolean isValid, isVisible = true;
	protected boolean isSelectable = true;
	protected boolean isEnabled = true, isOldEnabled = true;
	protected int notifySuspended = 0;
	protected int selectionCount = 0;
	protected int visibleCount = 0;
	protected int editCount = 0;
	protected double maximumScale, minimumScale = 0;
	protected boolean showTips = false, isDirty = false;	
	protected IMsoModelIf msoModel = null;
	protected EnumSet<MsoClassCode> myInterests = null;
	protected MsoLayerEventStack eventStack = null;
	protected Map<Integer,Selector<IMsoObjectIf>> selectors = null;
	protected boolean isTextShown = true;

	public AbstractMsoFeatureLayer(MsoClassCode classCode, 
			LayerCode layerCode, IMsoModelIf msoModel, 
			ISpatialReference srs, int shapeType, 
			MsoLayerEventStack eventStack) {

		// prepare
		this.classCode = classCode;
		this.layerCode = layerCode;
		this.msoModel = msoModel;
		this.srs = srs;
		this.selectors = new HashMap<Integer,Selector<IMsoObjectIf>>(); 
		this.eventStack = eventStack;

		featureClass = new MsoFeatureClass(shapeType);
		name = DiskoEnumFactory.getText(layerCode);

		// event handling
		myInterests = EnumSet.of(classCode);
		msoModel.getEventManager().addClientUpdateListener(this);

	}

	protected boolean addInterestIn(MsoClassCode classCode) {
		if(!myInterests.contains(classCode)) {
			return myInterests.add(classCode);
		}
		return false;			
	}
	
	protected boolean removeInterestIn(MsoClassCode classCode) {
		if(myInterests.contains(classCode) && !this.classCode.equals(classCode)) {
			return myInterests.remove(classCode);
		}
		return false;			
		
	}
	
	public IMsoModelIf getMsoModel() {
		return msoModel;
	}

	protected List<IMsoObjectIf> getGeodataMsoObjects(IMsoObjectIf msoObject) {
		List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(1);
		objects.add(msoObject);
		return objects;
	}
	
	public void handleMsoUpdateEvent(Update e) {
		try {

			// get event flags
			int mask = e.getEventTypeMask();
			
	        // get flag
	        boolean clearAll = (mask & MsoEvent.EventType.CLEAR_ALL_EVENT.maskValue()) != 0;
			
	        // get mso object and feature class
	        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			MsoFeatureClass msoFC = (MsoFeatureClass)featureClass;
			
	        // clear all?
	        if(clearAll) {
	        	// forward
	        	msoFC.removeAll();
	        	// set dirty
	        	isDirty = true;
	        }
	        else {
	        	// get flags
				boolean createdObject  = (mask & EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
				boolean deletedObject  = (mask & EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
				boolean modifiedObject = (mask & EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
				boolean addedReference = (mask & EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
				boolean removedReference = (mask & EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;			
				
				// get list of
				List<IMsoObjectIf> msoObjs = getGeodataMsoObjects(msoObj);
				
				// loop over all object
				for(IMsoObjectIf it: msoObjs) {
					
					// get feature	
					IMsoFeature msoFeature = msoFC.getFeature(it.getObjectId());
		
					// get other flags
					boolean isFeature = it.getMsoClassCode().equals(classCode);
					
					// add object?
					if (createdObject && msoFeature == null && isFeature) {
						// create and att to feature class				
						msoFeature = createMsoFeature(it);
						msoFC.addFeature(msoFeature);
						// update dirty flag
						isDirty = isDirty || isFeatureDirty(msoFeature);			
					}
					// is object modified?
					if ( (addedReference || removedReference || modifiedObject) 
							&& msoFeature != null && msoFeature.geometryIsChanged(it)) {
						// change geometry
						msoFeature.msoGeometryChanged();
						// update dirty flag
						isDirty = isDirty || isFeatureDirty(msoFeature);			
					}			
					// delete object?
					if ((deletedObject) && msoFeature != null && isFeature) {
						// remove from feature class
						msoFC.removeFeature(msoFeature);
						// update dirty flag
						isDirty = isDirty || isFeatureDirty(msoFeature);			
					}
				}
	        }
			
		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}

	protected IMsoFeature createMsoFeature(IMsoObjectIf msoFeature) 
	throws AutomationException, IOException {
		return null;
	}

	protected void loadObjects(Object[] objects)  {
		try {
			MsoFeatureClass msoFC = (MsoFeatureClass)featureClass;
			for (int i = 0; i < objects.length; i++) {
				IMsoObjectIf msoObj = (IMsoObjectIf)objects[i];
				IMsoFeature msoFeature = createMsoFeature(msoObj);
				msoFC.addFeature(msoFeature);
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addMsoLayerEventListener(IMsoLayerEventListener listener) {
		if (eventStack!=null) {
			eventStack.addMsoLayerEventListener(listener);
		}
	}

	public void removeMsoLayerEventListener(IMsoLayerEventListener listener) {
		if (eventStack!=null) {
			eventStack.removeMsoLayerEventListener(listener);
		}
	}

	protected void fireOnSelectionChanged(MsoLayerEventType type) {
		if (eventStack!=null) {
			eventStack.push(this, type);
		}
	}
	
	public void setSelected(IMsoFeature msoFeature, boolean selected) {
		if(msoFeature==null) return;
		if (!msoFeature.isSelected() && selected) {
			selectionCount++;
			msoFeature.setSelected(true);
			fireOnSelectionChanged(MsoLayerEventType.SELECTED_EVENT);
		} else if (msoFeature.isSelected() && !selected) {
			selectionCount--;
			msoFeature.setSelected(false);
			fireOnSelectionChanged(MsoLayerEventType.DESELECTED_EVENT);
		}
		isDirty = isDirty || isFeatureDirty(msoFeature);			
	}

	public int clearSelected() throws AutomationException, IOException {
		int count = 0;
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selectionCount--;
				count++;
				feature.setSelected(false);
				isDirty = isDirty || isFeatureDirty(feature);			
			}
		}
		selectionCount = 0;
		if(count>0) {
			fireOnSelectionChanged(MsoLayerEventType.DESELECTED_EVENT);
		}
		return count;
	}

	public List<IMsoFeature> getSelected() throws AutomationException, IOException {
		ArrayList<IMsoFeature> selection = new ArrayList<IMsoFeature>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature);
			}
			isDirty = isDirty || isFeatureDirty(feature);			
		}
		return selection;
	}

	public List<IMsoObjectIf> getSelectedMsoObjects() throws AutomationException, IOException {
		ArrayList<IMsoObjectIf> selection = new ArrayList<IMsoObjectIf>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature.getMsoObject());
			}
			isDirty = isDirty || isFeatureDirty(feature);			
		}
		return selection;
	}

	public int getSelectionCount(boolean update) throws IOException, AutomationException {
		if(update) {
			// reset
			selectionCount = 0;
			// loop over all features
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to editing?
					if(msoFeature.isSelected()) 
						selectionCount++;
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}			
		}
		// return count
		return selectionCount;
	}

	public IFeature searchData(Envelope env) throws IOException, AutomationException {
		return null;
	}

	public boolean isDirty() {
		// only check visible
		return isDirty(false);
	}
	
	public boolean isDirty(boolean checkAll) {
		// is dirty?
		if(isDirty) return true;
		// look at children
		try {
			// get count
			int count = featureClass.featureCount(null);
			// loop over all features
			for (int i = 0; i < count; i++) {
				// get mso feature
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// is not filtered and is visible?
				if(checkAll || isFeatureDirty(msoFeature)) {
					// is dirty?
					if(msoFeature.isDirty()) {
						isDirty = true; 
						return true; 
					}
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// is not dirty
		return false;
	}

	public String getName() throws IOException, AutomationException {
		return name;
	}

	public void setName(String name) throws IOException, AutomationException {
		this.name = name;
		isDirty = true; 

	}

	public IMsoManagerIf.MsoClassCode getClassCode() {
		return classCode;
	}

	public IMsoFeatureLayer.LayerCode getLayerCode() {
		return layerCode;
	}

	public boolean isValid() throws IOException, AutomationException {
		return true;
	}

	public double getMinimumScale() throws IOException, AutomationException {
		return minimumScale;
	}

	public void setMinimumScale(double scale) throws IOException, AutomationException {
		minimumScale = scale;
		isDirty = true; 

	}

	public double getMaximumScale() throws IOException, AutomationException {
		return maximumScale;
	}

	public void setMaximumScale(double scale) throws IOException, AutomationException {
		maximumScale = scale;
		isDirty = true;
	}

	public boolean isVisible() throws IOException, AutomationException {
		return isVisible;
	}

	public void setVisible(boolean isVisible) throws IOException, AutomationException {
		if(this.isVisible != isVisible) {
			this.isVisible = isVisible;
			isDirty = true;
		}
	}

	public boolean isShowTips() throws IOException, AutomationException {
		return showTips;
	}

	public void setShowTips(boolean isShowTips) throws IOException, AutomationException {
		this.showTips = isShowTips;
	}

	public boolean isCached() throws IOException, AutomationException {
		return isCached;
	}

	public void setCached(boolean isCached) throws IOException, AutomationException {
		this.isCached = isCached;
	}

	public void setSpatialReferenceByRef(ISpatialReference srs)
		throws IOException, AutomationException {
		this.srs = srs;
	}

	public ISpatialReference getSpatialReference() throws IOException, AutomationException {
		return srs;
	}

	public IEnvelope getExtent() throws IOException, AutomationException {
		return extent;
	}

	public void setExtent(IEnvelope extent) {
		this.extent = extent;
		isDirty = true;
	}

	public IUID getID() throws IOException, AutomationException {
		return null;
	}

	public void load(IVariantStream arg0) throws IOException, AutomationException {
		// Not Supported in Java
	}

	public void save(IVariantStream arg0) throws IOException, AutomationException {
		// Not Supported in Java
	}

	public IEnvelope getAreaOfInterest() throws IOException, AutomationException {
		return extent;
	}

	public double getLastMinimumScale() throws IOException, AutomationException {
		return 0;
	}

	public double getLastMaximumScale() throws IOException, AutomationException {
		return 0;
	}

	public String ILayerGeneralProperties_getLayerDescription()
		throws IOException, AutomationException {
		return null;
	}

	public void setLayerDescription(String arg0) throws IOException, AutomationException {
	}

	public String getTipText(double arg0, double arg1, double arg2)
		throws IOException, AutomationException {
		return null;
	}

	public int getSupportedDrawPhases() throws IOException, AutomationException {
		return esriViewDrawPhase.esriViewGeography;
	}

	public void draw(int arg0, IDisplay arg1, ITrackCancel arg2)
	throws IOException, AutomationException {
	}

	public String getDataSourceType() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDisplayField() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return null;
	}

	public IFeatureClass getFeatureClass() throws IOException, AutomationException {
		return featureClass;
	}

	public boolean isScaleSymbols() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSelectable() throws IOException, AutomationException {
		return isSelectable;
	}

	public IFeatureCursor search(IQueryFilter filter, boolean b) throws IOException, AutomationException {
		return featureClass.search(filter, b);
	}

	public void setDataSourceType(String arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public void setDisplayField(String arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public void setFeatureClassByRef(IFeatureClass featureClass) throws IOException, AutomationException {
		this.featureClass = featureClass;

	}

	public void setScaleSymbols(boolean arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public void setSelectable(boolean isSelectable) throws IOException, AutomationException {
		this.isSelectable = isSelectable;
	}


	public boolean isEnabled() {
		return this.isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		// any change?
		if(this.isEnabled != isEnabled) {
			this.isEnabled = isEnabled;
			isDirty = true;
		}
	}

	public boolean isNotifySuspended() {
		return notifySuspended>0;
	}	

	public void suspendNotify() {
		// increment
		notifySuspended++;
	}

	public void resumeNotify() {
		// decrement?
		if(notifySuspended>1)
			notifySuspended--;
		// only resume on last decrement
		if(notifySuspended==1){
			try {
				// clear count
				notifySuspended = 0;
				// has event stack?
				if (eventStack!=null) {
					eventStack.fire(this);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void consumeNotify() {
		/*/ decrement?
		if(notifySuspended>0)
			notifySuspended--;
		// only resume on last decrement
		if(notifySuspended==0){
		*/
			// clear stack
			eventStack.consume(this);
		//}
		
	}
	
	public IEnvelope getVisibleFeaturesExtent() {

		try {
			
			// initialize
			IEnvelope extent = null;

			// get feature count
			int count = featureClass.featureCount(null);

			// loop over all features and hide all other planned searches
			for (int i = 0; i < count; i++) {
				// get feature
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// is visible?
				if(msoFeature.isVisible()) {
					if(extent==null)
						extent = msoFeature.getExtent();
					else
						extent.union(msoFeature.getExtent());
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}
			// finished!
			return extent;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	public void setVisibleFeatures(boolean isVisible) {

		try {

			// get feature count
			int count = featureClass.featureCount(null);

			// loop over all features and hide all other planned searches
			for (int i = 0; i < count; i++) {
				// get feature
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// make visible
				if(!msoFeature.isVisible() && isVisible) {
					visibleCount++;
					msoFeature.setVisible(true);
				} else if(msoFeature.isVisible() && !isVisible) {
					visibleCount--;
					msoFeature.setVisible(false);
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void setVisibleFeatures(IMsoObjectIf msoObj, boolean match, boolean others) {

		try {
			// get id
			String ID = msoObj.getObjectId();

			// get feature count
			int count = featureClass.featureCount(null);

			// loop over all features and hide all other planned searches
			for (int i = 0; i < count; i++) {
				// get feature
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// make visible?
				if(msoFeature.getID().equals(ID)) {
					// make visible
					if(!msoFeature.isVisible() && match) {
						visibleCount++;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !match) {
						visibleCount--;
						msoFeature.setVisible(false);
					}
				}
				else {
					// make visible
					if(!msoFeature.isVisible() && others) {
						visibleCount++;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !others) {
						visibleCount--;
						msoFeature.setVisible(false);
					}
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void setVisibleFeatures(List<IMsoObjectIf> msoObjs, boolean match, boolean others) {

		try {

			// get feature count
			int count = featureClass.featureCount(null);

			// loop over all features and hide all other planned searches
			for (int i = 0; i < count; i++) {
				// get feature
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// make visible?
				if(msoObjs.contains(msoFeature.getMsoObject())) {
					// make visible
					if(!msoFeature.isVisible() && match) {
						visibleCount++;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !match) {
						visibleCount--;
						msoFeature.setVisible(false);
					}
				}
				else {
					// make visible
					if(!msoFeature.isVisible() && others) {
						visibleCount++;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !others) {
						visibleCount--;
						msoFeature.setVisible(false);
					}
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public int getVisibleFeatureCount(boolean update) throws AutomationException, IOException {
		int count = visibleCount;
		if(update) {
			// reset
			visibleCount = 0;
			// loop over all features
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to editing?
					if(msoFeature.isVisible()) 
						visibleCount++;
				}
				isDirty = isDirty || isFeatureDirty(msoFeature);			
			}			

		}
		// dirty?
		isDirty = isDirty || (count!=visibleCount);
		// return count
		return visibleCount;
	}

	public IMsoFeature getFeature(int index) throws AutomationException, IOException {
		return (IMsoFeature)featureClass.getFeature(index);
	}

	public int getFeatureCount() throws AutomationException, IOException {
		return featureClass.featureCount(null);
	}

	public Selector<IMsoObjectIf> getSelector(int id) {
		return selectors.get(id);
	}
	
	public boolean addSelector(Selector<IMsoObjectIf> selector,int id) {
		// replace existing
		selectors.put(id,selector);
		isDirty = true;
		return true;
	}

	public boolean removeSelector(int id) {
		if(selectors.containsKey(id)) {
			selectors.remove(id);
			isDirty = true;
			return true;
		}
		return false;	
	}
	
	protected boolean select(IMsoFeature msoFeature) {
		boolean doSelect = selectors.size()==0;
		for(Selector<IMsoObjectIf> it : selectors.values()) {
			// select?
			if(it.select(msoFeature.getMsoObject())) return true;
		}
		// select feature
		return doSelect;
	}

	public boolean isTextShown() {
		return isTextShown;
	}

	public void setTextShown(boolean isVisible) {
		isTextShown = isVisible;
	}
	
	public IEnvelope getDirtyExtent() throws AutomationException, IOException {
		// initialize
		IEnvelope extent = null;
		// check deep dirty state
		int count = featureClass.featureCount(null);
		for (int i = 0; i < count; i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isDirty()) {
				if(extent==null)
					extent = feature.getExtent();
				else
					extent.union(feature.getExtent());
			}
			isDirty = isDirty || isFeatureDirty(feature); 				
		}
		return extent;
	}
	
	private boolean isFeatureDirty(IMsoFeature msoFeature) {
		return (msoFeature!=null && select(msoFeature) 
				&& msoFeature.isVisible() && msoFeature.isDirty());
	}
	
}