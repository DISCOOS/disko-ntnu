package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JOptionPane;

/**
 *
 */
public class DiskoWpMessageLogImpl extends AbstractDiskoWpModule implements IDiskoWpMessageLog
{
    MessageLogPanel m_logPanel;

    public DiskoWpMessageLogImpl(IDiskoRole role) throws IllegalClassFormatException
    {
        // initialize with spesific map interests
    	super(role);
        initialize();
    }

    private void initialize()
    {
        loadProperties("properties");
        assignWpBundle(IDiskoWpMessageLog.class);

        m_logPanel = new MessageLogPanel(this);
        layoutComponent(m_logPanel.getPanel());
        
        m_logPanel.setLayersSelectable();
    }
	
    @Override
	public void activated()
    {
        super.activated();

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();	  
	        myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
	        myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myButtons.add(DiskoToolType.PAN_TOOL);
	        myButtons.add(DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(DiskoCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(DiskoCommandType.SCALE_COMMAND);
	        myButtons.add(DiskoCommandType.TOC_COMMAND);
	        myButtons.add(DiskoCommandType.GOTO_COMMAND);
	        myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
			// forward
			setupNavBar(myButtons,false);
		}	
		// make map visible (is not shown in work prosess before
		// MessageLogPanel.showMap() is called)
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(true);
    }

    @Override
	public void deactivated()
    {
    	super.deactivated();
    	
		// hide map
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(false);
		
		/*
    	m_logPanel.hidePanels();
    	m_logPanel.clearSelection();

    	// Delete current message
    	MessageLogBottomPanel.clearCurrentMessage();
    	*/
    }
    	
    	
    @Override
    public boolean confirmDeactivate()
    {

		// prevent reentry
		if(isWorking()) {
			// notify
			Utils.showWarning(getText("Working.header"), getText("Working.text"));
			// do not allow to deactivate
			return false;
		}
		
		// validate data
		if(MessageLogBottomPanel.isMessageDirty()) {
							
			// prompt user
			String[] options = {getText("DirtyMessageWarning.commit"),
					getText("DirtyMessageWarning.rollback"),getText("DirtyMessageWarning.cancel")};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
						getText("DirtyMessageWarning.text"),
						getText("DirtyMessageWarning.header"), JOptionPane.YES_NO_CANCEL_OPTION, 
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
			
			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// forward
				return MessageLogBottomPanel.apply(true);
			case JOptionPane.NO_OPTION:
				// forward
				MessageLogBottomPanel.clearCurrentMessage();						
				// allow deactive
				return true;
			case JOptionPane.CANCEL_OPTION:
				// do not deactivate
				return false;
			}						
		}		
		
		// allow deactivate
		return true;

    }

    /* (non-Javadoc)
    * @see com.geodata.engine.disko.task.DiskoAp#getCaption()
    */
    @Override
	public String getCaption()
    {
        return getText("Caption");
    }

    /* (non-Javadoc)
     * @see com.geodata.engine.disko.task.DiskoAp#cancel()
     */
    public void cancel()
    {
    }

    /* (non-Javadoc)
     * @see com.geodata.engine.disko.task.DiskoAp#finish()
     */
    public void finish()
    {
    }

	/**
	 * Adds or updates the message poi line and generates 
	 * or update the assosiated task
	 * @param type	The poi type
	 * @param point The position
	 */
	
}
