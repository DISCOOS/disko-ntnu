package org.redcross.sar.map.layer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.event.MsoLayerEventStack;
import org.redcross.sar.map.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureModel;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;

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

public abstract class AbstractMsoFeatureLayer
				implements IMsoFeatureLayer, IGeoDataset, IPersistVariant, ILayerGeneralProperties {


	protected int notifySuspended = 0;
	protected int selectionCount = 0;
	protected int visibleCount = 0;
	protected int editCount = 0;

	protected double maximumScale, minimumScale = 0;

	protected boolean isInterestChanged = false;
	protected boolean isCached = false;
	protected boolean isValid, isVisible = true;
	protected boolean isSelectable = true;
	protected boolean isEnabled = true, isOldEnabled = true;
	protected boolean showTips = false, isDirty = false;
	protected boolean isTextShown = true;

	protected String name;
	protected MsoClassCode classCode;
	protected LayerCode layerCode;
	protected IEnvelope extent;
	protected ISpatialReference srs;
	protected MsoFeatureModel featureClass;
	protected EnumSet<MsoClassCode> myInterests;
	protected MsoLayerEventStack eventStack;
	protected IDiskoMapManager manager;

	protected final Map<Integer,Selector<IMsoObjectIf>> selectors = new HashMap<Integer,Selector<IMsoObjectIf>>();

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public AbstractMsoFeatureLayer(
			int shapeType,
			MsoClassCode classCode,
			EnumSet<MsoClassCode> coClasses,
			LayerCode layerCode,
			ISpatialReference srs,
			MsoLayerEventStack eventStack,
			IDiskoMapManager manager) {

		// prepare
		this.classCode = classCode;
		this.layerCode = layerCode;
		this.srs = srs;
		this.eventStack = eventStack;
		this.manager = manager;
		this.featureClass = new MsoFeatureModel(shapeType);
		this.name = DiskoEnumFactory.getText(layerCode);

		// event handling
		myInterests =  EnumSet.of(classCode);
		myInterests.addAll(coClasses);

	}

	/* ===============================================================
	 * IMsoFeatureLayer implementation
	 * =============================================================== */

	/**
	 * Use this method to get geodata objects. </p>
	 *
	 * The default behavior is to return a list containing the passed
	 * MSO object. If other behavior is required, override this method.
	 */
	@SuppressWarnings("unchecked")
	public List<IMsoObjectIf> getGeodataMsoObjects(IMsoObjectIf msoObject) {
		List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(1);
		if(msoObject!=null && msoObject.getMsoClassCode().equals(classCode)) {
			objects.add((IMsoObjectIf)msoObject);
		}
		return objects;
	}


	public Collection<IMsoFeature> load(Collection<IMsoObjectIf> msoObjs)  {

		// initialize list
		Collection<IMsoFeature> workList = new Vector<IMsoFeature>(msoObjs.size());

		try {
			// remove all
			featureClass.removeAll();
			// add new
			for (IMsoObjectIf msoObj : msoObjs) {
				// get list of
				List<IMsoObjectIf> geodata = getGeodataMsoObjects(msoObj);
				// loop over all object
				for(IMsoObjectIf it: geodata) {
					IMsoFeature msoFeature = createMsoFeature(it);
					featureClass.addFeature(msoFeature);
					workList.add(msoFeature);
				}
			}
			setDirty(true);

		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// finished
		return workList;

	}

	public boolean removeAll() {
		// forward
		if(featureClass.removeAll()) {
			setDirty(true);
			return true;
		}
		return false;
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
		setDirty(isDirty || isFeatureDirty(msoFeature));
	}

	public int clearSelected() throws AutomationException, IOException {
		int count = 0;
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = getFeature(i);
			if (feature.isSelected()) {
				selectionCount--;
				count++;
				feature.setSelected(false);
				setDirty(isDirty || isFeatureDirty(feature));
			}
		}
		selectionCount = 0;
		if(count>0) {
			fireOnSelectionChanged(MsoLayerEventType.DESELECTED_EVENT);
		}
		return count;
	}

	public List<IMsoFeature> getSelectedFeatures() throws AutomationException, IOException {
		ArrayList<IMsoFeature> selection = new ArrayList<IMsoFeature>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature);
			}
			setDirty(isDirty || isFeatureDirty(feature));
		}
		return selection;
	}

	public List<IMsoObjectIf> getSelectedMsoObjects() throws AutomationException, IOException {
		ArrayList<IMsoObjectIf> selection = new ArrayList<IMsoObjectIf>();
		for (int i = 0; i < featureClass.featureCount(null); i++) {
			IMsoFeature feature = getFeature(i);
			if (feature.isSelected()) {
				selection.add(feature.getMsoObject());
			}
			setDirty(isDirty || isFeatureDirty(feature));
		}
		return selection;
	}

	public int getSelectionCount(boolean update) throws IOException, AutomationException {
		if(update) {
			// reset
			selectionCount = 0;
			// loop over all features
			for (int i = 0; i < featureClass.featureCount(null); i++) {
				IMsoFeature msoFeature = getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to editing?
					if(msoFeature.isSelected())
						selectionCount++;
				}
				setDirty(isDirty || isFeatureDirty(msoFeature));
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

	@SuppressWarnings("unchecked")
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
				IMsoFeature msoFeature = getFeature(i);
				// is not filtered and is visible?
				if(checkAll || isFeatureDirty(msoFeature)) {
					// is dirty?
					if(msoFeature.isDirty()) {
						setDirty(true);
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
		setDirty(true);

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
		setDirty(true);

	}

	public double getMaximumScale() throws IOException, AutomationException {
		return maximumScale;
	}

	public void setMaximumScale(double scale) throws IOException, AutomationException {
		maximumScale = scale;
		setDirty(true);
	}

	public boolean isVisible() throws IOException, AutomationException {
		return isVisible;
	}

	public void setVisible(boolean isVisible) throws IOException, AutomationException {
		if(this.isVisible != isVisible) {
			this.isVisible = isVisible;
			setDirty(true);
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
		this.featureClass.setSpatialReferenceByRef(srs);
	}

	public ISpatialReference getSpatialReference() throws IOException, AutomationException {
		return srs;
	}

	public IEnvelope getExtent() throws IOException, AutomationException {
		return extent;
	}

	public void setExtent(IEnvelope extent) {
		this.extent = extent;
		setDirty(true);
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

	public String getTipText(double x, double y, double tolerance)
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

	@Override
	public MsoFeatureModel getFeatureClass() throws IOException, AutomationException {
		return featureClass;
	}

	public boolean isScaleSymbols() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return false;
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
		this.featureClass = (MsoFeatureModel)featureClass;
	}

	public void setScaleSymbols(boolean arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub

	}

	public boolean isSelectable() throws IOException, AutomationException {
		return isSelectable;
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
			setDirty(true);
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
		// clear stack
		eventStack.consume(this);
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
				IMsoFeature msoFeature = getFeature(i);
				// is visible?
				if(msoFeature.isVisible()) {
					if(extent==null)
						extent = msoFeature.getExtent();
					else
						extent.union(msoFeature.getExtent());
				}
				setDirty(isDirty || isFeatureDirty(msoFeature));
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
				IMsoFeature msoFeature = getFeature(i);
				// make visible
				if(!msoFeature.isVisible() && isVisible) {
					visibleCount++;
					msoFeature.setVisible(true);
				} else if(msoFeature.isVisible() && !isVisible) {
					visibleCount--;
					msoFeature.setVisible(false);
				}
				setDirty(isDirty || isFeatureDirty(msoFeature));
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
				IMsoFeature msoFeature = getFeature(i);
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
				setDirty(isDirty || isFeatureDirty(msoFeature));
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
				IMsoFeature msoFeature = getFeature(i);
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
				setDirty(isDirty || isFeatureDirty(msoFeature));
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
				IMsoFeature msoFeature = getFeature(i);
				// set editing
				if(msoFeature!=null){
					// change to editing?
					if(msoFeature.isVisible())
						visibleCount++;
				}
				setDirty(isDirty || isFeatureDirty(msoFeature));
			}

		}
		// dirty?
		setDirty(isDirty || (count!=visibleCount));
		// return count
		return visibleCount;
	}

	@SuppressWarnings("unchecked")
	public IMsoFeature getFeature(IMsoObjectIf msoObj) throws AutomationException, IOException {
		return ((MsoFeatureModel)featureClass).getFeature(msoObj.getObjectId());
	}

	@SuppressWarnings("unchecked")
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
		setDirty(true);
		return true;
	}

	public boolean removeSelector(int id) {
		if(selectors.containsKey(id)) {
			selectors.remove(id);
			setDirty(true);
			return true;
		}
		return false;
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
			IMsoFeature feature = getFeature(i);
			if (feature.isDirty()) {
				if(extent==null)
					extent = feature.getExtent();
				else
					extent.union(feature.getExtent());
			}
			setDirty(isDirty || isFeatureDirty(feature));
		}
		return extent;
	}

	public int getRefreshRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setRefreshRate(int inMillis) {
		// TODO Auto-generated method stub
	}

	/* ===============================================================
	 * IMsoUpdateListenerIf implementation
	 * =============================================================== */

	public EnumSet<MsoClassCode> getInterests() {
		return myInterests;
	}

	public void handleMsoUpdateEvent(UpdateList events) {
		processMsoUpdateEvent(events);
	}

	/**
	 * Processes MSO update events. Long operations are returned as work list.
	 * Add and change operations are long operations, remove operations are fast.
	 * Hence, add and change operations are added to the work list, remove operations
	 * are executed directly. The isDirty flag is only set for operations executed
	 * in this procedure. This disables layer redraws until the work is actually done.
	 * If only add and change operations are required, isDirty is not changed to true.
	 * If any remove operations are executed directly, the isDirty bit is set to true.
	 */
	@SuppressWarnings("unchecked")
	public Collection<IMsoFeature> processMsoUpdateEvent(UpdateList events) {

		// initialize work list
		Collection<IMsoFeature> workList = new ArrayList<IMsoFeature>();

		try {

			// get feature class
			final MsoFeatureModel msoFC = getFeatureClass();

	        // clear all?
	        if(events.isClearAllEvent()) {
	        	// forward
	        	msoFC.removeAll();
	        	// set dirty
	        	setDirty(true);
	        }
	        else {

				// loop over all events
				for(MsoEvent.Update e : events.getEvents(myInterests)) {

					// consume loopback updates
					if(!e.isLoopback()) {

						// get event flags
						int mask = e.getEventTypeMask();

				        // get mso object and feature class
				        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

			        	// get flags
						boolean createdObject  = (mask & MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
						boolean deletedObject  = (mask & MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
						boolean modifiedObject = (mask & MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
						boolean addedReference = (mask & MsoEventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
						boolean removedReference = (mask & MsoEventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;

						// get list of
						List<IMsoObjectIf> msoObjs = getGeodataMsoObjects(msoObj);

						// loop over all object
						for(IMsoObjectIf it: msoObjs) {

							// get feature
							IMsoFeature msoFeature = getFeature(it);

							// get other flags
							boolean isFeature = it.getMsoClassCode().equals(classCode);

							// add object?
							if (createdObject && msoFeature == null && isFeature) {
								// create feature
								msoFeature = createMsoFeature(it);
								// try to add feature
								if(msoFC.addFeature(msoFeature)) {
									// add load work?
									workList.add(msoFeature);
								}
							}
							// is object modified?
							if ( (addedReference || removedReference || modifiedObject)
									&& msoFeature != null && msoFeature.isMsoChanged()) {
								// add load work?
								if(!workList.contains(msoFeature)) {
									workList.add(msoFeature);
								}
							}
							// delete object?
							if ((deletedObject) && msoFeature != null && isFeature) {
								// remove from feature class
								if(msoFC.removeFeature(msoFeature)) {
									setDirty(true);
								}
							}
						}
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
		// finished
		return workList;
	}

	/* ===============================================================
	 * Protected methods
	 * =============================================================== */

	protected void setDirty(boolean isDirty) {
		if(this.isDirty!=isDirty) {
			this.isDirty = isDirty;
			/*
			if(this instanceof RouteLayer)
				System.out.println(isDirty+"#"+this);
			*/
		}
	}

	protected IMsoFeature createMsoFeature(IMsoObjectIf msoFeature)
		throws AutomationException, IOException {
		return null;
	}


	protected void fireOnSelectionChanged(MsoLayerEventType type) {
		if (eventStack!=null) {
			eventStack.push(this, type);
		}
	}

	protected boolean select(IMsoFeature msoFeature) {
		try {
			if(msoFeature!=null && msoFeature.getShape()!=null) {
				boolean doSelect = selectors.size()==0;
				for(Selector<IMsoObjectIf> it : selectors.values()) {
					// select?
					if(it.select(msoFeature.getMsoObject())) return true;
				}
				// select feature
				return doSelect;
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	private boolean isFeatureDirty(IMsoFeature msoFeature) {
		return select(msoFeature) && msoFeature.isDirty();
	}
}