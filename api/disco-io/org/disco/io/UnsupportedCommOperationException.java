package org.disco.io;

public class UnsupportedCommOperationException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	private Throwable e;
	
	// Constructs an UnsupportedCommOperationException with no detail message.
	public UnsupportedCommOperationException() {
		super();
	}
    
	// Constructs an UnsupportedCommOperationException with the specified detail message.
	public UnsupportedCommOperationException(String message) {
		super(message);
	}
	
	// Constructs an UnsupportedCommOperationException from gnu.io.UnsupportedCommOperationException
	public UnsupportedCommOperationException(gnu.io.UnsupportedCommOperationException e) {
		super(e.getMessage());
		this.e = e; 
		initCause(e.getCause());
		setStackTrace(e.getStackTrace());
	}
		
	@Override
	public String getLocalizedMessage() {
		return e.getLocalizedMessage();
	}
	
	
	
	
}
