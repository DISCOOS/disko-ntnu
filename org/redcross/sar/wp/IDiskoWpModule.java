package org.redcross.sar.wp;

import java.util.List;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.ITickEventListenerIf;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.event.IMsoEventManagerIf;

/**
 * This interface provides access to properties and methods for
 * handling work process modules in the Disko application.
 * @author geira
 *
 */
public interface IDiskoWpModule {

	/**
	 * Get the name of this IDiskoWpModule. This name is used to identify
	 * the gui component (JPanel) where this module is placed. Implementations
	 * of this interface and subclasses must override this method to provide
	 * a unique name for the spesific work process module.
	 * @return The name of this IDiskoWpModule
	 */
	public String getName();

	/**
	 * Get the caption of this IDiskoWpModule. Implementations
	 * of this interface and subclasses must override this method to provide
	 * a caption for the spesific work process module. The caption is shown
	 * in the title bar of the JFrame
	 * @return The caption of this IDiskoWpModule
	 */
	public String getCaption();	
	
	/**
	 * Get a reference to the DiskoMap. If the implementing class has no map,
	 * null should be returned
	 * @return A reference to a DiskoMap
	 */
	public IDiskoMap getMap();

	public IDiskoRole getDiskoRole();

	/**
	 * Get a reference to the DiskoApplication.
	 * @return
	 */
	public IDiskoApplication getApplication();

	public void setCallingWp(String name);
	
	public String getCallingWp();

	/**
	 * @return true if work process data is changed
	 */
	public boolean isChanged();	
	
	/**
	 * @return true if work process is active
	 */
	public boolean isActive();	
	
	/**
	 * @return true if DiskoMap is different from null, false otherwise
	 */
	public boolean hasMap();

	/**
	 * @return true if this IDiskoWpModule has sub menu, false otherwise
	 */
	public boolean hasSubMenu();

	/**
	 * Called when this IDiskoWpModule is activated
	 */
	public void activated();

	/**
	 * Called when this IDiskoWpModule is deactivated
	 */
	public void deactivated();
	
	 /**
     * @return Returns whether or not WP module can be deactivated. E.g. if uncommitted data is stored.
     */
    public boolean confirmDeactivate();

	public void addDiskoWorkEventListener(IDiskoWorkListener listener);

    public void removeDiskoWorkEventListener(IDiskoWorkListener listener);

    public void showWarning(String msg);

    public IMsoModelIf getMsoModel();
    
    public ICmdPostIf getCmdPost();

    public IMsoManagerIf getMsoManager();

    public IMsoEventManagerIf getMsoEventManager();

    /**
     * Get ResourceBundle text
     * @param aKey Lookup key
     * @return The international text
     */
    public String getBundleText(String aKey);

    public void addTickEventListener(ITickEventListenerIf listener);

    public void removeTickEventListener(ITickEventListenerIf listener);
    
    /**
     * Called before operation is changed, allows WP to suspend any 
     * updates for faster execution
     * lists should be updated.
     */
    public void beforeOperationChange();
    
    /**
     * Called after operation is changed, allows WP to perform house-keeping. E.g. references to CmdPost
     * lists should be updated.
     */
    public void afterOperationChange();
    
    /**
     * Setup of navbar
     */
    public void setupNavBar(List<Enum<?>> buttons, boolean isSelected);
    
    /**
     * Used to check if NavBar must be initiated
     */
    public boolean isNavBarSetupNeeded();
    
    /**
     * Used to suspend map drawing and mso update events
     */
    public void suspendUpdate(); 
    
    /**
     * Used to resume map drawing and mso update events
     */
    public void resumeUpdate();

	
    public boolean isWorking();

	public int isWorkingCount();
	
	public int setIsWorking();
	
	public int setIsNotWorking();
    
    
}