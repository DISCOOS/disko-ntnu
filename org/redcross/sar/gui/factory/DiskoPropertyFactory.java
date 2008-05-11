package org.redcross.sar.gui.factory;

import java.util.Properties;

public class DiskoPropertyFactory {

	private final static BasicDiskoFactory m_basic = 
		new BasicDiskoFactory();
	private final static Properties m_default = m_basic.load("resources/disko.properties");
	
	public static String getText(String key) {
		return getText(key,null);
	}
	
	public static String getText(String key, Object resource) {
		// try custom resource
		String text = m_basic.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getProperty(key);
		// did found text?
		if((text==null || text.isEmpty())) {
			// get from installed resource
			text = m_basic.getTextFromInstalled(key);
		}
		// return best effort
		return text;
	}
		
	public static boolean setText(String key, String value) {
		// only update disko properties anonymously
		return m_basic.setText(key, value, false, true, m_default, "resources/disko");
	}
	
	public static boolean setText(String key, String value, Object resource) {
		// forward using a best effort principle if passed resource is unknown
		return setText(key, value, resource, null);
	}
	
	public static boolean setText(String key, String value, Object resource, String filename) {
		// forward 
		if(!m_basic.setText(key, value, false, true, resource,filename)) {
			// did not fly, try again with disko properties
			if(!setText(key, value)) {
				// still not success, finally try enums
				return m_basic.setText(key, value, true, true, m_default, "resources/disko");
			}
		}
		// failure!
		return false;
	}
	
	public static Object getResourceFromKey(String key) { 
		if(m_default.containsKey(key)) {
			return m_default;
		}
		return m_basic.getResourceFromKey(key);
	}
	
	public static BasicDiskoFactory getBasicFactory() {
		return m_basic;
	}
	
}
