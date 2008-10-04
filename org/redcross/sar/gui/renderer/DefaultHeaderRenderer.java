/**
 * 
 */
package org.redcross.sar.gui.renderer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.Iterator;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;

import org.redcross.sar.gui.model.DiskoTableColumnModel;
import org.redcross.sar.gui.model.IDiskoTableModel;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.util.Utils;

public class DefaultHeaderRenderer implements TableHeaderRenderer { 

	private static final long serialVersionUID = 1L;

	private final HeaderPanel m_panel;  
	
	private boolean m_vertical;
	private String m_editor;
	private ButtonSize m_buttonSize;
	
	
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */
	
	public DefaultHeaderRenderer() {
		this(ButtonSize.TINY,false);
	}
	
	public DefaultHeaderRenderer(boolean drawStrut) {
		this(ButtonSize.TINY,drawStrut);
	}
	
	public DefaultHeaderRenderer(ButtonSize buttonSize, boolean vertical) {
		m_panel = new HeaderPanel("",buttonSize);
		m_vertical = vertical;
		m_buttonSize = buttonSize;
		initialize();
	}
	
	private void initialize() {
		m_panel.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT", m_buttonSize), "button");
		m_panel.setButtonVisible("button", false);
		m_panel.addButton(new JCheckBox(),"checkbox");
		m_panel.setButtonVisible("checkbox", false);
		setEditor("button");		
	}

	/* ===============================================================
	 * TableHeaderRenderer implementation
	 * =============================================================== */
	
	public int getWidth(Graphics g, JTable table, int row, int col) {
		Object value = table.getColumnModel().getColumn(col).getHeaderValue();
		getTableCellRendererComponent(table,value,false,false,row, col);
		Icon icon = m_panel.getCaptionIcon();
		String text = m_panel.getCaptionText();
		int w = (icon!=null ? icon.getIconWidth() : 0) 
			  + Utils.getStringWidth(g, Utils.stripHtml(text))
			  + m_panel.getButtonsPanel().getTotalItemWidth(false);
		return w;
	}
	
	public void doClick() {
		m_panel.getButton(m_editor).doClick();
	}
	
	public void doClick(String name) {
		m_panel.getButton(name).doClick();
	}

	public JComponent getComponent() {
		return m_panel;
	}
	
	public String getEditor() {
		return m_editor;
	}
	
	public void setEditor(String name) {
		if(m_editor!=name) {
			if(m_editor!=null) {
				getEditorComponent(m_editor).setVisible(false);
			}
			m_editor = name;
		}
	}
	
	public JComponent getEditorComponent(String name) {
		return m_panel.getButton(name);
	}
	
	public int getDefaultHeight() {
		return DiskoButtonFactory.getButtonSize(m_buttonSize).height;
	}
	
	public void setBounds(Rectangle bounds) {
		m_panel.setBounds(bounds);
	}
	
	public void addActionListener(ActionListener listener) {
		m_panel.addActionListener(listener);		
	}

	public void removeActionListener(ActionListener listener) {
		m_panel.removeActionListener(listener);		
	}	
	
	public boolean isEditorVisible() {
		return getEditorComponent(m_editor).isVisible();
	}
	
	public void setEditorVisible(boolean isVisible) {
		getEditorComponent(m_editor).setVisible(isVisible);
	}
	
	/* ===============================================================
	 * TableCellRenderer implementation
	 * =============================================================== */
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		String text = (value == null) ? "" : value.toString(); 
		Vector<String> vector = parseHeader(text);
		if (vector.size() == 1 && false) {
			m_panel.setCaptionText(text);
		}
		else {
			m_panel.setCaptionText(generateHtml(vector).toString());
		}
		if(table!=null) {
			setBorder(col,table.getColumnCount());
			if(table.getModel() instanceof IDiskoTableModel) {
				IDiskoTableModel model = (IDiskoTableModel)table.getModel();
				col = table.convertColumnIndexToModel(col);
				setEditor(model.getHeaderEditor(col));
				setEditorVisible(model.isHeaderEditable(col));
			}
			if(table.getColumnModel() instanceof DiskoTableColumnModel) {
				DiskoTableColumnModel model = (DiskoTableColumnModel)table.getColumnModel();
				m_panel.setCaptionAlignment(model.getColumnAlignment(model.getColumn(col,false)));
			}
		}
		return m_panel;
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */
	
	private void setBorder(int col, int count) {
		if(col>0 && col==count-1) 
			m_panel.setInsets(0, 0, 1, 0);
		else if(col>0) 
			m_panel.setInsets(0, 0, 1, m_vertical ? 1 : 0);  
		else if(count==1)
			m_panel.setInsets(0, 0, 1, 0);
		else
			m_panel.setInsets(0, 0, 1, m_vertical ? 1 : 0);
	}
	
	private StringBuffer generateHtml(Vector<String> v) { 
		Iterator<String> it = v.iterator(); 
		StringBuffer buffer = new StringBuffer(); 
		buffer.append("<html>"); 
		while (it.hasNext()) { 
			String s = it.next(); 
			buffer.append(s); 
			buffer.append(""); 
		} 
		return buffer.append("</html>");
	}
	
	private Vector<String> parseHeader(String str) { 
		BufferedReader br = null; 
		br = new BufferedReader(new StringReader(str)); 
		String line; 
		Vector<String> v = new Vector<String>(); 
		try { 
			while ((line = br.readLine()) != null) { 
				v.addElement(line);
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
		return v; 
	}

	
}