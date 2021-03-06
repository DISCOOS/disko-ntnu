package org.redcross.sar.mso.work;

import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;

public abstract class AbstractMsoWork extends AbstractWork implements IMsoWork {

	/* ==================================================
	 * Constructors
	 * ================================================== */

	public AbstractMsoWork(boolean isSafe, boolean isModal,
			String message, long millisToPopup, boolean showProgress,
			boolean suspend) throws Exception {

		// forward
		super(HIGH_PRIORITY, isSafe, isModal, WorkerType.SAFE, message, millisToPopup, showProgress, suspend);

	}

	public AbstractMsoWork(int priority, boolean isSafe, boolean isModal,
			String message, long millisToPopup, boolean showProgress,
			boolean suspend) throws Exception {

		// forward
		super(priority, isSafe, isModal, WorkerType.SAFE, message, millisToPopup, showProgress, suspend);

	}

	/* ==================================================
	 * IWork implementation
	 * ================================================== */

	public abstract Object doWork(IWorkLoop loop);

}
