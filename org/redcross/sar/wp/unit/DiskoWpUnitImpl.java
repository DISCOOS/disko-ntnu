package org.redcross.sar.wp.unit;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.instrument.IllegalClassFormatException;

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implementation of the Unit work process
 * 
 * @author thomasl
 */
public class DiskoWpUnitImpl extends AbstractDiskoWpModule implements IDiskoWpUnit, IDiskoWorkListener
{
	private JPanel m_contentsPanel;
	
	private PersonnelTransferHandler m_personnelTransferHandler;
	
	private static JTabbedPane m_overviewTabPane;
	private static JTable m_personnelOverviewTable;
	private static JTable m_unitOverviewTable;
	private static JTable m_calloutOverviewTable;
	
	private static JPanel m_leftPanel;
	private static PersonnelDetailsLeftPanel m_personnelLeftDetailsPanel;
	private static UnitDetailsPanel m_unitDetailsLeftPanel;
	private static CalloutDetailsPanel m_calloutDetailsPanel;
	private static JLabel m_leftMessageLabel;
	
	private static JPanel m_bottomPanel;
	private static PersonnelAddressBottomPanel m_personnelAddressBottomPanel;
	private static PersonnelDetailsBottomPanel m_personnelBottomDetailsPanel;
	private static JLabel m_bottomMessageLabel;

	private JButton m_newPersonnelButton;
	private JButton m_newUnitButton;
	private JButton m_importCalloutButton;
	private JButton m_deleteButton;
	
	private static String m_leftViewId = PERSONNEL_DETAILS_VIEW_ID;
//	private static String m_bottomViewId = PERSONNEL_DETAILS_VIEW_ID;
	
	UnitTypeDialog m_unitTypeDialog;
	
	ImportCalloutDialog m_importCalloutDialog;
	
	private static IMsoModelIf m_msoModel;
	
	private static boolean m_newPersonnel = false;
	private static boolean m_newUnit = false;
	private static boolean m_newCallOut = false;

	public DiskoWpUnitImpl(IDiskoRole role) throws IllegalClassFormatException
	{
		super(role);
		
		// Initialize transfer handler
		try
		{
			m_personnelTransferHandler = new PersonnelTransferHandler(this);
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		initialize();
		initButtons();
		initTables();
		
		PersonnelUtilities.setMsoManager(this.getMsoManager());
		m_msoModel = this.getMsoModel();
	}
	
	private void initialize()
	{
		// Properties
		loadProperties("properties");
		assignWpBundle(IDiskoWpUnit.class);

		// Main panels
		m_contentsPanel = new JPanel(new BorderLayout());
		layoutComponent(m_contentsPanel);
		
		// Left panels
		m_leftPanel = new JPanel(new CardLayout());
		m_leftPanel.setMinimumSize(new Dimension(300, 550));
		m_leftPanel.setPreferredSize(new Dimension(390, 550));
		m_personnelLeftDetailsPanel = new PersonnelDetailsLeftPanel(this);
		m_leftPanel.add(m_personnelLeftDetailsPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_unitDetailsLeftPanel = new UnitDetailsPanel(this);
		m_leftPanel.add(m_unitDetailsLeftPanel, UNIT_VIEW_ID);
		m_calloutDetailsPanel = new CalloutDetailsPanel(this);
		m_leftPanel.add(m_calloutDetailsPanel, CALLOUT_VIEW_ID);
		JPanel leftMessagePanel = new JPanel();
		m_leftMessageLabel = new JLabel();
		leftMessagePanel.add(m_leftMessageLabel);
		m_leftPanel.add(leftMessagePanel, MESSAGE_VIEW_ID);
		
		
		// Bottom panels
		Dimension bottomPanelDimension = new Dimension(100, 150);
		m_bottomPanel = new JPanel(new CardLayout());
		m_personnelBottomDetailsPanel = new PersonnelDetailsBottomPanel(this);
		m_personnelBottomDetailsPanel.setPreferredSize(bottomPanelDimension);
		m_bottomPanel.add(m_personnelBottomDetailsPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_personnelAddressBottomPanel = new PersonnelAddressBottomPanel();
		m_bottomPanel.add(m_personnelAddressBottomPanel, PERSONNEL_ADDITIONAL_VIEW_ID);
		JPanel bottomMessagePanel = new JPanel();
		m_bottomMessageLabel = new JLabel();
		bottomMessagePanel.add(m_bottomMessageLabel);
		m_bottomPanel.add(bottomMessagePanel, MESSAGE_VIEW_ID);
		
		
		// Overview panels
		m_overviewTabPane = new JTabbedPane();
		m_overviewTabPane.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent ce)
			{
				JTabbedPane pane = (JTabbedPane)ce.getSource();
				int index = pane.getSelectedIndex();
				CardLayout layout = null;
				switch(index)
				{
				case 0:
					IUnitIf selectedUnit = m_unitDetailsLeftPanel.getUnit();
					if(selectedUnit == null)
					{
						layout = (CardLayout)m_leftPanel.getLayout();
						layout.show(m_leftPanel, PERSONNEL_DETAILS_VIEW_ID);
						m_leftViewId = PERSONNEL_DETAILS_VIEW_ID;
						layout = (CardLayout)m_bottomPanel.getLayout();
						layout.show(m_bottomPanel, PERSONNEL_ADDITIONAL_VIEW_ID);
					}
					break;
				case 1:
					if(!m_newUnit)
					{
						m_unitDetailsLeftPanel.setUnit(null);
						m_leftMessageLabel.setText(getText("SelectUnit.text"));
						layout = (CardLayout)m_leftPanel.getLayout();
						layout.show(m_leftPanel, MESSAGE_VIEW_ID);
						m_leftViewId = MESSAGE_VIEW_ID;
						m_bottomMessageLabel.setText(getText("SelectUnit.text"));
						layout = (CardLayout)m_bottomPanel.getLayout();
						layout.show(m_bottomPanel, MESSAGE_VIEW_ID);
					}
					break;
				case 2:
					m_leftMessageLabel.setText(getText("SelectCallOut.text"));
					layout = (CardLayout)m_leftPanel.getLayout();
					layout.show(m_leftPanel, MESSAGE_VIEW_ID);
					m_bottomMessageLabel.setText(getText("SelectCallOut.text"));
					layout = (CardLayout)m_bottomPanel.getLayout();
					layout.show(m_bottomPanel, MESSAGE_VIEW_ID);
					m_leftViewId = MESSAGE_VIEW_ID;
					break;
				}
					
			}
		});
			
		// Set up splitters
		JSplitPane horSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		horSplit.setDividerLocation(0.4);
		horSplit.setLeftComponent(m_leftPanel);
		horSplit.setRightComponent(m_overviewTabPane);
		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vertSplit.setLeftComponent(horSplit);
		vertSplit.setRightComponent(m_bottomPanel);
//		vertSplit.setDividerLocation(1.0);
		vertSplit.setResizeWeight(1.0);
		m_contentsPanel.add(vertSplit, BorderLayout.CENTER);
		
		m_unitTypeDialog = new UnitTypeDialog(this, m_overviewTabPane);
		m_unitTypeDialog.addDiskoWorkEventListener(this);
		
		m_importCalloutDialog = new ImportCalloutDialog(this);
		m_importCalloutDialog.addDiskoWorkEventListener(this);
	}

	private void initTables()
	{
		// Personnel
		PersonnelOverviewTableModel personnelModel = new PersonnelOverviewTableModel(this);
		m_personnelOverviewTable = new JTable(personnelModel);
		m_personnelOverviewTable.setColumnSelectionAllowed(false);
		m_personnelOverviewTable.setRowSelectionAllowed(true);
		m_personnelOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_personnelOverviewTable.addMouseListener(new PersonnelTableMouseListener());
		m_personnelOverviewTable.setTransferHandler(m_personnelTransferHandler);
		m_personnelOverviewTable.setDragEnabled(true);
		
		TableRowSorter<PersonnelOverviewTableModel> tableRowSorter = 
				new TableRowSorter<PersonnelOverviewTableModel>(personnelModel);
		m_personnelOverviewTable.setRowSorter(tableRowSorter);;
		tableRowSorter.setSortsOnUpdates(true);
		tableRowSorter.setSortable(2, false);
		tableRowSorter.setSortable(3, false);
		
		PersonnelOverviewTableEditor personnelRenderer = new PersonnelOverviewTableEditor(this);
		personnelRenderer.setTable(m_personnelOverviewTable);
		
		Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
		
		m_personnelOverviewTable.setRowHeight(dim.height + 10);
		TableColumn column = m_personnelOverviewTable.getColumnModel().getColumn(2);
		column.setMaxWidth(dim.width + 10);
		column = m_personnelOverviewTable.getColumnModel().getColumn(3);
		column.setPreferredWidth(dim.width * 3 + 20);
		column.setMaxWidth(dim.width * 3 + 20);
	    
		//m_personnelOverviewTable.setTableHeader(null);
		
		JScrollPane personnelOverviewScrollPane = new JScrollPane(m_personnelOverviewTable);
		m_overviewTabPane.addTab(getText("Personnel.text"), personnelOverviewScrollPane);
		
		// Unit
		UnitOverviewTableModel unitModel = new UnitOverviewTableModel(this);
		m_unitOverviewTable = new JTable(unitModel);
		m_unitOverviewTable.setColumnSelectionAllowed(false);
		m_unitOverviewTable.setRowSelectionAllowed(true);
		m_unitOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_unitOverviewTable.addMouseListener(new UnitTableMouseListener());
		
		UnitOverviewTableEditor unitRenderer = new UnitOverviewTableEditor(this);
		unitRenderer.setTable(m_unitOverviewTable);
		
		m_unitOverviewTable.setRowHeight(dim.height + 10);
		column = m_unitOverviewTable.getColumnModel().getColumn(1);
		column.setPreferredWidth(dim.width + 10);
		column.setMaxWidth(dim.width + 10);
		column = m_unitOverviewTable.getColumnModel().getColumn(2);
		column.setPreferredWidth(dim.width * 2 + 15);
		column.setMaxWidth(dim.width * 2 + 15);
		
		m_unitOverviewTable.setTableHeader(null);

		JScrollPane unitOverviewScrollPane = new JScrollPane(m_unitOverviewTable);
		m_overviewTabPane.addTab(getText("Unit.text"), unitOverviewScrollPane);
		
		// Call-out
		CalloutOverviewTableModel calloutModel = new CalloutOverviewTableModel(this);
		m_calloutOverviewTable = new JTable(calloutModel);
		m_calloutOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_calloutOverviewTable.addMouseListener(new CalloutTableMouseListener());
		
		m_calloutOverviewTable.setRowHeight(dim.height + 10);
		column = m_calloutOverviewTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(80);
		column.setMaxWidth(80);
		
		m_calloutOverviewTable.setTableHeader(null);
		
		JScrollPane OverviewScrollPane = new JScrollPane(m_calloutOverviewTable);
		m_overviewTabPane.addTab(getText("CallOut.text"), OverviewScrollPane);
	}
	
	private void initButtons()
	{
		String text = getText("NewPersonnelButton.text");
		Icon icon = DiskoIconFactory.createImageIcon("NEW_PERSONNEL", getText("NewPersonnelButton.icon")); 
		m_newPersonnelButton = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.NORMAL);
		m_newPersonnelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				newPersonnel();
			}
		});
		layoutButton(m_newPersonnelButton);
		
		text = getText("NewUnitButton.text");
		icon = DiskoIconFactory.createImageIcon("NEW_UNIT", getText("NewUnitButton.icon")); 
		m_newUnitButton = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.NORMAL);
		m_newUnitButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				newUnit();
			}
		});
		layoutButton(m_newUnitButton);
		
		text = getText("ImportCalloutButton.text");
		icon = DiskoIconFactory.createImageIcon("IMPORT_CALLOUT", getText("ImportCalloutButton.icon")); 
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
		return getText("Caption");
	}
	
	@Override
	public void activated()
	{
		super.activated();

		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// forward
			setupNavBar(Utils.getListNoneOf(DiskoToolType.class),false);
		}		
	}
	
	@Override
	public void deactivated()
	{
		super.deactivated();
	}
	
	@Override
	public boolean confirmDeactivate()
	{
		// prevent reentry
		if(isWorking()) {
			// notify
			Utils.showWarning(getText("Working.header"), getText("Working.text"));
			// do not allow to deactivate
			return false;
		}
		
		// validate data
		if(isChanged()) {
							
			// prompt user
			String[] options = {getText("DirtyMessageWarning.commit"),
					getText("DirtyMessageWarning.rollback"),getText("DirtyMessageWarning.cancel")};
			int ans = JOptionPane.showOptionDialog(getApplication().getFrame(),
						getText("DirtyMessageWarning.text"),
						getText("DirtyMessageWarning.header"), JOptionPane.YES_NO_CANCEL_OPTION, 
		                JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
			
			// select action
			switch(ans) {
			case JOptionPane.OK_OPTION:
				// forward
				return finish();
			case JOptionPane.NO_OPTION:
				// forward
				return cancel();						
			case JOptionPane.CANCEL_OPTION:
				// do not deactivate
				return false;
			}						
		}	
		
		// deactivate
		return true;
	}

	/**
	 * Cancel any creation process
	 */
	public boolean cancel()
	{
		if(m_newPersonnel)
		{
			//m_personnelLeftDetailsPanel.setPersonnel(null);
			//m_personnelLeftDetailsPanel.updateFieldContents();
			
			//m_personnelBottomDetailsPanel.setPersonnel(null);
			//m_personnelBottomDetailsPanel.updateFieldContents();
			
			m_newPersonnel = false;
			m_newPersonnelButton.setSelected(false);
			
			m_overviewTabPane.setEnabled(true);
			m_personnelOverviewTable.setEnabled(true);
		}
		
		if(m_newUnit)
		{
			//m_unitDetailsLeftPanel.setUnit(null);
			//m_unitDetailsLeftPanel.updateFieldContents();
			
			m_newUnit = false;
			m_newUnitButton.setSelected(false);
			
			m_overviewTabPane.setEnabled(true);
		}
		
		if(m_newCallOut)
		{
			m_overviewTabPane.setEnabled(true);
			
			//m_calloutDetailsPanel.setCallOut(null);
			//m_calloutDetailsPanel.updateFieldContents();
			
			m_newCallOut = false;
			m_importCalloutButton.setSelected(false);
		}
		
		this.getMsoModel().rollback();
		
		// success
		return true;
	}

	public boolean finish()
	{
		
		this.getMsoModel().suspendClientUpdate();
		
		// create a new personnel?
		if(m_newPersonnel)
		{
			// create data
			IPersonnelIf personnel = this.getMsoManager().createPersonnel();
			m_personnelLeftDetailsPanel.setPersonnel(personnel);
			m_newPersonnel = false;
			m_newPersonnelButton.setSelected(false);
			
			m_overviewTabPane.setEnabled(true);
			m_personnelOverviewTable.setEnabled(true);
		}
		
		// try to save personel
		if(!m_personnelLeftDetailsPanel.savePersonnel()) {
			// is new personell?
			if(m_newPersonnel) {
				// delete object!
				m_personnelLeftDetailsPanel.getPersonnel().deleteObject();
			}
			// failed?
			if(m_leftViewId == PERSONNEL_DETAILS_VIEW_ID) {
				// resume update
				this.getMsoModel().resumeClientUpdate();
				// failed!
				return false;
			}
		}
				
		// save adress information
		m_personnelAddressBottomPanel.savePersonnel();
		
		// Check for new unit
		m_unitDetailsLeftPanel.saveUnit();
		
		if(m_newUnit)
		{
			// Unit is set to ready on first commit
			IUnitIf unit = m_unitDetailsLeftPanel.getUnit();
			unit.setStatus(UnitStatus.READY);
			
			m_unitOverviewTable.setEnabled(true);
			m_newUnitButton.setSelected(false);
			m_newUnit = false;
		}
		
		// Check for new call-out
		if(m_newCallOut)
		{
			m_overviewTabPane.setEnabled(true);
			
			m_newCallOut = false;
			m_importCalloutButton.setSelected(false);
		}
		m_calloutDetailsPanel.saveCallOut();
		
		
		this.getMsoModel().resumeClientUpdate();
		
		// committ changes
		this.getMsoModel().commit();

		// success!
		return true;
		
	}
	
	/**
	 * Set up new personnel creation process
	 */
	private void newPersonnel()
	{
		// Single new object at a time
		if(!(m_newPersonnel || m_newUnit || m_newCallOut))
		{
			// View personnel details
			CardLayout layout = (CardLayout)m_leftPanel.getLayout();
			layout.show(m_leftPanel, PERSONNEL_DETAILS_VIEW_ID);
			
			m_leftViewId = PERSONNEL_DETAILS_VIEW_ID;
			
			 // Personnel address in bottom panel
			layout = (CardLayout)m_bottomPanel.getLayout();
			layout.show(m_bottomPanel, PERSONNEL_ADDITIONAL_VIEW_ID);
//			m_bottomViewId = PERSONNEL_ADDITIONAL_VIEW_ID;
			
			// View personnel table
			m_overviewTabPane.setSelectedIndex(0);
			
			m_personnelLeftDetailsPanel.setPersonnel(null);
			m_personnelLeftDetailsPanel.updateFieldContents();
			m_personnelLeftDetailsPanel.setTopLabelText("(" + this.getText("New.text") + ")");
			
			m_newPersonnel = true;
			m_newPersonnelButton.setSelected(true);
			
			m_overviewTabPane.setEnabled(false);
			m_personnelOverviewTable.setEnabled(false);
		}
		else
			Utils.showWarning("Begrensning", "Du må først avslutte " + (m_newPersonnel ? "registrering av nytt personell" : 
				(m_newCallOut ? "import av varsel" : "opprettelse av ny enhet")));

	}
	
	/**
	 * Set up new unit creation process
	 */
	private void newUnit()
	{
		// Single new object at a time
		if(!(m_newPersonnel || m_newUnit || m_newCallOut))
		{
			// View unit table
			m_overviewTabPane.setSelectedIndex(1);
			
			// Show message in left and bottom panels
			m_leftMessageLabel.setText(this.getText("SelectUnitType.text"));
			CardLayout layout = (CardLayout)m_leftPanel.getLayout();
			layout.show(m_leftPanel, MESSAGE_VIEW_ID);
			
			m_leftViewId = MESSAGE_VIEW_ID;
			
			m_bottomMessageLabel.setText(this.getText("SelectUnitType.text"));
			layout = (CardLayout)m_bottomPanel.getLayout();
			layout.show(m_bottomPanel, MESSAGE_VIEW_ID);
			
			// Show type dialog
			m_unitTypeDialog.setVisible(true);
			
			m_newUnitButton.setSelected(true);
			
			m_unitOverviewTable.setEnabled(false);
			
			m_newUnit = true;
			
		}
		else
			Utils.showWarning("Begrensning", "Du må først avslutte " + (m_newPersonnel ? "registrering av nytt personell" : 
				(m_newCallOut ? "import av varsel" : "opprettelse av ny enhet")));
	}
	
	/**
	 * Creates call-out and imports personnel
	 */
	private void importCallout()
	{
		// single object at the time
		if(!(m_newPersonnel || m_newUnit || m_newCallOut)) {
			m_newCallOut = true;
			m_overviewTabPane.setSelectedIndex(2);
			m_overviewTabPane.setEnabled(false);
			m_importCalloutButton.setSelected(true);
			m_importCalloutDialog.setLocationRelativeTo(m_contentsPanel,
					DiskoDialog.POS_CENTER, true, true);	
			m_importCalloutDialog.setVisible(true);
		}
		else {
			Utils.showWarning("Begrensning","Du først avslutte " + (m_newPersonnel ? "registrering av nytt personell" : 
				(m_newCallOut ? "impoer av varsel" : "opprettelse av ny enhet")));
		}
	}
	
	/**
	 * Called when delete is pressed, determines what to delete based on the contents of the details panel
	 */
	private void delete()
	{
		if(m_leftViewId == PERSONNEL_DETAILS_VIEW_ID)
		{
			// Delete currently selected personnel
			IPersonnelIf personnel = m_personnelLeftDetailsPanel.getPersonnel();
			if(personnel != null)
			{
				//  Confirm delete
				String[] options = {this.getText("Delete.text"), this.getText("Cancel.text")};
				int n = JOptionPane.showOptionDialog(
						this.getApplication().getFrame(),
						this.getText("DeletePersonnel.text"),
						this.getText("DeletePersonnel.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);

				if(n == JOptionPane.YES_OPTION)
				{
					try
					{
						PersonnelUtilities.deletePersonnel(personnel);
						
						// Commit
						this.getMsoModel().commit();
					} 
					catch (IllegalOperationException e)
					{
						//  Can not delete personnel, give error message
						ErrorDialog error = new ErrorDialog(this.getApplication().getFrame());
						error.showError(this.getText("CanNotDeletePersonnel.header"),
								this.getText("CanNotDeletePersonnel.details"));
					}					
				}
			}
		}
		else if(m_leftViewId == UNIT_VIEW_ID)
		{
			// Delete currently selected unit
			IUnitIf unit = m_unitDetailsLeftPanel.getUnit();
			if(unit != null)
			{
				//  Confirm delete
				String[] options = {this.getText("Delete.text"), this.getText("Cancel.text")};
				int n = JOptionPane.showOptionDialog(
						this.getApplication().getFrame(),
						this.getText("DeleteUnit.text"),
						this.getText("DeleteUnit.header"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						options,
						options[0]);

				if(n == JOptionPane.YES_OPTION)
				{
					try
					{
						UnitUtilities.deleteUnit(unit, this);
						
						m_msoModel.commit();
					}
					catch(IllegalOperationException e)
					{
						ErrorDialog error = new ErrorDialog(this.getApplication().getFrame());
						error.showError(this.getText("CanNotDeleteUnit.header"),
								this.getText("CanNotDeleteUnit.details"));
					}
				}
			}
		}
	}
	
	/**
	 * Sets personnel in detail view, table needs to repaint
	 * @param personnel
	 */
	public void setPersonnelLeft(IPersonnelIf personnel)
	{
		if(m_newPersonnel)
		{
			return;
		}
		
		m_personnelLeftDetailsPanel.setPersonnel(personnel);
		m_personnelLeftDetailsPanel.updateFieldContents();
		m_personnelAddressBottomPanel.setPersonnel(personnel);
		m_personnelAddressBottomPanel.updateFieldContents();
		
		m_personnelOverviewTable.repaint();
	}
	
	/**
	 * Sets personnel in bottom panel
	 * @param personnel
	 */
	public void setPersonnelBottom(IPersonnelIf personnel)
	{
		if(m_newPersonnel)
		{
			return;
		}
		
		m_personnelBottomDetailsPanel.setPersonnel(personnel);
		m_personnelBottomDetailsPanel.updateFieldContents();
		m_personnelAddressBottomPanel.setPersonnel(personnel);
		m_personnelAddressBottomPanel.updateFieldContents();
	}
	
	/**
	 * Sets unit in detail view, table needs to repaint
	 * @param unit
	 */
	public void setUnit(IUnitIf unit)
	{
		m_unitDetailsLeftPanel.setUnit(unit);
		m_unitDetailsLeftPanel.updateFieldContents();
		
		m_unitOverviewTable.repaint();
	}
	
	/**
	 * Sets whether personnel, unit or call out panels should be displayed in the left panel
	 * 
	 * @param viewId
	 */
	public void setLeftView(String viewId)
	{
		m_leftViewId = viewId;
		CardLayout layout = (CardLayout)m_leftPanel.getLayout();
		layout.show(m_leftPanel, viewId);
	}
	
	/**
	 * Select which of the overview tabs that should be shown
	 * 0 - Personnel
	 * 1 - Unit
	 * 2 - Call-out
	 */
	public void setOverviewPanel(int index)
	{
		m_overviewTabPane.setSelectedIndex(index);
	}
	
	/**
	 * Sets the bottom view panel
	 * @param viewId
	 */
	public void setBottomView(String viewId)
	{
//		m_bottomViewId = viewId;
		CardLayout layout = (CardLayout)m_bottomPanel.getLayout();
		layout.show(m_bottomPanel, viewId);
	}
	
	/**
	 * 
	 * @return Personnel that is being edited, if any, otherwise null
	 */
	public IPersonnelIf getEditingPersonnel()
	{
		return m_leftViewId == PERSONNEL_DETAILS_VIEW_ID ? m_personnelLeftDetailsPanel.getPersonnel() : null;
	}
	
	/**
	 * @return Unit being edited, null if none
	 */
	public IUnitIf getEditingUnit()
	{
		return m_leftViewId == UNIT_VIEW_ID ? m_unitDetailsLeftPanel.getUnit() : null;
	}
	
	/**
	 * Updates personnel details panel based on user selection
	 * 
	 * @author thomasl
	 */
	private class PersonnelTableMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{	
			Point clickedPoint = new Point(e.getX(), e.getY());
			int clickedColumn = m_personnelOverviewTable.columnAtPoint(clickedPoint);
			
			int clickedRow = m_personnelOverviewTable.rowAtPoint(clickedPoint);
			PersonnelOverviewTableModel model = (PersonnelOverviewTableModel)m_personnelOverviewTable.getModel();
			IPersonnelIf clickedPersonnel = model.getPersonnel(clickedRow);
			
			int clickCount = e.getClickCount();
			
			switch(clickedColumn)
			{
			case 0:
				// Display selected personnel
				if(clickCount >= 2)
				{
					// Show personnel in detail panel
					setPersonnelLeft(clickedPersonnel);
					setLeftView(PERSONNEL_DETAILS_VIEW_ID);
					setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);
				}
				else if(clickCount == 1)
				{
					
					if(m_leftViewId.equals(PERSONNEL_DETAILS_VIEW_ID))
					{
						// Show personnel details only if personnel panel is showing on single click
						setPersonnelLeft(clickedPersonnel);
						setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);
						setPersonnelBottom(clickedPersonnel);
					}
					else if(m_leftViewId.equals(UNIT_VIEW_ID))
					{
						// Show personnel details in bottom panel if unit details are displayed in the left panel
						setBottomView(PERSONNEL_DETAILS_VIEW_ID);
						setPersonnelBottom(clickedPersonnel);
					}
				}
				break;
			case 1:
				break;
			case 2:
				break;
			}
		}

		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
	}
	
	/**
	 * Updates unit details panel based on user selection
	 * 
	 * @author thomasl
	 */
	private class UnitTableMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			if(m_newUnit)
			{
				return;
			}
			
			Point clickedPoint = new Point(e.getX(), e.getY());
			int clickedColumn = m_unitOverviewTable.columnAtPoint(clickedPoint);
			int clickedRow = m_unitOverviewTable.rowAtPoint(clickedPoint);
			UnitOverviewTableModel model = (UnitOverviewTableModel)m_unitOverviewTable.getModel();
			IUnitIf clickedUnit = model.getUnit(clickedRow);
			
			switch(clickedColumn)
			{
			case 0:
				int clickCount = e.getClickCount();
				if(clickCount >= 2)
				{
					// Show personnel in left detail panel
					setUnit(clickedUnit);
					setLeftView(UNIT_VIEW_ID);
					
					m_bottomMessageLabel.setText(getText("SelectUnitPersonnel.text"));
				}
				else if(clickCount == 1)
				{
					// Show personnel details only if personnel panel is showing
					if(m_leftViewId.equals(UNIT_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
					{
						setUnit(clickedUnit);
						setLeftView(UNIT_VIEW_ID);
						
						m_bottomMessageLabel.setText(getText("SelectUnitPersonnel.text"));
					}
				}
				break;
			case 1:
				break;
			case 2:
				break;
			}
		}
		
		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
	}
	
	/**
	 * Updates callout details panel based on user selection
	 * 
	 * @author thomasl
	 */
	private class CalloutTableMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e)
		{
			Point clickedPoint = new Point(e.getX(), e.getY());
			int row = m_calloutOverviewTable.rowAtPoint(clickedPoint);
			int index = m_calloutOverviewTable.convertRowIndexToModel(row);
			CalloutOverviewTableModel model = (CalloutOverviewTableModel)m_calloutOverviewTable.getModel();
			ICalloutIf callout = model.getCallout(index);
			
			int clickCount = e.getClickCount();
			if(clickCount >= 2)
			{
				// Show personnel in detail panel
				CardLayout layout = (CardLayout)m_leftPanel.getLayout();
				layout.show(m_leftPanel, CALLOUT_VIEW_ID);
				m_leftViewId = CALLOUT_VIEW_ID;
				
				m_calloutDetailsPanel.setCallOut(callout);
				m_calloutDetailsPanel.updateFieldContents();
			}
			else if(clickCount == 1)
			{
				// Show personnel details only if personnel panel is showing
				if(m_leftViewId.equals(CALLOUT_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
				{
					CardLayout layout = (CardLayout)m_leftPanel.getLayout();
					layout.show(m_leftPanel, CALLOUT_VIEW_ID);
					m_leftViewId = CALLOUT_VIEW_ID;
					
					m_calloutDetailsPanel.setCallOut(callout);
					m_calloutDetailsPanel.updateFieldContents();
				}
			}
		}

		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){}
		public void mouseReleased(MouseEvent e){}
	}
	
	/*
	 * Getters and setters
	 */
	public boolean getNewPersonnel()
	{
		return m_newPersonnel;
	}
	
	public void setNewPersonnel(boolean newPersonnel)
	{
		m_newPersonnel = newPersonnel;
	}
	
	public boolean getNewUnit()
	{
		return m_newUnit;
	}
	
	public void setNewUnit(boolean newUnit)
	{
		m_newUnit = newUnit;
	}
	
	public boolean getNewCallOut()
	{
		return m_newCallOut;
	}
	
	public void setNewCallOut(boolean newCallOut)
	{
		m_newCallOut = newCallOut;
	}

	/**
	 * Cancel unit creation
	 */
	public void onWorkCancel(DiskoWorkEvent e)
	{
		if(e.getSource() instanceof ImportCalloutDialog)
		{
			// Import call-out canceled
			m_importCalloutDialog.setVisible(false);
			m_newCallOut = false;
			m_importCalloutButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			this.getMsoModel().rollback();
		}
		else
		{
			// Unit type dialog canceled
			m_newUnitButton.setSelected(false);
			m_unitTypeDialog.setVisible(false);
		}
		
	}
	
	/**
	 *  Handles unit type selection when creating new unit
	 */
	public void onWorkFinish(DiskoWorkEvent e)
	{
		
		if(e.getSource() instanceof ImportCalloutDialog)
		{
			m_importCalloutDialog.setVisible(false);
			m_newCallOut = false;
			m_importCalloutButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			this.getMsoModel().commit();
		}
		else if(e.getSource() instanceof UnitTypeDialog)
		{
			// Continue unit creation
			IUnitIf newUnit = null;
			UnitType type = m_unitTypeDialog.getUnitType();
			switch(type)
			{
			case BOAT:
				newUnit = m_msoModel.getMsoManager().createBoat("");
				break;
			case VEHICLE:
				newUnit = m_msoModel.getMsoManager().createVehicle("");
				break;
			case DOG:
				newUnit = m_msoModel.getMsoManager().createDog("");
				break;
			case AIRCRAFT:
				newUnit = m_msoModel.getMsoManager().createAircraft("");
				break;
			case TEAM:
				newUnit = m_msoModel.getMsoManager().createTeam("");
				break;
			}
			
			if(newUnit != null)
			{
				m_newUnit = true;
				
				newUnit.setStatus(UnitStatus.EMPTY);
				
				m_unitDetailsLeftPanel.setUnit(newUnit);
				m_unitDetailsLeftPanel.updateFieldContents();
				CardLayout layout = (CardLayout)m_leftPanel.getLayout();
				layout.show(m_leftPanel, UNIT_VIEW_ID);
				
				m_bottomMessageLabel.setText(this.getText("AddPersonnel.text"));
				layout = (CardLayout)m_bottomPanel.getLayout();
				layout.show(m_bottomPanel, MESSAGE_VIEW_ID);
			}
			
			m_unitTypeDialog.setVisible(false);
		}
	}

	public void onWorkChange(DiskoWorkEvent e){
		// Not in use!
	}

	public void afterOperationChange()
	{
		// TODO Auto-generated method stub
		
	}
}
