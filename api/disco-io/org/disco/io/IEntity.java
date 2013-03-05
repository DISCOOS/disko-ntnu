package org.disco.io;

import java.util.List;

public interface IEntity {

	public Object getCue();
	public Object getIdentity();	
	public void setIdentity(Object identity);	
	public boolean isIdentified();
	
	public void addPacket(IPacket packet);
	public void removePacket(IPacket packet);
	public List<IPacket> getPackets();
	public int getPacketCount();
	
}
