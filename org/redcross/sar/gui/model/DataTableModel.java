package org.redcross.sar.gui.model;

import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.redcross.sar.data.AbstractDataModel;
import org.redcross.sar.data.IDataBinderIf;
import org.redcross.sar.data.IDataIf;
import org.redcross.sar.data.IDataModelIf;
import org.redcross.sar.data.IDataSourceIf;

public abstract class DataTableModel<S,T extends IDataIf> extends DiskoTableModel
							implements IDataModelIf<S,T> {

	private static final long serialVersionUID = 1L;

	protected AbstractDataModel<S, T> impl;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public DataTableModel(Class<T> c) {
		// forward
		super();
		// forward
		impl = createDataModel(0,c);
		impl.addChangeListener(listener);
	}

	public DataTableModel(Class<T> c, String[] names, String[] captions) {
		// forward
		this(c,names,captions,captions.clone(),
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	public DataTableModel(Class<T> c, String[] names, String[] captions, String[] tooltips) {
		// forward
		this(c,names,captions,tooltips,
				defaultEditable(names.length),
				defaultEditors(names.length,"button"));
	}

	public DataTableModel(Class<T> c, String[] names,
						   String[] captions,
						   String[] tooltips,
						   Boolean[] editable,
						   String[] editors) {
		// forward
		super(names,captions,tooltips,editable,editors);

		// forward
		impl = createDataModel(names!=null ? names.length : 0,c);
		impl.addChangeListener(listener);

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
		if(impl!=null) impl.removeChangeListener(listener);

		// forward
		super.create(names, captions, tooltips, editable, editors,false);

		// create new data model
		impl = createDataModel(getColumnCount(),getDataClass());
		impl.addChangeListener(listener);

		// notify
		fireTableStructureChanged();
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

	public IDataBinderIf<S,? extends IDataIf,?> getBinder(IDataSourceIf<?> source) {
		return impl.getBinder(source);
	}

	public Collection<IDataBinderIf<S,? extends IDataIf,?>> getBinders() {
		return impl.getBinders();
	}

	public boolean connect(IDataBinderIf<S,? extends IDataIf,?> binder) {
		return impl.connect(binder);
	}

	public boolean disconnect(IDataBinderIf<S,? extends IDataIf,?> binder) {
		return impl.disconnect(binder);
	}

	public boolean disconnectAll() {
		return impl.disconnectAll();
	}

	public void load() {
		impl.load();
	}

	public void load(Collection<T> list) {
		impl.load(list);
	}

	public void addAll(Collection<T> list) {
		impl.addAll(list);
	}

	public void add(S id, T obj) {
		impl.add(id,obj);
	}

	public void update(S id, T obj) {
		impl.update(id,obj);
	}

	public void remove(S id) {
		impl.remove(id);
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

	public T getObject(int row) {
		return impl.getObject(row);
	}

	public Collection<T> getObjects() {
		return impl.getObjects();
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

	public void addChangeListener(ChangeListener listener) {
		impl.addChangeListener(listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		impl.removeChangeListener(listener);
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
	 * @param S id - The affected row id. If <code>null</code>, all rows are removed.
	 * @param boolean finalize - If <code>true</code>, object references to data should be set to <code>null</code>
	 */
	protected void cleanup(S id, boolean finalize) { /*NOP*/ }


	protected S[] translate(IDataIf[] data) { return null; }

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private AbstractDataModel<S, T> createDataModel(int size, Class<T> c) {
		AbstractDataModel<S, T> data = new AbstractDataModel<S, T>(size,c) {

			protected Object[] create(S id, T obj, int size) {
				return DataTableModel.this.create(id, obj, size);
			}

			protected Object[] update(S id, T obj, Object[] data) {
				return DataTableModel.this.update(id, obj, data);
			}

			protected S[] translate(IDataIf[] data) {
				return DataTableModel.this.translate(data);
			}

			public void cleanup(S id, boolean finalize) {
				DataTableModel.this.cleanup(id,finalize);
			}

		};
		return data;
	}

	/* =============================================================================
	 * Anonymous classes
	 * ============================================================================= */

	private final ChangeListener listener = new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
			fireTableDataChanged();
		}

	};

}

