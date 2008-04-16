package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.GotoDialog;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;

public class GotoCommand extends AbstractDiskoCommand {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the DrawTool
	 */
	public GotoCommand() throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoCommandType.GOTO_COMMAND;		
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

		// shows dialog first time onClick is invoked
		showDirect = true; 
		
		// create dialog
		dialog = new GotoDialog(Utils.getApp().getFrame());
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				GotoDialog gotoDialog = (GotoDialog)dialog;
				gotoDialog.onLoad((IDiskoMap)obj);
				gotoDialog.setLocationRelativeTo((JComponent)obj, DiskoDialog.POS_EAST, false);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
}
