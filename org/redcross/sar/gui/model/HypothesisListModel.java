package org.redcross.sar.gui.model;

import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractListModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IHypothesisIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;

public class HypothesisListModel extends AbstractListModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests;
	private IMsoModelIf msoModel = null;
	private IHypothesisIf[] data = null;

	public HypothesisListModel(IMsoModelIf msoModel) {
		// prepare
		this.myInterests = EnumSet.of(MsoClassCode.CLASSCODE_HYPOTHESIS);
		this.msoModel = msoModel;
		// add listeners
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		// forward
		load();
	}

	public EnumSet<MsoClassCode> getInterests() {
		return myInterests;
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		// clear all data?
        if(events.isClearAllEvent()) {
        	int max = Math.max(data!=null ? data.length-1: 0,0);
        	data = null;
			super.fireIntervalRemoved(this, 0, max);
        }
        else {
        	// loop over all events
			for(MsoEvent.Update e : events.getEvents(myInterests)) {

				// consume loopback updates
				if(!e.isLoopbackMode()) {

					int mask = e.getEventTypeMask();

			        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
			        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
			        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;

			        if (createdObject || modifiedObject || deletedObject ) {
						// forward
			        	load();
					}

				}
			}
		}
	}

	public IHypothesisIf getElementAt(int index) {
    	// invalid index?
    	if(data==null || !(index<data.length)) return null;
    	// return data
		return data[index];
	}

	public int getSize() {
		return (data!=null ? data.length : 0);
	}

	public void load() {
		// get data
		if(msoModel.getMsoManager().operationExists()) {
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			List<IHypothesisIf> list = cmdPost.getHypothesisList().selectItems(
					IHypothesisIf.ALL_SELECTOR,IHypothesisIf.NUMBER_COMPARATOR);
			data = list.toArray(new IHypothesisIf[0]);
			super.fireContentsChanged(this, 0, data.length-1);
		}
	}
}
