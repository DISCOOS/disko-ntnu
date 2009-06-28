package org.redcross.sar.work;

public interface IWorkPool {

  	/**
  	 * add a new work loop. Only work loops on unsafe threads are allowed to add
  	 *
  	 * @return long - The work loop id
  	 */
  	public long add(IWorkLoop loop);

  	/**
  	 * Use this method to destroy work loops
  	 *
  	 * @param long id - work loop id
  	 * @return Boolean - <code>true</code> if allowed,
  	 * <code>false</code> else.
  	 */
  	public boolean remove(long id);

  	/**
  	 * Get the number of work loops in work pool
  	 *
  	 * @return long - The work loop count
  	 */
  	public int getLoopCount();
  	
  	/**
  	 * 
  	 * @return Returns the minimum duty cycle allowed to be set
  	 */
  	public long getMinimumAllowedDutyCycle();

  	/**
  	 * Get maximum allowed utilization. </p>
  	 * 
  	 * The work pool uses the heuristic {@code 1/LoopCount}.
  	 * 
  	 * @return Return maximum allowed utilization.
  	 */
  	public double getMaximumAllowedUtilization();
  	
  	/**
  	 * Get work pool utilization. 
  	 * @return Returns average work pool utilization.
  	 */
  	public double getUtilization();
  	
  	/**
  	 * Get average work pool utilization. 
  	 * @return Returns average work pool utilization.
  	 */
  	public double getAverageUtilization(); 
  	
  	/**
  	 * Get maximum work pool utilization.
  	 * @return Returns maximum work pool utilization.
  	 */
  	public double getMaximumUtilization();

  	/**
  	 * Schedule work on work pool. The work pool will schedule the work
  	 * according to the work attributes.
  	 *
  	 * @param IWork work - work object
  	 *
  	 * @return long - The work id
  	 */
  	public long schedule(IWork work);

  	public boolean isUnsafe();

  	public boolean isSuspended();

  	public boolean suspend();

  	public boolean resume();

	public int getWorkCount();
	
	public IWork findWork(long id);

	public IWorkLoop findLoop(IWork work);

	public IWorkLoop findLoop(long id);
	
	public long getDutyCycle(long id);
	
}
