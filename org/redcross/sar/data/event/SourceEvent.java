package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataSource;

/**
 * 
 * @author Administrator
 *
 * @param <I> - the class or interface that implements the source event information
 */
public class SourceEvent<I> extends EventObject {

	private static final long serialVersionUID = 1L;

	private I info;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public SourceEvent(IDataSource<I> source, I info) {
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
	public IDataSource<I> getSource() {
		return (IDataSource<I>)super.getSource();
	}

	public I getInformation() {
		return info;
	}

}
