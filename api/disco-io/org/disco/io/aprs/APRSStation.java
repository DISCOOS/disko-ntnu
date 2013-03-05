package org.disco.io.aprs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.disco.core.geo.TimePos;
import org.disco.io.Entity;
import org.disco.io.IPacket;

public class APRSStation extends Entity {
	
	protected String callSign;
	protected String ssid;
	protected Object identity;
	
	protected APRSStation(String callSign, String ssid) {
		this.callSign = callSign;
		this.ssid = ssid;
	}
	
	protected APRSStation(APRSPacket packet) {
		this.callSign = packet.callSign;
		this.ssid = packet.ssid;
		packets.add(packet);
	}
	
	public Object getCue() {
		return callSign + "-" + ssid;
	}
	
	public String getCallSign() {
		return callSign;
	}
	
	public String getSSID() {
		return ssid;
	}
	
	public TimePos getLastKnownPosition() {
		if(packets.size()>0) {
			APRSPacket packet = (APRSPacket)packets.get(packets.size()-1);
			TimePos p = new TimePos(packet.getPosition().getPosition(), 
					packet.getAltitude(),packet.getTime());
			return p;
		}
		return null;
	}

	public Collection<TimePos> getTrack() {
		List<TimePos> items = new ArrayList<TimePos>();
		for(IPacket packet : packets) {
			APRSPacket it = (APRSPacket)packet;
			items.add(new TimePos(
				it.getPosition().getPosition(), 
				it.getAltitude(),it.getTime()));
		}
		return items;
	}
	
}
