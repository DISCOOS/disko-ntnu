package org.redcross.sar.wp.unit;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.FieldsPanel;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EnumSet;
import java.util.ResourceBundle;

import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.dialog.AssociationDialog;
import org.redcross.sar.gui.document.AutoCompleteDocument;
import org.redcross.sar.gui.event.IAutoCompleteListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.AssocUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.AssocUtils.Association;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

/**
 * JPanel displaying unit details
 *
 * @author thomasl, kennetgu
 */
public class UnitDetailsPanel extends JPanel implements IMsoUpdateListenerIf, ITickEventListenerIf
{
    private static final long serialVersionUID = 1L;
    private static final long UPDATE_INTERVAL = 60000;

    private static final Logger m_logger = Logger.getLogger(UnitDetailsPanel.class);
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

    private final ImageIcon m_pauseIcon = DiskoIconFactory.getIcon("GENERAL.PAUSE", "32x32");
    private final ImageIcon m_resumeIcon = DiskoIconFactory.getIcon("GENERAL.PLAY", "32x32");

    private IUnitIf m_currentUnit;

    private FieldsPanel m_infoPanel;
    private JToggleButton m_pauseToggleButton;
    private JToggleButton m_releaseToggleButton;
    private JButton m_showReportButton;
    private TextLineField m_leaderTextField;
    private TextLineField m_cellPhoneTextField;
    private TextLineField m_toneIDTextField;
    private TextLineField m_trackingIDTextField;
    private DTGField m_createdDTGField;
    private TextLineField m_callSignTextField;
    private TextLineField m_workTimeTextField;
    private TextLineField m_assignmentTextField;
    private TextLineField m_stopTimeTextField;
    private TextLineField m_associationTextField;
    
    private BasePanel m_personnelPanel;
    private DiskoTable m_personnelTable;

    private IDiskoWpUnit m_wp;

    private long m_timeCounter;

    public UnitDetailsPanel(IDiskoWpUnit wp)
    {
    	// prepare
        m_wp = wp;
        // initialize GUI
        initialize();
        // add listeners
        wp.addTickEventListener(this);
        wp.getMsoEventManager().addClientUpdateListener(this);
        wp.getMsoEventManager().addClientUpdateListener(getInfoPanel());
        getInfoPanel().addWorkFlowListener(new IWorkFlowListener() {
			public void onFlowPerformed(WorkFlowEvent e) {
				// only forward MSO changes
				if(e.isMsoData()) 
					m_wp.onFlowPerformed(e);				
			}        	
        });
    }

    private void initialize()
    {
    	// prepare
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        // add panels
        add(getInfoPanel());
        add(Box.createVerticalStrut(5));
        add(getPersonnelPanel());        
        
    }
        
    private FieldsPanel getInfoPanel() {
    	if(m_infoPanel==null) {
    		m_infoPanel = new FieldsPanel(m_resources.getString("UnitInfo.text"),"",false,false);
    		m_infoPanel.setColumns(2);
			m_infoPanel.setPreferredExpandedHeight(400);
			m_infoPanel.setMinimumSize(new Dimension(400,200));
    		m_infoPanel.addButton(getPauseButton(), "pause");
    		m_infoPanel.addButton(getShowReportButton(), "report");
    		m_infoPanel.addButton(getReleaseButton(), "release");
    		m_infoPanel.suspendLayout();
    		m_infoPanel.addField(getLeaderTextField());
    		m_infoPanel.addField(getCellPhoneTextField());
    		m_infoPanel.addField(getCallSignTextField());
    		m_infoPanel.addField(getWorkTimeTextField());
    		m_infoPanel.addField(getToneIDTextField());
    		m_infoPanel.addField(getStopTimeTextField());
    		m_infoPanel.addField(getTrackingIDTextField());
    		m_infoPanel.addField(getCreatedDTGField());
    		m_infoPanel.addField(getAssociationTextField());    		
    		m_infoPanel.setFieldSpanX("leader", 2);
    		m_infoPanel.setFieldSpanX("cellphone", 2);
    		m_infoPanel.setFieldSpanX("association", 2);
    		m_infoPanel.resumeLayout();
    		m_infoPanel.setInterests(m_wp.getMsoModel(), EnumSet.of(MsoClassCode.CLASSCODE_UNIT));
    	}
    	return m_infoPanel;
    }
    
    private JToggleButton getPauseButton() {
    	if(m_pauseToggleButton==null) {
	        m_pauseToggleButton = DiskoButtonFactory.createToggleButton("GENERAL.PAUSE",ButtonSize.SMALL);
	        m_pauseToggleButton.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent e)
	            {
	                if (m_currentUnit != null)
	                {
	                    try
	                    {
	                    	if(m_currentUnit.isPaused()) {
	                    		m_currentUnit.resume();
	                    		m_pauseToggleButton.setIcon(m_pauseIcon);
	                    	} else {
	                    		m_currentUnit.pause();
	                    		m_pauseToggleButton.setIcon(m_resumeIcon);
	                    	}
	
	                        // Commit small changes right away if new unit has been committed
	                        if (!m_wp.isNewUnit())
	                        {
	                            try {
	                				m_wp.getMsoModel().commit(m_wp.getCommitManager().getChanges(m_currentUnit));
	                			} catch (TransactionException ex) {
	                				m_logger.error("Failed to commit unit detail changes",ex);
	                			}            
	                        }
	                    }
	                    catch (IllegalOperationException ex)
	                    {
	                    	Utils.showWarning("Enhet kan ikke endre status");
	                    }
	                }
	            }
	        });	        
    	}
    	return m_pauseToggleButton;
    }

    private JToggleButton getReleaseButton() {
    	if(m_releaseToggleButton==null) {
	        // add release button
	        String text = m_resources.getString("DissolveButton.text");
	        String letter = m_resources.getString("DissolveButton.letter");
	        ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.CANCEL", "32x32");
	        m_releaseToggleButton = DiskoButtonFactory.createToggleButton(letter,text,icon,ButtonSize.SMALL);
	        m_releaseToggleButton.addActionListener(new ActionListener()
	        {
	            public void actionPerformed(ActionEvent arg0)
	            {
	                // Try to release unit
	                IUnitIf unit = m_wp.getEditingUnit();
	
	                try
	                {
	                    UnitUtilities.releaseUnit(unit);
	
	                    // Commit
	                    if (!m_wp.isNewUnit())
	                    {
                            try {
                				m_wp.getMsoModel().commit(m_wp.getCommitManager().getChanges(m_currentUnit));
                			} catch (TransactionException ex) {
                				m_logger.error("Failed to commit unit detail changes",ex);
                			}            
	                    }
	                }
	                catch (IllegalOperationException e)
	                {
	                	Utils.showError(m_resources.getString("ReleaseUnitError.header"),
	                            m_resources.getString("ReleaseUnitError.text"));
	                }
	            }
	        });
    	}
    	return m_releaseToggleButton;
    }
    
    private JButton getShowReportButton() {
    	if(m_showReportButton==null) {
	        m_showReportButton = DiskoButtonFactory.createButton("GENERAL.PRINT",ButtonSize.SMALL);
	        m_showReportButton.addActionListener(new ActionListener()
	         {
	             public void actionPerformed(ActionEvent arg0)
	             {
	             	IUnitIf unit = m_wp.getEditingUnit();
	             	DiskoReportManager diskoReport = m_wp.getApplication().getReportManager();
	             	diskoReport.printUnitLog(unit);
	             }
	         });
    	}
    	return m_showReportButton;
    }
        
    private TextLineField getLeaderTextField() {
    	if(m_leaderTextField==null) {
            m_leaderTextField = new TextLineField("leader",m_resources.getString("Leader.text"),false);    		
    	}
    	return m_leaderTextField;
    }

    private TextLineField getCellPhoneTextField() {
    	if(m_cellPhoneTextField==null) {
    		m_cellPhoneTextField = new TextLineField("cellphone",m_resources.getString("CellularPhone.text"),true);    		
    	}
    	return m_cellPhoneTextField;
    }

    private TextLineField getToneIDTextField() {
    	if(m_toneIDTextField==null) {
    		m_toneIDTextField = new TextLineField("toneid",m_resources.getString("FiveTone.text"),true);    		
    	}
    	return m_toneIDTextField;
    }

    private TextLineField getTrackingIDTextField() {
    	if(m_trackingIDTextField==null) {
    		m_trackingIDTextField = new TextLineField("trackingid",m_resources.getString("TrackingID.text"),true);    		
    	}
    	return m_trackingIDTextField;
    }
    
    private DTGField getCreatedDTGField() {
    	if(m_createdDTGField==null) {
    		m_createdDTGField = new DTGField("created",m_resources.getString("Created.text"),false);    		
    	}
    	return m_createdDTGField;
    }
    
    private TextLineField getCallSignTextField() {
    	if(m_callSignTextField==null) {
    		m_callSignTextField = new TextLineField("callsign",m_resources.getString("CallSign.text"),true);    		
    	}
    	return m_callSignTextField;
    }

    private TextLineField getWorkTimeTextField() {
    	if(m_workTimeTextField==null) {
    		m_workTimeTextField = new TextLineField("worktime",m_resources.getString("WorkTime.text"),false);    		
    	}
    	return m_workTimeTextField;
    }
    
    private TextLineField getAssignmentTextField() {
    	if(m_assignmentTextField==null) {
    		m_assignmentTextField = new TextLineField("Assignment",m_resources.getString("Assignment.text"),false);    		
    	}
    	return m_assignmentTextField;
    }
    
    private TextLineField getStopTimeTextField() {
    	if(m_stopTimeTextField==null) {
    		m_stopTimeTextField = new TextLineField("stoptime",m_resources.getString("StopTime.text"),false);    		
    	}
    	return m_stopTimeTextField;
    }

    private TextLineField getAssociationTextField() {
		if(m_associationTextField==null) {
		    m_associationTextField = new TextLineField("association","Tilhørighet",true);
			JTextField inputField = m_associationTextField.getTextField();
			AutoCompleteDocument doc = new AutoCompleteDocument(AssocUtils.getAssociations(-1,"{l:n} {l:s}"),inputField);
			inputField.setDocument(doc);
			doc.addAutoCompleteListener(new IAutoCompleteListener() {

				public void onSuggestionFound(AutoCompleteDocument document, String suggestion) {
					if(!m_associationTextField.isChangeable()) return;
					Association[] items = null;
					if(suggestion!=null) {
						items = AssocUtils.parse(suggestion,false,false);
					}
					if(m_currentUnit!=null) {
						m_associationTextField.setChangeable(false);
						m_currentUnit.suspendClientUpdate();
						if(items!=null) {
							m_currentUnit.setOrganization(items[0].getName());
							m_currentUnit.setDivision(items[0].getName());
							m_currentUnit.setDepartment(items[0].getName());
						} else {
							m_currentUnit.setOrganization(null);
							m_currentUnit.setDivision(null);
							m_currentUnit.setDepartment(null);
						}
						m_currentUnit.resumeClientUpdate(false);
						m_associationTextField.setChangeable(true);
						m_wp.onFlowPerformed(new WorkFlowEvent(this,m_currentUnit,WorkFlowEvent.EVENT_CHANGE));
					}
				}
				
			});
			m_associationTextField.setButtonVisible(true);
			m_associationTextField.addButtonActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if(m_currentUnit!=null) {
						AssociationDialog dlg = new AssociationDialog(Application.getInstance());
						dlg.setLocationRelativeTo(Application.getInstance());
						if(dlg.associate(getAssociationTextField().getValue(),m_currentUnit)) {
							updateFieldContents();
						}
					}
				}
				
			});
		}
		return m_associationTextField;
	}	
    
    private BasePanel getPersonnelPanel() {
    	if(m_personnelPanel==null) {
    		m_personnelPanel = new BasePanel(m_resources.getString("Personnel.text"),ButtonSize.SMALL);
    		m_personnelPanel.setHeaderVisible(false);
    		m_personnelPanel.setContainer(getPersonnelTable());
    	}
    	return m_personnelPanel;
    }
    
    private DiskoTable getPersonnelTable() {
    	if(m_personnelTable==null) {
            m_personnelTable = new DiskoTable();
            m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            m_personnelTable.addMouseListener(new UnitPersonnelMouseListener());
            m_personnelTable.setFillsViewportHeight(true);
            m_personnelTable.setModel(new UnitPersonnelTableModel());
            m_personnelTable.setDragEnabled(true);
            try
            {
                m_personnelTable.setTransferHandler(new PersonnelTransferHandler(m_wp));
            }
            catch (ClassNotFoundException e)
            {
                m_logger.error("Failed to set transfer handler",e);
            }
            UnitLeaderColumnRenderer leaderRenderer = new UnitLeaderColumnRenderer();
            leaderRenderer.setTable(m_personnelTable);

            JTableHeader tableHeader = m_personnelTable.getTableHeader();
            tableHeader.setResizingAllowed(false);
            tableHeader.setReorderingAllowed(false);
    	}
    	return m_personnelTable;
    }
    
    public boolean isChanged() {
    	return m_currentUnit!=null?m_currentUnit.isChanged():false;
    }
    
    public boolean isNew() {
    	return m_currentUnit!=null?!m_currentUnit.isCreated():false;
    }
    
    public boolean isSet() {
    	return m_currentUnit!=null;
    }
    
    
    /**
     * validate input data
     */
    public boolean isInputValid()
    {
        if (m_currentUnit != null)
        {            
            // success
        	return getIDs(true)!=null;
        }
        // failure
    	return false;
    }    

	private String[] getIDs(boolean validate) {
		
        String callSign = getCallSignTextField().getValue();
        String toneId = getToneIDTextField().getValue();
        String trackingId = getTrackingIDTextField().getValue();

		// validate
		if (!validate || (callSign!=null && !callSign.isEmpty()))
		{
			// finished!
			return new String[]{callSign,toneId,trackingId};
		}
		return null;
	}

    
    /**
     * @return Current unit
     */
    public IUnitIf getUnit()
    {
        return m_currentUnit;
    }

    public void setUnit(IUnitIf unit)
    {
        m_currentUnit = unit;
        if(unit!=null) {
        	getCallSignTextField().setMsoAttribute(unit.getCallSignAttribute());
        	getToneIDTextField().setMsoAttribute(unit.getToneIDAttribute());
        	getTrackingIDTextField().setMsoAttribute(unit.getTrackingIDAttribute());
        	getCallSignTextField().reset();
        	getToneIDTextField().reset();
        	getTrackingIDTextField().reset();
        }  else {
        	getCallSignTextField().clearMsoAttribute("");
        	getToneIDTextField().clearMsoAttribute("");
        	getTrackingIDTextField().clearMsoAttribute("");
        }
    }

    public void updateContents()
    {
        updateFieldContents();
        updateUnitPersonnel();
    }

    private void updateFieldContents()
    {
        if (m_currentUnit != null)
        {
            // update caption
            getInfoPanel().setCaptionText(MsoUtils.getUnitName(m_currentUnit,true));

            // update pause button
            m_pauseToggleButton.setSelected(m_currentUnit.getStatus() == UnitStatus.PAUSED);
            m_pauseToggleButton.setIcon(m_pauseToggleButton.isSelected()?m_resumeIcon:m_pauseIcon);

            // update released button
            m_releaseToggleButton.setSelected(m_currentUnit.getStatus() == UnitStatus.RELEASED);
            m_pauseToggleButton.setEnabled(!m_releaseToggleButton.isSelected());

            // update info panel
            IPersonnelIf leader = m_currentUnit.getUnitLeader();
            String leaderName = leader == null ? "" : leader.getFirstName() + " " + leader.getLastName();
            getLeaderTextField().setValue(leaderName);

            String cell = leader == null ? "" : leader.getTelephone1();
            getCellPhoneTextField().setValue(cell);
            
            if(getAssociationTextField().isChangeable()) {
	            if(m_currentUnit.getOrganization()!=null) {
		            Association assoc = AssocUtils.getOrganization(m_currentUnit.getOrganization());
		            assoc.setPattern("{1:n}");
		            if(m_currentUnit.getDivision()!=null) {
		            	assoc = AssocUtils.getDivision(assoc,m_currentUnit.getDivision());
			            assoc.setPattern("{2:n} {2:s}");
		                if(m_currentUnit.getDepartment()!=null) { 
		                	assoc = AssocUtils.getDepartment(assoc,m_currentUnit.getDepartment());
				            assoc.setPattern("{3:n} {3:s}");
		                }
		            }
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue(assoc.getText());
		            getAssociationTextField().setChangeable(true);
	        	} else {
		            getAssociationTextField().setChangeable(false);
		            getAssociationTextField().setValue("");
		            getAssociationTextField().setChangeable(true);        		
	        	}
            }
            
            String created = DTG.CalToDTG(m_currentUnit.getCreatedTime());
			getCreatedDTGField().setValue(created);

            IAssignmentIf assignment = m_currentUnit.getActiveAssignment();
            String assignmentString = assignment == null ? "" : assignment.getDefaultName();
            getAssignmentTextField().setValue(assignmentString);                        
            
            updateWorkTime();
            updateStopTime();

        } else {
            // No unit selected, clear fields
            getInfoPanel().setCaptionText(m_resources.getString("NoUnitSelected.text"));
            getLeaderTextField().setValue("");
            getCellPhoneTextField().setValue("");
            getToneIDTextField().setValue("");
            getTrackingIDTextField().setValue("");
            getCreatedDTGField().setValue("");
            getCallSignTextField().setValue("");
            getWorkTimeTextField().setValue("");
            getAssociationTextField().setValue("");
            getAssignmentTextField().setValue("");
            getStopTimeTextField().setValue("");
        }
    }

    private void updateUnitPersonnel() {
        if (m_currentUnit != null)
        {

            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            int row = m_personnelTable.getSelectedRow();
            model.setPersonnelList(m_wp.getMsoModel(),m_currentUnit.getUnitPersonnel());
            if(row!=-1) m_personnelTable.getSelectionModel().setSelectionInterval(row,row);

        } else
        {
            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            model.setPersonnelList(null,null);
        }
    }

    private void updateWorkTime()
    {
        if (m_currentUnit != null)
        {
        	double t = m_currentUnit.getDuration(IUnitIf.OCCUPIED_RANGE,true);
        	m_workTimeTextField.setValue(Utils.getTime((int)t));
        }
        else
        {
        	m_workTimeTextField.setValue(Utils.getTime(0));
        }
    }

    private void updateStopTime()
    {
        if (m_currentUnit != null)
        {
        	double t = m_currentUnit.getDuration(IUnitIf.IDLE_RANGE,true);
        	m_stopTimeTextField.setValue(Utils.getTime((int)t));
        }
        else
        {
        	m_stopTimeTextField.setValue(Utils.getTime(0));
        }
    }

	public EnumSet<MsoClassCode> getInterests() {
		return EnumSet.of(MsoClassCode.CLASSCODE_UNIT);
	}
    
    /**
     * Update field contents if MSO object changes
     */
	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		if(events.isClearAllEvent()) {
    		setUnit(null);
            updateContents();
		}
		else
		{
			// loop over all events
			for(MsoEvent.Update e : events.getEvents(MsoClassCode.CLASSCODE_UNIT))
			{
				// consume loopback updates
				if(!e.isLoopback())
				{

			        // get unit
			        IUnitIf unit = (IUnitIf)e.getSource();

					// is object modified?
					if (e.isChangeReferenceEvent()) {
						updateFieldContents();
						updateUnitPersonnel();
					}
					else if (e.isModifyObjectEvent()) {
						updateFieldContents();
					}

					// delete object?
					if (e.isDeleteObjectEvent() && unit == m_currentUnit) {
			    		setUnit(null);
			            updateContents();
					}
				}
			}
		}
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
    	if(isShowing() && m_wp.getMsoManager().operationExists()) {
            ICmdPostIf cmdPost = m_wp.getMsoManager().getCmdPost();
            if (cmdPost == null)
            {
                return;
            }
	        updateWorkTime();
	        updateStopTime();
    	}
    }

    public void setTimeCounter(long counter)
    {
        m_timeCounter = counter;
	}
    
    /**
     * Single click displays all personnel details in bottom panel.
     * Double click changes to personnel display
     *
     * @author thomasl
     */
    private class UnitPersonnelMouseListener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent me)
        {
            Point clickedPoint = new Point(me.getX(), me.getY());
            int row = m_personnelTable.rowAtPoint(clickedPoint);
            int index = m_personnelTable.convertRowIndexToModel(row);
            if(index==-1) return;
            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_personnelTable.getModel();
            IPersonnelIf personnel = model.getPersonnel(index);

            int clickCount = me.getClickCount();

            if (clickCount == 1)
            {
                // Display personnel info in bottom panel
                m_wp.setPersonnelBottom(personnel);
                m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
            } else if (clickCount == 2)
            {
                // Check if unit is new
                if (m_wp.isNewUnit())
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
                        try {
            				m_wp.getMsoModel().commit(m_wp.getCommitManager().getChanges(m_currentUnit));
            			} catch (TransactionException ex) {
            				m_logger.error("Failed to commit unit detail changes",ex);
            			}            
                    } else
                    {
                        // Abort view change
                        return;
                    }
                }

                // Change to personnel display
                m_wp.setPersonnelLeft(personnel);
                m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
                m_wp.setPersonnelBottom(personnel);
                m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
            }
        }
    }

    /**
     * Data model for table containing current unit personnel
     *
     * @author thomasl
     */
    public class UnitPersonnelTableModel extends AbstractMsoTableModel<IPersonnelIf>
    {
        private static final long serialVersionUID = 1L;

		private static final String NAME = "name";
		private static final String TELEPHONE = "telephone";
		private static final String EDIT = "edit";

		private IPersonnelListIf m_list;

		/* ===============================================================
		 * Constructors
		 * =============================================================== */

		public UnitPersonnelTableModel()
		{
			// forward
			super(IPersonnelIf.class,false);
			// create table
			create(getNames(),getCaptions());
		}

		/* ===============================================================
		 * MsoTableModel implementation
		 * =============================================================== */

		protected Object getCellValue(int row, String column) {
			// get personnel
			IPersonnelIf personnel = getId(row);
			// translate
			if(NAME.equals(column))
				return MsoUtils.getPersonnelName(personnel, false);
			else if(TELEPHONE.equals(column))
                return personnel.getTelephone1();
			else if(EDIT.equals(column))
				return personnel;
			// not found
			return null;
		}

		protected void cleanup(IUnitIf id, boolean finalize) {
			if(finalize) m_list = null;
		}

		/* ===============================================================
		 * AbstractTableModel implementation
		 * =============================================================== */

		@Override
		public boolean isCellEditable(int row, int column)
		{
			return column == 2;
		}

		/* ===============================================================
		 * Public methods
		 * =============================================================== */

		public IPersonnelListIf getPersonnelList() {
			return m_list;
		}

		public void setPersonnelList(IMsoModelIf model, IPersonnelListIf list)
		{

			// set list
			m_list = list;

			// uninstall?
			if(list == null) {
				disconnectAll();
				clear();
			}
			else {
				connect(model,list,IPersonnelIf.PERSONNEL_NAME_COMPARATOR);
				load(list);
			}
		}

		public IPersonnelIf getPersonnel(int row)
		{
			return getId(row);
		}

		/* ===============================================================
		 * Helper methods
		 * =============================================================== */

		public String[] getNames() {
			return new String[] {NAME, TELEPHONE, EDIT};
		}

		public String[] getCaptions() {
			return new String[] {"Navn", "Telefon", "Leder"};
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
            ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.EDIT", "32x32");
            m_leaderButton = DiskoButtonFactory.createButton(letter,text,icon,ButtonSize.SMALL);
            m_leaderButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent arg0)
                {
                    // Set unit leader to selected personnel
                    IUnitIf editingUnit = m_wp.getEditingUnit();

                    // has editing unit?
                    if(editingUnit!=null) {
	                    int index = m_table.convertRowIndexToModel(m_editingRow);
	                    if(index==-1) return;
	                    UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	                    IPersonnelIf newLeader = model.getPersonnel(index);

	                    // remove?
	                    if(editingUnit.getUnitLeader()==newLeader)
	                    	editingUnit.setUnitLeader(null);
	                    else
	                    	editingUnit.setUnitLeader(newLeader);

	                    // Commit changes¨
	                    if (!m_wp.isNewUnit())
	                    {
                            try {
                				m_wp.getMsoModel().commit(m_wp.getCommitManager().getChanges(m_currentUnit));
                			} catch (TransactionException ex) {
                				m_logger.error("Failed to commit unit detail changes",ex);
                			}            
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


        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column)
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
            if(index!=-1)
            {
	            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	            IPersonnelIf personnel = model.getPersonnel(index);

	            IUnitIf editingUnit = m_wp.getEditingUnit();
	            if (editingUnit != null)
	            {
	                m_leaderButton.setSelected(editingUnit.getUnitLeader() == personnel);
	            }
            }

            return m_panel;
        }
    }
    
}
