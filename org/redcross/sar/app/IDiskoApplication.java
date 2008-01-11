package org.redcross.sar.app;

import java.util.Properties;

import javax.swing.JFrame;

import org.redcross.sar.gui.NavBar;
import org.redcross.sar.gui.SysBar;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.output.DiskoReport;
import org.redcross.sar.thread.DiskoProgressMonitor;


/**
 * Provides access to properties and methods in the Disko application.
 * @author geira
 *
 */
public interface IDiskoApplication {
	
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
	 * Get the properties.
	 * @return
	 */
	public Properties getProperties();
	
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
	 * Return a reference to the UIFactory class. This class contains
	 * methods to create GUI component for this application
	 * @return A reference to the UIFactory
	 */
	public UIFactory getUIFactory();
	
	public NavBar getNavBar();
	
	public SysBar getSysBar();
	
	/**
	 * Get a reference to the DiskoMapManager.
	 * @return
	 */
	public IDiskoMapManager getDiskoMapManager();
	
	/**
	 * Get a reference to the DiskoModuleLoader. This class is responsible
	 * for loading work process modules
	 * @return A reference to the DiskoModuleLoader
	 */
	public DiskoModuleLoader getDiskoModuleLoader();
	
	/**
	 * Get a reference to the DiskoReport.
	 * @return
	 */
	public DiskoReport getDiskoReport();
	
	/**
	 * Open the login dialog
	 * @param rolleName Default role name
	 * @param user Default user name
	 * @param password Default password
	 */
	public void login(String rolleName, String user, char[] password);

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
    void chooseActiveOperation(boolean prompt);

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
     * Method for initiating av new rescueoperation
     */
    void newOperation();

    void operationAdded(String id);
    
    void shutdown();
    
}