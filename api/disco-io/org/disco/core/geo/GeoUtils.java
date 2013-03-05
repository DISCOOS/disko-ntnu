package org.disco.core.geo;

import java.awt.geom.Point2D;

public class GeoUtils {
	
	private static double MEAN_RADIUS = 6367444.6571; // mean polar and equatorial radius (WGS84)
	
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
	
	public static double convertNMEA(String field) {
		double inp = Double.valueOf(field).doubleValue();
		if(field.indexOf(46) > 3 || field.indexOf(46) < 0) {
			inp /= 100D;
			int i = (int)inp;
			inp -= i;
			inp *= 10D;
			inp /= 6D;
			return inp + (double)i;
		} else {
			return inp;
		}
	}

	public static double convertNMEA(double inp) {
		inp /= 100D;
		int i = (int)inp;
		inp -= i;
		inp *= 10D;
		inp /= 6D;
		return inp + (double)i;
	}    
	
	public static GeoPos fixLatLon(String packet, double lat, double lon ) {
		return new GeoPos(
        		(packet.indexOf(",E,") < 0 ? 180-lon : lon),
        		(packet.indexOf(",N,") < 0 ? -lat : lat));
    }	
	
	public static void fixLatLon(String packet, GeoPos position, double lat, double lon) {
		position.setPosition(
        		(packet.indexOf(",E,") < 0 ? 180-lon : lon),
        		(packet.indexOf(",N,") < 0 ? -lat : lat));
    }	


}
