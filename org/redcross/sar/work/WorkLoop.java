package org.redcross.sar.work;

import java.util.Vector;
import java.util.Collection;

import org.redcross.sar.work.IWork.ThreadType;

public class WorkLoop extends AbstractWorkLoop {

	/**
	 * Current work
	 */
	private FIFOEntry<IWork> m_current;

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	/**
	 * This implements a work loop (deamon) that will be executed on
	 * a unsafe thread if scheduled on the WorkPool. The target
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
	public WorkLoop(long dutyCycle, int timeout) throws Exception {
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
	protected WorkLoop(ThreadType thread, long dutyCycle, int timeout) throws Exception {
		// forward
		super(thread, dutyCycle, timeout);
	}

	/* =======================================================
	 * IWorkLoop implementation
	 * ======================================================= */

  	public synchronized IWork findWork(long id) {
		synchronized(m_queue) {
	  		for(FIFOEntry<IWork> it : m_queue) {
	  			IWork work = it.getEntry();
	  			if(work.getID() == id) return work;
	  		}
		}
  		return null;
  	}

  	public boolean contains(IWork work) {
  		return m_queue.contains(work);
  	}

	public synchronized boolean isScheduled(IWork work) {
		return m_queue.contains(work);
	}

	public synchronized boolean isExecuting(IWork work) {
		return (m_current!=null&&(m_current.getEntry() == work));
	}

	public boolean isIdle() {
		return (m_current!=null);
	}

	public synchronized int doWork() {

		// initialize
		int count = 0;
		long duration = 0;
		Collection<FIFOEntry<IWork>> loops = new Vector<FIFOEntry<IWork>>(100);

		// get cycle start time
		long tic = System.currentTimeMillis();
		
		// loop until finished or timeout
		while(duration < m_timeOut && m_queue.peek()!=null) {

			// get head of queue;
			m_current = m_queue.poll();
			
			// get work
			IWork work = m_current.getEntry();

			try {

				/* =============================================
				 * Determine if work can be executed safely
				 * =============================================
				 *
				 * If work loop is running on a safe thread,
				 * any work can be executed. Else, only work
				 * indicated safe is executed.
				 *
				 * ============================================= */
				if(getThreadType().equals(ThreadType.WORK_ON_SAFE) || work.isSafe()) {
					// do the work
					work.run();
				}

			} catch (RuntimeException e) {
				// TODO Log the error
				e.printStackTrace();
			}

			// increment
			count++;

			/* ==========================================================
			 * Reschedule loops
			 * ==========================================================
			 * This implements the work reschedule request. If isLoop()
			 * is true, this indicates that it should be rescheduled on
			 * the work loop. Each work found to have a isLoop true,
			 * should be added in a manner that retains the same order
			 * that they were scheduled the first time. Hence, each new
			 *
			 *
			 * ========================================================== */

			// add to reschedule list?
			if(work.isLoop()) loops.add(m_current);

			// update duration of this cycle so far
			duration = System.currentTimeMillis()-tic;

		}

		// reset
		m_current = null;

		// reschedule work?
		if(loops.size()>0)  m_queue.addAll(loops);

		// save duration
		logWorkTime(duration);

		// finished
		return count;

	}

	/* =======================================================
	 * Protected methods
	 * ======================================================= */

	@Override
    protected void add(IWork work,boolean register) {
		super.add(work,register);
		m_queue.add(new FIFOEntry<IWork>(work));
    }

	@Override
    protected boolean remove(IWork work) {
		super.remove(work);
		return m_queue.remove(work);
    }

	/*
	public static void main(String args[])
	{
		try {
			WorkPool pool = WorkPool.getInstance();
			WorkLoop loop = new WorkLoop(1000,500);
			for(int i=0;i<=10;i++) {
				loop.schedule(new Work(i,true,false,ThreadType.WORK_ON_LOOP,"",0,false,false) {
					public Void doWork() {
						System.out.println("Priority:"+m_priority);
						return null;
					}
				});
			}
			pool.add(loop);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

}
