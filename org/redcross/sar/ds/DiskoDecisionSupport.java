package org.redcross.sar.ds;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.redcross.sar.thread.DiskoWorkPool;

public class DiskoDecisionSupport {

	private static DiskoDecisionSupport m_this;
	
	private final Map<String,Map<Class<?>,IDsIf<?>>> m_lists = 
		new HashMap<String, Map<Class<?>,IDsIf<?>>>();
	
	private final DiskoWorkPool m_workPool;
	
	/*========================================================
  	 * Constructor
  	 *========================================================
  	 */
	DiskoDecisionSupport() throws Exception {
		// forward
		super();
		// get 
		m_workPool = DiskoWorkPool.getInstance();
	}
	
	
	/*========================================================
  	 * The singleton code
  	 *========================================================
  	 */

	/**
	 * Get singleton instance of class
	 * 
	 * @return Returns singleton instance of class
	 */
  	public static synchronized DiskoDecisionSupport getInstance() throws Exception {
  		if (m_this == null) {
  			// only allowed to be created on the AWT thread!
  			if(!SwingUtilities.isEventDispatchThread())
  				throw new Exception("DecisionSupport can only " +
  						"be instansiated on the Event Dispatch Thread");
  			// it's ok, we can call this constructor
  			m_this = new DiskoDecisionSupport();
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
  	 *========================================================
  	 */
  	 
	public List<IDsIf<?>> getEstimators(String oprID) {
		Map<Class<?>,IDsIf<?>> map = m_lists.get(oprID);
		if(map!=null) {
			return new ArrayList<IDsIf<?>>(map.values());
		}
		return null;
	}  	

	public boolean isInstalled(Class<? extends IDsIf<?>> c, String oprID) {
		for(IDsIf<?> e : getEstimators(oprID)) {
			if(e.getClass().equals(c)) return true;
		}
		return false;				
	}
	
	public IDsIf<?> getEstimator(Class<? extends IDsIf<?>> c, String oprID) {
		Map<Class<?>,IDsIf<?>> map = m_lists.get(oprID);
		if(map!=null) {
			return map.get(c);
		}		
		return null;
	}
	
	public IDsIf<?> install(Class<? extends IDsIf<?>> c, String oprID) {
		IDsIf<?> e = getEstimator(c, oprID);
		if(e!=null) {
			return e;
		}
		else {
			try {
				// get new instance
				Object obj = c.getConstructors()[0].newInstance(oprID);
				if(obj instanceof IDsIf<?>) {
					e = (IDsIf<?>)obj;
					install((IDsIf<?>)obj,oprID);
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
	
	private void install(IDsIf<?> e, String oprID) {
		Map<Class<?>,IDsIf<?>> map = m_lists.get(oprID);
		if(map==null) {
			map = new HashMap<Class<?>,IDsIf<?>>(1);
			m_lists.put(oprID,map);
		}
		map.put(e.getClass(),e);		
	}
			
	public boolean uninstall(Class<? extends IDsIf<?>> c, String oprID) {
		Map<Class<?>,IDsIf<?>> map = m_lists.get(oprID);
		if(map==null) {
			IDsIf<?> e = map.get(c);			
			if(e!=null) {
				// forward
				e.stop();				
				map.remove(c);
				return true;				
			}
		}
		return false;
	}
	
	public int start(String oprID) {
		int count = 0;
		for(IDsIf<?> e : getEstimators(oprID)) {
			if(start((Class<? extends IDsIf<?>>)e.getClass(),oprID)) count++;
		}
		return count;						
	}
	
	public boolean start(Class<? extends IDsIf<?>> c, String oprID) {
		boolean bFlag = false;
		IDsIf<?> e = getEstimator(c, oprID);
		if(e!=null) {
			// schedule?
			if(!m_workPool.containsWork(e)) {
				m_workPool.schedule(e);
				bFlag = true;
			}
			else if(!e.isWorking()) {
				bFlag = e.resume();
			}			
		}
		// finished
		return bFlag;
	}

	public int stop(String oprID) {
		int count = 0;
		for(IDsIf<?> e : getEstimators(oprID)) {
			if(stop((Class<? extends IDsIf<?>>)e.getClass(),oprID)) count++;
		}
		return count;						
	}

	public boolean stop(Class<? extends IDsIf<?>> c, String oprID) {
		boolean bFlag = false;
		IDsIf<?> e = getEstimator(c, oprID);
		if(e!=null) {
			// cancel work
			e.stop();
		}
		// finished
		return bFlag;
	}
	
	public int resume(String oprID) {
		int count = 0;
		// suspend old
		suspend(oprID);
		// resume new
		for(IDsIf<?> it : getEstimators(oprID)) {
			if(it.resume()) count++;
		}
		// finished
		return count;
	}
	
	public int suspend(String oprID) {
		int count = 0;
		// suspend old
		for(IDsIf<?> it : getEstimators(oprID)) {
			if(it.suspend()) count++;
		}
		// finished
		return count;
	}
	
	public int isWorking(String oprID) {
		int count = 0;
		// suspend old
		for(IDsIf<?> it : getEstimators(oprID)) {
			if(it.isWorking()) count++;
		}		
		// finished
		return count;
	}
}
