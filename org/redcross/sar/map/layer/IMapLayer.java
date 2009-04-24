package org.redcross.sar.map.layer;

import java.util.Collection;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.IMapData;

/**
 * 
 * @author Administrator
 *
 * @param <S> - the class or interface containing source data (one per D instance) 
 * @param <D> - the class or interface implementing map data
 */
public interface IMapLayer<S extends IData, D extends IMapData<S>> {
	
	public Enum<?> getClassCode();
	public Enum<?> getLayerCode();
	
	public boolean isVisible();
	public void setVisible(boolean isVisible);
	
	public boolean isDirty();
	
	public Collection<D> load(Collection<S> dataObjs);
	
	public boolean removeAll();
	
}
