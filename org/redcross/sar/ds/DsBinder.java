package org.redcross.sar.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redcross.sar.data.AbstractBinder;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.BinderEvent;
import org.redcross.sar.data.event.IBinderListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
import org.redcross.sar.ds.event.DsEvent.Update;

@SuppressWarnings("unchecked")
public class DsBinder<S extends IData, T extends IDsObject> extends AbstractBinder<S, T, DsEvent.Update> {

	private static final long serialVersionUID = 1L;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public DsBinder(Class<T> c) {
		// forward
		super(c);
	}

	/* =============================================================================
	 * IDataBinderIf implementation
	 * ============================================================================= */

	@Override
	public IDs<T> getSource() {
		return (IDs<T>)source;
	}

	public boolean connect(IDs<T> source) {
		return super.connect(source);
	}

	@Override
	public void addCoClass(Class<?> c, Selector<?> selector) {
		if(!IDsObject.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Only data a class of type " + IDsObject.class.getName() + " is a valid co-class");
		super.addCoClass(c,selector);
	}

	public boolean load(Collection<T> objects, boolean append) {
		// forward?
		if(!append) clear();
		// allowed?
		if(source!=null) {
			// sort objects
			List<T> list = sort(objects);
			// get DS objects
			IDsObject[] objs = new IDsObject[list.size()];
			list.toArray(objs);
			// select objects
			Object[] found = select(objs);
			// get data
			T[] data = (T[])found[0];
	        // any data selected?
	        if(data.length>0) {
	        	fireDataCreated(data, 0);
	        }
	        // any co-data selected?
	        if(data.length>0) {
	        	fireCoDataChanged(data, 0);
	        }
	        // success
	        return true;
		}
		// failed
		return false;
	}

	public boolean clear() {
		// query source for current values
		Collection<T> items = query();
		// allowed?
		if(items!=null) {
			// notify
			fireDataClearAll(getData(items), 0);
	        // success
	        return true;
		}
		// failed
		return false;
	}

	/* =============================================================================
	 * IDsUpdateListenerIf implementation
	 * ============================================================================= */

	@Override
	public void onSourceChanged(SourceEvent<Update> e) {

		// get DS update event
		DsEvent.Update u = e.getData();

		// get flags
		int flags = u.getFlags();

        // get data and co-classes
		Object[] found = select(u.getData());

		// get selected data
		T[] data = (T[])found[0];

        // any selected?
        if(data.length>0) {

	        // get type
	        DsEventType type = u.getType();

			// get flag
	        boolean clearAll = type.equals(DsEventType.CLEAR_ALL_EVENT);

	        if(clearAll) {
	        	fireDataClearAll(null, flags);
	        }
	        else {

		        // add object?
				if (type.equals(DsEventType.ADDED_EVENT)) {

					fireDataCreated(data, flags);

				}
				else if(type.equals(DsEventType.MODIFIED_EVENT)) {

					fireDataChanged(data,flags);

				}
				else if(type.equals(DsEventType.REMOVED_EVENT)) {

					fireDataDeleted(data,flags);

				}

	        }
        }

        // get selected co-data
		IDsObject[] coData = (IDsObject[])found[1];

		// has co-data?
        if(coData.length>0) {
        	fireCoDataChanged(coData,flags);
        }

		// get unselected data
		data = (T[])found[2];

		// has data?
        if(data.length>0) {
        	fireDataUnselected(data,flags);
        }

        // get unselected co-data
		coData = (IDsObject[])found[3];

		// has co-data?
        if(coData.length>0) {
        	fireCoDataUnselected(coData,flags);
        }

	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	protected T[] getData(Collection<T> items) {
		T[] data = (T[])new IDsObject[items.size()];
		items.toArray(data);
		return data;
	}

	protected Object[] select(IDsObject[] data) {
		List<IDsObject> dsList = new ArrayList<IDsObject>(data!=null ? data.length : 0);
		List<IDsObject> nsList = new ArrayList<IDsObject>(data!=null ? data.length : 0);
		List<IDsObject> coList = new ArrayList<IDsObject>(data!=null ? data.length : 0);
		List<IDsObject> noList = new ArrayList<IDsObject>(data!=null ? data.length : 0);
		for(int i=0; i<data.length; i++) {
			IDsObject dsObj = data[i];
			if(selectData(dsObj))
				dsList.add(dsObj);
			else if(isDataObject(dsObj))
				nsList.add(dsObj);
			if(selectCoObject(dsObj))
				coList.add(dsObj);
			else if(isCoObject(dsObj))
				noList.add(dsObj);
		}
		// allocate memory
		Object[] found = new Object[4];
		// get selected data
		data = new IDsObject[dsList.size()];
		dsList.toArray(data);
		IDsObject[] coClass = new IDsObject[coList.size()];
		coList.toArray(coClass);
		found[0] = data;
		found[1] = coClass;
		// get unselected data
		data = new IDsObject[nsList.size()];
		nsList.toArray(data);
		coClass = new IDsObject[noList.size()];
		noList.toArray(coClass);
		found[2] = data;
		found[3] = coClass;
		// finished
		return found;
	}

	protected S[] getIdx(IDsObject[] data) {
		int count = data!=null ? data.length : 0;
		IData[] idx = new IData[count];
		for(int i=0; i<count;  i++) {
			idx[i] = data[i].getId();
		}
		return (S[])idx;
	}

	protected void fireDataCreated(T[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),(IData[])data,mask);
		fireDataCreated(e);
	}

	protected void fireDataChanged(T[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(e);
		}
	}

	protected void fireDataDeleted(T[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataDeleted(e);
		}
	}

	protected void fireDataUnselected(T[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataUnselected(e);
		}
	}

	protected void fireDataClearAll(T[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataClearAll(e);
		}
	}

	protected void fireCoDataChanged(IDsObject[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onCoDataChanged(e);
		}
	}

	protected void fireCoDataUnselected(IDsObject[] data, int mask) {
		BinderEvent<S> e = new BinderEvent<S>(this,getIdx(data),data,mask);
		IBinderListener<S>[] list = listeners.getListeners(IBinderListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onCoDataUnselected(e);
		}
	}

}
