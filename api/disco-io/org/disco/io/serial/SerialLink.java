package org.disco.io.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import javax.swing.event.EventListenerList;

import org.disco.io.Link;
import org.disco.io.NoSuchPortException;
import org.disco.io.PortInUseException;
import org.disco.io.UnsupportedCommOperationException;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialLink extends Link {  

	public final static int DATABITS_5 = SerialPort.DATABITS_5;
	public final static int DATABITS_6 = SerialPort.DATABITS_6;
	public final static int DATABITS_7 = SerialPort.DATABITS_7;
	public final static int DATABITS_8 = SerialPort.DATABITS_8;
	public final static int FLOWCONTROL_NONE = SerialPort.FLOWCONTROL_NONE;
	public final static int FLOWCONTROL_RTSCTS_IN = SerialPort.FLOWCONTROL_RTSCTS_IN;
	public final static int FLOWCONTROL_RTSCTS_OUT = SerialPort.FLOWCONTROL_RTSCTS_OUT;
	public final static int FLOWCONTROL_XONXOFF_IN = SerialPort.FLOWCONTROL_XONXOFF_IN;
	public final static int FLOWCONTROL_XONXOFF_OUT = SerialPort.FLOWCONTROL_XONXOFF_OUT;
	public final static int PARITY_NONE = SerialPort.PARITY_NONE;
	public final static int PARITY_EVEN = SerialPort.PARITY_EVEN;
	public final static int PARITY_ODD = SerialPort.PARITY_ODD;
	public final static int PARITY_MARK = SerialPort.PARITY_MARK;
	public final static int PARITY_SPACE = SerialPort.PARITY_SPACE;
	public final static int STOPBITS_1 = SerialPort.STOPBITS_1;
	public final static int STOPBITS_1_5 = SerialPort.STOPBITS_1_5;
	public final static int STOPBITS_2 = SerialPort.STOPBITS_2;
	
	protected SerialPort port;
	
    protected EventListenerList listeners = new EventListenerList();
	
	protected SerialLink() {
		super("Serial");
	}
    
	public String getName() {
		return (name!=null ? name.replaceAll("//./", "") : null);
	}
	
	public boolean isOpen()  {
		return port!=null;
	}
	
	protected SerialPort getPort() throws IllegalStateException {
		if(port==null) {
			throw new IllegalStateException("Port is closed");
		}
		return port;
	}
	
	protected boolean open(String port, 
			int baudRate, int dataBits, 
			int stopBits, int parity, int flowCtrl) 
	
	throws IOException, NoSuchPortException, PortInUseException, 
		UnsupportedCommOperationException, TooManyListenersException  {
		
		// forward
		if(isOpen()) close();
				
		try {

			// valid port name?
			if(!(port.isEmpty() || port.length()==0)) {

				CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
			
				// try to open
				CommPort commPort =  portIdentifier.open("org.disco.io", 2000);
				
				// is of type SerialPort?
				if (commPort instanceof SerialPort)
	            {
	                
					// setup
					SerialPort serialPort = (SerialPort) commPort;
				    serialPort.setSerialPortParams(baudRate,dataBits,stopBits,parity);					    
				    serialPort.setFlowControlMode(flowCtrl);
					this.port = serialPort;
					this.name = serialPort.getName();
					this.in = serialPort.getInputStream();
					serialPort.addEventListener(listener);
					serialPort.notifyOnDataAvailable(true);
					this.out = serialPort.getOutputStream();					    
				    // success
				    return true;
	           
	            }										
			}
		}
		catch (gnu.io.NoSuchPortException e) {
			throw new NoSuchPortException(e, "Port " + port + " does not exist");
		} catch (gnu.io.PortInUseException e) {
			try {
				throw new PortInUseException(e, "Port " + port + " is in use by " + CommPortIdentifier.getPortIdentifier(port).getCurrentOwner());
			} catch (gnu.io.NoSuchPortException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
		return false;
	}

	protected boolean close() throws IllegalStateException {
	    // close port
		getPort().close();
		port.removeEventListener();
		// notify
		fireOnStreamClosed();
		// cleanup
		in = null;
		out = null;			
		this.port = null;
		// finished
		return true;
	}
    
	private SerialPortEventListener listener = new SerialPortEventListener() {
		
		public void serialEvent(SerialPortEvent e) {

			switch(e.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				try {  

					int b; 
					
					Integer b1 = null; 
					Integer bN = null;
					
					int i = 0;
					byte[] readBuffer = new byte[1024];

					// if stream is not bound in.read() method returns -1  
					while((b = in.read()) != -1) {
						if(b1==null) b1 = b;
						bN = b;
						readBuffer[i] = (new Integer(b)).byteValue();
						fireOnReceive((byte) b);
					}  
					
					System.out.println("Start: " + 
							Byte.toString(b1.byteValue()) + " Stop: " 
							+ Byte.toString(bN.byteValue()));
					
				} catch (IOException ex) {  
					ex.printStackTrace();  
				}   		
			}
		}		
	};
	
	/* ==================================================================
	 * SerialPort wrapper
	 * ==================================================================*/
		
	public int getBaudBase() throws IllegalStateException, UnsupportedCommOperationException, IOException {
		try {
			return getPort().getBaudBase();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public int getBaudRate() throws IllegalStateException {
		return getPort().getBaudRate();
	}

	public boolean getCallOutHangup() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().getCallOutHangup();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public int getDataBits() throws IllegalStateException {
		return getPort().getDataBits();
	}

	public int getDivisor() throws IllegalStateException, UnsupportedCommOperationException, IOException {
		try {
			return getPort().getDivisor();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public byte getEndOfInputChar() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().getEndOfInputChar();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public int getFlowControlMode() throws IllegalStateException {
		return getPort().getFlowControlMode();
	}

	public boolean getLowLatency() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().getLowLatency();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public int getParity() throws IllegalStateException {
		return getPort().getParity();
	}
	
	public byte getParityErrorChar() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().getParityErrorChar();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public int getStopBits() throws IllegalStateException {
		return getPort().getStopBits();
	}
	
	public String getUARTType() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().getUARTType();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public boolean isCD() throws IllegalStateException {
		return getPort().isCD();
	}
	
	public boolean isCTS() throws IllegalStateException {
		return getPort().isCTS();
	}

	public boolean isDSR() throws IllegalStateException {
		return getPort().isDSR();
	}
	
	public boolean isDTR() throws IllegalStateException {
		return getPort().isDTR();
	}
	
	public boolean isRI() throws IllegalStateException {
		return getPort().isRI();
	}
	
	public boolean isRTS() throws IllegalStateException {
		return getPort().isRTS();
	}

	public void notifyOnBreakInterrupt(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnBreakInterrupt(isEnabled);
	}

	public void notifyOnCarrierDetect(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnCarrierDetect(isEnabled);
	}
	
	public void notifyOnCTS(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnCTS(isEnabled);		
	}

	public void notifyOnDataAvailable(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnDataAvailable(isEnabled);		
	}
	
	public void notifyOnDSR(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnDSR(isEnabled);		
	}

	public void notifyOnFramingError(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnFramingError(isEnabled);				
	}

	public void notifyOnOutputEmpty(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnOutputEmpty(isEnabled);				
	}

	public void notifyOnOverrunError(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnOverrunError(isEnabled);						
	}

	public void notifyOnParityError(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnParityError(isEnabled);						
	}
	
	public void notifyOnRingIndicator(boolean isEnabled) throws IllegalStateException {
		getPort().notifyOnRingIndicator(isEnabled);						
	}
	
	public void sendBreak(int millis) throws IllegalStateException {
		getPort().sendBreak(millis);								
	}

	public boolean setBaudBase(int base) throws IllegalStateException, UnsupportedCommOperationException, IOException {
		try {
			return getPort().setBaudBase(base);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}								
	}
	
	public boolean setCallOutHangup(boolean isEnabled) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().setCallOutHangup(isEnabled);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public boolean setDivisor(int divisor) throws IllegalStateException, UnsupportedCommOperationException, IOException {
		try {
			return getPort().setDivisor(divisor);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public void setDTR(boolean isEnabled) throws IllegalStateException {
		getPort().setDTR(isEnabled);
	}

	public boolean setEndOfInputChar(byte mark) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().setEndOfInputChar(mark);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public void setFlowControlMode(int mode) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			getPort().setFlowControlMode(mode);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}		
	}
	
	public boolean setLowLatency() throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().setLowLatency();
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public boolean setParityErrorChar(byte mark) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().setParityErrorChar(mark);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public void setRTS(boolean isEnabled) throws IllegalStateException {
		getPort().setRTS(isEnabled);
	}
	
	public void setSerialPortParams(int baudRate, int dataBits, int stopBits, int parity) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			getPort().setSerialPortParams(baudRate, dataBits, stopBits, parity);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}
	
	public boolean setUARTType(String type, boolean test) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			return getPort().setUARTType(type, test);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}
	}

	public void disableReceiveFraming() throws IllegalStateException {
		getPort().disableReceiveFraming();
	}
	
	public void disableReceiveThreshold() throws IllegalStateException {
		getPort().disableReceiveThreshold();
	}
	
	public void disableReceiveTimeout() throws IllegalStateException {
		getPort().disableReceiveTimeout();
	}

	public void enableReceiveFraming(int framingByte) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			getPort().enableReceiveFraming(framingByte);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}		
	}

	public void enableReceiveThreshold(int thresholdBytes) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			getPort().enableReceiveThreshold(thresholdBytes);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}		
	}

	public void enableReceiveTimeout(int timeoutMillis) throws IllegalStateException, UnsupportedCommOperationException {
		try {
			getPort().enableReceiveTimeout(timeoutMillis);
		} catch (gnu.io.UnsupportedCommOperationException e) {
			throw new UnsupportedCommOperationException(e);
		}		
	}

	public int getInputBufferSize() throws IllegalStateException {
		return getPort().getInputBufferSize();
	}
	
	public InputStream getInputStream() throws IllegalStateException, IOException {
		return getPort().getInputStream();
	}
	
	public int getOutputBufferSize() throws IllegalStateException {
		return getPort().getOutputBufferSize();
	}

	public OutputStream getOutputStream() throws IllegalStateException, IOException {
		return getPort().getOutputStream();
	}
	
	public int getReceiveFramingByte() throws IllegalStateException {
		return getPort().getReceiveFramingByte();
	}
	
	public int getReceiveThreshold() throws IllegalStateException {
		return getPort().getReceiveThreshold();
	}
	
	public int getReceiveTimeout() throws IllegalStateException {
		return getPort().getReceiveTimeout();
	}

	public boolean isReceiveFramingEnabled() throws IllegalStateException {
		return getPort().isReceiveFramingEnabled();
	}
	
	public boolean isReceiveThresholdEnabled() throws IllegalStateException {
		return getPort().isReceiveThresholdEnabled();
	}
	
	public boolean isReceiveTimeoutEnabled() throws IllegalStateException {
		return isReceiveTimeoutEnabled();
	}
	
	public void setInputBufferSize(int size) throws IllegalStateException {
		getPort().setInputBufferSize(size);		
	}
	
	public void setOutputBufferSize(int size) throws IllegalStateException {
		getPort().setInputBufferSize(size);				
	}

	
	

	
}  
