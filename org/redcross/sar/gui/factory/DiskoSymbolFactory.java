package org.redcross.sar.gui.factory;

import java.awt.image.BufferedImage;

import java.util.ResourceBundle;

import org.redcross.sar.util.Internationalization;

public class DiskoSymbolFactory {

	private final static String m_path = "symbols";	

	private final static ResourceBundle m_default = 
		ResourceBundle.getBundle("resources/symbols");
	
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
		if (BasicDiskoFactory.fileExist(symbol)) {
			return symbol;
		}
		else {
			// get key
			String key = (!symbol.endsWith(".symbol")) 
					   ? BasicDiskoFactory.getKey(symbol,"symbol") : symbol;
			// get from passed resource
			String filename = BasicDiskoFactory.getText(key,resource);
			// try default bundle?
			if((filename==null || filename.isEmpty()) && m_default.containsKey(key))
				filename = m_default.getString(key);
			// found filename?
			if((filename==null || filename.isEmpty())) {
				// get from installed resource
				filename = Internationalization.getText(key);	
			}
			// found filename?
			if(filename!=null && !filename.isEmpty())
				// use relative path!
				return m_path + "/" + filename;
		}
		// failure
		return null;
	}

}
