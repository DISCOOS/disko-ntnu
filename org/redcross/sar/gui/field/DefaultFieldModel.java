package org.redcross.sar.gui.field;

public class DefaultFieldModel<V> extends AbstractFieldModel<V> {

	private V m_localValue;
	private V m_remoteValue;

	private int m_changeCount;
	private DataState m_state = DataState.STATE_LOOPBACK;
	private DataOrigin m_origin = DataOrigin.ORIGIN_REMOTE;
	
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
	public V getLocalValue() {
		return m_localValue;
	}

	@Override
	public void setLocalValue(V value) {
		if(isEqual(m_localValue,value)) {
			m_localValue = value;
			m_state = !isState(DataState.STATE_CONFLICT) ? DataState.STATE_CHANGED : m_state;
			m_origin = !isOrigin(DataOrigin.ORIGIN_CONFLICT) ? DataOrigin.ORIGIN_LOCAL : m_origin;
			incrementChangeCount();
		}
	}

	@Override
	public V getRemoteValue() {
		return m_remoteValue;
	}

	public void setRemoteValue(V value) {
		if(isEqual(m_remoteValue,value)) {
			boolean isConflict = (m_localValue!=null && !isEqual(m_localValue, value));
			m_remoteValue = value;
			m_state = !isState(DataState.STATE_CONFLICT) ? (isConflict ? DataState.STATE_CONFLICT : DataState.STATE_LOOPBACK) : m_state;
			m_origin = !isOrigin(DataOrigin.ORIGIN_CONFLICT) ? (isConflict ? DataOrigin.ORIGIN_CONFLICT : DataOrigin.ORIGIN_REMOTE) : m_origin;
			incrementChangeCount();
		}
	}
	
	@Override
	public boolean commit() {
		if(isState(DataState.STATE_CHANGED)) {
			m_remoteValue = m_localValue;
			m_localValue = null;
			m_origin = DataOrigin.ORIGIN_LOCAL;
			m_state = DataState.STATE_CHANGED;
			return true;
		}
		return false;
	}

	@Override
	public boolean rollback() {
		if(isState(DataState.STATE_CHANGED)) {
			m_remoteValue = m_localValue;
			m_localValue = null;
			m_origin = DataOrigin.ORIGIN_LOCAL;
			m_state = DataState.STATE_CHANGED;
			incrementChangeCount();
			return true;
		}
		return false;
	}
	
	
	@Override
	public boolean acceptLocalValue() {
		if(isState(DataState.STATE_CONFLICT)) {
			m_origin = DataOrigin.ORIGIN_LOCAL;
			m_state = DataState.STATE_CHANGED;
			return true;
		}
		return false;
	}

	@Override
	public boolean acceptRemoteValue() {
		if(isState(DataState.STATE_CONFLICT)) {
			m_localValue = null;
			m_origin = DataOrigin.ORIGIN_REMOTE;
			m_state = DataState.STATE_ROLLBACK;
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
