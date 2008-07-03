package org.redcross.sar.mso.data;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.committer.CommittableImpl;
import org.redcross.sar.util.except.IllegalDeleteException;

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
    	 * Setting a new reference follows the same principles
    	 * described in the method 
    	 * 
    	 * AttributeImpl.setAttrValue(T aValue, boolean isCreating);
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
                
            	// set state
            	newState = IMsoModelIf.ModificationState.STATE_SERVER;
                
                // register change
                registerDeletedReference(m_localValue,true);
                // prepare next
                m_localValue = null;                	
                
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
            	
                break;
            }
            case REMOTE_UPDATE_MODE:
            {

            	// check if a conflict has occurred?
            	boolean isConflict = (m_state == IMsoModelIf.ModificationState.STATE_LOCAL 
            			|| m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING) ? 
            			!equal(m_localValue, aReference) : false;
            	
            	// get next state
                newState = isConflict ? 
                		  IMsoModelIf.ModificationState.STATE_CONFLICTING 
                		: IMsoModelIf.ModificationState.STATE_SERVER;
                
                // no conflict?
                if(!isConflict) 
                {
                	// register change
                    registerDeletedReference(m_localValue,true);
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
            default:
            {
            	// local and server values in sync?
            	if (equal(m_serverValue, aReference))
                {
                	// set server state
                    newState = IMsoModelIf.ModificationState.STATE_SERVER;
                	// register change
                    registerDeletedReference(m_localValue,true);
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
            /*
            Remove reference the "ordinary" way. Will create event for modified reference and state in this object.
             */
            String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
            System.out.println("Delete reference from " + s + " to " + anObject);
            //System.out.println(s);
            setReference(null);
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
