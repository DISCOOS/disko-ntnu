package org.redcross.sar.work;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.redcross.sar.work.IWork.WorkerType;
import org.redcross.sar.work.IWork.WorkState;
import org.redcross.sar.work.event.IWorkListener;
import org.redcross.sar.work.event.IWorkLoopListener;
import org.redcross.sar.work.event.WorkEvent;
import org.redcross.sar.work.event.WorkLoopEvent;

public abstract class AbstractWorkLoop implements IWorkLoop {

	protected final static long LOG_DELAY_PRECISION = 15;
	
	/**
	 * Logger object
	 */
	protected final Logger m_logger; 
	
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
	 * Last time work was started (start of work cycle)
	 */
	protected long m_sTic = 0;
	
	/**
	 * Duty cycle startup delay log
	 */
	protected final ConcurrentLinkedQueue<Long> m_startupDelayLog = new ConcurrentLinkedQueue<Long>();
	/**
	 * Work Loop ID on Work Pool
	 */
	protected long m_id = 0;

	/**
	 * Type of worker that loop should be implemented on
	 */
	protected WorkerType m_workOn;

	/**
	 * Loop State
	 */
	protected LoopState m_state = LoopState.PENDING;

	/**
	 * Requested utilization in percent of duty cycle
	 */
	protected double m_requestedUtilization;

	/**
	 * Duty cycle in milliseconds. Work Pool will try
	 * to execute doWork() every duty cycle
	 */
	protected long m_requestedDutyCycle;

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
    protected final PriorityBlockingQueue<FIFOEntry<IWork>> m_queue = new PriorityBlockingQueue<FIFOEntry<IWork>>();

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	/**
	 * This implements a work loop (deamon) that will be executed on
	 * a new thread if scheduled on the WorkPool. TThe requested
	 * duty cycle is in milliseconds, duty cycle utilization is
	 * a factor greater than zero and less than one. The message is 
	 * used to for logging. </p>
	 * 
	 * <b>IMPORTANT</b>: Work executed on deamon work loops are
	 * only DISKO thread safe if and only if the work loop is
	 * executed on a safe DISKO thread (loop is scheduled on the DISKO
	 * Work Pool with work type EDT or SAFE). By definition,
	 * only one safe thread should exist in addition to the safe Event
	 * Dispatch Thread, for each DISKO application. This thread is
	 * automatically added to the DISKO work pool. Hence, scheduling a
	 * new work loop on the work pool with SAFE will fail if one
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
	public AbstractWorkLoop(long requestedDutyCycle, double requestedUtilization) throws Exception {
		// forward
		this(WorkerType.UNSAFE, requestedDutyCycle, requestedUtilization);
	}

	/**
	 * This implements a work loop (deamon) that will be executed on
	 * the indicated thread type if scheduled on the WorkPool. The requested
	 * duty cycle is in milliseconds, duty cycle utilization is
	 * a factor greater than zero and less than one. The message is 
	 * used to for logging. </p>
	 * 
	 * <i>This constructor is for internal use only!</i></p>
	 *
	 * <b>IMPORTANT</b>: Work executed on deamon work loops are
	 * only DISKO thread safe if and only if the work loop is
	 * executed on a safe DISKO thread (loop is scheduled on the DISKO
	 * Work Pool with work type EDT or SAFE). By definition,
	 * only one safe thread should exist in addition to the safe Event
	 * Dispatch Thread, for each DISKO application. This thread is
	 * automatically added to the DISKO work pool. Hence, scheduling a
	 * new work loop on the work pool with SAFE will fail if one
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
	protected AbstractWorkLoop(WorkerType workOn, long requestedDutyCycle, double requestedUtilization) {

		// prepare
		m_workOn = workOn;
		m_requestedDutyCycle = requestedDutyCycle;
		m_logger = Logger.getLogger(getClass()); 
		if(requestedUtilization<=0 || requestedUtilization>=1)
		{
			m_requestedUtilization = 0.1;
			// notify
			m_logger.debug("Requested utilization invalid ("+requestedUtilization+"). Used default instead (10%)");
		}
		else
		{
			m_requestedUtilization = requestedUtilization;
		}

	}

	/* =======================================================
	 * Public methods
	 * ======================================================= */

	public WorkPool getWorkPool() {
		if(m_pool==null) {
			try {
				m_pool = WorkPool.getInstance();
			} catch (Exception e) {
				m_logger.error("Failed to get work pool",e);
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
  		if(work==null)
  		{
  			throw new NullPointerException("Work can not be null");
  		}
  		// is invalid worker type?
  		if(work.getWorkOnType()!=getWorkOnType())
  		{
  			throw new IllegalArgumentException("Work can not be executed on worker (work requested " 
  					+ work.getWorkOnType() + " work loop, but worker is " + getWorkOnType());  			
  		}
  		// create id
  		long id = getWorkPool().createID();
  		// set id
  		work.setID(id);
		// add to list
		add(work,true);
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
	 * This Work Pool will only allow timeout <= dutyCycle / 2 to reduce the probabilti.</p>
	 */
	public abstract int doWork();

	/**
	 * Get unique work id allocated manually or by WorkPool.
	 * 
	 * @return Returns unique work id.
	 */
	public final long getID() {
		return m_id;
	}

	/**
	 * Set unique work id. The id is only possible to 
	 * change once. </p>
	 * 
	 * Setting this id to anything else than zero, should disable
	 * internal work time logging in the {@code doWork()} method. 
	 * 
	 * @return Returns {@code true} if id was set. 
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
     * Get the worker type to execute work loop on
     * @return Returns worker type to execute work on. 
     */
    public final WorkerType getWorkOnType() {
        return m_workOn;
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

	public final long getDutyCycle() {
		return m_pool!=null?m_pool.getDutyCycle(m_id) : getRequestedDutyCycle();
	}
	
	public final long getMinimumAllowedDutyCycle() {
		return m_pool!=null?m_pool.getMinimumAllowedDutyCycle():getRequestedDutyCycle();
	}

	public final long getRequestedDutyCycle() {
		return m_requestedDutyCycle;
	}

	@Override
	public long getAverageDutyCycleDelay() {
		long sum = 0;
		// get count
		int count = m_startupDelayLog.size();
		// can calculate?
		if(count>0) {
			synchronized(m_startupDelayLog) {
				// loop over all
				for(long it : m_startupDelayLog) {
					sum += it;
				}
			}
			// calculate average
			return sum/count;
		}
		return 0;
	}

	@Override
	public long getMaximumDutyCycleDelay() {
		long max = 0;
		synchronized(m_startupDelayLog) {
			// loop over all
			for(long it : m_startupDelayLog) {
				max = Math.max(max,it);
			}
		}
		// finished
		return max;
	}

	public final long getWorkTime() {
		return (long)(getDutyCycle()*getUtilization());
	}

	public final long getRequestedWorkTime() {
		return (long)(m_requestedDutyCycle*m_requestedUtilization);
	}
	
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
	public final long getMaximumWorkTime() {
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

	public final double getUtilization() {
		double u = getRequestedUtilization();
		double max = m_pool!=null?m_pool.getMaximumAllowedUtilization() : u;
		return Math.min(u, max);
	}
	
	public double getMaximumAllowedUtilization() {
		return m_pool!=null ? m_pool.getMaximumAllowedUtilization() : getRequestedUtilization();
	}
	
	
	public final double getRequestedUtilization() {
		return m_requestedUtilization;
	}
	
	public final double getAverageUtilization() {
		// can calculate?
		if(getDutyCycle()>0)
			return ((double)getAverageWorkTime())/((double)getDutyCycle());
		else
			return -1;
	}

	public final double getMaximumUtilization() {
		// can calculate?
		if(getDutyCycle()>0)
			return ((double)getMaximumWorkTime())/((double)getDutyCycle());
		else
			return -1;
	}
	
	@Override
	public final void logWorkTime(long duration) {
		// remove a random number from log?
		if(m_workTimeLog.size()==MAX_TIME_LOG_SIZE) {
			// get a random index to preserve the range of logged items
			int i = RANDOM.nextInt(MAX_TIME_LOG_SIZE-1);
			// remove
			m_workTimeLog.remove(i);
		}
		// add duration
		m_workTimeLog.add(duration);		
	}
	
	@Override
	public final void clearLogs() {
		m_workTimeLog.clear();
		m_startupDelayLog.clear();
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
	
	protected void onExit(long start) 
	{
		// get duty cycle
		long dc = getDutyCycle();
		
		// calculate the start to start delay
		long dl = m_sTic>0 ? System.currentTimeMillis() - m_sTic : 0;
		
		// calculate difference
		long dt = dl-dc;
		
		// was startup delayed?
		if(dt>LOG_DELAY_PRECISION)
		{
			// remove a random number from log?
			if(m_workTimeLog.size()==MAX_TIME_LOG_SIZE) {
				// get a random index to preserve the range of logged items
				int i = RANDOM.nextInt(MAX_TIME_LOG_SIZE-1);
				// remove
				m_startupDelayLog.remove(i);
			}
			// add to log
			m_startupDelayLog.add(new Long(dl-dc));
			// log event
			m_logger.warn("id:=" + m_id + ", dutyCycle:="+ dc + ", delay:="+dl+", difference:="+dt+", LOG_DELAY_PRECISION:="+LOG_DELAY_PRECISION);
		}
		
		// save new start time
		m_sTic = start;
		
	}
	

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

    protected void add(IWork work, boolean register) {
    	if(register) {
    		work.addWorkListener(m_workListener);
    	}
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
						m_queue.add(new FIFOEntry<IWork>(w));
					}
				}
			}

		}

    };

    /* =======================================================
	 * Inner classes
	 * ======================================================= */

    protected static class FIFOEntry<E extends Comparable<? super E>>
    							implements Comparable<FIFOEntry<E>> {
    	private final static AtomicLong seq = new AtomicLong();
    	private final long seqNum;
    	private final E entry;
    	
    	public FIFOEntry(E entry) {
    		seqNum = seq.getAndIncrement();
    		this.entry = entry;
    	}
    	
    	public E getEntry() { return entry; }
    	
    	public int compareTo(FIFOEntry<E> other) {
		    int res = entry.compareTo(other.entry);
		    if (res == 0 && other.entry != this.entry)
		    	res = (seqNum < other.seqNum ? -1 : 1);
		    return res;
    	}
    }    
}
