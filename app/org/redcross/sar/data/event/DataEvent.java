package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataModel;

public class DataEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public static final int ADDED_EVENT = 0;
	public static final int UPDATED_EVENT = 1;
	public static final int REMOVED_EVENT = 2;
	public static final int CLEAR_EVENT = 3;

	private int[] rows;
	private int type;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public DataEvent(IDataModel<?,?> source, int[] rows, int type) {
		// forward
		super(source);
		// prepare
		this.rows = rows;
		this.type = type;
	}

	/* ===========================================================
	 * Public methods
	 * =========================================================== */

	@Override
	@SuppressWarnings("unchecked")
	public IDataModel<?,?> getSource() {
		return (IDataModel<?,?>)super.getSource();
	}

	public int[] getRows() {
		return rows;
	}

	public int getType() {
		return type;
	}

}
