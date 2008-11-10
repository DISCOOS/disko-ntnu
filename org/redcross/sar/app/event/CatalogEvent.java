package org.redcross.sar.app.event;

import org.redcross.sar.app.IService;
import org.redcross.sar.app.IServiceCatalog;

public class CatalogEvent extends java.util.EventObject {

	private static final long serialVersionUID = 1L;

	public enum ServiceEventType
    {
    	INSTANCE_EVENT
    }

    private int m_flags;
    private ServiceEventType m_eventType;

    protected CatalogEvent(IServiceCatalog<? extends IService> source, ServiceEventType type, int flags)
    {
        super(source);
        m_flags = flags;
        m_eventType = type;
    }

	@Override
    @SuppressWarnings("unchecked")
    public IServiceCatalog<IService>  getSource() {
    	return (IServiceCatalog<IService> )super.getSource();
    }

    public int getFlags() {
    	return m_flags;
    }

    public ServiceEventType getType() {
    	return m_eventType;
    }

    public static class Instance extends CatalogEvent
    {

		private static final long serialVersionUID = 1L;

		private final IService m_service;

        public Instance(IServiceCatalog<? extends IService> source, IService service, int flags)
        {
            super(source,ServiceEventType.INSTANCE_EVENT,flags);
            m_service = service;
        }

        public IService getService() {
        	return m_service;
        }

    }


}
