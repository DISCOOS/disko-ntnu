package org.redcross.sar.gui.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Basic Disko factory class with localization capabilities. 
 * 
 * Wrapped by other Disko Factory classes to add localized 
 * Read and Write capabilities. This class also provides the
 * possibility to add custom resources of types.
 *  
 * 1. java.util.Properties (not localized)
 * 2. java.util.ResourceBundle (localized)
 * 
 * The dominance (which resource is used when equal keys 
 * are present) of custom installed resource over default 
 * resource(s) is governed by the wrapping class. 
 * 
 * @author kennetgu
 *
 */
public class BasicDiskoFactory {

	public static boolean supports(Object resource) {
		return (resource instanceof ResourceBundle) 
			|| (resource instanceof Properties);
	}
		
	public static boolean fileExist(String name) {
		try {
			if (name != null) {
				return ((new File(name)).exists());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static boolean createFile(String filename) {
		try {
			if (filename != null) {
				// create object
				File file = new File(filename);
				// create?
				if(!file.exists())
					file.createNewFile();
				// success
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// failure!
		return false;
	}
	
	
	public static String getKey(Enum<?> e, String suffix) {
		return (e!=null ? getKey(e.getClass().getSimpleName()+"."+e.toString(),suffix) : null);
	}
	
	
	public static String getKey(String prefix, String suffix) {
		return prefix + ((suffix!=null && !suffix.isEmpty()) ? "."+suffix : null);
	}

	public static String getText(String key, Object resource) {
		if(key==null || key.isEmpty()) return null;
		// translate
		if(resource instanceof ResourceBundle) {
			// cast to resource bundle
			ResourceBundle bundle = (ResourceBundle)resource;
			// get file name?
			if(bundle.containsKey(key))
				return  bundle.getString(key);
		}
		if(resource instanceof Properties) {
			// cast to resource bundle
			Properties properties =  (Properties)resource;
			// get file name?
			if(properties.containsKey(key))
				return properties.getProperty(key);
		}
		// not found
		return null;
	}	
	
	public String getString(String key, Object resource) {
		String text = getText(key,resource);
		return text==null ? key : text;
	}	

	public static boolean setText(String key, String value, boolean create, boolean write, Object resource, String filename) {
		// supported resource?
		if(supports(resource)) {
			// get properties
			Properties properties = getProperties(resource,filename,create);
			// success?
			if(properties!=null) {
				// contains key or create anyway?
				if(properties.containsKey(key) || create) {
					// forward
					properties.setProperty(key, value);
					// write to file?
					if(write) {
						// forward
						return store(properties,filename);
					}
				}
			}
		}
		// failure!
		return false;
	}
	
	/**
	 * Get the properties file from resource
	 * 
	 * If resource is null, then create from file name
	 * 
	 */
	public static Properties getProperties(Object resource, String filename, boolean create) {
		// try to get from current information
		if(resource instanceof Properties) {
			return (Properties)resource;
		}
		else if(resource instanceof ResourceBundle){
			return load(filename);
		}
		// does file not exist?
		if(!fileExist(filename)) {
			// create file?
			if(create) { 
				if(!createFile(filename)) {
					// failed to create file
					return null;
				}
			}
		}
		
		// file exist, load from file
		return load(filename);
	}

	/**
	 * Load the properties from file
	 * @param fileName The name (path) of the file
	 * @return 
	 * @throws Exception
	 */
	public static Properties load(String fileName) {
		try {
			if(fileExist(fileName)) {
				Properties prop = new Properties();
				FileInputStream in = new FileInputStream(fileName);
				prop.load(in); in.close();
				return prop;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	/**
	 * Store properties to file
	 * @param fileName The name (path) of the file
	 * @throws Exception
	 */
	public static boolean store(Properties properties, String fileName) {
		try {
			if(fileExist(fileName)) {
				FileOutputStream out = new FileOutputStream(fileName);
				properties.store(out, null); out.close();
				// success
				return true;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failure
		return false;
	}	
	
}
