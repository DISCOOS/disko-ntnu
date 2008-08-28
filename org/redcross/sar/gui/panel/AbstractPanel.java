package org.redcross.sar.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JPanel;

import org.redcross.sar.app.Utils;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IDiskoLayer.LayerCode;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;

import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractPanel extends JPanel implements IPanel {

	private static final long serialVersionUID = 1L;

	private boolean isDirty = false;	
	private int consumeCount = 0;
	private boolean requestHideOnFinish = true;
	private boolean requestHideOnCancel = true;
	
	protected IMsoModelIf msoModel = null;
	
	protected IMsoObjectIf msoObject = null;
	
	protected EnumSet<LayerCode> msoLayers = null;
	protected EnumSet<MsoClassCode> msoInterests = null;
	
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
		msoModel = Utils.getApp().getMsoModel();
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) {
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return msoInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
		
		// consume?
		if(!isChangeable()) return;
		
		// get mask
		int mask = e.getEventTypeMask();
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // get flag
        boolean clearAll = (mask & MsoEvent.EventType.CLEAR_ALL_EVENT.maskValue()) != 0;
		
        // clear all?
        if(clearAll) {
        	msoObjectClearAll(this.msoObject,mask);
        }
        else {
        	// get flags
	        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
	        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
	        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
	        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
	        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
			
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

	/* ===========================================
	 * IMsoLayerEventListener implementation
	 * ===========================================
	 */

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
	 * ===========================================
	 */
	
	public abstract void update();
	
	public abstract boolean doAction(String command);
	
	public abstract void addActionListener(ActionListener listener);
	
	public abstract void removeActionListener(ActionListener listener);
	
	public abstract void addDiskoWorkListener(IDiskoWorkListener listener);
	
	public abstract void removeDiskoWorkListener(IDiskoWorkListener listener);
	
	public abstract IPanelManager getManager();
	
	public abstract void setManager(IPanelManager manager);
	
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
		// suspend for faster update
		msoModel.suspendClientUpdate();
		// request action
		bFlag = beforeFinish();
		// resume updates
		msoModel.resumeClientUpdate();
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
	
	public EnumSet<MsoClassCode> getInterests() {
		return msoInterests;
	}
	
	public void setInterests(IMsoModelIf model, EnumSet<MsoClassCode> interests) {
		// unregister me?
		if(model!=msoModel && msoModel!=null)
			msoModel.getEventManager().removeClientUpdateListener(this);
		// prepare
		msoModel = model;
		this.msoInterests = EnumSet.noneOf(MsoClassCode.class);
		// add listener?
		if(model!=null) {
			msoModel.getEventManager().addClientUpdateListener(this);
			this.msoInterests = interests;
		}
	}
	
	/* ===========================================
	 * ActionListener implementation
	 * ===========================================
	 */
	
	public abstract void actionPerformed(ActionEvent e);
	
	/* ===========================================
	 * IDiskoWorkListener implementation 
	 * ===========================================
	 */
	
	public void onWorkPerformed(DiskoWorkEvent e) {
		fireOnWorkPerformed(e);
	}

	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */
	
	protected abstract void fireActionEvent(ActionEvent e);
	
	protected abstract void fireOnWorkFinish(Object source, Object data);
    
	protected abstract void fireOnWorkCancel(Object source, Object data);
    
	protected abstract void fireOnWorkChange(Object source, Object data);
    
	protected abstract void fireOnWorkPerformed(DiskoWorkEvent e);
	
	protected void setDirty(boolean isDirty, boolean update) {
		this.isDirty = isDirty;
		if(update) {
			setChangeable(false);
			update();
			setChangeable(true);
		}
	}
	
	protected void requestShow() {
		if(getManager()!=null)
			getManager().requestShow();
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
		
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
