package org.redcross.sar.wp.tactics;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitStringConverter extends TableStringConverter {

	@Override
	public String toString(TableModel model, int row, int column) {
		Object value = model.getValueAt(row, column);
		if(value!=null) {
			if (column == 0) {
				IUnitIf unit = (IUnitIf)value;
				return MsoUtils.getUnitName(unit, true);
			}
			else if (column > 0 && column < 3) {
				return String.valueOf(value);
			}
			else {
				return value.toString();
			}			
		}
		else
			return null;
	}
}
