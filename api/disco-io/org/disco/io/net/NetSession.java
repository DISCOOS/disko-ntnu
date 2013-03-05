package org.disco.io.net;

import java.io.IOException;

import org.disco.io.ICommand;
import org.disco.io.IParser;
import org.disco.io.IResponse;
import org.disco.io.Protocol;
import org.disco.io.Session;

public class NetSession extends Session {

	private String login;
	private String username;
	private String password;
	private String[] commands;

	private SocketLink link;
		
	public NetSession(String name, IParser parser, String token, String login) {	
		// forward
		super(name,token,parser);
		// save login string
		this.login = login;
	}

	@Override
	public SocketLink getLink() {
		if(link==null) {
			link = new SocketLink();
		}
		return (SocketLink)link;
	}
	
	public boolean isLoginReady() {
		return getLink().isLoginREADY();
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String[] getCommands() {
		return commands;
	}
	
	public boolean login(String username, String password, String... command) {
		if(isOpen() && getLink().isLoginREADY()) {
			String cmd = String.format(login,username,password,command);
			if(getLink().transmit(getParser().getMessage(cmd))) {
				getLink().setLoginPROGRESS();
				this.username = username;
				this.password = password;
				this.commands = command;
				return true;
			}
		}
		return false;
	}
	
	public boolean open(String host, int port) throws IOException, InterruptedException {

        // forward 
        if(getLink().open(host,port)) {
        	
        	// create protocol
			setProtocol(new Protocol(getLink(),getParser(),1024));        	
        	// notify
        	fireOnOpen();
        	// success
        	return true;
        }
        // failure
        return false;   
	}
	
	public String getType() {
		return "NET";
	}

	public boolean close() throws IllegalStateException, IOException, InterruptedException {
        // destroy current link from port
		if(getLink().isOpen()) {
			// remove protocol
			setProtocol(null);
    		// forward
			getLink().close();
        	// notify
        	fireOnClose();
        	// finished
        	return true;
        }		
		// failed
		return false;
	}	

	protected final static String OPEN_USAGE = "usage: open -host name -port number";
	protected final static String[] OPEN_ARGS = new String[]{"host","port"};

	@Override
	protected IResponse execute(ICommand cmd) { 
		
		// initialize
		int i = 0;
		String[] args = null;
		String[] flags = null;
		
		try {
			// parse command
			if(cmd.equals("open")) {
				// get arguments
				flags = OPEN_ARGS;
				// validate parameters
				if(cmd.containsAll(flags)) {
					// get arguments
					flags = cmd.getFlags(flags);
					// translate
					String host = flags[0]; i++;
					int port =  Integer.valueOf(flags[1]); i++;
					// execute
					return createResponse(cmd.getCommand(),open(host,port),false);					
				}
				// notify usage
				return createResponse(cmd.getCommand(),false, "arguments missing", OPEN_USAGE,false);			
			}
			
			// forward
			return createResponse("No operation",false,"no command found",false);
			
		} catch (Throwable e) {
			boolean isUnexpected = !isArgumentCause(e,i,args);
			return createResponse(cmd.getCommand(),e,(!isUnexpected?args[i]:"unknown"),isUnexpected);
		}
	
	}
	
}
