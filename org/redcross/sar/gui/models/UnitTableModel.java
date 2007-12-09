package org.redcross.sar.gui.models;

import java.util.EnumSet;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.EventType;
import org.redcross.sar.mso.event.MsoEvent.Update;

public class UnitTableModel extends AbstractTableModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private Object[] rows = null;
	private int numColumns = 0;
	private ICmdPostIf cmdPost = null;

	public UnitTableModel(IMsoModelIf msoModel, int numColumns) {
		this.numColumns = numColumns;
		myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		cmdPost = msoModel.getMsoManager().getCmdPost();
		update(cmdPost.getUnitListItems().toArray());
	}

	public void handleMsoUpdateEvent(Update e) {
		
        // get all flags
		int mask = e.getEventTypeMask();
		
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        
		if (createdObject  || modifiedObject || deletedObject) {
			update(cmdPost.getUnitListItems().toArray());
		}
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	private void update(Object[] data) {
		rows = new Object[(int)Math.floor(data.length / numColumns)+1];
		for (int i = 0; i < data.length; i++) { 
			int rowIndex = (int)Math.floor(i / numColumns);
			Object[] row = (Object[]) rows[rowIndex];
			if (row == null) {
				row = new Object[numColumns];
				for (int j = 0; j < numColumns && j < data.length; j++) {
					row[j] = data[rowIndex+j];
				}
				rows[rowIndex] = row;
			}
		}
		super.fireTableDataChanged();
	}

	public int getColumnCount() {
		return numColumns;
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
}
