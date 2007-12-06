package org.redcross.sar.app;

import java.util.List;

import org.redcross.sar.wp.IDiskoWpModule;

/**
 * Provides access to properties and methods for all members that 
 * implements this interface.
 * @author geira
 *
 */

public interface IDiskoRole {

	/**
	 * Add a new work process module to this DiskoRole.
	 * @param module A class that implements the IDiskoWpModule interface.
	 */
	public void addDiskoWpModule(IDiskoWpModule module);
	
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
	 * Get a reference to the current (active) work process module
	 * @return The current worksprocess
	 */
	public IDiskoWpModule getCurrentDiskoWpModule();
	
	/**
	 * Return a list of all IDiskoWpModules loaded for this IDiskoRole
	 * @return A list of IDiskoWpModules
	 */
	public List getDiskoWpModules();
	
	/**
	 * Get a reference to the DiskoApplication.
	 * @return A reference to the DiskoApplication
	 */
	public IDiskoApplication getApplication();
	
}