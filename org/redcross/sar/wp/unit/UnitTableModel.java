package org.redcross.sar.wp.unit;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
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

public class UnitTableModel extends DiskoTableModel implements IMsoUpdateListenerIf
{
	private static final long serialVersionUID = 1L;

	private List<IUnitIf> m_units;

	private IDiskoWpModule m_wpModule;

	private static final Selector<IUnitIf> m_unitSelector = new Selector<IUnitIf>()
	{
		public boolean select(IUnitIf anObject)
		{
			return true;
		}
	};

	private static final Comparator<IUnitIf> m_unitComparator = new Comparator<IUnitIf>()
	{
		public int compare(IUnitIf arg0, IUnitIf arg1)
		{
			if(arg0.getType() == arg1.getType())
			{
				return arg0.getNumber() - arg1.getNumber();
			}
			else
			{
				return arg0.getType().ordinal() - arg1.getType().ordinal();
			}
		}
	};

	public UnitTableModel(IDiskoWpUnit wp)
	{
		m_wpModule = wp;

		m_wpModule.getMsoModel().getEventManager().addClientUpdateListener(this);
		m_units = new LinkedList<IUnitIf>();
		// todo use linked list directly
		IUnitListIf allUnits = m_wpModule.getCmdPost().getUnitList();
		m_units.addAll(allUnits.selectItems(m_unitSelector, m_unitComparator));
		fireTableDataChanged();
		
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
        	m_units.clear();
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
		IUnitIf msoUnit = (IUnitIf)msoObj; 
		// add?        
        if(m_unitSelector.select(msoUnit)) {
			m_units.add(msoUnit);
			fireTableDataChanged();		
        }
	}
	
	private void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		// add?        
        if(m_units.contains(msoObj)) {
			fireTableDataChanged();		
        }
	}

	private void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		// add?        
        if(m_units.contains(msoObj)) {
        	m_units.remove(msoObj);
			fireTableDataChanged();		
        }
	}
	
	EnumSet<IMsoManagerIf.MsoClassCode> interestedIn = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
	
	public boolean hasInterestIn(IMsoObjectIf msoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return interestedIn.contains(msoObject.getMsoClassCode());
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Enhet";
		case 1:
			return "Vis";
		case 2:
			return "Oppløs";
		default:
			return null;
		}
	}

	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex == 1 || columnIndex == 2;
	}

	public int getRowCount()
	{
		return m_units.size();
	}

	public Object getValueAt(int row, int column)
	{
		// valid index?
		if(row<m_units.size()) {
			switch(column){
			case 0:
				// return name of unit
				return MsoUtils.getUnitName(m_units.get(row), true);
			case 1:
				// return name of unit
				return m_units.get(row);
			}
		}
		return null;
	}

	/*
	 * Return unit at given row in table model
	 */
	public IUnitIf getUnit(int clickedRow)
	{
		return clickedRow < m_units.size() ? m_units.get(clickedRow): null;
	}

}
