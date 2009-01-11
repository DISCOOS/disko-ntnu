package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.TocDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.util.Utils;

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
		type = MapCommandType.TOC_COMMAND;

		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		button.setFocusable(false);

		// shows dialog first time onClick is invoked
		showDirect = true;

		// create dialog
		dialog = new TocDialog(Utils.getApp().getFrame());

	}

	public void onCreate(Object obj) {
		try {
			if (obj instanceof IDiskoMap) {
				TocDialog tocDialog = (TocDialog)dialog;
				tocDialog.onLoad((IDiskoMap)obj);
				tocDialog.setSnapToLocation((JComponent)obj, DefaultDialog.POS_EAST, 0, true, false);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// forward
		super.onCreate(obj);
	}

	@Override
	public void onClick() {
		// forward
		((TocDialog)dialog).reload();
		// forward
		super.onClick();
	}


}
