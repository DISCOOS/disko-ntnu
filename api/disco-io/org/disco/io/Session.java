package org.disco.io;

import javax.swing.event.EventListenerList;

import org.disco.io.ISession;
import org.disco.io.event.IProtocolListener;
import org.disco.io.event.ISessionListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;

public abstract class Session implements ISession {

	private String name;
	private String mode = "command";
	private CommandParser cmd;

	protected IParser parser;
	protected IProtocol protocol;
	protected EventListenerList listeners = new EventListenerList();

	protected IProtocolListener listener = new IProtocolListener() {

		public void onReceive(ProtocolEvent e) {
			fireOnReceive(e);
		}

		public void onTransmit(ProtocolEvent e) {
			fireOnTransmit(e);
		}

		@Override
		public void onBufferOverflow(ProtocolEvent e) {
			fireOnBufferOverflow(e);			
		}

	};	

	/* ==============================================================
	 * Constructors
	 * ============================================================== */
	
	public Session(String name, String token, IParser parser) {
		this.name = name;
		this.parser = parser;
		this.cmd = new CommandParser(token);
	}

	/* ==============================================================
	 * Public methods
	 * ============================================================== */
	
	public IParser getParser() {
		return parser;
	}

	public abstract String getType();
	public abstract ILink getLink();	

	public String getMode() {
		return mode;
	}
	
	public boolean isCommandMode() {
		return "command".equalsIgnoreCase(mode);
	}
	
	public String setCommandMode() {
		mode = "command";
		return mode;
	}
	
	public boolean isTransmitMode() {
		return "transmit".equalsIgnoreCase(mode);
	}
	
	public String setTransmitMode() {
		mode = "transmit";
		return mode;
	}
		
	public String getName() {
		return name;
	}

	public IProtocol getProtocol() {
		return protocol;
	}	

	public boolean isOpen() {
		return getLink().isOpen();
	}

	public boolean transmit(String message) {

		if(isOpen() && !(message.isEmpty() || message.length()==0)) {
			return getProtocol().transmit(message);
		}				
		return false;

	}	

	public void addSessionListener(ISessionListener listener) {
		listeners.add(ISessionListener.class, listener);		
	}

	public void removeSessionListener(ISessionListener listener) {
		listeners.remove(ISessionListener.class, listener);				
	}

	public IResponse execute(String command) {

		// initialize
		int i = 0;
		String[] args = null;
		//String[] flags = null;
		
		// parse command
		ICommand cmd = this.cmd.parse(command);
				
		try {
			
			// parse
			if(cmd.equals("transmit")) {	
				// transmit command to session
				return createResponse(cmd.getCommand(),transmit(cmd.getCommand()),"Operation discarded","link not open or empty message",false);
			}
			else if(cmd.equals("changemode")) {
				// get mode
				String flag = cmd.getFlag("mode");
				// set current session?
				if("command".equalsIgnoreCase(flag)) {
					// apply mode
					setCommandMode();							
					// notify user
					return createResponse(command, true,false);					
				} 
				else if("transmit".equalsIgnoreCase(flag)) {
					// apply mode
					setTransmitMode();
					// notify user
					return createResponse(command, true,false);					

				}
				// notify user
				return createResponse(command,false,"invalid mode flag","mode not changed",false);																
			}
			else if(cmd.equals("status")) {
				// build status string
				String status = "open: " + getLink().getName()  + " | mode: " + getMode();
				// success
				return createResponse(cmd.getCommand(),(isOpen() ? status : "link not open"),false);
			} 			
						
			// forward
			return execute(cmd);
			
			
		} catch (Throwable e) {
			boolean isUnexpected = !isArgumentCause(e,i,args);
			return createResponse(cmd.getCommand(),e,(!isUnexpected?args[i]:"unknown"),isUnexpected);
		}		
		

	}

	/* ==============================================================
	 * Protected methods
	 * ============================================================== */
	
	protected void setProtocol(IProtocol protocol) {
		// unregister old?
		if(this.protocol!=null) 
			this.protocol.removeProtocolListener(listener);
		// update pointer
		this.protocol = protocol;
		// add listener?
		if(protocol!=null)
			protocol.addProtocolListener(listener);
	}

	protected void fireOnOpen() {

		// create event
		SessionEvent e = new SessionEvent(this,SessionEvent.EVENT_OPEN);

		// get listeners
		ISessionListener[] list = listeners.getListeners(ISessionListener.class);

		// notify listeners
		for(ISessionListener it:list) {
			it.onOpen(e);
		}

	}	

	protected void fireOnClose() {

		// create event
		SessionEvent e = new SessionEvent(this,SessionEvent.EVENT_CLOSE);

		// get listeners
		ISessionListener[] list = listeners.getListeners(ISessionListener.class);

		// notify listeners
		for(ISessionListener it:list) {
			it.onClose(e);
		}

	}	

	protected void fireOnReceive(ProtocolEvent e) {

		// get listeners
		ISessionListener[] list = listeners.getListeners(ISessionListener.class);

		// notify listeners
		for(ISessionListener it:list) {
			it.onReceive(this,e);
		}

	}

	protected void fireOnTransmit(ProtocolEvent e) {

		// get listeners
		ISessionListener[] list = listeners.getListeners(ISessionListener.class);

		// notify listeners
		for(ISessionListener it:list) {
			it.onTransmit(this,e);
		}

	}	
	
	protected void fireOnBufferOverflow(ProtocolEvent e) {

		// get listeners
		ISessionListener[] list = listeners.getListeners(ISessionListener.class);

		// notify listeners
		for(ISessionListener it:list) {
			it.onBufferOverflow(this,e);
		}

	}	
	
	protected abstract IResponse execute(ICommand cmd);
	
	protected Response createResponse(String command, Object value, boolean isUnexpected) {
		return createResponse(command, value,"cause unknown",isUnexpected);
	}

	protected Response createResponse(String command, Object value, String cause, boolean isUnexpected) {
		return createResponse(command, value,cause,"no operation",isUnexpected);
	}
	
	protected Response createResponse(String command, Object value, String cause, String fMsg, boolean isUnexpected) {
		return createResponse(command, value,cause,"OK",fMsg,"unexpected error, see application log",isUnexpected);
	}
	
	protected Response createResponse(String command, Object value, String cause, String sMsg, String fMsg, String uMsg, boolean isUnexpected) {
		return new Response(command, this,value,cause,sMsg,fMsg,uMsg,isUnexpected);
	}
	
	protected static boolean isBoolean(Object value) {
		return value instanceof Boolean;
	}

	protected static boolean isString(Object value) {
		return value instanceof String;
	}

	protected static boolean isException(Object value) {
		return value instanceof String;
	}
	
	protected static boolean isArgumentCause(Object value, int index, String[] args) {

		return args!=null && index < args.length && 
			   (value instanceof IllegalArgumentException ||
				value instanceof NumberFormatException || 
				value instanceof NoSuchPortException  || 
				value instanceof PortInUseException);	
	}		
	

}
