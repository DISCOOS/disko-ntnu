package org.redcross.sar.work;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.DiskoProgressPanel;
import org.redcross.sar.gui.DiskoProgressPanel.ProgressStyleType;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ProgressDialog;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IProgressListener;
import org.redcross.sar.work.event.ProgressEvent;
import org.redcross.sar.work.event.ProgressEvent.ProgressEventType;

/**
 * Singleton progress monitor class. It decides if a progress dialog should
 * be presented or not and ensures that access to graphical Swing components
 * are accessed on the AWT (Event Dispatch Thread) thread. The first invocation
 * of getInstance() MUST be done from the AWT thread!
 *
 * @author kennetgu
 *
 */
public class ProgressMonitor {

	private static int PAUSE_MILLIS = 500;
	private static int POPUP_MILLIS = 150;

	private static ProgressMonitor m_this;

	private int m_min = 0;
	private int m_max = 0;
	private int m_progress = 0;
	private int m_inAction = 0;
	private boolean m_inhibit = true;
	private long m_millisToPopup = POPUP_MILLIS;
	private long m_millisToCancel = 0;			// no automatic cancel as default
	private List<String> m_notes = null;
	private boolean m_intermediate = false;
	private DecisionWorker m_worker = null;
	private boolean m_oldState = false;

	private Component m_focusOwner;
	private DiskoGlassPane m_glassPane;
	private ProgressDialog m_progressDialog;
	private DiskoProgressPanel m_progressPanel;

	private EventListenerList m_listeners;

	private ProgressMonitor() {
		// prepare
		m_notes = new ArrayList<String>(1);
		m_notes.add("Vent litt");
		m_listeners = new EventListenerList();
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
  	public static synchronized ProgressMonitor getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DiskoProgressMonitor can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new ProgressMonitor();
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
		m_intermediate = (min == max);
		// start progress?
		if(m_inAction == 0) {
			// set default value
			m_notes.set(0,note);
			// update timeout values
			m_millisToPopup = millisToPopup;
			m_millisToCancel = millisToCancel;
			// reset progress
			m_progress = 0;
			// start decision thread
			create();
			// add note?
			if(m_inAction>0) {
				m_notes.add(note);
			}
		}
		else {
			// increment
			m_inAction++;
			// set note
			m_notes.add(note);
			// update progress bar
			scheduleUpdate();
			// forward
			fireUpdateProgressEvent(ProgressEventType.EVENT_CHANGE);
		}

		// return count
		return m_inAction;
	}

	public synchronized int progress(String note, int step, boolean auto) {
		if(m_inAction>0) {
			// update
			m_notes.set(m_inAction,note);
			m_progress = step;
			// update progress bar
			scheduleUpdate();
			// forward
			fireUpdateProgressEvent(ProgressEventType.EVENT_CHANGE);
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
		if(m_inAction>0) {
			m_notes.remove(m_inAction);
			m_inAction--;
			update();
		}
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
		if(m_inAction>0) {
			m_notes.remove(m_inAction);
			m_inAction--;
			update();
		}
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
		return m_notes.get(m_inAction);
	}

	/**
	 * Specifies the additional note that is displayed along with the progress
	 * message.
	 *
	 * @param note
	 */
	public synchronized void setNote(String note){
		// update
		m_notes.set(m_inAction,note);
		// notify?
		if(m_inAction>0) {
			// update progress panel
			scheduleUpdate();
			// forward
			fireUpdateProgressEvent(ProgressEventType.EVENT_CHANGE);
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

	public synchronized void addProgressListener(IProgressListener listener) {
		m_listeners.add(IProgressListener.class, listener);
	}

	public synchronized void removeProgressListener(IProgressListener listener) {
		m_listeners.remove(IProgressListener.class, listener);
	}

	public synchronized boolean isInhibit() {
		return m_inhibit;
	}

	public synchronized boolean setInhibit(boolean inhibit) {
		boolean bFlag = m_inhibit;
		m_inhibit = inhibit;
		return bFlag;
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
		if(SwingUtilities.isEventDispatchThread())
		{
			update();
		}
		else
		{
			// valid
			SwingUtilities.invokeLater(new Runnable() {
				public void run() { update(); }
			});
		}
	}

	private void update() {
		// valid?
		if(m_inAction>0) {
			// prepare progress bar
			if(getIntermediate()) {
				getProgressPanel().setLimits(m_min, m_max, true);
			}
			else {
				getProgressPanel().setLimits(0, 0, false);
			}
			// get note
			String note = m_notes.get(Math.min(m_notes.size()-1,m_inAction));
			// update progress
			getProgressPanel().setProgress(m_progress,note,note);
		}
	}

	public void setProgressSnapTo(final JComponent snapTo) {
		if(SwingUtilities.isEventDispatchThread()) {
			getProgressDialog().setSnapToLocation(snapTo, DefaultDialog.POS_CENTER, 0, true, false);
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setProgressSnapTo(snapTo);
				}
			});
		}
	}

	public synchronized boolean isProgressVisible() {
		return getProgressDialog().isVisible();
	}

	public void refreshProgress() {
		if(SwingUtilities.isEventDispatchThread()) {
			if(isProgressVisible()) {
				getProgressPanel().paintImmediately(getProgressPanel().getBounds());
			}
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					refreshProgress();
				}
			});
		}
	}

	private synchronized void scheduleEventStart() {
		// forward event to listeners
		fireUpdateProgressEvent(ProgressEventType.EVENT_START);
	}

	private synchronized void scheduleEventShow() {
		// show progress
		scheduleSetVisible(true);
		// forward event to listeners
		fireUpdateProgressEvent(ProgressEventType.EVENT_SHOW);
	}

	private synchronized void scheduleEventHide() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(ProgressEventType.EVENT_HIDE);
	}

	private synchronized void scheduleEventCancel() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(ProgressEventType.EVENT_CANCEL);
		// reset position
		setProgressSnapTo(m_glassPane);
	}

	private synchronized void scheduleEventFinish() {
		// hide progress
		scheduleSetVisible(false);
		// forward event to listeners
		fireUpdateProgressEvent(ProgressEventType.EVENT_FINISH);
		// reset position
		setProgressSnapTo(Utils.getApp().getFrame().getLayeredPane());
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
		// any change?
		if(isVisible!=getProgressDialog().isVisible()) {
			// get current focus owner?
			if(isVisible) {
				m_focusOwner = KeyboardFocusManager
					.getCurrentKeyboardFocusManager()
					.getFocusOwner();
			}
			// forward
			getGlassPane().setVisible(isVisible);
			getProgressDialog().setVisible(isVisible);
			getProgressDialog().doLayout();
		}
		// return to previous focus owner?
		if(!isVisible && m_focusOwner!=null) {
			m_focusOwner.requestFocusInWindow();
		}
	}

	private void fireUpdateProgressEvent(final ProgressEventType type) {
		if(SwingUtilities.isEventDispatchThread()) {
			// get event
			fireProgressEvent(new ProgressEvent(this,type));
		}
		else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// get event
					fireProgressEvent(new ProgressEvent(this,type));
				}
			});
		}
	}

	private void fireProgressEvent(ProgressEvent e) {
		// notify listeners
		for(IProgressListener it : m_listeners.getListeners(IProgressListener.class)) {
			it.changeProgress(e);
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
	private ProgressDialog getProgressDialog() {
		// initialize?
		if(m_progressDialog==null) {
			m_progressDialog = new ProgressDialog(Utils.getApp().getFrame(),false,ProgressStyleType.BAR_STYLE);
			getGlassPane().setProgressDialog(m_progressDialog);
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
			m_progressPanel = getProgressDialog().getProgressPanel();
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
					ProgressMonitor.this.cancel();
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
