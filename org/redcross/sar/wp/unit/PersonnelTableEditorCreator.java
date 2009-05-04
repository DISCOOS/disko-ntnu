package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.table.AbstractTableCell;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IPersonnelIf.PersonnelStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.TransactionException;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

/**
 * The personnel overview table renderer
 *
 * @author thomasl, kenneth
 */
public class PersonnelTableEditorCreator
{
	private static final Logger m_logger = Logger.getLogger(PersonnelTableEditorCreator.class);
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);
	private static final ImageIcon m_alertedIcon = DiskoIconFactory.getIcon("STATUS.ALERTED","32x32");
	private static final ImageIcon m_arrivedIcon = DiskoIconFactory.getIcon("STATUS.ARRIVED","32x32");
	private static final ImageIcon m_releasedIcon = DiskoIconFactory.getIcon("STATUS.RELEASED","32x32");
    
	public static void installEditor(JTable table, IDiskoWpUnit wp)
	{
		TableColumn column = table.getColumnModel().getColumn(2);

		EditPersonnelCellEditor editPersonnel = new EditPersonnelCellEditor(table,wp);
		column.setCellEditor(editPersonnel);
		column.setCellRenderer(editPersonnel);

		PersonnelStatusCellEditor personnelStatusEditor = new PersonnelStatusCellEditor(table,wp);
		column = table.getColumnModel().getColumn(3);
		column.setCellEditor(personnelStatusEditor);
		column.setCellRenderer(personnelStatusEditor);
	}

	/**
	 * Cell editor and renderer for changing personnel details
	 *
	 * @author thomasl, kenneth
	 */
	public static class EditPersonnelCellEditor extends AbstractTableCell
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private JPanel[] m_panel = new JPanel[2];
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_editButton = new AbstractButton[2];

		public EditPersonnelCellEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
			
			// prepare
			String text = m_resources.getString("EditButton.text");
			ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.EDIT","32x32");
			Dimension d  = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
			
			// create renderer panel
			m_panel[0] = new JPanel();
			m_panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[0].setBackground(m_table.getBackground());
			m_panel[0].setPreferredSize(d);
			m_editButton[0] = UIFactory.createButtonRenderer(null,text,icon,ButtonSize.SMALL);
			m_panel[0].add(m_editButton[0]);

			// create edit panel
			m_panel[1] = new JPanel(); 
			m_panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[1].setBackground(m_table.getBackground());
			m_panel[1].setPreferredSize(d);
			m_editButton[1] = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.SMALL);
			m_editButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Display selected personnel in details panel
					int modelIndex = m_table.convertRowIndexToModel(getEditCellRow());
					if(modelIndex==-1) return;
					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf selectedPersonnel = model.getPersonnel(modelIndex);
					m_wp.setPersonnelLeft(selectedPersonnel);
					m_wp.setLeftView(IDiskoWpUnit.PERSONNEL_DETAILS_VIEW_ID);
					fireEditingStopped();
				}
			});
			m_panel[1].add(m_editButton[1]);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			super.getTableCellEditorComponent(table, value, isSelected, row, column);
			updateCell(row);
			return getEditorComponent();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			updateCell(row);
			return getComponent();
		}

		private void updateCell(int row)
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

		@Override
		public int getCellWidth(Graphics g, JTable table, int row, int col) {
			if(isEditing()) {
				return m_panel[1].getPreferredSize().width+2;
			} else {
				return m_panel[0].getPreferredSize().width+2;
			}			
		}

		@Override
		protected JComponent getComponent() {
			return m_panel[0];
		}

		@Override
		protected JComponent getEditorComponent() {
			return m_panel[1];
		}
		
	}

	/**
	 * Cell renderer and editor for changing personnel status
	 *
	 * @author thomasl, kenneth
	 */
	public static class PersonnelStatusCellEditor extends AbstractTableCell
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;

		private JPanel[] m_panel = new JPanel[2];
		private JButton[] m_calloutButton = new JButton[2];
		private JButton[] m_arrivedButton = new JButton[2];
		private JButton[] m_releasedButton = new JButton[2];
		
		public PersonnelStatusCellEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
						
			// create renderer panel
			m_panel[0] = new JPanel();
			m_panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[0].setBackground(m_table.getBackground());
			m_panel[0].setPreferredSize(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL));

			String text = m_resources.getString("CalloutButton.text");
			m_calloutButton[0] = DiskoButtonFactory.createButton(null,text,m_alertedIcon,ButtonSize.SMALL);
			m_panel[0].add(m_calloutButton[0]);

			text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[0] = UIFactory.createButtonRenderer(null,text,m_arrivedIcon,ButtonSize.SMALL);
			m_panel[0].add(m_arrivedButton[0]);
			
			text = m_resources.getString("DismissButton.text");
	        m_releasedButton[0] = UIFactory.createButtonRenderer(null,text,m_releasedIcon,ButtonSize.SMALL);
			m_panel[0].add(m_releasedButton[0]);
			
			
			// create editor panel
			m_panel[1] = new JPanel();
			m_panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[1].setBackground(m_table.getBackground());
			m_panel[1].setPreferredSize(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL));
			
			text = m_resources.getString("CalloutButton.text");
			m_calloutButton[1] = DiskoButtonFactory.createButton(null,text,m_alertedIcon,ButtonSize.SMALL);
			m_calloutButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{

					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					UnitUtils.callOutPersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel status change",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel[1].add(m_calloutButton[1]);
			
			text = m_resources.getString("ArrivedButton.text");
			m_arrivedButton[1] = DiskoButtonFactory.createButton(null,text,m_arrivedIcon,ButtonSize.SMALL);
			m_arrivedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					UnitUtils.arrivedPersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel status change",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel[1].add(m_arrivedButton[1]);

			text = m_resources.getString("DismissButton.text");
			m_releasedButton[1] = DiskoButtonFactory.createButton(null,text,m_releasedIcon,ButtonSize.SMALL);
			m_releasedButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_wp.getMsoModel().suspendClientUpdate();

					PersonnelTableModel model = (PersonnelTableModel)m_table.getModel();
					IPersonnelIf personnel = model.getPersonnel(m_table.convertRowIndexToModel(getEditCellRow()));
					UnitUtils.releasePersonnel(personnel);

					if(!m_wp.isNewPersonnel())
					{
				        try {
							// Commit right away if no major updates
							m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(personnel));
						} catch (TransactionException ex) {
							m_logger.error("Failed to commit personnel status change",ex);
						}            
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

				}
			});
			m_panel[1].add(m_releasedButton[1]);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			super.getTableCellEditorComponent(table, value, isSelected, row, column);
			updateCell(row);
			return getEditorComponent();
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			updateCell(row);
			return getComponent();
		}


		private void updateCell(int row)
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
		
		@Override
		public int getCellWidth(Graphics g, JTable table, int row, int col) {
			if(isEditing()) {
				return m_panel[1].getPreferredSize().width+2;
			} else {
				return m_panel[0].getPreferredSize().width+2;
			}			
		}

		@Override
		protected JComponent getComponent() {
			return m_panel[0];
		}

		@Override
		protected JComponent getEditorComponent() {
			return m_panel[1];
		}		
		
	}
}