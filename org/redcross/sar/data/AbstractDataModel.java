package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.event.BinderAdapter;
import org.redcross.sar.data.event.BinderEvent;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.IDataListener;

public abstract class AbstractDataModel<S,T extends IData> implements IDataModel<S,T> {

	private static final long serialVersionUID = 1L;

	protected int size;

	protected int addOnCoUpdate;
	protected int removeOnCoUpdate;

	protected boolean addToTail;

	protected Class<T> dataClass;
	protected ITranslator<S, T> translator;

	protected final List<S> ids = new ArrayList<S>();
	protected final List<T> objects = new ArrayList<T>();
	protected final Map<S,Object[]> rows = new HashMap<S,Object[]>();

	protected final EventListenerList listeners = new EventListenerList();
	protected final List<IDataBinder<S,? extends IData,?>> binders = new ArrayList<IDataBinder<S,? extends IData,?>>(1);

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractDataModel(int size, Class<T> c) {
		// prepare
		this.size = size;
		this.dataClass = c;
		this.addToTail = true;
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

	public Collection<IDataBinder<S,? extends IData,?>> getBinders() {
		return new ArrayList<IDataBinder<S,? extends IData,?>>(binders);
	}

	public IDataBinder<S,? extends IData,?> getBinder(IDataSource<?> source) {
		// search for source
		for(IDataBinder<S,? extends IData,?> it : getBinders()) {
			// already connected?
			if(it.getSource()==source)
				return it;
		}
		return null;
	}

	public boolean connect(IDataBinder<S,? extends IData,?> binder) {
		// allowed?
		if(binder.getSource()!=null) {
			// search for source
			for(IDataBinder<S,? extends IData,?> it : getBinders()) {
				// already connected?
				if(it.getSource()==binder.getSource())
					return true;
			}
			// loop
			if(!binders.contains(binder)) {
				binder.addBinderListener(adapter);
				binders.add(binder);
				return true;
			}
		}
		// failed
		return false;
	}

	public boolean disconnect(IDataBinder<S,? extends IData,?> binder) {
		if(binders.contains(binder)) {
			binder.removeBinderListener(adapter);
			binders.remove(binder);
			return true;
		}
		return false;
	}

	public boolean disconnectAll() {
		// initialize counter
		int count = 0;
		// search for source
		for(IDataBinder<S,? extends IData,?> it : getBinders()) {
			// already connected?
			if(disconnect(it))count++;
		}
		// finished
		return count>0;
	}

	public boolean isAddToTail() {
		return addToTail;
	}

	public void setAddToTail(boolean isAddToTail) {
		addToTail = isAddToTail;
	}

	public int getAddOnCoUpdate() {
		return addOnCoUpdate;
	}

	public void setAddOnCoUpdate(int flag) {
		addOnCoUpdate = flag;
	}

	public int getRemoveOnCoUpdate() {
		return removeOnCoUpdate;
	}

	public void setRemoveOnCoUpdate(int flag) {
		removeOnCoUpdate = flag;
	}

	public void load() {
		// reset
		clear();
		// search for source
		for(IDataBinder<S,? extends IData,?> it : getBinders()) {
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
		for(IDataBinder<S, ?, ?> it : getBinders()) {
			// is data supported?
			if(isSupported(it.getDataClass())) {
				// cast binder to supported data type
				IDataBinder<S,T,?> binder = (IDataBinder<S,T,?>)it;
				// forward
				binder.load(list);
			}
		}
	}

	public int add(S id, T obj) {
		int iRow = findRowFromId(id);
		if (iRow == -1) {
			if(addToTail) {
				ids.add(id);
				objects.add(obj);
			}
			else {
				ids.add(0,id);
				objects.add(0,obj);
			}
			rows.put(id,create(id,obj,size));
		}
		// fill data into row
		return update(id,obj,false);
	}

	public int update(S id, T obj) {
		// forward
		return update(id,obj,false);
	}

	public int remove(S id) {
		int row = findRowFromId(id);
		if (row != -1) {
			cleanup(id,false);
			ids.remove(row);
			objects.remove(row);
			rows.remove(id);
		}
		return row;
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

	public Collection<S> getIds(Selector<S> selector, Comparator<S> comparator) {
		return DataUtils.selectItemsInCollection(selector, comparator, ids);
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

	public Collection<T> getObjects(Selector<T> selector, Comparator<T> comparator) {
		return DataUtils.selectItemsInCollection(selector, comparator, objects);
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
		fireDataChanged(new int[]{iRow},DataEvent.UPDATED_EVENT);
	}

	public void addDataListener(IDataListener listener) {
		listeners.add(IDataListener.class, listener);
	}

	public void removeDataListener(IDataListener listener) {
		listeners.remove(IDataListener.class, listener);
	}

	public ITranslator<S, T> getTranslator() {
		return translator;
	}

	public void setTranslator(ITranslator<S, T> translator) {
		this.translator = translator;
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
	@SuppressWarnings("unchecked")
	protected S[] translate(IData[] data) {
		if(translator!=null) {
			return translator.translate(data);
		}
		return null;
	}

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

	protected void fireDataChanged(int rows[], int type) {
		DataEvent e = new DataEvent(this,rows,type);
		IDataListener[] list = listeners.getListeners(IDataListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(e);
		}
	}

	protected int update(S id, T obj, boolean doAdd) {
		// get current data
		Object[] data = rows.get(id);
		if(data!=null) {
			// get object
			// forward
			data = update(id,obj,data);
			// save changes
			rows.put(id,data);
			// return index
			return findRowFromId(id);
		}
		else if (doAdd) {
			return add(id, obj);
		}
		return -1;
	}

	protected boolean isFlag(int pattern, int flag) {
		return (pattern & flag)!=0;
	}

	/* =============================================================================
	 * Anonymous classes
	 * ============================================================================= */

	private final BinderAdapter<S> adapter = new BinderAdapter<S>() {

		@SuppressWarnings("unchecked")
		public void onDataCreated(BinderEvent<S> e) {

			// get information
			S[] idx = e.getIdx();
			IData[] data = e.getData();

			// initialize counter
	        int count = data!=null ? data.length : 0;

	        // create list
	        Collection<Integer> list = new Vector<Integer>(count);

	        // initialize
	        int row = 0;

			// add data
        	for(int i=0;i<count;i++) {
        		IData d = data[i];
        		if(dataClass.isInstance(d)) {
        			row = add(idx[i],(T)d);
        		}
        		else {
        			row = add(idx[i],null);
        		}

        		// a row was changed?
    			if(row!=-1) list.add(row);

        	}

        	// update count
        	count = list.size();

    		// is dirty?
    		if(count>0) {

    	        // allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get indexes
    			for(Integer it : list) {
    				rows[i++] = it;
    			}

    			fireDataChanged(rows,DataEvent.ADDED_EVENT);

    		}

		}

		@SuppressWarnings("unchecked")
		public void onDataChanged(BinderEvent<S> e) {

			// get information
			S[] idx = e.getIdx();
			IData[] data = e.getData();

			// get count
	        int count = data!=null ? data.length : 0;

	        // create list
	        Collection<Integer> list = new Vector<Integer>(count);

	        // initialize
	        int row = 0;

			// update data
        	for(int i=0;i<count;i++) {

        		// get id
        		S id = idx[i];

        		// get data
        		T d = (T)data[i];

        		// update row data, add if not exist
        		if(dataClass.isInstance(d)) {
        			row = update(id,d,true);
        		}
        		else {
        			row = update(id,null,true);
        		}

        		// a row was changed?
    			if(row!=-1) list.add(row);

        	}

        	// update count
        	count = list.size();

    		// is dirty?
    		if(count>0) {

    	        // allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get indexes
    			for(Integer it : list) {
    				rows[i++] = it;
    			}

    			// notify
    			fireDataChanged(rows,DataEvent.UPDATED_EVENT);

    		}

		}

		@SuppressWarnings("unchecked")
		public void onDataDeleted(BinderEvent<S> e) {

			// get information
			S[] idx = e.getIdx();

			// get count
	        int count = idx!=null ? idx.length : 0;

	        // create list
	        Collection<Integer> list = new Vector<Integer>(count);

			// remove rows
        	for(int i=0;i<count;i++) {

        		// try to remove
				int row = remove(idx[i]);

				// was a row removed?
				if(row!=-1) list.add(row);

        	}

        	// update count
        	count = list.size();

    		// is dirty?
    		if(count>0) {

    	        // allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get indexes
    			for(Integer it : list) {
    				rows[i++] = it;
    			}

    			// notify
    			fireDataChanged(rows,DataEvent.REMOVED_EVENT);

    		}


		}

		@Override
		public void onDataUnselected(BinderEvent<S> e) {

			// get information
			S[] idx = e.getIdx();

			// get count
	        int count = idx!=null ? idx.length : 0;

	        // create remove list
	        Collection<Integer> removed = new Vector<Integer>(count);

			// remove rows
        	for(int i=0;i<count;i++) {

        		// try to remove
				int row = remove(idx[i]);

				// was a row removed?
				if(row!=-1) removed.add(row);

        	}

        	// update count
        	count = removed.size();

    		// is dirty?
    		if(count>0) {

    	        // allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get indexes
    			for(Integer it : removed) {
    				rows[i++] = it;
    			}

    			// notify
    			fireDataChanged(rows,DataEvent.REMOVED_EVENT);

    		}

    	}

		public void onDataClearAll(BinderEvent<S> e) {

			// notify
			cleanup(null,true);

			// remove all
			ids.clear();
			objects.clear();
			rows.clear();

			// notify
			fireDataChanged(null,DataEvent.CLEAR_EVENT);

		}

		public void onCoDataChanged(BinderEvent<S> e) {

			// get data information
			S[] idx = translate(e.getData());

			// get count
	        int count = idx!=null ? idx.length : 0;

	        // allocate memory
			List<Integer> list = new ArrayList<Integer>(count);

	        // update all identified rows
        	for(int i=0;i<count;i++) {
        		if(idx[i]!=null) {
        			int row = -1;
        			if(isFlag(e.getFlags(),addOnCoUpdate)) {
        				// add, update data if exists
        				row = add(idx[i], null);
        			}
        			else if(isFlag(e.getFlags(),removeOnCoUpdate)) {
        				// remove if exists
        				row = remove(idx[i]);
        			}
        			else {
        				// try to update only
            			row = update(idx[i],null,false);
        			}
        			// changed?
        			if(row!=-1) {
        				list.add(row);
        			}
        		}
        	}

        	// update change count
        	count = list.size();

    		// is dirty?
    		if(count>0) {

    			// allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get updated rows
    			for(Integer it : list) {
    				rows[i++] = it;
    			}

    			// notify
    			fireDataChanged(rows,DataEvent.UPDATED_EVENT);

    		}

		}

		public void onCoDataUnselected(BinderEvent<S> e) {

			// get data information
			S[] idx = translate(e.getData());

			// get count
	        int count = idx!=null ? idx.length : 0;

	        // create list
	        Collection<Integer> list = new Vector<Integer>(count);

			// remove rows
        	for(int i=0;i<count;i++) {

        		// try to remove
				int row = remove(idx[i]);

				// was a row removed?
				if(row!=-1) list.add(row);

        	}

        	// update count
        	count = list.size();

    		// is dirty?
    		if(count>0) {

    	        // allocate memory
    			int[] rows = new int[count];

    			// initialize
    			int i = 0;

    			// get indexes
    			for(Integer it : list) {
    				rows[i++] = it;
    			}

    			// notify
    			fireDataChanged(rows,DataEvent.REMOVED_EVENT);

    		}

    	}

	};

}

