package org.redcross.sar.map;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.redcross.sar.app.Utils;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.ITrackIf;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.IGeodataIf;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.Track;

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
import com.esri.arcgis.display.ISymbol;
import com.esri.arcgis.display.RgbColor;
import com.esri.arcgis.display.SimpleFillSymbol;
import com.esri.arcgis.display.SimpleLineSymbol;
import com.esri.arcgis.display.esriScreenCache;
import com.esri.arcgis.display.esriSimpleFillStyle;
import com.esri.arcgis.display.esriSimpleLineStyle;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureProxy;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeographicCoordinateSystem;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPolygon;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.ISpatialReferenceFactory2;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polygon;
import com.esri.arcgis.geometry.Polyline;
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
	
	private static IGeographicCoordinateSystem  geographicCS = null;
	private static Workspace workspace = null;
	
	public static Workspace getWorkspace() throws AutomationException, IOException {
		if (workspace == null) {
			String dbPath = Utils.getProperty("Database.path");
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
	
	public static Point getEsriPoint(Position pos, ISpatialReference srs) 
			throws IOException, AutomationException {
		Point p = new Point();
		p.setX(pos.getPosition().getX());
		p.setY(pos.getPosition().getY());
		p.setSpatialReferenceByRef(getGeographicCS());
		p.project(srs);
		return p;
	}
	
	public static IEnvelope expand(double ratio, IEnvelope env) 
		throws IOException, AutomationException {
		env.expand(ratio, ratio, true);
		return env.getEnvelope();
	}
	
	public static IEnvelope expand(double x, double y, boolean isRatio, IEnvelope env) 
		throws IOException, AutomationException {
		env.expand(x, y, isRatio);
		return env.getEnvelope();
	}
	
	public static IEnvelope offset(double x, double y, IEnvelope env) 
		throws IOException, AutomationException {
		env.offset(x, y);
		return env.getEnvelope();
	}
	
	public static IPoint getCenter(IEnvelope env) 
		throws IOException, AutomationException {
		IPoint p = new Point();
		p.setSpatialReferenceByRef(env.getSpatialReference());
		p.setX(env.getXMin()+env.getWidth()/2);
		p.setY(env.getYMin()+env.getHeight()/2);
		return p;
	}
	
	public static String pointToText(Point p, int max, int presision){
		String text = new String();
		try{
			String west = formatCoord(p.getX(),max,presision);
			String north = formatCoord(p.getY(),max,presision);
			text = west + " " + north;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return text; 
	}
	
	private static String formatCoord(double coord, int max, int presision){
		// round up to nearest long value
		long c = Math.round(coord);
		// get length of coordinate
		//int length = Long.toString(c).length();
		// limit presision to [1,max]
		presision = Math.max(1,Math.min(max, presision));
		// limit maximum on coordinate length
		//max = Math.min(max, length);
		// get minimum of limited max and presision
		int min = Math.max(1,Math.min(max, presision));
		// devide on presision
		String s = Long.toString(Math.round(c/Math.pow(10,max-min)));
		// cut to presision if coordinate value was to long
		if(s.length() > presision){
			s = s.substring(s.length()-presision);
		}
		else {
			// get length of padding
			int num = presision-s.length();
			// add padding
			for(int i=0;i<num;i++)
				s = "0"+ s;
		}
		return s;
	}
	
	public static String formatMGRS(String mgrs, int presision){
		return formatMGRS(mgrs,presision,false);
	}
	
	public static String formatMGRS(String mgrs, int presision, boolean html){
		String s = null;
		// is not null?
		if (mgrs != null) {
			// is empty?
			if (mgrs.length() > 0) { 
				// get elements
				String zone = mgrs.subSequence(0, 3).toString();
				String square = mgrs.subSequence(3, 5).toString();
				double x = Double.valueOf(mgrs.subSequence(5, 10).toString());
				double y = Double.valueOf(mgrs.subSequence(10, 15).toString());
				// build formated mgrs
				if(html) {
					s = zone + " " + square + " <b>" + formatCoord(x,5,presision) 
						+ " " + formatCoord(y,5,presision);
				}
				else {
					s = zone + " " + square + " " + formatCoord(x,5,presision) 
							+ " " +formatCoord(y,5,presision);					
				}
			}
		}
		return s;
	}
	
	public static String formatUTM(String utm, boolean html){
		String s = null;
		// is not null?
		if (utm != null) {
			// is empty?
			if (utm.length() > 0) { 
				// get elements
				String zone = utm.subSequence(0, 3).toString();
				double x = Double.valueOf(utm.subSequence(3, 9).toString());
				double y = Double.valueOf(utm.subSequence(10, 16).toString());
				// build formated mgrs
				if(html) {
					s = zone + " <b>" + formatCoord(x,7,7) 
						+ "E " + formatCoord(y,7,7) + "N";
				}
				else {
					s = zone + " " + formatCoord(x,7,7) 
					+ "E " + formatCoord(y,7,7) + "N";
				}
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
		Collection vertices = msoPolygon.getVertices();
		Iterator iter = vertices.iterator();
		while(iter.hasNext()) {
			GeoPos pos = (GeoPos)iter.next();
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
			new org.redcross.sar.util.mso.Polygon(null, null, numPoints);
		
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
			for(IPOIIf poi : msoList.getItems()) {
				// get object
				IGeometry geo = getEsriPoint(poi.getPosition(), srs);					
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
			for(IMsoObjectIf mso : msoList.getItems()) {
				// valid class type?
				if (code == null || mso.getMsoClassCode().equals(code)) {
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
						//geo = getEsriPolyline((Track)geodata, srs);
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
		Collection vertices = route.getPositions();
		Iterator iter = vertices.iterator();
		while(iter.hasNext()) {
			GeoPos pos = (GeoPos)iter.next();
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
	
	public static Route getMsoRoute(Polyline pl) 
			throws IOException, AutomationException {
		Polyline prjPolyline = (Polyline)pl.esri_clone();
		prjPolyline.project(getGeographicCS());
		int numPoints = prjPolyline.getPointCount();
		Route route = new Route(null, null, numPoints);
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
		Track track = new Track(null, null, numPoints);
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
			polygon.addPoint(polygon.getPoint(i), null, null);
		}
		//polyline.simplify();
		return polyline;
	}
	
	public static Polygon getPolygon(Polyline polyline) 
		throws IOException, AutomationException {
		// clone
		polyline = (Polyline)polyline.esri_clone();
		// closing
		polyline.addPoint(polyline.getFromPoint(), null, null);
		// create polygon
		Polygon polygon = new Polygon();
		polygon.setSpatialReferenceByRef(polyline.getSpatialReference());
		for (int i = 0; i < polyline.getPointCount(); i++) {
			polygon.addPoint(polyline.getPoint(i), null, null);
		}
		//polygon.simplify();
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

	public static String getMGRSfromPoint(Point p) 
			throws Exception {
		return p.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
	}

	public static String getUTMfromPoint(Point p) 
			throws Exception {
		String mgrs = p.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		String zone = mgrs.substring(0,3);
		ISpatialReference outgoingCoordSystem = getProjectedSpatialReference(zone);		
		p.project(outgoingCoordSystem);
		return zone + formatCoord(p.getX(),7,7)+ "E" + formatCoord(p.getY(),7,7) + "N";
		
	}
	
	public static String getDESfromPoint(Point p) 
		throws Exception {	
		return formatCoord(p.getX(),7,7)+ "E" + formatCoord(p.getY(),7,7) + "N";
	}
	
	public static String getDEGfromPoint(Point p) 
		throws Exception {
		return fromDEGtoDES(p.getX())+ "E" + fromDEGtoDES(p.getY()) + "N";
	}
	
	private static String fromDEGtoDES(double des) {
		
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
		
		// return string
		return Math.round(d) + Character.toString((char)186) +
			Math.round(m) + Character.toString((char)39) +
			Math.round(s) + Character.toString((char)34);
		
	}
	
	public static String getMGRSfromPosition(Position pos) 
		throws Exception {
		if(pos!=null)
			return getMGRSfromPosition(pos.getPosition());
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
	
	public static String getMGRSfromPosition(Point2D p) 
		throws Exception {
		Point point = new Point();
		point.setX(p.getX());
		point.setY(p.getY());
		point.setSpatialReferenceByRef(getGeographicCS());
		return point.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
	}
	
	public static String getUTMfromPosition(Point2D p) 
		throws Exception {
		Point point = new Point();
		point.setX(p.getX());
		point.setY(p.getY());
		point.setSpatialReferenceByRef(getGeographicCS());	
		String mgrs = point.createMGRS(5, true, esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		String zone = mgrs.substring(0,3);
		ISpatialReference outgoingCoordSystem = getProjectedSpatialReference(zone);		
		point.project(outgoingCoordSystem);
		return zone + formatCoord(point.getX(),7,7)+ "E" + formatCoord(point.getY(),7,7) + "N";
	}
	
	public static String getDEGfromPosition(Point2D p) 
		throws Exception {
		return formatCoord(p.getX(),7,7)+ "E" + formatCoord(p.getY(),7,7) + "N";
	}

	public static String getDESfromPosition(Point2D p) 
		throws Exception {
		return fromDEGtoDES(p.getX())+ "E" + fromDEGtoDES(p.getY()) + "N";
	}
	

	/**
	 * Converts a MGRS position string to a point in decimal degress
	 * 
	 * @param mgrs 	A MGRS string on the strict format ZZZSS(-)######N(-)######E, where
	 * 				zzz equals zone, SS equals 100-km square, x = ######E equals 
	 * 				east direction (x) and y = ######N equals north direction (y)
	 * @return {@link Point}
	 * @throws Exception
	 */	
	public static Point getPointFromMGRS(String mgrs) 
		throws Exception {
		// trim 
		mgrs = mgrs.trim();
		String prefix = mgrs.substring(0,5);
		String suffix = mgrs.substring(5,mgrs.length());
		suffix = suffix.toUpperCase().replace("E", "").replace("N", "");
		Point point = new Point();
		point.setSpatialReferenceByRef(getGeographicCS());
		point.putCoordsFromMGRS(prefix.concat(suffix), esriMGRSModeEnum.esriMGRSMode_NewWith180InZone01);
		return point;
	}
	
	/**
	 * Converts a UTM position string to a point in decimal degress
	 * 
	 * IMPORTANT: Only a subset of all UTM strings are supported (32-36 N)
	 * 
	 * @param utm 	A UTM string on the strict format ZZZ(-)########N(-)########E, where
	 * 				zzz equals zone, x = ########E equals east direction (x) 
	 * 				and y = ########N equals north direction (y)
	 * @return {@link Point}
	 * @throws Exception
	 */
	public static Point getPointFromUTM(String utm) 
		throws Exception {
		utm = utm.trim();
		String zone = utm.subSequence(0, 3).toString();
		String x = utm.subSequence(3, 10).toString();
		String y = utm.subSequence(11, 18).toString();
		Point point = new Point();
		ISpatialReference incommingCoordSystem = getProjectedSpatialReference(zone);		
		point.setSpatialReferenceByRef(incommingCoordSystem);
		point.setX(Double.valueOf(x));
		point.setY(Double.valueOf(y));
		point.project(getGeographicCS());
		return point;
	}

	/**
	 * Converts a DEGREE position string to a point in decimal degress
	 * 
	 * @param deg	A DEGREE string on the strict format (-)##*##'##''E(-)##*##'##''N, where
	 * 				x = ##*##'##''E equals east direction (x) 
	 * 				and y = ##*##'##''N equals north direction (y)
	 * @return {@link Point}
	 * @throws Exception
	 */
	public static Point getPointFromDEG(String deg) 
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
		
		// parse longitude (E) 
		double d1 = Double.valueOf(lat.subSequence(0, 2+offset).toString());
		double m1 = Double.valueOf(lat.subSequence(3+offset, 5+offset).toString());
		double s1 = Double.valueOf(lat.subSequence(6+offset, 8+offset).toString());
		
		// account for sign
		offset = lon.startsWith("-") ? 1 : 0;
		
		// parse latitude (N)
		double d2 = Double.valueOf(lon.subSequence(0, 2+offset).toString());
		double m2 = Double.valueOf(lon.subSequence(3+offset, 5+offset).toString());
		double s2 = Double.valueOf(lon.subSequence(6+offset, 8+offset).toString());
		
		// Determine longitude fraction from minutes and seconds
		double f1 = Double.valueOf(m1) / 60 + Double.valueOf(s1) / 3600;

		// Be careful to get the sign right.
		double dec1 = ( d1 < 0 ) ? d1 - f1 : d1 + f1;
		
		// Determine latitude fraction from minutes and seconds
		double f2 = Double.valueOf(m2) / 60 + Double.valueOf(s2) / 3600;
		
		// Be careful to get the sign right.
		double dec2 = ( d2 < 0 ) ? d2 - f2 : d2 + f2;
		
		// create point
		Point point = new Point();
		point.setSpatialReferenceByRef(getGeographicCS());
		point.setX(dec1);
		point.setY(dec2);
		return point;
		
	}
	
	/**
	 * Converts a DECIMAL DEGREE position string to a point in decimal degress
	 * 
	 * @param deg	A DEGREE string on the strict format ##.#[#-->#]E##.#[#-->#]N, where
	 * 				x = ##.#[#-->#]E equals east direction (x) 
	 * 				and y = ##.#[#-->#]N equals north direction (y)
	 * @return {@link Point}
	 * @throws Exception
	 */
	public static Point getPointFromDES(String des) 
		throws Exception {
	
		// remove any spaces
		des = des.trim().toUpperCase();
		
		// split into longitude and latitude
		String[] split = des.split("E");
		
		// get sub strings
		String lat = split[0]; 
		String lon = split[1].replace("N", ""); 
		
		// parse longitude (E) 
		double x = Double.valueOf(lat);
		
		// parse latitude (N)
		double y = Double.valueOf(lon);
		// create point
		Point point = new Point();
		point.setSpatialReferenceByRef(getGeographicCS());
		point.setX(x);
		point.setY(y);
		return point;
		
	}
		
	public static Position getPositionFromMGRS(String mgrs) 
			throws Exception {
		IPoint point = getPointFromMGRS(mgrs);
		return new Position(null, point.getX(), point.getY());
	}
	
	public static Position getPositionFromUTM(String utm) 
		throws Exception {
		Point point = getPointFromUTM(utm);
		return new Position(null,point.getX(),point.getY());
	}
	
	public static Position getPositionFromDEG(String deg) 
		throws Exception {
		Point point = getPointFromDEG(deg);
		return new Position(null,point.getX(),point.getY());
	}
	
	public static Position getPositionFromDES(String des) 
		throws Exception {
		Point point = getPointFromDES(des);
		return new Position(null,point.getX(),point.getY());
	}
	
	public static ISpatialReference getProjectedSpatialReference(String zone) {
		
		zone = zone.trim();
		
		if (zone.length()!=3) return null;
		
		ISpatialReference coordSys = null;
		try {
			SpatialReferenceEnvironment sRefEnv = new SpatialReferenceEnvironment();
			
			/* 
			 *
			 * UTM latitude is devided from C --> X (20 rows). Northern hemisphere is
			 * therefor from N --> X and suthern from C --> M.
			 * 
			 * UTM longitude is devided from 1 --> 60
			 * 
			 * Only Norway is supported yet (32-36 N)
			 * 
			 * TODO: Implement generic convertion from xxy, where xx=[1,60], y={S,N} to 
			 * esriSRProjCSType.esriSRProjCS_WGS1984UTM_xxy
			 */
			
			// upper or lower hemisphere?
			if(zone.substring(2, 3).compareToIgnoreCase("M")>0) {
				// Is nothern hemisphere, dispatch belt 
				if(zone.substring(0, 2).compareToIgnoreCase("32")==0) {
					coordSys = sRefEnv.createProjectedCoordinateSystem(esriSRProjCSType.esriSRProjCS_WGS1984UTM_32N);
				}
				else if(zone.substring(0, 2).compareToIgnoreCase("33")==0) {
					coordSys = sRefEnv.createProjectedCoordinateSystem(esriSRProjCSType.esriSRProjCS_WGS1984UTM_33N);				
				}
				else if(zone.substring(0, 2).compareToIgnoreCase("34")==0) {
					coordSys = sRefEnv.createProjectedCoordinateSystem(esriSRProjCSType.esriSRProjCS_WGS1984UTM_34N);				
				}
				else if(zone.substring(0, 2).compareToIgnoreCase("35")==0) {
					coordSys = sRefEnv.createProjectedCoordinateSystem(esriSRProjCSType.esriSRProjCS_WGS1984UTM_35N);				
				}
				else if(zone.substring(0, 2).compareToIgnoreCase("36")==0) {
					coordSys = sRefEnv.createProjectedCoordinateSystem(esriSRProjCSType.esriSRProjCS_WGS1984UTM_36N);				
				}
			}

		}
		catch(Exception e) {
			// invalid zone
		}
		return coordSys;
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
	 * @param lon1 - longitude of origin point in deceimal degrees
	 * @param lat2 - latitude of destination point in decimal degrees
	 * @param lon2 - longitude of destination point in decimal degrees
	 * 
	 * @return metricDistance - great circle distance in meters
	 */
	public static double greatCircleDistance(double lat1, double lon1, double lat2, double lon2) {
		double R = 6367444.6571; // mean polar and equatorial radius (WGS84)
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		return R * c;
	}
	
	
	/**
	 * Calculate Spherical Azimuth (Bearing) in degrees
	 * 
	 * @param lat0 - Latitude of origin point in decimal degrees
	 * @param lon0 - longitude of origin point in deceimal degrees
	 * @param lat  - latitude of destination point in decimal degrees
	 * @param lon  - longitude of destination point in decimal degrees
	 * 
	 * @return Spherical Azimuth (Bearing) in degrees
	 */
	public static double sphericalAzimuth(double lat0, double lon0, double lat, double lon) {
		double radLat0 = Math.toRadians(lat0);
		double radLon0 = Math.toRadians(lon0);
		double radLat  = Math.toRadians(lat);
		double radLon  = Math.toRadians(lon);
		double diff = radLon - radLon0;
		double coslat = Math.cos(radLat);

		return Math.toDegrees(normalizeAngle(Math.atan2(
			coslat * Math.sin(diff),
			(Math.cos(radLat0) * Math.sin(radLat) -
			Math.sin(radLat0) * coslat * Math.cos(diff))
		)));
	}
	
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
	
	
	public static IPoint createPoint(double x, double y) {
		try {
			// initialize group geometry
			IPoint p = new Point();
			p.setSpatialReferenceByRef(getGeographicCS());
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
		symbol.setOutline(getLineSymbol(outline));		
		
		// finished
		return symbol;
		
	}	
	
	public static SymbolBorder getSymbolBorder() throws UnknownHostException, IOException {
		
		// finished
		return getSymbolBorder(esriSimpleLineStyle.esriSLSSolid);
	}
	
	public static SymbolBorder getSymbolBorder(int style) throws UnknownHostException, IOException {
		
		// create border element
		SymbolBorder symbol = new SymbolBorder();
		symbol.setLineSymbol(getLineSymbol(style));
		
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
	    
	    // make cache
	    pLayer.setCached(true);
	    
	    // hide
	    pLayer.setVisible(false);
	    
	    // add to map
	    map.addLayer(pLayer);
	    	    
	    // return graphics layer
	    return pCGLayer;

	}	
	
}
