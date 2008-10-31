package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsPoolListener;
import org.redcross.sar.thread.IWorkLoop.LoopState;

public class DsPool {

	private static DsPool m_this;

	private final Map<String,Map<Class<?>,IDs<?>>> m_lists =
		new HashMap<String, Map<Class<?>,IDs<?>>>();

	private final EventListenerList m_listeners = new EventListenerList();

	/*========================================================
  	 * Constructor
  	 *======================================================== */

	DsPool() throws Exception {
		// forward
		super();
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
  	 * Public methods
  	 *======================================================== */

	public List<IDs<?>> getItems(String oprID) {
		List<IDs<?>> list = new ArrayList<IDs<?>>();
		Map<Class<?>,IDs<?>> map = m_lists.get(oprID);
		if(map!=null) {
			list.addAll(map.values());
		}
		return list;
	}

	public boolean contains(Class<? extends IDs<?>> c, String oprID) {
		for(IDs<?> e : getItems(oprID)) {
			if(e.getClass().equals(c)) return true;
		}
		return false;
	}

	public IDs<?> getItem(Class<? extends IDs<?>> c, String oprID) {
		Map<Class<?>,IDs<?>> map = m_lists.get(oprID);
		if(map!=null) {
			return map.get(c);
		}
		return null;
	}

	public IDs<?> install(Class<? extends IDs<?>> c, String oprID) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			return e;
		}
		else {
			try {
				// get new instance
				Object obj = c.getConstructors()[0].newInstance(oprID);
				if(obj instanceof IDs<?>) {
					e = (IDs<?>)obj;
					install((IDs<?>)obj,oprID);
				}
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return e;
	}

	private void install(IDs<?> e, String oprID) {
		Map<Class<?>,IDs<?>> map = m_lists.get(oprID);
		if(map==null) {
			map = new HashMap<Class<?>,IDs<?>>(1);
			m_lists.put(oprID,map);
		}
		map.put(e.getClass(),e);
		fireInstallEvent(e,0);
	}

	public boolean uninstall(Class<? extends IDs<?>> c, String oprID) {
		Map<Class<?>,IDs<?>> map = m_lists.get(oprID);
		if(map==null) {
			IDs<?> e = map.get(c);
			if(e!=null) {
				e.stop();
				map.remove(c);
				fireInstallEvent(e,1);
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public int start(String oprID) {
		int count = 0;
		for(IDs<?> e : getItems(oprID)) {
			if(start((Class<? extends IDs<?>>)e.getClass(),oprID)) count++;
		}
		return count;
	}

	public boolean start(Class<? extends IDs<?>> c, String oprID) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			return e.start();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public int stop(String oprID) {
		int count = 0;
		for(IDs<?> e : getItems(oprID)) {
			if(stop((Class<? extends IDs<?>>)e.getClass(),oprID)) count++;
		}
		return count;
	}

	public boolean stop(Class<? extends IDs<?>> c, String oprID) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			// cancel work
			return e.stop();
		}
		return false;
	}

	public int resume(String oprID) {
		int count = 0;
		// suspend old
		suspend(oprID);
		// resume new
		for(IDs<?> it : getItems(oprID)) {
			if(it.resume()) count++;
		}
		// finished
		return count;
	}

	public boolean resume(Class<? extends IDs<?>> c, String oprID) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			// resume work
			return e.resume();
		}
		return false;
	}

	public int suspend(String oprID) {
		int count = 0;
		// suspend old
		for(IDs<?> it : getItems(oprID)) {
			if(it.suspend()) count++;
		}
		// finished
		return count;
	}

	public boolean suspend(Class<? extends IDs<?>> c, String oprID) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			// suspend work
			return e.suspend();
		}
		return false;
	}


	public boolean isLoopState(Class<? extends IDs<?>> c, String oprID, LoopState state) {
		IDs<?> e = getItem(c, oprID);
		if(e!=null) {
			// suspend work
			return e.isLoopState(state);
		}
		return false;
	}

	public void addPoolListener(IDsPoolListener listener) {
		m_listeners.add(IDsPoolListener.class,listener);
	}

	public void removePoolListener(IDsPoolListener listener) {
		m_listeners.remove(IDsPoolListener.class,listener);
	}

	/*========================================================
  	 * Helper methods
  	 *======================================================== */

	private void fireInstallEvent(IDs<?> ds, int flags) {
		DsEvent.Install e = new DsEvent.Install(ds,flags);
		IDsPoolListener[] list = m_listeners.getListeners(IDsPoolListener.class);
		for(int i=0; i<list.length;i++) {
			list[i].handleInstallEvent(e);
		}
	}

}
