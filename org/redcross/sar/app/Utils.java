package org.redcross.sar.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.MessageDialog;
import org.redcross.sar.gui.factory.DiskoStringFactory;
import org.redcross.sar.thread.DiskoProgressMonitor;

/**
 * Utility class containing access to methods for handling properties.
 * @author geira
 *
 */
public class Utils {

	//private static Properties properties = null;
	private static MessageDialog messageDialog = null;
	private static ProgressMonitor progressMonitor = null;
	private static IDiskoApplication diskoApp = null;
	
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

	public static int showConfirm(String title, String msg,int option) {
		// get frame (if frame is locked use null)
		Frame frame = Utils.getApp().isLocked() ? null : getApp().getFrame();
		// forwar
		return JOptionPane.showConfirmDialog(frame, msg, title, option, MessageDialog.QUESTION_MESSAGE);		
	}
	
	public static void showMessage(String msg) {
		showMessage(DiskoStringFactory.getText("MESSAGE_HEADER_DEFAULT"),msg);
	}

	public static void showMessage(String title, String msg) {
		showMessage(title,msg,MessageDialog.INFORMATION_MESSAGE);		
	}
	
	public static void showWarning(String msg) {
		showWarning(DiskoStringFactory.getText("WARNING_HEADER_DEFAULT"),msg);
	}

	public static void showWarning(String title, String msg) {
		showMessage(title,msg,MessageDialog.WARNING_MESSAGE);		
	}
	
	public static void showError(String msg) {
		showError(DiskoStringFactory.getText("ERROR_HEADER_DEFAULT"),msg);
	}

	public static void showError(String title, String msg) {
		showMessage(title,msg,MessageDialog.ERROR_MESSAGE);		
	}
	
	public static void showMessage(final String title, final String msg, final int options)
	{
		
		// consume?
		if(isMessageDialogShown()) return;
		
		if(SwingUtilities.isEventDispatchThread()) {
			
			DiskoProgressMonitor monitor = null;
			
			// get monitor
			try { 
				monitor = DiskoProgressMonitor.getInstance(); 
			}
			catch(Exception e) { e.printStackTrace(); }
			
			// create message dialog
			messageDialog = new MessageDialog(getApp().getFrame());
			messageDialog.setLocationRelativeTo(getApp().getFrame(), DefaultDialog.POS_CENTER, false,true);
			
			// set inhibit flag
			boolean isInhibit= monitor.setInhibit(false);						
			
			// force progress dialog to hide
			monitor.hide();
			
			// show dialog
			messageDialog.showMessage(title, msg, options);
			
			// show progress dialog again
			monitor.showAgain(); 
			
			// resume mode
			monitor.setInhibit(isInhibit);
			
			// reset message dialog
			messageDialog = null;
			
		}
		else {
			Runnable r = new Runnable(){
				public void run(){
					showMessage(title, msg, options);
				}
			};
			SwingUtilities.invokeLater(r);
		}
	}
	
	public static boolean isMessageDialogShown() {
		return messageDialog!=null;
	}
	
	public static boolean isMessageDialog(Component c) {
		return (messageDialog!=null && messageDialog==c);
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
