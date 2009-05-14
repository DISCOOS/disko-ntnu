package org.redcross.sar.math;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoAttributeIf.IMsoEnumIf;
import org.redcross.sar.util.except.UnknownAttributeException;

public class MsoStatusInput<T extends IMsoObjectIf, E extends Enum<E>> extends AbstractInput<T, Integer> {

	protected final Enum<E> m_status;
	protected final String m_attrName;

	protected final String[] SAMPLE_NAMES = new String[] { "in", "out" };

	protected final List<T> m_count = new ArrayList<T>();

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public MsoStatusInput(String attrName, E status) {
		// forward
		super();
		// prepare
		m_status = status;
		m_attrName = attrName;
	}

	/* ============================================================
	 * IInput implementation
	 * ============================================================ */

	public Change<Integer> peek() {
		// initialize
		int inc = 0;
		int dec = 0;
		// initialize collection
		Collection<Long> chain = new Vector<Long>(m_queue.size());
		// loop over assignments
		for(T it : m_queue) {
			// get status attribute
			IMsoEnumIf<E> attr = getAttribute(it);
			// only use server values
			if(attr.isState(ModificationState.STATE_SERVER)) {
				// get status
				E status = attr.getValue();
				// get flags
				boolean exists = m_count.contains(it);
				boolean isReady = it.hasBeenDeleted() ? false : m_status.equals(status);
				// level increased?
				if(!exists && isReady) {
					// get time
					Calendar t = attr.getLastTime(status);
					// is time registered?
					if(t!=null) {
						// add to count
						m_count.add(it);
						// level incremented
						inc++;
						// get time in milliseconds
						long tic = t.getTimeInMillis();
						// add to time
						chain.add(tic);
					}
				}
				else if(exists && !isReady) {
					// get time
					Calendar t = attr.getLastTime(status);
					// is time registered?
					if(t!=null) {
						// remove assignment from level
						m_count.remove(it);
						// level decremented
						dec++;
						// get time in milliseconds
						long tic = t.getTimeInMillis();
						// add to time
						chain.add(tic);
					}
				}
			}
		}

		// finished
		return chain.size()>0 ? new Change<Integer>(SAMPLE_NAMES,new Integer[]{inc,dec},time(chain)) : null;

	}

	public Change<Integer> collect() {

		// forward
		Change<Integer> change = peek();

		// found change?
		if(change!=null) {

			// notify
			fireInputChanged();

		}

		// clear queue
		m_queue.clear();

		// finished
		return change;

	}
	/* ============================================================
	 * Protected methods
	 * ============================================================ */

	@SuppressWarnings("unchecked")
	protected IMsoEnumIf<E> getAttribute(IMsoObjectIf msoObj) {
		IMsoEnumIf<E> attr = null;
		try {
			attr = (IMsoEnumIf<E>)msoObj.getEnumAttribute(m_attrName);
		} catch (UnknownAttributeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		if(msoObj instanceof IUnitIf) {
			System.out.println();
		}
		*/
		return attr;

	}

	/**
	 * Calculate middle time
	 *
	 * @param chain - all time
	 * @return
	 */
	protected Calendar time(Collection<Long> chain) {
		// initialize
		long sum = 0;
		int size = chain.size();
		// calculate
		for(Long it : chain) {
			sum += it;
		}
		// create time in center of chain
		return size>0 ? time(sum/chain.size()) : null;
	}

	protected Calendar time(long millis) {
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(millis);
		return t;
	}

}
