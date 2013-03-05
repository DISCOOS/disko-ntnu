package org.redcross.sar.map.element;

import java.util.List;

import org.redcross.sar.data.IData;

public interface IGeodataCreator<D extends IData, G extends IData> {
	
	public boolean isChanged();

	public List<G> create(D dataObj);
	
	public List<G> getGeodataObjects();
	public int getGeodataObjectsCount();
	
}
