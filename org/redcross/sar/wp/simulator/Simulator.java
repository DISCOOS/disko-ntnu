package org.redcross.sar.wp.simulator;

import org.redcross.sar.thread.IWorkLoop;
import org.redcross.sar.thread.WorkLoop;
import org.redcross.sar.thread.WorkPool;
import org.redcross.sar.thread.IWorkLoop.LoopState;

public class Simulator  {

	private final WorkLoop m_workLoop;
	private final WorkPool m_workPool;

	public Simulator() throws Exception {
		// prepare
		m_workLoop = new WorkLoop(5000,1000);
		m_workPool = WorkPool.getInstance();
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
		// allowed?
		if(m_workLoop.getID()>0) {
			// remove work loop from work pool
			return m_workPool.remove(m_workLoop.getID());
		}
		return false;
	}

}
