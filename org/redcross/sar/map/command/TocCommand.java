package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.TocDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;

import com.esri.arcgis.interop.AutomationException;

public class TocCommand extends AbstractDiskoTool {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the DrawTool
	 */
	public TocCommand() throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoToolType.TOC_COMMAND;		
		
		// get current application
		IDiskoApplication app = Utils.getApp();
		
		// create button
		Dimension size = app.getUIFactory().getSmallButtonSize();
		button = new JButton();
		button.setPreferredSize(size);

		// create dialog
		dialog = new TocDialog(Utils.getApp());
		dialog.setIsToggable(false);
		showDirect = true; // shows dialog first time onClick is invoked
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				TocDialog tocDialog = (TocDialog)dialog;
				tocDialog.onLoad(map);
				tocDialog.setLocationRelativeTo(map, DiskoDialog.POS_EAST, true);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
