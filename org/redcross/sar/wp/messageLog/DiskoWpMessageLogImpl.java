package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 *
 */
public class DiskoWpMessageLogImpl extends AbstractDiskoWpModule implements IDiskoWpMessageLog
{
    MessageLogPanel m_logPanel;

    public DiskoWpMessageLogImpl() throws IllegalClassFormatException
    {
        // initialize with spesific map interests
    	super();
    	
    	// intialize GUI
        initialize();
    }

    private void initialize()
    {        
        // load class resource bundle
        assignWpBundle(IDiskoWpMessageLog.class);

        // install map
        installMap();
        
        // get message log panel. This panel implements the gui
        m_logPanel = new MessageLogPanel(this);
        
        // add the panel as main wp component
        layoutComponent(m_logPanel.getPanel());
        
        // ensure tha wp spesific layers are selectable 
        m_logPanel.setLayersSelectable();
        
		// install draw support in map 
		getMap().installEditSupport();        
		
		// hide map
		getMap().setVisible(false);
        
    }
	
    @Override
	public void activate(IDiskoRole role) {
		
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
		if(MessageLogPanel.isMapShown()) 
			MessageLogPanel.showMap();
		else
			MessageLogPanel.hideMap();
		// forward
		super.activate(role);

    }    	
    	
    @Override
    public boolean confirmDeactivate()
    {

		// prevent reentry
		if(isWorking()) {
			// notify
			Utils.showWarning(getBundleText("Working.header"), getBundleText("Working.text"));
			// do not allow to deactivate
			return false;
		}
		
		// validate data
		if(MessageLogBottomPanel.isMessageDirty()) {
							
			// prompt user
			String[] options = {getBundleText("DirtyMessageWarning.commit"),
					getBundleText("DirtyMessageWarning.rollback"),getBundleText("DirtyMessageWarning.cancel")};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
						getBundleText("DirtyMessageWarning.text"),
						getBundleText("DirtyMessageWarning.header"), JOptionPane.YES_NO_CANCEL_OPTION, 
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
    
	public void deactivate() {
		// hide map
		getMap().setVisible(false);
		// forward
		super.deactivate();
	}
    

    /* (non-Javadoc)
    * @see com.geodata.engine.disko.task.DiskoAp#getCaption()
    */
    @Override
	public String getCaption()
    {
        return getBundleText("MESSAGELOG");
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
