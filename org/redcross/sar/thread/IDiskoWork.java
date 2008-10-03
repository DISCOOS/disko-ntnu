/**
 * 
 */
package org.redcross.sar.thread;

/**
 * @author kennetgu
 *
 */
public interface IDiskoWork<T> extends Runnable {

	public enum WorkOnThreadType {
		WORK_ON_EDT,			// execute work on swing event 
								// dispatch thread
		WORK_ON_SAFE,			// execute work on the safe 
								// swing worker thread
		WORK_ON_NEW				// execute work on a new swing
								// worker thread
	}
	
	public boolean isModal();
	
	public boolean isThreadSafe();
	
	public boolean isDone();
	
	public boolean canShowProgess();
	
	public WorkOnThreadType getWorkOnThread();
	
	public void prepare();
	
	public T doWork();
	
	public void done();
	
	public T get();
	
	public long getWorkID();
	
	public void setWorkID(long id);
	
	public boolean isLoop();
	
	public long getDutyCycle();		
	
	public double getUtilization();
	
	public long getMaxWorkTime();		
	
	public long getAverageWorkTime();
	
	public void logWorkTime(long delay);
	
	public boolean suspend();
	
	public boolean resume();
	
	public boolean stop();
	
	public boolean isWorking();
	
	public boolean isSuspended();
	
	public long getAvailableTime();		
	public void setAvailableTime(long time);		
	
	
}
