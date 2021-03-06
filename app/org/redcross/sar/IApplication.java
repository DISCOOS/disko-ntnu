package org.redcross.sar;

import java.beans.PropertyChangeListener;

import javax.swing.JFrame;

import org.disco.io.IOManager;
import org.redcross.sar.ds.DsPool;
import org.redcross.sar.gui.DiskoKeyEventDispatcher;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.menu.NavMenu;
import org.redcross.sar.gui.menu.SysMenu;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.undo.EditManager;


/**
 * Provides access to properties and methods in the Disko application.
 * @author geira
 *
 */
public interface IApplication{

	public final static String bundleName = "org.redcross.sar.application";
	
	public final static String PROP_GUI_LAYOUT_MODE = "GUI.LAYOUT.MODE";

	/**
	 * Get the current (active) role
	 * @return The current role
	 */
	public IDiskoRole getCurrentRole();

	/**
	 * Get the current (active) map
	 * @return The current active map
	 */
	public IDiskoMap getCurrentMap();

	/**
	 * Get a reference to the main frame in this application. Is used
	 * when creating other frame and dialogs.
	 * @return The main frame
	 */
	public JFrame getFrame();

	/**
	 * Get a property whith the given name.
	 * @param key The name of the property
	 * @return A property with the given name
	 */
	public String getProperty(String key);

   /**
    * Get a property whith the given name.
    * @param key The name of the property
    * @param defaultValue The given defaultvalue
    * @return A property with the given name
    *
    */
   public String getProperty(String key,String defaultValue);

   /**
    * Set a property whith the given name.
    * @param key The name of the property
    * @param The given value
    * @return Set a property with the given name
    *
    */
   public boolean setProperty(String key, String value);

   /**
	 * Return a reference to the UIFactory class. This class contains
	 * methods to create GUI component for this application
	 * @return A reference to the UIFactory
	 */
	public UIFactory getUIFactory();

	/**
	 * Get navigation menu object.
	 * 
	 * @return The NavMenu object
	 */
	public NavMenu getNavMenu();

	/**
	 * Get system menu object.
	 * 
	 * @return The SysMenu object
	 */	
	public SysMenu getSysMenu();
	
	/**
	 * Get a reference to the model dispatcher.
	 * 
	 * @return
	 */
	public IDispatcherIf getDispatcher();
	
	/**
	 * Get a reference to the DiskoMapManager.
	 * 
	 * @return
	 */
	public IDiskoMapManager getMapManager();

	/**
	 * Return a reference to the EditManager class. This class contains
	 * methods to handle edits across work processes.
	 * @return A reference to the EditManager
	 */
	public EditManager getEditManager();
	
	/**
	 * Get a reference to the DiskoModuleManager. This class is responsible
	 * for loading work process modules
	 * @return A reference to the DiskoModuleLoader
	 */
	public DiskoModuleManager getModuleManager();

	/**
	 * Get a reference to the DiskoReportManager.
	 * @return
	 */
	public DiskoReportManager getReportManager();

	/**
	 * Open the login dialog
	 * @param rolleName Default role name
	 * @param user Default user name
	 * @param password Default password
	 */
	public boolean login(String role, String user, char[] password);

	/**
	 * Open the change role dialog
	 * @param rolleName Default role name
	 * @param user Default user name
	 * @param password Default password
	 */
	public boolean swapTo(String role, String user, char[] password);

    /**
     * Get a reference to current (active) MSO Model</p>
     * 
     * @return A reference to the model.
     */
    public IMsoModelIf getMsoModel();

    /**
     * Get a reference to current (active) MSO Model committer </p>
     * 
     * @return A reference to the model.
     */
    public IMsoTransactionManagerIf getTransactionManager();
    
    /**
     * choose the active operation
     * @param prompt user before choosing new operation
     */
    public boolean selectActiveOperation(boolean prompt);

    /**
     * Active operation with passed operation id
     *
     * @param operation id
     */
    public boolean activateOperation(String opId);

    /**
     * finished the active operation
     * @param prompt user before choosing new operation
     */
    public void finishOperation();

    /**
     * merges current active operation into another
     *
     * NOT IMPLEMENTED!
     *
     * @param prompt user before choosing new operation
     */
    public void mergeOperations();

    /**
     * Method for initiating a new rescue operation
     */
    public boolean createOperation(boolean prompt);

    /**
     * Get service pool.
     */
    public ServicePool getServicePool();
    
    /**
     * Get decision support pool.
     */
    public DsPool getDsPool();

    /**
     * Get IO manager 
     */
	public IOManager getIOManager();    

    /**
     * Requests a system shutdown
     *
     */
    public void shutdown();

    /**
     * Get user input lock state
     * 
     * @return Returns <code>true</code> if user input is locked, <code>false</code> otherwise. 
     */
    public boolean isLocked();
    
    /**
     * Set user input lock state
     * 
     * @param isLocked - if <code>true</code>, user input is locked.
     */    
	public void setLocked(boolean isLocked);

	/**
	 * Get loading flag. This flag is set during operation loading
	 * @return boolean
	 */
	public boolean isLoading();

	/**
	 * 
	 * @return 
	 */
	public DiskoKeyEventDispatcher getKeyEventDispatcher();

	/**
	 * Add a property change listener to the application
	 * 
	 * @param listener - the listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);
	
	/**
	 * Remove a property change listener from the application
	 * 
	 * @param listener - the listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener);	
	
	/**
	 * Invoke a application command
	 * @param cmd - Enum identifying the command
	 * @param requestFocus - if <code>true</code>, focus is requested after execution
	 * @return
	 */
	public boolean invoke(Enum<?> cmd, boolean requestFocus);

	/**
	 * Get touch mode flag
	 * 
	 * @return boolean
	 */
	public boolean isTouchMode();

}