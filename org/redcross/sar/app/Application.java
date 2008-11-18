package org.redcross.sar.app;

import no.cmr.tools.Log;
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
import org.redcross.sar.modeldriver.IModelDriverListenerIf;
import org.redcross.sar.modeldriver.ModelDriverAdapter;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.ProgressMonitor;
import org.redcross.sar.work.WorkPool;

import com.esri.arcgis.system.EngineInitializer;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Hashtable;
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
 * @author geira
 */
public class Application extends JFrame implements IApplication, WindowListener
{
	private static final ResourceBundle bundle = Internationalization.getBundle(IApplication.class);
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

	private IDiskoRole m_currentRole;
	private Hashtable<String, IDiskoRole> m_roles;
	private DiskoModuleManager m_moduleLoader;
	private UIFactory m_uiFactory;
	private IDiskoMapManager mapManager;
	private MsoModelImpl m_msoModel;
	private DiskoReportManager m_diskoReport;
	private DiskoKeyEventDispatcher m_keyEventDispatcher;
	private ServicePool m_servicePool;

	// counters
	private int lockCount = 0;

	// flags
	private boolean isLoading = false;
	private boolean waitingForNewOp = false;

	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		// prepare
		EngineInitializer.initializeVisualBeans();
		UIFactory.initLookAndFeel();

		// initialize GUI on new thread
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				Application thisClass = new Application();
				thisClass.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //EXIT_ON_CLOSE);
			}
		});
	}

	private Object[] loggedin=new Object[3];

	/**
	 * This is the default constructor
	 */
	public Application()
	{
		super();
		// register me
		Utils.setApp(this);
		// initialize ArcGIS
		initializeArcGISLicenses();
		// initialize GUI
		initialize();
	}

	private void initialize()
	{
		try
		{
			// initialize logging
			Log.init("DISKO");
			// set title
			this.setTitle("DISKO");
			// set glass pane
			this.setGlassPane(getUIFactory().getGlassPane());
            // set content panel
			this.setContentPane(getUIFactory().getContentPanel());
			// apply size and layout
			this.getFrame().setPreferredSize(new Dimension(1024,768));
			this.pack();
			// show extended
			this.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
			// add this as window listener
			this.addWindowListener(this);
			// initialize work pool to ensure that this is done on the EDT
			WorkPool.getInstance();
			// initialize decision support to ensure that this is done on the EDT
			DsPool.getInstance();
			// show me
			this.setVisible(true);
			//initiate model driver
			this.getMsoModel().getModelDriver().initiate();
			this.getMsoModel().getModelDriver().addModelDriverListener(m_driverAdapter);
			// load services?
			if(AppProps.getText("SERVICES.initialize").equals("true")) {
				this.getServices().installFromXmlFile(new File("DiskoServices.xml"));
				this.getServices().setAutomatic(AppProps.getText("SERVICES.automatic").equals("true"));
				this.getServices().setMaster(AppProps.getText("SERVICES.master").equals("true"));
			}
			// prepare reporting
			m_diskoReport = new DiskoReportManager(this);
			// set loading bit
			setLoading(true);
			// forward
			getUIFactory().getLoginDialog().showLogin(true);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void initializeArcGISLicenses()
	{
		try
		{
			com.esri.arcgis.system.AoInitialize ao = new com.esri.arcgis.system.AoInitialize();
			if (ao.isProductCodeAvailable(com.esri.arcgis.system.esriLicenseProductCode.esriLicenseProductCodeEngine) == com.esri.arcgis.system.esriLicenseStatus.esriLicenseAvailable)
			{
				ao.initialize(com.esri.arcgis.system.esriLicenseProductCode.esriLicenseProductCodeEngine);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
	 * @see org.redcross.sar.app.IApplication#getCurrentRole()
	 */
	public IDiskoRole getCurrentRole()
	{
		return m_currentRole;
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#getCurrentMap()
	 */
	public IDiskoMap getCurrentMap()
	{
		return getCurrentRole().getCurrentDiskoWpModule().getMap();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#getFrame()
	 */
	public JFrame getFrame()
	{
		return this;
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

	private void setLoading(boolean isLoading) {
		// update
		this.isLoading = isLoading;
		// disable global dispatcher?
		getKeyEventDispatcher().setEnabled(!(isLoading || isLocked()));
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#getUIFactory()
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

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#getDiskoMapManager()
	 */
	public IDiskoMapManager getMapManager()
	{
		if (mapManager == null)
		{
			try {
				mapManager = new DiskoMapManagerImpl(this,
						new File("MxdDocuments.xml"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mapManager;
	}

	public IMsoModelIf getMsoModel()
	{
		if (m_msoModel == null)
		{
			m_msoModel = MsoModelImpl.getInstance();
		}
		return m_msoModel;
	}

	public DiskoReportManager getReportManager(){
		return this.m_diskoReport;
	}

	public ServicePool getServices()
	{
		if (m_servicePool == null)
		{
			try {
				m_servicePool = ServicePool.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_servicePool;
	}

	public void windowClosing(WindowEvent e) {
		// consume?
		if(isLoading())
			Utils.showWarning("Vennligst vent til lasting er ferdig");
		else
			finishOperation();
	}

	public void windowClosed(WindowEvent e) {
		// NOP
	}

	public void windowOpened(WindowEvent e) {
		// NOP
	}

	public void windowIconified(WindowEvent e) {
		// NOP
	}

	public void windowDeiconified(WindowEvent e) {
		// NOP
	}

	public void windowActivated(WindowEvent e) {
		// NOP
	}

	public void windowDeactivated(WindowEvent e) {
		// NOP
	}

	public void windowGainedFocus(WindowEvent e) {
		// NOP
	}

	public void windowLostFocus(WindowEvent e) {
		// NOP
	}

	public void windowStateChanged(WindowEvent e) {
		// NOP
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#login(java.lang.String, java.lang.String, char[])
	 */
	public boolean login(final String role, final String user, final char[] password)
	{

		// TODO: implement authorization
		boolean auth = true;

		if (SwingUtilities.isEventDispatchThread()) {
			// update login properties
			loggedin[0] = role;
			loggedin[1] = user;
			loggedin[2] = password;
			// is model driver initiated?
			if (getMsoModel().getModelDriver().isInitiated()) {
				// forward
				selectActiveOperation(false);
			} else {
				// get maximum wait time
				long maxTime = Long.parseLong(getProperty("max.wait.time",
						"" + 60 * 1000));
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
	 * @see org.redcross.sar.app.IApplication#changeRole(java.lang.String, java.lang.String, char[])
	 */

	public boolean swapTo(final String role, final String user, final char[] password)
	{

		// TODO: implement authorization
		boolean auth = true;

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// update login properties
				loggedin[0] = role;
				loggedin[1] = user;
				loggedin[2] = password;
				// forward
				scheduleSetActiveRoleWorker(m_roles,m_currentRole,(String)loggedin[0]);
			}
		});

		// finished
		return auth;

	}
		/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#chooseActiveOperation()
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

			String oprId = Utils.getApp().getMsoModel().getModelDriver().getActiveOperationID();

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
	 * @see org.redcross.sar.app.IApplication#finishOperation()
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
					getMsoModel().getModelDriver().finishActiveOperation();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#mergeOperations()
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
			getMsoModel().getModelDriver().createNewOperation();
			return true;
		}

		// failed
		return false;
	}



	public void shutdown()
	{
		// forward
		dispose();
		getMsoModel().getModelDriver().shutDown();

	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IApplication#getDiskoModuleLoader()
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		return AppProps.setText(key,value);
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
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
		shutdown();
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
					if(m_msoModel.getModelDriver().isInitiated())
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
				e.printStackTrace();
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
					Utils.showError(bundle.getString(INIT_ERROR_SHUTDOWN_TEXT));
					shutdown();
				}
				// finished
				return;
			}
			catch(Exception e) {
				// print stack trace
				e.printStackTrace();
			}
			// model driver is not initiated. Display message to user and shut down
			Utils.showError(bundle.getString(WORK_ERROR_TEXT));
			shutdown();
		}

	}

	private void scheduleSetActiveOperation(String opID) {
		try {
			WorkPool.getInstance().schedule(new SetActiveOperation(opID));
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// force finish progress dialog
		try {
			ProgressMonitor.getInstance().finish(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(
				bundle.getString(WORK_ERROR_TITLE),
				bundle.getString(WORK_ERROR_TEXT));
		shutdown();
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
			super(0,false,true,ThreadType.WORK_ON_SAFE,"Kobler til aksjon " + opId,0,true,true);
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
					boolean flag = MsoModelImpl.getInstance().getModelDriver().setActiveOperation(m_opID);

					// return flag
					return flag;
				}

			}
			catch (Exception e) {
				e.printStackTrace();
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
						scheduleSetActiveRoleWorker(m_roles,m_currentRole,(String)loggedin[0]);
				}

				// notify
				fireAfterOperationChange();

				// set loading false
				setLoading(false);

				// finished
				return;
			}
			catch(Exception e) {
				// print stack trace
				e.printStackTrace();
			}
			// force finish progress dialog
			try {
				ProgressMonitor.getInstance().finish(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// model driver is not initiated. Display message to user and shut down
			Utils.showError(bundle.getString(WORK_ERROR_TEXT));
			shutdown();
		}
	}

	private void scheduleSetActiveRoleWorker(Hashtable<String,
			IDiskoRole> roles, IDiskoRole current, String loginRole) {
		try {
			WorkPool.getInstance().schedule(new SetActiveRoleWorker(roles,current,loginRole));
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// force finish progress dialog
		try {
			ProgressMonitor.getInstance().finish(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
		shutdown();
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
				// print stack trace
				e.printStackTrace();
			}
			// force finish progress dialog
			try {
				ProgressMonitor.getInstance().finish(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// model driver is not initiated. Display message to user and shut down
			Utils.showError(bundle.getString(WORK_ERROR_TEXT));
			shutdown();
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

	private final IModelDriverListenerIf m_driverAdapter = new ModelDriverAdapter() {

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
				// force finish progress
				try {
					ProgressMonitor.getInstance().finish(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// get active operations
				java.util.List<String[]> opList = getMsoModel().getModelDriver()
						.getActiveOperations();
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
