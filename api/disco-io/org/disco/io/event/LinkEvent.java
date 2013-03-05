package org.disco.io.event;

import java.util.EventObject;

import org.disco.io.ILink;

public class LinkEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private byte b;
	
	public LinkEvent(ILink source, byte b) {
		// forward
		super(source);
		// prepare
		this.b = b;
	}
		
	@Override
	public ILink getSource() {
		return (ILink)super.getSource();
	}

	public byte getByte() {
		return b;
	}


}
