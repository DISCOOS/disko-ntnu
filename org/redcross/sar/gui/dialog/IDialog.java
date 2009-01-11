package org.redcross.sar.gui.dialog;

import java.awt.Component;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.panel.IPanelManager;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.work.event.IWorkFlowListener;

public interface IDialog extends IChangeable, IPanelManager {

	/* =======================================================
	 * IDialog interface
	 * ======================================================= */

	public void snapTo();

	public boolean isMoveable();
	public void setMoveable(boolean isMoveable);

	public boolean isTranslucent();
	public boolean setTrancluent(boolean isTranslucent);

	public float getOpacity();
	public float setOpacity(float opacity);

	public int isTranslucentOn();
	public int setTrancluentOn(int isTranslucentOn);

	public int isMarkedOn();
	public int setMarkedOn(int isMarkedOn);

	public boolean isVisible();
	public void setVisible(boolean isVisible);

	public void cancelSetVisible();
	public void delayedSetVisible(boolean isVisible, int millisToShow);

	public void setSnapToLocation(Component snapTo, int position, int sizeTo, boolean snapToInside, boolean isSnapToLocked);

	public boolean isWorkSupported();

	public void setEscapeable(boolean isEscapeable);
	public boolean isEscapeable();

	/* ================================================
	 * IWork interface
	 * ================================================ */

	public boolean isDirty();
	public void setDirty(boolean isDirty);

	public boolean isChangeable();
	public void setChangeable(boolean isChangable);

	public IMsoObjectIf getMsoObject();
	public void setMsoObject(IMsoObjectIf msoObj);

	public void reset();
	public boolean finish();
	public boolean cancel();

	public void addWorkFlowListener(IWorkFlowListener listener);
	public void removeWorkFlowListener(IWorkFlowListener listener);

	/* =======================================================
	 * IPanelManager interface
	 * ======================================================= */

	public boolean requestMoveTo(int x, int y, boolean isRelative);
	public boolean requestResize(int w, int h, boolean isRelative);

	public boolean requestHide();
	public boolean requestShow();

}
