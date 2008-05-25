package org.redcross.sar.gui.factory;

import java.util.ResourceBundle;

import org.redcross.sar.util.Internationalization;

/**
 * <p>
 * Read-only Localized String Factory
 * <p>
 * The resource dominance - which resource is used when equal keys 
 * are present - is as follows:
 * <p>
 * 1. try passed custom resource<br>
 * 2. if key not found -> try default resource <br>
 * 3. if key not found -> try enums resource (DiskoEnumFactory)<br>
 * 4. if key not found -> try installed resources (Internationalization)<br>
 * 5. if all this fails -> null is returned 
 * 
 * 
 * @author kennetgu
 *
 */

public class DiskoStringFactory {


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
		String text = BasicDiskoFactory.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getString(key);
		// try enums factory?
		if((text==null || text.isEmpty()))
			text =DiskoEnumFactory.getText(key,resource);		
		// try installed resources?
		if((text==null || text.isEmpty())) {
			// get from installed resource
			text = Internationalization.getText(key);
		}
		// return best effort
		return text;
	}
	
	public String getString(String key, Object resource) {
		String text = getText(key,resource);
		return text==null ? key : text;
	}	
	
	private static String getKey(Object value) {
		String key = null;
		if(value!=null) {
			if(value instanceof Enum)
				key = BasicDiskoFactory.getKey((Enum)value, "text");
			else if (value instanceof String)
				key = value.toString();
			else
				key = BasicDiskoFactory.getKey(value.toString(), "text");
		}
		return key;
	}
	
}
