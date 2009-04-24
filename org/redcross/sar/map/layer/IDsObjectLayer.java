package org.redcross.sar.map.layer;

import org.redcross.sar.data.IData;
import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;

/**
 * 
 * @author Administrator
 *
 * @param <I> - the class or interface implementing the layer
 * @param <E> - the class or interface that implements the map element
 * @param <S> - the class or interface that implements the data source 
 */
public interface IDsObjectLayer<I,E,D extends IDsObject,G extends IData> extends IElementLayer<I,E,D,G> {

	public enum LayerCode {
		ESTIMATED_POSITION_LAYER
    }
	
	public LayerCode getLayerCode();
	
	public IDs<D> getSource();
	
}
