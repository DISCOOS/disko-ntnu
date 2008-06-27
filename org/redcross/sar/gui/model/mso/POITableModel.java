package org.redcross.sar.gui.model.mso;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

public class POITableModel extends AbstractTableModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private IAreaIf area = null;
	private ArrayList<Object[]> rows = null;
	
	public POITableModel(IMsoModelIf msoModel) {
		
		myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_POI);
		//myInterests.add(IMsoManagerIf.MsoClassCode.CLASSCODE_AREA);
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		rows = new ArrayList<Object[]>();
		
	}
	
	public void setArea(IAreaIf area) {
		this.area = area;
		if (area != null) {
			Iterator<?> iter = area.getAreaPOIs().getItems().iterator();
			while (iter.hasNext()) {
				IPOIIf poi = (IPOIIf)iter.next();
				add(poi);
			}
		}
		else {
			if (rows != null) {
				rows.clear();
			}
		}
		super.fireTableDataChanged();
	}

	
	
	public void handleMsoUpdateEvent(Update e) {
		
        // get all flags
		int mask = e.getEventTypeMask();

        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
        boolean clearAll = (mask & MsoEvent.EventType.CLEAR_ALL_EVENT.maskValue()) != 0;
		
        if(clearAll) {
        	rows.clear();
			super.fireTableDataChanged();
        }
        else { 		
        	
            // get object
            IPOIIf poi = (IPOIIf)e.getSource();
    		
            // handle event
			if (createdObject && area != null && area.getAreaPOIs().contains(poi)) {
				add(poi);
				super.fireTableDataChanged();
			}
			
			// is object modified?
			if ((modifiedObject || addedReference || removedReference) 
					&& area != null && area.getAreaPOIs().contains(poi)) {
				int index = getRow(poi);
				if (index > -1) {
					update(poi,rows.get(index));
					super.fireTableDataChanged();
				}
			}
			
			// is object deleted?
			if (deletedObject) {
				remove(poi);
				super.fireTableDataChanged();
			}
        }		
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	private void update(IPOIIf poi, Object[] row) {
		try {
			row[0] = new Integer(rows.size()+1);
			row[1] = DiskoEnumFactory.getText(poi.getType());
			row[2] = MapUtil.formatMGRS(MapUtil.getMGRSfromPosition(poi.getPosition(),5),3,5);		
			row[3] = poi.getRemarks();		
			row[4] = poi;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void updateSortNumber() {
		for (int i = 0; i < rows.size(); i++) {
			Object[] row = rows.get(i);
			row[0] = i+1;
		}
	}
	
	private void add(IPOIIf poi) {
		if (getRow(poi) == -1) {
			Object[] row = new Object[5];
			if (poi.getType() == POIType.START)
				rows.add(0, row);
			else if (poi.getType() == POIType.STOP) {
				rows.add(row);
			}
			else {
				if (rows.size() > 1)
					rows.add(rows.size()-1, row);
				else rows.add(row);
			}
			update(poi,row);
			updateSortNumber();
		}
	}
	
	private void remove(IPOIIf poi) {
		int rowIndex = getRow(poi);
		if (rowIndex > -1) {
			rows.remove(rowIndex);
			updateSortNumber();
		}
	}
	
	private int getRow(IPOIIf poi) {
		for (int i = 0; i < rows.size(); i++) {
			Object[] row = (Object[])rows.get(i);
			if (row[4].equals(poi)) {
				return i;
			}
		}
		return -1;
	}

	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return rows == null ? 0 : rows.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rows == null) return null;
    	if(!(rowIndex<rows.size())) return null;
		Object[] row = (Object[])rows.get(rowIndex);
		return row[columnIndex];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Integer.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Rekkefølge";
		case 1:
			return "Type";
		case 2:
			return "Posisjon";
		case 3:
			return "Merknad";
		default:
			return null;
		}
	}
}
