package org.redcross.sar.app;

import no.cmr.tools.Log;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.LoginDialog;
import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.OperationPanel;
import org.redcross.sar.gui.SysBar;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.map.DiskoMapManagerImpl;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.output.DiskoReport;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoProgressMonitor;
import org.redcross.sar.thread.DiskoWorkPool;

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
	private static final ResourceBundle bundle = ResourceBundle.getBundle("org.redcross.sar.app.properties.DiskoApplication");
	private static final String CONFIRMATION_TEXT = "CONFIRMATION.TEXT";
	private static final String CONFIRMATION_TITLE = "CONFIRMATION.HEADER";
	private static final String CHOOSEOPDESC = "CHOOSE.OP.DESC";
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
	private DiskoModuleLoader moduleLoader = null;
	private Properties properties = null;
	private UIFactory uiFactory = null;
	private IDiskoMapManager mapManager = null;
	private MsoModelImpl m_msoModel = null;
	private boolean waitingForNewOp=false;
	private DiskoReport diskoReport = null;
	
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

//			UIManager.put("ScrollBar.width", 30);

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
            // add operations panel
            //this.getUIFactory().getMainPanel().addComponent(new OperationPanel(), "SYSTEM.OPERATION");
            // show panel
            //this.getUIFactory().getMainPanel().showComponent("SYSTEM.OPERATION");
            // set content panel
			this.setContentPane(getUIFactory().getContentPanel());
			// apply size and layout
			this.getFrame().setPreferredSize(new Dimension(1024,768));
			this.pack();
			// show extended
			this.getFrame().setExtendedState(Frame.MAXIMIZED_BOTH);
			// initialize logging
			Log.init("DISKO");
			// add this as window listener
			this.addWindowListener(this);
			// show me
			this.setVisible(true);
			//initiate modeldriver
			this.getMsoModel().getModelDriver().initiate();
			this.getMsoModel().getModelDriver().setDiskoApplication(this);
			// show the login dialog
			final LoginDialog loginDialog = getUIFactory().getLoginDialog();
			loginDialog.setLocationRelativeTo((JComponent)getFrame().getContentPane(),LoginDialog.POS_CENTER,false);         
			diskoReport = new DiskoReport(this);
			java.awt.EventQueue.invokeLater(new Runnable() {public void 
				run() {
					loginDialog.setVisible(true,true);
				}}); 
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
		return ((DiskoGlassPane)getFrame().getGlassPane()).setLocked(isLocked);
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

	public NavBar getNavBar()
	{
		return getUIFactory().getMainPanel().getNavBar();
	}

	public SysBar getSysBar()
	{
		return getUIFactory().getMainPanel().getSysBar();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getDiskoMapManager()
	 */
	public IDiskoMapManager getDiskoMapManager()
	{
		if (mapManager == null)
		{
			mapManager = new DiskoMapManagerImpl(this);
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

	public DiskoReport getDiskoReport(){
		return this.diskoReport;
	}

	public void windowClosing(WindowEvent e) {
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
	 * @see org.redcross.sar.app.IDiskoApplication#chooseActiveOperation()
	 */
	public void chooseActiveOperation(boolean prompt)
	{
		// initialise answers
		String opId = null;
		int ans = JOptionPane.YES_OPTION;

		// prompt user?
		if(prompt) {
			ans = JOptionPane.showOptionDialog(
					uiFactory.getContentPanel(),
					bundle.getString(SWITCHACTION_DESC),
					bundle.getString(SWITCHACTION_TEXT),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null,null,null);
		}


		// switch operation?
		if(ans == JOptionPane.YES_OPTION) {

			// get active operations locally / on sara server
			java.util.List<String[]> opList = getMsoModel().getModelDriver().getActiveOperations();

			// found any?
			if (opList.size() > 1){
				// save active operations as options
				String[] options = new String[opList.size()];
				for (int i = 0; i < opList.size(); i++)
					options[i] = opList.get(i)[0];
				// show available active actions in an option dialog
				ans = JOptionPane.showOptionDialog(null, bundle.getString(CHOOSEOPDESC), 
						bundle.getString(CHOOSETEXT), JOptionPane.OK_CANCEL_OPTION, 
						JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				// did the user choose an operation?
				if (ans >= 0)
					opId = opList.get(ans)[1];
			}
			else if(opList.size()==1){
				// only one active operation, choose this one automatically
				opId = opList.get(0)[1];
			}
			// set selected operation as active?
			if(opId!=null) {
				// schedule work
				doSetActiveOperation(opId);
			}
		}
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

	public void operationFinished()
	{

		// force finish progress
		try {
			DiskoProgressMonitor.getInstance().finish(true);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// get active operations
		java.util.List<String[]> opList = getMsoModel().getModelDriver().getActiveOperations();

		// prompt user for actions
		String[] options = {bundle.getString("QUIT.APPLICATION.TEXT"), bundle.getString(CHOOSETEXT), bundle.getString("NEWACTION.TEXT")};
		int ans = JOptionPane.showOptionDialog(
				uiFactory.getContentPanel(),
				bundle.getString(OPERATION_FINISHED_TEXT),
				bundle.getString(OPERATION_FINISHED_TITLE),
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[0]);

		// user choose to exit the application
		if(ans==JOptionPane.YES_OPTION){
			shutdown();
		}
		else if(ans==JOptionPane.NO_OPTION){	
			
			// the user choose to select another active operation (if it exists)
			if(opList.size()>0) {
				// choose operation without prompt
				chooseActiveOperation(false);
			}
			else {
				// get maximum wait time
				long maxTime = Long.parseLong(getProperty("max.wait.time", "" + 60 * 1000));
				// add work to work pool. If initiation succeeds, the active operation 
				// is choosen. If initiation fails, the system will be shut down.
				doInitiateModelDriver(maxTime,true,false);
			}
		}
		else
			// forward
			newOperation();
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#mergeOperations()
	 */
	public void mergeOperations()
	{
		JOptionPane.showMessageDialog(null, "Beklager, fletting er foreløpig ikke støttet", "Ikke støttet", JOptionPane.WARNING_MESSAGE);
	}

	public void newOperation()
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
		}
	}

	public void operationAdded(String opId)
	{
		// is waiting for this operation
		if(waitingForNewOp){
			// reset flag
			waitingForNewOp=false;
			// notify user of new operation created
			JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(OPERATION_CREATED_TEXT),
					bundle.getString(OPERATION_CREATED_TITLE), JOptionPane.INFORMATION_MESSAGE);
			// schedule work
			doSetActiveOperation(opId);
		}
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#login(java.lang.String, java.lang.String, char[])
	 */
	public void login(String roleName, String user, char[] password)
	{

		// update login properties
		loggedin[0]=roleName;
		loggedin[1]=user;
		loggedin[2]=password;

		// is model driver initiated?
		if (getMsoModel().getModelDriver().isInitiated()){
			// forward
			chooseActiveOperation(false);
		}
		else {
			// get maximum wait time
			long maxTime = Long.parseLong(getProperty("max.wait.time", "" + 60 * 1000));
			// The model driver is not initiated. Schedule the initiation work. 
			// If initiation is successful the active operation is choosen. If initiation fails, 
			// the system will be shut down.
			doInitiateModelDriver(maxTime,true,false);
		}
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
	public DiskoModuleLoader getDiskoModuleLoader()
	{
		if (moduleLoader == null)
		{
			try
			{
				moduleLoader = new DiskoModuleLoader(this,
						new File("DiskoModules.xml"));
			}
			catch (Exception e2)
			{
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		return moduleLoader;
	}

	public Properties getProperties()
	{
		if (properties == null)
		{
			try
			{
				properties = Utils.loadProperties("properties");
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return properties;
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getProperty(java.lang.String)
	 */
	public String getProperty(String key)
	{
		return getProperties().getProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.redcross.sar.app.IDiskoApplication#getProperty(java.lang.String)
	 */
	public String getProperty(String key, String defaultvalue)
	{
		return getProperties().getProperty(key, defaultvalue);
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
		JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
				bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
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
					"Kobler til server",100,true);
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
				// is model not initiated?
				if(!m_msoModel.getModelDriver().isInitiated()) {
					// loop until maximum milliseconds is reached
					while ((tic - start) < m_millisToWait) {
						// get current list
						List<String[]> active = m_msoModel.getModelDriver().getActiveOperations();
						if (active.size()>0){
							//Get back onto awt thread
							return true;
						}
						else Thread.sleep(200);
						// get current time tic 
						tic = System.currentTimeMillis();
					}
					// failed to initiate model driver (timeout)
					return false;
				}
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
					chooseActiveOperation(m_prompt);
	
				// failed?
				if(!hasActive) {
					// model driver is not initiated. Display message to user and shut down
					JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(INIT_ERROR_SHUTDOWN_TEXT),
							bundle.getString(INIT_ERROR_TEXT), JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
					bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
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
		JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
				bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
		shutdown();				
	}
	
	class SetActiveOperation extends AbstractDiskoWork<Boolean> {

		private String m_opID = null;

		/**
		 * Constructor
		 * 
		 * @param opID
		 */
		SetActiveOperation(String opID) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Kobler til aksjon",100,true,false);
			// save
			m_opID = opID;
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
															
					// notify current work processes?
					if(currentRole!=null)
						currentRole.fireBeforeOperationChange();
										
					// return status
					boolean flag = MsoModelImpl.getInstance().getModelDriver().setActiveOperation(m_opID);
					
					// notify current work processes?
					if(currentRole!=null) {
						currentRole.fireAfterOperationChange();
					}
					
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
			JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
					bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
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
		JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
				bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
		shutdown();				
	}
	
	class SetActiveRoleWorker extends AbstractDiskoWork<Boolean> {

		private String m_loginRole = null;
		private IDiskoRole m_current = null;
		private Hashtable<String, IDiskoRole> m_roles = null;
		
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
					"Henter data",100,true);
			// prepare
			m_roles = roles;
			m_current = current;
			m_loginRole = loginRole;			
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
				// initiate roles?
				if(m_roles==null)
					m_roles = new Hashtable<String, IDiskoRole>();
				
				// get role name (null if not found)
				m_current = (IDiskoRole) m_roles.get(m_loginRole);
				
				// found role in table?
				if (m_current == null){
					// get role from module loader
					m_current = getDiskoModuleLoader().parseRole(m_loginRole);
					// put to table
					m_roles.put(m_loginRole, m_current);
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
					// get updated role table
					roles = m_roles;
					// get current role
					currentRole = m_current;
					// select first module
					currentRole.selectDiskoWpModule(currentRole.getDefaultDiskoWpModule());
					getUIFactory().getMainMenuPanel().showMenu(currentRole.getName());
					getUIFactory().getMenuPanel().setVisible(true);
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
			JOptionPane.showMessageDialog(uiFactory.getContentPanel(), bundle.getString(WORK_ERROR_TEXT),
					bundle.getString(WORK_ERROR_TITLE), JOptionPane.ERROR_MESSAGE);
			shutdown();				
		}		
	}
}  // @jve:decl-index=0:visual-constraint="10,10"
