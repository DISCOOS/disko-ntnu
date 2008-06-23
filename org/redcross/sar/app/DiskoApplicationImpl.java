package org.redcross.sar.app;

import no.cmr.tools.Log;
import org.redcross.sar.app.Utils;
import org.redcross.sar.ds.DiskoDecisionSupport;
import org.redcross.sar.ds.ete.RouteCostEstimator;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.DiskoKeyEventDispatcher;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.SysBarPanel;
import org.redcross.sar.map.DiskoMapManagerImpl;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoProgressMonitor;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.GlobalProps;
import org.redcross.sar.util.Internationalization;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

/**
 * Implements the DiskoApplication interface. This class is responsible for connecting to the
 * ArcGIS Engine API and the DiskoModule API.
 */

/**
 * @author geira
 */
public class DiskoApplicationImpl extends JFrame implements IDiskoApplication, WindowListener 
{
	private static final ResourceBundle bundle = 
		Internationalization.getBundle(IDiskoApplication.class);
	private static final String CONFIRMATION_TEXT = "CONFIRMATION.TEXT";
	private static final String CONFIRMATION_TITLE = "CONFIRMATION.HEADER";
	private static final String CHOOSETEXT = "CHOOSE.OP.TEXT";
	private static final String WORK_ERROR_TEXT = "WORK.ERROR.TEXT";
	private static final String WORK_ERROR_TITLE = "WORK.ERROR.TITLE";
	private static final String INIT_ERROR_TEXT = "INIT.ERROR.TEXT";
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
	private static final String OPERATION_CREATED_TITLE = "OPERATION.CREATED.HEADER";

	private static final long serialVersionUID = 1L;
	
	private IDiskoRole currentRole = null;
	private Hashtable<String, IDiskoRole> roles = null;
	private DiskoModuleManager moduleLoader = null;
	private UIFactory uiFactory = null;
	private IDiskoMapManager mapManager = null;
	private MsoModelImpl m_msoModel = null;
	private DiskoReportManager diskoReport = null;
	private DiskoKeyEventDispatcher keyEventDispatcher = null;
	
	// flags
	private boolean isLoading = false;
	private boolean waitingForNewOp=false;
	
	/**
	 * The main method.
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		com.esri.arcgis.system.EngineInitializer.initializeVisualBeans();
		initLookAndFeel();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				DiskoApplicationImpl thisClass = new DiskoApplicationImpl();
				thisClass.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); //EXIT_ON_CLOSE);
			}
		});
	}

	private Object[] loggedin=new Object[3];
	private final static String DefaultFont = "Tahoma";
	private final static String DialogFont = "Dialog";

	private final static int MediumSize = 14;
	private final static int LargeSize = 16;

	private final static Font DEFAULT_BOLD_SMALL_FONT = new Font(DefaultFont, Font.PLAIN, 10);

	private final static Font DEFAULT_PLAIN_MEDIUM_FONT = new Font(DefaultFont, Font.PLAIN, MediumSize);
	//private final static Font DEFAULT_BOLD_MEDIUM_FONT = new Font(DefaultFont,Font.BOLD,MediumSize);
	private final static Font DEFAULT_PLAIN_LARGE_FONT = new Font(DefaultFont, Font.PLAIN, LargeSize);
	//private final static Font DEFAULT_BOLD_LARGE = new Font(DefaultFont,Font.BOLD,LargeSize);

	private final static Font DIALOG_PLAIN_MEDIUM_FONT = new Font(DialogFont, Font.PLAIN, MediumSize);

	private static void initLookAndFeel()
	{
		try
		{
		
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			//UIManager.put("Button.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Button.font", DEFAULT_BOLD_SMALL_FONT);
			UIManager.put("CheckBox.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("CheckBoxMenuItem.acceleratorFont", DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("CheckBoxMenuItem.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("ColorChooser.font", DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("ComboBox.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("EditorPane.font", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("FileChooser.listFont", DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("FormattedTextField.font", DEFAULT_PLAIN_MEDIUM_FONT);
//			UIManager.put("InternalFrame.titleFont",font Trebuchet MS,bold,13);
			UIManager.put("Label.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("List.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Menu.acceleratorFont",DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("Menu.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuItem.acceleratorFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("MenuItem.font",DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("OptionPane.buttonFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("OptionPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("OptionPane.messageFont",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Panel.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("PasswordField.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("PopupMenu.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ProgressBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("RadioButton.font",DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("RadioButtonMenuItem.acceleratorFont",DIALOG_PLAIN_MEDIUM_FONT);
			UIManager.put("RadioButtonMenuItem.font", DEFAULT_PLAIN_LARGE_FONT);
			UIManager.put("ScrollPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Slider.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Spinner.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TabbedPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Table.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TableHeader.font",DEFAULT_PLAIN_MEDIUM_FONT);
			//          UIManager.put("TextArea.font",font Monospaced,plain,13);
			UIManager.put("TextField.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TextPane.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("TitledBorder.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToggleButton.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToolBar.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("ToolTip.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Tree.font",DEFAULT_PLAIN_MEDIUM_FONT);
			UIManager.put("Viewport.font",DEFAULT_PLAIN_MEDIUM_FONT);

			UIManager.put("ScrollBar.width", 25);
			
			// Because DISCO and ArcGIS Objects are mixing heavyweight (AWT.*) 
			// and lightweight (Swing.*) component, default lightweight behaviors
			// must be turned off. If not, components JPopup and JTooltips
			// will be shown below ArcGIS AWT components (MapBean etc.		
			// IMPORTANT: Do not put ArcGIS components which is implemented using
			// AWT in a JScrollPane. This will not work correctly. Instead, 
			// use an AWT ScrollPane.
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);			
			ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);						

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * This is the default constructor
	 */
	public DiskoApplicationImpl()
	{
		super();
		// registrate me
		Utils.setApp(this);
		// initialze ArcGIS
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
			// get disko glass pane
			DiskoGlassPane glassPane = getUIFactory().getGlassPane();
			// set glass pane
			this.setGlassPane(getUIFactory().getGlassPane());
			// get event listener
            AWTEventListener al = (AWTEventListener)glassPane;
            // add glass pane as mouse listener
            Toolkit.getDefaultToolkit().addAWTEventListener(al,
                    AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
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
			DiskoWorkPool.getInstance();
			// initialize decision support to ensure that this is done on the EDT
			DiskoDecisionSupport.getInstance();
			// show me
			this.setVisible(true);
			//initiate modeldriver
			this.getMsoModel().getModelDriver().initiate();			
			this.getMsoModel().getModelDriver().setDiskoApplication(this);
			// prepare reporting
			diskoReport = new DiskoReportManager(this);
			// set loading bit
			setLoading(true);
			// forward
			getUIFactory().getLoginDialog().showLogin(true);
			// show login later (this allows the main application frame to show first)
			//SwingUtilities.invokeLater(new Runnable() {public void 
			//	run() { getUIFactory().getLoginDialog().showLogin(true); }}); 
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
		if(keyEventDispatcher==null) {
			// create new
			keyEventDispatcher=new DiskoKeyEventDispatcher();
			// pass through events from classes
			keyEventDispatcher.addPassed(JTextArea.class);
			keyEventDispatcher.addPassed(JTextField.class);
			keyEventDispatcher.addPassed(JFormattedTextField.class);
			// add to current KeyboardFocusManager
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
								.addKeyEventDispatcher(keyEventDispatcher);
		}
		return keyEventDispatcher;
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getCurrentRole()
	 */
	public IDiskoRole getCurrentRole()
	{
		return currentRole;
	}


	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getCurrentMap()
	 */
	public IDiskoMap getCurrentMap()
	{
		return getCurrentRole().getCurrentDiskoWpModule().getMap();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getFrame()
	 */
	public JFrame getFrame()
	{
		return this;
	}
	
	public boolean isLocked() {
		return ((DiskoGlassPane)getFrame().getGlassPane()).isLocked();
	}
	
	public boolean setLocked(boolean isLocked) {
		getKeyEventDispatcher().setEnabled(!(isLocked || isLoading()));
		return ((DiskoGlassPane)getFrame().getGlassPane()).setLocked(isLocked);
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
	 * @see org.redcross.sar.app.IDiskoApplication#getUIFactory()
	 */
	public UIFactory getUIFactory()
	{
		if (uiFactory == null)
		{
			uiFactory = new UIFactory(this);
		}
		return uiFactory;
	}

	public NavBarPanel getNavBar()
	{
		return getUIFactory().getMainPanel().getNavBar();
	}

	public SysBarPanel getSysBar()
	{
		return getUIFactory().getMainPanel().getSysBar();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getDiskoMapManager()
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
		return this.diskoReport;
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
	 * @see org.redcross.sar.app.IDiskoApplication#login(java.lang.String, java.lang.String, char[])
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
				doInitiateModelDriver(maxTime, true, false);
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
	 * @see org.redcross.sar.app.IDiskoApplication#changeRole(java.lang.String, java.lang.String, char[])
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
				doSetActiveRoleWorker(roles,currentRole,(String)loggedin[0]);
			}
		});
		
		// finished
		return auth;
		
	}
		/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#chooseActiveOperation()
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

			// forward
			return getUIFactory().getOperationDialog().selectOperation(isLoading());
			
		}
		// failed
		return false;
	}
	
	public boolean activeOperation(String opId) {
		// set selected operation as active?
		if(opId!=null) {
			// schedule work
			doSetActiveOperation(opId);
			// finished
			return true;
		}		
		// failed
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#finishOperation()
	 */
	public void finishOperation()
	{
		String[] options = {bundle.getString("QUIT.APPLICATION.TEXT"), bundle.getString("QUIT.OPERATION.TEXT"), bundle.getString("QUIT.CANCEL.TEXT")};
		int ans = JOptionPane.showOptionDialog(
				uiFactory.getContentPanel(),
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
						uiFactory.getContentPanel(),
						bundle.getString(CONFIRMATION_TEXT),
						bundle.getString(CONFIRMATION_TITLE),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE,
						null,
						null,
						null);
				if(ans==JOptionPane.YES_OPTION) {
					getMsoModel().getModelDriver().finishActiveOperation();
				}
			}
		}
	}

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
				doSetActiveOperation(oprId);
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
				DiskoProgressMonitor.getInstance().finish(true);
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
			int ans = JOptionPane.showOptionDialog(uiFactory.getContentPanel(),
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
					doInitiateModelDriver(maxTime, true, false);
				}
			} else
				// forward
				createOperation();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					onOperationFinished(oprID,current);
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#mergeOperations()
	 */
	public void mergeOperations()
	{
		Utils.showWarning("Beklager, fletting er foreløpig ikke støttet");
	}

	public boolean createOperation()
	{
		int ans = JOptionPane.showOptionDialog(
				uiFactory.getContentPanel(),
				bundle.getString(NEWACTION_DESC),
				bundle.getString(NEWACTION_TEXT),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null,null,null);

		if(ans==JOptionPane.YES_OPTION) {
			waitingForNewOp=true;
			getMsoModel().getModelDriver().createNewOperation();
			return true;
		}
		return false;		
	}



	public void shutdown()
	{
		// forward
		dispose();
		getMsoModel().getModelDriver().shutDown();

	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getDiskoModuleLoader()
	 */
	public DiskoModuleManager getModuleManager()
	{
		if (moduleLoader == null)
		{
			try
			{
				moduleLoader = new DiskoModuleManager(this,
						new File("DiskoModules.xml"));
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return moduleLoader;
	}

	public String getProperty(String key)
	{
		return GlobalProps.getText(key);
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
		return GlobalProps.setText(key,value);
	}
	
	private void fireBeforeOperationChange() {
		// notify current work processes?
		if(currentRole!=null)
			currentRole.fireBeforeOperationChange();
	}

	private void fireAfterOperationChange() {
		// show selection dialog later...
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// auto select map from operation
				getMapManager().selectMap(isLocked());
			}
		});
		// notify current work processes?
		if(currentRole!=null) {
			currentRole.fireAfterOperationChange();
		}
		
	}
	
	private void doInitiateModelDriver(long millisToWait, boolean choose, boolean prompt) {
		try {
			DiskoWorkPool.getInstance().schedule(new InitiateModelDriver(millisToWait,true,false));
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
		shutdown();				
	}
	
	class InitiateModelDriver extends AbstractDiskoWork<Boolean> {

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
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Henter aksjonsliste",100,true,true,false,0);
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
			long start = tic;
			
			try {
				// loop until maximum milliseconds is reached
				while ((tic - start) < m_millisToWait) {
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

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 */
		@Override
		public void done() {

			try {
				
				// forward
				super.done();
				
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

	private void doSetActiveOperation(String opID) {
		try {
			DiskoWorkPool.getInstance().schedule(new SetActiveOperation(opID));
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// force finish progress dialog
		try {
			DiskoProgressMonitor.getInstance().finish(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(
				bundle.getString(WORK_ERROR_TITLE), 
				bundle.getString(WORK_ERROR_TEXT));
		shutdown();				
	}
	
	class SetActiveOperation extends AbstractDiskoWork<Boolean> {

		private String m_opID = null;

		/**
		 * Constructor
		 * 
		 * @param opID
		 */
		SetActiveOperation(String opId) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Kobler til aksjon " + opId,100,true,true,false,0);
			// save
			m_opID = opId;
			// set loading bit
			setLoading(true);
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
															
					// notify 
					fireBeforeOperationChange();
										
					// return status
					boolean flag = MsoModelImpl.getInstance().getModelDriver().setActiveOperation(m_opID);
					
					// notify
					fireAfterOperationChange();
					
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

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 */
		@Override
		public void done() {
			
			try {
				// forward
				super.done();	
				// get result
				if(get()) {
					// choose active operation?
					if(currentRole==null)
						doSetActiveRoleWorker(roles,currentRole,(String)loggedin[0]);
				}
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
				DiskoProgressMonitor.getInstance().finish(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// model driver is not initiated. Display message to user and shut down
			Utils.showError(bundle.getString(WORK_ERROR_TEXT));
			shutdown();				
		}		
	}

	private void doSetActiveRoleWorker(Hashtable<String, 
			IDiskoRole> roles, IDiskoRole current, String loginRole) {
		try {
			DiskoWorkPool.getInstance().schedule(new SetActiveRoleWorker(roles,current,loginRole));
			return;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// force finish progress dialog
		try {
			DiskoProgressMonitor.getInstance().finish(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// model driver is not initiated. Display message to user and shut down
		Utils.showError(bundle.getString(WORK_ERROR_TITLE), bundle.getString(WORK_ERROR_TEXT));
		shutdown();				
	}
	
	class SetActiveRoleWorker extends AbstractDiskoWork<Boolean> {

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
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Aktiverer rolle",100,true,true,false,0);
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
		 * Loads spesific role into the application. This is a lengthy operation
		 */	   
		@Override
		public Boolean doWork()
		{
			try {
				
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
			catch(Exception e) {
				e.printStackTrace();
				
			}
			// failed
			return false;
		}
	
		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 * 
		 */
		@Override
		public void done() {
			try {
				// forward
				super.done();	
				// success?
				if(get()) {
					// update roles table
					DiskoApplicationImpl.this.roles = this.roles;
					// update current role
					DiskoApplicationImpl.this.currentRole = this.currentRole;
					// show role spesific menu
					getUIFactory().getMainMenuPanel().showMenu(currentRole.getName());					
					// activate work process
					if(currentRole.getCurrentDiskoWpModule()==null)
						currentRole.selectDiskoWpModule(currentRole.getDefaultDiskoWpModule());						
					else 
						currentRole.getCurrentDiskoWpModule().activate(currentRole);
					// select first module
					getUIFactory().showAgain();
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
				DiskoProgressMonitor.getInstance().finish(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// model driver is not initiated. Display message to user and shut down
			Utils.showError(bundle.getString(WORK_ERROR_TEXT));
			shutdown();				
		}		
	}
}  // @jve:decl-index=0:visual-constraint="10,10"
