package org.redcross.sar.wp.unit;

import org.redcross.sar.Application;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Handles personnel logic
 */
public class ResourceUtils
{
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	/**
	 * Creates new personnel history instance.
	 *
	 * @return The reinstated personnel
	 * @param personnel Old personnel instance
	 * @param newStatus Status of new personnel instance
	 */
	public static IPersonnelIf reinstatePersonnel(IPersonnelIf personnel, PersonnelStatus newStatus)
	{
		// Get personnel at end of history chain
		IPersonnelIf lastOccurence = personnel.getLastOccurrence();
		
		// is last occurrence released?
		if(lastOccurence.getStatus() == PersonnelStatus.RELEASED)
		{
			
			// get mso model
			IMsoModelIf model = Application.getInstance().getMsoModel();
			
			// suspend update events
			model.suspendUpdate();
			
			// Reinstate resource
			IPersonnelIf newPersonnel = model.getMsoManager().createPersonnel();

			// Copy fields
			newPersonnel.setBirthdate(personnel.getBirthdate());
			newPersonnel.setDataSourceID(personnel.getDataSourceID());
			newPersonnel.setDataSourceName(personnel.getDataSourceName());
			newPersonnel.setDepartment(personnel.getDepartment());
			newPersonnel.setEstimatedArrival(personnel.getEstimatedArrival());
			newPersonnel.setFirstname(personnel.getFirstName());
			newPersonnel.setGender(personnel.getGender());
			newPersonnel.setLastname(personnel.getLastName());
			newPersonnel.setOrganization(personnel.getOrganization());
			newPersonnel.setPhoto(personnel.getPhoto());
			newPersonnel.setRemarks(personnel.getRemarks());
			newPersonnel.setResidence(personnel.getResidence());
			newPersonnel.setTelephone1(personnel.getTelephone1());
			newPersonnel.setTelephone2(personnel.getTelephone2());
			newPersonnel.setTelephone3(personnel.getTelephone3());
			newPersonnel.setType(personnel.getType());

			// Maintain personnel history chain
			lastOccurence.setNextOccurrence(newPersonnel);

			// Set status
			newPersonnel.setStatus(newStatus);
			if(newStatus == PersonnelStatus.ON_ROUTE)
			{
				newPersonnel.setCallOut(Calendar.getInstance());
			}
			else if(newStatus == PersonnelStatus.ARRIVED)
			{
				newPersonnel.setCallOut(Calendar.getInstance());
				newPersonnel.setArrived(Calendar.getInstance());
			}
			
			/*
			// commit changes
			try {
				List<IChangeSourceIf> changes = new Vector<IChangeSourceIf>(2);
				changes.add(model.getChanges(newPersonnel));
				changes.add(model.getChanges(lastOccurence));
				model.commit(changes);
			} catch (TransactionException e) {
				m_logger.error("Failed to commit reinstated personnel",e);
			}
			*/
			
			// resume events
			model.resumeUpdate();

			// finished
			return newPersonnel;
			
		} else {
			// personnel at end of history chain is not released
			return lastOccurence;
		}
	}

	/**
	 * @return User confirmation, whether to reinstate personnel or not
	 */
	public static boolean confirmReinstate(IPersonnelIf personnel)
	{
		String name = MsoUtils.getPersonnelName(personnel,false);
		String[] options = {DiskoStringFactory.getAnswerText(true),DiskoStringFactory.getAnswerText(false)};
		return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
				null,
				String.format(m_resources.getString("ReinstateReleasedPersonnel.text"),name),
				m_resources.getString("ReinstateReleasedPersonnel.header"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);
	}

	/**
	 * Call out personnel. Checks if personnel is released. Does not commit changes
	 * @param personnel
	 * @throws IllegalOperationException
	 */
	public static IPersonnelIf callOutPersonnel(IPersonnelIf personnel)
	{
		PersonnelStatus status = personnel.getStatus();
		if(status == PersonnelStatus.RELEASED)
		{
			if(confirmReinstate(personnel))
			{
				personnel = reinstatePersonnel(personnel, PersonnelStatus.ON_ROUTE);
			}
		}
		else if(!PersonnelStatus.ON_ROUTE.equals(status)) {
			personnel.setStatus(PersonnelStatus.ON_ROUTE);
			personnel.setCallOut(Calendar.getInstance());
		}

		return personnel;
	}

	/**
	 * Set personnel to arrived, checks if personnel is released. Does not commit changes
	 * @param personnel
	 * @return Reinstated personnel if any, otherwise {@code null}
	 * @throws IllegalOperationException
	 */
	public static IPersonnelIf arrivedPersonnel(IPersonnelIf personnel)
	{
		PersonnelStatus status = personnel.getStatus();

		if(status == PersonnelStatus.RELEASED)
		{
			if(confirmReinstate(personnel))
			{
				personnel = reinstatePersonnel(personnel, PersonnelStatus.ARRIVED);
			}
		}
		else if(!PersonnelStatus.ARRIVED.equals(status)) {
			personnel.setStatus(PersonnelStatus.ARRIVED);
			personnel.setArrived(Calendar.getInstance());
		}

		return personnel;
	}

	/**
	 * Releases personnel, changes are not committed
	 * @param personnel
	 */
	public static void releasePersonnel(IPersonnelIf personnel)
	{
		PersonnelStatus status = personnel.getStatus();
		if(!PersonnelStatus.RELEASED.equals(status)) {
			personnel.setStatus(PersonnelStatus.RELEASED);
			personnel.setReleased(Calendar.getInstance());
		}
	}

	/**
	 * @param personnel
	 * @return Whether or not personnel can be Allocated to any unit
	 */
	public static boolean canAssignPersonnelToUnit(IPersonnelIf personnel)
	{
		// Only on route or arrived personnel can be Allocated to a unit
		if(!(personnel.getStatus() == PersonnelStatus.ON_ROUTE ||
				personnel.getStatus() == PersonnelStatus.ARRIVED))
		{
			return false;
		}

		IMsoManagerIf manager = Application.getInstance().getMsoModel().getMsoManager();

		if(manager.operationExists()) {
			// TODO: replace with more general method
			// Personnel can only be Allocated to ONE unit
			for(IUnitIf unit : manager.getCmdPost().getUnitList().getObjects())
			{
				for(IPersonnelIf unitPersonnel : unit.getUnitPersonnel().getObjects())
				{
					if(!UnitStatus.RELEASED.equals(unit.getStatus()) &&  unitPersonnel == personnel)
					{
						return false;
					}
				}
			}
		}
		return true;
	}
	
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
				releaseUnit = (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
						m_resources.getString("ReleaseUnitWithAssignment.text"),
						m_resources.getString("ReleaseUnitWithAssignment.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]));
			}
			else
			{
				String[] options = {m_resources.getString("Yes.text"), m_resources.getString("No.text")};
				releaseUnit = (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(null,
						m_resources.getString("ReleaseUnit.text"),
						m_resources.getString("ReleaseUnit.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]));
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
	
}
