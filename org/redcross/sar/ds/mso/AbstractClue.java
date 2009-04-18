package org.redcross.sar.ds.mso;

import org.redcross.sar.data.IData;
import org.redcross.sar.ds.AbstractDsObject;
import org.redcross.sar.util.Utils;

public abstract class AbstractClue<S extends IData> extends AbstractDsObject implements ICue {

	protected final String m_name;
	protected final String[] m_attrNames;
	protected final Class<?>[] m_attrClasses;

	/* =============================================================
	 * Constructors
	 * ============================================================= */

	public AbstractClue(IData id, String name, String[] attrNames, Class<?>[] attrClasses) {
		// forward
		super(id);
		// prepare
		m_name = name;
		m_attrNames = attrNames;
		m_attrClasses = attrClasses;
	}

	/* =============================================================
	 * IDsObject implementation
	 * ============================================================= */

	@Override
	@SuppressWarnings("unchecked")
	public S getId() {
		return (S)super.getId();
	}

	public String getName() {
		return m_name;
	}

	public String getAttrName(int index) {
		return m_attrNames[index];
	}

	public int getAttrIndex(String name) {
		int count = m_attrNames.length;
		for(int i=0;i<count;i++) {
			if(m_attrNames[i].equals(name))
				return i;
		}
		return -1;
	}

	public int getAttrCount() {
		return m_attrNames.length;
	}

	public Class<?> getAttrClass(int index) {
		return m_attrClasses[index];
	}

	/* =============================================================
	 * Required methods
	 * ============================================================= */

	protected String getMethodName(String attrName) {
		return "get"+Utils.capitalize(attrName);
	}

}
