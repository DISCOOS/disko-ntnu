package org.redcross.sar.gui.factory;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.File;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.util.Internationalization;

/**
 * <p>
 * Read only Disko icon factory used to create Disko L&F icons.
 * <p>
 * Resource(s) are used to store icon file paths. 
 * <p>
 * The resource dominance - which resource is used when equal keys 
 * are present - is as follows:
 * <p>
 * 1. try passed custom resource<br>
 * 2. if key not found -> try default icons resource<br>
 * 3. if key not found -> try enums resource (DiskoEnumFactory)<br>
 * 4. if key not found -> try installed resource (Internationalization)<br>
 * 5. if all this fails -> a default icon is returned <br>
 * 
 * 
 * @author kennetgu
 *
 */

public class DiskoIconFactory {

	private final static String m_path = "icons";	

	private final static ResourceBundle m_default = 
		ResourceBundle.getBundle("resources/icons");
	
	public static ImageIcon getIcon(String icon, String catalog) {
		return getIcon(icon,catalog,null);
	}
	
	public static ImageIcon getIcon(String icon, String catalog, Object resource) {
		if (icon != null && !icon.isEmpty()) {
			// forward
			return createImageIcon(icon,getPath(icon,catalog,resource));
		}
		return null;
	}

	public static DiskoIcon getIcon(
			String icon, String catalog, Color color, float alfa) {
		return getIcon(icon,catalog,color,alfa,null);
	}
	
	public static DiskoIcon getIcon(
			String icon, String catalog, Color color, float alfa, Object resource) {
		if (icon != null && !icon.isEmpty()) {
			// forward
			return new DiskoIcon(createImageIcon(icon, 
					getPath(icon,catalog,resource)),color,alfa);
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
	
	public static String getPath(
			String icon, String catalog) {
		return getPath(icon,catalog,null);
	}
	
	public static String getPath(
			String icon, String catalog, Object resource) {
		
		// is an absolute path?
		if (BasicDiskoFactory.fileExist(icon)) {
			return icon;
		}
		// is a relative path?
		else if(BasicDiskoFactory.fileExist(m_path + "/" + catalog + "/" + icon)) {
			return m_path + "/" + catalog + "/" + icon;
		}
		else {
			// initialize
			String filename = BasicDiskoFactory.getText(icon, resource);
			// get default icon key
			String key = (!icon.endsWith(".icon")) ? BasicDiskoFactory.getKey(icon,"icon") : icon;
			// try default bundle?
			if((filename==null || filename.isEmpty()) && m_default.containsKey(key))
				filename = m_default.getString(key);
			// get from enum resource?
			if((filename==null || filename.isEmpty()))
				filename = DiskoEnumFactory.getText(key,resource);
			// found filename?
			if(filename==null || filename.isEmpty()) {
				// get from installed resources
				filename = Internationalization.getText(key);	
			}
			// found?
			if(filename!=null && !filename.isEmpty())
				// use relative path!
				return m_path + "/" + catalog + "/" + filename;
		}
		// failure
		return null;
	}	
	
	public static String getCatalog(ButtonSize size) {
		return DiskoButtonFactory.getCatalog(size);
	}
}
