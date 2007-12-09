package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import javax.swing.*;

import java.util.EnumSet;

/**
 *
 */
public class DiskoWpMessageLogImpl extends AbstractDiskoWpModule implements IDiskoWpMessageLog
{
    MessageLogPanel m_logPanel;

    public DiskoWpMessageLogImpl(IDiskoRole role)
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
	        EnumSet<DiskoToolType> myTools =
	                EnumSet.of(DiskoToolType.ZOOM_IN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myTools.add(DiskoToolType.PAN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_FULL_EXTENT_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myTools.add(DiskoToolType.MAP_TOGGLE_COMMAND);
	        myTools.add(DiskoToolType.SCALE_COMMAND);
	        myTools.add(DiskoToolType.TOC_COMMAND);
	        myTools.add(DiskoToolType.GOTO_COMMAND);
	        myTools.add(DiskoToolType.SELECT_FEATURE_TOOL);
			// forward
			setupNavBar(myTools,false);
		}	
		// make map visible (is not shown in work prosess before
		// MessageLogPanel.showMap() is called)
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(false);
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
    * @see com.geodata.engine.disko.task.DiskoAp#getName()
    */
    @Override
	public String getName()
    {
        return "Sambandslogg";
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
	
	/*
	public void setMessageLinePOI(POIType type, Point point) {
		
		// initialize
		IMessageLineIf messageLine = null;
		
		// get message (create if not exist)
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);

		// get mso manager
		IMsoManagerIf manager =  MsoModelImpl.getInstance().getMsoManager();
		
		// dispatch poi type
		if(type == POIType.GENERAL)
		{
			// Update/add POI in current message
			messageLine = message.findMessageLine(MessageLineType.POSITION, true);
		}
		else
		{
			// Update finding in message
			messageLine = message.findMessageLine(MessageLineType.POI, true);
			
			// Need to add/update task
			ITaskIf task = null;
			
			String text = getText("TaskSubType.FINDING.text");
			
			// Check for existing tasks
			for(ITaskIf messageTask : message.getMessageTasksItems())
			{
				if(messageTask.getType() == TaskType.INTELLIGENCE)
				{
					// Check to see if task is a finding task
					String taskText = messageTask.getTaskText().split(":")[0];
					if(taskText.equals(text.split(":")[0]))
					{
						// Message has a intelligence task, update this
						task = messageTask;
					}
				}
			}
			
			// If message does not have a finding task, create new
			if(task == null)
			{
				task = manager.createTask(Calendar.getInstance());
				task.setCreated(Calendar.getInstance());
				task.setAlert(Calendar.getInstance());
				task.setPriority(TaskPriority.HIGH);
				task.setResponsibleRole(null);
				task.setType(TaskType.INTELLIGENCE);
				task.setSourceClass(message.getMsoClassCode());
				task.setCreatingWorkProcess(getName());
//				task.setDependentObject(message.getSender());
				message.addMessageTask(task);
			}
			
			// Update task fields
			if(type == POIType.FINDING)
			{
				task.setTaskText(String.format(text, getText("Finding.text")));
			}
			else
			{
				task.setTaskText(String.format(text, getText("SilentWitness.text")));
			}
		}
		
		IPOIIf poi = messageLine.getLinePOI();
		if(poi == null)
		{
			poi=manager.createPOI();
			messageLine.setLinePOI(poi);
		}
		
		poi.setType(type);
		try {
			poi.setPosition(MapUtil.getMsoPosistion(point));
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}	
	*/
	
}
