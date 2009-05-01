/**
 * 
 */
package org.redcross.sar.gui.table;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.ButtonsPanel;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class TableCellButtons extends AbstractTableCell {

	private static final long serialVersionUID = 1L;
	
	private int m_alignment;
	private ButtonSize m_buttonSize;
	private JLabel m_label;
	private JPanel m_panel;
	private ButtonsPanel m_buttons;
	private String m_editor;
	
	/* ===============================================================
	 * Constructors
	 * =============================================================== */
	
	public TableCellButtons() {
		// forward
		this(SwingConstants.RIGHT,ButtonSize.TINY);
	}	
	
	public TableCellButtons(int alignment, ButtonSize buttonSize) {
		// forward
		super();
		// prepare
		m_alignment = alignment;
		m_buttonSize = buttonSize;
		// initialize gui
		initialize();
	}	
	
	private void initialize() {
		getPanel();
		setEditor("button");
	}
	
	/* ===============================================================
	 * Public methods
	 * =============================================================== */
	
	public void doClick() {
		getButtons().getButton(m_editor).doClick();
	}
	
	public void doClick(String name) {
		m_buttons.getButton(name).doClick();
	}

	public String getEditor() {
		return m_editor;
	}
	
	public void setEditor(String name) {
		if(m_editor!=name) {
			if(m_editor!=null) {
				m_buttons.getButton(m_editor).setVisible(false);
			}
			m_editor = name;
		}
	}
	
	/* ===============================================================
	 * PopupManager extension
	 * =============================================================== */
	
	public boolean installEditorPopup(String name, String editor) {
		// get button
		AbstractButton b = getButtons().getButton(editor);
		// found button?
		if(editor!=null) return installPopup(name,b,false);
		// not installed
		return false;
	}
	
	public JPopupMenu getEditorPopupMenu(String editor) {
		// get button
		AbstractButton b = getButtons().getButton(editor);
		if(editor!=null) {
			return getPopupMenu(b);
		}
		return null;
	}	
	
	/* ===============================================================
	 * Required methods
	 * =============================================================== */
	
	public int getCellWidth(Graphics g, JTable table, int row, int col) {
		Icon icon = getIcon(table,row,col);
		String text = getText(table,row,col);
		int w = (icon!=null ? icon.getIconWidth() + m_label.getIconTextGap() : 0) 
			  + Utils.getStringWidth(g, Utils.stripHtml(text))
			  + getButtons().getTotalItemWidth(false);
		return w;
	}
	
	protected JComponent getEditorComponent() {
		update();
		return getPanel();
	}
		
	protected JComponent getComponent() {
		update();
		return getPanel();
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */
	
	private ButtonsPanel getButtons() {
		if(m_buttons==null) {
			m_buttons = new ButtonsPanel(m_alignment,m_buttonSize);
			m_buttons.setOpaque(false);
			m_buttons.setBorder(null);
			m_buttons.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			m_buttons.setAlignmentY(JLabel.CENTER_ALIGNMENT);
			m_buttons.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT", m_buttonSize), "button");
			m_buttons.setButtonVisible("button", false);
			m_buttons.addButton(new JCheckBox(),"checkbox");
			m_buttons.setButtonVisible("checkbox", false);
			m_buttons.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					fireActionPerformed(e);
				}
				
			});
		}
		return m_buttons;
	}
	
	private JPanel getPanel() {
		if(m_panel==null) {
			m_panel = new JPanel();
			m_panel.setLayout(new BoxLayout(m_panel,BoxLayout.X_AXIS));
			m_panel.setOpaque(false);
			m_panel.setBorder(BorderFactory.createEmptyBorder());
			m_panel.add(getLabel());
			m_panel.add(Box.createHorizontalGlue());
			m_panel.add(getButtons());
		}
		return m_panel;
	}
	
	private JLabel getLabel() {
		if(m_label==null) {
			m_label = new JLabel();
			m_label.setOpaque(true);
			m_label.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			m_label.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		}
		return m_label;
	}
	
	private void update() {
		
		// created?
		if(m_label!=null) {
		
			// update label
			m_label.setIcon(getIcon()); 			
			m_label.setText(getText()); 			
			
			// update selection state?
			if(m_table!=null) {
				if (m_isCellSelected){
					m_label.setBackground(m_table.getSelectionBackground());
					m_label.setForeground(m_table.getSelectionForeground());
					m_buttons.setBackground(m_table.getSelectionBackground());
					m_buttons.setForeground(m_table.getSelectionForeground());
				} 
				else {
					m_label.setBackground(m_table.getBackground());
					m_label.setForeground(m_table.getForeground());
					m_buttons.setBackground(m_table.getBackground());
					m_buttons.setForeground(m_table.getForeground());
				}
			}
			
			// show or hide buttons?
			boolean isVisible = (m_isEditing || m_isShowPopup);
			
			// show button
			m_buttons.setButtonVisible(m_editor, isVisible);
						
		}
		
	}	
	
}
