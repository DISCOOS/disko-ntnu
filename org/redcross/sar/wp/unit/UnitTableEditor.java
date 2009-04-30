package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.util.except.TransactionException;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;

/**
 * The renderer for unit overview table
 *
 * @author thomasl
 */
public class UnitTableEditor
{
	private static final Logger m_logger = Logger.getLogger(UnitTableEditor.class);
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	private JTable m_table;

	private IDiskoWpUnit m_wp;

	public UnitTableEditor(IDiskoWpUnit wp)
	{
		m_wp = wp;
	}

	/**
	 * Set column renderer and editor. Column widths, as well as table row height
	 *
	 * @param unitTable
	 */
	public void setTable(JTable unitTable)
	{
		m_table = unitTable;

		// Set editor and renderer for column 1
		EditUnitCellEditor editUnit = new EditUnitCellEditor();
		TableColumn column = m_table.getColumnModel().getColumn(1);
		column.setCellEditor(editUnit);
		column.setCellRenderer(editUnit);

		// Set editor and renderer for column 2
		UnitStatusCellEditor unitStatusEditor = new UnitStatusCellEditor();
		column = m_table.getColumnModel().getColumn(2);
		column.setCellEditor(unitStatusEditor);
		column.setCellRenderer(unitStatusEditor);
	}

	/**
	 * Cell editor and renderer for the change unit cell in table
	 *
	 * @author thomasl
	 */
	public class EditUnitCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private int m_editingRow = -1;

		private JPanel m_panel;
		private JButton m_editButton;

		public EditUnitCellEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_table.getBackground());

			String text = m_resources.getString("EditButton.text");
			String letter = m_resources.getString("EditButton.letter");
			m_editButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_editButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set unit i unit details view
					int index = m_table.convertRowIndexToModel(m_editingRow);
					if(index==-1) return;
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
					m_wp.setUnit(unit);
					m_wp.setLeftView(IDiskoWpUnit.UNIT_VIEW_ID);
					fireEditingStopped();
				}
			});
			m_panel.add(m_editButton);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			m_editingRow = row;
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
			// Get unit at row
			int index = m_table.convertRowIndexToModel(row);
			if(index!=-1)
			{
				UnitTableModel model = (UnitTableModel)m_table.getModel();
				IUnitIf rowUnit = model.getUnit(index);

				// Get editing unit
				IUnitIf editingUnit = m_wp.getEditingUnit();

				m_editButton.setSelected(editingUnit == rowUnit);
			}

			return m_panel;
		}
	}

	/**
	 * Cell editor and renderer for unit status in the unit table
	 *
	 * @author thomasl
	 */
	public class UnitStatusCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		private int m_editingRow = -1;

		private JPanel m_panel;
		private JButton m_pauseButton;
		private JButton m_releaseButton;

		public UnitStatusCellEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_table.getBackground());

			String text = m_resources.getString("PauseButton.text");
			String letter = m_resources.getString("PauseButton.letter");
			m_pauseButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
			m_pauseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{

					// release unit
					int index = m_table.convertRowIndexToModel(m_editingRow);
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);

					m_wp.getMsoModel().suspendClientUpdate();

	                if (unit != null)
	                {
	                    try
	                    {
	                    	if(unit.isPaused())
	                    		unit.resume();
	                    	else
	                    		unit.pause();

							if(!m_wp.isNewUnit())
							{
								m_wp.commit();
							}

	                    }
	                    catch (IllegalOperationException ex)
	                    {
	                    	Utils.showWarning("Enhet kan ikke endre status");
	                    }
	                }
					m_wp.getMsoModel().resumeClientUpdate(true);

					fireEditingStopped();

				}
			});
			m_panel.add(m_pauseButton);

	        text = m_resources.getString("DissolveButton.text");
	        letter = m_resources.getString("DissolveButton.letter");
	        m_releaseButton = DiskoButtonFactory.createButton(letter,text,null,ButtonSize.SMALL);
	        m_releaseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// release unit
					int index = m_table.convertRowIndexToModel(m_editingRow);
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);

					m_wp.getMsoModel().suspendClientUpdate();

					try
					{
						// commit?
						if(UnitUtilities.releaseUnit(unit)) {
							if(!m_wp.isNewUnit())
							{
						        try {
									// Commit right away if no major updates
									m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(unit));
								} catch (TransactionException ex) {
									m_logger.error("Failed to commit unit details changes",ex);
								}            
							}
						}

					}
					catch (IllegalOperationException e1)
					{
						Utils.showError(m_resources.getString("ReleaseUnitError.header"),
								m_resources.getString("ReleaseUnitError.text"));
					}

					m_wp.getMsoModel().resumeClientUpdate(true);

					fireEditingStopped();
				}
			});
			m_panel.add(m_releaseButton);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			m_editingRow = row;
			updateCell(row);
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
			updateCell(row);
			return m_panel;
		}

		private void updateCell(int row)
		{
			int index = m_table.convertRowIndexToModel(row);
			UnitTableModel model = (UnitTableModel)m_table.getModel();
			IUnitIf unit = model.getUnit(index);

			// Update buttons
			//m_pauseButton.setSelected(unit.getStatus() == UnitStatus.PAUSED);
			m_releaseButton.setSelected(unit.getStatus() == UnitStatus.RELEASED);

		}
	}
}
