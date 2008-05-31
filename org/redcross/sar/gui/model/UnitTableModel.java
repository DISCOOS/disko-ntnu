package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AbstractUnit;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitTableModel extends AbstractTableModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private Object[] rows = null;
	private IMsoModelIf msoModel = null;
	private EnumSet<UnitStatus> status = null;

	public UnitTableModel(IMsoModelIf msoModel) {
		this(msoModel,EnumSet.noneOf(UnitStatus.class));
	}
	
	public UnitTableModel(IMsoModelIf msoModel,EnumSet<UnitStatus> status) {
		// prepare
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
		this.msoModel = msoModel;
		this.status = status;
		// add listeners
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		// update table data
		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		update(cmdPost.getUnitListItems().toArray());
	}

	public void handleMsoUpdateEvent(Update e) {
		
		// update table
		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		update((cmdPost!=null ? cmdPost.getUnitListItems().toArray() : null));
		
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	private void update(Object[] data) {
		if(data!=null) {
			List<Object> list = new ArrayList<Object>(data.length);
			// loop over all units
			for (int i = 0; i < data.length; i++) {
				// get unit
				IUnitIf unit = (IUnitIf )data[i];
				// filter?
				if(status.size()==0 || status.contains(unit.getStatus())) {
					// allocate memory
					Object[] row = new Object[5];
					// update row
					row[0] = unit;
					row[1] = unit.getUnitPersonnelItems().size();
					row[2] = getAllocated(unit);
					row[3] = unit.getRemarks();
					row[4] = unit.getStatus();
					// save row
					list.add(row);
				}
			}
			// get array
			rows = list.toArray();
		}
		else {
			rows = null;
		}
		super.fireTableDataChanged();
	}
	
	private String getAllocated(IUnitIf unit) {
		// initialize
		String text = "Ingen";
		// get assignment
		List<IAssignmentIf> data = unit.getAllocatedAssignments();
		// build string
		for(int i=0;i<data.size();i++) {
			// get name
			String name = MsoUtils.getAssignmentName(data.get(i),1).toLowerCase();
			text = (i==0 ? name : text +  "; " + name);
		}
		return text;
	}
    
	public int getColumnCount() {
		return 5;
	}

	public int getRowCount() {
		return rows.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
    	// invalid index?
    	if(!(rowIndex<rows.length)) return null;
    	// get row
		Object[] row = (Object[]) rows[rowIndex];
		if(row != null)
			return row[columnIndex];
		else
			return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return AbstractUnit.class; 
		case 1:
			return AssignmentImpl.class;
		case 2:
			return AssignmentImpl.class;
		case 3:
			return AssignmentImpl.class;
		case 4:
			return UnitStatus.class;
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Enhet";
		case 1:
			return "Antall mannskaper";
		case 2:
			return "Oppdrag i k�";
		case 3:
			return "Merknader";
		case 4:
			return "Status";
		default:
			return null;
		}
	}	
}
