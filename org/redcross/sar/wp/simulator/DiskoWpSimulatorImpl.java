package org.redcross.sar.wp.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.redcross.sar.IDiskoRole;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.IWorkLoop.LoopState;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import com.esri.arcgis.interop.AutomationException;

/**
 *
 */
public class DiskoWpSimulatorImpl extends AbstractDiskoWpModule implements IDiskoWpSimulator
{

	private static final int UPDATE_TIME_DELAY = 1000;	// updates every second
	private static final String CONTROL_CAPTION = "Simulator - <b>Kontrollpanel</b> (%s)";

	private long m_timeCounter = 0;

	private JSplitPane m_splitPane;
	private MapPanel m_mapPanel;
	private JPanel m_simulatorPanel;
	private TogglePanel m_controlPanel;
	private FieldsPanel m_simAttribsPanel;
	private DTGField m_startedTimeAttr;
	private TextLineField m_effortTimeAttr;
	private TextLineField m_avgSimTimeAttr;
	private TextLineField m_maxSimTimeAttr;
	private TextLineField m_utilSimTimeAttr;
	private JButton m_resumeButton;
	private JButton m_suspendButton;
	private JTabbedPane m_tabbedPane;
	private UnitsPanel m_activeUnitsPanel;
	private UnitsPanel m_archivedUnitsPanel;
	private AssignmentsPanel m_activeAssignmentsPanel;
	private AssignmentsPanel m_archivedAssignmentsPanel;

	private Simulator m_simulator;

    public DiskoWpSimulatorImpl() throws Exception
    {
		// forward
		super(getWpInterests(),getMapLayers());

		// initialize GUI
		initialize();
	}

	private static EnumSet<MsoClassCode> getWpInterests() {
		EnumSet<MsoClassCode> myInterests = EnumSet.of(MsoClassCode.CLASSCODE_OPERATION);
    	myInterests.add(MsoClassCode.CLASSCODE_AREA);
    	myInterests.add(MsoClassCode.CLASSCODE_UNIT);
    	myInterests.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static List<Enum<?>> getMapLayers() {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		list.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return list;
	}

    private void initialize()
    {

		try {
			
			// get properties
			assignWpBundle(IDiskoWpSimulator.class);

			// forward
			installMap();

			// set layout component
			layoutComponent(getSplitPane());

			// listen to work in this
			addWorkFlowListener(new IWorkFlowListener() {

				@Override
				public void onFlowPerformed(WorkFlowEvent e) {
					// auto update?
					if(e.isFinish())
						commit();
					else if(e.isCancel())
						rollback();
				}

			});

			// start update task
			registerUpdateTask();

			// make all layers unselectable
			getMap().getMsoLayerModel().setAllSelectable(false);


		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}


    }

    /**
     * Checking if any tasks have reached their alert time, and give the appropriate role a warning
     */
    private void registerUpdateTask()
    {
        this.addTickEventListener(new ITickEventListenerIf()
        {
            public long getInterval()
            {
                return UPDATE_TIME_DELAY;
            }

            public void setTimeCounter(long counter)
            {
                m_timeCounter = counter;
            }

            public long getTimeCounter()
            {
                return m_timeCounter;
            }

            @SuppressWarnings("unchecked")
            public void handleTick(TickEvent e)
            {
            	update(false);
            }
        });
    }

	@Override
	public void afterOperationChange() {
    	super.afterOperationChange();
		update(true);
	}

	private void update(boolean init) {
		if(!(isActive() && getMsoModel().getMsoManager().operationExists())) return;
		//SimpleDateFormat f = new SimpleDateFormat();
		if(init || true) {
			/*Calendar start = getMsoModel().getMsoManager().getOperation().getCreatedTime();
			String dtg = DTG.CalToDTG(start);
			dtg = f.format(start.getTime());
			Calendar c = getStartedTimeAttr().getValue();
			int seconds = (int)(c.getTimeInMillis() - c.getTimeInMillis())/1000;*/
			getStartedTimeAttr().setValue(getMsoModel().getMsoManager().getOperation().getCreatedTime());
		}
		Calendar c = getStartedTimeAttr().getValue();
		if(c!=null) {
			/*Calendar now = Calendar.getInstance();
			now.setTimeInMillis(System.currentTimeMillis());
			//System.out.println("START:"+f.format(c.getTime()));
			//System.out.println("NOW:"+f.format(now.getTime()));
			int seconds = (int)(now.getTimeInMillis() - c.getTimeInMillis())/1000;*/
			int seconds = (int)(System.currentTimeMillis() - c.getTimeInMillis())/1000;

			getEffortTimeAttr().setValue(Utils.getTime(seconds));
		}
		// get decision support statistics
		if(m_simulator!=null && !m_simulator.isLoopState(LoopState.PENDING)) {
			// get work loop
			IWorkLoop loop = m_simulator.getWorkLoop();
			getAvgSimTimeAttr().setValue(loop.getAverageWorkTime() + " ms");
			getMaxSimTimeAttr().setValue(loop.getMaxWorkTime() + " ms");
			getUtilSimTimeAttr().setValue(Math.round(loop.getUtilization()*100) + " %");
		}
	}

    public String getCaption() {
		return getBundleText("SIMULATOR");
	}

    private JSplitPane getSplitPane()
    {
        if (m_splitPane == null)
        {
        	m_splitPane = new JSplitPane();
        	m_splitPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        	m_splitPane.setLeftComponent(getMapPanel());
        	m_splitPane.setRightComponent(getSimulatorPanel());
        }
        return m_splitPane;
    }

    private MapPanel getMapPanel()
    {
        if (m_mapPanel == null)
        {
        	m_mapPanel = new MapPanel(getMap());
        	m_mapPanel.setNorthBarVisible(true);
        	m_mapPanel.setSouthBarVisible(true);
        	m_mapPanel.setBorder(UIFactory.createBorder());
			m_mapPanel.setMinimumSize(new Dimension(350,350));
			m_mapPanel.setPreferredSize(new Dimension(450,350));

        }
        return m_mapPanel;
    }

    private JPanel getSimulatorPanel()
    {
        if (m_simulatorPanel == null)
        {
        	m_simulatorPanel = new JPanel(new BorderLayout(5,5));
        	m_simulatorPanel.add(getControlPanel(),BorderLayout.NORTH);
        	m_simulatorPanel.add(getTabbedPane(),BorderLayout.CENTER);

        }
        return m_simulatorPanel;
    }

    private TogglePanel getControlPanel()
    {
        if (m_controlPanel == null)
        {
        	m_controlPanel = new TogglePanel(String.format(CONTROL_CAPTION,"Pause"),false,false,ButtonSize.SMALL);
        	m_controlPanel.setPreferredSize(new Dimension(400,210));
        	m_controlPanel.collapse();
        	m_controlPanel.addButton(getResumeButton(), "resume");
        	m_controlPanel.addButton(getSuspendButton(), "suspend");
        	m_controlPanel.addActionListener(new ActionListener() {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    				// prepare
    				String cmd = e.getActionCommand();
    				// translate
    				if("resume".equals(cmd)) {
    					resume();
    				}
    				else if("suspend".equals(cmd)) {
    					suspend();
    				}
    			}

    		});
        	// add attributes
        	m_controlPanel.setContainer(getSimAttribsPanel());

        }
        return m_controlPanel;
    }

    private Simulator getSimulator() {
    	if(m_simulator==null) {
    		try {
				m_simulator = new Simulator();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return m_simulator;
    }

    private void resume() {
    	// schedule?
    	if(getSimulator().resume()) {
    		getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Spiller"));
    	}
    }

    private void suspend() {
    	if(getSimulator().suspend()) {
    		getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Pause"));
    	}
    }

    private JButton getResumeButton() {
		if(m_resumeButton==null) {
			m_resumeButton = DiskoButtonFactory.createButton("GENERAL.PLAY", ButtonSize.SMALL);
		}
		return m_resumeButton;
	}

	private JButton getSuspendButton() {
		if(m_suspendButton==null) {
			m_suspendButton = DiskoButtonFactory.createButton("GENERAL.PAUSE", ButtonSize.SMALL);
		}
		return m_suspendButton;
	}

	private FieldsPanel getSimAttribsPanel()
    {
        if (m_simAttribsPanel == null)
        {
        	m_simAttribsPanel = new FieldsPanel("","",false,false);
        	m_simAttribsPanel.setHeaderVisible(false);
        	m_simAttribsPanel.setBorderVisible(false);
        	m_simAttribsPanel.setNotScrollBars();
        	m_simAttribsPanel.addField(getStartedTimeAttr());
        	m_simAttribsPanel.addField(getEffortTimeAttr());
        	m_simAttribsPanel.addField(getAvgSimTimeAttr());
        	m_simAttribsPanel.addField(getMaxSimTimeAttr());
        	m_simAttribsPanel.addField(getUtilSimTimeAttr());
        }
        return m_simAttribsPanel;
    }

	private DTGField getStartedTimeAttr()
    {
        if (m_startedTimeAttr == null)
        {
        	m_startedTimeAttr = new DTGField("scheduleedtime","Startet kl",
        			false, 130, 25, Calendar.getInstance());

        }
        return m_startedTimeAttr;
    }

	private TextLineField getEffortTimeAttr()
    {
        if (m_effortTimeAttr == null)
        {
        	m_effortTimeAttr = new TextLineField("efforttime","Innsatstid", false, 130, 25, 0);

        }
        return m_effortTimeAttr;
    }

	private TextLineField getAvgSimTimeAttr()
    {
        if (m_avgSimTimeAttr == null)
        {
        	m_avgSimTimeAttr = new TextLineField("avgtime","Arbeidstid (gj.sn)", false, 130, 25, 0);

        }
        return m_avgSimTimeAttr;
    }

	private TextLineField getMaxSimTimeAttr()
    {
        if (m_maxSimTimeAttr == null)
        {
        	m_maxSimTimeAttr = new TextLineField("maxtime","Arbeidstid (max)", false, 130, 25, 0);

        }
        return m_maxSimTimeAttr;
    }

	private TextLineField getUtilSimTimeAttr()
    {
        if (m_utilSimTimeAttr == null)
        {
        	m_utilSimTimeAttr = new TextLineField("utiltime","Arbeidstid (forbruk)", false, 130, 25, 0);

        }
        return m_utilSimTimeAttr;
    }

	/**
	 * This method initializes m_tabbedPane
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getTabbedPane() {
		if (m_tabbedPane == null) {
			m_tabbedPane = new JTabbedPane();
			Dimension dim = new Dimension(350,350);
			m_tabbedPane.setMinimumSize(dim);
			m_tabbedPane.setPreferredSize(dim);
			m_tabbedPane.addTab("Aktive",
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"),
					getActiveAssignmentsPanel(), null);
			m_tabbedPane.addTab("Utførte",
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"),
					getArchivedAssignmentsPanel(), null);
			m_tabbedPane.addTab("Aktive",
					DiskoIconFactory.getIcon("GENERAL.UNIT","32x32"),
					getActiveUnitsPanel(), null);
			m_tabbedPane.addTab("Oppløste",
					DiskoIconFactory.getIcon("GENERAL.UNIT","32x32"),
					getArchivedUnitsPanel(), null);
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);


		}
		return m_tabbedPane;

	}

	private AssignmentsPanel getActiveAssignmentsPanel()
    {
        if (m_activeAssignmentsPanel == null)
        {
        	m_activeAssignmentsPanel = new AssignmentsPanel(false);
        }
        return m_activeAssignmentsPanel;
    }

	private AssignmentsPanel getArchivedAssignmentsPanel()
    {
        if (m_archivedAssignmentsPanel == null)
        {
        	m_archivedAssignmentsPanel = new AssignmentsPanel(true);
        }
        return m_archivedAssignmentsPanel;
    }

	private UnitsPanel getActiveUnitsPanel()
    {
        if (m_activeUnitsPanel == null)
        {
        	m_activeUnitsPanel = new UnitsPanel(false);
        	m_activeUnitsPanel.addWorkFlowListener(this);
        }
        return m_activeUnitsPanel;
    }

	private UnitsPanel getArchivedUnitsPanel()
    {
        if (m_archivedUnitsPanel == null)
        {
        	m_archivedUnitsPanel = new UnitsPanel(true);
        	m_archivedUnitsPanel.addWorkFlowListener(this);
        }
        return m_archivedUnitsPanel;
    }

	public void activate(IDiskoRole role) {

		// forward
		super.activate(role);

		// setup of navbar needed?
		if(isNavMenuSetupNeeded()) {
			// get set of tools visible for this wp
			setupNavMenu(getDefaultNavBarButtons(),true);
		}

		// forward
		update(true);

	}

	private List<Enum<?>> getDefaultNavBarButtons() {
		List<Enum<?>> myButtons = Utils.getListNoneOf(MapToolType.class);
		myButtons.add(MapToolType.ZOOM_IN_TOOL);
		myButtons.add(MapToolType.ZOOM_OUT_TOOL);
		myButtons.add(MapToolType.PAN_TOOL);
		myButtons.add(MapToolType.SELECT_TOOL);
		myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
		myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
		myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
		myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
		myButtons.add(MapCommandType.SCALE_COMMAND);
		myButtons.add(MapCommandType.TOC_COMMAND);
		myButtons.add(MapCommandType.GOTO_COMMAND);
		return myButtons;
	}

    @Override
	public boolean commit() {
		// TODO Auto-generated method stub
		return doCommitWork();
	}

	@Override
	public boolean rollback() {
		// TODO Auto-generated method stub
		return doRollbackWork();
	}


	private boolean doCommitWork() {
		try {
			// forward work
			WorkPool.getInstance().schedule(new SimulatorWork(1));
			// do work
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean doRollbackWork() {
		try {
			WorkPool.getInstance().schedule(new SimulatorWork(2));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private class SimulatorWork extends ModuleWork {

		private int m_task = 0;

		/**
		 * Constructor
		 *
		 * @param task
		 */
		SimulatorWork(int task) throws Exception {
			// forward
			super();
			// prepare
			m_task = task;
		}

		@Override
		public Boolean doWork() {
			try {
				// dispatch task
				switch(m_task) {
				case 1: commit(); return true;
				case 2: rollback(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}


		@Override
		public void beforeDone() {

			try {
				// dispatch task
				switch(m_task) {
				case 1: fireOnWorkCommit(); break;
				case 2: fireOnWorkRollback(); break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void commit() {
			try{
				getMsoModel().commit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

		private void rollback() {
			try{
				getMsoModel().rollback();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

	}
}
