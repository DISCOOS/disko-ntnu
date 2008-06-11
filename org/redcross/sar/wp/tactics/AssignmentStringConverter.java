package org.redcross.sar.wp.tactics;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;

public class AssignmentStringConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object value = model.getValueAt(row, column);
		if (value instanceof IAssignmentIf) {
			IAssignmentIf assignment = (IAssignmentIf)value;
			String text = null;
			if (column == 1) {
				text = MsoUtils.getAssignmentName(assignment,1);
			}
			else if (column == 2) {
				text = DiskoEnumFactory.getText(assignment.getStatus());
			}
			return text;
		} 
		else if(value!=null)
			return value.toString();
		else
			return null;
	}
}
