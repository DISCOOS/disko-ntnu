package org.redcross.sar.wp.logistics;

import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.field.EnumField;
import org.redcross.sar.gui.field.IDiskoField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.renderer.MsoIconListCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;

public class UnitInfoPanel extends JPanel
{
	private final static long serialVersionUID = 1L;

    private final static String UNIT_PRINT = "UnitPrint";
    private final static String UNIT_CHANGE = "UnitChange";

	private FieldsPanel m_infoPanel;
	private BasePanel m_membersPanel;
	private JList m_membersList;

	private ActionListener m_listener;

	private IDiskoWpLogistics m_wp;

	/* ====================================================
	 * Constructors
	 * ==================================================== */

	public UnitInfoPanel(IDiskoWpLogistics wp, ActionListener listener)  {
		// forward
		super();
		// prepare
		m_wp = wp;
		m_listener = listener;
		// initialize gui
		initialize();
	}

	/* ====================================================
	 * Public methods
	 * ==================================================== */

	public void setUnit(IUnitIf unit) {
		if (unit != null)
		{
			getInfoPanel().setCaptionText(Utils.getHtml(Utils.getBold(MsoUtils.getUnitName(unit, false))));
			IPersonnelIf leader = unit.getUnitLeader();
			getInfoPanel().setValue("leader", leader!=null ? MsoUtils.getPersonnelName(unit.getUnitLeader(),false) : "");
			IAssignmentIf assignment = unit.getActiveAssignment();
			getInfoPanel().setValue("assignment", assignment!=null ? MsoUtils.getAssignmentName(unit.getActiveAssignment(),1) : "");
			getInfoPanel().setValue("5-tone", unit.getToneID());
			Calendar t0 = assignment!=null ? assignment.getTimeEstimatedFinished() : null;
			Calendar t1 = Calendar.getInstance();
			getInfoPanel().setValue("ete", t0!=null ? hoursSince(t1,t0): "");
			getInfoPanel().setValue("status", unit.getStatus());
			t0 = unit.getCreatedTime();
			getInfoPanel().setValue("worktime", t0!=null && t1!=null ? hoursSince(t0,t1) : "");
			DefaultListModel model = new DefaultListModel();
            for (IPersonnelIf p : unit.getUnitPersonnelItems())
            {
            	model.addElement(p);
            }
            getMembersList().setModel(model);

		} else
		{
			getInfoPanel().setCaptionText("Velg et enhet");
			getInfoPanel().setValue("leader", "");
			getInfoPanel().setValue("assignment", "");
			getInfoPanel().setValue("5-tone", "");
			getInfoPanel().setValue("ete", "00:00:00");
			getInfoPanel().setValue("status", null);
			getInfoPanel().setValue("worktime", "00:00:00");
			getMembersList().setModel(new DefaultListModel());
		}
		getInfoPanel().setButtonEnabled(UNIT_CHANGE, unit!=null);
		getInfoPanel().setButtonEnabled(UNIT_PRINT, unit!=null);
	}

	/* ====================================================
	 * Helper methods
	 * ==================================================== */

	private void initialize() {
		// prepare body layout
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		// add panels
		add(getInfoPanel());
		add(Box.createVerticalStrut(5));
		add(getMembersPanel());
	}

	private FieldsPanel getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new FieldsPanel("","Ingen egenskaper",false,false);
			m_infoPanel.setColumns(2);
			m_infoPanel.addField(createTextField("leader",0));
			m_infoPanel.addField(createTextField("assignment",1));
			m_infoPanel.addField(createTextField("5-tone",2));
			m_infoPanel.addField(createTextField("ete",3));
			m_infoPanel.addField(createEnumField("status",4));
			m_infoPanel.addField(createTextField("worktime",5));
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT",m_infoPanel.getButtonSize()), UNIT_CHANGE);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.PRINT",m_infoPanel.getButtonSize()), UNIT_PRINT);
			m_infoPanel.addActionListener(m_listener);
		}
		return m_infoPanel;

	}

	private BasePanel getMembersPanel() {
		if(m_membersPanel==null) {
			m_membersPanel = new BasePanel(m_wp.getBundleText("UnitInfoPanel_hdr_6.text"));
			m_membersPanel.setContainer(getMembersList());
			m_membersPanel.setScrollBarPolicies(
					BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
					BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return m_membersPanel;
	}

	private JList getMembersList() {
		if(m_membersList==null) {
			m_membersList = new JList();
			m_membersList.setCellRenderer(new MsoIconListCellRenderer(1,false,"24x24"));
			m_membersList.setVisibleRowCount(2);
		}
		return m_membersList;
	}

	private IDiskoField createTextField(String name, int index) {
		IDiskoField attr = new TextLineField(name,
				m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".text"),false,75,25);
		attr.setToolTipText(m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".tooltip"));
		return attr;
	}

	private IDiskoField createEnumField(String name, int index) {
		IDiskoField attr = new EnumField(name,
				m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".text"),false,75,25);
		attr.setToolTipText(m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".tooltip"));
		attr.setButtonVisible(false);
		return attr;
	}

	private String hoursSince(Calendar t0, Calendar t1) {
		long seconds = (t1.getTimeInMillis() - t0.getTimeInMillis())/1000;
		return Utils.getTime((int)seconds);
	}

}
