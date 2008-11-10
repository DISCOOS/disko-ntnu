package org.redcross.sar.wp.logistics;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.TransferHandler;

import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.panel.HeaderPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.logistics.AssignmentLabel.AssignmentLabelActionHandler;
import org.redcross.sar.wp.unit.IDiskoWpUnit;

public class InfoPanelHandler implements IMsoUpdateListenerIf, ActionListener, ITickEventListenerIf
{
    private final static String EMPTY_PANEL_NAME = "EmptyPanel";
    private final static String UNIT_PANEL_NAME = "UnitPanel";
    private final static String ASSIGNMENT_PANEL_NAME = "AssignmentPanel";
    private final static String ASSIGNMENT_SHOW_ASSIGNMENT_LIST_PANEL_NAME = "AssignmentList";
    private final static String ASG_RESULT = "AsgResult";
    private final static String ASG_RETURN = "AsgReturn";
    private final static String ASG_PRINT = "AsgPrint";
    private final static String ASG_CHANGE = "AsgChange";
    private final static String UNIT_PRINT = "UnitPrint";
    private final static String UNIT_CHANGE = "UnitChange";

    private IDiskoWpLogistics m_wpModule;

    private JPanel m_infoPanel;
    private UnitInfoPanel m_unitInfoPanel;
    private AssignmentInfoPanel m_assignmentInfoPanel;
    private AssignmentTilesPanel m_unitAssignmentsPanel;

    private boolean m_shallReturnToList;

    private IUnitIf m_displayedUnit;
    private int m_displayedUnitSelection;

    private IAssignmentIf m_displayedAssignment;
    private String m_displayedPanelName = "";

    private final AssignmentLabelActionHandler m_assignmentLabelMouseListener;

    private static final long m_timeInterval = 60 * 1000; // once every minute.

    private long m_timeCounter;

    private DiskoReportManager m_report = null;

    public InfoPanelHandler(JPanel anInfoPanel, IDiskoWpLogistics aWpModule, AssignmentLabelActionHandler anActionHandler)
    {
    	// prepare
        m_wpModule = aWpModule;
        m_infoPanel = anInfoPanel;
        m_assignmentLabelMouseListener = anActionHandler;
        m_report = aWpModule.getApplication().getReportManager();

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

	public EnumSet<MsoClassCode> getInterests() {
		return myInterests;
	}

	public void handleMsoUpdateEvent(MsoEvent.UpdateList events) {

		// loop over all events
		for(MsoEvent.Update e : events.getEvents(myInterests))
		{
			// consume loopback updates
			if(!e.isLoopback())
			{
		        if (ASSIGNMENT_PANEL_NAME.equals(m_displayedPanelName))
		        {
		            if (e.getSource() == m_displayedAssignment)
		            {
		                if (e.isClearAllEvent() || e.isDeleteObjectEvent())
		                {
		                    m_displayedAssignment = null;
		                }
		                renderAssignment();
		            }
		        } else if (ASSIGNMENT_SHOW_ASSIGNMENT_LIST_PANEL_NAME.equals(m_displayedPanelName))
		        {
		            renderAssignmentList();
		        } else if (UNIT_PANEL_NAME.equals(m_displayedPanelName))
		        {
		            if (e.getSource() == m_displayedUnit)
		            {
		                if (e.isClearAllEvent() || e.isDeleteObjectEvent())
		                {
		                    m_displayedUnit = null;
		                }
		                renderUnit();
		            }
		        }
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
		if(m_wpModule.getMsoManager().operationExists()) {
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
    }

    private void initUnitInfoPanel()
    {
        m_unitInfoPanel = new UnitInfoPanel(m_wpModule,this);
        m_infoPanel.add(m_unitInfoPanel, UNIT_PANEL_NAME);
    }

    private void initAssignmentInfoPanel()
    {
        m_assignmentInfoPanel = new AssignmentInfoPanel(m_wpModule,this);
        m_infoPanel.add(m_assignmentInfoPanel, ASSIGNMENT_PANEL_NAME);
    }

    private void initAssignmentListPanel()
    {
        // Build up a scrollpane with room for assignment labels.
        m_unitAssignmentsPanel = new AssignmentTilesPanel(m_wpModule, new SpringLayout(), 5, 5, false, m_assignmentLabelMouseListener, false);
        m_unitAssignmentsPanel.setCols(1);
        m_unitAssignmentsPanel.getHeaderPanel().setPreferredSize(new Dimension(40, 40));
        m_infoPanel.add(m_unitAssignmentsPanel, ASSIGNMENT_SHOW_ASSIGNMENT_LIST_PANEL_NAME);
    }

    void setAssignment(IAssignmentIf anAssignment, boolean shallReturnToList)
    {
        m_displayedAssignment = anAssignment;
        m_shallReturnToList = shallReturnToList;
        renderAssignment();
        showPanel(ASSIGNMENT_PANEL_NAME);
    }

    private void renderAssignment()
    {
    	m_assignmentInfoPanel.setAssignment(m_displayedAssignment);
    	m_assignmentInfoPanel.setBackButtonVisible(m_shallReturnToList);
    }

    void setUnitAssignmentSelection(IUnitIf aUnit, int aSelectionIndex)
    {
        m_displayedUnit = aUnit;
        m_displayedUnitSelection = aSelectionIndex;
        setupUnitAssignmentPanel();
        renderAssignmentList();
        showPanel(ASSIGNMENT_SHOW_ASSIGNMENT_LIST_PANEL_NAME);
    }

    private void setupUnitAssignmentPanel()
    {
        HeaderPanel header = m_unitAssignmentsPanel.getHeaderPanel();
        header.setCaptionText("<html><b>"+MessageFormat.format(m_wpModule.getBundleText("AsgListInfoPanel_hdr.text")+"<html><b>",
                UnitTableModel.getSelectedAssignmentText(m_wpModule, m_displayedUnitSelection).toLowerCase(),
                MsoUtils.getUnitName(m_displayedUnit,false)));
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

    void setUnit(IUnitIf aUnit, boolean showUnit)
    {
        m_displayedUnit = aUnit;
        if(showUnit) {
	        renderUnit();
	        showPanel(UNIT_PANEL_NAME);
        }
    }

    void renderUnit()
    {
    	m_unitInfoPanel.setUnit(m_displayedUnit);
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
            IDiskoWpModule calledModule = role.getDiskoWpModule("UNIT");
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
            System.out.println("Trykk 3: " + command + m_displayedAssignment.getNumber());
        } else if (command.equalsIgnoreCase(ASG_RETURN))
        {
            //System.out.println("Trykk 4: " + command + m_displayedAssignment.getNumber());
            renderAssignmentList();
            showPanel(ASSIGNMENT_SHOW_ASSIGNMENT_LIST_PANEL_NAME);
        } else if (command.equalsIgnoreCase(ASG_PRINT))
        {
        	// has assignment to print?
        	if(m_displayedAssignment!=null) {
	        	List<IAssignmentIf> assignments = new ArrayList<IAssignmentIf>(1);
	        	assignments.add(m_displayedAssignment);
	            m_report.printAssignments(assignments);
        	}
        } else if (command.equalsIgnoreCase(ASG_CHANGE))
        {
            IDiskoRole role = m_wpModule.getDiskoRole();
            IDiskoWpModule calledModule = role.getDiskoWpModule("TACTICS");
            if (calledModule != null)
            {
                role.selectDiskoWpModule(calledModule);
                calledModule.setCallingWp(m_wpModule.getName());
                try
                {
                	IDiskoMap map = calledModule.getMap();
                	map.suspendNotify();
                	map.clearSelected();
                	map.setSelected(m_displayedAssignment,true);
                	map.zoomToMsoObject(m_displayedAssignment);
                	map.flashMsoObject(m_displayedAssignment);
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
