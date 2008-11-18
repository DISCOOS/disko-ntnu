package org.redcross.sar.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JPanel;

import org.redcross.sar.gui.IMsoHolder;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMapLayer.LayerCode;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractPanel extends JPanel implements IPanel, IMsoHolder {

	private static final long serialVersionUID = 1L;

	private int m_isMarked = 0;

	private int consumeCount = 0;
	private int loopCount = 0;

	private boolean isDirty = false;
	private boolean requestHideOnFinish = true;
	private boolean requestHideOnCancel = true;

	protected IMsoModelIf msoModel;

	protected IMsoObjectIf msoObject;

	protected EnumSet<LayerCode> msoLayers;
	protected EnumSet<MsoClassCode> msoInterests;

	/* ===========================================
	 * Constructors
	 * ===========================================
	 */

	public AbstractPanel() {
		this("");
	}

	public AbstractPanel(String caption) {
		// prepare
        msoLayers =  EnumSet.noneOf(LayerCode.class);
		msoInterests = EnumSet.noneOf(MsoClassCode.class);
	}

	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public EnumSet<MsoClassCode> getInterests() {
		return msoInterests;
	}

	public void setInterests(IMsoModelIf model, EnumSet<MsoClassCode> interests) {
		// unregister?
		if(msoModel!=null) {
			msoModel.getEventManager().removeClientUpdateListener(this);
		}
		// initialize
		msoModel = model;
		msoInterests = EnumSet.noneOf(MsoClassCode.class);
		// add listener?
		if(model!=null) {
			msoInterests = interests;
			msoModel.getEventManager().addClientUpdateListener(this);
		}
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		// consume?
		if(!isChangeable()) return;

		// loop over all events
		for(MsoEvent.Update e : events.getEvents(msoInterests)) {

			// consume loopback updates
			if(!e.isLoopback()) {

				// get mask
				int mask = e.getEventTypeMask();

		        // get mso object
		        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

		        // get flag
		        boolean clearAll = (mask & MsoEvent.MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;

		        // clear all?
		        if(clearAll) {
		        	msoObjectClearAll(this.msoObject,mask);
		        }
		        else {
		        	// get flags
			        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
			        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
			        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
			        boolean addedReference = (mask & MsoEvent.MsoEventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
			        boolean removedReference = (mask & MsoEvent.MsoEventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;

			        // add object?
					if (createdObject) {
						msoObjectCreated(msoObj,mask);
					}
					// is object modified?
					if ( (addedReference || removedReference || modifiedObject)) {
						msoObjectChanged(msoObj,mask);
					}
					// delete object?
					if (deletedObject) {
						msoObjectDeleted(msoObj,mask);
					}
		        }

	        }
		}
	}

	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * =========================================== */

	public void onSelectionChanged(MsoLayerEvent e) {
		if (!e.isFinal()) return;
		try {
			// initialize
			IMsoObjectIf msoObj = null;
			List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
			// select new?
			if (selection != null && selection.size() > 0) {
				// get mso object
				msoObj = selection.get(0);
			}
			// forward
			setMsoObject(msoObj);
		} catch (AutomationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public boolean setSelectedMsoFeature(IDiskoMap map) {

		// initialize
		IMsoObjectIf msoObj = null;

		// catch exceptions
		try {
	        // get selected object
	        List<IMsoFeature> features = map.getMsoSelection();
	        if(features!=null && features.size()>0) {
        		// cast first item to IMsoFeature and get mso object
        		msoObj = features.get(0).getMsoObject();
	        }
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		// forward
		setMsoObject(msoObj);

		// finished
		return (msoObject!=null);

	}

	/* ===========================================
	 * IDiskoPanel implementation
	 * =========================================== */

	public abstract void update();

	public abstract boolean doAction(String command);

	public abstract void addActionListener(ActionListener listener);

	public abstract void removeActionListener(ActionListener listener);

	public abstract void addWorkFlowListener(IWorkFlowListener listener);

	public abstract void removeWorkFlowListener(IWorkFlowListener listener);

	public abstract IPanelManager getManager();

	public abstract void setManager(IPanelManager manager, boolean isMainPanel);

	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObj) {
		// prepare
		msoObject = msoObj;
		// forward
		update();
	}

	public void reset() {
		// consume change events
		setChangeable(false);
		// reapply mso object
		setMsoObject(getMsoObject());
		// reset flag?
		if(isDirty) setDirty(false);
		// resume change events
		setChangeable(true);
	}

	public boolean finish() {
		// get dirty flag
		boolean bFlag = isDirty();
		// consume?
		if(!isChangeable()) return false;
		// consume change events
		setChangeable(false);
		// suspend for faster update?
		if(msoModel!=null) msoModel.suspendClientUpdate();
		// request action
		bFlag = beforeFinish();
		// resume updates?
		if(msoModel!=null) msoModel.resumeClientUpdate(true);
		// finish?
		if(bFlag) {
			// request action
			afterFinish();
			// reset dirty flag
			setDirty(false);
		}
		// resume change events
		setChangeable(true);
		// finished
		return bFlag;
	}

	public boolean cancel() {
		// consume?
		if(!isChangeable()) return false;
		// consume change events
		setChangeable(false);
		// request action
		boolean bFlag = beforeCancel();
		// cancel?
		 if(bFlag) {
			// forward
			reset();
			// hide manager
			afterCancel();
			// reset dirty flag
			setDirty(false);
		 }
		// resume change events
		setChangeable(true);
		// finished
		return bFlag;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		setDirty(isDirty,true);
	}

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		if(m_isMarked != isMarked) {
			m_isMarked = isMarked;
		}
	}

	public boolean isRequestHideOnFinish() {
		return requestHideOnFinish;
	}

	public void setRequestHideOnFinish(boolean isEnabled) {
		requestHideOnFinish = isEnabled;
	}

	public boolean isRequestHideOnCancel() {
		return requestHideOnCancel;
	}

	public void setRequestHideOnCancel(boolean isEnabled) {
		requestHideOnCancel = isEnabled;
	}

	public boolean isChangeable() {
		return (consumeCount==0);
	}

	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			consumeCount++;
		else if(consumeCount>0)
			consumeCount--;
	}

	public boolean isLoop() {
		return (loopCount>0);
	}

	public void setLoop(boolean isLoop) {
		if(isLoop)
			loopCount++;
		else if(loopCount>0)
			loopCount--;
	}

	public EnumSet<LayerCode> getMsoLayers() {
		return msoLayers;
	}

	public void setMsoLayers(IDiskoMap map, EnumSet<LayerCode> layers) {
		// unregister?
		if(this.msoLayers!=null) {
			// loop over all layers
			for(LayerCode it: layers) {
				IMsoFeatureLayer l = map.getMsoLayer(it);
				if(l!=null) l.removeMsoLayerEventListener(this);
			}
		}
		this.msoLayers = layers!=null ? layers : EnumSet.noneOf(LayerCode.class);
		// register?
		if(layers!=null) {
			// loop over all layers
			for(LayerCode it: layers) {
				IMsoFeatureLayer l = map.getMsoLayer(it);
				if(l!=null) l.addMsoLayerEventListener(this);
			}
		}
	}


	/* ===========================================
	 * ActionListener implementation
	 * =========================================== */

	public abstract void actionPerformed(ActionEvent e);

	/* ===========================================
	 * IWorkListener implementation
	 * =========================================== */

	public void onFlowPerformed(WorkFlowEvent e) {
		fireOnWorkPerformed(e);
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected abstract void fireActionEvent(ActionEvent e);

	protected abstract void fireOnWorkFinish(Object source, Object data);

	protected abstract void fireOnWorkCancel(Object source, Object data);

	protected abstract void fireOnWorkChange(Object source, Object data);

	protected abstract void fireOnWorkPerformed(WorkFlowEvent e);

	protected void setDirty(boolean isDirty, boolean update) {
		this.isDirty = isDirty;
		if(update) {
			setChangeable(false);
			update();
			setChangeable(true);
		}
	}

	protected boolean requestShow() {
		if(getManager()!=null)
			return getManager().requestShow();
		return false;
	}

	protected boolean requestHide() {
		if(getManager()!=null)
			return getManager().requestHide();
		return false;
	}

	protected boolean requestMoveTo(int dx, int dy, boolean isRelative) {
		if(getManager()!=null)
			return getManager().requestMoveTo(dx, dy, isRelative);
		return false;
	}

	protected boolean requestResize(int w, int h, boolean isRelative) {
		if(getManager()!=null)
			return getManager().requestResize(w, h, isRelative);
		return false;
	}

	protected boolean requestFitToContent() {
		if(getManager()!=null)
			return getManager().requestFitToContent();
		return false;
	}

	protected boolean beforeFinish() {
		return true;
	}

	protected boolean beforeCancel() {
		return true;
	}

	protected void afterFinish() {
		fireOnWorkFinish(this,msoObject);
		if(getManager()!=null && requestHideOnFinish)
			getManager().requestHide();
	}

	protected void afterCancel() {
		fireOnWorkCancel(this,msoObject);
		if(getManager()!=null && requestHideOnCancel)
			getManager().requestHide();
	}

	protected void msoObjectCreated(IMsoObjectIf msoObj, int mask) { /*NOP*/ }

	protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.msoObject) {
			setMsoObject(msoObject);
		}
	}

	protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.msoObject) {
			// forward
			setMsoObject(null);
		}
	}

	protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
		// is same as selected?
		if(msoObj == this.msoObject) {
			// forward
			setMsoObject(null);
		}
	}
}
