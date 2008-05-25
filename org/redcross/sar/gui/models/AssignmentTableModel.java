package org.redcross.sar.gui.models;

import java.util.EnumSet;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

public class AssignmentTableModel extends AbstractTableModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private Object[] rows = null;
	private IMsoModelIf msoModel = null;

	public AssignmentTableModel(IMsoModelIf msoModel) {
		// prepare
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);
		this.msoModel = msoModel;
		// add listeners
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		// update table
		ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
		update((cmdPost!=null ? cmdPost.getAssignmentListItems().toArray() : null));
	}

	public void handleMsoUpdateEvent(Update e) {
		int mask = e.getEventTypeMask();
		
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
		
		if (createdObject || modifiedObject || deletedObject ) {
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			update((cmdPost!=null ? cmdPost.getAssignmentListItems().toArray() : null));
		}
		
	}
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	private void update(Object[] data) {
		if(data!=null) {
			rows = new Object[data.length];
			for (int i = 0; i < data.length; i++) {
				IAssignmentIf assignment = (IAssignmentIf)data[i];
				Object[] row = new Object[3];
				row[0] = new Boolean(false);
				row[1] = assignment;
				row[2] = assignment;
				rows[i] = row;
			}
		}
		else {
			rows = null;
		}
		super.fireTableDataChanged();
	}

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return (rows!=null ? rows.length : 0);
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
    	// invalid index?
    	if(rows==null || !(rowIndex<rows.length)) return null;
		Object[] row = (Object[]) rows[rowIndex];
		return row[columnIndex];
	}
	
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
    	if(rows==null) return;
		Object[] row = (Object[]) rows[rowIndex];
		row[columnIndex] = value;
        fireTableCellUpdated(rowIndex, columnIndex);
    }

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return AssignmentImpl.class;
		case 2:
			return AssignmentImpl.class;
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Velg";
		case 1:
			return "Oppdrag";
		case 2:
			return "Status";
		default:
			return null;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return true;
		}
		return false;
	}
	
}
