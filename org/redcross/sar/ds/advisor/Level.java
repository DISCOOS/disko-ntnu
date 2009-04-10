package org.redcross.sar.ds.advisor;

import java.util.Calendar;

import org.redcross.sar.data.IData;
import org.redcross.sar.math.IInput;
import org.redcross.sar.math.ILevel;

public class Level<S extends IData, T, D extends Number>
				extends AbstractCue<S> implements ILevel<T,D> {

	public static String[] ATTRIBUTE_NAMES = new String[] {
		"time","in","rin","level","out","rout","range"
	};

	public static Class<?>[] ATTRIBUTE_CLASSES = new Class<?>[] {
		Calendar.class,Integer.class,Double.class,
		Integer.class,Integer.class,Double.class,Integer.class
	};

	protected ILevel<T, D> m_level;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	@SuppressWarnings("unchecked")
	public Level(Class<D> dataClass, S id, String name, int range, String unit) {
		// forward
		super(id, name, ATTRIBUTE_NAMES, ATTRIBUTE_CLASSES);
		// create level object
		m_level = new org.redcross.sar.math.Level(dataClass,name,range,unit);
	}


	/* ============================================================
	 * IDsObject implementation
	 * ============================================================ */

	public boolean isDirty() {
		return m_level.getInput().isDirty();
	}

	public int calculate() throws Exception {

		// forward
		return m_level.calculate();

	}

	/* ============================================================
	 * ILevel implementation
	 * ============================================================ */

	/**
	 * Get level unit
	 */
	public String getUnit() {
		return m_level.getUnit();
	}

	/**
	 * Get input
	 *
	 * @return unit
	 */
	public D getIn() {
		return m_level.getIn();
	}

	/**
	 * Get input rate
	 *
	 * @return unit/min
	 */
	public Double getRin() {
		return m_level.getRin();
	}

	/**
	 * Get output
	 *
	 * @return unit
	 */
	public D getOut() {
		return  m_level.getOut();
	}

	/**
	 * Get output rate
	 *
	 * @return unit/min
	 */
	public Double getRout() {
		return  m_level.getRout();
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
		return m_level.getLevel();
	}

	/**
	 * Get input object
	 */
	public IInput<T, D> getInput() {
		return  m_level.getInput();
	}

	/**
	 * Set input object
	 */
	public void setInput(IInput<T, D> input) {
		m_level.setInput(input);
	}

}
