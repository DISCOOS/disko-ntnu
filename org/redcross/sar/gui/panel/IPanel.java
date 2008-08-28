package org.redcross.sar.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;

public interface IPanel extends IChangeable,
								IMsoUpdateListenerIf,
								IMsoLayerEventListener,
								IDiskoWorkListener,
								ActionListener {
	
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

	public void addDiskoWorkListener(IDiskoWorkListener listener);
	public void removeDiskoWorkListener(IDiskoWorkListener listener);
	
	public IPanelManager getManager();	
	public void setManager(IPanelManager manager);	

	/* ================================================
	 * IMsoUpdateListenerIf interface
	 * ================================================ */
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode);
    public void handleMsoUpdateEvent(MsoEvent.Update e);

	/* ================================================
	 * IMsoLayerEventListener interface
	 * ================================================ */

    public void onSelectionChanged(MsoLayerEvent e);
	
	/* ================================================
	 * IDiskoWorkListener interface
	 * ================================================ */
    
	public void onWorkPerformed(DiskoWorkEvent e);
	
	/* ================================================
	 * ActionListener interface
	 * ================================================ */
	
	public void actionPerformed(ActionEvent e);
	
}
