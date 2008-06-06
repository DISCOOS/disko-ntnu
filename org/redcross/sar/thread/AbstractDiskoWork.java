/**
 * 
 */
package org.redcross.sar.thread;

import java.util.Calendar;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.mso.MsoModelImpl;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractDiskoWork<S> implements IDiskoWork {

	protected S m_result = null;
	protected long m_millisToPopup = 0;
	protected String m_message = null;
	protected boolean m_isModal = false;
	protected boolean m_showProgress = false;
	protected boolean m_isDone = false;
	protected boolean m_suspend = false;
	protected boolean m_wasLocked = false;
	protected boolean m_isNotified = false;
	protected DiskoProgressMonitor m_monitor = null;
	protected WorkOnThreadType m_workOnThread = null;	
	
	private long m_tic = 0;
	
	protected DiskoGlassPane m_glassPane;
	
	/**
	 * Constructor
	 */
	public AbstractDiskoWork(boolean isThreadSafe, 
			boolean isModal, WorkOnThreadType workOnThread, 
			String message, long millisToPopup, 
			boolean showProgress, boolean suspend) throws Exception {
		// validate parameters
		if(!isThreadSafe && m_workOnThread == 
			WorkOnThreadType.WORK_ON_NEW) {
			// Only work that do not invoke GUI or Disko model methods
			// is safe to invoke on a new thread
			throw new IllegalArgumentException("Only thread safe work " +
					"can be executed on a new thread");
		}
		// progress dialog message
		m_message = message;						
		// instructs the work pool to schedule 
		// on spesified thread type
		m_workOnThread = workOnThread;
		// instructs the work pool do do the work 
		// application modal: no user input is accepted
		m_isModal = isModal || !isThreadSafe();
		// number of milli seconds before 
		// the progress dialog is shown
		m_millisToPopup = millisToPopup;
		// instructs the progress monitor to show the progress
		// dialog to be shown when millisToPopup has expired
		m_showProgress = showProgress;
		// get monitor
		m_monitor = DiskoProgressMonitor.getInstance();
		// set suspend flag
		m_suspend = suspend;

	}

	/**
	 * Used to indicate that work does not interact with 
	 * the mso model at all. 
	 */
	public boolean isThreadSafe() {
		return (m_workOnThread != 
			WorkOnThreadType.WORK_ON_NEW);
	}	
	
	/**
	 * Message to show in progress dialog. Progress dialog
	 * is only shown if work is done on the worker thread.
	 */
	public String getMessage() {
		return m_message;
	}	
	
	/**
	 * The thread type to execute work on
	 */
	public WorkOnThreadType getWorkOnThread() {
		return m_workOnThread;
	}

	/**
	 * The time in milli seconds to wait before progress
	 * dialog is shown
	 */
	public long getmillisToPopup() {
		return m_millisToPopup;
	}
	
	/** 
	 * If true, the work is done modal to user
	 * interaction. Is always true if work is not mso safe
	 */
	public boolean isModal() {
		return m_isModal;
	}

	/** 
	 * If true, the work is done
	 */
	public boolean isDone() {
		return m_isModal;
	}
	
	/** 
	 * Can only show progress if work is executed on 
	 * another thread then the event dispatch thread (EDT)  
	 */
	public boolean canShowProgess() {
		return !SwingUtilities.isEventDispatchThread();
	}
	
	/** 
	 * Implement the work in this method. 
	 */
	public abstract S doWork();
	
	/**
	 * Implements the run() method of interface Runnable
	 * This method is called by the DiskoWorkPool
	 */
	public void run() {
		// get flag
		boolean canShowProgress = canShowProgess();
		// show progress?
		if(m_showProgress && canShowProgress) {
			// set millis to progress popup?
			if(!m_monitor.isInAction())
				m_monitor.setMillisToPopup(m_millisToPopup);
			// notify progress monitor
			m_monitor.start(m_message);
			// set notified flag
			m_isNotified = true;
		}
		m_tic = Calendar.getInstance().getTimeInMillis();
		// prevent user input (keeps work pool concurrent)
		m_wasLocked = Utils.getApp().setLocked(true);
		// increment suspend counter?
		if(m_suspend)
			MsoModelImpl.getInstance().suspendClientUpdate();
		// catch any errors
		try {
			// forward
			set(doWork());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// decrement resume suspend counter?
		if(m_suspend)
			MsoModelImpl.getInstance().resumeClientUpdate();
		// is on event dispatch thread?
		if(SwingUtilities.isEventDispatchThread())
			done();
	}
	
	/** 
	 * Is called by the DiskoWorkPool instance if scheduled. If not scheduled
	 * 
	 */
	public void done() {
		// resume previous state
		Utils.getApp().setLocked(m_wasLocked);
		// notify progress monitor ?
		if(m_isNotified) {
			// notify progress monitor
			m_monitor.finish();
		}
		// set flag
		m_isDone = true;
		System.out.println("WORKER:Finished (" + (Calendar.getInstance().getTimeInMillis() - m_tic) + " ms)");
	}
	
	/** 
	 * Returns the result	 
	 * 
	 * */
	public S get() {
		// return result
		return m_result;
	}
	
	/** 
	 * Saves the result. 
	 */
	private S set(S result) {
		// Save the result
		m_result = result;
		// return the result
		return result;
	}
}
