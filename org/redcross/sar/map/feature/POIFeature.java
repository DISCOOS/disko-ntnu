package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.interop.AutomationException;

public class POIFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private Position pos = null;
	
	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IPOIIf poi = (IPOIIf)msoObj;
		return poi != null && msoObject!=null && poi.equals(msoObject);
	}

	@Override
	public void msoGeometryChanged() throws IOException, AutomationException {
		if (srs == null) return;
		IPOIIf poi = (IPOIIf)msoObject;
		pos = poi.getPosition();
		if (pos != null) {
			geometry = MapUtil.getEsriPoint(pos, srs);
		}
		else {
			geometry = null;
		}
	}
	
	public int getGeodataCount() {
		return pos != null ? 1: 0;
	}
	
	public Object getGeodata() {
		return pos;
	}
}
