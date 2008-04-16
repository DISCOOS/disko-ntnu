package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.ScaleDialog;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;

public class ScaleCommand extends AbstractDiskoCommand {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the DrawTool
	 */
	public ScaleCommand() throws IOException, AutomationException {
		
		// forward
		super();
		
		// set tool type
		type = DiskoCommandType.SCALE_COMMAND;		

		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

		// shows dialog first time onClick is invoked
		showDirect = true; 
		
		// create dialog
		dialog = new ScaleDialog(Utils.getApp(), this);
		
	}
	
	public void onCreate(Object obj) {
		try {
			if (obj instanceof IDiskoMap) {
				ScaleDialog scaleDialog = (ScaleDialog)dialog;
				scaleDialog.onLoad((IDiskoMap)obj);
				scaleDialog.setLocationRelativeTo((JComponent)obj, DiskoDialog.POS_EAST, true);			
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}	
}
