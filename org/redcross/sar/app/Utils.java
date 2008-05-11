package org.redcross.sar.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import java.io.FileInputStream;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.thread.DiskoProgressMonitor;

/**
 * Utility class containing access to methods for handling properties.
 * @author geira
 *
 */
public class Utils {

	//private static Properties properties = null;
	private static ErrorDialog errorDialog = null;
	private static ProgressMonitor progressMonitor = null;
	private static IDiskoApplication diskoApp = null;

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
	
	public static void setFixedSize(Component c, int w, int h) {
		Dimension dim = new Dimension(w,h);
		c.setSize(dim);
		c.setMinimumSize(dim);
		c.setPreferredSize(dim);
		c.setMaximumSize(dim);
	}
	
	public static List<Enum<?>> getListOf(Enum e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		list.add(e);
		return list;
	}
	
	public static List<Enum<?>> getListOfAll(Class e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		Iterator<Enum> it = EnumSet.allOf(e).iterator();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}
	
	public static List<Enum<?>> getListNoneOf(Class e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		Iterator<Enum> it = EnumSet.noneOf(e).iterator();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}
	
	public static boolean inApp(Component c) {
		Object[] windows = Window.getWindows();
		for(int i=0;i<windows.length;i++) {
			// is component descending from a window in this
			// application
			if(SwingUtilities.isDescendingFrom(c, (Component)windows[i]))
				return true;
		}
		return false;
	}
	
}
