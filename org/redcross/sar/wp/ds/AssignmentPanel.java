package org.redcross.sar.wp.ds;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.EnumSet;

import org.redcross.sar.Application;
import org.redcross.sar.data.Selector;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoBinder;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.IDiskoWpModule;

public class AssignmentPanel extends BasePanel {

	private static final long serialVersionUID = 1L;

	private int m_buffer;

	private IDiskoMap m_map;

	private Integer[] m_columns;
	private AssignmentTable m_table;
	private AssignmentTableModel m_data;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AssignmentPanel(IDiskoMap map, int buffer) {

		// forward
		super();

		// prepare
		m_map = map;
		m_buffer = buffer;

		// initialize GUI
		initialize();

	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public void connect(IMsoModelIf model) {
		MsoBinder<IAssignmentIf> binder = null;
		switch(m_buffer) {
		case 0:
			binder = createMsoBinder(model, IAssignmentIf.PENDING_SET, IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
			break;
		case 1:
			binder = createMsoBinder(model, IAssignmentIf.WORKING_SET, IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
			break;
		case 2:
			binder = createMsoBinder(model, IAssignmentIf.FINISHED_AND_REPORTED_SET, IAssignmentIf.ASSIGNMENT_TYPE_NUMBER_COMPERATOR);
			break;
		}
		m_data.connect(binder);
		binder.load(model.getMsoManager().getCmdPost().getAssignmentList());
	}

	public void connect(RouteCostEstimator model) {
		switch(m_buffer) {
		case 0:
			m_data.connect(model, createDsSelector(IAssignmentIf.PENDING_SET), RouteCost.ASSIGNMENT_COMPERATOR);
			break;
		case 1:
			m_data.connect(model, createDsSelector(IAssignmentIf.WORKING_SET), RouteCost.ASSIGNMENT_COMPERATOR);
			break;
		case 2:
			m_data.connect(model, createDsSelector(IAssignmentIf.FINISHED_AND_REPORTED_SET), RouteCost.ASSIGNMENT_COMPERATOR);
			break;
		}
		m_data.load();
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private void initialize() {
		// prepare base panel
		setHeaderVisible(false);
		Dimension d = new Dimension(getTable().getMinimumColumnTotalWidth(),100);
		setPreferredSize(d);
		setMinimumSize(d);
		// set body component
		setContainer(getTable());
	}

	private AssignmentTable getTable() {
		if(m_table==null) {

			// get columns
			switch(m_buffer) {
			case 0:
				m_columns = AssignmentTableModel.PENDING_COLUMNS;
				break;
			case 1:
				m_columns = AssignmentTableModel.ACTIVE_COLUMNS;
				break;
			case 2:
				m_columns = AssignmentTableModel.ARCHIVED_COLUMNS;
				break;
			}

			// create table
			m_table = new AssignmentTable(m_map);
			m_table.setVisibleColumns(m_columns, true);

			// prepare table model
			m_data = (AssignmentTableModel)m_table.getModel();
			m_data.setHeaderEditable(m_columns[m_columns.length-1], true);

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
			assignment = ((AssignmentTableModel)getTable().getModel()).getId(row);
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
					map.centerAt(assignment);
					map.flash(assignment);
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

	private static Selector<RouteCost> createDsSelector(final EnumSet<AssignmentStatus> select) {
		Selector<RouteCost> selector = new Selector<RouteCost>() {

			@Override
			public boolean select(RouteCost anObject) {
				if(anObject.getId() instanceof IAssignmentIf) {
					IAssignmentIf a = (IAssignmentIf)anObject.getId();
					return select.contains(a.getStatus());
				}
				return false;
			}

		};
		return selector;
	}

	private static MsoBinder<IAssignmentIf> createMsoBinder(IMsoModelIf source,
			EnumSet<AssignmentStatus> select, Comparator<IAssignmentIf> comparator) {

		MsoBinder<IAssignmentIf> binder = new MsoBinder<IAssignmentIf>(IAssignmentIf.class);
		binder.setSelector(createMsoSelector(select));
		binder.setComparator(comparator);
		binder.connect(source);
		return binder;
	}

	private static Selector<IAssignmentIf> createMsoSelector(final EnumSet<AssignmentStatus> select) {
		Selector<IAssignmentIf> selector = new Selector<IAssignmentIf>() {

			@Override
			public boolean select(IAssignmentIf anObject) {
				return select.contains(anObject.getStatus());
			}

		};
		return selector;
	}

}
