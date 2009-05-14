package org.redcross.sar;

import no.cmr.hrs.sar.tools.IDHelper;
import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.disco.io.IOManager;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.gui.DiskoGlassPaneUtils;
import org.redcross.sar.gui.DiskoKeyEventDispatcher;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.menu.SysMenu;
import org.redcross.sar.map.DiskoMapManagerImpl;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.mso.DispatcherImpl;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IDispatcherListenerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.DispatcherAdapter;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.SaraDispatcherImpl;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.ProgressMonitor;
import org.redcross.sar.work.WorkPool;

import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Implements the DiskoApplication interface. This class is responsible for connecting to the
 * ArcGIS Engine API and the DiskoModule API.
 */

/**
 * @author geira, kenneth
 */
public class Application implements IApplication
{
	private static final String CONFIRMATION_TEXT = "CONFIRMATION.TEXT";
	private static final String CONFIRMATION_TITLE = "CONFIRMATION.HEADER";
	private static final String CHOOSETEXT = "CHOOSE.OP.TEXT";
	private static final String WORK_ERROR_TEXT = "WORK.ERROR.TEXT";
	private static final String WORK_ERROR_TITLE = "WORK.ERROR.TITLE";
	private static final String INIT_ERROR_SHUTDOWN_TEXT = "INIT.ERROR.SHUTDOWN.TEXT";
	private static final String FINISH_TITLE = "FINISH.HEADER";
	private static final String FINISH_TEXT ="FINISH.TEXT";
	private static final String NEWACTION_TEXT ="NEWACTION.TEXT";
	private static final String NEWACTION_DESC ="NEWACTION.DESC";
	private static final String SWITCHACTION_TEXT ="SWITCHACTION.TEXT";
	private static final String SWITCHACTION_DESC ="SWITCHACTION.DESC";
	private static final String OPERATION_FINISHED_TITLE = "OPERATION.FINISHED.HEADER";
	private static final String OPERATION_FINISHED_TEXT ="OPERATION.FINISHED.TEXT";
	private static final String OPERATION_CREATED_TEXT = "OPERATION.CREATED.TEXT";

	private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(Application.class);	
	private static final ResourceBundle bundle = Internationalization.getBundle(IApplication.class);
    
	private JFrame m_frame;
    private IDiskoRole m_currentRole;
	private Hashtable<String, IDiskoRole> m_roles;
	private DiskoModuleManager m_moduleLoader;
	private UIFactory m_uiFactory;
	private IDiskoMapManager m_mapManager;
	private IDispatcherIf m_dispatcher;
	private IMsoModelIf m_msoModel;
	private DiskoReportManager m_diskoReport;
	private DiskoKeyEventDispatcher m_keyEventDispatcher;
	private IOManager m_ioManager;
	private ServicePool m_servicePool;
	private DsPool m_dsPool;
	private ProgressMonitor m_progresMonitor;
	private PropertyChangeSupport m_propertyChangeSupport;
	
	// undo support
	//private final UndoManager m_undoManager = new UndoManager();
	//private final UndoableEditSupport m_undoSupport = new UndoableEditSupport(m_undoManager);

	// counters
	private int lockCount = 0;

	// flags
	private boolean isLoading = false;
	private boolean waitingForNewOp = false;

	// login information
	private Object[] loggedIn = new Object[3];
	
	// ArcGIS system initialization
	private AoInitialize arcGIS;
	
	
	/*========================================================
  	 * Program startup
  	 *======================================================== */
	
	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		// prepare logger
		PropertyConfigurator.configure("log4j.properties");
		
		// notify startup
		logger.info("Application is loading...");
		
		// prepare continues...
		EngineInitializer.initializeVisualBeans();
		UIFactory.initLookAndFeel();

		// initialize GUI on new thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				Application.getInstance();
			}
		});
	}

	/*========================================================
  	 * Constructors
  	 *======================================================== */
	
  	/**
	 * This is the default constructor
	 */
	private Application()
	{
		super();
		// initialize later
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// register me
				Utils.setApp(Application.this);
				// initialize ArcGIS
				initializeArcGISLicenses();
				// initialize GUI
				initialize();
			}			
		});
	}
	
	 static {
         System.loadLibrary("rxtxSerial");
         System.loadLibrary("rxtxParallel");
     }
 	


	/*========================================================
  	 * The singleton code
  	 *======================================================== */

	private static Application m_this;
	
	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
    public static Application getInstance() {
  		if (m_this == null) {
  			try {
  	  			// it's ok, we can call this constructor
				m_this = new Application();
			} catch (Exception e) {
				logger.fatal("Failed to create application instance",e);
			}
  		}
  		return m_this;
  	}

    public static JFrame getFrameInstance() {
  		if (m_this != null) {
  			return m_this.m_frame;
  		}
  		return null;
  	}
    
	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
  	public Object clone() throws CloneNotSupportedException{
  		throw new CloneNotSupportedException();
  		// that'll teach 'em
  	}

	/*========================================================
  	 * *Initializing code
  	 *======================================================== */
	
	private void initialize()
	{
		// initialize logging
		Log.init("DISKO");
		// get frame
		JFrame frame = getFrame();
		// set title
		frame.setTitle("DISKO");
		// set glass pane
		frame.setGlassPane(getUIFactory().getGlassPane());
        // set content panel
		frame.setContentPane(getUIFactory().getContentPanel());
		// do not close when user clicks the close control box on the title line
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// add window closing handler
		frame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				// consume?
				if(isLoading())
					Utils.showWarning("Vennligst vent til lasting er ferdig");
				else
					finishOperation();
			}
			
		});
		// apply size and layout
		frame.setPreferredSize(new Dimension(1000,700));
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		frame.pack();
		// show me
		frame.setVisible(true);
		// show progress dialog
		getProgressMonitor().start("Initialiserer...",0,0,0,0,0);
		// initialize later
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try
				{
					// initialize work pool to ensure that this is done on the EDT
					WorkPool.getInstance();
					// initialize decision support to ensure that this is done on the EDT
					DsPool.getInstance();
					//initiate model driver
					getMsoModel().getDispatcher().initiate();
					getMsoModel().getDispatcher().addDispatcherListener(m_dispatcherAdapter);
					// prepare IO  
					IOManager.getInstance();
					// load services?
					if(AppProps.getText("SERVICES.initialize").equals("true")) {
						getServicePool().installFromXmlFile(new File("DiskoServices.xml"));
						getServicePool().setAutomatic(AppProps.getText("SERVICES.automatic").equals("true"));
						getServicePool().setMaster(AppProps.getText("SERVICES.master").equals("true"));
					}
					// prepare reporting
					m_diskoReport = new DiskoReportManager(Application.this);
					// hide progress dialog
					getProgressMonitor().finish();
					// set loading bit
					setLoading(true);
					// forward
					getUIFactory().getLoginDialog().showLogin(true);
					
				}
				catch (Exception e)
				{
					onFatalError("Fatal error","Failed to initialize application instance",e);
				}
			}			
		});
	}

	void initializeArcGISLicenses()
	{
		try
		{
			// Initialize ArcObjects at appropriate product level.
			if (getArcGisSystem().isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) == com.esri.arcgis.system.esriLicenseStatus.esriLicenseAvailable)
			{
				getArcGisSystem().initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
			}
		}
		catch (Exception e)
		{
			onFatalError("Fatal error","Failed to initialize ArcGIS system",e);
		}
	}

	public DiskoKeyEventDispatcher getKeyEventDispatcher() {
		if(m_keyEventDispatcher==null) {
			// create new
			m_keyEventDispatcher=new DiskoKeyEventDispatcher();
			// pass through events from classes
			m_keyEventDispatcher.addPassed(JTextArea.class);
			m_keyEventDispatcher.addPassed(JTextField.class);
			m_keyEventDispatcher.addPassed(JFormattedTextField.class);
			// add to current KeyboardFocusManager
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
								.addKeyEventDispatcher(m_keyEventDispatcher);
		}
		return m_keyEventDispatcher;
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#getCurrentRole()
	 */
	public IDiskoRole getCurrentRole()
	{
		return m_currentRole;
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#getCurrentMap()
	 */
	public IDiskoMap getCurrentMap()
	{
		return getCurrentRole().getCurrentDiskoWpModule().getMap();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#getFrame()
	 */
	public JFrame getFrame()
	{
		if(m_frame==null) {
			m_frame = new JFrame();
				
		}
		return m_frame;
	}

	public boolean isLocked() {
		return (lockCount>0);
	}

	public void setLocked(boolean isLocked) {

		// adjust lock count
		if(isLocked)
			lockCount++;
		else if(lockCount>0)
			lockCount--;

		// set lock state
		switch(lockCount) {
		case 1:
			getKeyEventDispatcher().setEnabled(false);
			DiskoGlassPaneUtils.setLocked(true);
			break;
		case 0:
			getKeyEventDispatcher().setEnabled(!isLoading());
			DiskoGlassPaneUtils.setLocked(false);
			break;
		}

	}

	public boolean isLoading() {
		return isLoading;
	}
	
	public boolean isTouchMode() {
		return "TOUCH".equalsIgnoreCase(AppProps.getText(PROP_GUI_LAYOUT_MODE));
	}


	private void setLoading(boolean isLoading) {
		// update
		this.isLoading = isLoading;
		// disable global dispatcher?
		getKeyEventDispatcher().setEnabled(!(isLoading || isLocked()));
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#getUIFactory()
	 */
	public UIFactory getUIFactory()
	{
		if (m_uiFactory == null)
		{
			m_uiFactory = new UIFactory(this);
		}
		return m_uiFactory;
	}

	public NavMenu getNavMenu()
	{
		return getUIFactory().getNavMenu();
	}

	public SysMenu getSysMenu()
	{
		return getUIFactory().getSysMenu();
	}

	public IDiskoMapManager getMapManager()
	{
		if (m_mapManager == null)
		{
			try {
				m_mapManager = new DiskoMapManagerImpl(this, new File("MxdDocuments.xml"));
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to create MapManager instance",e);
			}
		}
		return m_mapManager;
	}

	public IDispatcherIf getDispatcher() {
		if(m_dispatcher == null) {
	        boolean integrate = AppProps.getText("integrate.sara").equalsIgnoreCase("true");
	        m_dispatcher = integrate ? new SaraDispatcherImpl() : new DispatcherImpl();
		}
		return m_dispatcher;
	}
	
	public IMsoModelIf getMsoModel()
	{
		if (m_msoModel == null)
		{
			try {
				m_msoModel = new MsoModelImpl(getDispatcher());
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to create Mso Model instance",e);
			}
		}
		return m_msoModel;
	}
	
	public IMsoTransactionManagerIf getTransactionManager() {
		return (IMsoTransactionManagerIf)getMsoModel();
	}
	
	public DiskoReportManager getReportManager(){
		return this.m_diskoReport;
	}

	public ServicePool getServicePool()
	{
		if (m_servicePool == null)
		{
			try {
				m_servicePool = ServicePool.getInstance();
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to get ServicePool instance",e);
			}
		}
		return m_servicePool;
	}
	
	public DsPool getDsPool()
	{
		if (m_dsPool == null)
		{
			try {
				m_dsPool = DsPool.getInstance();
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to get DsPool instance",e);
			}
		}
		return m_dsPool;
	}

	public IOManager getIOManager()
	{
		if (m_ioManager == null)
		{
			try {
				m_ioManager = IOManager.getInstance();
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to get IOManager instance",e);
			}
		}
		return m_ioManager;
	}
	
	public AoInitialize getArcGisSystem() 
	{
		if (arcGIS == null)
		{
			try
			{
				arcGIS = new AoInitialize();
				if (arcGIS.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) == com.esri.arcgis.system.esriLicenseStatus.esriLicenseAvailable)
				{
					arcGIS.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
				}
			}
			catch (Exception e)
			{
				onFatalError("Fatal error","Failed to initialize ArcGIS licenses",e);
			}
		}
		return arcGIS;
	}
	
	public ProgressMonitor getProgressMonitor()
	{
		if (m_progresMonitor == null)
		{
			try {
				m_progresMonitor = ProgressMonitor.getInstance();
			} catch (Exception e) {
				onFatalError("Fatal error","Failed to get Progress Monitor instance",e);
			}
		}
		return m_progresMonitor;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#login(java.lang.String, java.lang.String, char[])
	 */
	public boolean login(final String role, final String user, final char[] password)
	{

		// TODO: implement authorization
		boolean auth = true;

		if (SwingUtilities.isEventDispatchThread()) {
			// update login properties
			loggedIn[0] = role;
			loggedIn[1] = user;
			loggedIn[2] = password;
			// is model driver initiated?
			if (getMsoModel().getDispatcher().isInitiated()) {
				// forward
				selectActiveOperation(false);
			} else {
				// get maximum wait time
				long maxTime = Long.parseLong(getProperty("max.wait.time","" + 60 * 1000));
				// The model driver is not initiated. Schedule the initiation work.
				// If initiation is successful the active operation is choosen. If initiation fails,
				// the system will be shut down.
				scheduleInitiateModelDriver(maxTime, true, false);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					login(role, user, password);
				}
			});
		}

		// finished
		return auth;

	}

	/*
	 * (non-Javadoc)
	 * @see org.redcross.sar.IApplication#changeRole(java.lang.String, java.lang.String, char[])
	 */

	public boolean swapTo(final String role, final String user, final char[] password)
	{

		// TODO: implement authorization
		boolean auth = true;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// update login properties
				loggedIn[0] = role;
				loggedIn[1] = user;
				loggedIn[2] = password;
				// forward
				scheduleSetActiveRoleWorker(m_roles,m_currentRole,(String)loggedIn[0]);
			}
		});

		// finished
		return auth;

	}
		/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#chooseActiveOperation()
	 */
	public boolean selectActiveOperation(boolean prompt)
	{

		// initialize answer
		int ans = JOptionPane.YES_OPTION;

		// prompt user?
		if(prompt) {
			ans = Utils.showConfirm(bundle.getString(SWITCHACTION_TEXT),
					bundle.getString(SWITCHACTION_DESC),
					JOptionPane.YES_NO_OPTION);
		}


		// switch operation?
		if(ans == JOptionPane.YES_OPTION) {

			String oprId = Application.getInstance().getMsoModel().getDispatcher().getActiveOperationID();

			// forward
			return getUIFactory().getOperationDialog().selectOperation(isLoading() || oprId==null);

		}
		// failed
		return false;
	}

	public boolean activateOperation(String opId) {
		// set selected operation as active?
		if(opId!=null) {
			// schedule work
			scheduleSetActiveOperation(opId);
			// finished
			return true;
		}
		// failed
		return false;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#finishOperation()
	 */
	public void finishOperation()
	{
		String[] options = {bundle.getString("QUIT.APPLICATION.TEXT"), bundle.getString("QUIT.OPERATION.TEXT"), bundle.getString("QUIT.CANCEL.TEXT")};
		int ans = JOptionPane.showOptionDialog(
				m_uiFactory.getContentPanel(),
				bundle.getString(FINISH_TEXT),
				bundle.getString(FINISH_TITLE),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

		if(ans==JOptionPane.YES_OPTION) {
			shutdown();
		}
		else {
			if(ans==JOptionPane.NO_OPTION) {
				ans = JOptionPane.showOptionDialog(
						m_uiFactory.getContentPanel(),
						bundle.getString(CONFIRMATION_TEXT),
						bundle.getString(CONFIRMATION_TITLE),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
				if(ans==JOptionPane.YES_OPTION) {
					// forward
					getMsoModel().getDispatcher().finishActiveOperation();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#mergeOperations()
	 */
	public void mergeOperations()
	{
		Utils.showWarning("Beklager, fletting er foreløpig ikke støttet");
	}

	public boolean createOperation(boolean prompt)
	{
		// initialize
		int ans = JOptionPane.YES_OPTION;

		// prompt user?
		if(prompt) {
			ans = JOptionPane.showOptionDialog(
					m_uiFactory.getContentPanel(),
					bundle.getString(NEWACTION_DESC),
					bundle.getString(NEWACTION_TEXT),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null,null,null);
		}

		// create?
		if(ans==JOptionPane.YES_OPTION) {
			waitingForNewOp = true;
			getMsoModel().getDispatcher().createNewOperation();
			return true;
		}

		// failed
		return false;
	}



	public void shutdown()
	{
		// cleanup
		if(getArcGisSystem()!=null) {
			try {
				getArcGisSystem().shutdown();
			} catch (Exception e) {
				logger.error("Failed to shutdown ArcGIS system",e);
			}
		}
		if(getServicePool()!=null) {
			getServicePool().destroyAll();
		}
		if(getIOManager()!=null) {
			getIOManager().closeAll();
		}
		if(getDispatcher()!=null) {
			getDispatcher().shutdown();
		}
		// forward
		getFrame().dispose();
	}
	
	public void onFatalError(String title, String message) {
		onFatalError(title, message,null);
	}
	
	public void onFatalError(String title, String message, Throwable e) {
		// log error
		if(e==null) 
			logger.fatal(message); 
		else 
			logger.fatal(message,e);
		// notify
		Utils.showError(title, message);
		// shutdown application safely
		shutdown();		
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.IApplication#getDiskoModuleLoader()
	 */
	public DiskoModuleManager getModuleManager()
	{
		if (m_moduleLoader == null)
		{
			try
			{
				m_moduleLoader = new DiskoModuleManager(this,
						new File("DiskoModules.xml"));
			}
			catch (Exception e)
			{
				logger.fatal("Failed to create DiskoModuleManager instance",e);
			}
		}
		return m_moduleLoader;
	}

	public String getProperty(String key)
	{
		return AppProps.getText(key);
	}

	public String getProperty(String key, String defaultvalue)
	{
		String value = getProperty(key);
		if(value==null || value.isEmpty())
			value = defaultvalue;
		return value;
	}

	public boolean setProperty(String key, String value)
	{
		String oldValue = AppProps.getText(key);
		if(AppProps.setText(key,value)) {
			// notify
			getPropertyChangeSupport().firePropertyChange(key, oldValue, value);
			// success
			return true;
		}
		// failed
		return false;
	}

	private PropertyChangeSupport getPropertyChangeSupport() {
		if(m_propertyChangeSupport==null) {
			m_propertyChangeSupport = new PropertyChangeSupport(this);
		}
		return m_propertyChangeSupport;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		getPropertyChangeSupport().removePropertyChangeListener(listener);
	}

	private void fireBeforeOperationChange() {
		// notify current work processes?
		if(m_currentRole!=null)
			m_currentRole.fireBeforeOperationChange();
	}

	private void fireAfterOperationChange() {

		// auto select map from operation
		getMapManager().selectMap(isLocked(),true);

		// start executing all pending map work in the background
		getMapManager().execute(false);

		// notify current work processes?
		if(m_currentRole!=null) {
			m_currentRole.fireAfterOperationChange();
		}

	}

	/* ===============================================================================
	 * Internal classes
	 * =============================================================================== */

	private void scheduleInitiateModelDriver(long millisToWait, boolean choose, boolean prompt) {
		try {
			WorkPool.getInstance().schedule(new InitiateModelDriver(millisToWait,true,false));
			return;
		}
		catch(Exception e) {
			logger.fatal("Failed to schedule InitiateModelDriver instance",e);
		}
		// model driver is not initiated. Display message to user and shut down
		onFatalError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
	}

	class InitiateModelDriver extends AbstractWork {

		private boolean m_choose = false;
		private boolean m_prompt = false;
		private long m_millisToWait = 0;
		private IMsoModelIf m_msoModel = null;

		/**
		 * Constructor
		 *
		 * @param model
		 * @param millisToWait
		 * @param choose
		 * @param prompt
		 */
		InitiateModelDriver(long millisToWait, boolean choose, boolean prompt) throws Exception {
			// forward
			super(0,false,true,ThreadType.WORK_ON_SAFE,"Henter aksjonsliste",0,true,true);
			// prepare objects
			m_msoModel = getMsoModel();
			m_choose = choose;
			m_prompt = prompt;
			m_millisToWait = millisToWait;
		}

		/**
		 * Worker
		 *
		 * Wait until one or more active operations are present in the model driver. There
		 * should always be at least one active operation. If not, the model driver is in a
		 * error state and the system must be shut down.
		 */
		@Override
		public Boolean doWork() {
			long tic = System.currentTimeMillis();
			long schedule = tic;

			try {
				// loop until maximum milliseconds is reached
				while ((tic - schedule) < m_millisToWait) {
					// is initiated
					if(m_msoModel.getDispatcher().isInitiated())
						//Get back onto awt thread
						return true;
					else
						// still not initiated
						Thread.sleep(200);
					// get current time tic
					tic = System.currentTimeMillis();
				}
				// failed to initiate model driver (timeout)
				return false;
			}
			catch (Exception e)
			{
				logger.error("Failed to execute InitiateModelDriver work",e);
			}
			return false;
		}

		public Boolean get() {
			return (Boolean)super.get();
		}

		/**
		 * done
		 *
		 * Executed on the Event Dispatch Thread.
		 */
		@Override
		public void afterDone() {

			try {

				// get result
				boolean hasActive = get();

				// choose active operation
				if(hasActive && m_choose)
					selectActiveOperation(m_prompt);

				// failed?
				if(!hasActive) {
					// model driver is not initiated. Display message to user and shut down
					onFatalError("Fatal error",bundle.getString(INIT_ERROR_SHUTDOWN_TEXT));
				}
				// finished
				return;
			}
			catch(Exception e) {
				onFatalError("Fatal error","Failed to execute InitiateModelDriver instance",e);
			}
			// model driver is not initiated. Display message to user and shut down
			onFatalError("Fatal error",bundle.getString(WORK_ERROR_TEXT));
		}

	}

	private void scheduleSetActiveOperation(String opID) {
		try {
			WorkPool.getInstance().schedule(new SetActiveOperation(opID));
			return;
		}
		catch(Exception e) {
			logger.fatal("Failed to schedule SetActiveOperation instance",e);
		}
		// force finish progress dialog
		getProgressMonitor().finish(true);
		// model driver is not initiated. Display message to user and shut down
		onFatalError(bundle.getString(WORK_ERROR_TITLE),bundle.getString(WORK_ERROR_TEXT));
	}

	class SetActiveOperation extends AbstractWork {

		private String m_opID = null;

		/**
		 * Constructor
		 *
		 * @param opID
		 */
		SetActiveOperation(String opId) throws Exception {
			// forward
			super(0,false,true,ThreadType.WORK_ON_SAFE,"Kobler til aksjon " + IDHelper.formatOperationID(opId),0,true,true);
			// save
			m_opID = opId;
			// set loading bit
			setLoading(true);
		}

		@Override
		public void afterPrepare() {
			// notify
			fireBeforeOperationChange();
		}

		/**
		 * Worker
		 *
		 * Sets the active operation in a worker thread.
		 */
		@Override
		public Boolean doWork() {

			// catch any exceptions
			try  {
				if(m_opID!=null)
				{

					// return status
					boolean flag = Application.getInstance().getMsoModel().getDispatcher().setActiveOperation(m_opID);

					// return flag
					return flag;
				}

			}
			catch (Exception e) {
				logger.error("Failed to execute SetActiveOperation work",e);
			}
			// failed
			return false;
		}

		public Boolean get() {
			return (Boolean)super.get();
		}

		/**
		 * done
		 *
		 * Executed on the Event Dispatch Thread.
		 */
		@Override
		public void afterDone() {

			try {

				// get result
				if(get()) {
					// choose active operation?
					if(m_currentRole==null)
						scheduleSetActiveRoleWorker(m_roles,m_currentRole,(String)loggedIn[0]);
				}

				// notify
				fireAfterOperationChange();

				// set loading false
				setLoading(false);

				// finished
				return;
			}
			catch(Exception e) {
				logger.fatal("Failed to execute SetActiveOperation work",e);
			}
			// force finish progress dialog
			getProgressMonitor().finish(true);
			// model driver is not initiated. Display message to user and shut down
			onFatalError("Fatal error",bundle.getString(WORK_ERROR_TEXT));
		}
	}

	private void scheduleSetActiveRoleWorker(Hashtable<String,
			IDiskoRole> roles, IDiskoRole current, String loginRole) {
		try {
			WorkPool.getInstance().schedule(new SetActiveRoleWorker(roles,current,loginRole));
			return;
		}
		catch(Exception e) {
			logger.fatal("Failed to schedule SetActiveRoleWorker work",e);
		}
		// force finish progress dialog
		getProgressMonitor().finish(true);
		// model driver is not initiated. Display message to user and shut down
		onFatalError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
	}

	class SetActiveRoleWorker extends AbstractWork {

		private String loginRole = null;
		private IDiskoRole currentRole = null;
		private Hashtable<String, IDiskoRole> roles = null;

		/**
		 * Constructor
		 *
		 * @param roles
		 * @param current
		 * @param loginRole
		 */
		SetActiveRoleWorker(Hashtable<String, IDiskoRole> roles,
				IDiskoRole current, String loginRole) throws Exception {
			// forward
			super(0,false,true,ThreadType.WORK_ON_SAFE,"Aktiverer rolle",0,true,true);
			// prepare
			this.roles = roles;
			this.currentRole = current;
			this.loginRole = loginRole;
			// hide all system component
			getUIFactory().hideAll();
		}

		/**
		 * Worker
		 *
		 * Loads specific role into the application. This is a lengthy operation
		 */
		@Override
		public Boolean doWork()
		{
			// initiate list?
			if(roles==null)
				roles = new Hashtable<String, IDiskoRole>();

			// parse role?
			if (!roles.containsKey(loginRole)){
				// get role from module manager
				currentRole = getModuleManager().parseRole(loginRole);
				// put to table
				roles.put(loginRole, currentRole);
			}
			else {
				// get role from list
				currentRole = roles.get(loginRole);
				// activate current module
				currentRole.selectDiskoWpModule(currentRole.getCurrentDiskoWpModule());
			}

			// success
			return true;
		}

		public Boolean get() {
			return (Boolean)super.get();
		}

		/**
		 * done
		 *
		 * Executed on the Event Dispatch Thread.
		 *
		 */
		@Override
		public void afterDone() {
			try {

				// success?
				if(get()) {
					// update roles table
					Application.this.m_roles = this.roles;
					// update current role
					Application.this.m_currentRole = this.currentRole;
					// show role specific menu
					getUIFactory().getMainMenu().showMenu(currentRole.getName());
					// activate work process
					if(currentRole.getCurrentDiskoWpModule()==null)
						currentRole.selectDiskoWpModule(currentRole.getDefaultDiskoWpModule());
					else
						currentRole.getCurrentDiskoWpModule().activate(currentRole);
					// select first module
					getUIFactory().showAll();
				}
				// finished
				return;
			}
			catch(Exception e) {
				logger.fatal("Failed to execute SetActiveRoleWorker work",e);
			}
			// force finish progress dialog
			getProgressMonitor().finish(true);
			// model driver is not initiated. Display message to user and shut down
			onFatalError("Fatal error",bundle.getString(WORK_ERROR_TEXT));
		}
	}

	@Override
	public boolean invoke(Enum<?> cmd, boolean requestFocus) {
		if(cmd instanceof MapCommandType ||
		   cmd instanceof MapToolType) {
			AbstractButton b = getNavMenu().getButton(cmd);
			if(b!=null) {
				boolean hasFocus = b.hasFocus();
				boolean isFocusable = b.isFocusable();
				b.setFocusable(false);
				b.doClick();
				b.setFocusable(isFocusable);
				if(requestFocus || hasFocus) b.requestFocusInWindow();
				return true;
			}
		}
		return false;
	}

	private final IDispatcherListenerIf m_dispatcherAdapter = new DispatcherAdapter() {

		public void onOperationCreated(final String oprId, final boolean current)
		{

			// only handle if current
			if(!current) return;

			if (SwingUtilities.isEventDispatchThread()) {
				// is waiting for this operation
				if (waitingForNewOp) {
					// reset flag
					waitingForNewOp = false;
					// notify user of new operation created?
					if (!isLocked())
						Utils.showMessage(String.format(bundle
								.getString(OPERATION_CREATED_TEXT), oprId));
					// schedule work
					scheduleSetActiveOperation(oprId);
				}
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						onOperationCreated(oprId,current);
					}
				});
			}
		}

		public void onOperationFinished(final String oprID, final boolean current)
		{
			// only handle if current
			if(!current) return;

			if (SwingUtilities.isEventDispatchThread()) {
				// force finish progress dialog
				getProgressMonitor().finish(true);
				// get active operations
				List<String[]> opList = getMsoModel().getDispatcher().getActiveOperations();
				// prompt user for actions
				String[] options = { bundle.getString("QUIT.APPLICATION.TEXT"),
						bundle.getString(CHOOSETEXT),
						bundle.getString("NEWACTION.TEXT") };
				int ans = JOptionPane.showOptionDialog(m_uiFactory.getContentPanel(),
						bundle.getString(OPERATION_FINISHED_TEXT), bundle
								.getString(OPERATION_FINISHED_TITLE),
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				// user choose to exit the application
				if (ans == JOptionPane.YES_OPTION) {
					shutdown();
				} else if (ans == JOptionPane.NO_OPTION) {

					// the user choose to select another active operation (if it exists)
					if (opList.size() > 0) {
						// choose operation without prompt
						selectActiveOperation(false);
					} else {
						// get maximum wait time
						long maxTime = Long.parseLong(getProperty("max.wait.time",
								"" + 60 * 1000));
						// add work to work pool. If initiation succeeds, the active operation
						// is choosen. If initiation fails, the system will be shut down.
						scheduleInitiateModelDriver(maxTime, true, false);
					}
				} else
					// forward
					createOperation(false);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						onOperationFinished(oprID,current);
					}
				});
			}
		}

	};

}  // @jve:decl-index=0:visual-constraint="10,10"
