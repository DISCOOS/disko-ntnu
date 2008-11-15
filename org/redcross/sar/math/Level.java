package org.redcross.sar.math;

import java.util.Calendar;
import java.util.Collection;

import org.redcross.sar.data.IData;

public class Level<S extends IData, T, D extends Number> implements ILevel<T,D> {

	public static Class<?>[] ATTRIBUTE_CLASSES = new Class<?>[] {
		Calendar.class,Integer.class,Double.class,
		Integer.class,Integer.class,Double.class,Integer.class
	};

	protected IInput<T, D> m_input;
	protected Variable<D> m_level;
	protected Average<D> m_in;
	protected Average<D> m_out;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	@SuppressWarnings("unchecked")
	public Level(Class<D> dataClass, String name, int range, String unit) {
		// create variables
		m_level = (Variable<D>)Variable.createVariable(dataClass, name, unit);
		m_in = (Average<D>)Average.createVariable(dataClass, "in",unit+"/e",range);
		m_out = (Average<D>)Average.createVariable(dataClass, "out",unit+"/e",range);
	}

	/* ============================================================
	 * ILevel implementation
	 * ============================================================ */

	/**
	 * Get name
	 */
	public String getName() {
		return m_level.m_name;
	}

	/**
	 * Get unit
	 */
	public String getUnit() {
		return m_level.m_unit;
	}

	/**
	 * Get input
	 *
	 * @return unit
	 */
	public D getIn() {
		return m_in.getValue();
	}

	/**
	 * Get input rate
	 *
	 * @return unit/min
	 */
	public Double getRin() {
		return m_in.getRate()*60;
	}

	/**
	 * Get output
	 *
	 * @return unit
	 */
	public D getOut() {
		return m_out.getValue();
	}

	/**
	 * Get output rate
	 *
	 * @return unit/min
	 */
	public Double getRout() {
		return m_out.getRate()*60;
	}

	/**
	 * Get current time
	 */
	public Calendar getTime() {
		return m_level.getTime();
	}

	/**
	 * Get level at current <code>time</clue>
	 */
	public D getLevel() {
		return m_level.getValue();
	}

	public IInput<T, D> getInput() {
		return m_input;
	}

	public void setInput(IInput<T, D> input) {
		m_input = input;
	}

	public int calculate() throws Exception {

		// initialize
		D net = null;

		// any input change pending?
		if(m_input!=null && m_input.isDirty()) {

			// collect changes
			Change<D> change = m_input.collect();

			// found change?
			if(change!=null) {

				// update input and output
				m_in.set(change.get("in"));
				m_out.set(change.get("out"));

				// calculate net level change
				net = m_in.subtract(m_out);

				// add net change to current level
				m_level.set(new Sample<D>(m_level.add(net),change.getTime()));

				// DEBUG: Print level and rates
				System.out.println(m_level.m_name + " = " + getIn() + ":" + getLevel() + ":" + getOut());

			}

		}

		// finished
		return net!=null ? (Integer)net : 0;

	}

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	protected Calendar time(long millis) {
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(millis);
		return t;
	}

	protected Calendar max(Calendar t1, Calendar t2) {
		return t1.compareTo(t2)>=0 ? t1 : t2;
	}

	protected Calendar min(Calendar t1, Calendar t2) {
		return t1.compareTo(t2)<=0 ? t1 : t2;
	}

	protected long[] extreeme(Collection<Long> chain) {
		// initialize
		long min = Long.MAX_VALUE;
		long max = Long.MIN_VALUE;
		// get sum of time in chain
		for(Long it : chain) {
			// update extremal values
			min = Math.min(min, it);
			max = Math.max(max, it);
		}
		// finished
		return new long[]{min,max};
	}

}
