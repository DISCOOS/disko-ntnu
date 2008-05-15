package org.redcross.sar.gui.factory;

import java.util.ResourceBundle;

/**
 * Read-only Localized String Factory
 * 
 * @author kennetgu
 *
 */

public class DiskoStringFactory {

	private final static BasicDiskoFactory m_basic = 
		new BasicDiskoFactory();
	private final static ResourceBundle m_default = 
		ResourceBundle.getBundle("resources/strings");
	
	public static String translate(Object value) {
		return translate(value,null);
	}
	
	public static String translate(Object value, Object resource) {
		// forward
		return getText(getKey(value),resource);
	}
	
	public static String getText(String key) {
		return getText(key,null);
	}
	
	public static String getText(String key, Object resource) {
		if(key==null || key.isEmpty()) return null;
		// try custom resource
		String text = m_basic.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getString(key);
		// try enums factory?
		if((text==null || text.isEmpty()))
			text =DiskoEnumFactory.getText(key,resource);		
		// try installed resources?
		if((text==null || text.isEmpty())) {
			// get from installed resource
			text = m_basic.getTextFromInstalled(key);
		}
		// return best effort
		return text;
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
	
	private static String getKey(Object value) {
		String key = null;
		if(value!=null) {
			if(value instanceof Enum)
				key = m_basic.getKey((Enum)value, "text");
			else
				key = m_basic.getKey(value.toString(), "text");
		}
		return key;
	}
	
}
