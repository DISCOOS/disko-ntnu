package org.redcross.sar.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redcross.sar.data.AbstractBinder;
import org.redcross.sar.data.IDataIf;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.IDataListenerIf;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
import org.redcross.sar.ds.event.DsEvent.Update;

@SuppressWarnings("unchecked")
public class DsBinder<S extends IDataIf, T extends IDsObjectIf> extends AbstractBinder<S, T, DsEvent.Update> {

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
	public void addCoClass(Class<?> c, Selector<?> selector) {
		if(!IDsObjectIf.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Only data a class of type " + IDsObjectIf.class.getName() + " is a valid co-class");
		super.addCoClass(c,selector);
	}

	public boolean load(Collection<T> objects) {
		// allowed?
		if(source!=null) {
			// sort objects
			List<T> list = sort(objects);
			// get DS objects
			IDsObjectIf[] objs = new IDsObjectIf[list.size()];
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
	        	fireDataCoClassChanged(data, 0);
	        }
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
		DsEvent.Update u = e.getInformation();

		// get flags
		int flags = u.getFlags();

        // get data and co-classes
		Object[] found = select(u.getData());

		// get data
		T[] data = (T[])found[0];

        // any selected?
        if(data.length>0) {

	        // get type
	        DsEventType type = u.getType();

			// get flag
	        boolean clearAll = type.equals(DsEventType.CLEAR_ALL_EVENT);

	        if(clearAll) {
	        	fireDataClearAll(data, flags);
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

        // get data co-classes
		IDsObjectIf[] coClass = (IDsObjectIf[])found[1];

		// has co-classes?
        if(coClass.length>0) {
        	fireDataCoClassChanged(coClass,flags);
        }
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	protected Object[] select(IDsObjectIf[] data) {
		List<IDsObjectIf> dsList = new ArrayList<IDsObjectIf>(data!=null ? data.length : 0);
		List<IDsObjectIf> coList = new ArrayList<IDsObjectIf>(data!=null ? data.length : 0);
		for(int i=0; i<data.length; i++) {
			IDsObjectIf dsObj = data[i];
			if(selectData(dsObj))
				dsList.add(dsObj);
			if(selectCoObject(dsObj))
				coList.add(dsObj);
		}
		data = new IDsObjectIf[dsList.size()];
		dsList.toArray(data);
		IDsObjectIf[] coClass = new IDsObjectIf[coList.size()];
		coList.toArray(coClass);
		Object[] found = new Object[2];
		found[0] = data;
		found[1] = coClass;
		return found;
	}

	protected S[] getIdx(IDsObjectIf[] data) {
		int count = data!=null ? data.length : 0;
		IDataIf[] idx = new IDataIf[count];
		for(int i=0; i<count;  i++) {
			idx[i] = data[i].getId();
		}
		return (S[])idx;
	}

	protected void fireDataCreated(T[] data, int mask) {
		DataEvent<S> e = new DataEvent<S>(this,getIdx(data),(IDataIf[])data,mask);
		fireDataCreated(e);
	}

	protected void fireDataChanged(T[] data, int mask) {
		DataEvent<S> e = new DataEvent<S>(this,getIdx(data),data,mask);
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(e);
		}
	}

	protected void fireDataDeleted(T[] data, int mask) {
		DataEvent<S> e = new DataEvent<S>(this,getIdx(data),data,mask);
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataDeleted(e);
		}
	}

	protected void fireDataClearAll(T[] data, int mask) {
		DataEvent<S> e = new DataEvent<S>(this,getIdx(data),data,mask);
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataClearAll(e);
		}
	}

	protected void fireDataCoClassChanged(IDsObjectIf[] data, int mask) {
		DataEvent<S> e = new DataEvent<S>(this,getIdx(data),data,mask);
		IDataListenerIf<S>[] list = listeners.getListeners(IDataListenerIf.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataCoClassChanged(e);
		}
	}

}
