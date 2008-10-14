package org.redcross.sar.data.event;

import java.util.EventListener;

public interface ISourceListenerIf<I> extends EventListener {

	public void onSourceChanged(SourceEvent<I> e);

}
