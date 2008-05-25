/**
 * 
 */
package org.redcross.sar.gui.renderers;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.Iterator;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;


public class DiskoHeaderCellRenderer implements TableCellRenderer { 

	private static final long serialVersionUID = 1L;
	private JLabel m_wrap;

	/**
	 * Creates new cell renderer
	 * 
	 * @param TableCellRenderer wrap Wraps a JLabel with the TableCellRenderer interface implemented
	 * 
	 */
	public DiskoHeaderCellRenderer(TableCellRenderer wrap) { 
		m_wrap = (JLabel)wrap;
	} 
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		String text = (value == null) ? "" : value.toString(); 
		Vector vector = parseHeader(text); 
		Component com = ((TableCellRenderer)m_wrap).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col); 
		if (vector.size() == 1 && false) { 
			return ((TableCellRenderer)m_wrap).getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);			 
		}
		else {
			((JLabel)com).setText(generateHtml(vector) .toString());
			((JLabel)com).setIcon(null);
			return com;
		}
	}
	
	private StringBuffer generateHtml(Vector v) { 
		Iterator it = v.iterator(); 
		StringBuffer buffer = new StringBuffer(); 
		buffer.append("<html>"); 
		while (it.hasNext()) { 
			String s = (String) it.next(); 
			buffer.append(s); 
			buffer.append(""); 
		} 
		return buffer.append("</html>"); 
	}
	
	private Vector parseHeader(String str) { 
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