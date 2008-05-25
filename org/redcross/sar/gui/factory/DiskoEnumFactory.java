package org.redcross.sar.gui.factory;

import java.util.ResourceBundle;

import org.redcross.sar.util.Internationalization;

/**
 * <p>
 * Read only Disko enum-to-property (text) factory.
 * Resource(s) are used to relate properties to enums. 
 * <p>
 * Typical subkeys following the enum key are: {.text, 
 * .icon, .tooltip, .symsbol}
 * <p> 
 * 1. java.util.Properties (not localized)<br>
 * 2. java.util.ResourceBundle (localized)<br>
 * <p>
 * The resource dominance - which resource is used when equal keys 
 * are present - is as follows:
 * <p>
 * 1. try passed custom resource<br>
 * 2. if key not found -> try enums resource (DiskoEnumFactory)<br>
 * 3. if key not found -> try installed resource (Internationalization)<br>
 * 4. if all this fails -> null is returned 
 * 
 * 
 * @author kennetgu
 *
 */

public class DiskoEnumFactory {

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
		ResourceBundle bundle = Internationalization.getBundle(e);
		// try to get value from resoures?
		if(bundle!=null)
			return getText(BasicDiskoFactory.getKey(e, suffix),bundle);
		// failed
		return null;
	}
		
	public static String getText(String key, Object resource) {
		// try custom resource
		String text = BasicDiskoFactory.getText(key, resource);
		// try disko properties?
		if((text==null || text.isEmpty()) && m_default.containsKey(key))
			text = m_default.getString(key);
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
	
	public static boolean setText(Enum e, String suffix, String value) {
		// get key
		String key = BasicDiskoFactory.getKey(e, suffix);
		// only update disko properties anonymously
		return BasicDiskoFactory.setText(key, value, false, true, m_default, "resources/disko");
	}
	
	public static boolean setText(Enum e, String suffix, String value, Object resource) {
		// forward using a best effort principle if passed resource is unknown
		return setText(e,suffix,value,resource, null);
	}
	
	public static boolean setText(Enum e, String suffix, String value, Object resource, String filename) {
		// get key
		String key = BasicDiskoFactory.getKey(e, suffix);
		// forward 
		if(!BasicDiskoFactory.setText(key, value, false, true, resource,filename)) {
			// did not fly, try again with disko properties
			if(!setText(e, suffix, value)) {
				// still not success, finally try enums
				return BasicDiskoFactory.setText(key, value, true, true, m_default, "resources/disko");
			}
		}
		// failure!
		return false;
	}	
}
