package org.redcross.sar.gui.panel;

import java.awt.event.ActionListener;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.thread.event.IWorkListener;

public interface IPanel extends IChangeable,
								IMsoUpdateListenerIf,
								IMsoLayerEventListener,
								IWorkListener,
								ActionListener {

	/* ================================================
	 * IChangeable interface
	 * ================================================ */

	public boolean isDirty();
	public void setDirty(boolean isDirty);

	public boolean isChangeable();
	public void setChangeable(boolean isChangable);

	public void reset();
	public boolean finish();
	public boolean cancel();

	/* ================================================
	 * IPanel interface
	 * ================================================ */

	public void update();

	public boolean isRequestHideOnFinish();
	public void setRequestHideOnFinish(boolean isEnabled);

	public boolean isRequestHideOnCancel();
	public void setRequestHideOnCancel(boolean isEnabled);

	public void addActionListener(ActionListener listener);
	public void removeActionListener(ActionListener listener);

	public void addWorkListener(IWorkListener listener);
	public void removeWorkListener(IWorkListener listener);

	public IPanelManager getManager();
	public void setManager(IPanelManager manager);

}
