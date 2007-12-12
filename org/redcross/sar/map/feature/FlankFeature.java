package org.redcross.sar.map.feature;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryCollection;
import com.esri.arcgis.geometry.Line;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.interop.AutomationException;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.ErrorDialog;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Route;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public class FlankFeature extends AbstractMsoFeature {

	private static final long serialVersionUID = 1L;
	private static final int LEFT_SIDE_FLANK  = 1;
	private static final int RIGHT_SIDE_FLANK = 2;
	private List<Polygon> leftFlanks  = null;
	private List<Polygon> rightFlanks = null;
//CMR	private GeoList geoList = null;
	private IMsoListIf<IMsoObjectIf> geoList = null;

	public FlankFeature() {
		leftFlanks  = new ArrayList<Polygon>();
		rightFlanks = new ArrayList<Polygon>();
	}

	public List getLeftFlanks() {
		return leftFlanks;
	}

	public List getRightFlanks() {
		return rightFlanks;
	}

	public boolean geometryIsChanged(IMsoObjectIf msoObj) {
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		return area.getAreaGeodata() != null && !area.getAreaGeodata().equals(getGeodata());
	}

	@Override
	public void msoGeometryChanged() throws IOException, AutomationException {
		if (srs == null) return;
		IAreaIf area = MsoUtils.getOwningArea(msoObject);
		if(area!=null) {
			geoList = area.getAreaGeodata();
	        if (geoList != null && geoList.size() > 0) {
				leftFlanks.clear();
				rightFlanks.clear();
				GeometryBag geomBag = new GeometryBag();
				Iterator<IGeodataIf> iter = area.getAreaGeodataIterator();       // todo sjekk etter endring av GeoCollection
				while (iter.hasNext()) {
	                IGeodataIf geodata = iter.next();
					if (geodata instanceof Route) {
	                    Polyline polyline = MapUtil.getEsriPolyline((Route)geodata, srs); // todo Denne er feil, MSO-modellen oppdateres ikke korrekt.
						geomBag.addGeometry(polyline, null, null);
						createFlankForRoute((Route)geodata);
					}
				}
				geometry = geomBag;
			}
		}
	}

	public Object getGeodata() {
		return geoList;
	}

	private void createFlankForRoute(Route route) throws IOException, AutomationException {
		String layout = route.getLayout();
		if (layout == null) {
			return;
		}
		Polyline path = MapUtil.getEsriPolyline(route, srs);
		if (!path.isEmpty())  {
			Hashtable params = getParams(layout);
			String leftDistParam     = (String)params.get("LeftDist");
			String rightDistParam    = (String)params.get("RightDist");
			String clipFeaturesParam = (String)params.get("ClipFeatures");

			int leftDist  = leftDistParam  != null ? Integer.parseInt(leftDistParam)  : 0;
			int rightDist = rightDistParam != null ? Integer.parseInt(rightDistParam) : 0;
			List clipFeatures = getClipFeatures(clipFeaturesParam);

			if (leftDist > 0) {
				try {
					createFlank(path, leftDist, clipFeatures, LEFT_SIDE_FLANK);
				} catch (AutomationException e) {
					showError("Kan ikke lage venstre flanke. Ugyldig geometri.", e.getDescription());
				}
			}
			if (rightDist > 0) {
				try {
					createFlank(path, rightDist, clipFeatures, RIGHT_SIDE_FLANK);
				} catch (AutomationException e) {
					showError("Kan ikke lage høyre flanke. Ugyldig geometri.", e.getDescription());
				}
			}
		}
	}

	private void showError(String msg, String description) {
		ErrorDialog dialog = Utils.getErrorDialog(Utils.getApp().getFrame());
		dialog.showError(msg, description);
	}

	private void createFlank(Polyline path, double dist, List clipFeatures, int side)
			throws IOException, AutomationException {
		Line n1 = new Line();
		Line n2 = new Line();
		Polyline pl = new Polyline();
		IGeometryCollection coll = null;
		Polygon buffer = (Polygon) path.buffer(dist);

		path.queryNormal(3, 0, false, dist *  1, n1);
		path.queryNormal(3, 0, false, dist * -1, n2);
		pl.addPoint(n1.getToPoint(), null, null);
		pl.addPoint(n2.getToPoint(), null, null);
		coll = buffer.cut2(pl);

		double d = path.getLength();
		path.queryNormal(12, d, false, dist *  1, n1);
		path.queryNormal(12, d, false, dist * -1, n2);
		pl.setFromPoint(n2.getToPoint());
		pl.setToPoint(n1.getToPoint());
		Polygon rest = (Polygon) coll.getGeometry(1);
		coll = rest.cut2(pl);

		IGeometry[] leftGeom  = new IGeometry[2];
		IGeometry[] rightGeom = new IGeometry[2];

		((Polygon) coll.getGeometry(1)).cut(path,leftGeom,rightGeom);

		if (side == LEFT_SIDE_FLANK) {
			Polygon leftFlank = clip((Polygon) leftGeom[0], clipFeatures);
			leftFlank.setSpatialReferenceByRef(srs);
			leftFlanks.add(leftFlank);
		}
		if (side == RIGHT_SIDE_FLANK) {
			Polygon rightFlank = clip((Polygon) rightGeom[0], clipFeatures);
			rightFlank.setSpatialReferenceByRef(srs);
			rightFlanks.add(rightFlank);
		}
	}

	private Polygon clip(Polygon flank, List clipFeatures) throws IOException, AutomationException {
		if (clipFeatures == null || clipFeatures.size() < 1) {
			return flank;
		}
		Polygon result = flank;
		for (int i = 0; i < clipFeatures.size(); i++) {
			IFeatureClass fclass = (IFeatureClass)clipFeatures.get(i);
			SpatialFilter spatialFilter = new SpatialFilter();
			spatialFilter.setGeometryByRef(flank.getEnvelope());
			spatialFilter.setGeometryField(fclass.getShapeFieldName());
			spatialFilter.setSpatialRel(esriSpatialRelEnum.esriSpatialRelIntersects);
			IFeatureCursor featureCursor = fclass.search(spatialFilter,false);
			IFeature feature = featureCursor.nextFeature();
			while (feature != null) {
				result = (Polygon)result.difference(feature.getShape());
				feature = featureCursor.nextFeature();
			}
		}
		return result;
	}

	private Hashtable getParams(String paramString) {
		Hashtable<String, String> params = new Hashtable<String, String>();
		StringTokenizer st1 = new StringTokenizer(paramString, "&");
		while(st1.hasMoreTokens()) {
			StringTokenizer st2 = new StringTokenizer(st1.nextToken(), "=");
			String name  = st2.nextToken();
			String value =  st2.nextToken();
			params.put(name, value);
		}
		return params;
	}

	private ArrayList getClipFeatures(String param) throws AutomationException, IOException {
		if (param == null) {
			return null;
		}
		ArrayList<IFeatureClass> result = new ArrayList<IFeatureClass>();
		StringTokenizer st = new StringTokenizer(param, ",");
		while(st.hasMoreTokens()) {
			String featureName = st.nextToken();
			IFeatureClass fc = MapUtil.getFeatureClass(featureName);
			if (fc != null) {
				result.add(fc);
			}
		}
		return result;
	}
}
