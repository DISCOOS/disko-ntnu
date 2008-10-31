package org.redcross.sar.mso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.redcross.sar.data.AbstractBinder;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.BinderEvent;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;

@SuppressWarnings("unchecked")
public class MsoBinder<T extends IMsoObjectIf> extends AbstractBinder<T,T,UpdateList> {

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
	public IMsoModelIf getSource() {
		return (IMsoModelIf)source;
	}

	public boolean connect(IMsoModelIf source) {
		return super.connect(source);
	}

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
	        if(data.length>0 && coClassList.size()>0) {
	        	fireCoDataChanged(data, 0);
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

	public void onSourceChanged(SourceEvent<MsoEvent.UpdateList> e) {

		// get MSO update event
		UpdateList events = e.getInformation();

		// clear all event?
		if(events.isClearAllEvent()) {
			MsoEvent.Update it = events.getEvents().get(0);
			IMsoObjectIf msoObj = it.getSource();
			fireDataClearAll((T)msoObj,it.getEventTypeMask());
		}
		else {

			// loop over all events
			for(MsoEvent.Update it : events.getEvents()) {

				// consume loopback updates
				if(!UpdateMode.LOOPBACK_UPDATE_MODE.equals(it.getUpdateMode())) {

					// get flags
					int mask = it.getEventTypeMask();

			        // get MSO object
			        IMsoObjectIf msoObj = (IMsoObjectIf)it.getSource();

			        // is data object?
			        if(selectData(msoObj)) {

			        	// get id
			        	T id = (T)msoObj;

				        // add object?
						if (it.isCreateObjectEvent()) {

							fireDataCreated(id, mask);

						}

						// is object modified?
						if (!it.isDeleteObjectEvent()  && (it.isModifyObjectEvent() || it.isChangeReferenceEvent())) {

							fireDataChanged(id,mask);

						}

						// delete object?
						if (it.isDeleteObjectEvent()) {

							fireDataDeleted(id,mask);

						}

			        }
			        else if(selectCoObject(msoObj)) {
			        	fireCoDataChanged(msoObj, mask);
			        }
			        else if (isDataObject(msoObj)) {
			        	fireDataUnselected((T)msoObj, mask);
			        }
			        else if (isCoObject(msoObj)) {
			        	fireCoDataUnselected(msoObj, mask);
			        }
				}
			}
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
		fireDataCreated(new BinderEvent<T>(this,id,id,mask));
	}

	protected void fireDataCreated(T id, int mask) {
		fireDataCreated(new BinderEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataChanged(T id, int mask) {
		fireDataChanged(new BinderEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataDeleted(T id, int mask) {
		fireDataDeleted(new BinderEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataUnselected(T id, int mask) {
		fireDataUnselected(new BinderEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireDataClearAll(T id, int mask) {
		fireDataClearAll(new BinderEvent<T>(this,getIdx(id),getData(id),mask));
	}

	protected void fireCoDataChanged(IMsoObjectIf data, int mask) {
		fireCoDataChanged(new BinderEvent<T>(this,null,new IMsoObjectIf[]{data},mask));
	}

	protected void fireCoDataChanged(IMsoObjectIf[] data, int mask) {
		fireCoDataChanged(new BinderEvent<T>(this,null,data,mask));
	}

	protected void fireCoDataUnselected(IMsoObjectIf data, int mask) {
		fireCoDataUnselected(new BinderEvent<T>(this,null,new IMsoObjectIf[]{data},mask));
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
