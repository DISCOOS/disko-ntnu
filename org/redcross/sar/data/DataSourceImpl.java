package org.redcross.sar.data;

import java.util.Collection;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.event.ISourceListenerIf;
import org.redcross.sar.data.event.SourceEvent;

public class DataSourceImpl<I> implements IDataSourceIf<I> {

    private final EventListenerList m_sourceListeners = new EventListenerList();

    /* =================================================================================
     * IDataSourceIf implementation
     * ================================================================================= */

	public boolean isSupported(Class<?> dataClass) {
		// TODO Auto-generated method stub
		return false;
	}

    public Collection<?> getItems(Class<?> c) {
    	return null;
    }

    public void addSourceListener(ISourceListenerIf<I> listener) {
		m_sourceListeners.add(ISourceListenerIf.class,listener);

	}

	public void removeSourceListener(ISourceListenerIf<I> listener) {
		m_sourceListeners.remove(ISourceListenerIf.class,listener);
	}

    /* =================================================================================
     * Public methods
     * ================================================================================= */

	@SuppressWarnings("unchecked")
	public void fireSourceChanged(SourceEvent<I> e) {
		// notify listeners
		ISourceListenerIf[] listeners = m_sourceListeners.getListeners(ISourceListenerIf.class);
		// loop over all listeners
		for(int i=0; i<listeners.length; i++) {
			listeners[i].onSourceChanged(e);
		}
	}

}
