package org.disco.io;

import gnu.io.NoSuchPortException;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.disco.io.event.CommPortOwnershipListener;

public class CommPortIdentifier {

	public static final int PORT_SERIAL = gnu.io.CommPortIdentifier.PORT_SERIAL;
	public static final int PORT_PARALLEL = gnu.io.CommPortIdentifier.PORT_PARALLEL;
	public static final int PORT_I2C = gnu.io.CommPortIdentifier.PORT_I2C;
	public static final int PORT_RS485 = gnu.io.CommPortIdentifier.PORT_RS485;
	public static final int PORT_RAW = gnu.io.CommPortIdentifier.PORT_RAW;
	
	private final gnu.io.CommPortIdentifier port;
		
	protected CommPortIdentifier(gnu.io.CommPortIdentifier port) {
		this.port = port;
	}
	
	public String getCurrentOwner() {
		return port.getCurrentOwner();
	}
	
	public String getName() {
		return port.getName();
	}
	
	public int getPortType() {
		return port.getPortType();
	}

	public synchronized boolean isCurrentlyOwned() {
		return port.isCurrentlyOwned();
	}
	
	public void addPortOwnershipListener(CommPortOwnershipListener listener) {
		port.addPortOwnershipListener(listener);
	}
	
	public void removePortOwnershipListener(CommPortOwnershipListener listener) {
		port.removePortOwnershipListener(listener);
	}
	
	public static CommPortIdentifier getPortIdentifier(String portName) throws NoSuchPortException {
		try {
			gnu.io.CommPortIdentifier identifier = gnu.io.CommPortIdentifier.getPortIdentifier(portName);		
			return identifier!=null ? new CommPortIdentifier(identifier) : null;
		} catch (gnu.io.NoSuchPortException e) {
			throw e;
		}
	}
	public static List<CommPortIdentifier> getPortIdentifiers() throws NoSuchPortException {
		Enumeration<?> enumeration = gnu.io.CommPortIdentifier.getPortIdentifiers();
		List<CommPortIdentifier> identifiers = new ArrayList<CommPortIdentifier>();
		while(enumeration.hasMoreElements()) {
			identifiers.add(new CommPortIdentifier((gnu.io.CommPortIdentifier)enumeration.nextElement()));
        }		
		return identifiers;
	}
	
}
