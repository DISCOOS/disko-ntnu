package org.redcross.sar.ds.sc;

import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IAttributeIf.IMsoEnumIf;
import org.redcross.sar.util.except.UnknownAttributeException;

public class StatusLevel<S extends IData, T extends IMsoObjectIf, E extends Enum<E>> extends AbstractLevel<S, T> {

	protected final Enum<E> m_status;
	protected final String m_attrName;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public StatusLevel(S id, String name, int range, String unit, String attrName, E status) {
		// forward
		super(id,name,range,unit);
		// prepare
		m_status = status;
		m_attrName = attrName;
	}

	/* ============================================================
	 * Protected methods
	 * ============================================================ */

	protected long[] change(Collection<T> list) {
		// initialize
		long inc = 0;
		long dec = 0;
		// initialize collection
		Collection<Long> chain = new Vector<Long>(list.size());
		// loop over assignments
		for(T it : list) {
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
		// calculate
		long tic = time(chain);

		// finished
		return new long[]{inc,dec,tic};
	}

	@SuppressWarnings("unchecked")
	protected IMsoEnumIf<E> getAttribute(IMsoObjectIf msoObj) {
		IMsoEnumIf<E> attr = null;
		try {
			attr = (IMsoEnumIf<E>)msoObj.getEnumAttribute(m_attrName);
		} catch (UnknownAttributeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return attr;

	}

	protected long time(Collection<Long> chain) {
		// initialize
		long sum = 0;
		long min = Long.MAX_VALUE;
		// get sum of time in chain
		for(Long it : chain) {
			// update minimum
			min = Math.min(min, it);
			// add
			sum+=it;
		}
		// calculate average time in chain
		long avg = chain.size()>0 ? sum/chain.size() : 0;
		// finished
		return avg;
	}

}
