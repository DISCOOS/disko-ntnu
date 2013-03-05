/**
 *
 */
package org.redcross.sar.work;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.redcross.sar.Application;
import org.redcross.sar.work.event.IWorkListener;
import org.redcross.sar.work.event.WorkEvent;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractWork implements IWork {

	public static final int REALTIME_PRIORITY = 0;
	public static final int HIGH_HIGH_PRIORITY = 1;
	public static final int HIGH_PRIORITY = 2;
	public static final int NORMAL_PRIORITY = 3;
	public static final int LOW_PRIORITY = 4;
	public static final int LOW_LOW_PRIORITY = 5;
	
    protected Object m_result;

    protected int m_priority = 0;

    protected long m_id = 0;
    protected long m_tic = 0;
    protected long m_millisToPopup = 0;

    protected WorkState m_state = WorkState.PENDING;

    protected String m_message;

    protected boolean m_isModal = false;
    protected boolean m_showProgress = false;
    protected boolean m_suspend = false;
    protected boolean m_isNotified = false;
    protected boolean m_isSafe = false;
    protected boolean m_isLoop = false;

    protected boolean m_isWorkingOnEdt;

    protected WorkerType m_workOn;

    protected final EventListenerList m_listeners = new EventListenerList();
    
    protected static ProgressMonitor m_monitor;

    /* ==================================================
     * Static constructor
     * ================================================== */
    
    static 
    {
    	try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
			    	try {
						m_monitor = ProgressMonitor.getInstance();
					} catch (Exception e) {
						e.printStackTrace();
					}    					
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
    
    /* ==================================================
     * Constructors
     * ================================================== */

    public AbstractWork(int priority, boolean isSafe,
            boolean isModal, WorkerType workOn,
            String message, long millisToPopup,
            boolean isProgressShown, boolean suspend) throws Exception {

    	// forward
    	this(priority, isSafe, isModal, workOn, message, millisToPopup, isProgressShown, suspend, false);

    }

    public AbstractWork(int priority, boolean isSafe,
            boolean isModal, WorkerType workOn,
            String message, long millisToPopup,
            boolean isProgressShown, boolean suspend, boolean isLoop) throws Exception {

        // validate parameters
        if(!isSafe && m_workOn ==
            WorkerType.UNSAFE) {
            // Only work that do not invoke Swing or MSO model methods
            // is safe to invoke on a new worker
            throw new IllegalArgumentException("Only safe work can be executed on a unsafe worker");
        }
        // save priority
        m_priority = priority;
        // progress dialog message
        m_message = message;
        // instructs the work pool to schedule
        // on specified worker type
        m_workOn = workOn;
        // instructs the work pool to do the work
        // application modal := no user input is accepted
        m_isModal = isModal || isLockRequired();
        // set worker safe flag
        m_isSafe = isSafe;
        // number of milliseconds before
        // the progress dialog is shown
        m_millisToPopup = millisToPopup;
        // instructs the progress monitor to show the progress
        // dialog to be shown when millisToPopup has expired
        m_showProgress = isProgressShown;
        // set suspend flag
        m_suspend = suspend;
        // set loop flag
        m_isLoop = isLoop;
    }

    /* ==================================================
     * Comparable implementation
     * ================================================== */

    public final int compareTo(IWork work) {
    	return m_priority - work.getPriority();
    }

    /* ==================================================
     * IWork implementation
     * ================================================== */

    /**
     * Get work priority
     */
    public final int getPriority() {
        return m_priority;
    }

    /**
     * Get work priority
     */
    public void setPriority(int priority) {
    	if(m_priority!=priority) {
    		m_priority=priority;
    		firePriorityChanged();
    	}
    }

    /**
     * Unique work id allocated by WorkPool
     */
    public final long getID() {
        return m_id;
    }

    /**
     * Unique work id Allocated by WorkPool
     */
    public final boolean setID(long id) {
        if(m_id==0) {
            m_id = id;
            return true;
        }
        return false;
    }

    /**
     * If true, indicates that work does access either Swing components
     * (which are not threading safe) nor the MSO model (which is
     * not threading safe).
     */
    public final boolean isSafe() {
        return m_isSafe;
    }

    public final boolean isLoop() {
    	return m_isLoop;
    }

    /**
     * Message to show in progress dialog. Progress dialog
     * is only shown if work is done on a worker (IWorkLoop).
     */
    public String getMessage() {
        return m_message;
    }

    /**
     * The worker type to execute work on
     */
    public final WorkerType getWorkOnType() {
        return m_workOn;
    }

    /**
     * The time in milliseconds to wait before progress
     * dialog is shown
     */
    public long getMillisToPopup() {
        return m_millisToPopup;
    }

    /**
     * If true, the work is done modal to user
     * interaction. Is always true if work is not mso safe
     */
    public final boolean isModal() {
        return m_isModal;
    }

    public void setShowProgress(boolean showProgress) {
    	m_showProgress = showProgress;
    	if(m_isNotified && !showProgress) {
    		hideProgress();
    	}
    	else {
    		showProgress();
    	}
    }

    public boolean getShowProgress() {
    	return m_showProgress;
    }

    /**
     * Can only show progress if work is executed on
     * another worker then the event dispatch thread (EDT)
     */
    public final boolean canShowProgess() {
        return !(isState(WorkState.FINISHED) || isState(WorkState.CANCELED)) && !m_isWorkingOnEdt;
    }

    /**
     * Prepares application to execute work safely. It is called automatically
     * from <code>run()</code> just before <code>doWork()</code>.</p>
     *
     * <code>prepare()</code> is always executed on the Event Dispatch Thread (EDT).
     * If <code>prepare()</code> is invoked on a worker other then EDT, this method will
     * block until EDT has executed the method. See
     * <code>SwingUtilities.invokeAndWait()</code> for more information about this.</p>
     *
     * <b>IMPORTANT</b>: A progress dialog will only be shown (see constructor
     * <code>AbstractWork()</code> if <code>prepare()</code> is called
     * from a worker other then the EDT.</p>
     *
     * If you schedule the work on the WorkPool, WorkPool will do all for you.
     */
    public final void prepare() {
        // only run on EDT
        if (SwingUtilities.isEventDispatchThread()) {
        	// forward
        	setState(WorkState.EXECUTING);
            // forward
            beforePrepare();
            // set start time?
            if(m_tic==0) m_tic = Calendar.getInstance().getTimeInMillis();
            // is user input prevention required? (keeps work pool concurrent)
            if (m_isModal) Application.getInstance().setLocked(true);
            // increment suspend counter?
            if (m_suspend) Application.getInstance().getMsoModel().suspendChange();
            // forward
            afterPrepare();
        } else {
            // set start time
            m_tic = Calendar.getInstance().getTimeInMillis();
            // forward
            showProgress();
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

    public final void run() {
    	run(null);
    }
    
    public final void run(IWorkLoop loop) {

    	// set flag
    	m_isWorkingOnEdt = SwingUtilities.isEventDispatchThread();

        // forward
        prepare();

        // catch any errors
        try {
            // forward
            set(doWork(loop));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // forward
        done();

    }

    /**
     * Override and implement the work in this method.
     */
    public abstract Object doWork(IWorkLoop loop);

    /**
     * This method should be called after <code>doWork()</code> is finished.
     * <code>done()</code> is always executed on the Event Dispatch Thread (EDT).
     * If <code>done()</code> is invoked on a worker other then EDT, it will
     * be scheduled to run on EDT and return without blocking.<p>
     * <code>doWork()</code> is called automatically by <code>run()</code>.</p>
     *
     * If you schedule the work on the WorkPool, WorkPool will do all for you.</p>
     */
    public final void done() {
        // only run on EDT
        if (SwingUtilities.isEventDispatchThread()) {
            // forward
            beforeDone();
            // decrement resume suspend counter?
            if (m_suspend)
                Application.getInstance().getMsoModel().resumeUpdate();
            // resume previous state?
            if (m_isModal) Application.getInstance().setLocked(false);
            // forward
            hideProgress();
            // set state
            setState(m_isLoop ? WorkState.PENDING : WorkState.FINISHED);
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
     * Returns the result
     *
     * */
    public Object get() {
        // return result
        return m_result;
    }

    /**
     * Saves the result.
     */
    private final Object set(Object result) {
        // Save the result
        m_result = result;
        // return the result
        return result;
    }

    public final boolean cancel() {
        return setState(WorkState.CANCELED);
    }

    public final WorkState getState() {
    	return m_state;
    }

    public final boolean isState(WorkState state) {
        return m_state.equals(state);
    }

	public final void addWorkListener(IWorkListener listener) {
		m_listeners.add(IWorkListener.class,listener);
	}

	public final void removeWorkListener(IWorkListener listener) {
		m_listeners.remove(IWorkListener.class,listener);
	}

    /* ====================================================
     * Protected methods
     * ==================================================== */

    /**
     * Override this method to do additional prepare work on EDT
     */
    protected void beforePrepare() {}

    /**
     * Override this method to do additional prepare work on EDT
     */
    protected void afterPrepare() {}


    /**
     * Override this method to do additional work on EDT
     */
    protected void beforeDone() {}

    /**
     * Override this method to do additional work on EDT
     */
    protected void afterDone() {}


    protected boolean setState(WorkState state) {
    	if(!m_state.equals(state)) {
    		m_state = state;
    		fireStateChanged();
    		return true;
    	}
		return false;

    }

    protected void fireStateChanged() {
    	WorkEvent e = new WorkEvent(this,m_state,WorkEvent.STATE_EVENT);
    	IWorkListener[] list = m_listeners.getListeners(IWorkListener.class);
    	for(int i=0; i<list.length; i++) {
    		list[i].onWorkChange(e);
    	}
    }

    protected void firePriorityChanged() {
    	WorkEvent e = new WorkEvent(this,m_priority,WorkEvent.PRIORITY_EVENT);
    	IWorkListener[] list = m_listeners.getListeners(IWorkListener.class);
    	for(int i=0; i<list.length; i++) {
    		list[i].onWorkChange(e);
    	}
    }

    protected void showProgress() {
        // show progress?
        if (m_showProgress && !m_isNotified && canShowProgess()) {
            // set millis to progress popup?
            if (!m_monitor.isInAction()) m_monitor.setMillisToPopup(m_millisToPopup);
            // notify progress monitor
            m_monitor.start(m_message);
            // set notified flag
            m_isNotified = true;
        }

    }

    protected void hideProgress() {
    	if(m_isNotified) {
			m_isNotified = false;
			m_monitor.finish();
    	}

    }

    /* ====================================================
     * Helper methods
     * ==================================================== */

    private boolean isLockRequired() {
        return 	WorkerType.SAFE.equals(m_workOn) ||
                WorkerType.EDT.equals(m_workOn);
    }



}
