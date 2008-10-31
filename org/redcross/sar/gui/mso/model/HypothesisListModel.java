package org.redcross.sar.gui.mso.model;

import java.util.EnumSet;

import javax.swing.AbstractListModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;

public class HypothesisListModel extends AbstractListModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests;
	private IMsoModelIf msoModel = null;
	private Object[] data = null;

	public HypothesisListModel(IMsoModelIf msoModel) {
		// prepare
		this.myInterests = EnumSet.of(MsoClassCode.CLASSCODE_HYPOTHESIS);
		this.msoModel = msoModel;
		// add listeners
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		// get data
		if(msoModel.getMsoManager().operationExists()) {
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			data = (cmdPost.getHypothesisListItems().toArray());
			super.fireContentsChanged(this, 0, data.length-1);
		}
	}

	public EnumSet<MsoClassCode> getInterests() {
		return myInterests;
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		// loop over all events
		for(MsoEvent.Update e : events.getEvents(myInterests)) {

			// consume loopback updates
			if(!UpdateMode.LOOPBACK_UPDATE_MODE.equals(e.getUpdateMode())) {

				int mask = e.getEventTypeMask();

		        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
		        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
		        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
		        boolean clearAll = (mask & MsoEvent.MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;

		        if(clearAll) {
		        	int max = data!=null ? data.length-1: 0;
		        	data = null;
					super.fireContentsChanged(this, 0, max);
		        }
		        else if (createdObject || modifiedObject || deletedObject ) {
					// get data
					ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
					data = (cmdPost!=null ? cmdPost.getHypothesisListItems().toArray() : null);
					super.fireContentsChanged(this, 0, data!=null ? data.length-1: 0);
				}

			}
		}
	}

	public Object getElementAt(int index) {
    	// invalid index?
    	if(data==null || !(index<data.length)) return null;
    	// return data
		return data[index];
	}

	public int getSize() {
		return (data!=null ? data.length : 0);
	}
}
