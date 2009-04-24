package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.IMapData;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public interface IMsoFeature extends IMapData<IMsoObjectIf>, IFeature {

	public Object getID();

	public IMsoObjectIf getMsoObject();

	public void setMsoObject(IMsoObjectIf msoObject) throws IOException, AutomationException;

	public boolean create() throws IOException, AutomationException;

	public boolean isMsoChanged();

	public Object getGeodata();

	public int getGeodataCount();

	public void setSpatialReference(ISpatialReference srs) throws IOException, AutomationException;

	public ISpatialReference getSpatialReference() throws IOException, AutomationException;

	public boolean isSelected();
	public void setSelected(boolean isSelected);

	public boolean isVisible();

	public void setVisible(boolean isVisible);

	public boolean isDirty();
	public void setDirty(boolean isDirty);

	public String getCaption();
	public void setCaption(String caption);

}
