package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.model.AbstractMsoTableModel;
import org.redcross.sar.gui.table.DiskoTable;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICalloutIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelListIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.mso.DTG;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.ResourceBundle;

/**
 * JPanel displaying alert details
 *
 * @author thomasl
 */
public class CalloutDetailsPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private JLabel m_topLabel;
	private JButton m_printButton;

	private JTextField m_titleTextField;
	private JTextField m_createdTextField;
	private JTextField m_organizationTextField;
	private JTextField m_departmentTextField;
	private JTable m_personnelTable;

	private ICalloutIf m_callout;

	private IDiskoWpUnit m_wp;

	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	public CalloutDetailsPanel(IDiskoWpUnit wp)
	{
		m_wp = wp;
		initialize();
	}

	private void initialize()
	{
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(4, 4, 4, 4);

		// Top panel
		JPanel topPanel = new JPanel(new BorderLayout());
		m_topLabel = new JLabel();
		topPanel.add(m_topLabel, BorderLayout.CENTER);
		m_printButton = DiskoButtonFactory.createButton("GENERAL.PRINT",ButtonSize.NORMAL);
		topPanel.add(m_printButton, BorderLayout.EAST);
		gbc.gridwidth = 2;
		this.add(topPanel, gbc);
		gbc.gridy++;
		gbc.gridwidth = 1;

		// Title
		m_titleTextField = new JTextField();
		layoutComponent(m_resources.getString("Title.text"), m_titleTextField, gbc);

		// Created
		m_createdTextField = new JTextField();
		m_createdTextField.setEditable(false);
		layoutComponent(m_resources.getString("Created.text"), m_createdTextField, gbc);

		// Organization
		m_organizationTextField = new JTextField();
		layoutComponent(m_resources.getString("Organization.text"), m_organizationTextField, gbc);

		// Department
		m_departmentTextField = new JTextField();
		layoutComponent(m_resources.getString("Department.text"), m_departmentTextField, gbc);

		// Personnel table
		m_personnelTable = new DiskoTable(new CallOutPersonnelTableModel());
		m_personnelTable.setFillsViewportHeight(true);
		m_personnelTable.addMouseListener(new CallOutPersonnelMouseListener());
		m_personnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		CallOutPersonnelStatusEditor editor = new CallOutPersonnelStatusEditor();
		m_personnelTable.setColumnSelectionAllowed(false);
		m_personnelTable.setRowHeight(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).height + 10);

		TableColumn column = m_personnelTable.getColumnModel().getColumn(2);
		column.setCellEditor(editor);
		column.setCellRenderer(editor);
		column.setPreferredWidth(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).width * 2 + 15);
		column.setMaxWidth(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL).width * 2 + 15);

		// no header
		m_personnelTable.setTableHeader(null);

		JScrollPane personnelTableScrollPane = new JScrollPane(m_personnelTable);
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		this.add(personnelTableScrollPane, gbc);
	}

	private void layoutComponent(String label, JComponent component, GridBagConstraints gbc)
	{
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		this.add(component, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		this.add(new JLabel(label), gbc);

		gbc.gridy++;
	}

	/**
	 * Update field contents based on current call-out
	 */
	public void updateFieldContents()
	{
		if(m_callout == null)
		{
			m_topLabel.setText("");
			m_titleTextField.setText("");
			m_createdTextField.setText("");
			m_organizationTextField.setText("");
			m_departmentTextField.setText("");
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			model.setPersonnelList(m_wp.getMsoModel(),null);
		}
		else
		{
			String topText = m_resources.getString("CallOut.text") + " " + DTG.CalToDTG(m_callout.getCreated());
			m_topLabel.setText(topText);

			m_titleTextField.setText(m_callout.getTitle());

			String created = DTG.CalToDTG(m_callout.getCreated());
			m_createdTextField.setText(created);

			m_organizationTextField.setText(m_callout.getOrganization());

			m_departmentTextField.setText(m_callout.getDepartment());

			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			model.setPersonnelList(m_wp.getMsoModel(),m_callout.getPersonnelList());
		}
	}

	/*
	 * Setters and getters
	 */
	public ICalloutIf getCallOut()
	{
		return m_callout;
	}

	public void setCallOut(ICalloutIf callout)
	{
		m_callout = callout;
	}

	/**
	 * Updates attributes
	 */
	public boolean saveCallOut()
	{
		if(m_callout != null)
		{
			m_callout.suspendClientUpdate();

			m_callout.setTitle(m_titleTextField.getText());
			m_callout.setOrganization(m_organizationTextField.getText());
			m_callout.setDepartment(m_departmentTextField.getText());

			m_callout.resumeClientUpdate(true);
		}
		// success!
		return true;
	}

	/**
	 * Personnel data for current call-out in details panel
	 *
	 * @author thomasl, kennetgu
	 */
	private class CallOutPersonnelTableModel extends AbstractMsoTableModel<IPersonnelIf>
	{
		private static final long serialVersionUID = 1L;

		private static final String NAME = "name";
		private static final String STATUS = "status";
		private static final String EDIT = "edit";

		private IPersonnelListIf m_list;

		/* ===============================================================
		 * Constructors
		 * =============================================================== */

		public CallOutPersonnelTableModel()
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
			else if(STATUS.equals(column))
				return personnel.getImportStatusText();
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

			// prepare
			m_list = list;

			// install model?
			if(list!=null) {
				connect(model,list,IPersonnelIf.PERSONNEL_NAME_COMPARATOR);
				load(list);
			}
			else {
				disconnectAll();
				clear();
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
			return new String[] {NAME, STATUS, EDIT};
		}

		public String[] getCaptions() {
			return new String[] {"Navn", "Status", "Endre"};
		}

	}

	/**
	 * Column editor for call-out personnel status changes
	 *
	 * @author thomasl
	 */
	private class CallOutPersonnelStatusEditor extends AbstractCellEditor
		implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private JPanel m_panel;
		private JButton m_arrivedButton;
		private JButton m_releaseButton;

		private int m_editingRow;

		public CallOutPersonnelStatusEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_personnelTable.getBackground());

			String text = m_resources.getString("ArrivedButton.letter");
			String letter = m_resources.getString("ArrivedButton.text");
			m_arrivedButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_arrivedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set personnel status to arrived
					CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
					int index = m_personnelTable.convertRowIndexToModel(m_editingRow);
					if(index==-1) return;
					IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);
					IPersonnelIf newPersonnelInstance = PersonnelUtilities.arrivedPersonnel(personnel);
					if(newPersonnelInstance != personnel)
					{
						// Personnel was reinstated. Replace reference in call-out
						m_callout.getPersonnelList().remove(personnel);
						if(!m_callout.getPersonnelList().exists(newPersonnelInstance))
						{
							m_callout.getPersonnelList().add(newPersonnelInstance);
						}
					}

					if(!(m_wp.getNewCallOut() || m_wp.getNewPersonnel() || m_wp.getNewUnit()))
					{
						// Commit right away if no major updates
						m_wp.getMsoModel().commit();
					}
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_panel.add(m_arrivedButton);

			text = m_resources.getString("ReleaseButton.letter");
			letter = m_resources.getString("ReleaseButton.text");
			m_releaseButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_releaseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Release personnel
					CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
					int index = m_personnelTable.convertRowIndexToModel(m_editingRow);
					if(index==-1) return;
					IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);
					PersonnelUtilities.releasePersonnel(personnel);
					if(!m_wp.getNewCallOut())
					{
						// Commit right away if not new call-out
						m_wp.getMsoModel().commit();
					}
					fireEditingStopped();
					m_personnelTable.repaint();
				}
			});
			m_panel.add(m_releaseButton);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			updateCell(row);
			return m_panel;
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean arg2, int row, int column)
		{
			m_editingRow = row;
			updateCell(row);
			return m_panel;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		private void updateCell(int row)
		{
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			int index = m_personnelTable.convertRowIndexToModel(row);
			if(index==-1) return;
			IPersonnelIf personnel = (IPersonnelIf)model.getValueAt(index, 2);

			m_arrivedButton.setSelected(personnel.getStatus() == PersonnelStatus.ARRIVED);
			m_releaseButton.setSelected(personnel.getStatus() == PersonnelStatus.RELEASED);
		}
	}

	private class CallOutPersonnelMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent me)
		{
			Point clickedPoint = new Point(me.getX(), me.getY());
			int row = m_personnelTable.rowAtPoint(clickedPoint);
			int index = m_personnelTable.convertRowIndexToModel(row);
			if(index==-1) return;
			CallOutPersonnelTableModel model = (CallOutPersonnelTableModel)m_personnelTable.getModel();
			IPersonnelIf personnel = model.getPersonnel(index);

			int clickCount = me.getClickCount();

			if(clickCount == 1)
			{
				// Display personnel info in bottom panel
				m_wp.setPersonnelBottom(personnel);
				m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
			}
			else if(clickCount == 2)
			{
				// Check if unit is new
				if(m_wp.getNewUnit() || m_wp.getNewCallOut())
				{
					// Abort view change
					return;
				}

				// Change to personnel display
				m_wp.setPersonnelLeft(personnel);
				m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
				m_wp.setPersonnelBottom(personnel);
				m_wp.setBottomView(IDiskoWpUnit.PERSONNEL_ADDITIONAL_VIEW_ID);
			}
		}

		public void mouseEntered(MouseEvent arg0){}
		public void mouseExited(MouseEvent arg0){}
		public void mousePressed(MouseEvent arg0){}
		public void mouseReleased(MouseEvent arg0){}
	}
}
