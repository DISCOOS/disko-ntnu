package org.redcross.sar.gui;

import java.awt.GridBagLayout;

import javax.swing.JList;
import javax.swing.JPanel;

import org.redcross.sar.app.Utils;

public class OperationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private DiskoPanel m_panel = null;
	private JList m_list = null;
	
	public OperationPanel() {
		// initialize GUI
		initialize();
	}
	
	/**
	 * Initialize this
	 */
	private void initialize() {
		this.setLayout(new GridBagLayout());
		this.add(getPanel());
	}
	
	/**
	 * Initialize the panel
	 */
	private JPanel getPanel() {
		if(m_panel == null) {
			m_panel = new DiskoPanel();
			m_panel.setBodyComponent(getList());
			m_panel.setCaptionText("Velg operasjon");
			Utils.setFixedSize(m_panel, 400, 300);
		}
		return m_panel;
	}
	
	/**
	 * Initialize the list 
	 */
	private JList getList() {
		if(m_list == null) {
			m_list = new JList();
		}
		return m_list;
	}
}
