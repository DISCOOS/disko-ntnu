package org.redcross.sar.work;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.event.EventListenerList;

import org.redcross.sar.work.IWork.ThreadType;
import org.redcross.sar.work.IWork.WorkState;
import org.redcross.sar.work.event.IWorkListener;
import org.redcross.sar.work.event.IWorkLoopListener;
import org.redcross.sar.work.event.WorkEvent;
import org.redcross.sar.work.event.WorkLoopEvent;

public abstract class AbstractWorkLoop implements IWorkLoop {

	/**
	 * Maximum number of logged work time durations
	 */
	protected final static int MAX_TIME_LOG_SIZE = 100;

	/**
	 * Random generator used to select log index for deletion
	 */
	protected final static Random RANDOM = new Random(89652467667623L);

	/**
	 * Work time log
	 */
	protected final ConcurrentLinkedQueue<Long> m_workTimeLog = new ConcurrentLinkedQueue<Long>();

	/**
	 * Work Loop ID on Work Pool
	 */
	protected long m_id;

	/**
	 * Type of thread that loop should be implemented on
	 */
	protected ThreadType m_thread;

	/**
	 * Loop State
	 */
	protected LoopState m_state = LoopState.PENDING;

	/**
	 * Time out in milliseconds. Exits doWork() after given timeout
	 */
	protected long m_timeOut;

	/**
	 * Duty cycle in milliseconds. Work Pool will try
	 * to execute doWork() every duty cycle
	 */
	protected long m_dutyCycle;

	/**
	 * The Work Pool
	 */
	protected WorkPool m_pool;

	/**
	 * IWorkListener list
	 */
    protected final EventListenerList m_listeners = new EventListenerList();

	/**
	 * Work queue
	 */
    protected final PriorityBlockingQueue<IWork> m_queue = new PriorityBlockingQueue<IWork>();

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	/**
	 * This implements a work loop (deamon) that will be executed on
	 * a new thread if scheduled on the WorkPool. The target
	 * duty cycle is in milliseconds, same for the work cycle timeout.
	 * The message is used to for logging. </p>
	 *
	 * <b>IMPORTANT</b>: Work executed on deamon work loops are
	 * only DISKO thread safe if and only if the work loop is
	 * executed on a safe DISKO thread (loop is scheduled on the DISKO
	 * Work Pool with work type WORK_ON_SAFE or WORK_ON_EDT). By definition,
	 * only one safe thread should exist in addition to the safe Event
	 * Dispatch Thread, for each DISKO application. This thread is
	 * automatically added to the DISKO work pool. Hence, scheduling a
	 * new work loop on the work pool with WORK_ON_SAFE will fail if one
	 * already exists. If a work loop is implemented onto any other
	 * thread, the following must be kept in mind about scheduled work: </p>
	 *
	 * To ensure that work complies to the DISKO thread safe requirements,
	 * do not implement work that change the MSO model, nor access
	 * any Swing components. If these guidelines are not followed, it will
	 * result in inconsistent data and possibly severe GUI failures. </p>
	 *
	 * This IWorkLoop implementation will check the passed work an try to
	 * determine if the work can be executed safely. If the work loop is
	 * not executed on a safe thread, and the scheduled work implements
	 * the IWork interface, only work with <code>isThreadSafe():=false</code>
	 * is executed. In general, work that is not DISKO thread safe,
	 * should be scheduled on the DISKO work pool directly! The DISKO Work
	 * Pool has a safe thread running already. </p>
	 */
	public AbstractWorkLoop(long dutyCycle, int timeout) throws Exception {
		// forward
		this(ThreadType.WORK_ON_UNSAFE, dutyCycle, timeout);
	}

	/**
	 * This implements a work loop (deamon) that will be executed on
	 * the indicated thread type if scheduled on the WorkPool. The target
	 * duty cycle is in milliseconds, same for the work cycle timeout.
	 * The message is used to for logging. This constructor is for
	 * internal use only!</p>
	 *
	 * <b>IMPORTANT</b>: Work executed on deamon work loops are
	 * only DISKO thread safe if and only if the work loop is
	 * executed on a safe DISKO thread (loop is scheduled on the DISKO
	 * Work Pool with work type WORK_ON_SAFE or WORK_ON_EDT). By definition,
	 * only one safe thread should exist in addition to the safe Event
	 * Dispatch Thread, for each DISKO application. This thread is
	 * automatically added to the DISKO work pool. Hence, scheduling a
	 * new work loop on the work pool with WORK_ON_SAFE will fail if one
	 * already exists. If a work loop is implemented onto any other
	 * thread, the following must be kept in mind about scheduled work: </p>
	 *
	 * To ensure that work complies to the DISKO thread safe requirements,
	 * do not implement work that change the MSO model, nor access
	 * any Swing components. If these guidelines are not followed, it will
	 * result in inconsistent data and possibly severe GUI failures. </p>
	 *
	 * This IWorkLoop implementation will check the passed work an try to
	 * determine if the work can be executed safely. If the work loop is
	 * not executed on a safe thread, and the scheduled work implements
	 * the IWork interface, only work where <code>isSafe()</code> is
	 * <code>false</code> is executed. In general, work that is not
	 * DISKO thread safe, should be scheduled on the DISKO work pool
	 * directly! The DISKO Work Pool has a safe thread running already. </p>
	 *
	 */
	protected AbstractWorkLoop(ThreadType thread, long dutyCycle, int timeout) {

		// prepare
		m_thread = thread;
		m_dutyCycle = dutyCycle;
		m_timeOut = Math.min(timeout,m_dutyCycle/2);

	}

	/* =======================================================
	 * Public methods
	 * ======================================================= */

	public WorkPool getWorkPool() {
		if(m_pool==null) {
			try {
				m_pool = WorkPool.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return m_pool;
	}

	/* =======================================================
	 * IWorkLoop implementation
	 * ======================================================= */

	/**
	 * Schedules work on work loop in prioritized order. Work with high
	 * priority will be schedules in front of work with low priority.
	 *
	 * If work priority is changed, the position in the work loop
	 * queue is updated using <code>setPriority(IWork work)</code>.
	 *
	 */

  	public final long schedule(IWork work) {
  		// is null?
  		if(work==null) throw new NullPointerException("Work can not be null");
  		// create id
  		long id = getWorkPool().createID();
  		// set id
  		work.setID(id);
		// add to list
		add(work);
		// ensure fast execution
		resume();
		// finished
		return id;
  	}

  	public final boolean revoke(IWork work) {
  		// is null?
  		if(work==null) throw new NullPointerException("Work can not be null");
  		// is not already executing it?
  		if(isExecuting(work)) {
			// remove from list
			return remove(work);
  		}
  		// if already executing
  		return false;
  	}

  	public abstract IWork findWork(long id);

  	public abstract boolean contains(IWork work);

  	public abstract boolean isScheduled(IWork work);

	public abstract boolean isExecuting(IWork work);

	/**
	 * This method will be executed every given duty cycle. </p>
	 *
	 * <b>IMPORTANT</b>: The system can not guarantee that <code>doWork()</code> will be
	 * executed after with the given duty cycle time. It is only an advisory value that the
	 * work pool is using to determine when to start the next execution after the previous was
	 * finished. In the best possible circumstances, <code>doWork()</code> will be invoked
	 * 10 ms after the last execution. In this case, the CPU load of other threads are
	 * insignificant and the timeout property is equal or greater than the cycle time. This
	 * would however, result in a inefficient time schedule and a low system responsiveness.</p>
	 *
	 * This Work Pool will only allow MAX(timeout) = dutyCycle/2.</p>
	 */
	public abstract int doWork();

	/**
	 * Unique work id Allocated by WorkPool
	 */
	public final long getID() {
		return m_id;
	}

	/**
	 * Unique work id Allocated by WorkPool
	 */
	public final boolean setID(long id) {
		if(id==0 || m_id==0) {
			m_id = id;
			return true;
		}
		return false;
	}

	public final LoopState getState() {
		return m_state;
	}

	/**
     * The thread type to execute work on
     */
    public final ThreadType getThreadType() {
        return m_thread;
    }

	public final boolean resume() {
		if(isState(LoopState.SUSPENDED)) {
			return requestState(LoopState.EXECUTING);
		}
		return false;
	}

	public final boolean suspend() {
		if(isState(LoopState.IDLE) || isState(LoopState.EXECUTING)) {
			return setState(LoopState.SUSPENDED);
		}
		return false;
	}

	public final boolean cancel() {
		if(isState(LoopState.IDLE) || isState(LoopState.EXECUTING)) {
			return setState(LoopState.CANCELED);
		}
		return false;
	}

	public final void run() {
		// is executing
		setState(LoopState.EXECUTING);
		// forward
		doWork();
		// is idle?
		if(isState(LoopState.EXECUTING)) setState(LoopState.IDLE);
	}

	/**
	 * The value return indicates the requested duty cycle (time between
	 * two calls to <code>doWork()</code>) for the work cycle.<p>
	 * If work is scheduled on WorkPool, the work cycle can be
	 * executed, suspended, resumed and stopped automatically.
	 */
	public final long getDutyCycle() {
		return m_dutyCycle;
	}

	/**
	 * Returns average work time
	 */
	public final long getAverageWorkTime() {
		long sum = 0;
		// get count
		int count = m_workTimeLog.size();
		// can calculate?
		if(count>0) {
			synchronized(m_workTimeLog) {
				// loop over all
				for(long it : m_workTimeLog) {
					sum += it;
				}
			}
			// calculate average
			return sum/count;
		}
		return 0;
	}

	/**
	 * Returns maximum work time
	 */
	public final long getMaxWorkTime() {
		long max = 0;
		synchronized(m_workTimeLog) {
			// loop over all
			for(long it : m_workTimeLog) {
				max = Math.max(max,it);
			}
		}
		// finished
		return max;
	}

	/**
	 * Returns work time utilization as the ratio of average work time on duty cycle
	 */
	public final double getUtilization() {
		// can calculate?
		if(getDutyCycle()>0)
			return ((double)getAverageWorkTime())/((double)getDutyCycle());
		else
			return -1;
	}

	@Override
	public final void logWorkTime(long delay) {
		// remove a random number from log?
		if(m_workTimeLog.size()==MAX_TIME_LOG_SIZE) {
			// get a random index to preserve the range of logged items
			int i = RANDOM.nextInt(MAX_TIME_LOG_SIZE-1);
			// remove
			m_workTimeLog.remove(i);
		}
		// add new number to log
		m_workTimeLog.add(delay);
	}

	public final long getTimeOut() {
		return m_timeOut;
	}

	public final void setTimeOut(long time) {
		m_timeOut = Math.min(time,m_dutyCycle/2);
	}

    public final boolean isState(LoopState state) {
        return m_state.equals(state);
    }

	public final void addWorkLoopListener(IWorkLoopListener listener) {
		m_listeners.add(IWorkLoopListener.class,listener);
	}

	public final void removeWorkLoopListener(IWorkLoopListener listener) {
		m_listeners.remove(IWorkLoopListener.class,listener);
	}

	/* =======================================================
	 * Protected methods
	 * ======================================================= */

	protected boolean requestState(LoopState state) {
    	if(!m_state.equals(state)) {
    		fireStateChanged(state);
    		return true;
    	}
		return false;
    }

	protected boolean setState(LoopState state) {
    	if(!m_state.equals(state)) {
    		m_state = state;
    		fireStateChanged(state);
    		return true;
    	}
		return false;
    }

    protected void fireStateChanged(LoopState state) {
    	WorkLoopEvent e = new WorkLoopEvent(this,state,WorkLoopEvent.STATE_EVENT);
    	IWorkLoopListener[] list = m_listeners.getListeners(IWorkLoopListener.class);
    	for(int i=0; i<list.length; i++) {
    		list[i].onLoopChange(e);
    	}
    }

    protected void fireWorkChanged(IWork work) {
    	WorkLoopEvent e = new WorkLoopEvent(this,work,WorkLoopEvent.WORK_EVENT);
    	IWorkLoopListener[] list = m_listeners.getListeners(IWorkLoopListener.class);
    	for(int i=0; i<list.length; i++) {
    		list[i].onLoopChange(e);
    	}
    }

    protected void add(IWork work) {
    	work.addWorkListener(m_workListener);
    }

    protected boolean remove(IWork work) {
    	work.removeWorkListener(m_workListener);
    	return true;
    }

	/* =======================================================
	 * Anonymous classes
	 * ======================================================= */

    IWorkListener m_workListener = new IWorkListener() {

		public void onWorkChange(WorkEvent e) {
			if(e.isStateEvent()) {
				// get source
				IWork w = e.getSource();
				// translate
				if(w.isState(WorkState.FINISHED) || w.isState(WorkState.CANCELED)) {
					remove(w);
					fireWorkChanged(w);
				}
			}
			else if(e.isPriorityEvent()){

				// get source
				IWork w = e.getSource();

				/* ==========================================
				 * IMPORTANT!
				 * ==========================================
				 *
				 * To prevent a possible race condition,
				 * synchronization on the m_queue is
				 * required.
				 *
				 * The race condition occurs when the thread
				 * that implements the work loop polls the
				 * work from the head of the work queue
				 * concurrent with this method.
				 *
				 * From the poll moment to the invocation
				 * of IWork.run(), the work state is still
				 * pending. During this time, the race
				 * condition may occur. By applying a
				 * synchronized block on m_queue, any access
				 * to m_queue will block until the end of the
				 * synchronized(m_queue). Hence, if the work
				 * object exist in m_queue, rescheduling
				 * safely is possible by removing it, and
				 * adding it again. The PrioriyQueue will
				 * insert the work at the appropriate place
				 * based on the updated priority
				 *
				 * ==========================================*/

				synchronized(m_queue) {
					// reschedule possible?
					if(m_queue.contains(w)) {
						// remove from queue
						m_queue.remove(w);
						// reschedule
						m_queue.add(w);
					}
				}
			}

		}

    };


}
