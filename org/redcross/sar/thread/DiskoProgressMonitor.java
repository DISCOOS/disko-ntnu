package org.redcross.sar.thread;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.DiskoProgressDialog;
import org.redcross.sar.gui.DiskoProgressPanel;
import org.redcross.sar.thread.DiskoProgressEvent.DiskoProgressEventType;

/**
 * Singleton disko progress class. It decides if a progress dialog should
 * be presented or not and ensures that access to graphical Swing components 
 * are accessed on the AWT (Event Dispatch Thread) thread. The first invocation
 * of getInstance() MUST be done from the AWT thread!
 * 
 * @author kennetgu
 *
 */
public class DiskoProgressMonitor {
	   
	private static int PAUSE_MILLIS = 500;
	
	private static DiskoProgressMonitor m_this;
	
	private int m_min = 0;
	private int m_max = 0;
	private int m_progress = 0;
	private int m_isInAction = 0;
	private boolean m_inhibit = true;
	private long m_millisToPopup = 10;
	private String m_note = null;
	private boolean m_intermediate = false;
	private DecisionWorker m_worker = null;
	private boolean m_hasProgress = false;
	private boolean m_oldState = false;
	
	private DiskoGlassPane m_glassPane = null;
	private DiskoProgressDialog m_progressDialog = null;
	private DiskoProgressPanel m_progressPanel = null;
	
	private List<IDiskoProgressListener> m_listeners = null;

	private DiskoProgressMonitor() {
		// prepare
		m_listeners = new ArrayList<IDiskoProgressListener>();
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
  	public static synchronized DiskoProgressMonitor getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DiskoProgressMonitor can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new DiskoProgressMonitor();
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
  	 * Synchronized methods (thread safe)
  	 *========================================================
  	 */
  	
	public synchronized int start(String note) {
		return start(note,0,0);
	}
	
	public synchronized int start(String note, int min, int max) {
		return start(note,min,max,0);
	}
	
	public synchronized int start(String note, int min, int max, int progress) {
		// initialize
		m_min = 0;
		m_max = 0;
		m_progress = progress; 
		m_note = note;
		m_intermediate = (min == max);
		// start progress?
		if(m_isInAction == 0) {
			// reset progress
			m_progress = 0;
			// start decision thread
			create();
		}
		else {
			// increment
			m_isInAction++;
			// update progress bar
			update();
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_UPDATE);			
		}
		// return count
		return m_isInAction;
	}
	
	public synchronized int progress(String note, int step, boolean auto) {
		if(m_isInAction>0) {		
			// update
			m_note = note;
			m_progress = step;
			// update progress bar
			update();
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_UPDATE);
		}
		else if (auto){
			start(note);
		}
		return m_isInAction;
	}
	
	public synchronized int finish() {
		return(finish(false));
	}
	
	public synchronized int finish(boolean force) {		
		// decrement
		if(m_isInAction>0)
			m_isInAction--;		
		// finished?
		if(m_isInAction==0 || force) {		
			// forward
			destroy();
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_FINISH);
		}
		// resturn state
		return m_isInAction;
	}
	
	/**
	 * Returns the maximum value -- the higher end of the progress value.
	 * 
	 */
	public synchronized int getMaximum() {
		return m_max;
	}

	/**
	 * Returns the amount of time it will take for the popup to appear.
	 */
	public synchronized long getMillisToPopup() {
		return m_millisToPopup;
	}

	/**
	 * Specifies the amount of time it will take for the popup to appear.
	 * 
	 * @param millisToPopup
	 */
	public synchronized void setMillisToPopup(long millisToPopup){
		m_millisToPopup = millisToPopup;
	}

	/**
	 * Returns the minimum value -- the lower end of the progress value.
	 * 
	 */
	public synchronized int getMinimum() {
		return m_min;
	}

	/**
	 * Specifies the additional note that is displayed along with the progress
	 * message.
	 * 
	 */
	public synchronized String getNote() {
		return m_note;
	}

	/**
	 * Specifies the additional note that is displayed along with the progress
	 * message.
	 * 
	 * @param note
	 */
	public synchronized void setNote(String note){
		// update
		m_note = note;
		// notify?
		if(m_isInAction>0) {
			// update progress panel
			update();			
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_UPDATE);
		}
	}

	/**
	 * Get current progress value of operation beeing monitored
	 * 
	 */
	public synchronized int getProgress() {
		return m_progress;
	}
	
	/**
	 * If true, this indicates that the length of the operation si not known.
	 * 
	 */
	public synchronized boolean getIntermediate() {
		return m_intermediate;
	}
	
	/**
	 * Returns true if the start method has been invoked. Remains true until
	 * either cancel or finish method is called.
	 * 
	 */
	public synchronized boolean isInAction() {
		// return count
		return (m_isInAction>0);
	}

	/**
	 * Returns count of actions
	 * 
	 */
	public synchronized int isInActionCount() {
		// return count
		return m_isInAction;
	}
		
	public synchronized void addListener(IDiskoProgressListener listener) {
		m_listeners.add(listener);		
	}

	public synchronized void removeListener(IDiskoProgressListener listener) {
		m_listeners.remove(listener);
	}
	
	public synchronized boolean isInhibit() {
		return m_inhibit;
	}
	
	public synchronized void setInhibit(boolean inhibit) {
		m_inhibit = inhibit;
	}
	
	public synchronized void hide() {
		// get state
		m_oldState = isVisible();
		if(m_oldState)
			setVisible(false);
	}
	
	public synchronized boolean showAgain() {
		// show?
		if(m_oldState && !m_inhibit && !isVisible()) {
			setVisible(true);
			return true;
		}
		return false;
	}
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
	
	private synchronized void create() {
		try {				
			// create decision?
			if(m_worker==null) {
				// reset flag
				m_hasProgress = false;
				// is in action
				m_isInAction = 1;
				// create decision worker
				m_worker = new DecisionWorker(m_millisToPopup);
				// execute in the background
				m_worker.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void destroy() {
		// not in action any more
		m_isInAction = 0;
		// destroy?
		if(m_worker!=null) {
			// cancel thread
			m_worker.cancel();
			// reset pointer  
			m_worker = null; // (execute can only be invoked once per instance)
		}
		// forward
		setVisible(false);
	}
	
	private void update() {
		// valid?
		if(m_isInAction>0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						// prepate progress bar
						if(getIntermediate()) {
							getProgressPanel().setLimits(m_min, m_max, true);
						}
						else {
							getProgressPanel().setLimits(0, 0, false);
						}
						// update progress 
						getProgressPanel().setProgress(m_progress,m_note,m_note);			
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	public synchronized boolean isVisible() {
		// hide progress
		return getGlassPane().isVisible() && getProgressDialog().isVisible();
	}
	
	public void setProgressLocationAt(final JComponent c) { 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				getGlassPane().setProgressLocationAt(c);
			}
		});		
	}

	
	private synchronized void setVisible(final boolean isVisible) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// prepare?
					if(isVisible) {
						update();
					}
					getGlassPane().setVisible(isVisible);
					getProgressDialog().setVisible(isVisible);				
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void fireUpdateProgressEvent(final DiskoProgressEventType type) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					// get event
					fireProgressEvent(new DiskoProgressEvent(this,type));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void fireProgressEvent(DiskoProgressEvent e) {
		// notify listeners
		for(int i=0;i<m_listeners.size();i++) {
			m_listeners.get(i).changeProgress(e);
		}					
	}
	
	/**
	 * Helper function
	 * 
	 * @return DiskoGlassPane
	 */
	private DiskoGlassPane getGlassPane() {
    	if(m_glassPane==null) {
    		m_glassPane = (DiskoGlassPane)Utils.getApp().getFrame().getGlassPane();
    	}
    	return m_glassPane;
    }
	
	/**
	 * Helper function
	 * 
	 * @return DiskoProgressDialog
	 */
	private DiskoProgressDialog getProgressDialog() {
		// initialize?
		if(m_progressDialog==null) {
			m_progressDialog = getGlassPane().getProgressDialog();
		}
		return m_progressDialog;		
	}
	
	/**
	 * Helper function
	 * 
	 * @return DiskoProgressPanel
	 */
	private DiskoProgressPanel getProgressPanel() {
		// initialize?
		if(m_progressPanel==null) {
			m_progressPanel = getGlassPane().getProgressDialog().getProgressPanel();
		}
		return m_progressPanel;		
	}
	
  	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */
  	
	class DecisionWorker implements ActionListener {
		
		private long m_start = 0;
		private long m_millisToPopup = 0;
		private Timer m_timer = null;
		private boolean m_isCancelled = false;
		
		public DecisionWorker(long millisToPopup) {
			// save decision delay
			m_millisToPopup = millisToPopup;
			// create timer
			m_timer = new Timer(PAUSE_MILLIS, this);
		}

		public boolean start() {
			// is not running?
			if(!m_timer.isRunning()) {
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
			// hide progress
			setVisible(false);
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
			// has no progress?
			if(!m_isCancelled && !m_hasProgress && System.currentTimeMillis()- m_start > m_millisToPopup) {
				// stop timer
				m_timer.stop();
				// show progress
				setVisible(true);
				// forward event to listeners
				fireUpdateProgressEvent(DiskoProgressEventType.EVENT_START);
			}
		}			
	}
}
