package org.redcross.sar.ds;
 
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.redcross.sar.AbstractCatalog;

public class DsPool extends AbstractCatalog<IDs<?>> {

	private static DsPool m_this;
	private final static Logger m_logger = Logger.getLogger(DsPool.class);

	/*========================================================
  	 * Constructors
  	 *======================================================== */

	DsPool() throws Exception {
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
  	public static synchronized DsPool getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DsPool can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new DsPool();
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
  		return IDs.class.isAssignableFrom(service);
  	}

	public IDs<?> create(Class<? extends IDs<?>> service, Object id) {
		IDs<?> e = getItem(service, id);
		if(e!=null) {
			return e;
		}
		else {
			// get name
			String name = service.getSimpleName();
			try {
				// get new instance
				Object obj = service.getConstructors()[0].newInstance(id);
				if(obj instanceof IDs<?>) {
					e = (IDs<?>)obj;
					create((IDs<?>)obj,id);
				}
			} catch (Exception ex) {
				m_logger.error("Failed to create " + name + " instance", ex);
			}
		}
		return e;
	}

}
