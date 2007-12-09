package org.redcross.sar.gui.models;

import java.util.EnumSet;

import javax.swing.AbstractListModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
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
	private ICmdPostIf cmdPost = null;
	private Object[] data = null;

	public HypothesisListModel(IMsoModelIf msoModel) {
		myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_HYPOTHESIS);
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		cmdPost = msoModel.getMsoManager().getCmdPost();
		data = cmdPost.getHypothesisListItems().toArray();
		super.fireContentsChanged(this, 0, data.length-1);
	}

	public void handleMsoUpdateEvent(Update e) {
		int mask = e.getEventTypeMask();

        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
		
		if (createdObject || modifiedObject || deletedObject ) {
			data = cmdPost.getHypothesisListItems().toArray();
			super.fireContentsChanged(this, 0, data.length-1);
		}
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}

	public Object getElementAt(int index) {
    	// invalid index?
    	if(!(index<data.length)) return null;
    	// return data
		return data[index];
	}

	public int getSize() {
		return data.length;
	}
}
