package org.redcross.sar.gui.model;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.modeldriver.IModelDriverListenerIf;
import org.redcross.sar.util.Utils;

public class OperationTableModel extends AbstractTableModel implements IModelDriverListenerIf {

	private static final long serialVersionUID = 1L;

	private Object[] rows = null;
	private IModelDriverIf modelDriver = null;
	
	public OperationTableModel() {
		// forward
		super();
		// prepare
		modelDriver = Utils.getApp().getMsoModel().getModelDriver();
		// add to listeners
		modelDriver.addModelDriverListener(this);
	}

	public int update() {
		int current = -1;
		// get active operations locally / on sara server
		List<String[]> data = modelDriver.getActiveOperations();
		// has active operations?
		if(data!=null) {
			// allocate memory
			rows = new Object[data.size()];
			// loop over all units
			for (int i = 0; i < data.size(); i++) {
				// allocate memory
				Object[] row = new Object[1];
				// get operation id
				String oprID = (String)data.get(i)[0];
				// update row
				row[0] = oprID;
				// is current?
				if(modelDriver.getActiveOperationID()==oprID)
					current = i;
				// save row
				rows[i] = row;
			}
		}
		else {
			rows = null;
		}
		// notify
		super.fireTableDataChanged();
		// finished
		return current;
	}
    
	public int getColumnCount() {
		return 1;
	}

	public int getRowCount() {
		return rows!=null ? rows.length : 0;
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
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Aksjonsnummer";
		default:
			return null;
		}
	}

	@Override
	public void onOperationCreated(String oprID, boolean current) {
		// forward
		update();
		
	}

	@Override
	public void onOperationFinished(String oprID, boolean current) {
		// forward
		update();
	}	
}
