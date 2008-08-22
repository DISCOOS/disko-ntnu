package org.redcross.sar.gui.mso.table;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;

import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.util.mso.Track;

public class TrackTable extends DiskoTable {

	private static final long serialVersionUID = 1L;
	
	public TrackTable() {
		
		// forward
		super();
		
		// create the model
		TrackTableModel model = new TrackTableModel();
		
		// set the model
		setModel(model);
		
		// prepare header
        JTableHeader tableHeader = getTableHeader();
        tableHeader.setReorderingAllowed(false);        
        
        // prepare table
		setBorder(null);
        setRowHeight(20);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setShowGrid(false);
        setFillsViewportHeight(true);
        setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setColumnSelectionAllowed(false);
		
	}
	
	public void load(Track track) {
		((TrackTableModel)getModel()).load(track);
		
	}
	
	public Track getTrack() {
		return ((TrackTableModel)getModel()).getTrack();		
	}
	
}
