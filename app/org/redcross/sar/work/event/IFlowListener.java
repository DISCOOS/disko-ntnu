package org.redcross.sar.work.event;

import java.util.EventListener;

public interface IFlowListener extends EventListener {

	public void onFlowPerformed(FlowEvent e);

}
