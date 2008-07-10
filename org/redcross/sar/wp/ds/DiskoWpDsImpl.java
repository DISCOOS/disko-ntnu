package org.redcross.sar.wp.ds;

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
import org.redcross.sar.ds.DiskoDecisionSupport;
import org.redcross.sar.ds.ete.RouteCostEstimator;
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
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.wp.AbstractDiskoWpModule;
import org.redcross.sar.wp.ds.AssignmentsPanel;

/**
 *
 */
public class DiskoWpDsImpl extends AbstractDiskoWpModule implements IDiskoWpDs
{
	
	private static final int UPDATE_TIME_DELAY = 1000;	// updates every second
	private static final String CONTROL_CAPTION = "<html>Beslutningsstøtte - <b>Kontrollpanel</b> (%s)</html>";

	private long m_timeCounter = 0;
	
	private JSplitPane m_splitPane = null;
	private MapPanel m_mapPanel = null;
	private JPanel m_estimatorPanel = null;
	private BasePanel m_controlPanel = null;
	private AttributesPanel m_estAttribsPanel = null;
	private DTGAttribute m_startedTimeAttr = null;
	private TextFieldAttribute m_effortTimeAttr = null;	
	private TextFieldAttribute m_avgEstTimeAttr = null;	
	private TextFieldAttribute m_maxEstTimeAttr = null;	
	private TextFieldAttribute m_utilEstTimeAttr = null;	
	private JButton m_resumeButton = null;
	private JButton m_suspendButton = null;
	private JButton m_stopButton = null;	
	private JTabbedPane m_tabbedPane = null;
	private AssignmentsPanel m_assignmentsPanel = null;
	
	private DiskoDecisionSupport m_ds = null;
	private RouteCostEstimator m_ete = null; 
	
    public DiskoWpDsImpl() throws Exception
    {
		// forward
		super(getWpInterests(),getMapLayers());
		
		// initialize GUI
		initialize();
		
		// install estimators
		install();
		
	}

	private static EnumSet<MsoClassCode> getWpInterests() {
		EnumSet<MsoClassCode> myInterests = EnumSet.of(MsoClassCode.CLASSCODE_OPERATION);
    	myInterests.add(MsoClassCode.CLASSCODE_AREA);    	
    	myInterests.add(MsoClassCode.CLASSCODE_UNIT);
    	myInterests.add(MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMapLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers;
		myLayers = EnumSet.of(IMsoFeatureLayer.LayerCode.OPERATION_AREA_MASK_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
	    return myLayers;
	}
	
    private void initialize()
    {
		// get properties
		assignWpBundle(IDiskoWpDs.class);
		
		// forward
		installMap();
	
		// set layout component
		layoutComponent(getSplitPane());
		
		// start update task
		registerUpdateTask();
		
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
        
    private JSplitPane getSplitPane()
    {
        if (m_splitPane == null)
        {
        	m_splitPane = new JSplitPane();
        	m_splitPane.setLeftComponent(getMapPanel());
        	m_splitPane.setRightComponent(getEstimatorPanel());
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
   
    private JPanel getEstimatorPanel()
    {
        if (m_estimatorPanel == null)
        {
        	m_estimatorPanel = new JPanel(new BorderLayout(5,5));
        	m_estimatorPanel.add(getControlPanel(),BorderLayout.NORTH);
        	m_estimatorPanel.add(getTabbedPane(),BorderLayout.CENTER);
        	
        }        
        return m_estimatorPanel;
    }
    
    private BasePanel getControlPanel()
    {
        if (m_controlPanel == null)
        {
        	m_controlPanel = new BasePanel(String.format(CONTROL_CAPTION,"Stoppet"));
        	m_controlPanel.setPreferredSize(new Dimension(400,210));
        	m_controlPanel.addButton(getResumeButton(), "resume");
        	m_controlPanel.addButton(getSuspendButton(), "suspend");		
        	m_controlPanel.addButton(getStopButton(), "stop");		
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
    				else if("stop".equals(cmd)) {
    					stop();
    				}	
    			}
    			
    		});
        	
        	  // add attributes
        	m_controlPanel.setBodyComponent(getEstAttribsPanel());
        	
        }        
        return m_controlPanel;
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
	
	private JButton getStopButton() {
		if(m_stopButton==null) {
			m_stopButton = DiskoButtonFactory.createButton("GENERAL.STOP", ButtonSize.SMALL);
		}
		return m_stopButton;
	}
	
	private AttributesPanel getEstAttribsPanel()
    {
        if (m_estAttribsPanel == null)
        {
        	m_estAttribsPanel = new AttributesPanel("","",false,false);
        	m_estAttribsPanel.setHeaderVisible(false);
        	m_estAttribsPanel.setBorderVisible(false);
        	m_estAttribsPanel.setNotScrollBars();
        	m_estAttribsPanel.addAttribute(getStartedTimeAttr());
        	m_estAttribsPanel.addAttribute(getEffortTimeAttr());
        	m_estAttribsPanel.addAttribute(getAvgEstTimeAttr());
        	m_estAttribsPanel.addAttribute(getMaxEstTimeAttr());
        	m_estAttribsPanel.addAttribute(getUtilEstTimeAttr());
        }        
        return m_estAttribsPanel;
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
	
	private TextFieldAttribute getAvgEstTimeAttr()
    {
        if (m_avgEstTimeAttr == null)
        {
        	m_avgEstTimeAttr = new TextFieldAttribute("avgtime","Arbeidstid (gj.sn)",130,"",false);
        	Utils.setFixedSize(m_avgEstTimeAttr,250,25);
        	
        }        
        return m_avgEstTimeAttr;
    }
	
	private TextFieldAttribute getMaxEstTimeAttr()
    {
        if (m_maxEstTimeAttr == null)
        {
        	m_maxEstTimeAttr = new TextFieldAttribute("maxtime","Arbeidstid (max)",130,"",false);
        	Utils.setFixedSize(m_maxEstTimeAttr,250,25);
        	
        }        
        return m_maxEstTimeAttr;
    }
	
	private TextFieldAttribute getUtilEstTimeAttr()
    {
        if (m_utilEstTimeAttr == null)
        {
        	m_utilEstTimeAttr = new TextFieldAttribute("utiltime","Arbeidstid (forbruk)",130,"",false);
        	Utils.setFixedSize(m_utilEstTimeAttr,250,25);
        	
        }        
        return m_utilEstTimeAttr;
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
    public void beforeOperationChange() {
    	suspend();
    	super.beforeOperationChange();
    }
    
	@Override
	public void afterOperationChange() {
    	super.afterOperationChange();
		install();
		update(true);
	}

	private void update(boolean init) {
		if(!(isActive() && getMsoModel().getMsoManager().operationExists())) return;
		if(init) {
			getStartedTimeAttr().setValue(getMsoModel().getMsoManager().getOperation().getCreatedTime());
		}
		Calendar c = getStartedTimeAttr().getValue();
		if(c!=null) {
			int seconds = (int)(System.currentTimeMillis() - getStartedTimeAttr().getValue().getTimeInMillis())/1000;
			getEffortTimeAttr().setValue(Utils.getTime(seconds));
		}		
		// get decision support statistics
		if(m_ete!=null && m_ete.isWorking()) {
			getAvgEstTimeAttr().setValue(m_ete.getAverageWorkTime() + " ms");
			getMaxEstTimeAttr().setValue(m_ete.getMaxWorkTime() + " ms");
			getUtilEstTimeAttr().setValue(Math.round(m_ete.getUtilization()*100) + " %");
		}
	}
	
    public String getCaption() {
		return getBundleText("DS");
	}
    
    private DiskoDecisionSupport getDs() {
    	if(m_ds==null) {
    		try {
				m_ds = DiskoDecisionSupport.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}
    	return m_ds;
    }

    private String getActiveOperationId() {
    	return MsoModelImpl.getInstance().getModelDriver().getActiveOperationID();
    }
    
    private void install() {
    	String oprID = getActiveOperationId();    	
    	m_ete = (RouteCostEstimator)getDs().install(RouteCostEstimator.class, oprID);
    	m_ete.load();
    	getAssignmentsPanel().setEstimator(m_ete);
    }
    
    private void resume() {
    	// forward
    	getDs().start(getActiveOperationId());
    	// forward
    	getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Spiller"));  	
    }
        
    private void suspend() {
    	// forward
    	getDs().suspend(getActiveOperationId());
    	getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Pause"));    	
    }	
    
    private void stop() {
    	// forward
    	getDs().stop(getActiveOperationId());
    	// forward
    	getControlPanel().setCaptionText(String.format(CONTROL_CAPTION,"Stoppet"));  	
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
	
}
