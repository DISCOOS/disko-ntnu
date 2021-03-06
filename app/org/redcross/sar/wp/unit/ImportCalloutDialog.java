package org.redcross.sar.wp.unit;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.FileExplorerDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.gui.table.FileTable;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelImportStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.work.event.IFlowListener;

/**
 * Dialog handling import of new call-outs
 *
 * @author thomasl
 */
public class ImportCalloutDialog extends DefaultDialog
{
	private static final long serialVersionUID = 1L;

	private static final String IMPORT_ID = "IMPORT";
	private static final String CONFIRM_ID = "CONFIRM";
	private static String m_currentPanel = IMPORT_ID;

	List<PersonnelAuxiliary> m_personnelList;

	private DefaultPanel m_contentsPanel;
	private JPanel m_cardsPanel;

	private JPanel m_importPanel;
	private JTextField m_dtgTextField;
	private JTextField m_titleTextField;
	private JTextField m_organizationTextField;
	private JTextField m_departmentTextField;
	private JTextField m_fileTextField;
	private JButton m_fileDialogButton;

	private JPanel m_confirmPanel;
	private JTable m_personnelTable;
	private ImportPersonnelTableModel m_tableModel;

	private JButton m_backButton;
	private JButton m_nextButton;
	private JButton m_okButton;

	private IDiskoWpUnit m_wpUnit;
	
	private ICalloutIf m_callout;

	public ImportCalloutDialog(IDiskoWpUnit wp, Component parent)
	{
		// forward
		super(wp.getApplication().getFrame());

		// prepare
		m_wpUnit = wp;
		m_personnelList = new LinkedList<PersonnelAuxiliary>();

		// initialize GUI
		initialize();

		// show in center of parent
		setSnapToLocation(parent, DefaultDialog.POS_CENTER, DefaultDialog.SIZE_TO_OFF, true, false);

	}

	private void initialize()
	{
		// prepare dialog
		this.setModal(true);

		// create content panel
		m_contentsPanel = new DefaultPanel(m_wpUnit.getBundleText("ImportCallOut.text") + " 1/2");
		m_contentsPanel.setScrollBarPolicies(BasePanel.VERTICAL_SCROLLBAR_NEVER, BasePanel.HORIZONTAL_SCROLLBAR_NEVER);

		// prepare body panel
		m_contentsPanel.setContainerLayout(new CardLayout());
		m_cardsPanel = (JPanel)m_contentsPanel.getContainer();

		// forward
		initializeImportPanel();
		initializeConfirmPanel();
		initializeButtons();

		// add work listener
		m_contentsPanel.addFlowListener((IFlowListener)m_wpUnit);

		// add action listener
		m_contentsPanel.addActionListener(actionListener);

		// add content panel
		this.setContentPane(m_contentsPanel);
		this.pack();

	}

	private void initializeImportPanel()
	{
		m_importPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(4, 4, 4, 4);

		m_dtgTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, "DTG", m_dtgTextField, gbc, 1);

		m_titleTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_wpUnit.getBundleText("Title.text"), m_titleTextField, gbc, 1);

		m_organizationTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_wpUnit.getBundleText("Organization.text"), m_organizationTextField, gbc, 1);

		m_departmentTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_wpUnit.getBundleText("Department.text"), m_departmentTextField, gbc, 1);

		gbc.gridwidth = 3;
		m_importPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
		gbc.gridy++;

		gbc.gridwidth = 1;
		m_fileTextField = new JTextField();
		layoutComponent(m_importPanel, m_wpUnit.getBundleText("FileName.text"), m_fileTextField, gbc, 0);

		//String text = m_wpUnit.getBundleText("File.text");
		m_fileDialogButton = DiskoButtonFactory.createButton("GENERAL.EDIT",m_contentsPanel.getButtonSize());
		m_fileDialogButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//Bring up file dialog
				FileExplorerDialog fc = new FileExplorerDialog();
				fc.setFilter(FileTable.createExtensionFilter("*.txt;*.cvs;*.dat",";"));
				Object file = fc.select(m_fileTextField.getText(),"Velg fil",null);
				if(file != null)
				{
					m_fileTextField.setText(((File)file).getPath());
				}
			}
		});
		gbc.gridx =  2;
		m_importPanel.add(m_fileDialogButton, gbc);
		gbc.gridy++;

		JTextArea fileDescription = new JTextArea();
		fileDescription.setBackground(m_importPanel.getBackground());
		fileDescription.setEditable(false);
		fileDescription.setWrapStyleWord(true);
		fileDescription.setLineWrap(true);
		fileDescription.setText(m_wpUnit.getBundleText("PersonnelFileDescription.text"));
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		m_importPanel.add(fileDescription, gbc);

		gbc.gridy++;
		m_importPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);

		m_cardsPanel.add(m_importPanel, IMPORT_ID);
	}

	private void initializeConfirmPanel()
	{
		m_confirmPanel = new JPanel();
		m_confirmPanel.setLayout(new BoxLayout(m_confirmPanel, BoxLayout.PAGE_AXIS));

		m_tableModel = new ImportPersonnelTableModel();
		m_personnelTable = new DiskoTable(m_tableModel);

		Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
		m_personnelTable.setRowHeight(dim.height + 10);
		TableColumn column = m_personnelTable.getColumnModel().getColumn(0);
		column.setPreferredWidth(dim.width + 10);
		column.setMaxWidth(dim.width + 10);

		column = m_personnelTable.getColumnModel().getColumn(2);
		column.setPreferredWidth(dim.width * 3 + 20);
		column.setMaxWidth(dim.width * 3 + 20);

		ImportPersonnelCellEditor editor = new ImportPersonnelCellEditor();
		m_personnelTable.setDefaultEditor(Object.class, editor);
		m_personnelTable.setDefaultRenderer(Object.class, editor);

		m_personnelTable.setTableHeader(null);

		JScrollPane tableScrollPane = UIFactory.createScrollPane(m_personnelTable,true);
		m_confirmPanel.add(tableScrollPane);

		m_cardsPanel.add(m_confirmPanel, CONFIRM_ID);
	}

	private void initializeButtons()
	{
		ButtonSize buttonSize = m_contentsPanel.getButtonSize();
		m_backButton = DiskoButtonFactory.createButton("GENERAL.BACK",buttonSize);
		m_backButton.setEnabled(false);
		m_contentsPanel.insertButton("finish", m_backButton, "back");

		m_nextButton = DiskoButtonFactory.createButton("GENERAL.NEXT",buttonSize);
		m_contentsPanel.insertButton("finish", m_nextButton, "next");

		m_okButton = (JButton)m_contentsPanel.getButton("finish");
		m_okButton.setEnabled(false);
	}

	private void layoutComponent(JPanel panel, String label, JComponent component, GridBagConstraints gbc, int height)
	{
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.gridheight = Math.max(1, height);
		panel.add(component, gbc);

		gbc.gridx = 0;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		panel.add(new JLabel(label), gbc);

		gbc.gridy += height;
	}

	public ICalloutIf importCallout() {
		// prepare
		m_callout = null;
		// show (will block)
		setVisible(true);
		// finished;
		return m_callout;
	}
	
	private void clearForm()
	{
		m_personnelList.clear();
		m_titleTextField.setText("");
		m_dtgTextField.setText(DTG.CalToDTG(Calendar.getInstance()));
		m_organizationTextField.setText("");
		m_departmentTextField.setText("");
		m_fileTextField.setText("");
	}

	/**
	 * Imports call-out personnel data from selected file
	 *
	 * @throws IOException
	 */
	private boolean importFile() throws IOException
	{
		boolean bFlag = false;
		String filePath = m_fileTextField.getText();
		File file = new File(filePath);

		if(file.exists()) {

			m_personnelList.clear();
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = null;

			// Parse lines
			while((line = input.readLine()) != null)
			{
				PersonnelAuxiliary personnel = new PersonnelAuxiliary();

				String[] fields = line.split("\\t");

				int i = 0;
				if(!fields[i].equals(""))
				{
					personnel.setId(fields[i]);
				}
				i++;

				if(i<fields.length)
				{
					personnel.setFirstName(fields[i]);
					i++;
				}
				if(i<fields.length)
				{
					personnel.setLastName(fields[i]);
					i++;
				}
				if(i<fields.length)
				{
					personnel.setPhone(fields[i]);
					i++;
				}
				if(i<fields.length)
				{
					personnel.setReportStatus(fields[i]);
				}

				m_personnelList.add(personnel);
			}

			m_personnelTable.tableChanged(null);

			// success
			bFlag = true;

		}
		else {
			Utils.showWarning("Begrensning", "Fil " + filePath + " eksisterer ikke");
		}
		// finished
		return bFlag;
	}

	/**
	 * Checks if personnel about to be imported already exists.
	 * Checks name for the time being
	 */
	private void checkForPreExistingPersonnel()
	{
//		// Build key hash-set
//		HashSet<String> personnelKeys = new HashSet<String>();
//		for(IPersonnelIf personnel : m_wpUnit.getMsoManager().getCmdPost().getAttendanceListItems())
//		{
//			personnelKeys.add(personnel.getFirstname() + " " + personnel.getLastname());
//		}
//
//		// Check personnel
//		String importPersonnelKey = null;
//		for(PersonnelAuxiliary personnel : m_personnelList)
//		{
//			importPersonnelKey = personnel.getFirstName() + " " + personnel.getLastName();
//			personnel.setPreExisting(personnelKeys.contains(importPersonnelKey));
//
//		}

		if(m_wpUnit.getMsoManager().operationExists()) {

			// Brute-force check
			for(PersonnelAuxiliary personnel : m_personnelList)
			{
				for(IPersonnelIf potentialMatch : m_wpUnit.getMsoManager().getCmdPost().getAttendanceListItems())
				{
					personnel.equals(potentialMatch);
				}
			}
		}
	}

	/**
	 * Saves the imported call-out and personnel to MSO
	 */
	@SuppressWarnings("null")
	private void saveCallout()
	{
		// Create call-out
		m_callout = m_wpUnit.getMsoManager().createCallout();

		try
		{
			Calendar t = Calendar.getInstance();
			m_callout.setCreated(DTG.DTGToCal(
					t.get(Calendar.YEAR),t.get(Calendar.MONTH),
					m_dtgTextField.getText()));
		}
		catch (IllegalMsoArgumentException e)
		{
			// Set created to now
			m_callout.setCreated(Calendar.getInstance());
		}
		m_callout.setTitle(m_titleTextField.getText());
		m_callout.setOrganization(m_organizationTextField.getText());
		m_callout.setDepartment(m_departmentTextField.getText());

		// Import personnel
		for(PersonnelAuxiliary personnel : m_personnelList)
		{
			IPersonnelIf msoPersonnel = null;
			if(personnel.isInclude())
			{
				if(personnel.isValid())
				{
					if(personnel.isPreExisting())
					{
						// Update existing personnel
						if(personnel.isCreateNew())
						{
							// Create new personnel instance
							msoPersonnel = m_wpUnit.getMsoManager().createPersonnel();
							msoPersonnel.setDataSourceID(personnel.getId());
							msoPersonnel.setFirstname(personnel.getFirstName());
							msoPersonnel.setLastname(personnel.getLastName());
							msoPersonnel.setTelephone1(personnel.getPhone());
							msoPersonnel.setImportStatus(PersonnelImportStatus.IMPORTED);
							personnel.getPersonnelRef().setNextOccurrence(msoPersonnel);
						}
						else if(personnel.isUpdate())
						{
							// Update existing personnel
							msoPersonnel = personnel.getPersonnelRef();
							msoPersonnel.setDataSourceID(personnel.getId());
							msoPersonnel.setFirstname(personnel.getFirstName());
							msoPersonnel.setLastname(personnel.getLastName());
							msoPersonnel.setTelephone1(personnel.getPhone());
							msoPersonnel.setImportStatus(PersonnelImportStatus.UPDATED);
						}
						else if(personnel.isKeepExisting())
						{
							// Keep personnel
							msoPersonnel = personnel.getPersonnelRef();
							msoPersonnel.setImportStatus(PersonnelImportStatus.KEPT);
						}

						// Reinstate released personnel
						if(msoPersonnel.getStatus() == PersonnelStatus.RELEASED)
						{
							msoPersonnel = ResourceUtils.reinstatePersonnel(msoPersonnel, PersonnelStatus.ON_ROUTE);
						}
						else if(msoPersonnel.getStatus() != PersonnelStatus.ARRIVED)
						{
							msoPersonnel.setStatus(PersonnelStatus.ON_ROUTE);
						}
					}
					else
					{
						// Create new personnel
						msoPersonnel = m_wpUnit.getMsoManager().createPersonnel();
						msoPersonnel.setFirstname(personnel.getFirstName());
						msoPersonnel.setLastname(personnel.getLastName());
						msoPersonnel.setTelephone1(personnel.getPhone());
						msoPersonnel.setStatus(PersonnelStatus.ON_ROUTE);
					}

					m_callout.addPersonel(msoPersonnel);

				}
				else {
					// TODO: implement handling of invalid imports (show list off invalid imports)
				}
			}
		}
	}

	/**
	 * Personnel auxiliary class. Delay MSO update until commit
	 *
	 * @author thomasl
	 */
	private enum PersonnelUpdateType
	{
		UPDATE_EXISTING,
		KEEP_EXISTING,
		CREATE_NEW
	}

	private final ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e)
		{
			String cmd = e.getActionCommand();
			if("back".equals(cmd)) {
				if(m_currentPanel == CONFIRM_ID) {
					m_contentsPanel.setCaptionText(m_wpUnit.getBundleText("ImportCallOut.text") + " 1/2");
					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, IMPORT_ID);
					m_currentPanel = IMPORT_ID;

					m_okButton.setEnabled(false);
					m_backButton.setEnabled(false);
				}
			}
			else if("next".equals(cmd)) {
				if(m_currentPanel == IMPORT_ID)
				{
					try
					{
						if(importFile()) {
							checkForPreExistingPersonnel();
							m_contentsPanel.setCaptionText(m_wpUnit.getBundleText("ImportCallOut.text") + " 2/2");
							CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
							layout.show(m_cardsPanel, CONFIRM_ID);
							m_currentPanel = CONFIRM_ID;
							m_contentsPanel.setDirty(true);
							m_okButton.setEnabled(true);
							m_backButton.setEnabled(true);
						}
					}
					catch (FileNotFoundException ex) {
						Utils.showWarning("Fil " +  m_fileTextField.getText() + " eksisterer ikke");
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				} else {
					m_contentsPanel.setCaptionText(m_wpUnit.getBundleText("ImportCallOut.text") + " 1/2");
					saveCallout();
					CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
					layout.show(m_cardsPanel, IMPORT_ID);
					m_currentPanel = IMPORT_ID;
					clearForm();
					m_contentsPanel.finish();
				}
			} else if("finish".equals(cmd)) {
				m_contentsPanel.setCaptionText(m_wpUnit.getBundleText("ImportCallOut.text") + " 1/2");
				saveCallout();
				CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
				layout.show(m_cardsPanel, IMPORT_ID);
				m_currentPanel = IMPORT_ID;
				clearForm();				
			}
			else if("cancel".equals(cmd)) {
				m_contentsPanel.setCaptionText(m_wpUnit.getBundleText("ImportCallOut.text") + " 1/2");
				CardLayout layout = (CardLayout)m_cardsPanel.getLayout();
				layout.show(m_cardsPanel, IMPORT_ID);
				m_currentPanel = IMPORT_ID;
				m_callout = null;
			}
		}
	};

	private class PersonnelAuxiliary
	{
		private String m_id;
		private String m_firstName;
		private String m_lastName;
		private String m_phone;
		private String m_reportStatus;

		private boolean m_include = true;
		private boolean m_preExisting;
		private PersonnelUpdateType m_updateType = PersonnelUpdateType.UPDATE_EXISTING;

		IPersonnelIf m_personnelRef = null;

		public boolean equals(IPersonnelIf personnel)
		{
			// Check name, for now
			String thisName = m_firstName + " " + m_lastName;
			String thatName = personnel.getFirstName() + " " + personnel.getLastName();
			boolean same = thisName.equals(thatName);

			if(same)
			{
				m_personnelRef = personnel;
				// Set reference to end of personnel history chain
				while(m_personnelRef.getNextOccurrence() != null)
				{
					m_personnelRef = m_personnelRef.getNextOccurrence();
				}
				m_preExisting = true;
			}

			return same;
		}

		public IPersonnelIf getPersonnelRef()
		{
			return m_personnelRef;
		}

		public void setPreExisting(boolean preExisting)
		{
			m_preExisting = preExisting;
		}

		public String getId()
		{
			return m_id;
		}

		public void setId(String id)
		{
			this.m_id = id;
		}

		public String getFirstName()
		{
			return m_firstName;
		}

		public void setFirstName(String name)
		{
			m_firstName = name;
		}

		public String getLastName()
		{
			return m_lastName;
		}

		public void setLastName(String name)
		{
			m_lastName = name;
		}

		public String getPhone()
		{
			return m_phone;
		}

		public void setPhone(String phone)
		{
			this.m_phone = phone;
		}

		public String getReportStatus()
		{
			return m_reportStatus;
		}

		public void setReportStatus(String status)
		{
			m_reportStatus = status;
		}

		public boolean isValid()
		{
			return m_firstName!=null && !m_firstName.isEmpty() &&
				m_lastName!=null && !m_lastName.isEmpty();
		}

		public boolean isInclude()
		{
			return m_include;
		}

		public void setInclude(boolean include)
		{
			m_include = include;
		}

		public boolean isPreExisting()
		{
			return m_preExisting;
		}

		public void setUpdateType(PersonnelUpdateType type)
		{
			this.m_updateType = type;
		}

		public boolean isUpdate()
		{
			return m_updateType == PersonnelUpdateType.UPDATE_EXISTING;
		}

		public boolean isKeepExisting()
		{
			return m_updateType == PersonnelUpdateType.KEEP_EXISTING;
		}

		public boolean isCreateNew()
		{
			return m_updateType == PersonnelUpdateType.CREATE_NEW;
		}
	}

	/**
	 * Table data for personnel about to be imported from
	 *
	 * @author thomasl
	 */
	private class ImportPersonnelTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		public int getColumnCount()
		{
			return 3;
		}

		public int getRowCount()
		{
			return m_personnelList.size();
		}

		public Object getValueAt(int row, int column)
		{
			PersonnelAuxiliary personnel = m_personnelList.get(row);
			switch(column)
			{
			case 0:
				return personnel.isInclude();
			case 1:
				return personnel.getFirstName() + " " + personnel.getLastName();
			case 2:
				Boolean status[] = {personnel.isPreExisting(), personnel.isUpdate(), personnel.isKeepExisting(), personnel.isCreateNew()};
				return status;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex == 2 || columnIndex == 0;
		}

	    @Override
	    public String getColumnName(int column)
	    {
	    	return null;
	    }
	}

	/**
	 * Personnel import table cell editor and renderer
	 *
	 * @author thomasl
	 */
	private class ImportPersonnelCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private int m_editingRow;

		private JPanel m_includePanel;
		private JCheckBox m_includeCheckBox;
		private JLabel m_nameLabel;
		private JPanel m_optionsPanel;
		private JButton m_updateButton;
		private JButton m_keepButton;
		private JButton m_newButton;

		public ImportPersonnelCellEditor()
		{
			m_includePanel = new JPanel();
			m_includePanel.setBackground(m_personnelTable.getBackground());
			m_includeCheckBox = new JCheckBox();
			m_includeCheckBox.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Toggle personnel include
					PersonnelAuxiliary personnel = m_personnelList.get(m_editingRow);
					personnel.setInclude(!personnel.isInclude());
					fireEditingStopped();
				}
			});
			m_includePanel.add(m_includeCheckBox);

			m_nameLabel = new JLabel();

			m_optionsPanel = new JPanel();
			m_optionsPanel.setBackground(m_personnelTable.getBackground());

			String text = m_wpUnit.getBundleText("UpdateButton.text");
			String letter = m_wpUnit.getBundleText("UpdateButton.letter");
			m_updateButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_updateButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Set update type to update existing
					PersonnelAuxiliary personnel = m_personnelList.get(m_editingRow);
					personnel.setUpdateType(PersonnelUpdateType.UPDATE_EXISTING);
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_optionsPanel.add(m_updateButton);

			text = m_wpUnit.getBundleText("KeepButton.text");
			letter = m_wpUnit.getBundleText("KeepButton.letter");
			m_keepButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_keepButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Set update type to keep existing
					PersonnelAuxiliary personnel = m_personnelList.get(m_editingRow);
					personnel.setUpdateType(PersonnelUpdateType.KEEP_EXISTING);
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_optionsPanel.add(m_keepButton);

			text = m_wpUnit.getBundleText("NewButton.text");
			letter = m_wpUnit.getBundleText("NewButton.letter");
			m_newButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_newButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Set update type to create new personnel
					PersonnelAuxiliary personnel = m_personnelList.get(m_editingRow);
					personnel.setUpdateType(PersonnelUpdateType.CREATE_NEW);
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_optionsPanel.add(m_newButton);
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean arg2, int row, int column)
		{
			m_editingRow = row;
			updateCell(row);
			switch(column)
			{
			case 0:
				return m_includePanel;
			case 1:
				return m_nameLabel;
			case 2:
				return m_optionsPanel;
			}
			return null;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			updateCell(row);
			switch(column)
			{
			case 0:
				return m_includePanel;
			case 1:
				return m_nameLabel;
			case 2:
				return m_optionsPanel;
			}
			return null;
		}

		private void updateCell(int row)
		{
			boolean include = (Boolean)m_tableModel.getValueAt(row, 0);
			m_includeCheckBox.setSelected(include);

			String name = (String)m_tableModel.getValueAt(row, 1);
			m_nameLabel.setText(name);

			Boolean status[] = (Boolean[])m_tableModel.getValueAt(row, 2);
			m_updateButton.setVisible(status[0]);
			m_keepButton.setVisible(status[0]);
			m_newButton.setVisible(status[0]);
			m_updateButton.setSelected(status[1]);
			m_keepButton.setSelected(status[2]);
			m_newButton.setSelected(status[3]);
		}
	}
}
