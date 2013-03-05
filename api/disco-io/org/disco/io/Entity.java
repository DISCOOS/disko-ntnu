package org.disco.io;

import java.util.ArrayList;
import java.util.List;

import org.disco.io.IEntity;
import org.disco.io.IPacket;

public abstract class Entity implements IEntity {
	
	protected String ssid;
	protected Object identity;
	protected List<IPacket> packets = new ArrayList<IPacket>();
	
	public abstract Object getCue();
	
	public Object getIdentity() {
		return identity;
	}
	
	public void setIdentity(Object identity) {
		this.identity = identity;
	}
	
	public boolean isIdentified() {
		return (identity!=null);
	}	
	
	public void addPacket(IPacket packet) {
		if(!packets.contains(packet)) {
			packets.add(packet);			
		}		
	}

	public void removePacket(IPacket packet) {
		packets.remove(packet);
	}

	public IPacket getPacket(int index) {
		return packets.get(index);
	}
	
	public int getPacketCount() {
		return packets.size();
	}
	
	public List<IPacket> getPackets() {
		List<IPacket> items = new ArrayList<IPacket>();
		items.addAll(packets);
		return items;
	}
	
}
