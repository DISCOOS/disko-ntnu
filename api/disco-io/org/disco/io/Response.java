package org.disco.io;

import java.io.IOException;
import java.util.EventObject;
import java.util.TooManyListenersException;

public class Response extends EventObject implements IResponse {
	
	private static final long serialVersionUID = 1L;
	
	String command;
	Object value;
	String cause;
	String sMsg;
	String fMsg;
	String uMsg;
	boolean isUnexpected;

	protected Response(String command, Object source, Object value, String cause, String sMsg, String fMsg, String uMsg, boolean isUnexpected) {
		super(source);
		this.command = command;
		this.value = value;
		this.cause = cause;
		this.sMsg = sMsg;
		this.fMsg = fMsg;
		this.uMsg = uMsg;
		this.isUnexpected = isUnexpected;
	}
	
	public boolean isBoolean() {
		return isBoolean(value);
	}

	public boolean isString() {
		return isString(value);
	}

	public boolean isException() {
		return value instanceof Throwable;
	}
	
	public boolean isArgumentCause() {

		return (value instanceof NumberFormatException || 
				value instanceof NoSuchPortException  || 
				value instanceof PortInUseException);	
	}		
	
	public boolean isUnexpected() {
		return isUnexpected;
	}
	
	public void throwExecption() throws Throwable {
		if(isException()) {
			throw (Throwable)value;
		}
	}

	public String getCommand() {
		return command;
	}
	
	public Object getValue() {
		return value;
	}
	
	public String getString() {
		if(isString())
			return (String)value;
		return null;
	}
	
	public boolean isBatch() {
		return (value instanceof IResponse[]);
	}
	
	
	public Boolean getBoolean() {
		if(isBoolean())
			return (Boolean)value;
		return null;
	}
	
	public Throwable getException() {
		if(isException())
			return (Throwable)value;
		return null;
	}
	
	public String translate() {
		
		// translate
		if(isString())
			return (String)value;
		else if(isBoolean())
			return ((Boolean)value).booleanValue() ? sMsg : fMsg + " ("+cause+")";
		else if(isBatch()) {
			// prepare
			IResponse[] items = (IResponse[])value;  
			String lines = sMsg + "{" + items[0].translate();
			// loop over all
			for(int i=1; i<items.length; i++) {
				lines = lines.concat(", " + items[i].translate());
			}
			return lines.concat("}");
		}
		else if(isException()) {
			Throwable e = (Throwable)value;
			if (e instanceof IllegalArgumentException) {
				return "Illegal argument found: " +e.getMessage();
			} else if (e instanceof ArrayIndexOutOfBoundsException) {
				return "The too few arguments";
			} else if (e instanceof NumberFormatException) {
				return "Argument " + cause + " has a wrong number format";
			} else if (e instanceof IllegalStateException) {
				return "IllegalStateException: " + e.getMessage();
			} else if (e instanceof UnsupportedCommOperationException) {
				return "UnsupportedCommOperationException: " + e.getMessage();
			} else if (e instanceof IOException) {
				return "IOException: " + e.getMessage();
			} else if (e instanceof TooManyListenersException) {
				return "TooManyListenersException: " + e.getMessage();
			} else if (e instanceof NoSuchPortException) {
				return "Port " + cause + " do not exist";
			} else if (e instanceof PortInUseException) {
				return e.getMessage(); // "Port " + cause + " is in use by " + IOManager.getPortIdentifier(cause).getCurrentOwner();
			} else {
				return getStackTrace();
			}

		}
		// failed to translate
		return uMsg;
	}		
	
	public String getStackTrace() {
		Throwable e = getException();
		if(e!=null) {
		    final StringBuilder result = new StringBuilder();
		    result.append(e.toString());
		    result.append(": ");
		    result.append("Unexpected error, see application log (" + e.getMessage() + ")");
		    String NEW_LINE = System.getProperty("line.separator");
		    result.append(NEW_LINE);

		    //add each element of the stack trace
		    for (StackTraceElement element : e.getStackTrace() ){
		      result.append( "\t" + element );
		      result.append( NEW_LINE );
		    }
		    return result.toString();
		}
		return null;
	  }
	
	public static boolean isBoolean(Object value) {
		return value instanceof Boolean;
	}

	public static boolean isString(Object value) {
		return value instanceof String;
	}

	public static boolean isException(Object value) {
		return value instanceof String;
	}
	
	public static boolean isArgumentCause(Object value, int index, String[] args) {

		return args!=null && index < args.length && 
			   (value instanceof IllegalArgumentException ||
				value instanceof NumberFormatException || 
				value instanceof NoSuchPortException  || 
				value instanceof PortInUseException);	
	}			
	
}
