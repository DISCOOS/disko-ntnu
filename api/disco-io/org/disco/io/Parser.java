package org.disco.io;

import java.util.HashMap;
import java.util.Map;

public abstract class Parser implements IParser {

	protected String name;
	protected Map<String, String> replies = new HashMap<String, String>();
    
	public abstract String getName();
	
	public IPacket parseRX(String message) {                		
		// can parse?
		if(message!=null && !message.isEmpty()) {
	        // parse
			String replay = replies.get(message);
			if(replay!=null && !replay.isEmpty()) { 
        		return createPacket("RX", message, false, true);
			}
		}
		return createPacket("RX", message, true, false);
	}
	
	public IPacket parseTX(String message) {
		return createPacket("TX", message, true, false);
	}
	
	public IPacket parseAutoReply(IPacket packet) {
		// get message
		String message = packet.getMessage();
		// can parse?
		if(message!=null && !message.isEmpty()) {
	        // parse
			String replay = replies.get(message);
			if(replay!=null && !replay.isEmpty()) {
        		return createPacket("TX",replay,false,false);
			}
		}
		return null;
	}
	
    public abstract byte[] getMessage(String message);
      
    public String getMessage(byte[] buffer, int len) {  
        return new String(buffer, 0, len);  
    }

	public String addAutoReplay(String message, String reply) {
		return replies.put(message, reply);		
	}

	public boolean removeAutoReplay(String message) {
		if(replies.containsKey(message)) {
			replies.remove(message);
			return true;
		}
		return false;	
	}

	public String getReply(String message) {
		return replies.get(message);
	}

	public boolean repliesOn(String message) {
		return replies.containsKey(message);
	}
	
	public String findMessage(String reply) {
		for(String message: replies.keySet()) {
			if(replies.get(message).compareTo(reply)==0)
				return message; 
		}
		return null;
	}
	
	public static IPacket createPacket(
			String type, String message, 
			boolean isUnknown, boolean autoReplyExists) {
		return new Packet(type,message,isUnknown,autoReplyExists);
	}
		
}
