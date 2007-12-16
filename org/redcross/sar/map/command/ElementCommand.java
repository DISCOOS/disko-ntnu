package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.map.ElementDialog;
import org.redcross.sar.gui.map.TocDialog;
import org.redcross.sar.gui.map.ElementPanel.ElementEvent;
import org.redcross.sar.gui.map.ElementPanel.IElementEventListener;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;

import com.esri.arcgis.interop.AutomationException;

public class ElementCommand extends AbstractDiskoTool implements IElementEventListener {
	
	private static final long serialVersionUID = 1L; 
	
	/**
	 * Constructs the DrawTool
	 */
	public ElementCommand() throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoToolType.ELEMENT_COMMAND;		
		
		// get current application
		IDiskoApplication app = Utils.getApp();
		
		// create button
		Dimension size = app.getUIFactory().getSmallButtonSize();
		button = new JButton();
		button.setPreferredSize(size);

		// create dialog
		dialog = new ElementDialog(Utils.getApp().getFrame());
		dialog.setIsToggable(false);
		showDirect = true; // shows dialog first time onClick is invoked
		
		// add listener
		((ElementDialog)dialog).getElementPanel().addElementListener(this);
		
	}
	
	public void onCreate(Object obj) {
		
		try {
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				ElementDialog elementDialog = (ElementDialog)dialog;
				elementDialog.setLocationRelativeTo(map, DiskoDialog.POS_WEST, false);			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void onElementChange(Enum element) {
		// update toggle button
		button.setIcon(Utils.getIcon(element));
		button.setToolTipText(Utils.translate(element));
	}

	public void onElementCenterAt(ElementEvent e) { /* Not in use */ }
	public void onElementDelete(ElementEvent e)  { /* Not in use */ }
	public void onElementEdit(ElementEvent e)  { /* Not in use */ }
	public void onElementSelected(ElementEvent e)  { /* Not in use */ }
	
}
