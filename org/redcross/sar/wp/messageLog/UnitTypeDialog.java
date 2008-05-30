package org.redcross.sar.wp.messageLog;

import no.cmr.tools.Log;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.util.Internationalization;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Collection;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

/**
 * Dialog for selecting unit type
 *
 * @author thomasl
 */
public class UnitTypeDialog extends DefaultDialog implements IEditMessageComponentIf
{
	private static final long serialVersionUID = 1L;

	private JPanel m_contentsPanel = null;

	private JFormattedTextField m_textField = null;
	private LinkedList<JButton> m_buttons;

	/**
	 * @param wp Message log work process
	 * @param textField Text field displaying communicator number prefix
	 */
	public UnitTypeDialog(IDiskoWpMessageLog wp, JFormattedTextField textField)
	{
		super(wp.getApplication().getFrame());

		m_textField = textField;

		m_contentsPanel = new JPanel(new GridLayout(3, 2));

		initButtons();

		this.add(m_contentsPanel);
		this.pack();
	}

	/**
	 *
	 */
	private void initButtons()
	{
		m_buttons = new LinkedList<JButton>();
        JButton aircraftButton = null;
        JButton boatButton = null;
        JButton dogButton = null;
        JButton vehicleButton = null;
        JButton teamButton = null;
        JButton commandPostButton = null;
        JButton allUnits = null;

		try
		{
            ResourceBundle bundle = Internationalization.getBundle(IUnitIf.class);
            if (bundle == null)
            {
                return;
            }

            aircraftButton = addButton(bundle,UnitType.AIRCRAFT);
			m_buttons.add(aircraftButton);

			boatButton = addButton(bundle, UnitType.BOAT);
			m_buttons.add(boatButton);

			dogButton = addButton(bundle, UnitType.DOG);
			m_buttons.add(dogButton);

			vehicleButton = addButton(bundle, UnitType.VEHICLE);
			m_buttons.add(vehicleButton);

			teamButton = addButton(bundle, UnitType.TEAM);
			m_buttons.add(teamButton);

			commandPostButton = addButton(bundle, UnitType.CP);
			m_buttons.add(commandPostButton);
				        
			allUnits = addButton("GENERAL.ALL",null,null,null);
			m_buttons.add(allUnits);
			
		}
		catch(MissingResourceException e)
		{
			Log.error("Could not find unit properties file");
		}
	}

    private JButton addButton(ResourceBundle bundle, UnitType unitType) throws MissingResourceException
    {
        String unitName = unitType.name();
        String unitText = bundle.getString("UnitType." +unitName + ".text");
        String unitLetter = bundle.getString("UnitType." +unitName + ".letter");
        String unitIcon = bundle.getString("UnitType." +unitName + ".icon");
        return addButton(unitText,unitLetter,unitIcon,unitType);
    }

    private JButton addButton(String name, final String unitTypeLetter, String icon, UnitType unitType)
	{

    	// create button
		JButton button = DiskoButtonFactory.createButton(name,ButtonSize.NORMAL);
		
		// set unit type name?
		if(unitType!=null)
			button.setActionCommand(unitType.name());
		// set icon?		
		if(button.getIcon()==null && icon!=null && icon.length()!=0)
			button.setIcon(DiskoIconFactory.getIcon(icon,"48x48"));
		// set text?		
		if(button.getIcon()==null && button.getText()==null)
			button.setText(name);
		// set tootip text?
		if(button.getToolTipText()==null)
			button.setToolTipText(name);

		// Let the buttons manipulate the text field, setting the contents to the unit type code
		button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				if(m_textField != null){
					m_textField.setText(unitTypeLetter);
				}
				else{
					Log.error(" UnitTypeDialog.addButton: Text-field not set");
				}
			}
		});

		// add button
		m_contentsPanel.add(button);

		// return new button
		return button;
		
	}

	public Collection<JButton> getButtons()
	{
		return m_buttons;
	}

	/**
	 *
	 */
	public void clearContents()
	{
	}

	/**
	 * {
	 */
	public void hideComponent()
	{
		this.setVisible(false);
	}

	/**
	 *
	 */
	public void newMessageSelected(IMessageIf message)
	{
	}

	/**
	 *
	 */
	public void showComponent()
	{
		this.setVisible(true);
	}

}
