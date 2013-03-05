package org.redcross.sar.work.event;

import java.util.EventListener;

public interface IWorkLoopListener extends EventListener {

	public void onLoopChange(WorkLoopEvent e);

}
