package org.redcross.sar.app;

import javax.swing.event.EventListenerList;

import org.redcross.sar.app.event.IServiceListener;
import org.redcross.sar.app.event.ServiceEvent;
import org.redcross.sar.work.event.IWorkLoopListener;
import org.redcross.sar.work.event.WorkLoopEvent;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkLoop;
import org.redcross.sar.work.WorkPool;
import org.redcross.sar.work.IWorkLoop.LoopState;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractService implements IService {

	/**
	 * Operation id
	 */
	protected final String m_oprID;

	/**
	 * List of update listeners.
	 */
	protected final EventListenerList m_listeners = new EventListenerList();

	/**
	 * The work pool
	 */
	protected final WorkPool m_workPool;

	/**
	 * The work loop
	 */
	protected final WorkLoop m_workLoop;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractService(String oprID,
			int dutyCycle, int timeOut) throws Exception {

		// prepare
		m_oprID = oprID;
		m_workPool = WorkPool.getInstance();
		m_workLoop = new WorkLoop(dutyCycle,timeOut);
		m_workLoop.addWorkLoopListener(m_loopListener);

	}

	/* ============================================================
	 * IService implementation
	 * ============================================================ */

	public String getOprID() {
		return m_oprID;
	}

	public IWorkLoop getWorkLoop() {
		return m_workLoop;
	}

	public LoopState getLoopState() {
		return m_workLoop.getState();
	}

	public boolean isLoopState(LoopState state) {
		return m_workLoop.isState(state);
	}

	public boolean start() {

		// allowed?
		if(m_workLoop.getID()==0) {
			// add work loop to work pool
			return (m_workPool.add(m_workLoop)>0);
		}
		return false;
	}

	public boolean resume() {
		return m_workLoop.resume();
	}

	public boolean suspend() {
		return m_workLoop.suspend();
	}

	public boolean stop() {
		// initialize
		boolean bFlag = false;
		// allowed?
		if(m_workLoop.getID()>0) {
			// remove work loop from work pool
			bFlag = m_workPool.remove(m_workLoop.getID());
		}
		// finished
		return bFlag;
	}

	public void addServiceListener(IServiceListener listener) {
		m_listeners.add(IServiceListener.class,listener);
	}

	public void removeServiceListener(IServiceListener listener) {
		m_listeners.remove(IServiceListener.class,listener);
	}

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	private void fireExecuteEvent(ServiceEvent.Execute e) {
		IServiceListener[] list = m_listeners.getListeners(IServiceListener.class);
 		for(int i=0;i<list.length; i++) {
			list[i].handleExecuteEvent(e);
		}
	}

	/* ============================================================
	 * Anonymous classes
	 * ============================================================ */

	private IWorkLoopListener m_loopListener = new IWorkLoopListener() {

		@Override
		public void onLoopChange(WorkLoopEvent e) {
			if(e.isStateEvent()) {
				fireExecuteEvent(new ServiceEvent.Execute(AbstractService.this,0));
			}
		}

	};

}
