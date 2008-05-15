package org.redcross.sar.gui.factory;

import java.util.ResourceBundle;

/**
 * Read only Disko enum-to-property (text) factory.
 * <br>
 * Resource(s) are used to relate properties to enums. 
 * <br>
 * Typical subkeys following the enum key are: {.text, 
 * .icon, .tooltip, .symsbol}
 * <br>
 * To facilitate resource customization (file paths) this 
 * class wraps the BasicDiskoFactory. The follwing resource types are
 * supported:
 * <br> 
 * java.util.Properties (not localized)
 * <br>
 * java.util.ResourceBundle (localized)
 * <br>
 * The resource dominance - which resource is used when equal keys 
 * are present - is as follows:
 * <br>
 * 1. try passed custom resource
 * <br>
 * 2. if key not found -> try default icons resource
 * <br>
 * 3. if key not found -> try enums resource (DiskoEnumFactory)
 * <br>
 * 4. if key not found -> try installed resources
 * <br>
 * 5. if key not found -> if key is enum and suffix is text -> try to get enum text from source
 * <br>
 * 6. if all this fails -> a default icon is returned 
 * 
 * 
 * @author kennetgu
 *
 */

public class DiskoEnumFactory {

	private final static BasicDiskoFactory m_basic = 
		new BasicDiskoFactory();
	private final static ResourceBundle m_default = 
		ResourceBundle.getBundle("resources/enums");
	
	public static String getTooltip(Enum e) {
		return getText(e,"tooltip",null);
	}
	
	public static String getText(Enum e) {
		return getText(e,"text",null);
	}
	
	public static String getIcon(Enum e) {
		return getText(e,"icon",null);
	}
	
	public static String getSymbol(Enum e) {
		return getText(e,"symbol",null);
	}
	
	public static String getText(Enum e, String suffix) {
		return getText(e,suffix,null);
	}
	
	public static String getText(Enum e, String suffix, Object resource) {
		// get key
		String text = getText(BasicDiskoFactory.getKey(e, suffix),resource);
		// try get enum text from resource bundle assosiated with the enums declaring class?
		if((text==null || text.isEmpty()) && suffix!=null) {
			text = translate(e,suffix);
		}
		// return best effort
		return text;
	}
	
	private static String translate(Enum e, String suffix) {
		// search for bundle
		ResourceBundle bundle = BasicDiskoFactory.getBundle(e);
		// try to get value from resoures?
		if(bundle!=null)
			return getText(BasicDiskoFactory.getKey(e, suffix),bundle);
		// failed
		return null;
	}
		
	public static String getText(String key, Object resource) {
		// try custom resource
		String text = m_basic.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getString(key);
		// did found text?
		if((text==null || text.isEmpty())) {
			// get from installed resource
			text = m_basic.getTextFromInstalled(key);
		}
		// return best effort
		return text;
	}
	
	public static boolean setText(Enum e, String suffix, String value) {
		// get key
		String key = BasicDiskoFactory.getKey(e, suffix);
		// only update disko properties anonymously
		return m_basic.setText(key, value, false, true, m_default, "resources/disko");
	}
	
	public static boolean setText(Enum e, String suffix, String value, Object resource) {
		// forward using a best effort principle if passed resource is unknown
		return setText(e,suffix,value,resource, null);
	}
	
	public static boolean setText(Enum e, String suffix, String value, Object resource, String filename) {
		// get key
		String key = BasicDiskoFactory.getKey(e, suffix);
		// forward 
		if(!m_basic.setText(key, value, false, true, resource,filename)) {
			// did not fly, try again with disko properties
			if(!setText(e, suffix, value)) {
				// still not success, finally try enums
				return m_basic.setText(key, value, true, true, m_default, "resources/disko");
			}
		}
		// failure!
		return false;
	}
	
	public static Object getResourceFromEnum(Enum e, String suffix) { 
		// get key
		String key = BasicDiskoFactory.getKey(e, suffix);
		// try default
		if(m_default.containsKey(key)) {
			return m_default;
		}
		// try installed in basic
		return m_basic.getResourceFromKey(key);
	}
	
	public static BasicDiskoFactory getBasicFactory() {
		return m_basic;
	}
	
}
