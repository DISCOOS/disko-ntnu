package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.data.IDataIf;

public abstract class AbstractDsObject implements IDsObjectIf {

	private final List<Object[]> m_samples = new ArrayList<Object[]>(1);						
	
	private IDataIf m_id;
	
	/* =============================================================
	 * Constructors
	 * ============================================================= */
	
	public AbstractDsObject(IDataIf id) {
		m_id = id;
	}
	
	/* =============================================================
	 * Public methods
	 * ============================================================= */
	
	public IDataIf getId() {
		return m_id;
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
		if(obj instanceof IDsObjectIf) {
			return ((IDsObjectIf)obj).getId().equals(getId());
		}
		return false;
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
