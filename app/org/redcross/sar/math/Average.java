package org.redcross.sar.math;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Average<D extends Number> extends Variable<D> implements IAverage<D> {

	protected int m_maxSize;

    protected final Class<D> m_dataClass;
	protected final LinkedList<Sample<D>> m_samples = new LinkedList<Sample<D>>();

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Average(Class<D> dataClass, String name, String unit) {
		// prepare
		this(dataClass,name,unit,2);
	}

	public Average(Class<D> dataClass, String name, String unit, int maxSize) {
		// forward
		super(dataClass, name, unit);
		// prepare
		m_maxSize = Math.max(2, maxSize);
        m_dataClass = dataClass;
	}

	/* ============================================================
	 * IAverage implementation
	 * ============================================================ */

	public int getMaxSize() {
		return m_maxSize;
	}

	public void setMaxSize(int size) {
		// limit
		m_maxSize = Math.max(2, size);
		// force maximum size on queue
		while(m_samples.size()>m_maxSize) {
			m_samples.poll();
		}
	}

	public void clear() {
		m_samples.clear();
	}

	public boolean sample(IVariable<D> var) {
		// change?
		if(var!=null && !var.isEmpty()) {
			m_samples.add(var.get());
			if(m_samples.size()>m_maxSize) m_samples.poll();
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public D calculate() {
		// get size of samples
		int size = m_samples.size();
		// can calculate average?
		if(size>0) {
			// calculate average
			if(m_dataClass.isAssignableFrom(java.lang.Integer.class)) {
				int sum = 0;
				for(Sample<D> it : m_samples) {
					sum += (java.lang.Integer)it.m_data;
				}
				return (D)new java.lang.Integer(sum/size);
			}
			else if(m_dataClass.isAssignableFrom(java.lang.Double.class)) {
				double sum = 0;
				for(Sample<D> it : m_samples) {
					sum += (java.lang.Double)it.m_data;
				}
				return (D)new java.lang.Double(sum/size);
			}
			else if(m_dataClass.isAssignableFrom(java.lang.Long.class)) {
				long sum = 0;
				for(Sample<D> it : m_samples) {
					sum += (java.lang.Long)it.m_data;
				}
				return (D)new java.lang.Long(sum/size);
			}
		}
		// failed
		return null;
	}

	public int size() {
		return m_samples.size();
	}

	public Calendar getTime(int sample) {
		return (Calendar)m_samples.get(sample).m_time.clone();
	}

	public D getValue(int sample) {
		return m_samples.get(sample).m_data;
	}

	public Queue<Sample<D>> getSamples() {
		return new LinkedList<Sample<D>>(m_samples);
	}

	public long getDuration() {
		if(m_samples.size()>0) {
			Calendar[] time = extreeme();
			return getDuration(time[0],time[1]);
		}
		return 0L;
	}

	public long getDuration(Calendar t) {
		if(m_samples.size()>0) {
			Calendar[] time = extreeme();
			return getDuration(time[0],t);
		}
		return 0L;
	}

	public double getRate() {
		double time = getDuration();
		D value = calculate();
		return time>0 && value!=null ? value.doubleValue()/time: 0;
	}

	/* ============================================================
	 * IVariable implementation
	 * ============================================================ */

	@Override
	public Sample<D> set(Sample<D> sample) {
		// forward
		Sample<D> old = super.set(sample);
		// add to samples
		sample(this);
		// finished
		return old;
	}

	/* ============================================================
	 * Helper methods
	 * ============================================================ */
    /**
     * Called when sample values are changed
     */
    protected Sample<D> change(Sample<D> sample) {
    	// forward
    	Sample<D> old = super.change(sample);
    	// update samples
    	int index = m_samples.indexOf(old);
    	if(index!=-1){
    		m_samples.remove(index);
    		m_samples.add(index,sample);
    	}
    	// finished
    	return old;
    }

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	protected static Calendar max(Calendar t1, Calendar t2) {

		if(t1==null && t2==null)
			return null;
		else if(t1==null && t2!=null)
			return t2;
		else if(t1!=null && t2==null)
			return t1;

		return t1.compareTo(t2)>=0 ? t1 : t2;

	}

	protected static Calendar min(Calendar t1, Calendar t2) {

		if(t1==null && t2==null)
			return null;
		else if(t1==null && t2!=null)
			return t2;
		else if(t1!=null && t2==null)
			return t1;

		return t1.compareTo(t2)<=0 ? t1 : t2;

	}

	protected Calendar[] extreeme() {
		// initialize
		Calendar min = null;
		Calendar max = null;
		// get sum of time in chain
		for(Sample<D> it : m_samples) {
			// update extremal values
			min = min(min, it.m_time);
			max = max(max, it.m_time);
		}
		// finished
		return new Calendar[]{min,max};
	}

	public long getDuration(Calendar t1, Calendar t2) {
		return t1!=null && t2!=null ?
				t2.getTimeInMillis() - t1.getTimeInMillis() : 0;
	}

    /* ============================================================
     * Static methods
     * ============================================================ */

    public static Average<? extends Number> createVariable(Class<? extends Number> dataClass, String name, String unit) {
    	return createVariable(dataClass, name, unit, 2);
    }

    public static Average<? extends Number> createVariable(Class<? extends Number> dataClass, String name, String unit, int maxSize) {
		// calculate average
		if(dataClass.isAssignableFrom(java.lang.Integer.class)) {
			return new Average.Integer(name,unit,maxSize);
		}
		else if(dataClass.isAssignableFrom(java.lang.Double.class)) {
			return new Average.Double(name,unit,maxSize);
		}
		else if(dataClass.isAssignableFrom(java.lang.Long.class)) {
			return new Average.Long(name,unit,maxSize);
		}
		// failed
		return null;
    }

    /* ============================================================
     * Inner classes
     * ============================================================ */

    public static class Integer extends Average<java.lang.Integer> {

        public Integer(String name, String unit) {
            super(java.lang.Integer.class, name, unit);
        }

        public Integer(String name, String unit, int maxSize) {
            super(java.lang.Integer.class, name, unit, maxSize);
        }

    }

    public static class Long extends Average<java.lang.Long> {

        public Long(String name, String unit) {
            super(java.lang.Long.class, name, unit);
        }

        public Long(String name, String unit, int maxSize) {
            super(java.lang.Long.class, name, unit, maxSize);
        }

    }

    public static class Double extends Average<java.lang.Double> {

        public Double(String name, String unit) {
            super(java.lang.Double.class, name, unit);
        }

        public Double(String name, String unit, int maxSize) {
            super(java.lang.Double.class, name, unit, maxSize);
        }

    }


}
