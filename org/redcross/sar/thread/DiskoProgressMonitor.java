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
	private static int POPUP_MILLIS = 150;
	
	private static DiskoProgressMonitor m_this;
	
	private int m_min = 0;
	private int m_max = 0;
	private int m_progress = 0;
	private int m_inAction = 0;
	private boolean m_inhibit = true;
	private long m_millisToPopup = POPUP_MILLIS;
	private long m_millisToCancel = 0;			// no automatic cancel as default
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
		return start(note,min,max,0,POPUP_MILLIS,0);
	}
	
	public synchronized int start(String note, int min, int max, int progress, int millisToPopup, int millisToCancel) {
		// initialize
		m_min = 0;
		m_max = 0;
		m_progress = progress; 
		m_note = note;
		m_intermediate = (min == max);
		m_hasProgress = false;
		// start progress?
		if(m_inAction == 0) {
			// update timeout values
			m_millisToPopup = millisToPopup;
			m_millisToCancel = millisToCancel;
			// reset progress
			m_progress = 0;
			// start decision thread
			create();
		}
		else {
			// increment
			m_inAction++;
			// update progress bar
			scheduleUpdate();
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_UPDATE);			
		}
		// return count
		return m_inAction;
	}
	
	public synchronized int progress(String note, int step, boolean auto) {
		if(m_inAction>0) {		
			// update
			m_note = note;
			m_progress = step;
			m_hasProgress = true;
			// update progress bar
			scheduleUpdate();
			// forward
			fireUpdateProgressEvent(DiskoProgressEventType.EVENT_UPDATE);
		}
		else if (auto){
			start(note);
		}
		return m_inAction;
	}
	
	public synchronized int finish() {
		return(finish(false));
	}
	
	public synchronized int finish(boolean force) {		
		// decrement
		if(m_inAction>0)
			m_inAction--;		
		// finished?
		if(m_inAction==0 || force) {		
			// forward
			destroy();
			// forward
			scheduleEventFinish();
		}
		// return state
		return m_inAction;
	}
	
	public synchronized int cancel() {
		return(cancel(false));
	}
	
	public synchronized int cancel(boolean force) {		
		// decrement
		if(m_inAction>0)
			m_inAction--;
		// finished?
		if(m_inAction==0 || force) {		
			// forward
			destroy();
			// forward
			scheduleEventCancel();
		}
		// return state
		return m_inAction;
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
	 * Returns the amount of time before a start is cancelled if no progress is received
	 */
	public synchronized long getMillisToCancel() {
		return m_millisToCancel;
	}

	/**
	 * Specifies the amount of time before a start is cancelled if no progress is received
	 * 
	 * @param millisToCancel
	 */
	public synchronized void setMillisToCancel(long millisToCancel){
		m_millisToCancel = millisToCancel;
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
		if(m_inAction>0) {
			// update progress panel
			scheduleUpdate();			
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
		return (m_inAction>0);
	}

	/**
	 * Returns count of actions
	 * 
	 */
	public synchronized int isInActionCount() {
		// return count
		return m_inAction;
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
		m_oldState = isProgressVisible();
		if(m_oldState) scheduleEventHide();
	}
	
	public synchronized boolean showAgain() {
		// show?
		if(m_oldState && !m_inhibit && !isProgressVisible()) {
			scheduleSetVisible(true);
			return true;
		}
		return false;
	}
	
  	/*========================================================
  	 * Private methods
  	 *========================================================
  	 */
	
	private synchronized void create() {
		try {				
			// create decision?
			if(m_worker==null) {
				// reset flag
				m_hasProgress = false;
				// is in action
				m_inAction = 1;
				// notify
				scheduleEventStart();
				// forward?
				if(m_millisToPopup==0) scheduleEventShow();					
				// start worker?
				if(m_millisToPopup>0 || m_millisToCancel>0) {
					// create decision worker
					m_worker = new DecisionWorker(m_millisToPopup);
					// execute after millisToPopup
					m_worker.start();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private synchronized void destroy() {
		// not in action any more
		m_inAction = 0;
		// destroy?
		if(m_worker!=null) {
			// cancel thread
			m_worker.cancel();
			// reset pointer  
			m_worker = null;
		}
	}
	
	private void scheduleUpdate() {
		if(SwingUtilities.isEventDispatchThread()) { update(); }
		else {
			// valid
			SwingUtilities.invokeLater(new Runnable() {
				public void run() { update(); }
			});
		}
	}
	
	private void update() {
		// valid?
		if(m_inAction>0) {
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
	}
	
	public void setProgressLocationAt(final JComponent c) { 
		if(SwingUtilities.isEventDispatchThread()) {
			getGlassPane().setProgressLocationAt(c);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getGlassPane().setProgressLocationAt(c);
				}
			});
		}
	}

	public synchronized boolean isProgressVisible() {
		return getProgressDialog().isVisible();
	}
	
	private synchronized void scheduleEventStart() {
		// forward event to listeners
		fireUpdateProgressEvent(DiskoProgressEventType.EVENT_START);							
	}
		
	private synchronized void scheduleEventShow() {
		// show progress
		scheduleSetVisible(true);
		// forward event to listeners
		fireUpdateProgressEvent(DiskoProgressEventType.EVENT_SHOW);							
	}
	
	private synchronized void scheduleEventHide() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(DiskoProgressEventType.EVENT_HIDE);							
	}
	
	private synchronized void scheduleEventCancel() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(DiskoProgressEventType.EVENT_CANCEL);		
		// reset position
		setProgressLocationAt(null);
	}
	
	private synchronized void scheduleEventFinish() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(DiskoProgressEventType.EVENT_FINISH);							
		// reset position
		setProgressLocationAt(null);
	}
	
	
	private synchronized void scheduleSetVisible(final boolean isVisible) {
		if(SwingUtilities.isEventDispatchThread()) {
			setVisible(isVisible);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setVisible(isVisible);
				}
			});
		}
	}
	
	private void setVisible(boolean isVisible) {
		// prepare?
		if(isVisible) scheduleUpdate();
		// forward
		getGlassPane().setVisible(isVisible);
		getProgressDialog().setVisible(isVisible);
		getProgressDialog().doLayout();
	}
	
	private void fireUpdateProgressEvent(final DiskoProgressEventType type) {
		if(SwingUtilities.isEventDispatchThread()) {
			// get event
			fireProgressEvent(new DiskoProgressEvent(this,type));
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// get event
					fireProgressEvent(new DiskoProgressEvent(this,type));
				}
			});
		}
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
				// allowed
				return true;
			}
			// disallowed!
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
			if(!m_isCancelled) {
				// show progress?
				if(m_millisToPopup>0 && isTimedOut(m_millisToPopup)) {
					// stop timer?
					if(m_millisToCancel==0) m_timer.stop();
					// forward
					scheduleEventShow();
				}
				if(m_millisToCancel>0 && isTimedOut(m_millisToCancel)) {
					// stop this (cancel override popup).
					m_timer.stop();
					// forward
					DiskoProgressMonitor.this.cancel();
				}
			}
			else {
				// was cancelled
				m_timer.stop();
			}
		}	
		
		private boolean isTimedOut(long millis) {
			return System.currentTimeMillis() - m_start > millis;
		}
	}
}
