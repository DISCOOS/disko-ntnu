package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ScaleDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
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
		type = MapCommandType.SCALE_COMMAND;

		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		button.setFocusable(false);

		// shows dialog first time onClick is invoked
		showDirect = true;

		// create dialog
		dialog = new ScaleDialog(Application.getInstance());

	}

	public void onCreate(Object obj) {
		try {
			if (obj instanceof IDiskoMap) {
				ScaleDialog scaleDialog = (ScaleDialog)dialog;
				scaleDialog.onLoad((IDiskoMap)obj);
				scaleDialog.setSnapToLocation((JComponent)obj, DefaultDialog.POS_EAST, 0, true, false);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
