package org.disco.io;

import javax.swing.event.EventListenerList;

import org.disco.io.event.ILinkListener;
import org.disco.io.event.IProtocolListener;
import org.disco.io.event.LinkEvent;
import org.disco.io.event.ProtocolEvent;

public class Protocol implements IProtocol {
	   
	protected ILink link;
    protected int tail = 0;      
    protected byte[] buffer;
    
    protected IParser parser;
    protected EventListenerList listeners = new EventListenerList();
    
    public Protocol(ILink link, IParser parser, int bufferSize) {
    	setLink(link);
    	setParser(parser);
        buffer = new byte[bufferSize];
    }
      
	public String getName() {
		return parser.getName();
	}
	
    public void onReceive(byte b) throws BufferOverflowException{  
        // parse received byte
        if (parser.parse(b)) {  
            onMessage();
        } else {  
    		try {
				buffer[tail] = b;
				tail++;
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new BufferOverflowException(
						"Receive buffer overflown (max " + 
						buffer.length + " bytes)");
			}  
        }  
    }  
   
    public boolean transmit(String message) {  
        // forward to parser
    	IPacket packet = parser.parseTX(message);
		// send replay
		if(link.transmit(parser.getMessage(packet.getMessage()))) {
			fireOnTransmit(packet);
			return true;
    	}
        return false;
    }  
    
    public void onStreamClosed() {  
        onMessage();  
    }  
       
    public void onBufferOverflow() {
    	// is overflow?
        if (tail>=buffer.length) {  
	        // constructing message  
	        String message = parser.getMessage(buffer, tail-1);
	        // reset buffer
	        tail = 0;
	        // create packet
	        IPacket packet = new Packet("Overflow",message,true,false);
	        // notify
	        fireOnBufferOverflow(packet);
        }
    }
    
    
    /* 
     * When message is recognized onMessage is invoked  
     */  
    protected void onMessage() {  
    	// has data in buffer?
        if (tail!=0) {          	
        	// constructing message  
            String message = parser.getMessage(buffer, tail);
            // reset buffer
            tail = 0;
    		// forward to parser
    		IPacket packet = parser.parseRX(message);
    		// is complete and not a duplicate?
    		if(packet!=null && !(packet.isIncomplete() || packet.isDuplicate())) {
	    		// notify
	    		fireOnReceive(packet);
	    		// has a auto replay?
				if(packet.autoReplyExists()) {
	    			// parse to replay
	    			packet = parser.parseAutoReply(packet);
	    			// send replay
	    			if(link.transmit(parser.getMessage(packet.getMessage()))) {
	    				fireOnTransmit(packet);
	    			}    			
	    		}
    		} 
        } 
    }  
        
    protected void fireOnReceive(IPacket packet) {
        
    	// create event
    	ProtocolEvent e = new ProtocolEvent(this,packet,ProtocolEvent.RX_EVENT);
    	
    	// get listeners
    	IProtocolListener[] list = listeners.getListeners(IProtocolListener.class);
    	
    	// notify listeners
    	for(IProtocolListener it:list) {
    		it.onReceive(e);
    	}
    	
    }

    protected void fireOnTransmit(IPacket packet) {
        
    	// create event
    	ProtocolEvent e = new ProtocolEvent(this,packet,ProtocolEvent.TX_EVENT);
    	
    	// get listeners
    	IProtocolListener[] list = listeners.getListeners(IProtocolListener.class);
    	
    	// notify listeners
    	for(IProtocolListener it:list) {
    		it.onTransmit(e);
    	}
    	
    }
    
    protected void fireOnBufferOverflow(IPacket packet) {
        
    	// create event
    	ProtocolEvent e = new ProtocolEvent(this,packet,ProtocolEvent.OVERFLOW_EVENT);
    	
    	// get listeners
    	IProtocolListener[] list = listeners.getListeners(IProtocolListener.class);
    	
    	// notify listeners
    	for(IProtocolListener it:list) {
    		it.onBufferOverflow(e);
    	}
    	
    }
    
    
	public void addProtocolListener(IProtocolListener listener) {
		listeners.add(IProtocolListener.class, listener);		
	}

	public void removeProtocolListener(IProtocolListener listener) {
		listeners.remove(IProtocolListener.class, listener);				
	}

	public IParser getParser() {
		return parser;
	}
	
	public void setParser(IParser parser) {
		this.parser = parser;
	}
	
	public ILink getLink() {  
		return link;
	}  
	
	public void setLink(ILink link) {
		// unregister?
		if(this.link!=null)
			this.link.removeLinkListener(listener);
		this.link = link;
		if(link!=null)
			link.addLinkListener(listener);
	}
	
	protected ILinkListener listener = new ILinkListener() {

		public void onReceive(LinkEvent e) {
			// forward
			try {
				Protocol.this.onReceive(e.getByte());
			} catch (BufferOverflowException ex) {
				onBufferOverflow();
			}
		}

		public void onStreamClosed(ILink link) {
			// forward
			Protocol.this.onStreamClosed();			
		}
		
		
	};
	
}
