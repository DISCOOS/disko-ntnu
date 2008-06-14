package org.redcross.sar.wp.messageLog;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.ICommunicatorIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageIf.MessageStatus;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.mso.util.MsoUtils;

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

		m_editAssignmentPanel.getAttribute("Time").setCaption(
				 m_wpMessageLog.getBundleText("CompletedTimeLabel.text") + ": ");
		
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
	public void updateAssignmentLineList()
	{
		MessageLineListModel model = (MessageLineListModel)m_messageLineList.getModel();
		model.setMessageLineType(MessageLineType.COMPLETED);
	}

	/**
	 * Add new started message line. If unit has started or assigned assignment this is completed, else unit
	 * assignment queue is shown, if this is empty assignment pool is shown
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
		
		if(unitHasCompletedAssignment()) {
			
			// notify reason
			Utils.showWarning("Du kan ikke registrere mer enn ett utført oppdrag per melding");
			
			// finished
			return;
			
		} 
	
		// get unit if exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		IUnitIf unit = getAvailableUnit(message);

		if(unitHasAssignedAssignment(unit) || unitHasStartedAssignment(unit)) {
		
			// get assignment status
			IAssignmentIf assigned = unit.getAssignedAssignment();
			IAssignmentIf executing = unit.getExecutingAssigment();

			// try not committed assignments?
			if(assigned==null) {
				IMessageLineIf line = getAddedLine(MessageLineType.ASSIGNED);
				assigned = (line!=null) ? line.getLineAssignment() : null;				
				
			}
			if(executing==null) {
				IMessageLineIf line = getAddedLine(MessageLineType.STARTED);
				executing = (line!=null) ? line.getLineAssignment() : null;				
			}

			// get assignment
			IAssignmentIf assignment = executing == null ? assigned : executing;
			
			// prompt user
			Object[] options = {m_wpMessageLog.getBundleText("yes.text"), m_wpMessageLog.getBundleText("no.text")};
			int n = JOptionPane.showOptionDialog(m_wpMessageLog.getApplication().getFrame(),
					String.format(m_wpMessageLog.getBundleText("UnitCompletedAssignment.text"), 
							MsoUtils.getMsoObjectName(unit,0), MsoUtils.getMsoObjectName(assignment,1)),
					m_wpMessageLog.getBundleText("UnitCompletedAssignment.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			// 
			if(n == JOptionPane.YES_OPTION)
			{
				if(executing == null)
				{
					// Adding both started and completed lines
					AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, 
							MessageLineType.STARTED, MessageLineType.COMPLETED, Calendar.getInstance(), unit, assignment);
					m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, assignment, false));
					m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, assignment, false));
				}
				else
				{
					AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, 
							MessageLineType.COMPLETED, MessageLineType.COMPLETED, Calendar.getInstance(), unit, assignment);
				}

				m_addedLines.add(message.findMessageLine(MessageLineType.COMPLETED, assignment, false));
				
				MessageLogBottomPanel.showCompletePanel();
			}
			
		}
		else if(unitHasNextAssignment(unit))
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
			Utils.showWarning("Du må først oppgi avsender. Avsender er den som har utført oppdraget og kan derfor ikke være et KO");
			MessageLogBottomPanel.showChangeFromPanel();
			return;
		}
		// success
		m_assignmentUnit = unit;
	}

	/**
	 * Add selected assignments to current message
	 */
	protected void addSelectedAssignment()
	{
		if(m_assignmentUnit!=null && m_selectedAssignment!=null)
		{

			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ASSIGNED,
					MessageLineType.COMPLETED,
					Calendar.getInstance(),
					m_assignmentUnit, m_selectedAssignment);
			
			// add to lines
			m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.COMPLETED, m_selectedAssignment, false));
			
		}

		MessageLogBottomPanel.showCompletePanel();
	}
	
    public void showComponent()
    {
    	super.showComponent();
    	
    	m_messageLinesPanel.setCaptionText("Utført oppdrag");
        
    }
	
}
