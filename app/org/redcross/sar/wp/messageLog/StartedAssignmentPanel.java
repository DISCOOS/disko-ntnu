package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IMessageLineIf;
import org.redcross.sar.mso.data.IMessageLineIf.MessageLineType;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.AssignmentUtilities;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
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

		// forward
		super(wp);

		// prepare
		m_editAssignmentPanel.getField("Time").setCaptionText(
				 m_aWp.getBundleText("StartedTimeLabel.text") + ": ");

	}

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

		String time = DTG.CalToDTG(messageLine.getLineTime());
		if(time.isEmpty())
		{
			time = DTG.CalToDTG(Calendar.getInstance());
		}
		m_editAssignmentPanel.setValue("Time",time);

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
			Utils.showWarning(m_aWp.getBundleText("MessageTaskOperationError.header"),
					m_aWp.getBundleText("MessageTaskOperationError.details"));

			// finished
			return;

		}

		// get unit if exists
		IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
		IUnitIf unit = getAvailableUnit(message);

		if(unitHasStartedAssignment(unit)) {

			// notify reason
			Utils.showWarning(String.format("%s kan ikke utf�re mer enn ett oppdrag om gangen", MsoUtils.getUnitName(unit, false)));

			// finished
			return;

		}

		if(unitHasAllocatedAssignment(unit))
		{
			// If unit has Allocated, ask if this is started
			IAssignmentIf assignment = unit.getAllocatedAssignment();

			// try not committed assignments?
			if(assignment==null) {
				IMessageLineIf line = getAddedLine(MessageLineType.ALLOCATED);
				assignment = (line!=null) ? line.getLineAssignment() : null;
			}

			Object[] options = {m_aWp.getBundleText("yes.text"), m_aWp.getBundleText("no.text")};
			int n = JOptionPane.showOptionDialog(m_aWp.getApplication().getFrame(),
					String.format(m_aWp.getBundleText("UnitStartedAssignment.text"), MsoUtils.getMsoObjectName(unit,0), MsoUtils.getMsoObjectName(assignment,1)),
					m_aWp.getBundleText("UnitStartedAssignment.header"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[0]);

			if(n == JOptionPane.YES_OPTION)
			{

				// Set assignment started
				AssignmentUtilities.createAssignmentChangeMessageLines(message, MessageLineType.STARTED,
						MessageLineType.STARTED, Calendar.getInstance(), unit, assignment);

				m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, assignment, false));

				MessageLogBottomPanel.showStartPanel();
			}
		}
		else if(unitEnqueuedAssignment(unit))
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
			Utils.showWarning("Du m� f�rst oppgi en lovlig avsender. Avsender er den som har utf�rt oppdraget og kan derfor ikke v�re et KO");
			MessageLogBottomPanel.showFromPanel();
			return;
		}
		// success
		m_owningUnit = unit;

	}

	protected void addSelectedAssignment()
	{
		if(m_owningUnit!=null && m_selectedAssignment!=null)
		{
			IMessageIf message = MessageLogBottomPanel.getCurrentMessage(true);

			AssignmentUtilities.createAssignmentChangeMessageLines(message,
					MessageLineType.ALLOCATED, MessageLineType.STARTED,
					Calendar.getInstance(), m_owningUnit, m_selectedAssignment);

			// add to lines
			m_addedLines.add(message.findMessageLine(MessageLineType.ALLOCATED, m_selectedAssignment, false));
			m_addedLines.add(message.findMessageLine(MessageLineType.STARTED, m_selectedAssignment, false));

		}

		MessageLogBottomPanel.showStartPanel();

	}

    public void showEditor()
    {
    	super.showEditor();

    	m_messageLinesPanel.setCaptionText("Startet oppdrag");

    }

}
