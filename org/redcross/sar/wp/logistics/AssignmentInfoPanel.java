package org.redcross.sar.wp.logistics;

import java.awt.TextArea;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.attribute.EnumAttribute;
import org.redcross.sar.gui.attribute.IDiskoAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;

public class AssignmentInfoPanel extends JPanel
{
	private final static long serialVersionUID = 1L;

	private final static String ASG_RETURN = "AsgReturn";
	private final static String ASG_PRINT = "AsgPrint";
	private final static String ASG_CHANGE = "AsgChange";

	private AttributesPanel m_infoPanel;
	private BasePanel m_remarksPanel;
	private TextArea m_remarksArea;

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
			getInfoPanel().setValue("eta", assignment.getTimeEstimatedFinished());
			IUnitIf unit = assignment.getOwningUnit();
			getInfoPanel().setValue("unit", unit!=null ? MsoUtils.getUnitName(unit, false) : "");
			Calendar t = assignment.getTime(AssignmentStatus.FINISHED);
			if(t==null) t = assignment.getTime(AssignmentStatus.ABORTED);
			getInfoPanel().setValue("mta", t);
			getInfoPanel().setValue("status", assignment.getStatus());
			Calendar t0 = assignment.getTime(AssignmentStatus.EXECUTING);
			getInfoPanel().setValue("worktime", t0!=null && t!=null ? hoursSince(t,t0) : "00:00:00");
		} else
		{
			getInfoPanel().setCaptionText("Velg et oppdrag");
			getInfoPanel().setValue("priority", null);
			getInfoPanel().setValue("eta", null);
			getInfoPanel().setValue("unit", "");
			getInfoPanel().setValue("mta", null);
			getInfoPanel().setValue("status", null);
			getInfoPanel().setValue("worktime", "00:00:00");
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

	private AttributesPanel getInfoPanel() {
		if(m_infoPanel==null) {
			m_infoPanel = new AttributesPanel("","Ingen egenskaper",false,false);
			m_infoPanel.setColumns(2);
			m_infoPanel.addAttribute(createEnumAttribute("priority",0));
			m_infoPanel.addAttribute(createDTGAttribute("eta",1));
			m_infoPanel.addAttribute(createTextFieldAttribute("unit",2));
			m_infoPanel.addAttribute(createDTGAttribute("mta",3));
			m_infoPanel.addAttribute(createEnumAttribute("status",4));
			m_infoPanel.addAttribute(createTextFieldAttribute("worktime",5));	
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.BACK",m_infoPanel.getButtonSize()), ASG_RETURN);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.PRINT",m_infoPanel.getButtonSize()), ASG_PRINT);
			m_infoPanel.addButton(DiskoButtonFactory.createButton("GENERAL.EDIT",m_infoPanel.getButtonSize()), ASG_CHANGE);
			m_infoPanel.addActionListener(m_listener);
		}
		return m_infoPanel;
	}

	private BasePanel getRemarksPanel() {
		if(m_remarksPanel==null) {
			m_remarksPanel = new BasePanel(m_wp.getBundleText("AsgInfoPanel_hdr_6.text"));
			m_remarksPanel.setBodyComponent(getRemarksArea());
			m_remarksPanel.setFitBodyOnResize(true);
			m_remarksPanel.setScrollBarPolicies(
					BasePanel.VERTICAL_SCROLLBAR_AS_NEEDED, 
					BasePanel.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return m_remarksPanel;
	}

	private TextArea getRemarksArea() {
		if(m_remarksArea==null) {
			m_remarksArea = new TextArea();
			m_remarksArea.setEditable(false);
			m_remarksArea.setRows(3);
		}
		return m_remarksArea;
	}

	private IDiskoAttribute createTextFieldAttribute(String name, int index) {
		IDiskoAttribute attr = new TextFieldAttribute(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,100,25,"");
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		return attr;
	}

	private IDiskoAttribute createEnumAttribute(String name, int index) {
		IDiskoAttribute attr = new EnumAttribute(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,100,25);
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		attr.setButtonVisible(false);
		return attr;
	}

	private IDiskoAttribute createDTGAttribute(String name, int index) {
		IDiskoAttribute attr = new DTGAttribute(name,
				m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".text"),false,100,25);
		attr.setToolTipText(m_wp.getBundleText("AsgInfoPanel_hdr_"+index+".tooltip"));
		return attr;
	}

	private String hoursSince(Calendar t0, Calendar t1) {
		long seconds = (t1.getTimeInMillis() - t0.getTimeInMillis())/1000;
		return Utils.getTime((int)seconds);
	}

}
