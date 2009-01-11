package org.redcross.sar.wp.unit;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.mso.data.IUnitIf.UnitType;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.work.event.IWorkFlowListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.EnumSet;

/**
 * Dialog for choosing unit type
 *
 * @author thomasl
 */
public class UnitTypeDialog extends DefaultDialog
{
	private static final long serialVersionUID = 1L;

	private JList m_typeList;

	private static UnitType m_type;

	private IDiskoWpUnit m_wpUnit;

	public UnitTypeDialog(IDiskoWpUnit wpUnit, Component parent)
	{
		// forward
		super(wpUnit.getApplication().getFrame());

		// prepare
		m_wpUnit = wpUnit;

		// initialize GUI
		initialize();

		// show in center of parent
		setSnapToLocation(parent, DefaultDialog.POS_CENTER, DefaultDialog.SIZE_TO_OFF, true, false);

	}

	private void initialize()
	{

		// set modal
		this.setModal(true);

		// create content panel
		DefaultPanel panel = new DefaultPanel(m_wpUnit.getBundleText("CreateNewUnit.text"));
		panel.setPreferredContainerSize(new Dimension(300, 400));

		// create unit list
		m_typeList = new JList();
		m_typeList.setPreferredSize(new Dimension(400, 400));

		EnumSet<UnitType> data  = EnumSet.complementOf(EnumSet.of(UnitType.CP));
		m_typeList.setListData(data.toArray());
		m_typeList.setCellRenderer(new UnitTypeCellRenderer());
		m_typeList.setFixedCellHeight(DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL).height);
		m_typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m_typeList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				if(!isChangeable()) return;
				m_type = (UnitType)m_typeList.getSelectedValue();
				setDirty(true);
			}

		});
		m_typeList.setSelectedIndex(0);

		// add mouse listener
		m_typeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){
			   if(e.getClickCount() == 2){
			     int index = m_typeList.locationToIndex(e.getPoint());
			     ListModel model = m_typeList.getModel();
			     m_type =(UnitType)model.getElementAt(index);;
			     m_typeList.ensureIndexIsVisible(index);
			     m_typeList.setSelectedIndex(index);
			     finish();
			   }
			}
		});

		// set list as body
		panel.setContainer(m_typeList);

		// add work listener
		panel.addWorkFlowListener((IWorkFlowListener)m_wpUnit);

		// add content panel
		this.setContentPane(panel);
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

			ImageIcon icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(type),"48x48");
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

	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		setDirty(true);
	}
}
