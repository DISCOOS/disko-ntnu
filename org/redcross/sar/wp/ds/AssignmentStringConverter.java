package org.redcross.sar.wp.ds;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.util.mso.TimePos;

public class AssignmentStringConverter extends TableStringConverter {

	private static final String EMPTY = "";
	private static final NumberFormat f11 = new DecimalFormat("0.0");

	public String toString(TableModel m, int row, int column) {
		// not filtered?
		if(m instanceof AssignmentTableModel && row!=-1 && column!=-1) {
			// cast to AssignmentTableModel
			AssignmentTableModel model = (AssignmentTableModel)m;
			// get value
			Object value = model.getValueAt(row, column);
			switch(column) {
			case AssignmentTableModel.NAME_INDEX:
				IAssignmentIf assignment = (IAssignmentIf)value;
				return assignment!=null ? MsoUtils.getAssignmentName(assignment,1) : EMPTY;
			case AssignmentTableModel.UNIT_INDEX:
				IUnitIf unit = (IUnitIf)value;
				return unit!=null ? MsoUtils.getUnitName(unit,false) : EMPTY;
			case AssignmentTableModel.STATUS_INDEX:
				return value!=null ? DiskoEnumFactory.getText((AssignmentStatus)value) : EMPTY;
			case AssignmentTableModel.ETE_INDEX:
			case AssignmentTableModel.MTE_INDEX:
			case AssignmentTableModel.XTE_INDEX:
				return value instanceof Integer ? Utils.getTime(Integer.valueOf(value.toString())) : EMPTY;
			case AssignmentTableModel.ETA_INDEX:
			case AssignmentTableModel.MTA_INDEX:
			case AssignmentTableModel.XTA_INDEX:
				return value instanceof Calendar ? DTG.CalToDTG((Calendar)value) : EMPTY;
			case AssignmentTableModel.EDE_INDEX:
			case AssignmentTableModel.EDA_INDEX:
			case AssignmentTableModel.MDE_INDEX:
			case AssignmentTableModel.MDA_INDEX:
			case AssignmentTableModel.XDE_INDEX:
			case AssignmentTableModel.XDA_INDEX:
				return value instanceof Double ? Math.round((Double)value) + " m" : EMPTY;
			case AssignmentTableModel.ESE_INDEX:
			case AssignmentTableModel.ESA_INDEX:
			case AssignmentTableModel.MSE_INDEX:
			case AssignmentTableModel.MSA_INDEX:
			case AssignmentTableModel.XSE_INDEX:
			case AssignmentTableModel.XSA_INDEX:
				return value instanceof Double ? f11.format(((Double)value)* 3.6) + " km/t" : EMPTY;
			case AssignmentTableModel.ECP_INDEX:
				String position = EMPTY;
				if(value instanceof TimePos) {
					try {
						MapUtil.getMGRSfromPosition(((TimePos)value).getPosition(), 5);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return position;
			default:
				return value!=null ? value.toString() : EMPTY;
			}
		}
		return EMPTY;

	}

}
