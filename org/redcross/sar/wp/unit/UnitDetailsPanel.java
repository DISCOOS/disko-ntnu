package org.redcross.sar.wp.unit;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.ResourceBundle;

/**
 * JPanel displaying unit details
 *
 * @author thomasl
 */
public class UnitDetailsPanel extends JPanel implements IMsoUpdateListenerIf, ITickEventListenerIf
{
    private static final long serialVersionUID = 1L;

    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

    private IUnitIf m_currentUnit;

    private JLabel m_topPanelLabel;
    private JToggleButton m_pauseToggleButton;
    private JButton m_releaseButton;
    private JButton m_showReportButton;

    private JTextField m_leaderTextField;
    private JTextField m_cellPhoneTextField;
    private JTextField m_toneIDTextField;
    private JTextField m_createdTextField;
    private JTextField m_callsignTextField;
    private JTextField m_fieldTimeTextField;
    private JTextField m_assignmentTextField;
    private JTextField m_stopTimeTextField;
    private JTable m_personnelTable;

    private IDiskoWpUnit m_wpUnit;

    private static final long UPDATE_INTERVAL = 60000;
    private long m_timeCounter;

    public UnitDetailsPanel(IDiskoWpUnit wp)
    {
        m_wpUnit = wp;
        wp.getMsoEventManager().addClientUpdateListener(this);
        initialize();
    }

    private void initialize()
    {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(4, 4, 4, 4);

        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel topButtonsPanel = new JPanel();
        topButtonsPanel.setBorder(null);
        m_topPanelLabel = new JLabel();
        topPanel.add(m_topPanelLabel, BorderLayout.CENTER);
        String text = m_resources.getString("PauseButton.text");
        String letter = m_resources.getString("PauseButton.letter");
        m_pauseToggleButton = DiskoButtonFactory.createToggleButton(letter,text,null,ButtonSize.NORMAL);
        m_pauseToggleButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                if (m_currentUnit != null)
                {
                    try
                    {
                        UnitUtilities.toggleUnitPause(m_currentUnit);

                        // Commit small changes right away if new unit has been committed
                        if (!m_wpUnit.getNewUnit())
                        {
                            m_wpUnit.getMsoModel().commit();
                        }
                    }
                    catch (IllegalOperationException e)
                    {
                    }
                }
            }
        });
        topButtonsPanel.add(m_pauseToggleButton);
        
        text = m_resources.getString("DissolveButton.text");
        letter = m_resources.getString("DissolveButton.letter");        
        m_releaseButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.NORMAL);
        m_releaseButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                // Try to release unit
                IUnitIf unit = m_wpUnit.getEditingUnit();

                try
                {
                    UnitUtilities.releaseUnit(unit);

                    // Commit
                    if (!m_wpUnit.getNewUnit())
                    {
                        m_wpUnit.getMsoModel().commit();
                    }
                }
                catch (IllegalOperationException e)
                {
                    ErrorDialog error = new ErrorDialog(null);
                    error.showError(m_resources.getString("ReleaseUnitError.header"),
                            m_resources.getString("ReleaseUnitError.text"));
                }
            }
        });
        topButtonsPanel.add(m_releaseButton);
        m_showReportButton = DiskoButtonFactory.createButton("GENERAL.PRINT",ButtonSize.NORMAL);
        m_showReportButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent arg0)
             {
             	IUnitIf unit = m_wpUnit.getEditingUnit();
             	DiskoReportManager diskoReport = m_wpUnit.getApplication().getReportManager();
             	diskoReport.printUnitLog(unit);
             }
         });
        topButtonsPanel.add(m_showReportButton);
        topPanel.add(topButtonsPanel, BorderLayout.EAST);
        gbc.gridwidth = 4;
        this.add(topPanel, gbc);
        gbc.gridy++;

        // Leader
        m_leaderTextField = new JTextField();
        m_leaderTextField.setEditable(false);
        gbc.gridwidth = 3;
        layoutComponent(0, m_resources.getString("Leader.text"), m_leaderTextField, gbc, 1);

        // Cell phone
        m_cellPhoneTextField = new JTextField();
        m_cellPhoneTextField.setEditable(false);
        gbc.gridwidth = 3;
        layoutComponent(0, m_resources.getString("CellularPhone.text"), m_cellPhoneTextField, gbc, 1);

        // 5-tone
        m_toneIDTextField = new JTextField();
        layoutComponent(0, m_resources.getString("FiveTone.text"), m_toneIDTextField, gbc, 0);

        // Created
        m_createdTextField = new JTextField();
        m_createdTextField.setEditable(false);
        layoutComponent(2, m_resources.getString("Created.text"), m_createdTextField, gbc, 1);

        // Call sign
        m_callsignTextField = new JTextField();
        layoutComponent(0, m_resources.getString("CallSign.text"), m_callsignTextField, gbc, 0);

        // Field time
        m_fieldTimeTextField = new JTextField();
        m_fieldTimeTextField.setEditable(false);
        layoutComponent(2, m_resources.getString("FieldTime.text"), m_fieldTimeTextField, gbc, 1);

        // Assignment
        m_assignmentTextField = new JTextField();
        m_assignmentTextField.setEditable(false);
        layoutComponent(0, m_resources.getString("Assignment.text"), m_assignmentTextField, gbc, 0);

        // Stop time
        m_stopTimeTextField = new JTextField();
        m_stopTimeTextField.setEditable(false);
        layoutComponent(2, m_resources.getString("StopTime.text"), m_stopTimeTextField, gbc, 1);

        // Personnel table
        m_personnelTable = new JTable();
        m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_personnelTable.addMouseListener(new UnitPersonnelMouseListener());
        m_personnelTable.setFillsViewportHeight(true);
        m_personnelTable.setModel(new UnitPersonnelTableModel());
        m_personnelTable.setDragEnabled(true);
        try
        {
            m_personnelTable.setTransferHandler(new PersonnelTransferHandler(m_wpUnit));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        UnitLeaderColumnRenderer leaderRenderer = new UnitLeaderColumnRenderer();
        leaderRenderer.setTable(m_personnelTable);

//        JTableHeader tableHeader = m_personnelTable.getTableHeader();
//        tableHeader.setResizingAllowed(false);
//        tableHeader.setReorderingAllowed(false);
        m_personnelTable.setTableHeader(null);

        JScrollPane personnelTableScrollPane = new JScrollPane(m_personnelTable);
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(personnelTableScrollPane, gbc);
    }

    private void layoutComponent(int column, String label, JComponent component, GridBagConstraints gbc, int height)
    {
        gbc.weightx = 1.0;
        gbc.gridheight = Math.max(1, height);
        gbc.gridx = column + 1;
        this.add(component, gbc);

        gbc.weightx = 0.0;
        gbc.gridx = column;
        gbc.gridwidth = 1;
        this.add(new JLabel(label), gbc);

        gbc.gridy += height;
    }

    public void setUnit(IUnitIf unit)
    {
        m_currentUnit = unit;
    }

    public void updateFieldContents()
    {
        if (m_currentUnit != null)
        {
            // Fill in fields with unit values
            String topText = m_currentUnit.getTypeText() + " " + m_currentUnit.getNumber() +
                    " (" + m_currentUnit.getStatusText() + ")";
            m_topPanelLabel.setText(topText);

            // Pause button
            m_pauseToggleButton.setSelected(m_currentUnit.getStatus() == UnitStatus.PAUSED);

            // Released button
            m_releaseButton.setSelected(m_currentUnit.getStatus() == UnitStatus.RELEASED);

            IPersonnelIf leader = m_currentUnit.getUnitLeader();
            String leaderName = leader == null ? "" : leader.getFirstname() + " " + leader.getLastname();
            m_leaderTextField.setText(leaderName);

            String cell = leader == null ? "" : leader.getTelephone1();
            m_cellPhoneTextField.setText(cell);

            String toneId = m_currentUnit.getToneID();
            m_toneIDTextField.setText(toneId);

//			String created = DTG.CalToDTG(m_currentUnit.get)
//			m_createdTextField.setText(created); TODO

            String callsign = m_currentUnit.getCallSign();
            m_callsignTextField.setText(callsign);

            IAssignmentIf assignment = m_currentUnit.getActiveAssignment();
            String assignmentString = assignment == null ? "" : assignment.getTypeAndNumber();
            m_assignmentTextField.setText(assignmentString);

            updateFieldTime();

            updateStopTime();

            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            model.setPersonnel(m_currentUnit.getUnitPersonnel());
        } else
        {
            // Unit is null, clear fields
            m_topPanelLabel.setText("");
            m_leaderTextField.setText("");
            m_cellPhoneTextField.setText("");
            m_toneIDTextField.setText("");
            m_createdTextField.setText("");
            m_callsignTextField.setText("");
            m_fieldTimeTextField.setText("");
            m_assignmentTextField.setText("");
            m_stopTimeTextField.setText("");
            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            model.setPersonnel(null);
        }
    }

    private void updateFieldTime()
    {
        if (m_currentUnit != null)
        {
            IAssignmentIf assignment = m_currentUnit.getActiveAssignment();
            if (assignment != null)
            {
                Calendar timeAssigned = assignment.getTimeAssigned();
                if (timeAssigned != null)
                {
                    long minutes = (Calendar.getInstance().getTimeInMillis() - timeAssigned.getTimeInMillis()) / 60000;
                    String fieldTime = null;
                    if (minutes > 59)
                    {
                        fieldTime = minutes / 60 + m_resources.getString("Hours.text")
                                + " " + minutes % 60 + m_resources.getString("Minutes.text");
                    } else
                    {
                        fieldTime = minutes + " " + m_resources.getString("Minutes.text");
                    }
                    m_fieldTimeTextField.setText(fieldTime);
                }
            } else
            {
                m_fieldTimeTextField.setText("");
            }
        }
    }

    private void updateStopTime()
    {
        // TODO
    }

    /**
     * @return Current unit
     */
    public IUnitIf getUnit()
    {
        return m_currentUnit;
    }

    /**
     * Display any updates made to current editing unit
     */
    public void handleMsoUpdateEvent(Update e)
    {
        IUnitIf unit = (IUnitIf) e.getSource();
        if (unit == m_currentUnit)
        {
        	if(e.isDeleteObjectEvent())
        	{
        		m_currentUnit = null;
        	}
            updateFieldContents();
        }
    }

    /**
     * Interested in unit updates
     */
    EnumSet<IMsoManagerIf.MsoClassCode> interestedIn = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);

    public boolean hasInterestIn(IMsoObjectIf msoObject)
    {
        return interestedIn.contains(msoObject.getMsoClassCode());
    }


    /**
     * Single click displays all personnel details in bottom panel.
     * Double click changes to personnel display
     *
     * @author thomasl
     */
    private class UnitPersonnelMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent me)
        {
            Point clickedPoint = new Point(me.getX(), me.getY());
            int row = m_personnelTable.rowAtPoint(clickedPoint);
            int index = m_personnelTable.convertRowIndexToModel(row);
            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            IPersonnelIf personnel = model.getPersonnel(index);

            int clickCount = me.getClickCount();

            if (clickCount == 1)
            {
                // Display personnel info in bottom panel
                m_wpUnit.setPersonnelBottom(personnel);
                m_wpUnit.setBottomView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
            } else if (clickCount == 2)
            {
                // Check if unit is new
                if (m_wpUnit.getNewUnit())
                {
                    String[] options = {m_resources.getString("Yes.text"), m_resources.getString("No.text")};
                    int n = JOptionPane.showOptionDialog(null,
                            m_resources.getString("ChangeToPersonnelView.text"),
                            m_resources.getString("ChangeToPersonnelView.header"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (n == JOptionPane.YES_OPTION)
                    {
                        // Commit unit
                        m_wpUnit.getMsoModel().commit();
                    } else
                    {
                        // Abort view change
                        return;
                    }
                }

                // Change to personnel display
                m_wpUnit.setPersonnelLeft(personnel);
                m_wpUnit.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
                m_wpUnit.setPersonnelBottom(personnel);
                m_wpUnit.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
            }
        }

        public void mouseEntered(MouseEvent arg0)
        {
        }

        public void mouseExited(MouseEvent arg0)
        {
        }

        public void mousePressed(MouseEvent arg0)
        {
        }

        public void mouseReleased(MouseEvent arg0)
        {
        }
    }

    /**
     * Data model for table containing current unit personnel
     *
     * @author thomasl
     */
    public class UnitPersonnelTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1L;

        IPersonnelListIf m_personnel;

        /**
         * Sets the current personnel and updates table
         *
         * @param personnel
         */
        public void setPersonnel(IPersonnelListIf personnel)
        {
            m_personnel = personnel;
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column)
        {
            return null;
        }

        public int getColumnCount()
        {
            return 3;
        }

        public int getRowCount()
        {
            return m_personnel == null ? 0 : m_personnel.size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == 2;
        }

        public Object getValueAt(int row, int column)
        {
            IPersonnelIf personnel = (IPersonnelIf) m_personnel.getItems().toArray()[row];
            switch (column)
            {
                case 0:
                    return personnel.getFirstname() + " " + personnel.getLastname();
                case 1:
                    return personnel.getTelephone1();
            }
            return null;
        }

        public IPersonnelIf getPersonnel(int selectedRow)
        {
            return m_personnel == null || selectedRow < 0 ? null : (IPersonnelIf) m_personnel.getItems().toArray()[selectedRow];
        }

        public IPersonnelListIf getPersonnel()
        {
            return m_personnel;
        }
    }

    /**
     * Renderer and editor for the leader selection column
     *
     * @author thomasl
     */
    private class UnitLeaderColumnRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
    {
        private static final long serialVersionUID = 1L;

        private JPanel m_panel;
        private JButton m_leaderButton;
        JTable m_table;
        private int m_editingRow;

        public UnitLeaderColumnRenderer()
        {

            m_panel = new JPanel();

            String letter = m_resources.getString("LeaderButton.letter");
            String text = m_resources.getString("LeaderButton.text");
            m_leaderButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
            m_leaderButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    // Set unit leader to selected personnel
                    IUnitIf editingUnit = m_wpUnit.getEditingUnit();

                    // has editing unit?
                    if(editingUnit!=null) {
	                    int index = m_table.convertRowIndexToModel(m_editingRow);
	                    UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	                    IPersonnelIf newLeader = model.getPersonnel(index);
	
	                    editingUnit.setUnitLeader(newLeader);
	
	                    // Commit changes¨
	                    if (!m_wpUnit.getNewUnit())
	                    {
	                        m_wpUnit.getMsoModel().commit();
	                    }
	
	                    fireEditingStopped();
                    }
                }
            });
            m_panel.add(m_leaderButton);
        }

        public void setTable(JTable table)
        {
            m_table = table;
            m_panel.setBackground(m_table.getBackground());

            TableColumn column = m_table.getColumnModel().getColumn(2);
            column.setCellEditor(this);
            column.setCellRenderer(this);
            Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
            column.setPreferredWidth(dim.width + 10);
            column.setMaxWidth(dim.width + 10);
            m_table.setRowHeight(dim.height + 10);
        }


        public Component getTableCellEditorComponent(JTable arg0, Object arg1,
                                                     boolean arg2, int row, int column)
        {
            m_editingRow = row;
            return m_panel;
        }


        public Object getCellEditorValue()
        {
            return null;
        }


        public Component getTableCellRendererComponent(JTable arg0,
                                                       Object arg1, boolean arg2, boolean arg3, int row, int column)
        {
            int index = m_table.convertRowIndexToModel(row);
            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
            IPersonnelIf personnel = model.getPersonnel(index);

            IUnitIf editingUnit = m_wpUnit.getEditingUnit();
            if (editingUnit != null)
            {
                m_leaderButton.setSelected(editingUnit.getUnitLeader() == personnel);
            }

            return m_panel;
        }
    }

    /**
     * Saves field contents in unit MSO object
     */
    public boolean saveUnit()
    {
        if (m_currentUnit != null)
        {
            String toneId = m_toneIDTextField.getText();
            String callSign = m_callsignTextField.getText();

            m_currentUnit.suspendClientUpdate();

            m_currentUnit.setToneID(toneId);
            m_currentUnit.setCallSign(callSign);

            m_currentUnit.resumeClientUpdate();
        }
        // success!
    	return true;
    }

    public long getInterval()
    {
        return UPDATE_INTERVAL;
    }

    public long getTimeCounter()
    {
        return m_timeCounter;
    }

    /**
     * Update time dependent fields
     */
    public void handleTick(TickEvent e)
    {
        ICmdPostIf cmdPost = m_wpUnit.getCmdPost();
        if (cmdPost == null)
        {
            return;
        }

        updateFieldTime();
        updateStopTime();
    }

    public void setTimeCounter(long counter)
    {
        m_timeCounter = counter;
	}
}
