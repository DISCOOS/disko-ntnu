package org.redcross.sar.ds.sc;

import org.redcross.sar.ds.AbstractDsObject;

public abstract class AbstractDecision extends AbstractDsObject implements IDecision {

	private boolean m_isDirty;

	private final String m_name;
	private final String[] m_attrNames;
	private final Class<?>[] m_attrClasses;

	/* =============================================================
	 * Constructors
	 * ============================================================= */

	public AbstractDecision(ICue id, String name, String[] attrNames, Class<?>[] attrClasses) {
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
	public ICue getId() {
		return (ICue)super.getId();
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

	public boolean isDirty() {
		return m_isDirty;
	}
}
