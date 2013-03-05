package org.disco.io.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import javax.swing.event.EventListenerList;

import org.disco.io.Link;

public class SocketLink extends Link {  
	
	protected Socket socket;
	protected SocketReader rin;
	protected Thread tin;
	
	protected static final int LOGIN_NO = 0; 
	protected static final int LOGIN_READY = 1; 
	protected static final int LOGIN_PROGRESS = 2; 
	protected static final int LOGIN_YES = 2; 
	
	private int loginState = LOGIN_NO;
	
    protected EventListenerList listeners = new EventListenerList();
	
	protected SocketLink() {
		super("Socket");
	}
    
	public String getName() {
		return name;
	}
	
	public boolean isLoginNO() {
		return (loginState == LOGIN_READY);
	}
			
	protected void setLoginNO() {
		this.loginState = LOGIN_NO;
	}
	
	public boolean isLoginREADY() {
		return (loginState == LOGIN_READY);
	}
		
	protected void setLoginREADY() {
		this.loginState = LOGIN_READY;
	}
	
	public boolean isLoginProgress() {
		return (loginState == LOGIN_PROGRESS);
	}
	
	protected void setLoginPROGRESS() {
		this.loginState = LOGIN_PROGRESS;
	}
	
	public boolean isLoginYES() {
		return (loginState == LOGIN_YES);
	}
	
	protected void setLoginYES() {
		this.loginState = LOGIN_YES;
	}
	
	public boolean isOpen()  {
		return socket!=null ? socket.isConnected() && !socket.isClosed() : false;
	}
	
	protected Socket getSocket() throws IllegalStateException {
		if(socket==null) {
			throw new IllegalStateException("Socket is closed");
		}
		return socket;
	}
	
	protected boolean open(String host, int port) throws IOException, IllegalStateException, InterruptedException {
		
		// forward
		if(isOpen()) close();
				
		// valid port name?
		if(!(host.isEmpty() || host.length()==0)) {

			// set login state
			setLoginNO();
			
			// forward
			socket = new Socket(host, port);
			
			// setup
			this.name = host + ":" + port;
			this.in = socket.getInputStream();
			this.out = socket.getOutputStream();
			
			// start listening
			rin = new SocketReader(in);
			tin = new Thread(rin);
			tin.start();
			
		    // success
		    return true;
		}
		// failure
		return false;
	}

	protected boolean close() throws IllegalStateException, IOException, InterruptedException {
		// is connected?
		if(isOpen()) {
			// terminate reader thread?
			if(tin.isAlive()) 
			{
				tin.interrupt();
				rin.shutdown();
				tin.join(5000);
			}
		    // close socket
			getSocket().close();
			// notify
			fireOnStreamClosed();
			// cleanup
			in = null;
			out = null;			
			rin = null;
			tin = null;
			socket = null;
			setLoginNO();
			// finished
			return true;
		}
		return false;
	}
	
	@Override
    protected void fireOnReceive(byte b) {
        
		/* ======================================================
		 * Telnet login process
		 * 1. set isLoginNO() state
		 * 2. Connect socket to server
		 * 3. (Any) server response -> set isLoginReady() state 
		 * 4. Send login command
		 * 5. set isLoginPROGRESS();
		 * 6. (any) server response --> set isLoginYES() state
		 * ====================================================== */
		
		// set login ready state?
		if(isLoginNO()) setLoginREADY();
		else if(isLoginREADY()) setLoginPROGRESS(); 
		else if(isLoginProgress()) setLoginYES(); 
		
    	// forward
		super.fireOnReceive(b);
    	
    }
	
    
	private class SocketReader implements Runnable 
    {
        InputStream in;
        
        boolean isRunning = true;
        
        public SocketReader (InputStream in)
        {
            this.in = in;
        }
        
        public void run ()
        {

            try
            {
				int b; 
				
				// if stream is not bound in.read() method returns -1  
				while(isRunning && (b = in.read()) != -1) {
					fireOnReceive((byte) b);
				}  
				
            }
            catch ( IOException e )
            {            	
                e.printStackTrace();
            	try {
                	// do cleanup
					close();
				} catch (IllegalStateException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }            
        }
        
        public void shutdown() {
        	isRunning = false;
        }
    }		
	
	/* ==================================================================
	 * Socket wrapper
	 * ==================================================================*/
		
}  

