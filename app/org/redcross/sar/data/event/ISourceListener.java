package org.redcross.sar.data.event;

import java.util.EventListener;

/**
 * 
 * @author Administrator
 *
 * @param <D> - the class or interface that implements the source event information
 */
public interface ISourceListener<D> extends EventListener {

	public void onSourceChanged(SourceEvent<D> e);

}
