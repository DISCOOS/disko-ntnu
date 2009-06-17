package org.redcross.sar.gui.field;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.gui.event.FieldModelEvent;
import org.redcross.sar.gui.event.IFieldModelListener;

/**
 * This abstract class partially implements the 
 * IFieldModel interface. </p>
 *  
 * The class extender is required to implementing the 
 * <code>isBoundTo()</code>,
 * <code>getLocalValue()</code>,
 * <code>getRemoteValue()</code>,
 * <code>setLocalValue(V value)</code>, 
 * <code>translateOrigin()</code>, 
 * <code>translateState()</code>, and
 * <code>translateChangeCount()</code> methods. </p> 
 * 
 * @author kenneth
 *
 * @param <V> - Value data type
 * @param <I> - Item data type
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFieldModel<V> implements IFieldModel<V> {

	private static final long serialVersionUID = 1L;	

	protected static final Logger m_logger = Logger.getLogger(AbstractFieldModel.class);
	
	private final EventListenerList m_listeners = new EventListenerList();
	
	private int m_lastChangeCount = -1;
	private DataState m_lastState = DataState.NONE;
	private DataOrigin m_lastOrigin = DataOrigin.NONE;
	

	/* ==================================================================
	 *  Constructors
	 * ================================================================== */

	public AbstractFieldModel() { /*NOP*/ }

	/* ==================================================================
	 *  Public methods
	 * ================================================================== */
	
	@Override
	public DataOrigin getOrigin() {
		return translateOrigin();
	}

	@Override
	public boolean isOrigin(DataOrigin origin) {
		DataOrigin e = translateOrigin();
		return e!=null&&e.equals(origin);
	}

	@Override
	public DataState getState() {
		return translateState();
	}

	@Override
	public boolean isState(DataState state) {
		DataState e = translateState();
		return e!=null&&e.equals(state);
	}
	
	@Override
	public final boolean isChanged() {
		return(translateChangeCount()!=m_lastChangeCount);
	}
	
	@Override
	public boolean isChangedSince(int changeCount) {
		return(translateChangeCount()!=changeCount);
	}
	
	@Override
	public V getValue() {
		switch(getOrigin()) {
		case LOCAL:
		case CONFLICT:
			return getLocalValue();
		case REMOTE:
			return getRemoteValue();
		}
		// data origin is NOSOURCE
		return null;
	}
	
	@Override
	public abstract Object getSource();
	
	@Override
	public abstract boolean isBoundTo(Object source);
	
	@Override
	public abstract V getLocalValue();
	
	@Override
	public abstract void setLocalValue(V value);
	
	@Override
	public abstract V getRemoteValue();
	
	@Override
	public abstract boolean acceptLocalValue();

	@Override
	public abstract boolean acceptRemoteValue();

	@Override
	public void reset() {
		// reset internal counters and states
		m_lastChangeCount = -1;
		m_lastState = DataState.NONE;
		m_lastOrigin = DataOrigin.NONE;
		// parse source(s) into the model
		parse();
	}
	
	@Override
	public void parse() { 
		// forward
		int flags = updateState(translateState());
		flags += updateOrigin(translateOrigin());
		flags += updateChangeCount(translateChangeCount());
		fireFieldModelChanged(flags);
	}

	
	@Override
	public void addFieldModelListener(IFieldModelListener listener) {
		m_listeners.add(IFieldModelListener.class,listener);		
	}

	@Override
	public void removeFieldModelListener(IFieldModelListener listener) {
		m_listeners.remove(IFieldModelListener.class,listener);		
	}	
	
	/* ==================================================================
	 *  Protected methods
	 * ================================================================== */
	
	/**
	 * Get current change count value
	 * 
	 * @return int - current change count
	 */
	protected abstract int translateChangeCount();
	
	/**
	 * Translate anything to origin
	 *   
	 * @return - the data origin
	 */
	protected abstract DataState translateState();

	/**
	 * Translate anything to state
	 *   
	 * @return - the data state
	 */
	protected abstract DataOrigin translateOrigin();
	
	/**
	 * Notify listeners of given change
	 * 
	 * @param type - the change type
	 */
	protected void fireFieldModelChanged(int type) {
		if(type>0) {
			FieldModelEvent e = new FieldModelEvent(this,type);
			IFieldModelListener[] list = m_listeners.getListeners(IFieldModelListener.class);
			for(IFieldModelListener it : list) {
				it.onFieldModelChanged(e);
			}
		}
	}
	
    protected static boolean isEqual(Object v1, Object v2)
    {
        return v1!=null && (v1 == v2 || v1.equals(v2));
    }
	
	
	/* ==================================================================
	 *  Private methods
	 * ================================================================== */
	
	/**
	 * Change change count. Notifies listeners if changed
	 * 
	 * @param state - the new last STATE
	 * 
	 * @return Returns FieldModelEvent.EVENT_VALUE_CHANGED if changed, 0 otherwise.  
	 */
	private int updateChangeCount(int changeCount) {
		if(m_lastChangeCount != changeCount) {
			m_lastChangeCount = changeCount;
			return FieldModelEvent.EVENT_VALUE_CHANGED;
		}
		return 0;
	}
	
	/**
	 * Change STATE type. Notifies listeners if changed
	 * 
	 * @param state - the new last STATE
	 * @return Returns FieldModelEvent.EVENT_STATE_CHANGED if changed, 0 otherwise.  
	 */
	private int updateState(DataState state) {
		if(m_lastState != state) {
			m_lastState = state;
			return FieldModelEvent.EVENT_STATE_CHANGED;
		}
		return 0;
	}
	
	/**
	 * Change ORIGIN type. Notifies listeners if changed
	 * 
	 * @param state - the new last ORIGIN
	 * @return Returns FieldModelEvent.EVENT_ORIGIN_CHANGED if changed, 0 otherwise.  
	 */
	private int updateOrigin(DataOrigin origin) {
		if(m_lastOrigin != origin) {
			m_lastOrigin = origin;
			return FieldModelEvent.EVENT_ORIGIN_CHANGED;
		}
		return 0;
	}	
}
 