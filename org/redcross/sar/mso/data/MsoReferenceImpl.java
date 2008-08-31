package org.redcross.sar.mso.data;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.committer.CommittableImpl;

import java.util.Collection;
import java.util.Vector;

/**
 *
 */
public class MsoReferenceImpl<T extends IMsoObjectIf> implements IMsoReferenceIf<T>, IMsoObjectHolderIf<T>
{
    private final AbstractMsoObject m_owner;
    private final String m_name;
    private final int m_cardinality; 
    private boolean m_canDelete = true;
    protected T m_localValue = null;
    protected T m_serverValue = null;
    protected int m_changeCount;    
    
    //private boolean m_changed = false;
    protected IMsoModelIf.ModificationState m_state = IMsoModelIf.ModificationState.STATE_UNDEFINED;

    public MsoReferenceImpl(AbstractMsoObject theOwner, String theName, int theCardinality, boolean canDelete)
    {
        m_owner = theOwner;
        m_name = theName;
        m_canDelete = canDelete;
        m_cardinality = theCardinality;
    }

    public String getName()
    {
        return m_name;
    }

    public T getReference()
    {
        return m_state == IMsoModelIf.ModificationState.STATE_LOCAL ? m_localValue : m_serverValue;
    }

    public void setCanDelete(boolean canDelete)
    {
        m_canDelete = canDelete;
    }

    public boolean canDelete()
    {
    	if (MsoModelImpl.getInstance().getUpdateMode() != IMsoModelIf.UpdateMode.LOCAL_UPDATE_MODE)
        {
            return true;
        }
        return m_canDelete;
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }
    
    protected void incrementChangeCount() {
    	m_changeCount++;
    }    
    
    public IMsoModelIf.ModificationState getState()
    {
        return m_state;
    }

    private void registerDeletedReference(T anObject, boolean remove)
    {
        if (anObject != null)
        {
        	if(remove) ((AbstractMsoObject) anObject).removeDeleteListener(this);
        	((AbstractMsoObject) anObject).registerRemovedReference();

        }
    }

    private void registerAddedReference(T anObject, boolean add)
    {
        if (anObject != null)
        {
        	if(add) ((AbstractMsoObject) anObject).addDeleteListener(this);
        	((AbstractMsoObject) anObject).registerAddedReference();
        }
    }

    private void registerReferenceChange(T newRef, T oldRef)
    {
        if (newRef != null || oldRef != null)
        {
            if (newRef != null && oldRef != null)
            {
            	incrementChangeCount();
            	m_owner.registerModifiedReference();
            } else if (newRef != null)
            {
            	incrementChangeCount();
                m_owner.registerAddedReference();
            } else
            {
            	incrementChangeCount();
                m_owner.registerRemovedReference();
            }
        }
    }    
    
    public void setReference(T aReference)
    {
    	/* ========================================================
    	 * Setting a new reference value is dependent on the
    	 * update mode of the MSO model. If the model is in 
    	 * LOOPBACK_UPDATE_MODE, the reference should reflect
    	 * the server value without any conflict detection. If the
    	 * model is in REMOTE_UPDATE_MODE, a change from the server
    	 * is registered and the reference value should be analyzed
    	 * to detect any conflicts between local value (private) and 
    	 * the server value (shared). If the model is in 
    	 * LOCAL_UPDATE_MODE, the reference value state should be 
    	 * updated according to the passed reference value.
    	 * 
    	 * Server value and local value should only be present 
    	 * concurrently during a local update sequence (between two 
    	 * commit() or rollback() invocations), or if a conflict has 
    	 * occurred (remote update during a local update sequence).
    	 * Hence, if the reference value is in a LOCAL or 
    	 * CONFLICTING, both values will be present. Else,
    	 * only server value will be present (SERVER state).
    	 *  
    	 * A commit() or rollback() will reset the local value
    	 * to null to ensure state consistency. 
    	 * 
    	 * ======================================================== */    	    	
    	
        IMsoModelIf.UpdateMode updateMode = MsoModelImpl.getInstance().getUpdateMode();
        IMsoModelIf.ModificationState newState;
        boolean valueChanged = false;

        // get current reference
        T oldReference = getReference();
        
        // replace current relation
        switch (updateMode)
        {
            case LOOPBACK_UPDATE_MODE:
            {
                
            	/* ===========================================================
            	 * Update to server value state without any conflict detection.
            	 * 
            	 * After a commit, all changes are looped back to the model
            	 * from the message queue. This ensures that only changes
            	 * that are successfully committed to the global message 
            	 * queue becomes valid. If any errors occurred, the message queue 
            	 * is given precedence over local values. 
            	 * 
            	 * Because of limitations in SaraAPI, it is not possible to 
            	 * distinguish between messages committed by this model instance 
            	 * and other model instances. Hence, any change received from 
            	 * the message queue may be a novel change. Because 
            	 * LOOPBACK_UPDATE_MODE is only a proper update mode if the
            	 * received update is a loop back from this model instance, the
            	 * mode can not be used with the current SaraAPI. If used, 
            	 * local changes will be overwritten by because conflict detection 
            	 * is disabled in this mode.
            	 * 
            	 * The fix to this problem is to assume that if a commit is
            	 * executed without any exception from the message queue, then all 
            	 * changes was posted and forwarded to all listeners. Hence, the
            	 * attribute value can be put in server mode directly after a commit 
            	 * is executed. 
            	 * 
            	 * 		!!! The postProcessCommit() implements this fix !!! 
            	 * 
            	 * If the source of each change could be uniquely identified 
            	 * (at the model instance level), change messages received as a 
            	 * result of a commit by this model, could be group together and 
            	 * forwarded to the model using the LOOPBACK_UPDATE_MODE. This would 
            	 * be the correct and intended usage of this mode.
            	 * 
            	 * IMPORTANT: The local reference is only deleted from its 
            	 * object holder if local and server references are 
            	 * different because  
            	 * 
            	 * A)	The local references is no longer valid.
            	 * 	
            	 * B) 	When local and server references are equal, 
            	 * 		the reference to the object holder is already 
            	 * 		established.
            	 * 
            	 * =========================================================== */
            	
            	// set state
            	newState = IMsoModelIf.ModificationState.STATE_SERVER;
                
            	// is equal?
            	boolean isConflict = !equal(m_localValue, aReference);
            	
                // only delete local reference from object holder
                // if local reference is different from server reference
                if(isConflict) 
                {
                	// remove expired reference from object holder
                	registerDeletedReference(m_localValue,true);
                }
                
                // only server value is kept
                m_localValue = null;                	                
                
            	// any change?
                if (!equal(m_serverValue, aReference))
                {
                	// remove server reference from object holder
                	registerDeletedReference(m_serverValue,true);
                	
                    // prepare next
                    m_serverValue = aReference;
                    
                    // register? 
                    if(isConflict) 
                    {
                    	// add reference to object holder
                    	registerAddedReference(m_serverValue,true);
                    }
                    
                    // set flag
                    valueChanged = true;
                    
                }
            	
                
                break;
            }
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
				 * reference are equal, the reference state is changed to SERVER.             	 
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
            	boolean isConflict = (m_state == IMsoModelIf.ModificationState.STATE_LOCAL 
            			|| m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING) ? 
            			!equal(m_localValue, aReference) : false;
            	
            	// get next state
                newState = isConflict ? 
                		  IMsoModelIf.ModificationState.STATE_CONFLICTING 
                		: IMsoModelIf.ModificationState.STATE_SERVER;
                
                // only reset local value if no conflict is found, 
                if(!isConflict) 
                {
                    // prepare next
                    m_localValue = null;                	
                }
                
                // any change?
                if (!equal(m_serverValue, aReference))
                {
                	// register
                    registerDeletedReference(m_serverValue,true);
                    // prepare next
                    m_serverValue = aReference;
                    // register
                    registerAddedReference(m_serverValue,true);
                    // set flag
                    valueChanged = true;
                }         
                
                // notify change on every conflict
                valueChanged |= isConflict;

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
            	 * should be set to SERVER. If the new reference is different 
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
            	 * C)
            	 * 
            	 * =========================================================== */

            	
            	// local and server values in sync?
            	if (equal(m_serverValue, aReference))
                {
                	// set server state
                    newState = IMsoModelIf.ModificationState.STATE_SERVER;
                	// register change
                    registerDeletedReference(m_localValue,true);
                    // set flag
                    valueChanged = (m_localValue!=null);
                    // prepare next
                    m_localValue = null;
                } else
                {                	
                	// set local state
                    newState = IMsoModelIf.ModificationState.STATE_LOCAL;                    
                	// register
                    registerDeletedReference(m_localValue,true);
                    // prepare next
                    m_localValue = aReference;
                    // register
                    registerAddedReference(m_localValue,true);
                    // set flag
                    valueChanged = true;
                }
            }
        }
        
        // is state changed?
        if (m_state != newState)
        {
            m_state = newState;
            valueChanged = true;
        }
        
        // notify change?
        if (valueChanged)
        {
            registerReferenceChange(aReference, oldReference);
        }

    }

    protected boolean equal(T v1, T v2)
    {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }
        
    
    /*
    CMR IMPLEMENTATION, replaced by Kengu
    public void setReference(T aReference)
    {
        IMsoModelIf.UpdateMode updateMode = MsoModelImpl.getInstance().getUpdateMode();
        T oldReference = getReference();
        IMsoModelIf.ModificationState newState;
        boolean valueChanged = false;

        // prepare to replace current relation 
        if (m_state != IMsoModelIf.ModificationState.STATE_LOCAL)
        {
            if (m_serverValue != null)
            {
                ((AbstractMsoObject) m_serverValue).removeDeleteListener(this);
                registerDeletedReference(m_serverValue);
            }
        } else
        {
            if (m_localValue != null)
            {
                ((AbstractMsoObject) m_localValue).removeDeleteListener(this);
                registerDeletedReference(m_localValue);
            }
        }

        // replace current relation
        switch (updateMode)
        {
            case LOOPBACK_UPDATE_MODE:
            {
                newState = IMsoModelIf.ModificationState.STATE_SERVER;
                m_serverValue = aReference;
                if (m_serverValue != null)
                {
                    ((AbstractMsoObject) m_serverValue).addDeleteListener(this);
                    ((AbstractMsoObject) m_serverValue).registerAddedReference();
                }
                valueChanged = true;
                break;
            }
            case REMOTE_UPDATE_MODE:
            {
                newState = m_state == IMsoModelIf.ModificationState.STATE_LOCAL ? 
                		IMsoModelIf.ModificationState.STATE_CONFLICTING : IMsoModelIf.ModificationState.STATE_SERVER;
                m_serverValue = aReference;
                if (m_serverValue != null)
                {
                    ((AbstractMsoObject) m_serverValue).addDeleteListener(this);
                    ((AbstractMsoObject) m_serverValue).registerAddedReference();
                }
                break;
            }
            default:
            {
                newState = IMsoModelIf.ModificationState.STATE_LOCAL;
                m_localValue = aReference;
                if (m_localValue != null)
                {
                    ((AbstractMsoObject) m_localValue).addDeleteListener(this);
                    registerAddedReference(m_localValue);
                }
            }
        }
        if (m_state != newState)
        {
            m_state = newState;
            valueChanged = true;
        }

        if (valueChanged)
        {
        	/*
            if (oldReference != null)
            {
                //System.out.println("Removed reference from " + m_owner + " to " + oldReference);
            }
            if (aReference != null)
            {
                //System.out.println("Added reference from " + m_owner + " to " + aReference);
            }
        	
            registerReferenceChange(aReference, oldReference);
        }
        //m_changed = valueChanged;
    }
    */

    public Vector<T> getConflictingValues()
    {
        if (m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING)
        {
            Vector<T> retVal = new Vector<T>(2);
            retVal.add(m_serverValue);
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
    
    /**
     * Perform rollback
     */
    public void rollback()
    {
        T oldLocalValue = m_localValue;
        m_localValue = null;
        boolean isChanged = m_state == IMsoModelIf.ModificationState.STATE_LOCAL 
        				 || m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING;
        m_state = IMsoModelIf.ModificationState.STATE_SERVER;
        if (isChanged)
        {
        	// notify
            registerDeletedReference(oldLocalValue,true);
            registerAddedReference(m_serverValue,false);
            registerReferenceChange(m_serverValue, oldLocalValue);
        }
    }

    /**
     * Perform local commit
     * Change values without changing listeners etc.
     */
    public void postProcessCommit()
    {
        boolean isChanged = m_state == IMsoModelIf.ModificationState.STATE_LOCAL 
        				 || m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING;
        m_state = IMsoModelIf.ModificationState.STATE_SERVER;
        if (isChanged)
        {
            m_serverValue = m_localValue;
            registerDeletedReference(m_localValue, true);
            m_localValue = null;
            registerReferenceChange(m_serverValue, m_serverValue);
        }
    }



    private boolean acceptConflicting(IMsoModelIf.ModificationState aState)
    {
        if (m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING)
        {
            if (aState == IMsoModelIf.ModificationState.STATE_LOCAL)
            {
            	/* ==========================================================
            	 * resolve conflict as a local value state (keep local value)
            	 * 
            	 * Both server and local values must be kept to enable
            	 * future conflict detection  
            	 * 
            	 * ========================================================== */
                registerAddedReference(m_localValue,false);
                registerDeletedReference(m_serverValue,false);
            } else
            {
            	/* ==========================================================
            	 * resolve conflict as a server value state (overwrite local value)
            	 * 
            	 * Since server value is chosen, the local value must be
            	 * erased, together with any delete listener
            	 * 
            	 * ========================================================== */
            	registerAddedReference(m_serverValue,false);
                registerDeletedReference(m_localValue,true);
                m_localValue = null;
            }
            m_state = aState;
            // notify
            m_owner.registerModifiedReference();
            return true;
        }
        return false;
    }

    public boolean acceptLocal()
    {
        //MsoModelImpl.getInstance().setLocalUpdateMode();
        boolean retVal = acceptConflicting(IMsoModelIf.ModificationState.STATE_LOCAL);
        //MsoModelImpl.getInstance().restoreUpdateMode();
        return retVal;
    }

    public boolean acceptServer()
    {
        //MsoModelImpl.getInstance().setRemoteUpdateMode();
        boolean retVal = acceptConflicting(IMsoModelIf.ModificationState.STATE_SERVER);
        //MsoModelImpl.getInstance().restoreUpdateMode();
        return retVal;
    }


    public boolean isUncommitted()
    {
        return m_state == IMsoModelIf.ModificationState.STATE_LOCAL;
    }

    public boolean canDeleteReference(T anObject)
    {
        return (getReference() == anObject && canDelete());
    }

    public boolean doDeleteReference(T anObject)
    {
        if (getReference() == anObject && canDelete())
        {
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
            System.out.println("Delete reference from " + s + " to " + anObject);

        	// remove server reference?
        	if (m_serverValue!=null)
            {
            	// remove from object holder
                registerDeletedReference(m_serverValue,true);
            }
        	
        	// remove local reference?
        	if (m_localValue!=null)
            {
            	// register
                registerDeletedReference(m_localValue,true);
            }            
        	        
            return true;
        }
        return false;
    }

    public Collection<CommittableImpl.CommitReference> getCommittableRelations()
    {
        Vector<CommittableImpl.CommitReference> retVal = new Vector<CommittableImpl.CommitReference>();
        if (m_state == IMsoModelIf.ModificationState.STATE_LOCAL)
        {
            if (m_serverValue != null && !m_serverValue.hasBeenDeleted())
            {
                retVal.add(new CommittableImpl.CommitReference(m_name, m_owner, m_serverValue, CommitManager.CommitType.COMMIT_DELETED));
            }
            if (m_localValue != null)
            {
                retVal.add(new CommittableImpl.CommitReference(m_name, m_owner, m_localValue, CommitManager.CommitType.COMMIT_CREATED));
            }

        }
        return retVal;
    }

}
