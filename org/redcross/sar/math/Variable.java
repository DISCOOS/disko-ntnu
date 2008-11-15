package org.redcross.sar.math;

import java.util.Calendar;

public abstract class Variable<D extends Number> implements IVariable<D>, IMath<D> {

    protected String m_name;
    protected String m_unit;
    protected Sample<D> m_sample;
    protected Class<D> m_dataClass;

    /* ============================================================
     * Constructors
     * ============================================================ */

    protected Variable(Class<D> dataClass, String name, String unit) {
        // prepare
        m_name = name;
        m_unit = unit;
        m_dataClass = dataClass;
    }

    /* ============================================================
     * IVariable implementation
     * ============================================================ */

    /**
     * Test if variable is empty
     */
    public boolean isEmpty() {
    	return (m_sample==null);
    }

    /**
     * Get variable name
     */
    public String getName() {
        return m_name;
    }

    /**
     * Get variable unit
     */
    public String getUnit() {
        return m_unit;
    }

    /**
     * Get sample time
     */
    public Calendar getTime() {
    	return m_sample!=null ? m_sample.m_time : null;
    }

    /**
     * set new time object. The value is not changed.
     *
     * @return Calendar - old time object
     */
    public Calendar setTime(Calendar time) {
    	Calendar old = null;
    	if(m_sample!=null) {
    		old = m_sample.m_time;
    		change(new Sample<D>(m_sample.m_data,time));
    	}
    	return old;
    }

    /**
     * Get value
     */
    public D getValue() {
    	return m_sample!=null ? m_sample.m_data : null;
    }

    /**
     * Set new data object. The time is not changed.
     *
     * @return D - old data object
     */
    public D setValue(D value) {
    	D old = null;
    	if(m_sample!=null) {
    		old = m_sample.m_data;
    		change(new Sample<D>(value,m_sample.m_time));
    	}
    	return old;
    }


    /**
     * Get sample
     *
     * @return D - value
     */
    public Sample<D> get() {
        return m_sample;
    }

    /**
     * Set sample
     *
     * @return D - old value
     */
    public Sample<D> set(Sample<D> sample) {
        Sample<D> old = m_sample;
        m_sample = sample;
        return old;
    }

    /**
     * Set sample
     *
     * @return D - old value
     */
    public Sample<D> set(D value, Calendar time) {
        return set(new Sample<D>(value,time));
    }

    /**
     * Set sample data class
     *
     * @return D - old value
     */
    public Class<D> getDataClass() {
        return m_dataClass;
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
		return (D)MathUtils.add(m_dataClass, this, value);
	}

    /**
     * Basic divide operation.</p>
     *
     * @return D - The fraction after dividing this variable with the specified variable value
     */
	@SuppressWarnings("unchecked")
	public D divide(Object value) {
		// forward
		return (D)MathUtils.divide(m_dataClass, this, value);
	}

    /**
     * Basic multiply operation.</p>
     *
     * @return D - The product of this variable and specified variable values
     */
	@SuppressWarnings("unchecked")
	public D multiply(Object value) {
		// forward
		return (D)MathUtils.multiply(m_dataClass, this, value);
	}

    /**
     * Basic subtract operation.</p>
     *
     * @return D - The subtraction of the specified variable value from this variable value
     */
	@SuppressWarnings("unchecked")
	public D subtract(Object value) {
		// forward
		return (D)MathUtils.subtract(m_dataClass, this, value);
	}

    /**
     * Modulus operation.</p>
     *
     * @return D - The modulus of the specified variable value on this variable value
     */
	@SuppressWarnings("unchecked")
	public D mod(Object value) {
		// forward
		return (D)MathUtils.mod(m_dataClass, this, value);
	}

    /* ============================================================
     * Static methods
     * ============================================================ */

    public static Variable<? extends Number> createVariable(Class<? extends Number> dataClass, String name, String unit) {
		// calculate average
		if(dataClass.isAssignableFrom(java.lang.Integer.class)) {
			return new Variable.Integer(name,unit);
		}
		else if(dataClass.isAssignableFrom(java.lang.Double.class)) {
			return new Variable.Double(name,unit);
		}
		else if(dataClass.isAssignableFrom(java.lang.Long.class)) {
			return new Variable.Long(name,unit);
		}
		// failed
		return null;
    }

    /* ============================================================
     * Protected methods
     * ============================================================ */

    /**
     * Called when sample values are changed
     */
    protected Sample<D> change(Sample<D> sample) {
    	Sample<D> old = m_sample;
    	m_sample = sample;
    	return old;
    }

    /* ============================================================
     * Inner classes
     * ============================================================ */

    public static class Integer extends Variable<java.lang.Integer> {

        public Integer(String name, String unit) {
            super(java.lang.Integer.class, name, unit);
        }

    }

    public static class Long extends Variable<java.lang.Long> {

        public Long(String name, String unit) {
            super(java.lang.Long.class, name, unit);
        }

    }

    public static class Double extends Variable<java.lang.Double> {

        public Double(String name, String unit) {
            super(java.lang.Double.class, name, unit);
        }

    }


}
