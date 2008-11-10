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

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.event.DiskoMouseAdapter;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 * Implementation of the Unit work process
 *
 * @author thomasl
 */
public class DiskoWpUnitImpl extends AbstractDiskoWpModule implements IDiskoWpUnit, IWorkFlowListener
{
	private JPanel m_contentsPanel;

	private PersonnelTransferHandler m_personnelTransferHandler;

	private static JTabbedPane m_overviewTabPane;
	private static JTable m_personnelOverviewTable;
	private static JTable m_unitOverviewTable;
	private static JTable m_calloutOverviewTable;

	private static JPanel m_leftPanel;
	private static PersonnelDetailsLeftPanel m_personnelDetailsLeftPanel;
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
	private static String m_bottomViewId = PERSONNEL_DETAILS_VIEW_ID;

	UnitTypeDialog m_unitTypeDialog;

	ImportCalloutDialog m_importCalloutDialog;

	private static boolean m_newPersonnel = false;
	private static boolean m_newUnit = false;
	private static boolean m_newCallOut = false;

	public DiskoWpUnitImpl() throws IllegalClassFormatException
	{
		super();

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

	}

	private void initialize()
	{
		// Properties
		assignWpBundle(IDiskoWpUnit.class);

		// Main panels
		m_contentsPanel = new JPanel(new BorderLayout());
		layoutComponent(m_contentsPanel);

		// Left panels
		m_leftPanel = new JPanel(new CardLayout());
		m_leftPanel.setBorder(null);
		m_leftPanel.setMinimumSize(new Dimension(300, 550));
		m_leftPanel.setPreferredSize(new Dimension(390, 550));
		m_personnelDetailsLeftPanel = new PersonnelDetailsLeftPanel(this);
		m_leftPanel.add(m_personnelDetailsLeftPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_unitDetailsLeftPanel = new UnitDetailsPanel(this);
		m_leftPanel.add(m_unitDetailsLeftPanel, UNIT_VIEW_ID);
		m_calloutDetailsPanel = new CalloutDetailsPanel(this);
		m_leftPanel.add(m_calloutDetailsPanel, CALLOUT_VIEW_ID);
		JPanel leftMessagePanel = new JPanel(new BorderLayout(0,0));
		m_leftMessageLabel = new JLabel();
		m_leftMessageLabel.setVerticalAlignment(SwingUtilities.CENTER);
		m_leftMessageLabel.setHorizontalAlignment(SwingUtilities.CENTER);
		leftMessagePanel.add(m_leftMessageLabel,BorderLayout.CENTER);
		m_leftPanel.add(leftMessagePanel, MESSAGE_VIEW_ID);


		// Bottom panels
		Dimension bottomPanelDimension = new Dimension(100, 150);
		m_bottomPanel = new JPanel(new CardLayout());
		m_bottomPanel.setBorder(null);
		m_personnelBottomDetailsPanel = new PersonnelDetailsBottomPanel(this);
		m_personnelBottomDetailsPanel.setPreferredSize(bottomPanelDimension);
		m_bottomPanel.add(m_personnelBottomDetailsPanel, PERSONNEL_DETAILS_VIEW_ID);
		m_personnelAddressBottomPanel = new PersonnelAddressBottomPanel();
		m_bottomPanel.add(m_personnelAddressBottomPanel, PERSONNEL_ADDITIONAL_VIEW_ID);
		JPanel bottomMessagePanel = new JPanel(new BorderLayout(0,0));
		m_bottomMessageLabel = new JLabel();
		m_bottomMessageLabel.setVerticalAlignment(SwingUtilities.CENTER);
		m_bottomMessageLabel.setHorizontalAlignment(SwingUtilities.CENTER);
		bottomMessagePanel.add(m_bottomMessageLabel,BorderLayout.CENTER);
		m_bottomPanel.add(bottomMessagePanel, MESSAGE_VIEW_ID);

		// Overview panels
		m_overviewTabPane = new JTabbedPane();

		// Set up splitters
		JSplitPane horSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		horSplit.setBorder(BorderFactory.createEmptyBorder());
		horSplit.setDividerLocation(0.4);
		horSplit.setLeftComponent(m_leftPanel);
		horSplit.setRightComponent(m_overviewTabPane);
		JSplitPane vertSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		vertSplit.setLeftComponent(horSplit);
		vertSplit.setRightComponent(m_bottomPanel);
		vertSplit.setResizeWeight(1.0);
		m_contentsPanel.add(vertSplit, BorderLayout.CENTER);

		// create dialogs
		m_unitTypeDialog = new UnitTypeDialog(this, m_overviewTabPane);
		m_importCalloutDialog = new ImportCalloutDialog(this);

		// initialize
		setLeftMessage(getBundleText("SelectPersonnel.text"));
		setBottomMessage(getBundleText("SelectPersonnel.text"));
		setLeftView(MESSAGE_VIEW_ID);
		setBottomView(MESSAGE_VIEW_ID);

	}

	private void initTables()
	{
		/* -----------------------------------------
		 * Create Personnel list table
		 * ----------------------------------------- */
		PersonnelTableModel personnelModel = new PersonnelTableModel(getMsoModel());
		personnelModel.setColumnAlignment(2, SwingConstants.CENTER);
		personnelModel.setColumnAlignment(3, SwingConstants.CENTER);
		m_personnelOverviewTable = new DiskoTable(personnelModel);
		m_personnelOverviewTable.setColumnSelectionAllowed(false);
		m_personnelOverviewTable.setRowSelectionAllowed(true);
		m_personnelOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_personnelOverviewTable.addMouseListener(new PersonnelTableMouseListener());
		m_personnelOverviewTable.setTransferHandler(m_personnelTransferHandler);
		m_personnelOverviewTable.setDragEnabled(true);

		m_personnelOverviewTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()) return;

				if(m_leftViewId.equals(UNIT_VIEW_ID))
				{
					// get selected row
					int row = m_personnelOverviewTable.getSelectedRow();
					if(row!=-1) {
						setPersonnelBottom((IPersonnelIf)m_personnelOverviewTable.getValueAt(row,3));
					}
					setBottomView(PERSONNEL_DETAILS_VIEW_ID);
				}
			}

		});

		TableRowSorter<PersonnelTableModel> tableRowSorter =
				new TableRowSorter<PersonnelTableModel>(personnelModel);
		m_personnelOverviewTable.setRowSorter(tableRowSorter);;
		tableRowSorter.setMaxSortKeys(1);
		tableRowSorter.setSortsOnUpdates(true);
		tableRowSorter.setSortable(2, false);
		tableRowSorter.setSortable(3, false);

		PersonnelTableEditor personnelRenderer = new PersonnelTableEditor(this);
		personnelRenderer.setTable(m_personnelOverviewTable);

		Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);

		m_personnelOverviewTable.setRowHeight(dim.height + 10);
		TableColumn column = m_personnelOverviewTable.getColumnModel().getColumn(2);
		column.setMaxWidth(dim.width + 10);
		column = m_personnelOverviewTable.getColumnModel().getColumn(3);
		column.setPreferredWidth(dim.width * 3 + 20);
		column.setMaxWidth(dim.width * 3 + 20);

		JTableHeader header = m_personnelOverviewTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

		JScrollPane scrollPane = UIFactory.createScrollPane(m_personnelOverviewTable,true);
		m_overviewTabPane.addTab(getBundleText("Personnel.text"),
				DiskoIconFactory.getIcon("GENERAL.PERSONNELLIST", "32x32"), scrollPane);

		/* -----------------------------------------
		 * Create Unit list table
		 * ----------------------------------------- */
		UnitTableModel unitModel = new UnitTableModel(getMsoModel());
		m_unitOverviewTable = new DiskoTable(unitModel);
		m_unitOverviewTable.setColumnSelectionAllowed(false);
		m_unitOverviewTable.setRowSelectionAllowed(true);
		m_unitOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_unitOverviewTable.addMouseListener(new UnitTableMouseListener());
		unitModel.setColumnAlignment(1, SwingConstants.CENTER);
		unitModel.setColumnAlignment(2, SwingConstants.CENTER);

		UnitTableEditor unitRenderer = new UnitTableEditor(this);
		unitRenderer.setTable(m_unitOverviewTable);

		m_unitOverviewTable.setRowHeight(dim.height + 10);
		column = m_unitOverviewTable.getColumnModel().getColumn(1);
		column.setPreferredWidth(dim.width + 10);
		column.setMaxWidth(dim.width + 10);
		column = m_unitOverviewTable.getColumnModel().getColumn(2);
		column.setPreferredWidth(dim.width * 3 + 20);
		column.setMaxWidth(dim.width * 3 + 20);

		header = m_unitOverviewTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

		scrollPane = UIFactory.createScrollPane(m_unitOverviewTable,true);
		m_overviewTabPane.addTab(getBundleText("Unit.text"),
				DiskoIconFactory.getIcon("GENERAL.UNITLIST", "32x32"), scrollPane);

		/* -----------------------------------------
		 * Create Callout list table
		 * ----------------------------------------- */
		CalloutTableModel calloutModel = new CalloutTableModel(getMsoModel());
		m_calloutOverviewTable = new DiskoTable(calloutModel);
		m_calloutOverviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_calloutOverviewTable.addMouseListener(new CalloutTableMouseListener());

		m_calloutOverviewTable.setRowHeight(dim.height + 10);
		column = m_calloutOverviewTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(80);
		column.setMaxWidth(80);

		header = m_calloutOverviewTable.getTableHeader();
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);

		scrollPane = UIFactory.createScrollPane(m_calloutOverviewTable,true);
		m_overviewTabPane.addTab(getBundleText("CallOut.text"),
				DiskoIconFactory.getIcon("GENERAL.CALLOUTLIST", "32x32"), scrollPane);
	}

	private void initButtons()
	{
		String text = getBundleText("NewPersonnelButton.text");
		Icon icon = DiskoIconFactory.createImageIcon("NEW_PERSONNEL", getBundleText("NewPersonnelButton.icon"));
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
		icon = DiskoIconFactory.createImageIcon("NEW_UNIT", getBundleText("NewUnitButton.icon"));
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
		icon = DiskoIconFactory.createImageIcon("IMPORT_CALLOUT", getBundleText("ImportCalloutButton.icon"));
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

		// setup of navbar needed?
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

	/**
	 * Cancel any creation process
	 */
	public boolean rollback()
	{

		getMsoModel().suspendClientUpdate();

		if(m_newPersonnel)
		{
			m_newPersonnel = false;
			m_newPersonnelButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			m_personnelOverviewTable.setEnabled(true);
			showSelectedPersonnel();
		}

		if(m_newUnit)
		{
			m_newUnit = false;
			m_newUnitButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			m_unitOverviewTable.setEnabled(true);
			showSelectedUnit();
		}

		if(m_newCallOut)
		{
			m_newCallOut = false;
			m_importCalloutButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			m_calloutOverviewTable.setEnabled(true);
			showSelectedCallOut();
		}

		// forward
		getMsoModel().resumeClientUpdate(true);

		// forward
		getMsoModel().rollback();

		// finished
		return  super.rollback();

	}

	public boolean commit()
	{

		getMsoModel().suspendClientUpdate();

		// create a new personnel?
		if(m_newPersonnel)
		{
			// create data
			IPersonnelIf personnel = this.getMsoManager().createPersonnel();
			m_personnelDetailsLeftPanel.setPersonnel(personnel);
			m_newPersonnel = false;
			m_newPersonnelButton.setSelected(false);
			m_overviewTabPane.setEnabled(true);
			m_personnelOverviewTable.setEnabled(true);
		}

		// try to save personnel
		if(!m_personnelDetailsLeftPanel.savePersonnel()) {
			// is new personnel?
			if(m_newPersonnel) {
				// delete object!
				m_personnelDetailsLeftPanel.getPersonnel().delete();
			}
			// failed?
			if(m_leftViewId == PERSONNEL_DETAILS_VIEW_ID) {
				// resume update
				this.getMsoModel().resumeClientUpdate(true);
				// failed!
				return false;
			}
		}

		// save address information
		m_personnelAddressBottomPanel.savePersonnel();

		// Check for new unit
		m_unitDetailsLeftPanel.saveUnit();

		if(m_newUnit)
		{
			m_unitOverviewTable.setEnabled(true);
			m_newUnitButton.setSelected(false);
			m_newUnit = false;
		}

		// Check for new call-out
		if(m_newCallOut)
		{
			m_newCallOut = false;
			m_overviewTabPane.setEnabled(true);
			m_calloutOverviewTable.setEnabled(true);
			m_importCalloutButton.setSelected(false);
		}
		m_calloutDetailsPanel.saveCallOut();

		// forward
		getMsoModel().commit();

		// notify
		getMsoModel().resumeClientUpdate(true);

		// finished
		return super.commit();

	}

	private void showSelectedPersonnel() {
		int row = m_personnelOverviewTable.getSelectedRow();
		if(row!=-1) {
			// get selected personnel
			IPersonnelIf personnel = (IPersonnelIf)m_personnelOverviewTable.getValueAt(row, 2);
			// Show personnel in detail panel
			setPersonnelLeft(personnel);
			setPersonnelBottom(personnel);
			setLeftView(PERSONNEL_DETAILS_VIEW_ID);
			setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);
		}
		else {
			// update messages
			setLeftMessage(getBundleText("SelectPersonnel.text"));
			setBottomMessage(getBundleText("SelectPersonnel.text"));
			// Show unit in left detail panel
			setLeftView(MESSAGE_VIEW_ID);
			setBottomView(MESSAGE_VIEW_ID);
		}
	}

	private void showSelectedUnit() {
		int row = m_unitOverviewTable.getSelectedRow();
		if(row!=-1) {
			// get selected unit
			IUnitIf unit = (IUnitIf)m_unitOverviewTable.getValueAt(row, 1);
			// update bottom message
			setBottomMessage(getBundleText("SelectUnitPersonnel.text"));
			// Show unit in left detail panel
			setUnit(unit);
			setLeftView(UNIT_VIEW_ID);
			setBottomView(MESSAGE_VIEW_ID);
		}
		else {
			// update messages
			setLeftMessage(getBundleText("SelectUnit.text"));
			setBottomMessage(getBundleText("SelectUnit.text"));
			// Show unit in left detail panel
			setLeftView(MESSAGE_VIEW_ID);
			setBottomView(MESSAGE_VIEW_ID);
		}
		m_unitDetailsLeftPanel.setUnit(null);
	}

	private void showSelectedCallOut() {
		int row = m_calloutOverviewTable.getSelectedRow();
		if(row!=-1) {
			// get selected callout
			ICalloutIf callout = (ICalloutIf)m_calloutOverviewTable.getValueAt(row, 2);
			// update bottom message
			setBottomMessage(getBundleText("SelectCallOutPersonnel.text"));
			// show callout view
			m_calloutDetailsPanel.setCallOut(callout);
			m_calloutDetailsPanel.updateFieldContents();
			setLeftView(CALLOUT_VIEW_ID);
			setBottomView(MESSAGE_VIEW_ID);
		}
		else {
			// update messages
			setLeftMessage(getBundleText("SelectCallOut.text"));
			setBottomMessage(getBundleText("SelectCallOut.text"));
			// Show unit in left detail panel
			setLeftView(MESSAGE_VIEW_ID);
			setBottomView(MESSAGE_VIEW_ID);
		}
	}

	/**
	 * Set up new personnel creation process
	 */
	private void newPersonnel()
	{
		// Single new object at a time
		if(!(m_newPersonnel || m_newUnit || m_newCallOut))
		{

			// View personnel table
			m_overviewTabPane.setSelectedIndex(0);

			// show personnel views
			setLeftView(PERSONNEL_DETAILS_VIEW_ID);
			setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);

			m_personnelDetailsLeftPanel.setPersonnel(null);
			m_personnelDetailsLeftPanel.updateFieldContents();
			m_personnelDetailsLeftPanel.setTopLabelText("(" + this.getBundleText("New.text") + ")");

			// set flag
			m_newPersonnel = true;

			// prepare components
			m_newPersonnelButton.setSelected(true);
			m_overviewTabPane.setEnabled(false);
			m_personnelOverviewTable.setEnabled(false);

			// notify
			fireOnWorkChange(m_leftViewId);
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

			// prepare components
			m_newUnitButton.setSelected(true);
			m_unitOverviewTable.setEnabled(false);

			// set flag
			m_newUnit = true;

			// notify
			fireOnWorkChange(m_leftViewId);

			// Show type dialog (is modal, will lock)
			m_unitTypeDialog.setVisible(true);

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

			// View unit table
			m_overviewTabPane.setSelectedIndex(2);

			// set flag
			m_newCallOut = true;

			// prepare components
			m_overviewTabPane.setEnabled(false);
			m_calloutOverviewTable.setEnabled(false);
			m_importCalloutButton.setSelected(true);
			m_importCalloutDialog.setLocationRelativeTo(
					m_contentsPanel,DefaultDialog.POS_CENTER, true, true);

			// notify
			fireOnWorkChange(m_leftViewId);

			// forward
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
			IPersonnelIf personnel = m_personnelDetailsLeftPanel.getPersonnel();
			if(personnel != null)
			{
				//  Confirm delete
				String[] options = {this.getBundleText("Delete.text"), this.getBundleText("Cancel.text")};
				int n = JOptionPane.showOptionDialog(
						this.getApplication().getFrame(),
						this.getBundleText("DeletePersonnel.text"),
						this.getBundleText("DeletePersonnel.header"),
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
						Utils.showError(this.getBundleText("CanNotDeletePersonnel.header"),
								this.getBundleText("CanNotDeletePersonnel.details"));
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
				String[] options = {this.getBundleText("Delete.text"), this.getBundleText("Cancel.text")};
				int n = JOptionPane.showOptionDialog(
						this.getApplication().getFrame(),
						this.getBundleText("DeleteUnit.text"),
						this.getBundleText("DeleteUnit.header"),
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

						getMsoModel().commit();
					}
					catch(IllegalOperationException e)
					{
						Utils.showError(this.getBundleText("CanNotDeleteUnit.header"),
								this.getBundleText("CanNotDeleteUnit.details"));
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

		m_personnelDetailsLeftPanel.setPersonnel(personnel);
		m_personnelDetailsLeftPanel.updateFieldContents();
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
		m_unitDetailsLeftPanel.updateContents();

		m_unitOverviewTable.repaint();
	}

	public void setLeftMessage(String msg)
	{
		m_leftMessageLabel.setText(msg);
	}

	public void setBottomMessage(String msg)
	{
		m_bottomMessageLabel.setText(msg);
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
		m_bottomViewId = viewId;
		CardLayout layout = (CardLayout)m_bottomPanel.getLayout();
		layout.show(m_bottomPanel, viewId);
	}

	/**
	 *
	 * @return Personnel that is being edited, if any, otherwise null
	 */
	public IPersonnelIf getEditingPersonnel()
	{
		return m_leftViewId == PERSONNEL_DETAILS_VIEW_ID ? m_personnelDetailsLeftPanel.getPersonnel() : null;
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
			int clickedColumn = m_personnelOverviewTable.columnAtPoint(clickedPoint);
			int clickedRow = m_personnelOverviewTable.rowAtPoint(clickedPoint);
			PersonnelTableModel model = (PersonnelTableModel)m_personnelOverviewTable.getModel();
			IPersonnelIf clickedPersonnel = model.getPersonnel(clickedRow);

			if(clickedColumn == 0)
			{
				if(m_leftViewId.equals(PERSONNEL_DETAILS_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
				{
					// Show personnel details only if personnel panel is showing on single click
					setPersonnelLeft(clickedPersonnel);
					setPersonnelBottom(clickedPersonnel);
					setLeftView(PERSONNEL_DETAILS_VIEW_ID);
					setBottomView(PERSONNEL_ADDITIONAL_VIEW_ID);
				}
				else if(m_leftViewId.equals(UNIT_VIEW_ID))
				{
					// Show personnel details in bottom panel if unit details are displayed in the left panel
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

			if(m_newUnit)
			{
				return;
			}

			Point clickedPoint = new Point(e.getX(), e.getY());
			int clickedColumn = m_unitOverviewTable.columnAtPoint(clickedPoint);
			int clickedRow = m_unitOverviewTable.rowAtPoint(clickedPoint);
			UnitTableModel model = (UnitTableModel)m_unitOverviewTable.getModel();
			IUnitIf clickedUnit = model.getUnit(clickedRow);

			if(clickedColumn == 0)
			{
				// update bottom message
				setBottomMessage(getBundleText("SelectUnitPersonnel.text"));
				// Show unit in left detail panel
				setUnit(clickedUnit);
				setLeftView(UNIT_VIEW_ID);
				setBottomView(MESSAGE_VIEW_ID);
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
			if(m_leftViewId.equals(CALLOUT_VIEW_ID) || m_leftViewId.equals(MESSAGE_VIEW_ID))
			{

				// get callout
				Point clickedPoint = new Point(e.getX(), e.getY());
				int row = m_calloutOverviewTable.rowAtPoint(clickedPoint);
				int index = m_calloutOverviewTable.convertRowIndexToModel(row);
				if(index==-1) return;
				CalloutTableModel model = (CalloutTableModel)m_calloutOverviewTable.getModel();
				ICalloutIf callout = model.getCallout(index);

				// update bottom message
				setBottomMessage(getBundleText("SelectCallOutPersonnel.text"));

				// show callout view
				m_calloutDetailsPanel.setCallOut(callout);
				m_calloutDetailsPanel.updateFieldContents();
				setLeftView(CALLOUT_VIEW_ID);
				setBottomView(MESSAGE_VIEW_ID);

			}

		}
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

	public void onFlowPerformed(WorkFlowEvent e){

		if(e.isFinish()) {

			if(m_newCallOut)
			{

				m_newCallOut = false;

				m_importCalloutButton.setSelected(false);
				m_overviewTabPane.setEnabled(true);

				commit();

			}
			else if(m_newUnit)
			{
				// Continue unit creation
				IUnitIf newUnit = null;
				UnitType type = m_unitTypeDialog.getUnitType();
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
					m_newUnit = true;

					m_unitDetailsLeftPanel.setUnit(newUnit);
					m_unitDetailsLeftPanel.updateContents();
					setLeftView(UNIT_VIEW_ID);

					setBottomMessage(getBundleText("AddPersonnel.text"));
					setBottomView(MESSAGE_VIEW_ID);
				}

				m_unitTypeDialog.setVisible(false);
			}
		}
		else if(e.isCancel()) {

			if(m_newCallOut)
			{
				// Import call-out canceled
				m_importCalloutDialog.setVisible(false);
				m_newCallOut = false;
				m_importCalloutButton.setSelected(false);
				m_overviewTabPane.setEnabled(true);
				// forward
				rollback();
			}
			else if(m_newUnit)
			{
				// Unit type dialog canceled
				m_newUnit = false;
				m_newUnitButton.setSelected(false);
				m_unitTypeDialog.setVisible(false);
				// forward
				rollback();
			}

		}

	}

	public void afterOperationChange()
	{
		if(getMsoModel().getMsoManager().operationExists()) {
			ICmdPostIf cmdPost = getMsoModel().getMsoManager().getCmdPost();
			PersonnelTableModel m1 = (PersonnelTableModel)m_personnelOverviewTable.getModel();
			m1.load(cmdPost.getAttendanceList());
			UnitTableModel m2 = (UnitTableModel)m_unitOverviewTable.getModel();
			m2.load(cmdPost.getUnitList());
			CalloutTableModel m3 = (CalloutTableModel)m_calloutOverviewTable.getModel();
			m3.load(cmdPost.getCalloutList());
		}

	}

	@Override
	public boolean isChanged() {
		if(super.isChanged()) return true;
		return m_newPersonnel || m_newUnit || m_newCallOut;
	}

}
