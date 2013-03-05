package org.redcross.sar.io;
 
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.redcross.sar.AbstractCatalog;
import org.redcross.sar.ds.DsPool;

public class IOMapperPool extends AbstractCatalog<IIOMapper<?>> {

	private static IOMapperPool m_this;
	private final static Logger m_logger = Logger.getLogger(DsPool.class);

	/*========================================================
  	 * Constructors
  	 *======================================================== */

	IOMapperPool() throws Exception {
		super(true);
	}

	/*========================================================
  	 * The singleton code
  	 *======================================================== */

	/**
	 * Get singleton instance of class
	 *
	 * @return Returns singleton instance of class
	 */
  	public static synchronized IOMapperPool getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("IOPool can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new IOMapperPool();
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
  	 * ISerciveFactory implementation
  	 *======================================================== */

  	public boolean isCreatorOf(Class<?> service) {
  		return IIOMapper.class.isAssignableFrom(service);
  	}

	public IIOMapper<?> create(Class<? extends IIOMapper<?>> service, Object id) {
		IIOMapper<?> e = getItem(service, id);
		if(e!=null) {
			return e;
		}
		else {
			// get name
			String name = service.getSimpleName();
			try {
				// get new instance
				Object obj = service.getConstructors()[0].newInstance(id);
				if(obj instanceof IIOMapper<?>) {
					e = (IIOMapper<?>)obj;
					create((IIOMapper<?>)obj,id);
				}
			} catch (Exception ex) {
				m_logger.error("Failed to create " + name + " instance", ex);
			}

		}
		return e;
	}

}
