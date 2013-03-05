package org.redcross.sar.map;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.redcross.sar.AppProps;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureModel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.tool.IDrawTool.DrawMode;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

import com.esri.arcgis.analyst3d.SimpleMarker3DSymbol;
import com.esri.arcgis.carto.CompositeGraphicsLayer;
import com.esri.arcgis.carto.ICompositeGraphicsLayer;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IIdentify;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.carto.IPictureElement3;
import com.esri.arcgis.carto.IRowIdentifyObjectProxy;
import com.esri.arcgis.carto.SymbolBackground;
import com.esri.arcgis.carto.SymbolBorder;
import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.display.IFillSymbol;
import com.esri.arcgis.display.ILineSymbol;
import com.esri.arcgis.display.IMarkerSymbol;
import com.esri.arcgis.display.ISymbol;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.display.SimpleMarkerSymbol;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleFillStyle;
import com.esri.arcgis.display.esriSimpleLineStyle;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IFeatureProxy;
import com.esri.arcgis.geodatabase.ISpatialFilter;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.CircularArc;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeographicCoordinateSystem;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryCollection;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.ISpatialReferenceFactory2;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.Ring;
import com.esri.arcgis.geometry.SpatialReferenceEnvironment;
import com.esri.arcgis.geometry.esriMGRSModeEnum;
import com.esri.arcgis.geometry.esriSRGeoCSType;
import com.esri.arcgis.geometry.IProximityOperator;
import com.esri.arcgis.geometry.esriSRProjCSType;
import com.esri.arcgis.geometry.esriSegmentExtension;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IArray;

public class MapUtil {

	private static double MEAN_RADIUS = 6367444.6571; // mean polar and equatorial radius (WGS84)

	private static Workspace workspace = null;
	private static IGeographicCoordinateSystem  geographicCS = null;

	public static Workspace getWorkspace() throws AutomationException, IOException {
		if (workspace == null) {
			String dbPath = AppProps.getText("Database.path");
			FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
			workspace = (Workspace) factory.openFromFile(dbPath, 0);
		}
		return workspace;
	}

	public static IFeatureClass getFeatureClass(String name) throws AutomationException,
			IOException {
		return getWorkspace().openFeatureClass(name);
	}

	public static IGeographicCoordinateSystem getGeographicCS()
			throws IOException, AutomationException {
    	if (geographicCS == null) {
    		ISpatialReferenceFactory2 spaRefFact = new SpatialReferenceEnvironment();
    		geographicCS = spaRefFact.createGeographicCoordinateSystem(
    				esriSRGeoCSType.esriSRGeoCS_WGS1984);
    	}
    	return geographicCS;
    }

	public static Point getEsriPoint(Point2D.Double pos, ISpatialReference srs)
		throws IOException, AutomationException {
		Point p = new Point();
		p.setX(pos.getX());
		p.setY(pos.getY());
		p.setSpatialReferenceByRef(getGeographicCS());
		p.project(srs);
		return p;
	}
	public static Point getEsriPoint(GeoPos pos, ISpatialReference srs)
			throws IOException, AutomationException {
		return getEsriPoint(pos.getPosition(),srs);
	}

	public static IEnvelope expand(double ratio, IEnvelope env)
		throws IOException, AutomationException {
		IEnvelope extent = env.getEnvelope();
		extent.expand(ratio, ratio, true);
		return extent;
	}

	public static IEnvelope expand(double x, double y, boolean isRatio, IEnvelope env)
		throws IOException, AutomationException {
		IEnvelope extent = env.getEnvelope();
		extent.expand(x, y, isRatio);
		return extent;
	}

	public static IEnvelope offset(double x, double y, IEnvelope env)
		throws IOException, AutomationException {
		IEnvelope extent = env.getEnvelope();
		extent.offset(x, y);
		return extent;
	}

	public static IPoint getCenter(IEnvelope env)
		throws IOException, AutomationException {
		IPoint p = new Point();
		p.setSpatialReferenceByRef(env.getSpatialReference());
		p.setX(env.getXMin()+env.getWidth()/2);
		p.setY(env.getYMin()+env.getHeight()/2);
		return p;
	}

	public static String pointToText(Point p, int max, int digits){
		return pointToText(p, max, digits, false);
	}

	public static String pointToText(Point p, int max, int digits, boolean isBold){
		return pointToText(p, max, digits, 0, isBold?digits:0);		
	}
	
	public static String pointToText(Point p, int max, int digits, int sIdx, int eIdx) {
		String text = new String();
		try{
			String e = formatCoord(p.getX(),max,digits,sIdx,eIdx);
			String n = formatCoord(p.getY(),max,digits,sIdx,eIdx);
			text = e + "E " + n + "N";

		}catch(Exception e){
			e.printStackTrace();
		}
		return text;
		
	}
	
	
	public static String[] getCoords(String text) {
		// remove any directions
		String coords = text.replace("E", "").replace("N", "");
		// get x and y coordinates
		int length = coords.length();
		int divide = length/2;
		String x = coords.subSequence(0, divide).toString();
		String y = coords.subSequence(divide, length).toString();
		// finished
		return new String[]{x,y};
	}
	
	/*
	public static void main(String[] args)
	{
		System.out.println(formatCoord(151.0,5,3));
	}
	*/
	
	/**
	 * 
	 * @param coord - the coordinate value 
	 * @param max - maximum number of digits in coordinate value
	 * @param digits - the number of digits to format as string
	 * @param isBold - formats coordinate as bold text (html)
	 * @return The coordinate value formatted as string. If number of digits are 
	 * less than the precision, then zeros are padded at the beginning of the string.
	 */
	public static String formatCoord(double coord, int max, int digits, boolean isBold) {
		return formatCoord(coord, max, digits, 0, isBold?digits:0);
	}
	
	/**
	 * 
	 * @param coord - the coordinate value 
	 * @param max - maximum number of digits in coordinate value
	 * @param digits - the number of digits to format as string
	 * @param sIdx - inserts a bold text start tag before digit index (html)
	 * @param eIdx - inserts a bold text end tag after digit index (html). 
	 * @return The coordinate value formatted as string. If number of digits are 
	 * less than the precision, then zeros are padded at the beginning of the string.
	 */
	public static String formatCoord(double coord, int max, int digits, int sIdx, int eIdx) {
		// round up to nearest long value
		long c = Math.round(coord);
		// limit digits
		digits = (digits>max ? max : digits);
		// divide on precision
		String s = Long.toString(Math.round(c/Math.pow(10,max-digits)));
		// cut to precision if coordinate value was to long
		if(s.length() > max){
			s = s.substring(s.length()-max);
		}
		else {
			// get length of padding
			int num = digits-s.length();
			// add padding in front of number
			for(int i=0;i<num;i++)
				s = "0"+ s;
		}
		return Utils.insertHtml(s, sIdx, eIdx, "b");
	}

	/**
	 * <b>Formats MGRS string</b></p>
	 * 
	 * @param mgrs - the MGRS string to format
	 * @param max - the number of digits each coordinate in the mgrs string is codified with
	 * @param digits - the number of digits to print
	 * @return MGRS string formated according to the supplied arguments.
	 */
	public static String formatMGRS(String mgrs, int max, int digits){
		return formatMGRS(mgrs,max,digits,false);
	}

	/**
	 * <b>Formats MGRS string (as html)</b></p>
	 * 
	 * @param mgrs - the MGRS string to format
	 * @param max - the number of digits each coordinate in the mgrs string is codified with
	 * @param digits - the number of digits to print
	 * @param isBold - coordinates are formatted bold with html tags if <code>true</code>
	 * @return MGRS string formated according to the supplied arguments.
	 */	
	public static String formatMGRS(String mgrs, int max, int digits, boolean isBold){
		return formatMGRS(mgrs, max, digits, 0, isBold?digits:0);	
	}

	/**
	 * <b>Formats MGRS string (as html)</b></p>
	 * 
	 * @param mgrs - the MGRS string to format
	 * @param max - the number of digits each coordinate in the mgrs string is codified with
	 * @param digits - the number of digits to print
	 * @param sIdx - inserts a bold text start tag before digit index (html)
	 * @param eIdx - inserts a bold text end tag after digit index (html). 
	 * @return UTM string formated according to the supplied arguments.
	 */	
	public static String formatMGRS(String mgrs, int max, int digits, int sIdx, int eIdx){	
	
		String s = null;
		
		// is not null?
		if (mgrs != null) {
			// is empty?
			if (mgrs.length() > 0) {
				// get elements
				String zone = mgrs.subSequence(0, 3).toString();
				String square = mgrs.subSequence(3, 5).toString();
				// remove any directions and get coordinates
				String coords[] = MapUtil.getCoords(mgrs.subSequence(5, mgrs.length()).toString().replace("E", "").replace("N", ""));
				// get x and y coordinates
				double x = Double.valueOf(coords[0]);
				double y = Double.valueOf(coords[1]);
				// build formated MGRS
				s = zone + " " + square + " " + 
					formatCoord(x,max,digits,sIdx,eIdx) + " " + 
					formatCoord(y,max,digits,sIdx,eIdx);
			}
		}
		return s;
	}

	/**
	 * <b>Formats UTM string</b></p>
	 *  
	 * @param utm - the UTM string to format
	 * @param max - the number of digits each coordinate in the mgrs string is codified with
	 * @param digits - the number of digits to print
	 * @return UTM string formated according to the supplied arguments.
	 */
	public static String formatUTM(String utm, int max, int digits){
		return formatUTM(utm,max,digits,false);
	}
	
	/**
	 * <b>Formats UTM string (as html)</b></p>
	 * 
	 * @param utm - the UTM string to format
	 * @param max - the number of digits each coordinate in the mgrs string is codified with
	 * @param digits - the number of digits to print
	 * @param isBold - coordinates are formatted bold with html tags if <code>true</code>
	 * @return UTM string formated according to the supplied arguments.
	 */	
	public static String formatUTM(String utm, int max, int digits, boolean isBold){
		return formatUTM(utm, max, digits, 0, isBold?digits:0);
	}
	
	/**
	 * <b>Formats UTM string (as html)</b></p>
	 * 
	 * @param utm - the UTM string to format
	 * @param max - the number of digits each coordinate in the UTM string is codified with
	 * @param digits - the number of digits to print
	 * @param sIdx - inserts a bold text start tag before digit index (html)
	 * @param eIdx - inserts a bold text end tag after digit index (html). 
	 * @return UTM string formated according to the supplied arguments.
	 */	
	public static String formatUTM(String utm, int max, int digits, int sIdx, int eIdx){
	
		String s = null;
		
		// is not null?
		if (utm != null) {
			// is empty?
			if (utm.length() > 0) {
				// get elements
				String zone = utm.subSequence(0, 3).toString();
				double x = Double.valueOf(utm.subSequence(3, 9).toString());
				double y = Double.valueOf(utm.subSequence(10, 16).toString());
				// build formated UTM
				s = zone + " " + 
					formatCoord(x,max,digits,sIdx,eIdx) + "E " + 
					formatCoord(y,max,digits,sIdx,eIdx) + "N";
			}
		}
		return s;
	}

	public static Point getEsriPoint(double x, double y, ISpatialReference srs)
	throws IOException, AutomationException {
		Point p = new Point();
		p.setX(x);
		p.setY(y);
		p.setSpatialReferenceByRef(getGeographicCS());
		p.project(srs);
		return p;
	}

	public static Position getMsoPosistion(Point p)
			throws IOException, AutomationException {
		Point prjPoint = (Point)p.esri_clone();
		prjPoint.project(getGeographicCS());
		return new Position(null, prjPoint.getX(), prjPoint.getY());
	}

	public static Polygon getEsriPolygon(org.redcross.sar.util.mso.Polygon msoPolygon,
			ISpatialReference srs) throws IOException, AutomationException {
		Polygon esriPolygon = new Polygon();
		Collection<GeoPos> vertices = msoPolygon.getVertices();
		Iterator<GeoPos> iter = vertices.iterator();
		while(iter.hasNext()) {
			GeoPos pos = iter.next();
			Point2D.Double pnt2D = pos.getPosition();
			Point p = new Point();
			p.setX(pnt2D.getX());
			p.setY(pnt2D.getY());
			esriPolygon.addPoint(p, null, null);
		}
		esriPolygon.setSpatialReferenceByRef(getGeographicCS());
		esriPolygon.project(srs);
		return esriPolygon;
	}

	public static org.redcross.sar.util.mso.Polygon getMsoPolygon(Polygon p)
			throws IOException, AutomationException {
		Polygon prjPolygon = (Polygon)p.esri_clone();
		prjPolygon.project(getGeographicCS());
		int numPoints = prjPolygon.getPointCount();
		org.redcross.sar.util.mso.Polygon msoPolygon =
			new org.redcross.sar.util.mso.Polygon(null, "", numPoints);

		for (int i = 0; i < numPoints; i++) {
			IPoint pnt = prjPolygon.getPoint(i);
			msoPolygon.add(pnt.getX(), pnt.getY());
		}
		return msoPolygon;
	}

	public static GeometryBag getEsriGeometryBag(IPOIListIf msoList, ISpatialReference srs)
		throws IOException, AutomationException {
        if (msoList != null && msoList.size() > 0) {
			GeometryBag geomBag = new GeometryBag();
			geomBag.setSpatialReferenceByRef(srs);
			for(IPOIIf poi : msoList.getObjects()) {
				// get object
				IGeometry geo = getEsriPoint(poi.getPosition().getGeoPos(), srs);
				// add?
				if(geo!=null)
					geomBag.addGeometry(geo, null, null);
			}
			return geomBag;
		}
        return null;
	}

	public static GeometryBag getEsriGeometryBag(IMsoListIf<IMsoObjectIf> msoList,
			MsoClassCode code, ISpatialReference srs)
		throws IOException, AutomationException {
        if (msoList != null && msoList.size() > 0) {
			GeometryBag geomBag = new GeometryBag();
			geomBag.setSpatialReferenceByRef(srs);
			for(IMsoObjectIf mso : msoList.getObjects()) {
				// valid class type?
				if (code == null || mso.getClassCode().equals(code)) {
					// initialize
					IGeometry geo = null;
					// parse
					if(mso instanceof IRouteIf) {
						// get geodata
						IGeodataIf geodata = ((IRouteIf)mso).getGeodata();
						// convert to a polyline
						geo = getEsriPolyline((Route)geodata, srs);
					}
					else if(mso instanceof ITrackIf) {
						// get geodata
						IGeodataIf geodata = ((ITrackIf)mso).getGeodata();
						// convert to a polyline
						geo = getEsriPolyline((Track)geodata, srs);
					}
					// add?
					if(geo!=null)
						geomBag.addGeometry(geo, null, null);
				}
			}
			return geomBag;
		}
        return null;
	}

	public static Polygon getEsriPolygon(Route route, ISpatialReference srs)
			throws IOException, AutomationException {
		Polyline esriPolyline = getEsriPolyline(route, srs);
		return getPolygon(esriPolyline);
	}

	public static Polyline getEsriPolyline(Route route, ISpatialReference srs)
			throws IOException, AutomationException {
		Polyline esriPolyline = new Polyline();
		Collection<GeoPos> vertices = route.getItems();
		Iterator<GeoPos> iter = vertices.iterator();
		while(iter.hasNext()) {
			GeoPos pos = iter.next();
			Point2D.Double pnt2D = pos.getPosition();
			Point p = new Point();
			p.setX(pnt2D.getX());
			p.setY(pnt2D.getY());
			esriPolyline.addPoint(p, null, null);
		}
		esriPolyline.setSpatialReferenceByRef(getGeographicCS());
		esriPolyline.project(srs);
		return esriPolyline;
	}

	public static Polygon getEsriPolygon(Track track, ISpatialReference srs)
		throws IOException, AutomationException {
		Polyline esriPolyline = getEsriPolyline(track, srs);
		return getPolygon(esriPolyline);
	}

	public static Polyline getEsriPolyline(Track track, ISpatialReference srs)
		throws IOException, AutomationException {
		Polyline esriPolyline = new Polyline();
		Collection<TimePos> vertices = track.getItems();
		Iterator<TimePos> iter = vertices.iterator();
		while(iter.hasNext()) {
			TimePos pos = iter.next();
			Point2D.Double pnt2D = pos.getPosition();
			Point p = new Point();
			p.setX(pnt2D.getX());
			p.setY(pnt2D.getY());
			esriPolyline.addPoint(p, null, null);
		}
		esriPolyline.setSpatialReferenceByRef(getGeographicCS());
		esriPolyline.project(srs);
		return esriPolyline;
	}

	public static Polyline getEsriPolyline(GeoPos p1, GeoPos p2, ISpatialReference srs)
		throws IOException, AutomationException {
		Polyline esriPolyline = new Polyline();
		Point p = new Point();
		p.setX(p1.getPosition().x);
		p.setY(p1.getPosition().y);
		esriPolyline.addPoint(p, null, null);
		p = new Point();
		p.setX(p2.getPosition().x);
		p.setY(p2.getPosition().y);
		esriPolyline.addPoint(p, null, null);
		esriPolyline.setSpatialReferenceByRef(getGeographicCS());
		esriPolyline.project(srs);
		return esriPolyline;
	}

	public static Polyline getEsriPolyline(Collection<GeoPos> points, ISpatialReference srs)
		throws IOException, AutomationException {
		Polyline esriPolyline = new Polyline();
		for(GeoPos it : points) {
			Point p = new Point();
			p.setX(it.getPosition().x);
			p.setY(it.getPosition().y);
			esriPolyline.addPoint(p, null, null);
		}
		esriPolyline.setSpatialReferenceByRef(getGeographicCS());
		esriPolyline.project(srs);
		return esriPolyline;
	}

	public static Route getMsoRoute(Polyline pl)
			throws IOException, AutomationException {
		Polyline prjPolyline = (Polyline)pl.esri_clone();
		prjPolyline.project(getGeographicCS());
		int numPoints = prjPolyline.getPointCount();
		Route route = new Route(null, "", numPoints);
		for (int i = 0; i < numPoints; i++) {
			IPoint pnt = prjPolyline.getPoint(i);
			route.add(pnt.getX(), pnt.getY());
		}
		return route;
	}

	public static Track getMsoTrack(Polyline pl, List<Calendar> timesteps)
			throws IOException, AutomationException {
		Polyline prjPolyline = (Polyline)pl.esri_clone();
		prjPolyline.project(getGeographicCS());
		int numPoints = prjPolyline.getPointCount();
		Track track = new Track(null, "", numPoints);
		for (int i = 0; i < numPoints; i++) {
			IPoint pnt = prjPolyline.getPoint(i);
			track.add(pnt.getX(), pnt.getY(),timesteps.get(i));
		}
		return track;
	}

	public static Polyline getPolyline(Polygon polygon)
			throws IOException, AutomationException {
		Polyline polyline = new Polyline();
		polyline.setSpatialReferenceByRef(polygon.getSpatialReference());
		for (int i = 0; i < polygon.getPointCount(); i++) {
			polyline.addPoint(polygon.getPoint(i), null, null);
		}
		//polyline.simplify();
		return polyline;
	}

	public static Polygon getPolygon(IPoint p, double size)
		throws IOException, AutomationException {
		// get extent
		IEnvelope extent = getEnvelope(p,size);
		// create polygon
		Polygon polygon = new Polygon();
		polygon.setSpatialReferenceByRef(p.getSpatialReference());
		polygon.setRectangle(extent);
		polygon.simplify();
		return polygon;
	}
	public static Polygon getPolygon(Polyline polyline)
		throws IOException, AutomationException {
		// create polygon
		Polygon polygon = new Polygon();
		polygon.setSpatialReferenceByRef(polyline.getSpatialReference());
		for (int i = 0; i < polyline.getPointCount(); i++) {
			polygon.addPoint(polyline.getPoint(i), null, null);
		}
		polygon.simplify();
		return polygon;
	}

	public static Polyline getPolyline(IEnvelope e)
		throws IOException, AutomationException {
		// create polygon
		Polyline polyline = new Polyline();
		polyline.setSpatialReferenceByRef(e.getSpatialReference());
		polyline.addPoint(e.getUpperLeft(), null, null);
		polyline.addPoint(e.getUpperRight(), null, null);
		polyline.addPoint(e.getLowerRight(), null, null);
		polyline.addPoint(e.getLowerLeft(), null, null);
		polyline.addPoint(e.getUpperLeft(), null, null);
		return polyline;
	}

	public static Polyline getPolyline(GeometryBag bag)
		throws IOException, AutomationException {
		Polyline polyline = new Polyline();
		// only add valid geometries
		for(int i=0;i<bag.getGeometryCount();i++) {
			IGeometry geo = bag.getGeometry(i);
			if(geo instanceof IGeometryCollection)
				polyline.addGeometryCollection((IGeometryCollection)geo);
			else if(geo instanceof IPoint)
				polyline.addPoint((IPoint)geo, null, null);
		}
		polyline.setSpatialReferenceByRef(bag.getSpatialReference());
		return polyline;
	}

	public static IEnvelope getEnvelope(IPoint p, double size)
			throws IOException, AutomationException {
		IEnvelope env = new Envelope();
		env.setSpatialReferenceByRef(p.getSpatialReference());
		double xmin = p.getX()-size/2;
		double ymin = p.getY()-size/2;
		double xmax = p.getX()+size/2;
		double ymax = p.getY()+size/2;
		env.putCoords(xmin, ymin, xmax, ymax);
		return env;
	}

	public static String getMGRSfromPoint(Point p, int digits) throws Exception {
		return getMGRSfromPoint(p,digits,false);
	}
	
	public static String getMGRSfromPoint(Point p, int digits, boolean isBold)
			throws Exception {
		return getMGRSfromPoint(p,digits,0,isBold?5:0);
	}
	
	public static String getMGRSfromPoint(Point p, int digits, int sIdx, int eIdx)
		throws Exception {
		if(p!=null) {
			// get mgrs format
			String mgrs = p.createMGRS(digits, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
			// insert html?
			if(sIdx>=0&&sIdx<eIdx) {
				mgrs = mgrs.substring(0, 4) + 
					Utils.insertHtml(mgrs.substring(5, 9), sIdx, eIdx, "b") + 
					Utils.insertHtml(mgrs.substring(10, 4), sIdx, eIdx, "b");
			}
			return mgrs;
		}
		return null;
	}

	public static String getUTMfromPoint(Point p) throws Exception {
		return getUTMfromPoint(p,false);
	}
	
	public static String getUTMfromPoint(Point p, boolean isBold) throws Exception {
		return getUTMfromPoint(p,0,isBold?7:0);
	}
	
	public static String getUTMfromPoint(Point p, int sIdx, int eIdx) throws Exception {	
		String mgrs = p.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		String zone = mgrs.substring(0,3);
		ISpatialReference outgoingCoordSystem = getProjectedSpatialReference(getSRProjCS_WGS1984UTM_Zone(zone));
		p.project(outgoingCoordSystem);
		return zone + formatCoord(p.getX(),7,7,sIdx,eIdx)+ "E" + formatCoord(p.getY(),7,7,sIdx,eIdx) + "N";

	}

	public static String getDESfromPoint(Point p)
		throws Exception {
		Point point = (Point)p.esri_clone();
		point.project(getGeographicCS());
		DecimalFormat format = new DecimalFormat("00.0000");
		return format.format(point.getX()) + "E" + format.format(point.getY()) + "N";
	}

	public static String getDEGfromPoint(Point p)
		throws Exception {
		Point point = (Point)p.esri_clone();
		point.project(getGeographicCS());
		return fromDEStoDEG(point.getX())+ "E" + fromDEStoDEG(point.getY()) + "N";
	}

	private static String fromDEStoDEG(double des) {

		// Get degrees by chopping off at the decimal
		double d = Math.floor( des );

		// correction required since floor() is not the same as int()
		if ( d < 0 )
			d = d + 1;

		// Get fraction after the decimal
		double fd = Math.abs( des - d );

		// Convert this fraction to seconds (without minutes)
		double fs = fd * 3600;

		// Determine number of whole minutes in the fraction
		double m = Math.floor( fs / 60 );

		// Put the remainder in seconds
		double s = fs - m * 60;

		// Fix rounoff errors
		if ( Math.rint( s ) == 60 ) {
			m = m + 1;
			s = 0;
		}

		if ( Math.rint( m ) == 60 ) {
			if ( d < 0 )
				d = d - 1;
			else // ( dfDegree => 0 )
				d = d + 1;

			m = 0;
		}

		String dt = String.valueOf(Math.round(d));
		if(dt.length()==1) dt = "0" + dt;
		String mt = String.valueOf(Math.round(m));
		if(mt.length()==1) mt = "0" + mt;
		String st = String.valueOf(Math.round(s));
		if(st.length()==1) st = "0" + st;

		// return string
		return dt + "d" + mt + "m" + st + "s";

	}

	public static String getDEMfromPoint(Point p)
		throws Exception {
		Point point = (Point)p.esri_clone();
		point.project(getGeographicCS());
		return fromDEStoDEM(point.getX())+ "E" + fromDEStoDEM(point.getY()) + "N";
	}

	private static String fromDEStoDEM(double des) {

		// Get degrees by chopping off at the decimal
		double d = Math.floor( des );

		// correction required since floor() is not the same as int()
		if ( d < 0 )
			d = d + 1;

		// Get fraction after the decimal
		double fd = Math.abs( des - d );

		// Convert this fraction to decimal minutes
		double m = fd * 60;

		// create numeric format
		DecimalFormat format = new DecimalFormat("00.000000");

		// create strings
		String dt = String.valueOf(Math.round(d));
		if(dt.length()==1) dt = "0" + dt;
		String mt = format.format(m);

		// return string
		return dt + "d" + mt + "m";

	}

	public static String getMGRSfromPosition(Position pos, int digits)
		throws Exception {
		if(pos!=null)
			return getMGRSfromPosition(pos.getPosition(),digits);
		else
			return null;
	}

	public static String getUTMfromPosition(Position pos)
		throws Exception {
		return getUTMfromPosition(pos.getPosition());
	}

	public static String getDEGfromPosition(Position pos)
		throws Exception {
		return getDEGfromPosition(pos.getPosition());
	}

	public static String getDESfromPosition(Position pos)
		throws Exception {
		return getDESfromPosition(pos.getPosition());
	}

	public static String getDEMfromPosition(Position pos)
		throws Exception {
		return getDEMfromPosition(pos.getPosition());
	}

	public static String getMGRSfromPosition(Point2D p,int digits)
		throws Exception {
		Point point = new Point();
		point.setSpatialReferenceByRef(getGeographicCS());
		point.setX(p.getX());
		point.setY(p.getY());
		return point.createMGRS(digits, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
	}

	public static String getUTMfromPosition(Point2D p) throws Exception {
		return getUTMfromPosition(p,false);
	}
	
	public static String getUTMfromPosition(Point2D p, boolean isBold) throws Exception {
		return getUTMfromPosition(p,0,isBold?7:0);
	}
	
	public static String getUTMfromPosition(Point2D p, int sIdx, int eIdx)
		throws Exception {
		Point point = new Point();
		point.setSpatialReferenceByRef(getGeographicCS());
		point.setX(p.getX());
		point.setY(p.getY());
		String mgrs = point.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		String zone = mgrs.substring(0,3);
		ISpatialReference outgoingCoordSystem = getProjectedSpatialReference(getSRProjCS_WGS1984UTM_Zone(zone));
		point.project(outgoingCoordSystem);
		return zone + formatCoord(point.getX(),7,7,sIdx,eIdx) + "E" 
		            + formatCoord(point.getY(),7,7,sIdx,eIdx) + "N";
	}

	public static String getDEGfromPosition(Point2D p)
		throws Exception {
		return fromDEStoDEG(p.getX())+ "E" + fromDEStoDEG(p.getY()) + "N";
	}

	public static String getDESfromPosition(Point2D p)
		throws Exception {
		DecimalFormat format = new DecimalFormat("00.0000");
		return format.format(p.getX()) + "E" + format.format(p.getY()) + "N";
	}

	public static String getDEMfromPosition(Point2D p)
		throws Exception {
		return fromDEStoDEM(p.getX())+ "E" + fromDEStoDEM(p.getY()) + "N";
	}

	public static Position getPositionFromMGRS(String mgrs)
			throws Exception {

		// trim
		mgrs = mgrs.trim();
		String prefix = mgrs.substring(0,5);
		String suffix = mgrs.substring(5,mgrs.length());
		suffix = suffix.toUpperCase().replace("E", "").replace("N", "");
		Point p = new Point();
		p.setSpatialReferenceByRef(getGeographicCS());
		p.putCoordsFromMGRS(prefix.concat(suffix), esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		p.project(getGeographicCS());
		return new Position(null, p.getX(), p.getY());
	}

	public static Position getPositionFromUTM(String utm)
		throws Exception {

		utm = utm.trim();
		String zone = utm.subSequence(0, 3).toString();
		zone = getSRProjCS_WGS1984UTM_Zone(zone);
		String suffix = utm.substring(3,utm.length()).toUpperCase().replace("E", "").replace("N", "");
		String x = suffix.subSequence(0, 7).toString();
		String y = suffix.subSequence(7, 14).toString();
		Point p = new Point();
		ISpatialReference incommingCoordSystem = getProjectedSpatialReference(zone);
		p.setSpatialReferenceByRef(incommingCoordSystem);
		p.setX(Double.valueOf(x));
		p.setY(Double.valueOf(y));
		p.project(getGeographicCS());

		return new Position(null,p.getX(),p.getY());
	}

	public static Position getPositionFromDEG(String deg)
		throws Exception {

		// remove any spaces
		deg = deg.trim().toUpperCase();

		// split into longitude and latitude
		String[] split = deg.split("E");

		// get sub strings
		String lat = split[0];
		String lon = split[1].replace("N", "0");

		// account for sign
		int offset = lat.startsWith("-") ? 1 : 0;
		
		// remove invalid characters
		lat = lat.replaceAll(",","\\.").replaceAll("-", "");

		// parse longitude (E)
		double d1 = Double.valueOf(lat.substring(0, 2));
		double m1 = Double.valueOf(lat.substring(3, 5));
		double s1 = Double.valueOf(lat.substring(6, 8));

		// negate?
		d1 = (offset>0? -d1 : d1);
		
		// account for sign
		offset = lon.startsWith("-") ? 1 : 0;

		// remove invalid characters
		lon = lon.replaceAll(",","\\.").replaceAll("-", "");
		
		// parse latitude (N)
		double d2 = Double.valueOf(lon.substring(0, 2));
		double m2 = Double.valueOf(lon.substring(3, 5));
		double s2 = Double.valueOf(lon.substring(6, 8));

		// negate?
		d2 = (offset>0? -d2 : d2);
		
		// Determine longitude fraction from minutes and seconds
		double f1 = Double.valueOf(m1) / 60 + Double.valueOf(s1) / 3600;

		// Be careful to get the sign right.
		double dec1 = ( d1 < 0 ) ? d1 - f1 : d1 + f1;

		// Determine latitude fraction from minutes and seconds
		double f2 = Double.valueOf(m2) / 60 + Double.valueOf(s2) / 3600;

		// Be careful to get the sign right.
		double dec2 = ( d2 < 0 ) ? d2 - f2 : d2 + f2;

		// create point
		Point p = new Point();
		p.setSpatialReferenceByRef(getGeographicCS());
		p.setX(dec1);
		p.setY(dec2);
		p.project(getGeographicCS());

		// finished
		return new Position(null,p.getX(),p.getY());
	}

	public static Position getPositionFromDES(String des)
		throws Exception {

		// remove any spaces
		des = des.trim().toUpperCase();

		// split into longitude and latitude
		String[] split = des.split("E");
		String lon = split[0];
		split = split[1].split("N");
		String lat = split[0];

		// parse longitude (E)
		double x = Double.valueOf(lon.replace(",", "."));

		// parse latitude (N)
		double y = Double.valueOf(lat.replace(",", "."));

		// create point
		Point p = new Point();
		p.setSpatialReferenceByRef(getGeographicCS());
		p.setX(x);
		p.setY(y);
		p.project(getGeographicCS());

		//Point point = getEsriPointFromDES(des,getGeographicCS());

		return new Position(null,p.getX(),p.getY());
	}

	public static Position getPositionFromDEM(String dem)
		throws Exception {

		// remove any spaces
		dem = dem.trim().toUpperCase();

		// split into longitude and latitude
		String[] split = dem.split("E");

		// get sub strings
		String lat = split[0];
		String lon = split[1].replace("N", "0");

		// account for sign
		int offset = lat.startsWith("-") ? 1 : 0;

		// remove invalid characters
		lat = lat.replaceAll(",","\\.").replaceAll("-", "");
		
		// parse longitude (E)
		double d1 = Double.valueOf(lat.substring(0, 2));
		double m1 = Double.valueOf(lat.substring(3));

		// negate?
		d1 = (offset>0? -d1 : d1);
		
		// account for sign
		offset = lon.startsWith("-") ? 1 : 0;

		// remove invalid characters
		lon = lon.replaceAll(",","\\.").replaceAll("-", "");
		
		// parse latitude (N)
		double d2 = Double.valueOf(lon.substring(0, 2));
		double m2 = Double.valueOf(lon.substring(3));
		
		// negate?
		d2 = (offset>0? -d2 : d2);
		
		// Determine longitude fraction from minutes and seconds
		double f1 = Double.valueOf(m1) / 60;

		// Be careful to get the sign right.
		double dec1 = ( d1 < 0 ) ? d1 - f1 : d1 + f1;

		// Determine latitude fraction from minutes and seconds
		double f2 = Double.valueOf(m2) / 60;

		// Be careful to get the sign right.
		double dec2 = ( d2 < 0 ) ? d2 - f2 : d2 + f2;

		// create point
		Point p = new Point();
		p.setSpatialReferenceByRef(getGeographicCS());
		p.setX(dec1);
		p.setY(dec2);
		p.project(getGeographicCS());

		//Point point = getEsriPointFromDEM(des,getGeographicCS());

		return new Position(null,p.getX(),p.getY());
	}

	public static String unformatDEM(String text) {
		String[] split = text.split("d");
		String step = split[0];
		if(step.length()<2) step = "0" + step;
		String value = step;
		split = split[1].split("m");
		step = split[0];
		value += "-" + step;
		return value;
	}

	public static String unformatDEG(String text) {
		String[] split = text.split("d");
		String step = split[0];
		if(step.length()<2) step = "0" + step;
		String value = step;
		split = split[1].split("m");
		step = split[0];
		if(step.length()<2) step = "0" + step;
		value += "-" + step;
		split = split[1].split("s");
		value += "-" + split[0];
		return value;
	}

	/**
	 *
	 * UTM latitude is devided from C --> X (20 rows). Northern hemisphere is
	 * therefore from N --> X and suthern from C --> M.
	 *
	 */

	public static String getSRProjCS_WGS1984UTM_Zone(String zone) {

		// prepare
		zone = zone.trim();

		// valid?
		if (zone.length()==3) {

			// upper or lower hemisphere?
			if(zone.substring(2, 3).compareToIgnoreCase("M")>0)
				return zone.substring(0, 2) + "N";
			else
				return zone.substring(0, 2) + "S";
		}
		// failed
		return null;
	}

	public static String getSRProjCS_WGS1984UTM_Zone(double x, double y) {

		int nZone;
	    double dZone;
		String suffix;

		// get prefix
	    if(x >= 0.0)
	    	suffix = "N";
	    else
	    	suffix = "S";

	    // calculate zone
	    dZone = (180.0 + x) / 6.0;
	    nZone = (int)dZone;
	    if(dZone > nZone) nZone++;

	    // finished
	    return nZone + suffix;

	}

	public static ISpatialReference getProjectedSpatialReference(String zone) {

		try {

			// initialize
			ISpatialReference srs = null;
			SpatialReferenceEnvironment sRefEnv = new SpatialReferenceEnvironment();

		    // get UTM projection string
		    String strUTM = "esriSRProjCS_WGS1984UTM_" + zone;

		    // convert utm string to esriSRProjCSType int value
		    Field field = esriSRProjCSType.class.getField(strUTM);
		    int type = field.getInt(esriSRProjCSType.class);

		    // create projected coordinate system
		    srs = sRefEnv.createProjectedCoordinateSystem(type);

		    // finished
		    return srs;

		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// failed
		return null;
	}

	/**
	 *  Densify route
	 *
	 *  @param int min Minimum distance between two points
	 *  @param int max Maximum distance between two points
	 */
	public static Polyline densify(Polyline polyline, double min, double max) {
		try {
			// initialize
			double d = 0;
			// get clone
			polyline = (Polyline)polyline.esri_clone();
			// densify to maximum length?
			if(max>0) {
				polyline.densify(max, 0);
			}
			// has no lower limit?
			if(min == 0) return polyline;
			// get new polyline
			Polyline newPolyline = new Polyline();
			newPolyline.setSpatialReferenceByRef(polyline.getSpatialReference());
			// get point count
			int count = polyline.getPointCount();
			// has points?
			if(count > 0) {
				// get first position
				IPoint p1 = polyline.getPoint(0);
				// add first point to densified polyline
				newPolyline.addPoint(p1, null, null);
				// loop over all
				for(int i=1;i<count;i++) {
					// get next point
					IProximityOperator p2 = (IProximityOperator)polyline.getPoint(i);
					// calculate absolute distance
					// (will on be accurate for short distances)
					d += Math.abs(p2.returnDistance(p1));
					// large or equal minimum length?
					if(d > min || i == count-1) {
						// add to densified polyline
						newPolyline.addPoint(polyline.getPoint(i), null, null);
						// reset distanse
						d = 0;
					}
					// save current point
					p1 = (IPoint)p2;
				}
			}
			// success
			return newPolyline;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// failed
		return null;
	}

	/**
	 * Calculate great circle distance in meters.
	 *
	 * @param lat1 - Latitude of origin point in decimal degrees
	 * @param lon1 - longitude of origin point in decimal degrees
	 * @param lat2 - latitude of destination point in decimal degrees
	 * @param lon2 - longitude of destination point in decimal degrees
	 *
	 * @return metricDistance - great circle distance in meters
	 */
	public static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {

		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		        Math.sin(dLon/2) * Math.sin(dLon/2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return MEAN_RADIUS * c;
	}


	/**
	 * Calculate Spherical Azimuth (Bearing) in degrees
	 *
	 * @param lat1 - Latitude of origin point in decimal degrees
	 * @param lon1 - longitude of origin point in decimal degrees
	 * @param lat2  - latitude of destination point in decimal degrees
	 * @param lon2  - longitude of destination point in decimal degrees
	 *
	 * @return Spherical Azimuth (Bearing) in degrees
	 */
	public static double sphericalAzimuth(double lat1, double lon1, double lat2, double lon2) {
		double radLat0 = Math.toRadians(lat1);
		double radLon0 = Math.toRadians(lon1);
		double radLat  = Math.toRadians(lat2);
		double radLon  = Math.toRadians(lon2);
		double diff = radLon - radLon0;
		double coslat = Math.cos(radLat);

		return Math.toDegrees(normalizeAngle(Math.atan2(
			coslat * Math.sin(diff),
			(Math.cos(radLat0) * Math.sin(radLat) -
			Math.sin(radLat0) * coslat * Math.cos(diff))
		)));
	}

	/**
	 * Calculate new coordinates from start coordinates, distance from start and azimuth direction
	 *
	 * @param double lat0 - start latitude in decimal degrees
	 * @param double lon0 - start longitude in decimal degrees
	 * @param double d - distance from start in meters
	 * @param double azimuth - bearing from start in degrees
	 *
	 * @return Coordinates in decimal degrees
	 */
	public static Point2D.Double getCoordinate(double lat0, double lon0, double d, double azimuth) {
		double radLat0 = Math.toRadians(lat0);
		double radLon0 = Math.toRadians(lon0);
		double radDist = d / MEAN_RADIUS;
		double radAzim  = Math.toRadians(azimuth);
		/**/
		// calculate latitude
		double radLat1 = Math.asin(Math.sin(radLat0)*Math.cos(radDist)+Math.cos(radLat0)*Math.sin(radDist)*Math.cos(radAzim));
		// calculate longitude movement
	    double dlon = Math.atan2(Math.sin(radAzim)*Math.sin(radDist)*Math.cos(radLat0),Math.cos(radDist)-Math.sin(radLat0)*Math.sin(radLat1));
	    // calculate longitude
	    double radLon1 = reminder(radLon0 + dlon + Math.PI , 2*Math.PI) - Math.PI;
	    // convert to degrees
		double lat1 = Math.toDegrees(radLat1);
		double lon1 = Math.toDegrees(radLon1);
		// finished
		return new Point2D.Double(lon1,lat1);
	}

	/**
	 * Calculate the remainder of the double floating point division of dividend y on divisor x
	 * @param y - dividend
	 * @param x - divisor
	 * @return - the reminder, equivalent to Y - X * floor(Y/X)
	 */
	public static double reminder(double y, double x) {
		return y - x*Math.floor(y/x);
	}

	/**
	 * Calculate a normalized angle (always greater then 0)
	 *
	 * @param double angle - angle to normalize
	 *
	 * @return An angle greater then 0
	 */
	public static double normalizeAngle(double angle) {
		if (angle < 0) angle = 2*Math.PI + angle;
		return angle;
	}

	public static void snapPolyLineTo(Polyline pl, Collection<IIdentify> list, double size) {

		try {
			// initialize
			double min = -1;
			IEnvelope pSearch = null;
			int count = pl.getPointCount();
			IFeatureProxy pFeature = null;
			IProximityOperator pOperator = null;
			IRowIdentifyObjectProxy pRowObj = null;

			// create search envelope
			pSearch = new Envelope();
			pSearch.putCoords(0, 0, 0, 0);
			pSearch.setHeight(size);
			pSearch.setWidth(size);

			// loop over all points
			for(int i=0;i<count;i++) {

				// update current
				IPoint p = pl.getPoint(i);

				// prepare search envelope
				pSearch.centerAt(p);

				// get iterator
				Iterator<IIdentify> it = list.iterator();

				// initialize
				min = -1;
				pOperator = null;

				// loop over all
				while(it.hasNext()) {

					// identify height below point
					IArray arr = it.next().identify(pSearch);

					// found road?
					if (arr != null) {

						// Get the feature that was identified by casting to IRowIdentifyObject
						pRowObj = new IRowIdentifyObjectProxy(arr.getElement(0));
						pFeature = new IFeatureProxy(pRowObj.getRow());

						// get geometry
						IGeometry geom = pFeature.getShape();

						// get geometry shape
						int type = geom.getGeometryType();

						// has proximity operator?
						if (type == esriGeometryType.esriGeometryPoint ||
								type == esriGeometryType.esriGeometryPolyline ||
								type == esriGeometryType.esriGeometryPolygon) {

							// return nearest distance
							double d = ((IProximityOperator)geom).returnDistance(p);

							// less?
							if (min == -1 || d < min) {
								min = d;
								pOperator = (IProximityOperator)geom;
							}
						}
					}
				}

				// return nearest distance
				if (pOperator != null) {
					IPoint near = pOperator.returnNearestPoint(p, esriSegmentExtension.esriNoExtension);
					// snap to nearest point?
					if(near != null)
						pl.updatePoint(i, near);
				}

			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static IPoint createPoint() {
		try {
			return createPoint(0,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IPoint createPoint(IPoint p) {
		try {
			return createPoint(p.getX(), p.getY());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IPoint createPoint(GeoPos p) {
		return createPoint(p.getPosition().x, p.getPosition().y);
	}

	public static IPoint createPoint(double x, double y) {
		try {
			// initialize group geometry
			IPoint p = new Point();
			p.setX(x);
			p.setY(y);
			return p;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IEnvelope createEnvelope() {
		try {
			// create envelope
			return getEnvelope(createPoint(),0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IEnvelope createEnvelope(IPoint p) {
		try {
			// create envelope
			return getEnvelope(p,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static IEnvelope getElementBounds(IDisplay display, IElement element)
		throws UnknownHostException, IOException {
		// initialize
		IEnvelope e = new Envelope();
		// get hdc flag
		boolean bFlag = display.getHDC()==0;
		// get default hdc?
		if(bFlag)
			display.startDrawing(0,(short) esriScreenCache.esriNoScreenCache);
		// query
		element.queryBounds(display, e);
		// finished with hdc?
		if(bFlag)
			display.finishDrawing();
		// finished
		return e;
	}

	public static IEnvelope getSymbolBounds(IDisplay display,
			ISymbol symbol, IGeometry geometry) throws UnknownHostException, IOException {
		// initialize
		IPolygon b = new Polygon();
		// get hdc flag
		boolean bFlag = display.getHDC()==0;
		// get default hdc?
		if(bFlag)
			display.startDrawing(0,(short) esriScreenCache.esriNoScreenCache);
		// query
		symbol.queryBoundary(display.getHDC(), display.getDisplayTransformation(), geometry, b);
		// finished with hdc?
		if(bFlag)
			display.finishDrawing();
		// finished
		return (b!=null) ? b.getEnvelope() : null;
	}

	public static IEnvelope getPictureBounds(IDisplay display, IElement element) throws AutomationException, IOException {
		// valid picture element?
		if(element instanceof IPictureElement3) {
			// initialize
			double[] w = {0};
			double[] h = {0};
			// get hdc flag
			boolean bFlag = display.getHDC()==0;
			// get default hdc?
			if(bFlag) display.startDrawing(0,(short) esriScreenCache.esriNoScreenCache);
			// forward using the IPictureElement3 interface
			((IPictureElement3)element).queryIntrinsicSize(w, h);
			// finished with hdc?
			if(bFlag) display.finishDrawing();
			// convert to display units
			w[0] = display.getDisplayTransformation().fromPoints(w[0]);
			h[0] = display.getDisplayTransformation().fromPoints(h[0]);
			// initialize
			IPoint p = null;
			// get geometry
			IGeometry g = element.getGeometry();
			if(g!=null && !g.isEmpty())
				p = getCenter(g.getEnvelope());
			else
				p = MapUtil.createPoint();
			// create envelope
			IEnvelope e = createEnvelope(p);
			// apply width and height
			e.setWidth(w[0]);
			e.setHeight(h[0]);
			// center at pont
			e.centerAt(p);
			// finished
			return e;
		}
		// failed
		return null;
	}

	public static RgbColor getRgbColor(int r, int g, int b) throws UnknownHostException, IOException {

		// create color
		RgbColor color = new RgbColor();
		color.setRed(r);
		color.setGreen(g);
		color.setBlue(b);

		// finished
		return color;
	}

	public static IMarkerSymbol getMarkerSymbol(int r, int g, int b,  int style, boolean is3D) throws UnknownHostException, IOException {
		return getMarkerSymbol(getRgbColor(r, g, b), style, is3D);
	}

	public static IMarkerSymbol getMarkerSymbol(RgbColor color, int style, boolean is3D) throws UnknownHostException, IOException {

		if(is3D) {
			SimpleMarker3DSymbol marker = new SimpleMarker3DSymbol();
			marker.setStyle(style);
			marker.setColor(color);
			marker.setSize(12);
			marker.setBillboardDisplay(true);
			return marker;
		}
		else {
			SimpleMarkerSymbol marker = new SimpleMarkerSymbol();
			marker.setStyle(style);
			marker.setColor(color);
			marker.setSize(12);
			return marker;
		}
	}

	public static ILineSymbol getLineSymbol(int line) throws UnknownHostException, IOException {

		// finished
		return getLineSymbol(128,128,128,line);

	}

	public static ILineSymbol getLineSymbol(int r, int g, int b, int line) throws UnknownHostException, IOException {

		// finished
		return getLineSymbol(getRgbColor(r,g,b),line);

	}

	public static ILineSymbol getLineSymbol(RgbColor color, int line) throws UnknownHostException, IOException {

		// create outline symbol
		SimpleLineSymbol symbol = new SimpleLineSymbol();
		symbol.setColor(color);
		symbol.setStyle(line);

		// finished
		return symbol;

	}

	public static IFillSymbol getFillSymbol(int fill, int outline) throws UnknownHostException, IOException {

		// finished
		return getFillSymbol(240,240,240,fill,outline);

	}

	public static IFillSymbol getFillSymbol(int r, int g, int b, int fill, int outline) throws UnknownHostException, IOException {

		// finished
		return getFillSymbol(getRgbColor(r,g,b),fill,outline);

	}

	public static IFillSymbol getFillSymbol(RgbColor color, int fill, int outline) throws UnknownHostException, IOException {

		// create fill symbol
		SimpleFillSymbol  symbol = new SimpleFillSymbol();
		symbol.setColor(color);
		symbol.setStyle(fill);

		// set outline symbol in fill symbol
		symbol.setOutline(getLineSymbol(color,outline));

		// finished
		return symbol;

	}

	public static SymbolBorder getSymbolBorder() throws UnknownHostException, IOException {

		// finished
		return getSymbolBorder(esriSimpleLineStyle.esriSLSSolid);
	}

	public static SymbolBorder getSymbolBorder(int style) throws UnknownHostException, IOException {

		// finished
		return getSymbolBorder(style,0);
	}

	public static SymbolBorder getSymbolBorder(int style, int gap) throws UnknownHostException, IOException {

		// create border element
		SymbolBorder symbol = new SymbolBorder();
		symbol.setLineSymbol(getLineSymbol(style));
		symbol.setGap(gap);

		// finished
		return symbol;
	}

	public static SymbolBorder getSymbolBorder(int r, int g, int b, int style) throws UnknownHostException, IOException {

		// create border element
		SymbolBorder symbol = new SymbolBorder();
		symbol.setLineSymbol(getLineSymbol(r,g,b,style));

		// finished
		return symbol;
	}


	public static SymbolBackground getSymbolBackground(double sx, double sy) throws UnknownHostException, IOException {

		// create background
		SymbolBackground symbol = new SymbolBackground();
		symbol.setFillSymbol(getFillSymbol(esriSimpleFillStyle.esriSFSSolid, esriSimpleLineStyle.esriSLSSolid));
		symbol.setHorizontalSpacing(sx);
		symbol.setVerticalSpacing(sy);

		// finished
		return symbol;
	}

	public static ICompositeGraphicsLayer createCompositeGraphicsLayer(IMap map, String name) throws AutomationException, IOException {

		// has this layer already?
		int count = map.getLayerCount();
		for(int i=0;i<count;i++) {
			ILayer l = map.getLayer(i);
			if(l instanceof ICompositeGraphicsLayer) {
				if(name.equalsIgnoreCase(map.getLayer(i).getName())) {
					return (ICompositeGraphicsLayer)l;
				}
			}
		}

		// not found, create and name the new graphics layer
	    ICompositeGraphicsLayer pCGLayer = new CompositeGraphicsLayer();

	    // cast to ILayer
	    ILayer pLayer = (ILayer)pCGLayer;

	    // ser name
	    pLayer.setName(name);

	    // hide
	    pLayer.setVisible(false);

	    // add to map
	    map.addLayer(pLayer);

	    // return graphics layer
	    return pCGLayer;

	}

	public static String getDrawText(IMsoObjectIf msoObj, MsoClassCode code, DrawMode mode) {
		// initialize
		String undef = DiskoEnumFactory.getText(DrawMode.MODE_UNDEFINED);
		// parse state
		if(DrawMode.MODE_CREATE.equals(mode)) {
			return DiskoEnumFactory.getText(DrawMode.MODE_CREATE) + " " + (code != null
					? DiskoEnumFactory.getText(code): undef);
		}
		else if(DrawMode.MODE_REPLACE.equals(mode)) {
			return DiskoEnumFactory.getText(DrawMode.MODE_REPLACE) + " " + (msoObj != null
					? MsoUtils.getMsoObjectName(msoObj, 1) : undef);
		}
		else if(DrawMode.MODE_CONTINUE.equals(mode)) {
			return DiskoEnumFactory.getText(DrawMode.MODE_CONTINUE) + " på "
					+ (msoObj != null ? MsoUtils.getMsoObjectName(msoObj, 1) : undef);
		}
		else if(DrawMode.MODE_APPEND.equals(mode)) {
			return DiskoEnumFactory.getText(DrawMode.MODE_APPEND) + " "+ (code != null
					? DiskoEnumFactory.getText(code): undef);
		}
		return undef;
	}

	public static IEnvelope getMsoExtent(IMsoObjectIf msoObj, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// parse
		if(msoObj instanceof IOperationAreaIf) {
			// forward
			return getOperationAreaExtent((IOperationAreaIf)msoObj, map, constrain);
		}
		else if(msoObj instanceof ISearchAreaIf) {
			// forward
			return getSearchAreaExtent((ISearchAreaIf)msoObj, map ,constrain);
		}
		else if(msoObj instanceof IAreaIf) {
			// get forward
			return MapUtil.getAreaExtent((IAreaIf)msoObj,map ,constrain);
		}
		else if(msoObj instanceof IRouteIf) {
			// get forward
			return MapUtil.getRouteExtent((IRouteIf)msoObj,map ,constrain);
		}
		else if(msoObj instanceof ITrackIf) {
			// get forward
			return MapUtil.getTrackExtent((ITrackIf)msoObj,map ,constrain);
		}
		else if(msoObj instanceof IPOIIf) {
			// get forward
			return MapUtil.getPOIExtent((IPOIIf)msoObj,map ,constrain);
		}
		else if(msoObj instanceof IUnitIf) {
			// get forward
			return MapUtil.getUnitExtent((IUnitIf)msoObj,map ,constrain);
		}
		// failed!
		return null;

	}

	public static IEnvelope getOperationAreaExtent(IOperationAreaIf area, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// initialize frame
		IEnvelope frame = null;
		// get geometry bag of all lines
		IGeometry geoArea =
			MapUtil.getEsriPolygon(area.getGeodata(),map.getSpatialReference());
		// create frame
		if(geoArea!=null)
			frame = geoArea.getEnvelope();
		// limit to minimal extent
		if(constrain) frame.union(getConstrainExtent(getCenter(frame), map));
		// finished
		return frame;
	}

	public static IEnvelope getSearchAreaExtent(ISearchAreaIf area, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// initialize frame
		IEnvelope frame = null;
		// get geometry bag of all lines
		IGeometry geoArea =
			MapUtil.getEsriPolygon(area.getGeodata(),map.getSpatialReference());
		// create frame
		if(geoArea!=null)
			frame = geoArea.getEnvelope();
		// limit to minimal extent
		if(constrain) frame.union(getConstrainExtent(getCenter(frame), map));
		// finished
		return frame;
	}

	public static IEnvelope getAreaExtent(IAreaIf area, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// initialize frame
		IEnvelope frame = null;
		// get geometry bag of all lines
		IGeometry geoArea = getEsriGeometryBag(
				//area.getAreaGeodata().getClone(),
				area.getAreaGeodata(),
        		MsoClassCode.CLASSCODE_ROUTE,
        		map.getSpatialReference());
		// get geometry bag of all points
		IGeometry geoPOI = getEsriGeometryBag(
				area.getAreaPOIs(),
				map.getSpatialReference());
		// create frame
		if(geoArea!=null) {
			frame = geoArea.getEnvelope();
		}
		if(geoPOI!=null) {
			// union with frame
			if(frame==null)
				frame = geoPOI.getEnvelope();
			else
				frame.union(geoPOI.getEnvelope());
		}
		// limit to minimal extent
		if(constrain) frame.union(getConstrainExtent(getCenter(frame), map));
		// finished
		return frame;
	}

	public static IEnvelope getRouteExtent(IRouteIf route, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// initialize frame
		IEnvelope frame = null;
		// convert to a polyline
		IPolyline p = getEsriPolyline(route.getGeodata(), map.getSpatialReference());
		// create frame
		if(p!=null)
			frame = p.getEnvelope();
		// limit to minimal extent
		if(constrain) frame.union(getConstrainExtent(getCenter(frame), map));
		// finished
		return frame;
	}

	public static IEnvelope getTrackExtent(ITrackIf track, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// initialize frame
		Envelope frame = null;
		// convert to a polyline
		IPolyline p = getEsriPolyline(track.getGeodata(), map.getSpatialReference());
		// create frame
		if(p!=null)
			frame = (Envelope)p.getEnvelope();
		// limit to minimal extent
		if(constrain) frame.union(getConstrainExtent(getCenter(frame), map));
		// finished
		return frame;
	}

	public static IEnvelope getPOIExtent(IPOIIf poi, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// get polyline
		IPoint p = getEsriPoint(poi.getPosition().getGeoPos(),map.getSpatialReference());
		// limit to minimal extent
		if(constrain)
			return getConstrainExtent(p,map);
		else
			return p.getEnvelope();
	}

	public static IEnvelope getUnitExtent(IUnitIf unit, IDiskoMap map, boolean constrain) throws AutomationException, IOException {
		// get point
		IPoint p = getEsriPoint(unit.getPosition().getGeoPos(),map.getSpatialReference());
		// limit to minimal extent
		if(constrain)
			return getConstrainExtent(p,map);
		else
			return p.getEnvelope();
	}

	public static IEnvelope getConstrainExtent(IEnvelope extent, IDiskoMap map) throws AutomationException, IOException {
		// get constrain extent
		Envelope constrain = (Envelope)getConstrainExtent(getCenter(extent), map);
		// create union
		constrain.union(extent);
		// finished
		return constrain;
	}

	public static IEnvelope getConstrainExtent(IPoint p, IDiskoMap map) throws AutomationException, IOException {
		if(p==null || p.isEmpty()) return null;
		// get minimum envelope
		if(map.isEditSupportInstalled())
			return getEnvelope(p, map.getSnapAdapter().getSnapTolerance()*10);
		else
			return getEnvelope(p, 100);

	}

	public static boolean addPointsAfter(IPoint p, Polyline source, Polyline path) throws AutomationException, IOException {

		// initialize
		boolean iB[] = null;
		int iP[] = null;
		int iS[] = null;

		// cut source polyline at point p
		source.splitAtPoint(p, true, false, iB, iP, iS);

		// was source split?
		if(iB.length>0) {

			// continue to add subsequent segment end-points to path
			for(int i = iS[0];i<iB.length;i++) {
				// reach
				path.addPoint(source.getSegment(i).getFromPoint(), null, null);
			}

			// success
			return false;

		}
		// failed
		return false;
	}

	public static boolean addPointsBetween(IPoint p1, IPoint p2, Polyline source, Polyline path) throws AutomationException, IOException {

		// initialize
		IPoint s1 = new Point();
		IPoint s2 = new Point();
		double dL1[] = {0};
		double dT1[] = {0};
		boolean bR1[] = {false};
		double dL2[] = {0};
		double dT2[] = {0};
		boolean bR2[] = {false};

		// get information about nearest points
		source.queryPointAndDistance(esriSegmentExtension.esriNoExtension,p1,false,s1,dL1,dT1,bR1);
		source.queryPointAndDistance(esriSegmentExtension.esriNoExtension,p2,false,s2,dL2,dT2,bR2);

		// calculate orientation
		boolean revert = (dL2[0]<dL1[0]);

		// calculate min and max length along the curve (range)
		double min = (revert ? dL2[0] : dL1[0]);
		double max = (revert ? dL1[0] : dL2[0]);

		// get point count
		int count = source.getPointCount();

		// has points?
		if(count>0) {

			// initalize distance
			double d = 0;

			// initialize flag
			boolean inside = false;

			// initialize collection
			List<IPoint> points = new ArrayList<IPoint>();

			// get first point as IProximityOperator
			IProximityOperator p = (IProximityOperator)source.getPoint(0);

			// add all point between p1 and p2
			for(int i = 1; i<count; i++) {

				// get point
				IPoint it = source.getPoint(i);

				// calculate distance along curve from last point
				d += p.returnDistance(it);

				// update point
				p = (IProximityOperator)it;

				// start to add points?
				if(d >= min) inside = true;

				// stop to add points?
				if(d > max) break;

				// add point?
				if(inside) {
					// reverted order?
					if(revert)
						points.add(0,it);
					else
						points.add(it);
				}
			}

			// add points
			if(points.size()>0) {
				for(IPoint it : points) {
					path.addPoint(it,null,null);
				}
				// success
				return true;
			}
		}

		// failure!
		return false;

	}

	/* This method can be uses to algebraically compare X,Y positions.
	 *
	 * IMPORTANT! This method does not take coordinate system and
	 * projections into account. Thus, if the two points are projected
	 * with different coordinate systems, the comparison is undefined!
	 *
	 */

	public static boolean is2DEqual(IPoint p1, IPoint p2) throws AutomationException, IOException {
		return (p1.getX()==p2.getX() && p1.getY()==p2.getY());
	}

	public static boolean is2DEqual(IPoint p1, double x, double y) throws AutomationException, IOException {
		return (p1.getX()==x && p1.getY()==y);
	}

	public static IEnvelope getOperationExtent(IDiskoMap map) throws AutomationException, IOException {
		// initialize
		IEnvelope extent = null;
		// get layer
		IMsoFeatureLayer l = map.getMsoLayer(IMsoFeatureLayer.LayerCode.OPERATION_AREA_LAYER);
		// get extent?
		if(l!=null) {
			extent = getUnion(extent,l.getVisibleFeaturesExtent());
		}
		// get layer
		l = map.getMsoLayer(IMsoFeatureLayer.LayerCode.SEARCH_AREA_LAYER);
		// get extent?
		if(l!=null) {
			extent = getUnion(extent,l.getVisibleFeaturesExtent());
		}
		// get layer
		l = map.getMsoLayer(IMsoFeatureLayer.LayerCode.AREA_LAYER);
		// get extent?
		if(l!=null) {
			extent = getUnion(extent,l.getVisibleFeaturesExtent());
		}
		// get layer
		l = map.getMsoLayer(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		// get extent?
		if(l!=null) {
			extent = getUnion(extent,l.getVisibleFeaturesExtent());
		}
		// get layer
		l = map.getMsoLayer(IMsoFeatureLayer.LayerCode.POI_LAYER);
		// get extent?
		if(l!=null) {
			extent = getUnion(extent,l.getVisibleFeaturesExtent());
		}
		// not found?
		if(extent==null || extent.isEmpty())
			extent = map.getFullExtent();
		// finished
		return extent;
	}

	public static IEnvelope getUnion(IEnvelope e1, IEnvelope e2) throws AutomationException, IOException {
		// found?
		if(e2!=null && !e2.isEmpty()) {
			if(e1==null)
				e1 = e2;
			else
				e1.union(e2);
		}
		return e1;
	}

	public static IFeatureCursor search(IFeatureClass fc, IPoint p, double size) throws UnknownHostException, IOException {
		return search(fc,p,size,esriSpatialRelEnum.esriSpatialRelIntersects);
	}

	public static IFeatureCursor search(IFeatureClass fc, IPoint p, double size, int relation) throws UnknownHostException, IOException {
		ISpatialFilter filter = new SpatialFilter();
		filter.setGeometryByRef(MapUtil.getPolygon(p, size));
		filter.setSpatialRel(relation);
		return fc.search(filter, false);
	}

	public static IFeatureCursor search(IFeatureClass fc, IEnvelope extent) throws UnknownHostException, IOException {
		return search(fc,extent,esriSpatialRelEnum.esriSpatialRelContains);
	}

	public static IFeatureCursor search(IFeatureClass fc, IEnvelope extent, int relation) throws UnknownHostException, IOException {
		ISpatialFilter filter = new SpatialFilter();
		filter.setGeometryByRef(extent);
		filter.setSpatialRel(relation);
		return fc.search(filter, false);
	}

	public static List<Object[]> selectMsoFeaturesFromPoint(IPoint p, IDiskoMap map, double min, double max) throws Exception  {

		// prepare result
		List<Object[]> list = new ArrayList<Object[]>();

		// get elements
		List<IMsoFeatureLayer> layers = map.getMsoLayers();

		// search for feature
		for (int i = 0; i < layers.size(); i++) {
			IMsoFeatureLayer l = (IMsoFeatureLayer)layers.get(i);
			if(l.isSelectable() && l.isVisible()) {
				// get features in search extent
				IFeatureCursor c = search((MsoFeatureModel)l.getFeatureClass(), p, max);
				// select features within {min, max} distance of point p
				Object[] found = selectFeature(c,p,min,max);
				// found?
				if(found!=null) {
					// get feature
					IMsoFeature f = (IMsoFeature)found[0];
					// update minimum length
					min = (Double)found[1];
					// found?
					if(f!=null) {
						list.add(new Object[]{f,l,min});
					}
				}
			}
		}

		// finished
		return list;

	}

	public static List<Object[]> selectMsoFeaturesFromEnvelope(IEnvelope extent, IPoint p, IDiskoMap map, double min, double max, int relation) throws Exception  {

		// prepare result
		List<Object[]> list = new ArrayList<Object[]>();

		// get elements
		List<IMsoFeatureLayer> layers = map.getMsoLayers();

		// search for feature
		for (int i = 0; i < layers.size(); i++) {
			IMsoFeatureLayer l = (IMsoFeatureLayer)layers.get(i);
			if(l.isSelectable() && l.isVisible() && l.isEnabled()) {
				// get all features within search extent
				IFeatureCursor c = search((MsoFeatureModel)l.getFeatureClass(), extent, relation);
				// select features within {min, max} distance of point p?
				if(!(p==null || p.isEmpty())) {
					// forward
					Object[] found = selectFeature(c,p,min,max);
					// found?
					if(found!=null) {
						// get feature
						IMsoFeature f = (IMsoFeature)found[0];
						// update minimum length
						min = (Double)found[1];
						// found?
						if(f!=null) {
							list.add(new Object[]{f,l,min});
						}
					}
				}
				else {
					// add all features found
					IFeature f = c.nextFeature();
					// loop over all features in search extent
					while(f!=null) {
						// is mso feature?
						if (f instanceof IMsoFeature) {
							// is feature selectable?
							if(((IMsoFeature)f).isVisible()) {
								// found
								list.add(new Object[]{f,l,-1});
							}
						}
						// get next feature
						f = c.nextFeature();
					}
				}
			}
		}
		// finished
		return list;
	}

	private static Object[] selectFeature(IFeatureCursor c, IPoint p, double min, double max) throws AutomationException, IOException {
		// initialize
		IMsoFeature ff = null;
		// get fist feature in search extent
		IFeature f = c.nextFeature();
		// loop over all features in search extent
		while(f!=null) {
			// is mso feature?
			if (f instanceof IMsoFeature) {
				// is feature selectable?
				if(((IMsoFeature)f).isVisible()) {
					// get first minimum distance
					double d = getMinimumDistance(f,p);
					// has valid distance?
					if(d!=-1) {
						int shapeType = f.getFeatureType();
						// save found feature?
						if((min==-1 || (d<min) || shapeType==esriGeometryType.esriGeometryPoint) && (d<max)) {
							// initialize
							min = d;
							ff = (IMsoFeature)f;
						}
					}
					else {
						// save found feature
						ff = (IMsoFeature)f;
					}
				}
			}
			// get next feature
			f = c.nextFeature();
		}
		// finished
		return new Object[]{ff,min};
	}

	public static double getMinimumDistance(Object f, IPoint p) throws AutomationException, IOException {
		double min = -1;
		if(f instanceof IProximityOperator) {
			min = ((IProximityOperator)f).returnDistance(p);
		}
		else if(f instanceof IMsoFeature) {
			// get shape
			IGeometry geom = ((IMsoFeature)f).getShape();
			// is geometry bag?
			if(geom instanceof GeometryBag) {
				// cast
				GeometryBag geomBag = (GeometryBag)geom;
				// get count
				int count = geomBag.getGeometryCount();
				// has items?
				if(count>0) {
					// get minimum length of first
					min = getMinimumDistance(geomBag.getGeometry(0), p);
					// loop
					for(int i=1;i<count;i++) {
						// get distance
						double d = getMinimumDistance(geomBag.getGeometry(i), p);
						// update minimum distance
						if(d>0)
							min = java.lang.Math.min(min, d);
					}
				}
			}
			// has proximity operator?
			else if(geom instanceof IProximityOperator) {
				// get point
				IProximityOperator opr = (IProximityOperator)(geom);
				IProximityOperator p2 =  (IProximityOperator)opr.returnNearestPoint(p, esriSegmentExtension.esriExtendEmbedded);
				min = p2.returnDistance(p);
			}
		}
		return min;
	}

	public static IPolygon createCircle(IPoint p, double r) throws AutomationException, IOException {
		Polygon polygon = new Polygon();
		polygon.setSpatialReferenceByRef(p.getSpatialReference());
		Ring ring = new Ring();
		CircularArc arc =  new CircularArc();
		ring.addSegment(arc, null, null);
		polygon.addGeometry(ring, null,null);
		arc.putCoordsByAngle(p, 0, 2*Math.PI, r);
		polygon.geometriesChanged();
		return polygon;
	}

	public static boolean isFloatEqual(Point2D.Double p1, Point2D.Double p2) {

		// is same instance?
		if(p1==p2) return true;

		// is just one null?
		if(p1==null && p2!=null || p1!=null && p2==null) return false;

		// compare float
		return (float)p1.x == (float)p2.x && (float)p1.y == (float)p2.y;

	}

	public static Hashtable<String, String> getLayoutParams(String paramString) {
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



}
