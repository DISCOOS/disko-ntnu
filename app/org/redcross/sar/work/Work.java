/**
 *
 */
package org.redcross.sar.work;

/**
 * @author kennetgu
 *
 */
public class Work extends AbstractWork {


	/* ==================================================
	 * Constructors
	 * ================================================== */

	public Work(boolean isSafe,
			boolean isModal, WorkerType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend) throws Exception {

		// forward
		super(NORMAL_PRIORITY,isSafe,isModal,thread,message,millisToPopup,showProgress,suspend);

	}

	public Work(int priority, boolean isSafe,
			boolean isModal, WorkerType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend) throws Exception {

		// forward
		super(priority,isSafe,isModal,thread,message,millisToPopup,showProgress,suspend);

	}

	public Work(boolean isSafe,
			boolean isModal, WorkerType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend, boolean isLoop) throws Exception {

		// forward
		super(0,isSafe,isModal,thread,message,millisToPopup,showProgress,suspend,isLoop);

	}

	public Work(int priority,boolean isSafe,
			boolean isModal, WorkerType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend, boolean isLoop) throws Exception {

		// forward
		super(priority,isSafe,isModal,thread,message,millisToPopup,showProgress,suspend,isLoop);

	}

	/* ==================================================
	 * IWork implementation
	 * ================================================== */


	/**
	 * Override and implement the work in this method.
	 */
	public Object doWork(IWorkLoop loop) {
		return null;
	}

}
