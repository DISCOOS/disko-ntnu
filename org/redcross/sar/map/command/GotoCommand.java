package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.GotoDialog;
import org.redcross.sar.gui.map.TocDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;

public class GotoCommand extends AbstractDiskoTool {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the DrawTool
	 */
	public GotoCommand() throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoToolType.GOTO_COMMAND;		
		
		// get current application
		IDiskoApplication app = Utils.getApp();
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

		// create dialog
		dialog = new GotoDialog(Utils.getApp().getFrame());
		dialog.setIsToggable(false);
		showDirect = true; // shows dialog first time onClick is invoked
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				GotoDialog GotoDialog = (GotoDialog)dialog;
				GotoDialog.onLoad(map);
				GotoDialog.setLocationRelativeTo(map, DiskoDialog.POS_EAST, false);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
