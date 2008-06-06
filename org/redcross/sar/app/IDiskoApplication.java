package org.redcross.sar.app;

import javax.swing.JFrame;

import org.redcross.sar.gui.DiskoKeyEventDispatcher;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.panel.NavBarPanel;
import org.redcross.sar.gui.panel.SysBarPanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.output.DiskoReportManager;


/**
 * Provides access to properties and methods in the Disko application.
 * @author geira
 *
 */
public interface IDiskoApplication {
	
	public final static String bundleName = "org.redcross.sar.app.application";
	
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
	
	public NavBarPanel getNavBar();
	
	public SysBarPanel getSysBar();
	
	/**
	 * Get a reference to the DiskoMapManager.
	 * @return
	 */
	public IDiskoMapManager getMapManager();
	
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
     * Get a reference to the MsoModel.
     * This class is responsible for all communication with the data model and data server (SARA).
     * @return A reference to the MsoModel.
     */
    public IMsoModelIf getMsoModel();

    /**
     * choose the active operation
     * @param prompt user before choosing new operation
     */
    boolean selectActiveOperation(boolean prompt);

    /**
     * Active operation with passed operation id
     * 
     * @param operation id
     */
    boolean activeOperation(String opId);
    
    /**
     * finished the active operation
     * @param prompt user before choosing new operation
     */
    void finishOperation();

    /**
     * For cleanup and module handling when active operation has been finished
     */
    void operationFinished();

    /**
     * merges current active operation into another
     * 
     * NOT IMPLEMENTED!
     * 
     * @param prompt user before choosing new operation
     */
    void mergeOperations();

    /**
     * Method for initiating a new rescue operation
     */
    boolean createOperation();

    void onOperationCreated(String id);
    
    void shutdown();
    
	boolean isLocked();
	
	public boolean setLocked(boolean isLocked);
	
	boolean isLoading();
	
	public DiskoKeyEventDispatcher getKeyEventDispatcher();

    
}