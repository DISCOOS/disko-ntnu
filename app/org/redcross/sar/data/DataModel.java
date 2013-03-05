package org.redcross.sar.data;

/**
 * This class implements a generic IDataModel template 
 * using the AbstractDataModel class. Use this class to create 
 * fully functional IDataModel instances only given the 
 * ID class and data class types. For example, if a IDataModel instance
 * where the ID class type is Integer, and the data class type is
 * Object, the following code is sufficient:</p>
 * {@code IDataModel<Integer,Object> model = new DataModel<Integer,Object>();}</p>
 * 
 * @author kenneth
 *
 * @param <S> - The ID class type
 * @param <T> - The data class type
 */
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
	protected IRow create(S id, T obj, int size) { return AbstractDataModel.createRow(size); }

	/**
	 * Is fired when IDataModel.update(S id, T obj, Row data) is called.
	 *
	 * @param S id - The updated row id
	 * @param T obj - The updated data object
	 * @param IRow data - the row data to update 
	 */
	protected IRow update(S id, T obj, IRow data) { return data; };

	/**
	 * Is fired when IDataModel.remove(S id) and onDataClearAll() is called internally.
	 *
	 * @param S id - The affected row id. If <code>null</code>, all rows are removed.
	 * @param boolean finalize - If <code>true</code>, object references to data should be set to <code>null</code>
	 */
	protected void cleanup(S id,boolean finalize) { return; };

}

