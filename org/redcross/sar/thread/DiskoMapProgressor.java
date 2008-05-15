package org.redcross.sar.thread;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.gui.factory.DiskoStringFactory;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IStepProgressor;

public class DiskoMapProgressor implements IStepProgressor {

	private static final long serialVersionUID = 1L;
	private static final String PROGRESS_DEFAUT_MAP_TEXT = "PROGRESS.DEFAULT.MAP.text";
	
	private JComponent map = null;
	private String message = null;
	private boolean isActive = false;
	
	public DiskoMapProgressor(JComponent map)  {
		this.map = map;
		this.message = DiskoStringFactory.getText(PROGRESS_DEFAUT_MAP_TEXT);
	}
	
	public int getMaxRange() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMinRange() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPosition() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getStepValue() throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int offsetPosition(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMaxRange(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		
	}

	public void setMinRange(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		
	}

	public void setPosition(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		
	}

	public void setStepValue(int arg0) throws IOException, AutomationException {
		// TODO Auto-generated method stub
		
	}
	
	public String getMessage() throws IOException, AutomationException {
		return message;
	}

	public void setMessage(String message) throws IOException, AutomationException {
		// prepare
		this.message = message;		
		try {
			// set note
			DiskoProgressMonitor.getInstance().setNote(message);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void show() throws IOException, AutomationException {
		try {
			// is already active?
			if(isActive) return;
			// set flag
			isActive = true;
			// set relative to map
			DiskoProgressMonitor.getInstance().setProgressLocationAt(map);
			// force to show directly
			DiskoProgressMonitor.getInstance().setMillisToPopup(0);
			// start progress
			DiskoProgressMonitor.getInstance().start(DiskoStringFactory.getText(PROGRESS_DEFAUT_MAP_TEXT));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void step() throws IOException, AutomationException {
		try {
			// is active?
			if(isActive)
				DiskoProgressMonitor.getInstance().progress(message, 0, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void hide() throws IOException, AutomationException {
		try {
			// is already inactive?
			if(!isActive) return;
			// reset flag
			isActive = false;
			// cancel progress
			DiskoProgressMonitor.getInstance().finish();
			// set relative to default frame
			DiskoProgressMonitor.getInstance().setProgressLocationAt(null);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}
