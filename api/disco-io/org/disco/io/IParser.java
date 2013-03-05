package org.disco.io;

public interface IParser {
	
	// get parser name
	public String getName();
	
	// parse token
	public boolean parse(byte b);
	
	// parse incoming message
	public IPacket parseRX(String message);
	
	// parse outgoing message
	public IPacket parseTX(String message);
	
	// parse auto replay
	public IPacket parseAutoReply(IPacket packet);
	
    // get proper message as bytes
    public byte[] getMessage(String message);
    
    // get proper sub message as bytes
    public String getMessage(byte[] buffer, int len);
          
    // add auto-replay
    public String addAutoReplay(String message, String replay);
    
    // remove auto-replay
    public boolean removeAutoReplay(String message);
    
    // check if auto reply is registered
    public boolean repliesOn(String message);
    
    // return replied message if auto reply is registered
    public String getReply(String message);
    
    // search for message that replies this
    public String findMessage(String reply);
            
}
