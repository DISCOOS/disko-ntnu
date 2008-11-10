package org.redcross.sar.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.redcross.sar.app.IService;
import org.redcross.sar.app.IServiceCatalog;
import org.redcross.sar.app.event.CatalogEvent;
import org.redcross.sar.app.event.ICatalogListener;
import org.redcross.sar.work.IWorkLoop.LoopState;

public abstract class AbstractCatalog<T extends IService> implements IServiceCatalog<T>{

	private final boolean m_isSingleton;

	private final Map<String,Map<Class<?>,T>> m_instances =
		new HashMap<String, Map<Class<?>,T>>();

	private final EventListenerList m_listeners = new EventListenerList();

	/*========================================================
  	 * Constructors
  	 *======================================================== */

	protected AbstractCatalog(boolean isSingleton) {
		// prepare
		m_isSingleton = isSingleton;
	}

	/*========================================================
  	 * ISerciveFactory implementation
  	 *======================================================== */

	public boolean isSingleton() {
		return m_isSingleton;
	}

  	public abstract boolean isCreatorOf(Class<?> service);

	public List<T> getItems(String oprID) {
		List<T> list = new ArrayList<T>();
		Map<Class<?>,T> map = m_instances.get(oprID);
		if(map!=null) {
			list.addAll(map.values());
		}
		return list;
	}

	public List<String> getOperations() {
		return new ArrayList<String>(m_instances.keySet());
	}

	public boolean contains(Class<? extends T> service) {
		return getItems(service).size()>0;
	}

	public List<T> getItems(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(Map<Class<?>,T> map : m_instances.values()) {
			T item = map.get(service);
			if(item!=null) list.add(item);

		}
		return list;
	}

	public boolean contains(Class<? extends T> service, String oprID) {
		for(T e : getItems(oprID)) {
			if(e.getClass().equals(service)) return true;
		}
		return false;
	}

	public T getItem(Class<? extends T> service, String oprID) {
		Map<Class<?>,T> map = m_instances.get(oprID);
		if(map!=null) {
			return map.get(service);
		}
		return null;
	}

	public List<T> createAll(Class<? extends T> service, List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String oprID : oprIDs) {
			T item = create(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public abstract T create(Class<? extends T> service, String oprID);

	public List<T> destroyAll() {
		return destroyAll(getOperations());
	}

	@SuppressWarnings("unchecked")
	public List<T> destroyAll(String oprID) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(oprID)) {
			T item = destroy((Class<? extends T>)it.getClass(),oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<T> destroyAll(List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String oprID : oprIDs) {
			list.addAll(destroyAll(oprID));
		}
		return list;
	}

	public List<T> destroyAll(Class<? extends T> service) {
		return destroyAll(service,getOperations());
	}

	public List<T> destroyAll(Class<? extends T> service, List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String oprID : oprIDs) {
			T item = destroy(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public T destroy(Class<? extends T> service, String oprID) {
		Map<Class<?>,T> map = m_instances.get(oprID);
		if(map==null) {
			T e = map.get(service);
			if(e!=null) {
				e.stop();
				map.remove(service);
				fireCatalogEvent(e,1);
				return e;
			}
		}
		return null;
	}

	public List<T> startAll() {
		return startAll(getOperations());
	}

	public List<T> startAll(String oprID) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(oprID)) {
			if(it.start()) list.add(it);
		}
		return list;
	}

	public List<T> startAll(List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String it : oprIDs) {
			list.addAll(startAll(it));
		}
		return list;
	}

	public List<T> startAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(String oprID : getOperations()) {
			T item = start(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> startAll(Class<? extends T> service, List<String> oprIDs) {
		return startAll(service,oprIDs);
	}

	public T start(Class<? extends T> service, String oprID) {
		T it = getItem(service, oprID);
		if(it!=null) {
			if(it.start()) return it;
		}
		return null;
	}

	public List<T> stopAll() {
		return stopAll(getOperations());
	}

	public List<T> stopAll(String oprID) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(oprID)) {
			if(it.stop()) list.add(it);
		}
		return list;
	}

	public List<T> stopAll(List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String it : oprIDs) {
			list.addAll(stopAll(it));
		}
		return list;
	}

	public List<T> stopAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(String oprID : getOperations()) {
			T item = stop(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> stopAll(Class<? extends T> service, List<String> oprIDs) {
		return stopAll(service,oprIDs);
	}

	public T stop(Class<? extends T> service, String oprID) {
		T it = getItem(service, oprID);
		if(it!=null) {
			// cancel work
			if(it.stop()) return it;
		}
		return null;
	}

	public List<T> resumeAll() {
		return resumeAll(getOperations());
	}

	public List<T> resumeAll(String oprID) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(oprID)) {
			if(it.resume()) list.add(it);
		}
		return list;
	}

	public List<T> resumeAll(List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String it : oprIDs) {
			list.addAll(resumeAll(it));
		}
		return list;
	}

	public List<T> resumeAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(String oprID : getOperations()) {
			T item = resume(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> resumeAll(Class<? extends T> service, List<String> oprIDs) {
		return resumeAll(service,oprIDs);
	}

	public T resume(Class<? extends T> service, String oprID) {
		T it = getItem(service, oprID);
		if(it!=null) {
			// resume work
			if(it.resume()) return it;
		}
		return null;
	}

	public List<T> suspendAll() {
		return suspendAll(getOperations());
	}

	public List<T> suspendAll(String oprID) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(oprID)) {
			if(it.suspend()) list.add(it);
		}
		return list;
	}

	public List<T> suspendAll(List<String> oprIDs) {
		List<T> list = new ArrayList<T>();
		for(String it : oprIDs) {
			list.addAll(suspendAll(it));
		}
		return list;
	}

	public List<T> suspendAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(String oprID : getOperations()) {
			T item = suspend(service,oprID);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> suspendAll(Class<? extends T> service, List<String> oprIDs) {
		return suspendAll(service,oprIDs);
	}

	public T suspend(Class<? extends T> service, String oprID) {
		T it = getItem(service, oprID);
		if(it!=null) {
			// suspend work
			if(it.suspend()) return it;
		}
		return null;
	}

	public boolean isLoopState(Class<? extends T> service, String oprID, LoopState state) {
		T it = getItem(service, oprID);
		if(it!=null) {
			// suspend work
			return it.isLoopState(state);
		}
		return false;
	}

	public void addCatalogListener(ICatalogListener listener) {
		m_listeners.add(ICatalogListener.class,listener);
	}

	public void removeCatalogListener(ICatalogListener listener) {
		m_listeners.remove(ICatalogListener.class,listener);
	}

	/*========================================================
  	 * Protected methods
  	 *======================================================== */

	protected void create(T it, String oprID) {
		Map<Class<?>,T> map = m_instances.get(oprID);
		if(map==null) {
			map = new HashMap<Class<?>,T>(1);
			m_instances.put(oprID,map);
		}
		map.put(it.getClass(),it);
		it.load();
		fireCatalogEvent(it,0);
	}

	/*========================================================
  	 * Helper methods
  	 *======================================================== */

	private void fireCatalogEvent(IService ds, int flags) {
		CatalogEvent.Instance e = new CatalogEvent.Instance(this,ds,flags);
		ICatalogListener[] list = m_listeners.getListeners(ICatalogListener.class);
		for(int i=0; i<list.length;i++) {
			list[i].handleCatalogEvent(e);
		}
	}

}
