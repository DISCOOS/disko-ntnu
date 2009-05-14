package org.redcross.sar.gui.model;

import java.util.Collection;
import java.util.Comparator;

import javax.swing.SwingUtilities;

import org.redcross.sar.data.AbstractDataModel;
import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.IDataModel;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.data.ITranslator;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.event.DataEvent;
import org.redcross.sar.data.event.IDataListener;

public abstract class AbstractDataTableModel<S,T extends IData> extends DiskoTableModel
							implements IDataModel<S,T> {

	private static final long serialVersionUID = 1L;

	protected AbstractDataModel<S, T> impl;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractDataTableModel(Class<T> c) {
		// forward
		super();
		// forward
		impl = createDataModel(0,c);
		impl.addDataListener(listener);
	}

	public AbstractDataTableModel(Class<T> c, String[] names, String[] captions) {
		// forward
		this(c,names,captions,captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	public AbstractDataTableModel(Class<T> c, String[] names, String[] captions, String[] tooltips) {
		// forward
		this(c,names,captions,tooltips,
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	public AbstractDataTableModel(Class<T> c, String[] names,
						   String[] captions,
						   String[] tooltips,
						   Boolean[] editable,
						   String[] editors) {
		// forward
		super(names,captions,tooltips,editable,editors);

		// forward
		impl = createDataModel(names!=null ? names.length : 0,c);
		impl.addDataListener(listener);

	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	@Override
	public void create(Object[] names, Object[] captions) {
		// use captions as tooltips
		this.create(names, captions, captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	@Override
	public void create(Object[] names, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors) {

		// uninstall
		if(impl!=null) impl.removeDataListener(listener);

		// forward
		super.create(names, captions, tooltips, editable, editors,false);

		// create new data model
		impl = createDataModel(getColumnCount(),getDataClass());
		impl.addDataListener(listener);

		// notify
		fireTableStructureChanged();

	}

	public boolean isAddToTail() {
		return impl.isAddToTail();
	}

	public void setAddToTail(boolean isAddToTail) {
		impl.setAddToTail(isAddToTail);
	}


	/* =============================================================================
	 * IDataModel implementation
	 * ============================================================================= */

	public Class<T> getDataClass() {
		return impl.getDataClass();
	}

	public boolean isSupported(Class<?> dataClass) {
		return impl.isSupported(dataClass);
	}

	public IDataBinder<S,? extends IData,?> getBinder(IDataSource<?> source) {
		return impl.getBinder(source);
	}

	public Collection<IDataBinder<S,? extends IData,?>> getBinders() {
		return impl.getBinders();
	}

	public boolean isConnected(IDataSource<?> source) {
		return impl.isConnected(source);
	}

	public boolean connect(IDataBinder<S,? extends IData,?> binder) {
		return impl.connect(binder);
	}

	public boolean disconnect(IDataBinder<S,? extends IData,?> binder) {
		return impl.disconnect(binder);
	}

	public boolean disconnectAll() {
		return impl.disconnectAll();
	}

	public int getAddOnCoUpdate() {
		return impl.getAddOnCoUpdate();
	}

	public void setAddOnCoUpdate(int flag) {
		impl.setAddOnCoUpdate(flag);
	}

	public int getRemoveOnCoUpdate() {
		return impl.getRemoveOnCoUpdate();
	}

	public void setRemoveOnCoUpdate(int flag) {
		impl.setRemoveOnCoUpdate(flag);
	}

	public void load() {
		impl.load();
	}

	public void load(Collection<T> list) {
		impl.load(list);
	}

	public void load(Collection<T> list, boolean append) {
		impl.load(list,append);
	}

	public int add(S id, T obj) {
		return impl.add(id,obj);
	}

	public int update(S id, T obj) {
		return impl.update(id,obj);
	}

	public int remove(S id) {
		return impl.remove(id);
	}

	public void clear() {
		impl.clear();
	}

	public int findRowFromId(S id) {
		return impl.findRowFromId(id);
	}

	public int findRowFromObject(T obj) {
		return impl.findRowFromObject(obj);
	}

	public S getId(int row) {
		return impl.getId(row);
	}

	public Collection<S> getIds() {
		return impl.getIds();
	}

	public Collection<S> getIds(Selector<S> selector, Comparator<S> comparator) {
		return impl.getIds(selector,comparator);
	}

	public T getObject(int row) {
		return impl.getObject(row);
	}

	public Collection<T> getObjects() {
		return impl.getObjects();
	}

	public Collection<T> getObjects(Selector<T> selector, Comparator<T> comparator) {
		return impl.getObjects(selector,comparator);
	}

	public Object[] getData(int row) {
		return impl.getData(row);
	}

	public int getRowCount() {
		return impl.getRowCount();
	}

	public Object getValueAt(int iRow, int iCol) {
		return impl.getValueAt(iRow,iCol);
	}

	public void setValueAt(Object value, int iRow, int iCol) {
		impl.setValueAt(value, iRow,iCol);
	}

	public void addDataListener(IDataListener listener) {
		impl.addDataListener(listener);
	}

	public void removeDataListener(IDataListener listener) {
		impl.removeDataListener(listener);
	}

	public ITranslator<S, IData> getTranslator() {
		return impl.getTranslator();
	}

	public void setTranslator(ITranslator<S, IData> translator) {
		impl.setTranslator(translator);
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
	protected Object[] create(S id, T obj, int size) { return new Object[size]; }

	/**
	 * Is fired when IDataModel.update(S id, T obj) is called.
	 *
	 * @param S id - The updated row id
	 * @param T obj - The updated data object
	 * @param int size - array of values to update
	 */
	protected abstract Object[] update(S id, T obj, Object[] data);

	/**
	 * Is fired when IDataModel.remove(S id) is called.
	 *
	 * @param S id - The affected row id. If <code>null</code>, all rows should be removed.
	 * @param boolean finalize - If <code>true</code>, all references to data should be set to <code>null</code>
	 */
	protected void cleanup(S id, boolean finalize) { /*NOP*/ }

	/**
	 * Implements default Data-To-ID translation. Override this is
	 * alternative translation is needed.
	 *
	 * @param IData[] data - the data to translate
	 * @return S[] - the ids translated from data
	 */
	@SuppressWarnings("unchecked")
	protected S[] translate(IData[] data) {
		ITranslator<S, IData> translator = impl.getTranslator();
		if(translator!=null) {
			return translator.translate(data);
		}
		return null;
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private AbstractDataModel<S, T> createDataModel(int size, Class<T> c) {
		AbstractDataModel<S, T> data = new AbstractDataModel<S, T>(size,c) {

			protected Object[] create(S id, T obj, int size) {
				return AbstractDataTableModel.this.create(id, obj, size);
			}

			protected Object[] update(S id, T obj, Object[] data) {
				return AbstractDataTableModel.this.update(id, obj, data);
			}

			protected S[] translate(IData[] data) {
				return AbstractDataTableModel.this.translate(data);
			}

			public void cleanup(S id, boolean finalize) {
				AbstractDataTableModel.this.cleanup(id,finalize);
			}

		};
		return data;
	}

	private int[] getIndexes(int[] rows) {
		if(rows==null) {
			int uBound = getRowCount()-1;
			if(uBound<0)
				return new int[]{-1,-1};
			else
				return new int[]{0,uBound};
		}
		if(rows.length<1) {
			return new int[]{-1,-1};
		}
		if(rows.length==1) {
			return new int[]{rows[0],rows[0]};
		}
		int uBound = rows.length-1;
		// search for maximum and minimum index values
		int min = Integer.MAX_VALUE;
		int max = -1;
		for(int i=0;i<=uBound;i++) {
			min = Math.min(rows[i], min);
			max = Math.max(rows[i], max);
		}
		return new int[]{min,max};
	}

	/* =============================================================================
	 * Anonymous classes
	 * ============================================================================= */

	private final IDataListener listener = new IDataListener() {

		public void onDataChanged(final DataEvent e) {

			// this should only be performed on EDT!
			if (SwingUtilities.isEventDispatchThread()) {

				int[] idx = getIndexes(e.getRows());
				try {
					// get row count
					int count = getRowCount();
					/* =====================================================
					 * Hack: Resolves a refresh problem in JTable.
					 * =====================================================
					 * Occurs when a single row is added to an empty model,
					 * or removed from an model with only one row.
					 * ===================================================== */
					if (idx[0] == -1 || idx[0] == idx[1] && count <= 1
							|| count == idx[1] - idx[0] + 1) {
						fireTableDataChanged();
					} else {
						switch (e.getType()) {
						case DataEvent.ADDED_EVENT:
							fireTableRowsInserted(idx[0], idx[idx.length - 1]);
							break;
						case DataEvent.UPDATED_EVENT:
							fireTableRowsUpdated(idx[0], idx[idx.length - 1]);
							break;
						case DataEvent.REMOVED_EVENT:
							fireTableRowsDeleted(idx[0], idx[idx.length - 1]);
							break;
						case DataEvent.CLEAR_EVENT:
						default:
							fireTableDataChanged();
							break;
						}
					}
				} catch (IndexOutOfBoundsException ex) {
					// HACK: Consume errors from DefaultRowSorter
					fireTableDataChanged();
					//ex.printStackTrace();
				} catch (RuntimeException ex) {
					ex.printStackTrace();
				}
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						onDataChanged(e);
					}
				});
			}
		}

	};

}

