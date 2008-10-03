package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Selector;
import org.redcross.sar.wp.IDiskoWpModule;

import org.redcross.sar.gui.model.DiskoTableModel;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Table model for the personnel overview panel
 *
 * @author thomasl
 */
public class PersonnelTableModel extends DiskoTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;
	private List<IPersonnelIf> m_persons;
	private IDiskoWpModule m_wpModule;

	/**
	 * Select personnel at the end of the history chain
	 */
	private static Selector<IPersonnelIf> m_activePersonnelSelector = new Selector<IPersonnelIf>()
	{
		public boolean select(IPersonnelIf personnel)
		{
			return personnel.getNextOccurence() == null;
		}
	};

	/**
	 * Sort personnel on name
	 */
	private static final Comparator<IPersonnelIf> m_personnelComparator = new Comparator<IPersonnelIf>()
	{
		public int compare(IPersonnelIf o1, IPersonnelIf o2)
		{
			if(o1.getFirstName()!=null)
				return o1.getFirstName().compareTo(o2.getFirstName());
			else if(o2.getFirstName()!=null) 
				return o2.getFirstName().compareTo(o1.getFirstName());
			else
				return 0;
		}
	};

	public PersonnelTableModel(IDiskoWpUnit wp)
	{
		m_wpModule = wp;
		wp.getMsoModel().getEventManager().addClientUpdateListener(this);
		m_persons = new LinkedList<IPersonnelIf>();
		IPersonnelListIf allPersonnel = m_wpModule.getCmdPost().getAttendanceList();
		m_persons.addAll(allPersonnel.selectItems(m_activePersonnelSelector, m_personnelComparator));
		fireTableDataChanged();		
	}

	public int getColumnCount()
	{
		return 4;
	}

	public int getRowCount()
	{
		return m_persons.size();
	}

	public Object getValueAt(int row, int column)
	{
		if(!(row<m_persons.size())) return null;
		
		IPersonnelIf personnel = m_persons.get(row);
		switch(column)
		{
		case 0:
			return personnel.getFirstName() + " " + personnel.getLastName();
		case 1:
          // Set unit
            IUnitIf personnelUnit = null;
            if(m_wpModule.getMsoManager().operationExists()) {            
	            for (IUnitIf unit : m_wpModule.getMsoManager().getCmdPost().getUnitListItems())
	            {
	                if (unit.getStatus() != UnitStatus.RELEASED)
	                {
	                    if (unit.getUnitPersonnel().exists(personnel))
	                    {
	                        personnelUnit = unit;
	                        break;
	                    }
	                }
	            }
            }
            return personnelUnit == null ? "" : MsoUtils.getUnitName(personnelUnit,false);
		case 2:
			return personnel;
		}
		// failure
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 2 || columnIndex == 3;
	}
	
	public void handleMsoUpdateEvent(Update e) {
		
		// get mask
		int mask = e.getEventTypeMask();
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // get flag
        boolean clearAll = (mask & MsoEvent.MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;
		
        // clear all?
        if(clearAll) {
        	m_persons.clear();
			fireTableDataChanged();		
        }
        else {
        	// get flags
	        boolean createdObject  = (mask & MsoEvent.MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
	        boolean deletedObject  = (mask & MsoEvent.MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
	        boolean modifiedObject = (mask & MsoEvent.MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
	        boolean addedReference = (mask & MsoEvent.MsoEventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
	        boolean removedReference = (mask & MsoEvent.MsoEventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
			
	        // add object?
			if (createdObject) {
				msoObjectCreated(msoObj,mask);
			}
			// is object modified?
			if ( (addedReference || removedReference || modifiedObject)) {
				msoObjectChanged(msoObj,mask);
			}
			// delete object?
			if (deletedObject) {
				msoObjectDeleted(msoObj,mask);		
			}
        }
	}

	private void msoObjectCreated(IMsoObjectIf msoObj, int mask) {
		IPersonnelIf msoPersonnel = (IPersonnelIf)msoObj; 
		// add?        
        if(m_activePersonnelSelector.select(msoPersonnel)) {
        	m_persons.add(msoPersonnel);
			fireTableDataChanged();		
        }
	}
	
	private void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		// add?        
        if(m_persons.contains(msoObj)) {
			fireTableDataChanged();		
        }
	}

	private void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		// remove?        
        if(m_persons.contains(msoObj)) {
        	m_persons.remove(msoObj);
			fireTableDataChanged();		
        }
	}
	
	EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_PERSONNEL);
	
	/**
	 * Interested in personnel changes
	 */
	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return myInterests.contains(msoObject.getMsoClassCode());
	}

	/**
	 * @param clickedRow
	 * @return Personnel at given row in table
	 */
	public IPersonnelIf getPersonnel(int clickedRow)
	{
		if(clickedRow >= 0)
		{
			return clickedRow < m_persons.size() ? m_persons.get(clickedRow) : null;
		}
		else
		{
			return null;
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Navn";
		case 1:
			return "Underordnet";
		case 2:
			return "Vis";
		case 3:
			return "Status";
		default:
			return null;
		}
	}
	
}
