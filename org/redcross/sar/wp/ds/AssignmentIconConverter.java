package org.redcross.sar.wp.ds;

import javax.swing.Icon;
import javax.swing.table.TableModel;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.table.TableIconConverter;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

public class AssignmentIconConverter implements TableIconConverter {

	public Icon toIcon(TableModel m, int row, int column) {
		
		// not filtered?
		if(m instanceof AssignmentTableModel && row!=-1) {
			// cast to AssignmentTableModel
			AssignmentTableModel model = (AssignmentTableModel)m;
			// get value
			Object value = model.getValueAt(row, column);
			switch(column) {
			case AssignmentTableModel.NAME_INDEX:
				IAssignmentIf assignment = (IAssignmentIf)value;
				return assignment!=null ? DiskoIconFactory.getIcon(
							DiskoEnumFactory.getIcon(MsoUtils.getType(assignment,true)),"32x32") : null;
			case AssignmentTableModel.UNIT_INDEX:
				IUnitIf unit = (IUnitIf)value;
				return unit!=null ? DiskoIconFactory.getIcon(
							DiskoEnumFactory.getIcon(MsoUtils.getType(unit,true)),"32x32") : null;
			}
		}
		return null;
		
	}
	
}
