package org.redcross.sar.mso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redcross.sar.data.AbstractBinder;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

@SuppressWarnings("unchecked")
public class MsoBinder<T extends IMsoObjectIf> extends AbstractBinder<T,T,Update> {

	private static final long serialVersionUID = 1L;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public MsoBinder(Class<T> c) {
		// forward
		super(c);
	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public void setSelector(IMsoListIf<T> list) {
		setSelector(createListSelector(list));
	}

	public boolean load(IMsoListIf<T> list) {
		return load(list.getItems());
	}

	/* =============================================================================
	 * IDataBinderIf implementation
	 * ============================================================================= */

	@Override
	public void addCoClass(Class<?> c, Selector<?> selector) {
		if(!IMsoObjectIf.class.isAssignableFrom(c))
			throw new IllegalArgumentException("Only data a class of type " + IMsoObjectIf.class.getName() + " is a valid co-class");
		super.addCoClass(c, selector);
	}

	@Override
	public boolean load(Collection<T> objects) {
		// allowed?
		if(source!=null) {
			// sort objects
			List<T> list = sort(objects);
			// get MSO objects
			IMsoObjectIf[] objs = new IMsoObjectIf[list.size()];
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
		// failure
		return false;
	}

	/* =============================================================================
	 * ISourceListenerIf implementation
	 * ============================================================================= */

	public void onSourceChanged(SourceEvent<Update> e) {

		// get MSO update event
		Update u = e.getInformation();

		// get flags
		int mask = u.getEventTypeMask();

        // get MSO object
        IMsoObjectIf msoObj = (IMsoObjectIf)u.getSource();

        // is data object?
        if(selectData(msoObj)) {

        	// get id
        	T id = (T)msoObj;

			// get flag
	        boolean clearAll = (mask & MsoEvent.MsoEventType.CLEAR_ALL_EVENT.maskValue()) != 0;

	        if(clearAll) {
	        	fireDataClearAll(id, mask);
	        }
	        else {

		        // add object?
				if (u.isCreateObjectEvent()) {

					fireDataCreated(id, mask);

				}

				// is object modified?
				if (!u.isDeleteObjectEvent()  && (u.isModifyObjectEvent() || u.isChangeReferenceEvent())) {

					fireDataChanged(id,mask);

				}

				// delete object?
				if (u.isDeleteObjectEvent()) {

					fireDataDeleted(id,mask);

				}
	        }
        }
        else if(selectCoObject(msoObj)) {
        	fireDataCoClassChanged(msoObj, mask);
        }
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	protected Object[] select(IMsoObjectIf[] data) {
		List<IMsoObjectIf> msoList = new ArrayList<IMsoObjectIf>(data!=null ? data.length : 0);
		List<IMsoObjectIf> coList = new ArrayList<IMsoObjectIf>(data!=null ? data.length : 0);
		for(int i=0; i<data.length; i++) {
			IMsoObjectIf msoObj = data[i];
			if(selectData(msoObj))
				msoList.add(msoObj);
			if(selectCoObject(msoObj))
				coList.add(msoObj);
		}
		data = new IMsoObjectIf[msoList.size()];
		msoList.toArray(data);
		IMsoObjectIf[] coClass = new IMsoObjectIf[coList.size()];
		coList.toArray(coClass);
		Object[] found = new Object[2];
		found[0] = data;
		found[1] = coClass;
		return found;
	}

	protected T[] getIdx(T id) {
		IMsoObjectIf[] data = new IMsoObjectIf[]{id};
		return (T[])data;
	}

	protected T[] getData(T id) {
		IMsoObjectIf[] data = new IMsoObjectIf[]{id};
		return (T[])data;
	}

	protected void fireDataCreated(T[] id, int mask) {
		fireDataCreated(new DataEvent<T>(this,id,id,mask));
	}

	protected void fireDataCreated(T id, int mask) {
		fireDataCreated(new DataEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataChanged(T id, int mask) {
		fireDataChanged(new DataEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataDeleted(T id, int mask) {
		fireDataDeleted(new DataEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataClearAll(T id, int mask) {
		fireDataClearAll(new DataEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataCoClassChanged(IMsoObjectIf data, int mask) {
		fireDataCoClassChanged(new DataEvent<T>(this,null,new IMsoObjectIf[]{data},mask));
	}

	protected void fireDataCoClassChanged(IMsoObjectIf[] data, int mask) {
		fireDataCoClassChanged(new DataEvent<T>(this,null,data,mask));
	}

	private Selector<T> createListSelector(final IMsoListIf<T> list) {
		Selector<T> selector = new Selector<T>() {
			public boolean select(T anObject) {
				return list.exists(anObject);
			}
		};
		return selector;
	}


}
