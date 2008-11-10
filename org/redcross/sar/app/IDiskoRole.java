package org.redcross.sar.app;

import java.util.List;

import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * Provides access to properties and methods for all members that 
 * implements this interface.
 * @author geira
 *
 */

public interface IDiskoRole extends IWorkFlowListener {

	/**
	 * Add a new work process module to this DiskoRole.
	 * @param module A class that implements the IDiskoWpModule interface.
	 * @param isDefault Module is the default module for the role
	 */
	public void addDiskoWpModule(IDiskoWpModule module, boolean isDefault);
	
	/**
	 * Activate an work process module with the given ID, 
	 * usually the name of the work process
	 * @param id An id that identifies a work process module
	 */
	public IDiskoWpModule selectDiskoWpModule(String id);
	
	public IDiskoWpModule selectDiskoWpModule(int index);
	
	/**
	 * Activate an works process module at the given index.
	 * @param index And index that identifies a work process module
	 */
	public IDiskoWpModule selectDiskoWpModule(IDiskoWpModule module);
	
	public IDiskoWpModule getDefaultDiskoWpModule();
	
	public IDiskoWpModule getDiskoWpModule(int index);
	
	public IDiskoWpModule getDiskoWpModule(String id);
	
	public int getDiskoWpModuleCount();

	/**
	 * Get the name of this DiskoRole.
	 * @return The name
	 */
	public String getName();

	/**
	 * Get the title of this DiskoRole.
	 * @return The title
	 */
	public String getTitle();

	/**
	 * Get a description of this DiskoRole.
	 * @return The description
	 */
	public String getDescription();
	
	/**
	 * Notify that a operation change is pending
	 * @return The current worksprocess
	 */
	public void fireBeforeOperationChange();
	
	/**
	 * Re-initialize all work processes
	 * @return The current worksprocess
	 */
	public void fireAfterOperationChange();
	
	/**
	 * Get a reference to the current (active) work process module
	 * @return The current worksprocess
	 */
	public IDiskoWpModule getCurrentDiskoWpModule();
	
	/**
	 * Return a list of all IDiskoWpModules loaded for this IDiskoRole
	 * @return A list of IDiskoWpModules
	 */
	public List<IDiskoWpModule> getDiskoWpModules();
	
	/**
	 * Get a reference to the DiskoApplication.
	 * @return A reference to the DiskoApplication
	 */
	public IApplication getApplication();
	
	/**
	 * 
	 */
	public void onFlowPerformed(WorkFlowEvent e);
	
}