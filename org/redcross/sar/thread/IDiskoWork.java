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
	
	public T doWork();
	
	public void done();
	
	public T get();
	
}
