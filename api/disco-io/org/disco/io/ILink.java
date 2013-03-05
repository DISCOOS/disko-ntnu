package org.disco.io;

import org.disco.io.event.ILinkListener;

public interface ILink {

	public String getName();	
	public String getType();	
	public boolean isOpen();
	
	public boolean transmit(byte[] bytes);
	
	public void addLinkListener(ILinkListener listener);
	public void removeLinkListener(ILinkListener listener);
	
	
		
}
