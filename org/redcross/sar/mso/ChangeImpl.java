package org.redcross.sar.mso;

import java.util.Calendar;

import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoDataIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;

/**
 * Base class for managing change objects
 */
@SuppressWarnings("unchecked")
public abstract class ChangeImpl implements IChangeIf
{
	protected int m_mask;
	protected long m_seqNo = -1;
	protected IMsoDataIf m_object;
	protected UpdateMode m_mode;
	protected boolean m_isLoopbackMode;
	protected boolean m_isRollbackMode;
	protected Calendar m_time;
	
	public ChangeImpl(IMsoDataIf aObject, UpdateMode aMode, MsoEventType mask, boolean isLoopback, boolean isRollback)
	{
		m_object = aObject;
		m_mode = aMode;
		m_mask = mask.maskValue();
		m_isLoopbackMode = isLoopback;
		m_isRollbackMode = isRollback;
		m_time = Calendar.getInstance();
		m_seqNo = -1;
	}

	@Override 
	public UpdateMode getUpdateMode()
	{
		return m_mode;
	}
	
	@Override
	public long getSeqNo() {
		return m_seqNo;
	}
	
	public void setSeqNo(long seqNo)
	{
		m_seqNo = seqNo;
	}

	@Override
	public int getMask()
	{
		return m_mask;
	}
	
	@Override
	public boolean isFlagSet(int flag) {
		return isFlagSet(flag, getMask());
	}

	@Override
	public boolean isChanged() 
	{
		return getMask()>0;
	}
	
	@Override
    public boolean isObjectCreated()
    {
    	return isFlagSet(MsoEventType.CREATED_OBJECT_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isObjectDeleted()
    {
    	return isFlagSet(MsoEventType.DELETED_OBJECT_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isObjectModified()
    {
    	return isFlagSet(MsoEventType.MODIFIED_DATA_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationAdded()
    {
    	return isFlagSet(MsoEventType.ADDED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationRemoved()
    {
    	return isFlagSet(MsoEventType.REMOVED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationModified()
    {
    	return isFlagSet(MsoEventType.ADDED_RELATION_EVENT.maskValue(),getMask())
    	    || isFlagSet(MsoEventType.REMOVED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isAllDataCleared()
    {
    	return isFlagSet(MsoEventType.CLEAR_ALL_EVENT.maskValue(),getMask());
    }
    
	@Override
	public boolean isLoopbackMode() 
	{
		return m_isLoopbackMode;
	}

	@Override
	public boolean isRollbackMode() 
	{
		return m_isRollbackMode;
	}
	
	@Override
	public IMsoDataIf getObject() 
	{
		return m_object;
	}
	
    @Override
	public String getObjectId() 
    {
    	return m_object.getObjectId();
    }

	@Override
	public int compareTo(IChangeIf change) {
		return (int)(m_seqNo - change.getSeqNo());
	}
    
	public static boolean isFlagSet(int flag, int mask)
    {
    	return (mask & flag) == flag;
    }
	
	public IChangeIf clone() throws CloneNotSupportedException
	{
		return null;
	}
    
	/**
	 * Class for changed MsoObjects.
	 */
	public static class ChangeObject extends ChangeImpl implements IChangeObjectIf
	{
		public ChangeObject(IMsoObjectIf anObject)
		{
			this(anObject,anObject.getModel().getUpdateMode(),MsoEventType.EMPTY_EVENT,false,false);
		}
		
		public ChangeObject(IMsoObjectIf anObject, UpdateMode aMode, MsoEventType aMask, boolean isLoopback, boolean isRollback)
		{
			super(anObject,aMode,aMask,isLoopback,isRollback);
		}
		
		@Override
		public IMsoObjectIf getMsoObject()
		{
			return (IMsoObjectIf)m_object;
		}

		@Override
		public IChangeObjectIf clone() {
			ChangeObject aChange = new ChangeObject(getMsoObject(),
					m_mode,MsoEventType.EMPTY_EVENT,
					m_isLoopbackMode,m_isRollbackMode);
			aChange.m_mask = m_mask;
			return aChange;
		}		
			    
	}

	public static class ChangeAttribute extends ChangeImpl implements IChangeAttributeIf
	{
		private Object m_value;

		public ChangeAttribute(IMsoAttributeIf<?> theAttribute, UpdateMode aMode, Object value, MsoEventType aMask, boolean isLoopback, boolean isRollback)
		{
			super(theAttribute,aMode,aMask,isLoopback,isRollback);
			m_value = value;
		}

		@Override
		public int getMask()
		{
			return m_mask;
		}
		
		@Override
		public String getName() {
			return getMsoAttribute().getName();
		}
		
		@Override
		public Object getValue() {
			return m_value;
		}

		@Override
		public IMsoAttributeIf<?> getMsoAttribute() {
			return (IMsoAttributeIf<?>)getObject();
		}

		@Override
		public IMsoObjectIf getOwnerObject() {
			return getMsoAttribute().getOwnerObject();
		}

		@Override
		public IChangeAttributeIf clone() {
			ChangeAttribute aChange = new ChangeAttribute(getMsoAttribute(),
					m_mode,m_value,MsoEventType.EMPTY_EVENT,
					m_isLoopbackMode,m_isRollbackMode);
			aChange.m_mask = m_mask;
			return aChange;
		}		
		
	}

	/**
	 * Class for storing changes made to a relation.
	 * A relation is defined by a relation name and two MsoObjects.
	 * The name is needed since some classes may have several types of relations to another class.
	 */
	public static class ChangeRelation extends ChangeImpl implements IChangeRelationIf
	{
		private IMsoObjectIf m_referredObj;

		public ChangeRelation(IMsoRelationIf<?> aRelation, UpdateMode aMode, MsoEventType aMask, IMsoObjectIf theReferredObj, boolean isLoopback, boolean isRollback)
		{
			super(aRelation, aMode, aMask, isLoopback, isRollback);
			m_referredObj = theReferredObj;
		}

		@Override
		public int getMask()
		{
			return m_mask;
		}
		
	    @Override
		public String getName()
		{
			return getMsoRelation().getName();
		}
	    	  
	    @Override
		public boolean isInList() {
			return getMsoRelation().isInList();
		}

		@Override
		public IMsoObjectIf getRelatingObject()
		{
			return getMsoRelation().getOwnerObject();
		}

	    @Override
		public IMsoObjectIf getRelatedObject()
		{
			return m_referredObj;
		}
					
	    @Override
		public IMsoRelationIf<?> getMsoRelation() {
			return (IMsoRelationIf<?>)getObject();
		}

		@Override
		public IChangeRelationIf clone() {
			ChangeRelation aChange = new ChangeRelation(getMsoRelation(),
					m_mode,MsoEventType.EMPTY_EVENT, m_referredObj,
					m_isLoopbackMode,m_isRollbackMode);
			aChange.m_mask = m_mask;
			return aChange;
		}		
		
	    @Override
		public boolean equals(Object o)
	    {
	        if (this == o)
	        {
	            return true;
	        }
	        if (o == null || getClass() != o.getClass())
	        {
	            return false;
	        }

	        ChangeRelation refObj = (ChangeRelation) o;

	        if (getName() != null && !getName().equals(refObj.getName()))
	        {
	            return false;
	        }
	        if (getRelatedObject() != null && getRelatedObject().equals(refObj.getRelatedObject()))
	        {
	            return false;
	        }

	        return true;
	    }

	    @Override
	    public int hashCode()
	    {
	        int result = 7;
	        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
	        result = 31 * result + (getRelatedObject() != null ? getRelatedObject().hashCode() : 0);
	        result = 31 * result + (getRelatingObject() != null ? getRelatingObject().hashCode() : 0);
	        return result;
	    }		
		
	}
}
