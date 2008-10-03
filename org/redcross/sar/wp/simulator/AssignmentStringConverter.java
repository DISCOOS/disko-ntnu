package org.redcross.sar.wp.simulator;

import java.util.Calendar;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.DTG;

public class AssignmentStringConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object value = model.getValueAt(row, column);
		switch(column) {
		case 0: 
			IAssignmentIf assignment = (IAssignmentIf)value;
			return MsoUtils.getAssignmentName(assignment,1);
		case 1:	// unit
			IUnitIf unit = (IUnitIf)value;
			return unit!=null ? MsoUtils.getUnitName(unit,true) : "";
		case 2:
			return DTG.CalToDTG((Calendar)value);
		case 3:
			assignment = (IAssignmentIf)value;
			Calendar t = assignment.getTime(assignment.getStatus());
			return (t!=null ? DTG.CalToDTG(t) : "");
		case 4:
			return DiskoEnumFactory.getText((AssignmentStatus)value);
		} 
		if(value!=null)
			return value.toString();
		else
			return null;
	}
}
