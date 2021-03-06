package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ElementDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.ElementPanel.ElementEvent;
import org.redcross.sar.gui.panel.ElementPanel.IElementEventListener;

import com.esri.arcgis.interop.AutomationException;

public class ElementCommand extends AbstractDiskoCommand implements IElementEventListener {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the DrawTool
	 */
	public ElementCommand() throws IOException, AutomationException {

		// forward
		super();

		// set tool type
		type = MapCommandType.ELEMENT_COMMAND;

		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		button.setFocusable(false);

		// create dialog
		dialog = new ElementDialog(Application.getFrameInstance());
		showDirect = true; // shows dialog first time onClick is invoked

		// add listener
		((ElementDialog)dialog).getElementPanel().addElementListener(this);

	}

	public void onCreate(Object obj) {
		// NOP
		try {
			if (obj instanceof JComponent) {
				ElementDialog elementDialog = (ElementDialog)dialog;
				elementDialog.setSnapToLocation((JComponent)obj, DefaultDialog.POS_EAST, 0, true, false);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onElementChange(Enum<?> element) {
		// update toggle button
		button.setIcon(DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(element),"48x48"));
		button.setToolTipText(DiskoEnumFactory.getTooltip(element));
	}

	public void onElementCenterAt(ElementEvent e) { /* Not in use */ }
	public void onElementDelete(ElementEvent e)  { /* Not in use */ }
	public void onElementEdit(ElementEvent e)  { /* Not in use */ }
	public void onElementSelected(ElementEvent e)  { /* Not in use */ }

}
