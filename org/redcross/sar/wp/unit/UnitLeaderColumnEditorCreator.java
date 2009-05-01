package org.redcross.sar.wp.unit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.table.AbstractTableCell;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.wp.unit.UnitDetailsPanel.UnitPersonnelTableModel;

/**
 * 
 * @author kenneth
 *
 */
public class UnitLeaderColumnEditorCreator {

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
    	UnitLeaderColumnEditor editUnit = new UnitLeaderColumnEditor(table,wp);
		TableColumn column = table.getColumnModel().getColumn(2);
		column.setCellEditor(editUnit);
		column.setCellRenderer(editUnit);
        Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
        column.setPreferredWidth(dim.width + 10);
        column.setMaxWidth(dim.width + 10);
        table.setRowHeight(dim.height + 10);
    }
    
    /**
     * Renderer and editor for the leader selection column
     *
     * @author thomasl
     */
    private static class UnitLeaderColumnEditor extends AbstractTableCell
    {
        private static final long serialVersionUID = 1L;

		private JTable m_table;
		private JPanel[] m_panel = new JPanel[2];
		private IDiskoWpUnit m_wp;

		private AbstractButton[] m_leaderButton = new AbstractButton[2];

        public UnitLeaderColumnEditor(JTable table, IDiskoWpUnit wp)
        {
        	
        	// prepare
        	m_wp = wp;
            m_table = table;
            
            // initialize
            String text = m_resources.getString("LeaderButton.text");
            ImageIcon icon = DiskoIconFactory.getIcon("GENERAL.EDIT", "32x32");
			Dimension d  = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
            
			// create renderer panel
			m_panel[0] = new JPanel();
			m_panel[0].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[0].setBackground(m_table.getBackground());
			m_panel[0].setPreferredSize(d);
			m_leaderButton[0] = UIFactory.createButtonRenderer(null,text,icon,ButtonSize.SMALL);
			m_panel[0].add(m_leaderButton[0]);
            
			// create editor panel
			m_panel[1] = new JPanel(); 
			m_panel[1].setLayout(new FlowLayout(FlowLayout.CENTER));
			m_panel[1].setBackground(m_table.getBackground());
			m_panel[1].setPreferredSize(d);
            m_leaderButton[1] = DiskoButtonFactory.createToggleButton(null,text,icon,ButtonSize.SMALL);
            m_leaderButton[1].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // Set unit leader to selected personnel
                    IUnitIf editingUnit = m_wp.getEditingUnit();

                    // has editing unit?
                    if(editingUnit!=null) {
	                    int index = m_table.convertRowIndexToModel(getEditCellRow());
	                    if(index==-1) return;
	                    UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	                    IPersonnelIf newLeader = model.getPersonnel(index);

	                    // remove?
	                    if(editingUnit.getUnitLeader()==newLeader)
	                    	editingUnit.setUnitLeader(null);
	                    else
	                    	editingUnit.setUnitLeader(newLeader);

	                    fireEditingStopped();
                    }
                }
            });
            m_panel[1].add(m_leaderButton[1]);            
            
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
	            UnitPersonnelTableModel model = (UnitPersonnelTableModel) m_table.getModel();
	            IPersonnelIf rowPersonnel = model.getPersonnel(index);

				// Get editing unit
				IPersonnelIf editingPersonnel = m_wp.getEditingPersonnel();

	            if (isEditing() && editingPersonnel != null)
	            {
					if(isEditing()) {
						m_leaderButton[1].setSelected(editingPersonnel == rowPersonnel);
					} else {
						m_leaderButton[0].setSelected(editingPersonnel == rowPersonnel);
					}
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
