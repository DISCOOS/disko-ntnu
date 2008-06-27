package org.redcross.sar.wp.ds;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.model.MsoObjectTableModel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.wp.IDiskoWpModule;

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
			m_table.addMouseListener(new DiskoMouseAdapter() {

				@Override
				public void mouseDownExpired(MouseEvent e) {
					centerAtAssignment(getSelected());
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						centerAtAssignment(getSelected());
					}
				}

			});
		}
		return m_table;
	}	
	
	private IAssignmentIf getSelected() {
		IAssignmentIf assignment = null;
		int row = getTable().getSelectedRow();
		if(row>-1) {
			assignment = (IAssignmentIf)((MsoObjectTableModel<?>)
					getTable().getModel()).getMsoObject(row);
		}
		return assignment;	
	}
	
	private static void centerAtAssignment(IAssignmentIf assignment) {
		// get installed map
		IDiskoMap map = getInstalledMap();
		// has map?
		if(map!=null) {						
			try {
				// center at position?
				if(assignment!=null) {
					map.suspendNotify();
					map.clearSelected();
					map.setSelected(assignment, true);
					map.centerAtSelected();
					map.flashSelected();
					map.resumeNotify();
				}
				else
					Utils.showWarning("Ingen oppdrag funnet");
			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}
	
	private static IDiskoMap getInstalledMap() {
		// try to get map from current 
		IDiskoWpModule module = Utils.getApp().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}	
	
	public void setEstimator(RouteCostEstimator estimator) {
		getTable().setEstimator(estimator);
	}
	
	
}
