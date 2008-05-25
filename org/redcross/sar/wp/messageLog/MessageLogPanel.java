package org.redcross.sar.wp.messageLog;

import org.redcross.sar.gui.map.MapStatusBar;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.layer.IMsoFeatureLayer.LayerCode;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: vinjar
 * Date: 25.jun.2007
 * To change this template use File | Settings | File Templates.
 */

/**
 * Panel containing all of message log work process
 */
public class MessageLogPanel
{
    public static final int PANEL_WIDTH = 800;

    private static final String MAP_ID = "MAP";
    private static final String LOG_ID = "LOG";

    private JPanel WorkspacePanel;
    private MessageLogBottomPanel m_messagePanel;
    private static JSplitPane m_splitter1;
    private static IDiskoWpMessageLog m_wpModule;
    private static IDiskoMap m_map;
    private static JTable m_logTable;
    private MessageRowSelectionListener m_rowSelectionListener;
    private static JScrollPane m_scrollPane1;
    private static JPanel m_tablePanel;

    /**
     * @param aWp Message log work process
     */
    public MessageLogPanel(IDiskoWpMessageLog aWp)
    {
        m_wpModule = aWp;
        m_map = m_wpModule.getMap();

        WorkspacePanel = new JPanel();
        WorkspacePanel.setLayout(new BorderLayout(0, 0));

        m_splitter1 = new JSplitPane();
        m_splitter1.setContinuousLayout(false);
        m_splitter1.setRequestFocusEnabled(true);
        m_splitter1.setOrientation(0);

        WorkspacePanel.add(m_splitter1, BorderLayout.CENTER);

        // get nav button
        JToggleButton navButton = aWp.getApplication().getUIFactory()
        	.getMainMenuPanel().getNavToggleButton();

        // add action listener
        navButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// is wp active?
				if(m_wpModule.isActive() && m_tablePanel!=null) {
					// get button
					final JToggleButton b = (JToggleButton)e.getSource();
	            	// toggle
					if(b.isSelected()){
						// run after this event has returned to ensure that the map 
						// is drawn in DiskoMap without bugged output
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								// show map
					        	CardLayout cards = (CardLayout) m_tablePanel.getLayout();
					            cards.show(m_tablePanel, MAP_ID);
							}							
						});
					}
					else {						
			        	CardLayout cards = (CardLayout) m_tablePanel.getLayout();
			            cards.show(m_tablePanel, LOG_ID);
			        }					
				}
				
			}
        	
        });
        
        initTablePanel();
        initMessagePanel();
    }

    private void initMessagePanel()
    {
        m_messagePanel = new MessageLogBottomPanel();
        m_messagePanel.setWp(m_wpModule);
        m_messagePanel.initialize(m_logTable);

        m_messagePanel.setMinimumSize(new Dimension(PANEL_WIDTH, MessageLogBottomPanel.PANEL_HEIGHT));
        m_messagePanel.setPreferredSize(new Dimension(PANEL_WIDTH, MessageLogBottomPanel.PANEL_HEIGHT));

        // Message panel should be informed of updates to MSO-model
        m_wpModule.getMsoEventManager().addClientUpdateListener(m_messagePanel);
        
        // Let row selection listener update message panel
        m_rowSelectionListener.setPanel(m_messagePanel);

        m_splitter1.setContinuousLayout(false);
        m_splitter1.setResizeWeight(1.0);
        m_splitter1.setRightComponent(m_messagePanel);
    }

    private void initTablePanel()
    {
    	m_tablePanel = new JPanel();
    	CardLayout layout = new CardLayout();
    	m_tablePanel.setLayout(layout);
    	m_tablePanel.setFocusCycleRoot(true);
    	m_splitter1.setLeftComponent(m_tablePanel);

        m_scrollPane1 = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_scrollPane1.setOpaque(false);

        m_map.setNorthBarVisible(true);
        m_map.setSouthBarVisible(true);

        m_tablePanel.add(m_scrollPane1, LOG_ID);
        m_tablePanel.add((JComponent)m_map, MAP_ID);

        m_logTable = new JTable();
        m_scrollPane1.setViewportView(m_logTable);

        m_rowSelectionListener = new MessageRowSelectionListener();
        m_logTable.getSelectionModel().addListSelectionListener(m_rowSelectionListener);

        final MessageTableModel model = new MessageTableModel(m_logTable, m_wpModule, m_rowSelectionListener);
        m_rowSelectionListener.setModel(model);
        m_logTable.setModel(model);
        m_logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_logTable.setCellSelectionEnabled(false);
        m_logTable.setRowSelectionAllowed(true);
        m_logTable.setColumnSelectionAllowed(false);
        m_logTable.setRowMargin(2);
        m_logTable.setRowHeight(22);

        // Register the log table with the row selection listener
        m_rowSelectionListener.setTable(m_logTable);

        // Set column widths
        TableColumn column = m_logTable.getColumnModel().getColumn(0);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH);
        column = m_logTable.getColumnModel().getColumn(1);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 10);
        column = m_logTable.getColumnModel().getColumn(2);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        column = m_logTable.getColumnModel().getColumn(3);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        column = m_logTable.getColumnModel().getColumn(5);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH * 2);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH * 2);
        column = m_logTable.getColumnModel().getColumn(6);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 40);
        column.setPreferredWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 40);

        // Init custom renderer
        m_logTable.setDefaultRenderer(Object.class, new MessageTableRenderer());

        JTableHeader tableHeader = m_logTable.getTableHeader();
        tableHeader.setResizingAllowed(false);
        tableHeader.setReorderingAllowed(false);
    }

    public void setLayersSelectable() {
        try {
        	// buffer changes
        	m_map.suspendNotify();
        	// disable selection
        	m_map.getMsoLayer(LayerCode.OPERATION_AREA_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.OPERATION_AREA_MASK_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.SEARCH_AREA_LAYER).setSelectable(false);
        	m_map.getMsoLayer(LayerCode.AREA_LAYER).setSelectable(false);
        	// enable selection
        	m_map.getMsoLayer(LayerCode.POI_LAYER).setSelectable(true);
        	m_map.getMsoLayer(LayerCode.UNIT_LAYER).setSelectable(true);
        	// resume events
        	m_map.resumeNotify();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
	}

    /**
     * @return Reference to entire panel
     */
    public JPanel getPanel()
    {
        setTableData();
        return WorkspacePanel;
    }

    public static void setTableData()
    {
        MessageTableModel ltm = (MessageTableModel) m_logTable.getModel();
        ltm.buildTable(null);
        ltm.fireTableDataChanged();
        /*int index = ltm.getRowCount()-1;
        m_logTable.getSelectionModel().setSelectionInterval(index, index);*/
    }

    /**
     * Hide all components
     */
    public void hidePanels()
    {
        m_messagePanel.hideEditPanels();
    }

    /**
     * Clear all button selections
     */
    public void clearSelection()
    {
        m_messagePanel.clearSelection();
    }

    /**
     * Makes map in bottom panel visible
     */
    public static void showMap()
    {
        // get nav button
        JToggleButton navButton = m_wpModule.getApplication().getUIFactory()
        	.getMainMenuPanel().getNavToggleButton();
        // is not selected?
        if(!navButton.isSelected()) {
        	navButton.doClick();
        }
    }

    /**
     * Hides bottom map behind message log table
     */
    public static void hideMap()
    {
        // get nav button
        JToggleButton navButton = m_wpModule.getApplication().getUIFactory()
        	.getMainMenuPanel().getNavToggleButton();
        // is not selected?
        if(navButton.isSelected()) {
        	navButton.doClick();
        }
    }

    /**
     * @return Work process map
     */
    public static IDiskoMap getMap()
    {
        return m_map;
    }
}
