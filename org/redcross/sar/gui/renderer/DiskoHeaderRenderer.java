/**
 * 
 */
package org.redcross.sar.gui.renderer;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;
import java.util.Iterator;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class DiskoHeaderRenderer extends HeaderPanel implements TableCellRenderer { 

	private static final long serialVersionUID = 1L;

	private boolean vertical;
	
	public DiskoHeaderRenderer() {
		this(ButtonSize.SMALL,false);
	}
	
	public DiskoHeaderRenderer(boolean vertical) {
		this(ButtonSize.SMALL,vertical);
	}
	
	public DiskoHeaderRenderer(ButtonSize buttonSize, boolean vertical) {
		super("",buttonSize);
		this.vertical = vertical;
		//addButton("toggleSort","");
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		String text = (value == null) ? "" : value.toString(); 
		Vector<String> vector = parseHeader(text);
		if (vector.size() == 1 && false) {
			setCaptionText(text);
		}
		else {
			setCaptionText(generateHtml(vector).toString());
		}
		if(table!=null) setBorder(col,table.getColumnCount());
		requestFocusInWindow(hasFocus);		
		return this;
	}
	
	private void setBorder(int col, int count) {
		if(col>0 && col==count-1) 
			setInsets(0, 0, 1, 0);
		else if(col>0) 
			setInsets(0, 0, 1, vertical ? 1 : 0);  
		else if(count==1)
			setInsets(0, 0, 1, 0);
		else
			setInsets(0, 0, 1, vertical ? 1 : 0);
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