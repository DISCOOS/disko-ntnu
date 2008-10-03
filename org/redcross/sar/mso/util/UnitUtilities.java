package org.redcross.sar.mso.util;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.unit.IDiskoWpUnit;

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
	 * Releases a unit
	 * @param unit The unit
	 * @throws IllegalOperationException Thrown if unit can not be released
	 */
	public static boolean releaseUnit(IUnitIf unit) throws IllegalOperationException
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

				// Release unit
				unit.setStatus(UnitStatus.RELEASED);
			}
			return releaseUnit;
		}
		return false;
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
		
		// get cmd post
		ICmdPostIf cmdPost = wp.getCmdPost();

		// no command post?
		if(cmdPost==null) return;
		
		// Check message log
		for(IMessageIf message : wp.getCmdPost().getMessageLogItems())
		{
			if(message.getSender() == unit || message.getReceiver() == unit)
			{
				throw new IllegalOperationException();
			}
		}

		unit.delete();
	}
}
