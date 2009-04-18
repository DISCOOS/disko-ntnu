package org.redcross.sar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.redcross.sar.IService;
import org.redcross.sar.IServiceCatalog;
import org.redcross.sar.event.CatalogEvent;
import org.redcross.sar.event.ICatalogListener;
import org.redcross.sar.work.IWorkLoop.LoopState;

public abstract class AbstractCatalog<T extends IService> implements IServiceCatalog<T>{

	private final boolean m_isSingleton;

	private final Map<Object,Map<Class<?>,T>> m_instances =
		new HashMap<Object, Map<Class<?>,T>>();

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

	public List<T> getItems() {
		List<T> list = new ArrayList<T>();
		for(Map<Class<?>,T> it : m_instances.values()) {
			list.addAll(it.values());
		}
		return list;
	}
	
	public List<T> getItems(Object id) {
		List<T> list = new ArrayList<T>();
		Map<Class<?>,T> map = m_instances.get(id);
		if(map!=null) {
			list.addAll(map.values());
		}
		return list;
	}

	public List<Object> getIDs() {
		return new ArrayList<Object>(m_instances.keySet());
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

	public boolean contains(Class<? extends T> service, Object id) {
		for(T e : getItems(id)) {
			if(e.getClass().equals(service)) return true;
		}
		return false;
	}

	public T getItem(Class<? extends T> service, Object id) {
		Map<Class<?>,T> map = m_instances.get(id);
		if(map!=null) {
			return map.get(service);
		}
		return null;
	}

	public List<T> createAll(Class<? extends T> service, List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object id : ids) {
			T item = create(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public abstract T create(Class<? extends T> service, Object id);

	public List<T> destroyAll() {
		return destroyAll(getIDs());
	}

	@SuppressWarnings("unchecked")
	public List<T> destroyAll(Object id) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(id)) {
			T item = destroy((Class<? extends T>)it.getClass(),id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<T> destroyAll(List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object id : ids) {
			list.addAll(destroyAll(id));
		}
		return list;
	}

	public List<T> destroyAll(Class<? extends T> service) {
		return destroyAll(service,getIDs());
	}

	public List<T> destroyAll(Class<? extends T> service, List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object id : ids) {
			T item = destroy(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public T destroy(Class<? extends T> service, Object id) {
		Map<Class<?>,T> map = m_instances.get(id);
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
		return startAll(getIDs());
	}

	public List<T> startAll(Object id) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(id)) {
			if(it.start()) list.add(it);
		}
		return list;
	}

	public List<T> startAll(List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object it : ids) {
			list.addAll(startAll(it));
		}
		return list;
	}

	public List<T> startAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(Object id : getIDs()) {
			T item = start(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> startAll(Class<? extends T> service, List<Object> ids) {
		return startAll(service,ids);
	}

	public T start(Class<? extends T> service, Object id) {
		T it = getItem(service, id);
		if(it!=null) {
			if(it.start()) return it;
		}
		return null;
	}

	public List<T> stopAll() {
		return stopAll(getIDs());
	}

	public List<T> stopAll(Object id) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(id)) {
			if(it.stop()) list.add(it);
		}
		return list;
	}

	public List<T> stopAll(List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object it : ids) {
			list.addAll(stopAll(it));
		}
		return list;
	}

	public List<T> stopAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(Object id : getIDs()) {
			T item = stop(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> stopAll(Class<? extends T> service, List<Object> ids) {
		return stopAll(service,ids);
	}

	public T stop(Class<? extends T> service, Object id) {
		T it = getItem(service, id);
		if(it!=null) {
			// cancel work
			if(it.stop()) return it;
		}
		return null;
	}

	public List<T> resumeAll() {
		return resumeAll(getIDs());
	}

	public List<T> resumeAll(Object id) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(id)) {
			if(it.resume()) list.add(it);
		}
		return list;
	}

	public List<T> resumeAll(List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object it : ids) {
			list.addAll(resumeAll(it));
		}
		return list;
	}

	public List<T> resumeAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(Object id : getIDs()) {
			T item = resume(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> resumeAll(Class<? extends T> service, List<Object> ids) {
		return resumeAll(service,ids);
	}

	public T resume(Class<? extends T> service, Object id) {
		T it = getItem(service, id);
		if(it!=null) {
			// resume work
			if(it.resume()) return it;
		}
		return null;
	}

	public List<T> suspendAll() {
		return suspendAll(getIDs());
	}

	public List<T> suspendAll(Object id) {
		List<T> list = new ArrayList<T>();
		for(T it : getItems(id)) {
			if(it.suspend()) list.add(it);
		}
		return list;
	}

	public List<T> suspendAll(List<Object> ids) {
		List<T> list = new ArrayList<T>();
		for(Object it : ids) {
			list.addAll(suspendAll(it));
		}
		return list;
	}

	public List<T> suspendAll(Class<? extends T> service) {
		List<T> list = new ArrayList<T>();
		for(Object id : getIDs()) {
			T item = suspend(service,id);
			if(item!=null) list.add(item);
		}
		return list;
	}

	public List<T> suspendAll(Class<? extends T> service, List<Object> ids) {
		return suspendAll(service,ids);
	}

	public T suspend(Class<? extends T> service, Object id) {
		T it = getItem(service, id);
		if(it!=null) {
			// suspend work
			if(it.suspend()) return it;
		}
		return null;
	}

	public boolean isLoopState(Class<? extends T> service, Object id, LoopState state) {
		T it = getItem(service, id);
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

	protected void create(T it, Object id) {
		Map<Class<?>,T> map = m_instances.get(id);
		if(map==null) {
			map = new HashMap<Class<?>,T>(1);
			m_instances.put(id,map);
		}
		map.put(it.getClass(),it);
		it.init();
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
