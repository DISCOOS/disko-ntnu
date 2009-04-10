package org.redcross.sar.data;

public class DataModel<S,T extends IData> extends AbstractDataModel<S, T> {

	private static final long serialVersionUID = 1L;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public DataModel(Class<T> c) {
		// forward
		super(1,c);
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
	protected Object[] update(S id, T obj, Object[] data) { return data; };

	/**
	 * Is fired when IDataModel.remove(S id) and onDataClearAll() is called internally.
	 *
	 * @param S id - The affected row id. If <code>null</code>, all rows are removed.
	 * @param boolean finalize - If <code>true</code>, object references to data should be set to <code>null</code>
	 */
	protected void cleanup(S id,boolean finalize) { return; };

}

