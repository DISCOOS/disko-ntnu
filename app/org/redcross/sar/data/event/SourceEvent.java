package org.redcross.sar.data.event;

import java.util.EventObject;

import org.redcross.sar.data.IDataSource;

/**
 * 
 * @author Administrator
 *
 * @param <D> - the class or interface that implements the source event data
 */
public class SourceEvent<D> extends EventObject {

	private static final long serialVersionUID = 1L;

	private D data;

	/* ===========================================================
	 * Constructors
	 * =========================================================== */

	public SourceEvent(IDataSource<D> source, D data) {
		// forward
		super(source);
		// prepare
		this.data = data;
	}

	/* ===========================================================
	 * Public methods
	 * =========================================================== */

	@Override
	@SuppressWarnings("unchecked")
	public IDataSource<D> getSource() {
		return (IDataSource<D>)super.getSource();
	}

	public D getData() {
		return data;
	}
	
}
