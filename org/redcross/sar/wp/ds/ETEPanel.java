package org.redcross.sar.wp.ds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.redcross.sar.AppProps;
import org.redcross.sar.Application;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.event.IServiceListener;
import org.redcross.sar.event.ServiceEvent.Execute;
import org.redcross.sar.gui.dialog.DirectoryChooserDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextField;
import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.panel.FieldPane;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.IWorkLoop.LoopState;
import org.redcross.sar.wp.ds.AssignmentPanel;

public class ETEPanel extends JPanel
{

	private static final long serialVersionUID = 1L;
	private static final String CONTROL_CAPTION = "ETE - <b>Kontrollpanel</b> (%s)";

	private TogglePanel m_etePanel;
	private FieldPane m_etePropPanel;
	private DTGField m_startedTimeAttr;
	private TextField m_effortTimeAttr;
	private TextField m_avgEstTimeAttr;
	private TextField m_maxEstTimeAttr;
	private TextField m_utilEstTimeAttr;
	private TextField m_catalogAttr;
	private JButton m_loadButton;
	private JButton m_saveButton;
	private JButton m_resumeButton;
	private JButton m_suspendButton;
	private JButton m_stopButton;
	private JTabbedPane m_tabbedPane;
	private AssignmentPanel m_pendingAssignmentPanel;
	private AssignmentPanel m_activeAssignmentPanel;
	private AssignmentPanel m_archivedAssignmentPanel;

	private DsPool m_ds;
	private IDiskoMap m_map;
	private IMsoModelIf m_model;
	private RouteCostEstimator m_ete;

    public ETEPanel(IDiskoMap map, IMsoModelIf model) throws Exception
    {

    	// prepare
    	m_map = map;
    	m_model = model;

		// initialize GUI
		initialize();

	}

    private void initialize()
    {
    	setLayout(new BorderLayout(5,5));
    	add(getETEPanel(),BorderLayout.NORTH);
    	add(getTabbedPane(),BorderLayout.CENTER);
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
			m_tabbedPane.addTab("Avventer",
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"),
					getPendingAssignmentPanel(), null);
			m_tabbedPane.addTab("Aktive",
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"),
					getActiveAssignmentPanel(), null);
			m_tabbedPane.addTab("Utførte",
					DiskoIconFactory.getIcon("SEARCH.PATROL","32x32"),
					getArchivedAssignmentPanel(), null);
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		}
		return m_tabbedPane;

	}

	private AssignmentPanel getPendingAssignmentPanel()
    {
        if (m_pendingAssignmentPanel == null)
        {
        	m_pendingAssignmentPanel = new AssignmentPanel(m_map,0);
        	m_pendingAssignmentPanel.connect(Application.getInstance().getMsoModel());
        }
        return m_pendingAssignmentPanel;
    }

	private AssignmentPanel getActiveAssignmentPanel()
    {
        if (m_activeAssignmentPanel == null)
        {
        	m_activeAssignmentPanel = new AssignmentPanel(m_map,1);
        	m_activeAssignmentPanel.connect(Application.getInstance().getMsoModel());
        }
        return m_activeAssignmentPanel;
    }

	private AssignmentPanel getArchivedAssignmentPanel()
    {
        if (m_archivedAssignmentPanel == null)
        {
        	m_archivedAssignmentPanel = new AssignmentPanel(m_map,2);
        	m_archivedAssignmentPanel.connect(Application.getInstance().getMsoModel());
        }
        return m_archivedAssignmentPanel;
    }

    private TogglePanel getETEPanel()
    {
        if (m_etePanel == null)
        {
        	m_etePanel = new TogglePanel(String.format(CONTROL_CAPTION,"Stoppet"),false,false,ButtonSize.SMALL);
        	m_etePanel.setPreferredSize(new Dimension(400,235));
        	m_etePanel.collapse();
        	m_etePanel.addButton(getLoadButton(), "load");
        	m_etePanel.addButton(getSaveButton(), "save");
        	m_etePanel.addButton(getResumeButton(), "resume");
        	m_etePanel.addButton(getSuspendButton(), "suspend");
        	m_etePanel.addButton(getStopButton(), "stop");
        	m_etePanel.addActionListener(new ActionListener() {

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
        	m_etePanel.setContainer(getEtePropPanel());

        }
        return m_etePanel;
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

	private FieldPane getEtePropPanel()
    {
        if (m_etePropPanel == null)
        {
        	m_etePropPanel = new FieldPane("","",false,false);
        	m_etePropPanel.setHeaderVisible(false);
        	m_etePropPanel.setBorderVisible(false);
        	m_etePropPanel.setNotScrollBars();
        	m_etePropPanel.addField(getStartedTimeAttr());
        	m_etePropPanel.addField(getEffortTimeAttr());
        	m_etePropPanel.addField(getAvgEstTimeAttr());
        	m_etePropPanel.addField(getMaxEstTimeAttr());
        	m_etePropPanel.addField(getUtilEstTimeAttr());
        	m_etePropPanel.addField(getCatalogAttr());
        }
        return m_etePropPanel;
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

	private TextField getEffortTimeAttr()
    {
        if (m_effortTimeAttr == null)
        {
        	m_effortTimeAttr = new TextField("efforttime","Innsatstid", false, 130, 25, 0);

        }
        return m_effortTimeAttr;
    }

	private TextField getAvgEstTimeAttr()
    {
        if (m_avgEstTimeAttr == null)
        {
        	m_avgEstTimeAttr = new TextField("avgtime","Arbeidstid (gj.sn)", false, 130, 25, 0);

        }
        return m_avgEstTimeAttr;
    }

	private TextField getMaxEstTimeAttr()
    {
        if (m_maxEstTimeAttr == null)
        {
        	m_maxEstTimeAttr = new TextField("maxtime","Arbeidstid (max)", false, 130, 25, 0);

        }
        return m_maxEstTimeAttr;
    }

	private TextField getUtilEstTimeAttr()
    {
        if (m_utilEstTimeAttr == null)
        {
        	m_utilEstTimeAttr = new TextField("utiltime","Arbeidstid (forbruk)", false, 130, 25, 0);

        }
        return m_utilEstTimeAttr;
    }

	private TextField getCatalogAttr() {
		if (m_catalogAttr==null) {
			m_catalogAttr = new TextField("Catalog","Katalog",false,130,25,AppProps.getText("DS.ETE.LOGGING.path"));
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

 	public void update() {
		Calendar c = m_model.getMsoManager().getOperation().getCreatedTime();
		getStartedTimeAttr().setValue(c);
		if(c!=null) {
			int seconds = (int)(System.currentTimeMillis() - getStartedTimeAttr().getValue().getTimeInMillis())/1000;
			getEffortTimeAttr().setValue(Utils.getTime(seconds));
		}
		// get decision support statistics
		if(m_ete!=null && !m_ete.isLoopState(LoopState.PENDING)) {
			// get work loop
			IWorkLoop loop = m_ete.getWorkLoop();
			// update fields
			getAvgEstTimeAttr().setValue(loop.getAverageWorkTime() + " ms");
			getMaxEstTimeAttr().setValue(loop.getMaximumWorkTime() + " ms");
			getUtilEstTimeAttr().setValue(Math.round(loop.getAverageUtilization()*100) + " %");
		}
	}

    private DsPool getDs() {
    	if(m_ds==null) {
    		try {
				m_ds = DsPool.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return m_ds;
    }

    private String getActiveOprID() {
    	return m_model.getDispatcher().getActiveOperationID();
    }

    public void connect() {
    	// get active operation
    	String oprID = getActiveOprID();
    	// disconnect from estimator?
    	if(m_ete!=null) m_ete.removeServiceListener(m_serviceListener);
    	// create estimator
    	RouteCostEstimator ete = (RouteCostEstimator)getDs().getItem(RouteCostEstimator.class, oprID);
    	// found?
    	if(ete!=null) {
	    	// connect to estimator
	    	getPendingAssignmentPanel().connect(ete);
	    	getActiveAssignmentPanel().connect(ete);
	    	getArchivedAssignmentPanel().connect(ete);
	    	// set active
	    	m_ete = ete;
	    	// update caption
	    	getETEPanel().setCaptionText(getCaption(m_ete.getLoopState()));
    	}
    }

    private void load() {
    	File file = new File(AppProps.getText("DS.ETE.LOGGING.path"));
    	if(file.exists()) {
    		m_ete.importSamples(file.toString());
    	}
    	else {
    		Utils.showWarning("Advarsel","Loggfil " + AppProps.getText("DS.ETE.LOGGING.path") + " finnes ikke");
    	}
    }

    private void save() {
		String file = m_ete.exportSamples(AppProps.getText("DS.ETE.LOGGING.path"));
		getCatalogAttr().setValue(file);
		Utils.showMessage("Bekreftelse","Loggfilen " + file + " er opprettet");
    }

    private void resume() {
    	// forward
    	doWork(1);
    	// forward
    	getETEPanel().setCaptionText(String.format(CONTROL_CAPTION,"Spiller"));
    }

    private void suspend() {
    	// forward
    	getDs().suspend(RouteCostEstimator.class, getActiveOprID());
    	getETEPanel().setCaptionText(String.format(CONTROL_CAPTION,"Pause"));
    }

    private void stop() {
    	// forward
    	getDs().stop(RouteCostEstimator.class, getActiveOprID());
    	// forward
    	getETEPanel().setCaptionText(String.format(CONTROL_CAPTION,"Stoppet"));
    }

	private String getCaption(LoopState state) {
		switch(state) {
		case PENDING:
		case EXECUTING:
		case IDLE:
	    	return String.format(CONTROL_CAPTION,"Spiller");
		case SUSPENDED:
	    	return String.format(CONTROL_CAPTION,"Pause");
	    default:
	    	return String.format(CONTROL_CAPTION,"Stoppet");
		}
	}

	private boolean doWork(int task) {
		try {
			// forward work
			WorkPool.getInstance().schedule(new Work(task));
			// do work
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	class Work extends AbstractWork {

		private int m_task = 0;

		/**
		 * Constructor
		 *
		 * @param task
		 */
		Work(int task) throws Exception {
			// forward
			super(NORMAL_PRIORITY,true,false,WorkerType.UNSAFE,"Vent litt",500,true,false);
			// prepare
			m_task = task;
		}

		@Override
		public Boolean doWork(IWorkLoop loop) {
			try {
				// dispatch task
				switch(m_task) {
				case 1: start(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}

		private void start() {
			try{
				// is suspended?
				if(m_ete.isLoopState(LoopState.SUSPENDED)) {
					m_ete.resume();
				}
				else {
		    		// load data
		    		//m_ete.load();
		    		// forward
		    		m_ete.start();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}

	}

	private IServiceListener m_serviceListener = new IServiceListener() {

		@Override
		public void handleExecuteEvent(Execute e) {
	    	// update caption
	    	getETEPanel().setCaptionText(getCaption(m_ete.getLoopState()));
		}

	};

}
