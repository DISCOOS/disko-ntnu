
package org.redcross.sar.gui.panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class ListSelectorPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;

	private JList m_list = null;
	
	public ListSelectorPanel() {
		this("",true,true,ButtonSize.SMALL);
	}
	
	public ListSelectorPanel(String caption, boolean finish, boolean cancel, ButtonSize buttonSize) {
		// forward
		super(caption,finish,cancel,buttonSize);
		// initialize GUI
		initialize();
		
	}
	/**
	 * Initialize this
	 */
	private void initialize() {
		// set table
		setBodyComponent(getList());		
	}
	
	/**
	 * Initialize the list 
	 */
	public JList getList() {
		if(m_list == null) {
			m_list = new JList();
			m_list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					// consume?
					if(!isChangeable()) return;
					// set dirty flag
					setDirty(e.getFirstIndex()>=0,false);					
				}
				
			});
			m_list.addKeyListener(new KeyAdapter() {

				@Override
				public void keyPressed(KeyEvent e) {
					if(KeyEvent.VK_ENTER == e.getKeyCode())
						finish();
					else if(KeyEvent.VK_ESCAPE == e.getKeyCode())
						cancel();					
				}

			});
			m_list.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						finish();
					}
				}

			});
			
		}
		return m_list;
	}
	
	public Object getSelected() {
		return m_list.getSelectedValue();
	}
	
	@Override
	public void update() {
		// try to set focus on table
		getList().requestFocus();
		// forward
		super.update();
	}
	
}
