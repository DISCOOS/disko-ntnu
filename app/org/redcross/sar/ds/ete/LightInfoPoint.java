package org.redcross.sar.ds.ete;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.redcross.sar.map.MapUtil;
import org.redcross.sar.util.mso.GeoPos;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.TimePos;

/**
 *  LightInfo data point class
 *
 * @author kennetgu
 *
 */
public class LightInfoPoint {

	public static final int DAY_LIGHT = 0;
	public static final int CIVIL_TWILIGHT = 1;
	public static final int NAUTICAL_TWILIGHT = 2;
	public static final int ASTRONOMICAL_TWILIGHT = 3;
	public static final int NIGHT_DARK = 4;

	private static final double J2000 = 2451545.0;
	private static final double PERIHELION = 102.9372;
	private static final double OBLIQUITY = 23.45;
	private static final double THETA_0 = 280.1600;
	private static final double THETA_1 = 360.9856235;
	private static final double C_1 = 1.9148;
	private static final double C_2 = 0.02;
	private static final double C_3 = 0.0003;
	private static final double M_0 = 357.5291;
	private static final double M_1 = 0.98560028;
	private static final double J_0 = 0.0009;
	private static final double J_1 = 0.0053;
	private static final double J_2 = -(0.0069);
	private static final double J_3 = 1.0000000;

	private static final double PRECISION = 1e-4;

	private int m_offset;
	private TimePos m_i;
	private double m_horizon;
	private double m_azimuth;

	/* ==================================================
	 * Public constructors
	 * ================================================== */

	/**
	 *  Constructor
	 *
	 *  @param lon LightInfo at longitude
	 *  @param lat LightInfo at latitude
	 *
	 */
	public LightInfoPoint(double lon, double lat){

		// use current time
		Calendar t = Calendar.getInstance();

		// forward
		initialize(t, lon, lat, getTimeZone(t));

		}

	/**
	 *  Constructor
	 *
	 *  @param t Time at position
	 *  @param lon LightInfo at longitude
	 *  @param lat LightInfo at latitude
	 *  @param offset Time zone offset
	 *
	 */
	public LightInfoPoint(Calendar t, double lon, double lat, int offset){
		// forward
		initialize(t, lon, lat, offset);
	}

	private void initialize(Calendar t, double lon, double lat, int offset) {

		// save
		m_i = new TimePos(lon,lat,t);

		// set time zone
		m_offset = offset;

		// fill
		estimateHorizontalCoordinates(this);

	}



	/* ==================================================
	 * Public methods
	 * ================================================== */

	/**
	 * Get time zone offset (in 1h UT steps)
	 *
	 * @return Time zone offset
	 */
	public int getTimeZone() {
		return m_offset;
	}

	/**
	 * Set time zone offset (in 1h UT steps) <\p>
	 *
	 * Time automatically adjusted to local time
	 */
	public void setTimeZone(int offset) {
		// any change?
		if(m_offset != offset) {
			// apply change in offset
			m_i.getTime().roll(Calendar.HOUR_OF_DAY, offset - m_offset);
			// save offset
			m_offset = offset;
		}
	}

	/**
	 * Get observer position
	 *
	 * @return Observer position
	 */
	public GeoPos getPosition() {
		return m_i;
	}

	/**
	 * Get time of observation in local time
	 *
	 * @return Time of observation
	 */
	public Calendar getTime() {
		return getTime(true);
	}

	/**
	 * Get time of observation in local or UTC time (GMT)
	 *
	 * @return Time of observation
	 */
	public Calendar getTime(boolean local) {
		if(local)
			return (Calendar)m_i.getTime().clone();
		else {
			// forward
			return getTime(m_i.getTime(),-m_offset);
		}
	}

	/**
	 * Get sun's horizon
	 *
	 * @return Sun's angular horizon (elevation) at given location and time (in degrees)
	 */
	public double getHorizon() {
		return m_horizon;
	}

	/**
	 * Get sun's horizon as function of light index for day, twilight and night definitions
	 *
	 * @return Sun's angular horizon (elevation)
	 */
	public static double getHorizonFromIndex(int index) {
		switch(index) {
		case DAY_LIGHT: return -0.83;
		case CIVIL_TWILIGHT: return -6;
		case NAUTICAL_TWILIGHT: return -12;
		case ASTRONOMICAL_TWILIGHT: return -18;
		case NIGHT_DARK: return -18;
		default: return -0.83;
		}
	}

	/**
	 * Get light index from sun's horizon
	 *
	 * @return Sun's angular horizon (elevation)
	 */
	public static int getIndexFromHorizon(double angle) {
		if(angle>-0.83)
			return DAY_LIGHT;
		else if(angle<=-0.83 && angle>-6)
			return CIVIL_TWILIGHT;
		else if(angle<-6 && angle>=-12)
			return NAUTICAL_TWILIGHT;
		else if(angle<-12 && angle>=18)
			return ASTRONOMICAL_TWILIGHT;
		else
			return NIGHT_DARK;
	}

	public boolean isDayLight() {
		return getIndexFromHorizon(m_horizon) == DAY_LIGHT;
	}

	public boolean isCivilTwilight() {
		return getIndexFromHorizon(m_horizon) == CIVIL_TWILIGHT;
	}

	public boolean isNauticalTwilight() {
		return getIndexFromHorizon(m_horizon) == NAUTICAL_TWILIGHT;
	}

	public boolean isAstronomicalTwilight() {
		return getIndexFromHorizon(m_horizon) == ASTRONOMICAL_TWILIGHT;
	}

	public boolean isNightDark() {
		return getIndexFromHorizon(m_horizon) == NIGHT_DARK;
	}

	/**
	 * Get sun's azimuth
	 *
	 * @return Sun's azimuth (south is 0 degrees, west is 180 degrees)
	 */
	public double getAzimuth() {
		return m_azimuth;
	}

	public static Calendar getNoon(int year, int month, int day, double lon, double lat) {
		return getTransit(year,month,day,lon,lat,0);
	}

	public static Calendar getDaylight(int year, int month, int day, double lon, double lat, boolean sunrise) {
		return getTransit(year,month,day,lon,lat,getHorizonFromIndex(DAY_LIGHT),sunrise);
	}

	public static Calendar getCivilTwilight(int year, int month, int day, double lon, double lat, boolean sunrise) {
		return getTransit(year,month,day,lon,lat,getHorizonFromIndex(CIVIL_TWILIGHT),sunrise);
	}

	public static Calendar getNauticalTwilight(int year, int month, int day, double lon, double lat, boolean sunrise) {
		return getTransit(year,month,day,lon,lat,getHorizonFromIndex(NAUTICAL_TWILIGHT),sunrise);
	}

	public static Calendar getAstronomicalTwilight(int year, int month, int day, double lon, double lat, boolean sunrise) {
		return getTransit(year,month,day,lon,lat,getHorizonFromIndex(ASTRONOMICAL_TWILIGHT),sunrise);
	}

	public static Calendar getNightDark(int year, int month, int day, double lon, double lat, boolean sunrise) {
		return getTransit(year,month,day,lon,lat,getHorizonFromIndex(NIGHT_DARK),sunrise);
	}

	public static Calendar getTransit(int year, int month, int day, double lon, double lat, double horizon, boolean sunrise) {

		// get Julian Date at noon
		double J = getJulianDate(year,month,day,12);

		// get sun's mean anomaly
		double M = getMeanAnomaly(J);

		// get sun's equation of center
		double C = getEquationOfCenter(M);

		// get the sun's ecliptical longitude
		double lambda = getEclipticalLongitude(M,C);

		// get sun's declination
		double delta = getDeclination(lambda);

		// convert to radians
		double h_rad = Math.toRadians(horizon);
		double lat_rad = Math.toRadians(lat);
		double delta_rad = Math.toRadians(delta);

		// get cos(H)
		double cosH = (Math.sin(h_rad) - Math.sin(lat_rad)*Math.sin(delta_rad))/
			(Math.cos(lat_rad)*Math.cos(delta_rad));

		/* ================================================================
		 * 1. Sun always above given horizon
		 *
		 * If cos(H) is less than -1.0, then the Sun is always above our
		 * altitude limit. If we were computing rise/set times, the Sun
		 * is then aboute the horizon continuously; we have Midnight Sun.
		 * Or, if we computed a twilight, then the sky never gets dark
		 * (a good example is Stockholm, Sweden, at midsummer midnight:
		 * the Sun then only reaches about 7 degrees below the horizon:
		 * there will be civil twilight, but never nautical or
		 * astronomical twilight, at midsummer in Stockholm). Hence no
		 * transit time is defined at this horizon angle.
		 * ================================================================ */

		if(cosH<-1.0) return null;

		 /* ================================================================
		 * 2. Sun always below given horizon
		 *
		 * If cos(H) is greater than +1.0, then the Sun is always below
		 * our altitude limit. One example is midwinter in the arctics,
		 * when the Sun never gets above the horizon. Hence no transit
		 * time is defined at this horizon angle.
		 * ================================================================ */

		if(cosH>1.0) return null;

		 /* ================================================================
		 * 3. Sun passes given position
		 *
		 * If cos(H) is between +1.0 and -1.0, then we can estimate the UT
		 * at which the sun transits this horizon
		 * ================================================================ */

		// get hour angle
		double H = Math.toDegrees(Math.acos(cosH));

		// forward
		return getTransit(year, month, day, lon, lat, sunrise ? -H : H);
	}

	/* ==================================================
	 * Private static methods
	 * ================================================== */

	private static Calendar getTransit(int year, int month, int day, double lon, double lat, double H_target) {

		// get Julian Date at noon
		double J = getJulianDate(year,month,day,12);

		// get Julian date factor
		double n = Math.round((J - J2000 - J_0)/J_3 - (H_target - lon)/360.0);

		// get resonable Julian date estimate
		double J_est = J2000 + J_0 + (H_target - lon) * J_3 / 360.0 + J_3 * n;

		// get mean anomaly for estimated Julian date
		double M = getMeanAnomaly(J_est);

		// get an estimate for the sun's ecliptical longitude
		double L_est = limit(M + PERIHELION + 180.0);

		// get a better estimate for the transit Julian date
		double J_transit = J_est + J_1 * Math.sin(Math.toRadians(M)) + J_2 * Math.sin(Math.toRadians(2 * L_est));

		// forward
		J_transit = improveTransit(J_transit,H_target,lon);

		// get calendar from Julian date
		return getCalendar(J_transit);

	}

	private static double improveTransit(double J, double H, double X) {
		double J_last = J;
		double J_trans = J;
		double H_target = H;
		do {
			// save result
			J_last = J_trans;
			// updat
			H_target = H;
			// get hour angle at current transit date
			H = getHourAngleEx(J_trans, X);
			// improve estimate
			J_trans = J_trans + (H_target - H)/(360.0) * J_3;
			// finished?
		} while(Math.abs(J_last-J_trans)>PRECISION);

		return J_trans;
	}

	private static Calendar getTime(Calendar t, int offset) {
		if(t==null) return null;
		if(offset==0)
			return (Calendar)t.clone();
		else {
			// get clone
			t = (Calendar)t.clone();
			// get UTC?
			if(offset!=0) t.roll(Calendar.HOUR_OF_DAY, offset);
			// finished
			return t;
		}
	}

	private static void estimateHorizontalCoordinates(LightInfoPoint p) {

        // get Julian date for local time
		double J = getJulianDate(p.getTime(true));

		// get sun's mean anomaly
		double M = getMeanAnomaly(J);

		// get sun's equation of center
		double C = getEquationOfCenter(M);

		// get the sun's ecliptical longitude
		double lambda = getEclipticalLongitude(M,C);

		// get sun's right ascension
		double alpha = getRightAscension(lambda);

		// get sun's declination
		double delta = getDeclination(lambda);

		// get geographical longitude
		double X = p.m_i.getPosition().x;

		// get sun's local sidereal time
		double theta = getLocalSideRealTime(J,X);

		// get the sun's hour angle (in degrees, if in hours, use H/15 or 1 hour = 15 degrees)
		double H = getHourAngle(theta,alpha);

		// get geographical latitude
		double Y = p.m_i.getPosition().y;

		// get sun's horizon
		p.m_horizon = getHorizon(delta, H, Y);

		// get sun's azimuth angle
		p.m_azimuth = getAzimuth(delta, H, Y);

	}

	/*
	private static Calendar getTime(int year, int month, int day, int hour, int minute, int second, int offset) {

		// create point in time
		Calendar t = Calendar.getInstance();
		t.set(Calendar.YEAR, year);
		t.set(Calendar.MONTH, month);
		t.set(Calendar.DAY_OF_MONTH, day);
		t.set(Calendar.AM_PM,(int)hour<12 ? 0 : 1);
		t.set(Calendar.HOUR_OF_DAY, hour);
		t.set(Calendar.MINUTE, minute);
		t.set(Calendar.SECOND, second);

		// adjust for time zone offset
		t.roll(Calendar.HOUR_OF_DAY, offset);

		// finished
		return t;

	}
	*/

	private static double getJulianDate(Calendar t) {

        // forward
        return getJulianDate(
        			(double)t.get(Calendar.YEAR),
        			(double)t.get(Calendar.MONTH)+1,
        			(double)t.get(Calendar.DAY_OF_MONTH),
        			(double)(t.get(Calendar.HOUR_OF_DAY) +
        			t.get(Calendar.MINUTE)/60 +
        			t.get(Calendar.SECOND)/(60*60)));

	}


	private static double getJulianDate(double y, double m, double d, double UT) {

        // Step 1
        if(m<3) {
        	m += 12;
        	y--;
        }

        // Step 2
        int c = (int)(2 - Math.floor(y/100) + Math.floor(y/400));

        // Step 3
        d += UT/24;

        // Step 4
        double J = Math.floor(1461*(y+4716)/4) + Math.floor(153*(m+1)/5) + d + c - 1524.5;

        // finished
        return J;

    }

	private static Calendar getCalendar(double J) {

		// parse
		double p = Math.floor(J + 0.5);
		double s_1 = p + 68569;
		double n = Math.floor(4*s_1/146097);
		double s_2 = s_1 - Math.floor((146097*n+3)/4);
		double i = Math.floor(4000*(s_2+1)/1461001);
		double s_3 = s_2 - Math.floor(1461*i/4)+31;
		double q = Math.floor(80*s_3/2447);
		double e = s_3 - Math.floor(2447*q/80);
		double s_4 = Math.floor(q/11);
		double j = 100 * (n - 49) + i + s_4;
		double m = q + 2 - 12 * s_4;
		double d = e + J - p + 0.5;

		// create
		Calendar t = Calendar.getInstance();
		t.set(Calendar.YEAR, (int)j);
		t.set(Calendar.MONTH, (int)(m-1));
		t.set(Calendar.DAY_OF_MONTH, (int)d);
		double hour = d - (int)d;
		t.set(Calendar.AM_PM,(int)(hour*24)<12 ? 0 : 1);
		t.set(Calendar.HOUR_OF_DAY, (int)(hour*24));
		double min = hour*24 - (int)(hour*24);
		double sec = min*60 - (int)(min*60);
		t.set(Calendar.MINUTE, (int)(min*60)+(int)Math.round(sec));
		t.set(Calendar.SECOND, 0);

		// finished
		return t;
	}

	private static int getTimeZone(Calendar t) {
		long time = t.getTimeInMillis();
		return (int)(TimeZone.getDefault().getOffset(time)/(1000*60*60));
	}

	private static double getMeanAnomaly(double J) {

		// get sun's mean anomaly
		double M = M_0  + M_1 * (J - J2000);

		// limit to [0,360>
		return limit(M);

	}

	private static double getEquationOfCenter(double M) {
		// get sun's equation of Center
		double C = C_1 * Math.sin(Math.toRadians(M)) + C_2 * Math.sin(Math.toRadians(2*M)) + C_3 * Math.sin(Math.toRadians(3*M));
		// finished
		return C;
	}

	private static double getEclipticalLongitude(double M, double C) {

		// get the sun's ecliptical longitude
		double lambda = M + PERIHELION + C + 180;

		// limit to [0,360>
		return limit(lambda);

	}

	private static double getRightAscension(double lambda) {
		// get sun's right ascension
		double alpha = Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(lambda))*Math.cos(Math.toRadians(OBLIQUITY)), Math.cos(Math.toRadians(lambda))));
		// finished
		return alpha;
	}

	private static double getDeclination(double lambda) {
		// get sun's declination
		double delta = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(lambda))*Math.sin(Math.toRadians(OBLIQUITY))));
		// finished
		return delta;
	}

	private static double getLocalSideRealTime(double J, double X) {
		// get sun's local sidereal time
		double theta = THETA_0 + THETA_1 * (J - J2000) + X;
		// limit to [0,360>
		return limit(theta);
	}

	private static double getHourAngle(double theta, double alpha) {
		// get the sun's hour angle (in degrees, if in hours, use H/15 or 1 hour = 15 degrees)
		double H = theta - alpha;
		// finished
		return H;
	}

	private static double getHourAngleEx(double J, double X) {

		// get sun's mean anomaly
		double M = getMeanAnomaly(J);

		// get sun's equation of center
		double C = getEquationOfCenter(M);

		// get the sun's ecliptical longitude
		double lambda = getEclipticalLongitude(M,C);

		// get sun's right ascension
		double alpha = getRightAscension(lambda);

		// get sun's local sidereal time
		double theta = getLocalSideRealTime(J,X);

		// get the sun's hour angle (in degrees, if in hours, use H/15 or 1 hour = 15 degrees)
		double H = getHourAngle(theta,alpha);

		// finished
		return H;

	}

	private static double getHorizon(double delta, double H, double Y) {
		// calculate angular horizon
		double horizon = Math.toDegrees(Math.asin(
				Math.sin(Math.toRadians(Y))*Math.sin(Math.toRadians(delta))
				+ Math.cos(Math.toRadians(Y))*Math.cos(Math.toRadians(delta))*Math.cos(Math.toRadians(H))));
		// finished
		return horizon;
	}

	private static double getAzimuth(double delta, double H, double Y) {
		// get sun's azimuth angle
		double azimuth = Math.toDegrees(Math.atan2(Math.sin(Math.toRadians(H)),
				Math.cos(Math.toRadians(H))*Math.sin(Math.toRadians(Y))
				- Math.tan(Math.toRadians(delta))*Math.cos(Math.toRadians(Y))));
		// finished
		return azimuth;
	}

	private static double limit(double angle) {
        while(angle>360) {
        	angle-=360;
        }
        while(angle<0) {
        	angle+=360;
        }
        return angle;
	}

	public static void main(String args[])
	{
		Calendar t = Calendar.getInstance(); // 1 April 2004 at 12:00
		t.set(Calendar.YEAR, 2002);
		t.set(Calendar.MONTH, 6);
		t.set(Calendar.DAY_OF_MONTH, 13);
		t.set(Calendar.HOUR_OF_DAY, 12);
		t.set(Calendar.MINUTE, 0);
		t.set(Calendar.SECOND, 0);

		// get position
		Position p = new Position("",4.3,52.1);
		/*
		try {
			p = MapUtil.getPositionFromUTM("32V05500007039000");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		long tic = System.currentTimeMillis();

		LightInfoPoint light  = new LightInfoPoint(t,p.getPosition().x,p.getPosition().y,0);
		light.print();

		System.out.println("TIME USED:" + (System.currentTimeMillis() - tic) + " ms");

	}

	private void print() {

		try {
			System.out.println("POSITION: " + MapUtil.getDEGfromPosition(getPosition().getPosition()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean local = true;

		printTime("CALENDAR","00:00:00",getTime(),local);
		System.out.println("TIME ZONE OFFSET: " + (m_offset>=0 ? "+" : "-") + m_offset + " UTC");

		NumberFormat f1 = new DecimalFormat("#.#");
        System.out.println("Horizon:"+(f1.format(m_horizon))+" degree");
        System.out.println("Azimuth:"+f1.format(m_azimuth)+" degree");

        // get latitude and longitude
        double lon = getPosition().getPosition().x;
        double lat = getPosition().getPosition().y;

        // get year, month and day
        int year = getTime().get(Calendar.YEAR);
        int month = getTime().get(Calendar.MONTH)+1;
        int day = getTime().get(Calendar.DAY_OF_MONTH);

		Calendar t = getNoon(year,month,day,lon,lat);
		printTime("NOON","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getDaylight(year,month,day,lon,lat,true);
		printTime("DAYLIGTH-SUNRISE","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getDaylight(year,month,day,lon,lat,false);
		printTime("DAYLIGTH-SUNSET","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getCivilTwilight(year,month,day,lon,lat,true);
		printTime("CIVIL-TWILIGHT-SUNRISE","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getCivilTwilight(year,month,day,lon,lat,false);
		printTime("CIVIL-TWILIGHT-SUNSET","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getNauticalTwilight(year,month,day,lon,lat,true);
		printTime("NAUTICAL-TWILIGHT-SUNRISE","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getNauticalTwilight(year,month,day,lon,lat,false);
		printTime("NAUTICAL-TWILIGHT-SUNSET","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getAstronomicalTwilight(year,month,day,lon,lat,true);
		printTime("ASTRONOMICAL-TWILIGHT-SUNRISE","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getAstronomicalTwilight(year,month,day,lon,lat,false);
		printTime("ASTRONOMICAL-TWILIGHT-SUNSET","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getNightDark(year,month,day,lon,lat,true);
		printTime("NIGHT-DARK-SUNRISE","00:00:00",getTime(t, local ? m_offset : 0),local);

		t = getNightDark(year,month,day,lon,lat,false);
		printTime("NIGHT-DARK-SUNSET","00:00:00",getTime(t, local ? m_offset : 0),local);
	}

	private void printTime(String text, String isNull, Calendar t, boolean local) {
		SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		System.out.println(text + ": " + (t==null ? isNull : f.format(t.getTime()) + (local ? " Local" : " UTC")));
	}

}
