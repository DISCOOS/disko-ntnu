package org.redcross.sar.gui.factory;

import java.awt.image.BufferedImage;

import java.util.ResourceBundle;

public class DiskoSymbolFactory {

	private final static String m_path = "symbols";	
	private final static BasicDiskoFactory m_basic = 
		new BasicDiskoFactory();
	private final static ResourceBundle m_default = 
		ResourceBundle.getBundle("files/symbols");
	
	public static BufferedImage getSymbol(String symbol) {
		return getSymbol(symbol,null);
	}
	
	public static BufferedImage getSymbol(String symbol, Object resource) {
		if (symbol != null && !symbol.isEmpty()) {
			// forward
			try {
				return DiskoImageFactory.createImage(getPath(symbol,resource));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String getPath(String symbol) {
		return getPath(symbol,null);
	}
	
	public static String getPath(String symbol, Object resource) {
		
		// is path?
		if (m_basic.isPath(symbol)) {
			return symbol;
		}
		else {
			// get key
			String key = (!symbol.endsWith(".symbol")) 
					   ? m_basic.getKey(symbol,"symbol") : symbol;
			// get from passed resource
			String filename = m_basic.getText(key,resource);
			// try default bundle?
			if((filename==null || filename.isEmpty()) && m_default.containsKey(key))
				filename = m_default.getString(key);
			// found filename?
			if((filename==null || filename.isEmpty())) {
				// get from installed resource
				filename = m_basic.getTextFromInstalled(key);	
			}
			// found filename?
			if(filename!=null && !filename.isEmpty())
				// use relative path!
				return m_path + "/" + filename;
		}
		// failure
		return null;
	}

	public static String getKey(Enum e) {
		return m_basic.getKey(e,"symbol");
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
