package org.disco.io;

public class NoSuchPortException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public NoSuchPortException(gnu.io.NoSuchPortException e, String message) {
		super();
		this.message = message;
		initCause(e.getCause());
		setStackTrace(e.getStackTrace());
	}
		
	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getLocalizedMessage() {
		return message;
	}	

}
