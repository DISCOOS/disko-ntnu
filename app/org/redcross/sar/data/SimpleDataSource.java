package org.redcross.sar.data;

import java.util.Collection;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;

/**
 * Simple implementation of IDataSource</p>
 *
 * @author kennetgu
 *
 * @param <D> - The Source Event Data type. See {@link ISourceListener}.
 */

public class SimpleDataSource<D> implements IDataSource<D> {

    private final Object m_id;
    
    private final EventListenerList m_sourceListeners = new EventListenerList();

    /* =================================================================================
     * Constructors
     * ================================================================================= */
    
    public SimpleDataSource(Object id) {
    	// prepare
    	m_id = id;
    }
    
    /* =================================================================================
     * IDataSourceIf implementation
     * ================================================================================= */

    public Object getID() {
    	return m_id;
    }
    
    public boolean isAvailable() {
    	return true;
    }
    
	public boolean isSupported(Class<?> dataClass) {
		// TODO Auto-generated method stub
		return false;
	}

    public Collection<?> getItems(Class<?> c) {
    	return null;
    }
    
    public Collection<?> getItems(Enum<?> e) {
    	return null;
    }

    public void addSourceListener(ISourceListener<D> listener) {
		m_sourceListeners.add(ISourceListener.class,listener);

	}

	public void removeSourceListener(ISourceListener<D> listener) {
		m_sourceListeners.remove(ISourceListener.class,listener);
	}

    /* =================================================================================
     * Public methods
     * ================================================================================= */

	@SuppressWarnings("unchecked")
	public void fireSourceChanged(SourceEvent<D> e) {
		// notify listeners
		ISourceListener[] listeners = m_sourceListeners.getListeners(ISourceListener.class);
		// loop over all listeners
		for(int i=0; i<listeners.length; i++) {
			listeners[i].onSourceChanged(e);
		}
	}

}
