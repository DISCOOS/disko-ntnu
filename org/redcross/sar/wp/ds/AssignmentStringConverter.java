package org.redcross.sar.wp.ds;

import java.util.Calendar;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import no.cmr.common.util.SimpleDecimalFormat;

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

	private static final SimpleDecimalFormat f11 = new SimpleDecimalFormat(1,1);
	
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
				return assignment!=null ? MsoUtils.getAssignmentName(assignment,1) : "";
			case AssignmentTableModel.UNIT_INDEX:
				IUnitIf unit = (IUnitIf)value;
				return unit!=null ? MsoUtils.getUnitName(unit,true) : "";
			case AssignmentTableModel.STATUS_INDEX:
				return value!=null ? DiskoEnumFactory.getText((AssignmentStatus)value) : "";
			case AssignmentTableModel.ETE_INDEX:
			case AssignmentTableModel.MTE_INDEX:
			case AssignmentTableModel.XTE_INDEX:
				String text = value instanceof Integer ? Utils.getTime(Integer.valueOf(value.toString())) : "00:00:00";
				return model!=null ? (model.isActive(row) ? Utils.getHtml(Utils.getBold(text)) : text) : text;
			case AssignmentTableModel.ETA_INDEX:
			case AssignmentTableModel.MTA_INDEX:
			case AssignmentTableModel.XTA_INDEX:
				return value instanceof Calendar ? DTG.CalToDTG((Calendar)value) : null;
			case AssignmentTableModel.EDE_INDEX:
			case AssignmentTableModel.EDA_INDEX:
			case AssignmentTableModel.MDE_INDEX:
			case AssignmentTableModel.MDA_INDEX:
			case AssignmentTableModel.XDE_INDEX:
			case AssignmentTableModel.XDA_INDEX:
				return value instanceof Double ? Math.round((Double)value) + " m" : "0 m";
			case AssignmentTableModel.ESE_INDEX:
			case AssignmentTableModel.ESA_INDEX:
			case AssignmentTableModel.MSE_INDEX:
			case AssignmentTableModel.MSA_INDEX:
			case AssignmentTableModel.XSE_INDEX:
			case AssignmentTableModel.XSA_INDEX:
				return value instanceof Double ? f11.format(((Double)value)* 3.6) + " km/t" : "0 km/t";
			case AssignmentTableModel.ECP_INDEX:
				String position = "";
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
				return value!=null ? value.toString() : "";
			}
		}
		return "";
		
	}
	
}
