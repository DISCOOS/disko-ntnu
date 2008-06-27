/**
 * 
 */
package org.redcross.sar.thread;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.mso.MsoModelImpl;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractDiskoWork<S> implements IDiskoWork<S> {

	private final static int MAX_TIME_LOG_SIZE = 100;
	private final static Random RANDOM = new Random(89652467667623L);
	
	protected S m_result = null;
	protected long m_millisToPopup = 0;
	protected long m_dutyCycle = 1000;		// execute doWork every second
	protected long m_availableTime = 1000;
	protected String m_message = null;
	protected boolean m_isModal = false;
	protected boolean m_showProgress = false;
	protected boolean m_isDone = false;
	protected boolean m_suspend = false;
	protected boolean m_isNotified = false;
	protected boolean m_isLoop = false;
	protected boolean m_isThreadSafe = false;
	protected DiskoWorkPool m_workPool = null;
	protected DiskoProgressMonitor m_monitor = null;
	protected WorkOnThreadType m_workOnThread = null;	

	private long m_id = 0;
	private long m_tic = 0;
	
	protected DiskoGlassPane m_glassPane;
	
	protected final ConcurrentLinkedQueue<Long> m_workTimeLog = new ConcurrentLinkedQueue<Long>(); 
	
	/**
	 * Constructor
	 */
	public AbstractDiskoWork(boolean isThreadSafe, 
			boolean isModal, WorkOnThreadType workOnThread, 
			String message, long millisToPopup, 
			boolean showProgress, boolean suspend, 
			boolean isLoop, long dutyCycle) throws Exception {
		
		// validate parameters
		if(!isThreadSafe && m_workOnThread == 
			WorkOnThreadType.WORK_ON_NEW) {
			// Only work that do not invoke Swing or MSO model methods
			// is safe to invoke on a new thread
			throw new IllegalArgumentException("Only thread safe work " +
					"can be executed on a new thread");
		}
		// progress dialog message
		m_message = message;						
		// instructs the work pool to schedule 
		// on specified thread type
		m_workOnThread = workOnThread;
		// instructs the work pool do do the work 
		// application modal: no user input is accepted
		m_isModal = isModal || !isThreadSafe;
		// set safe flag
		m_isThreadSafe = isThreadSafe;
		// number of milliseconds before 
		// the progress dialog is shown
		m_millisToPopup = millisToPopup;
		// instructs the progress monitor to show the progress
		// dialog to be shown when millisToPopup has expired
		m_showProgress = showProgress;
		// get work pool
		m_workPool = DiskoWorkPool.getInstance();
		// get monitor
		m_monitor = DiskoProgressMonitor.getInstance();
		// set suspend flag
		m_suspend = suspend;
		// set loop mode
		m_isLoop = isLoop;
		// save duty cycle
		m_dutyCycle = dutyCycle;
		// set available work time equal an half duty cycle
		m_availableTime = m_dutyCycle / 2;

	}
	
	/**
	 * Unique work id assigned by DiskoWorkPool 
	 */
	public long getID() {
		// TODO Auto-generated method stub
		return m_id;
	}

	/**
	 * Unique work id assigned by DiskoWorkPool 
	 */
	public void setID(long id) {
		if(m_id==0) m_id = id;		
	}
	
	/**
	 * If true, indicates that work does access either Swing components 
	 * (which are not threading safe) nor the MSO model (which is 
	 * not threading safe).
	 */
	public boolean isThreadSafe() {
		return m_isThreadSafe;
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
	 * The time in milliseconds to wait before progress
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
	public synchronized boolean isDone() {
		return m_isDone;
	}
	
	/** 
	 * If true, <code>doWork()</code> should be executed 
	 * in loops (work cycles) with given <code>getDutyCycle()</code>.<p>
	 * If work is scheduled on DiskoWorkPool, the work cycle can be
	 * executed, suspended, resumed and stopped automatically. 
	 */
	public boolean isLoop() {
		return m_isLoop;
	}

	/** 
	 * The value return indicates the requested duty cycle (time between
	 * two calls to <code>doWork()</code>) for the work cycle.<p>
	 * If work is scheduled on DiskoWorkPool, the work cycle can be
	 * executed, suspended, resumed and stopped automatically. 
	 */
	public long getDutyCycle() {
		return m_dutyCycle;
	}
		
	/** 
	 * Can only show progress if work is executed on 
	 * another thread then the event dispatch thread (EDT)  
	 */
	public boolean canShowProgess() {
		return !SwingUtilities.isEventDispatchThread();
	}
	
	/**
	 * Returns average work time
	 */
	public synchronized long getAverageWorkTime() {
		long sum = 0;
		// get count
		int count = m_workTimeLog.size();
		// can calculate?
		if(count>0) {
			// loop over all
			for(long it : m_workTimeLog) {
				sum += it;
			}
			// calculate average
			return sum/count;
		}
		return 0;
	}

	/**
	 * Returns maximum work time
	 */
	public synchronized long getMaxWorkTime() {
		long max = 0;
		// loop over all
		for(long it : m_workTimeLog) {
			max = Math.max(max,it);
		}
		// finished
		return max;
	}

	/**
	 * Returns work time utilization as the ratio of average work time on duty cycle
	 */
	public synchronized double getUtilization() {
		// can calculate?
		if(getDutyCycle()>0)
			return ((double)getAverageWorkTime())/((double)getDutyCycle());
		else
			return -1;
	}

	@Override
	public synchronized void logWorkTime(long delay) {
		// remove a random number from log?
		if(m_workTimeLog.size()==MAX_TIME_LOG_SIZE) {
			// get a random index
			int i = RANDOM.nextInt(MAX_TIME_LOG_SIZE-1);
			// remove
			m_workTimeLog.remove(i);
		}
		// add new number to log
		m_workTimeLog.add(delay);		
	}

	public long getAvailableTime() {
		return m_availableTime;
	}
	
	public void setAvailableTime(long time) {
		m_availableTime = time;
	}
		
	/** 
	 * Prepares application to execute work safely. It is called automatically 
	 * from <code>run()</code> if <code>doWork()</code> should only be executed 
	 * once (<code>isLoop()</code> is <code>false</code>). <code>prepare()</code> 
	 * is always executed on the Event Dispatch Thread (EDT). If <code>prepare()</code>
	 * is invoked on a thread other then EDT, this method will block until EDT
	 * has executed the method. See <code>SwingUtilities.invokeAndWait()</code> for 
	 * more information.<p> 
	 * <b>IMPORTANT</b><p>
	 * 1. If work is executed more than once, <code>prepare()</code> 
	 * must be called <b>before</b> first invocation of <code>run()</code>.<p>
	 * 2. If work is executed more than once, <code>done()</code> must
	 * be called when work is finished.<p>
	 * 3. Progress dialog will only be shown (see constructor 
	 * <code>AbstractDiskoWork()</code> if <code>prepare()</code> is called 
	 * from a thread other then the EDT.<p>
	 * If you schedule the work on the DiskoWorkPool, DiskoWorkPool will do all
	 * for you.
	 */
	public final void prepare() {
		// only run on EDT
		if (SwingUtilities.isEventDispatchThread()) {
			// forward
			beforePrepare();
			// set start time?
			if(m_tic==0)
				m_tic = Calendar.getInstance().getTimeInMillis();
			// prevent user input? (keeps work pool concurrent)
			if (!WorkOnThreadType.WORK_ON_NEW.equals(m_workOnThread))
				Utils.getApp().setLocked(true);
			// increment suspend counter?
			if (m_suspend)
				MsoModelImpl.getInstance().suspendClientUpdate();
			// forward
			afterPrepare();
		} else {
			// set start time
			m_tic = Calendar.getInstance().getTimeInMillis();
			// get flag
			boolean canShowProgress = canShowProgess();
			// show progress?
			if (m_showProgress && canShowProgress) {
				// set millis to progress popup?
				if (!m_monitor.isInAction())
					m_monitor.setMillisToPopup(m_millisToPopup);
				// notify progress monitor
				m_monitor.start(m_message);
				// set notified flag
				m_isNotified = true;
			}
			// block on EDT to ensure that prepare() is executed
			// before doWork() is invoked
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						prepare();
					}
				});
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * Override this method to do additional prepare work on EDT
	 */	
	protected void beforePrepare() {}
	
	/**
	 * Override this method to do additional prepare work on EDT
	 */	
	protected void afterPrepare() {}
	
	/**
	 * This method executes work safely in application by ensuring that 
	 * unsafe work (Swing and MSO model) is executed concurrently within 
	 * the Swing and MSO Model limitations.<p>
	 * This method may call all of the following methods:<p>
	 * <code>prepare()</code>, <code>doWork()</code>, and <code>done()</code>.<p>
	 * This depends on which thread <code>run()</code> is invoked on, and 
	 * if the work should be executed more then once (<code>isLoop()</code> 
	 * is <code>true</code>).<p>
	 * Method <code>prepare()</code> is only called if <code>doWork()</code> 
	 * is executed once (<code>isLoop()</code> is <code>false</code>).<p>
	 * Method <code>done()</code> is only called if <code>doWork()</code> 
	 * is executed once and <code>run()</code> was invoking on Event 
	 * Dispatch Thread (EDT).<p>
	 * <b>IMPORTANT</b><p> 
	 * 1. If work is executed more than once, <code>prepare()</code> 
	 * must be called <b>before</b> first invocation of <code>run()</code>.<p>
	 * 2. If work is executed more than once, <code>done()</code> must
	 * be called when work is finished.<p>
	 * If you schedule the work on the DiskoWorkPool, DiskoWorkPool will do all
	 * for you.
	 */	
	public void run() {

		// prepare?
		if(!m_isLoop) prepare();
		
		// catch any errors
		try {
			// forward
			set(doWork());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// done?
		if(!m_isLoop && SwingUtilities.isEventDispatchThread()) {
			done();
		}
		
	}
	
	/** 
	 * Override and implement the work in this method. 
	 */
	public abstract S doWork();
	
	/** 
	 * This method should be called after <code>doWork()</code> is finished. 
	 * <code>done()</code> is always executed on the Event Dispatch Thread (EDT).
	 * If <code>done()</code> is invoked on a thread other then EDT, it will 
	 * be scheduled to run on EDT and return without blocking.<p>
	 * <code>doWork()</code> is called automatically by the DiskoWorkPool 
	 * instance if scheduled, or by <code>run()</code> if <code>doWork()</code> 
	 * should only be executed once (<code>isLoop()</code> is <code>true</code>) 
	 * and <code>run()</code> was invoking on Event Dispatch Thread (EDT).<p>
	 * <b>IMPORTANT</b><p> 
	 * 1. If work is executed more than once, <code>prepare()</code> 
	 * must be called <b>before</b> first invocation of <code>run()</code>.<p>
	 * 2. If work is executed more than once, <code>done()</code> must
	 * be called when work is finished.<p>
	 * If you schedule the work on the DiskoWorkPool, DiskoWorkPool will do all
	 * for you.<p>
	 */
	public final void done() {
		// only run on EDT
		if (SwingUtilities.isEventDispatchThread()) {
			// forward
			beforeDone();
			// decrement resume suspend counter?
			if (m_suspend)
				MsoModelImpl.getInstance().resumeClientUpdate();
			// resume previous state?
			// prevent user input? (keeps work pool concurrent)
			if (!WorkOnThreadType.WORK_ON_NEW.equals(m_workOnThread))
				Utils.getApp().setLocked(false);
			// notify progress monitor ?
			if (m_isNotified) {
				// notify progress monitor
				m_monitor.finish();
			}
			// set flag
			m_isDone = true;
			// DEBUG: print to console
			System.out.println("WORKER:Finished ("
					+ (Calendar.getInstance().getTimeInMillis() - m_tic)
					+ " ms)");
			// reset start time
			m_tic=0;
			// forward
			afterDone();
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					done();
				}
			});
		}
	}
	
	/**
	 * Override this method to do additional work on EDT
	 */	
	protected void beforeDone() {}
	
	/**
	 * Override this method to do additional work on EDT
	 */	
	protected void afterDone() {}
	
	
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

	public boolean resume() {
		return m_workPool.resume(this);
	}

	public boolean suspend() {
		return m_workPool.suspend(this);
	}
	
	public boolean stop() {
		return m_workPool.stop(this);
	}
	
	public boolean isWorking() {
		return m_workPool.isWorking(this);
	}
	
	
}
