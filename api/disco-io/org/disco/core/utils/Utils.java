package org.disco.core.utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import org.disco.core.geo.GeoPos;
import org.disco.core.geo.TimePos;

/**
 * Utility class containing access to methods for handling properties.
 * @author geira
 *
 */
public class Utils {

	public final static String DATA_STRING_FORMAT = "yyyy.MM.dd HH:mm:ss Z";

	public static String getPackageName(Class<?> c) {
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
		Dimension d = c.getPreferredSize();
		c.setPreferredSize(new Dimension(d!=null ? d.width : c.getWidth(),h+adjust));
		c.setMaximumSize(new Dimension(Integer.MAX_VALUE,h+adjust));
	}

	public static void setFixedWidth(Component c, int w) {
		setFixedWidth(c,w,0);
	}

	public static void setFixedWidth(Component c, int w, int adjust) {
		// limit height, allow any width
		c.setMinimumSize(new Dimension(w+adjust, 0));
		Dimension d = c.getPreferredSize();
		c.setPreferredSize(new Dimension(w+adjust,d!=null ? d.height : c.getHeight()));
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

	@SuppressWarnings("unchecked")
	public static List<Enum<?>> getListOfAll(Class e) {
		List<Enum<?>> list = new ArrayList<Enum<?>>();
		Iterator<Enum<?>> it = EnumSet.allOf(e).iterator();
		while(it.hasNext()) {
			list.add(it.next());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
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
		float sign = Math.signum(seconds);
		seconds = Math.abs(seconds);
		int hours = Math.abs(seconds) / 3600;
		seconds = seconds % 3600;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		DecimalFormat formatter = new DecimalFormat("00");
		return formatter.format(sign*hours)
		+ ":" + formatter.format(minutes)
		+ ":" + formatter.format(seconds);
	}

	public static String toString(Object value) {
		if(value instanceof Integer) {
			return String.valueOf(value);
		}
		else if(value instanceof Double) {
			return String.valueOf(value);
		}
		else if(value instanceof Calendar) {
			DateFormat formatter = new SimpleDateFormat(DATA_STRING_FORMAT);
			return formatter.format(((Calendar)value).getTime());
		}
		else if(value instanceof TimePos) {
			return ((TimePos)value).toString();
		}
		else if(value instanceof GeoPos) {
			return ((TimePos)value).toString();
		}
		return value!=null ? value.toString() : "";
	}

	public static Object valueOf(String value, Class<?> c) {
		if(value!=null) {
			try {
				if(c.equals(Integer.class)) {
					return Integer.valueOf(value);
				}
				else if(c.equals(Double.class)) {
					return Double.valueOf(value);
				}
				else if(c.equals(Calendar.class)) {
					DateFormat formatter = new SimpleDateFormat(DATA_STRING_FORMAT);
					return formatter.parse(value);
				}
				else if(c.equals(TimePos.class)) {
					// parse string
					String[] split = value.split(" ");
					// create position
					Point2D.Double p = new Point2D.Double(
							Double.valueOf(split[0]),
							Double.valueOf(split[1]));
					// create time
					Calendar t = (Calendar)valueOf(split[2], Calendar.class);
					return new TimePos(p,t);
				}
				else if(c.equals(GeoPos.class)) {
					// parse string
					String[] split = value.split(" ");
					// create position
					Point2D.Double p = new Point2D.Double(
							Double.valueOf(split[0]),
							Double.valueOf(split[1]));
					return new GeoPos(p);
				}
			}
			catch (Exception e) {
				// Consume
				return null;
			}
		}
		return value!=null ? value.toString() : "";
	}

	public static String trimHtml(String text) {
		if(text!=null) {
			if(text.length()>5) {
				if(text.substring(0, 6).equalsIgnoreCase("<html>"))
					text = text.substring(6, text.length());
			}
			if(text.length()>6) {
				if(text.substring(text.length()-7, text.length()).equalsIgnoreCase("</html>"))
					text = text.substring(0, text.length()-7);
			}
		}
		return text;
	}

	public static String stripHtml(String text) {
		if(text!=null) {
			String sep = System.getProperty("line.separator");
			text = text.replace("</p>", sep+sep);
			text = text.replace("<br>", sep);
			text = text.replaceAll("\\<.*?>","");
		}
		return text;
	}

	public static String getBold(String text) {
		return "<b>"+text+"</b>";
	}

	public static String getHtml(String text) {
		return "<html>"+text+"</html>";
	}

	public static List<String> readLines(String file, String skip) {

		// prepare
		List<String> lines = new ArrayList<String>();
	    
	    try {
	      //	use buffering, reading one line at a time
	      // FileReader always assumes default encoding is OK!
	      BufferedReader input =  new BufferedReader(new FileReader(file));
	      try {
	        String line = null; //not declared within while loop
	        /*
	        * readLine is a bit quirky :
	        * it returns the content of a line MINUS the newline.
	        * it returns null only for the END of the stream.
	        * it returns an empty String if two newlines appear in a row.
	        */
	        while (( line = input.readLine()) != null){
	        	if(!(line==null || line.isEmpty() || line.startsWith(skip)) )
	        		lines.add(line);
	        }
	      }
	      finally {
	        input.close();
	      }
	    }
	    catch (IOException ex){
	      ex.printStackTrace();
	    }
	    
	    return lines;
	}

	public static String[][] importText(String file, String token) {

		// initialize
		int row = 0;
		int col = 0;
		String line = null;
		String[][] matrix = null;

		// get file
		File f = new File(file);

		// exists?
		if(f.exists()) {

			try {

				// get readers
				FileReader reader = new FileReader(f);
				LineNumberReader counter = new LineNumberReader(reader);
				BufferedReader buffered = new BufferedReader(reader);

				// allocate memory
				counter.skip(f.length());
				matrix = new String [counter.getLineNumber()][];

				//read each line of text file
				while((line = buffered.readLine()) != null)
				{
					// get tokenizer for this line
					StringTokenizer st = new StringTokenizer(line,token);

					// allocate memory
					matrix[row] = new String[st.countTokens()];

					// import text
					while (st.hasMoreTokens())
					{
						//get next token and store it in the array
						matrix[row][col] = st.nextToken();
						col++;
					}
					col = 0;
					row++;
				}

				// close readers
				counter.close();
				buffered.close();
				reader.close();

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// finished
		return matrix;
	}

	public static void exportText(String[][] matrix, String file, String token) {

		// get file
		File f = new File(file);

		try {

			// create file?
			if(!f.exists()) {
				f.createNewFile();
			}

			// get readers
			FileWriter writer = new FileWriter(f);
			BufferedWriter buffered = new BufferedWriter(writer);

			//read each line of text file
			for(int i=0;i<matrix.length;i++) {
				// get row
				String[] row = matrix[i];
				// loop over all columns
				for(int j=0;j<row.length;j++) {
					if(j>0) buffered.write(token);
					buffered.write(row[j]);
				}
				// write new line
				buffered.newLine();
			}

			// close writers
			buffered.close();
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getExtensionRegExp(String extensions, String delimiter) {

		// initialize
		String regexp = "";
		String[] list = extensions.split(delimiter);

		// create a union of regular expressions
		for(String ext : list) {
			// insert escape characters
			ext = ext.replaceAll("\\.", "\\\\.");
			ext = ext.replaceAll("\\*", "\\.*");
			// build
			if(regexp.isEmpty())
				regexp = ext;
			else
				regexp = regexp.concat("|"+ext);
		}

		// finished
		return regexp;

	}

	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	public static String capitalize(String s) {
		if (s.length() == 0) return s;
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	public static char convert(byte b) {
		//	To Char
		return (char)(b & 0xFF);
	}

	public static byte convert(char c) {
		// To Byte
		return (byte)(c & 0xFF);		
	}



}
