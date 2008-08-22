package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class AbstractDsObject implements IDsObjectIf {

	private Object m_id;
	
	/* =============================================================
	 * Constructors
	 * ============================================================= */
	
	public AbstractDsObject(Object id) {
		m_id = id;
	}
	
	/* =============================================================
	 * Public methods
	 * ============================================================= */
	
	public Object getId() {
		return m_id;
	}

	public Object getAttrValue(Object key) {
		String name = null;
		Object value = null;
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
		// finished
		return value;
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
	
}
