package org.disco.io.serial;

import java.io.File;
import java.io.IOException;
import java.util.TooManyListenersException;

import org.disco.core.utils.Utils;
import org.disco.io.ICommand;
import org.disco.io.IParser;
import org.disco.io.IResponse;
import org.disco.io.NoSuchPortException;
import org.disco.io.PortInUseException;
import org.disco.io.Protocol;
import org.disco.io.UnsupportedCommOperationException;

public class TNCSession extends SerialSession {

	protected String configPath;
	
	public TNCSession(String name, IParser parser, String token, String configPath) {	
		// forward
		super(name,parser,token);
		// set configuration path
		this.configPath = configPath;
	}

	@Override
	public TNCLink getLink() {
		if(link==null) {
			link = new TNCLink(configPath);
		}
		return (TNCLink)link;
	}

	public String getDevice() {
		return getLink().getDevice();
	}
	
	public void setDevice(String name) {
		getLink().setDevice(name);
	}
		
	public String getHostMode() {
		return getLink().getHostMode();
	}
	
	public void setHostMode(String name) {
		getLink().setHostMode(name);
	}
	
	public String getConfigPath() {
		return configPath;
	}
	
	public void setConfigPath(String path) {
		configPath = path;
		getLink().setConfigPath(path);
	}
	
	public void initTNC() throws IllegalStateException {
		getLink().initTNC();
	}
	
	public void initTNC(String file) throws IllegalStateException {
		getLink().initTNC(file);
		
	}
	
	public void restoreTNC() throws IllegalStateException {
		getLink().restoreTNC();		
	}
	
	public void restoreTNC(String file) throws IllegalStateException {
		getLink().restoreTNC(file);
	}	
	
	public boolean open(
			String tnc, String port, 
			int baudRate, int dataBits, 
			int stopBits, int parity, 
			int flowCtrl, String hostMode, boolean initTNC) 
	throws NoSuchPortException, PortInUseException, 
		UnsupportedCommOperationException, IOException, 
		TooManyListenersException {

        // forward 
        if(getLink().open(port, baudRate, dataBits, 
        		stopBits, parity, flowCtrl)) {
        	
        	// create protocol
			setProtocol(new Protocol(getLink(),getParser(),1024));        	
        	// setup TNC
        	getLink().setDevice(tnc);
        	getLink().setHostMode(hostMode);
        	// initialize the TNC device?
        	if(initTNC) getLink().initTNC();
        	// notify
        	fireOnOpen();
        	// success
        	return true;
        }
        // failure
        return false;   
	}
	
	public boolean close(boolean restoreTNC) {
		// validate
		if(isOpen()) {
	    	// restore TNC?
			if(restoreTNC) getLink().restoreTNC();
			// forward
			return super.close();
		}
		// failure
		return false;
	}
	
	protected final static String OPEN_USAGE = "usage: open " +
			"-port name -tnctype name -baudrate number -databits 5|6|7|8 -stopbits a|b|c " +
			"-parity n|e|o|m|s -flowctrl n|x|h -hostmode name -inittnc y|n";
	protected final static String[] OPEN_ARGS = new String[]{
			"port","tnctype","baudrate","databits","stopbits","parity","flowctrl","hostmode","inittnc"};

	protected final static String INIT_USAGE = "usage: init [-filename name]";
	protected final static String[] INIT_ARGS = new String[]{"filename"};

	protected final static String RESTORE_USAGE = "usage: restore [-filename name]";
	protected final static String[] RESTORE_ARGS = new String[]{"filename"};
	
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
					String port = flags[0]; i++;
					String host = flags[1]; i++;
					int baudRate =  Integer.valueOf(flags[2]); i++;
					int dataBits = parseDataBits(flags[3]); i++;
					int stopBits = parseStopBits(flags[4]); i++;
					int parity = parseParity(flags[5]); i++;
					int flowCtrl = parseFlowCtrl(flags[6]); i++;
					String mode = flags[7]; i++;
					boolean init = parseInitTncFlag(flags[8]); i++;
					// execute
					return createResponse(cmd.getCommand(),
							open(host,port, baudRate, dataBits, 
									stopBits, parity, flowCtrl, 
									mode, init),false);					
				}
				// notify usage
				return createResponse(cmd.getCommand(),false, "arguments missing", OPEN_USAGE,false);
			}
			else if(cmd.equals("init")) {
				// get arguments
				args = INIT_ARGS;
				// validate parameters
				if(cmd.contains(args[0])) {
					// get arguments
					flags = cmd.getFlags(args);
					// validate file
					File file = new File(flags[0]); i++;
					if(file.exists() && file.isFile() 
							&& Utils.getExtension(file).equalsIgnoreCase("tnc")) {
						// forward
						initTNC(flags[0]);
						// notify usage
						return createResponse(cmd.getCommand(),true,"","TNC initialized","","",false);
					}					
					// notify usage
					return createResponse(cmd.getCommand(),false, "File not found: " + flags[0],"No operation", false);
				}
				// forward
				initTNC();
				// notify usage
				return createResponse(cmd.getCommand(),true,"","TNC configuration initialized","","",false);
			}				
			else if(cmd.equals("restore")) {
				// get arguments
				args = RESTORE_ARGS;
				// validate parameters
				if(cmd.contains(args[0])) {
					// get arguments
					flags = cmd.getFlags(args);
					// validate file
					File file = new File(flags[0]); i++;
					if(file.exists() && file.isFile() 
							&& Utils.getExtension(file).equalsIgnoreCase("tnc")) {
						// forward
						restoreTNC(flags[0]);
						// notify usage
						return createResponse(cmd.getCommand(),true,"","TNC configuration restored","","",false);
					}					
					// notify usage
					return createResponse(cmd.getCommand(),false, "File not found: " + flags[0], "No operation", false);
				}
				// forward
				restoreTNC();
				// notify usage
				return createResponse(cmd.getCommand(),true,"","TNC configuration restored","","",false);
			}
			else if(cmd.equals("setParam")) {
				// get arguments
				args = SET_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// get arguments
					flags = cmd.getFlags(args);
					// get name
					String name = flags[0]; i++;
					// translate
					if("tncType".equalsIgnoreCase(name)) {
						getLink().setDevice(flags[1]);
					}
					else if("hostMode".equalsIgnoreCase(name)) {
						getLink().setHostMode(flags[1]);
					}
					// forward
					return super.execute(cmd);
				}
				// notify usage
				return createResponse(cmd.getCommand(),false, "arguments missing", SET_USAGE,false);
			}			
			else if(cmd.equals("get")) {
				// get arguments
				args = GET_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// initialize 
					Object param = null;
					// get arguments
					flags = cmd.getFlags(args);
					// get name
					String name = flags[0]; i++;
					// translate
					if("tncType".equalsIgnoreCase(name)) {
						param = getLink().getDevice();
					}
					else if("hostMode".equalsIgnoreCase(name)) {
						param = getLink().getHostMode();
					}

					else {
						// notify error
						return createResponse(cmd.getCommand(),false, "parameter missing", name + " is unknown",false);						
					}
					// success
					return createResponse(cmd.getCommand(),(param!=null ? param : "not set"),false);
				}
				// notify error
				return createResponse(cmd.getCommand(),false, "arguments missing", SET_USAGE,false);			
			}
			
			// forward
			return super.execute(cmd);
			
		} catch (Throwable e) {
			boolean isUnexpected = !isArgumentCause(e,i,args);
			return createResponse(cmd.getCommand(),e,(!isUnexpected?args[i]:"unknown"),isUnexpected);
		}
	
	}
	
	// y|n
	protected boolean parseInitTncFlag(String value) throws IllegalArgumentException {
		char c = value.charAt(0);
		switch(c) {
		case 'y': return true;
		case 'n': return false;
		}
		throw new IllegalArgumentException("Invalid initTNC flag");
	}	
	
}
