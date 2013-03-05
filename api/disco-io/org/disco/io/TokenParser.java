package org.disco.io;

import org.disco.core.utils.Utils;

public class TokenParser extends Parser {

	protected String name;
	protected String tokens;
	protected String prefix;
	protected String postfix;
	
	public TokenParser(String name, String prefix, String postfix, String tokens) {
		this.name = name;
		this.prefix = (prefix!=null ? prefix : "");
		this.postfix = (postfix!=null ? postfix : "");
		this.tokens = (tokens!=null ? tokens : "");
	}
	
	public boolean parse(byte b) {	
		// convert to char
		char c = Utils.convert(b);
        // simple protocol: each message ends with one of these tokens 
        return tokens.contains(Character.valueOf(c).toString());
	}
	
    // helper methods   
    public byte[] getMessage(String message) {
    	if(message!=null) {
	    	message = message.replaceAll(prefix, "").replaceAll(postfix, "");
    	}
        return (prefix+message+postfix).getBytes();
    }

	@Override
	public String getName() {
		return "LineParser";
	}  
	
}
