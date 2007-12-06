package org.redcross.sar.event;

import java.util.EventObject;

public class MsoLayerEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum MsoLayerEventType {
		SELECTION_CHANGED_EVENT
	}
	
	protected MsoLayerEventType eventType;
	
	public MsoLayerEvent(Object source, MsoLayerEventType type) {
		super(source);
		eventType = type;
	}
	
	public MsoLayerEventType getEventType() {
		return eventType;
	}
}
