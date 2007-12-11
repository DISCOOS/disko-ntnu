package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.AssignmentTransferUtilities;

import java.util.Calendar;

import javax.swing.JOptionPane;

/**
 * Dialog for setting assignment to complete
 * See {@link AbstractAssignmentPanel} for details
 *
 * @author thomasl
 */
public class CompletedAssignmentPanel extends AbstractAssignmentPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param wp Message log work process
	 */
	public CompletedAssignmentPanel(IDiskoWpMessageLog wp)
	{
		super(wp);

		m_timeLabel.setText(m_wpMessageLog.getText("CompletedTimeLabel.text") + ": ");
	}

	/**
	 * Removes completed lines added to message since last commit.
	 * If assign and/or started lines were added, these will be removed as well
	 */
	public void cancelUpdate()
	{
		if(linesAdded())
		{
			// Remove added lines
			for(IMessageLineIf line : m_addedLines)
			{
				line.deleteObject();
				m_addedLines.remove(line);
			}

			// Cancel assign and started message lines if added
			MessageLogBottomPanel.cancelAssign();
			MessageLogBottomPanel.cancelStarted();
		}
	}

	/**
	 *
	 */
	@Override
	protected void updateMessageLine()
	{
		super.updateMessageLine();

		// Perform action show in list
		MessageLogBottomPanel.showListPanel();
	}

	/**
	 * Sets message line type to complete in message line list model, list is updated accordingly
	 */
	protected void updateAssignmentLineList()
	{
		AssignmentListModel model = (AssignmentListModel)m_assignmentLineList.getModel();
		model.setMessageLineType(MessageLineType.COMPLETE);
	}

	/**
	 * Add new started message line. If unit has started or assigned assignment this is completed, else unit
	 * assignment queue is shown, if this is empty assignment pool is shown
	 */
	protected void addNewMessageLine()
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		IUnitIf unit = (IUnitIf)message.getSender();
		IAssignmentIf assignedAssignment = unit.getAssignedAssignment();
		IAssignmentIf executingAssignment = unit.getExecutingAssigment();
		IAssignmentIf assignment = executingAssignment == null ? assignedAssignment : executingAssignment;


		if(assignment != null)
		{
			this.hideComponent();

			// If unit has assigned or started assignment, ask user if this is completed
			if(!AssignmentTransferUtilities.unitCanAccept(unit, AssignmentStatus.FINISHED))
			{
				Utils.showWarning(m_wpMessageLog.getText("CanNotCompleteError.header"),
						String.format(m_wpMessageLog.getText("CanNotCompleteError.details"), 
								MsoUtils.getMsoObjectName(unit,1), MsoUtils.getMsoObjectName(assignment,1)));
				this.hideComponent();
				return;
			}

			Object[] options = {m_wpMessageLog.getText("yes.text"), m_wpMessageLog.getText("no.text")};
			int n = JOptionPane.showOptionDialog(m_wpMessageLog.getApplication().getFrame(),
					String.format(m_wpMessageLog.getText("UnitCompletedAssignment.text"), 
							MsoUtils.getMsoObjectName(unit,1), MsoUtils.getMsoObjectName(assignment,1)),
					m_wpMessageLog.getText("UnitCompletedAssignment.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if(n == JOptionPane.YES_OPTION)
			{
				if(executingAssignment == null)
				{
					// Adding both started and completed lines
					AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, MessageLineType.STARTED, MessageLineType.COMPLETE,
							Calendar.getInstance(), assignment);
					m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, assignment, false));
				}
				else
				{
					AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, MessageLineType.COMPLETE, MessageLineType.COMPLETE,
							Calendar.getInstance(), assignment);
				}

				m_addedLines.add(message.findMessageLine(MessageLineType.COMPLETE, assignment, false));

				MessageLogBottomPanel.showCompletePanel();
			}
		}
		else if(unitHasNextAssignment())
		{
			// Else unit may have completed allocated assignment
			showNextAssignment();
		}
		else if(unit!=null)
		{
			// Unit may have completed assignment in assignment pool
			showAssignmentPool();
		}
		else {
			Utils.showWarning("Du må først oppgi avsender");
			this.hideComponent();
		}
	}

	/**
	 * Add completed assignment line to current message, sets assignment to the previously selected assignment
	 */
	protected void addSelectedAssignment()
	{
		if(m_selectedAssignment != null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ASSIGNED,
					MessageLineType.COMPLETE,
					Calendar.getInstance(),
					m_selectedAssignment);

			m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.COMPLETE, m_selectedAssignment, false));
		}

		MessageLogBottomPanel.showCompletePanel();
	}
}
