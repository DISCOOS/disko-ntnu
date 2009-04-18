package org.redcross.sar.wp.unit;

import org.redcross.sar.Application;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;

import java.util.Calendar;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Handles personnel logic
 */
public class PersonnelUtilities
{
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	/**
	 * Creates new personnel history instance.
	 *
	 * @return The reinstated personnel
	 * @param personnel Old personnel instance
	 * @param newStatus Status of new personnel instance
	 */
	public static IPersonnelIf reinstateResource(IPersonnelIf personnel, PersonnelStatus newStatus)
	{
		// Get personnel at end of history chain
		IPersonnelIf nextOccurence = personnel;
		while(nextOccurence.getNextOccurence() != null)
		{
			nextOccurence = nextOccurence.getNextOccurence();
		}

		if(nextOccurence.getStatus() == PersonnelStatus.RELEASED)
		{
			// Reinstate resource
			IPersonnelIf newPersonnel = Application.getInstance().getMsoModel().getMsoManager().createPersonnel();
			newPersonnel.suspendClientUpdate();

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
			nextOccurence.setNextOccurence(newPersonnel);

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

			newPersonnel.resumeClientUpdate(true);

			return newPersonnel;
		}
		else
		{
			// Personnel at end of history chain is not released, return this
			return nextOccurence;
		}
	}

	/**
	 * @return User confirmation, whether to reinstate personnel or not
	 */
	public static boolean confirmReinstate()
	{
		String[] options = {m_resources.getString("Yes.text"), m_resources.getString("No.text")};
		return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(
				null,
				m_resources.getString("ReinstateReleasedPersonnel.text"),
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
			if(confirmReinstate())
			{
				personnel = reinstateResource(personnel, PersonnelStatus.ON_ROUTE);
			}
		}
		else
		{
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
			if(confirmReinstate())
			{
				personnel = reinstateResource(personnel, PersonnelStatus.ARRIVED);
			}
		}
		else
		{
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
		personnel.setStatus(PersonnelStatus.RELEASED);
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
			for(IUnitIf unit : manager.getCmdPost().getUnitList().getItems())
			{
				for(IPersonnelIf unitPersonnel : unit.getUnitPersonnel().getItems())
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
	 * Deletes personnel instance. History chain is kept
	 * @param personnel The personnel
	 * @throws IllegalOperationException Thrown if personnel can not be deleted
	 */
	public static void deletePersonnel(IPersonnelIf personnel) throws IllegalOperationException
	{
		if(personnel.getStatus() != PersonnelStatus.IDLE)
		{
			throw new IllegalOperationException("Personnel status not IDLE, cannot delete");
		}

		if(!personnel.delete())
		{
			throw new IllegalOperationException("Failed to delete personnel object");
		}
	}
}
