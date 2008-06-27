package org.redcross.sar.gui.model.mso;

import java.util.EnumSet;

import javax.swing.AbstractListModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

public class HypothesisListModel extends AbstractListModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private IMsoModelIf msoModel = null;
	private Object[] data = null;

	public HypothesisListModel(IMsoModelIf msoModel) {
		// prepare
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS);
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

	public void handleMsoUpdateEvent(Update e) {
		int mask = e.getEventTypeMask();

        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean clearAll = (mask & MsoEvent.EventType.CLEAR_ALL_EVENT.maskValue()) != 0;
		
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

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
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
