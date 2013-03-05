package org.redcross.sar.gui.field;

import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;

public class DefaultFieldModel<V> extends AbstractFieldModel<V> {

	private V m_localValue;
	private V m_remoteValue;

	private int m_changeCount;
	private DataState m_state = DataState.NONE;
	private DataOrigin m_origin = DataOrigin.NONE;
	
	/* ==================================================================
	 *  Public methods
	 * ================================================================== */
	
	@Override
	public Object getSource() {
		return null;
	}
	
	@Override
	public boolean isBoundTo(Object source) {
		return false;
	}

	@Override
	public void setOrigin(DataOrigin origin) {
		if(origin!=null && !m_origin.equals(origin))
		{
			m_origin = origin;
			incrementChangeCount();
		}
	}

	@Override
	public void setState(DataState state) {
		if(state!=null && !m_state.equals(state))
		{
			m_state = state;
			incrementChangeCount();
		}
	}
	
	@Override
	public V getValue() {
		switch(getOrigin())
		{
		case LOCAL:
		case CONFLICT:
			return getLocalValue();
		case REMOTE:
			return getRemoteValue();
		}
		// NONE or MIXED origins.
		return null;
	}
	
	@Override
	public boolean setValue(V value) {
		switch(getOrigin())
		{
		case LOCAL:
		case CONFLICT:
			return setLocalValue(value);
		case REMOTE:
			return setLocalValue(value);
		}
		// NONE or MIXED origins
		return false;
	}

	@Override
	public V getLocalValue() {
		return m_localValue;
	}

	@Override
	public boolean setLocalValue(V value) {
		if(!isEqual(m_localValue,value)) {
			m_localValue = value;
			m_state = !isState(DataState.CONFLICT) ? DataState.CHANGED : m_state;
			m_origin = !isOrigin(DataOrigin.CONFLICT) ? DataOrigin.LOCAL : m_origin;
			incrementChangeCount();
			return true;
		}
		return false;
	}

	@Override
	public V getRemoteValue() {
		return m_remoteValue;
	}

	@Override
	public boolean setRemoteValue(V value) {
		if(!isEqual(m_remoteValue,value)) {
			boolean isConflict = (m_localValue!=null && !isEqual(m_localValue, value));
			m_remoteValue = value;
			m_state = !isState(DataState.CONFLICT) ? (isConflict ? DataState.CONFLICT : DataState.LOOPBACK) : m_state;
			m_origin = !isOrigin(DataOrigin.CONFLICT) ? (isConflict ? DataOrigin.CONFLICT : DataOrigin.REMOTE) : m_origin;
			incrementChangeCount();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean commit() {
		if(isState(DataState.CHANGED)) {
			m_remoteValue = m_localValue;
			m_localValue = null;
			m_origin = DataOrigin.LOCAL;
			m_state = DataState.CHANGED;
			return true;
		}
		return false;
	}

	@Override
	public boolean rollback() {
		if(isState(DataState.CHANGED)) {
			m_remoteValue = m_localValue;
			m_localValue = null;
			m_origin = DataOrigin.LOCAL;
			m_state = DataState.CHANGED;
			incrementChangeCount();
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean acceptLocalValue() {
		if(isState(DataState.CONFLICT)) {
			m_origin = DataOrigin.LOCAL;
			m_state = DataState.CHANGED;
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptRemoteValue() {
		if(isState(DataState.CONFLICT)) {
			m_localValue = null;
			m_origin = DataOrigin.REMOTE;
			m_state = DataState.ROLLBACK;
			return true;
		}
		return false;
	}
	
	/* ==================================================================
	 *  Protected methods
	 * ================================================================== */
	
	@Override
	protected int translateChangeCount() {
		return m_changeCount;
	}

	@Override
	protected DataOrigin translateOrigin() {
		return m_origin;
	}

	@Override
	protected DataState translateState() {
		return m_state;
	}
	
	/* ==================================================================
	 *  Private methods
	 * ================================================================== */

	private void incrementChangeCount() {
		m_changeCount++;
	}

}
