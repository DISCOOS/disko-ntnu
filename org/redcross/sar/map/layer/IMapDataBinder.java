package org.redcross.sar.map.layer;

import java.util.Collection;
import java.util.EnumSet;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.map.event.IMapDataListener;

/**
 * 
 * @author Administrator
 *
 * @param <S> - the class or interface that implements the data id object
 * @param <T> - the class or interface that implements the data object
 * @param <I> - the class or interface that implements the source event information
 * @param <L> - the class or interface that implements the IMapLayer interface
 */
@SuppressWarnings("unchecked")
public interface IMapDataBinder<S,T extends IData,I,L extends IMapLayer> extends IDataBinder<S,T,I> {

	public boolean isActive();
	public int getBufferCount();
	public boolean activate(boolean wait);
	public boolean activate(boolean showProgress, boolean wait);
	public boolean deactivate();

	public boolean execute(boolean showProgress, boolean wait);

	public void setShowProgress(boolean showProgress);

	public boolean connect(IDataSource<I> model, boolean load);
	public boolean connect(IDataSource<I> model, Collection<L> layers, boolean load);

	public void addLayer(L layer);
	public boolean setLayers(Collection<L> layers);
	public Collection<L> getLayers();
	
	public EnumSet<?> getInterests();

	public void removeLayer(L layer);

	public void addMapDataListener(IMapDataListener listener);
	public void removeMapDataListener(IMapDataListener listener);
	
	public boolean disconnect();
	public boolean clear();
	public boolean load();
	
}
