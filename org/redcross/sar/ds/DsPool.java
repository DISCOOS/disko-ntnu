package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.AbstractCatalog;

public class DsPool extends AbstractCatalog<IDs<?>> {

	private static DsPool m_this;

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

	public IDs<?> create(Class<? extends IDs<?>> service, String oprID) {
		IDs<?> e = getItem(service, oprID);
		if(e!=null) {
			return e;
		}
		else {
			try {
				// get new instance
				Object obj = service.getConstructors()[0].newInstance(oprID);
				if(obj instanceof IDs<?>) {
					e = (IDs<?>)obj;
					create((IDs<?>)obj,oprID);
				}
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
		return e;
	}

}
