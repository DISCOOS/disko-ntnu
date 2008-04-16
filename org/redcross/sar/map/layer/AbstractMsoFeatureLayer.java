package org.redcross.sar.map.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.IMsoLayerEventListener;
import org.redcross.sar.event.MsoLayerEvent;
import org.redcross.sar.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
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
	protected boolean isEditing = false;
	protected boolean isEnabled = true, isOldEnabled = true;
	protected boolean isNotifySuspended = true;
	protected int selectionCount = 0;
	protected int visibleCount = 0;
	protected int editCount = 0;
	protected double maximumScale, minimumScale = 0;
	protected boolean showTips, isDirty = false;	
	protected ArrayList<IMsoLayerEventListener> listeners = null;
	protected MsoLayerEvent msoLayerEvent = null;
	protected IMsoModelIf msoModel = null;
	protected EnumSet<MsoClassCode> myInterests = null;
	protected HashMap<MsoLayerEventType,MsoLayerEvent> suspendedEvents = null;
	protected Map<Integer,Selector<IMsoObjectIf>> selectors = null;
	protected boolean isTextShown = true;

	public AbstractMsoFeatureLayer(MsoClassCode classCode, 
			LayerCode layerCode, IMsoModelIf msoModel, 
			ISpatialReference srs, int shapeType) {

		// initialize objects
		this.classCode = classCode;
		this.layerCode = layerCode;
		this.msoModel = msoModel;
		this.srs = srs;
		this.selectors = new HashMap<Integer,Selector<IMsoObjectIf>>(); 

		featureClass = new MsoFeatureClass(shapeType);
		name = Utils.translate(layerCode);

		// event handling
		listeners = new ArrayList<IMsoLayerEventListener>();
		msoLayerEvent = new MsoLayerEvent(this,MsoLayerEventType.SELECTION_CHANGED_EVENT);
		suspendedEvents = new HashMap<MsoLayerEventType,MsoLayerEvent>(1);		
		myInterests = EnumSet.of(classCode);
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);

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

	public void handleMsoUpdateEvent(Update e) {
		try {

			// try to get mso feature
			MsoFeatureClass msoFC = (MsoFeatureClass)featureClass;
			IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			IMsoFeature msoFeature = msoFC.getFeature(msoObj.getObjectId());

			// get event flags
			int mask = e.getEventTypeMask();
			boolean createdObject  = (mask & EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
			boolean deletedObject  = (mask & EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
			boolean modifiedObject = (mask & EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
			boolean addedReference = (mask & EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
			boolean removedReference = (mask & EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;			
			
			// get other flags
			boolean isFeature = msoObj.getMsoClassCode().equals(classCode);
			
			// add object?
			if (createdObject && msoFeature == null && isFeature) {
				msoFeature = createMsoFeature(msoObj);
				msoFC.addFeature(msoFeature);
				if (msoFeature.getShape() != null) {
					isDirty = true;
				}
			}
			// is object modified?
			if ( (addedReference || removedReference || modifiedObject) 
					&& msoFeature != null && msoFeature.geometryIsChanged(msoObj)) {
				msoFeature.msoGeometryChanged();
				isDirty = true;
			}
			
			// delete object?
			if ((deletedObject) && msoFeature != null && isFeature) {
				msoFC.removeFeature(msoFeature);
				isDirty = true;
			}
			
		} catch (AutomationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addDiskoLayerEventListener(IMsoLayerEventListener listener) {
		if (listeners.indexOf(listener) == -1) {
			listeners.add(listener);
		}
	}

	public void removeDiskoLayerEventListener(IMsoLayerEventListener listener) {
		listeners.remove(listener);
	}

	protected void fireOnSelectionChanged() {
		try {
			if(isNotifySuspended) {
				suspendedEvents.put(MsoLayerEventType.SELECTION_CHANGED_EVENT, msoLayerEvent);
			}
			else {
				for (int i = 0; i < listeners.size(); i++) {
					listeners.get(i).onSelectionChanged(msoLayerEvent);
				}
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setSelected(IMsoFeature msoFeature, boolean selected) {
		if (!msoFeature.isSelected() && selected) {
			selectionCount++;
			isDirty = true;
			msoFeature.setSelected(true);
			fireOnSelectionChanged();
		} else if (msoFeature.isSelected() && !selected) {
			selectionCount--;
			isDirty = true;
			msoFeature.setSelected(false);
			fireOnSelectionChanged();
		}
	}

	public int clearSelected() throws AutomationException, IOException {
		int count = 0;
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selectionCount--;
				count++;
				feature.setSelected(false);
			}
		}
		if(count>0) {
			isDirty = true;
			fireOnSelectionChanged();
		}
		return count;
	}

	public List getSelected() throws AutomationException, IOException {
		ArrayList<IMsoFeature> selection = new ArrayList<IMsoFeature>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature);
			}
		}
		return selection;
	}

	public List getSelectedMsoObjects() throws AutomationException, IOException {
		ArrayList<IMsoObjectIf> selection = new ArrayList<IMsoObjectIf>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature.getMsoObject());
			}
		}
		return selection;
	}

	public int getSelectionCount(boolean update) throws IOException, AutomationException {
		int count = selectionCount;
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
			}			
			// dirty?
			isDirty = isDirty || (count!=selectionCount);
		}
		// return count
		return selectionCount;
	}

	public IFeature searchData(Envelope env) throws IOException, AutomationException {
		return null;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public String getName() throws IOException, AutomationException {
		return name;
	}

	public void setName(String name) throws IOException, AutomationException {
		this.name = name;
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

	public void setMinimumScale(double arg0) throws IOException, AutomationException {
		minimumScale = arg0;
	}

	public double getMaximumScale() throws IOException, AutomationException {
		return maximumScale;
	}

	public void setMaximumScale(double arg0) throws IOException, AutomationException {
		maximumScale = arg0;
	}

	public boolean isVisible() throws IOException, AutomationException {
		return isVisible;
	}

	public void setVisible(boolean isVisible) throws IOException, AutomationException {
		isDirty = isDirty || (this.isVisible != isVisible);
		this.isVisible = isVisible;
	}

	public boolean isShowTips() throws IOException, AutomationException {
		return showTips;
	}

	public void setShowTips(boolean arg0) throws IOException, AutomationException {
		showTips = arg0;
	}

	public boolean isCached() throws IOException, AutomationException {
		return isCached;
	}

	public void setCached(boolean arg0) throws IOException, AutomationException {
		isCached = arg0;
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

	/*
	public boolean isEditing() {
		return isEditing;
	}

	public int getEditCount(boolean update) throws IOException, AutomationException {
		int count = editCount;
		if(update) {
			// reset
			editCount = 0;
			// loop over all features
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to editing?
					if(msoFeature.isEditing()) 
						editCount++;
				}
			}			
			// dirty?
			isDirty = isDirty || (count!=editCount);
		}
		// return count
		return editCount;
	}

	public List startEdit(IMsoObjectIf msoObj) throws IOException, AutomationException {
		
		// is not editing?
		if(!isEditing) {
			// save old state
			isOldEnabled = isEnabled;
		}
		
		// enable
		setEnabled(true);
		
		// set edit flag
		setEditing(true);
		
		// set object edit state?
		if(msoObj!=null) {
			MsoFeatureClass msoFC = (MsoFeatureClass)getFeatureClass();
			IMsoFeature msoFeature = msoFC.getFeature(msoObj.getObjectId());
			// set editing
			if(msoFeature!=null){
				// change to editing?
				if(!msoFeature.isEditing()) {
					msoFeature.setEditing(true);
					editCount++;
					isDirty = true;				
				}
			}
		}
		
		/* no abstract references to mso objects are defined in this abstract class.
		 * However, classes that extends this abstract class may have mso features that
		 * reference other mso objects that belongs to yet another mso feature layer. The
		 * AreaLayer is one such example where each area has a poi list. These poi
		 * must also be marked as isEditing. Thus AreaLayer must override this method
		 * in the following manner:
		 * 
		 * 1. Override startEditing(*)
		 * 2. In the overridden method, call super.startEditing(*) to invoke this method first
		 * 3. Then, collect any referenced objects
		 * 4. The overridden method return the list of any referenced mso objects
		 */
	/*
		return null;
	}

	public List stopEdit(IMsoObjectIf msoObj) throws IOException, AutomationException {

		// restore old
		setEnabled(isOldEnabled);

		// update edit count
		getEditCount(true);

		// stop all edits?
		if(msoObj==null) {
			// loop over all features
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature msoFeature = (IMsoFeature)featureClass.getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to not editing?
					if(msoFeature.isEditing()) {
						msoFeature.setEditing(false);
						editCount--;
						isDirty = true;				
					}
				}
			}
		}
		else {
			MsoFeatureClass msoFC = (MsoFeatureClass)getFeatureClass();
			IMsoFeature msoFeature = msoFC.getFeature(msoObj.getObjectId());
			// set editing
			if(msoFeature!=null){
				// change to not editing?
				if(msoFeature.isEditing()) {
					msoFeature.setEditing(false);
					editCount--;
					isDirty = true;				
				}
			}
		}
		
		// are all edits stopped?
		setEditing(editCount != 0);

		/* no abstract references to mso objects are defined in this abstract class.
		 * However, classes that extends this abstract class may have mso features that
		 * reference other mso objects that belongs to yet another mso feature layer. The
		 * AreaLayer is one such example where each area has a poi list. These poi
		 * must also be marked as isEditing. Thus AreaLayer must override this method
		 * in the following manner:
		 * 
		 * 1. Override stopEdit(*)
		 * 2. In the overridden method, call super.stopEdit(*) to invoke this method first
		 * 3. Then, collect any referenced objects
		 * 4. The overridden method return the list of any referenced mso objects
		 */

	/*
		return null;
	}	

	public ArrayList<IMsoFeature> getEditing() throws AutomationException, IOException {
		ArrayList<IMsoFeature> edits = new ArrayList<IMsoFeature>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = (IMsoFeature)featureClass.getFeature(i);
			if (feature.isEditing()) {
				edits.add(feature);
			}
		}
		return edits;
	}

	*/
	
	public boolean isEnabled() {
		return this.isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		// only allowed if not editing
		if(!isEditing) {
			isDirty = isDirty || (this.isEnabled != isEnabled);
			this.isEnabled = isEnabled;
		}
	}

	private void setEditing(boolean isEditing) {
		// only allowed if not editing
		isDirty = isDirty || (this.isEditing != isEditing);
		this.isEditing = isEditing;
	}
	
	public void suspendNotify() {
		isNotifySuspended = true;
	}

	public void resumeNotify() {
		try {
			if(suspendedEvents.containsKey(MsoLayerEventType.SELECTION_CHANGED_EVENT)) {
				for (int i = 0; i < listeners.size(); i++) {
					listeners.get(i).onSelectionChanged(msoLayerEvent);
				}
			}
			// clear suspended events
			suspendedEvents.clear();
			// clear flag
			isNotifySuspended = false;
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isNotifySuspended() {
		return isNotifySuspended;
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
					isDirty = true;
					msoFeature.setVisible(true);
				} else if(msoFeature.isVisible() && !isVisible) {
					visibleCount--;
					isDirty = true;
					msoFeature.setVisible(false);
				}
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
						isDirty = true;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !match) {
						visibleCount--;
						isDirty = true;
						msoFeature.setVisible(false);
					}
				}
				else {
					// make visible
					if(!msoFeature.isVisible() && others) {
						visibleCount++;
						isDirty = true;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !others) {
						visibleCount--;
						isDirty = true;
						msoFeature.setVisible(false);
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
	}

	public void setVisibleFeatures(List msoObjs, boolean match, boolean others) {

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
						isDirty = true;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !match) {
						visibleCount--;
						isDirty = true;
						msoFeature.setVisible(false);
					}
				}
				else {
					// make visible
					if(!msoFeature.isVisible() && others) {
						visibleCount++;
						isDirty = true;
						msoFeature.setVisible(true);
					} else if(msoFeature.isVisible() && !others) {
						visibleCount--;
						isDirty = true;
						msoFeature.setVisible(false);
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

}