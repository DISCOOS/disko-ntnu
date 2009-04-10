package org.redcross.sar.data.event;

import java.util.EventListener;

public interface ISourceListener<I> extends EventListener {

	public void onSourceChanged(SourceEvent<I> e);

}
