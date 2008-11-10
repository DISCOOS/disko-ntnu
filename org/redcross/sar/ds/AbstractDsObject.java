package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.data.IData;

public abstract class AbstractDsObject implements IDsObject {

	private final List<Object[]> m_samples = new ArrayList<Object[]>(1);

	private IData m_id;

	protected boolean m_isArchived = false;					// if true, estimate() returns previous calculated ete() value
	protected boolean m_isSuspended = false;				// if true, estimate() returns previous calculated ete() value


	/* =============================================================
	 * Constructors
	 * ============================================================= */

	public AbstractDsObject(IData id) {
		m_id = id;
	}

	/* =============================================================
	 * IDsObject implementation
	 * ============================================================= */

	public IData getId() {
		return m_id;
	}

	/**
	 * If true, estimation is finished
	 */
	public boolean isArchived() {
		return m_isArchived;
	}

	/**
	 * If true, estimation is suspended
	 */
	public boolean isSuspended() {
		return m_isSuspended;
	}

	/**
	 * Archive result
	 */
	public void archive() {
		addSample();
		m_isArchived = true;
	}

	/**
	 * sets isSuspended() true. Only possible if not archived
	 */
	public void suspend() {
		m_isSuspended = !m_isArchived;
	}

	/**
	 * sets isSuspended() false
	 */
	public void resume() {
		m_isSuspended = false;
	}

	public Object getAttrValue(Object key) {
		return getAttrValue(key,-1);
	}

	public Object getAttrValue(Object key, int sample) {
		Object value = null;
		// current value?
		if(sample==-1) {
			String name = null;
			if(key instanceof Integer) {
				name = getAttrName((Integer)key);
			}
			else if (key instanceof String) {
				name = (String) key;
			}
			if(name!=null) {
				Class<?> c = this.getClass();
				try {
					Method method =  c.getMethod(name);
					value = method.invoke(this);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			int index = -1;
			if(key instanceof Integer) {
				index = (Integer)key;
			}
			else if (key instanceof String) {
				index = getAttrIndex((String)key);
			}
			// found?
			if(index!=-1) {
				value = m_samples.get(sample)[index];
			}
		}
		// finished
		return value;
	}

	public int getSampleCount() {
		return m_samples.size();
	}

	public Object[][] samples() {
		Object[][] samples = new Object[m_samples.size()][getAttrCount()];
		return m_samples.toArray(samples);
	}

	public void load(Object[][] samples) {
		m_samples.clear();
		for(int i=0;i<samples.length;i++)
			m_samples.add(samples[i]);
	}

	@Override
	public boolean equals(Object obj) {
		if(super.equals(obj)) return true;
		if(obj instanceof IDsObject) {
			return ((IDsObject)obj).getId().equals(getId());
		}
		return false;
	}

	/* =============================================================
	 * Comparable implementation
	 * ============================================================= */

	public int compareTo(IData o) {
		// default implementation
		return this.hashCode() - o.hashCode();
	}

	/* =============================================================
	 * Public abstract methods
	 * ============================================================= */

	public abstract int getAttrCount();

	public abstract String getAttrName(int index);

	public abstract int getAttrIndex(String name);

	public abstract Class<?> getAttrClass(int index);

	/* =============================================================
	 * Helper methods
	 * ============================================================= */

	protected void addSample() {

		// get attribute count
		int count = getAttrCount();

		// allocate memory
		Object[] sample = new Object[count];

		// loop over all parameters and
		for(int i=0;i<count;i++)
			sample[i] = getAttrValue(i);

		// add to samples
		m_samples.add(sample);

	}

	protected void clearSamples() {
		m_samples.clear();
	}

}
