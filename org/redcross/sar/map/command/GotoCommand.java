package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.GotoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.util.Utils;

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
		type = MapCommandType.GOTO_COMMAND;		
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		button.setFocusable(false);

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
				gotoDialog.setLocationRelativeTo((JComponent)obj, DefaultDialog.POS_EAST, false, true);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick() {
		// prepare?
		if(!dialog.isVisible()) {
			// update on center of map
			((GotoDialog)dialog).getPoint();
			// activate SelectFeatureTool
			Utils.getApp().invoke(MapToolType.SELECT_TOOL,false);
		}
		// forward
		super.onClick();
	}	
	
}
