package org.redcross.sar.gui.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

	private final Map<Object,String> m_fileNames = new HashMap<Object,String>(); 	
	private final Map<String,Object> m_installed = new HashMap<String,Object>(); 	
		
	public boolean supports(Object resource) {
		return (resource instanceof ResourceBundle) 
			|| (resource instanceof Properties);
	}
	
	public boolean isInstalled(Object resource) {
		// is installed?
		return m_fileNames.containsKey(resource);
	}
	
	public boolean isInstalled(String filename) {
		// is installed?
		return m_installed.containsKey(filename);
	}
	
	public String getFileName(Object resource) {
		// is installed?
		if(isInstalled(resource)) {
			// loop over all
			return m_fileNames.get(resource);
		}
		// not installed
		return null;
	}
	
	public Object getResource(String filename) {
		// is installed?
		if(isInstalled(filename)) {
			// get installed resource
			return m_installed.get(filename);
		}
		// not installed
		return null;
	}
	
	public Object getResourceFromKey(String key) {
		for(Object it: m_installed.values()) {
			if(it instanceof ResourceBundle) {
				// cast to resource bundle
				ResourceBundle bundle =  (ResourceBundle)it;
				// get file name?
				if(bundle.containsKey(key)) return it;
			}
			if(it instanceof Properties) {
				// cast to resource bundle
				Properties properties =  (Properties)it;
				// get file name?
				if(properties.containsKey(key)) return it;
			}
		}
		// not found
		return null;
		
	}
	
	public boolean install(String filename,Object resource) {
		// supported resource?
		if(supports(resource)) {
			// not installed already?
			if(!isInstalled(resource)) {
				// install
				m_fileNames.put(resource,filename);
				m_installed.put(filename,resource);
				// success
				return true;
			}
		}
		// failure
		return false;
	}
		
	public boolean uninstall(Object resource) {
		// is installed?
		if(isInstalled(resource)) {
			// get file name
			String filename = getFileName(resource);
			// uninstall
			m_fileNames.remove(resource);
			m_installed.remove(filename);
			// successfully uninstalled
			return true;
		}
		// failure
		return false;
	}
	
	public boolean uninstall(String filename) {
		// is installed?
		if(isInstalled(filename)) {
			// get file name
			Object resource = getFileName(filename);
			// uninstall
			m_fileNames.remove(resource);
			m_installed.remove(filename);
			// successfully uninstalled
			return true;
		}
		// failure
		return false;
	}	
	
	protected boolean isPath(String object) {
		try {
			if (object != null) {
				return (new File(object)).exists();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	protected boolean createFile(String filename) {
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
	
	
	protected String getKey(Enum e, String suffix) {
		return getKey(e.getClass().getSimpleName()+"."+e.toString(),suffix);
	}
	
	
	protected String getKey(String prefix, String suffix) {
		return prefix + ((suffix!=null && !suffix.isEmpty()) ? "."+suffix : null);
	}

	protected String getText(String key, Object resource) {
		// translate
		if(resource instanceof ResourceBundle) {
			// cast to resource bundle
			ResourceBundle bundle =  (ResourceBundle)resource;
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
	
	protected String getTextFromInstalled(String key) {
		for(Object it: m_installed.values()) {
			String text = getText(key, it);
			if(text!=null && !text.isEmpty())
				return text;
		}
		//  not found
		return null;			
	}
			
	protected boolean setText(String key, String value, boolean create, boolean write, Object resource, String filename) {
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
						// update file name from installed?
						if(!isPath(filename)) {
							filename = getFileName(resource);
						}
						// forward
						return store(properties,filename);
					}
				}
			}
		}
		// failure!
		return false;
	}
	
	protected boolean setTextToInstalled(String key, String value, boolean create, boolean write) {
		for(Object it: m_installed.values()) {
			if(setText(key, value, create, write, it, null)) {
				// success!
				return true;
			}
		}
		//  failed
		return false;			
	}
	
	/**
	 * Get the properties file from resource
	 * 
	 * If resource is null, then create from file name
	 * 
	 */
	protected Properties getProperties(Object resource, String filename, boolean create) {
		// try to get from current information
		if(resource instanceof Properties) {
			return (Properties)resource;
		}
		else if(resource instanceof ResourceBundle){
			if(isInstalled(resource)) {
				return load(getFileName(resource));
			}
		}
		// does file not exist?
		if(!isPath(filename)) {
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
	protected Properties load(String fileName) {
		try {
			if(isPath(fileName)) {
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
	protected boolean store(Properties properties, String fileName) {
		try {
			if(isPath(fileName)) {
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
