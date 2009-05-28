package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.IChangeIf.IChangeReferenceIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.mso.data.AttributeImpl.MsoInteger;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.InvalidReferenceException;
import org.redcross.sar.util.except.MsoRuntimeException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.UnknownAttributeException;
import org.redcross.sar.util.mso.*;

/**
 * Base class for all data objects in the MSO data model.
 * Has double bookkeeping of all attributes, both in a HashMap (for name lookup) and an ArrayList (for indexing)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMsoObject implements IMsoObjectIf
{
	/**
	 * The logger object for all AbstractMsoObject objects
	 */	
    private static final Logger m_logger = Logger.getLogger(AbstractMsoObject.class);
	
    /**
     * The Object ID, must exist for all MSO Objects .
     */
    private final IObjectIdIf m_msoObjectId;

    /**
     * Reference to EventImpl Manager.
     */
    protected final IMsoModelIf m_msoModel;


    /**
     * Reference to EventImpl Manager.
     */
    protected final IMsoEventManagerIf m_eventManager;

    /**
     * Hook to the main list owning this object
     */
    protected MsoListImpl m_owningMainList;

    /**
     * Map of attributes for name lookup.
     */
    private final Map<String, AttributeImpl> m_attributeMap = new LinkedHashMap<String, AttributeImpl>();

    /**
     * ArrayList of attributes for index lookup.
     */
    private final ArrayList<AttributeImpl> m_attributeList = new ArrayList<AttributeImpl>();

    /**
     * Set of reference objects (one-to-one relations).
     */
    private final Map<String, MsoReferenceImpl> m_referenceObjects = new LinkedHashMap<String, MsoReferenceImpl>();

    /**
     * Set of reference lists (one-to-many relations).
     */
    private final Map<String, MsoListImpl> m_referenceLists = new LinkedHashMap<String, MsoListImpl>();

    /**
     * TODO: Explain...
     */
    private final Map<IMsoDataStateIf,List<ClientUpdate>> m_clientUpdateBuffer = new HashMap<IMsoDataStateIf, List<ClientUpdate>>();
    
    /**
     * Mask for suspended client update events. This member buffers 
     * the union of all update masks registered when 
     * client updates notifications are suspended
     */
    //private Map<IMsoDataStateIf,Integer> m_sClientUpdateMask = new HashMap<IMsoDataStateIf, Integer>();
    
    /**
     * Dominant update mode, where LOCAL_UPDATE_MODE overwrites all 
     * other modes. This member buffers the union of all update modes 
     * registered when client updates notifications are suspended
     */
    //private Map<IMsoDataStateIf,UpdateMode> m_sUpdateMode = new HashMap<IMsoDataStateIf, UpdateMode>();
    

    /**
     * Loopback flag. Should only be <code>true</code> if all buffered
     * update events registered, when client updates notifications are
     * suspended, are loopbacks.
     */
    //private Map<IMsoDataStateIf,Integer> m_sIsLoopback = new HashMap<IMsoDataStateIf, Integer>();

    /**
     * Rollback flag. Should only be <code>true</code> if all buffered 
     * update events registered, when client update notifications are
     * suspended, are rollbacks.
     */
    //private Map<IMsoDataStateIf,Integer> m_sIsRollback = new HashMap<IMsoDataStateIf, Integer>();
    
    /**
     * Mask for suspended derived update events for client
     */
    private int m_derivedUpdateMask = 0;

    /**
     * Change tracking counter
     */
    private int m_changeCount = 0;

    /**
     * Indicator that tells if {@link #m_attributeList} is sorted
     */
    private boolean m_listSorted = true;

    /**
     * Suspend client event count
     */
    private int m_suspendClientUpdateCount = 0;

    /**
     * Suspend derived update events flag
     */
    private boolean m_suspendDerivedUpdate = false;

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
    public AbstractMsoObject(IMsoModelIf theMsoModel, IObjectIdIf anObjectId)
    {
        if (anObjectId == null || anObjectId.getId() == null || anObjectId.getId().length() == 0)
        {
            throw new MsoRuntimeException("Try to create object with no well defined object id.");
        }
        m_msoObjectId = anObjectId;
        m_msoModel = theMsoModel;
        m_eventManager = m_msoModel.getEventManager();
        m_logger.info("Created " + this + " in model " + m_msoModel.getID());
        suspendClientUpdate();
        suspendDerivedUpdate();
        registerCreatedObject(this,theMsoModel.getUpdateMode());
    }

	/*-------------------------------------------------------------------------------------------
	 * Initializing methods
	 *-------------------------------------------------------------------------------------------*/

    /**
     * This method MUST be called after creation of IMsoObjectIf objects.
     *
     * If not, no MSO attributes, lists or references will be created. If this
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
        resumeDerivedUpdate();
        if(resume) resumeClientUpdate(false);
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
        for (AttributeImpl attr : m_attributeList)
        {
            if(attr instanceof MsoEnum) {
            	MsoEnum e = (MsoEnum)attr;
            	e.setInitialTime(t);
            }
        }
    }

    public boolean isCreated()
    {
        return m_msoObjectId.isCreated();
    }
    
    @Override
	public boolean isLocalState() {
    	return isState(ModificationState.STATE_LOCAL);
	}

	@Override
	public boolean isRemoteState() {
		return isState(ModificationState.STATE_REMOTE);
	}

	@Override
	public boolean isConflictState() {
		return isState(ModificationState.STATE_CONFLICT);
	}
	
	@Override
	public boolean isLoopbackMode() {
        for (AttributeImpl attr : m_attributeList)
        {
            if(!attr.isLoopbackMode()) return false;
        }
        for (MsoListImpl list : m_referenceLists.values())
        {
            if(!list.isLoopbackMode()) return false;
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            if(!reference.isLoopbackMode()) return false;
        }				
        return true;
	}
	
	@Override
	public boolean isRollbackMode() {
        for (AttributeImpl attr : m_attributeList)
        {
            if(!attr.isRollbackMode()) return false;
        }
        for (MsoListImpl list : m_referenceLists.values())
        {
            if(!list.isRollbackMode()) return false;
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            if(!reference.isRollbackMode()) return false;
        }				
        return true;		
	}
	

	@Override
	public boolean isMixedState() {
		int count = 0;
		count = isLocalState()?count+1:count;
		count = isRemoteState()?count+1:count;
		count = isConflictState()?count+1:count;  
		return count>1;
	}
	
	private boolean isState(ModificationState state) {
		
        for (AttributeImpl attr : m_attributeList)
        {
            if(!attr.isLocalState()) return false;
        }
        for (MsoListImpl list : m_referenceLists.values())
        {
            if(!list.isLocalState()) return false;
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            if(!reference.isLocalState()) return false;
        }				
        return true;
	}
	
    public boolean isChanged() {
    	return m_msoModel.getChanges(this)!=null;
    }
    
    public boolean isChangedSince(int changeCount)
    {
        return (m_changeCount>=changeCount);
    }
    
    public int getChangeCount()
    {
        return m_changeCount;
    }

    public String shortDescriptor()
    {
        return toString();
    }

    public String toString()
    {
        return Internationalization.translate(getMsoClassCode()) + " " + m_msoObjectId.getId();
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
    	if (!m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE))
        {
            return true;
        }

    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.isReferenceDeletable(this)) return false;
        }
        // all object holders allow a deletion
        return true;
    }
    
    public boolean delete(boolean deep)
    {
        if (isDeletable())
        {
        	suspendClientUpdate();        	
        	if(deep)
        	{
	            for (MsoListImpl list : m_referenceLists.values())
	            {
	                list.removeAll();
	            }
	
	            for (MsoReferenceImpl reference : m_referenceObjects.values())
	            {
	                if (reference.getReference() != null)
	                {
	                    reference.setReference(null);
	                	m_logger.info("Deleted object reference from " + this + " to " + reference.getReference());
	                }
	            }
        	}
            destroy();
            resumeClientUpdate(true);
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
     * delete owned objects in lists, or references from this object to
     * other owned objects explicitly. The result of calling this method 
     * mindlessly is a potential memory leak because owned objects may still 
     * point to this object. Hence, the object will not be garbage collected.</p>
     * 
     */
    @SuppressWarnings("unchecked")
	private void destroy()
    {
        /* get dirty flag (requires server update notification 
         * if true and update mode is LOCAL_UPDATE_MODE)*/
        boolean isDirty = !m_isDeleted;
        
        /* Get loopback flag. This test is based on the assumption 
         * that an invocation of destroy() only occurs once in 
         * LOCAL_UPDATE_MODE, and once is REMOVE_UPDATE_MODE, when
         * the object is deleted locally. It is implicitly assumed 
         * that destroy is not called any more after this. */
        boolean isLoopback = m_isDeleted && !m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        
        /* Get rollback flag. Delete results in a rollback 
         * mode if, and only, the object is not created (the
         * object does not exist remotely). */
        boolean isRollback = !(m_isDeleted || m_msoObjectId.isCreated()) && m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);

        // set as deleted
        m_isDeleted = true;
        
        // suspend update notifications
    	suspendClientUpdate();
    	
    	// notify object holders
    	m_logger.info("Notify holders that references to " + this + " should be deleted");
        while (m_objectHolders.size() > 0)
        {
            Iterator<IMsoObjectHolderIf> iterator = m_objectHolders.iterator();
            IMsoObjectHolderIf myHolder = iterator.next();
            myHolder.deleteReference(this);
        }
        
        // notify client update listeners
        registerDeletedObject(this,m_msoModel.getUpdateMode(),isDirty,isLoopback,isRollback);
        
        // resume update notifications
    	resumeClientUpdate(true);
    	
    }
    
    public List<IMsoObjectHolderIf<IMsoObjectIf>> getUndeleteableReferenceHolders()
    {
    	// create list
    	List<IMsoObjectHolderIf<IMsoObjectIf>> list =
    		new ArrayList<IMsoObjectHolderIf<IMsoObjectIf>>(m_objectHolders.size());
    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.isReferenceDeletable(this)) list.add(holder);
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
        return new LinkedHashMap(m_attributeMap);
    }
    
    public AttributeImpl getAttribute(String aName) throws UnknownAttributeException
    {
        AttributeImpl retVal = m_attributeMap.get(aName.toLowerCase());
        if (retVal == null)
        {
            throw new UnknownAttributeException("Unknown attribute name '" + aName + "' in " + this.getClass().toString());
        }
        return retVal;
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

    @SuppressWarnings("unchecked")
    public IMsoAttributeIf.IMsoEnumIf getEnumAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoEnumIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Enum.class);
        }
    }

    @SuppressWarnings("unchecked")
    public IMsoAttributeIf.IMsoEnumIf getEnumAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IMsoAttributeIf.IMsoEnumIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Enum.class);
        }
    }

    @SuppressWarnings("unchecked")
    public void rearrangeAttribute(AttributeImpl anAttr, int anIndexNo)
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
	public Set<IMsoObjectHolderIf<?>> getOwningObjects() {
		Set<IMsoObjectHolderIf<?>> set = null; 
		for(IMsoObjectHolderIf it : m_objectHolders)
		{
			set.add(it);
		}
		return set;
	}

	@Override
	public IMsoObjectHolderIf<?> getObjectHolder(IMsoObjectIf msoObj) {
		for(MsoReferenceImpl it : m_referenceObjects.values()) {
			IMsoObjectIf refObj = it.getReference();
			if(refObj!=null&&refObj.equals(msoObj)) return it;
		}
		for(MsoListImpl it : m_referenceLists.values()) {
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
	public IMsoReferenceIf<?> getReference(IMsoObjectIf msoObj) {
		IMsoObjectHolderIf<?> holder = getObjectHolder(msoObj);
		if(holder instanceof IMsoReferenceIf<?>) {
			return (IMsoReferenceIf<?>)holder;
		}
		return ((IMsoListIf)holder).getReference(msoObj);
	}

	@Override
	public void setObjectReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException {
        IMsoReferenceIf refObj = m_referenceObjects.get(aReferenceName.toLowerCase());
        if (refObj == null)
        {        	
        	throw new InvalidReferenceException("The reference " + aReferenceName + " is unknown");
        	
        } else
        {
        	if(!refObj.setReference(anObject)) throw new InvalidReferenceException("The reference can not be null (cardinality is greater than 0)");
        }
	}

    public Map<String, List<IMsoObjectIf>> getObjects() {
    	int size = m_referenceObjects.size()+m_referenceLists.size();
    	Map<String, List<IMsoObjectIf>> map = new LinkedHashMap<String, List<IMsoObjectIf>>(size);
    	for(String it : m_referenceObjects.keySet()) {
    		List<IMsoObjectIf> list = new Vector<IMsoObjectIf>(1);
    		list.add(m_referenceObjects.get(it).getReference());
    		map.put(it, list);
    	}
    	for(String it : m_referenceLists.keySet()) {
    		List<IMsoObjectIf> list = new Vector<IMsoObjectIf>(1);
    		list.addAll(m_referenceLists.get(it).getObjects());
    		map.put(it, list);
    	}
    	return map;
    }
	
	public Map<String, IMsoReferenceIf<?>> getObjectReferences()
    {
        return new LinkedHashMap(m_referenceObjects);
    }
    
	@Override
    public void addListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException {
    	IMsoListIf list = m_referenceLists.get(aReferenceListName);
    	if(list==null) throw new InvalidReferenceException("List reference " + aReferenceListName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidReferenceException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()>=list.size()) throw new InvalidReferenceException("Relation can not be added because the number of items in the list size is equal to or greater than the cardinality");
    	if(!list.add(anObject)) throw new InvalidReferenceException("An reference to object " + anObject + " already exist or the object is null or not properly initialized");
    }

    @Override
    public void removeListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException {
    	IMsoListIf list = m_referenceLists.get(aReferenceListName);
    	if(list==null) throw new InvalidReferenceException("List reference " + aReferenceListName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidReferenceException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()<=list.size()) throw new InvalidReferenceException("Relation can not be removed because the number of items in the list size equal to or is less than the cardinality");
    	if(!list.remove(anObject)) throw new InvalidReferenceException("A reference to object " + anObject + " does not exist, or the reference is not deletable");   	
    }    
    
    @Override
    public Map<String, IMsoListIf<IMsoObjectIf>> getListReferences()
    {
        return new LinkedHashMap(m_referenceLists);
    }

    public Map<String, IMsoListIf<IMsoObjectIf>> getListReferences(Class<?> c, boolean isEqual)
    {
    	Map<String, IMsoListIf<IMsoObjectIf>> map = new LinkedHashMap<String, IMsoListIf<IMsoObjectIf>>();
    	if(isEqual)
    	{
	    	for(String it : m_referenceLists.keySet()) {
	    		MsoListImpl<IMsoObjectIf> list = m_referenceLists.get(it);
	    		if(list.getObjectClass().equals(c)) map.put(it,list);
	    	}
    	}
    	else {
	    	for(String it : m_referenceLists.keySet())
	    	{
	    		MsoListImpl<IMsoObjectIf> list = m_referenceLists.get(it);
	    		if(list.getObjectClass().isAssignableFrom(c)) map.put(it,list);
	    	}
    	}
        return map;
    }

    public Map<String, IMsoListIf<IMsoObjectIf>> getListReferences(MsoClassCode c) {
    	Map<String, IMsoListIf<IMsoObjectIf>> map = new LinkedHashMap<String, IMsoListIf<IMsoObjectIf>>();
    	for(String it : m_referenceLists.keySet())
    	{
    		MsoListImpl<IMsoObjectIf> list = m_referenceLists.get(it);
    		IMsoObjectIf msoObj = list.getHeadObject();
    		if(msoObj!=null && msoObj.getMsoClassCode().equals(c)) map.put(it,list);
    	}
        return map;
    }

    public boolean commit() throws TransactionException {
    	IChangeRecordIf changes = m_msoModel.getChanges(this);
    	if(changes!=null) {
    		m_msoModel.commit(changes);
    	}
    	return false;
    }    
    
    public void commit(List<IChangeIf> objects) throws TransactionException
    {
    	IChangeRecordIf changes = m_msoModel.getChanges(this);
    	if(changes!=null) {
            
    		// initialize residue lists
            List<String> objectIds = new ArrayList<String>(objects.size());
            List<IChangeReferenceIf> listRefs = new ArrayList<IChangeReferenceIf>(objects.size());
            
	        //boolean dataModified = false;
	        for (IChangeIf it : objects)
	        {
	        	// translate into objects
	        	if(it instanceof IChangeAttributeIf) 
	        	{
	        		IMsoAttributeIf<?> attr = ((IChangeAttributeIf)it).getMsoAttribute();
		        	if(m_attributeList.contains(attr)) 
		        	{
		        		attr.rollback();
		        	}
	        	} else if(it instanceof IChangeReferenceIf)
	        	{
	        		// get reference change object
	        		IChangeReferenceIf refObj = (IChangeReferenceIf)it;
	        		// get referred object 
	        		IMsoObjectIf msoObj = refObj.getReferredObject();
	        		// get referred object id
	        		String id = msoObj.getObjectId();
	        		// check for existence
	        		if(m_referenceObjects.containsKey(id)) {
	        			objectIds.add(id);
	        		} else {
	        			listRefs.add(refObj);
	        		}
	        	}
	        }
        
	        // loop over found (one-to-one) object references
	        for (String id : objectIds)
	        {
	        	MsoReferenceImpl reference  = m_referenceObjects.get(id);
	            reference.rollback();
	        }
	        
	        // loop over all (one-to-many) list references and forward list residue
	        for (MsoListImpl list : m_referenceLists.values())
	        {
	            list.rollback(listRefs);
	        }
	        
    		m_msoModel.commit(changes);
    		
    	}
    	
	        
        
    }    
    
    @SuppressWarnings("unchecked")
    public void rollback()
    {
    	// reset deleted mode
        m_isDeleted = false;
        
        // loop over all attributes
        for (AttributeImpl attr : m_attributeList)
        {
            attr.rollback();
        }
        
        // loop over all (one-to-one) object references
        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            reference.rollback();
        }
        
        // loop over all (one-to-many) list references
        for (MsoListImpl list : m_referenceLists.values())
        {
            list.rollback();
        }
        
        // is not created remotely?
        if(!isCreated())
        {
        	// destroy me
        	destroy();
        }

    }
    
    public void rollback(List<IChangeIf> objects)
    {
    	// reset flags
        m_isDeleted = false;
        
        // initialize residue lists
        List<String> objectIds = new ArrayList<String>(objects.size());
        List<IChangeReferenceIf> listRefs = new ArrayList<IChangeReferenceIf>(objects.size());
        
        //boolean dataModified = false;
        for (IChangeIf it : objects)
        {
        	// translate into objects
        	if(it instanceof IChangeAttributeIf) 
        	{
        		IMsoAttributeIf<?> attr = ((IChangeAttributeIf)it).getMsoAttribute();
	        	if(m_attributeList.contains(attr)) 
	        	{
	        		attr.rollback();
	        	}
        	} else if(it instanceof IChangeReferenceIf)
        	{
        		// get reference change object
        		IChangeReferenceIf refObj = (IChangeReferenceIf)it;
        		// get referred object 
        		IMsoObjectIf msoObj = refObj.getReferredObject();
        		// get referred object id
        		String id = msoObj.getObjectId();
        		// check for existence
        		if(m_referenceObjects.containsKey(id)) {
        			objectIds.add(id);
        		} else {
        			listRefs.add(refObj);
        		}
        	}
        }
        
        // loop over found (one-to-one) object references
        for (String id : objectIds)
        {
        	MsoReferenceImpl reference  = m_referenceObjects.get(id);
            reference.rollback();
        }
        
        // loop over all (one-to-many) list references and forward list residue
        for (MsoListImpl list : m_referenceLists.values())
        {
            list.rollback(listRefs);
        }
        
    }
    
    public boolean isClientUpdateSuspended() 
    {
    	return m_suspendClientUpdateCount>0 || m_msoModel.isClientUpdateSuspended();
    }
   
    public void suspendClientUpdate()
    {
        m_suspendClientUpdateCount++;
    }

    public boolean resumeClientUpdate(boolean all)
    {
    	// initialize
    	boolean bFlag = false;
    	
    	// decrement counter?
    	if(m_suspendClientUpdateCount>0) m_suspendClientUpdateCount--;
    	
    	// consume?
        if (m_suspendClientUpdateCount==0 && !m_msoModel.isClientUpdateSuspended())
        {

	        // notify MSO manager
	        m_eventManager.enterResume();
	
	        // notify clients of updates in this object
	        bFlag |= notifyClientUpdate();
	        
	        // notify updates in referenced objects?
	        if(all)
	        {
		        
	        	for (MsoReferenceImpl<?> it : m_referenceObjects.values())
		        {
		        	IMsoObjectIf msoObj = it.getReference();
		        	if(msoObj!=null) {
		        		bFlag |= msoObj.resumeClientUpdate(all);
		        	}
		        }
		        
		        for (MsoListImpl list : m_referenceLists.values())
		        {
		            bFlag = list.resumeClientUpdate(true);
		        }
	
	        }
	
	        // notify MSO manager
	        m_eventManager.leaveResume();
	        	        
        }
        
        // finished
        return bFlag;
        
    }

    public Object validate() {

    	for (IMsoAttributeIf<?> it : m_attributeList)
        {
            if(!it.validate()) return it;
        }

        for (MsoListImpl list : m_referenceLists.values())
        {
        	Object retVal = list.validate();
            if(!isTrue(retVal)) return retVal;
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
        	Object retVal = reference.validate();
            if(!isTrue(retVal)) return retVal;
        }


        // is valid
        return true;
    }

    public IMsoModelIf getModel () {
    	return m_msoModel;
    }

    @Override
	public IChangeObjectIf getChange() {
    	
    	// get changes
    	IChangeRecordIf aRecord = m_msoModel.getChanges(this);
    	
    	// get change object if exists
    	return aRecord!=null?aRecord.getChangedObject():null;
    
	}

	@Override
    public Collection<IChangeIf.IChangeAttributeIf> getChangedAttributes()
    {
        Vector<IChangeIf.IChangeAttributeIf> changes = new Vector<IChangeIf.IChangeAttributeIf>();
        for (AttributeImpl<?> it : m_attributeList)
        {
        	if(it.isChanged()) 
        	{
        		changes.add(it.getChange());
        	}
        }
    	return changes;
    }
    
    public Collection<IChangeIf.IChangeAttributeIf> getChangedAttributes(Collection<IChangeIf> partial)
    {
        Vector<IChangeIf.IChangeAttributeIf> changes = new Vector<IChangeIf.IChangeAttributeIf>();
        for(IChangeIf it : partial) 
        {
        	if(it instanceof IChangeAttributeIf) 
        	{
        		IMsoAttributeIf<?> attr = ((IChangeAttributeIf)it).getMsoAttribute();
        		attr = m_attributeMap.get(attr.getName().toLowerCase());
        		if(attr.isChanged()) 
        		{
	        		changes.add(attr.getChange());
		        }
        	}
        }
    	return changes;
    }

    public Collection<IChangeIf.IChangeReferenceIf> getChangedObjectReferences()
    {
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            changes.addAll(reference.getChangedReferences());
        }
        return changes;
    }

    public Collection<IChangeIf.IChangeReferenceIf> getChangedObjectReferences(Collection<IChangeIf> partial)
    {
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            changes.addAll(reference.getChangedReferences());
        }
        return changes;
    }
    
    public Collection<IChangeIf.IChangeReferenceIf> getChangedListReferences()
    {
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoListImpl list : m_referenceLists.values())
        {
            //if (!list.isMain())
            //{
            changes.addAll(list.getChangedReferences());
            //}
        }
        return changes;
    }
    
    public Collection<IChangeIf.IChangeReferenceIf> getChangedListReferences(Collection<IChangeIf> partial)
    {
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoListImpl list : m_referenceLists.values())
        {
            //if (!list.isMain())
            //{
            changes.addAll(list.getChangedReferences(partial));
            //}
        }
        return changes;
    }
    

	/* =============================================================
	 * Comparable implementation
	 * ============================================================= */

	public int compareTo(IData data) {
		// default implementation
		if(data instanceof IMsoObjectIf) {
			return m_msoObjectId.getId().compareTo(((IMsoObjectIf)data).getObjectId());
		}
		else
		{
			return this.hashCode() - data.hashCode();
		}
	}

	/*-------------------------------------------------------------------------------------------
	 * Protected methods
	 *-------------------------------------------------------------------------------------------*/

    /**
     * This method is called by the owning IMsoListIf object
     * when this is created.
     */
    protected void setOwningMainList(MsoListImpl aList)
    {
        if (aList == null)
        {
            throw new MsoRuntimeException("Try to assign a null main list.");
        }
        if (m_owningMainList != null && aList != m_owningMainList)
        {
            throw new MsoRuntimeException("Try to assign another main list.");
        }
        m_owningMainList = aList;
    }

    /**
     * Renumber duplicate numbers
     *
     * This method is also called by from some constructors. In these cases, {@link #m_owningMainList} is null.
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
    	if(m_msoModel.isUpdateMode(UpdateMode.REMOTE_UPDATE_MODE)) {
	        if (m_owningMainList != null)
	        {
	        	// get renumbered objects
	            List<ISerialNumberedIf> renumbered = m_owningMainList.renumberDuplicateNumbers(this);
	            // any conflicts found?
	            if(renumbered.size()>0) {
	            	// found, get items that must be update directly
                	IMsoTransactionManagerIf committer = (IMsoTransactionManagerIf)m_msoModel;
                	List<IChangeRecordIf> updates = new ArrayList<IChangeRecordIf>(renumbered.size());
                    for(ISerialNumberedIf it : renumbered) {
    					// get update holder set
    					IChangeRecordIf holder = committer.getChanges(it);
    					// has updates?
    					if(holder!=null) {
    						// try to set partial and to updates if succeeded
    						if(holder.setFilter(it.getNumberAttribute().getName())) {
    							updates.add(holder);
    						}
    					}
                    }
	            	// commit changes (this will not affect any other local changes)
                    if(updates.size()>0) {
                        try {
                        	m_msoModel.commit(updates);
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
    protected void addAttribute(AttributeImpl anAttribute)
    {
        if (anAttribute != null)
        {
            String attrName = anAttribute.getName().toLowerCase();
            if (m_attributeMap.containsKey(attrName))
            {
                AttributeImpl attr = m_attributeMap.remove(attrName);
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
     * Add a one-to-many reference (a MSO list of references from this to other IMsoObjectIf)</p>
     *
     * The extending object should call
     * this method from <code>defineLists</code>
     *
     * @param aList - the list to add
     */
    protected void addList(MsoListImpl aList)
    {
        if (aList != null)
        {
            m_referenceLists.put(aList.getName().toLowerCase(), aList);
        } else
        {
            Log.error("Error in setup: " + this + ": Try to add null list");
        }
    }

    /**
     * Add a one-to-one object reference (from this to another IMsoObjectIf)</p>
     *
     * The extending object should call this method from <code>defineReference</code>
     *
     * @param aReference - the reference to add
     */
    protected void addObject(MsoReferenceImpl aReference)
    {
        if (aReference != null)
        {
            m_referenceObjects.put(aReference.getName().toLowerCase(), aReference);
        } else
        {
            Log.error("Error in setup: " + this + ": Try to add null reference");
        }
    }

    protected void incrementChangeCount() {
    	m_changeCount++;
    }

    protected void registerAddedReference(IMsoDataStateIf source, 
    		UpdateMode aMode, boolean notifyServer, boolean isLoopback, boolean isRollback)
    {
        registerUpdate(source, aMode, 
        		MsoEventType.ADDED_REFERENCE_EVENT, notifyServer, isLoopback ,isRollback);
    	m_logger.info("Raised ADDED_REFERENCE_EVENT in " + this);
    }

    protected void registerRemovedReference(IMsoDataStateIf source,
    		UpdateMode aMode, boolean notifyServer, boolean isLoopback, boolean isRollback)
    {
        registerUpdate(source, aMode, 
        		MsoEventType.REMOVED_REFERENCE_EVENT, notifyServer, isLoopback, isRollback);
    	m_logger.info("Raised REMOVED_REFERENCE_EVENT in " + this);
    }

    protected void registerModifiedReference(IMsoDataStateIf source,
    		UpdateMode aMode, boolean notifyServer, boolean isLoopback, boolean isRollback)
    {
        registerUpdate(source, aMode, 
        		MsoEventType.MODIFIED_REFERENCE_EVENT, notifyServer, isLoopback, isRollback);
    	m_logger.info("Raised MODIFIED_REFERENCE_EVENT in " + this);
    }

    protected void registerCreatedObject(IMsoDataStateIf source, UpdateMode aMode)
    {
        registerUpdate(source, aMode, MsoEventType.CREATED_OBJECT_EVENT, true, false, false);
    	m_logger.info("Raised CREATED_OBJECT_EVENT in " + this);
    }

    protected void registerDeletedObject(IMsoDataStateIf source,
    		UpdateMode aMode, boolean notifyServer, boolean isLoopback, boolean isRollback)
    {
        registerUpdate(source, aMode, 
        		MsoEventType.DELETED_OBJECT_EVENT, notifyServer, isLoopback, isRollback);
    	m_logger.info("Raised DELETED_OBJECT_EVENT in " + this);
    }

    protected void registerModifiedData(IMsoDataStateIf source,
    		UpdateMode aMode, boolean notifyServer, boolean isLoopback, boolean isRollback)
    {
        registerUpdate(source, aMode, 
        		MsoEventType.MODIFIED_DATA_EVENT, notifyServer, isLoopback, isRollback);
    	m_logger.info("Raised MODIFIED_DATA_EVENT in " + this);
    }

    /**
     * Notify server listeners
     *
     * @param anEventTypeMask Mask for Type of event for server
     */
    protected void notifyServerUpdate(UpdateMode aMode, int anEventTypeMask,boolean isLoopbackMode, boolean isRollbackMode)
    {
        if (anEventTypeMask != 0)
        {
            m_eventManager.notifyServerUpdate(this, aMode, anEventTypeMask, isLoopbackMode, isRollbackMode);
        }
    }

    /**
     * Notify derived listeners
     */
    protected void notifyDerivedUpdate()
    {
        if (m_derivedUpdateMask != 0)
        {
            m_eventManager.notifyDerivedUpdate(this, m_derivedUpdateMask);
            m_derivedUpdateMask = 0;
        }
    }


    protected void suspendDerivedUpdate()
    {
        m_suspendDerivedUpdate = true;
    }

    protected void resumeDerivedUpdate()
    {
        m_suspendDerivedUpdate = false;
        notifyDerivedUpdate();
    }

    /*-------------------------------------------------------------------------------------------
	 * Helper methods
	 *-------------------------------------------------------------------------------------------*/

    private void registerUpdate(IMsoDataStateIf source, UpdateMode aUpdateMode, 
    		MsoEventType anEventType, boolean notifyServer, boolean isLoopbackMode, boolean isRollbackMode)
    {
    	
    	// track change
    	incrementChangeCount();

    	// get client update mask
        int clientEventTypeMask = anEventType.maskValue();
        
        // get server update mask
        int serverEventTypeMask = ((notifyServer || isRollbackMode) && aUpdateMode == UpdateMode.LOCAL_UPDATE_MODE) ? clientEventTypeMask : 0;
        
        // notify derived update listeners
        m_derivedUpdateMask |= clientEventTypeMask;
        if (!m_suspendDerivedUpdate)
        {
            notifyDerivedUpdate();
        }

        /* =========================================
         * Create client updates object
         * ========================================= */
        // get current list of changes
        List<ClientUpdate> list = m_clientUpdateBuffer.get(source);
        if(list==null) 
        {
        	list = new ArrayList<ClientUpdate>();
        	m_clientUpdateBuffer.put(source, list);
        }
        // is legal rollback mode?
        if(isRollbackMode && UpdateMode.LOCAL_UPDATE_MODE.equals(aUpdateMode))
        {
        	/* clear current buffer (a valid rollback mode means that
        	 * local changes made so far is discarded) */
        	list.clear();        	
        }
        // buffer update
        list.add(new ClientUpdate(source,aUpdateMode,clientEventTypeMask,isLoopbackMode,isRollbackMode));        	
        
        // notify server update listeners
        notifyServerUpdate(aUpdateMode,serverEventTypeMask,isLoopbackMode,isRollbackMode);

        // notify client update listeners?
        if (!isClientUpdateSuspended())
        {
            notifyClientUpdate();
        }  

    }
    
    /**
     * Notify client listeners
     */
    protected boolean notifyClientUpdate()
    {
    	// has buffered changes?
    	if(m_clientUpdateBuffer.size()>0)
    	{
	    	// initialize
	    	ClientUpdate localUpdate = new ClientUpdate(this,null,0,false,true);
	    	ClientUpdate remoteUpdate = new ClientUpdate(this,null,0,true,false);
	    	
	    	// calculate remote and local client updates 
	    	for(List<ClientUpdate> list : m_clientUpdateBuffer.values()) 
	    	{
	    		for(ClientUpdate update : list)
	    		{
	    			if(UpdateMode.REMOTE_UPDATE_MODE.equals(update.m_aUpdateMode)) 
	    			{
	    				remoteUpdate.m_aMask |= update.m_aMask;
	    				remoteUpdate.m_isLoopback &= update.m_isLoopback;
	    			}
	    			else {
	    				localUpdate.m_aMask |= update.m_aMask;
	    				localUpdate.m_isRollback &= update.m_isRollback;
	    			}    			
	    		}
	    	}
	    	
	    	// notify remote update?
	    	if(remoteUpdate.m_aMask!=0) 
	    	{
		        m_eventManager.notifyClientUpdate(this, 
		        		UpdateMode.REMOTE_UPDATE_MODE, remoteUpdate.m_aMask, 
		        		remoteUpdate.m_isLoopback, remoteUpdate.m_isRollback);
	    	}
	    	// notify remote update?
	    	if(localUpdate.m_aMask!=0) 
	    	{
		        m_eventManager.notifyClientUpdate(this, 
		        		UpdateMode.LOCAL_UPDATE_MODE, localUpdate.m_aMask, 
		        		localUpdate.m_isLoopback, localUpdate.m_isRollback);
	    	}
	    	
	    	// clear buffered client updates
	        m_clientUpdateBuffer.clear();
	        
	        // clients were notified
	        return true;
	        
    	}
    	
    	// failure
    	return false;
    }    

    private boolean isTrue(Object value) {
    	if(value instanceof Boolean)
    		return (Boolean)value;
    	return false;
    }

    private UnknownAttributeException attributeCast(String aName, Class aClass)
    {
        return new UnknownAttributeException("Unknown attribute name '" + aName + "' of class " + aClass.toString() + " in " + this.getClass().toString());
    }

    private UnknownAttributeException attributeCast(int anIndex, Class aClass)
    {
        return new UnknownAttributeException("Unknown attribute index '" + anIndex + "' of class " + aClass.toString() + " in " + this.getClass().toString());
    }

    private AttributeImpl getAttribute(int anIndex) throws UnknownAttributeException
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

    private void arrangeList()
    {
        Collections.sort(m_attributeList);
        int i = 0;
        for (AttributeImpl attr : m_attributeList)
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
    public static class EnumSelector<T extends Enum, M extends IMsoObjectIf> implements Selector<M>
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

    public static class StatusSelector<T extends IEnumStatusHolder, E extends Enum> implements Selector<T>
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
    public static class StatusSetSelector<T extends IEnumStatusHolder, E extends Enum> implements Selector<T>
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

	public MsoClassCode getClassCode() {
		return getMsoClassCode();
	}
	
	private class ClientUpdate {
		IMsoDataStateIf m_source;
		int m_aMask;
		boolean m_isLoopback;
		boolean m_isRollback;
		UpdateMode m_aUpdateMode;
		
		public ClientUpdate(IMsoDataStateIf source, UpdateMode aUpdateMode, int aMask, boolean isLoopback, boolean isRollback) {
			m_source = source;
			m_aMask = aMask;
			m_isLoopback = isLoopback;
			m_isRollback = isRollback;
			m_aUpdateMode = aUpdateMode;
		}
	}

}