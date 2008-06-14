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

		m_editAssignmentPanel.getAttribute("Time").setCaption(
				 m_wpMessageLog.getBundleText("StartedTimeLabel.text") + ": ");

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
		m_editAssignmentPanel.getAttribute("Time").setValue(time);

		CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
		layout.show(m_cardsPanel, EDIT_ASSIGNMENT_ID);
	}

	public void updateAssignmentLineList()
	{
		MessageLineListModel model = (MessageLineListModel)m_messageLineList.getModel();
		model.setMessageLineType(MessageLineType.STARTED);
	}

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
				
		if(unitHasStartedAssignment(unit)) {
			
			// notify reason
			Utils.showWarning(String.format("%s kan ikke utføre mer enn ett oppdrag om gangen", MsoUtils.getUnitName(unit, false)));
			
			// finished
			return;
			
		}
		
		if(unitHasAssignedAssignment(unit))
		{
			// If unit has assigned, ask if this is started
			IAssignmentIf assignment = unit.getAssignedAssignment();

			// try not committed assignments?
			if(assignment==null) {
				IMessageLineIf line = getAddedLine(MessageLineType.ASSIGNED);
				assignment = (line!=null) ? line.getLineAssignment() : null;			
			}
			
			Object[] options = {m_wpMessageLog.getBundleText("yes.text"), m_wpMessageLog.getBundleText("no.text")};
			int n = JOptionPane.showOptionDialog(m_wpMessageLog.getApplication().getFrame(),
					String.format(m_wpMessageLog.getBundleText("UnitStartedAssignment.text"), MsoUtils.getMsoObjectName(unit,0), MsoUtils.getMsoObjectName(assignment,1)),
					m_wpMessageLog.getBundleText("UnitStartedAssignment.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if(n == JOptionPane.YES_OPTION)
			{

				// Set assignment started
				AssignmentTransferUtilities.createAssignmentChangeMessageLines(message, MessageLineType.STARTED,
						MessageLineType.STARTED, Calendar.getInstance(), unit, assignment);

				m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, assignment, false));

				MessageLogBottomPanel.showStartPanel();
			}
		}
		else if(unitHasNextAssignment(unit))
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
			Utils.showWarning("Du må først oppgi en lovlig avsender. Avsender er den som har utført oppdraget og kan derfor ikke være et KO");
			MessageLogBottomPanel.showChangeFromPanel();
			return;
		}
		// success
		m_assignmentUnit = unit;

	}

	protected void addSelectedAssignment()
	{
		if(m_assignmentUnit!=null && m_selectedAssignment!=null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);
			
			AssignmentTransferUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ASSIGNED, MessageLineType.STARTED,
					Calendar.getInstance(), m_assignmentUnit, m_selectedAssignment);
			
			// add to lines
			m_addedLines.add(message.findMessageLine(MessageLineType.ASSIGNED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, m_selectedAssignment, false));
			
		}

		MessageLogBottomPanel.showStartPanel();
	}
	
    public void showComponent()
    {
    	super.showComponent();
    	
    	m_messageLinesPanel.setCaptionText("Startet oppdrag");
        
    }
	
}
