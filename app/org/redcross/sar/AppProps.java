package org.redcross.sar;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.redcross.sar.gui.factory.BasicDiskoFactory;
import org.redcross.sar.util.Internationalization;

/**
 * <p>
 * Read-Write Disko property (text) factory.
 * <p>
 * <p>
 * The resource dominance - which resource is used when equal keys 
 * are present - is as follows:
 * <p>
 * 1. try passed custom resource<br>
 * 2. if key not found -> try enums resource (DiskoEnumFactory)<br>
 * 3. if key not found -> try installed resources (Internationalization)<br>
 * 4. if all this fails -> null is returned 
 * 
 * 
 * @author kennetgu
 *
 */

public class AppProps {

	private final static String PROPERTIES_FILE = 
		"resources/disko.properties";
	
	private final static Properties m_default = 
		BasicDiskoFactory.load(PROPERTIES_FILE);
	
	public static String getText(String key) {
		return getText(key,null);
	}
	
	public static String getText(String key, Object resource) {
		// try custom resource
		String text = BasicDiskoFactory.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getProperty(key);
		// try installed resources?
		if((text==null || text.isEmpty())) {
			text = Internationalization.getText(key);
		}
		// return best effort
		return text;
	}
	
	public String getString(String key, Object resource) {
		String text = getText(key,resource);
		return text==null ? key : text;
	}
		
	public static boolean setText(String key, String value) {
		// only update disko properties anonymously
		return BasicDiskoFactory.setText(key, value, true, true, m_default, PROPERTIES_FILE);
	}
	
	public static boolean setText(String key, String value, Object resource, String filename) {
		// forward 
		if(!BasicDiskoFactory.setText(key, value, true, true, resource,filename)) {
			// did not fly, try again with disko properties
			if(!setText(key, value)) {
				// still not success, finally try default
				return BasicDiskoFactory.setText(key, value, true, true, m_default, PROPERTIES_FILE);
			}
		}
		// failure!
		return false;
	}
	
	public static void save() {
		IDiskoRole role = Application.getInstance().getCurrentRole();
		if(role!=null) setText("STARTUP.LAST.ROLE",role.getTitle());
		String id = Application.getInstance().getDispatcher().getCurrentOperationID();
		if(id!=null) setText("STARTUP.LAST.OPID",id);
		Logger.getLogger(AppProps.class).info("Properties saved");
	}
	
}
