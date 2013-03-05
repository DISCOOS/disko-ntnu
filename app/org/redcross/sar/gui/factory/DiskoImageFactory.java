package org.redcross.sar.gui.factory;

import java.awt.image.BufferedImage;

import java.io.File;
import java.util.Properties;

import javax.imageio.ImageIO;

public class DiskoImageFactory {
	
	/**
	 * Create a image 
	 * @param path The path to the image
	 * @param name A name to identify the icon
	 * @return A ImageIcon
	 * @throws Exception
	 */
	public static BufferedImage createImage(String path) 
	throws Exception {
		BufferedImage bi = null;
		if (path != null) {
			File file = new File(path);
			if (file.exists()) {
				java.net.URL imgURL = file.toURI().toURL();
				bi = ImageIO.read(imgURL);
			}
		}
		else
		{
			// create default image
			bi = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics2D g2 = bi.createGraphics();
			java.awt.Color col = new java.awt.Color(255, 0, 0);
			g2.setColor(col);
			g2.fill3DRect(0, 0, 30, 30, true);
		}
		return bi;
	}
	
	/**
	 * Create a image 
	 * @param path The path to the saved image
	 * @param format Image save format 
	 * @throws Exception
	 */
	public static void saveImage(BufferedImage image, String path, String format) 
	throws Exception {
		if (path != null) {
			File file = new File(path);
			if (file.exists()) {
				saveImage(image,file,format);
			}
		}
	}
	
	/**
	 * Create a image 
	 * @param file The file to save image in
	 * @param format Image save format 
	 * @throws Exception
	 */
	public static void saveImage(BufferedImage image, File file, String format) 
	throws Exception {
		if (file != null) {
			if (file.exists()) {
				ImageIO.write(image, format, file);
			}
		}
	}	
	
	public static BufferedImage getImage(String key, Properties properties) {
		try {
			return createImage(properties.getProperty(key));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
		
}
