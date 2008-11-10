package org.redcross.sar.app.event;

import org.redcross.sar.app.IService;

public class ServiceEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	public enum ServiceEventType
    {
    	EXECUTE_EVENT
    }

    private int m_flags;
    private ServiceEventType m_eventType;

    protected ServiceEvent(IService source, ServiceEventType type, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }

    @Override
    public IService getSource() {
    	return (IService)super.getSource();
    }

    public int getFlags() {
    	return m_flags;
    }

    public ServiceEventType getType() {
    	return m_eventType;
    }

    public static class Execute extends ServiceEvent
    {

		private static final long serialVersionUID = 1L;

        public Execute(IService source, int flags)
        {
            super(source,ServiceEventType.EXECUTE_EVENT,flags);
        }

    }


}
