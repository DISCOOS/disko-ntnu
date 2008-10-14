package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.redcross.sar.data.event.DataAdapter;
import org.redcross.sar.data.event.DataEvent;

public abstract class AbstractDataModel<S,T extends IDataIf> implements IDataModelIf<S,T> {

	private static final long serialVersionUID = 1L;

	protected int size;
	protected Class<T> dataClass;

	protected final List<S> ids = new ArrayList<S>();
	protected final List<T> objects = new ArrayList<T>();
	protected final Map<S,Object[]> rows = new HashMap<S,Object[]>();

	protected final EventListenerList listeners = new EventListenerList();
	protected final List<IDataBinderIf<S,? extends IDataIf,?>> binders = new ArrayList<IDataBinderIf<S,? extends IDataIf,?>>(1);

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractDataModel(int size, Class<T> c) {
		// prepare
		this.size = size;
		this.dataClass = c;
	}

	/* =============================================================================
	 * IDataModelIf implementation
	 * ============================================================================= */

	public Class<T> getDataClass() {
		return dataClass;
	}

	public boolean isSupported(Class<?> dataClass) {
		return this.dataClass.isAssignableFrom(dataClass);
	}

	public Collection<IDataBinderIf<S,? extends IDataIf,?>> getBinders() {
		return new ArrayList<IDataBinderIf<S,? extends IDataIf,?>>(binders);
	}

	public IDataBinderIf<S,? extends IDataIf,?> getBinder(IDataSourceIf<?> source) {
		// search for source
		for(IDataBinderIf<S,? extends IDataIf,?> it : binders) {
			// already connected?
			if(it.getSource()==source)
				return it;
		}
		return null;
	}

	public boolean connect(IDataBinderIf<S,? extends IDataIf,?> binder) {
		// allowed?
		if(binder.getSource()!=null) {
			// search for source
			for(IDataBinderIf<S,? extends IDataIf,?> it : binders) {
				// already connected?
				if(it.getSource()==binder.getSource())
					return true;
			}
			// loop
			if(!binders.contains(binder)) {
				binder.addDataListener(adapter);
				binders.add(binder);
				return true;
			}
		}
		// failed
		return false;
	}

	public boolean disconnect(IDataBinderIf<S,? extends IDataIf,?> binder) {
		if(binders.contains(binder)) {
			binder.removeDataListener(adapter);
			binders.remove(binder);
			return true;
		}
		return false;
	}

	public boolean disconnectAll() {
		// initialize counter
		int count = 0;
		// search for source
		for(IDataBinderIf<S,? extends IDataIf,?> it : binders) {
			// already connected?
			if(disconnect(it))count++;
		}
		// finished
		return count>0;
	}

	public void load() {
		// reset
		clear();
		// search for source
		for(IDataBinderIf<S,? extends IDataIf,?> it : binders) {
			it.load();
		}
	}

	public void load(Collection<T> list) {
		// reset
		clear();
		// forward
		addAll(list);
	}

	@SuppressWarnings("unchecked")
	public void addAll(Collection<T> list) {
		// loop over all binders
		for(IDataBinderIf<S, ?, ?> it : getBinders()) {
			// is data supported?
			if(isSupported(it.getDataClass())) {
				// cast binder to supported data type
				IDataBinderIf<S,T,?> binder = (IDataBinderIf<S,T,?>)it;
				// forward
				binder.load(list);
			}
		}
	}

	public void add(S id, T obj) {
		int iRow = findRowFromId(id);
		if (iRow == -1) {
			ids.add(id);
			objects.add(obj);
			rows.put(id,create(id,obj,size));
		}
		update(id,obj);
	}

	public void update(S id, T obj) {
		// get current data
		Object[] data = rows.get(id);
		// exists?
		if(data!=null) {
			// forward
			data = update(id,obj,data);
			// save changes
			rows.put(id,data);
		}
	}

	public void remove(S id) {
		int row = findRowFromId(id);
		if (row != -1) {
			cleanup(id,false);
			ids.remove(row);
			objects.remove(row);
			rows.remove(id);
		}
	}

	public void clear() {
		if(ids.size()>0) cleanup(null,false);
		ids.clear();
		objects.clear();
		rows.clear();
	}

	public int findRowFromId(S id) {
		if(id!=null) {
			int i=0;
			for(S it : ids) {
				if (it.equals(id)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	public int findRowFromObject(T obj) {
		if(obj!=null) {
			int i=0;
			for(T it : objects) {
				if (it.equals(obj)) {
					return i;
				}
				i++;
			}
		}
		return -1;
	}

	public S getId(int row) {
		if(row>-1 && row<rows.size()) {
			return ids.get(row);
		}
		return null;
	}

	public Collection<S> getIds() {
		return new ArrayList<S>(ids);
	}

	public T getObject(int row) {
		if(row>-1 && row<rows.size()) {
			return objects.get(row);
		}
		return null;
	}

	public Collection<T> getObjects() {
		return new ArrayList<T>(objects);
	}

	public Object[] getData(int row) {
		if(row>-1 && row<rows.size()) {
			return rows.get(ids.get(row));
		}
		return null;
	}

	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int iRow, int iCol) {
    	if(!(iRow>=0 && iRow<objects.size())) return null;
    	S id = ids.get(iRow);
		if(id==null) return null;
		Object[] row = rows.get(id);
		if(row==null) return null;
		if(!(iCol>=0 && iCol<row.length)) return null;
		return row[iCol];
	}

	public void setValueAt(Object value, int iRow, int iCol) {
    	if(!(iRow>=0 && iRow<objects.size())) return;
    	S id = ids.get(iRow);
		if(id==null) return;
		Object[] row = rows.get(id);
		if(row==null) return;
		if(!(iCol>=0 && iCol<row.length)) return;
		row[iCol] = value;
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(ChangeListener.class, listener);
	}

	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	/**
	 * Is fired when IDataModel.add(S id) is called.
	 *
	 * @param S id - The added row id
	 * @param T obj - The added data object
	 * @param int size - number of values in object
	 */
	protected abstract Object[] create(S id, T obj, int size);

	/**
	 * Is fired when IDataModel.update(S id, T obj) is called.
	 *
	 * @param S id - The updated row id
	 * @param T obj - The updated data object
	 * @param int size - array of values to update
	 */
	protected abstract Object[] update(S id, T obj, Object[] data);

	/**
	 * Is fired when IDataModel.remove(S id) and onDataClearAll() is called internally.
	 *
	 * @param S id - The affected row id. If <code>null</code>, all rows are removed.
	 * @param boolean finalize - If <code>true</code>, object references to data should be set to <code>null</code>
	 */
	protected abstract void cleanup(S id,boolean finalize);

	/**
	 * Is fired when a data in co-class is changed
	 *
	 * @param IDataIf[] data - The data in co-class to translate into a row id in data class
	 *
	 * @return S[] row id array
	 */
	protected abstract S[] translate(IDataIf[] data);

	protected static Boolean[] defaultEditable(int size) {
		Boolean[] editable = new Boolean[size];
		for(int i=0;i<size;i++)
			editable[i] = false;
		return editable;
	}

	protected static String[] defaultEditors(int size, String name) {
		String[] editors = new String[size];
		for(int i=0;i<size;i++)
			editors[i] = name;
		return editors;
	}

	protected void fireDataChanged() {
		ChangeEvent e = new ChangeEvent(this);
		ChangeListener[] list = listeners.getListeners(ChangeListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].stateChanged(e);
		}
	}

	/* =============================================================================
	 * Anonymous classes
	 * ============================================================================= */

	private final DataAdapter<S> adapter = new DataAdapter<S>() {

		@SuppressWarnings("unchecked")
		public void onDataCreated(DataEvent<S> e) {

			// get information
			S[] idx = e.getIdx();
			IDataIf[] data = e.getData();

			// initialize counter
	        int count = data!=null ? data.length : 0;

        	for(int i=0;i<count;i++) {
        		IDataIf d = data[i];
        		if(dataClass.isInstance(d))
        			add(idx[i],(T)d);
        		else
        			add(idx[i],null);
        	}

    		// is dirty?
    		if(count>0) {

    			fireDataChanged();

    		}

		}

		@SuppressWarnings("unchecked")
		public void onDataChanged(DataEvent<S> e) {

			// get information
			S[] idx = e.getIdx();
			IDataIf[] data = e.getData();

			// get count
	        int count = data!=null ? data.length : 0;

        	for(int i=0;i<count;i++) {
        		IDataIf d = data[i];
        		if(dataClass.isInstance(d))
    				update(idx[i],(T)d);
        		else
    				update(idx[i],null);
        	}

    		// is dirty?
    		if(count>0) {

    			fireDataChanged();

    		}

		}

		@SuppressWarnings("unchecked")
		public void onDataDeleted(DataEvent<S> e) {

			// get information
			S[] idx = e.getIdx();

			// get count
	        int count = idx!=null ? idx.length : 0;

        	for(int i=0;i<count;i++) {

				remove(idx[i]);

        	}

    		// is dirty?
    		if(count>0) {

    			fireDataChanged();

    		}

		}

		public void onDataClearAll(DataEvent<S> e) {

			// notify
			cleanup(null,true);

			// remove all
			ids.clear();
			objects.clear();
			rows.clear();

			// notify
			fireDataChanged();

		}

		public void onDataCoClassChanged(DataEvent<S> e) {

			// get data information
			S[] idx = translate(e.getData());

			// get count
	        int count = idx!=null ? idx.length : 0;

	        // initialize dirty counter
	        int dirty = 0;

	        // update all identified rows
        	for(int i=0;i<count;i++) {
        		if(idx[i]!=null) {
        			update(idx[i],null);
        			dirty++;
        		}
        	}

    		// is dirty?
    		if(dirty>0) {

    			fireDataChanged();

    		}

		}

	};

}

