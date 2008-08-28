package org.redcross.sar.wp.logistics;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.gui.renderer.DiskoHeaderRenderer;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.event.IMsoLayerEventListener;
import org.redcross.sar.map.event.MsoLayerEvent;
import org.redcross.sar.map.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IDiskoLayer.LayerCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.util.MsoUtils;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 11.apr.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 *
 */
public class LogisticsPanel implements IMsoLayerEventListener
{

    private JPanel WorkspacePanel;
    private IDiskoMap m_map;
    private JPanel m_assignmentPanel;
    private JPanel m_unitPanel;
    private JPanel m_infoPanel;
    private JTable m_unitTable;
    private JSplitPane m_splitter1;
    private JSplitPane m_splitter2;
    private JSplitPane m_splitter3;
    private JScrollPane m_scrollPane1;
    private JScrollPane m_AssignmentSubPaneLeft;
    private JScrollPane m_AssignmentSubPaneRight;
    private IDiskoWpLogistics m_wpModule;
    private IUnitListIf m_unitList;
    private IAssignmentListIf m_assignmentList;

    private IUnitIf m_mapSelectedUnit;
    private IAssignmentIf m_mapSelectedAssignment;
    private AssignmentScrollPanel m_selectableAssignmentsPanel;
    private AssignmentScrollPanel m_priAssignmentsPanel;
    //private AssignmentTransferHandler m_assignmentTransferHandler;

    private InfoPanelHandler m_infoPanelHandler;

    AssignmentDisplayModel m_asgDisplayModel;
    UnitTableModel m_unitTableModel;
    private AssignmentLabel.AssignmentLabelActionHandler m_labelActionHandler;
    private AssignmentLabel.AssignmentLabelActionHandler m_listPanelActionHandler;

    private IconRenderer.IconActionHandler m_iconActionHandler;

    private boolean m_mapSelectedByButton = false;

    public LogisticsPanel(IDiskoWpLogistics aWp)
    {
        setupUI();
        m_wpModule = aWp;
        m_map = m_wpModule.getMap();

        m_unitList = m_wpModule.getCmdPost().getUnitList();
        m_assignmentList = m_wpModule.getCmdPost().getAssignmentList();

        /*if (!defineTransferHandler())
        {
            return;
        }*/
        defineSubpanelActionHandlers();

		MapPanel panel = new MapPanel(m_map);
		panel.setNorthBarVisible(true);
		panel.setSouthBarVisible(true);
        m_splitter3.setLeftComponent(panel);
        
//        setSplitters();
//        setPanelSizes();
        initUnitTable();
        initInfoPanels();
        initAssignmentPanels();
        addToListeners();
        WorkspacePanel.addComponentListener(new ComponentListener()
        {
            boolean initialized = false;

            public void componentResized(ComponentEvent e)
            {
            }

            public void componentMoved(ComponentEvent e)
            {
            }

            public void componentShown(ComponentEvent e)
            {
                if (!initialized)
                {
                    setSplitters();
                    setPanelSizes();
                    WorkspacePanel.validate();
                    initialized = true;
                }
            }

            public void componentHidden(ComponentEvent e)
            {
            }
        });
    }

    public void reInitPanel()
     {
         m_unitList = m_wpModule.getCmdPost().getUnitList();
         m_assignmentList = m_wpModule.getCmdPost().getAssignmentList();
         m_unitTableModel = (UnitTableModel) m_unitTable.getModel();
         m_unitTableModel.reInitModel(m_unitList);
         m_asgDisplayModel.reInitModel(m_assignmentList);
     }

    public void setLayersSelectable() {
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

    /*
    private boolean defineTransferHandler()
    {
        try
        {
            m_assignmentTransferHandler = new AssignmentTransferHandler(m_wpModule);
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return false;
        }
        return true;
    }
    */

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

    	//getUnitTable().clearSelection();
        setSelectedAssignmentInPanels(null);
        getInfoPanelHandler().setUnit(anUnit,true);
        
		// show progress
		m_map.showProgressor(true);
		
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

					// hide progress
					m_map.hideProgressor();
					
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
        
		// show progress
		m_map.showProgressor(true);
		
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

					// hide progress dialog
					m_map.hideProgressor();
					
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

    private void initAssignmentPanels()
    {
        m_AssignmentSubPaneRight.setMinimumSize(new Dimension(180, 0));
        m_AssignmentSubPaneLeft.setPreferredSize(new Dimension(180, 0));
        m_AssignmentSubPaneLeft.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        m_AssignmentSubPaneLeft.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        m_AssignmentSubPaneRight.setMinimumSize(new Dimension(60, 0));
        m_AssignmentSubPaneRight.setPreferredSize(new Dimension(60, 0));
        m_AssignmentSubPaneRight.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_AssignmentSubPaneRight.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        m_selectableAssignmentsPanel = new AssignmentScrollPanel(m_wpModule,m_AssignmentSubPaneLeft, new FlowLayout(FlowLayout.LEFT, 5, 5), m_labelActionHandler, true);
        //m_selectableAssignmentsPanel.setTransferHandler(m_assignmentTransferHandler);

        JLabel hl;
        hl = m_selectableAssignmentsPanel.getHeaderLabel();
        hl.setHorizontalAlignment(SwingConstants.CENTER);
        hl.setPreferredSize(new Dimension(40, 40));

        m_priAssignmentsPanel = new AssignmentScrollPanel(m_wpModule,m_AssignmentSubPaneRight, new FlowLayout(FlowLayout.LEFT, 5, 5), m_labelActionHandler, true);
        //m_priAssignmentsPanel.setTransferHandler(m_assignmentTransferHandler);
        hl = m_priAssignmentsPanel.getHeaderLabel();
        hl.setHorizontalAlignment(SwingConstants.CENTER);
        hl.setPreferredSize(new Dimension(40, 40));

        m_asgDisplayModel = new AssignmentDisplayModel(m_selectableAssignmentsPanel, m_priAssignmentsPanel, m_wpModule.getMsoEventManager(), m_assignmentList);
    }

    private void initUnitTable()
    {
        m_unitTableModel = new UnitTableModel(m_unitTable, m_wpModule, m_unitList, m_iconActionHandler);
        m_unitTable.setModel(m_unitTableModel);
        m_unitTable.setAutoCreateColumnsFromModel(true);
        m_unitTable.setDefaultRenderer(IconRenderer.UnitIcon.class, new LogisticsIconRenderer());
        m_unitTable.setDefaultRenderer(IconRenderer.AssignmentIcon.class, new LogisticsIconRenderer());
        m_unitTable.setDefaultRenderer(IconRenderer.InfoIcon.class, new LogisticsIconRenderer.InfoIconRenderer());
        m_unitTable.setShowHorizontalLines(false);
        m_unitTable.setShowVerticalLines(true);
        m_unitTable.setRowMargin(2);
        JTableHeader tableHeader = m_unitTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
        tableHeader.setDefaultRenderer(new DiskoHeaderRenderer(tableHeader.getDefaultRenderer()));
        m_unitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_unitTable.setCellSelectionEnabled(true);
        JTableHeader th = m_unitTable.getTableHeader();
        th.setPreferredSize(new Dimension(40, 40));

        //m_unitTable.setTransferHandler(m_assignmentTransferHandler);

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

        m_unitTable.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                /*m_unitTableModel.setSelectedCell(m_unitTable.getSelectionModel().getLeadSelectionIndex(),
                        m_unitTable.getColumnModel().getSelectionModel().
                                getLeadSelectionIndex());
                                */
            }

            public void focusLost(FocusEvent e)
            {
                //m_unitTable.clearSelection();
            }
        });
    }

    private void initInfoPanels()
    {
        m_infoPanelHandler = new InfoPanelHandler(m_infoPanel, m_wpModule, m_listPanelActionHandler);
        //m_infoPanelHandler.setSelectionTransferHandler(m_assignmentTransferHandler);
    }

    private void addToListeners()
    {
        //m_wpModule.getMsoEventManager().addClientUpdateListener(this);
        IMsoFeatureLayer msoLayer = m_map.getMsoLayer(LayerCode.AREA_LAYER);
        msoLayer.addMsoLayerEventListener(this);
    }

    private void setPanelSizes()
    {
        // minimum and preferred sizes are nice to have
        m_unitPanel.setMinimumSize(new Dimension(320, 600));
        m_unitPanel.setPreferredSize(new Dimension(320, 600));
        m_assignmentPanel.setMinimumSize(new Dimension(250, 600));
        m_assignmentPanel.setPreferredSize(new Dimension(250, 600));
        m_infoPanel.setMinimumSize(new Dimension(325, 200));
        m_infoPanel.setPreferredSize(new Dimension(325, 200));
    }

    private void setSplitters()
    {
        // Splitter between map/info panels and assignment/unit panels
        m_splitter1.setContinuousLayout(false);
        m_splitter1.setDividerLocation(Math.max(375, m_splitter1.getWidth() - 590));
        m_splitter1.setResizeWeight(1.0);

        // Splitter between assignment and unit panels
        m_splitter2.setContinuousLayout(false);
        m_splitter2.setDividerLocation(250);
        m_splitter2.setResizeWeight(1.0);

        // Splitter between map and info panels, make tha map initially a square
        m_splitter3.setContinuousLayout(false);
        m_splitter3.setDividerLocation(Math.max(375, m_splitter3.getHeight() - 280));
        m_splitter3.setResizeWeight(1.0);
    }

    /*
    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT,
            IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) 
	{
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
        return myInterests.contains(aMsoObject.getMsoClassCode());
    }

    public void handleMsoUpdateEvent(MsoEvent.Update e)
    {
    }
	*/
    
    public JPanel getPanel()
    {
        setTableData();
        return WorkspacePanel;
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

    private void setTableData()
    {
        UnitTableModel utm = (UnitTableModel) m_unitTable.getModel();
        utm.buildTable();
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


    private void setupUI()
    {
        WorkspacePanel = new JPanel();
        WorkspacePanel.setLayout(new BorderLayout(0, 0));
        m_splitter1 = new JSplitPane();
        m_splitter1.setContinuousLayout(false);
        m_splitter1.setRequestFocusEnabled(true);
        WorkspacePanel.add(m_splitter1, BorderLayout.CENTER);
        m_splitter2 = new JSplitPane();
        m_splitter2.setContinuousLayout(false);
        m_splitter1.setRightComponent(m_splitter2);
        m_unitPanel = new JPanel();
        m_unitPanel.setLayout(new BorderLayout(0, 0));
        m_unitPanel.setFocusCycleRoot(true);
        m_splitter2.setRightComponent(m_unitPanel);
        m_scrollPane1 = new JScrollPane();
        m_scrollPane1.setOpaque(false);
        m_unitPanel.add(m_scrollPane1, BorderLayout.CENTER);
        m_unitTable = new JTable();
        m_scrollPane1.setViewportView(m_unitTable);
        m_assignmentPanel = new JPanel();
        m_assignmentPanel.setLayout(new BorderLayout(5,5));
        m_splitter2.setLeftComponent(m_assignmentPanel);
        m_AssignmentSubPaneLeft = new JScrollPane();
        m_assignmentPanel.add(m_AssignmentSubPaneLeft, BorderLayout.CENTER);
        m_AssignmentSubPaneRight = new JScrollPane();
        m_AssignmentSubPaneRight.setRequestFocusEnabled(false);
        m_assignmentPanel.add(m_AssignmentSubPaneRight, BorderLayout.EAST);
        m_splitter3 = new JSplitPane();
        m_splitter3.setContinuousLayout(false);
        m_splitter3.setOrientation(0);
        m_splitter1.setLeftComponent(m_splitter3);
        m_infoPanel = new JPanel();
        m_infoPanel.setLayout(new CardLayout(0, 0));
        m_splitter3.setRightComponent(m_infoPanel);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        WorkspacePanel = new JPanel();
        WorkspacePanel.setLayout(new BorderLayout(0, 0));
        m_splitter1 = new JSplitPane();
        m_splitter1.setContinuousLayout(false);
        m_splitter1.setRequestFocusEnabled(true);
        WorkspacePanel.add(m_splitter1, BorderLayout.CENTER);
        m_splitter2 = new JSplitPane();
        m_splitter2.setContinuousLayout(false);
        m_splitter1.setRightComponent(m_splitter2);
        m_unitPanel = new JPanel();
        m_unitPanel.setLayout(new BorderLayout(0, 0));
        m_unitPanel.setFocusCycleRoot(true);
        m_splitter2.setRightComponent(m_unitPanel);
        m_scrollPane1 = new JScrollPane();
        m_scrollPane1.setOpaque(false);
        m_unitPanel.add(m_scrollPane1, BorderLayout.CENTER);
        m_unitTable = new JTable();
        m_scrollPane1.setViewportView(m_unitTable);
        m_assignmentPanel = new JPanel();
        m_assignmentPanel.setLayout(new BorderLayout(0, 0));
        m_splitter2.setLeftComponent(m_assignmentPanel);
        m_AssignmentSubPaneLeft = new JScrollPane();
        m_assignmentPanel.add(m_AssignmentSubPaneLeft, BorderLayout.CENTER);
        m_AssignmentSubPaneRight = new JScrollPane();
        m_AssignmentSubPaneRight.setRequestFocusEnabled(false);
        m_assignmentPanel.add(m_AssignmentSubPaneRight, BorderLayout.EAST);
        m_splitter3 = new JSplitPane();
        m_splitter3.setContinuousLayout(false);
        m_splitter3.setOrientation(0);
        m_splitter1.setLeftComponent(m_splitter3);
        m_infoPanel = new JPanel();
        m_infoPanel.setLayout(new CardLayout(0, 0));
        m_splitter3.setRightComponent(m_infoPanel);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return WorkspacePanel;
    }
}
