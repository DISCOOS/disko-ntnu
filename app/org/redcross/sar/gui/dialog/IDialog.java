package org.redcross.sar.gui.dialog;

import java.awt.Component;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.panel.IPanelManager;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.work.event.IFlowListener;

public interface IDialog extends IChangeable, IPanelManager {

	/**
	 * The dialog translucent state is set manually 
	 * {@code setTranslucent(boolean isEnabled)}
	 */
    public static final int TRANSLUCENT_MANUAL = 0;

    /**
	 * The dialog translucent state is set automatically 
	 * when the dialog gets and loose focus.
	 */
    public static final int TRANSLUCENT_ONFOCUS = 1;

    /**
	 * The dialog translucent state is set automatically 
	 * when the mouse enters and leaves the dialog bounds.
	 */
    public static final int TRANSLUCENT_ONMOUSE = 2;

	/**
	 * The dialog marked state (colored border) is set 
	 * manually {@code setMarked(boolean isEnabled)}
	 */
    public static final int MARKED_MANUAL = TRANSLUCENT_MANUAL;

    /**
	 * The dialog changes marked state automatically 
	 * when the dialog gets and loose focus.
	 */
    public static final int MARKED_ONFOCUS = TRANSLUCENT_ONFOCUS;

    /**
	 * The dialog change marked state automatically 
	 * when the dialog gets and loose focus.
	 */
    public static final int MARKED_ONMOUSE = TRANSLUCENT_ONMOUSE;

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

	/**
	 * Get translucent on state. The states are, TRANSLUCENT_MANUAL,
	 * TRANSLUCENT_ONFOCUS, TRANSLUCENT_ONMOUSE. 
	 * 
	 * @return Returns the translucent on state.
	 */		
	public int getTranslucentOn();

	/**
	 * Set translucent on state. The states are, TRANSLUCENT_MANUAL,
	 * TRANSLUCENT_ONFOCUS, TRANSLUCENT_ONMOUSE. 
	 * 
	 * @return Returns the old translucent on state.
	 */		
	public int setTrancluentOn(int isTranslucentOn);

	/**
	 * Get marked on state. The states are, MARKED_MANUAL,
	 * MARKED_ONFOCUS, MARKED_ONMOUSE.
	 * 
	 * @return Returns the marked on state.
	 */
	public int getMarkedOn();

	/**
	 * set marked on state. The states are, MARKED_MANUAL,
	 * MARKED_ONFOCUS, MARKED_ONMOUSE.
	 * 
	 * @return Returns the old marked on state.
	 */
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

	public void addFlowListener(IFlowListener listener);
	public void removeFlowListener(IFlowListener listener);

	/* =======================================================
	 * IPanelManager interface
	 * ======================================================= */

	public boolean requestMoveTo(int x, int y, boolean isRelative);
	public boolean requestResize(int w, int h, boolean isRelative);

	public boolean requestHide();
	public boolean requestShow();

}
