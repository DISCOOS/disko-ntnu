package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.TransactionException;


import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

/**
 * The personnel overview table renderer
 *
 * @author thomasl
 */
public class PersonnelTableEditor
{
	private static final Logger m_logger = Logger.getLogger(PersonnelTableEditor.class);
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);
    
	private JTable m_table;

	private IDiskoWpUnit m_wp;

	public PersonnelTableEditor(IDiskoWpUnit wp)
	{
		m_wp = wp;
	}

	public void setTable(JTable overviewTable)
	{
		m_table = overviewTable;

		TableColumn column = m_table.getColumnModel().getColumn(2);

		EditPersonnelCellEditor editPersonnel = new EditPersonnelCellEditor();
		column.setCellEditor(editPersonnel);
		column.setCellRenderer(editPersonnel);

		PersonnelStatusCellEditor personnelStatusEditor = new PersonnelStatusCellEditor();
		column = m_table.getColumnModel().getColumn(3);
		column.setCellEditor(personnelStatusEditor);
		column.setCellRenderer(personnelStatusEditor);
	}

	/**
	 * Cell editor and renderer for changing personnel details
	 *
	 * @author thomasl
	 */
	public class EditPersonnelCellEditor extends AbstractCellEditor
		implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private int m_editRow = -1;

		JButton m_editButton;
		JPanel m_panel;

		public EditPersonnelCellEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_table.getBackground());

			String text = m_resources.getString("EditButton.text");
			String letter = m_resources.getString("EditButton.letter");
			m_editButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_editButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Display selected personnel in details panel
					int modelIndex = m_table.convertRowIndexToModel(m_editRow);
					if(modelIndex==-1) return;
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);
					m_wp.setPersonnelLeft(selectedPersonnel);
					m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
					fireEditingStopped();
				}
			});
			m_panel.add(m_editButton);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			m_editRow = row;
			return m_panel;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
//			fireEditingStopped();

			// update selection?
			if(column<3) {
				// Get personnel at row
				PersonnelTableModel model = (PersonnelTableModel)table.getModel();
				row = table.convertRowIndexToModel(row);
				if(row!=-1)
				{
					IPersonnelIf rowPersonnel = model.getPersonnel(row);

					// Get personnel in personnel details panel
					IPersonnelIf editingPersonnel = m_wp.getEditingPersonnel();

					m_editButton.setSelected(editingPersonnel == rowPersonnel);
				}
			}
			else
				isSelected = false;
			return m_panel;
		}
	}

	/**
	 * Cell renderer and editor for changing personnel status
	 *
	 * @author thomasl
	 */
	public class PersonnelStatusCellEditor extends AbstractCellEditor
		implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private JPanel m_panel;

		private int m_row;

		private JButton m_calloutButton;
		private JButton m_arrivedButton;
		private JButton m_releasedButton;

		public PersonnelStatusCellEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_table.getBackground());

			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.RIGHT);
			m_panel.setLayout(fl);

			String text = m_resources.getString("CalloutButton.text");
			String letter = m_resources.getString("CalloutButton.letter");
			m_calloutButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_calloutButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{

					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(m_row));
					UnitUtils.callOutPersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel details changes",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel.add(m_calloutButton);

			text = m_resources.getString("ArrivedButton.text");
			letter = m_resources.getString("ArrivedButton.letter");
			m_arrivedButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_arrivedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(m_row));
					UnitUtils.arrivedPersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel details changes",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel.add(m_arrivedButton);

			text = m_resources.getString("DismissButton.text");
			letter = m_resources.getString("DismissButton.letter");
			m_releasedButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_releasedButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(m_row));
					UnitUtils.releasePersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel details changes",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel.add(m_releasedButton);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			m_row = row;
			updatePanel();
			return m_panel;
		}

		public Object getCellEditorValue()
		{
			return null;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			fireEditingStopped();
			m_row = row;
			updatePanel();
			return m_panel;
		}

		private void updatePanel()
		{
			// Get current personnel
			int modelIndex = m_table.convertRowIndexToModel(m_row);
			PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
			IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);

			if(selectedPersonnel!=null) {
				PersonnelStatus status = selectedPersonnel.getStatus();
				// Set button selection
				m_calloutButton.setSelected(status == PersonnelStatus.ON_ROUTE || status == PersonnelStatus.ARRIVED);
				m_arrivedButton.setSelected(status == PersonnelStatus.ARRIVED);
				m_releasedButton.setSelected(status == PersonnelStatus.RELEASED);
			}
		}
	}
}