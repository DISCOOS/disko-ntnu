package org.redcross.sar.app;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import java.text.DecimalFormat;
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
		// set isMessageDialog flag
		messageDialog = new MessageDialog(frame);
		// forwar
		int ans = JOptionPane.showConfirmDialog(frame, msg, title, option, MessageDialog.QUESTION_MESSAGE);
		// reset dialog
		messageDialog = null;
		// finished
		return ans;		
		
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
	
	public static boolean showMessage(final String title, final String msg, final int options)
	{		
		// consume?
		if(isMessageDialogShown()) return false;
		
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
			messageDialog.addFocusListener(new FocusAdapter() {
            	@Override
                public void focusGained(FocusEvent e) {
            		// if
            		messageDialog.requestFocusInWindow();
                }
            });    		
			
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
		// success 
		return true;
	}
	
	public static boolean isMessageDialogShown() {
		return messageDialog!=null;
	}
	
	public static boolean isMessageDialog(Component c) {
		return (c!=null) && (messageDialog==c || c instanceof JOptionPane);
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
	
	public static void setFixedHeight(Component c, int h) {
		setFixedHeight(c,h,0);
	}
	
	public static void setFixedHeight(Component c, int h, int adjust) {
		// limit height, allow any width
		c.setMinimumSize(new Dimension(0,h+adjust));
		c.setPreferredSize(new Dimension(h+adjust,h+adjust));
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE,h+adjust));
	}
	
	public static void setFixedWidth(Component c, int w) {
		setFixedWidth(c,w,0);
	}
	
	public static void setFixedWidth(Component c, int w, int adjust) {
		// limit height, allow any width
		c.setMinimumSize(new Dimension(w+adjust, 0));
		c.setPreferredSize(new Dimension(w+adjust,w+adjust));
		c.setMaximumSize(new Dimension(w+adjust, Integer.MAX_VALUE));
	}
	
	public static void setFixedSize(Component c, int w, int h) {
		setFixedSize(c,w,h,0,0);
	}
	
	public static void setFixedSize(Component c, int w, int h, int dw, int dh) {
		Dimension d = new Dimension(w+dw,h+dh);
		c.setSize(d);
		c.setMinimumSize((Dimension)d.clone());
		c.setPreferredSize((Dimension)d.clone());
		c.setMaximumSize((Dimension)d.clone());
	}
	
	public static void setAnySize(Component c, int w, int h) {
		setAnySize(c,w,h,0,0);
	}
	
	public static void setAnySize(Component c, int w, int h, int dw, int dh) {
		Dimension d = new Dimension(w+dw,h+dh);
		c.setSize(d);
		c.setPreferredSize((Dimension)d.clone());
		c.setMinimumSize(new Dimension(0,0));
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));		
	}
	
	
	public static List<Enum<?>> getListOf(Enum<?> e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		list.add(e);
		return list;
	}
	
	public static List<Enum<?>> getListOfAll(Class e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		Iterator<Enum<?>> it = EnumSet.allOf(e).iterator();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}
	
	public static List<Enum<?>> getListNoneOf(Class e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		Iterator<Enum<?>> it = EnumSet.noneOf(e).iterator();
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
	
    public static boolean isNumeric(String str)
    {
    	return isNumeric(str,Number.class);
    }
    
    public static Class<? extends Number> getNumericClass(Object value)
    {
    	if(value instanceof Number) {
    		return ((Number)value).getClass();
    	}
    	else if(value instanceof String) {
    		String str = value.toString();
        	if(isNumeric(str,Integer.class))
        		return Integer.class;
        	else if(isNumeric(str,Double.class))
        		return Double.class;
        	else if(isNumeric(str,Long.class))
        		return Long.class;
        	else if(isNumeric(str,Float.class))
        		return Float.class;
        	if(isNumeric(str,Byte.class))
        		return Byte.class;
        	else if(isNumeric(str,Short.class))
        		return Short.class;
        	
    	}
    	return null;
    }
    
    
    public static boolean isNumeric(String str, Class<? extends Number> c)
    {
        try
        {
            if (c.equals(Number.class))
            {
            	if(isNumeric(str,Integer.class))
            		return true;
            	else if(isNumeric(str,Double.class))
            		return true;
            	else if(isNumeric(str,Long.class))
            		return true;
            	else if(isNumeric(str,Float.class))
            		return true;
            	if(isNumeric(str,Byte.class))
            		return true;
            	else if(isNumeric(str,Short.class))
            		return true;
            	
                return false;
                
            }
            else if (c.equals(Integer.class))
            {
                Integer.parseInt(str);
            }
            else if (c.equals(Double.class))
            {
            	Double.parseDouble(str);
            }
            else if (c.equals(Long.class))
            {
                Long.parseLong(str);
            }
            else if (c.equals(Float.class))
            {
                Float.parseFloat(str);
            }
            else if (c.equals(Byte.class))
            {
                Byte.parseByte(str);
            }
            else if (c.equals(Short.class))
            {
                Short.parseShort(str);
            }
            else {
            	return false;
            }
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
 
        return true;
    }

    public static Object parseNumeric(String str)
    {
    	return parseNumeric(str,Number.class);
    }
    
    public static Object parseNumeric(String str, Class<? extends Number> c)
    {
        try
        {
            if (c.equals(Number.class))
            {
            	if(isNumeric(str,Integer.class))
            		return parseNumeric(str,Integer.class);
            	else if(isNumeric(str,Double.class))
            		return parseNumeric(str,Double.class);
            	else if(isNumeric(str,Long.class))
            		return parseNumeric(str,Long.class);
            	else if(isNumeric(str,Float.class))
            		return parseNumeric(str,Float.class);
            	if(isNumeric(str,Byte.class))
            		return parseNumeric(str,Byte.class);
            	else if(isNumeric(str,Short.class))
            		return parseNumeric(str,Short.class);
            	
            }
            else if (c.equals(Byte.class))
            {
                return Byte.parseByte(str);
            }
            else if (c.equals(Double.class))
            {
            	return Double.parseDouble(str);
            }
            else if (c.equals(Float.class))
            {
            	return Float.parseFloat(str);
            }
            else if (c.equals(Integer.class))
            {
            	return Integer.parseInt(str);
            }
            else if (c.equals(Long.class))
            {
            	return Long.parseLong(str);
            }
            else if (c.equals(Short.class))
            {
            	return Short.parseShort(str);
            }
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }
 
        return null;
    }
    
	public static String getTime(int seconds) {
		int hours = seconds / 3600;
		seconds = seconds % 3600;
		int minutes = seconds / 60;
		seconds = seconds % 60;		
		DecimalFormat formatter = new DecimalFormat("00");		
		//GregorianCalendar date = new GregorianCalendar(1900,1,1,hours,minutes,seconds);
		//SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		//return formatter.format(date.getTime());
		return formatter.format(hours) 
				+ ":" + formatter.format(minutes) 
				+ ":" + formatter.format(seconds); 
	}    
	
	public static String stripHtml(String caption) {
		if(caption.length()>5) {
			if(caption.substring(0, 6).equalsIgnoreCase("<html>"))
				caption = caption.substring(6, caption.length());			
		}
		if(caption.length()>6) {
			if(caption.substring(caption.length()-7, caption.length()).equalsIgnoreCase("</html>"))
				caption = caption.substring(0, caption.length()-7);
		}
		return caption;
	}
    
}
