package org.disco.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.disco.core.utils.Utils;
import org.disco.io.event.EntityEvent;
import org.disco.io.event.IBrokerListener;
import org.disco.io.event.IManagerListener;
import org.disco.io.event.ISessionListener;
import org.disco.io.event.ProtocolEvent;
import org.disco.io.event.SessionEvent;

public final class IOManager {


	private static final String SESSION = "Current session: %1$s";

	private ISession currentSession;

	private final CommandParser cmd = new CommandParser("-");

	private EventAdapter listener = new EventAdapter();
	private EventListenerList listeners = new EventListenerList();
	private Map<String,ISession> sessions = new HashMap<String,ISession>();

	/*========================================================
	 * The singleton code
	 *========================================================*/

	private static IOManager m_this;

	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
	public static synchronized IOManager getInstance() {
		if (m_this == null) {
			// it's ok, we can call this constructor
			m_this = new IOManager();
		}
		return m_this;
	}

	static {
		System.loadLibrary("rxtxSerial");
		System.loadLibrary("rxtxParallel");
	}

	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
		// that'll teach 'em
	}

	/*========================================================
	 * Public methods
	 *========================================================*/

	public boolean addSession(ISession session) {
		if(!sessions.containsKey(session.getName())) {
			sessions.put(session.getName().toLowerCase(),session);
			session.addSessionListener(listener);
			if(session instanceof IBroker) {
				((IBroker<?>)session).addBrokerListener(listener);
			}
			fireOnSessionAdded(session);
			return true;
		}
		return false;
	}

	public boolean removeSession(ISession session) {
		return removeSession(session.getName());
	}

	public boolean removeSession(String name) {
		name = name.toLowerCase();
		if(sessions.containsKey(name.toLowerCase())) {
			ISession session = sessions.get(name);
			session.removeSessionListener(listener);
			if(session instanceof IBroker) {
				((IBroker<?>)session).removeBrokerListener(listener);
			}
			sessions.remove(name);
			fireOnSessionRemoved(session);
			return true;
		}
		return false;
	}

	public ISession getSession(String name) {
		return sessions.get(name.toLowerCase());
	}

	public Collection<ISession> getSessions() {
		return sessions.values();
	}

	public Collection<IBroker<?>> getBrokers() {
		Collection<IBroker<?>> brokers  = new ArrayList<IBroker<?>>();
		for(ISession it : sessions.values()) {
			if(it instanceof IBroker) {
				brokers.add((IBroker<?>)it);
			}
		}
		return brokers;
	}

	public void releaseAll()  {
		for(ISession it : sessions.values()) {
			removeSession(it);
		}
	}

	public int closeAll()  {
		int count = 0;
		for(ISession it : sessions.values()) {
			try {
				if(it.close()) count++;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	public static CommPortIdentifier getPortIdentifier(String name) throws NoSuchPortException {
		try {
			return CommPortIdentifier.getPortIdentifier(name);
		} catch (gnu.io.NoSuchPortException e) {
			throw new NoSuchPortException(e, "Port " + name + " does not exist");
		}                  
	}  	

	public static List<CommPortIdentifier> getPortIdentifiers(boolean all) throws NoSuchPortException {
		try {
			return CommPortIdentifier.getPortIdentifiers();
		} catch (gnu.io.NoSuchPortException e) {
			throw new NoSuchPortException(e, "No ports found");
		}                  
	}

	public static List<CommPortIdentifier> getSerialPortIdentifiers(boolean all) throws NoSuchPortException {

		try {
			List<CommPortIdentifier> serialPorts = new ArrayList<CommPortIdentifier>();

			for(CommPortIdentifier it : CommPortIdentifier.getPortIdentifiers()) {
				if(it.getPortType() == CommPortIdentifier.PORT_SERIAL) {
					if(all || !it.isCurrentlyOwned()) {
						serialPorts.add(it);	        		
					}
				}
			}

			return serialPorts;
		} catch (gnu.io.NoSuchPortException e) {
			throw new NoSuchPortException(e, "No ports found");
		}                  

	}

	public void addManagerListener(IManagerListener listener) {
		listeners.add(IManagerListener.class, listener);		
	}

	public void removeManagerListener(IManagerListener listener) {
		listeners.remove(IManagerListener.class, listener);				
	}

	public ISession getCurrentSession() {
		return currentSession;
	}

	public boolean setCurrentSession(String name) {
		ISession session = getSession(name);
		if(session!=null) {
			boolean bFlag = (currentSession!=session);  
			currentSession = session;
			if(bFlag)fireOnCurrentSessionChanged(session);
			return true;			
		}
		return false;
	}

	protected final static String OPEN_USAGE = "usage: open -session name +<session spesfic flags>";
	protected final static String[] OPEN_ARGS = new String[]{"session"};

	protected final static String SET_USAGE = "usage: set -param name -value value";
	protected final static String[] SET_ARGS = new String[]{"param","value"};

	protected final static String GET_USAGE = "usage: get -param name";
	protected final static String[] GET_ARGS = new String[]{"param"};

	protected final static String STATUS_USAGE = "usage: status [session]";
	protected final static String[] STATUS_ARGS = new String[]{"session"};

	protected final static String LIST_USAGE = "usage: list sessions|brokers";
	protected final static String[] LIST_ARGS = new String[]{"session","brokers"};

	protected final static String CHANGEMODE_USAGE = "usage: changemode -session name -mode command|transmit";
	protected final static String[] CHANGEMODE_ARGS = new String[]{"session","mode"};

	public IResponse execute(String command) { 

		// initialize
		int i = 0;
		String[] args = null;
		String[] flags = null;

		// parse command (syntax: name -arg1 flag -arg2 flag etc.
		ICommand cmd = this.cmd.parse(command);		

		try {

			/* =================================================
			 * command: mode (only available from IO manager)
			 * ================================================= */
			if(cmd.equals("changemode")) {
				// get arguments
				args = CHANGEMODE_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// get flags
					flags = cmd.getFlags(args); ++i;
					// get current session
					ISession session = getSession(flags[0]);++i;
					// session found?
					if(session!=null) {
						// execute
						return session.execute(command);
					}
					// notify user
					return createResponse(command, false,"session not found","Close failed",false);					
				}
				// notify usage
				return createResponse(command,false,"arguments missing",CHANGEMODE_USAGE,false);
			}			
			/* =================================================
			 * command: current
			 * ================================================= */
			else if(cmd.equals("current")) {
				if(currentSession!=null) {
					return createResponse(command, "current session: " + currentSession.getName(), false);					
				}
				return createResponse(command, "no session set", false);
			} 
			/* =================================================
			 * command: open
			 * ================================================= */
			else if(cmd.equals("open")) {
				// validate parameters
				if(cmd.contains("session")) {
					// get arguments
					String name = cmd.getFlag("session"); ++i;
					// get current session
					ISession session = getSession(name);
					if(session!=null) {
						return session.execute(command);
					}
					// notify user
					return createResponse(command, false,"session not found","Close failed",false);
				}
				// forward to current session
				else if(currentSession!=null) {
					// execute
					return currentSession.execute(command);
				}				
				// notify usage
				return createResponse(command, false, "arguments missing", OPEN_USAGE,false);

			} 
			/* =================================================
			 * command: close
			 * ================================================= */
			else if(cmd.equals("close")) {
				// validate parameters
				if(cmd.contains("all")) {
					int count = closeAll();
					return createResponse(command, count>0?count + " sessions closeed":false,"no sessions was connected","Close all failed", false);
				}
				else if(cmd.getArgs().size()==1) {
					// translate
					String name = cmd.getArgs().iterator().next(); i++;
					// get current session
					ISession session = getSession(name);
					if(session!=null) {
						return session.execute("close");
					}
					// notify user
					return createResponse(command, false,"session not found","Close failed",false);
				}
				// forward to current session
				else if(currentSession!=null) {
					// execute
					return currentSession.execute(command);
				}				

				// notify usage
				return createResponse(command, false, "arguments missing", STATUS_USAGE,false);

			} 
			/* =================================================
			 * command: status
			 * ================================================= */
			else if(cmd.equals("status")) {
				// get arguments
				args = STATUS_ARGS;
				// validate parameters
				if(cmd.contains("session")) {
					// get arguments
					String name = cmd.getFlag("session");
					// get current session
					ISession session = getSession(name);
					if(session!=null) {
						return session.execute(command);
					}
					// notify user
					return createResponse(command, false,"session not found","Not status available",false);					
				}
				else if(cmd.getArgs().size()==1) {
					// translate
					String name = cmd.getArgs().iterator().next(); i++;
					// get current session
					ISession session = getSession(name);
					if(session!=null) {
						return session.execute(command);
					}
					// notify user
					return createResponse(command, false,"session not found","Not status available",false);
				}
				// forward to current session
				else if(currentSession!=null) {
					// execute
					return currentSession.execute(command);
				}				
				// notify usage
				return createResponse(command, false, "arguments missing", STATUS_USAGE,false);				
			} 
			/* =================================================
			 * command: list
			 * ================================================= */
			else if(cmd.equals("list")) {				
				// validate parameters
				if(cmd.contains("sessions")) {
					// initialize
					String list = "{";
					// build list
					for(ISession it : getSessions()) {
						if(list.length()==1) list = list.concat(it.getName());
						else list = list.concat(", "+it.getName());
					}
					list = list.concat("}");
					// notify user
					return createResponse(command, list.length()>2?list:false,"no sessions found",list,false);
				}
				else if(cmd.contains("brokers")) {
					// initialize
					String list = "{";
					// build list
					for(ISession it : getBrokers()) {
						if(list.length()==1) list = list.concat(it.getName());
						else list = list.concat(", "+it.getName());
					}
					list = list.concat("}");
					// notify user
					return createResponse(command, list.length()>2?list:false,"no brokers found",list,false);

				}
				// forward to current session
				else if(currentSession!=null) {
					// execute
					return currentSession.execute(command);
				}				
				// notify usage
				return createResponse(command, false, "arguments missing", LIST_USAGE,false);
			} 
			/* =================================================
			 * command: set
			 * ================================================= */
			else if(cmd.equals("set")) {
				// get arguments
				args = SET_ARGS;
				// validate parameters
				if(cmd.contains("session")) {
					// translate
					String name = cmd.getFlag("session"); i++;
					// notify usage
					return createResponse(command, setCurrentSession(name), "not found", "set session failed", false);
				}
				// validate parameters
				else if(cmd.containsAll(args)) {
					// get arguments
					flags = cmd.getFlags(args);
					// translate
					String name = flags[0]; i++;
					// set current session?
					if("session".equalsIgnoreCase(name)) {
						// translate
						String flag = flags[1]; i++;
						// notify usage
						return createResponse(command, setCurrentSession(flag), "not found", "set session failed", false);
					}
					// forward to current session
					else if(currentSession!=null) {
						// execute
						return currentSession.execute(command);
					}
				}

				// notify usage
				return createResponse(command, false, "arguments missing", SET_USAGE,false);

			} 
			/* =================================================
			 * command: get
			 * ================================================= */
			else if(cmd.equals("get")) {
				// get arguments
				args = GET_ARGS;
				// validate parameters
				if(cmd.containsAll(args)) {
					// get arguments
					flags = cmd.getFlags(args);
					// translate
					String name = flags[0]; i++;
					// set current session?
					if("session".equalsIgnoreCase(name)) {
						// get current session
						ISession session = getCurrentSession();						
						// notify usage
						return createResponse(command, (session!=null?String.format(SESSION,session.getName()) : "no session set"),false);
					}
					// forward to current session
					else if(currentSession!=null) {
						// execute
						return currentSession.execute(command);
					}
				}

				// notify usage
				return createResponse(command, false, "arguments missing", GET_USAGE,false);

			} 
			/* =================================================
			 * command: batch
			 * ================================================= */
			else if(cmd.equals("batch")) {
				// validate parameters
				if(cmd.contains("filename")) {
					// get arguments
					String filename = cmd.getFlag("filename"); ++i;
					// validate file
					File file = new File(filename);
					if(file.exists() && file.isFile() 
							&& Utils.getExtension(file).equalsIgnoreCase("cmd")) {

						// import lines
						List<String> lines = Utils.readLines(filename,"#");

						// allocate memory
						IResponse[] r = new IResponse[lines.size()];

						// loop over all lines
						for(int j=0 ; j<lines.size(); j++) {
							// fetch line from list
							String line = lines.get(j);
							// discard?
							r[j] = execute(line);
						}

						// finished
						return createResponse(command, this, r, "", "Batch executed", "", "", false);

					}

					// notify user
					return createResponse(command, false,"no valid file found","batch failed",false);
				}

			} 

			/* =================================================
			 * command not recognized, forward to current session
			 * ================================================= */
			else if(currentSession!=null) {
				// execute
				return currentSession.execute(command);
			}

			// not allowed
			return createResponse(command, false,(command==null || command.isEmpty() ? "Empty command" : "Current session not set"), "No operation", false);

		} catch (Throwable e) {
			boolean isUnexpected = !Response.isArgumentCause(e,i,args);
			return createResponse(command, e,(!isUnexpected?args[i]:"unknown"),isUnexpected);
		}

	}

	protected Response createResponse(String command, Object value, boolean isUnexpected) {
		return createResponse(command, value,"cause unknown",isUnexpected);
	}

	protected Response createResponse(String command, Object value, String cause, boolean isUnexpected) {
		return createResponse(command, value,cause,"Failure",isUnexpected);
	}

	protected Response createResponse(String command, Object value, String cause, String fMsg, boolean isUnexpected) {
		return createResponse(command, this, value,cause,"OK",fMsg,"Unexpected error, see application log",isUnexpected);
	}

	protected Response createResponse(String command, Object source, Object value, String cause, String sMsg, String fMsg, String uMsg, boolean isUnexpected) {
		return new Response(command,source,value,cause,sMsg,fMsg,uMsg,isUnexpected);
	}		

	/* ==============================================================
	 * Public classes 
	 * ============================================================== */

	protected void fireOnReceive(ISession session,ProtocolEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onReceive(session,e);
		}

	}

	protected void fireOnTransmit(ISession session, ProtocolEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onTransmit(session,e);
		}

	}
	
	protected void fireOnBufferOverflow(ISession session, ProtocolEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onBufferOverflow(session,e);
		}

	}		

	protected void fireOnEntityDetected(IBroker<?> broker, EntityEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onEntityDetected(broker,e);
		}

	}  	

	protected void fireOnOpen(SessionEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onOpen(e);
		}

	}  	

	protected void fireOnClose(SessionEvent e) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onClose(e);
		}

	}  	

	protected void fireOnSessionAdded(ISession session) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onSessionAdded(session);
		}

	}

	protected void fireOnSessionRemoved(ISession session) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onSessionRemoved(session);
		}

	}	    

	protected void fireOnCurrentSessionChanged(ISession session) {

		// get listeners
		IManagerListener[] list = listeners.getListeners(IManagerListener.class);

		// notify listeners
		for(IManagerListener it:list) {
			it.onCurrentSessionChanged(session);
		}

	}	        

	private class EventAdapter implements ISessionListener, IBrokerListener {

		public void onReceive(ISession session, ProtocolEvent e) {
			fireOnReceive(session,e);
		}

		public void onTransmit(ISession session, ProtocolEvent e) {
			fireOnTransmit(session,e);
		}

		public void onBufferOverflow(ISession session, ProtocolEvent e) {
			fireOnBufferOverflow(session,e);
		}

		public void onEntityDetected(IBroker<?> broker, EntityEvent e) {
			fireOnEntityDetected(broker, e);			
		}

		public void onOpen(SessionEvent e) {
			fireOnOpen(e);			
		}

		public void onClose(SessionEvent e) {
			fireOnClose(e);			
		}

	};	


}
