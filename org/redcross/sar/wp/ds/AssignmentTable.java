package org.redcross.sar.wp.ds;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.model.DiskoTableColumnModel;
import org.redcross.sar.gui.table.AbstractTableCell;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.gui.table.DiskoTableHeader;
import org.redcross.sar.gui.table.TableCellButtons;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.TimePos;

import com.esri.arcgis.interop.AutomationException;

public class AssignmentTable extends DiskoTable {

	private static final long serialVersionUID = 1L;
		
	final private Icon checked = DiskoIconFactory.getIcon("GENERAL.FINISH", 
				DiskoButtonFactory.getCatalog(ButtonSize.TINY));
	final private Icon unchecked = DiskoIconFactory.getIcon("GENERAL.EMPTY", 
			 	DiskoButtonFactory.getCatalog(ButtonSize.TINY));
	
	private IDiskoMap map;
	private TableRowSorter<AssignmentTableModel> tableRowSorter;
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */
	
	@SuppressWarnings("unchecked")
	public AssignmentTable(IDsIf<RouteCost> ds, IDiskoMap map, boolean archived) {
		
		// forward
		super(new AssignmentTableModel(ds,archived));
		
		// prepare
		this.map = map;
		
		// create the model
		AssignmentTableModel model = (AssignmentTableModel)getModel();
		
		// assign the model
		setModel(model);
		
		// forward
		installHeader();
		
		// set string converter
		setStringConverter(new AssignmentStringConverter());
		
		// add row sorter
		tableRowSorter = new TableRowSorter<AssignmentTableModel>(model);
		tableRowSorter.setStringConverter(getStringConverter());
		tableRowSorter.setComparator(0, IAssignmentIf.ASSIGNMENT_COMPERATOR);
		tableRowSorter.setMaxSortKeys(1);
		tableRowSorter.setSortsOnUpdates(true);
		setRowSorter(tableRowSorter);
		
        // set default renderers
        setDefaultRenderer(Object.class, createRenderer());
        setDefaultRenderer(AssignmentStatus.class, createRenderer());
        setDefaultRenderer(Integer.class, createRenderer());
        setDefaultRenderer(Double.class, createRenderer());
        setDefaultRenderer(IAssignmentIf.class, createRenderer());
        setDefaultRenderer(IUnitIf.class, createRenderer());        
        setDefaultRenderer(AbstractButton.class, createEditor(false));
        
        // set default editors
        setDefaultEditor(AbstractButton.class, createEditor(true));
        
        // set layout
		setRowHeight(35);
		setColumnWidths();
		setAutoFitWidths(true);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setShowVerticalLines(false);
		setNoneColumnsVisible();
				
		if(archived)
			setVisibleColumns(AssignmentTableModel.ARCHIVED_COLUMNS, true);
		else
			setVisibleColumns(AssignmentTableModel.ACTIVE_COLUMNS, true);
						
		// set header alignments
		DiskoTableColumnModel columnModel = (DiskoTableColumnModel)getColumnModel();
		for(int i=0; i< getColumnCount();i++) {
			// set alignment
			switch(i) {
			case AssignmentTableModel.NAME_INDEX:				
			case AssignmentTableModel.UNIT_INDEX:
			case AssignmentTableModel.STATUS_INDEX:
				columnModel.setColumnAlignment(columnModel.getColumn(i,false),SwingConstants.LEFT);
				break;
			default:
				columnModel.setColumnAlignment(columnModel.getColumn(i,false),SwingConstants.RIGHT);
				break;
			}
			
		}
		
	}
	
	/* ===============================================================
	 * Public methods
	 * =============================================================== */
	
	public void install(RouteCostEstimator ds) {
		((AssignmentTableModel)getModel()).install(ds);
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */
	
	private void installHeader() {
		
		// get header
		final DiskoTableHeader header = (DiskoTableHeader)getTableHeader();

		// do not allow to reorder or resize columns
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		
		// install popup menu		
		header.createPopupMenu("actions");
		header.installEditorPopup("actions","button");
		header.addMenuItem("actions", "Rediger kolonner...", 
					"GENERAL.EDIT",DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
					"actions.edit.columns");
		header.addMenuItem("actions", new JSeparator(JSeparator.HORIZONTAL), 
				"actions.edit.separator1");
		header.addMenuItem("actions", "Fjern sortering", 
				"GENERAL.CANCEL",DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
				"actions.edit.sorting");
		header.addMenuItem("actions",  "Sorter kun en kolonne", 
				"GENERAL.FINISH", DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
				"actions.edit.togglemaxsort");
		
		// listen to editor actions
		header.addActionListener(new ActionListener() {
				
			public void actionPerformed(ActionEvent e) {
				
				headerActionPerformed(header,e);
			}
				
		});
		
	}
	
	private AssignmentCellRenderer createRenderer() {
		return new AssignmentCellRenderer();
	}
	
	private TableCellButtons createEditor(boolean actions) {
		final TableCellButtons editor = new TableCellButtons();
		editor.setEditorShown(true);
		editor.setIconConverter(new AssignmentIconConverter());
		editor.setStringConverter(new AssignmentStringConverter());
		if(actions) {
			editor.createPopupMenu("actions");
			editor.installEditorPopup("actions","button");
			editor.addMenuItem("actions", "Gå til oppdrag", 
					"MAP.CENTERAT",DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
					"actions.goto.assignment");
			editor.addMenuItem("actions", "Gå til enhets siste kjente posisjon", 
					"MAP.CENTERAT",DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
					"actions.goto.lkp");
			editor.addMenuItem("actions", "Gå til enhets estimerte posisjon", 
					"MAP.CENTERAT",DiskoButtonFactory.getCatalog(ButtonSize.TINY), 
					"actions.goto.ecp");
			// listen to editor actions
			editor.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					cellActionPerformed(editor,e);
				}
				
			});
		}
		return editor;
	}
	
	private void headerActionPerformed(DiskoTableHeader header, ActionEvent e) {
		// get action command
		String cmd = e.getActionCommand();
		// translate
		if("actions.edit.sorting".equals(cmd)) {			
			tableRowSorter.setSortKeys(null);
		}
		else if("actions.edit.togglemaxsort".equals(cmd)) {
			// get toggle state
			JMenuItem cb = (JMenuItem)e.getSource();
			if(tableRowSorter.getMaxSortKeys()>1) {
				int count = tableRowSorter.getSortKeys().size();
				int column = count > 1 && !tableRowSorter.getSortKeys().get(0)
					.getSortOrder().equals(SortOrder.UNSORTED) ?  tableRowSorter.getSortKeys().get(0).getColumn() : -1; 
				tableRowSorter.setMaxSortKeys(1);
				// reapply sort?
				if(column!=-1) {
					tableRowSorter.toggleSortOrder(column);
					tableRowSorter.toggleSortOrder(column);
				}
				cb.setIcon(checked);
			}
			else {
				tableRowSorter.setMaxSortKeys(3);
				cb.setIcon(unchecked);
			}
		}
	}
	
	private void cellActionPerformed(AbstractTableCell cell, ActionEvent e) {
		
		// get unit
		if(getModel() instanceof AssignmentTableModel) {
			// cast to AssignmentTableModel
			AssignmentTableModel model = (AssignmentTableModel)getModel();
			// get model indexes
			int row = convertRowIndexToModel(cell.getCellRow());
			// get action command
			String cmd = e.getActionCommand();
			// translate
			if("actions".equals(cmd)) {
				IAssignmentIf assignment = model.getAssignment(row);
				boolean isArchived = assignment.getStatus().ordinal()>AssignmentStatus.EXECUTING.ordinal();
				cell.setMenuItemVisible("actions", "actions.goto.lkp", model.getOwningUnit(row)!=null ? 
						!isArchived && model.getOwningUnit(row).getLastKnownPosition()!=null : false);
				cell.setMenuItemVisible("actions", "actions.goto.ecp", assignment.hasBeenStarted() && !isArchived);
			}
			else if("actions.goto.assignment".equals(cmd)) {
				centerAtMsoObject(model.getAssignment(row));
			}
			else if("actions.goto.lkp".equals(cmd)) {			
				centerAtMsoObject(model.getOwningUnit(row));
			}
			else if("actions.goto.ecp".equals(cmd)) {
				TimePos p = (TimePos)model.getValueAt(row,AssignmentTableModel.ECP_INDEX);
				centerAtPosition(p.getGeoPos());
			}
		}
	}
	
	private void centerAtPosition(final GeoPos p) {
		if(p!=null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						map.centerAtPosition(p);
						map.flashPosition(p);
					} catch (AutomationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					;
				}
			});
		}	
	}
	
	private void centerAtMsoObject(final IMsoObjectIf msoObj) {
		if(msoObj!=null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						map.centerAtMsoObject(msoObj);
						map.flashMsoObject(msoObj);
					} catch (AutomationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					;
				}
			});
		}		
	}
	
	private void setColumnWidths() {
		DiskoTableColumnModel model = (DiskoTableColumnModel)getColumnModel(); 
		for (int i = 0; i < model.getColumnCount(false); i++) {
			TableColumn column = model.getColumn(i,false);
			switch(i) {
			case AssignmentTableModel.NAME_INDEX:
				setColumnWidth(column, 175, true, true, false); break;
			case AssignmentTableModel.UNIT_INDEX:
				setColumnWidth(column, 100, true, true, false); break;
			case AssignmentTableModel.STATUS_INDEX:
				setColumnWidth(column, 100, true, true, false); break;
			case AssignmentTableModel.ETE_INDEX:
			case AssignmentTableModel.MTE_INDEX:
			case AssignmentTableModel.XTE_INDEX:
				setColumnWidth(column, 65, true, true, false); break;
			case AssignmentTableModel.ETA_INDEX:
			case AssignmentTableModel.MTA_INDEX:
			case AssignmentTableModel.XTA_INDEX:
				setColumnWidth(column, 55, true, true, false); break;
			case AssignmentTableModel.EDE_INDEX:
			case AssignmentTableModel.EDA_INDEX:
			case AssignmentTableModel.MDE_INDEX:
			case AssignmentTableModel.MDA_INDEX:
			case AssignmentTableModel.XDE_INDEX:
			case AssignmentTableModel.XDA_INDEX:
				setColumnWidth(column, 65, true, true, false); break;
			case AssignmentTableModel.ESE_INDEX:
			case AssignmentTableModel.ESA_INDEX:
			case AssignmentTableModel.MSE_INDEX:
			case AssignmentTableModel.MSA_INDEX:
			case AssignmentTableModel.XSE_INDEX:
			case AssignmentTableModel.XSA_INDEX:
				setColumnWidth(column, 55, true, true, false); break;
			}
		}
	}	
	
}
