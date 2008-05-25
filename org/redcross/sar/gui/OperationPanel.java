
package org.redcross.sar.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class OperationPanel extends DefaultDiskoPanel {

	private static final long serialVersionUID = 1L;

	private OperationTable m_table = null;
	
	public OperationPanel() {
		// forward
		super("Velg aktiv aksjon");
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
	private OperationTable getTable() {
		if(m_table == null) {
			m_table = new OperationTable();
			m_table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					// consume?
					if(!isChangeable()) return;
					// consume change
					setChangeable(false);
					// set dirty flag
					setDirty(e.getFirstIndex()>=0);					
					// resume change
					setChangeable(true);
				}
				
			});
			m_table.addKeyListener(new KeyAdapter() {

				public void keyTyped(KeyEvent e) {
					if(KeyEvent.VK_ENTER == e.getKeyCode())
						finish();
					else if(KeyEvent.VK_ESCAPE == e.getKeyCode())
						cancel();					
				}
				
			});
		}
		return m_table;
	}
	
	public String getSelectedOperation() {
		return m_table.getValueAt(m_table.getSelectedRow(),0).toString();
	}
	
	@Override
	public void update() {
		// forward
		super.update();
		// consume?
		if(!isChangeable()) return;
		// consume changes
		setChangeable(false);
		// update table
		getTable().update();
		// resume changes
		setChangeable(true);
		// select first?
		if(getTable().getRowCount()>0)
			getTable().getSelectionModel().setSelectionInterval(0, 0);		
	}
	
}
