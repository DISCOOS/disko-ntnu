package org.redcross.sar.math;

public class MathUtils {

    /**
     * Basic add operation.</p>
     *
     * @param Class numberClass - The number class to use
     * @param Object v1 - The first argument
     * @param Object v2 - The second argument
     *
     * @return Number - The sum <code>v1 + v2</code>
     */
	@SuppressWarnings("unchecked")
	public static Number add(Class<? extends Number> numberClass, Object v1, Object v2) {
		// initialize
		Number result = null;
		// get numbers
		Number n1 = init(numberClass,getNumber(v1));
		Number n2 = init(numberClass,getNumber(v2));
		// are both arguments supported?
		if(n1!=null && n2!=null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				result = (java.lang.Integer)n1 + (java.lang.Integer)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				result = (java.lang.Double)n1 + (java.lang.Double)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				result = (java.lang.Long)n1 + (java.lang.Long)n2;
			}
		}
		// finished
		return result;
	}

    /**
     * Basic divide operation.</p>
     *
     * @param Class numberClass - The number class to use
     * @param Object v1 - The first argument
     * @param Object v2 - The second argument
     *
     * @return Number - The fraction <code>v1 / v2</code>
     *
     */
	@SuppressWarnings("unchecked")
	public static Number divide(Class<? extends Number> numberClass, Object v1, Object v2) {
		// initialize
		Number result = null;
		// get numbers
		Number n1 = init(numberClass,getNumber(v1));
		Number n2 = init(numberClass,getNumber(v2));
		// are both arguments supported?
		if(n1!=null && n2!=null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				result = (java.lang.Integer)n1 / (java.lang.Integer)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				result = (java.lang.Double)n1 / (java.lang.Double)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				result = (java.lang.Long)n1 / (java.lang.Long)n2;
			}
		}
		// finished
		return result;
	}

    /**
     * Basic multiply operation.</p>
     *
     * @param Class numberClass - The number class to use
     * @param Object v1 - The first argument
     * @param Object v2 - The second argument
     *
     * @return Number - The product <code>v1 * v2</code>
     *
     */
	@SuppressWarnings("unchecked")
	public static Number multiply(Class<? extends Number> numberClass, Object v1, Object v2) {
		// initialize
		Number result = null;
		// get numbers
		Number n1 = init(numberClass,getNumber(v1));
		Number n2 = init(numberClass,getNumber(v2));
		// are both arguments supported?
		if(n1!=null && n2!=null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				result = (java.lang.Integer)n1 * (java.lang.Integer)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				result = (java.lang.Double)n1 * (java.lang.Double)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				result = (java.lang.Long)n1 * (java.lang.Long)n2;
			}
		}
		// finished
		return result;
	}

    /**
     * Basic subtract operation.</p>
     *
     * @param Class numberClass - The number class to use
     * @param Object v1 - The first argument
     * @param Object v2 - The second argument
     *
     * @return Number - The subtraction <code>v1 - v2</code>
     *
     */
	@SuppressWarnings("unchecked")
	public static Number subtract(Class<? extends Number> numberClass, Object v1, Object v2) {
		// initialize
		Number result = null;
		// get numbers
		Number n1 = init(numberClass,getNumber(v1));
		Number n2 = init(numberClass,getNumber(v2));
		// are both arguments supported?
		if(n1!=null && n2!=null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				result = (java.lang.Integer)n1 - (java.lang.Integer)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				result = (java.lang.Double)n1 - (java.lang.Double)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				result = (java.lang.Long)n1 - (java.lang.Long)n2;
			}
		}
		// finished
		return result;
	}

    /**
     * Modulus operation.</p>
     *
     * @param Class numberClass - The number class to use
     * @param Object v1 - The first argument
     * @param Object v2 - The second argument
     *
     * @return Number - The modulus <code>v1 % v2</code>
     *
     */
	@SuppressWarnings("unchecked")
	public static Number mod(Class<? extends Number> numberClass, Object v1, Object v2) {
		// initialize
		Number result = null;
		// get numbers
		Number n1 = init(numberClass,getNumber(v1));
		Number n2 = init(numberClass,getNumber(v2));
		// are both arguments supported?
		if(n1!=null && n2!=null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				result = (java.lang.Integer)n1 % (java.lang.Integer)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				result = (java.lang.Double)n1 % (java.lang.Double)n2;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				result = (java.lang.Long)n1 % (java.lang.Long)n2;
			}
		}
		// finished
		return result;
	}

    /**
     * Converts a object to <code>Number</code> if possible.</p>
     *
     * @param Object value - Object containing a number
     *
     * @return Number - A <code>Number</code> object if exists, <code>null</code> otherwise.
     */
    @SuppressWarnings("unchecked")
	public static Number getNumber(Object value) {
    	// translate
    	if(value instanceof Number) {
    		return (Number)value;
    	}
    	else if(value instanceof Sample) {
    		return ((Sample<Number>)value).getData();
    	}
    	else if(value instanceof IVariable) {
    		return ((IVariable<Number>)value).getValue();
    	}
    	// failed
    	return null;
    }

    public static Number init(Class<? extends Number> numberClass, Number number) {
    	if(number==null) {
			// calculate
			if(numberClass.isAssignableFrom(java.lang.Integer.class)) {
				return 0;
			}
			else if(numberClass.isAssignableFrom(java.lang.Double.class)) {
				return 0.0;
			}
			else if(numberClass.isAssignableFrom(java.lang.Long.class)) {
				return 0L;
			}
    	}
    	return number;
    }

}
