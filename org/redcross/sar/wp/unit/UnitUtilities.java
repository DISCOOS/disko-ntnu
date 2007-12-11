package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;

import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Handles logic in units
 *
 * @author thomasl
 */
public class UnitUtilities
{
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	/**
	 * Toggles pause status for given unit
	 * @param unit
	 * @throws IllegalOperationException Thrown if pause status can't be toggled (i.e. unit is initializing)
	 */
	public static void toggleUnitPause(IUnitIf unit) throws IllegalOperationException
	{
		if(unit.getStatus() == UnitStatus.INITIALIZING || unit.getStatus() == UnitStatus.RELEASED)
		{
			throw new IllegalOperationException();
		}

		// Toggle pause
		if(unit.getStatus() == UnitStatus.PAUSED)
		{
			unit.setStatus(UnitStatus.WORKING);
		}
		else
		{
			unit.setStatus(UnitStatus.PAUSED);
		}
	}

	/**
	 * Releases a unit
	 * @param unit The unit
	 * @throws IllegalOperationException Thrown if unit can not be released
	 */
	public static void releaseUnit(IUnitIf unit) throws IllegalOperationException
	{
		if(unit.getStatus() != UnitStatus.RELEASED)
		{
			IAssignmentIf activeAssignment = unit.getActiveAssignment();

			// Get user confirmation
			boolean releaseUnit;
			if(activeAssignment != null)
			{
				String[] options = {m_resources.getString("Yes.text"), m_resources.getString("No.text")};
				releaseUnit = JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
						m_resources.getString("ReleaseUnitWithAssignment.text"),
						m_resources.getString("ReleaseUnitWithAssignment.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
			}
			else
			{
				String[] options = {m_resources.getString("Yes.text"), m_resources.getString("No.text")};
				releaseUnit = JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
						m_resources.getString("ReleaseUnit.text"),
						m_resources.getString("ReleaseUnit.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);
			}

			// Release unit
			if(releaseUnit)
			{
				// Abort assignment
				if(activeAssignment != null)
				{
					activeAssignment.setStatus(AssignmentStatus.ABORTED);
				}

				// Release personnel, keep references for history
				for(IPersonnelIf personnel : unit.getUnitPersonnelItems())
				{
					personnel.setStatus(PersonnelStatus.RELEASED);
				}

				// Release unit
				unit.setStatus(UnitStatus.RELEASED);
			}
		}
	}

	/**
	 * Deletes a unit, unit is completely removed, no history is kept. Changes are not committed.
	 * Personnel is released
	 * @param unit The unit
	 * @param wp Work process
	 * @throws IllegalOperationException Thrown if unit can't be deleted
	 */
	public static void deleteUnit(IUnitIf unit, IDiskoWpModule wp) throws IllegalOperationException
	{
		// Check assignments
		if(unit.getActiveAssignment() != null)
		{
			throw new IllegalOperationException();
		}

		// Check message log
		for(IMessageIf message : wp.getCmdPost().getMessageLogItems())
		{
			if(message.getSender() == unit || message.getSingleReceiver() == unit)
			{
				throw new IllegalOperationException();
			}
		}

		// TODO Check finding?
		// TODO Check intelligence?
		// TODO Check tasks?

		// Release personnel
		for(IPersonnelIf personnel : unit.getUnitPersonnelItems())
		{
			personnel.setStatus(PersonnelStatus.RELEASED);
		}

		unit.deleteObject();
	}
}
