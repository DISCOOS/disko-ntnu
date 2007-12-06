package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;

import com.esri.arcgis.interop.AutomationException;

public class OperationAreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private org.redcross.sar.util.mso.Polygon polygon = null;
	
	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObj;
		return opArea.getGeodata() != null && !opArea.getGeodata().equals(getGeodata());
	}

	public void msoGeometryChanged() throws IOException, AutomationException {
		if (srs == null) return;
		IOperationAreaIf opArea = (IOperationAreaIf)msoObject;
		polygon = opArea.getGeodata();
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
