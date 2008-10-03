
package org.redcross.sar.gui.panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.redcross.sar.gui.table.ColumnTable;

public class ColumnPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;

	private ColumnTable m_table = null;
	
	public ColumnPanel() {
		// forward
		super("Vel kolonner");
		// initialize GUI
		initialize();
	}
	
	/**
	 * Initialize this
	 */
	private void initialize() {
		// set table
		setBodyComponent(getTable());
	}
	
	/**
	 * Initialize the list 
	 */
	private ColumnTable getTable() {
		if(m_table == null) {
			m_table = new ColumnTable();
			m_table.getModel().addTableModelListener(new TableModelListener() {

				@Override
				public void tableChanged(TableModelEvent e) {
					// consume?
					if(!isChangeable()) return;
					// set dirty flag
					setDirty(true);					
				}
				
			});
			m_table.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if(KeyEvent.VK_ENTER == e.getKeyCode())
						finish();
					else if(KeyEvent.VK_ESCAPE == e.getKeyCode())
						cancel();					
				}

			});
			
		}
		return m_table;
	}
	
	public void edit(TableColumnModel model) {
		//getTable().lo
	}
		
}
