
package org.redcross.sar.gui.panel;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.gui.OperationTable;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class OperationPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;

	private JButton m_createButton = null;
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
		// insert button
		insertButton("finish", getCreateButton(), "create");
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
					// set dirty flag
					setDirty(e.getFirstIndex()>=0,false);					
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
			m_table.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						finish();
					}
				}

			});
			
		}
		return m_table;
	}
	
	/**
	 * Initialize the create button
	 */
	private JButton getCreateButton() {
		if(m_createButton == null) {
			m_createButton = DiskoButtonFactory.createButton("SYSTEM.CREATE", ButtonSize.NORMAL);
		}
		return m_createButton;
	}
	
	@Override
	public void setPreferredBodySize(Dimension dimension) {
		getTable().setPreferredSize(dimension);
		getTable().setPreferredScrollableViewportSize(dimension);
		getScrollPane().setPreferredSize(dimension);
	}
	
	public String getSelectedOperation() {
		return m_table.getValueAt(m_table.getSelectedRow(),0).toString();
	}
	
	@Override
	public void update() {
		// consume changes
		setChangeable(false);
		// update table
		getTable().update();
		// set dirty
		setDirty(true, false);
		// resume changes
		setChangeable(true);
		// try to set focus on table
		getTable().requestFocus();
		// forward
		super.update();
	}
	
}
