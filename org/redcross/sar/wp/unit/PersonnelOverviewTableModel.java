package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Selector;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.table.AbstractTableModel;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Table model for the personnel overview panel
 *
 * @author thomasl
 */
public class PersonnelOverviewTableModel extends AbstractTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;
	private List<IPersonnelIf> m_displayPersonnel;
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
			if(o1.getFirstname()!=null)
				return o1.getFirstname().compareTo(o2.getFirstname());
			else if(o2.getFirstname()!=null) 
				return o2.getFirstname().compareTo(o1.getFirstname());
			else
				return 0;
		}
	};

	public PersonnelOverviewTableModel(IDiskoWpUnit wp)
	{
		m_wpModule = wp;
		wp.getMsoModel().getEventManager().addClientUpdateListener(this);
		m_displayPersonnel = new LinkedList<IPersonnelIf>();
		IPersonnelListIf allPersonnel = m_wpModule.getCmdPost().getAttendanceList();
		m_displayPersonnel.addAll(allPersonnel.selectItems(m_activePersonnelSelector, m_personnelComparator));
	}

	public int getColumnCount()
	{
		return 4;
	}

	public int getRowCount()
	{
		return m_displayPersonnel.size();
	}

	public Object getValueAt(int row, int column)
	{
		if(!(row<m_displayPersonnel.size())) return null;
		
		IPersonnelIf personnel = m_displayPersonnel.get(row);
		switch(column)
		{
		case 0:
			return personnel.getFirstname() + " " + personnel.getLastname();
		case 1:
          // Set unit
            IUnitIf personnelUnit = null;
            for (IUnitIf unit : m_wpModule.getMsoManager().getCmdPost().getUnitListItems())
            {
                if (unit.getStatus() != UnitStatus.RELEASED)
                {
                    if (unit.getUnitPersonnel().contains(personnel))
                    {
                        personnelUnit = unit;
                        break;
                    }
                }
            }		
            return personnelUnit == null ? "" : MsoUtils.getUnitName(personnelUnit,false);
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 2 || columnIndex == 3;
	}
	
	/**
	 * Table data changed if personnel data changed in MSO
	 */
	public void handleMsoUpdateEvent(Update e)
	{
		IPersonnelListIf allPersonnel = m_wpModule.getCmdPost().getAttendanceList();
		m_displayPersonnel.clear();
		m_displayPersonnel.addAll(allPersonnel.selectItems(m_activePersonnelSelector, m_personnelComparator));
		fireTableDataChanged();
	}

	EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(
			IMsoManagerIf.MsoClassCode.CLASSCODE_PERSONNEL);
	/**
	 * Interested in personnel changes
	 */
	public boolean hasInterestIn(IMsoObjectIf msoObject)
	{
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
			return clickedRow < m_displayPersonnel.size() ? m_displayPersonnel.get(clickedRow) : null;
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
			return "Fornavn";
		case 1:
			return "Tildelt";
		case 2:
			return "Vis";
		case 3:
			return "Status";
		default:
			return null;
		}
	}
	
}
