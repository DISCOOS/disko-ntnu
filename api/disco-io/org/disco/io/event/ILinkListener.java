package org.disco.io.event;

import java.util.EventListener;

import org.disco.io.ILink;

public interface ILinkListener extends EventListener {

	public void onReceive(LinkEvent e);
	public void onStreamClosed(ILink link);
	
}
