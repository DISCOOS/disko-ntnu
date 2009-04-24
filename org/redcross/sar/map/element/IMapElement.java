package org.redcross.sar.map.element;

import java.util.List;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.IMapData;
import org.redcross.sar.map.event.IMapElementListener;

import com.esri.arcgis.geometry.IEnvelope;

public interface IMapElement<E,D extends IData,G extends IData> extends IMapData<D> {

	/* ================================================
	 * IMapElement interface
	 * ================================================ */

	public Object getID();	
	public D getDataObject();

	public E getElementImpl();
	
	public boolean isVisible();
	public void setVisible(boolean isVisible);

	public boolean isSelected();
	public void setSelected(boolean isSelected);
	
	public boolean isCaptionShown();
	public void setCaptionShown(boolean isVisible);
	
	public String getCaption();
	public void setCaption(String caption);
		
	public void addMapElementListener(IMapElementListener listener);
	public void removeMapElementListener(IMapElementListener listener);
	
	public boolean isDirty();
	public IEnvelope getExtent();
	
	public List<G> create();
	public boolean isChanged();

	public List<G> getGeodataObjects();
	public int getGeodataObjectCount();

}
