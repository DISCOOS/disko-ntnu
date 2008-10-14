package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.IDataBinderIf;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.IDataListenerIf;
import org.redcross.sar.data.event.ISourceListenerIf;
import org.redcross.sar.data.event.SourceEvent;

@SuppressWarnings("unchecked")
public abstract class AbstractBinder<S,T extends IDataIf, I>
										 implements IDataBinderIf<S,T,I>, ISourceListenerIf<I> {

	private static final long serialVersionUID = 1L;

	protected IDataSourceIf<I> source;
	protected Class<T> dataClass;
	protected Selector<T> selector;
	protected Comparator<T> comparator;

	protected final EventListenerList listeners = new EventListenerList();
	protected final List<Class<?>> coClassList = new ArrayList<Class<?>>();
	protected final Map<Class<?>,Selector<?>> coSelectorList = new HashMap<Class<?>,Selector<?>>();

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractBinder(Class<T> c) {
		// prepare
		dataClass = c;
	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public boolean selectData(IDataIf obj) {
		if(dataClass.isInstance(obj)) {
			return (selector!=null ? selector.select((T)obj) : true);
		}
		return false;
	}

	public boolean selectCoObject(IDataIf obj) {
		if(isCoObject(obj)) {
			Selector selector = coSelectorList.get(obj);
			return (selector!=null ? selector.select(obj) : true);
		}
		return false;
	}

	/* =============================================================================
	 * IDataBinderIf implementation
	 * ============================================================================= */

	public abstract boolean load(Collection<T> objects);

	public boolean connect(IDataSourceIf<I> source) {
		if(source!=null && this.source==null && source.isSupported(dataClass)) {
			this.source = source;
			this.source.addSourceListener(this);
			return true;
		}
		return false;
	}

	public void disconnect() {
		if(source!=null) {
			source.removeSourceListener(this);
			source=null;
		}
	}

	public boolean load() {
		// has model?
		if(source!=null) {
			// forward
			return load(DataUtils.selectItemsInCollection(getSelector(), (Collection<T>)getSource().getItems(getDataClass())));
		}
		// failure
		return false;
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	public IDataSourceIf<I> getSource() {
		return source;
	}

	public boolean isSupported(Class<?> dataClass) {
		return this.dataClass.isAssignableFrom(dataClass);
	}

	public boolean isCoObject(IDataIf obj) {
		if(obj!=null) {
			return isCoClass(obj.getClass());
			}
		return false;
	}

	public void addCoClass(Class<?> c, Selector<?> selector) {
		if(c.equals(dataClass))
			throw new IllegalArgumentException("The data class " + dataClass.getName() + " is not a valid co-class");
		if(!coClassList.contains(c)) {
			coClassList.add(c);
			coSelectorList.put(c,selector);
		}
	}

	public void removeCoClass(Class<?> c) {
		if(coClassList.contains(c)) {
			coClassList.remove(c);
			coSelectorList.remove(c);
		}
	}

	public boolean isCoClass(Class<?> c) {
		for(Class<?> it : coClassList) {
			if(it.isAssignableFrom(c)) return true;
		}
		return false;
	}

	public Selector<T> getSelector() {
		return selector;
	}

	public void setSelector(Selector<T> selector) {
		this.selector = selector;
	}

	public Comparator<T> getComparator() {
		return comparator;
	}
	public void setComparator(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void addDataListener(IDataListenerIf<S> listener) {
		listeners.add(IDataListenerIf.class, listener);
	}

	public void removeDataListener(IDataListenerIf<S> listener) {
		listeners.remove(IDataListenerIf.class, listener);
	}

	/* =============================================================================
	 * IDataSourceIf implementation
	 * ============================================================================= */

	public abstract void onSourceChanged(SourceEvent<I> e);

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	protected List<T> sort(Collection<T> objs) {
		List<T> list = new ArrayList(objs);
		Collections.sort(list, getComparator());
		return list;
	}

	protected void fireDataLoaded(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCreated(e);
		}
	}

	protected void fireDataCreated(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCreated(e);
		}
	}

	protected void fireDataChanged(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(e);
		}
	}

	protected void fireDataDeleted(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataDeleted(e);
		}
	}

	protected void fireDataClearAll(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataClearAll(e);
		}
	}

	protected void fireDataCoClassChanged(DataEvent<S> e) {
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCoClassChanged(e);
		}
	}


}
