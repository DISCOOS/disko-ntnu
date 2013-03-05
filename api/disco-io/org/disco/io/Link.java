package org.disco.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.event.EventListenerList;

import org.disco.io.event.ILinkListener;
import org.disco.io.event.LinkEvent;

public abstract class Link implements ILink {  

	protected String name;
	protected String type;
	protected InputStream in;  
	protected OutputStream out;  
	
    protected EventListenerList listeners = new EventListenerList();
	
    protected Link(String type) {
    	this.type = type;
    }
    	
	public abstract String getName();	
	public abstract boolean isOpen();
	
	public String getType() {
		return type;
	}
	
    public boolean transmit(byte[] bytes) {
    	
    	if(out!=null) {
    	
	        try {
	        	
	            // sending through serial port is simply writing into OutputStream  
	            out.write(bytes);  
	            out.flush();
	            
	            // success
	            return true;
	            
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }
	        
    	}
    	
        // failure
        return true;
    	
    }  
    
    protected void fireOnReceive(byte b) {
        
    	// create event
    	LinkEvent e = new LinkEvent(this,b);
    	
    	// get listeners
    	ILinkListener[] list = listeners.getListeners(ILinkListener.class);
    	
    	// notify listeners
    	for(ILinkListener it:list) {
    		it.onReceive(e);
    	}
    	
    }
    
    protected void fireOnStreamClosed() {
        
    	// get listeners
    	ILinkListener[] list = listeners.getListeners(ILinkListener.class);
    	
    	// notify listeners
    	for(ILinkListener it:list) {
    		it.onStreamClosed(this);
    	}
    	
    }
    
	public void addLinkListener(ILinkListener listener) {
		listeners.add(ILinkListener.class, listener);		
	}

	public void removeLinkListener(ILinkListener listener) {
		listeners.remove(ILinkListener.class, listener);				
	}
	
}  
