package org.redcross.sar.map.work;

import java.util.Collection;
import java.util.Map;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.IMapData;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.work.IWork;

public interface IMapWork<D extends IMapData<IData>, L extends IMapLayer<IData, D>> extends IWork {

	public int size();
	public void merge(Map<L, Collection<D>> work);	
	
}
