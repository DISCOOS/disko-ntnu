package org.redcross.sar.map.event;

import java.util.EventObject;

public class MapElementEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private int type;
	
	public static int STATE_EVENT = 1;
	public static int CREATE_EVENT = 2;
	public static int UPDATE_EVENT = 3;
	
	public MapElementEvent(Object source, int type) {
		// forward
		super(source);
		// prepare
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isStateEvent() {
		return type == STATE_EVENT;
	}

	public boolean isCreateEvent() {
		return type == CREATE_EVENT;
	}
	
	public boolean isUpdateEvent() {
		return type == UPDATE_EVENT;
	}
	
}
