package org.redcross.sar.app;

import java.awt.Color;
import java.awt.Frame;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.DiskoCustomIcon;
import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.thread.DiskoProgressMonitor;

/**
 * Utility class containing access to methods for handling properties.
 * @author geira
 *
 */
public class Utils {

	private static Properties properties = null;
	private static ErrorDialog errorDialog = null;
	private static ProgressMonitor progressMonitor = null;
	private static IDiskoApplication diskoApp = null;

	/**
	 * Load the properties in a file with the given name
	 * @param fileName The name (path) of the file
	 * @return 
	 * @throws Exception
	 */
	public static Properties loadProperties(String fileName) throws Exception {
		Properties prop = new Properties();
		FileInputStream in = new FileInputStream(fileName);
		prop.load(in);
		in.close();
		return prop;
	}

	/**
	 * Get the default properties
	 * @return 
	 * @throws Exception
	 */
	public static Properties getProperties() {
		if (properties == null) {
			try {
				properties = loadProperties("properties");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return properties;
	}
	
	public static String getProperty(String key) {
		return getProperties().getProperty(key);
	}

	public static String setProperty(String key, String value) {
		String old = getProperties().getProperty(key);
		getProperties().setProperty(key,value);
		return old;
	}

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
	
	public static String translate(Object obj) {
		if (obj == null) {
			return "";
		}
		Properties props = getProperties();
		String key = null;
		if (obj instanceof Enum) {
			Enum e = (Enum)obj;
			key = e.getClass().getSimpleName()+"."+e.name()+".text";
			return (props != null ? props.getProperty(key, e.name()) : e.name());
		}
		String str = obj.toString();
		key = str+".text";
		return props.getProperty(key, str);
	}

	public static String getKey(Enum e, String suffix) {
		String key = e.getClass().getSimpleName()+"."+e.name()+"."+suffix;		
		return key;
	}
	
	public static String getSymbolPath(Enum e) {
		if (e != null) {
			String key = e.getClass().getSimpleName()+"."+e.name()+".symbol";
			try {
				Properties props = getProperties();
				return props != null ? props.getProperty(key) : null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}			
		}
		return null;
	}
	
	public static BufferedImage getSymbol(Enum e) {
		if (e != null) {
			String key = e.getClass().getSimpleName()+"."+e.name()+".symbol";
			return getImage(key);
		}
		return null;
	}
	
	public static ImageIcon getIcon(Enum e, String catalog) {
		if (e != null) {
			String key = e.getClass().getSimpleName()+"."+e.name()+".icon";
			return getIcon(key,catalog);
		}
		return null;
	}

	public static DiskoCustomIcon getIcon(Enum e, String catalog, Color color, float alfa) {
		if (e != null) {
			String key = e.getClass().getSimpleName()+"."+e.name()+".icon";
			return getIcon(key,catalog,color,alfa);
		}
		return null;
	}	

	public static String getIconText(Enum e) {
		if (e != null) {
			try {
				Properties props = getProperties();
				String key = e.getClass().getSimpleName()+"."+e.name()+".text";
				return props != null ? props.getProperty(key) : null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	public static ImageIcon getIcon(String key, String catalog) {
		try {
			Properties props = getProperties();
			return props != null ? DiskoIconFactory.getIcon(props.getProperty(key), catalog) : null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}

	public static BufferedImage getImage(String key) {
		try {
			Properties props = getProperties();
			return props != null ? createImage(props.getProperty(key)) : null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}
	
	public static DiskoCustomIcon getIcon(String key, String catalog, Color color, float alfa) {
		try {
			Properties props = getProperties();
			return props != null ? new DiskoCustomIcon(
					DiskoIconFactory.getIcon(props.getProperty(key), catalog),color,alfa) : null;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return null;
	}	

	public static ErrorDialog getErrorDialog(Frame owner) {
		// update
		errorDialog = new ErrorDialog(owner);
		errorDialog.setLocationRelativeTo(owner);
		// return
		return errorDialog;
	}

	public static ProgressMonitor startProgress(Frame owner, 
			Object message, String note, int min, int max,
			boolean intermediate) {
		// is finished with last?
		if(progressMonitor!=null) {
			// create new monitor			
			progressMonitor = new ProgressMonitor(owner,message,note,min,max);
		}
		return progressMonitor;
	}

	public static boolean notifyProgress(int progress, String msg, int min, int max) {
		if(progressMonitor!=null) {
			if(progressMonitor.isCanceled()) {
				progressMonitor.close();
				return false;
			} 
			else {				
				progressMonitor.setNote(msg);
				progressMonitor.setProgress(progress);
				return true;
			}
		}
		return false;
	}

	public static void stopProgress(int value, int min, int max) {

	}

	public static void setApp(IDiskoApplication app) {
		diskoApp = app;
	}

	public static IDiskoApplication getApp() {
		return diskoApp;
	}

	public static void showWarning(String msg)
	{
		showWarning(msg,getApp().getFrame());
	}

	public static void showWarning(String msg, Frame owner)
	{
		showWarning(null,msg,owner);
	}	
	
	public static void showWarning(String title, String msg) {
		showWarning(title,msg,getApp().getFrame());		
	}
	
	public static void showWarning(String title, String msg, Frame owner)
	{
		final String header = title;
		final String message = msg;
		final Frame frame = owner;
		Runnable r = new Runnable()
		{
			public void run()
			{
				try {
					// force progress dialog to hide
					DiskoProgressMonitor.getInstance().hide();
				}
				catch(Exception e) {
					e.printStackTrace();					
				}
				// show dialog
				JOptionPane.showMessageDialog(frame,
						message, header, JOptionPane.WARNING_MESSAGE);
				try {
					// show progress dialog again
					DiskoProgressMonitor.getInstance().showAgain();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		SwingUtilities.invokeLater(r);
	}
	
	public static String getPackageName(Class c) {
		String fullyQualifiedName = c.getName();
		int lastDot = fullyQualifiedName.lastIndexOf ('.');
		if (lastDot==-1){ return null; }
		String packageName = fullyQualifiedName.substring (0,lastDot);
		lastDot = packageName.lastIndexOf ('.');
		if (lastDot==-1){ return null; }
		return packageName.substring (lastDot+1,packageName.length()).toUpperCase();
	}	
}
