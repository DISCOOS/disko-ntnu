package org.redcross.sar.wp.logistics;

import com.esri.arcgis.interop.AutomationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.gui.table.DiskoTableHeader;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IMapLayer.LayerCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.logistics.UnitTableModel.UnitTableRowSorter;


/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 11.apr.2007
 * To change this template use File | Settings | File Templates.
 */

public class LogisticsPanel implements IMsoLayerEventListener
{

	final private Icon checked = DiskoIconFactory.getIcon("GENERAL.FINISH",
			DiskoButtonFactory.getCatalog(ButtonSize.TINY));
	final private Icon unchecked = DiskoIconFactory.getIcon("GENERAL.EMPTY",
		 	DiskoButtonFactory.getCatalog(ButtonSize.TINY));

	private JPanel m_contentPanel;
    private JSplitPane m_mainSplitter;

    private JSplitPane m_leftSplitter;
    private MapPanel m_mapPanel;
    private JPanel m_infoPanel;

    private JPanel m_rightPanel;
    private AssignmentTilesPanel m_selectableAssignmentsPanel;
    private AssignmentTilesPanel m_priAssignmentsPanel;
    private JScrollPane m_unitsScrollPane;
    private DiskoTable m_unitTable;

    private IDiskoMap m_map;
    private IDiskoWpLogistics m_wpModule;

    private IUnitListIf m_unitList;

    private IUnitIf m_mapSelectedUnit;
    private IAssignmentIf m_mapSelectedAssignment;

    private InfoPanelHandler m_infoPanelHandler;

    AssignmentDisplayModel m_assignmentDisplayModel;
    UnitTableModel m_unitTableModel;

    private AssignmentLabel.AssignmentLabelActionHandler m_labelActionHandler;
    private AssignmentLabel.AssignmentLabelActionHandler m_listPanelActionHandler;

    private IconRenderer.IconActionHandler m_iconActionHandler;

    private boolean m_mapSelectedByButton = false;

    public LogisticsPanel(IDiskoWpLogistics aWp)
    {
    	// prepare
        m_wpModule = aWp;
        m_map = m_wpModule.getMap();
        m_unitList = m_wpModule.getCmdPost().getUnitList();

        defineSubpanelActionHandlers();

        initialize();

        addToListeners();

    }

    public void reInitPanel()
    {
        m_unitList = m_wpModule.getCmdPost().getUnitList();
        m_unitTableModel = (UnitTableModel) m_unitTable.getModel();
        m_unitTableModel.getMsoBinder().setSelector(m_unitList);
        m_unitTableModel.load(m_unitList);
        m_assignmentDisplayModel.load();
    }

    public void setSelectableLayers() {
        try {
        	// buffer changes
        	m_map.suspendNotify();
        	// disable selection
        	m_map.getMsoLayer(LayerCode.OPERATION_AREA_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.OPERATION_AREA_MASK_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.SEARCH_AREA_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.POI_LAYER).setSelectable(false);
        	// enable selection
        	m_map.getMsoLayer(LayerCode.AREA_LAYER).setSelectable(true);
        	m_map.getMsoLayer(LayerCode.UNIT_LAYER).setSelectable(true);
        	// resume events
        	m_map.resumeNotify();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}

    private void defineSubpanelActionHandlers()
    {
        m_labelActionHandler = new AssignmentLabel.AssignmentLabelActionHandler()
        {
            public void handleClick(IAssignmentIf anAssignment)
            {
            	getUnitTable().clearSelection();
                singelAssignmentClick(anAssignment, false);
            }
        };

        m_listPanelActionHandler = new AssignmentLabel.AssignmentLabelActionHandler()
        {
            public void handleClick(IAssignmentIf anAssignment)
            {
                singelAssignmentClick(anAssignment, true);
            }
        };

        m_iconActionHandler = new IconRenderer.IconActionHandler()
        {
            public void handleClick(IUnitIf aUnit)
            {
            	singelUnitClick(aUnit);
            }

            public void handleClick(IAssignmentIf anAssignment)
            {
                singelAssignmentClick(anAssignment, false);
            }

            public void handleClick(IUnitIf aUnit, int aSelectorIndex)
            {
            	setSelectedAssignmentInPanels(null);
                getInfoPanelHandler().setUnitAssignmentSelection(aUnit, aSelectorIndex);
            }
        };
    }

    private void singelUnitClick(final IUnitIf anUnit)
    {

        setSelectedAssignmentInPanels(null);
        getInfoPanelHandler().setUnit(anUnit,true);

        // select later
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {

					// consume selection events
					m_mapSelectedByButton = true;

					// suspend events
					m_map.suspendNotify();

					// reset current?
					if (m_mapSelectedUnit != null) {
						m_map.setSelected(m_mapSelectedUnit, false);
						m_mapSelectedUnit = null;
					}

					// select next?
					if (anUnit != null) {
						m_mapSelectedUnit = anUnit;
						m_map.setSelected(m_mapSelectedUnit, true);
						m_map.zoomToMsoObject(m_mapSelectedUnit);
					}

					// resume events
					m_map.resumeNotify();

					// listen for selection events
					m_mapSelectedByButton = false;


				} catch (AutomationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
    }

    private void singelAssignmentClick(final IAssignmentIf anAssignment, boolean calledFromListPanel)
    {

    	setSelectedAssignmentInPanels(anAssignment);
        getInfoPanelHandler().setAssignment(anAssignment, calledFromListPanel);
		getInfoPanelHandler().setUnit(anAssignment.getOwningUnit(),false);

        // select later
        SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {

					// consume selection events
					m_mapSelectedByButton = true;

					// suspend events
					m_map.suspendNotify();

					// reset current?
					if (m_mapSelectedAssignment != null) {
						m_map.setSelected(m_mapSelectedAssignment, false);
						m_mapSelectedAssignment = null;
					}

					// select next?
					if (anAssignment.getPlannedArea() != null) {
						m_mapSelectedAssignment = anAssignment;
						m_map.suspendNotify();
						m_map.setSelected(m_mapSelectedAssignment, true);
						m_map.zoomToMsoObject(m_mapSelectedAssignment);
						m_map.resumeNotify();
					}

					// resume events
					m_map.resumeNotify();

					// handle selection events
					m_mapSelectedByButton = false;

				} catch (AutomationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
    }

    private void setSelectedAssignmentInPanels(IAssignmentIf anAssignment)
    {
        m_selectableAssignmentsPanel.setSelectedAssignment(anAssignment);
        m_priAssignmentsPanel.setSelectedAssignment(anAssignment);
    }

    private void initUnitTable()
    {
        m_unitTableModel = new UnitTableModel(m_unitTable, m_wpModule, m_iconActionHandler);
        m_unitTableModel.getMsoBinder().setSelector(m_unitList);
        m_unitTableModel.load(m_unitList);
        m_unitTable.setModel(m_unitTableModel);
        m_unitTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        m_unitTable.setDefaultRenderer(IconRenderer.UnitIcon.class, new LogisticsIconRenderer());
        m_unitTable.setDefaultRenderer(IconRenderer.AssignmentIcon.class, new LogisticsIconRenderer());
        m_unitTable.setDefaultRenderer(IconRenderer.InfoIcon.class, new LogisticsIconRenderer.InfoIconRenderer());
        m_unitTable.setShowHorizontalLines(false);
        m_unitTable.setShowVerticalLines(true);
        m_unitTable.setRowMargin(2);
        m_unitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_unitTable.setCellSelectionEnabled(true);
        TableColumnModel columns = m_unitTable.getColumnModel();
        DiskoTable.setColumnWidth(columns.getColumn(0), 66, true, true, true);
        DiskoTable.setColumnWidth(columns.getColumn(1), 66, true, true, true);
        DiskoTable.setColumnWidth(columns.getColumn(2), 66, true, true, true);
        DiskoTable.setColumnWidth(columns.getColumn(3), 66, true, true, true);
        DiskoTable.setColumnWidth(columns.getColumn(4), 66, true, true, true);
        DiskoTable.setColumnWidth(columns.getColumn(5), 66, true, true, false);

        final DiskoTableHeader header = m_unitTable.getDiskoTableHeader();
        header.setResizingAllowed(false);
        header.setReorderingAllowed(false);
        header.setFixedHeight(40);
        header.createPopupMenu("actions");

		// install popup menu
		header.createPopupMenu("actions");
		header.installEditorPopup("actions","button");
		header.addMenuItem("actions", "Fjern sortering",
				"GENERAL.CANCEL",DiskoButtonFactory.getCatalog(ButtonSize.TINY),
				"actions.edit.sorting");
		header.addMenuItem("actions",  "Sorter kun en kolonne",
				"GENERAL.FINISH", DiskoButtonFactory.getCatalog(ButtonSize.TINY),
				"actions.edit.togglemaxsort");

		// listen to editor actions
		header.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				// get row sorter
				UnitTableRowSorter tableRowSorter = m_unitTableModel.getRowSorter();

				// get action command
				String cmd = e.getActionCommand();

				// translate
				if("actions.edit.sorting".equals(cmd)) {
					tableRowSorter.clearSort();
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

		});



        ListSelectionListener l = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting() || !m_unitTable.hasFocus())
                {
                    return;
                }
                boolean isSelected = !m_unitTable.getSelectionModel().isSelectionEmpty();
                int row = m_unitTable.getSelectionModel().getLeadSelectionIndex();
                int col = m_unitTable.getColumnModel().getSelectionModel().getLeadSelectionIndex();
                m_unitTableModel.setSelectedCell(row,col,isSelected);

            }
        };
        m_unitTable.getSelectionModel().addListSelectionListener(l);
        m_unitTable.getColumnModel().getSelectionModel().addListSelectionListener(l);

    }

    private void addToListeners()
    {
        IMsoFeatureLayer msoLayer = m_map.getMsoLayer(LayerCode.AREA_LAYER);
        msoLayer.addMsoLayerEventListener(this);
    }

    public JPanel getPanel()
    {
        return m_contentPanel;
    }

    public IDiskoMap getMap()
    {
        return m_map;
    }

    public InfoPanelHandler getInfoPanelHandler()
    {
        return m_infoPanelHandler;
    }

    public JTable getUnitTable()
    {
        return m_unitTable;
    }

    public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException
    {
    	if(!e.isFinal()) return;
        List<IMsoObjectIf> selection = e.getSelectedMsoObjects();
        if (selection != null && selection.size() > 0)
        {
            IMsoObjectIf msoObject = selection.get(0);
            if (msoObject instanceof IAreaIf)
            {
            	IAreaIf area = MsoUtils.getOwningArea(msoObject);
            	if(area!=null) {
	                IAssignmentIf assignment = area.getOwningAssignment();
	                if (assignment != null)
	                {
	                    if (!m_mapSelectedByButton)
	                    {
	                    	m_mapSelectedAssignment = assignment;
		                    if(!m_unitTableModel.setSelected(assignment, MsoLayerEventType.SELECTED_EVENT.equals(e.getEventType()))) {
			                    setSelectedAssignmentInPanels(m_mapSelectedAssignment);
		                        getInfoPanelHandler().setAssignment(m_mapSelectedAssignment, false);
		                    }
		                    m_map.zoomToMsoObject(m_mapSelectedAssignment);
	                    }
	                }
            	}
            }
            else if(msoObject instanceof IUnitIf) {
                if (!m_mapSelectedByButton)
                {
                    m_unitTableModel.setSelected(msoObject, MsoLayerEventType.SELECTED_EVENT.equals(e.getEventType()));
                	m_map.zoomToMsoObject(msoObject);
                }
            }
        }
        else {
        	m_mapSelectedAssignment = null;
        	m_unitTable.clearSelection();
            setSelectedAssignmentInPanels(null);
            getInfoPanelHandler().setUnit(null, false);
            getInfoPanelHandler().setAssignment(null, false);
        }
    }


    private void initialize()
    {

    	// create content panel
        m_contentPanel = new JPanel();
        m_contentPanel.setLayout(new BorderLayout(0, 0));
        m_contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create main splitter (map and info on left, assignments and units on the right)
        m_mainSplitter = new JSplitPane();
        m_mainSplitter.setBorder(BorderFactory.createEmptyBorder());
        m_mainSplitter.setResizeWeight(1.0);
        m_mainSplitter.setContinuousLayout(false);
        m_mainSplitter.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        m_contentPanel.add(m_mainSplitter, BorderLayout.CENTER);

        // create left splitter
        m_leftSplitter = new JSplitPane();
        m_leftSplitter.setBorder(BorderFactory.createEmptyBorder());
        m_leftSplitter.setResizeWeight(1.0);
        m_leftSplitter.setContinuousLayout(false);
        m_leftSplitter.setOrientation(JSplitPane.VERTICAL_SPLIT);
        m_mainSplitter.setLeftComponent(m_leftSplitter);

        // create north part of left side of main split
		m_mapPanel = new MapPanel(m_map,true);
		m_mapPanel.setMinimumSize(new Dimension(200, 200));
        m_mapPanel.setPreferredSize(new Dimension(200, 200));
		m_mapPanel.setNorthBarVisible(true);
		m_mapPanel.setSouthBarVisible(true);
        m_leftSplitter.setTopComponent(m_mapPanel);

        // create south part of left side of main split
        m_infoPanel = new JPanel();
        m_infoPanel.setMinimumSize(new Dimension(200, 400));
        m_infoPanel.setPreferredSize(new Dimension(200, 400));
        m_infoPanel.setLayout(new CardLayout(0, 0));
        m_leftSplitter.setBottomComponent(m_infoPanel);
        m_infoPanelHandler = new InfoPanelHandler(m_infoPanel, m_wpModule, m_listPanelActionHandler);

        // create right side panel of main split
        m_rightPanel = new JPanel();
        m_rightPanel.setBorder(null);
        m_rightPanel.setLayout(new BoxLayout(m_rightPanel,BoxLayout.X_AXIS));
        m_mainSplitter.setRightComponent(m_rightPanel);

    	// prepare right content
        JPanel assignments = new JPanel();
        assignments.setLayout(new BoxLayout(assignments,BoxLayout.X_AXIS));
        m_selectableAssignmentsPanel = new AssignmentTilesPanel(m_wpModule, new SpringLayout(), 5, 5, false, m_labelActionHandler, true);
        m_selectableAssignmentsPanel.getHeaderPanel().setPreferredSize(new Dimension(40, 40));
        m_selectableAssignmentsPanel.setMinimumSize(new Dimension(124, 100));
        m_selectableAssignmentsPanel.setMaximumSize(new Dimension(124, Integer.MAX_VALUE));
        m_selectableAssignmentsPanel.setScrollBarPolicies(
        		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        m_priAssignmentsPanel = new AssignmentTilesPanel(m_wpModule, new SpringLayout(), 5, 5, false, m_labelActionHandler, true);
        m_priAssignmentsPanel.getHeaderPanel().setPreferredSize(new Dimension(40, 40));
        m_priAssignmentsPanel.setMinimumSize(new Dimension(62, 100));
        m_priAssignmentsPanel.setPreferredSize(new Dimension(62, 100));
        m_priAssignmentsPanel.setMaximumSize(new Dimension(62, Integer.MAX_VALUE));
        m_priAssignmentsPanel.setNotScrollBars();

        // create east part of right side of main split
        assignments.add(m_selectableAssignmentsPanel);
        assignments.add(Box.createHorizontalStrut(5));
        assignments.add(m_priAssignmentsPanel);

        m_rightPanel.add(assignments);
        m_rightPanel.add(Box.createHorizontalStrut(5));

        // create west part of right side of main split
        m_unitTable = new DiskoTable(true);
        m_unitTable.setFocusCycleRoot(true);
        m_unitsScrollPane = UIFactory.createScrollPane(m_unitTable);
        m_unitsScrollPane.setBorder(UIFactory.createBorder());
        m_unitsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_unitsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        m_unitsScrollPane.setOpaque(false);
        Utils.setFixedWidth(m_unitsScrollPane, 400);
        m_rightPanel.add(m_unitsScrollPane);

    	// prepare table
        initUnitTable();

        // prepare display model
        m_assignmentDisplayModel = new AssignmentDisplayModel(m_selectableAssignmentsPanel, m_priAssignmentsPanel, m_wpModule.getMsoModel());
        m_rightPanel.addComponentListener(m_assignmentDisplayModel);

        // reset splitter divider locations
        m_mainSplitter.resetToPreferredSizes();
        m_leftSplitter.resetToPreferredSizes();



    }

}
