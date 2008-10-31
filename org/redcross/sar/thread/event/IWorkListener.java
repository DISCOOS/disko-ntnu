package org.redcross.sar.thread.event;

import java.util.EventListener;

public interface IWorkListener extends EventListener {

	public void onWorkPerformed(WorkEvent e);

}
