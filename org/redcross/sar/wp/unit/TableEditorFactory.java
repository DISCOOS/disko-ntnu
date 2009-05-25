package org.redcross.sar.wp.unit;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.table.TableCellContainer;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.unit.UnitDetailsPanel.UnitPersonnelTableModel;

/**
 * 
 * @author kenneth
 *
 */
public class TableEditorFactory {

	private static final ImageIcon m_editIcon = DiskoIconFactory.getIcon("GENERAL.EDIT","32x32");
	private static final ImageIcon m_alertedIcon = DiskoIconFactory.getIcon("STATUS.ALERTED","32x32");
	private static final ImageIcon m_arrivedIcon = DiskoIconFactory.getIcon("STATUS.ARRIVED","32x32");
	private static final ImageIcon m_releasedIcon = DiskoIconFactory.getIcon("STATUS.RELEASED","32x32");
	private static final ImageIcon m_finishIcon = DiskoIconFactory.getIcon("GENERAL.FINISH","32x32");
	private static final ImageIcon m_playIcon = DiskoIconFactory.getIcon("GENERAL.PLAY","32x32");
	private static final ImageIcon m_pauseIcon = DiskoIconFactory.getIcon("GENERAL.PAUSE","32x32");
	
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);
	private static final Dimension m_buttonSize = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
	
	/* =============================================================
	 * Unit Leader editors
	 * ============================================================= */
	
	/**
	 * Install Unit Leader Editor
	 *
	 * @param table
	 * @param wp
	 */	
    public static void installUnitLeaderEditor(JTable table, IDiskoWpUnit wp, int toogleColumn)
    {
		// Set editor and renderer for column 1
    	UnitLeaderEditor editUnit = new UnitLeaderEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(toogleColumn);
		column.setCellEditor(editUnit);
		column.setCellRenderer(editUnit);
    }
    
    /**
     * Renderer and editor for the leader selection column
     *
     * @author thomasl, kenneth
     */
    private static class UnitLeaderEditor extends TableCellContainer
    {
        private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_leaderButton = new AbstractButton[2];

        public UnitLeaderEditor(JTable table, IDiskoWpUnit wp)
        {
        	
        	// prepare
        	m_wp = wp;
            m_table = table;
            
            // initialize
            String text = m_resources.getString("LeaderButton.text");
            
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_leaderButton[0] = UIFactory.createButtonRenderer(null,text,m_editIcon,ButtonSize.SMALL);
			container.add(m_leaderButton[0]);
            
			// create editor panel
			container = getEditorComponent(); 
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
            m_leaderButton[1] = DiskoButtonFactory.createToggleButton(null,text,m_editIcon,ButtonSize.SMALL);
            m_leaderButton[1].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Set unit leader to selected personnel
                    IUnitIf editingUnit = m_wp.getEditingUnit();

                    // has editing unit?
                    if(editingUnit!=null) {
	                    int index = m_table.convertRowIndexToModel(getEditCellRow());
	                    if(index==-1) return;
	                    UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	                    IPersonnelIf newLeader = model.getPersonnel(index);

	                    // toggle
	                    if(editingUnit.getUnitLeader()==newLeader)
	                    	editingUnit.setUnitLeader(null);
	                    else
	                    	editingUnit.setUnitLeader(newLeader);
	                    updateCell(m_editCellRow);
						m_wp.onFlowPerformed(new FlowEvent(this,editingUnit,FlowEvent.EVENT_CHANGE));
                    }
					fireEditingStopped();
                }
            });
            container.add(m_leaderButton[1]);            
            
        }

		@Override
		protected void updateCell(int row)
		{
			// Get unit at row
			int index = m_table.convertRowIndexToModel(row);
			if(index!=-1)
			{
	            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	            IPersonnelIf rowPersonnel = model.getPersonnel(index);

				// Get editing unit
				IUnitIf editingUnit = m_wp.getEditingUnit();

	            if (editingUnit != null)
	            {
					if(isEditing()) {
						m_leaderButton[1].setSelected(editingUnit.getUnitLeader() == rowPersonnel);
					} else {
						m_leaderButton[0].setSelected(editingUnit.getUnitLeader() == rowPersonnel);
					}
	            }
            }
		}                	
    }
	
	/* =============================================================
	 * Unit editors
	 * ============================================================= */
    
    /**
	 * Install unit editors
	 *
	 * @param table
	 * @param wp
	 */
	public static void installUnitEditor(JTable table, IDiskoWpUnit wp, int editColumn, int statusColumn)
	{
		// Set editor and renderer for edit column 
		UnitEditor editUnit = new UnitEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(editColumn);
		column.setCellEditor(editUnit);
		column.setCellRenderer(editUnit);

		// Set editor and renderer for status column
		UnitStatusEditor editStatus = new UnitStatusEditor(table,wp);
		column = table.getColumnModel().getColumn(statusColumn);
		column.setCellEditor(editStatus);
		column.setCellRenderer(editStatus);
	}

	/**
	 * Cell editor and renderer for the change unit cell in table
	 *
	 * @author thomasl, kenneth
	 */
	public static class UnitEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_editButton = new AbstractButton[2];

		public UnitEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
			
			// prepare
			String text = m_resources.getString("EditButton.text");
			
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[0] = UIFactory.createButtonRenderer(null,text,m_editIcon,ButtonSize.SMALL);
			container.add(m_editButton[0]);

			// create edit panel
			container = getEditorComponent(); 
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[1] = DiskoButtonFactory.createButton(null,text,m_editIcon,ButtonSize.SMALL);
			m_editButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set unit i unit details view
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					if(index==-1) return;
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
					if(m_wp.setUnit(unit)) {
						m_wp.setLeftView(IDiskoWpUnit.UNIT_DETAILS_VIEW_ID);
					}
						fireEditingStopped();
				}
			});
			container.add(m_editButton[1]);
		}

		@Override
		protected void updateCell(int row)
		{
			// Get unit at row
			int index = m_table.convertRowIndexToModel(row);
			if(index!=-1)
			{
				UnitTableModel model = (UnitTableModel)m_table.getModel();
				IUnitIf rowUnit = model.getUnit(index);

				// Get editing unit
				IUnitIf editingUnit = m_wp.getEditingUnit();

				if(isEditing()) {
					m_editButton[1].setSelected(editingUnit == rowUnit);
				} else {
					m_editButton[0].setSelected(editingUnit == rowUnit);
				}
				
			}
		}		
	}

	/**
	 * Cell editor and renderer for unit status in the unit table
	 *
	 * @author thomasl, kenneth
	 */
	public static class UnitStatusEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;
	
		private JButton[] m_pauseButton = new JButton[2];
		private JButton[] m_releaseButton = new JButton[2];

		public UnitStatusEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
			
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			String text = m_resources.getString("PauseButton.text");
			m_pauseButton[0] = UIFactory.createButtonRenderer(null,text,m_pauseIcon,ButtonSize.SMALL);
			container.add(m_pauseButton[0]);

	        text = m_resources.getString("DissolveButton.text");
	        m_releaseButton[0] = UIFactory.createButtonRenderer(null,text,m_finishIcon,ButtonSize.SMALL);
			container.add(m_releaseButton[0]);
			
			// create editor panel
			container = getEditorComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			text = m_resources.getString("PauseButton.text");
			m_pauseButton[1] = DiskoButtonFactory.createButton(null,text,m_pauseIcon,ButtonSize.SMALL);
			m_pauseButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
	                if (unit != null)
	                {
	                    try
	                    {
	                    	if(unit.isPaused()) unit.resume(); else unit.pause();
							m_wp.onFlowPerformed(new FlowEvent(this,unit,FlowEvent.EVENT_CHANGE));
						}
	                    catch (IllegalOperationException ex)
	                    {
	                    	Utils.showWarning("Enhet kan ikke endre status");
	                    }
	                }
					fireEditingStopped();
				}
			});
			container.add(m_pauseButton[1]);

	        text = m_resources.getString("DissolveButton.text");
	        m_releaseButton[1] = DiskoButtonFactory.createButton(null,text,m_finishIcon,ButtonSize.SMALL);
	        m_releaseButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// release unit
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
					try
					{
						// commit?
						if(ResourceUtils.releaseUnit(unit)) {
							m_wp.onFlowPerformed(new FlowEvent(this,unit,FlowEvent.EVENT_CHANGE));
						}										
					}            
					catch (IllegalOperationException e1)
					{
						Utils.showError(m_resources.getString("ReleaseUnitError.header"),
								m_resources.getString("ReleaseUnitError.text"));
					}
					fireEditingStopped();
				}
			});
			container.add(m_releaseButton[1]);
		}

		@Override
		protected void updateCell(int row)
		{
			int index = m_table.convertRowIndexToModel(row);
			UnitTableModel model = (UnitTableModel)m_table.getModel();
			IUnitIf unit = model.getUnit(index);

			// Update buttons
			if(isEditing()) {
				m_pauseButton[1].setIcon(unit.getStatus() == UnitStatus.PAUSED?m_playIcon:m_pauseIcon);
				m_releaseButton[1].setSelected(unit.getStatus() == UnitStatus.RELEASED);
			} else {
				m_pauseButton[0].setIcon(unit.getStatus() == UnitStatus.PAUSED?m_playIcon:m_pauseIcon);
				m_releaseButton[0].setSelected(unit.getStatus() == UnitStatus.RELEASED);
			}			

		}		
	}
	
	/* =============================================================
	 * Personnel editors
	 * ============================================================= */
	
    /**
	 * Install personnel editors
	 *
	 * @param table
	 * @param wp
	 */
	public static void installPersonnelEditor(JTable table, IDiskoWpUnit wp, int editColumn, int statusColumn)
	{
		PersonnelEditor editPersonnel = new PersonnelEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(editColumn);
		column.setCellEditor(editPersonnel);
		column.setCellRenderer(editPersonnel);

		PersonnelStatusCellEditor personnelStatusEditor = new PersonnelStatusCellEditor(table,wp);
		column = table.getColumnModel().getColumn(statusColumn);
		column.setCellEditor(personnelStatusEditor);
		column.setCellRenderer(personnelStatusEditor);
		
	}

	/**
	 * Cell editor and renderer for changing personnel details
	 *
	 * @author thomasl, kenneth
	 */
	public static class PersonnelEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_editButton = new AbstractButton[2];

		public PersonnelEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
			
			// prepare
			String text = m_resources.getString("EditButton.text");
			
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[0] = UIFactory.createButtonRenderer(null,text,m_editIcon,ButtonSize.SMALL);
			container.add(m_editButton[0]);

			// create edit panel
			container = getEditorComponent(); 
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[1] = DiskoButtonFactory.createButton(null,text,m_editIcon,ButtonSize.SMALL);
			m_editButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Display selected personnel in details panel
					int modelIndex = m_table.convertRowIndexToModel(getEditCellRow());
					if(modelIndex==-1) return;
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);
					// Show personnel details
					if(m_wp.setPersonnelLeft(selectedPersonnel)) {
						m_wp.setPersonnelBottom(selectedPersonnel);
						m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
						m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
					}
					fireEditingStopped();
				}
			});
			container.add(m_editButton[1]);
		}

		@Override
		protected void updateCell(int row)
		{
			// Get personnel at row
			PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
			row = m_table.convertRowIndexToModel(row);
			if(row!=-1)
			{
				IPersonnelIf rowPersonnel = model.getPersonnel(row);

				// Get personnel in personnel details panel
				IPersonnelIf editingPersonnel = m_wp.getEditingPersonnel();

				if(isEditing()) {
					m_editButton[1].setSelected(editingPersonnel == rowPersonnel);
				} else {
					m_editButton[0].setSelected(editingPersonnel == rowPersonnel);
				}
			}	
		}
	}

	/**
	 * Cell renderer and editor for changing personnel status
	 *
	 * @author thomasl, kenneth
	 */
	public static class PersonnelStatusCellEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;

		private JButton[] m_calloutButton = new JButton[2];
		private JButton[] m_arrivedButton = new JButton[2];
		private JButton[] m_releasedButton = new JButton[2];
		
		public PersonnelStatusCellEditor(JTable table, IDiskoWpUnit wp)
		{
			// prepare
			m_wp = wp;
			m_table = table;
			
			// get preferred cell size
			Dimension d = new Dimension(m_buttonSize.width*3+20,m_buttonSize.height);
						
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(d);

			String text = m_resources.getString("CalloutButton.text");
			m_calloutButton[0] = DiskoButtonFactory.createButton(null,text,m_alertedIcon,ButtonSize.SMALL);
			container.add(m_calloutButton[0]);

			text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[0] = UIFactory.createButtonRenderer(null,text,m_arrivedIcon,ButtonSize.SMALL);
			container.add(m_arrivedButton[0]);
			
			text = m_resources.getString("ReleasedButton.text");
	        m_releasedButton[0] = UIFactory.createButtonRenderer(null,text,m_releasedIcon,ButtonSize.SMALL);
			container.add(m_releasedButton[0]);
			
			// create editor panel
			container = getEditorComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(d);
			
			text = m_resources.getString("CalloutButton.text");
			m_calloutButton[1] = DiskoButtonFactory.createButton(null,text,m_alertedIcon,ButtonSize.SMALL);
			m_calloutButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					PersonnelStatus oldStatus = personnel.getStatus();
					// any change?
					if(!PersonnelStatus.ON_ROUTE.equals(oldStatus)) {
						IPersonnelIf nextPersonnel = ResourceUtils.callOutPersonnel(personnel);
						updateCell(m_editCellRow);
						// update view?
						if(m_wp.getEditingPersonnel()==personnel &&
							nextPersonnel!=personnel) 
						{
							m_wp.setPersonnelLeft(nextPersonnel);
						}
						m_wp.onFlowPerformed(new FlowEvent(this,personnel,FlowEvent.EVENT_CHANGE));
					}
					fireEditingStopped();
				}
			});
			container.add(m_calloutButton[1]);
			
			text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[1] = DiskoButtonFactory.createButton(null,text,m_arrivedIcon,ButtonSize.SMALL);
			m_arrivedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					PersonnelStatus oldStatus = personnel.getStatus();
					if(!PersonnelStatus.ARRIVED.equals(oldStatus)) {
						IPersonnelIf nextPersonnel = ResourceUtils.arrivedPersonnel(personnel);
						updateCell(m_editCellRow);
						if(nextPersonnel!=personnel) m_wp.setPersonnelLeft(nextPersonnel);
						m_wp.onFlowPerformed(new FlowEvent(this,personnel,FlowEvent.EVENT_CHANGE));
					}
					fireEditingStopped();
				}
			});
			container.add(m_arrivedButton[1]);

			text = m_resources.getString("ReleasedButton.text");
			m_releasedButton[1] = DiskoButtonFactory.createButton(null,text,m_releasedIcon,ButtonSize.SMALL);
			m_releasedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					PersonnelStatus oldStatus = personnel.getStatus();
					if(!PersonnelStatus.RELEASED.equals(oldStatus)) {
						ResourceUtils.releasePersonnel(personnel);
						updateCell(m_editCellRow);
						m_wp.onFlowPerformed(new FlowEvent(m_releasedButton[1],personnel,FlowEvent.EVENT_CHANGE));
					}
					fireEditingStopped();
				}
			});
			container.add(m_releasedButton[1]);
			
		}

		@Override
		protected void updateCell(int row)
		{
			// Get current personnel
			int modelIndex = m_table.convertRowIndexToModel(row);
			PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
			IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);

			if(selectedPersonnel!=null) {
				
				// get status
				PersonnelStatus status = selectedPersonnel.getStatus();
				
				// Set button selection
				if(isEditing()) {
					m_calloutButton[1].setSelected(status == PersonnelStatus.ON_ROUTE || status == PersonnelStatus.ARRIVED);
					m_arrivedButton[1].setSelected(status == PersonnelStatus.ARRIVED);
					m_releasedButton[1].setSelected(status == PersonnelStatus.RELEASED);
				} else {
					m_calloutButton[0].setSelected(status == PersonnelStatus.ON_ROUTE || status == PersonnelStatus.ARRIVED);
					m_arrivedButton[0].setSelected(status == PersonnelStatus.ARRIVED);
					m_releasedButton[0].setSelected(status == PersonnelStatus.RELEASED);					
				}
			}
		}		
	}	
	
	/* =============================================================
	 * Call Out editors
	 * ============================================================= */
	
	/**
	 * Install Call Out Personnel Editor
	 *
	 * @param table
	 * @param wp
	 */	
    public static void installCalloutEditor(JTable table, IDiskoWpUnit wp, int editColumn)
    {
		// Set editor and renderer for column 1
    	CallOutEditor editCallout = new CallOutEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(editColumn);
		column.setCellEditor(editCallout);
		column.setCellRenderer(editCallout);
    }
    
    /**
	 * Column editor for call-out personnel status changes
	 *
	 * @author thomasl, kenneth
	 */
    public static class CallOutEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;
		
		private JButton[] m_editButton = new JButton[2];

		public CallOutEditor(JTable table, IDiskoWpUnit wp)
		{
			// prepare
			m_wp = wp;
			m_table = table;
			
			// prepare
			String text = m_resources.getString("EditButton.text");
			ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.EDIT","32x32");
			
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[0] = UIFactory.createButtonRenderer(null,text,icon,ButtonSize.SMALL);
			container.add(m_editButton[0]);

			// create edit panel
			container = getEditorComponent(); 
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(m_buttonSize);
			m_editButton[1] = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.SMALL);
			m_editButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set unit i unit details view
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					if(index==-1) return;
					CalloutTableModel model = (CalloutTableModel)m_table.getModel();
					ICalloutIf callout = model.getCallout(index);
					m_wp.setCallout(callout);
					m_wp.setLeftView(IDiskoWpUnit.CALLOUT_DETAILS_VIEW_ID);
					fireEditingStopped();
				}
			});
			container.add(m_editButton[1]);
		}
		
	}	
	
	/* =============================================================
	 * Call Out personnel editors
	 * ============================================================= */
	
	/**
	 * Install Call Out Personnel Editor
	 *
	 * @param table
	 * @param wp
	 */	
    public static void installCalloutPersonnelEditor(JTable table, IDiskoWpUnit wp, int statusColumn)
    {
		// Set editor and renderer for column 1
    	CallOutPersonnelStatusEditor editCallout = new CallOutPersonnelStatusEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(statusColumn);
		column.setCellEditor(editCallout);
		column.setCellRenderer(editCallout);
		
    }
    
    /**
	 * Column editor for call-out personnel status changes
	 *
	 * @author thomasl, kenneth
	 */
    public static class CallOutPersonnelStatusEditor extends TableCellContainer
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;
		
		private JButton[] m_arrivedButton = new JButton[2];
		private JButton[] m_releasedButton = new JButton[2];

		public CallOutPersonnelStatusEditor(JTable table, IDiskoWpUnit wp)
		{
			// prepare
			m_wp = wp;
			m_table = table;
			
			// get preferred cell size
			Dimension d = new Dimension(m_buttonSize.width*2+15,m_buttonSize.height);
						
			// create renderer panel
			JPanel container = getComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(d);

			String text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[0] = UIFactory.createButtonRenderer(null,text,m_arrivedIcon,ButtonSize.SMALL);
			container.add(m_arrivedButton[0]);
			
			text = m_resources.getString("ReleasedButton.text");
	        m_releasedButton[0] = UIFactory.createButtonRenderer(null,text,m_releasedIcon,ButtonSize.SMALL);
			container.add(m_releasedButton[0]);
			
			// create editor panel
			container = getEditorComponent();
			container.setLayout(new FlowLayout(FlowLayout.CENTER));
			container.setBackground(m_table.getBackground());
			container.setPreferredSize(d);
			
			text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[1] = DiskoButtonFactory.createButton(null,text,m_arrivedIcon,ButtonSize.SMALL);
			m_arrivedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_table.getModel();					
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					IPersonnelIf reinstated = ResourceUtils.arrivedPersonnel(personnel);
					if(reinstated != personnel)
					{
						// Personnel was reinstated. Replace reference in call-out
						model.getPersonnelList().remove(personnel);
						if(!model.getPersonnelList().exists(reinstated))
						{
							model.getPersonnelList().add(reinstated);
						}
						m_wp.onFlowPerformed(new FlowEvent(m_arrivedButton[1],personnel,FlowEvent.EVENT_CHANGE));
					}					
					fireEditingStopped();
				}
			});
			container.add(m_arrivedButton[1]);

			text = m_resources.getString("ReleasedButton.text");
			m_releasedButton[1] = DiskoButtonFactory.createButton(null,text,m_releasedIcon,ButtonSize.SMALL);
			m_releasedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_table.getModel();					
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					PersonnelStatus oldStatus = personnel.getStatus();
					if(!PersonnelStatus.RELEASED.equals(oldStatus)) {
						ResourceUtils.releasePersonnel(personnel);
						updateCell(m_editCellRow);
						m_wp.onFlowPerformed(new FlowEvent(m_releasedButton[1],personnel,FlowEvent.EVENT_CHANGE));
					}
					fireEditingStopped();
				}
			});
			container.add(m_releasedButton[1]);
		}		
		
		@Override
		protected void updateCell(int row)
		{
			// Get current personnel
			int modelIndex = m_table.convertRowIndexToModel(row);
			CalloutPersonnelTableModel model = (CalloutPersonnelTableModel)m_table.getModel();
			IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);

			if(selectedPersonnel!=null) {
				
				// get status
				PersonnelStatus status = selectedPersonnel.getStatus();
				
				// Set button selection
				if(isEditing()) {
					m_arrivedButton[1].setSelected(status == PersonnelStatus.ARRIVED);
					m_releasedButton[1].setSelected(status == PersonnelStatus.RELEASED);
				} else {
					m_arrivedButton[0].setSelected(status == PersonnelStatus.ARRIVED);
					m_releasedButton[0].setSelected(status == PersonnelStatus.RELEASED);					
				}
			}
		}
	}
}
