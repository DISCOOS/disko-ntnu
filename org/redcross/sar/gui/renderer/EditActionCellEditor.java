package org.redcross.sar.gui.renderer;

import org.redcross.sar.mso.data.IAssignmentIf;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.EnumSet;

public class EditActionCellEditor extends AbstractCellEditor implements
		TableCellEditor, TableCellRenderer {

	private static final long serialVersionUID = 1L;
	private JPanel panel = null;
	private JButton editButton = null;
	private JButton copyButton = null;
	private EnumSet<IAssignmentIf.AssignmentStatus> editable = null;

	public EditActionCellEditor() {
		editable = EnumSet.of(IAssignmentIf.AssignmentStatus.DRAFT);
		editable.add(IAssignmentIf.AssignmentStatus.READY);
		editable.add(IAssignmentIf.AssignmentStatus.EMPTY);
//		editable.add(IAssignmentIf.AssignmentStatus.PAUSED);  Fjernet VW
		editable.add(IAssignmentIf.AssignmentStatus.ABORTED);
		
		panel = new JPanel();
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.RIGHT);
		panel.setLayout(fl);
		
		editButton = new JButton("REDIGERE");
		editButton.setActionCommand("EDIT");
		editButton.setFocusable(false);
		panel.add(editButton);
		
		copyButton = new JButton("KOPIER");
		copyButton.setActionCommand("COPY");
		copyButton.setFocusable(false);
		panel.add(copyButton);
	}
	
	public void addActionListener(ActionListener listener) {
		editButton.addActionListener(listener);
		copyButton.addActionListener(listener);
	}

	public Component getTableCellEditorComponent(JTable table, Object value, 
			boolean isSelected, int row, int column) {
		IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(row, 1);
		updatePanel(table, assignment);
		return panel;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(row, 1);
		updatePanel(table, assignment);
		return panel;
	}
	
	private void updatePanel(JTable table, IAssignmentIf assignment) {
		if (editable.contains(assignment.getStatus())) {
			editButton.setVisible(true);
		}
		else {
			editButton.setVisible(false);
		}
		panel.setBackground(table.getBackground());
		panel.setForeground(table.getForeground());
	}

	public Object getCellEditorValue() {
		return null;
	}
}
