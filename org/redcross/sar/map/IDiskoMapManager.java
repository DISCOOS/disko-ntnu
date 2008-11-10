package org.redcross.sar.map;

import java.awt.Component;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.work.IMapWork;

import com.esri.arcgis.controls.MapControl;

public interface IDiskoMapManager {

	public boolean loadXmlFile();
	public boolean storeXmlFile();

	public int installMxdDocs();
	public int uninstallMxdDocs();
	public int getMxdDocCount();
	public boolean installMxdDoc(String mxdDoc);
	public boolean uninstallMxdDoc(String mxdDoc);
	public boolean isMxdDocInstalled(String mxddoc);

	public boolean isMxdDocsInSync();
	public boolean synchronizeMxdDocs();

	public String getMxdDoc();
	public boolean setMxdDoc(String mxdDoc);
	public boolean loadMxdDoc();

	public IDiskoMap createMap(EnumSet<IMsoFeatureLayer.LayerCode> myLayers);

	public IDiskoMap getCurrentMap();

	public IDiskoMap getPrintMap();
	public List<IDiskoMap> getMaps();
	public List<MapSourceInfo> getMapInfoList();
	public MapSourceInfo getMapInfo(String mxdDoc);

	public int getMapBaseCount();
	public int toggleMapBase();
	public String getMapBase(int index);

	public boolean checkMxdDoc(String mxddoc, boolean keep);
	public MapControl getTmpMap(String mxddoc, boolean keep);

	public void selectMap(boolean autoselect, boolean useWorkPool);

	public boolean isMap(Component c);

	public void schedule(IMapWork work);

	public void execute(boolean wait);
	public void execute(IDiskoMap exclude, boolean wait);

}