package org.redcross.sar;

import java.util.List;

import org.redcross.sar.event.ICatalogListener;
import org.redcross.sar.work.IWorkLoop.LoopState;

public interface IServiceCatalog<T extends IService> {

	public boolean isSingleton();

	public boolean isCreatorOf(Class<?> service);

	public List<T> getItems();
	public List<T> getItems(Object id);
	public List<Object> getIDs();

	public boolean contains(Class<? extends T> service);
	public List<T> getItems(Class<? extends T> service);

	public boolean contains(Class<? extends T> service, Object id);
	public T getItem(Class<? extends T> service, Object id);

	public List<T> createAll(Class<? extends T> service, List<Object> ids);

	public T create(Class<? extends T> service, Object id);

	public List<T> destroyAll();
	public List<T> destroyAll(Object id);
	public List<T> destroyAll(List<Object> id);
	public List<T> destroyAll(Class<? extends T> service);
	public List<T> destroyAll(Class<? extends T> service, List<Object> ids);

	public T destroy(Class<? extends T> service, Object id);

	public List<T> startAll();
	public List<T> startAll(Object id);
	public List<T> startAll(List<Object> ids);
	public List<T> startAll(Class<? extends T> service);
	public List<T> startAll(Class<? extends T> service, List<Object> ids);

	public T start(Class<? extends T> service, Object id);

	public List<T> stopAll();
	public List<T> stopAll(Object id);
	public List<T> stopAll(List<Object> ids);
	public List<T> stopAll(Class<? extends T> service);
	public List<T> stopAll(Class<? extends T> service, List<Object> ids);

	public T stop(Class<? extends T> service, Object id);

	public List<T> resumeAll();
	public List<T> resumeAll(Object id);
	public List<T> resumeAll(List<Object> ids);
	public List<T> resumeAll(Class<? extends T> service);
	public List<T> resumeAll(Class<? extends T> service, List<Object> ids);

	public T resume(Class<? extends T> service, Object id);

	public List<T> suspendAll();
	public List<T> suspendAll(Object id);
	public List<T> suspendAll(List<Object> ids);
	public List<T> suspendAll(Class<? extends T> service);
	public List<T> suspendAll(Class<? extends T> service, List<Object> ids);

	public T suspend(Class<? extends T> service, Object id);

	public boolean isLoopState(Class<? extends T> service, Object id, LoopState state);

	public void addCatalogListener(ICatalogListener listener);
	public void removeCatalogListener(ICatalogListener listener);

}
