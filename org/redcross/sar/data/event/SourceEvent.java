package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataSourceIf;

public class SourceEvent<I> extends EventObject {

	private static final long serialVersionUID = 1L;

	private I info;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public SourceEvent(IDataSourceIf<I> source, I info) {
		// forward
		super(source);
		// prepare
		this.info = info;
	}

	/* ===========================================================
	 * Public methods
	 * =========================================================== */

	@Override
	@SuppressWarnings("unchecked")
	public IDataSourceIf<I> getSource() {
		return (IDataSourceIf<I>)super.getSource();
	}

	public I getInformation() {
		return info;
	}

}
