package org.redcross.sar.work.event;

import java.util.EventListener;

public interface IWorkListener extends EventListener {

	public void onWorkChange(WorkEvent e);

}
