package org.redcross.sar.work;

import org.redcross.sar.work.IWork.WorkerType;
import org.redcross.sar.work.event.IWorkLoopListener;

public interface IWorkLoop extends Runnable {

	public enum LoopState {
		PENDING,
		IDLE,
		EXECUTING,
		SUSPENDED,
		FINISHED,
		CANCELED
	}

	public long getID();
	public boolean setID(long id);

	public LoopState getState();
	public boolean isState(LoopState state);

	public WorkerType getWorkOnType();

	public long schedule(IWork work);
	public boolean revoke(IWork work);

	public boolean suspend();
	public boolean resume();
	public boolean cancel();

	public int doWork();

	public IWork findWork(long id);

	public boolean contains(IWork work);
	public boolean isScheduled(IWork work);
	public boolean isExecuting(IWork work);

	/**
	 * Get the requested duty cycle (time between two calls to 
	 * <code>doWork()</code>) for this work loop in milliseconds.</p>
	 * 
	 * NOTE: The actual duty cycle depends on the duty cycle 
	 * utilization of other work loops present in the application. </p>
	 * 
	 * @return Returns the requested duty cycle for this work loop.
	 */
	public long getRequestedDutyCycle();
	
	/**
	 * Get limited the duty cycle (time between two calls to 
	 * <code>doWork()</code>) for this work loop in milliseconds.</p>
	 * 
	 * The limited duty cycle is calculated by {@code 
	 * MIN(RequestedDutyCycle,MinimumDutyCycleAllowed)}. </p>
	 * 
	 * NOTE: The actual duty cycle depends on the duty cycle 
	 * utilization of other work loops present in the application. </p>
	 * 
	 * @return Returns the requested duty cycle for this work loop.
	 */
	public long getDutyCycle();
	
	/**
	 * Get minimum duty cycle allowed.
	 * 
	 * @return Returns minimum duty cycle allowed.
	 */
	public long getMinimumAllowedDutyCycle();
	
	/**
	 * Get average duty cycle startup delay.
	 * 
	 * @return Returns average duty cycle startup delay.
	 */
	public long getAverageDutyCycleDelay();
	
	/**
	 * Get maximum duty cycle startup delay. 
	 * 
	 * @return Returns maximum duty cycle startup delay.
	 */
	public long getMaximumDutyCycleDelay();
	
	/**
	 * Get work time in milliseconds. </p>
	 * 
	 * If work is not finished after the given number 
	 * of milliseconds, the work should be paused, and resumed
	 * at the beginning of the next duty cycle awarded by the work pool. </p>
	 * 
	 * To ensure that enough time is left for other work loops to complete their
	 * scheduled work, the work time should be limited to a value 
	 * significantly less than the duty cycle, typically 2 times lower or more. </p>
	 * 
	 * The work time is calculated by {@code DutyCycle*RequestedUtilization}. </p>
	 * 
	 * @return Returns the timeout in milliseconds.
	 */
	public long getWorkTime();

	/**
	 * Get requested work time (timeout) in milliseconds. </p>
	 * 
	 * If work is not finished after the given number 
	 * of milliseconds, the work should be paused, and resumed
	 * at the beginning of the next duty cycle awarded by the work pool. </p>
	 * 
	 * @return Returns the requested work time (timeout) in milliseconds.
	 */
	public long getRequestedWorkTime();
	
	/**
	 * Calculate average work time.
	 * 
	 * @return Returns average work time.
	 */
	public long getAverageWorkTime();
	
	/**
	 * Calculate maximum work time.
	 * 
	 * @return Returns maximum work time.
	 */
	public long getMaximumWorkTime();

	/**
	 * Get duty cycle utilization. </p>
	 * 
	 * The maximum allowed utilization should depend on 
	 * the number or loops present. </p>
	 * 
	 * @return Returns duty cycle utilization as the ratio 
	 * of timeout on duty cycle
	 */
	public double getUtilization();

	/**
	 * Get maximum allowed duty cycle utilization.
	 * 
	 * @return Returns maximum allowed duty cycle utilization.
	 */
	public double getMaximumAllowedUtilization();
	
	/**
	 * Get requested utilization of duty cycle.
	 * 
	 * @return Returns requested utilization of duty cycle.
	 */
	public double getRequestedUtilization();
	
	/**
	 * Get average duty cycle utilization as the 
	 * ratio of average work time on duty cycle
	 * 
	 * @return Returns average duty cycle utilization.
	 */	
	public double getAverageUtilization();
	
	/**
	 * Get maximum duty cycle utilization as the 
	 * ratio of maximum work time on duty cycle
	 * 
	 * @return Returns maximum duty cycle utilization.
	 */	
	public double getMaximumUtilization();

	/**
	 * Clear work time log. This also affect average and 
	 * maximum work time and utilization calculations.
	 */
	public void clearLogs();
	
	/**
	 * Set logged work time duration.
	 *  
	 * @param delay - the work time duration
	 */
	public void logWorkTime(long duration);

	public void addWorkLoopListener(IWorkLoopListener listener);
	public void removeWorkLoopListener(IWorkLoopListener listener);


}
