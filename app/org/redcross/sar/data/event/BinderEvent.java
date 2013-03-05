package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.IData;

public class BinderEvent<S> extends EventObject {

	private static final long serialVersionUID = 1L;

	private S[] idx;
	private IData[] data;
	private int flags;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public BinderEvent(IDataBinder<S,? extends IData,?> source, S[] idx, IData[] data, int flags) {
		// forward
		super(source);
		// prepare
		this.idx = idx;
		this.data = data;
		this.flags = flags;
	}

	/* ===========================================================
	 * Public methods
	 * =========================================================== */

	@Override
	@SuppressWarnings("unchecked")
	public IDataBinder<S,? extends IData,?> getSource() {
		return (IDataBinder<S,? extends IData,?>)super.getSource();
	}

	public S[] getIdx() {
		return idx;
	}

	public IData[] getData() {
		return data;
	}

	public int getFlags() {
		return flags;
	}

}
