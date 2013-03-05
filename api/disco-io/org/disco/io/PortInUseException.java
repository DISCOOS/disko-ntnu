package org.disco.io;

public class PortInUseException extends Throwable {

	private static final long serialVersionUID = 1L;
	
	private String message;
	
	public PortInUseException(gnu.io.PortInUseException e, String message) {
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
