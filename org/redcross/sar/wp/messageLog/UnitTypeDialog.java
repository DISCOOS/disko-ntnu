package org.redcross.sar.wp.messageLog;

import no.cmr.tools.Log;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
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

import javax.swing.Icon;
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

	private BasePanel m_contentsPanel = null;

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

		m_contentsPanel = new BasePanel("Vis enheter");
		m_contentsPanel.setBodyLayout(new GridLayout(3, 2));
		m_contentsPanel.setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_NEVER, 
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		Utils.setFixedSize(m_contentsPanel,150,185);
		initButtons();

		this.setContentPane(m_contentsPanel);
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

            aircraftButton =  addButton(UnitType.AIRCRAFT);
			m_buttons.add(aircraftButton);

			boatButton = addButton(UnitType.BOAT);
			m_buttons.add(boatButton);

			dogButton = addButton(UnitType.DOG);
			m_buttons.add(dogButton);

			vehicleButton = addButton(UnitType.VEHICLE);
			m_buttons.add(vehicleButton);

			teamButton = addButton(UnitType.TEAM);
			m_buttons.add(teamButton);

			commandPostButton = addButton(UnitType.CP);
			m_buttons.add(commandPostButton);
				        
			allUnits = DiskoButtonFactory.createButton("GENERAL.ALL", ButtonSize.NORMAL);
			m_buttons.add(allUnits);
			((JPanel)m_contentsPanel.getBodyComponent()).add(allUnits);
			
		}
		catch(MissingResourceException e)
		{
			Log.error("Could not find unit properties file");
		}
	}

    private JButton addButton(UnitType unitType) throws MissingResourceException
    {
        String unitName = unitType.name();
        String unitText = DiskoButtonFactory.getText("UnitType." +unitName + ".text",null);
        String unitLetter = DiskoButtonFactory.getText("UnitType." +unitName + ".letter",null);
        Icon icon = DiskoButtonFactory.getIcon("UnitType." +unitName + ".icon","48x48",null);
        return addButton(unitText,unitLetter,icon,unitType);
    }

    private JButton addButton(String name, final String unitTypeLetter, Icon icon, UnitType unitType)
	{

    	// create button
		JButton button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
		
		// set unit type name?
		if(unitType!=null)
			button.setActionCommand(unitType.name());
		// set icon
		button.setIcon(icon);
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
		((JPanel)m_contentsPanel.getBodyComponent()).add(button);

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
