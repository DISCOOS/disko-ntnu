package org.redcross.sar.wp.logistics;

import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.output.DiskoReport;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.unit.IDiskoWpUnit;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

/**
 *
 */
public class InfoPanelHandler implements IMsoUpdateListenerIf, ActionListener, ITickEventListenerIf
{
    private final static String EMPTY_PANEL_NAME = "EmptyPanel";
    private final static String UNIT_PANEL_NAME = "UnitPanel";
    private final static String ASSIGNMENT_PANEL_NAME = "AssignmentPanel";
    private final static String ASSIGNMENT_LIST_PANEL_NAME = "AssignmentList";
    private final static String ASG_RESULT = "AsgResult";
    private final static String ASG_RETURN = "AsgReturn";
    private final static String ASG_PRINT = "AsgPrint";
    private final static String ASG_CHANGE = "AsgChange";
    private final static String UNIT_PRINT = "UnitPrint";
    private final static String UNIT_CHANGE = "UnitChange";

    private final static int ASSIGNMENT_INFO_PANEL_TOP_ELEMENTS = 5;
    private final static int ASSIGNMENT_INFO_PANEL_CENTER_ELEMENTS = 1;
    private final static int ASSIGNMENT_INFO_PANEL_BUTTONS = 4;

    private final static int UNIT_INFO_PANEL_TOP_ELEMENTS = 4;
    private final static int UNIT_INFO_PANEL_CENTER_ELEMENTS = 2;
    private final static int UNIT_INFO_PANEL_BUTTONS = 2;

    private IDiskoWpLogistics m_wpModule;
    private JPanel m_infoPanel;
    private LogisticsInfoPanel m_unitInfoPanel;
    private LogisticsInfoPanel m_assignmentInfoPanel;
    private AssignmentScrollPanel m_unitAssignmentsPanel;

    private boolean m_shallReturnToList;

    private IUnitIf m_displayedUnit;
    private int m_displayedUnitSelection;

    private IAssignmentIf m_displayedAsssignment;
    private String m_displayedPanelName = "";

    private final AssignmentLabel.AssignmentLabelActionHandler m_assignmentLabelMouseListener;

    private static final long m_timeInterval = 60 * 1000; // once every minute.

    private long m_timeCounter;
    
    private DiskoReport m_report = null;

    public InfoPanelHandler(JPanel anInfoPanel, IDiskoWpLogistics aWpModule, AssignmentLabel.AssignmentLabelActionHandler anActionHandler)
    {
    	// prepare
        m_infoPanel = anInfoPanel;
        m_assignmentLabelMouseListener = anActionHandler;
        m_wpModule = aWpModule;
        m_report = aWpModule.getApplication().getDiskoReport();

        m_infoPanel.add(new JPanel(), EMPTY_PANEL_NAME);

        initUnitInfoPanel();
        initAssignmentInfoPanel();
        initAssignmentListPanel();
        showPanel(EMPTY_PANEL_NAME);

        aWpModule.getMsoEventManager().addClientUpdateListener(this);
        aWpModule.addTickEventListener(this);
    }

    private final EnumSet<IMsoManagerIf.MsoClassCode> myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT,
            IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT,
            IMsoManagerIf.MsoClassCode.CLASSCODE_PERSONNEL);

    public boolean hasInterestIn(IMsoObjectIf aMsoObject)
    {
        return myInterests.contains(aMsoObject.getMsoClassCode());
    }

    public void handleMsoUpdateEvent(MsoEvent.Update e)
    {
        if (ASSIGNMENT_PANEL_NAME.equals(m_displayedPanelName))
        {
            if (e.getSource() == m_displayedAsssignment)
            {
                if (e.isDeleteObjectEvent())
                {
                    m_displayedAsssignment = null;
                }
                renderAssignment();
            }
        } else if (ASSIGNMENT_LIST_PANEL_NAME.equals(m_displayedPanelName))
        {
            renderAssignmentList();
        } else if (UNIT_PANEL_NAME.equals(m_displayedPanelName))
        {
            if (e.getSource() == m_displayedUnit)
            {
                if (e.isDeleteObjectEvent())
                {
                    m_displayedUnit = null;
                }
                renderUnit();
            }
        }
    }


    public void setTimeCounter(long aCounter)
    {
        m_timeCounter = aCounter;
    }

    public long getTimeCounter()
    {
        return m_timeCounter;
    }

    public long getInterval()
    {
        return m_timeInterval;
    }

    /**
     * Handle tick event.
     * <p/>
     * Update GUI due to tick events. Shall only update GUI objects that are depending on current time.
     *
     * @param e The evet object, not used.
     */
    public void handleTick(TickEvent e)
    {
        ICmdPostIf cmdPost = m_wpModule.getMsoManager().getCmdPost();
        if (cmdPost == null)
        {
            return;
        }

        if (ASSIGNMENT_PANEL_NAME.equals(m_displayedPanelName))
        {
            renderAssignment();
        } else if (UNIT_PANEL_NAME.equals(m_displayedPanelName))
        {
            renderUnit();
        }
    }

    private void initUnitInfoPanel()
    {
        m_unitInfoPanel = new LogisticsInfoPanel(UNIT_INFO_PANEL_TOP_ELEMENTS, UNIT_INFO_PANEL_CENTER_ELEMENTS, UNIT_INFO_PANEL_BUTTONS);
        m_infoPanel.add(m_unitInfoPanel, UNIT_PANEL_NAME);
        m_unitInfoPanel.setHeaders(new String[]{m_wpModule.getBundleText("UnitInfoPanel_hdr_0.text"),
                m_wpModule.getBundleText("UnitInfoPanel_hdr_1.text"),
                m_wpModule.getBundleText("UnitInfoPanel_hdr_2.text"),
                m_wpModule.getBundleText("UnitInfoPanel_hdr_3.text"),
                m_wpModule.getBundleText("UnitInfoPanel_hdr_4.text"),
                m_wpModule.getBundleText("UnitInfoPanel_hdr_5.text")});

        m_unitInfoPanel.setButtons(
        		new String[]{"GENERAL.EDIT","GENERAL.PRINT"},
        		new String[]{UNIT_CHANGE, UNIT_PRINT}, this);
    }

    private void initAssignmentInfoPanel()
    {
        m_assignmentInfoPanel = new LogisticsInfoPanel(ASSIGNMENT_INFO_PANEL_TOP_ELEMENTS, ASSIGNMENT_INFO_PANEL_CENTER_ELEMENTS, ASSIGNMENT_INFO_PANEL_BUTTONS);
        m_infoPanel.add(m_assignmentInfoPanel, ASSIGNMENT_PANEL_NAME);
        m_assignmentInfoPanel.setHeaders(new String[]{m_wpModule.getBundleText("AsgInfoPanel_hdr_0.text"),
                m_wpModule.getBundleText("AsgInfoPanel_hdr_1.text"),
                m_wpModule.getBundleText("AsgInfoPanel_hdr_2.text"),
                m_wpModule.getBundleText("AsgInfoPanel_hdr_3.text"),
                m_wpModule.getBundleText("AsgInfoPanel_hdr_4.text"),
                m_wpModule.getBundleText("AsgInfoPanel_hdr_5.text")});
        m_assignmentInfoPanel.setButtons(
        		new String[]{"GENERAL.EDIT",          
                "GENERAL.PRINT",
                "GENERAL.EMPTY",
                "GENERAL.LEFT"}, 
                new String[]{ASG_CHANGE, ASG_PRINT, ASG_RESULT, ASG_RETURN}, this);
    }

    private void initAssignmentListPanel()
    {
        // Build up a scrollpane with room for assignment labels.
        JScrollPane scrollpane = new JScrollPane();
        m_unitAssignmentsPanel = new AssignmentScrollPanel(m_wpModule,scrollpane, new SpringLayout(), 5, 5, false, m_assignmentLabelMouseListener, false);
        m_unitAssignmentsPanel.setCols(1);
        JLabel hl = m_unitAssignmentsPanel.getHeaderLabel();
        hl.setHorizontalAlignment(SwingConstants.CENTER);
        hl.setPreferredSize(new Dimension(40, 40));
        m_infoPanel.add(scrollpane, ASSIGNMENT_LIST_PANEL_NAME);
    }

    void setAssignment(IAssignmentIf anAssignment, boolean shallReturnToList)
    {
        m_displayedAsssignment = anAssignment;
        m_shallReturnToList = shallReturnToList;
        renderAssignment();
        showPanel(ASSIGNMENT_PANEL_NAME);
    }

    private void renderAssignment()
    {
        if (m_displayedAsssignment != null)
        {
            m_assignmentInfoPanel.setTopText(0, MsoUtils.getAssignmentName(m_displayedAsssignment, 2));
            m_assignmentInfoPanel.setTopText(1, m_displayedAsssignment.getTypeText());
            IUnitIf unit = m_displayedAsssignment.getOwningUnit();
            if (unit != null)
            {
                m_assignmentInfoPanel.setTopText(2, unit.getUnitNumber());
            } else
            {
                m_assignmentInfoPanel.setTopText(2, "");
            }
            m_assignmentInfoPanel.setTopText(3, m_displayedAsssignment.getPriorityText());
            m_assignmentInfoPanel.setTopText(4, hoursSince(m_displayedAsssignment.getTimeAssigned()));
            m_assignmentInfoPanel.setTopText(5,m_displayedAsssignment.getRemarks());
        } else
        {
            m_assignmentInfoPanel.clearTopTexts();
            m_assignmentInfoPanel.clearCenterTexts();
        }
        m_assignmentInfoPanel.setButtonEnabled(0, m_displayedAsssignment != null);
        m_assignmentInfoPanel.setButtonEnabled(1, m_displayedAsssignment != null);
        m_assignmentInfoPanel.setButtonEnabled(2, m_displayedAsssignment != null && m_displayedAsssignment.hasBeenFinished());
        m_assignmentInfoPanel.setButtonVisible(3, m_shallReturnToList);
        m_assignmentInfoPanel.repaint();
    }

    private String hoursSince(Calendar aTime)
    {
        if (aTime != null)
        {
            Calendar now = Calendar.getInstance();
            long actTimeInMillis = now.getTimeInMillis() - aTime.getTimeInMillis();
            float actTimeInHours = actTimeInMillis / 3600000;
            int displayedTimeInHours = Math.round(actTimeInHours);
            return Integer.toString(displayedTimeInHours);
        }
        return " ";
    }

    void setUnitSelection(IUnitIf aUnit, int aSelectionIndex)
    {
        m_displayedUnit = aUnit;
        m_displayedUnitSelection = aSelectionIndex;
        setupUnitAssignmentPanel();
        renderAssignmentList();
        showPanel(ASSIGNMENT_LIST_PANEL_NAME);
    }

    private void setupUnitAssignmentPanel()
    {
        JLabel hl = m_unitAssignmentsPanel.getHeaderLabel();
        hl.setText(MessageFormat.format(m_wpModule.getBundleText("AsgListInfoPanel_hdr.text"),
                UnitTableModel.getSelectedAssignmentText(m_wpModule, m_displayedUnitSelection).toLowerCase(),
                m_displayedUnit.getUnitNumber()));
        m_unitAssignmentsPanel.setSelectedUnit(m_displayedUnit);
        m_unitAssignmentsPanel.setSelectedStatus(UnitTableModel.getSelectedAssignmentStatus(m_displayedUnitSelection));
    }

    void setSelectionTransferHandler(TransferHandler aTransferHandler)
    {
        m_unitAssignmentsPanel.setTransferHandler(aTransferHandler);
    }

    private void renderAssignmentList()
    {
        Collection<IAssignmentIf> assigments = UnitTableModel.getSelectedAssignments(m_displayedUnit, m_displayedUnitSelection);
        m_unitAssignmentsPanel.setAssignmentList(assigments);
        m_unitAssignmentsPanel.renderPanel();
    }

    void setUnit(IUnitIf aUnit)
    {
        m_displayedUnit = aUnit;
        renderUnit();
        showPanel(UNIT_PANEL_NAME);
    }

    void renderUnit()
    {
        if (m_displayedUnit != null)
        {
            m_unitInfoPanel.setTopText(0, m_displayedUnit.getUnitNumber() + " " + m_displayedUnit.getStatusText());
            m_unitInfoPanel.setTopText(1, m_displayedUnit.getCallSign());
            IAssignmentIf activeAsg = m_displayedUnit.getActiveAssignment();
            if (activeAsg != null)
            {
                m_unitInfoPanel.setTopText(2, Integer.toString(activeAsg.getNumber()));
                m_unitInfoPanel.setTopText(3, hoursSince(activeAsg.getTimeAssigned()));
            } else
            {
                m_unitInfoPanel.setTopText(2, "");
                m_unitInfoPanel.setTopText(3, "");
            }
            IPersonnelIf person = m_displayedUnit.getUnitLeader();
            if (person != null)
            {
                m_unitInfoPanel.setCenterText(0, person.getFirstname() + " " + person.getLastname());
            } else
            {
                m_unitInfoPanel.setCenterText(0, " ");
            }

            Vector<String> memberNames = new Vector<String>();
            for (IPersonnelIf p : m_displayedUnit.getUnitPersonnelItems())
            {
                memberNames.add(p.getFirstname() + " " + p.getLastname());
            }
            m_unitInfoPanel.setCenterText(1, memberNames);
            m_unitInfoPanel.setButtonEnabled(0, true);
            m_unitInfoPanel.setButtonEnabled(1, true);
        } else
        {
            m_unitInfoPanel.clearTopTexts();
            m_unitInfoPanel.clearCenterTexts();
        }
        m_unitInfoPanel.setButtonEnabled(0, m_displayedUnit != null);
        m_unitInfoPanel.setButtonEnabled(1, m_displayedUnit != null);
        m_unitInfoPanel.repaint();
    }

    private void showPanel(String aPanelName)
    {
        m_displayedPanelName = aPanelName;
        CardLayout cl = (CardLayout) m_infoPanel.getLayout();
        cl.show(m_infoPanel, aPanelName);
    }

    public void actionPerformed(ActionEvent e)
    {
        String command = e.getActionCommand();
        if (command == null || command.length() == 0)
        {
            return;
        }
        if (command.equalsIgnoreCase(UNIT_CHANGE))
        {
            //System.out.println("Trykk 1: " + command + m_displayedUnit.getUnitNumber());
            IDiskoRole role = m_wpModule.getDiskoRole();
            String id = role.getName() + "UNITS";
            IDiskoWpModule calledModule = role.getDiskoWpModule(id);
            if (calledModule != null && calledModule instanceof IDiskoWpUnit)
            {
                IDiskoWpUnit calledUnitModule = (IDiskoWpUnit) calledModule;
                role.selectDiskoWpModule(calledModule);
                calledModule.setCallingWp(m_wpModule.getName());
                calledUnitModule.setOverviewPanel(1);
                calledUnitModule.setUnit(m_displayedUnit);
                calledUnitModule.setLeftView(IDiskoWpUnit.UNIT_VIEW_ID);

            } else
            {
                m_wpModule.showWarning("ChangeWPNotFound_Tactics.text");
            }
        } else if (command.equalsIgnoreCase(UNIT_PRINT))
        {
        	// has unit to print out?
        	if(m_displayedUnit!=null) {
        		m_report.printUnitLog(m_displayedUnit);
        	}
        } else if (command.equalsIgnoreCase(ASG_RESULT))
        {
            System.out.println("Trykk 3: " + command + m_displayedAsssignment.getNumber());
        } else if (command.equalsIgnoreCase(ASG_RETURN))
        {
            System.out.println("Trykk 4: " + command + m_displayedAsssignment.getNumber());
            renderAssignmentList();
            showPanel(ASSIGNMENT_LIST_PANEL_NAME);
        } else if (command.equalsIgnoreCase(ASG_PRINT))
        {
        	// has assignment to print?
        	if(m_displayedAsssignment!=null) {
	        	List<IAssignmentIf> assignments = new ArrayList<IAssignmentIf>(1);
	        	assignments.add(m_displayedAsssignment);
	            m_report.printAssignments(assignments);
        	}
        } else if (command.equalsIgnoreCase(ASG_CHANGE))
        {
            IDiskoRole role = m_wpModule.getDiskoRole();
            String id = role.getName() + "TACTICS";
            IDiskoWpModule calledModule = role.getDiskoWpModule(id);
            if (calledModule != null)
            {
                role.selectDiskoWpModule(calledModule);
                calledModule.setCallingWp(m_wpModule.getName());
                try
                {
                	IDiskoMap map = m_wpModule.getMap();
                	map.suspendNotify();
                	map.setSelected(m_displayedAsssignment, true);
                	// this will refresh all layers
                	map.zoomToMsoObject(m_displayedAsssignment);
                	map.resumeNotify();
                }
                catch (AutomationException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (IOException e1)
                {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else
            {
                m_wpModule.showWarning("ChangeWPNotFound_Tactics.text");
            }
        }
    }
}
