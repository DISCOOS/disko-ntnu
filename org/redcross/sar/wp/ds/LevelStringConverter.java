package org.redcross.sar.wp.ds;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.ds.sc.ICue;
import org.redcross.sar.ds.sc.ILevel;
import org.redcross.sar.util.Utils;

public class LevelStringConverter extends TableStringConverter {

	private static final String EMPTY = "";
	private static final String SPACE = " ";
	private static final NumberFormat f11 = new DecimalFormat("0.0");

	private boolean m_isHtml;

	public LevelStringConverter(boolean isHtml) {
		m_isHtml = isHtml;
	}

	public String toString(TableModel m, int row, int column) {
		// not filtered?
		if(m instanceof LevelTableModel && row!=-1 && column!=-1) {
			// cast to CueTableModel
			LevelTableModel model = (LevelTableModel)m;
			// get level
			ILevel level = (ILevel)model.getObject(row);
			// get value
			Object value = model.getValueAt(row, column);
			switch(column) {
			case LevelTableModel.NAME_INDEX:
				ICue cue = (ICue)value;
				return cue!=null ? cue.getName() : EMPTY;
			case LevelTableModel.INPUT_INDEX:
			case LevelTableModel.OUTPUT_INDEX:
				return value!=null ? getText(f11.format((Double)value), level.unit() + "/min") : EMPTY;
			case LevelTableModel.LEVEL_INDEX:
				return value!=null ? getText(String.valueOf(value), level.unit()): EMPTY;
			default:
				return value!=null ? value.toString() : EMPTY;
			}
		}
		return EMPTY;

	}

	private String getText(String value, String unit) {
		return m_isHtml ? Utils.getHtml(value + getUnitText(unit)) : value + getUnitText(unit);
	}

	private String getUnitText(String text) {
		return SPACE + (m_isHtml ? "<small style=\"color:gray\">" + text : text);
	}

}
