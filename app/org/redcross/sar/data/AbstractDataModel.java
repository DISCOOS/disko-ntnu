package org.redcross.sar.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
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
	protected ITranslator<S, IData> translator;

	protected final List<S> ids = new ArrayList<S>();
	protected final List<T> objects = new ArrayList<T>();
	protected final Map<S,IRow> rows = new HashMap<S,IRow>();

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

	public boolean isConnected(IDataSource<?> source) {
		return (getBinder(source)!=null);
	}

	public boolean connect(IDataBinder<S,? extends IData,?> binder) {
		// allowed?
		if(binder.getSource()!=null) {
			// is not already connected?
			if(!isConnected(binder.getSource())) {
				// loop
				if(!binders.contains(binder)) {
					binder.addBinderListener(adapter);
					binders.add(binder);
					return true;
				}
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
		load(list,false);
	}

	@SuppressWarnings("unchecked")
	public void load(Collection<T> list, boolean append) {
		// loop over all binders
		for(IDataBinder<S, ?, ?> it : getBinders()) {
			// is data supported?
			if(isSupported(it.getDataClass())) {
				// cast binder to supported data type
				IDataBinder<S,T,?> binder = (IDataBinder<S,T,?>)it;
				// forward
				binder.load(list,append);
			}
		}
	}

	public int add(S id, T obj) {
		return add(id, obj, true);
	}

	public void clear() {
		clear(true);
	}

	public int remove(S id) {
		return remove(id, true);
	}

	public int update(S id, T obj) {
		return update(id, obj, true);
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
			return rows.get(ids.get(row)).getData();
		}
		return null;
	}

	public int getRowCount() {
		return rows.size();
	}
	
	public IRow getRow(int index) {
    	if(!(index>=0 && index<objects.size())) return null;
    	S id = ids.get(index);
		if(id==null) return null;
		return rows.get(id);		
	}

	public ICell getCell(int iRow, int iCol) {
		IRow row = getRow(iRow);
		if(row==null) return null;
		if(!(iCol>=0 && iCol<size)) return null;
		return row.getCell(iCol);
	}
	
	public Object getValueAt(int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return null;
		return cell.getValue();
	}

	public void setValueAt(Object value, int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return;
		cell.setValue(value);
		fireDataChanged(new int[]{iRow},DataEvent.UPDATED_EVENT);
	}
	
	public DataOrigin getOriginAt(int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return null;
		return cell.getDataOrigin();
	}
	
	public void setOriginAt(DataOrigin origin, int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return;
		cell.setDataOrigin(origin);
	}
	
	public DataState getStateAt(int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return null;
		return cell.getDataState();
	}
	
	public void setStateAt(DataState state, int iRow, int iCol) {
		ICell cell = getCell(iRow, iCol);
		if(cell==null) return;
		cell.setDataState(state);
	}
	

	public void addDataListener(IDataListener listener) {
		listeners.add(IDataListener.class, listener);
	}

	public void removeDataListener(IDataListener listener) {
		listeners.remove(IDataListener.class, listener);
	}

	public ITranslator<S, IData> getTranslator() {
		return translator;
	}

	public void setTranslator(ITranslator<S, IData> translator) {
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
	protected abstract IRow create(S id, T obj, int size);

	/**
	 * Is fired when IDataModel.update(S id, T obj) is called.
	 *
	 * @param S id - The updated row id
	 * @param T obj - The updated data object
	 * @param IRow data - the row data
	 */
	protected abstract IRow update(S id, T obj, IRow data);

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

	protected boolean isFlag(int pattern, int flag) {
		return (pattern & flag)!=0;
	}

	protected int add(S id, T obj, boolean notify) {
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
		return update(id,obj,false,notify);
	}

	protected int update(S id, T obj, boolean notify) {
		// forward
		return update(id,obj,false, notify);
	}

	protected int update(S id, T obj, boolean doAdd, boolean notify) {
		// get current data
		IRow data = rows.get(id);
		if(data!=null) {
			// forward
			data = update(id,obj,data);
			// save changes
			rows.put(id,data);
			// find row index
			int row = findRowFromId(id);
			// notify?
			if(notify) fireDataChanged(new int[]{row},DataEvent.UPDATED_EVENT);
			// finished
			return row;
		}
		else if (doAdd) {
			return add(id, obj,notify);
		}
		return -1;
	}

	protected int remove(S id, boolean notify) {
		int row = findRowFromId(id);
		if (row != -1) {
			cleanup(id,false);
			ids.remove(row);
			objects.remove(row);
			rows.remove(id);
			if(notify) fireDataChanged(new int[]{row},DataEvent.REMOVED_EVENT);
		}
		return row;
	}

	protected void clear(boolean notify) {
		int size = ids.size();
		if(size>0) cleanup(null,false);
		int[] idx = new int[size];
		for(int i=0;i<size;i++) {
			idx[i]=i;
		}
		ids.clear();
		objects.clear();
		rows.clear();
		if(notify) fireDataChanged(idx,DataEvent.CLEAR_EVENT);
	}

	/**
	 * Create a Row instance of given size.
	 * 
	 * @param size - the size of the row (number of cells)
	 * @return A Row instance of given size.
	 */
	public static IRow createRow(int size) {
		return new Row(size);
	}
	
	/**
	 * Create a Row instance with given data.
	 * 
	 * @param data - the row data (cell values)
	 * @return A Row instance with given data.
	 */
	public static IRow createRow(Object[] data) {
		return new Row(data);
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
        			row = add(idx[i], (T)d, false);
        		}
        		else {
        			row = add(idx[i], null, false);
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
        			row = update(id, d, true, false);
        		}
        		else {
        			row = update(id, null, true, false);
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
				int row = remove(idx[i], false);

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
				int row = remove(idx[i], false);

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

			// clear all?
			if(e.getData()==null) {
				clear(true);
			}
			else {

				// get information
				S[] idx = e.getIdx();

				// get count
		        int count = idx!=null ? idx.length : 0;

		        // create remove list
		        Collection<Integer> removed = new Vector<Integer>(count);

				// remove rows
	        	for(int i=0;i<count;i++) {

	        		// try to remove
					int row = remove(idx[i], false);

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
	    			fireDataChanged(rows,DataEvent.CLEAR_EVENT);

	    		}
			}

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
        				row = add(idx[i], null, false);
        			}
        			else if(isFlag(e.getFlags(),removeOnCoUpdate)) {
        				// remove if exists
        				row = remove(idx[i], false);
        			}
        			else {
        				// try to update only
            			row = update(idx[i], null, false, false);
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
				int row = remove(idx[i], false);

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
	
	/* =============================================================================
	 * Inner classes
	 * ============================================================================= */
	
	private final static class Row implements IRow {
		
		protected DataState state; 
		protected DataOrigin origin; 
		protected Cell[] cells;
		
		protected Row(int size) {
			this(size,DataOrigin.NONE,DataState.NONE);
		}
		
		protected Row(int size, DataOrigin origin, DataState state) {
			cells = new Cell[size];
			for(int i=0;i<size;i++){
				cells[i] = new Cell();
			}			
			this.origin = origin;
			this.state = state;
		}
		
		protected Row(Object[] data) {
			cells = new Cell[data.length];
			for(int i=0;i<data.length;i++){
				cells[i] = new Cell();
				cells[i].value = data[i];
			}
		}
		
		public DataOrigin getDataOrigin() {
			return origin;
		}

		public void setDataOrigin(DataOrigin origin) {
			this.origin = origin;
		}
		
		public DataState getDataState() {
			return state;
		}
		
		public void setDataState(DataState state) {
			this.state = state;
		}
		
		public Object getValue(int index) {
			return cells[index].value;
		}
		
		public void setValue(int index, Object value) {
			cells[index].value = value;
		}
		
		public ICell[] getCells() {
			return cells;
		}
		
		public Object[] getData() {
			Object[] data = new Object[cells.length];
			for(int i=0;i<cells.length;i++) {
				data[i]=cells[i].value;
			}
			return data;
		}
		
		public void setData(Object[] values) {
			for(int i=0;i<cells.length;i++) {
				setValue(i,values[i]);
			}
		}
		
		public ICell getCell(int index) {
			return cells[index];
		}
		
	};
	
	private final static class Cell implements ICell {
		
		protected Object value;
		protected DataState state;
		protected DataOrigin origin;
		
		protected Cell() {
			this(null,DataOrigin.NONE,DataState.NONE);
		}
		protected Cell(Object value, DataOrigin origin, DataState state) {
			this.value = value;
			this.origin = origin;
			this.state = state;
		}
		
		public DataOrigin getDataOrigin() {
			return origin;
		}

		public void setDataOrigin(DataOrigin origin) {
			this.origin = origin;
		}
		
		public DataState getDataState() {
			return state;
		}
		
		public void setDataState(DataState state) {
			this.state = state;
		}
		
		public Object getValue() {
			return value;
		}
		
		public void setValue(Object value) {
			this.value = value;
		}
		
	}

}

