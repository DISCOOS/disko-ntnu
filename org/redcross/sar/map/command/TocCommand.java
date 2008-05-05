package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.TocDialog;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;

public class TocCommand extends AbstractDiskoCommand {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the command
	 */
	public TocCommand() throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoCommandType.TOC_COMMAND;		
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

		// shows dialog first time onClick is invoked
		showDirect = true;
		
		// create dialog
		dialog = new TocDialog(Utils.getApp());
		
	}
	
	public void onCreate(Object obj) {		
		try {
			if (obj instanceof IDiskoMap) {
				TocDialog tocDialog = (TocDialog)dialog;
				tocDialog.onLoad((IDiskoMap)obj);
				tocDialog.setLocationRelativeTo((JComponent)obj, DiskoDialog.POS_EAST, true, true);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// forward
		super.onCreate(obj);		
	}	
}
