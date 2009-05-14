package org.redcross.sar.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.Scrollable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.gui.model.DiskoTableColumnModel;
import org.redcross.sar.gui.model.IDiskoTableModel;
import org.redcross.sar.gui.renderer.DiskoTableCellRenderer;
import org.redcross.sar.util.Utils;

public class DiskoTable extends JTable {

	private static final long serialVersionUID = 1L;

	protected boolean m_autoFitWidths;

	protected TableStringConverter m_converter;

	/* ====================================================
	 * Constructors
	 * ==================================================== */

	public DiskoTable() {
		// forward
		super();
		// initialize GUI
		initialize(false);
	}

	public DiskoTable(boolean showVerticalHeaderLines) {
		// forward
		super();
		// initialize GUI
		initialize(showVerticalHeaderLines);
	}

	public DiskoTable(TableModel model) {
		// forward
		super();
		// initialize GUI
		initialize(false);
		// set model
		setModel(model);
	}

	public DiskoTable(TableModel model, boolean showVerticalHeaderLines) {
		// forward
		super();
		// initialize GUI
		initialize(showVerticalHeaderLines);
		// set model
		setModel(model);
	}

	private void initialize(boolean showVerticalHeaderLines) {

		// prepare
		setRowHeight(22);
		setBorder(BorderFactory.createEmptyBorder());
		setAutoFitWidths(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		setTableHeader(new DiskoTableHeader(showVerticalHeaderLines));
		setDefaultRenderer(Object.class, new DiskoTableCellRenderer());
		setColumnModel(new DiskoTableColumnModel());

		// auto create is not supported
		autoCreateColumnsFromModel = false;

		// add mouse listener
		addMouseMotionListener(new MouseMotionAdapter() {

			int m_editRow = -1;
			int m_editCol = -1;

			@Override
			public void mouseMoved(MouseEvent e) {

				int row = rowAtPoint(e.getPoint());
				int col = columnAtPoint(e.getPoint());

				if(row!=-1 && col!=-1 &&
					(m_editRow!=row || m_editCol!=col)) {
					 TableCellRenderer r = getCellRenderer(row, col);
					 if(r instanceof AbstractTableCell) {
						 m_editCol = col;
						 m_editRow = row;
						 editCellAt(row, col);
					 }
				}

			}

		});
		/* =================================================
		 * BUG-FIX: When autoFitWidthColumns is true,
		 * the table columns are auto fitted when the 
		 * table is shown for the first time. This bug-fix
		 * resolves the issue by implementing a one-shot
		 * column auto fit using a component listener.
		 * ================================================= */
		// add a one-shot refresh listener
		addHierarchyBoundsListener(new HierarchyBoundsListener() {

			@Override
			public void ancestorMoved(HierarchyEvent e) { fire(); }
			@Override
			public void ancestorResized(HierarchyEvent e) { fire(); }
			
			private void fire() {
				if(getGraphics()!=null) {
					if(isAutoFitWidths()) {
						autoFitWidthColumns();
					}
					removeHierarchyBoundsListener(this);
				}
			}
			
		});
	}

	/* ====================================================
	 * Overridden methods
	 * ==================================================== */	
	
	@Override
	public void setModel(TableModel model) {
		// forward
		super.setModel(model);
		// forward?
		if(!autoCreateColumnsFromModel) createDefaultColumnsFromModel();
	}

	@Override
	public final void setAutoCreateColumnsFromModel(boolean autoCreateColumnsFromModel) {
		// Notify
		throw new UnsupportedOperationException("AutoCreateColumnsFromModel not supported");
	}

	/**
     * Returns false to indicate that horizontal scrollbars are required
     * to display the table while honoring perferred column widths. Returns
     * true if the table can be displayed in viewport without horizontal
     * scrollbars. For more information, see Java bug report #1027936.
     *
     * @return true if an auto-resizing mode is enabled and the viewport
     * width is larger than the table's preferred size, otherwise return false.
     *
     * @see Scrollable#getScrollableTracksViewportWidth
     *
     */

	@Override
    public boolean getScrollableTracksViewportWidth() {
	   	if (autoResizeMode != AUTO_RESIZE_OFF) {
	 	    if (getParent() instanceof JViewport) {
	 	    	return (((JViewport)getParent()).getWidth() > getMinimumSize().width);
	 	    }
	 	}
	 	return super.getScrollableTracksViewportWidth();
    }

	@Override
    public Dimension getMinimumSize() {
        // Use correct resizing behavior
        if (getParent() instanceof JViewport) {
 	    	JViewport viewPort = (JViewport)getParent();
 	    	int count = getRowCount();
 	    	int height = 0;
 	    	int overflowHeight = viewPort.getHeight();
 	    	for(int i=0;i<count;i++) {
 	    		int h = getRowHeight(i);
 	    		height += getRowHeight(i);
 	    		if(height+h>overflowHeight) {
 	    			break;
 	    		}
 	    		height += h;
 	    	}
        	return new Dimension(getPreferredColumnTotalWidth(),height);
        }
        return super.getMinimumSize();
    }

	@Override
	public void tableChanged(TableModelEvent e) {
		// forward
		super.tableChanged(e);
		// auto fit column widths?
		if(m_autoFitWidths) autoFitWidthColumns();
	}

	@Override
	public void setRowSorter(RowSorter<? extends TableModel> sorter) {
		// forward
		super.setRowSorter(sorter);
	}

	@Override
	public void createDefaultColumnsFromModel() {
		// keep visible state on persistent columns?
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			// get column model
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			// get column count
			int count = model.getColumnCount(false);
			// initialize
			Map<Object, Boolean> states = new HashMap<Object, Boolean>(count);
			// get current visible columns
			for(int i=0;i<count;i++) {
				TableColumn column = model.getColumn(i, false);
				states.put(column.getIdentifier(),model.isColumnVisible(column));
			}
			// forward
			super.createDefaultColumnsFromModel();
			// apply old visible states to persistent columns
			// get current visible columns
			for(Object it : states.keySet()) {
				int index = model.getColumnIndex(it, false);
				if(index!=-1) {
					TableColumn column = model.getColumn(index, false);
					model.setColumnVisible(column, states.get(it));
				}
			}
		}
		else {
			// allow default operation
			super.createDefaultColumnsFromModel();
		}
	}

	/* ====================================================
	 * Public methods
	 * ==================================================== */

	public DiskoTableHeader getDiskoTableHeader() {
		if(getTableHeader() instanceof DiskoTableHeader)
			return (DiskoTableHeader)getTableHeader();
		return null;
	}

	public int getColumnTotalWidth() {
		int width = 0;
		Enumeration<TableColumn> c = getColumnModel().getColumns();

		while(c.hasMoreElements()) {
			width += c.nextElement().getWidth();
		}

		return width;
	}

	public int getMinimumColumnTotalWidth() {
		int width = 0;
		Enumeration<TableColumn> c = getColumnModel().getColumns();

		while(c.hasMoreElements()) {
			width += c.nextElement().getMinWidth();
		}

		return width;
	}

	public int getPreferredColumnTotalWidth() {
		int width = 0;
		Enumeration<TableColumn> c = getColumnModel().getColumns();

		while(c.hasMoreElements()) {
			width += c.nextElement().getPreferredWidth();
		}
		return width;
	}

	public boolean isColumnVisible(int col) {
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			col = convertColumnIndexToModel(col);
			return model.isColumnVisible(model.getColumn(col));
		}
		return true;
	}

	/**
	 * Show/hide array of columns
	 * @param ids - array of column indexes.
	 * @param isVisible - show or hide column
	 */
	public void setVisibleColumns(String[] names, boolean isVisible) {
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			for(int i=0; i<names.length;i++) {
				int col = model.getColumnIndex(names[i]);
				if(col!=-1) {
					model.setColumnVisible(model.getColumn(col),isVisible);
				}
			}
		}
	}

	/**
	 * Show/hide array of columns
	 * @param idx - array of column indexes.
	 * @param isVisible - show or hide column
	 */
	public void setVisibleColumns(Integer[] idx, boolean isVisible) {
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			for(int i=0; i<idx.length;i++) {
				TableColumn column = model.getColumnByModelIndex(idx[i]);
				if(column!=null) {
					model.setColumnVisible(column,isVisible);
				}
			}
		}
	}

	/**
	 * Hide all columns in table
	 */
	public void setNoneColumnsVisible() {
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			model.setNoneColumnsVisible();
		}
	}

	/**
	 * Show all columns in table
	 */
	public void setAllColumnsVisible() {
		if(getColumnModel() instanceof DiskoTableColumnModel) {
			DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel();
			model.setAllColumnsVisible();
		}
	}

	public void autoFitWidthColumns() {
		// get models
		TableModel data = getModel();
		TableColumnModel columns = getColumnModel();
		// set flags
		boolean setMax = false;
		boolean isDTM = (data instanceof IDiskoTableModel);
		// cast to IDiskoTableModel?
		IDiskoTableModel model = isDTM?(IDiskoTableModel)data:null;
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
				// get maximum width
				if(!isDTM || model.getColumnFixedWidth(i)<0) {
					// reset maximum column width flag
					setMax = false;
					// loop over all rows?
					for(int j=0;j<jCount;j++) {
						// maximize width
						max = Math.max(max,getCellWidth(g,j,i));
					}
				} else {
					// use fixed width
					max = Math.max(max,model.getColumnFixedWidth(i));
					// set maximum column width flag
					setMax = true;
				}
				// set width
				setColumnWidth(column, max, true, true, setMax);
				
			}
		}
	}

	public boolean isAutoFitWidths() {
		return m_autoFitWidths;
	}
		
	public void setAutoFitWidths(boolean isEnabled) {
		m_autoFitWidths = isEnabled;
	}

	public TableStringConverter getStringConverter() {
		return m_converter;
	}

	public void setStringConverter(TableStringConverter converter) {
		m_converter = converter;
	}

	public static void setColumnWidth(TableColumn column, int width, boolean min, boolean preferred, boolean max) {
		if(column!=null) {
			if(min) column.setMinWidth(width);
			if(preferred) column.setPreferredWidth(width);
			if(max) column.setMaxWidth(width);
		}
	}

	/* ====================================================
	 * Helper methods
	 * ==================================================== */

	private int getHeaderWidth(Graphics g, TableColumn column, int index) {
		Object value = column.getHeaderValue();
		if(getTableHeader() instanceof DiskoTableHeader) {
			return ((DiskoTableHeader)getTableHeader()).getRendererWidth(g,index);
		}
		return Utils.getStringWidth(g,value);

	}

	private int getCellWidth(Graphics g, int row, int col) {
		// are indexes valid?
		if(row!=-1 && row<getRowCount()&& col!=-1 && col<getColumnCount()) {
			Object value = getValueAt(row, col);
			TableCellRenderer renderer = getCellRenderer(row, col);
			if(renderer instanceof AbstractTableCell) {
				return ((AbstractTableCell)renderer).getCellWidth(g,this,row,col);
			}
			String text = "";
			Component c = renderer.getTableCellRendererComponent(this, value, false, false, row, col);
			if(value instanceof Icon) {
				return ((Icon)value).getIconWidth()+5;
			}
			else {
				// prepare
				text = value!=null ? value.toString() : "";
				if(m_converter!=null) {
					row = convertRowIndexToModel(row);
					col = convertColumnIndexToModel(col);
					text = m_converter.toString(getModel(), row, col);
				}
			}
			return Utils.getStringWidth(g,c.getFont(),text) +
			(c instanceof JLabel && ((JLabel)c).getIcon()!=null
					? ((JLabel)c).getIcon().getIconWidth() + ((JLabel)c).getIconTextGap() : 0) + 10;
		}
		return 0;
	}

}
