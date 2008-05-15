package org.redcross.sar.map;

import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.esri.arcgis.controls.MapControl;

public interface IDiskoMapManager {

	public int installMxdDocs();
	
	public int getInstallMxdDocCount();
	
	public String getMxdDoc();
	public boolean setMxdDoc(String mxdDoc);
	public boolean loadMxdDoc();
	
	public IDiskoMap createMap(EnumSet<IMsoFeatureLayer.LayerCode> myLayers);

	public IDiskoMap getCurrentMap();
	
	public List<IDiskoMap> getMaps();
	public List<MapSourceInfo> getMapsInfo();
	public IDiskoMap getPrintMap();	
	
	public int getMapBaseCount();
	public int toggleMapBase();
	public String getMapBase(int index);
	
	public boolean checkMxdDoc(String mxddoc, boolean keep);
	public MapControl getTmpMap(String mxddoc, boolean keep);
	
	
}