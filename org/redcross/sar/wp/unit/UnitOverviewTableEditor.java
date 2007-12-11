package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class UnitOverviewTableEditor
{
    private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	private JTable m_table;

	private IDiskoWpUnit m_wpUnit;

	public UnitOverviewTableEditor(IDiskoWpUnit wp)
	{
		m_wpUnit = wp;
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

			m_editButton = DiskoButtonFactory.createSmallButton(m_resources.getString("EditButton.letter"));
			m_editButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Set unit i unit details view
					int index = m_table.convertRowIndexToModel(m_editingRow);
					UnitOverviewTableModel model = (UnitOverviewTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
					m_wpUnit.setUnit(unit);
					m_wpUnit.setLeftView(IDiskoWpUnit.UNIT_VIEW_ID);
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
			UnitOverviewTableModel model = (UnitOverviewTableModel)m_table.getModel();
			IUnitIf rowUnit = model.getUnit(index);

			// Get editing unit
			IUnitIf editingUnit = m_wpUnit.getEditingUnit();

			m_editButton.setSelected(editingUnit == rowUnit);

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
		private JCheckBox m_releaseCheckButton;

		public UnitStatusCellEditor()
		{
			m_panel = new JPanel();
			m_panel.setBackground(m_table.getBackground());

			m_pauseButton = DiskoButtonFactory.createSmallButton(m_resources.getString("PauseButton.letter"));
			m_pauseButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{
					// Toggle working status
					int index = m_table.convertRowIndexToModel(m_editingRow);
					UnitOverviewTableModel model = (UnitOverviewTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);

					try
					{
						UnitUtilities.toggleUnitPause(unit);

						// Commit
						if(!m_wpUnit.getNewUnit())
						{
							m_wpUnit.getMsoModel().commit();
						}
					}
					catch (IllegalOperationException e)
					{
						ErrorDialog error = new ErrorDialog(null);
						error.showError(m_resources.getString("PauseUnitError.header"),
								m_resources.getString("PauseUnitError.text"));
					}

					fireEditingStopped();
				}
			});
			m_panel.add(m_pauseButton);

			m_releaseCheckButton = new JCheckBox();
			m_releaseCheckButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					// Release unit
					int index = m_table.convertRowIndexToModel(m_editingRow);
					UnitOverviewTableModel model = (UnitOverviewTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);

					try
					{
						UnitUtilities.releaseUnit(unit);

						// Commit
						if(!m_wpUnit.getNewUnit())
						{
							m_wpUnit.getMsoModel().commit();
						}
					}
					catch (IllegalOperationException e1)
					{
						ErrorDialog error = new ErrorDialog(null);
						error.showError(m_resources.getString("ReleaseUnitError.header"),
								m_resources.getString("ReleaseUnitError.text"));
					}

					fireEditingStopped();
				}
			});
			m_panel.add(m_releaseCheckButton);
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
			UnitOverviewTableModel model = (UnitOverviewTableModel)m_table.getModel();
			IUnitIf unit = model.getUnit(index);

			// Update buttons
			m_pauseButton.setSelected(unit.getStatus() == UnitStatus.PAUSED);
			m_releaseCheckButton.setSelected(unit.getStatus() != UnitStatus.RELEASED);
		}
	}
}
