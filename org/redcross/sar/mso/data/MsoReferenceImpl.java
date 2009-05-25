package org.redcross.sar.mso.data;

import org.redcross.sar.mso.ChangeImpl;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.ChangeType;
import org.redcross.sar.mso.IChangeIf.IChangeReferenceIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.IMsoModelIf.ModificationState;

import java.util.Collection;
import java.util.Vector;

/**
 * <b>This class manages one-to-one references. </b></p>
 * 
 * The purpose of this class is to manage one-to-one references between 
 * IMsoObjectIf instances. The class is an intermediate object between
 * each object in the reference. </p>
 * 
 * The <code>setReference(IMsoObjectIf anObject)</code> 
 * method maintains the reference from the owner object (also known as 
 * "from object", must be an AbstractMsoObject instance) to the 
 * referenced object (also known as the "to object"). Each time a 
 * reference is set or reset, the owner is notified.</p> 
 * 
 * Referenced object may or may not be deleted, depending on the 
 * type of relation. Class members <code>isDeletable()</code> and 
 * <code>getCardinality()</code> is used to govern this. As long as 
 * cardinality is greater than zero, referenced objects can not 
 * deleted, nor set to null using <code>setReference(null)</code>. </p> 
 * 
 * This class implements IMsoObjectHolderIf. The IMsoObjectHolderIf 
 * interface enables the referenced object to check if it can delete itself 
 * (see <code>isReferenceDeletable(IMsoObjectIf anObject)</code> 
 * and enables the referenced object to notify when it is deleted. 
 * When a referenced object is deleted, the reference should be 
 * deleted also (see <code>deleteReference(IMsoObjectIf anObject)</code>).</p> 
 *  
 * @author vinjar, kenneth
 */
public class MsoReferenceImpl<T extends IMsoObjectIf> implements IMsoReferenceIf<T>, IMsoObjectHolderIf<T>
{
    private final AbstractMsoObject m_owner;

    protected T m_localValue = null;
    protected T m_remoteValue = null;
    protected int m_changeCount;

    protected ModificationState m_state = ModificationState.STATE_UNDEFINED;

    private final String m_name;
    private final int m_cardinality;
    protected final IMsoModelIf m_msoModel;

    private boolean m_isDeleteable = true;
    private boolean m_isLoopbackMode = false;
    private boolean m_isRollbackMode = false;
    
    protected MsoReferenceImpl(AbstractMsoObject theOwner, String theName, int theCardinality, boolean isDeletable)
    {
        m_owner = theOwner;
    	if(m_owner==null)
    	{
    		throw new IllegalArgumentException("MsoReferenceImpl must have a owner");
    	}
        m_name = theName;
        m_msoModel = m_owner.getModel();
        m_cardinality = theCardinality;
        m_isDeleteable = isDeletable;
    }

    public String getName()
    {
        return m_name;
    }
    
    public IMsoObjectIf getOwner() {
    	return m_owner;
    }    

    public T getReference()
    {
        return m_state == ModificationState.STATE_LOCAL ? m_localValue : m_remoteValue;
    }

    public T getLocalReference()
    {
        return m_localValue;
    }
    
    public T getRemoteReference()
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

    public boolean isChanged() {
    	return m_state == ModificationState.STATE_LOCAL 
    		|| m_state == ModificationState.STATE_CONFLICT;
    }

    public boolean isChangedSince(int changeCount)
    {
        return (m_changeCount>=changeCount);
    }
    
    public int getChangeCount()
    {
        return m_changeCount;
    }

    protected void incrementChangeCount() {
    	m_changeCount++;
    }

    public ModificationState getState()
    {
        return m_state;
    }

    public boolean isState(ModificationState state) {
    	return (m_state==state);
    }

    @Override
	public boolean isLocalState() {
    	
		return m_state==ModificationState.STATE_LOCAL;
	}

	@Override
	public boolean isRemoteState() {
		return m_state==ModificationState.STATE_REMOTE;
	}

	@Override
	public boolean isConflictState() {
		return m_state==ModificationState.STATE_CONFLICT;
	}
	
	@Override
	public boolean isLoopbackMode() {
		return m_isLoopbackMode;
	}

	@Override
	public boolean isRollbackMode() {
		return m_isRollbackMode;
	}
	
	@Override
	public boolean isMixedState() {
		return false;
	}
	
    public boolean setReference(T aReference)
    {
    	/* ========================================================
    	 * Setting a new reference value is dependent on the
    	 * update mode of the MSO model. 
    	 * 
    	 * If the model is in REMOTE_UPDATE_MODE, a change from 
    	 * the server is registered and the reference value 
    	 * should be analyzed to detect any conflicts between 
    	 * local value (private) and the server value (shared). 
    	 * 
    	 * If the reference been changed locally (isChanged() is true), 
    	 * and the model is in REMOTE_UPDATE_MODE, this update may 
    	 * be a loopback. Since loopback updates are just a ACK
    	 * from the server, the attribute value is not changed. Hence,
    	 * IMsoClientUpdateListener listeners is not required to fetch
    	 * the attribute value. However, loopback updates may be used to
    	 * indicate to the user that the commit was successful.
    	 *  
    	 * If the model is in LOCAL_UPDATE_MODE, the reference 
    	 * value state should be updated according to the 
    	 * passed reference value.
    	 *
    	 * Server value and local value should only be present
    	 * concurrently during a local update sequence (between two
    	 * commit or rollback transactions), or if a conflict has
    	 * occurred (remote update during a local update sequence).
    	 * Hence, if the reference value is in a LOCAL or
    	 * CONFLICTING, both values will be present. Else,
    	 * only server value will be present (REMOTE state).
    	 *
    	 * A commit() or rollback() will reset the local value
    	 * to null to ensure state consistency.
    	 *
    	 * ======================================================== */

        UpdateMode updateMode = m_msoModel.getUpdateMode();
        ModificationState newState;
        boolean isDirty = false;
        boolean isLoopback = false;
        boolean isRollback = false;

        // get current reference
        T oldReference = getReference();

        // replace current relation
        switch (updateMode)
        {
            case REMOTE_UPDATE_MODE:
            {

            	/* ===========================================================
            	 * Update to server value state with conflict detection.
            	 *
            	 * If the model is in REMOTE_UPDATE_MODE, this indicates that
            	 * an external change is detected (message queue update), and the
            	 * model should be update accordingly.
            	 *
            	 * If the reference is in local state, the local reference may
            	 * be different from the server reference. Local changes are made
            	 * by the user (GUI) or a local service (the application). When a
            	 * remote update occurs (a change received from the message queue),
            	 * the new reference state depends on the new server reference
            	 * and current (local) reference. If these are different, a conflict has
            	 * occurred. This is indicated by setting the reference state
            	 * to CONFLICTING. Methods for resolving conflicts are supplied
				 * by this class. If the new server reference and current (local)
				 * reference are equal, the reference state is changed to REMOTE.
            	 *
            	 * IMPORTANT: The local reference is never deleted from the object
            	 * holder because
            	 *
            	 * A) 	When local and server references are different, this is
            	 * 		by definition a conflict an both must be kept.
            	 *
            	 * B)	When local and server references are equal,
            	 * 		the reference to the object holder is already
            	 * 		established.
            	 *
            	 * =========================================================== */

            	// check if a conflict has occurred?
            	boolean isConflict = (m_state == ModificationState.STATE_LOCAL
            			|| m_state == ModificationState.STATE_CONFLICT) ?
            			!equal(m_localValue, aReference) : false;

            	// get next state
                newState = isConflict ?
                		  ModificationState.STATE_CONFLICT
                		: ModificationState.STATE_REMOTE;

                // no conflict found?
                if(!isConflict)
                {
                	/* If the reference is locally changed, and the 
                	 * remote reference value equals the local reference 
                	 * value, this is a loopback */
                    isLoopback = (isChanged() && equal(m_localValue, aReference));
                    // reset local value
                    m_localValue = null;
                }

                // any change?
                if (!equal(m_remoteValue, aReference))
                {
                	// register
                    registerDeletedReference(m_remoteValue,true,false,isLoopback,false);
                    // prepare next
                    m_remoteValue = aReference;
                    // register
                    registerAddedReference(aReference,true,false,isLoopback,false);
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
            	 * Update reference to the appropriate state
            	 *
            	 * The default update mode is LOCAL_UPDATE_MODE. This mode
            	 * indicates that the change in reference value originates
            	 * from a GUI (user) or Service (application) invocation, and
            	 * not from an external change made on a different model
            	 * instance (message queue). If the new local (current)
            	 * reference equals the server reference, the reference state
            	 * should be set to REMOTE. If the new reference is different
            	 * from the server reference, the reference state should be
            	 * set to LOCAL.
            	 *
            	 * IMPORTANT 1: Current local reference is always deleted
            	 * from its object holder, unless it is NULL, because
            	 *
            	 * A)	When the new local references is equal to the server
            	 * 		reference, current local reference is expired and should
            	 * 		by definition not be kept
            	 *
            	 * B)	When the new local references is different from current
            	 * 		local reference, current local reference is going to be
            	 * 		replaced. Hence current reference must be removed from
            	 * 		the object holder.
            	 *
            	 * =========================================================== */

            	// is not allowed?
            	if(aReference==null && m_cardinality>0) return false;

            	// local and server values in sync?
            	if (equal(m_remoteValue, aReference))
                {
                	// set server state
                    newState = ModificationState.STATE_REMOTE;
                	// register change
                    registerDeletedReference(m_localValue,true,true,false,true);
                    // set flag
                    isDirty = (m_localValue!=null);
                    // prepare next
                    m_localValue = null;
                    // set flag
                    isRollback = true;
                } else
                {
                	// set local state
                    newState = ModificationState.STATE_LOCAL;
                	// register deletion of current local value
                    registerDeletedReference(m_localValue,true,true,false,false);
                    // prepare next
                    m_localValue = aReference;
                    // register
                    registerAddedReference(aReference,true,true,false,false);
                    // set flag
                    isDirty = true;
                }
            }
        }

        // is state changed?
        if (m_state != newState)
        {
            m_state = newState;
            isDirty = true;
        }

        // notify change?
        if (isDirty || isLoopback)
        {
            registerChangedReference(aReference, oldReference, isDirty, isLoopback, isRollback);
        }
        
        // set loopback mode
        m_isLoopbackMode = isLoopback;
        
        // set rollback mode
        m_isRollbackMode = isRollback;
        
        // success
        return true;

    }

    public boolean isReferenceDeletable(T anObject)
    {
        return isDeletable()				// reference must be deleteable 
        	&& anObject!=null 				// the object can not be null
        	&& getReference() == anObject; 	// the object must equal the referenced object
    }

    public boolean deleteReference(T anObject)
    {
        if (isReferenceDeletable(anObject))
        {
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
            System.out.println("Delete reference from " + s + " to " + anObject);

        	// remove server reference?
        	if (m_remoteValue!=null)
            {
            	// remove from object holder
                registerDeletedReference(m_remoteValue,true,true,false,false);
            }

        	// remove local reference?
        	if (m_localValue!=null)
            {
            	// register
                registerDeletedReference(m_localValue,true,true,false,false);
            }
        	// success
            return true;
        }
        // not allowed
        return false;
    }
    
    public boolean rollback()
    {

    	// prepare
        T oldLocalValue = m_localValue;
        m_localValue = null;

        // get change flag
        boolean isDirty = m_state == ModificationState.STATE_LOCAL
        				 || m_state == ModificationState.STATE_CONFLICT;

        // update state
        m_state = ModificationState.STATE_REMOTE;

        // notify?
        if (isDirty)
        {
        	// reset loopback mode
            m_isLoopbackMode = false;            
            // set rollback mode
            m_isRollbackMode = true;
        	// notify
            registerDeletedReference(oldLocalValue,true,false,false,true);
            registerAddedReference(m_remoteValue,false,false,false,true);
            registerChangedReference(m_remoteValue,oldLocalValue,false,false,true);
        }
        return isDirty;
    }    
    
    protected boolean equal(T v1, T v2)
    {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }

    public Vector<T> getConflictingValues()
    {
        if (m_state == ModificationState.STATE_CONFLICT)
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
    		return (getReference()!=null);
    	}
    	return true;
    }

    private boolean acceptConflicting(ModificationState aState)
    {
        if (m_state == ModificationState.STATE_CONFLICT)
        {
            if (aState == ModificationState.STATE_LOCAL)
            {
            	/* ==========================================================
            	 * resolve conflict as a local value state (keep local value)
            	 *
            	 * Both server and local values must be kept to enable
            	 * future conflict detection
            	 *
            	 * ========================================================== */
                registerAddedReference(m_localValue,false,false,false,false);
                registerDeletedReference(m_remoteValue,false,false,false,false);
            } else
            {
            	/* ==========================================================
            	 * resolve conflict as a server value state (overwrite local value)
            	 *
            	 * Since server value is chosen, the local value must be
            	 * erased, together with any delete listener
            	 *
            	 * ========================================================== */
            	registerAddedReference(m_remoteValue,false,false,false,false);
                registerDeletedReference(m_localValue,true,false,false,false);
                m_localValue = null;
            }
            m_state = aState;
            // notify
            m_owner.registerModifiedReference(this,m_msoModel.getUpdateMode(),false,false,false);
            return true;
        }
        return false;
    }
    
    public boolean acceptLocal()
    {
        return acceptConflicting(ModificationState.STATE_LOCAL);
    }

    public boolean acceptRemote()
    {
        return acceptConflicting(ModificationState.STATE_REMOTE);
    }

	/**
	 * Notify referenced object that a reference to it has been added
	 * 
	 * @param anObject - the object whom references is added to
	 * @param add - this is added as delete listener in anObject if <code>true</code> 
	 * @param updateServer - the reference change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE.
	 * @param isLoopback - the registration is a loopback (ACK from server)
	 */
    private void registerAddedReference(T anObject, boolean add, boolean updateServer, boolean isLoopback, boolean isRollback)
    {
        if (anObject != null)
        {
        	if(add) ((AbstractMsoObject) anObject).addMsoObjectHolder(this);
        	((AbstractMsoObject) anObject).registerAddedReference(this,
        			m_msoModel.getUpdateMode(),updateServer,isLoopback,isRollback);
        }
    }

    /**
     * Notify referenced object that a reference to it has been deleted
     * 
     * @param anObject - the object whom references is removed from
     * @param remove - this is removed as delete listener in anObject if <code>true</code> 
     * @param updateServer - the reference change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE.
     * @param isLoopback - the registration is a loopback (ACK from server)
     */
    private void registerDeletedReference(T anObject, boolean remove, boolean updateServer, boolean isLoopback, boolean isRollback)
    {
        if (anObject != null)
        {
        	if(remove) ((AbstractMsoObject) anObject).removeMsoObjectHolder(this);
        	((AbstractMsoObject) anObject).registerRemovedReference(this,m_msoModel.getUpdateMode(),updateServer,isLoopback,isRollback);

        }
    }
    
    /**
     * Notify reference owner that a reference has been changed
     * 
     * @param newRef - the reference object after update
     * @param oldRef - the reference object before update
     * @param updateServer -the reference change is forwarded to server if <code>true</code> and 
	 * UpdateMode is LOCAL_UPDATE_MODE.
     * @param isLoopback - the registration is a loopback (ACK from server)
     * @param isRollback - the registration is a rollback of local value to remote value
     */
    private void registerChangedReference(T newRef, T oldRef, boolean updateServer, boolean isLoopback, boolean isRollback)
    {
        if (newRef != null || oldRef != null)
        {
            if (newRef != null && oldRef != null)
            {
        		incrementChangeCount();
        		
            	m_owner.registerModifiedReference(this,m_msoModel.getUpdateMode(),updateServer,isLoopback,isRollback);            	
            } 
            else if (newRef != null)
            {
            	incrementChangeCount();
            	
                m_owner.registerAddedReference(this,m_msoModel.getUpdateMode(),updateServer,isLoopback,isRollback);                
            } 
            else
            {
            	incrementChangeCount();
            	
                m_owner.registerRemovedReference(this,m_msoModel.getUpdateMode(),updateServer,isLoopback,isRollback);	                
            }
        }
    }

    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences()
    {
    	/* ==================================================================
    	 * Algorithm for committing reference
    	 * ================================================================== */
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        if (m_state == ModificationState.STATE_LOCAL)
        {
        	// notify that current (server) reference should be deleted?
            if (m_remoteValue != null && !m_remoteValue.isDeleted())
            {
                changes.add(new ChangeImpl.ChangeReference(m_name, m_owner, m_remoteValue, ChangeType.DELETED));
            }
            // notify that a new (server) reference should created?
            if (m_localValue != null)
            {
                changes.add(new ChangeImpl.ChangeReference(m_name, m_owner, m_localValue, ChangeType.CREATED));
            }
        }
        return changes;
    }
    
    public Collection<IChangeIf.IChangeReferenceIf> getChangedReferences(Collection<IChangeIf> partial)
    {
    	/* ==================================================================
    	 * Algorithm for conditionally committing a reference
    	 * ================================================================== */
        Vector<IChangeIf.IChangeReferenceIf> changes = new Vector<IChangeIf.IChangeReferenceIf>();
        if (m_state == ModificationState.STATE_LOCAL) 
        {
        	// loop over all partial 
        	for(IChangeIf it : partial) 
	        {
        		if(it instanceof IChangeReferenceIf) 
        		{
        			// get referred object
        			IMsoObjectIf msoObj = ((IChangeReferenceIf)it).getReferredObject();
        			// is the same as this?
        			
		        	// notify that current (server) reference should be deleted?
		            if (m_remoteValue != null && msoObj == m_remoteValue && !m_remoteValue.isDeleted())
		            {
		                changes.add(new ChangeImpl.ChangeReference(m_name, m_owner, m_remoteValue, ChangeType.DELETED));
		            }
		            
		            // notify that a new (server) reference should created?
		            if (m_localValue != null && m_localValue == msoObj)
		            {
		                changes.add(new ChangeImpl.ChangeReference(m_name, m_owner, m_localValue, ChangeType.CREATED));
		            }
        		}
	        }
    	}
        return changes;
    }
    
    
}
