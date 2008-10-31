/**
 *
 */
package org.redcross.sar.thread;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractWork implements IWork {

    protected Object m_result;

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

    protected ThreadType m_thread;

    protected final EventListenerList m_listeners = new EventListenerList();
    protected final ProgressMonitor m_monitor = ProgressMonitor.getInstance();

    /* ==================================================
     * Constructors
     * ================================================== */

    public AbstractWork(boolean isSafe,
            boolean isModal, ThreadType thread,
            String message, long millisToPopup,
            boolean isProgressShown, boolean suspend) throws Exception {

    	// forward
    	this(isSafe, isModal, thread, message, millisToPopup, isProgressShown, suspend, false);

    }

    public AbstractWork(boolean isSafe,
            boolean isModal, ThreadType thread,
            String message, long millisToPopup,
            boolean showProgress, boolean suspend, boolean isLoop) throws Exception {

        // validate parameters
        if(!isSafe && m_thread ==
            ThreadType.WORK_ON_UNSAFE) {
            // Only work that do not invoke Swing or MSO model methods
            // is safe to invoke on a new thread
            throw new IllegalArgumentException("Only thread safe work " +
                    "can be executed on a unsafe thread");
        }
        // progress dialog message
        m_message = message;
        // instructs the work pool to schedule
        // on specified thread type
        m_thread = thread;
        // instructs the work pool to do the work
        // application modal := no user input is accepted
        m_isModal = isModal || isLockRequired();
        // set thread safe flag
        m_isSafe = isSafe;
        // number of milliseconds before
        // the progress dialog is shown
        m_millisToPopup = millisToPopup;
        // instructs the progress monitor to show the progress
        // dialog to be shown when millisToPopup has expired
        m_showProgress = showProgress;
        // set suspend flag
        m_suspend = suspend;
        // set loop flag
        m_isLoop = isLoop;
    }

    /* ==================================================
     * IWork implementation
     * ================================================== */

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
     * is only shown if work is done on the worker thread.
     */
    public String getMessage() {
        return m_message;
    }

    /**
     * The thread type to execute work on
     */
    public final ThreadType getThreadType() {
        return m_thread;
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
    		m_isNotified = false;
    		m_monitor.finish();
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
     * another thread then the event dispatch thread (EDT)
     */
    public final boolean canShowProgess() {
        return !(isState(WorkState.FINISHED) || isState(WorkState.CANCELED)) && !m_isWorkingOnEdt;
    }

    /**
     * Prepares application to execute work safely. It is called automatically
     * from <code>run()</code> just before <code>doWork()</code>.</p>
     *
     * <code>prepare()</code> is always executed on the Event Dispatch Thread (EDT).
     * If <code>prepare()</code> is invoked on a thread other then EDT, this method will
     * block until EDT has executed the method. See
     * <code>SwingUtilities.invokeAndWait()</code> for more information about this.</p>
     *
     * <b>IMPORTANT</b>: A progress dialog will only be shown (see constructor
     * <code>AbstractWork()</code> if <code>prepare()</code> is called
     * from a thread other then the EDT.</p>
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
            if (m_isModal) Utils.getApp().setLocked(true);
            // increment suspend counter?
            if (m_suspend) MsoModelImpl.getInstance().suspendClientUpdate();
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
     * thread and EDT is valid.</p>
     *
     * If you schedule the work on the WorkPool, WorkPool will do all for you.
     */
    public final void run() {

    	// set flag
    	m_isWorkingOnEdt = SwingUtilities.isEventDispatchThread();

        // forward
        prepare();

        // catch any errors
        try {
            // forward
            set(doWork());
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
    public abstract Object doWork();

    /**
     * This method should be called after <code>doWork()</code> is finished.
     * <code>done()</code> is always executed on the Event Dispatch Thread (EDT).
     * If <code>done()</code> is invoked on a thread other then EDT, it will
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
                MsoModelImpl.getInstance().resumeClientUpdate(true);
            // resume previous state?
            if (m_isModal) Utils.getApp().setLocked(false);
            // notify progress monitor ?
            if (m_isNotified) {
                // notify progress monitor
                m_monitor.finish();
            }
            // set state
            setState(m_isLoop ? WorkState.PENDING : WorkState.FINISHED);
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

	public final void addChangeListener(ChangeListener listener) {
		m_listeners.add(ChangeListener.class,listener);
	}

	public final void removeChangeListener(ChangeListener listener) {
		m_listeners.remove(ChangeListener.class,listener);
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
    	ChangeEvent e = new ChangeEvent(this);
    	ChangeListener[] list = m_listeners.getListeners(ChangeListener.class);
    	for(int i=0; i<list.length; i++) {
    		list[i].stateChanged(e);
    	}
    }

    /* ====================================================
     * Helper methods
     * ==================================================== */

    private void showProgress() {
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

    private boolean isLockRequired() {
        return 	ThreadType.WORK_ON_SAFE.equals(m_thread) ||
                ThreadType.WORK_ON_EDT.equals(m_thread);
    }



}
