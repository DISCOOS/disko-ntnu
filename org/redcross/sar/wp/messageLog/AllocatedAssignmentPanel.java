package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;

import java.util.Calendar;

/**
 * Dialog for assigning unit an assignment
 *
 * @author thomasl
 *
 */
public class AllocatedAssignmentPanel extends AbstractAssignmentPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param wp Message log work process
	 */
	public AllocatedAssignmentPanel(IDiskoWpMessageLog wp)
	{
		super(wp);

		 m_editAssignmentPanel.getAttribute("Time").setCaptionText(
				 m_wpMessageLog.getBundleText("AllocatedTimeLabel.text") + ": ");
		 
	}

	/**
	 *
	 */
	@Override
	protected void updateMessageLine()
	{
		super.updateMessageLine();
		super.showEditor();
	}

	/**
	 * Sets the line type to {@link MessageLineType#Allocated} in list mode, which in turn causes list to update.
	 */
	public void updateAssignmentLineList()
	{
		MessageLineListModel model = (MessageLineListModel)m_messageLineList.getModel();
		model.setMessageLineType(MessageLineType.ALLOCATED);
	}

	/**
	 * Adds a new Allocated message line to message. If unit has assignments in buffer these are shown,
	 * else all available assignments are shown
	 */
	protected void addNewMessageLine()
	{
				
		// violation?
		if(!MessageLogBottomPanel.isNewMessage()) {
			
			// notify reason
			Utils.showWarning(m_wpMessageLog.getBundleText("MessageTaskOperationError.header"),
					m_wpMessageLog.getBundleText("MessageTaskOperationError.details"));
			
			// finished
			return;
			
		}
		
		// get unit if exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		IUnitIf unit = getAvailableUnit(message);
				
		// violation?
		if(unitHasAllocatedAssignment(unit) || unitHasStartedAssignment(unit)) {
		
			// notify reason
			Utils.showWarning(String.format("%s kan ikke tildeles mer enn ett oppdrag om gangen", MsoUtils.getUnitName(unit, false)));
			
			// finished
			return;
			
		}
		
		if(unitEnqueuedAssignment(unit))
		{
			// If unit has next in assignment buffer, let user choose from these
			showNextAssignment();
		}
		else if(unit!=null)
		{
			// Unit could have started from assignment pool
			showAssignmentPool();
		}
		else {
			Utils.showWarning("Du må først oppgi lovlig mottaker. Mottaker er den som skal utføre oppdraget og kan derfor ikke være et KO");
			MessageLogBottomPanel.showToPanel();
			return;

		}
		// success
		m_assignmentUnit = unit;
	}

	/**
	 * Adds the selected assignment as an assign message line in the current message
	 */
	protected void addSelectedAssignment()
	{
		if(m_assignmentUnit!=null && m_selectedAssignment!=null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ALLOCATED, MessageLineType.ALLOCATED,
					Calendar.getInstance(), m_assignmentUnit, m_selectedAssignment);
			
			// add to lines
			m_addedLines.add(message.findMessageLine(MessageLineType.ALLOCATED, m_selectedAssignment, false));
			
		}
		
		MessageLogBottomPanel.showAssignPanel();
	}
	
    public void showEditor()
    {
    	super.showEditor();
    	
    	m_messageLinesPanel.setCaptionText("Tildelt oppdrag");
        
    }
    
	
}
