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

import org.redcross.sar.app.event.IServiceListener;
import org.redcross.sar.app.event.ServiceEvent.Execute;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.gui.dialog.DirectoryChooserDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.TextLineField;
import org.redcross.sar.gui.model.FileTreeModel;
import org.redcross.sar.gui.panel.FieldsPanel;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.ds.advisor.Advisor;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.IWorkLoop.LoopState;

public class AdvisorPanel extends JPanel
{

	private static final long serialVersionUID = 1L;
	private static final String CONTROL_CAPTION = "Rådgiver - <b>Kontrollpanel</b> (%s)";

	private TogglePanel m_advisorPanel;
	private FieldsPanel m_advisorPropPanel;
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
	private LevelPanel m_levelPanel;


	private DsPool m_ds;
	private Advisor m_advisor;
	private IMsoModelIf m_model;

    public AdvisorPanel(IMsoModelIf model)
    {

    	// prepare
    	m_model = model;

		// initialize GUI
		initialize();

	}

    private void initialize()
    {
    	setLayout(new BorderLayout(5,5));
    	add(getAdvisorPanel(),BorderLayout.NORTH);
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
			m_tabbedPane.addTab("Variabler",
					DiskoIconFactory.getIcon("GENERAL.HYPOTHESIS","32x32"),
					getLevelPanel(), null);
			m_tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		}
		return m_tabbedPane;

	}

	private LevelPanel getLevelPanel()
    {
        if (m_levelPanel == null)
        {
        	m_levelPanel = new LevelPanel();
        }
        return m_levelPanel;
    }


    private TogglePanel getAdvisorPanel()
    {
        if (m_advisorPanel == null)
        {
        	m_advisorPanel = new TogglePanel(String.format(CONTROL_CAPTION,"Stoppet"),false,false,ButtonSize.SMALL);
        	m_advisorPanel.setPreferredSize(new Dimension(400,235));
        	m_advisorPanel.setExpanded(false);
        	m_advisorPanel.addButton(getLoadButton(), "load");
        	m_advisorPanel.addButton(getSaveButton(), "save");
        	m_advisorPanel.addButton(getResumeButton(), "resume");
        	m_advisorPanel.addButton(getSuspendButton(), "suspend");
        	m_advisorPanel.addButton(getStopButton(), "stop");
        	m_advisorPanel.addActionListener(new ActionListener() {

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
        	m_advisorPanel.setContainer(getEtePropPanel());

        }
        return m_advisorPanel;
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

	private FieldsPanel getEtePropPanel()
    {
        if (m_advisorPropPanel == null)
        {
        	m_advisorPropPanel = new FieldsPanel("","",false,false);
        	m_advisorPropPanel.setHeaderVisible(false);
        	m_advisorPropPanel.setBorderVisible(false);
        	m_advisorPropPanel.setNotScrollBars();
        	m_advisorPropPanel.addField(getStartedTimeAttr());
        	m_advisorPropPanel.addField(getEffortTimeAttr());
        	m_advisorPropPanel.addField(getAvgEstTimeAttr());
        	m_advisorPropPanel.addField(getMaxEstTimeAttr());
        	m_advisorPropPanel.addField(getUtilEstTimeAttr());
        	m_advisorPropPanel.addField(getCatalogAttr());
        }
        return m_advisorPropPanel;
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
			m_catalogAttr = new TextLineField("Catalog","Katalog",false,130,25,AppProps.getText("DS.ADVISOR.LOGGING.path"));
			m_catalogAttr.setButtonVisible(true);
			m_catalogAttr.setButtonEnabled(true);
			m_catalogAttr.addButtonActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					DirectoryChooserDialog dirChooser = new DirectoryChooserDialog();
					File file = new File(AppProps.getText("DS.ADVISOR.LOGGING.path"));
					dirChooser.setRoot(FileTreeModel.COMPUTER);
					String msg = "Velg katalog";
					Icon icon = UIManager.getIcon("OptionPane.informationIcon");
					Object selected = dirChooser.select(file.toString(), msg, icon);
					if(selected != null) {
						m_catalogAttr.setValue(selected);
						AppProps.setText("DS.ADVISOR.LOGGING.path",m_catalogAttr.getValue());
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
		if(m_advisor!=null && !m_advisor.isLoopState(LoopState.PENDING)) {
			// get work loop
			IWorkLoop loop = m_advisor.getWorkLoop();
			// update fields
			getAvgEstTimeAttr().setValue(loop.getAverageWorkTime() + " ms");
			getMaxEstTimeAttr().setValue(loop.getMaxWorkTime() + " ms");
			getUtilEstTimeAttr().setValue(Math.round(loop.getUtilization()*100) + " %");
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
    	return m_model.getModelDriver().getActiveOperationID();
    }

    public void connect() {
    	// get active operation
    	String oprID = getActiveOprID();
    	// disconnect from advisor?
    	if(m_advisor!=null) m_advisor.removeServiceListener(m_serviceListener);
    	// get advisor from pool
    	Advisor advisor = (Advisor)getDs().getItem(Advisor.class, oprID);
    	// found?
    	if(advisor!=null) {
	    	// connect to advisor
	    	getLevelPanel().connect(advisor);
	    	// listen for service changes
	    	advisor.addServiceListener(m_serviceListener);
	    	// set active
	    	m_advisor = advisor;
	    	// update caption
	    	getAdvisorPanel().setCaptionText(getCaption(m_advisor.getLoopState()));
    	}
    }

    private void load() {
    	File file = new File(AppProps.getText("DS.ADVISOR.LOGGING.path"));
    	if(file.exists()) {
    		m_advisor.importSamples(file.toString());
    	}
    	else {
    		Utils.showWarning("Advarsel","Loggfil " + AppProps.getText("DS.ADVISOR.LOGGING.path") + " finnes ikke");
    	}
    }

    private void save() {
		String file = m_advisor.exportSamples(AppProps.getText("DS.ADVISOR.LOGGING.path"));
		getCatalogAttr().setValue(file);
		Utils.showMessage("Bekreftelse","Loggfilen " + file + " er opprettet");
    }

    private void resume() {
		// is suspended?
		if(m_advisor.isLoopState(LoopState.SUSPENDED)) {
			m_advisor.resume();
		}
		else {
    		// forward
			m_advisor.start();
		}
    }

    private void suspend() {
    	// forward
    	getDs().suspend(Advisor.class, getActiveOprID());
    }

    private void stop() {
    	// forward
    	getDs().stop(Advisor.class, getActiveOprID());
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

	private IServiceListener m_serviceListener = new IServiceListener() {

		@Override
		public void handleExecuteEvent(Execute e) {
	    	// update caption
	    	getAdvisorPanel().setCaptionText(getCaption(m_advisor.getLoopState()));
		}

	};
}
