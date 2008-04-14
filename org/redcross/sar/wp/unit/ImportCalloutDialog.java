package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelImportStatus;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.DTG;
import org.redcross.sar.wp.IDiskoWpModule;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
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
import java.io.FileReader;
import java.io.IOException;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dialog handling import of new call-outs
 * 
 * @author thomasl
 */
public class ImportCalloutDialog extends DiskoDialog
{
	private static final long serialVersionUID = 1L;

    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	private static final String IMPORT_ID = "IMPORT";
	private static final String CONFIRM_ID = "CONFIRM";
	private static String m_currentPanel = IMPORT_ID;
	
	List<PersonnelAuxiliary> m_personnelList;
	
	private JPanel m_contentsPanel;
	private JPanel m_topPanel;
	private JPanel m_bottomPanel;
	
	private JPanel m_importPanel;
	private JLabel m_importTopLabel;
	private JTextField m_dtgTextField;
	private JTextField m_titleTextField;
	private JTextField m_organizationTextField;
	private JTextField m_departmentTextField;
	private JTextField m_fileTextField;
	private JButton m_fileDialogButton;
	
	private JPanel m_confirmPanel;
	private JLabel m_confirmTopLanel;
	private JTable m_personnelTable;
	private ImportPersonnelTableModel m_tableModel;
	
	private JButton m_backButton;
	private JButton m_nextButton;
	private JButton m_cancelButton;
	private JButton m_okButton;
	
	private IDiskoWpModule m_wpModule;
	
	public ImportCalloutDialog(IDiskoWpModule wp)
	{
		super(wp.getApplication().getFrame());
		this.setModal(true);
		m_wpModule = wp;
		m_personnelList = new LinkedList<PersonnelAuxiliary>();
		
		initialize();
	}
	
	private void initialize()
	{
		this.setPreferredSize(new Dimension(400, 450));
		m_contentsPanel = new JPanel();
		m_contentsPanel.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.PAGE_AXIS));
		m_topPanel = new JPanel(new CardLayout());
		m_contentsPanel.add(m_topPanel);
		m_bottomPanel = new JPanel();
		m_contentsPanel.add(m_bottomPanel);
		
		initializeImportPanel();
		initializeConfirmPanel();
		initializeButtons();
		
		this.add(m_contentsPanel);
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
		
		m_importTopLabel = new JLabel();
		m_importTopLabel.setText(m_resources.getString("ImportCallOut.text") + " 1/2");
		gbc.gridwidth = 3;
		m_importPanel.add(m_importTopLabel, gbc);
		gbc.gridy++;
		
		m_dtgTextField = new JTextField();
		layoutComponent(m_importPanel, "DTG", m_dtgTextField, gbc, 1);
		
		m_titleTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_resources.getString("Title.text"), m_titleTextField, gbc, 1);
		
		m_organizationTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_resources.getString("Organization.text"), m_organizationTextField, gbc, 1);
		
		m_departmentTextField = new JTextField();
		gbc.gridwidth = 2;
		layoutComponent(m_importPanel, m_resources.getString("Department.text"), m_departmentTextField, gbc, 1);
		
		gbc.gridwidth = 3;
		m_importPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
		gbc.gridy++;
		
		gbc.gridwidth = 1;
		m_fileTextField = new JTextField();
		layoutComponent(m_importPanel, m_resources.getString("FileName.text"), m_fileTextField, gbc, 0);

		String text = m_resources.getString("File.text");
		m_fileDialogButton = DiskoButtonFactory.createButton(text,text,null,ButtonSize.NORMAL);
		m_fileDialogButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				//Bring up file dialog
				JFileChooser fc = new JFileChooser();
				int n = fc.showOpenDialog(null);
				if(n == JFileChooser.APPROVE_OPTION)
				{
					java.io.File file = fc.getSelectedFile();
					m_fileTextField.setText(file.getPath());
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
		fileDescription.setText(m_resources.getString("PersonnelFileDescription.text"));
		gbc.gridwidth = 3;
		gbc.gridx = 0;
		m_importPanel.add(fileDescription, gbc);
		
		gbc.gridy++;
		m_importPanel.add(new JSeparator(JSeparator.HORIZONTAL), gbc);
		
		m_topPanel.add(m_importPanel, IMPORT_ID);
	}

	private void initializeConfirmPanel()
	{
		m_confirmPanel = new JPanel();
		m_confirmPanel.setLayout(new BoxLayout(m_confirmPanel, BoxLayout.PAGE_AXIS));
		
		m_confirmTopLanel = new JLabel(m_resources.getString("ImportCallOut.text") + " 2/2");
		m_confirmPanel.add(m_confirmTopLanel);
		
		m_tableModel = new ImportPersonnelTableModel();
		m_personnelTable = new JTable(m_tableModel);
		
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
		
		JScrollPane tableScrollPane = new JScrollPane(m_personnelTable);
		m_confirmPanel.add(tableScrollPane);
		
		m_topPanel.add(m_confirmPanel, CONFIRM_ID);
	}

	private void initializeButtons()
	{
		m_backButton = DiskoButtonFactory.createButton("GENERAL.BACK",ButtonSize.NORMAL);
		m_backButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if(m_currentPanel == CONFIRM_ID)
				{
					CardLayout layout = (CardLayout)m_topPanel.getLayout();
					layout.show(m_topPanel, IMPORT_ID);
					m_currentPanel = IMPORT_ID;
					
					m_okButton.setEnabled(false);
					m_backButton.setEnabled(false);
				}
			}
		});
		m_backButton.setEnabled(false);
		m_bottomPanel.add(m_backButton);
		
		m_nextButton = DiskoButtonFactory.createButton("GENERAL.NEXT",ButtonSize.NORMAL);
		m_nextButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				if(m_currentPanel == IMPORT_ID)
				{
					try
					{
						importFile();
						checkForPreExistingPersonnel();
					} 
					catch (IOException e)
					{
					}
					
					CardLayout layout = (CardLayout)m_topPanel.getLayout();
					layout.show(m_topPanel, CONFIRM_ID);
					m_currentPanel = CONFIRM_ID;
					
					m_okButton.setEnabled(true);
					m_backButton.setEnabled(true);
				}
				else
				{
					saveCallout();
					CardLayout layout = (CardLayout)m_topPanel.getLayout();
					layout.show(m_topPanel, IMPORT_ID);
					m_currentPanel = IMPORT_ID;
					clearContents();
					fireOnWorkFinish();
				}
			}
		});
		m_bottomPanel.add(m_nextButton);
		
		m_okButton = DiskoButtonFactory.createButton("GENERAL.OK",ButtonSize.NORMAL);
		m_okButton.setEnabled(false);
		m_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveCallout();
				CardLayout layout = (CardLayout)m_topPanel.getLayout();
				layout.show(m_topPanel, IMPORT_ID);
				m_currentPanel = IMPORT_ID;
				clearContents();
				fireOnWorkFinish();
			}
		});
		m_bottomPanel.add(m_okButton);
		
		m_cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
		m_cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				CardLayout layout = (CardLayout)m_topPanel.getLayout();
				layout.show(m_topPanel, IMPORT_ID);
				m_currentPanel = IMPORT_ID;
				fireOnWorkCancel();
			}
		});
		m_bottomPanel.add(m_cancelButton);
		
		
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
	
	private void clearContents()
	{
		m_personnelList.clear();
		
		m_titleTextField.setText("");
		m_dtgTextField.setText("");
		m_organizationTextField.setText("");
		m_departmentTextField.setText("");
		m_fileTextField.setText("");
	}
	
	/**
	 * Imports call-out personnel data from selected file
	 * 
	 * @throws IOException 
	 */
	private void importFile() throws IOException
	{
		m_personnelList.clear();
		String filePath = m_fileTextField.getText();
		File file = new File(filePath);
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
	}
	
	/**
	 * Checks if personnel about to be imported already exists.
	 * Checks name for the time being
	 */
	private void checkForPreExistingPersonnel()
	{
//		// Build key hash-set
//		HashSet<String> personnelKeys = new HashSet<String>();
//		for(IPersonnelIf personnel : m_wpModule.getMsoManager().getCmdPost().getAttendanceListItems())
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
		
		// Brute-force check
		for(PersonnelAuxiliary personnel : m_personnelList)
		{
			for(IPersonnelIf potentialMatch : m_wpModule.getMsoManager().getCmdPost().getAttendanceListItems())
			{
				personnel.equals(potentialMatch);
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
		ICalloutIf callout = m_wpModule.getMsoManager().createCallout();
		
		try
		{
			callout.setCreated(DTG.DTGToCal(m_dtgTextField.getText()));
		} 
		catch (IllegalMsoArgumentException e)
		{
			// TODO Set created to now?
			callout.setCreated(Calendar.getInstance());
		}
		callout.setTitle(m_titleTextField.getText());
		callout.setOrganization(m_organizationTextField.getText());
		callout.setDepartment(m_departmentTextField.getText());
		
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
							msoPersonnel = m_wpModule.getMsoManager().createPersonnel();
							msoPersonnel.setDataSourceID(personnel.getId());
							msoPersonnel.setFirstname(personnel.getFirstName());
							msoPersonnel.setLastname(personnel.getLastName());
							msoPersonnel.setTelephone1(personnel.getPhone());
							msoPersonnel.setImportStatus(PersonnelImportStatus.IMPORTED);
							personnel.getPersonnelRef().setNextOccurence(msoPersonnel);
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
							msoPersonnel = PersonnelUtilities.reinstateResource(msoPersonnel, PersonnelStatus.ON_ROUTE);
						}
						else if(msoPersonnel.getStatus() != PersonnelStatus.ARRIVED)
						{
							msoPersonnel.setStatus(PersonnelStatus.ON_ROUTE);
						}
					}
					else
					{
						// Create new personnel
						msoPersonnel = m_wpModule.getMsoManager().createPersonnel();
						msoPersonnel.setFirstname(personnel.getFirstName());
						msoPersonnel.setLastname(personnel.getLastName());
						msoPersonnel.setTelephone1(personnel.getPhone());
						msoPersonnel.setStatus(PersonnelStatus.ON_ROUTE);
					}
				
					callout.addPersonel(msoPersonnel);
					
				}
				else {
					// TODO: implement handling of invalid imports
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
			String thatName = personnel.getFirstname() + " " + personnel.getLastname();
			boolean same = thisName.equals(thatName);
			
			if(same)
			{
				m_personnelRef = personnel;
				// Set reference to end of personnel history chain
				while(m_personnelRef.getNextOccurence() != null)
				{
					m_personnelRef = m_personnelRef.getNextOccurence();
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
	 * Table data for personnel about to be imported
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
			
			String letter = m_resources.getString("UpdateButton.text");
			String text = m_resources.getString("UpdateButton.letter");
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
			
			letter = m_resources.getString("KeepButton.text");
			text = m_resources.getString("KeepButton.letter");
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
			
			letter = m_resources.getString("NewButton.text");
			text = m_resources.getString("NewButton.letter");
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
