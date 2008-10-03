package org.redcross.sar.wp.ds;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;

import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.model.DsObjectTableModel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWpModule;

public class AssignmentsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;
	
	private boolean m_archived;
	
	private IDiskoMap m_map;
	private AssignmentTable m_table;
	
	public AssignmentsPanel(IDiskoMap map, boolean archived) {
		
		// forward
		super();
		
		// prepare
		m_map = map;
		m_archived = archived;
		
		// initialize gui
		initialize();
		
	}
	
	private void initialize() {
		// prepare base panel
		setHeaderVisible(false);
		Dimension d = new Dimension(getTable().getMinimumColumnTotalWidth(),100);
		setPreferredSize(d);
		setMinimumSize(d);
		// set body component
		setBodyComponent(getTable());
	}

	private AssignmentTable getTable() {
		if(m_table==null) {
			m_table = new AssignmentTable(null,m_map,m_archived);
			int width = m_table.getMinimumColumnTotalWidth();
			m_table.setMinimumSize(new Dimension(width,35));
			m_table.addMouseListener(new DiskoMouseAdapter() {

				@Override
				public void mouseDownExpired(MouseEvent e) {
					centerAtAssignment(getSelected(e.getPoint()));
				}

				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()>1) {
						centerAtAssignment(getSelected(e.getPoint()));
					}
				}

			});
		}
		return m_table;
	}	
	
	private IAssignmentIf getSelected(Point p) {
		IAssignmentIf assignment = null;
		int row = getTable().rowAtPoint(p);
		if(row>-1) {
			assignment = (IAssignmentIf)((DsObjectTableModel<?>)
					getTable().getModel()).getDsObject(row).getId();
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
					map.centerAtMsoObject(assignment);
					map.flashMsoObject(assignment);
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
	
	public void install(RouteCostEstimator ds) {
		getTable().install(ds);
	}
	
	
}
