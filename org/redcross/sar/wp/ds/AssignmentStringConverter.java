package org.redcross.sar.wp.ds;

import java.util.Calendar;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import no.cmr.common.util.SimpleDecimalFormat;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.DTG;

public class AssignmentStringConverter extends TableStringConverter {

	private static final SimpleDecimalFormat f11 = new SimpleDecimalFormat(1,1);
	
	/*
	private JTable m_table;
	
	public AssignmentStringConverter(JTable table) {
		m_table = table;
	}
	*/
	
	@Override
	public String toString(TableModel model, int row, int column) {
		// convert to model?
		//row = m_table!=null ? m_table.convertRowIndexToModel(row) : row;
		// not filtered?
		if(row!=-1) {
			// get value
			Object value = model.getValueAt(row, column);
			switch(column) {
			case 0: // assignment name
				IAssignmentIf assignment = (IAssignmentIf)value;
				return MsoUtils.getAssignmentName(assignment,1);
			case 1: // status
				return DiskoEnumFactory.getText((AssignmentStatus)value);
			case 2:	// eta
				return value instanceof Calendar ? DTG.CalToDTG((Calendar)value) : null;
			case 3:	// ete
				return value instanceof Integer ? Utils.getTime((Integer)value) : "00:00:00";
			case 4:	// ede
				return value instanceof Double ? Math.round((Double)value) + " m" : "0 m";
			case 5:	// eas
				return value instanceof Double ? f11.format(((Double)value)* 3.6) + " km/t" : "0 km/t";
			default:
				return value!=null ? value.toString() : null;
			}
		}
		return null;
		
	}
}
