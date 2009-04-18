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
 * @param <I> - The Source Event Data type. See {@link ISourceListener}.
 */

public class SimpleDataSource<I> implements IDataSource<I> {

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
    
	public boolean isSupported(Class<?> dataClass) {
		// TODO Auto-generated method stub
		return false;
	}

    public Collection<?> getItems(Class<?> c) {
    	return null;
    }

    public void addSourceListener(ISourceListener<I> listener) {
		m_sourceListeners.add(ISourceListener.class,listener);

	}

	public void removeSourceListener(ISourceListener<I> listener) {
		m_sourceListeners.remove(ISourceListener.class,listener);
	}

    /* =================================================================================
     * Public methods
     * ================================================================================= */

	@SuppressWarnings("unchecked")
	public void fireSourceChanged(SourceEvent<I> e) {
		// notify listeners
		ISourceListener[] listeners = m_sourceListeners.getListeners(ISourceListener.class);
		// loop over all listeners
		for(int i=0; i<listeners.length; i++) {
			listeners[i].onSourceChanged(e);
		}
	}

}
