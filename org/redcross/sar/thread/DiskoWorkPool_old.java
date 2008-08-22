/**
 * 
 */
package org.redcross.sar.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.redcross.sar.thread.IDiskoWork.WorkOnThreadType;

/**
 * Singleton DISKO work pool class. Ensures that work can be executed in
 * another thread in a MSO safe manner. Methods that are not MSO thread safe 
 * are executed on the AWT thread. DISKO only supports ONE thread in 
 * addition to the AWT thread. This class schedules all work that must be
 * multitasked (executed either on the AWT (EDT) or SwingWorker thread). 
 * 
 * The first invocation of getInstance() MUST be done from the EDT thread!
 * 
 * @author kennetgu
 *
 */
public class DiskoWorkPool_old {

	private static DiskoWorkPool_old m_this;
	
	private long m_nextID = 1;
	private boolean m_isSuspended = false;
	private List<IDiskoWork<?>> m_isUnsafe = null;
	private Map<IDiskoWork<?>,DiskoWorker> m_workers = null;
	private ConcurrentLinkedQueue<IDiskoWork<?>> m_queue = null; 
  	
  	/**
	 *  private constructor
	 */		
	DiskoWorkPool_old() {
		m_isUnsafe = new ArrayList<IDiskoWork<?>>(); 
		m_workers = new HashMap<IDiskoWork<?>,DiskoWorker>();
		m_queue = new ConcurrentLinkedQueue<IDiskoWork<?>>();
	}
	
  	/*========================================================
  	 * The singleton code
  	 *========================================================
  	 */

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized DiskoWorkPool_old getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DiskoWorkPool can only " +
  						"be instansiated on the Event Dispatch Thread");  		
  			// it's ok, we can call this constructor
  			m_this = new DiskoWorkPool_old();
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
  	 * Synchronized pool methods (thread safe)
  	 *========================================================
  	 */
  	
  	public synchronized long schedule(IDiskoWork<?> work) {
  		// is null?
  		if(work==null) throw new NullPointerException("Work can not be null");
  		// create id
  		long id = createID();   		
  		// allocate id
  		work.setID(id);
  		// execute on new thread?
  		if(work.getWorkOnThread()==
				WorkOnThreadType.WORK_ON_NEW) {
  			// run work on a new swing worker new thread that is not 
  			// concurrent. Thus, should not contain unsafe work.
  	  		// is not thread safe?
  	  		if(!work.isThreadSafe()) throw new IllegalArgumentException("Work on new thread must be thread safe");
  	  		// put on work pool?
  	  		if(isSuspended()) {
  	  			// add to list 
  				m_queue.add(work);  
  	  		}
  	  		else {
  	  			// execute now
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
  	
  	public synchronized boolean isUnsafe() {
		return m_isUnsafe.size()>0;
  	}
  	
  	public synchronized boolean isSuspended() {
		return m_isSuspended;
  	}
  	  	
  	public synchronized boolean suspend() {
		if(!m_isSuspended) {
			m_isSuspended = true;
			return true;
		}
		return false;
  	}
  	
  	public synchronized boolean resume() {
		if(m_isSuspended) {
			m_isSuspended = false;
			doWork();
			return true;
		}
		return false;
  	}
  	
	public synchronized int getWorkCount() {
  		return m_queue.size();
  	}
	
	public synchronized IDiskoWork<?> getWork(long id) {
		for(DiskoWorker it : m_workers.values()) { 
			IDiskoWork<?> work = it.getWork();
			if(id == work.getID())
				return work;
		}
		return null;
	}
	
	public synchronized boolean containsWork(long id) {
		return (getWork(id)!=null);
	}
	
	public synchronized boolean containsWork(IDiskoWork<?> work) {
		return m_workers.containsKey(work);
	}
	
	public synchronized boolean isWorking(IDiskoWork<?> work) {
		if(work!=null) {
			if(containsWork(work)) {
				return m_workers.get(work).isWorking();				
			}
		}
		return false;
	}
	
	public synchronized boolean isWorking(long id) {
		return isWorking(getWork(id));
	}
	
	public synchronized boolean suspend(long id) {
		return suspend(getWork(id));
	}
	
	public synchronized boolean suspend(IDiskoWork<?> work) {
		if(containsWork(work) && work.isLoop()) {
			return m_workers.get(work).suspend();
		}
		return false;
	}
	
	public synchronized boolean resume(long id) {
		return resume(getWork(id));
	}
	
	public synchronized boolean resume(IDiskoWork<?> work) {
		if(containsWork(work) && work.isLoop()) {
			return m_workers.get(work).resume();
		}
		return false;
	}
	
	public synchronized boolean stop(long id) {
		return stop(getWork(id));
	}
	
	public synchronized boolean stop(IDiskoWork<?> work) {
		if(work!=null && work.isLoop()) {
			DiskoWorker worker = m_workers.get(work);
			if(!worker.isCancelled()) {
				worker.resume();
				return worker.cancel(false);
			}
		}
		return false;
	}
	
  	/*========================================================
  	 * private pool methods (thread safe)
  	 *========================================================
  	 */
	
	private void pushUnsafe(IDiskoWork<?> work) {
		if(!m_isUnsafe.contains(work))
			m_isUnsafe.add(work);
	}
	
	private void popUnsafe(IDiskoWork<?> work) {
		if(m_isUnsafe.contains(work))
			m_isUnsafe.remove(work);
	}
	
	private long createID() {
		long id = m_nextID; 
		m_nextID++;
		return id;
	}
  	
  	private IDiskoWork<?> getWork() {
		// get work
		return m_queue.poll();
  	}
  	
  	private synchronized void doWork() {
		// is worker available?
		if(!(isSuspended() || isUnsafe())) {
	  		// get first work in queue
	  		IDiskoWork<?> work = getWork();
	  		// execute consecutive EDT work on stack
	  		while(work!=null &&
	  				work.getWorkOnThread()==
	  				WorkOnThreadType.WORK_ON_EDT) {
	  	  		// execute work on EDT
	            execute(work,true);
	  	  		// get next work in queue
	  	  		work = getWork();
	  		}
	  		// execute on other thread then EDT?
	  		if(work!=null && work.getWorkOnThread()
	  				==WorkOnThreadType.WORK_ON_SAFE) {
	  			// forward on safe thread
	  			execute(work,false);
			}		  			
  		}
	}
  	
  	private void execute(final IDiskoWork<?> work, boolean onEDT) {
  		
  		// execute on EDT?
  		if(onEDT) {
	  		// ensure work on EDT
			if (SwingUtilities.isEventDispatchThread()) {
				// increment unsafe state?
				if(!work.isThreadSafe()) pushUnsafe(work);
				// execute
				work.run();
				// forward
				done(work);
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						execute(work,true);
					}
				});
			}
  		}
  		/* This will start a new swing worker thread. If work
  		 * is unsafe, isUnsafe is set to prevent the new work scheduled
  		 * is not executed until this work is finished (concurrent execution)
  		*/ 
  		else {
  			// increment unsafe state?
  	  		if(!work.isThreadSafe()) pushUnsafe(work);
  			// create worker
  	  		DiskoWorker worker = new DiskoWorker(work);
  	  		// add to workers
  			m_workers.put(work,worker);
  			// execute work
  			worker.execute();
  		}
  	}
  	
  	private synchronized void done(IDiskoWork<?> work) {
		// get result
		work.done();
		// remove from worker map
		m_workers.remove(work);
		// decrement unsafe state
  		popUnsafe(work);
		// continue
		doWork();
  	}
  	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	private class DiskoWorker extends SwingWorker<Object,Integer> {

		private final static long RESUME_DELAY = 1000;		// check if resume should occur every second
		private final static long MINIMUM_DUTY_CYCLE = 10;	// duty cycle can not be less then 10 milliseconds.
		
		private IDiskoWork<?> m_work = null;
		private boolean m_isWorking = false;
		
		DiskoWorker(IDiskoWork<?> work) {
			m_work = work;
		}
		
		public IDiskoWork<?> getWork() {
			return m_work;
		}
		
		public boolean isWorking() {
			return m_isWorking;
		}
		
		public boolean suspend() {
			if(m_work.isLoop() && m_isWorking) {
				m_isWorking = false;
				return true;
			}
			return false;
		}
		
		public boolean resume() {
			if(m_work.isLoop() && !(m_isWorking || isCancelled())) {
				m_isWorking = true;		
				return true;
			}
			return false;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			// catch cancel events
			try {	
				
				// initialize time tic
				long tic = 0;
				
				// set flag
				m_isWorking = true;
				
				// is work cycle?
				if(m_work.isLoop()) {

					// prepare to work
					m_work.prepare();
					
					// execute until cancel
					while(!isCancelled()) {
						
						// ensure that access to m_isWorking is done concurrently
			            try {
			            	
			            	// get duty cycle
			            	long duty = m_work.getDutyCycle();
			            	
			            	// sleep for specified time before checking again
			            	Thread.sleep(RESUME_DELAY);
			            	
			            	// try to resume
			                synchronized(this) {
			                	// stop if suspended, or canceled
			                    while (m_isWorking && !isCancelled()) {
			                    	// get current time tic
			                    	tic = System.currentTimeMillis();
			    					// execute work
			    					m_work.run();
			    					// get current work time
			    					long worked = System.currentTimeMillis() - tic;
			    					// log work time
			    					m_work.logWorkTime(worked);
			    					// calculate time left before new work cycle starts, limit to minimum duty cycle.
			    					long remainder = Math.max(MINIMUM_DUTY_CYCLE,duty - worked);
			    					// sleep for reminder of time
			    					Thread.sleep(remainder);
			                    }
			                }
			                
			            } catch (InterruptedException e) { /* NOP */ }
			            
					}
					
					// finished
					m_work.done();
					
				}
				else {
                	// get current time tic
                	tic = System.currentTimeMillis();
					// execute once (prepare() and done() is automatically handled)
					m_work.run();
					// log work time
					m_work.logWorkTime(System.currentTimeMillis() - tic);
					
				}
				// reset flag
				m_isWorking = false;				
	            // return result
	            return m_work.get();
            } 
			catch(Exception e) {
				// not a interrupt?
				if(!(e instanceof InterruptedException)) 
					e.printStackTrace();
			}
			// failed
			return null;
		}
	  	
	  	@Override
		protected void done() {
			// forward to work pool
			DiskoWorkPool_old.this.done(m_work);
		}		
	}
	
	private class SafeWorker extends SwingWorker<Object,Integer> {
			
		final private ConcurrentLinkedQueue<IDiskoWork<?>> m_queue =  
			new ConcurrentLinkedQueue<IDiskoWork<?>>();
		
		private Thread m_this = null;
				
		public synchronized void schedule(IDiskoWork<?> work) {
	  		
  			// add to list 
			m_queue.add(work);			
			
	  	}
		
		@Override
		protected Object doInBackground() throws Exception {					
			
			// TODO Auto-generated method stub
			return null;
			
		}
		
	}

	
}
