package org.redcross.sar.wp.simulator;

import java.awt.Dimension;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.panel.BasePanel;

public class AssignmentsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	
	private AssignmentTable m_table = null;
	
	public AssignmentsPanel() {
		
		// forward
		super();
		
		// initialize gui
		initialize();
		
	}
	
	private void initialize() {
		// prepare base panel
		setHeaderVisible(false);
		setPreferredSize(new Dimension(300,100));
		setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED, 
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		// set body component
		setBodyComponent(getTable());
	}

	private AssignmentTable getTable() {
		if(m_table==null) {
			m_table = new AssignmentTable(Utils.getApp().getMsoModel());
		}
		return m_table;
	}
	
}
