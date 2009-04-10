package org.redcross.sar.app;

import java.util.List;

import org.redcross.sar.app.event.ICatalogListener;
import org.redcross.sar.work.IWorkLoop.LoopState;

public interface IServiceCatalog<T extends IService> {

	public boolean isSingleton();

	public boolean isCreatorOf(Class<?> service);

	public List<T> getItems(String oprID);
	public List<String> getOperations();

	public boolean contains(Class<? extends T> service);
	public List<T> getItems(Class<? extends T> service);

	public boolean contains(Class<? extends T> service, String oprID);
	public T getItem(Class<? extends T> service, String oprID);

	public List<T> createAll(Class<? extends T> service, List<String> oprIDs);

	public T create(Class<? extends T> service, String oprID);

	public List<T> destroyAll();
	public List<T> destroyAll(String oprID);
	public List<T> destroyAll(List<String> oprIDs);
	public List<T> destroyAll(Class<? extends T> service);
	public List<T> destroyAll(Class<? extends T> service, List<String> oprIDs);

	public T destroy(Class<? extends T> service, String oprID);

	public List<T> startAll();
	public List<T> startAll(String oprID);
	public List<T> startAll(List<String> oprIDs);
	public List<T> startAll(Class<? extends T> service);
	public List<T> startAll(Class<? extends T> service, List<String> oprIDs);

	public T start(Class<? extends T> service, String oprID);

	public List<T> stopAll();
	public List<T> stopAll(String oprID);
	public List<T> stopAll(List<String> oprIDs);
	public List<T> stopAll(Class<? extends T> service);
	public List<T> stopAll(Class<? extends T> service, List<String> oprIDs);

	public T stop(Class<? extends T> service, String oprID);

	public List<T> resumeAll();
	public List<T> resumeAll(String oprID);
	public List<T> resumeAll(List<String> oprIDs);
	public List<T> resumeAll(Class<? extends T> service);
	public List<T> resumeAll(Class<? extends T> service, List<String> oprIDs);

	public T resume(Class<? extends T> service, String oprID);

	public List<T> suspendAll();
	public List<T> suspendAll(String oprID);
	public List<T> suspendAll(List<String> oprIDs);
	public List<T> suspendAll(Class<? extends T> service);
	public List<T> suspendAll(Class<? extends T> service, List<String> oprIDs);

	public T suspend(Class<? extends T> service, String oprID);

	public boolean isLoopState(Class<? extends T> service, String oprID, LoopState state);

	public void addCatalogListener(ICatalogListener listener);
	public void removeCatalogListener(ICatalogListener listener);

}
