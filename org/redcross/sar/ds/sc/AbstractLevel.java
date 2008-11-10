package org.redcross.sar.ds.sc;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

public abstract class AbstractLevel<S extends IData, T extends IData>
				extends AbstractClue<S> implements ILevel {

	public static String[] ATTRIBUTE_NAMES = new String[] {
		"time","in","level","out","range"
	};

	public static Class<?>[] ATTRIBUTE_CLASSES = new Class<?>[] {
		Calendar.class,Double.class,Integer.class,Double.class,Integer.class
	};

	protected int m_range;
	protected Calendar m_initTime;
	protected String m_unit;

	protected final List<T> m_count = new ArrayList<T>();
	protected final Collection<T> m_queue = new Vector<T>();
	protected final LinkedList<Sample<Integer[]>> m_changes = new LinkedList<Sample<Integer[]>>();

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractLevel(S id, String name, int range, String unit) {
		// forward
		super(id, name, ATTRIBUTE_NAMES, ATTRIBUTE_CLASSES);
		// prepare
		m_range = range;
		m_initTime = Calendar.getInstance();
		m_unit = unit;
	}

	/* ============================================================
	 * ILevel implementation
	 * ============================================================ */

	/**
	 * Get estimation time range. The range is the number events that is
	 * required to estimate the production rate.
	 */
	public int range() {
		return m_range;
	}

	/**
	 * Get level unit
	 */
	public String unit() {
		return m_unit;
	}

	/**
	 * Get estimated production input rate for given sample range.
	 *
	 * @return unit/min
	 */
	public double in() {
		long time = duration();
		return time>0 ? average(0)/time*60 : 0;
	}

	/**
	 * Get estimated production output rate for given sample range.
	 *
	 * @return unit/min
	 */
	public double out() {
		long time = duration();
		return time>0 ? average(2)/time*60 : 0;
	}

	/**
	 * Get current clue time
	 */
	public Calendar time() {
		return m_changes.size()> 0 ? m_changes.getLast().m_time : m_initTime;
	}

	/**
	 * Get number of assignments with status {@link AssignmentStatus#READY}
	 * at current <code>time</clue>
	 */
	public int level() {
		return m_changes.size()> 0 ? m_changes.getLast().m_data[1] : 0;
	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public void schedule(T data) {
		if(!m_queue.contains(data)) {
			m_isDirty |= m_queue.add(data);
		}
	}

	public void schedule(Collection<T> list) {
		for(T it : list) {
			if(!m_queue.contains(it)) {
				m_isDirty |= m_queue.add(it);
			}
		}
	}

	public int calculate() throws Exception {
		// any change in level?
		long[] steps = change(m_queue);
		// get info
		long inc = steps[0];
		long dec = steps[1];
		long tic = steps[2];
		// any change?
		if(inc!=0 || dec!=0) {
			// overflow?
			if(m_changes.size()==m_range) {
				// remove head
				m_changes.poll();
			}
			// get time
			Calendar t = time(tic);
			// add level and rates
			m_changes.add(new Sample<Integer[]>(t,new Integer[]{(int)inc,level() + (int)(inc-dec),(int)dec}));
			// add sample
			addSample();
		}
		// get size of work
		int size = m_queue.size();
		// finished
		m_queue.clear();
		// reset flag
		m_isDirty = false;
		// DEBUG: Print level and rates
		System.out.println("PRODUCTION = " + in() + ":" + level() + ":" + out());
		// finished
		return size;
	}

	/* ============================================================
	 * Protected methods
	 * ============================================================ */

	protected abstract long[] change(Collection<T> list);

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	protected Calendar time(long millis) {
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(millis);
		return t;
	}

	protected double average(int index) {

		// initialize
		double avg = 0;

		// loop over samples
		for(Sample<Integer[]> it : m_changes) {
			avg += it.m_data[index];
		}

		// finished
		return (avg / m_changes.size());

	}

	protected long duration() {

		// initialize
		long time = 0;

		// can calculate?
		if(m_changes.size()>0) {
			// calculate duration
			time = m_changes.getLast().m_time.getTimeInMillis() - m_changes.getFirst().m_time.getTimeInMillis();
		}
		// finished
		return time/1000;
	}


}
