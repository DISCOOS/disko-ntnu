package org.redcross.sar.wp.unit;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoButtonFactory;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.util.Internationalization;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.EnumSet;

/**
 * Dialog for choosing unit type
 *
 * @author thomasl
 */
public class UnitTypeDialog extends DiskoDialog
{
	private static final long serialVersionUID = 1L;

	private JPanel m_contentsPanel;
	private JButton m_okButton;
	private JButton m_cancelButton;
	//private EnumSet<UnitType> m_listValues;
	private JList m_typeList;

	private static UnitType m_type;

	private IDiskoWpUnit m_wpUnit;

	public UnitTypeDialog(IDiskoWpUnit wpUnit, JComponent parentComponent)
	{
		super(wpUnit.getApplication().getFrame());
		m_wpUnit = wpUnit;
		initialize(parentComponent);
	}

	private void initialize(JComponent parentComponent)
	{
		this.setLocationRelativeTo(parentComponent, DiskoDialog.POS_CENTER, true);
		this.setPreferredSize(new Dimension(400, 500));
		m_contentsPanel = new JPanel();
		m_contentsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		m_contentsPanel.setLayout(new BoxLayout(m_contentsPanel, BoxLayout.PAGE_AXIS));

		// Labels
		//m_contentsPanel.add(new JLabel(m_wpUnit.getText("CreateNewUnit.text")));
		//m_contentsPanel.add(Box.createRigidArea(new Dimension(10, 20)));
		m_contentsPanel.add(new JLabel(m_wpUnit.getText("ChooseUnitType.text")));

		// List
		m_typeList = new JList();
		EnumSet<UnitType> data  = EnumSet.of(
				UnitType.AIRCRAFT,
				UnitType.BOAT,
				UnitType.DOG,
				UnitType.TEAM,
				UnitType.VEHICLE);
		m_typeList.setListData(data.toArray());
		m_typeList.setCellRenderer(new UnitTypeCellRenderer());
		m_typeList.setFixedCellHeight(DiskoButtonFactory.NORMAL_BUTTON_SIZE.height);
		m_typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane tableScroller = new JScrollPane(m_typeList);
		m_contentsPanel.add(tableScroller);

		// Buttons
		JPanel actionButtonRow = new JPanel();

		m_cancelButton = DiskoButtonFactory.createNormalButton(DiskoButtonFactory.ButtonType.CancelButton);
		m_cancelButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				fireOnWorkCancel();
			}
		});
		actionButtonRow.add(m_cancelButton);

		m_okButton = DiskoButtonFactory.createNormalButton(DiskoButtonFactory.ButtonType.OkButton);
		m_okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// get type
				m_type = (UnitType)m_typeList.getSelectedValue();
				
				// was type selected?
				if(m_type==null)
					Utils.showWarning("Du må først velge en enhet");
				else
					fireOnWorkFinish();
			}
		});
		actionButtonRow.add(m_okButton);

		m_contentsPanel.add(actionButtonRow);

		this.add(m_contentsPanel);
		this.pack();
	}

	public UnitType getUnitType()
	{
		return m_type;
	}

	public class UnitTypeCellRenderer extends JLabel implements ListCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public UnitTypeCellRenderer()
		{
			this.setOpaque(true);
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean hasFocus)
		{
			UnitType type = (UnitType)value;

			ImageIcon icon = Utils.getIcon(type);
			this.setIcon(icon);

			String text = Internationalization.translate(type);
			setText(text);

			if(isSelected)
			{
				this.setBackground(list.getSelectionBackground());
				this.setForeground(list.getSelectionForeground());
			}
			else
			{
				this.setBackground(list.getBackground());
				this.setForeground(list.getForeground());
			}
			return this;
		}
	}
}
