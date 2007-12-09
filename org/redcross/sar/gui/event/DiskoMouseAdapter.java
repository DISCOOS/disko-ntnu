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
public class DiskoMouseAdapter extends MouseAdapter
{
	private static int PAUSE_MILLIS = 100;
	private static int MILLIS_TO_SHOW = 1000;
	
	private final DialogWorker m_worker;
	
	private final ArrayList<DiskoMouseDelayListener> m_listeners;
	
	public DiskoMouseAdapter() {
		// prepare
		m_worker = new DialogWorker(MILLIS_TO_SHOW);
		m_listeners = new ArrayList<DiskoMouseDelayListener>();
	}
	
	public DiskoMouseAdapter(int millisToShow) {
		// prepare
		m_worker = new DialogWorker(millisToShow);
		m_listeners = new ArrayList<DiskoMouseDelayListener>();
	}
	
	public void addDiskoMouseDelayListener(DiskoMouseDelayListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeDiskoMouseDelayListener(DiskoMouseDelayListener listener) {
		m_listeners.remove(listener);
	}
	
	public long getMillisToShow() {
		return m_worker.m_millisToShow;
	}
	
  	/*========================================================
  	 * Override mouse events
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
  	 * Helper methods
  	 *========================================================
  	 */
	
	private void fireMouseDownExpired(MouseEvent e) {
		for(DiskoMouseDelayListener it: (List<DiskoMouseDelayListener>)m_listeners) {
			it.mouseDownExpired(e);
		}
	}
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	public interface DiskoMouseDelayListener {
		public void mouseDownExpired(MouseEvent e);
	}
	
	private class DialogWorker implements ActionListener {
		
		private long m_start = 0;
		private long m_millisToShow = 0;
		private Timer m_timer = null;
		private boolean m_isCancelled = false;
		private MouseEvent m_event = null;
		
		public DialogWorker(long millisToShow) {
			// save decision delay
			m_millisToShow = millisToShow;
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
			if(!m_isCancelled && System.currentTimeMillis()- m_start > m_millisToShow) {
				// stop timer
				m_timer.stop();
				// notify
				fireMouseDownExpired(m_event);
				// stop timer
				m_timer.stop();
			}
		}			
	}	
}
