package org.disco.io.serial;

import gnu.io.SerialPort;

import java.io.IOException;
import java.util.List;
import java.util.TooManyListenersException;

import org.disco.io.CommPortIdentifier;
import org.disco.io.ICommand;
import org.disco.io.IOManager;
import org.disco.io.IParser;
import org.disco.io.IResponse;
import org.disco.io.NoSuchPortException;
import org.disco.io.PortInUseException;
import org.disco.io.Protocol;
import org.disco.io.Session;
import org.disco.io.UnsupportedCommOperationException;
 
public class SerialSession extends Session {

	protected SerialLink link;
	
	public SerialSession(String name, IParser parser, String token) throws IllegalStateException {
		// forward
		super(name,token,parser);
	}

	public String getType() {
		return "Serial";
	}
		
	public List<CommPortIdentifier> getSerialPortIdentifiers(boolean all) throws NoSuchPortException {
        
		return IOManager.getSerialPortIdentifiers(all);
		
	}
	
	public boolean open(String port, 
			int baudRate, int dataBits, 
			int stopBits, int parity, 
			int flowCtrl) 
	throws NoSuchPortException, PortInUseException, 
		UnsupportedCommOperationException, IOException, 
		TooManyListenersException {

        // forward 
        if(getLink().open(port, baudRate, dataBits, 
        		stopBits, parity, flowCtrl)) {        	
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
	
	public boolean close() {
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
	
	public SerialLink getLink() {
		if(link==null) {
			link = new SerialLink();
		}
		return link;
	}
		
	public String Response(String command) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected final static String OPEN_USAGE = "usage: open " +
		"-port name -baudrate number -databits 5|6|7|8 -stopbits a|b|c " +
		"-parity n|e|o|m|s -flowctrl n|x|h";
	protected final static String[] OPEN_ARGS = new String[]{
		"port","baudrate","databits","stopbits","parity","flowctrl"};
	
	protected final static String SET_USAGE = "usage: set -param name -value value";
	protected final static String[] SET_ARGS = new String[]{"param","value"};

	protected final static String GET_USAGE = "usage: set -param name";
	protected final static String[] GET_ARGS = new String[]{"param","value"};

	protected IResponse execute(ICommand cmd) { 
		
		// initialize
		int i = 0;
		String[] args = null;
		String[] flags = null;
		
		try {
			
			// parse command
			if(cmd.equals("open")) {
				// get arguments
				args = OPEN_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// get arguments
					flags = cmd.getFlags(args);
					// translate
					String port = flags[0]; i++;
					int baudRate = Integer.valueOf(flags[1]); i++;
					int dataBits = Integer.valueOf(parseDataBits(flags[2])); i++;
					int stopBits = Integer.valueOf(parseStopBits(flags[3])); i++;
					int parity = Integer.valueOf(parseParity(flags[4])); i++;
					int flowCtrl = Integer.valueOf(parseFlowCtrl(flags[5])); i++;
					// execute
					return createResponse(cmd.getCommand(),open(port, 
							baudRate, dataBits, 
							stopBits, parity, flowCtrl),false);					
				}
				// notify usage
				return createResponse(cmd.getCommand(),false, "arguments missing", OPEN_USAGE,false);
			}
			else if(cmd.equals("close")) {
				// execute
				return createResponse(cmd.getCommand(),close(),"already closed",false);
			}
			else if(cmd.equals("set")) {
				// get arguments
				args = SET_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// get arguments
					flags = cmd.getFlags(args);
					// get name
					String name = flags[0]; i++;
					// translate
					if("baudRate".equalsIgnoreCase(name)) {
						int baudRate = Integer.valueOf(flags[1]); i++;
						getLink().setSerialPortParams(
								baudRate, 
								getLink().getDataBits(), 
								getLink().getStopBits(), 
								getLink().getParity());
					}
					else if("dataBits".equalsIgnoreCase(name)) {
						int dataBits = parseDataBits(flags[1]); i++;
						getLink().setSerialPortParams(
								getLink().getBaudRate(), 
								dataBits, 
								getLink().getStopBits(), 
								getLink().getParity());
					}
					else if("stopBits".equalsIgnoreCase(name)) {
						int stopBits = parseStopBits(flags[1]); i++;
						getLink().setSerialPortParams(
								getLink().getBaudRate(), 
								getLink().getDataBits(), 
								stopBits, 
								getLink().getParity());
					}
					else if("parity".equalsIgnoreCase(name)) {
						int parity = parseParity(flags[1]); i++;
						getLink().setSerialPortParams(
								getLink().getBaudRate(), 
								getLink().getDataBits(), 
								getLink().getStopBits(), 
								parity);
					}
					else if("flowCtrl".equalsIgnoreCase(name)) {
						int flowCtrl = parseFlowCtrl(flags[1]); i++;
						getLink().setFlowControlMode(flowCtrl);
					}					
					else {
						// notify usage
						return createResponse(cmd.getCommand(),false, "parameter missing", name + " is unknown",false);						
					}
					// success
					return createResponse(cmd.getCommand(),true,false);
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
					if("baudRate".equalsIgnoreCase(name)) {
						param = getLink().getBaudRate();
					}
					else if("dataBits".equalsIgnoreCase(name)) {
						param = getLink().getDataBits();
					}
					else if("stopBits".equalsIgnoreCase(name)) {
						param = getLink().getStopBits();
					}
					else if("parity".equalsIgnoreCase(name)) {
						param = getLink().getParity();
					}
					else if("flowCtrl".equalsIgnoreCase(name)) {
						param = getLink().getFlowControlMode();
					}					
					else {
						// notify error
						return createResponse(cmd.getCommand(),false, "Parameter missing", name + " is unknown",false);						
					}
					// success
					return createResponse(cmd.getCommand(),(param!=null ? param : "not set"),false);
				}
				// notify error
				return createResponse(cmd.getCommand(),false, "arguments missing", SET_USAGE,false);
			}		
			else if(isTransmitMode()) {
				// transmit command to session
				return createResponse(cmd.getCommand(),
						transmit(cmd.getCommand()),
						"link not open or empty message",
						"TX: " +cmd.getCommand(),"no operation",
						"unexpected error, see application log",false);
			}
			
			// not allowed
			return createResponse(cmd.getCommand(), false,"not allowed in command mode", "no operation", false);
			
			
		} catch (Throwable e) {
			boolean isUnexpected = !isArgumentCause(e,i,args);
			return createResponse(cmd.getCommand(),e,(!isUnexpected?args[i]:"unknown"),isUnexpected);
		}
		
	}

	//	 5|6|7|8	
	protected int parseDataBits(String value) throws IllegalArgumentException {
		char c = value.charAt(0);
		switch(c) {
		case '5': return SerialPort.DATABITS_5;
		case '6': return SerialPort.DATABITS_6;
		case '7': return SerialPort.DATABITS_7;
		case '8': return SerialPort.DATABITS_8;
		}
		throw new IllegalArgumentException("Invalid dataBits flag");
	}

	// a|b|c
	protected int parseStopBits(String value) throws IllegalArgumentException {
		char c = value.charAt(0);
		switch(c) {
		case 'a': return SerialPort.STOPBITS_1;
		case 'b': return SerialPort.STOPBITS_1_5;
		case 'c': return SerialPort.STOPBITS_2;
		}
		throw new IllegalArgumentException("Invalid stopBits flag");
	}
	
	// n|e|o|m|s
	protected int parseParity(String value) throws IllegalArgumentException {
		char c = value.charAt(0);
		switch(c) {
		case 'n': return SerialPort.PARITY_NONE;
		case 'e': return SerialPort.PARITY_EVEN;
		case 'o': return SerialPort.PARITY_ODD;
		case 'm': return SerialPort.PARITY_MARK;
		case 's': return SerialPort.PARITY_SPACE;
		}
		throw new IllegalArgumentException("Invalid parity flag");
	}
	
	// n|x|h
	protected int parseFlowCtrl(String value) throws IllegalArgumentException {
		char c = value.charAt(0);
		switch(c) {
		case 'n': return SerialPort.FLOWCONTROL_NONE;
		case 'x': return SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT;
		case 'h': return SerialPort.FLOWCONTROL_RTSCTS_IN + SerialPort.FLOWCONTROL_RTSCTS_OUT;
		}
		throw new IllegalArgumentException("Invalid flowCtrl flag");
	}

}

