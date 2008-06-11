package org.redcross.sar.wp.messageLog;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.wp.IDiskoWpModule;

public interface IDiskoWpMessageLog extends IDiskoWpModule, IDiskoWorkListener
{
    public final static String bundleName = "org.redcross.sar.wp.messageLog.messageLog";
    
    public enum MessageLogActionType {
    	CHANGE_DTG,
        CHANGE_FROM,
        CHANGE_TO,
        SET_TEXT,
        SET_POSITION,
        SET_POI,
        SET_ASSIGNMENT,
        SET_STARTED,
        SET_COMPLETED,
        SHOW_LIST,
        DELETE,
        CHANGE_TASKS,
        CANCEL,
        SET_WAIT_STATUS,
        SET_FINISHED,
        CENTER_AT
    }

}