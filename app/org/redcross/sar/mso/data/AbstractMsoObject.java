package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.ChangeImpl;
import org.redcross.sar.mso.ChangeRecordImpl;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IChangeIf.IChangeRelationIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.mso.data.AttributeImpl.MsoInteger;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.InvalidRelationException;
import org.redcross.sar.util.except.MsoRuntimeException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.UnknownAttributeException;
import org.redcross.sar.util.mso.*;

/**
 * Abstract class on which all MSO data objects in the MSO data model is based on.
 * This class has double book keeping of attributes, both in a hash map for name 
 * lookup, and an array list for index lookup.
 * 
 * @author vinjar, kenneth
 * 
 */
//@SuppressWarnings("unchecked")
public abstract class AbstractMsoObject implements IMsoObjectIf
{
	/**
	 * The logger object for all AbstractMsoObject objects
	 */	
    private Logger m_logger;
	
    /**
     * The unique object id, must exist for all MSO Objects.
     */
    private final IObjectIdIf m_msoObjectId;

    /**
     * Hook to the MSO model instance.
     */
    protected final IMsoModelIf m_model;

    /**
     * Reference to EventImpl Manager.
     */
    protected final IMsoEventManagerIf m_eventManager;

    /**
     * Hook to the main list owning this object
     */
    @SuppressWarnings("unchecked")
	protected MsoListImpl m_mainList;

    /**
     * Map of attributes for name lookup.
     */
    private final Map<String, AttributeImpl<?>> m_attributeMap = new LinkedHashMap<String, AttributeImpl<?>>();

    /**
     * List of attributes for index lookup.
     */
    private final ArrayList<AttributeImpl<?>> m_attributeList = new ArrayList<AttributeImpl<?>>();

    /**
     * Set of relation objects (one-to-one relations).
     */
    private final Map<String, MsoRelationImpl<IMsoObjectIf>> m_relationObjects = new LinkedHashMap<String, MsoRelationImpl<IMsoObjectIf>>();

    /**
     * Set of relation lists (one-to-many relations).
     */
    private final Map<String, MsoListImpl<IMsoObjectIf>> m_relationLists = new LinkedHashMap<String, MsoListImpl<IMsoObjectIf>>();

    /**
     * Endless loop prevention
     */
    private boolean m_suspendChangeInProgress = false;
    
    /**
     * Suspend client event count
     */
    private int m_suspendChangeCount = 0;
    
    /**
     * Change buffer implementation. This list 
     * contains all changes occurred since change updates was 
     * suspended.
     */
    private List<IChangeIf> m_slaveChangeBuffer = new Vector<IChangeIf>();
    
    /**
     * Derived change buffer implementation. This list
     * contains all changes occurred since derived updates was 
     * suspended.
     */
    private List<IChangeIf> m_coChangeBuffer = new Vector<IChangeIf>();
    
    /**
     * Suspend derived update events flag
     */
    private boolean m_suspendCoChange = false;
    
    /**
     * Change tracking counter
     */
    private int m_changeCount = 0;

    /**
     * Indicator that tells if {@link #m_attributeList} is sorted
     */
    private boolean m_listSorted = true;

    /**
     * Set of object holders, used when deleting object
     */
    private final Set<IMsoObjectHolderIf> m_objectHolders = new HashSet<IMsoObjectHolderIf>();

    /**
     * Flag indicating that required abstract methods are called
     */
    private boolean m_isSetup = false;

    /**
     * Flag indicating that the object has been successfully deleted
     */
    private boolean m_isDeleted;

	/*-------------------------------------------------------------------------------------------
	 * Constructors
	 *-------------------------------------------------------------------------------------------*/

    /**
     * Constructor
     *
     * @param anObjectId The Object Id
     */
    protected AbstractMsoObject(IMsoModelIf theMsoModel, IObjectIdIf anObjectId)
    {
        if (anObjectId == null || anObjectId.getId() == null || anObjectId.getId().length() == 0)
        {
            throw new MsoRuntimeException("Tried to create object with no well defined object id.");
        }
        
    	// initialize logger
        m_logger = Logger.getLogger(getClass());
        
        // initialize object
        m_msoObjectId = anObjectId;
        m_model = theMsoModel;
        m_eventManager = m_model.getEventManager();
        // prepare creation
        suspendChange();
        suspendCoChange();
        // finalize creation
        registerCreatedObject();
        // log the occurrence
        m_logger.debug("Created " + this + " in model " + m_model.getID());
    }

	/*-------------------------------------------------------------------------------------------
	 * Initializing methods
	 *-------------------------------------------------------------------------------------------*/

    /**
     * This method MUST be called after creation of IMsoObjectIf objects.
     *
     * If not, no MSO attributes, lists or relations will be created. If this
     * is created by a MsoListImpl, the list will invoke the method
     * automatically.
     *
     * @param boolean resume - If <code>true</code>, updates are resumed and update
     * listeners changes will be notified of any changes. If more update actions are
     * required after setup is required, use <code>false</code> to buffer these changes
     * and call <code>resumeClientUpdate()</code> when finished. This ensures that
     * a minimum of update events are propagated throughout the system.
     *
     */
    public void setup(boolean resume)
    {
        if (m_isSetup)
        {
            Log.error("Error in setup: " + this + " is setup already");
        }
        m_isSetup = true;
        defineAttributes();
        defineLists();
        defineObjects();
        resumeCoChange();
        if(resume) resumeChange(false);
    }

    /**
     * This empty method should contain all IAttributeIf defining actions
     *
     * The method must be implemented by extending object
     */
    protected abstract void defineAttributes();

    /**
     * This empty method should contain all IMsoListIf defining actions
     *
     * This method must be implemented by extending object
     */
    protected abstract void defineLists();

    /**
     * This empty method should contain all IMsoReferenceIf defining actions
     *
     * This method must be implemented by extending object
     */
    protected abstract void defineObjects();

    /*-------------------------------------------------------------------------------------------
	 * Public methods
	 *-------------------------------------------------------------------------------------------*/

    public String getObjectId()
    {
        return m_msoObjectId.getId();
    }

    public Calendar getCreatedTime()
    {
        return m_msoObjectId.getCreatedTime();
    }

    /**
     * Sets created state
     *
     */
    public void setCreatedTime(Date time) {
    	m_msoObjectId.setCreatedTime(time);
    	Calendar t = m_msoObjectId.getCreatedTime();
        for (AttributeImpl<?> attr : m_attributeList)
        {
            if(attr instanceof MsoEnum) {
            	MsoEnum<?> e = (MsoEnum<?>)attr;
            	e.setInitialTime(t);
            }
        }
    }

    public boolean isCreated()
    {
        return m_msoObjectId.isCreated();
    }
    
    @Override
	public boolean isOriginLocal() {
    	return isOrigin(IData.DataOrigin.LOCAL);
	}

	@Override
	public boolean isOriginRemote() {
		return isOrigin(IData.DataOrigin.REMOTE);
	}

	@Override
	public boolean isOriginConflict() {
		return isOrigin(IData.DataOrigin.CONFLICT);
	}
	
	public boolean isOriginMixed() {
		return isOrigin(IData.DataOrigin.MIXED);
	}	
	
	@Override
	public boolean isOrigin(IData.DataOrigin origin) {
		if(origin!=null)			
		{
			return origin.equals(getOrigin());
		}
        return false;
	}
	
	@Override
	public IData.DataOrigin getOrigin() {
		
		IData.DataOrigin origin = IData.DataOrigin.NONE;
		
        for (AttributeImpl<?> attr : m_attributeList)
        {
        	IData.DataOrigin o = attr.getOrigin();
            if(!origin.equals(o)) 
            {
            	if(!origin.equals(IData.DataOrigin.NONE))
    			{
            		return IData.DataOrigin.MIXED;
    			}
        		origin = o;
            }
        }
        for (MsoListImpl<?> list : m_relationLists.values())
        {
        	IData.DataOrigin o = list.getOrigin();
            if(!origin.equals(o)) 
            {
            	if(!origin.equals(IData.DataOrigin.NONE))
    			{
            		return IData.DataOrigin.MIXED;
    			}
        		origin = o;
            }
        }

        for (MsoRelationImpl<?> relation : m_relationObjects.values())
        {
        	IData.DataOrigin o = relation.getOrigin();
            if(!origin.equals(o)) 
            {
            	if(!origin.equals(IData.DataOrigin.NONE))
    			{
            		return IData.DataOrigin.MIXED;
    			}
        		origin = o;
            }
        }		
        // all data has same origin
        return origin;
	}

    public boolean isChanged() {
    	return m_model.getChanges(this)!=null;
    }
    
    public boolean isChangedSince(int changeCount)
    {
        return (m_changeCount>=changeCount);
    }
    
    public int getChangeCount()
    {
        return m_changeCount;
    }
    
	
	@Override
	public boolean isLoopbackMode() {
        for (AttributeImpl<?> attr : m_attributeList)
        {
            if(!attr.isLoopbackMode()) return false;
        }
        for (MsoListImpl<?> list : m_relationLists.values())
        {
            if(!list.isLoopbackMode()) return false;
        }

        for (MsoRelationImpl<?> relation : m_relationObjects.values())
        {
            if(!relation.isLoopbackMode()) return false;
        }				
        return true;
	}
	
	@Override
	public boolean isRollbackMode() {
        for (AttributeImpl<?> attr : m_attributeList)
        {
            if(!attr.isRollbackMode()) return false;
        }
        for (MsoListImpl<?> list : m_relationLists.values())
        {
            if(!list.isRollbackMode()) return false;
        }

        for (MsoRelationImpl<?> relation : m_relationObjects.values())
        {
            if(!relation.isRollbackMode()) return false;
        }				
        return true;		
	}
	
	@Override
	public boolean isState(DataState state) {
		if(state!=null)
		{
			return state.equals(getState());
		}
		return false;
	}

	@Override
	public DataState getState() {
		DataState state = DataState.NONE;
		if(isOriginMixed())
		{
			return DataState.MIXED;
		}
		else if(isChanged())
		{
			if(isOriginConflict())
			{
				state = DataState.CONFLICT;
			}
			else 
			{
				state = DataState.CHANGED;
			}
			
		}
		else 
		{
			if(isRollbackMode())
			{
				state = DataState.ROLLBACK;
			}
			else if(isOriginRemote() || isLoopbackMode())
			{
				state = DataState.LOOPBACK;
			} 
		}
		// mixed state?
		if(isDeleted() && !state.equals(DataState.NONE)) 
		{
			// finished
			return DataState.MIXED;
		}
		
		// finished
		return state;
	}    

    public String shortDescriptor()
    {
        return toString();
    }

    public String toString()
    {
        return Internationalization.translate(getClassCode()) + " " + m_msoObjectId.getId();
    }

    protected void addMsoObjectHolder(IMsoObjectHolderIf aHolder)
    {
    	// only add once
    	if(!m_objectHolders.contains(aHolder))
    		m_objectHolders.add(aHolder);

        //System.out.println("Add delete listener from: " + this + " to: " + aHolder + ", count = " + m_objectHolders.size());
    }

    protected void removeMsoObjectHolder(IMsoObjectHolderIf aHolder)
    {
    	// only remove if exists
    	if(m_objectHolders.contains(aHolder))
    		m_objectHolders.remove(aHolder);

        //System.out.println("Remove delete listener from: " + this + " to: " + aHolder + ", count = " + m_objectHolders.size());
    }

    public int getMsoObjectHolderCount()
    {
        return m_objectHolders.size();
    }

    public boolean isDeleted()
    {
        return m_isDeleted;
    }

    public boolean isDeletable()
    {

    	// In REMOTE_UPDATE_MODE, delete is always allowed. Only in
    	// LOCAL mode are delete operations validated.
    	if (!m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE))
        {
            return true;
        }

    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.isRelationDeletable(this)) return false;
        }
        // all object holders allow a deletion
        return true;
    }
    
    public boolean delete(boolean deep)
    {
        if (isDeletable())
        {
        	suspendChange();        	
        	if(deep)
        	{
	            for (MsoListImpl<?> list : m_relationLists.values())
	            {
	                list.removeAll();
	            }
	
	            for (MsoRelationImpl<?> relation : m_relationObjects.values())
	            {
	                if (relation.get() != null)
	                {
	                    relation.set(null);
	                	m_logger.debug("Deleted object relation from " + this + " to " + relation.get());
	                }
	            }
        	}
            destroy();
            resumeChange(true);
            return true;
        }
        return false;
    }

    /**
     * <b>This method is intended for internal use only!</b></p>
     *
     * <b>IMPORTANT! </b> Use <code>delete()</code> to delete a object. </p>
     * 
     * The method only performs a delete on the object and
     * notifies any object holders of this delete. It does not
     * delete owned objects in lists, or relations from this object to
     * other owned objects explicitly. The result of calling this method 
     * mindlessly is a potential memory leak because owned objects may still 
     * point to this object. Hence, the object will not be garbage collected.</p>
     * 
     */
    @SuppressWarnings("unchecked")
	private void destroy()
    {
        /* Get loopback flag. This test is based on the assumption 
         * that an invocation of destroy() only occurs once in 
         * LOCAL_UPDATE_MODE, and once is REMOVE_UPDATE_MODE, when
         * the object is deleted locally. It is implicitly assumed 
         * that destroy is not called any more after this. */
        boolean isLoopback = m_isDeleted && !m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        
        /* Get rollback flag. Delete results in a rollback 
         * mode if, and only, the object is not created (the
         * object does not exist remotely). */
        boolean isRollback = !(m_isDeleted || m_msoObjectId.isCreated()) && m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);

        // set as deleted
        m_isDeleted = true;
        
        // suspend update notifications
    	suspendChange();
    	
    	// notify object holders
    	m_logger.debug("Notify holders that relations to " + this + " should be deleted");
        while (m_objectHolders.size() > 0)
        {
            Iterator<IMsoObjectHolderIf> iterator = m_objectHolders.iterator();
            IMsoObjectHolderIf myHolder = iterator.next();
            myHolder.deleteRelation(this);
        }
        
        // notify client update listeners
        registerDeletedObject(isLoopback,isRollback);
        
        // resume update notifications
    	resumeChange(true);
    	
    }
    
    public List<IMsoObjectHolderIf> getUndeleteableObjectHolders()
    {
    	// create list
    	List<IMsoObjectHolderIf> list =
    		new ArrayList<IMsoObjectHolderIf>(m_objectHolders.size());
    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.isRelationDeletable(this)) list.add(holder);
        }

        // finished
        return list;
    }

    public boolean isSetup()
    {
        return m_isSetup;
    }

    public Map<String, IMsoAttributeIf<?>> getAttributes()
    {
        return new LinkedHashMap<String, IMsoAttributeIf<?>>(m_attributeMap);
    }
    
    public AttributeImpl<?> getAttribute(String aName) throws UnknownAttributeException
    {
        AttributeImpl<?> retVal = m_attributeMap.get(aName.toLowerCase());
        if (retVal == null)
        {
            throw new UnknownAttributeException("Unknown attribute name '" + aName + "' in " + this.getClass().toString());
        }
        return retVal;
    }
    
    public AttributeImpl<?> getAttribute(int anIndex) throws UnknownAttributeException
    {
        if (!m_listSorted)
        {
            arrangeList();
        }
        if (m_attributeList.size() > anIndex && anIndex >= 0)
        {
            return m_attributeList.get(anIndex);
        }
        throw new UnknownAttributeException("Unknown attribute index " + anIndex + " in " + this.getClass().toString());
    }

    public IMsoAttributeIf.IMsoBooleanIf getBooleanAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoBooleanIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Boolean.class);
        }
    }

    public IMsoAttributeIf.IMsoBooleanIf getBooleanAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoBooleanIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Boolean.class);
        }
    }

    public IMsoAttributeIf.IMsoIntegerIf getIntegerAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoIntegerIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Integer.class);
        }
    }

    public IMsoAttributeIf.IMsoIntegerIf getIntegerAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoIntegerIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Integer.class);
        }
    }

    public IMsoAttributeIf.IMsoDoubleIf getDoubleAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoDoubleIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Double.class);
        }
    }

    public IMsoAttributeIf.IMsoDoubleIf getDoubleAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoDoubleIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Double.class);
        }
    }

    public IMsoAttributeIf.IMsoStringIf getStringAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoStringIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, String.class);
        }
    }

    public IMsoAttributeIf.IMsoStringIf getStringAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoStringIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, String.class);
        }
    }

    public IMsoAttributeIf.IMsoCalendarIf getCalendarAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoCalendarIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Calendar.class);
        }
    }

    public IMsoAttributeIf.IMsoCalendarIf getCalendarAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoCalendarIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Calendar.class);
        }
    }

    public IMsoAttributeIf.IMsoPositionIf getPositionAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoPositionIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Position.class);
        }
    }

    public IMsoAttributeIf.IMsoPositionIf getPositionAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoPositionIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Position.class);
        }
    }

    public IMsoAttributeIf.IMsoTimePosIf getTimePosAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoTimePosIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, TimePos.class);
        }
    }

    public IMsoAttributeIf.IMsoTimePosIf getTimePosAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoTimePosIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, TimePos.class);
        }
    }

    public IMsoAttributeIf.IMsoTrackIf getTrackAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoTrackIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Track.class);
        }
    }

    public IMsoAttributeIf.IMsoTrackIf getTrackAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoTrackIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Track.class);
        }
    }

    public IMsoAttributeIf.IMsoRouteIf getRouteAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoRouteIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Route.class);
        }
    }

    public IMsoAttributeIf.IMsoRouteIf getRouteAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoRouteIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Route.class);
        }
    }

    public IMsoAttributeIf.IMsoPolygonIf getPolygonAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoPolygonIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Polygon.class);
        }
    }

    public IMsoAttributeIf.IMsoPolygonIf getPolygonAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoPolygonIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Polygon.class);
        }
    }

    public IMsoAttributeIf.IMsoEnumIf<?> getEnumAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoEnumIf<?>) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Enum.class);
        }
    }

    public IMsoAttributeIf.IMsoEnumIf<?> getEnumAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoEnumIf<?>) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Enum.class);
        }
    }

    @SuppressWarnings("unchecked")
    public void rearrangeAttribute(AttributeImpl<?> anAttr, int anIndexNo)
    {
        if (!m_listSorted)
        {
            arrangeList();
        }
        int attrIndexNo = anAttr.getIndexNo();
        anIndexNo = Math.max(0, anIndexNo);
        anIndexNo = Math.min(m_attributeList.size(), anIndexNo);
        if (attrIndexNo == anIndexNo)
        {
            return;
        }
        if (anIndexNo < attrIndexNo)
        {
            for (int i = anIndexNo; i < attrIndexNo; i++)
            {
                m_attributeList.get(i).renumber(i + 1);
            }
            anAttr.renumber(anIndexNo);
        } else
        {
            for (int i = attrIndexNo; i < anIndexNo; i++)
            {
                m_attributeList.get(i + 1).renumber(i);
            }
            anAttr.renumber(anIndexNo);
        }
        arrangeList();
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(String aName, Object aValue) throws UnknownAttributeException
    {
        AttributeImpl attr = getAttribute(aName);
        attr.set(aValue);
    }

    @SuppressWarnings("unchecked")
    public void setAttribute(int anIndex, Object aValue) throws UnknownAttributeException
    {
        AttributeImpl attr = getAttribute(anIndex);
        attr.set(aValue);
    }
    
	@Override
	public Set<IMsoObjectHolderIf> getOwningObjects() {
		Set<IMsoObjectHolderIf> set = null; 
		for(IMsoObjectHolderIf it : m_objectHolders)
		{
			set.add(it);
		}
		return set;
	}

	@Override
	public IMsoObjectHolderIf getObjectHolder(IMsoObjectIf msoObj) {
		for(MsoRelationImpl<? super IMsoObjectIf> it : m_relationObjects.values()) {
			IMsoObjectIf refObj = it.get();
			if(refObj!=null&&refObj.equals(msoObj)) return it;
		}
		for(MsoListImpl<? super IMsoObjectIf> it : m_relationLists.values()) {
			if(it.contains(msoObj)) return it;
		}
		// not contained by this object
		return null;
	}

	@Override
	public boolean contains(IMsoObjectIf msoObj) {
		return getObjectHolder(msoObj)!=null;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IMsoRelationIf<?> getRelation(IMsoObjectIf msoObj) {
		IMsoObjectHolderIf holder = getObjectHolder(msoObj);
		if(holder instanceof IMsoRelationIf) {
			return (IMsoRelationIf<IMsoObjectIf>)holder;
		}
		return ((IMsoListIf<IMsoObjectIf>)holder).getRelation(msoObj);
	}

	@Override
	public void setObjectRelation(IMsoObjectIf anObject, String aReferenceName) throws InvalidRelationException {
        IMsoRelationIf<? super IMsoObjectIf> refObj = m_relationObjects.get(aReferenceName.toLowerCase());
        if (refObj == null)
        {        	
        	throw new InvalidRelationException("The relation " + aReferenceName + " is unknown");
        	
        } else
        {
        	if(!refObj.set(anObject)) throw new InvalidRelationException("The relation can not be null (cardinality is greater than 0)");
        }
	}

    public Map<String, List<IMsoObjectIf>> getObjects() {
    	int size = m_relationObjects.size()+m_relationLists.size();
    	Map<String, List<IMsoObjectIf>> map = new LinkedHashMap<String, List<IMsoObjectIf>>(size);
    	for(String it : m_relationObjects.keySet()) {
    		List<IMsoObjectIf> list = new Vector<IMsoObjectIf>(1);
    		list.add(m_relationObjects.get(it).get());
    		map.put(it, list);
    	}
    	for(String it : m_relationLists.keySet()) {
    		List<IMsoObjectIf> list = new Vector<IMsoObjectIf>(1);
    		list.addAll(m_relationLists.get(it).getObjects());
    		map.put(it, list);
    	}
    	return map;
    }
	
	public Map<String, IMsoRelationIf<?>> getObjectRelations()
    {
        return new LinkedHashMap<String, IMsoRelationIf<?>>(m_relationObjects);
    }
    
	@Override
    public void addListRelation(IMsoObjectIf anObject, String aReferenceListName) throws InvalidRelationException {
    	IMsoListIf<? super IMsoObjectIf> list = m_relationLists.get(aReferenceListName);
    	if(list==null) throw new InvalidRelationException("List relation " + aReferenceListName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidRelationException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()>=list.size()) throw new InvalidRelationException("Relation can not be added because the number of items in the list size is equal to or greater than the cardinality");
    	if(!list.add(anObject)) throw new InvalidRelationException("An relation to object " + anObject + " already exist or the object is null or not properly initialized");
    }

    @Override
    public void removeListRelation(IMsoObjectIf anObject, String aReferenceListName) throws InvalidRelationException {
    	IMsoListIf<? super IMsoObjectIf> list = m_relationLists.get(aReferenceListName);
    	if(list==null) throw new InvalidRelationException("List relation " + aReferenceListName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidRelationException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()<=list.size()) throw new InvalidRelationException("Relation can not be removed because the number of items in the list size equal to or is less than the cardinality");
    	if(!list.remove(anObject)) throw new InvalidRelationException("A relation to object " + anObject + " does not exist, or the relation is not deletable");   	
    }    
    
    @Override
    public Map<String, IMsoListIf<?>> getListRelations()
    {
        return new LinkedHashMap<String, IMsoListIf<?>>(m_relationLists);
    }

    public Map<String, IMsoListIf<?>> getListRelations(Class<?> c, boolean isEqual)
    {
    	Map<String, IMsoListIf<?>> map = new LinkedHashMap<String, IMsoListIf<?>>();
    	if(isEqual)
    	{
	    	for(String it : m_relationLists.keySet()) {
	    		MsoListImpl<?> list = m_relationLists.get(it);
	    		if(list.getObjectClass().equals(c)) map.put(it,list);
	    	}
    	}
    	else {
	    	for(String it : m_relationLists.keySet())
	    	{
	    		MsoListImpl<?> list = m_relationLists.get(it);
	    		if(list.getObjectClass().isAssignableFrom(c)) map.put(it,list);
	    	}
    	}
        return map;
    }

    public Map<String, IMsoListIf<?>> getListRelations(MsoClassCode c) {
    	Map<String, IMsoListIf<?>> map = new LinkedHashMap<String, IMsoListIf<?>>();
    	for(String it : m_relationLists.keySet())
    	{
    		MsoListImpl<?> list = m_relationLists.get(it);
    		IMsoObjectIf msoObj = list.getHeadObject();
    		if(msoObj!=null && msoObj.getClassCode().equals(c)) map.put(it,list);
    	}
        return map;
    }

    public boolean commit() throws TransactionException {
    	IChangeRecordIf rs = m_model.getChanges(this);
    	if(rs!=null) {
    		m_model.commit(rs);
    	}
    	return false;
    }    
    
    @SuppressWarnings("unchecked")
    public boolean rollback()
    {
    	IChangeRecordIf rs = m_model.getChanges(this);
    	if(rs!=null) {
    		
    		// suspend update
    		m_model.suspendChange();
    		
        	// initialize restore flag
        	boolean bFlag = !m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);

        	// ensure that model is in local update mode
        	if(bFlag) m_model.setLocalUpdateMode();

        	// reset deleted mode
            m_isDeleted = false;
            
            // loop over all changed attributes
            for (IChangeAttributeIf it : rs.getAttributeChanges())
            {
            	it.getMsoAttribute().rollback();
            }
            
            // loop over all relations
            for (IChangeRelationIf it : rs.getRelationChanges())
            {
                it.getMsoRelation().rollback();
            }
            
            // is not created remotely?
            if(!isCreated())
            {
            	// destroy me
            	destroy();
            }

        	// restore previous update mode?
            if(bFlag) m_model.restoreUpdateMode();
            
            // finalize rollback
            m_model.resumeUpdate();
    		
            // success
            return true;
    	}
    	
    	// failure
    	return false;
    	
    }
    
    public boolean isUpdateSuspended() 
    {
    	return m_suspendChangeCount>0 || m_model.isChangeSuspended();
    }
   
    public void suspendChange()
    {
        m_suspendChangeCount++;
    }

    public boolean resumeChange(boolean all)
    {    	
    	// consume?
    	if(m_suspendChangeInProgress) return false;
    	
    	// prevent endless resume loop
    	m_suspendChangeInProgress = true;
    	
    	// initialize
    	boolean bFlag = false;
    	
    	// decrement counter?
    	if(m_suspendChangeCount>0) m_suspendChangeCount--;
    	
    	// consume?
        if (m_suspendChangeCount==0 && !m_model.isChangeSuspended())
        {

        	// notify MSO manager
	        m_eventManager.enterResume();
	
	        // notify changes occurred in this object
	        bFlag |= notifyChange();
	        
	        // notify updates in related objects?
	        if(all)
	        {		        
	        	for (MsoRelationImpl<?> it : m_relationObjects.values())
		        {
		        	IMsoObjectIf msoObj = it.get();
		        	if(msoObj!=null) {
		        		bFlag |= msoObj.resumeChange(all);
			            //m_logger.debug("resumeLocalUpdate::"+this);
		        	}
		        }
		        
		        for (MsoListImpl<IMsoObjectIf> list : m_relationLists.values())
		        {
		            bFlag |= list.resumeClientUpdate(all);
		            //m_logger.debug("resumeLocalUpdate::"+this);
		        }
	
	        }
	
	        // notify MSO manager
	        m_eventManager.leaveResume();
	        	        
        }
        
        // reset in-progress flag
        m_suspendChangeInProgress = false;
        
        // finished
        return bFlag;
        
    }

    public Object validate() {

    	for (IMsoAttributeIf<?> it : m_attributeList)
        {
            if(!it.validate()) return it;
        }

        for (MsoListImpl<IMsoObjectIf> list : m_relationLists.values())
        {
        	Object retVal = list.validate();
            if(!isTrue(retVal)) return retVal;
        }

        for (MsoRelationImpl<?> relation : m_relationObjects.values())
        {
        	Object retVal = relation.validate();
            if(!isTrue(retVal)) return retVal;
        }


        // is valid
        return true;
    }

    public IMsoModelIf getModel() {
    	return m_model;
    }

    @Override
	public IChangeRecordIf getChanges() {
    	return m_model.getChanges(this);    
	}

	@Override
    public Collection<IChangeIf.IChangeAttributeIf> getAttributeChanges()
    {
		IChangeRecordIf rs = m_model.getChanges(this);
		if(rs!=null)
		{
			return rs.getAttributeChanges();
		}
    	return new Vector<IChangeIf.IChangeAttributeIf>();
    }
    
    public Collection<IChangeIf.IChangeRelationIf> getObjectRelationChanges()
    {
		IChangeRecordIf rs = m_model.getChanges(this);
		if(rs!=null)
		{
			return rs.getObjectReferenceChanges();
		}
		return new Vector<IChangeIf.IChangeRelationIf>();
    }

    public Collection<IChangeIf.IChangeRelationIf> getListRelationChanges()
    {
		IChangeRecordIf rs = m_model.getChanges(this);
		if(rs!=null)
		{
			return rs.getListReferenceChanges();
		}
		return new Vector<IChangeIf.IChangeRelationIf>();
    }
    
    public boolean isRootObject() {
    	return (m_mainList==null);
    }
    
    @SuppressWarnings("unchecked")
	public MsoListImpl<? super IMsoObjectIf> getMainList() 
    {
    	return m_mainList;
    }
    
    public IMsoObjectIf getOwnerObject() 
    {
    	if(m_mainList!=null)
    	{
    		return m_mainList.m_owner;
    	}
    	return null;
    }

	/* =============================================================
	 * Comparable implementation
	 * ============================================================= */

	public int compareTo(IData anObject) {
		// default implementation
		if(anObject instanceof IMsoObjectIf) {
			return m_msoObjectId.getId().compareTo(((IMsoObjectIf)anObject).getObjectId());
		}
		else
		{
			return anObject != null ? this.hashCode() - anObject.hashCode() : -1;
		}
	}

	/*-------------------------------------------------------------------------------------------
	 * Protected methods
	 *-------------------------------------------------------------------------------------------*/

    /**
     * This method is called by the owning IMsoListIf object
     * when this is created.
     */
    protected void setMainList(MsoListImpl<?> aList)
    {
        if (aList == null)
        {
            throw new MsoRuntimeException("Try to assign a null main list.");
        }
        if (m_mainList != null && aList != m_mainList)
        {
            throw new MsoRuntimeException("Try to assign another main list.");
        }
        m_mainList = aList;
    }

    /**
     * Renumber duplicate numbers
     *
     * This method is also called by from some constructors. In these cases, {@link #m_mainList} is null.
     */
    @SuppressWarnings("unchecked")
	protected void setNumber(MsoInteger serial, int aNumber)
    {

    	// update
        serial.setValue(aNumber);

    	/* ==============================================================
    	 * Renumbering of duplicates is required because serial number
    	 * is locally incremented in each MSO model instance. Hence,
    	 * serial numbers are not generated globally by the server.
    	 * Hence, when  a new serial number is created, this only
    	 * applies locally. The same serial number may exist in another
    	 * MSO model, but this is not known before it is committed. When
    	 * committed the duplicate serial number is detected by searching
    	 * for other objects in the main list with the same serial number
    	 * as this object. If a different object with same serial number is
    	 * found, the first created serial number is kept (the serial number
    	 * of this object). Every object with equal or greater serial number
    	 * than the serial number of this object is incremented. Any
    	 * changes objects are returned as a list of IUpdateHolderIf, which
    	 * MUST BE COMMITTED TO THE SERVER DIRECTLY! This ensures that
    	 * conflict are resolved quickly without any user commit dependence.
    	 * If user commit is required, the resolved conflict may not become
    	 * visible in some time. During this time, the serial number may
    	 * be used. When the resolved conflict is committed at a later state,
    	 * the changed serial number may confuse the user.
    	 *
    	 * The resolved conflict should produce cues visible to the user!
    	 *
    	 * ============================================================== */

        // Only validate in REMOTE mode (server update)
    	if(m_model.isUpdateMode(UpdateMode.REMOTE_UPDATE_MODE)) {
	        if (m_mainList != null)
	        {
	        	// get renumbered objects
	            List<IMsoObjectIf> renumbered = m_mainList.renumberDuplicateNumbers(this);
	            
	            // any conflicts found?
	            if(renumbered.size()>0) {
	            	// found, get items that must be update directly
                	IMsoTransactionManagerIf committer = (IMsoTransactionManagerIf)m_model;
                	List<IChangeRecordIf> updates = new ArrayList<IChangeRecordIf>(renumbered.size());
                    for(IMsoObjectIf it : renumbered) {
    					// get update holder set
    					IChangeRecordIf holder = committer.getChanges(it);
    					// has updates?
    					if(holder!=null) {
    						// try to set partial and to updates if succeeded
    						if(holder.setFilter(((ISerialNumberedIf)it).getNumberAttribute())) {
    							updates.add(holder);
    						}
    					}
                    }
	            	// commit changes (this will not affect any other local changes)
                    if(updates.size()>0) {
                        try {
                        	m_model.commit(updates);
                		} catch (TransactionException ex) {
                			m_logger.error("Failed to commit resolved serial number conflicts",ex);
                		}                                	                    
                    }
	            }
	        }
    	}
    }

    /**
     * Add a new attribute. </p>
     *
     * Maintains the double bookkeeping under the following conditions:
     * 1) do not accept duplicated names, 2) duplicated numbers are accepted. </p>
     *
     * The extending object should call this method from <code>defineAttributes</code>
     *
     * @param anAttribute - Attribute to add
     */
    protected void addAttribute(AttributeImpl<?> anAttribute)
    {
        if (anAttribute != null)
        {
            String attrName = anAttribute.getName().toLowerCase();
            if (m_attributeMap.containsKey(attrName))
            {
                AttributeImpl<?> attr = m_attributeMap.remove(attrName);
                m_attributeList.remove(attr);
            }
            m_attributeMap.put(attrName, anAttribute);
            m_attributeList.add(anAttribute);
            m_listSorted = false;
        } else
        {
            Log.error("Error in setup: " + this + ": Try to add null Attribute");
        }
    }

    /**
     * Add a one-to-many relation (a MSO list of relations from this to other IMsoObjectIf)</p>
     *
     * The extending object should call
     * this method from <code>defineLists</code>
     *
     * @param aList - the list to add
     */
    @SuppressWarnings("unchecked")
	protected void addList(MsoListImpl<?> aList)
    {
        if (aList != null)
        {
            m_relationLists.put(aList.getName().toLowerCase(), (MsoListImpl<IMsoObjectIf>)aList);
        } else
        {
            Log.error("Error in setup: " + this + ": Try to add null list");
        }
    }

    /**
     * Add a one-to-one object relation (from this to another IMsoObjectIf)</p>
     *
     * The extending object should call this method from <code>defineReference</code>
     *
     * @param aReference - the relation to add
     */
    @SuppressWarnings("unchecked")
    protected void addObject(MsoRelationImpl<?> aReference)
    {
        if (aReference != null)
        {
            m_relationObjects.put(aReference.getName().toLowerCase(), (MsoRelationImpl<IMsoObjectIf>)aReference);
        } else
        {
            Log.error("Error in setup: " + this + ": Try to add null relation");
        }
    }

    protected int incrementChangeCount() {
    	m_changeCount++;
    	return m_changeCount;
    }

    private void registerCreatedObject()
    {
    	registerChange(new ChangeImpl.ChangeObject(
    			this, m_model.getUpdateMode(), 
        		MsoEventType.CREATED_OBJECT_EVENT, 
        		false, false)) ;    	
    	m_logger.debug("Registered CREATED_OBJECT_EVENT in " + this);
    }

    private void registerDeletedObject(boolean isLoopback, boolean isRollback)
    {
    	registerChange(new ChangeImpl.ChangeObject(
    			this, m_model.getUpdateMode(), 
        		MsoEventType.DELETED_OBJECT_EVENT, 
        		isLoopback, isRollback)) ;    	
    	m_logger.debug("Registered DELETED_OBJECT_EVENT in " + this);
    }
    
    protected void registerModifiedData(IChangeIf aChange)
    {
    	if(MsoEvent.isMask(aChange.getMask(),MsoEventType.MODIFIED_DATA_EVENT))
    	{
	        registerChange(aChange);
	    	m_logger.debug("Registered MODIFIED_DATA_EVENT in " + this);
    	}
    }

    protected void registerAddedRelation(IChangeIf aChange)
    {
    	if(MsoEvent.isMask(aChange.getMask(),MsoEventType.ADDED_RELATION_EVENT))
    	{
    		registerChange(aChange);
        	m_logger.debug("Raised ADDED_RELATION_EVENT in " + this);    		
    	}
    }

    protected void registerRemovedRelation(IChangeIf aChange)
    {
    	if(MsoEvent.isMask(aChange.getMask(),MsoEventType.REMOVED_RELATION_EVENT))
    	{
    		registerChange(aChange);
        	m_logger.debug("Raised REMOVED_RELATION_EVENT in " + this);
    	}
    }

    /**
     * Notify that master should be updated.
     *
     * @param aChange - the change
     */
    protected void notifyUpdate(IChangeIf aChange)
    {
        if (aChange.getMask() != 0)
        {
        	ChangeRecordImpl rs = new ChangeRecordImpl(this,aChange.getUpdateMode());
        	rs.record(aChange,false);
        	if(rs.isChanged())
        	{
        		m_eventManager.notifyUpdate(rs);
        	}
        }
    }

    /**
     * Notify derived listeners
     */
    protected boolean notifyCoChange()
    {
    	boolean bFlag = false;
        if (m_coChangeBuffer.size() > 0)
        {
        	ChangeRecordImpl rRs = new ChangeRecordImpl(this,UpdateMode.REMOTE_UPDATE_MODE);
        	ChangeRecordImpl lRs = new ChangeRecordImpl(this,UpdateMode.LOCAL_UPDATE_MODE);
        	for(IChangeIf it : m_coChangeBuffer)
        	{
        		rRs.record(it,false);
        		lRs.record(it,false);
        	}
        	// notify remote changes?
        	if(rRs.isChanged())
        	{
        		m_eventManager.notifyCoChange(rRs);
        		bFlag = true;
        	}
        	// notify local changes?
        	if(lRs.isChanged())
        	{
        		m_eventManager.notifyCoChange(lRs);
        		bFlag = true;
        	}
        	// clear buffered changes
            m_coChangeBuffer.clear();
        }
        return bFlag;
    }


    protected void suspendCoChange()
    {
        m_suspendCoChange = true;
    }

    protected void resumeCoChange()
    {
        m_suspendCoChange = false;
        notifyCoChange();
    }

    /*-------------------------------------------------------------------------------------------
	 * Helper methods
	 *-------------------------------------------------------------------------------------------*/

    private void registerChange(IChangeIf aChange)
    {
    	
    	// track change
    	incrementChangeCount();

        // buffer changes
    	m_slaveChangeBuffer.add(aChange);
        m_coChangeBuffer.add(aChange);
        
        // notify derived update listeners?
        if (!m_suspendCoChange)
        {
            notifyCoChange();
        }

        // notify server update listeners?
        if(m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE)
        		|| aChange.isLoopbackMode())
        {
        	notifyUpdate(aChange);
        }

        // notify slave change listeners?
        if (!isUpdateSuspended())
        {
            notifyChange();
        }  

    }
        
    /**
     * Notify change listeners
     */
    private boolean notifyChange()
    {	
    	boolean bFlag = false;
        if (m_slaveChangeBuffer.size() > 0)
        {
        	ChangeRecordImpl rRs = new ChangeRecordImpl(this,UpdateMode.REMOTE_UPDATE_MODE);
        	ChangeRecordImpl lRs = new ChangeRecordImpl(this,UpdateMode.LOCAL_UPDATE_MODE);
        	for(IChangeIf it : m_slaveChangeBuffer)
        	{
        		switch(it.getUpdateMode())
        		{
        		case REMOTE_UPDATE_MODE:
            		rRs.record(it,false);
            		break;
        		case LOCAL_UPDATE_MODE:
            		lRs.record(it,false);
            		break;
        		}
        	}
        	// notify remote changes?
        	if(rRs.isChanged())
        	{
        		m_eventManager.notifyChange(rRs);
        		bFlag = true;
        	}
        	// notify local changes?
        	if(lRs.isChanged())
        	{
        		m_eventManager.notifyChange(lRs);
        		bFlag = true;
        	}
        	// clear buffered changes
            m_slaveChangeBuffer.clear();
        }
        return bFlag;
    }    

    private boolean isTrue(Object value) {
    	if(value instanceof Boolean)
    		return (Boolean)value;
    	return false;
    }

    private UnknownAttributeException attributeCast(String aName, Class<?> aClass)
    {
        return new UnknownAttributeException("Unknown attribute name '" + aName + "' of class " + aClass.toString() + " in " + this.getClass().toString());
    }

    private UnknownAttributeException attributeCast(int anIndex, Class<?> aClass)
    {
        return new UnknownAttributeException("Unknown attribute index '" + anIndex + "' of class " + aClass.toString() + " in " + this.getClass().toString());
    }

    private void arrangeList()
    {
        Collections.sort(m_attributeList);
        int i = 0;
        for (AttributeImpl<?> attr : m_attributeList)
        {
            attr.renumber(i++);
        }
        m_listSorted = true;
    }

    /*-------------------------------------------------------------------------------------------
	 * Inner classes
	 *-------------------------------------------------------------------------------------------*/

    /**
     * Class for holding Object ID Strings
     * <p/>
     * Is merely a wrapper around a String, used in order not to mismatch with other String objects.
     */
    public static class ObjectId implements IObjectIdIf
    {
        private final String m_id;
        private Calendar m_created;

        public ObjectId(String anId, Date created)
        {
            m_id = anId;
            m_created = convert(created);
        }

        public String getId()
        {
            return m_id;
        }

		public Calendar getCreatedTime() {
			return m_created;
		}

		public void setCreatedTime(Date time) {
			m_created = convert(time);
		}

		public boolean isCreated() {
			return m_created!=null;
		}

		private Calendar convert(Date date) {
            if(date!=null) {
	            Calendar cal = Calendar.getInstance();
	            cal.setTime(date);
	            return cal;
            }
            return null;
		}
    }

    /**
     * Selector where selection is based on comparisons with a specific object
     */
    public abstract static class SelfSelector<T extends IMsoObjectIf, M extends IMsoObjectIf> implements Selector<M>
    {
        T m_object;

        public void setSelfObject(T myObject)
        {
            m_object = myObject;
        }
    }

    /**
     * Selector used for selecting assignments with a given status.
     */
    public static class EnumSelector<T extends Enum<?>, M extends IMsoObjectIf> implements Selector<M>
    {
        T m_selectValue;
        String m_attributeName;

        /**
         * Construct a Selector object
         *
         * @param aStatus        The status to test against
         * @param anAttributName Name of Enum attribute
         */
        public EnumSelector(T aStatus, String anAttributName)
        {
            m_selectValue = aStatus;
            m_attributeName = anAttributName;
        }

        public boolean select(M anObject)
        {
            try
            {
                return anObject.getEnumAttribute(m_attributeName) == m_selectValue;
            }
            catch (UnknownAttributeException e)
            {
                return false;
            }
        }
    }

    public static class StatusSelector<T extends IEnumStatusHolder<?>, E extends Enum<?>> implements Selector<T>
    {
        E m_selectValue;

        /**
         * Construct a Selector object
         *
         * @param aStatus The status to test against
         */
        public StatusSelector(E aStatus)
        {
            m_selectValue = aStatus;
        }

        public boolean select(T anObject)
        {
            return anObject.getStatus() == m_selectValue;
        }
    }

    /**
     * Selector used for selecting assignments with status in a given set.
     */
    public static class StatusSetSelector<T extends IEnumStatusHolder<?>, E extends Enum<?>> implements Selector<T>
    {
        EnumSet<? extends E> m_valueSet;

        /**
         * Construct a Selector object
         *
         * @param aValueSet The status set to test against
         */
        public StatusSetSelector(EnumSet<? extends E> aValueSet)
        {
            m_valueSet = aValueSet;
        }

        public StatusSetSelector()
        {
            //To change body of created methods use File | Settings | File Templates.
        }

        public boolean select(T anObject)
        {
            return m_valueSet.contains(anObject.getStatus());
        }
    }

	public MsoDataType getDataType() {
		return MsoDataType.OBJECT;
	}

}