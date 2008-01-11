package org.redcross.sar.gui.factory;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.redcross.sar.gui.DiskoCustomIcon;

public class DiskoIconFactory {

	private final static String m_path = "icons";	
	private final static ResourceBundle m_bundle = 
		ResourceBundle.getBundle("org.redcross.sar.gui.factory.icons");
	
	public static ImageIcon getIcon(String icon, String catalog) {
		return getIcon(icon,catalog,null);
	}
	
	public static ImageIcon getIcon(String icon, String catalog,ResourceBundle bundle) {
		if (icon != null && !icon.isEmpty()) {
			// forward
			return createImageIcon(icon,getIconPath(icon,catalog,bundle));
		}
		return null;
	}

	public static DiskoCustomIcon getIcon(
			String icon, String catalog, Color color, float alfa) {
		return getIcon(icon,catalog,color,alfa,null);
	}
	
	public static DiskoCustomIcon getIcon(
			String icon, String catalog, Color color, float alfa, ResourceBundle bundle) {
		if (icon != null && !icon.isEmpty()) {
			// forward
			return new DiskoCustomIcon(createImageIcon(icon, 
					getIconPath(icon,catalog,bundle)),color,alfa);
		}
		return null;
	}
	
	/**
	 * Create a image icon
	 * @param path The path to the icon image
	 * @param icon A name to identify the icon
	 * @return A ImageIcon
	 */
	public static ImageIcon createImageIcon(String name, String path){
		try {
			if (path != null) {
				File file = new File(path);
				if (file.exists()) {
					java.net.URL imgURL = file.toURI().toURL();
					return new ImageIcon(imgURL, name);
				}
			}
			else
			{
				BufferedImage defaultImage = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);

				java.awt.Graphics2D g2 = defaultImage.createGraphics();
				java.awt.Color col = new java.awt.Color(255, 0, 0);
				g2.setColor(col);
				g2.fill3DRect(0, 0, 30, 30, true);

				ImageIcon img=new ImageIcon(defaultImage,path+" not found");

				return img;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getIconPath(String icon, String catalog,ResourceBundle bundle) {
		// get path
		if (isPath(icon) || icon.endsWith(".icon")) {
			return icon;
		}
		else {
			// get key
			String key = getKey(icon,"icon");			
			// initialize icon file icon
			String filename = null;
			// try custom bundle?
			if(bundle!=null && bundle.containsKey(key)) 
				filename = bundle.getString(key);
			// try factory bundle?
			if((filename==null || filename.isEmpty()) && m_bundle.containsKey(key))
				filename = m_bundle.getString(key);
			// found?
			if(filename!=null && !filename.isEmpty())
				return m_path + "/" + catalog + "/" + filename;
		}
		// failure
		return null;
	}
	
	private static boolean isPath(String icon) {
		try {
		if (icon != null) {
			return (new File(icon)).exists();
		}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	private static String getKey(String icon, String suffix) {
		String key = icon+"."+suffix;		
		return key;
	}
	
	
}
