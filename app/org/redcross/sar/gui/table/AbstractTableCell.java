/**
 * 
 */
package org.redcross.sar.gui.table;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableStringConverter;

import org.redcross.sar.gui.PopupManager;
import org.redcross.sar.gui.model.ITableModel;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractTableCell implements TableCellEditor, TableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	protected int m_cellRow;
	protected int m_cellColumn;
	protected JTable m_table;	
	protected Object m_value;
	
	protected int m_editCellRow;
	protected int m_editCellColumn;
	protected boolean m_hasCellFocus;
	protected boolean m_isCellSelected;
	protected JTable m_editTable;	
	protected Object m_editValue;
	protected boolean m_hasEditCellFocus;
	protected boolean m_isEditCellSelected;
	
	protected boolean m_isEditing;
	protected boolean m_isShowPopup;
	
	private TableIconConverter m_icons;
	private TableStringConverter m_strings;

	final protected ChangeEvent m_changeEvent = new ChangeEvent(this);;	
	final protected EventListenerList m_listeners = new EventListenerList();
	final protected PopupManager m_popupMananger = new PopupManager();
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */
	
	public AbstractTableCell() {
		
		// forward
		super();
		
		// register show popup actions
		addActionListener(m_popupMananger);
		
		// listen to menu item actions
		m_popupMananger.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(e);				
			}
			
		});
				
	}	
	
	/* ===============================================================
	 * Abstract methods
	 * =============================================================== */
	
	public abstract int getCellWidth(Graphics g, JTable table, int row, int col);
	
	protected abstract JComponent getEditorComponent();
	protected abstract JComponent getComponent();
		
	/* ===============================================================
	 * Public methods
	 * =============================================================== */
	
	public void setCellEditorValue(Object value) {
		m_value = value;
	}
	
	public int getCellRow() {
		return m_cellRow; 
	}
	
	public int getCellColumn() {
		return m_cellColumn; 
	}
	
	public int getEditCellRow() {
		return m_editCellRow; 
	}
	
	public int getEditCellColumn() {
		return m_editCellColumn; 
	}
	
	public boolean isCellSelected() {
		return m_isCellSelected;
	}
	
	public boolean isEditCellSelected() {
		return m_isEditCellSelected;
	}
	
	public JTable getTable() {
		return m_table;
	}
	
	public JTable getEditTable() {
		return m_editTable;
	}
	
	public boolean hasCellFocus() {
		return m_hasCellFocus;
	}
	
	public boolean hasEditCellFocus() {
		return m_hasEditCellFocus;
	}
	
	public boolean isEditing() {
		return m_isEditing;
	}
	
	public boolean isShowPopup() {
		return m_isShowPopup;
	}
	
	public void setShowPopup(boolean isShown) {
		m_isShowPopup = isShown;
	}
	
	public TableStringConverter getStringConverter() {
		return m_strings;
	}
	
	public void setStringConverter(TableStringConverter converter) {
		m_strings = converter;
	}
	
	public String getText() {
		String text = getText(m_table,m_cellRow,m_cellColumn); 
		// get text
		return text!=null ? text : ""; 			
	}
	
	public String getEditText() {
		String text = getText(m_editTable,m_editCellRow,m_editCellColumn); 
		// get text
		return text!=null ? text : ""; 			
	}
	
	public TableIconConverter getIconConverter() {
		return m_icons;
	}
	
	public void setIconConverter(TableIconConverter converter) {
		m_icons = converter;	
	}	
	
	public Icon getIcon() {
		// get icon
		return getIcon(m_table,m_cellRow,m_cellColumn);
	}	
	
	public Icon getEditIcon() {
		// get icon
		return getIcon(m_editTable,m_editCellRow,m_editCellColumn);
	}	
	
	public void addActionListener(ActionListener listener) {
		m_listeners.add(ActionListener.class, listener);		
	}
	
	public void removeActionListener(ActionListener listener) {
		m_listeners.remove(ActionListener.class, listener);		
	}
		
	/* ===============================================================
	 * CellEditor implementation
	 * =============================================================== */
	
	public Object getCellValue() {
		return m_value;
	}
	
	public Object getCellEditorValue() {
		return m_editValue;
	}

	public boolean isCellEditable(EventObject e) {
		return true;
	}

	public boolean shouldSelectCell(EventObject e) {
		return true;
	}

	public void cancelCellEditing() {
		fireEditingCanceled();
	}
	
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}
	
	public void addCellEditorListener(CellEditorListener listener) {
		m_listeners.add(CellEditorListener.class, listener);
	}

	public void removeCellEditorListener(CellEditorListener listener) {
		m_listeners.remove(CellEditorListener.class, listener);
	}
	
	/* ===============================================================
	 * PopupManager implementation (wrapper)
	 * =============================================================== */
	
	public JPopupMenu createPopupMenu(String name) {
		return m_popupMananger.createPopupMenu(name);
	}
	
	public boolean installPopup(String name, JComponent component, boolean onMouseAction) {
		return m_popupMananger.installPopup(name, component, onMouseAction);		
	}
	
	public JPopupMenu getPopupMenu(String name) {
		return m_popupMananger.getPopupMenu(name);		
	}	
	
	public JPopupMenu getPopupMenu(JComponent component) {
		return m_popupMananger.getPopupMenu(component);		
	}	
		
	public List<JComponent> getInstalledPopups(String menu) {
		return m_popupMananger.getInstalledPopups(menu);		
	}	
	
	public boolean addMenuItem(
			String menu, 
			String caption, 
			String name) {
		return m_popupMananger.addMenuItem(menu, caption, name);
	}
	
	public boolean addMenuItem(
			String menu, 
			String caption, 
			String icon, 
			String catalog,
			String name) {
		return m_popupMananger.addMenuItem(menu, caption, icon, catalog, name);
	}
		
	public boolean addMenuItem(String menu, Component item, String name) {
		return m_popupMananger.addMenuItem(menu, item, name);
	}
	
	public boolean removeMenuItem(String menu, String name) {
		return m_popupMananger.removeMenuItem(menu, name);
	}
	
	public Component findMenuItem(String menu, String name) {
		return m_popupMananger.findMenuItem(menu, name);
	}
	
	public boolean isMenuItemEnabled(String menu, String name) {
		return m_popupMananger.isMenuItemEnabled(menu, name);
	}
	
	public void setMenuItemEnabled(String menu, String name, boolean isEnabled) {
		m_popupMananger.setMenuItemEnabled(menu, name,isEnabled);
	}
		
	public boolean isMenuItemVisible(String menu, String name) {
		return m_popupMananger.isMenuItemVisible(menu, name);
	}
	
	public void setMenuItemVisible(String menu, String name, boolean isVisible) {
		m_popupMananger.setMenuItemVisible(menu, name,isVisible);
	}
			
	/* ===============================================================
	 * TableCellEditor implementation
	 * =============================================================== */
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		
		// prepare
		m_editValue = value;
		m_editCellRow = row;
		m_editCellColumn = column;
		m_isEditCellSelected = isSelected;
		m_editTable = table;
		m_isEditing = true;
		
		// draw buttons
		return getEditorComponent();
		
	}
	
	/* ===============================================================
	 * TableCellEditor implementation
	 * =============================================================== */
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		// prepare
		m_value = value;
		m_cellRow = row;
		m_cellColumn = column;
		m_isCellSelected = isSelected;
		m_table = table;
		m_isEditing = false;
		m_hasCellFocus = false;
		
		// draw buttons
		return getComponent();
		
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */
	
	protected int getColumnFixedWidth(JTable table, int column) {
		// check if width is fixed?
		if(table.getModel() instanceof ITableModel) {
			ITableModel model = (ITableModel)table.getModel();
			column = table.convertColumnIndexToModel(column);
			if(model.isColumnWidthFixed(column)) {
				return model.getColumnFixedWidth(column);
			} 
		}
		return -1;
	}

	protected void fireActionPerformed(ActionEvent e) {
		ActionListener[] list = m_listeners.getListeners(ActionListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].actionPerformed(e);
		}		
	}
	
	protected void fireEditingCanceled() {
		CellEditorListener[] list = m_listeners.getListeners(CellEditorListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].editingCanceled(m_changeEvent);
		}
	}

	protected void fireEditingStopped() {
		CellEditorListener[] list = m_listeners.getListeners(CellEditorListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].editingStopped(m_changeEvent);
		}
	}
	
	protected Icon getIcon(JTable table, int row, int col) {
		// is valid?
		if(table!=null && row!=-1 && row<table.getRowCount()&& col!=-1 && col<table.getColumnCount()) {			
			// use string converter?
			if(m_icons!=null) {
				// convert indexes to model
				row = table.convertRowIndexToModel(row);
				col = table.convertColumnIndexToModel(col);
				// get text
				return m_icons.toIcon(table.getModel(), row, col); 
			}
		}
		return null;
	}	
	
	protected String getText(JTable table, int row, int col) {
		// are table and indexes valid?
		if(table!=null && row!=-1 && row<table.getRowCount()&& col!=-1 && col<table.getColumnCount()) {			
			// use string converter?
			if(m_strings!=null) {
				// convert indexes to model
				row = table.convertRowIndexToModel(row);
				col = table.convertColumnIndexToModel(col);
				// get text
				return m_strings.toString(table.getModel(), row, col); 
			}
		}
		return null;
	}	
	
}
