package org.redcross.sar.map.feature;

import java.io.IOException;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Polygon;

import com.esri.arcgis.interop.AutomationException;

public class SearchAreaFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	
	private Polygon polygon;
	private int changeCount;
	
	public boolean isMsoChanged(IMsoObjectIf msoObj) {
		ISearchAreaIf searchArea = (ISearchAreaIf)msoObj;
		boolean gChanged = isGeodataChanged(searchArea.getGeodata());
		boolean cChanged = !caption.equals(MsoUtils.getSearchAreaName(searchArea));
		isDirty = gChanged || cChanged;
		return gChanged || cChanged;
	}

	private boolean isGeodataChanged(Polygon p) {
		return (   polygon==null 
				|| p==null 
				|| !polygon.equals(p)
				|| changeCount!=p.getChangeCount());
	}
	
	@Override
	public void msoChanged() throws IOException, AutomationException {
		if (srs == null) return;
		ISearchAreaIf searchArea = (ISearchAreaIf)msoObject;
		polygon = searchArea.getGeodata();
		if (polygon != null) {
			geometry = MapUtil.getEsriPolygon(polygon, srs);
			changeCount = polygon.getChangeCount();
		}
		else {
			geometry = null;
			changeCount = 0;
		}		
		caption = MsoUtils.getSearchAreaName(searchArea);
		super.msoChanged();
	}
	
	public int getGeodataCount() {
		return polygon != null ? 1: 0;
	}
	
	public Object getGeodata() {
		return polygon;
	}
}
