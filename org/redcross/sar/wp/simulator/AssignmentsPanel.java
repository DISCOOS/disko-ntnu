package org.redcross.sar.wp.simulator;

import java.awt.Dimension;
import java.awt.event.MouseEvent;

import org.redcross.sar.app.Application;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWpModule;

public class AssignmentsPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private boolean m_archived;
	private AssignmentTable m_table;

	public AssignmentsPanel(boolean archived) {

		// forward
		super();

		// prepare
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
		setContainer(getTable());
		// set scroll bar policies
		setScrollBarPolicies(
				BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
				BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
	}

	private AssignmentTable getTable() {
		if(m_table==null) {
			m_table = new AssignmentTable(Application.getInstance().getMsoModel(),m_archived);
			int width = m_table.getMinimumColumnTotalWidth();
			m_table.setMinimumSize(new Dimension(width,35));
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
			assignment = (IAssignmentIf)((AbstractMsoTableModel<?>)
					getTable().getModel()).getObject(row);
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
		IDiskoWpModule module = Application.getInstance().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}


}
