package org.redcross.sar.wp.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.AttributesPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.wp.AbstractDiskoWpModule;

/**
 *
 */
public class DiskoWpSimulatorImpl extends AbstractDiskoWpModule implements IDiskoWpSimulator
{
	
	private static final int UPDATE_TIME_DELAY = 1000;	// updates every second
	private static final String CONTROL_CAPTION = "<html>Simulator - <b>Kontrollpanel</b> (%s)</html>";

	private long m_timeCounter = 0;
	
	private JSplitPane m_splitPane = null;
	private MapPanel m_mapPanel = null;
	private JPanel m_simulatorPanel = null;
	private BasePanel m_controlPanel = null;
	private AttributesPanel m_simAttribsPanel = null;
	private DTGAttribute m_startedTimeAttr = null;
	private TextFieldAttribute m_effortTimeAttr = null;	
	private TextFieldAttribute m_avgSimTimeAttr = null;	
	private TextFieldAttribute m_maxSimTimeAttr = null;	
	private TextFieldAttribute m_utilSimTimeAttr = null;	
	private JButton m_resumeButton = null;
	private JButton m_suspendButton = null;
	private JTabbedPane m_tabbedPane = null;
	private UnitsPanel m_unitsPanel = null;
	private AssignmentsPanel m_assignmentsPanel = null;
	
	private Simulator m_simulator = null;
	private DiskoWorkPool m_workPool = null;
	
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
    	myInterests.add(MsoClassCode.CLASSCODE_ROUTE);
    	myInterests.add(MsoClassCode.CLASSCODE_POI);
    	myInterests.add(MsoClassCode.CLASSCODE_SEARCHAREA);
    	myInterests.add(MsoClassCode.CLASSCODE_OPERATIONAREA);
    	myInterests.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return myLayers;
	}
	
    private void initialize()
    {
		// get properties
		assignWpBundle(IDiskoWpSimulator.class);
		
		// forward
		installMap();
	
		// set layout component
		layoutComponent(getSplitPane());
		
		// listen to work in this
		addDiskoWorkListener(new IDiskoWorkListener() {

			@Override
			public void onWorkPerformed(DiskoWorkEvent e) {
				// auto update?
				if(e.isFinish()) 
					commit();
				else if(e.isCancel()) 
					rollback();
			}
			
		});
		
		// start update task
		registerUpdateTask();
		
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
		update(true);
	}

	private void update(boolean init) {
		if(!isActive()) return;
		if(init) {
			getStartedTimeAttr().setValue(getMsoModel().getMsoManager().getOperation().getCreationTime());
		}
		Calendar c = getStartedTimeAttr().getValue();
		if(c!=null) {
			int seconds = (int)(System.currentTimeMillis() - getStartedTimeAttr().getValue().getTimeInMillis())/1000;
			getEffortTimeAttr().setValue(Utils.getTime(seconds));
		}		
		// get simulator statistics?
		if(getWorkPool().isWorking(getSimulator())) {
			getAvgSimTimeAttr().setValue(getSimulator().getAverageWorkTime() + " ms");
			getMaxSimTimeAttr().setValue(getSimulator().getMaxWorkTime() + " ms");
			getUtilSimTimeAttr().setValue(Math.round(getSimulator().getUtilization()*100) + " %");
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
			Dimension dim = new Dimension(600,350);
			m_mapPanel.setMinimumSize(dim);
			m_mapPanel.setPreferredSize(dim);
        	
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
    
    private BasePanel getControlPanel()
    {
        if (m_controlPanel == null)
        {
        	m_controlPanel = new BasePanel(String.format(CONTROL_CAPTION,"Pause"));
        	m_controlPanel.setPreferredSize(new Dimension(400,210));
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
        	m_controlPanel.setBodyComponent(getSimAttribsPanel());
        	
        }        
        return m_controlPanel;
    }
    
       
    
    private DiskoWorkPool getWorkPool() {
    	if(m_workPool==null) {
    		try {
    			m_workPool = DiskoWorkPool.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}
    	return m_workPool;
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
    	if(!getWorkPool().containsWork(getSimulator())) {
    		getWorkPool().schedule(getSimulator());
    	}
    	else {
    		getWorkPool().resume(getSimulator());
    	}   
    	getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Spiller"));  	
    }
        
    private void suspend() {
    	// schedule?
    	if(getWorkPool().containsWork(getSimulator())) {
    		getWorkPool().stop(getSimulator());
    	}
    	getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Pause"));    	
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
	
	private AttributesPanel getSimAttribsPanel()
    {
        if (m_simAttribsPanel == null)
        {
        	m_simAttribsPanel = new AttributesPanel("","",false,false);
        	m_simAttribsPanel.setHeaderVisible(false);
        	m_simAttribsPanel.setBorderVisible(false);
        	m_simAttribsPanel.setNotScrollBars();
        	m_simAttribsPanel.addAttribute(getStartedTimeAttr());
        	m_simAttribsPanel.addAttribute(getEffortTimeAttr());
        	m_simAttribsPanel.addAttribute(getAvgSimTimeAttr());
        	m_simAttribsPanel.addAttribute(getMaxSimTimeAttr());
        	m_simAttribsPanel.addAttribute(getUtilSimTimeAttr());
        }        
        return m_simAttribsPanel;
    }
	
	private DTGAttribute getStartedTimeAttr()
    {
        if (m_startedTimeAttr == null)
        {
        	m_startedTimeAttr = new DTGAttribute("startedtime","Startet kl",130,0,false);
        	Utils.setFixedSize(m_startedTimeAttr,250,25);
        	
        }        
        return m_startedTimeAttr;
    }
	
	private TextFieldAttribute getEffortTimeAttr()
    {
        if (m_effortTimeAttr == null)
        {
        	m_effortTimeAttr = new TextFieldAttribute("efforttime","Innsatstid",130,"",false);
        	Utils.setFixedSize(m_effortTimeAttr,250,25);
        	
        }        
        return m_effortTimeAttr;
    }
	
	private TextFieldAttribute getAvgSimTimeAttr()
    {
        if (m_avgSimTimeAttr == null)
        {
        	m_avgSimTimeAttr = new TextFieldAttribute("avgtime","Arbeidstid (gj.sn)",130,"",false);
        	Utils.setFixedSize(m_avgSimTimeAttr,250,25);
        	
        }        
        return m_avgSimTimeAttr;
    }
	
	private TextFieldAttribute getMaxSimTimeAttr()
    {
        if (m_maxSimTimeAttr == null)
        {
        	m_maxSimTimeAttr = new TextFieldAttribute("maxtime","Arbeidstid (max)",130,"",false);
        	Utils.setFixedSize(m_maxSimTimeAttr,250,25);
        	
        }        
        return m_maxSimTimeAttr;
    }
	
	private TextFieldAttribute getUtilSimTimeAttr()
    {
        if (m_utilSimTimeAttr == null)
        {
        	m_utilSimTimeAttr = new TextFieldAttribute("utiltime","Arbeidstid (forbruk)",130,"",false);
        	Utils.setFixedSize(m_utilSimTimeAttr,250,25);
        	
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
			m_tabbedPane.addTab("Oppdrag", 
					DiskoIconFactory.getIcon("GENERAL.ELEMENT","32x32"), 
					getAssignmentsPanel(), null);
			m_tabbedPane.addTab("Bevegelse",
					DiskoIconFactory.getIcon("GENERAL.UNIT","32x32"), 
					getUnitsPanel(), null);
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		}
		return m_tabbedPane;
	
	}    
	
	private AssignmentsPanel getAssignmentsPanel()
    {
        if (m_assignmentsPanel == null)
        {
        	m_assignmentsPanel = new AssignmentsPanel();
        }        
        return m_assignmentsPanel;
    }
    
	private UnitsPanel getUnitsPanel()
    {
        if (m_unitsPanel == null)
        {
        	m_unitsPanel = new UnitsPanel();
        	m_unitsPanel.addDiskoWorkListener(this);
        }        
        return m_unitsPanel;
    }
	
	public void activate(IDiskoRole role) {
		
		// forward
		super.activate(role);
		
		// setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get set of tools visible for this wp
			setupNavBar(getDefaultNavBarButtons(),true);
		}
		
		// forward
		update(true);
						
	}
    
	private List<Enum<?>> getDefaultNavBarButtons() {
		List<Enum<?>> myButtons = Utils.getListNoneOf(DiskoToolType.class);
		myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
		myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
		myButtons.add(DiskoToolType.PAN_TOOL);
		myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
		myButtons.add(DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND);
		myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
		myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
		myButtons.add(DiskoCommandType.MAP_TOGGLE_COMMAND);
		myButtons.add(DiskoCommandType.SCALE_COMMAND);
		myButtons.add(DiskoCommandType.TOC_COMMAND);
		myButtons.add(DiskoCommandType.GOTO_COMMAND);
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
			DiskoWorkPool.getInstance().schedule(new SimulatorWork(1));
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
			DiskoWorkPool.getInstance().schedule(new SimulatorWork(2));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}	
	
	private class SimulatorWork extends ModuleWork<Boolean> {

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
		public void done() {
			
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
			// do the rest
			super.done();
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
