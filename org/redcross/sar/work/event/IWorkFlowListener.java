package org.redcross.sar.work.event;

import java.util.EventListener;

public interface IWorkFlowListener extends EventListener {

	public void onFlowPerformed(WorkFlowEvent e);

}
