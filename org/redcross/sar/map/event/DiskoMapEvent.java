package org.redcross.sar.map.event;

import java.util.EventObject;

import org.redcross.sar.map.IDiskoMap;

public class DiskoMapEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    public enum MapEventType
    {
        MOUSE_CLICK,
        MOUSE_MOVE,
        EXTENT_CHANGED,
        MAP_REPLACED,
        SELECTION_CHANGED
    }

    private int m_flags;
    private Object[] m_data;
    private MapEventType m_eventType;

    public DiskoMapEvent(IDiskoMap source, MapEventType type, Object[] data, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }

    @Override
    public IDiskoMap getSource() {
        return (IDiskoMap)super.getSource();
    }

    public Object[] getData() {
        return m_data;
    }

    public int getFlags() {
        return m_flags;
    }

    public MapEventType getType() {
        return m_eventType;
    }

    public boolean isType(MapEventType type) {
        return m_eventType.equals(type);
    }


}
