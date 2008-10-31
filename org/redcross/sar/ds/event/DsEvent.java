package org.redcross.sar.ds.event;

import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;

public class DsEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	public enum DsEventType
    {
    	ADDED_EVENT,
    	MODIFIED_EVENT,
    	REMOVED_EVENT,
    	CLEAR_ALL_EVENT,
    	INSTALL_EVENT,
    	EXECUTE_EVENT
    }

    private int m_flags;
    private DsEventType m_eventType;

    protected DsEvent(IDs<?> source, DsEventType type, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }

    @Override
    public IDs<?> getSource() {
    	return (IDs<?>)super.getSource();
    }

    public int getFlags() {
    	return m_flags;
    }

    public DsEventType getType() {
    	return m_eventType;
    }

    public static class Update extends DsEvent
    {

		private static final long serialVersionUID = 1L;

		private final IDsObject[] m_data;

        public Update(IDs<?> source, DsEventType type, int flags, IDsObject[] data)
        {
            super(source,type,flags);
            m_data = data;
        }

        public IDsObject[] getData() {
        	return m_data;
        }

    }

    public static class Install extends DsEvent
    {

		private static final long serialVersionUID = 1L;

        public Install(IDs<?> source, int flags)
        {
            super(source,DsEventType.INSTALL_EVENT,flags);
        }

    }

    public static class Execute extends DsEvent
    {

		private static final long serialVersionUID = 1L;

        public Execute(IDs<?> source, int flags)
        {
            super(source,DsEventType.EXECUTE_EVENT,flags);
        }

    }


}
