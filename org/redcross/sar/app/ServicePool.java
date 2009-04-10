/**
 *
 */
package org.redcross.sar.app;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.modeldriver.ModelDriverAdapter;
import org.redcross.sar.mso.MsoModelImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Singleton class for automatic start of services
 *
 * @author kennetgu
 *
 */
public class ServicePool {

	private static ServicePool m_this;

	private boolean m_isMaster = true;
	private boolean m_isAutomatic = true;

	private final ClassLoader m_loader;
	private final IModelDriverIf m_driver;

	private final Set<Class<? extends IService>>
	m_installed = new HashSet<Class<? extends IService>>();

	@SuppressWarnings("unchecked")
	private final Map<Class<? extends IServiceCatalog>,IServiceCatalog<IService>>
	m_catalogs = new HashMap<Class<? extends IServiceCatalog>,IServiceCatalog<IService>>();

	/*========================================================
	 * Constructors
	 *======================================================== */

	private ServicePool() throws Exception {

		// get singletons
		m_loader = ClassLoader.getSystemClassLoader();
		m_driver = MsoModelImpl.getInstance().getModelDriver();

		// add listener
		m_driver.addModelDriverListener(new ModelDriverAdapter() {

			@Override
			public void onOperationCreated(String oprID, boolean current) {
				doAutoOn(oprID,false);
			}

			@Override
			public void onOperationFinished(String oprID, boolean current) {
				doAutoOff(oprID,true);
			}

			@Override
			public void onOperationActivated(String oprID) {
				doAutoOn(oprID,true);
			}

			@Override
			public void onOperationDeactivated(String oprID) {
				doAutoOff(oprID,false);
			}

			private void doAutoOn(String oprID, boolean isActivated) {
				if(m_isAutomatic) {
					/* ============================================
					 *
					 * If ServicePool is master, services for all
					 * operations are both created and started,
					 *
					 * If server is not master, only services for
					 * the active operations is created and started.
					 *
					 * ============================================ */
					if(m_isMaster) {
						createAll(oprID);
						startAll(oprID);
					}
					else if(isActivated) {
						createAll(oprID);
						startAll(oprID);
					}
				}
			}

			private void doAutoOff(String oprID, boolean isDestroyed) {
				if(m_isAutomatic) {
					/* ============================================
					 *
					 * If ServicePool is master, services for all
					 * operations should be keep running until
					 * it is destroyed.
					 *
					 * If server is not master, only services for
					 * the active operations is kept running.
					 *
					 * ============================================ */
					if(isDestroyed) {
						destroyAll(oprID);
					}
					else if (!m_isMaster) {
						stopAll(oprID);
					}
				}
			}

		});
	}

	/*========================================================
	 * The singleton code
	 *======================================================== */

	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
	public static synchronized ServicePool getInstance() throws Exception {
		if (m_this == null) {
			// only allowed to be created on the AWT thread!
			if(!SwingUtilities.isEventDispatchThread())
				throw new Exception("WorkPool can only " +
				"be instansiated on the Event Dispatch Thread");
			// it's ok, we can call this constructor
			m_this = new ServicePool();
		}
		return m_this;
	}

	/**
	 * Method overridden to protect singleton
	 *
	 * @throws CloneNotSupportedException
	 * @return Returns nothing. Method overridden to protect singleton
	 */
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
		// that'll teach 'em
	}

	/*========================================================
	 * Public methods
	 *======================================================== */

	public boolean isAutomatic() {
		return m_isAutomatic;
	}

	public void setAutomatic(boolean isAutomatic) {
		m_isAutomatic = isAutomatic;
	}

	public boolean isMaster() {
		return m_isMaster;
	}

	public void setMaster(boolean isMaster) {
		m_isMaster = isMaster;
	}

	/**
	 * Add a service catalog.
	 *
	 * @param catalog - Service catalog instance
	 *
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean addCatalog(String catalog, boolean isSingleton) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(catalog);
			// is supported?
			if(IServiceCatalog.class.isAssignableFrom(cls)) {
				// forward
				return addCatalog((Class<IServiceCatalog<IService>>)cls,isSingleton);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;

	}

	/**
	 * Add a service catalog.
	 *
	 * @param catalog - Service catalog class
	 * @param isSingleton - If <code>true</code>, method <code>getInstance()</code> is used.
	 *
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean addCatalog(Class<IServiceCatalog<IService>> catalog, boolean isSingleton) {
		// valid?
		if(!m_catalogs.containsKey(catalog.getClass())) {
			try {
				// initialize
				Object obj = null;
				// is singleton?
				if(isSingleton) {
					// get instance method
					Method method =  catalog.getMethod("getInstance");
					obj = method.invoke(this);
				}
				else {
					// get new instance
					obj = catalog.getConstructors()[0].newInstance();
				}
				// success?
				if(obj instanceof IServiceCatalog) {
					m_catalogs.put(catalog,(IServiceCatalog)obj);
					return true;
				}
			} catch (NoSuchMethodException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IllegalArgumentException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (SecurityException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (InstantiationException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IllegalAccessException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (InvocationTargetException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Remove given service catalog
	 *
	 * @param catalog - Service catalog instance
	 * @param destroyAll - if <code>true</code>, destroy all services
	 *
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean removeCatalog(String catalog, boolean destroyAll) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(catalog);
			// is supported?
			if(IServiceCatalog.class.isAssignableFrom(cls)) {
				// forward
				return removeCatalog((Class<IServiceCatalog<IService>>)cls,destroyAll);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;

	}

	/**
	 * Remove given service catalog.
	 *
	 * @param catalog - Service catalog instance
	 * @param destroyAll - if <code>true</code>, destroy all services
	 *
	 * @return boolean
	 */
	public boolean removeCatalog(Class<IServiceCatalog<IService>> catalog, boolean destroyAll) {
		if(m_catalogs.containsKey(catalog)) {
			IServiceCatalog<IService> instance = m_catalogs.get(catalog);
			if(destroyAll) instance.destroyAll();
			m_catalogs.remove(catalog);
			return true;
		}
		return false;
	}

	/**
	 * Get catalogs with created services from operation id.
	 *
	 * @param oprID - operation id
	 *
	 * @return boolean
	 */
	public IServiceCatalog<IService> getCatalog(Class<? extends IService> service) {
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			if(it.isCreatorOf(service)) {
				return it;
			}
		}
		return null;
	}

	/**
	 * Get catalogs with created services from operation id.
	 *
	 * @param oprID - operation id
	 *
	 * @return boolean
	 */
	public List<IServiceCatalog<IService>> getCatalogsInUse(String oprID) {
		List<IServiceCatalog<IService>> catalogs = new ArrayList<IServiceCatalog<IService>>();
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			if(it.getItems(oprID).size()>0) {
				catalogs.add(it);
			}
		}
		return catalogs;
	}

	/**
	 * Install services from file
	 *
	 * @throws Exception
	 */
	public void installFromXmlFile(File file) throws Exception {
		// load xml document
		FileInputStream instream = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(instream);
		// get all service catalogs
		NodeList catalogs = doc.getElementsByTagName("IServiceCatalog");
		// install service catalogs
		for (int i = 0; i < catalogs.getLength(); i++) {
			// get catalog
			Element catalog = (Element)catalogs.item(i);
			// get class name and
			String className = catalog.getAttribute("classname");
			boolean isSingleton = isSingleton(catalog);
			// add catalog
			addCatalog(className, isSingleton);
			// get all services in catalog
			NodeList services = catalog.getElementsByTagName("IService");
			// install services
			for (int j = 0; j < services.getLength(); j++) {
				Element service = (Element)services.item(j);
				className = service.getAttribute("classname");
				// install service from class name
				install(className);
			}
		}
	}

	/**
	 * Install service from class name
	 *
	 * @param service - The service class name.
	 *
	 * @return boolean
	 */

	@SuppressWarnings("unchecked")
	public boolean install(String service) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(service);
			// is supported?
			if(isSupported(cls)) {
				// validate
				if(!isInstalled((Class<IService>)cls)) {
					return m_installed.add((Class<IService>)cls);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	/**
	 * Uninstall service from class name
	 *
	 * @param service - The service class name.
	 *
	 * @return boolean
	 */

	@SuppressWarnings("unchecked")
	public boolean uninstall(String service, boolean destroyAll) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(service);
			// is supported?
			if(isSupported(cls)) {
				// cast to Class<IService>
				Class<IService> c = (Class<IService>)cls;
				// validate
				if(isInstalled(c)) {
					if(destroyAll) destroyAll(c);
					return m_installed.remove(c);
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	/**
	 * Check if given service class is supported.
	 *
	 * @param service - Service class name
	 *
	 * @return boolean
	 */
	public boolean isSupported(String servise) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(servise);
			// validate
			return isSupported(cls);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	/**
	 * Check if given service class is supported.
	 *
	 * @param service - Service class
	 *
	 * @return boolean
	 */
	public boolean isSupported(Class<?> service) {
		for(IServiceCatalog<?> it : m_catalogs.values()) {
			if(it.isCreatorOf(service)) return true;
		}
		return false;
	}

	/**
	 * Check if given service is installed.
	 *
	 * @param service - Service class name
	 *
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean isInstalled(String service) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(service);
			// is supported?
			if(isSupported(cls)) {
				// validate
				return isInstalled((Class<IService>)cls);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return false;
	}

	/**
	 * Check if given service is installed.
	 *
	 * @param service - Service class
	 *
	 * @return boolean
	 */
	public boolean isInstalled(Class<? extends IService> service) {
		return m_installed.contains(service);
	}

	/**
	 * Get all installed services.
	 *
	 * @param service - Service class
	 * @param oprID - operation id
	 *
	 * @return boolean
	 */
	public Set<Class<? extends IService>> getInstalled(String oprID) {
		Set<Class<? extends IService>> set = new HashSet<Class<? extends IService>>();
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			List<IService> list = it.getItems(oprID);
			for(IService service : list) {
				if(m_installed.contains(service.getClass())) {
					set.add(service.getClass());
				}
			}
		}
		return set;
	}


	/**
	 * Create services from file
	 *
	 * @throws Exception
	 */
	public void createFromXmlFile(File file, String oprID) throws Exception {
		// load xml document
		FileInputStream instream = new FileInputStream(file);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(instream);
		// get all service catalogs
		NodeList catalogs = doc.getElementsByTagName("DiskoService");
		// install service catalogs
		for (int i = 0; i < catalogs.getLength(); i++) {
			// get catalog
			Element catalog = (Element)catalogs.item(i);
			// get all services in catalog
			NodeList services = catalog.getElementsByTagName("DiskoService");
			// install services
			for (int j = 0; j < services.getLength(); j++) {
				Element service = (Element)services.item(j);
				String className = service.getAttribute("classname");
				// forward
				install(className);
				create(className,oprID);
			}
		}
	}

	/**
	 * Create all installed services for given operation
	 *
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> createAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// create installed services
		for(Class<? extends IService> it : m_installed) {
			IService service = create(it,oprID);
			if(service!=null) list.add(service);
		}
		return list;
	}

	/**
	 * Create given service for given operation
	 *
	 * @param service - Service class name
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public IService create(String service, String oprID) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(service);
			// is supported?
			if(isSupported(service)) {
				// forward
				return create((Class<IService>)cls,oprID);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return null;
	}

	/**
	 * create given service
	 *
	 * @param service - Service class
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public IService create(Class<? extends IService> service, String oprID) {
		// is installed?
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				// create service
				return catalog.create(service, oprID);
			}
		}
		// failed
		return null;
	}

	/**
	 * Destroy all installed services for given operation
	 *
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// create installed services
		for(Class<? extends IService> it : m_installed) {
			IService service = destroy(it,oprID);
			if(service!=null) list.add(service);
		}
		return list;
	}

	/**
	 * Destroy installed services for all operations
	 *
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll(Class<? extends IService> service) {
		// get catalog
		IServiceCatalog<IService> catalog = getCatalog(service);
		// found?
		if(catalog!=null) {
			return catalog.destroyAll(service);
		}
		return null;
	}

	/**
	 * Destroy installed services for given list of operations
	 *
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll(Class<? extends IService> service, List<String> oprIDs) {
		// get catalog
		IServiceCatalog<IService> catalog = getCatalog(service);
		// found?
		if(catalog!=null) {
			return catalog.destroyAll(service,oprIDs);
		}
		return null;
	}

	/**
	 * destroy given service for given operation
	 *
	 * @param service - Service class name
	 * @param oprID - operation id
	 */
	@SuppressWarnings("unchecked")
	public IService destroy(String service, String oprID) {
		try {
			// load class from library
			Class<?> cls = m_loader.loadClass(service);
			// is supported?
			if(isSupported(cls)) {
				// forward
				return destroy((Class<IService>)cls,oprID);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// failed
		return null;
	}

	/**
	 * destroy given service
	 *
	 * @param service - Service class
	 */
	public IService destroy(Class<? extends IService> service, String oprID) {
		// is installed?
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				// forward
				return catalog.destroy(service, oprID);
			}
		}
		// failed
		return null;
	}

	/**
	 * Check if given service is created for given operation
	 *
	 * @param service - service to look for
	 * @param oprID - operation id
	 *
	 * @return boolean
	 */
	public boolean contains(Class<? extends IService> service, String oprID) {
		// get catalog
		IServiceCatalog<IService> catalog = getCatalog(service);
		// found?
		if(catalog!=null) {
			return catalog.contains(service,oprID);
		}
		return false;
	}

	/**
	 * Return instance of given service for given operation
	 *
	 * @param service - service to look for
	 * @param oprID - operation id
	 *
	 * @return IService
	 */
	public IService getItem(Class<? extends IService> service,String oprID) {
		// validate
		if(isInstalled(service)) {
			IServiceCatalog<IService> catalog = getCatalog(service);
			if(catalog!=null) {
				return catalog.getItem(service, oprID);
			}
		}
		return null;
	}


	/**
	 * Get all created service for given operation.
	 *
	 * @param oprID - Operation id
	 *
	 * @return List
	 */
	public List<IService> getItems(String oprID) {
		// initialize
		List<IService> items = new ArrayList<IService>();
		// loop over catalogs
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			List<IService> list = it.getItems(oprID);
			for(IService service : list) {
				if(m_installed.contains(service.getClass())) {
					items.add(service);
				}
			}
		}
		return items;
	}

	/**
	 * Start all installed services for given operation.
	 *
	 * @param oprID - Operation id
	 *
	 */
	public List<IService> startAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(oprID);
		// forward
		for(IService it : inUse) {
			it = start(it.getClass(),oprID);
			if(it!=null) list.add(it);
		}
		return list;
	}

	/**
	 * Start a given service for given operation.
	 *
	 * @param service - Service class
	 * @param oprID - Operation id
	 *
	 * @return List
	 */
	public IService start(Class<? extends IService> service, String oprID) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.start(service, oprID);
			}
		}
		return null;
	}

	/**
	 * Resume all installed services that are suspended for given operation.
	 *
	 * @param oprID - Operation id
	 *
	 */
	public List<IService> resumeAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(oprID);
		// forward
		for(IService it : inUse) {
			it = resume(it.getClass(),oprID);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Resume installed service that is suspended for given operation.
	 *
	 * @param service - Service class
	 * @param oprID - Operation id
	 *
	 */
	public IService resume(Class<? extends IService> service, String oprID) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.resume(service, oprID);
			}
		}
		return null;
	}

	/**
	 * Suspend all installed services that are started for given operation.
	 *
	 * @param oprID - Operation id
	 *
	 */
	public List<IService> suspendAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(oprID);
		// forward
		for(IService it : inUse) {
			it = suspend(it.getClass(),oprID);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Suspend installed service that is started for given operation.
	 *
	 * @param service - Service class
	 * @param oprID - Operation id
	 *
	 */
	public IService suspend(Class<? extends IService> service, String oprID) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.suspend(service, oprID);
			}
		}
		return null;
	}

	/**
	 * Stop all installed services that are started for given operation.
	 *
	 * @param oprID - Operation id
	 *
	 */
	public List<IService> stopAll(String oprID) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(oprID);
		// forward
		for(IService it : inUse) {
			it = stop(it.getClass(),oprID);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Stop installed service that is started for given operation.
	 *
	 * @param service - Service class
	 * @param oprID - Operation id
	 *
	 */
	public IService stop(Class<? extends IService> service, String oprID) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.stop(service, oprID);
			}
		}
		return null;
	}

	/*========================================================
	 * Helper methods
	 *======================================================== */

	private boolean isSingleton(Element catalog) {
		if(catalog.hasAttribute("isSingleton")) {
			String text = catalog.getAttribute("isSingleton");
			if(text!=null && !text.isEmpty()) {
				return new Boolean(text);
			}
		}
		return false;
	}


}
