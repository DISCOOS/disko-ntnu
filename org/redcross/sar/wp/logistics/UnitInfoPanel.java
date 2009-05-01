package org.redcross.sar.wp.logistics;

import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.EnumField;
import org.redcross.sar.gui.field.IDiskoField;
import org.redcross.sar.gui.field.PositionField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.gui.renderer.MsoIconListCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.Position;

public class UnitInfoPanel extends JPanel
{
	private final static long serialVersionUID = 1L;

    public final static String UNIT_PRINT = "UnitPrint";
    public final static String UNIT_CHANGE = "UnitChange";
    public final static String UNIT_CENTERAT = "CenterAt";

	private FieldsPanel m_infoPanel;
	private TogglePanel m_membersPanel;
	private JList m_membersList;

	private ActionListener m_listener;

	private IUnitIf m_unit;
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
		// initialize GUI
		initialize();
	}

	/* ====================================================
	 * Public methods
	 * ==================================================== */

	public IUnitIf getUnit() {
		return m_unit;
	}
	
	public void setUnit(IUnitIf unit) {
		if (unit != null)
		{
			getInfoPanel().setCaptionText(Utils.getHtml(Utils.getBold(MsoUtils.getUnitName(unit, false))));
			if(unit.getLastKnownPosition()!=null) {
				Position p = new Position("",unit.getLastKnownPosition().getPosition());
				getInfoPanel().setValue("position", p);
			} else {
				getInfoPanel().setValue("position", null);
			}
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
            for (IPersonnelIf it : unit.getUnitPersonnelItems())
            {
            	model.addElement(it);
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
		// update button states
		getInfoPanel().setButtonEnabled(UNIT_CHANGE, unit!=null);
		getInfoPanel().setButtonEnabled(UNIT_PRINT, unit!=null);
		getInfoPanel().setButtonEnabled(UNIT_CENTERAT, unit!=null);		
		// save unit
		m_unit = unit;
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
			m_infoPanel.setPreferredExpandedHeight(200);
			m_infoPanel.setButtonVisible("toggle", true);
			m_infoPanel.addField(createPositionField("position",0));
			m_infoPanel.addField(createTextField("leader",1));
			m_infoPanel.addField(createTextField("assignment",2));
			m_infoPanel.addField(createTextField("ete",4));
			m_infoPanel.addField(createTextField("5-tone",3));
			m_infoPanel.addField(createEnumField("status",5));
			m_infoPanel.addField(createTextField("worktime",6));
			m_infoPanel.setFieldSpanX("position", 2);
			m_infoPanel.setFieldSpanX("leader", 2);
			m_infoPanel.setFieldSpanX("assignment", 2);
			ButtonSize size = m_infoPanel.getButtonSize();
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT",size), UNIT_CHANGE);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.PRINT",size), UNIT_PRINT);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("MAP.CENTERAT",size), UNIT_CENTERAT);
			m_infoPanel.addActionListener(m_listener);
		}
		return m_infoPanel;

	}

	private TogglePanel getMembersPanel() {
		if(m_membersPanel==null) {
			m_membersPanel = new TogglePanel(m_wp.getBundleText("UnitInfoPanel_hdr_7.text"),false,false);
			m_membersPanel.setPreferredExpandedHeight(100);
			m_membersPanel.setContainer(getMembersList());
			m_membersPanel.setExpanded(false);
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

	private IDiskoField createPositionField(String name, int index) {
		PositionField attr = new PositionField(name,
				m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".text"),
				false,75,25,1);
		attr.setToolTipText(m_wp.getBundleText("UnitInfoPanel_hdr_"+index+".tooltip"));
		attr.setHtml(true);
		attr.setButtonVisible(false);
		return attr;
	}
	
	private String hoursSince(Calendar t0, Calendar t1) {
		long seconds = (t1.getTimeInMillis() - t0.getTimeInMillis())/1000;
		return Utils.getTime((int)seconds);
	}
}
