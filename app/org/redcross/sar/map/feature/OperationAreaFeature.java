package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.util.mso.Polygon;

import com.esri.arcgis.interop.AutomationException;

public class OperationAreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private Polygon polygon;
	private int changeCount;

	public boolean isMsoChanged() {
		IOperationAreaIf area = (IOperationAreaIf)msoObject;
		boolean gChanged = isGeodataChanged(area.getGeodata());
		setDirty(isDirty || gChanged);
		return gChanged;
	}

	private boolean isGeodataChanged(Polygon p) {
		return (   polygon==null
				|| p==null
				|| !polygon.equals(p)
				|| changeCount!=p.getChangeCount());
	}

	public boolean create() throws IOException, AutomationException {
    	if(super.create()) {
			IOperationAreaIf opArea = (IOperationAreaIf)msoObject;
			polygon = opArea.getGeodata();
			if (polygon != null) {
				geometry = MapUtil.getEsriPolygon(polygon, srs);
				changeCount = polygon.getChangeCount();
			}
			else {
				geometry = null;
				changeCount = 0;
			}
			setDirty(isDirty || (getShape()!=null));
			return true;
    	}
    	return false;
	}

	public int getGeodataCount() {
		return polygon != null ? 1: 0;
	}

	public Object getGeodata() {
		return polygon;
	}
}
