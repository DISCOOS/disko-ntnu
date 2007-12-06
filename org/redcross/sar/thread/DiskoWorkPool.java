/**
 * 
 */
package org.redcross.sar.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.redcross.sar.thread.IDiskoWork.WorkOnThreadType;

/**
 * Singleton disko work pool class. Ensures that work can be executed in
 * another thread in a mso safe manner. Methods that are not mso thread safe 
 * are executed on the AWT thread. Disko only supports ONE thread in 
 * addition to the AWT thread. This class schedules all work that must be
 * multitasked (executed either on the AWT (EDT) or SwingWorker thread). 
 * 
 * The first invocation of getInstance() MUST be done from the AWT thread!
 * 
 * @author kennetgu
 *
 */
public class DiskoWorkPool {

	private static DiskoWorkPool m_this;
	
	private boolean m_isWorking = false;
	private DiskoWorker m_waitOn = null;
	private Map<IDiskoWork,DiskoWorker> m_workers = null;
	private ConcurrentLinkedQueue<IDiskoWork> m_queue = null;
  	
  	/**
	 *  private constructor
	 */		
	DiskoWorkPool() {
		m_workers = new HashMap<IDiskoWork,DiskoWorker>();
		m_queue = new ConcurrentLinkedQueue<IDiskoWork>();
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
  	public static synchronized DiskoWorkPool getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DiskoWorkPool can only " +
  						"be instansiated on the Event Dispatch Thread");  		
  			// it's ok, we can call this constructor
  			m_this = new DiskoWorkPool();
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
  	
  	public synchronized int schedule(IDiskoWork work) {
  		// is null?
  		if(work==null) throw new NullPointerException("Work can not be null");
  		// execute on new thread?
  		if(work.getWorkOnThread()==
				WorkOnThreadType.WORK_ON_NEW) {
  			// run work on a new swingworker new thread that is not 
  			// concurrent. Thus, should not contain unsafe work.
  			return execute(work,true);
  		}
  		else {

  			// add to list 
			m_queue.add(work);  // (throws NullPointer Exception if work is null)
			// forward
			doWork();
			// return size
	  		return m_queue.size();	
  		}
  	}
  	
  	public synchronized boolean isWorking() {
		return m_isWorking;
  	}
  	
	public synchronized int getWorkCount() {
  		return m_queue.size();
  	}

  	/*========================================================
  	 * private pool methods (thread safe)
  	 *========================================================
  	 */
  	
  	private IDiskoWork getWork() {
		// get work
		return m_queue.poll();
  	}
  	
  	private synchronized void doWork() {
		// is worker available?
		if(!isWorking()) {
	  		// get first work in queue
	  		IDiskoWork work = getWork();
	  		// 
	  		while(work!=null &&
	  				work.getWorkOnThread()==
	  				WorkOnThreadType.WORK_ON_EDT) {
				// execute work
	            execute(work); 
	  	  		// get next work in queue
	  	  		work = getWork();
	  		}
	  		// execute on other thread then EDT?
	  		if(work!=null && work.getWorkOnThread()
	  				==WorkOnThreadType.WORK_ON_SAFE) {
	  			// forward
	  			execute(work,false);
			}		  			
  		}
	}
  	
  	private int execute(IDiskoWork work, boolean isThreadSafe) {
		// set work flag?
  		if(!isThreadSafe)
  			m_isWorking = true;
		// create worker
  		DiskoWorker worker = new DiskoWorker(work);
  		// add to workers
		m_workers.put(work,worker);
		// execute work
		worker.execute();
		// return number in set
		return m_workers.size();
  	}
  	
  	private void execute(IDiskoWork work) {
  		// create runnable
  		
  		if(SwingUtilities.isEventDispatchThread()) {
			// execute
			work.run(); 
  		}
  		else {
  			// invoke on EDT thread and wait for result
			SwingUtilities.invokeLater(work);
  		}
  	}
  	
  	private synchronized void done(IDiskoWork work) {
		// set work flag?
  		if(work.isThreadSafe())
  			m_isWorking = false;
		// get result
		work.done();
		// remove from worker map
		m_workers.remove(work);
		// do next work?
		if(work.isThreadSafe()) {
			// continue
			doWork();
		}
  	}
  	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	private class DiskoWorker extends SwingWorker<Object,Integer> {

		private IDiskoWork m_work = null;
		
		DiskoWorker(IDiskoWork work) {
			m_work = work;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			// catch cancel events
			try {	
				// execute
				m_work.run(); 
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
			DiskoWorkPool.this.done(m_work);
		}		
	}	
}

