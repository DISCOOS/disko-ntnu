package org.redcross.sar.work;

import java.util.Vector;
import java.util.Collection;

import org.redcross.sar.work.IWork.WorkerType;

/**
 * This class extends the AbstractWorkLoop class. It implements default work 
 * loop functionality that complies with the work pool rules. (only one SAFE WORK POOL) 
 * @author Administrator
 *
 */
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
	 * a unsafe thread if scheduled on the WorkPool. The requested
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
	 * A) To ensure that work complies to the DISKO thread safe requirements,
	 * do not implement work that change the MSO model, nor access
	 * any Swing components. If these guidelines are not followed, it will
	 * result in inconsistent data and possibly severe GUI failures. </p>
	 *
	 * B) This IWorkLoop implementation will check the passed work an try to
	 * determine if the work can be executed safely. If the work loop is
	 * not executed on a safe thread, and the scheduled work implements
	 * the IWork interface, only work with <code>isThreadSafe():=false</code>
	 * is executed. In general, work that is not DISKO thread safe,
	 * should be scheduled on the DISKO work pool directly! The DISKO Work
	 * Pool has a safe thread running already. </p>
	 */
	public WorkLoop(long requestedDutyCycle, double requestedUtilization) throws Exception {
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
	 * A) To ensure that work complies to the DISKO thread safe requirements,
	 * do not implement work that change the MSO model, nor access
	 * any Swing components. If these guidelines are not followed, it will
	 * result in inconsistent data and possibly severe GUI failures. </p>
	 *
	 * B) This IWorkLoop implementation will check the passed work an try to
	 * determine if the work can be executed safely. If the work loop is
	 * not executed on a safe thread, and the scheduled work implements
	 * the IWork interface, only work where <code>isSafe()</code> is
	 * <code>false</code> is executed. In general, work that is not
	 * DISKO thread safe, should be scheduled on the DISKO work pool
	 * directly! The DISKO Work Pool has a safe thread running already. </p>
	 *
	 */
	protected WorkLoop(WorkerType workOn, long requestedDutyCycle, double requestedUtilization) throws Exception {
		// forward
		super(workOn, requestedDutyCycle, requestedUtilization);
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
		while(duration < getWorkTime() && m_queue.peek()!=null) {

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
				if(getWorkOnType().equals(WorkerType.SAFE) || work.isSafe()) {
					// do the work
					work.run(this);
				}
				else
				{
					m_logger.debug("Failed to execute work. Work was not safe"+work);
				}

			} catch (RuntimeException e) {
				m_logger.error("Failed to execute work",e);
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
			if(work.isLoop()) { 
				loops.add(m_current);
			}

			// update duration of this cycle so far
			duration = System.currentTimeMillis()-tic;

		}

		// reset
		m_current = null;

		// reschedule work?
		if(loops.size()>0)  m_queue.addAll(loops);

		// save work time?
		if(getID()==0)
		{
			logWorkTime(duration);
		}
		
		// calculate startup delay
		onExit(tic);		

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
				loop.schedule(new Work(i,true,false,ThreadType.UNSAFE,"",0,false,false) {
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
