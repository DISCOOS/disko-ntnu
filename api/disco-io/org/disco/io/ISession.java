package org.disco.io;

import org.disco.io.event.ISessionListener;

public interface ISession {
	
	public String getName();
	public String getType();
	public String getMode();
	
	public boolean isCommandMode();	
	public String setCommandMode();
	
	public boolean isTransmitMode();
	public String setTransmitMode();

	public ILink getLink();
	public IParser getParser();
	public IProtocol getProtocol();
	
	public boolean isOpen();
	public boolean close() throws Throwable;
	
	public boolean transmit(String message);	
	public void addSessionListener(ISessionListener listener);
	public void removeSessionListener(ISessionListener listener);
	
	public IResponse execute(String command);
		
}
