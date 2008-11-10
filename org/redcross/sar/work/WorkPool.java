/**
 *
 */
package org.redcross.sar.work;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.redcross.sar.work.IWork.ThreadType;
import org.redcross.sar.work.IWork.WorkState;
import org.redcross.sar.work.IWorkLoop.LoopState;
import org.redcross.sar.work.event.IWorkLoopListener;
import org.redcross.sar.work.event.WorkLoopEvent;

/**
 * Singleton DISKO work pool class. Ensures that work can be executed in
 * on several threads in a MSO and Swing safe manner. Work that is indicated not
 * thread safe, is executed on either the EDT or SAFE thread . DISKO only supports
 * ONE SAFE thread in addition to the EDT thread. The work pool ensures that work on
 * these threads are executed synchronously.
 *
 * <b>IMPORTANT</b>: The first invocation of getInstance() MUST be done from the EDT thread!
 *
 * @author kennetgu
 *
 */
public class WorkPool {

	private static WorkPool m_this;

	private long m_safeID = 0;
	private long m_unsafeID = 0;
	private long m_nextID = 0;

	private boolean m_isSuspended = false;

	private List<IWork> m_isUnsafe;
	private ConcurrentLinkedQueue<IWork> m_queue;
	private ConcurrentHashMap<Long,Worker> m_workers;

	private final Object m_nextLock = new Object();

  	/*========================================================
  	 * Constructors singleton code
  	 *======================================================== */

  	/**
	 *  private constructor
  	 * @throws Exception
	 */
	private WorkPool() throws Exception {
		// prepare
		m_isUnsafe = new ArrayList<IWork>();
		m_workers = new ConcurrentHashMap<Long,Worker>();
		m_queue = new ConcurrentLinkedQueue<IWork>();
		// create default work loops
		m_safeID = add(new WorkLoop(ThreadType.WORK_ON_SAFE,100,50));
		m_unsafeID = add(new WorkLoop(ThreadType.WORK_ON_UNSAFE,100,50));
	}

  	/*========================================================
  	 * The singleton code
  	 *======================================================== */

	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
  	public static synchronized WorkPool getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("WorkPool can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new WorkPool();
  		}
  		return m_this;
  	}

	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
  	public Object clone() throws CloneNotSupportedException{
  		throw new CloneNotSupportedException();
  		// that'll teach 'em
  	}

  	/*========================================================
  	 * Public methods
  	 *======================================================== */

  	/**
  	 * add a new work loop. Only work loops on unsafe threads are allowed to add
  	 *
  	 * @return long - The work loop id
  	 */
  	public long add(IWorkLoop loop) {
  		// validate
  		validate(loop,m_safeID==0);
  		// create id
  		long id = createID();
  		// allocate id
  		loop.setID(id);
  		// add loop listener
  		loop.addWorkLoopListener(m_loopListener);
		// create worker
  		Worker worker = new Worker(loop);
  		// add to workers
		m_workers.put(id,worker);
		// start work loop
		worker.execute();
	  	// schedule now
  		return id;
  	}

  	/**
  	 * Use this method to destroy work loops
  	 *
  	 * @param long id - work loop id
  	 * @return Boolean - <code>true</code> if allowed,
  	 * <code>false</code> else.
  	 */
  	public boolean remove(long id) {
  		return remove(id,true,false);
  	}

  	/**
  	 * Get the number of work loops in work pool
  	 *
  	 * @return long - The work loop count
  	 */
  	public int getLoopCount() {
  		return m_workers.size();
  	}

  	/**
  	 * Schedule work on work pool. The work pool will schedule the work
  	 * according to the work attributes.
  	 *
  	 * @param IWork work - work object
  	 *
  	 * @return long - The work id
  	 */
  	public long schedule(IWork work) {
  		// forward
  		validate(work);
  		// create id
  		long id = createID();
  		// allocate id
  		work.setID(id);
  		// execute on unsafe loop?
  		if(work.isSafe() &&
  				ThreadType.WORK_ON_UNSAFE.equals(work.getThreadType())) {
  	  		// put on work pool?
  	  		if(isSuspended()) {
  	  			// add to list
  				m_queue.add(work);
  	  		}
  	  		else {
  	  			// execute now on a new swing worker thread
  	  			execute(work,false);
  	  		}
  		}
  		else {
  			// add to list
			m_queue.add(work);
			// forward
			doWork();
  		}
  		// finished
  		return id;
  	}

  	public boolean isUnsafe() {
  		synchronized(m_isUnsafe) {
  			return m_isUnsafe.size()>0;
  		}
  	}

  	public boolean isSuspended() {
		return m_isSuspended;
  	}

  	public boolean suspend() {
		if(!m_isSuspended) {
			m_isSuspended = true;
			return true;
		}
		return false;
  	}

  	public boolean resume() {
		if(m_isSuspended) {
			m_isSuspended = false;
			doWork();
			return true;
		}
		return false;
  	}

	public int getWorkCount() {
  		return m_queue.size();
  	}

	public IWork findWork(long id) {
		synchronized(m_workers) {
			for(Worker it : m_workers.values()) {
				IWorkLoop loop = it.getLoop();
				IWork work = loop.findWork(id);
				if(work!=null) return work;
			}
		}
		return null;
	}

	public IWorkLoop findLoop(long id) {
		synchronized(m_workers) {
			for(Worker it : m_workers.values()) {
				IWorkLoop loop = it.getLoop();
				if(loop.getID()==id) return loop;
			}
		}
		return null;
	}

	public IWorkLoop findLoop(IWork work) {
		synchronized(m_workers) {
			for(Worker it : m_workers.values()) {
				IWorkLoop loop = it.getLoop();
				if(loop.contains(work)) return loop;
			}
		}
		return null;
	}

  	/*========================================================
  	 * protected pool methods (thread safe)
  	 *======================================================== */

	protected long createID() {
		synchronized(m_nextLock) {
			m_nextID++;
			return m_nextID;
		}
	}

  	/*========================================================
  	 * private pool methods (thread safe)
  	 *======================================================== */

	private void validate(IWorkLoop loop, boolean any) {
  		// is null?
  		if(loop==null)
  			throw new NullPointerException("Work loop can not be null");
  		// check valid id?
  		if(loop.getID()!=0) {
  			throw new IllegalArgumentException("Work loops can only be added once (found id " + loop.getID() + ")");
  		}
		synchronized(m_workers) {
	  		// loop over all existing and compare
	  		for(Worker it : m_workers.values()) {
	  			if(it.getLoop()==loop) {
	  				throw new IllegalArgumentException("Work loops can only be added once");
	  			}
	  		}
		}
  		// check valid thread type?
  		if(!any && ThreadType.WORK_ON_SAFE.equals(loop.getThreadType())) {
  			throw new IllegalArgumentException("Only work loops on unsafe threads are allowed");
  		}
	}

	private void validate(IWork work) {
  		// is null?
  		if(work==null)
  			throw new NullPointerException("Work can not be null");
  		// is legal thread type?
  		if(ThreadType.WORK_ON_LOOP.equals(work.getThreadType())) {
  			throw new IllegalArgumentException("Work not supported by WorkPool. Work should be executed on a (deamon) work loop");
  		}
  		// execute on new thread?
  		if(!work.isSafe() && ThreadType.WORK_ON_UNSAFE.equals(work.getThreadType())) {
  			// run work on a new swing worker new thread that is not
  			// concurrent. Thus, should not contain unsafe work.
  	  		throw new IllegalArgumentException("Work on new thread must be thread safe");
  		}
	}

	private void pushUnsafe(IWork work) {
		synchronized(m_isUnsafe) {
			if(!m_isUnsafe.contains(work))
				m_isUnsafe.add(work);
		}
	}

	private void popUnsafe(IWork work) {
		synchronized(m_isUnsafe) {
			if(m_isUnsafe.contains(work))
				m_isUnsafe.remove(work);
		}
	}

  	private IWork getWork() {
		// get work
		return m_queue.poll();
  	}

  	private void doWork() {
		// is worker available?
		if(!(isSuspended() || isUnsafe())) {
	  		// get first work in queue
	  		IWork work = getWork();
	  		// execute consecutive EDT work on stack
	  		while(work!=null &&
	  				work.getThreadType()==
	  				ThreadType.WORK_ON_EDT) {
	  	  		// execute work on EDT
	            execute(work,true);
	  	  		// get next work in queue
	  	  		work = getWork();
	  		}
	  		// execute on other thread then EDT?
	  		if(work!=null) {
	  			// forward on safe thread
	  			execute(work,false);
			}
  		}
	}

  	private void execute(final IWork work, boolean onEDT) {

  		// execute on EDT?
  		if(onEDT) {
	  		// ensure work on EDT
			if (SwingUtilities.isEventDispatchThread()) {
				// increment unsafe state?
				if(!work.isSafe()) pushUnsafe(work);
				// execute
				work.run();
				// forward
				progress(work);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						execute(work,true);
					}
				});
			}
  		}

  		/* ==============================================
  		 *  Pick a work loop
  		 * ============================================== */
  		else {
  			// safe work?
  	  		if(work.isSafe()) {
  	  			// get suitable unsafe work loop
  	  			IWorkLoop loop = select();
  	  			// forward
				loop.schedule(work);
  	  		}
  	  		else {
  	  			// increment unsafe state
  	  			pushUnsafe(work);
  	  			// get safe work loop worker
  	  			Worker worker = m_workers.get(m_safeID);
  	  			// found worker
  	  			if(worker!=null) {
	  	  	  		// schedule on safe worker
  	  				worker.getLoop().schedule(work);
  	  			}
  	  		}
  		}
  	}

  	private IWorkLoop select() {
  		// select the worker that is less utilized
		List<Object[]> loops = new ArrayList<Object[]>(m_workers.size());
		synchronized(m_workers) {
	  		// select an idle worker
			for(Worker w : m_workers.values()) {
				IWorkLoop loop = w.getLoop();
				if(loop.getID()!=m_safeID) {
					if(loop.isState(LoopState.IDLE)) {
						return loop;
					}
					loops.add(new Object[]{loop.getUtilization(),loop});
				}
			}
		}
		// sort utilization ascending
		Collections.sort(loops,m_comparator);
		// select the loop with lowest utilization at this point
		return loops.size()>0 ? (IWorkLoop)loops.get(0)[1] : null;
  	}

  	private void progress(IWork work) {
		// decrement unsafe state
  		popUnsafe(work);
		// continue
		doWork();
  	}

  	private boolean remove(long id, boolean cancel, boolean force) {
		// allowed?
		if(force || id>m_unsafeID) {
			// get worker
			Worker w = m_workers.get(id);
			// found worker?
	  		if(w!=null) {
	  			// cancel operation?
	  			if(cancel)w.cancel(true);
	  			// reset id
	  			w.getLoop().setID(0);
	  	  		// remove loop listener
	  			w.getLoop().removeWorkLoopListener(m_loopListener);
	  			// remove from map
	  			return (m_workers.remove(id)!=null);
	  		}

		}
		return false;
  	}

  	/*========================================================
  	 * Anonymous classes
  	 *======================================================== */

	private IWorkLoopListener m_loopListener = new IWorkLoopListener() {

		@Override
		public void onLoopChange(WorkLoopEvent e) {
			// listen for work changes
			if(e.isWorkEvent()) {
				// cast to work
				IWork work = (IWork)e.getData();
				// cleanup?
				if(	work.isState(WorkState.FINISHED)
					|| work.isState(WorkState.CANCELED)) {
					// forward
					progress(work);
				}
			}

		}

	};

  	private static Comparator<Object[]> m_comparator = new Comparator<Object[]>() {

		@Override
		public int compare(Object[] o1, Object[] o2) {
			Double u1 = (Double)o1[0];
			Double u2 = (Double)o2[0];
			return u1.compareTo(u2);
		}

  	};

  	/*========================================================
  	 * Inner classes
  	 *======================================================== */

	private class Worker extends SwingWorker<Object,Integer> {

		private final static long RESUME_DELAY = 1000;		// check if resume should occur every second
		private final static long MINIMUM_DUTY_CYCLE = 10;	// duty cycle can not be less then 10 milliseconds.

		private IWorkLoop m_loop;

		private boolean m_isExecuting = false;
		private boolean m_isSuspended = false;

		private Thread m_thread;

		private final Object m_lock = new Object();

		/* =========================================
		 * Constructors
		 * ========================================= */

		public Worker(IWorkLoop loop) {
			m_loop = loop;
			m_loop.addWorkLoopListener(new IWorkLoopListener() {

				@Override
				public void onLoopChange(WorkLoopEvent e) {
					if(e.isStateEvent()) {
						LoopState state = (LoopState)e.getData();
						switch(state) {
							case CANCELED: Worker.this.cancel(false); break;
							case EXECUTING: Worker.this.resume(); break;
							case SUSPENDED: Worker.this.suspend(); break;
						}
					}
				}

			});
		}

		/* =========================================
		 * Constructors
		 * ========================================= */

		public IWorkLoop getLoop() {
			return m_loop;
		}

		public boolean isExecuting() {
			return m_isExecuting && !(m_isSuspended || isDone() || isCancelled());
		}

		public boolean isSuspended() {
			return m_isSuspended && m_isExecuting && !(isDone() || isCancelled());
		}

		public boolean suspend() {
			if(isExecuting()) {
				synchronized(m_lock) {
					m_isSuspended = true;
					return true;
				}
			}
			return false;
		}

		public boolean resume() {
			if(isSuspended()) {
				synchronized(m_lock) {
					m_isSuspended = false;
					m_thread.interrupt();
					return true;
				}
			}
			return false;
		}

		@Override
		protected Object doInBackground() throws Exception {

			// catch cancel events
			try {

				// initialize time tic
				long tic = 0;

				// get current thread
				m_thread = Thread.currentThread();

				// set flag
				m_isExecuting = true;

            	// get duty cycle
            	long duty = m_loop.getDutyCycle();

            	// loop until canceled
                while (!isCancelled()) {

                	// clear current interrupt state
                	Thread.interrupted();

                	// resume on interrupt
		            try {

                    	// is suspended?
                    	if(isSuspended()) {
	    					// sleep and try again
	    					Thread.sleep(RESUME_DELAY);
                    	}
                    	else {
	                    	// get current time tic
	                    	tic = System.currentTimeMillis();
	    					// execute work
	                    	m_loop.run();
	    					// get current work time
	    					long worked = System.currentTimeMillis() - tic;
	    					// log work time
	    					m_loop.logWorkTime(worked);
	    					// calculate time left before new work cycle starts, limit to minimum duty cycle.
	    					long remainder = Math.max(MINIMUM_DUTY_CYCLE,duty - worked);
	    					// sleep for reminder of time
	    					Thread.sleep(remainder);
                    	}
		            } catch (InterruptedException e) { /* NOP */ }
                }

				// reset flag
				m_isExecuting = false;

	            // return result
	            return m_loop.getUtilization();

            }
			catch(Exception e) {
				// TODO: handle error globally
				e.printStackTrace();
			}
			// failed
			return null;
		}

	  	@Override
		protected void done() {
			// forward to work pool
			WorkPool.this.remove(m_loop.getID(),false,true);
		}
	}
}

