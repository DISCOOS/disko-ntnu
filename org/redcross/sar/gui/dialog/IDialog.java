package org.redcross.sar.gui.dialog;

import java.awt.Component;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.panel.IPanelManager;
import org.redcross.sar.mso.data.IMsoObjectIf;

public interface IDialog extends IChangeable, IPanelManager {

	/* =======================================================
	 * IDialog interface
	 * ======================================================= */
	
	public void snapTo();

	public boolean isMoveable();
	public void setMoveable(boolean isMoveable);
	
	public void cancelSetVisible();
	public void delayedSetVisible(boolean isVisible, int millisToShow); 	
	
	public void setLocationRelativeTo(Component buddy, int policy, boolean sizeToFit, boolean snapToInside);
		
	public boolean isWorkSupported();
	
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
	
	public void addDiskoWorkListener(IDiskoWorkListener listener);	
	public void removeDiskoWorkListener(IDiskoWorkListener listener);
	
	/* =======================================================
	 * IPanelManager interface
	 * ======================================================= */
	
	public boolean requestMoveTo(int x, int y);
	public boolean requestResize(int w, int h);
	
	public boolean requestHide();
	public boolean requestShow();

}
