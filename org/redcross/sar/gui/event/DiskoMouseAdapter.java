package org.redcross.sar.gui.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/**
 * Listener that shows double clicked message line in edit mode
 * 
 * @author kenneth
 */
public class DiskoMouseAdapter extends MouseAdapter implements DiskoMouseListener
{
	private static int PAUSE_MILLIS = 100;
	private static int MILLIS_TO_SHOW = 500;
	
	private final DialogWorker m_worker;
	
	public DiskoMouseAdapter() {
		this(MILLIS_TO_SHOW);
	}
	
	public DiskoMouseAdapter(int millisToWait) {
		// prepare
		m_worker = new DialogWorker(millisToWait);
	}
	
	public long getMillisToWait() {
		return m_worker.m_millisToWait;
	}
	
	public void setMillisToWait(int millisToWait) {
		m_worker.m_millisToWait = millisToWait;
	}
	
  	/*========================================================
  	 * Override methods
  	 *========================================================
  	 */
	
	@Override
	public void mousePressed(MouseEvent e) {
		// forward
		super.mousePressed(e);
		// forward
		m_worker.start(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// forward
		super.mouseReleased(e);
		// forward
		m_worker.cancel();
	}

  	/*========================================================
  	 * DiskoMouseListener implementation
  	 *========================================================
  	 */
	
	public void mouseDownExpired(MouseEvent e) { /* NOP */ }
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
	
	private class DialogWorker implements ActionListener {
		
		private long m_start = 0;
		private long m_millisToWait = 0;
		private Timer m_timer = null;
		private boolean m_isCancelled = false;
		private MouseEvent m_event = null;
		
		public DialogWorker(long millisToWait) {
			// save decision delay
			m_millisToWait = millisToWait;
			// create timer
			m_timer = new Timer(PAUSE_MILLIS, this);
		}

		public boolean start(MouseEvent e) {
			// is not running?
			if(!m_timer.isRunning()) {
				// save
				m_event = e;
				// on construction, set time in milli seconds
				m_start = System.currentTimeMillis();
				// start timer
				m_timer.start();
				// reset flag
				m_isCancelled = false;
				// success
				return true;
			}
			// invalid
			return false;			
		}
		
		public boolean cancel() {
			// is running?
			if(m_timer.isRunning()) {
				// reset flag
				m_isCancelled = true;
				// stop timer
				m_timer.stop();
				// success
				return true;
			}
			// invalid
			return false;			
		}
		
		public boolean isRunning() {
			return m_timer.isRunning();
		}
							
		/**
		 * Worker 
		 * 
		 * Executed on the Event Dispatch Thread
		 * 
		 */
		@Override		
		public void actionPerformed(ActionEvent e) {
			// delay expired?
			if(!m_isCancelled && System.currentTimeMillis()- m_start > m_millisToWait) {
				// stop timer
				m_timer.stop();
				// notify
				mouseDownExpired(m_event);
				// stop timer
				m_timer.stop();
			}
		}			
	}	
}
