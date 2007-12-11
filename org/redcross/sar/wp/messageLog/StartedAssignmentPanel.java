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
import org.redcross.sar.util.mso.DTG;

import java.awt.CardLayout;
import java.util.Calendar;

import javax.swing.JOptionPane;

/**
 * Panel for starting an assignment
 * See {@link AbstractAssignmentPanel} for details
 *
 * @author thomasl
 */
public class StartedAssignmentPanel extends AbstractAssignmentPanel
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param wp Message log work process
	 */
	public StartedAssignmentPanel(IDiskoWpMessageLog wp)
	{
		super(wp);

		m_timeLabel.setText(m_wpMessageLog.getText("StartedTimeLabel.text") + ": ");
	}

	/**
	 * Remove added message lines of type started. If any assigned lines were added, these are removed as well
	 */
	public void cancelUpdate()
	{
		for(IMessageLineIf line : m_addedLines)
		{
			line.deleteObject();
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

		// Perform action show in list
		MessageLogBottomPanel.showListPanel();
	}

	protected void showHasAssignment()
	{
		// Must have started message line to reach this point
		IMessageLineIf messageLine = MessageLogBottomPanel.getCurrentMessage(true).findMessageLine(MessageLineType.STARTED, false);

		String time = DTG.CalToDTG(messageLine.getOperationTime());
		if(time.isEmpty())
		{
			time = DTG.CalToDTG(Calendar.getInstance());
		}
		m_timeTextField.setText(time);

		CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
		layout.show(m_cardsPanel, EDIT_ASSIGNMENT_ID);
	}

	protected void updateAssignmentLineList()
	{
		AssignmentListModel model = (AssignmentListModel)m_assignmentLineList.getModel();
		model.setMessageLineType(MessageLineType.STARTED);
	}

	protected void addNewMessageLine()
	{
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
		IUnitIf unit = (IUnitIf)message.getSender();
		IAssignmentIf assignment = null;

		if(unitHasAssignedAssignment())
		{
			// If unit has assigned, ask if this is started
			assignment = unit.getActiveAssignment();

			// Check that unit can accept assignment
			if(!AssignmentTransferUtilities.unitCanAccept(unit, AssignmentStatus.EXECUTING))
			{
				Utils.showWarning(m_wpMessageLog.getText("CanNotStartError.header"),
						String.format(m_wpMessageLog.getText("CanNotStartError.details"), 
								MsoUtils.getMsoObjectName(unit,1), MsoUtils.getMsoObjectName(assignment,1)));
				this.hideComponent();
				return;
			}

			hideComponent();

			Object[] options = {m_wpMessageLog.getText("yes.text"), m_wpMessageLog.getText("no.text")};
			int n = JOptionPane.showOptionDialog(m_wpMessageLog.getApplication().getFrame(),
					String.format(m_wpMessageLog.getText("UnitStartedAssignment.text"), MsoUtils.getMsoObjectName(unit,1), MsoUtils.getMsoObjectName(assignment,1)),
					m_wpMessageLog.getText("UnitStartedAssignment.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if(n == JOptionPane.YES_OPTION)
			{

				// Set assignment started
				AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, MessageLineType.STARTED,
						MessageLineType.STARTED, Calendar.getInstance(), assignment);

				m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, assignment, false));

				MessageLogBottomPanel.showStartPanel();
			}
		}
		else if(unitHasNextAssignment())
		{
			// Else unit could have started from allocated buffer
			showNextAssignment();
		}
		else if(unit!=null)
		{
			// Unit could have started from assignment pool
			showAssignmentPool();
		}
		else {
			Utils.showWarning("Du må først oppgi avsender");
			this.hideComponent();
		}
	}

	protected void addSelectedAssignment()
	{
		if(m_selectedAssignment != null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ASSIGNED,
					MessageLineType.STARTED,
					Calendar.getInstance(),
					m_selectedAssignment);

			m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, m_selectedAssignment, false));
		}

		MessageLogBottomPanel.showStartPanel();
	}
}
