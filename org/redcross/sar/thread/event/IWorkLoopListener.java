package org.redcross.sar.thread.event;

import java.util.EventListener;

public interface IWorkLoopListener extends EventListener {

	public void onWorkLoopChange(WorkLoopEvent e);

}
