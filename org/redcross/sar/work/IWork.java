/**
 *
 */
package org.redcross.sar.work;

import org.redcross.sar.work.event.IWorkListener;

/**
 * @author kennetgu
 *
 */
public interface IWork extends Runnable, Comparable<IWork> {

	public enum WorkerType {
		/** Execute work on swing event dispatch thread */
		EDT,			

		/** Execute work on the SAFE worker (IWorkLoop). </p>
		 * Safe worker are synchronized with the event 
		 * dispatch thread (EDT) to ensure that Swing and
		 * MSO model is synchronized. </p>
		 * The application will block user input during 
		 * work execution if work is marked as
		 * unsafe. Work is marked as unsafe if 
		 * {@code IWork.isSafe()} is {@code false}.*/
		SAFE,			

		/** Execute work on a UNSAFE worker (IWorkLoop). </p> 
		 * Unsafe workers are NOT synchronized with the event 
		 * dispatch thread (EDT), nor the SAFE WORK LOOP. 
		 * The work should therefore not contain any 
		 * invocations of methods in Swing or MSO model.
		 * If these safeguards are not followed, MSO
		 * data integrity may be corrupted and unexpected GUI
		 * behavior may occur. </p>
		 * The application DO NOT block user input during 
		 * work execution, regardless of the indicated safe 
		 * state, see {@code IWork.isSafe()}.*/
		UNSAFE
	}

	public enum WorkState {
		PENDING,
		EXECUTING,
		CANCELED,
		FINISHED
	}

	public int getPriority();
	public void setPriority(int priority);

	public boolean canShowProgess();
    public boolean getShowProgress();
    public void setShowProgress(boolean showProgress);

	public long getMillisToPopup();

	public WorkerType getWorkOnType();

    /**
     * This method executes work safely by ensuring that
     * unsafe work (Swing and MSO model) is executed concurrently within
     * the Swing and MSO Model limitations.</p>
     *
     * This method calls the following methods: <code>prepare()</code>,
     * <code>doWork()</code>, and <code>done()</code>.</p>
     *
     * <b>IMPORTANT</b>: If not invoked from the Event Dispatch Thread,
     * <code>prepare()</code> will block until it is executed
     * successfully. This ensures that synchronization between the safe
     * worker and EDT is valid.</p>
     *
     * If you schedule the work on the WorkPool, WorkPool will do all for you.
     */
    public void run();
    
    /**
     * This method executes work safely by ensuring that
     * unsafe work (Swing and MSO model) is executed concurrently within
     * the Swing and MSO Model limitations.</p>
     *
     * This method calls the following methods: <code>prepare()</code>,
     * <code>doWork()</code>, and <code>done()</code>.</p>
     *
     * <b>IMPORTANT</b>: If not invoked from the Event Dispatch Thread,
     * <code>prepare()</code> will block until it is executed
     * successfully. This ensures that synchronization between the safe
     * worker and EDT is valid.</p>
     *
     * If you schedule the work on the WorkPool, WorkPool will do all for you.
     * 
     * @param loop - the work loop that invoked the run methods. If no
     * work loop exists, pass {@code null}. 
     */
    public void run(IWorkLoop loop);
	
	public void prepare();

	public Object doWork(IWorkLoop loop);

	public boolean cancel();

	public void done();

	public Object get();

	public long getID();
	public boolean setID(long id);

	/**
	 * Indicates to the executing work loop if the work should be
	 * rescheduled or not when finished.
	 *
	 * @return boolean
	 */
	public boolean isLoop();

	public WorkState getState();

	/**
	 * This flag indicates whether or not the work implemented in <code>doWork()</code> honors
	 * the DISKO Thread Safe Requirements. To ensure that work complies to these requirements,
	 * do not implement work that change or access the MSO model, nor access any Swing components
	 * if <code>isSafe()</code> is <code>true</code>. If these guidelines are not followed,
	 * it will result in inconsistent data and possibly severe GUI failures. </p>
	 *
	 * <b>IMPORTANT</b>: The DISKO Work Pool and Work List trusts this flag and will allow work
	 * that is indicated to be safe to run on unsafe threads, regardless of the actual safeness of the
	 * implemented work! </p>
	 *
	 * @return Boolean - <code>true</code> if work is safe, <code>false</code> otherwise.
	 */
	public boolean isSafe();

	/**
	 *
	 * @return Boolean - <code>true</code> if GUI should be locked during execution,
	 * <code>false</code> otherwise.
	 */
	public boolean isModal();

	/**
	 *
	 * @param WorkState state - compare to state
	 * @return
	 */
	public boolean isState(WorkState state);

	public void addWorkListener(IWorkListener listener);
	public void removeWorkListener(IWorkListener listener);


}
