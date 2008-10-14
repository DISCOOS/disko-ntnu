package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataBinderIf;
import org.redcross.sar.data.IDataIf;

public class DataEvent<S> extends EventObject {

	private static final long serialVersionUID = 1L;

	private S[] idx;
	private IDataIf[] data;
	private int flags;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public DataEvent(IDataBinderIf<S,? extends IDataIf,?> source, S[] idx, IDataIf[] data, int flags) {
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
	public IDataBinderIf<S,? extends IDataIf,?> getSource() {
		return (IDataBinderIf<S,? extends IDataIf,?>)super.getSource();
	}

	public S[] getIdx() {
		return idx;
	}

	public IDataIf[] getData() {
		return data;
	}

	public int getFlags() {
		return flags;
	}

}
