package org.redcross.sar.mso.data;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData;
import org.redcross.sar.mso.ChangeImpl;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeRelationIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.util.except.TransactionException;

import java.util.Collection;
import java.util.Vector;

/**
 * <b>This class manages one-to-one relations. </b></p>
 * 
 * The purpose of this class is to manage one-to-one relations between 
 * IMsoObjectIf instances. The class is an intermediate object between
 * each object in the relation. </p>
 * 
 * The <code>set(IMsoObjectIf anObject)</code> 
 * method maintains the relation from the owner object (also known as 
 * "from object", must be an AbstractMsoObject instance) to the 
 * relationd object (also known as the "to object"). Each time a 
 * relation is set or reset, the owner is notified.</p> 
 * 
 * Related object may or may not be deleted, depending on the 
 * type of relation. Class members <code>isDeletable()</code> and 
 * <code>getCardinality()</code> is used to govern this. As long as 
 * cardinality is greater than zero, related objects can not 
 * deleted, nor set to null using <code>set(null)</code>. </p> 
 * 
 * This class implements IMsoObjectHolderIf. The IMsoObjectHolderIf 
 * interface enables the related object to check if it can delete itself 
 * (see <code>isRelationDeletable(IMsoObjectIf anObject)</code> 
 * and enables the related object to notify the referencing object
 * when it is deleted.  When a related object is deleted, the 
 * relation should be deleted also (see 
 * <code>deleteRelation(IMsoObjectIf anObject)</code>).</p> 
 *  
 * @author vinjar, kenneth
 */
public class MsoRelationImpl<T extends IMsoObjectIf> implements IMsoRelationIf<T>, IMsoObjectHolderIf
{
	/**
	 * The logger object for all AbstractMsoObject objects
	 */	
    private static final Logger m_logger = Logger.getLogger(MsoRelationImpl.class);

    /**
     * The relation owner (referring object)
     */
    private final AbstractMsoObject m_owner;

    /**
     * The local object relation (referred object)
     */
    protected T m_localValue = null;

    /**
     * The remote object relation (referred object)
     */
    protected T m_remoteValue = null;
    
    /**
     * The number of changes made since creation
     */
    
    protected int m_changeCount;

    /**
     * Current modification state
     */
    protected DataOrigin m_origin = DataOrigin.NONE;

    /**
     * Relation name
     */
    private final String m_name;
    
    /**
     * Relation cardinality
     */
    private final int m_cardinality;
    
    /**
     * The model which objects belongs
     */
    protected final IMsoModelIf m_model;

    /**
     * The list instance. Only set if this relation is part
     * of an on-to-many relation
     */
    private MsoListImpl<T> m_inList = null;
    
    /**
     * The deleteable state flag.
     */
    private boolean m_isDeleteable = true;
    
    /**
     * The LOOPBACK mode flag
     */
    private boolean m_isLoopbackMode = false;

    /**
     * The ROLLBACK mode flag
     */
    private boolean m_isRollbackMode = false;
        
    /* ================================================================
     * Constructors
     * ================================================================ */
    
    protected MsoRelationImpl(AbstractMsoObject theOwner, String theName, int theCardinality, boolean isDeletable, MsoListImpl<T> inList)
    {
        m_owner = theOwner;
    	if(m_owner==null)
    	{
    		throw new IllegalArgumentException("MsoRelationImpl must have a owner");
    	}
        m_name = theName;
        m_model = m_owner.getModel();
        m_cardinality = theCardinality;
        m_isDeleteable = isDeletable;
        m_inList = inList;
    }

    /* ================================================================
     * Public methods
     * ================================================================ */
    
    public String getName()
    {
        return m_name;
    }
    
    public IMsoObjectIf getOwnerObject() {
    	return m_owner;
    }    

	/**
	 * Get relation object id.
	 * 
	 * @return Returns relation object id.
	 */
	public String getObjectId() {
		return getOwnerObject().getObjectId() + "#" + getName();
	}
	
    public T getLocal()
    {
        return m_localValue;
    }
    
    public T getRemote()
    {
        return m_remoteValue;
    }
 
    public void setDeletable(boolean isDeletable)
    {
    	m_isDeleteable = isDeletable;
    }

    public boolean isDeletable()
    {
        return m_isDeleteable;
    }

    public boolean isInList()
    {
        return (m_inList!=null);
    }
    
    public boolean isChanged() {
    	return m_origin == DataOrigin.LOCAL 
    		|| m_origin == DataOrigin.CONFLICT;
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
	public boolean isDeleted() {
		return m_owner.isDeleted();
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
		if(isChanged())
		{
			if(m_origin.equals(DataOrigin.CONFLICT))
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
    
    public DataOrigin getOrigin()
    {
        return m_origin;
    }

    public boolean isOrigin(DataOrigin origin) {
    	if(m_origin!=null)
    	{
    		return m_origin.equals(origin);
    	}
    	return false;
    }

    @Override
	public boolean isOriginLocal() {
    	
		return m_origin==DataOrigin.LOCAL;
	}

	@Override
	public boolean isOriginRemote() {
		return m_origin==DataOrigin.REMOTE;
	}

	@Override
	public boolean isOriginConflict() {
		return m_origin==DataOrigin.CONFLICT;
	}
	
	@Override
	public boolean isOriginMixed() {
		return false;
	}
	
	@Override
	public boolean isLoopbackMode() {
		return m_isLoopbackMode;
	}

	@Override
	public boolean isRollbackMode() {
		return m_isRollbackMode;
	}
	
    public T get()
    {
        return m_origin == DataOrigin.LOCAL ? m_localValue : m_remoteValue;
    }

    public boolean set(T aRelation) 
    {
		// valid object?
		if (isSetup(aRelation))
		{
	    	/* ========================================================
	    	 * Setting a new relation value is dependent on the
	    	 * update mode of the MSO model. 
	    	 * 
	    	 * If the model is in REMOTE_UPDATE_MODE, a change from 
	    	 * the server is registered and the relation value 
	    	 * should be analyzed to detect any conflicts between 
	    	 * local value (private) and the server value (shared). 
	    	 * 
	    	 * If the relation been changed locally (isChanged() is true), 
	    	 * and the model is in REMOTE_UPDATE_MODE, this update may 
	    	 * be a loopback. Since loopback updates are just a ACK
	    	 * from the server, the attribute value is not changed. Hence,
	    	 * IMsoClientUpdateListener listeners is not required to fetch
	    	 * the attribute value. However, loopback updates may be used to
	    	 * indicate to the user that the commit was successful.
	    	 *  
	    	 * If the model is in LOCAL_UPDATE_MODE, the relation 
	    	 * value origin should be updated according to the 
	    	 * passed relation value.
	    	 *
	    	 * Server value and local value should only be present
	    	 * concurrently during a local update sequence (between two
	    	 * commit or rollback transactions), or if a conflict has
	    	 * occurred (remote update during a local update sequence).
	    	 * Hence, if the relation value is in a LOCAL or
	    	 * CONFLICTING, both values will be present. Else,
	    	 * only server value will be present (REMOTE origin).
	    	 *
	    	 * A commit() or rollback() will reset the local value
	    	 * to null to ensure origin consistency.
	    	 *
	    	 * ======================================================== */
	
	        UpdateMode updateMode = m_model.getUpdateMode();
	        DataOrigin newOrigin = m_origin;
	        boolean isDirty = false;
	        boolean isLoopback = false;
	        boolean isRollback = false;
	
	        // get current relation
	        T oldObj = get();
	
	        // replace current relation
	        switch (updateMode)
	        {
	            case REMOTE_UPDATE_MODE:
	            {
	
	            	/* ===========================================================
	            	 * Update to server value origin with conflict detection.
	            	 *
	            	 * If the model is in REMOTE_UPDATE_MODE, this indicates that
	            	 * an external change is detected (message queue update), and the
	            	 * model should be update accordingly.
	            	 *
	            	 * If the relation is in local origin, the local relation may
	            	 * be different from the server relation. Local changes are made
	            	 * by the user (GUI) or a local service (the application). When a
	            	 * remote update occurs (a change received from the message queue),
	            	 * the new relation origin depends on the new server relation
	            	 * and current (local) relation. If these are different, a conflict has
	            	 * occurred. This is indicated by setting the relation origin
	            	 * to CONFLICTING. Methods for resolving conflicts are supplied
					 * by this class. If the new server relation and current (local)
					 * relation are equal, the relation origin is changed to REMOTE.
	            	 *
	            	 * IMPORTANT: The local relation is never deleted from the object
	            	 * holder because
	            	 *
	            	 * A) 	When local and remote relation is different, this is
	            	 * 		by definition a conflict an both must be kept.
	            	 *
	            	 * B)	When local and remote relation are equal,
	            	 * 		the relation to the object holder is already
	            	 * 		established.
	            	 *
	            	 * =========================================================== */
	
	            	// check if a conflict has occurred?
	            	boolean isConflict = (m_origin == DataOrigin.LOCAL
	            			|| m_origin == DataOrigin.CONFLICT) ?
	            			!equal(m_localValue, aRelation) : false;
	
	            	// get next origin
	                newOrigin = isConflict ?
	                		  DataOrigin.CONFLICT
	                		: DataOrigin.REMOTE;
	
	                // no conflict found?
	                if(!isConflict)
	                {
	                	/* If the relation is locally changed, and the 
	                	 * remote relation value equals the local relation 
	                	 * value, this is a loopback */
	                    isLoopback = (isChanged() && equal(m_localValue, aRelation));
	                    // reset local value
	                    m_localValue = null;
	                }
	
	                // any change?
	                if (!equal(m_remoteValue, aRelation))
	                {
	                	// register
	                    registerRelationRemoved(m_remoteValue,true,isLoopback,false);
	                    // prepare next
	                    m_remoteValue = aRelation;
	                    // register
	                    registerRelationAdded(aRelation,true,isLoopback,false);
	                    // set flag
	                    isDirty = true;
	                }
	
	                // notify change on every conflict
	                isDirty |= isConflict;
	
	                break;
	            }
	            default: // LOCAL_UPDATE_MODE
	            {
	
	            	/* ===========================================================
	            	 * Update relation to the appropriate origin
	            	 *
	            	 * The default update mode is LOCAL_UPDATE_MODE. This mode
	            	 * indicates that the change in relation value originates
	            	 * from a GUI (user) or Service (application) invocation, and
	            	 * not from an external change made on a different model
	            	 * instance (message queue). If the new local (current)
	            	 * relation equals the server relation, the relation origin
	            	 * should be set to REMOTE. If the new relation is different
	            	 * from the server relation, the relation origin should be
	            	 * set to LOCAL.
	            	 *
	            	 * IMPORTANT 1: Current local relation is always deleted
	            	 * from its object holder, unless it is NULL, because
	            	 *
	            	 * A)	When the new local relation is equal to the remote
	            	 * 		relation current local relation is expired and should
	            	 * 		by definition not be kept
	            	 *
	            	 * B)	When the new local relation is different from current
	            	 * 		local relation, current local relation is going to be
	            	 * 		replaced. Hence current relation must be removed from
	            	 * 		the object holder.
	            	 *
	            	 * =========================================================== */
	
	            	// is not allowed?
	            	if(aRelation==null && m_cardinality>0) return false;
	
	            	// local and server values in sync?
	            	if (equal(m_remoteValue, aRelation))
	                {
	                	// set server origin
	                    newOrigin = DataOrigin.REMOTE;
	                    // set change flag
	                    isRollback = (m_localValue!=null || m_origin!=newOrigin);
	                    // a change indicates that a rollback has occured
	                    isDirty = isRollback;
	                	// register rollback to remote value
	                    registerRelationRemoved(m_localValue,true,false,isRollback);
	                    // prepare next
	                    m_localValue = null;
	                } 
	            	else if(!equal(m_localValue, aRelation))
	                {
	            		/* =============================================
	            		 * Consistency of list (one-to-many) relations
	            		 * =============================================
	            		 * Changes in relations that belongs to list 
	            		 * relations, are validated against the list
	            		 * they belongs. See MsoListImpl.isAllowed();
	            		 * ============================================= */
	            		
	            		// If this relation is part of an list relation, validating
	            		// the change is required. However, the first time the local
	            		// relation is set, this validation is not required because 
	            		// the previous relation is null. The list only require
	            		// that changes of non-null relations are validated.
	            		//
	            		// validate?
	            		if(m_inList!=null && m_localValue!=null)
	            		{
	            			// validate
	            			if(!m_inList.isChangeable(this, m_localValue, aRelation)) 
            				{
	            				// change was not allowed by list
	            				return false;
            				}
	            		}
	                	// set local origin
	                    newOrigin = DataOrigin.LOCAL;
	                	// register deletion of current local 
	                    // value (if exists, this is a rollback)
	                    registerRelationRemoved(m_localValue,true,false,true);
	                    // prepare next
	                    m_localValue = aRelation;
	                    // register
	                    registerRelationAdded(aRelation,true,false,false);
	                    // set dirty flag
	                    isDirty = true;
	                }
	            }
	        }
	
	        // is origin changed?
	        if (m_origin != newOrigin)
	        {
	            m_origin = newOrigin;
	            isDirty = true;
	        }
	
	        // set loopback mode
	        m_isLoopbackMode = isLoopback;
	        
	        // set rollback mode
	        m_isRollbackMode = isRollback;
	        
	        // notify change?
	        if (isDirty || isLoopback || isRollback)
	        {
	            registerRelationModified(oldObj, aRelation, isLoopback, isRollback);
	        }
	        
	        // success
	        return true;
	        
		}
		
		// failure
		return false;
		
    }

    public boolean isRelationDeletable(IMsoObjectIf anObject)
    {
    	return anObject!=null && anObject.isDeleted() 	// is relation object already deleted?
	        || (isDeletable()							// relation must be deleteable 
	        	&& anObject!=null 						// the object can not be null
	        	&& get() == anObject); 					// the object must equal the related object
    }

    public boolean deleteRelation(IMsoObjectIf anObject)
    {
        if (isRelationDeletable(anObject))
        {
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();

        	// remove local relation?
        	if (m_localValue != null)
            {
            	// register rollback
                registerRelationRemoved(m_localValue,true,false,true);
            }
        	
        	// remove remote relation?
        	if (m_remoteValue != null)
            {
            	// remove from object holder
                registerRelationRemoved(m_remoteValue,true,false,false);
            }

        	// log event
            m_logger.debug("Deleted relation from " + s + " to " + anObject + " in relation " + m_name);
            
        	// success
            return true;
        }
        // not allowed
        return false;
    }
    
    public boolean commit() throws TransactionException
    {
    	// get dirty flag
        boolean isDirty = isChanged();
        // is changed?
        if(isDirty)
        {
        	// get change source
        	IChangeRecordIf changes = m_owner.getModel().getChanges(m_owner);
        	// add this as partial commit
        	changes.addFilter(this);
	        // increment change count
	        incrementChangeCount();
	        // commit changes
        	m_owner.getModel().commit(changes);
        }
        return isDirty;
    }
    
    public boolean rollback()
    {
    	// initialize restore flag
    	boolean bFlag = !m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);

    	// ensure that model is in local update mode
    	if(bFlag) m_model.setLocalUpdateMode();
    	
        // get change flag
        boolean isDirty = m_origin == DataOrigin.LOCAL
        				 || m_origin == DataOrigin.CONFLICT;

        // notify?
        if (isDirty)
        {
        	// in list (part of one-to-many relation)?
        	if(m_inList!=null)
        	{
        		// forward to list
        		if(!m_inList.rollback(this)) 
    			{
        			// rollback not allowed by list
        			return false;
    			}
        	}
        	// reset loopback mode
            m_isLoopbackMode = false;            
            // set rollback mode
            m_isRollbackMode = true;
        	// get old value
            T oldLocalValue = m_localValue;
            // reset local value
            m_localValue = null;
            // update origin
            m_origin = DataOrigin.REMOTE;
            
        	// notify
            registerRelationRemoved(oldLocalValue,true,false,true);
            registerRelationAdded(m_remoteValue,false,false,true);
            registerRelationModified(oldLocalValue,m_remoteValue,false,true);
        }
        
    	// restore previous update mode?
        if(bFlag) m_model.restoreUpdateMode();
    	
    	// finished
        return isDirty;
    }    

    public Vector<T> getConflictingValues()
    {
        if (m_origin == DataOrigin.CONFLICT)
        {
            Vector<T> retVal = new Vector<T>(2);
            retVal.add(m_remoteValue);
            retVal.add(m_localValue);
            return retVal;
        }
        return null;
    }

    public int getCardinality()
    {
        return m_cardinality;
    }

    public boolean validate() {
    	if(m_cardinality>0) {
    		return (get()!=null);
    	}
    	return true;
    }

    public boolean acceptLocal()
    {
        return acceptConflicting(DataOrigin.LOCAL);
    }

    public boolean acceptRemote()
    {
        return acceptConflicting(DataOrigin.REMOTE);
    }

    public Collection<IChangeIf.IChangeRelationIf> getChanges()
    {
    	Collection<IChangeIf.IChangeRelationIf> changes = new Vector<IChangeRelationIf>();
    	IChangeRecordIf rs = m_model.getChanges(m_owner);
    	if(rs!=null)
		{
    		for(IChangeIf it : rs.get(this))
			{
    			changes.add((IChangeRelationIf)it);
			}
		}
    	return changes;
    	
    	/* ==================================================================
    	 * Algorithm for committing relation
    	 * ================================================================== */
    	/*
        Vector<IChangeIf.IChangeRelationIf> changes = new Vector<IChangeIf.IChangeRelationIf>();
        if (m_origin == DataOrigin.LOCAL)
        {
        	// notify that current (server) relation should be deleted?
            if (m_remoteValue != null && !m_remoteValue.isDeleted())
            {
                changes.add(new ChangeImpl.ChangeRelation(this, m_remoteValue, ChangeType.DELETED));
            }
            // notify that a new (server) relation should created?
            if (m_localValue != null)
            {
                changes.add(new ChangeImpl.ChangeRelation(this, m_localValue, ChangeType.CREATED));
            }
        }
        return changes;
        */
    }
    
    @Override
    public int compareTo(IData o) {
    	return 0;
    }
    
    /* ================================================================
     * Protected methods
     * ================================================================ */
    
    protected void incrementChangeCount() {
    	m_changeCount++;
    }

    protected boolean equal(T v1, T v2)
    {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }
    
	@Override
	public MsoDataType getDataType() {
		return MsoDataType.ONTO_RELATION;
	}
    
	@Override
	public MsoClassCode getClassCode() {
		return MsoClassCode.CLASSCODE_NOCLASS;
	}
	
    /* ================================================================
     * Private methods
     * ================================================================ */
    
	private boolean isSetup(T msoObj) {
		if(msoObj == null) return true;
		return msoObj.isSetup();	
	}
	
    private boolean acceptConflicting(DataOrigin aOrigin)
    {
        if (m_origin == DataOrigin.CONFLICT)
        {
        	
        	T oldRef = null;
        	T newRef = null;
        	
            if (aOrigin == DataOrigin.LOCAL)
            {
            	/* ==========================================================
            	 * resolve conflict as a local value origin (keep local value)
            	 *
            	 * Both server and local values must be kept to enable
            	 * future conflict detection
            	 *
            	 * ========================================================== */
                registerRelationAdded(m_localValue,false,false,false);
                registerRelationRemoved(m_remoteValue,false,false,false);
            } else
            {
            	/* ==========================================================
            	 * resolve conflict as a server value origin (overwrite local value)
            	 *
            	 * Since server value is chosen, the local value must be
            	 * erased, together with any delete listener
            	 *
            	 * ========================================================== */
            	registerRelationAdded(m_remoteValue,false,false,false);
                registerRelationRemoved(m_localValue,true,false,false);
                m_localValue = null;
            }
            // update origin
            m_origin = aOrigin;
            // notify
            registerRelationModified(oldRef, newRef, false, false);
            return true;
        }
        return false;
    }
    
	/**
	 * Notify related object that a relation to it has been added
	 * 
	 * @param anObject - the object whom relation is added to
	 * @param add - this is added as delete listener in anObject if <code>true</code> 
	 * @param updateRemote - the relation change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE.
	 * @param isLoopback - the registration is a loopback (ACK from server)
	 */
    private void registerRelationAdded(T anObject, boolean add, boolean isLoopback, boolean isRollback)
    {
        if (anObject != null)
        {
        	if(add) ((AbstractMsoObject) anObject).addMsoObjectHolder(this);
        	((AbstractMsoObject) anObject).registerAddedRelation(new ChangeImpl.ChangeRelation(
        			this,m_model.getUpdateMode(),MsoEventType.ADDED_RELATION_EVENT,
        			anObject,isLoopback,isRollback));
        }
    }

    /**
     * Notify related object that a relation to it has been removed
     * 
     * @param anObject - the object whom relation is removed from
     * @param remove - this is removed as delete listener in anObject if <code>true</code> 
     * @param updateRemote - the relation change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE.
     * @param isLoopback - the registration is a loopback (ACK from server)
     */
    private void registerRelationRemoved(T anObject, boolean remove, boolean isLoopback, boolean isRollback)
    {
        if (anObject != null)
        {
        	if(remove) ((AbstractMsoObject) anObject).removeMsoObjectHolder(this);
        	((AbstractMsoObject) anObject).registerRemovedRelation(new ChangeImpl.ChangeRelation(
        			this,m_model.getUpdateMode(),MsoEventType.REMOVED_RELATION_EVENT,
        			anObject,isLoopback,isRollback));

        }
    }
    
    /**
     * Notify relation owner that a relation has been changed
     * @param oldObj - the relation object before update
     * @param newObj - the relation object after update
     * @param isLoopback - the registration is a loopback (ACK from server)
     * @param isRollback - the registration is a rollback of local value to remote value
     * @param updateRemote -the relation change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE (checked by the AbstractMsoObject instance).
     */
    private void registerRelationModified(T oldObj, T newObj, boolean isLoopback, boolean isRollback)
    {
    	boolean bFlag = (oldObj != null || newObj != null);
    	
    	if(bFlag) incrementChangeCount();
    	
        if (oldObj != null)
        {
            m_owner.registerRemovedRelation(new ChangeImpl.ChangeRelation(
        			this,m_model.getUpdateMode(),MsoEventType.REMOVED_RELATION_EVENT,
        			oldObj,isLoopback,isRollback));
            
        } 
        if (newObj != null)
        {
            m_owner.registerAddedRelation(new ChangeImpl.ChangeRelation(
        			this,m_model.getUpdateMode(),MsoEventType.ADDED_RELATION_EVENT,
        			newObj,isLoopback,isRollback));
            
        } 
        // only notify list if data is not in conflict. If data
        // is in conflict, the relation is already registered
        // with the local value. Hence, no notification is required             
        if(m_inList!=null && oldObj!=null && bFlag && isOriginLocal() && !isOriginConflict())
        {
        	m_inList.change(this, oldObj, newObj);
        }
    }

}
