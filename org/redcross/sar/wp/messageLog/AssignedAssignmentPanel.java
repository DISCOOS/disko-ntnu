package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;

import java.util.Calendar;

/**
 * Dialog for assigning unit an assignment
 *
 * @author thomasl
 *
 */
public class AssignedAssignmentPanel extends AbstractAssignmentPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param wp Message log work process
	 */
	public AssignedAssignmentPanel(IDiskoWpMessageLog wp)
	{
		super(wp);

		 m_editAssignmentPanel.getAttribute("Time").setCaption(
				 m_wpMessageLog.getBundleText("AssignedTimeLabel.text") + ": ");
	}

	/**
	 * Deletes all message lines that was added since last commit
	 */
	public void cancelUpdate()
	{
		// Remove all added lines
		for(int i=0; i<m_addedLines.size(); i++)
		{
			m_addedLines.get(i).deleteObject();
		}

		m_addedLines.clear();
	}


	/**
	 *
	 */
	@Override
	protected void updateMessageLine()
	{
		super.updateMessageLine();
		MessageLogBottomPanel.showListPanel();
	}

	/**
	 * Sets the line type to {@link MessageLineType#ASSIGNED} in list mode, which in turn causes list to update.
	 */
	protected void updateAssignmentLineList()
	{
		MessageLineListModel model = (MessageLineListModel)m_messageLineList.getModel();
		model.setMessageLineType(MessageLineType.ASSIGNED);
	}

	/**
	 * Adds a new assigned message line to message. If unit has assignments in buffer these are shown,
	 * else all available assignments are shown
	 */
	protected void addNewMessageLine()
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		IUnitIf unit = (IUnitIf)message.getSingleReceiver();

		// Assure that unit can accept assignment
		if(AssignmentTransferUtilities.unitCanAccept(unit, AssignmentStatus.ASSIGNED))
		{
			if(unitHasNextAssignment())
			{
				// If unit has next in assignment buffer, let user choose from these
				showNextAssignment();
			}
			else
			{
				// Else get assignments from assignment pool
				showAssignmentPool();
			}
		}
		else
		{
			Utils.showWarning(m_wpMessageLog.getBundleText("CanNotAssignError.header"),
					String.format(m_wpMessageLog.getBundleText("CanNotAssignError.details"), 
							unit.getTypeAndNumber(), ""));
		}
	}

	/**
	 * Adds the selected assignment as an assign message line in the current message
	 */
	protected void addSelectedAssignment()
	{
		if(m_selectedAssignment != null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(
					message,
					MessageLineType.ASSIGNED,
					MessageLineType.ASSIGNED,
					Calendar.getInstance(),
					m_selectedAssignment);

			m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, m_selectedAssignment, false));
		}

		MessageLogBottomPanel.showAssignPanel();
	}
}
