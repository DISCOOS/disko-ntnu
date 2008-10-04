package org.redcross.sar.wp.ds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.ds.DiskoDecisionSupport;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.event.TickEvent;
import org.redcross.sar.gui.dialog.DirectoryChooserDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.map.MapPanel;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.layer.EstimatedPositionLayer;
import org.redcross.sar.map.layer.IDiskoLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.layer.IDiskoLayer.LayerCode;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Utils;
import org.redcross.sar.wp.AbstractDiskoWpModule;
import org.redcross.sar.wp.ds.AssignmentsPanel;

public class DiskoWpDsImpl extends AbstractDiskoWpModule implements IDiskoWpDs
{
	
	private static final int UPDATE_TIME_DELAY = 1000;	// updates every second
	private static final String CONTROL_CAPTION = "Beslutningsstøtte - <b>Kontrollpanel</b> (%s)";

	private long m_timeCounter = 0;
	
	private JSplitPane m_splitPane;
	private MapPanel m_mapPanel;
	private JPanel m_estimatorPanel;
	private BasePanel m_controlPanel;
	private FieldsPanel m_estAttribsPanel;
	private DTGField m_startedTimeAttr;
	private TextLineField m_effortTimeAttr;
	private TextLineField m_avgEstTimeAttr;
	private TextLineField m_maxEstTimeAttr;
	private TextLineField m_utilEstTimeAttr;
	private TextLineField m_catalogAttr;
	private JButton m_loadButton;
	private JButton m_saveButton;
	private JButton m_resumeButton;
	private JButton m_suspendButton;
	private JButton m_stopButton;
	private JTabbedPane m_tabbedPane;
	private AssignmentsPanel m_activeAssignmentsPanel;
	private AssignmentsPanel m_archivedAssignmentsPanel;
	
	private DiskoDecisionSupport m_ds;
	private RouteCostEstimator m_estimator; 
	
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
		myLayers.add(IDiskoLayer.LayerCode.ESTIMATED_POSITION_LAYER);
	    return myLayers;
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
			m_tabbedPane.addTab("Aktive", 
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"), 
					getActiveAssignmentsPanel(), null);
			m_tabbedPane.addTab("Utførte", 
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"), 
					getArchivedAssignmentsPanel(), null);
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		}
		return m_tabbedPane;
	
	}    
	
	private AssignmentsPanel getActiveAssignmentsPanel()
    {
        if (m_activeAssignmentsPanel == null)
        {
        	m_activeAssignmentsPanel = new AssignmentsPanel(getMap(),false);
        }        
        return m_activeAssignmentsPanel;
    }
        
	private AssignmentsPanel getArchivedAssignmentsPanel()
    {
        if (m_archivedAssignmentsPanel == null)
        {
        	m_archivedAssignmentsPanel = new AssignmentsPanel(getMap(),true);
        }        
        return m_archivedAssignmentsPanel;
    }
	
    private JSplitPane getSplitPane()
    {
        if (m_splitPane == null)
        {
        	m_splitPane = new JSplitPane();
        	m_splitPane.setBorder(BorderFactory.createEmptyBorder());
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
        	m_mapPanel.setBorder(UIFactory.createBorder());
			m_mapPanel.setMinimumSize(new Dimension(350,350));
			m_mapPanel.setPreferredSize(new Dimension(450,350));
        	
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
        	m_controlPanel = new BasePanel(String.format(CONTROL_CAPTION,"Stoppet"),ButtonSize.SMALL);
        	m_controlPanel.setPreferredSize(new Dimension(400,235));
        	m_controlPanel.addButton(getLoadButton(), "load");
        	m_controlPanel.addButton(getSaveButton(), "save");
        	m_controlPanel.addButton(getResumeButton(), "resume");
        	m_controlPanel.addButton(getSuspendButton(), "suspend");		
        	m_controlPanel.addButton(getStopButton(), "stop");		
        	m_controlPanel.addActionListener(new ActionListener() {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    				// prepare
    				String cmd = e.getActionCommand();
    				// translate
    				if("load".equals(cmd)) {
    					load();
    				}
    				else if("save".equals(cmd)) {
    					save();
    				}
    				else if("resume".equals(cmd)) {
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
    
    private JButton getLoadButton() {
		if(m_loadButton==null) {
			m_loadButton = DiskoButtonFactory.createButton("GENERAL.OPEN", ButtonSize.SMALL);
		}
		return m_loadButton;
	}
	
	private JButton getSaveButton() {
		if(m_saveButton==null) {
			m_saveButton = DiskoButtonFactory.createButton("GENERAL.SAVE", ButtonSize.SMALL);
		}
		return m_saveButton;
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
	
	private FieldsPanel getEstAttribsPanel()
    {
        if (m_estAttribsPanel == null)
        {
        	m_estAttribsPanel = new FieldsPanel("","",false,false);
        	m_estAttribsPanel.setHeaderVisible(false);
        	m_estAttribsPanel.setBorderVisible(false);
        	m_estAttribsPanel.setNotScrollBars();
        	m_estAttribsPanel.addAttribute(getStartedTimeAttr());
        	m_estAttribsPanel.addAttribute(getEffortTimeAttr());
        	m_estAttribsPanel.addAttribute(getAvgEstTimeAttr());
        	m_estAttribsPanel.addAttribute(getMaxEstTimeAttr());
        	m_estAttribsPanel.addAttribute(getUtilEstTimeAttr());
        	m_estAttribsPanel.addAttribute(getCatalogAttr());
        }        
        return m_estAttribsPanel;
    }
	
	private DTGField getStartedTimeAttr()
    {
        if (m_startedTimeAttr == null)
        {
        	m_startedTimeAttr = new DTGField("startedtime","Startet kl", 
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
	
	private TextLineField getAvgEstTimeAttr()
    {
        if (m_avgEstTimeAttr == null)
        {
        	m_avgEstTimeAttr = new TextLineField("avgtime","Arbeidstid (gj.sn)", false, 130, 25, 0);
        	
        }        
        return m_avgEstTimeAttr;
    }
	
	private TextLineField getMaxEstTimeAttr()
    {
        if (m_maxEstTimeAttr == null)
        {
        	m_maxEstTimeAttr = new TextLineField("maxtime","Arbeidstid (max)", false, 130, 25, 0);
        	
        }        
        return m_maxEstTimeAttr;
    }
	
	private TextLineField getUtilEstTimeAttr()
    {
        if (m_utilEstTimeAttr == null)
        {
        	m_utilEstTimeAttr = new TextLineField("utiltime","Arbeidstid (forbruk)", false, 130, 25, 0);
        	
        }        
        return m_utilEstTimeAttr;
    }
	
	private TextLineField getCatalogAttr() {
		if (m_catalogAttr==null) {
			m_catalogAttr = new TextLineField("Catalog","Katalog",false,130,25,AppProps.getText("DS.ETE.LOGGING.path"));
			m_catalogAttr.setButtonVisible(true);
			m_catalogAttr.setButtonEnabled(true);
			m_catalogAttr.addButtonActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DirectoryChooserDialog dirChooser = new DirectoryChooserDialog();
					File file = new File(AppProps.getText("DS.ETE.LOGGING.path"));
					dirChooser.setRoot(FileTreeModel.COMPUTER);					
					String msg = "Velg katalog";
					Icon icon = UIManager.getIcon("OptionPane.informationIcon");					
					Object selected = dirChooser.select(file.toString(), msg, icon);
					if(selected != null) {
						m_catalogAttr.setValue(selected);
						AppProps.setText("DS.ETE.LOGGING.path",m_catalogAttr.getValue());
					}        					
				}
				
			});
		}
		return m_catalogAttr;
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
		if(m_estimator!=null && m_estimator.isWorking()) {
			getAvgEstTimeAttr().setValue(m_estimator.getAverageWorkTime() + " ms");
			getMaxEstTimeAttr().setValue(m_estimator.getMaxWorkTime() + " ms");
			getUtilEstTimeAttr().setValue(Math.round(m_estimator.getUtilization()*100) + " %");
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
    	m_estimator = (RouteCostEstimator)getDs().install(RouteCostEstimator.class, oprID);
    	EstimatedPositionLayer l = ((EstimatedPositionLayer)getMap().getDiskoLayer(LayerCode.ESTIMATED_POSITION_LAYER));
    	l.setEstimator(m_estimator);
    	getActiveAssignmentsPanel().install(m_estimator);
    	getArchivedAssignmentsPanel().install(m_estimator);
    	m_estimator.load();
    }
    
    private void load() {
    	File file = new File(AppProps.getText("DS.ETE.LOGGING.path"));
    	if(file.exists()) {
    		m_estimator.importSamples(file.toString());
    	}
    	else {
    		Utils.showWarning("Advarsel","Loggfil " + AppProps.getText("DS.ETE.LOGGING.path") + " finnes ikke");
    	}
    }
    
    private void save() {
		String file = m_estimator.exportSamples(AppProps.getText("DS.ETE.LOGGING.path"));
		Utils.showMessage("Bekreftelse","Loggfilen " + file + " er opprettet");
    }

    private void resume() {
    	// forward
    	doWork(1);
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
	
	private boolean doWork(int task) {
		try {
			// forward work
			DiskoWorkPool.getInstance().schedule(new DsWork(task));
			// do work
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class DsWork extends ModuleWork<Boolean> {

		private int m_task = 0;
		
		/**
		 * Constructor
		 * 
		 * @param task
		 */
		DsWork(int task) throws Exception {
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
				case 1: start(); return true;
				case 2: rollback(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		
		private void start() {
			try{
		    	// load?
		    	if(!(m_estimator.isSuspended() || m_estimator.isWorking()))
		    		m_estimator.load();
		    	// forward
		    	getDs().start(getActiveOperationId());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
    
}
