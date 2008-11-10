package org.redcross.sar.wp.messageLog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.menu.MainMenu;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.gui.table.DiskoTableHeader;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.layer.IDiskoLayer.LayerCode;
import org.redcross.sar.mso.data.IMessageIf;

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

    private JPanel m_contentPanel;
    private static String m_current = LOG_ID;
    private MessageLogBottomPanel m_messagePanel;
    private static JSplitPane m_splitter1;
    private static IDiskoWpMessageLog m_wpModule;
    private static IDiskoMap m_map;
    private static DiskoTable m_logTable;
    private static JScrollPane m_scrollPane;
    private static JPanel m_tablePanel;
    private static TableRowSorter<MessageTableModel> m_rowSorter;

    /**
     * @param aWp Message log work process
     */
    public MessageLogPanel(IDiskoWpMessageLog aWp)
    {
        m_wpModule = aWp;
        m_map = m_wpModule.getMap();

        m_contentPanel = new JPanel();
        m_contentPanel.setLayout(new BorderLayout(0, 0));

        m_splitter1 = new JSplitPane();
        m_splitter1.setContinuousLayout(false);
        m_splitter1.setRequestFocusEnabled(true);
        m_splitter1.setOrientation(0);
        m_splitter1.setBorder(BorderFactory.createEmptyBorder());

        m_contentPanel.add(m_splitter1, BorderLayout.CENTER);

        // get nav menu
        MainMenu menu = aWp.getApplication().getUIFactory().getMainMenu();

        // add action listener
        menu.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// get action command
				String cmd = e.getActionCommand();
				// handle?
				if("SYSTEM.NAV".equals(cmd) && m_wpModule.isActive() && m_tablePanel!=null) {
					// get button
					final JToggleButton b = (JToggleButton)e.getSource();
	            	// toggle
					if(b.isSelected()){
						// show map
			        	CardLayout cards = (CardLayout) m_tablePanel.getLayout();
			            cards.show(m_tablePanel, MAP_ID);
			            m_current = MAP_ID;
			            m_map.setVisible(true);
					}
					else {
			            m_map.setVisible(false);
			        	CardLayout cards = (CardLayout) m_tablePanel.getLayout();
			            cards.show(m_tablePanel, LOG_ID);
			            m_current = LOG_ID;
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

		MapPanel panel = new MapPanel(m_map);
		panel.setNorthBarVisible(true);
		panel.setSouthBarVisible(true);
        m_tablePanel.add(panel, MAP_ID);

        m_logTable = new DiskoTable();

        final MessageTableModel model = new MessageTableModel(m_logTable, m_wpModule);
        m_logTable.setModel(model);
        m_logTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_logTable.setCellSelectionEnabled(false);
        m_logTable.setRowSelectionAllowed(true);
        m_logTable.setColumnSelectionAllowed(false);
        m_logTable.setRowHeight(28);

        // listen for mouse events
        m_logTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				int row = m_logTable.rowAtPoint(e.getPoint());
				int col = m_logTable.columnAtPoint(e.getPoint());
				if(row!=-1 && (e.getClickCount()==1 && col==0 || e.getClickCount()>1)) {
					IMessageIf message = model.getMessage(row);
					if(message!=null) {
						// get expanded/collapsed state
				        Boolean expanded = model.isMessageExpanded(message.getObjectId());
				        expanded = (expanded == null ? false : !expanded);
				        model.setMessageExpanded(message.getObjectId(),expanded);
				        model.updateRowHeights();
					}
					MessageLogBottomPanel.showListPanel();
				}
			}

		});

        // register a row selection listener
        m_logTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {

				// consume?
				if(e.getValueIsAdjusting()) return;

				// get selection model
				ListSelectionModel m = (ListSelectionModel)e.getSource();

				// force current selection?
				if(m_logTable.getSelectedColumn()==0) {
					IMessageIf message = MessageLogBottomPanel.getCurrentMessage(false);
					if(message!=null) {
						int row = model.findRow(message.getObjectId());
						if(row!=-1) {
							m.setSelectionInterval(row, row);
							return;
						}
					}
				}

				// Get selected row index
				int row = m.getMinSelectionIndex();

				// convert to model
				row = m_logTable.convertRowIndexToModel(row);

				// get selected message
		        IMessageIf message = (row!=-1 ?
		        		(IMessageIf)model.getMessage(row) : null);

				// Update top message panel
				MessageLogBottomPanel.newMessageSelected(message);
				MessageLogBottomPanel.showListPanel();

		        // update table
				m_logTable.repaint();

			}

        });

        // listen to key events
        m_logTable.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()) {
				case KeyEvent.VK_SPACE: {
					int row = m_logTable.convertRowIndexToModel(m_logTable.getSelectedRow());
					if(row!=-1) {
						IMessageIf message = model.getMessage(row);
						MessageLogBottomPanel.newMessageSelected(message);
						if(message!=null) {
							// get expanded/collapsed state
					        Boolean expanded = model.isMessageExpanded(message.getObjectId());
					        expanded = (expanded == null ? false : !expanded);
					        model.setMessageExpanded(message.getObjectId(),expanded);
					        model.updateRowHeights();
					    }
						MessageLogBottomPanel.showListPanel();
					}
					break;

				}
				case KeyEvent.VK_ESCAPE:
					int row = m_logTable.convertRowIndexToModel(m_logTable.getSelectedRow());
					if(row!=-1) {
						IMessageIf message = model.getMessage(row);
						model.setMessageExpanded(message.getObjectId(), false);
					}
					m_logTable.clearSelection();
					MessageLogBottomPanel.newMessageSelected(null);
					MessageLogBottomPanel.showListPanel();
					model.updateRowHeights();
				}
			}

		});

        // Set column widths
        TableColumn column = m_logTable.getColumnModel().getColumn(0);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH*2);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH*2);
        column = m_logTable.getColumnModel().getColumn(1);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 10);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 10);
        column = m_logTable.getColumnModel().getColumn(2);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        column = m_logTable.getColumnModel().getColumn(3);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH + 1);
        // Column 4 is flexible
        column = m_logTable.getColumnModel().getColumn(5);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH * 2);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH * 2);
        column = m_logTable.getColumnModel().getColumn(6);
        column.setMinWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH+65);
        column.setMaxWidth(MessageLogBottomPanel.SMALL_PANEL_WIDTH+65);

        // initialize custom renderers
        m_logTable.setDefaultRenderer(Object.class, new MessageCellRenderer());

        // initialize sorting
        installSort(model);

        // initialize header
        installHeader();

        // create table scroll pane
        m_scrollPane = UIFactory.createScrollPane(m_logTable, true,
        		ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_scrollPane.setOpaque(false);

        // add table scroll pane to card layout
        m_tablePanel.add(m_scrollPane, LOG_ID);

        // forward
        hideMap();

    }

    private void installSort(MessageTableModel model) {

        // create table sorter
        m_rowSorter = new TableRowSorter<MessageTableModel>(model);
        m_rowSorter.setComparator(0, IMessageIf.MESSAGE_NUMBER_COMPARATOR);
        m_rowSorter.setStringConverter(new MessageStringConverter());
        m_rowSorter.setMaxSortKeys(1);
        m_rowSorter.setSortsOnUpdates(true);
        m_rowSorter.toggleSortOrder(0); // ascending
        m_rowSorter.toggleSortOrder(0); // descending
        m_logTable.setRowSorter(m_rowSorter);

    }

	private void installHeader() {

		// create icons
		final Icon checked = DiskoIconFactory.getIcon("GENERAL.FINISH",
				DiskoButtonFactory.getCatalog(ButtonSize.TINY));
		final Icon unchecked = DiskoIconFactory.getIcon("GENERAL.EMPTY",
			 	DiskoButtonFactory.getCatalog(ButtonSize.TINY));

		// get header
		final DiskoTableHeader header = (DiskoTableHeader)m_logTable.getTableHeader();

		// do not allow to reorder or resize columns
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

		// set editor
		int column = m_logTable.getColumnCount()-1;
		header.setEditable(column, true);

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

				// get action command
				String cmd = e.getActionCommand();
				// translate
				if("actions.edit.sorting".equals(cmd)) {
					m_rowSorter.setSortKeys(null);
				}
				else if("actions.edit.togglemaxsort".equals(cmd)) {
					// get toggle state
					JMenuItem cb = (JMenuItem)e.getSource();
					if(m_rowSorter.getMaxSortKeys()>1) {
						int count = m_rowSorter.getSortKeys().size();
						int column = count > 1 && !m_rowSorter.getSortKeys().get(0)
							.getSortOrder().equals(SortOrder.UNSORTED) ?  m_rowSorter.getSortKeys().get(0).getColumn() : -1;
							m_rowSorter.setMaxSortKeys(1);
						// reapply sort?
						if(column!=-1) {
							m_rowSorter.toggleSortOrder(column);
							m_rowSorter.toggleSortOrder(column);
						}
						cb.setIcon(checked);
					}
					else {
						m_rowSorter.setMaxSortKeys(3);
						cb.setIcon(unchecked);
					}
				}
			}

		});

        m_rowSorter.addRowSorterListener(new RowSorterListener() {

			public void sorterChanged(RowSorterEvent e) {
				header.repaint();
			}

        });

	}

    public void setSelectableLayers() {
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
        return m_contentPanel;
    }

    /**
     * Hide all components
     */
    public void hidePanels()
    {
    	MessageLogBottomPanel.hideEditPanels(false);
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
		// is wp active?
		if(m_wpModule.isActive() && m_tablePanel!=null) {
	        // show nav menu
	        m_wpModule.getApplication().getUIFactory().setNavMenuVisible(true);
			// show map
        	m_current = MAP_ID;
	    	CardLayout cards = (CardLayout) m_tablePanel.getLayout();
	        cards.show(m_tablePanel, MAP_ID);
        	SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// hide map
			        m_map.setVisible(true);
				}
			});
		}
    }

    /**
     * Hides bottom map behind message log table
     */
    public static void hideMap()
    {
		// is allowed?
		if(m_tablePanel!=null) {
	        // hide nav menu
	        m_wpModule.getApplication().getUIFactory().setNavMenuVisible(false);
			// hide map
			m_map.setVisible(false);
			m_current = LOG_ID;
    		CardLayout cards = (CardLayout) m_tablePanel.getLayout();
    		cards.show(m_tablePanel, LOG_ID);
		}
    }

    /**
     * @return Work process map
     */
    public static IDiskoMap getMap()
    {
        return m_map;
    }

    public static boolean isMapShown() {
    	return (m_current == MAP_ID);
    }
}
