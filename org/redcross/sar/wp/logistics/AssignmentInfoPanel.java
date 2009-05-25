package org.redcross.sar.wp.logistics;

import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.EnumField;
import org.redcross.sar.gui.field.IField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.panel.FieldPane;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;

public class AssignmentInfoPanel extends JPanel
{
	private final static long serialVersionUID = 1L;

    public final static String ASG_RESULT = "AsgResult";
	public final static String ASG_RETURN = "AsgReturn";
	public final static String ASG_PRINT = "AsgPrint";
	public final static String ASG_CHANGE = "AsgChange";

	private FieldPane m_infoPanel;
	private TogglePanel m_remarksPanel;
	private JTextArea m_remarksArea;

	private ActionListener m_listener;

	private IDiskoWpLogistics m_wp;

	/* ====================================================
	 * Constructors
	 * ==================================================== */

	public AssignmentInfoPanel(IDiskoWpLogistics wp, ActionListener listener)  {
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

	public void setAssignment(IAssignmentIf assignment) {
		if (assignment != null)
		{
			getInfoPanel().setCaptionText(Utils.getHtml(Utils.getBold(MsoUtils.getAssignmentName(assignment, 1))));
			getInfoPanel().setValue("priority", assignment.getPriority());
			getInfoPanel().setValue("eta",
					AssignmentStatus.EXECUTING.equals(assignment.getStatus()) ?
							assignment.getTimeEstimatedFinished() : null);
			IUnitIf unit = assignment.getOwningUnit();
			getInfoPanel().setValue("unit", unit!=null ? MsoUtils.getUnitName(unit, false) : "");
			Calendar t = assignment.getTime(AssignmentStatus.FINISHED);
			if(t==null) t = assignment.getTime(AssignmentStatus.ABORTED);
			getInfoPanel().setValue("mta", t);
			getInfoPanel().setValue("status", assignment.getStatus());
			Calendar t0 = assignment.getTime(AssignmentStatus.EXECUTING);
			getInfoPanel().setValue("worktime", t0!=null && t!=null ? hoursSince(t,t0) : "00:00:00");
			getRemarksArea().setText(assignment.getRemarks());
		} else
		{
			getInfoPanel().setCaptionText("Velg et oppdrag");
			getInfoPanel().setValue("priority", null);
			getInfoPanel().setValue("eta", null);
			getInfoPanel().setValue("unit", "");
			getInfoPanel().setValue("mta", null);
			getInfoPanel().setValue("status", null);
			getInfoPanel().setValue("worktime", "00:00:00");
			getRemarksArea().setText("");
		}
		getInfoPanel().setButtonEnabled(ASG_CHANGE, assignment!=null);
		getInfoPanel().setButtonEnabled(ASG_PRINT, assignment!=null);
		getInfoPanel().setButtonVisible(ASG_RETURN, assignment!=null);
	}

	public boolean isBackButtonVisible() {
		return getInfoPanel().isButtonVisible(ASG_RETURN);
	}

	public void setBackButtonVisible(boolean isVisible) {
		getInfoPanel().setButtonVisible(ASG_RETURN,isVisible);
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
		add(getRemarksPanel());
	}

	private FieldPane getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new FieldPane("","Ingen egenskaper",false,false);
			m_infoPanel.setPreferredExpandedHeight(175);
			m_infoPanel.setColumns(2);
			m_infoPanel.addField(createEnumField("priority",0));
			m_infoPanel.addField(createDTGField("eta",1));
			m_infoPanel.addField(createTextField("unit",2));
			m_infoPanel.addField(createDTGField("mta",3));
			m_infoPanel.addField(createEnumField("status",4));
			m_infoPanel.addField(createTextField("worktime",5));
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.BACK",m_infoPanel.getButtonSize()), ASG_RETURN);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.PRINT",m_infoPanel.getButtonSize()), ASG_PRINT);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT",m_infoPanel.getButtonSize()), ASG_CHANGE);
			m_infoPanel.addActionListener(m_listener);
		}
		return m_infoPanel;
	}

	private TogglePanel getRemarksPanel() {
		if(m_remarksPanel==null) {
			m_remarksPanel = new TogglePanel(m_wp.getBundleText("AsgInfoPanel_hdr_6.text"),false,false);
			m_remarksPanel.setPreferredExpandedHeight(150);
			m_remarksPanel.setContainer(getRemarksArea());
			m_remarksPanel.setScrollBarPolicies(
					TogglePanel.VERTICAL_SCROLLBAR_AS_NEEDED,
					TogglePanel.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return m_remarksPanel;
	}

	private JTextArea getRemarksArea() {
		if(m_remarksArea==null) {
			m_remarksArea = new JTextArea();
			m_remarksArea.setEditable(false);
			m_remarksArea.setBorder(BorderFactory.createEmptyBorder());
			m_remarksArea.setLineWrap(true);
			m_remarksArea.setWrapStyleWord(true);
			m_remarksArea.setRows(3);
		}
		return m_remarksArea;
	}

	private IField<?> createTextField(String name, int index) {
		IField<?> attr = new TextField(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,75,25,"");
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		return attr;
	}

	private IField<?> createEnumField(String name, int index) {
		IField<?> attr = new EnumField(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,75,25);
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		attr.setButtonVisible(false);
		return attr;
	}

	private IField<?> createDTGField(String name, int index) {
		IField<?> attr = new DTGField(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,75,25);
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		return attr;
	}

	private String hoursSince(Calendar t0, Calendar t1) {
		long seconds = (t1.getTimeInMillis() - t0.getTimeInMillis())/1000;
		return Utils.getTime((int)seconds);
	}

}
