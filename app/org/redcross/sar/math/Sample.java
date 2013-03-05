package org.redcross.sar.math;

import java.util.Calendar;

public class Sample<D extends Number> implements IMath<D> {

	public final D m_data;
	public final Calendar m_time;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Sample(D data,Calendar time) {
		// prepare
		m_data = data;
		m_time = time;
	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public Calendar getTime() {
		return (Calendar)m_time.clone();
	}

	public D getData() {
		return m_data;
	}

	@SuppressWarnings("unchecked")
	public Class<D> getDataClass() {
		return m_data!=null ? (Class<D>)m_data.getClass() : null;
	}

    /* ============================================================
     * IMath implementation
     * ============================================================ */

    /**
     * Basic add operation.</p>
     *
     * @return D - The sum of this variable and specified variable values
     */
	@SuppressWarnings("unchecked")
	public D add(Object value) {
		// forward
		return (D)MathUtils.add(getDataClass(), this, value);
	}

    /**
     * Basic divide operation.</p>
     *
     * @return D - The fraction after dividing this variable with the specified variable value
     */
	@SuppressWarnings("unchecked")
	public D divide(Object value) {
		// forward
		return (D)MathUtils.divide(getDataClass(), this, value);
	}

    /**
     * Basic multiply operation.</p>
     *
     * @return D - The product of this variable and specified variable values
     */
	@SuppressWarnings("unchecked")
	public D multiply(Object value) {
		// forward
		return (D)MathUtils.multiply(getDataClass(), this, value);
	}

    /**
     * Basic subtract operation.</p>
     *
     * @return D - The subtraction of the specified variable value from this variable value
     */
	@SuppressWarnings("unchecked")
	public D subtract(Object value) {
		// forward
		return (D)MathUtils.subtract(getDataClass(), this, value);
	}

    /**
     * Modulus operation.</p>
     *
     * @return D - The modulus of the specified variable value on this variable value
     */
	@SuppressWarnings("unchecked")
	public D mod(Object value) {
		// forward
		return (D)MathUtils.mod(getDataClass(), this, value);
	}


}