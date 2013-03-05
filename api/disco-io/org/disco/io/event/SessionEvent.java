package org.disco.io.event;

import java.util.EventObject;

import org.disco.io.ISession;

public class SessionEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public static final int EVENT_OPEN = 1;
	public static final int EVENT_CLOSE = 2;
	
	private int type;
		
	public SessionEvent(ISession source, int type) {
		// forward
		super(source);
	}	
		
	@Override
	public ISession getSource() {
		return (ISession)super.getSource();
	}

	public int getType() {
		return type;
	}

}
