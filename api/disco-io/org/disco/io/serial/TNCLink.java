package org.disco.io.serial;

public class TNCLink extends SerialLink {

	protected String device;
	protected String hostMode;
	protected String configPath;
	
	public TNCLink(String configPath) {
		super();
		this.configPath = configPath;		
	}
	
	public String getDevice() {
		return device;
	}
	
	protected void setDevice(String name) {
		device = name;
	}
		
	public String getConfigPath() {
		return configPath;
	}
	
	protected void setConfigPath(String path) {
		configPath = path;
	}
	
	public String getHostMode() {
		return hostMode;
	}
	
	protected void setHostMode(String name) {
		hostMode = name;
	}
		
	protected void initTNC() throws IllegalStateException {
		
	}
	
	protected void initTNC(String file) throws IllegalStateException {
		
	}
	
	protected void restoreTNC() throws IllegalStateException {
		
	}
	
	protected void restoreTNC(String file) throws IllegalStateException {
		
	}
	
}
