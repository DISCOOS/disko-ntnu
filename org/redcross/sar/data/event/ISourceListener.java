package org.redcross.sar.data.event;

import java.util.EventListener;

/**
 * 
 * @author Administrator
 *
 * @param <I> - the class or interface that implements the source event information
 */
public interface ISourceListener<I> extends EventListener {

	public void onSourceChanged(SourceEvent<I> e);

}
