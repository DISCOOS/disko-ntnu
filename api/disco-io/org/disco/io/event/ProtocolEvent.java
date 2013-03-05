package org.disco.io.event;

import java.util.EventObject;

import org.disco.io.IPacket;
import org.disco.io.IProtocol;

public class ProtocolEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	public static final int RX_EVENT = 1;
	public static final int TX_EVENT = 2;
	public static final int OVERFLOW_EVENT = -1;
	
	private int type;
	private IPacket packet;
	
	public ProtocolEvent(IProtocol source, IPacket packet, int type) {
		// forward
		super(source);
		// prepare
		this.type = type;
		this.packet = packet;
	}
		
	@Override
	public IProtocol getSource() {
		return (IProtocol)super.getSource();
	}
	
	public IPacket getPacket() {
		return packet;
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isRX() {
		return type == RX_EVENT;
	}

	public boolean isTX() {
		return type == TX_EVENT;
	}

}
