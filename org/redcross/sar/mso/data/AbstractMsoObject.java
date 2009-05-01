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
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeSourceIf;
import org.redcross.sar.mso.IMsoTransactionManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
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
	 * The logger object
	 */
	
    private static final Logger m_logger = Logger.getLogger(AbstractMsoObject.class);
	
    /**
     * The Object ID, must exist for all MSO Objects .
     */
    private final IObjectIdIf m_MsoObjectId;

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
     * Mask for suspended client update events
     */
    private int m_clientUpdateMask = 0;

    /**
     * Dominant update mode, where LOCAL_UPDATE_MODE overwrites all other modes
     */
    private UpdateMode m_updateMode = null;

    /**
     * Loopback flag, should only be <code>true</code> if all registered
     * updates are a result of change in SERVER VALUE that results in
     * SERVER VALUE == LOCAL VALUE.
     */
    private boolean m_isLoopback = true;

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
     * Suspend client event flag
     */
    private boolean m_suspendClientUpdate = false;

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
    private boolean m_hasBeenDeleted;

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
        m_MsoObjectId = anObjectId;
        m_msoModel = theMsoModel;
        m_eventManager = m_msoModel.getEventManager();
        System.out.println("Created " + this);
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
        return m_MsoObjectId.getId();
    }

    public Calendar getCreatedTime()
    {
        return m_MsoObjectId.getCreatedTime();
    }

    /**
     * Sets created state
     *
     */
    public void setCreatedTime(Date time) {
    	m_MsoObjectId.setCreatedTime(time);
    	Calendar t = m_MsoObjectId.getCreatedTime();
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
        return m_MsoObjectId.isCreated();
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
        return Internationalization.translate(getMsoClassCode()) + " " + m_MsoObjectId.getId();
    }

    public void addDeleteListener(IMsoObjectHolderIf aHolder)
    {
    	// only add once
    	if(!m_objectHolders.contains(aHolder))
    		m_objectHolders.add(aHolder);

        //System.out.println("Add delete listener from: " + this + " to: " + aHolder + ", count = " + m_objectHolders.size());
    }

    public void removeDeleteListener(IMsoObjectHolderIf aHolder)
    {
    	// only remove if exists
    	if(m_objectHolders.contains(aHolder))
    		m_objectHolders.remove(aHolder);

        //System.out.println("Remove delete listener from: " + this + " to: " + aHolder + ", count = " + m_objectHolders.size());
    }

    public int listenerCount()
    {
        return m_objectHolders.size();
    }

    public boolean delete()
    {
        if (canDelete())
        {
        	suspendClientUpdate();
            for (MsoListImpl list : m_referenceLists.values())
            {
                list.deleteAll();
            }

            for (MsoReferenceImpl reference : m_referenceObjects.values())
            {
                if (reference.getReference() != null)
                {
                    System.out.println("Delete reference from " + this + " to " + reference.getReference());
                    reference.setReference(null);
                }
            }
            doDelete();
            resumeClientUpdate(true);
            return true;
        }
        return false;
    }

    public boolean canDelete()
    {

    	// In LOOPBACK and SERVER MODE, delete is always allowed. Only in
    	// LOCAL mode are delete operations validated.
    	if (!m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE))
        {
            return true;
        }

    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.canDeleteReference(this)) return false;
        }
        // all object holders allow a deletion
        return true;
    }

    public List<IMsoObjectHolderIf<IMsoObjectIf>> deletePreventedBy()
    {
    	// create list
    	List<IMsoObjectHolderIf<IMsoObjectIf>> list =
    		new ArrayList<IMsoObjectHolderIf<IMsoObjectIf>>(m_objectHolders.size());
    	// all object holders must allow their relation to this object to be deleted.
        for (IMsoObjectHolderIf holder : this.m_objectHolders)
        {
            if(!holder.canDeleteReference(this)) list.add(holder);
        }

        // finished
        return list;
    }

    public boolean hasBeenDeleted()
    {
        return m_hasBeenDeleted;
    }

    /**
     * This method is intended for internal use only!<p>
     *
     * The method only performs a delete on the object and
     * notifies any object holders (lists) of this delete. It does not
     * delete owned objects (in lists), or references from this object to
     * other objects explicitly (recursive delete is not executed).
     * The result is a memory leak because objects in lists and
     * and any object references may still point to this object. Hence,
     * the object will not be garbage collected.<p>
     * Use <code>delete()</code> to delete a object.
     */
    @SuppressWarnings("unchecked")
	public void doDelete()
    {
    	suspendClientUpdate();
        System.out.println("Deleting " + this);
        while (m_objectHolders.size() > 0)
        {
            Iterator<IMsoObjectHolderIf> iterator = m_objectHolders.iterator();
            IMsoObjectHolderIf myHolder = iterator.next();
            myHolder.doDeleteReference(this);
        }
        // get flags
        boolean isChanged = !m_hasBeenDeleted;
        boolean isLoopback = m_hasBeenDeleted && !m_msoModel.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        // set as deleted
        m_hasBeenDeleted = true;
        // notify
        registerDeletedObject(this,m_msoModel.getUpdateMode(),isChanged,isLoopback);
    	resumeClientUpdate(true);
    }

    public boolean isSetup()
    {
        return m_isSetup;
    }

    public Map<String, IAttributeIf<?>> getAttributes()
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

    public IAttributeIf.IMsoBooleanIf getBooleanAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoBooleanIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Boolean.class);
        }
    }

    public IAttributeIf.IMsoBooleanIf getBooleanAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoBooleanIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Boolean.class);
        }
    }

    public IAttributeIf.IMsoIntegerIf getIntegerAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoIntegerIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Integer.class);
        }
    }

    public IAttributeIf.IMsoIntegerIf getIntegerAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoIntegerIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Integer.class);
        }
    }

    public IAttributeIf.IMsoDoubleIf getDoubleAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoDoubleIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Double.class);
        }
    }

    public IAttributeIf.IMsoDoubleIf getDoubleAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoDoubleIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Double.class);
        }
    }

    public IAttributeIf.IMsoStringIf getStringAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoStringIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, String.class);
        }
    }

    public IAttributeIf.IMsoStringIf getStringAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoStringIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, String.class);
        }
    }

    public IAttributeIf.IMsoCalendarIf getCalendarAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoCalendarIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Calendar.class);
        }
    }

    public IAttributeIf.IMsoCalendarIf getCalendarAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoCalendarIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Calendar.class);
        }
    }

    public IAttributeIf.IMsoPositionIf getPositionAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoPositionIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Position.class);
        }
    }

    public IAttributeIf.IMsoPositionIf getPositionAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoPositionIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Position.class);
        }
    }

    public IAttributeIf.IMsoTimePosIf getTimePosAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoTimePosIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, TimePos.class);
        }
    }

    public IAttributeIf.IMsoTimePosIf getTimePosAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoTimePosIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, TimePos.class);
        }
    }

    public IAttributeIf.IMsoTrackIf getTrackAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoTrackIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Track.class);
        }
    }

    public IAttributeIf.IMsoTrackIf getTrackAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoTrackIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Track.class);
        }
    }

    public IAttributeIf.IMsoRouteIf getRouteAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoRouteIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Route.class);
        }
    }

    public IAttributeIf.IMsoRouteIf getRouteAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoRouteIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Route.class);
        }
    }

    public IAttributeIf.IMsoPolygonIf getPolygonAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoPolygonIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Polygon.class);
        }
    }

    public IAttributeIf.IMsoPolygonIf getPolygonAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoPolygonIf) getAttribute(aName);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(aName, Polygon.class);
        }
    }

    @SuppressWarnings("unchecked")
    public IAttributeIf.IMsoEnumIf getEnumAttribute(int anIndex) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoEnumIf) getAttribute(anIndex);
        }
        catch (ClassCastException e)
        {
            throw attributeCast(anIndex, Enum.class);
        }
    }

    @SuppressWarnings("unchecked")
    public IAttributeIf.IMsoEnumIf getEnumAttribute(String aName) throws UnknownAttributeException
    {
        try
        {
            return (IAttributeIf.IMsoEnumIf) getAttribute(aName);
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


	public Map<String, IMsoObjectIf> getObjectReferences()
    {
        return new LinkedHashMap(m_referenceObjects);
    }
    
    /**
     * Set or reset a relation between this and another 
     * IMsoObjectIf (one-to-one relation) 
     * @param anObject - the object set a relation to, or relation to set to <code>null</code>  
     * @param aReferenceName - the name of the relation
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, or if the relation is required (cardinality is greater than <code>0</code>) 
     */
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
    		IMsoObjectIf msoObj = list.getItem();
    		if(msoObj!=null && msoObj.getMsoClassCode().equals(c)) map.put(it,list);
    	}
        return map;
    }
    
    /**
     * Add a reference to an one-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to add a reference to
     * @param aReferenceName The reference list
     * @throws InvalidReferenceException is thrown if the list reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, 
     * or if an reference to the IMsoObjectIf object already exists, or the object is 
     * null or not properly initialized 
     */
    public void addListReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException {
    	IMsoListIf list = m_referenceLists.get(aReferenceName);
    	if(list==null) throw new InvalidReferenceException("List reference " + aReferenceName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidReferenceException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()>=list.size()) throw new InvalidReferenceException("Relation can not be added because the number of items in the list size is equal to or greater than the cardinality");
    	if(!list.add(anObject)) throw new InvalidReferenceException("An reference to object " + anObject + " already exist or the object is null or not properly initialized");
    }

    /**
     * Remove a reference from a on-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to remove a reference from
     * @param aReferenceName The reference list
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, or if 
     * an reference to IMsoObjectIf object does not exist, or is not deleteable 
     */
    public void removeListReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException {
    	IMsoListIf list = m_referenceLists.get(aReferenceName);
    	if(list==null) throw new InvalidReferenceException("List reference " + aReferenceName + " does not exist");
    	if(!list.getObjectClass().isInstance(anObject)) throw new InvalidReferenceException("The list object class and IMsoObjectIf object class does not match (" + anObject + ")");
    	if(list.getCardinality()<=list.size()) throw new InvalidReferenceException("Relation can not be removed because the number of items in the list size equal to or is less than the cardinality");
    	if(!list.remove(anObject)) throw new InvalidReferenceException("A reference to object " + anObject + " does not exist, or the reference is not deletable");   	
    }
    
    /**
     * Rollback local changes. Generates client update event.
     */
    @SuppressWarnings("unchecked")
    public void rollback()
    {
        m_hasBeenDeleted = false;
        boolean dataModified = false;
        for (AttributeImpl attr : m_attributeList)
        {
            dataModified |= attr.rollback();
        }
        if (dataModified)
        {
            registerModifiedData(this,m_msoModel.getUpdateMode(),false,false);
        }

        for (MsoListImpl list : m_referenceLists.values())
        {
            list.rollback();
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            reference.rollback();
        }
    }
    
    /**
     * Rollback changes in supplied attributes in this object. Generates a client update event.
     * @param attrs - the attributes to rollback changes in
     */    
    public void rollback(List<IAttributeIf<?>> attrs)
    {
        m_hasBeenDeleted = false;
        boolean dataModified = false;
        for (IAttributeIf attr : attrs)
        {
        	if(m_attributeList.contains(attr)) {
        		dataModified |= attr.rollback();
        	}
        }
        if (dataModified)
        {
            registerModifiedData(this,m_msoModel.getUpdateMode(),false,false);
        }

    }
    

    /**
     * Post process commit of local changes.
     * Generates client update event.
     */
    @SuppressWarnings("unchecked")
    public void postProcessCommit()
    {
        boolean dataModified = false;
        for (AttributeImpl attr : m_attributeList)
        {
            dataModified |= attr.postProcessCommit();
        }
        if (dataModified)
        {
            registerModifiedData(this,m_msoModel.getUpdateMode(),false,false);
        }

        for (MsoListImpl list : m_referenceLists.values())
        {
            list.postProcessCommit();
        }

        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            reference.postProcessCommit();
        }
    }

    /**
     * Suspend update of client listeners.
     * <p/>
     * Sets suspend mode, all updates are suspended until #resumeClientUpdate is called, or the object is deleted.
     */
    public void suspendClientUpdate()
    {
        m_suspendClientUpdate = true;
    }

    public void resumeClientUpdate(boolean all)
    {

    	// consume?
        if (m_msoModel.isUpdateSuspended())
        {
            return;
        }

        // notify MSO manager
        m_eventManager.enterResume();

        // resume update notifications
        m_suspendClientUpdate = false;

        // notify updates in this object
        notifyClientUpdate();

        // notify updates in referenced objects?
        if(all)
        {
	        for (MsoListImpl list : m_referenceLists.values())
	        {
	            list.resumeClientUpdate(true);
	        }

        }

        // notify MSO manager
        m_eventManager.leaveResume();


    }

    public Object validate() {

    	for (IAttributeIf<?> it : m_attributeList)
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

    public Collection<IChangeIf.IChangeReferenceIf> getCommittableAttributeRelations()
    {
        Vector<IChangeIf.IChangeReferenceIf> retVal = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoReferenceImpl reference : m_referenceObjects.values())
        {
            retVal.addAll(reference.getCommittableRelations());
        }
        return retVal;
    }

    public Collection<IChangeIf.IChangeReferenceIf> getCommittableListRelations()
    {
        Vector<IChangeIf.IChangeReferenceIf> retVal = new Vector<IChangeIf.IChangeReferenceIf>();
        for (MsoListImpl list : m_referenceLists.values())
        {
            if (!list.isMain())
            {
                retVal.addAll(list.getCommittableRelations());
            }
        }
        return retVal;
    }

	/* =============================================================
	 * Comparable implementation
	 * ============================================================= */

	public int compareTo(IData data) {
		// default implementation
		if(data instanceof IMsoObjectIf) {
			return m_MsoObjectId.getId().compareTo(((IMsoObjectIf)data).getObjectId());
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
                	List<IChangeSourceIf> updates = new ArrayList<IChangeSourceIf>(renumbered.size());
                    for(ISerialNumberedIf it : renumbered) {
    					// get update holder set
    					IChangeSourceIf holder = committer.getChanges(it);
    					// has updates?
    					if(holder!=null) {
    						// try to set partial and to updates if succeeded
    						if(holder.setPartial(it.getNumberAttribute().getName())) {
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

    protected void registerAddedReference(Object source, UpdateMode aMode, boolean updateServer, boolean isLoopback)
    {
        System.out.println("Raise ADDED_REFERENCE_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.ADDED_REFERENCE_EVENT, updateServer, isLoopback);
    }

    protected void registerRemovedReference(Object source,
    		UpdateMode aMode, boolean updateServer, boolean isLoopback)
    {
    	System.out.println("Raise REMOVED_REFERENCE_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.REMOVED_REFERENCE_EVENT, updateServer, isLoopback);
    }

    protected void registerModifiedReference(Object source,
    		UpdateMode aMode, boolean updateServer, boolean isLoopback)
    {
    	System.out.println("Raise MODIFIED_REFERENCE_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.MODIFIED_REFERENCE_EVENT, updateServer, isLoopback);
    }

    protected void registerCreatedObject(Object source, UpdateMode aMode)
    {
    	System.out.println("Raise CREATED_OBJECT_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.CREATED_OBJECT_EVENT, true, false);
    }

    protected void registerDeletedObject(Object source,
    		UpdateMode aMode, boolean updateServer, boolean isLoopback)
    {
        System.out.println("Raise DELETED_OBJECT_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.DELETED_OBJECT_EVENT, updateServer, isLoopback);
    }

    protected void registerModifiedData(Object source,
    		UpdateMode aMode, boolean updateServer, boolean isLoopback)
    {
        System.out.println("Raise MODIFIED_DATA_EVENT in " + this);
        registerUpdate(aMode, MsoEventType.MODIFIED_DATA_EVENT, updateServer, isLoopback);
    }

    /**
     * Notify server listeners
     *
     * @param anEventTypeMask Mask for Type of event for server
     */
    protected void notifyServerUpdate(UpdateMode aMode, int anEventTypeMask)
    {
        if (anEventTypeMask != 0)
        {
            m_eventManager.notifyServerUpdate(this, aMode, anEventTypeMask);
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

    /**
     * Notify client listeners
     */
    protected void notifyClientUpdate()
    {
        if (m_clientUpdateMask != 0)
        {
            m_eventManager.notifyClientUpdate(this, m_updateMode, m_isLoopback, m_clientUpdateMask);
            m_updateMode = null;
            m_isLoopback = true;
            m_clientUpdateMask = 0;
        }
    }
    
    public boolean isChanged() {
    	return m_clientUpdateMask!=0;
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

    private void registerUpdate(UpdateMode aMode, MsoEventType anEventType, boolean updateServer, boolean isLoopback)
    {

    	// track change
    	incrementChangeCount();

    	// get update mode
    	//UpdateMode anUpdateMode = m_msoModel.getUpdateMode();
        int clientEventTypeMask = anEventType.maskValue();
        int serverEventTypeMask = (updateServer && aMode == UpdateMode.LOCAL_UPDATE_MODE) ? clientEventTypeMask : 0;

        // notify derived update listeners
        m_derivedUpdateMask |= clientEventTypeMask;
        if (!m_suspendDerivedUpdate)
        {
            notifyDerivedUpdate();
        }

        /* =========================================
         * Accumulate updates
         * ========================================= */
        m_clientUpdateMask |= clientEventTypeMask;

        /* =========================================
         * Set dominant update mode. If any local
         * updates occur, this should override any
         * existing update mode.
         * ========================================= */
        m_updateMode = (m_updateMode==null
        		|| !UpdateMode.LOCAL_UPDATE_MODE.equals(m_updateMode) ? aMode : m_updateMode);

        /* =========================================
         * Update loopback flag. Is loopback as long
         * as all updates are loopbacks. Else, flag
         * is reset.
         * ========================================= */
        m_isLoopback = isLoopback && m_isLoopback;

        //if(!m_isLoopback)
        //	System.out.println("!isLoopback::"+this);

        // notify client update listeners?
        if (!(m_suspendClientUpdate || m_msoModel.isUpdateSuspended()))
        {
            notifyClientUpdate();
        }

        // notify server update listeners
        notifyServerUpdate(aMode,serverEventTypeMask);

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




}