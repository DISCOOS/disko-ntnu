package org.redcross.sar.data;

import java.util.Collection;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;

/**
 * Abstract class for implementation of the IDataSource interface. IDataSource objects
 * are the main data stores in DISKO. Examples are the IMsoModelIf and IDs model
 * implementations. </p>
 *
 * When connecting IDataSource objects with views (GUI), us a IDataBinder implementation
 * to map the subset in the view. This subset of data is also a model and is implemented using
 * a IDataModel interface. Several generic classes are available for rapid implementation of
 * views. For example, use a MsoBinder (IDataBinder) to connect the MSO model to a
 * MsoTableModel (IDataModel). MsoTableModel implements the TableModel, which enables the use
 * of a JTable. For DISKO look and feel, use DiskoTable which extends JTable. </p>
 *
 * This Model-View-Controller design pattern enables rapid integration of DISKO data with
 * Java Swing components. When other GUI data model are required, extend the AbstractDataModel. </p>
 *
 * @author kennetgu
 *
 * @param <I> - The Source Event Data type. See {@link ISourceListener}.
 */

public abstract class AbstractDataSource<I> implements IDataSource<I> {

    private final EventListenerList m_sourceListeners = new EventListenerList();

    /* =================================================================================
     * IDataSourceIf implementation
     * ================================================================================= */

    public abstract Object getID();
    
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
