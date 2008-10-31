/**
 *
 */
package org.redcross.sar.thread;

import javax.swing.event.ChangeListener;

/**
 * @author kennetgu
 *
 */
public interface IWork extends Runnable {

	public enum ThreadType {
		WORK_ON_EDT,			/* execute work on
								/* swing event dispatch thread */

		WORK_ON_SAFE,			/* execute work on the safe
								/* swing worker thread */

		WORK_ON_UNSAFE,			/* execute work on a unsafe
								/* swing worker thread */

		WORK_ON_LOOP			/* execute work on a work loop */
	}

	public enum WorkState {
		PENDING,
		EXECUTING,
		CANCELED,
		FINISHED
	}

	public boolean canShowProgess();
    public boolean getShowProgress();
    public void setShowProgress(boolean showProgress);

	public long getMillisToPopup();

	public ThreadType getThreadType();

	public void prepare();

	public Object doWork();

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

	public void addChangeListener(ChangeListener listener);
	public void removeChangeListener(ChangeListener listener);


}
