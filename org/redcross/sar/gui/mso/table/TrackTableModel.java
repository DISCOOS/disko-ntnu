/**
 * 
 */
package org.redcross.sar.gui.mso.table;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

/**
 * @author kennetgu
 *
 */
public class TrackTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	
	private static final String[] COLUMNS = {"Index",
											"Altitude",
											"Distance",
											"Time",
											"Speed",
											"EAD",			// estimated altitude difference 
											"EDE",			// estimated distance enroute
											"ETE",			// estimated time enroute
											"EAS",			// estimated average speed enroute
											"ECP"};			// estimated current position

	private Track m_track;
	private List<Object[]> m_rows;
	
	/* =======================================================
	 * Constructors
	 * ======================================================= */
	
	public TrackTableModel() {
		m_rows = new ArrayList<Object[]>();
	}
	
	/* =======================================================
	 * AbstractTableModel implementation
	 * ======================================================= */
	
	public int getColumnCount() {
		return COLUMNS.length;
	}

	public int getRowCount() {
		return m_rows.size();
	}

	public Object getValueAt(int nRow, int nCol) {
    	// invalid index?
    	if(nRow<0 || nRow>m_rows.size()-1) return null;
    	// get row
		Object[] row = m_rows.get(nRow);
		// return cell value
		return row[nCol];
	}
	
    public void setValueAt(Object value, int nRow, int nCol) {
    	// invalid index?
    	if(nRow<0 || nRow>m_rows.size()-1) return;
    	// get row
		Object[] row = m_rows.get(nRow);
		// update cell value
		row[nCol] = value;
		// notify change
        fireTableCellUpdated(nRow, nCol);
    }

	public String getColumnName(int nCol) {
    	// invalid index?
    	if(nCol<0 || nCol>COLUMNS.length-1) return null;
    	// finished
    	return COLUMNS[nCol];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
		case 0: return Integer.class;
		case 1: return Double.class;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9: return String.class;
		default: return Object.class;
		}
	}
	
	/* =======================================================
	 * Public methods
	 * ======================================================= */
	
	public void load(Track track) {
		// forward
		clear();
		// set track
		m_track = track;
		// has track?
		if(track!=null) {
			// get point count
			int count = track.size();
			// is a valid track?
			if(count>1) {
				// initialize altitude difference
				double ead = 0.0;
				double h1 = track.get(0).getAltitude();
				// initialize decimal formats
				NumberFormat f10 = new DecimalFormat("#");
				NumberFormat f11 = new DecimalFormat("#.#");
				// calculate
				for(int i=1;i<count;i++) {
					// get next point
					TimePos p = track.get(i);
					// allocate memory
					Object[] row = new Object[COLUMNS.length];
					// update
					double h2 = p.getAltitude();
					double d = h2 - h1;
					h1 = h2;
					ead += d;
					row[0] = i;
					row[1] = d;
					row[2] = f10.format(track.getDistance(i-1, i, false)) + " m";
					row[3] = Utils.getTime((int)track.getDuration(i-1, i));
					row[4] = f11.format(track.getSpeed(i-1, i, false)*3.6) + " km/t";
					row[5] = f10.format(ead) + " m";
					row[6] = f10.format(track.getDistance(i-1, count-1, false)) + " m";
					row[7] = Utils.getTime((int)track.getReminder(i-1));
					row[8] = f11.format(track.getSpeed(i-1, count-1, false)*3.6) + " km/t";
					try {
						row[9] = MapUtil.getMGRSfromPosition(p.getPosition(), 3);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// add to rows
					m_rows.add(row);
				}
			}
		}
		fireTableDataChanged();
	}
	
	public void clear() {
		// forward
		m_rows.clear();
		// notify change
        fireTableDataChanged();
	}
	
	public Track getTrack() {
		return m_track;
	}

}

