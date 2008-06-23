package org.redcross.sar.wp.simulator;

import java.util.Calendar;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.DTG;

public class AssignmentStringConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object value = model.getValueAt(row, column);
		if (value instanceof IAssignmentIf) {
			switch(column) {
			case 0: 
				IAssignmentIf assignment = (IAssignmentIf)value;
				return MsoUtils.getAssignmentName(assignment,1);
			case 1:
				return DiskoEnumFactory.getText((AssignmentStatus)value);
			case 2:
				return DTG.CalToDTG((Calendar)value);
			}
			return null;
		} 
		else if(value!=null)
			return value.toString();
		else
			return null;
	}
}
