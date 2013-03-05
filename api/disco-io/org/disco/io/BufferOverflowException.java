package org.disco.io;

public class BufferOverflowException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	// Constructs an BufferOverflowException with no detail message.
	public BufferOverflowException() {
		super();
	}
    
	// Constructs an BufferOverflowException with the specified detail message.
	public BufferOverflowException(String message) {
		super(message);
	}

}
