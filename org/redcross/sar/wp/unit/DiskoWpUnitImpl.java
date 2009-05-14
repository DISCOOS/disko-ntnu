package org.redcross.sar.wp.unit;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.instrument.IllegalClassFormatException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.redcross.sar.IDiskoRole;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implementation of the Unit work process
 *
 * @author thomasl, kenneth
 */
public class DiskoWpUnitImpl extends AbstractDiskoWpModule implements IDiskoWpUnit, IWorkFlowListener
{
	private JPanel m_contentsPanel;

	private PersonnelTransferHandler m_personnelTransferHandler;

	private JTabbedPane m_mainTabPane;
	private DiskoTable m_personnelTable;
	private DiskoTable m_unitTable;
	private DiskoTable m_calloutTable;

	private JPanel m_leftPanel;
	private PersonnelDetailsLeftPanel m_personnelDetailsLeftPanel;
	private UnitDetailsPanel m_unitDetailsLeftPanel;
	private CalloutDetailsPanel m_calloutDetailsPanel;
	private JLabel m_leftMessageLabel;

	private JPanel m_bottomPanel;
	private PersonnelAddressBottomPanel m_personnelAddressBottomPanel;
	private PersonnelDetailsBottomPanel m_personnelDetailsBottomPanel;
	private JLabel m_bottomMessageLabel;

	private JButton m_newPersonnelButton;
	private JButton m_newUnitButton;
	private JButton m_importCalloutButton;
	private JButton m_deleteButton;

	private String m_leftViewId = PERSONNEL_DETAILS_VIEW_ID;
	private String m_bottomViewId = PERSONNEL_DETAILS_VIEW_ID;

	private UnitTypeDialog m_unitTypeDialog;
	private ImportCalloutDialog m_importCalloutDialog;

	public DiskoWpUnitImpl() throws IllegalClassFormatException
	{
		super(Logger.getLogger(DiskoWpUnitImpl.class));

		// Initialize transfer handler
		try
		{
			m_personnelTransferHandler = new PersonnelTransferHandler(this);
		}
		catch (ClassNotFoundException e)
		{
			m_logger.error("Failed to get transfer handler",e);
		}

		initialize();
		initButtons();
		initTables();

	}

	private void initialize()
	{
		// Properties
		assignWpBundle(IDiskoWpUnit.class);

		// Main panels
		m_contentsPanel = new JPanel(new BorderLayout());
		m_contentsPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		layoutComponent(m_contentsPanel);

		// Left panels
		m_leftPanel = new JPanel(new CardLayout());
		m_leftPanel.setBorder(BorderFactory.createEmptyBorder());
		m_leftPanel.setMinimumSize(new Dimension(300, 550));
		m_leftPanel.setPreferredSize(new Dimension(390, 550));
		m_personnelDetailsLeftPanel = new PersonnelDetailsLeftPanel(this);
		m_leftPanel.add(m_personnelDetailsLeftPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_unitDetailsLeftPanel = new UnitDetailsPanel(this);
		m_leftPanel.add(m_unitDetailsLeftPanel, UNIT_DETAILS_VIEW_ID);
		m_calloutDetailsPanel = new CalloutDetailsPanel(this);
		m_leftPanel.add(m_calloutDetailsPanel, CALLOUT_DETAILS_VIEW_ID);
		JPanel leftMessagePanel = new JPanel(new BorderLayout(0,0));
		leftMessagePanel.setBorder(UIFactory.createBorder());
		m_leftMessageLabel = new JLabel();
		m_leftMessageLabel.setVerticalAlignment(SwingUtilities.CENTER);
		m_leftMessageLabel.setHorizontalAlignment(SwingUtilities.CENTER);
		leftMessagePanel.add(m_leftMessageLabel,BorderLayout.CENTER);
		m_leftPanel.add(leftMessagePanel, MESSAGE_VIEW_ID);


		// Bottom panels
		Dimension bottomPanelDimension = new Dimension(100, 150);
		m_bottomPanel = new JPanel(new CardLayout());
		m_bottomPanel.setBorder(BorderFactory.createEmptyBorder());
		m_personnelDetailsBottomPanel = new PersonnelDetailsBottomPanel(this);
		m_personnelDetailsBottomPanel.setPreferredSize(bottomPanelDimension);
		m_bottomPanel.add(m_personnelDetailsBottomPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_personnelAddressBottomPanel = new PersonnelAddressBottomPanel(this);
		m_bottomPanel.add(m_personnelAddressBottomPanel, PERSONNEL_ADDITIONAL_VIEW_ID);
		JPanel bottomMessagePanel = new JPanel(new BorderLayout(0,0));
		bottomMessagePanel.setBorder(UIFactory.createBorder());
		m_bottomMessageLabel = new JLabel();
		m_bottomMessageLabel.setVerticalAlignment(SwingUtilities.CENTER);
		m_bottomMessageLabel.setHorizontalAlignment(SwingUtilities.CENTER);
		bottomMessagePanel.add(m_bottomMessageLabel,BorderLayout.CENTER);
		m_bottomPanel.add(bottomMessagePanel, MESSAGE_VIEW_ID);

		// Overview panels
		m_mainTabPane = new JTabbedPane();
		m_mainTabPane.setTabPlacement(JTabbedPane.BOTTOM);
		m_mainTabPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				switch(m_mainTabPane.getSelectedIndex()) {
				case 0: m_personnelTable.repaint(); break;
				case 1: m_unitTable.repaint(); break;
				case 2: m_calloutTable.repaint(); break;
				}
			}
			
		});

		// Set up splitters
		JSplitPane horSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		horSplit.setBorder(BorderFactory.createEmptyBorder());
		horSplit.setDividerLocation(0.4);
		horSplit.setLeftComponent(m_leftPanel);
		horSplit.setRightComponent(m_mainTabPane);
		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vertSplit.setBorder(BorderFactory.createEmptyBorder());
		vertSplit.setLeftComponent(horSplit);
		vertSplit.setRightComponent(m_bottomPanel);
		vertSplit.setResizeWeight(1.0);
		m_contentsPanel.add(vertSplit, BorderLayout.CENTER);

		// create dialogs
		m_unitTypeDialog = new UnitTypeDialog(this, getApplication().getFrame());
		m_importCalloutDialog = new ImportCalloutDialog(this, getApplication().getFrame());

		// initialize view
		setLeftMessage(getBundleText("SelectPersonnel.text"));
		setBottomMessage(getBundleText("SelectPersonnel.text"));
		setLeftView(MESSAGE_VIEW_ID);
		setBottomView(MESSAGE_VIEW_ID);
		
	}

	private void initTables()
	{
		// get the dimension of a small button
		Dimension d = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
		
		/* -----------------------------------------
		 * Create Personnel list table
		 * ----------------------------------------- */
		PersonnelTableModel personnelModel = new PersonnelTableModel(getMsoModel());
		personnelModel.setColumnAlignment(2, SwingConstants.CENTER);
		personnelModel.setColumnAlignment(3, SwingConstants.CENTER);
		personnelModel.setColumnAlignment(4, SwingConstants.CENTER);
		personnelModel.setColumnFixedWidth(3, d.width+10);
		personnelModel.setColumnFixedWidth(4, d.width*3+20);
		m_personnelTable = new DiskoTable(personnelModel);
		m_personnelTable.setColumnSelectionAllowed(false);
		m_personnelTable.setRowSelectionAllowed(true);
		m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_personnelTable.addMouseListener(new PersonnelTableMouseListener());
		m_personnelTable.setTransferHandler(m_personnelTransferHandler);
		m_personnelTable.setDragEnabled(true);
		m_personnelTable.setShowVerticalLines(false);
		m_personnelTable.setRowHeight(d.height + 10);
		m_personnelTable.setAutoFitWidths(true);		
		m_personnelTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;

				if(m_leftViewId.equals(UNIT_DETAILS_VIEW_ID))
				{
					// get selected row
					int row = m_personnelTable.getSelectedRow();
					if(row!=-1) {
						setPersonnelBottom((IPersonnelIf)m_personnelTable.getValueAt(row,3));
					}
					setBottomView(PERSONNEL_DETAILS_VIEW_ID);
				}
			}

		});

		// set personnel table editor
		TableEditorFactory.installPersonnelEditor(m_personnelTable,this,3,4);
		
		// initialize header
		JTableHeader header = m_personnelTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

		// install row sorter
		TableRowSorter<PersonnelTableModel> personnelSorter =
				new TableRowSorter<PersonnelTableModel>(personnelModel);
		m_personnelTable.setRowSorter(personnelSorter);
		personnelSorter.setMaxSortKeys(1);
		personnelSorter.setSortsOnUpdates(true);
		personnelSorter.setSortable(3, false);
		personnelSorter.setSortable(4, false);

		// add to tabbed pane
		JScrollPane scrollPane = UIFactory.createScrollPane(m_personnelTable,true,5,5,5,5);
		m_mainTabPane.addTab(getBundleText("Personnel.text"),
				DiskoIconFactory.getIcon("GENERAL.PERSONNELLIST", "32x32"), scrollPane);

		/* -----------------------------------------
		 * Create Unit list table
		 * ----------------------------------------- */
		UnitTableModel unitModel = new UnitTableModel(getMsoModel());
		unitModel.setColumnAlignment(1, SwingConstants.CENTER);
		unitModel.setColumnAlignment(2, SwingConstants.CENTER);
		unitModel.setColumnFixedWidth(1, d.width+10);
		unitModel.setColumnFixedWidth(2, d.width*2+15);
		m_unitTable = new DiskoTable(unitModel);
		m_unitTable.setColumnSelectionAllowed(false);
		m_unitTable.setRowSelectionAllowed(true);
		m_unitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_unitTable.addMouseListener(new UnitTableMouseListener());
		m_unitTable.setShowVerticalLines(false);
		m_unitTable.setAutoFitWidths(true);		
		m_unitTable.setRowHeight(d.height + 10);
		
		// set unit table editor
		TableEditorFactory.installUnitEditor(m_unitTable,this,1,2);

		// initialize header
		header = m_unitTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		
		// install row sorter
		TableRowSorter<UnitTableModel> unitSorter =
			new TableRowSorter<UnitTableModel>(unitModel);
		m_unitTable.setRowSorter(unitSorter);;
		unitSorter.setMaxSortKeys(1);
		unitSorter.setSortsOnUpdates(true);
		unitSorter.setSortable(1, false);
		unitSorter.setSortable(2, false);
		
		// add to tabbed pane
		scrollPane = UIFactory.createScrollPane(m_unitTable,true,5,5,5,5);
		m_mainTabPane.addTab(getBundleText("Unit.text"),
				DiskoIconFactory.getIcon("GENERAL.UNITLIST", "32x32"), scrollPane);
		
		/* -----------------------------------------
		 * Create call-out list table
		 * ----------------------------------------- */
		CalloutTableModel calloutModel = new CalloutTableModel(getMsoModel());
		calloutModel.setColumnAlignment(1, SwingConstants.CENTER);
		calloutModel.setColumnFixedWidth(1, d.width+10);
		m_calloutTable = new DiskoTable(calloutModel);
		m_calloutTable.setColumnSelectionAllowed(false);
		m_calloutTable.setRowSelectionAllowed(true);
		m_calloutTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
		m_calloutTable.addMouseListener(new CalloutTableMouseListener());
		m_calloutTable.setRowHeight(d.height + 10);
		m_calloutTable.setAutoFitWidths(true);
		
		// initialize header
		header = m_calloutTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		
		// install row sorter
		TableRowSorter<CalloutTableModel> calloutSorter =
			new TableRowSorter<CalloutTableModel>(calloutModel);
		m_calloutTable.setRowSorter(calloutSorter);;
		calloutSorter.setMaxSortKeys(1);
		calloutSorter.setSortsOnUpdates(true);
		
		// add to tabbed pane
		scrollPane = UIFactory.createScrollPane(m_calloutTable,true,5,5,5,5);
		m_mainTabPane.addTab(getBundleText("CallOut.text"),
				DiskoIconFactory.getIcon("GENERAL.CALLOUTLIST", "32x32"), scrollPane);
	}

	private void initButtons()
	{
		String text = getBundleText("NewPersonnelButton.text");
		Icon icon = DiskoIconFactory.getIcon(getBundleText("NewPersonnelButton.icon"),"48x48");
		m_newPersonnelButton = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.NORMAL);
		m_newPersonnelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				newPersonnel();
			}
		});
		layoutButton(m_newPersonnelButton);

		text = getBundleText("NewUnitButton.text");
		icon = DiskoIconFactory.getIcon(getBundleText("NewUnitButton.icon"),"48x48");
		m_newUnitButton = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.NORMAL);
		m_newUnitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				newUnit();
			}
		});
		layoutButton(m_newUnitButton);

		text = getBundleText("ImportCalloutButton.text");
		icon = DiskoIconFactory.getIcon(getBundleText("ImportCalloutButton.icon"),"48x48");
		m_importCalloutButton = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.NORMAL);
		m_importCalloutButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				importCallout();
			}
		});
		layoutButton(m_importCalloutButton);

		m_deleteButton = DiskoButtonFactory.createButton("GENERAL.DELETE",ButtonSize.NORMAL);
		m_deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				delete();
			}
		});
		layoutButton(m_deleteButton);
	}

	@Override
	public String getCaption()
	{
		return getBundleText("UNIT");
	}

	@Override
	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// setup of navigation bar needed?
		if(isNavMenuSetupNeeded()) {
			// forward
			setupNavMenu(Utils.getListNoneOf(MapToolType.class),false);
		}
	}

	@Override
	public boolean confirmDeactivate()
	{
		// prevent reentry
		if(isWorking()) {
			// notify
			Utils.showWarning(getBundleText("Working.header"), getBundleText("Working.text"));
			// do not allow to deactivate
			return false;
		}

		// validate data
		if(isChanged()) {

			// prompt user
			String[] options = {getBundleText("DirtyMessageWarning.commit"),
					getBundleText("DirtyMessageWarning.rollback"),getBundleText("DirtyMessageWarning.cancel")};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
						getBundleText("DirtyMessageWarning.text"),
						getBundleText("DirtyMessageWarning.header"), JOptionPane.YES_NO_CANCEL_OPTION,
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);

			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// forward
				return commit();
			case JOptionPane.NO_OPTION:
				// forward
				return rollback();
			case JOptionPane.CANCEL_OPTION:
				// do not deactivate
				return false;
			}
		}

		// deactivate
		return true;
	}

	public boolean commit()
	{
    	if(isEditValid()) {
    		    		
			// try to commit changes
            try {
                getMsoModel().commit(getMsoModel().getChanges(getUncomittedChanges()));
                return super.commit();
    		} catch (TransactionException ex) {
    			m_logger.error("Failed to commit changed unit data",ex);
    		}            
    	}
    	return false;
	}

	/**
	 * Cancel any creation process
	 */
	public boolean rollback()
	{
    	if(isChanged()) {
    		
            try {
                getMsoModel().rollback(getMsoModel().getChanges(getUncomittedChanges()));
                return super.rollback();
    		} catch (TransactionException ex) {
    			m_logger.error("Failed to rollback changed unit data",ex);
    		}            
    	}
    	return false;
	}
	
	/**
	 * Set up new personnel creation process
	 */
	private void newPersonnel()
	{
		// Single new object at a time
		if(isEditValid())
		{
			// create personnel
			IPersonnelIf personnel = getMsoManager().createPersonnel();
			m_personnelDetailsLeftPanel.setCaptionText("(" + this.getBundleText("New.text") + ")");

			// set data
			setPersonnelLeft(personnel);
			setPersonnelBottom(personnel);

			// select personnel table tab
			setMainTab(0);

			// update views
			setLeftView(PERSONNEL_DETAILS_VIEW_ID);
			setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);

			// notify
			fireOnWorkChange(personnel);
			
		} 
	}

	/**
	 * Set up new unit creation process
	 */
	private void newUnit()
	{
		// Single new object at a time
		if(isEditValid())
		{

			// select unit
			UnitType type = m_unitTypeDialog.select();
			
			// unit selected?
			if(type!=null) {
				IUnitIf newUnit = null;
				switch(type)
				{
				case BOAT:
					newUnit = getMsoModel().getMsoManager().createBoat("");
					break;
				case VEHICLE:
					newUnit = getMsoModel().getMsoManager().createVehicle("");
					break;
				case DOG:
					newUnit = getMsoModel().getMsoManager().createDog("");
					break;
				case AIRCRAFT:
					newUnit = getMsoModel().getMsoManager().createAircraft("");
					break;
				case TEAM:
					newUnit = getMsoModel().getMsoManager().createTeam("");
					break;
				}

				if(newUnit != null)
				{
					// set data
					setUnit(newUnit);
					
					// update views
					setLeftView(UNIT_DETAILS_VIEW_ID);
					setBottomView(MESSAGE_VIEW_ID);
					setBottomMessage(getBundleText("AddPersonnel.text"));
					
					// select unit table tab
					setMainTab(1);

					// notify change
					fireOnWorkChange(newUnit);
					
				}	
			}
		}	
	}

	/**
	 * Creates call-out and imports personnel
	 */
	private void importCallout()
	{
		// single object at the time
		if(isEditValid()) {

			// set dialog position
			m_importCalloutDialog.setSnapToLocation(m_contentsPanel,DefaultDialog.POS_CENTER, DefaultDialog.SIZE_TO_OFF, true, false);
			
			// prompt user for import
			ICalloutIf callout = m_importCalloutDialog.importCallout();
			
			// was a call-out imported?
			if(callout!=null) {
				
				// set data
				setCallout(callout);

				// select call-out tab
				setMainTab(2);

				// notify
				fireOnWorkChange(callout);
				
			}
		} 
	}
	
	/**
	 * Called when delete is pressed, determines what to delete based on the contents of the details panel
	 */
	private void delete()
	{
		// initialize
		JTable table = null;
		// get selected mso object
		switch(m_mainTabPane.getSelectedIndex()) {
		case 0: 
			table = m_personnelTable; 
			PersonnelTableModel personnelModel = (PersonnelTableModel)table.getModel();
			if(table.getSelectedRow()>=0) {
				IPersonnelIf personnel  = personnelModel.getPersonnel(
						table.convertRowIndexToModel(table.getSelectedRow()));
				if(personnel != null)
				{
					//  Confirm delete
					String name = MsoUtils.getPersonnelName(personnel, false);
					String[] options = {this.getBundleText("Delete.text"), this.getBundleText("Cancel.text")};
					int n = JOptionPane.showOptionDialog(
							this.getApplication().getFrame(),
							String.format(this.getBundleText("DeletePersonnel.text"),name),
							this.getBundleText("DeletePersonnel.header"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);

					if(n == JOptionPane.YES_OPTION)
					{
						if(!MsoUtils.delete(personnel, 0))
						{
							// notify failure
							Utils.showError(
								this.getBundleText("CanNotDeletePersonnel.header"),
								this.getBundleText("CanNotDeletePersonnel.details"));
						} else {
							fireOnWorkChange(personnel);
						}
					}
				}
			}
			break;
		case 1: 
			table = m_unitTable; 
			UnitTableModel unitModel = (UnitTableModel)table.getModel();
			if(table.getSelectedRow()>=0) {
				IUnitIf unit = unitModel.getUnit(
						table.convertRowIndexToModel(table.getSelectedRow()));
				if(unit != null)
				{
	
					//  Confirm delete
					String name = MsoUtils.getUnitName(unit, false);
					String[] options = {this.getBundleText("Delete.text"), this.getBundleText("Cancel.text")};
					int n = JOptionPane.showOptionDialog(
							this.getApplication().getFrame(),
							String.format(this.getBundleText("DeleteUnit.text"),name),
							this.getBundleText("DeleteUnit.header"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							options,
							options[0]);
	
					if(n == JOptionPane.YES_OPTION)
					{
						if(!MsoUtils.delete(unit, 0)) {
							Utils.showError(
								this.getBundleText("CanNotDeleteUnit.header"),
								this.getBundleText("CanNotDeleteUnit.details"));
						} else {
							fireOnWorkChange(unit);
						}
					}
				}
			}
			break;
		case 2: table = m_calloutTable; break;
		}
		
	}

	public boolean isEditValid() {
		if(isChanged()) {
			if(!m_personnelDetailsLeftPanel.isEditValid()) {
				m_leftViewId = PERSONNEL_DETAILS_VIEW_ID;
				setView(m_leftPanel,m_leftViewId);
				return false;
			}
			if(!m_unitDetailsLeftPanel.isEditValid()) {
				m_leftViewId = UNIT_DETAILS_VIEW_ID;
				setView(m_leftPanel,m_leftViewId);
				return false;
			}
			if(!m_calloutDetailsPanel.isEditValid()) {
				m_leftViewId = CALLOUT_DETAILS_VIEW_ID;
				setView(m_leftPanel,m_leftViewId);
				return false;
			}
		}
		return true;
	}
	
	public boolean isDataValid() {
		return false;
	}
	
	/**
	 * Sets personnel in left and bottom detail view
	 * @param personnel
	 */
	public boolean setPersonnel(IPersonnelIf personnel)	{
		if(m_personnelDetailsLeftPanel.isEditValid())
		{
			m_personnelDetailsLeftPanel.setPersonnel(personnel);
			m_personnelDetailsBottomPanel.setPersonnel(personnel);
			m_personnelAddressBottomPanel.setPersonnel(personnel);
			m_personnelTable.repaint();
			return true;
		}
		return false;
	}
	
	/**
	 * Sets personnel in left detail view
	 * @param personnel
	 */
	public boolean setPersonnelLeft(IPersonnelIf personnel)
	{
		if(!isChanged() || m_personnelDetailsLeftPanel.isEditValid())
		{
			m_personnelDetailsLeftPanel.setPersonnel(personnel);
			m_personnelAddressBottomPanel.setPersonnel(personnel);
			m_personnelTable.repaint();
			return true;
		}
		return false;
	}

	/**
	 * Sets personnel in bottom view
	 */
	public void setPersonnelBottom(IPersonnelIf personnel)
	{
		m_personnelDetailsBottomPanel.setPersonnel(personnel);
		m_personnelAddressBottomPanel.setPersonnel(personnel);
	}

	/**
	 * Sets unit in detail view
	 * @param unit
	 */
	public boolean setUnit(IUnitIf unit)
	{
		if(!isChanged() || m_unitDetailsLeftPanel.isEditValid())
		{			
			m_unitDetailsLeftPanel.setUnit(unit);
			m_unitDetailsLeftPanel.updateContents();	
			m_unitTable.repaint();
			return true;
		}
		return false;		
	}
	
	public boolean setCallout(ICalloutIf callout) {
		
		if(!isChanged() || m_calloutDetailsPanel.isEditValid())
		{
			m_calloutDetailsPanel.setCallOut(callout);
			m_calloutDetailsPanel.updateFieldContents();		
			m_calloutTable.repaint();
			return true;
		}
		return false;		
	}

	public void setLeftMessage(String msg)
	{
		m_leftMessageLabel.setText(msg);
	}

	public void setBottomMessage(String msg)
	{
		m_bottomMessageLabel.setText(msg);
	}

	public String getLeftViewID() {
		return m_leftViewId;
	}
	
	/**
	 * Sets whether personnel, unit or call out panels should be displayed in the left panel
	 *
	 * @param viewId
	 */
	public boolean setLeftView(String viewId)
	{
		if(isEditValid()) {
			m_leftViewId = viewId;
			setView(m_leftPanel,viewId);
			return true;
		}
		return false;
	}

	/**
	 * Select which of the overview tabs that should be shown
	 * 0 - Personnel
	 * 1 - Unit
	 * 2 - Call-out
	 */
	public void setMainTab(int index)
	{
		m_mainTabPane.setSelectedIndex(index);
	}

	public String getBottomViewID() {
		return m_bottomViewId;
	}
	
	/**
	 * Sets the bottom view panel
	 * @param viewId
	 */
	public void setBottomView(String viewId)
	{
		m_bottomViewId = viewId;
		setView(m_bottomPanel,viewId);
	}
	
	private void setView(JPanel panel, String viewId) {
		CardLayout layout = (CardLayout)panel.getLayout();
		layout.show(panel, viewId);		
	}

	/**
	 * @return Personnel that is being edited, <code>null</code> otherwise 
	 */
	public IPersonnelIf getEditingPersonnel()
	{
		return m_personnelDetailsLeftPanel.getPersonnel();
	}

	/**
	 * @return Unit that is being edited, <code>null</code> otherwise 
	 */
	public IUnitIf getEditingUnit()
	{
		return m_unitDetailsLeftPanel.getUnit();
	}

	/**
	 * @return Unit that is being edited, <code>null</code> otherwise 
	 */
	public ICalloutIf getEditingCallout()
	{
		return m_calloutDetailsPanel.getCallOut();
	}
	
	/**
	 * Updates personnel details panel based on user selection
	 *
	 * @author thomasl, kenneth
	 */
	private class PersonnelTableMouseListener extends DiskoMouseAdapter
	{
		@Override
		public void mouseDownExpired(MouseEvent e) {
			handle(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			int clickCount = e.getClickCount();
			if(clickCount > 1)
			{
				handle(e);
			}
		}

		private void handle(MouseEvent e) {

			Point clickedPoint = new Point(e.getX(), e.getY());
			int clickedColumn = m_personnelTable.columnAtPoint(clickedPoint);
			int clickedRow = m_personnelTable.rowAtPoint(clickedPoint);
			PersonnelTableModel model = (PersonnelTableModel)m_personnelTable.getModel();
			IPersonnelIf clickedPersonnel = model.getPersonnel(clickedRow);

			if(clickedColumn == 0)
			{
				if(m_leftViewId.equals(PERSONNEL_DETAILS_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
				{
					if(setPersonnelLeft(clickedPersonnel)) {
						setPersonnelBottom(clickedPersonnel);
						setLeftView(PERSONNEL_DETAILS_VIEW_ID);
						setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);
					}
				}
				else if(m_leftViewId.equals(UNIT_DETAILS_VIEW_ID))
				{
					setPersonnelBottom(clickedPersonnel);
					setBottomView(PERSONNEL_DETAILS_VIEW_ID);
				}
			}
		}
	}

	/**
	 * Updates unit details panel based on user selection
	 *
	 * @author thomasl
	 */
	private class UnitTableMouseListener extends DiskoMouseAdapter
	{
		@Override
		public void mouseDownExpired(MouseEvent e) {
			handle(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			int clickCount = e.getClickCount();
			if(clickCount > 1)
			{
				handle(e);
			}
		}

		private void handle(MouseEvent e) {

			Point clickedPoint = new Point(e.getX(), e.getY());
			int clickedColumn = m_unitTable.columnAtPoint(clickedPoint);
			int clickedRow = m_unitTable.rowAtPoint(clickedPoint);
			UnitTableModel model = (UnitTableModel)m_unitTable.getModel();
			IUnitIf clickedUnit = model.getUnit(clickedRow);

			if(clickedColumn == 0)
			{
				// update bottom message
				setBottomMessage(getBundleText("SelectUnitPersonnel.text"));
				// Show unit in left detail panel
				if(setUnit(clickedUnit)) {
					setLeftView(UNIT_DETAILS_VIEW_ID);
					setBottomView(MESSAGE_VIEW_ID);
				}
			}

		}

	}

	/**
	 * Updates callout details panel based on user selection
	 *
	 * @author thomasl
	 */
	private class CalloutTableMouseListener extends DiskoMouseAdapter
	{
		@Override
		public void mouseDownExpired(MouseEvent e) {
			handle(e);
		}

		public void mouseClicked(MouseEvent e)
		{
			int clickCount = e.getClickCount();
			if(clickCount > 1)
			{
				handle(e);
			}
		}

		private void handle(MouseEvent e) {

			// Show call out view
			if(m_leftViewId.equals(CALLOUT_DETAILS_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
			{

				// get callout
				Point clickedPoint = new Point(e.getX(), e.getY());
				int row = m_calloutTable.rowAtPoint(clickedPoint);
				int index = m_calloutTable.convertRowIndexToModel(row);
				if(index==-1) return;
				CalloutTableModel model = (CalloutTableModel)m_calloutTable.getModel();
				ICalloutIf callout = model.getCallout(index);

				// update bottom message
				setBottomMessage(getBundleText("SelectCallOutPersonnel.text"));

				// show callout view
				m_calloutDetailsPanel.setCallOut(callout);
				m_calloutDetailsPanel.updateFieldContents();
				setLeftView(CALLOUT_DETAILS_VIEW_ID);
				setBottomView(MESSAGE_VIEW_ID);

			}

		}
	}

	public void afterOperationChange()
	{
		super.afterOperationChange();
		
		if(getMsoModel().getMsoManager().operationExists()) {
			setPersonnelLeft(null);
			setPersonnelBottom(null);
			setUnit(null);
			setCallout(null);
			ICmdPostIf cmdPost = getMsoModel().getMsoManager().getCmdPost();
			PersonnelTableModel m1 = (PersonnelTableModel)m_personnelTable.getModel();
			m1.load(cmdPost.getAttendanceList());
			m_personnelTable.autoFitWidthColumns();
			UnitTableModel m2 = (UnitTableModel)m_unitTable.getModel();
			m2.load(cmdPost.getUnitList());
			m_unitTable.autoFitWidthColumns();
			CalloutTableModel m3 = (CalloutTableModel)m_calloutTable.getModel();
			m3.load(cmdPost.getCalloutList());
			m_calloutTable.autoFitWidthColumns();
		}

	}

}
