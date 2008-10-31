package org.redcross.sar.mso.work;

import org.redcross.sar.thread.AbstractWork;

public abstract class AbstractMsoWork extends AbstractWork implements IMsoWork {

	/* ==================================================
	 * Constructors
	 * ================================================== */

	public AbstractMsoWork(boolean isSafe, boolean isModal,
			String message, long millisToPopup, boolean showProgress,
			boolean suspend) throws Exception {

		// forward
		super(isSafe, isModal, ThreadType.WORK_ON_SAFE, message, millisToPopup, showProgress, suspend);

	}

	/* ==================================================
	 * IWork implementation
	 * ================================================== */

	public abstract Object doWork();

}
