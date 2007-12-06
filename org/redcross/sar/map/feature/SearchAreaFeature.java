package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;

import com.esri.arcgis.interop.AutomationException;

public class SearchAreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private org.redcross.sar.util.mso.Polygon polygon = null;
	
	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		ISearchAreaIf searchArea = (ISearchAreaIf)msoObj;
		return searchArea.getGeodata() != null && !searchArea.getGeodata().equals(getGeodata());
	}

	@Override
	public void msoGeometryChanged() throws IOException, AutomationException {
		if (srs == null) return;
		ISearchAreaIf searchArea = (ISearchAreaIf)msoObject;
		polygon = searchArea.getGeodata();
		if (polygon != null) {
			geometry = MapUtil.getEsriPolygon(polygon, srs);
		}
		else {
			geometry = null;
		}
	}
	
	public int getGeodataCount() {
		return polygon != null ? 1: 0;
	}
	
	public Object getGeodata() {
		return polygon;
	}
}
