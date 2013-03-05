package org.disco.io.aprs;

import org.disco.core.geo.GeoPos;
import org.disco.io.Packet;

public class APRSPacket extends Packet {

	protected Integer icon;
	
	protected boolean isAltIcons;
	protected boolean isRealtime;
	
	protected Long time;

	protected Double course;		// in degrees 
	protected Double speed;			// in kph
	protected Double hpe;			// in meters
	protected Double vpe;			// in meters
	protected Double epe;			// in meters
	
	protected String callSign;		// the sender call sign
	protected String ssid;			// the number after a call sign
	protected String path;			// the path
	protected String type;
	protected String comment;
	protected String fix;
	
	private GeoPos position;
	
	/**
	 * IPacketIf attributes
	 */
	public static final String[] ATTRIBUTE_NAMES = {
		"CallSign","SSID","Path","Icon","Position",
		"Course","Speed","Altitude","TimeInMillis",
		"HPE","VPE","EPE","Comment"
	};

	/**
	 * IPacketIf attribute classes
	 */
	public static final Class<?>[] ATTRIBUTE_CLASSES = {
		String.class, String.class, String.class, Integer.class, GeoPos.class, 
		Double.class, Double.class, Double.class, Long.class, 
		Double.class, Double.class, Double.class, String.class
	};	
	
	public APRSPacket(String type, String message, boolean isUnknown, boolean autoReplyExists, boolean isRealtime) {
		super(type,message,isUnknown,autoReplyExists);
		this.isRealtime = isRealtime;		
	}
	
	@Override
	public boolean isIncomplete() {
		return !(isHeaderSet() && isTimeSet() && isPositionSet());
	}

	public boolean isHeaderSet() {
		return (getCue()!=null);
	}

	public boolean isCommentSet() {
		return (comment!=null);
	}

	public String getCue() {
		return (callSign!=null ? callSign : "") 
			+ (ssid!=null ? (ssid.indexOf('-')!=-1 ? ssid : "-" + ssid) : "");
	}
	
	public String getCallSign() {
		return callSign;
	}
	public void setCallSign(String callSign) {
		this.callSign = callSign;
	}
	
	public String getSSID() {
		return ssid;
	}

	public void setSSID(String ssid) {
		this.ssid = ssid;
	}
	
	public Integer getIcon() {
		return icon;
	}

	public void setIcon(Integer icon) {
		this.icon = icon;
	}
	
	public GeoPos getPosition() {
		return position;
	}

	public void setPosition(GeoPos position) {
		this.position = position;
	}
	
	public void setPosition(double lon, double lat) {
		position = new GeoPos(lon,lat);
	}
	
	public void setPosition(double lon, double lat, double alt) {
		position = new GeoPos(lon,lat,alt);
	}
	
	public String getPath() {
		return path;
	}	
	
	public void setPath(String path) {
		this.path = path;
	}	
	
	public Double getCourse() {
		return course;
	}

	public void setCourse(Double course) {
		this.course = course;
	}
	
	public Double getSpeed() {
		return speed;
	}

	public void setSpeed(Double speed) {
		this.speed = speed;
	}
	
	public Double getAltitude() {
		return getPosition()!=null ? getPosition().getAltitude() : 0.0;
	}

	public void setAltitude(Double altitude) {
		if(getPosition()!=null) {
			getPosition().setAltitude(altitude);
		}
	}
	
	public boolean isAltIcons() {
		return isAltIcons;
	}
	
	public void setAltIcons(boolean isAltIcons) {
		this.isAltIcons = isAltIcons;
	}
	
	public boolean isRealtime() {
		return isRealtime;
	}
	
	public boolean isPositionSet() {
		return (position!=null);
	}
	
	public boolean isTimeSet() {
		return (time!=null);
	}

	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Horizontal position error
	 * @return Error (in meters)
	 */
	public Double getHPE() {
		return hpe;
	}

	public void setHPE(Double hpe) {
		this.hpe = hpe;
	}
	
	/**
	 * Vertical position error
	 * @return Error (in meters)
	 */
	public Double getVPE() {
		return vpe;
	}
	
	public void setVPE(Double vpe) {
		this.vpe = vpe;
	}
	
	/**
	 * Estimated position error
	 * @return Error (in meters)
	 */
	public Double getEPE() {
		return epe;
	}

	public void setEPE(Double epe) {
		this.epe = epe;
	}
	
	@Override
	public String getAttrName(int index) {
		return ATTRIBUTE_NAMES[index];
	}

	@Override
	public int getAttrIndex(String name) {
		int count = ATTRIBUTE_NAMES.length;
		for(int i=0;i<count;i++) {
			if(ATTRIBUTE_NAMES[i].equals(name))
				return i;
		}
		return -1;
	}

	@Override
	public int getAttrCount() {
		return ATTRIBUTE_NAMES.length;
	}

	@Override
	public Class<?> getAttrClass(int index) {
		return ATTRIBUTE_CLASSES[index];
	}	

}
