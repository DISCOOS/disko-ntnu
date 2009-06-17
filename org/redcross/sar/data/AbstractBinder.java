package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.BinderEvent;
import org.redcross.sar.data.event.IBinderListener;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;

/**
 * 
 * @author Administrator
 *
 * @param <S> - the class or interface that implements the data id object
 * @param <T> - the class or interface that implements the data object
 * @param <D> - the class or interface that implements the source event data (information)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractBinder<S,T extends IData, D>
										 implements IDataBinder<S,T,D>, ISourceListener<D> {

	private static final long serialVersionUID = 1L;

	protected IDataSource<D> source;
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

	public boolean selectData(IData obj) {
		if(isDataObject(obj)) {
			return (selector!=null ? selector.select((T)obj) : true);
		}
		return false;
	}

	public boolean selectCoObject(IData obj) {
		if(isCoObject(obj)) {
			Selector selector = coSelectorList.get(obj);
			return (selector!=null ? selector.select(obj) : true);
		}
		return false;
	}

	/* =============================================================================
	 * IDataBinderIf implementation
	 * ============================================================================= */

	public abstract boolean clear();
	public abstract boolean load(Collection<T> objects, boolean append);

	public boolean load() {
		// has model?
		if(source!=null) {
			// forward
			return load(query(),false);
		}
		// failure
		return false;
	}
	
	public boolean load(Collection<T> objects) {
		return load(objects,false);
	}
	
	public boolean connect(IDataSource<D> source) {
		if(source!=null && this.source==null && source.isSupported(dataClass)) {
			this.source = source;
			this.source.addSourceListener(this);
			return true;
		}
		return false;
	}

	public boolean disconnect() {
		if(source!=null) {
			clear();
			source.removeSourceListener(this);
			source=null;
			return true;
		}
		return false;
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	public boolean isDataObject(IData obj) {
		if(obj!=null) {
			return (dataClass.isInstance(obj));
		}
		return false;
	}

	public IDataSource<D> getSource() {
		return source;
	}

	public boolean isSupported(Class<?> dataClass) {
		if(dataClass!=null) {
			return this.dataClass.isAssignableFrom(dataClass);
		}
		return false;
	}

	public boolean isCoObject(IData obj) {
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

	public void addBinderListener(IBinderListener<S> listener) {
		listeners.add(IBinderListener.class, listener);
	}

	public void removeBinderListener(IBinderListener<S> listener) {
		listeners.remove(IBinderListener.class, listener);
	}

	/* =============================================================================
	 * IDataSourceIf implementation
	 * ============================================================================= */

	public abstract void onSourceChanged(SourceEvent<D> e);

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	//protected abstract T[] getData(Collection<T> items);

	protected Collection<T> query() {
		if(source!=null && source.isAvailable())
			return (Collection<T>)getSource().getItems(getDataClass());
		else
			return null;
	}

	protected List<T> sort(Collection<T> objs) {
		List<T> list = new ArrayList(objs);
		Comparator<T> comparator = getComparator();
		if(comparator==null)
			Collections.sort(list, null);
		else
			Collections.sort(list, getComparator());
		return list;
	}

	protected void fireDataLoaded(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCreated(e);
		}
	}

	protected void fireDataCreated(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCreated(e);
		}
	}

	protected void fireDataChanged(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(e);
		}
	}

	protected void fireDataDeleted(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataDeleted(e);
		}
	}

	protected void fireDataUnselected(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataUnselected(e);
		}
	}

	protected void fireDataClearAll(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataClearAll(e);
		}
	}

	protected void fireCoDataChanged(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onCoDataChanged(e);
		}
	}

	protected void fireCoDataUnselected(BinderEvent<S> e) {
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onCoDataUnselected(e);
		}
	}



}
