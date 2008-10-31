/**
 *
 */
package org.redcross.sar.thread;

/**
 * @author kennetgu
 *
 */
public class Work extends AbstractWork {


	/* ==================================================
	 * Constructors
	 * ================================================== */

	public Work(boolean isSafe,
			boolean isModal, ThreadType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend) throws Exception {

		// forward
		super(isSafe,isModal,thread,message,millisToPopup,showProgress,suspend);

	}

	public Work(boolean isSafe,
			boolean isModal, ThreadType thread,
			String message, long millisToPopup,
			boolean showProgress, boolean suspend, boolean isLoop) throws Exception {

		// forward
		super(isSafe,isModal,thread,message,millisToPopup,showProgress,suspend,isLoop);

	}

	/* ==================================================
	 * IWork implementation
	 * ================================================== */


	/**
	 * Override and implement the work in this method.
	 */
	public Object doWork() {
		return null;
	}

}
