/**
 *
 */
package org.redcross.sar;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.redcross.sar.mso.IDispatcherIf;
import org.redcross.sar.mso.DispatcherAdapter;
import org.redcross.sar.util.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <b>Singleton class for service management</b></p>
 * 
 * This singleton manages all installed services. Each service is uniquely identified by  
 * <code>IService::getID()</code>. If the IService instance is data source bound 
 * (<code>IService:isDataSourceBound()</code> is <code>true</code>), then 
 * <code>IService::getID()</code> equals <code>IDataSource::getID()</code>. Hence, data 
 * bound services can be created, started, stopped and destroyed using data source ids. </p>
 *
 * @author kennetgu
 *
 */
public class ServicePool {

	private static ServicePool m_this;

	private boolean m_isMaster = true;
	private boolean m_isAutomatic = true;

	private final ClassLoader m_loader;
	private final IDispatcherIf m_dispatcher;

	private final Set<Class<? extends IService>>
		m_installed = new HashSet<Class<? extends IService>>();

	@SuppressWarnings("unchecked")
	private final Map<Class<? extends IServiceCatalog>,IServiceCatalog<IService>>
		m_catalogs = new HashMap<Class<? extends IServiceCatalog>,IServiceCatalog<IService>>();
	
	private static final Logger m_logger = Logger.getLogger(ServicePool.class);

	/*========================================================
	 * Constructors
	 *======================================================== */

	private ServicePool() throws Exception {

		// get singletons
		m_loader = ClassLoader.getSystemClassLoader();
		m_dispatcher = Application.getInstance().getMsoModel().getDispatcher();

		// add listener
		m_dispatcher.addDispatcherListener(new DispatcherAdapter() {

			@Override
			public void onOperationCreated(String oprID, boolean isLoopback) {
				doAutoOn(oprID,false);
			}

			@Override
			public void onOperationActivated(String oprID) {
				doAutoOn(oprID,true);
			}

			@Override
			public void onOperationDeactivated(String oprID) {
				doAutoOff(oprID,false);
			}

			@Override
			public void onOperationFinished(String oprID, boolean isLoopback) {
				doAutoOff(oprID,true);
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
		// changed?
		if(m_isMaster != isMaster) {
			// update
			m_isMaster = isMaster;
		}
	}

	/**
	 * Add a service catalog.
	 *
	 * @param catalog - Service catalog instance
	 *
	 * @return boolean
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws SecurityException 
	 */
	@SuppressWarnings("unchecked")
	public boolean addCatalog(String catalog, boolean isSingleton) throws ClassNotFoundException, SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		// load class from library
		Class<?> cls = m_loader.loadClass(catalog);
		// is supported?
		if(IServiceCatalog.class.isAssignableFrom(cls)) {
			// forward
			return addCatalog((Class<IServiceCatalog<IService>>)cls,isSingleton);
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
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("unchecked")
	public boolean addCatalog(Class<IServiceCatalog<IService>> catalog, boolean isSingleton) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		// valid?
		if(!m_catalogs.containsKey(catalog.getClass())) {
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
				m_logger.info("IServiceCatalog added: " + catalog.getSimpleName());
				return true;
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
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public boolean removeCatalog(String catalog, boolean destroyAll) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(catalog);
		// is supported?
		if(IServiceCatalog.class.isAssignableFrom(cls)) {
			// forward
			return removeCatalog((Class<IServiceCatalog<IService>>)cls,destroyAll);
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
			m_logger.info("IServiceCatalog removed: " + catalog.getSimpleName());
			return true;
		}
		return false;
	}

	/**
	 * Get catalogs with created services from service type (class).
	 *
	 * @param service - service class
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
	 * Get catalogs with created services from service id.
	 *
	 * @param id - service id
	 *
	 * @return boolean
	 */
	public List<IServiceCatalog<IService>> getCatalogsInUse(Object id) {
		List<IServiceCatalog<IService>> catalogs = new ArrayList<IServiceCatalog<IService>>();
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			if(it.getItems(id).size()>0) {
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
		// initialize 
		int count = 0;
		// notify
		m_logger.info("installFromXmlFile()::started...");
		// load xml document
		Document doc = Utils.getXmlDoc(file);
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
				if(install(className)) count++;
			}
		}
		// notify
		m_logger.info("installFromXmlFile()::finished, number of services installed is " + count);
	}

	/**
	 * Install service from class name
	 *
	 * @param service - The service class name.
	 *
	 * @return boolean
	 * @throws ClassNotFoundException 
	 */

	@SuppressWarnings("unchecked")
	public boolean install(String service) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(service);
		// is supported?
		if(isSupported(cls)) {
			// validate
			if(!isInstalled((Class<IService>)cls)) {
				// notify
				m_logger.info("IService installed: " + service);
				// finished
				return m_installed.add((Class<IService>)cls);
			}
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
	 * @throws ClassNotFoundException 
	 */

	@SuppressWarnings("unchecked")
	public boolean uninstall(String service, boolean destroyAll) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(service);
		// is supported?
		if(isSupported(cls)) {
			// cast to Class<IService>
			Class<IService> c = (Class<IService>)cls;
			// validate
			if(isInstalled(c)) {
				// uninstall
				if(destroyAll) destroyAll(c);				
				m_installed.remove(c);
				// notify
				m_logger.info("IService uninstalled: " + service);
				// finished
				return true;
			}
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
	 * @throws ClassNotFoundException 
	 */
	public boolean isSupported(String servise) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(servise);
		// validate
		return isSupported(cls);
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
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public boolean isInstalled(String service) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(service);
		// is supported?
		if(isSupported(cls)) {
			// validate
			return isInstalled((Class<IService>)cls);
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
	 * Get all installed services from service id
	 *
	 * @param service - Service class
	 * @param id - service id
	 *
	 * @return boolean
	 */
	public Set<Class<? extends IService>> getInstalled(Object id) {
		Set<Class<? extends IService>> set = new HashSet<Class<? extends IService>>();
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			List<IService> list = it.getItems(id);
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
	public void createFromXmlFile(File file, Object id) throws Exception {
		// load xml document
		Document doc = Utils.getXmlDoc(file);
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
				create(className,id);
			}
		}
	}

	/**
	 * Create all installed services for given service id
	 *
	 * @param id - service id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> createAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// create installed services
		for(Class<? extends IService> it : m_installed) {
			IService service = create(it,id);
			if(service!=null) list.add(service);
		}
		return list;
	}

	/**
	 * Create given service for given service id
	 *
	 * @param service - Service class name
	 * @param id - service id
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public IService create(String service, Object id) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(service);
		// is supported?
		if(isSupported(service)) {
			// forward
			return create((Class<IService>)cls,id);
		}
		// failed
		return null;
	}

	/**
	 * create given service
	 *
	 * @param service - Service class
	 * @param id - service id
	 */
	@SuppressWarnings("unchecked")
	public IService create(Class<? extends IService> service, Object id) {
		// is installed?
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				// create service
				return catalog.create(service, id);
			}
		}
		// failed
		return null;
	}

	/**
	 * Destroy all installed services
	 *
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll() {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// create installed services
		for(IService it : getItems()) {
			IService service = destroy(it.getClass(),it.getID());
			if(service!=null) list.add(service);
		}
		return list;
	}
	
	/**
	 * Destroy all installed services for given service id
	 *
	 * @param id - service id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// create installed services
		for(Class<? extends IService> it : m_installed) {
			IService service = destroy(it,id);
			if(service!=null) list.add(service);
		}
		return list;
	}

	/**
	 * Destroy installed services for all operations
	 *
	 * @param id - service id
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
	 * @param id - service id
	 */
	@SuppressWarnings("unchecked")
	public List<IService> destroyAll(Class<? extends IService> service, List<Object> ids) {
		// get catalog
		IServiceCatalog<IService> catalog = getCatalog(service);
		// found?
		if(catalog!=null) {
			return catalog.destroyAll(service,ids);
		}
		return null;
	}

	/**
	 * destroy given service for given service id
	 *
	 * @param service - Service class name
	 * @param id - service id
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public IService destroy(String service, Object id) throws ClassNotFoundException {
		// load class from library
		Class<?> cls = m_loader.loadClass(service);
		// is supported?
		if(isSupported(cls)) {
			// forward
			return destroy((Class<IService>)cls,id);
		}
		// failed
		return null;
	}

	/**
	 * destroy given service
	 *
	 * @param service - Service class
	 */
	public IService destroy(Class<? extends IService> service, Object id) {
		// is installed?
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				// forward
				return catalog.destroy(service, id);
			}
		}
		// failed
		return null;
	}

	/**
	 * Check if given service is created for given service id
	 *
	 * @param service - service to look for
	 * @param id - service id
	 *
	 * @return boolean
	 */
	public boolean contains(Class<? extends IService> service, Object id) {
		// get catalog
		IServiceCatalog<IService> catalog = getCatalog(service);
		// found?
		if(catalog!=null) {
			return catalog.contains(service,id);
		}
		return false;
	}

	/**
	 * Return instance of given service for given service id
	 *
	 * @param service - service to look for
	 * @param id - service id
	 *
	 * @return IService
	 */
	public IService getItem(Class<? extends IService> service,Object id) {
		// validate
		if(isInstalled(service)) {
			IServiceCatalog<IService> catalog = getCatalog(service);
			if(catalog!=null) {
				return catalog.getItem(service, id);
			}
		}
		return null;
	}

	/**
	 * Get all created services.
	 *
	 * @return List
	 */
	public List<IService> getItems() {
		// initialize
		List<IService> items = new ArrayList<IService>();
		// loop over catalogs
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			items.addAll(it.getItems());
		}
		return items;
	}
	
	/**
	 * Get all created service for given service id.
	 *
	 * @param id - service id
	 *
	 * @return List
	 */
	public List<IService> getItems(Object id) {
		// initialize
		List<IService> items = new ArrayList<IService>();
		// loop over catalogs
		for(IServiceCatalog<IService> it : m_catalogs.values()) {
			List<IService> list = it.getItems(id);
			for(IService service : list) {
				if(m_installed.contains(service.getClass())) {
					items.add(service);
				}
			}
		}
		return items;
	}

	/**
	 * Start all installed services for given service id.
	 *
	 * @param id - service id
	 *
	 */
	public List<IService> startAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(id);
		// forward
		for(IService it : inUse) {
			it = start(it.getClass(),id);
			if(it!=null) list.add(it);
		}
		return list;
	}

	/**
	 * Start a given service for given service id.
	 *
	 * @param service - Service class
	 * @param id - service id
	 *
	 * @return List
	 */
	public IService start(Class<? extends IService> service, Object id) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.start(service, id);
			}
		}
		return null;
	}

	/**
	 * Resume all installed services that are suspended for given service id.
	 *
	 * @param id - service id
	 *
	 */
	public List<IService> resumeAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(id);
		// forward
		for(IService it : inUse) {
			it = resume(it.getClass(),id);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Resume installed service that is suspended for given service id.
	 *
	 * @param service - Service class
	 * @param id - service id
	 *
	 */
	public IService resume(Class<? extends IService> service, Object id) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.resume(service, id);
			}
		}
		return null;
	}

	/**
	 * Suspend all installed services that are started for given service id.
	 *
	 * @param id - service id
	 *
	 */
	public List<IService> suspendAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(id);
		// forward
		for(IService it : inUse) {
			it = suspend(it.getClass(),id);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Suspend installed service that is started for given service id.
	 *
	 * @param service - Service class
	 * @param id - service id
	 *
	 */
	public IService suspend(Class<? extends IService> service, Object id) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.suspend(service, id);
			}
		}
		return null;
	}

	/**
	 * Stop all installed services that are started for given service id.
	 *
	 * @param id - service id
	 *
	 */
	public List<IService> stopAll(Object id) {
		// initialize
		List<IService> list = new ArrayList<IService>();
		// get services in use
		List<IService> inUse = getItems(id);
		// forward
		for(IService it : inUse) {
			it = stop(it.getClass(),id);
			if(it!=null) list.add(it);
		}
		return list;  	}

	/**
	 * Stop installed service that is started for given service id.
	 *
	 * @param service - Service class
	 * @param id - service id
	 *
	 */
	public IService stop(Class<? extends IService> service, Object id) {
		// validate
		if(isInstalled(service)) {
			// get catalog
			IServiceCatalog<IService> catalog = getCatalog(service);
			// has catalog?
			if(catalog!=null) {
				return catalog.stop(service, id);
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
