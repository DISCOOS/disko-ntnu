package org.redcross.sar.gui.mso.panel;

import no.cmr.tools.Log;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.util.SpringUtilities;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.util.Internationalization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;

/**
 * Panel for selecting unit type
 *
 * @author kennetgu
 */
public class UnitTypeInputPanel extends DefaultPanel
{
	private static final long serialVersionUID = 1L;
	
	private final JToggleButton m_dummy = new JToggleButton();
	private final ButtonGroup m_buttonGroup = new ButtonGroup();
	private final Map<UnitType,AbstractButton> m_buttons = new HashMap<UnitType, AbstractButton>();
	
	private int m_cols;
	private UnitType m_selected;
	

	/* ==========================================================
	 * Constructors
	 * ==========================================================*/
	
	public UnitTypeInputPanel()
	{
		// forward
		this("Type",3);		
	}

	public UnitTypeInputPanel(String caption, int cols)
	{
		// forward
		super(caption,false,false,ButtonSize.SMALL);
		
		// prepare
		m_cols = cols;
		
		// initialize GUI
		initialize();
		
	}
	
	/* ========================================================
	 * Public methods
	 * ======================================================== */
	
	public UnitType getType() {
		return m_selected;
	}
	
	public void setType(UnitType value) {
		if(value==null) {
			m_dummy.doClick();
		}
		else {
			// get button
			AbstractButton b = m_buttons.get(value);
			// forward
			b.doClick();
		}
		// update
		m_selected = value;
	}
	
	/* ==========================================================
	 * Helper methods
	 * ==========================================================*/
	
	private void initialize() {
		// prepare 		
		setBodyLayout(new SpringLayout());
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setNotScrollBars();
		// forward
		createButtons();
	}
	
	private void createButtons()
	{
		try
		{
            ResourceBundle bundle = Internationalization.getBundle(IUnitIf.class);
            if (bundle == null)
            {
                return;
            }

            UnitType[] types = UnitType.values();
            
            for(int i=0;i<types.length;i++) {
            	// forward
            	addButton(types[i]);
            }

            // create SELECT ALL button 
            JButton button = DiskoButtonFactory.createButton("GENERAL.ALL", ButtonSize.NORMAL);
            button.setActionCommand("SELECTED.ALL");
            
            // forward
            addButton(button,(UnitType)null);
            
            // add dummy button
            m_buttonGroup.add(m_dummy);
            
            // calculate rows
            int rows = types.length / m_cols + 1;
            
            // apply layout
            SpringUtilities.makeCompactGrid((JPanel)getBodyComponent(), rows, m_cols, 0, 0, 0, 0);
			
		}
		catch(MissingResourceException e)
		{
			Log.error("Could not find unit properties file");
		}
	}

    private void addButton(UnitType type)
    {
    	
    	// create button
    	AbstractButton button = DiskoButtonFactory.createToggleButton(type,ButtonSize.NORMAL);
    	
    	// set action command
    	button.setActionCommand(type.name());    	

    	// add to group
    	m_buttonGroup.add(button);
    	
    	// forward
    	addButton(button,type);

    }
    
    private void addButton(AbstractButton button, final UnitType type)
    {

    	// do not allow focus
        button.setFocusable(false);
    	
    	// add to map
    	m_buttons.put(type, button);
    	
		// Let the buttons manipulate the text field, setting the contents to the unit type code
		button.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				// update
				m_selected = type;
				// clear selection?
				if(m_selected==null) m_dummy.doClick();
				// forward
				fireOnWorkChange(e.getSource(),e.getActionCommand());
			}
		});

		// add button
		addBodyChild(button);

    }
    

}
