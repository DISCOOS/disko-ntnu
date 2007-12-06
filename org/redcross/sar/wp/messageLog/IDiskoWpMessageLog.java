package org.redcross.sar.wp.messageLog;

import org.redcross.sar.mso.data.IMessageIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.wp.IDiskoWpModule;

import com.esri.arcgis.geometry.Point;

public interface IDiskoWpMessageLog extends IDiskoWpModule
{
    public final static String bundleName = "org.redcross.sar.wp.messageLog.messageLog";

    /**
	 * Adds or updates the message poi line and generates 
	 * or update the assosiated task
	 * @param type	The poi type
	 * @param point The position
	 */
	
	//public void setMessageLinePOI(POIType type, Point point);
	
}