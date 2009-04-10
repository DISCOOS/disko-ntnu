package org.redcross.sar.math;

import java.util.Collection;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import org.redcross.sar.ds.advisor.event.IInputListener;

public abstract class AbstractInput<I,C extends Number> implements IInput<I,C> {

	protected final Collection<I> m_queue = new Vector<I>();
	protected final EventListenerList m_listeners = new EventListenerList();

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractInput() { /*NOP*/ }

	/* ============================================================
	 * IInput implementation
	 * ============================================================ */

	/**
	 * Get dirty state
	 */
	public boolean isDirty() {
		return m_queue.size()>0;
	}

	public void addInputListener(IInputListener listener) {
		m_listeners.add(IInputListener.class,listener);
	}

	public void removeInputListener(IInputListener listener) {
		m_listeners.remove(IInputListener.class,listener);
	}


	/* ============================================================
	 * Public methods
	 * ============================================================ */

	/**
	 * Schedule input data
	 */
	public void schedule(I data) {
		if(!m_queue.contains(data)) {
			m_queue.add(data);
			fireInputChanged();
		}
	}

	/**
	 * Schedule list of input data
	 */
	public void schedule(Collection<I> list) {
		int count = 0;
		for(I it : list) {
			if(!m_queue.contains(it)) {
				m_queue.add(it);
				count++;
			}
		}
		if(count>0) fireInputChanged();
	}

	/**
	 * Calculate change from scheduled list of input data. List of input will be cleared.
	 *
	 * @return Change object
	 */
	public abstract Change<C> collect();

	/**
	 * Calculate change from scheduled list of input data.
	 *
	 * @return Change object
	 */
	public abstract Change<C> peek();

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	protected void fireInputChanged() {
		ChangeEvent e = new ChangeEvent(this);
		IInputListener[] list = m_listeners.getListeners(IInputListener.class);
		for(IInputListener it : list) {
			it.onInputChanged(e);
		}
	}

}
