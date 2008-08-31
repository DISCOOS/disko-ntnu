package org.redcross.sar.gui.table;

import javax.swing.JTable;

import org.redcross.sar.gui.renderer.DiskoHeaderRenderer;

public class DiskoTable extends JTable {

	private static final long serialVersionUID = 1L;
	
	/* ====================================================
	 * Constructors
	 * ==================================================== */
	public DiskoTable() {
		// initialize GUI
		initialize();
	}
	
	private void initialize() {
		setBorder(null);
		getTableHeader().setDefaultRenderer(new DiskoHeaderRenderer());
	}
	
	/* ====================================================
	 * Public methods
	 * ==================================================== */
	
	/*
	public void autoWidthColumns() {
		// get models
		TableModel data = getModel();
		TableColumnModel columns = getColumnModel();
		// get column count
		int iCount = columns.getColumnCount();
		// get row count
		int jCount = data.getRowCount();
		// get graphics object			
		Graphics g = getGraphics();
		// has graphics object?
		if(g!=null) {
			// loop over all columns
			for(int i=0;i<iCount;i++) {
				// get column
				TableColumn column = columns.getColumn(i);				
				// get header width
				int max = getHeaderWidth(g,column,i);
				// loop over all rows
				for(int j=0;j<jCount;j++) {
					// maximize width
					max = Math.max(max, getCellWidth(g, data, j, i));
				}
				
				// increase width
	 			max = max + 20;
				
				// set preferred width
	 			column.setWidth(max);
				column.setMinWidth(max);
				column.setPreferredWidth(max);
				column.setMaxWidth(max);
				//System.out.println(column.getWidth());
				//column.setResizable(true);
			}
		}
	}
	
	private int getHeaderWidth(Graphics g, TableColumn column, int index) {
		Component c = null;
		Object value = column.getHeaderValue();
		if(column.getCellRenderer()!=null) {
			c = column.getCellRenderer().getTableCellRendererComponent(this, value, false, false, 0, index);
			return c.getWidth();
		}
		return getStringWidth(g, value);
			
	}
	
	private int getCellWidth(Graphics g, TableModel data, int row, int col) {
		Component c = null;
		Object value = data.getValueAt(row, col);
		if(getCellRenderer(row, col)!=null) {
			c = getCellRenderer(row, col).getTableCellRendererComponent(this,value,false,false,row,col);
			return c.getWidth();
		}
		return getStringWidth(g,value);
	}
	
	private int getStringWidth(Graphics g, Object value) {
		return (value!=null ? g.getFontMetrics().stringWidth(value.toString()) : 0);
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		// auto fit column widths
		autoWidthColumns();
		// forward
		super.tableChanged(e);
	}
	*/
	

}
