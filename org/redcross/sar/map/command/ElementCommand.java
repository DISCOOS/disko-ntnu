package org.redcross.sar.map.command;

import java.io.IOException;

import javax.swing.JComponent;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.ElementPanel.ElementEvent;
import org.redcross.sar.gui.map.ElementPanel.IElementEventListener;

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
		type = DiskoCommandType.ELEMENT_COMMAND;		
		
		// create button
		button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);

		// create dialog
		dialog = new ElementDialog(Utils.getApp().getFrame());
		dialog.setIsToggable(false);
		showDirect = true; // shows dialog first time onClick is invoked
		
		// add listener
		((ElementDialog)dialog).getElementPanel().addElementListener(this);
		
	}
	
	public void onCreate(Object obj) {
		// NOP
		try {
			if (obj instanceof JComponent) {
				ElementDialog elementDialog = (ElementDialog)dialog;
				elementDialog.setLocationRelativeTo((JComponent)obj, DiskoDialog.POS_EAST, false);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void onElementChange(Enum element) {
		// update toggle button
		button.setIcon(Utils.getIcon(element,"48x48"));
		button.setToolTipText(Utils.translate(element));
	}

	public void onElementCenterAt(ElementEvent e) { /* Not in use */ }
	public void onElementDelete(ElementEvent e)  { /* Not in use */ }
	public void onElementEdit(ElementEvent e)  { /* Not in use */ }
	public void onElementSelected(ElementEvent e)  { /* Not in use */ }
	
}
