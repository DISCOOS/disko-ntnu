package org.redcross.sar.wp.unit;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.table.AbstractTableCell;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.UnitUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.IllegalOperationException;
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
 * The renderer for unit overview table
 *
 * @author thomasl, kenneth
 */
public class UnitTableEditorCreator
{
	private static final Logger m_logger = Logger.getLogger(UnitTableEditorCreator.class);
	private static final ResourceBundle m_resources = Internationalization.getBundle(IDiskoWpUnit.class);

	/**
	 * Set column renderer and editor. Column widths, as well as table row height
	 *
	 * @param table
	 * @param wp
	 */
	public static void installEditor(JTable table, IDiskoWpUnit wp)
	{
		// Set editor and renderer for column 1
		EditUnitCellEditor editUnit = new EditUnitCellEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(1);
		column.setCellEditor(editUnit);
		column.setCellRenderer(editUnit);

		// Set editor and renderer for column 2
		UnitStatusCellEditor editStatus = new UnitStatusCellEditor(table,wp);
		column = table.getColumnModel().getColumn(2);
		column.setCellEditor(editStatus);
		column.setCellRenderer(editStatus);
	}

	/**
	 * Cell editor and renderer for the change unit cell in table
	 *
	 * @author thomasl, kenneth
	 */
	public static class EditUnitCellEditor extends AbstractTableCell
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private JPanel[] m_panel = new JPanel[2];
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_editButton = new AbstractButton[2];

		public EditUnitCellEditor(JTable table, IDiskoWpUnit wp)
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
				public void actionPerformed(ActionEvent arg0)
				{
					// Set unit i unit details view
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					if(index==-1) return;
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);
					m_wp.setUnit(unit);
					m_wp.setLeftView(IDiskoWpUnit.UNIT_VIEW_ID);
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
			// Get unit at row
			int index = m_table.convertRowIndexToModel(row);
			if(index!=-1)
			{
				UnitTableModel model = (UnitTableModel)m_table.getModel();
				IUnitIf rowUnit = model.getUnit(index);

				// Get editing unit
				IUnitIf editingUnit = m_wp.getEditingUnit();

				if(isEditing()) {
					m_editButton[1].setSelected(editingUnit == rowUnit);
				} else {
					m_editButton[0].setSelected(editingUnit == rowUnit);
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
	 * Cell editor and renderer for unit status in the unit table
	 *
	 * @author thomasl, kenneth
	 */
	public static class UnitStatusCellEditor extends AbstractTableCell
	{
		private static final long serialVersionUID = 1L;

		private JTable m_table;
		private IDiskoWpUnit m_wp;
	
		private JPanel[] m_panel = new JPanel[2];
		private JButton[] m_pauseButton = new JButton[2];
		private JButton[] m_releaseButton = new JButton[2];

		public UnitStatusCellEditor(JTable table, IDiskoWpUnit wp)
		{
			m_wp = wp;
			m_table = table;
			
			// create renderer panel
			m_panel[0] = new JPanel();
			m_panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[0].setBackground(m_table.getBackground());
			m_panel[0].setPreferredSize(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL));
			String text = m_resources.getString("PauseButton.text");
			ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.PAUSE","32x32");
			m_pauseButton[0] = UIFactory.createButtonRenderer(null,text,icon,ButtonSize.SMALL);
			m_panel[0].add(m_pauseButton[0]);

	        text = m_resources.getString("DissolveButton.text");
			icon = DiskoIconFactory.getIcon("GENERAL.FINISH","32x32");
	        m_releaseButton[0] = UIFactory.createButtonRenderer(null,text,icon,ButtonSize.SMALL);
			m_panel[0].add(m_releaseButton[0]);
			
			// create editor panel
			m_panel[1] = new JPanel();
			m_panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[1].setBackground(m_table.getBackground());
			m_panel[1].setPreferredSize(DiskoButtonFactory.getButtonSize(ButtonSize.SMALL));
			text = m_resources.getString("PauseButton.text");
			icon = DiskoIconFactory.getIcon("GENERAL.PAUSE","32x32");
			m_pauseButton[1] = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.SMALL);
			m_pauseButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0)
				{

					int index = m_table.convertRowIndexToModel(getEditCellRow());
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
								m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(unit));
							}

	                    }
	                    catch (TransactionException ex)
	                    {
	                    	m_logger.error("Failed to commit unit status change",ex);	                    	
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
			m_panel[1].add(m_pauseButton[1]);

	        text = m_resources.getString("DissolveButton.text");
			icon = DiskoIconFactory.getIcon("GENERAL.FINISH","32x32");
	        m_releaseButton[1] = DiskoButtonFactory.createButton(null,text,icon,ButtonSize.SMALL);
	        m_releaseButton[1].addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{

					// release unit
					int index = m_table.convertRowIndexToModel(getEditCellRow());
					UnitTableModel model = (UnitTableModel)m_table.getModel();
					IUnitIf unit = model.getUnit(index);

					m_wp.getMsoModel().suspendClientUpdate();

					try
					{
						// commit?
						if(UnitUtilities.releaseUnit(unit)) {
							if(!m_wp.isNewUnit())
							{
								// Commit right away if no major updates
								m_wp.getMsoModel().commit(m_wp.getMsoModel().getChanges(unit));
							}
						}

					} catch (TransactionException ex) {
						m_logger.error("Failed to commit unit status change",ex);
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
			m_panel[1].add(m_releaseButton[1]);
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
			int index = m_table.convertRowIndexToModel(row);
			UnitTableModel model = (UnitTableModel)m_table.getModel();
			IUnitIf unit = model.getUnit(index);

			// Update buttons
			if(isEditing()) {
				m_pauseButton[1].setSelected(unit.getStatus() == UnitStatus.PAUSED);
				m_releaseButton[1].setSelected(unit.getStatus() == UnitStatus.RELEASED);
			} else {
				m_pauseButton[0].setSelected(unit.getStatus() == UnitStatus.PAUSED);
				m_releaseButton[0].setSelected(unit.getStatus() == UnitStatus.RELEASED);
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
