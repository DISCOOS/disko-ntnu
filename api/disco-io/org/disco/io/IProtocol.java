package org.disco.io;

import org.disco.io.event.IProtocolListener;

public interface IProtocol {  
    
    // protocol manager handles each received byte  
	public void onReceive(byte b) throws BufferOverflowException;

	// protocol manager handles sending messages
	public boolean transmit(String message);
	
    // get link
    public ILink getLink();
	
    // set link
    public void setLink(ILink link);
    
    // protocol manager handles broken stream  
    public void onStreamClosed();
    
    // protocol manager handles buffer overrun   
    public void onBufferOverflow();
      
    // add protocol listeners
    public void addProtocolListener(IProtocolListener listener);
    
    // remove protocol listeners
    public void removeProtocolListener(IProtocolListener listener);
    
    // get parser
    public IParser getParser();
    
    // set parser
    public void setParser(IParser parser);
    
    // get protocol name
    public String getName();
    
}  