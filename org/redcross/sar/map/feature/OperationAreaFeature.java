package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;

import com.esri.arcgis.interop.AutomationException;

public class OperationAreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private org.redcross.sar.util.mso.Polygon polygon = null;
	
	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IOperationAreaIf opArea = (IOperationAreaIf)msoObj;
		boolean gChanged = opArea.getGeodata() != null && !opArea.getGeodata().equals(getGeodata());
		isDirty = gChanged;
		return gChanged;
	}

	public void msoGeometryChanged() throws IOException, AutomationException {
		if (srs == null) return;
		IOperationAreaIf opArea = (IOperationAreaIf)msoObject;
		polygon = opArea.getGeodata();
		if (polygon != null)
			geometry = MapUtil.getEsriPolygon(polygon, srs);
		else
			geometry = null;
		super.msoGeometryChanged();
	}
	
	public int getGeodataCount() {
		return polygon != null ? 1: 0;
	}
	
	public Object getGeodata() {
		return polygon;
	}
}
