package org.redcross.sar.mso;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoReferenceIf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Base class for managing change objects
 */
@SuppressWarnings("unchecked")
public abstract class ChangeImpl implements IChangeIf
{
	public final Calendar m_timestamp;
	public ChangeType m_changeType;


	public ChangeImpl(ChangeType aChangeType)
	{
		m_changeType = aChangeType;
		m_timestamp = Calendar.getInstance();
	}

	public ChangeType getType()
	{
		return m_changeType;
	}


	/**
	 * Class for changed MsoObjects.
	 */
	public static class ChangeObject extends ChangeImpl implements IChangeObjectIf
	{
		private final IMsoObjectIf m_object;
		private final List<IChangeIf> m_partial;

		public ChangeObject(IMsoObjectIf anObject, ChangeType aCommitType, List<IChangeIf> partial)
		{
			super(aCommitType);
			m_object = anObject;
			m_partial = partial!=null ? partial : new ArrayList<IChangeIf>(0);
		}

		@Override
		public IMsoObjectIf getMsoObject()
		{
			return m_object;
		}

		@Override
		public List<IChangeIf> getChanges() {
			return m_partial;
		}

		@Override
		public boolean isFiltered() {
			return (m_partial.size()>0);
		}

		@Override
		public boolean containsFilter(IChangeIf filter) {
			if(isFiltered()) {
				return m_partial.contains(filter);
			}
			return false;
		}

		@Override
		public boolean containsFilter(IMsoAttributeIf<?> attribute) {
			if(isFiltered()) {
				for(IChangeIf it : m_partial) {
					if(it instanceof IChangeAttributeIf) {
						IChangeAttributeIf attr = (IChangeAttributeIf)it;
						if(attr.getMsoAttribute()==attribute) return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean containsFilter(IMsoObjectIf reference) {
			if(isFiltered()) {
				for(IChangeIf it : m_partial) {
					if(it instanceof IChangeReferenceIf) {
						IChangeReferenceIf refObj = (IChangeReferenceIf)it;
						if(refObj.getReferredObject()==reference) return true;
					}
				}
			}
			return false;
		}

	}

	public static class ChangeAttribute extends ChangeImpl implements IChangeAttributeIf
	{
		private final IMsoAttributeIf<?> m_attr;

		public ChangeAttribute(IMsoAttributeIf<?> theAttribute, ChangeType aCommitType)
		{
			super(aCommitType);
			m_attr = theAttribute;
		}

		@Override
		public String getName() {
			return m_attr.getName();
		}

		@Override
		public IMsoAttributeIf<?> getMsoAttribute() {
			return m_attr;
		}

		@Override
		public IMsoObjectIf getOwner() {
			return m_attr.getOwner();
		}


	}

	/**
	 * Class for storing changed references.
	 * A relation is defined by a relation name and two MsoObjects.
	 * The name is needed since some classes may have several types of relations to another class.
	 */
	public static class ChangeReference extends ChangeImpl implements IChangeReferenceIf
	{
		private final IMsoObjectIf m_referredObj;
		private final IMsoReferenceIf<?> m_refObj;

		public ChangeReference(IMsoReferenceIf<?> aReference, IMsoObjectIf theReferredObj, ChangeType aChangeType)
		{
			super(aChangeType);
			m_refObj = aReference;
			m_referredObj = theReferredObj;
		}
		
	    @Override
		public String getName()
		{
			return m_refObj.getName();
		}

	    @Override
		public IMsoObjectIf getReferringObject()
		{
			return m_refObj.getOwner();
		}

	    @Override
		public IMsoObjectIf getReferredObject()
		{
			return m_referredObj;
		}
					
	    @Override
		public IMsoReferenceIf<?> getReference() {
			return m_refObj;
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

	        ChangeReference refObj = (ChangeReference) o;

	        if (getName() != null && !getName().equals(refObj.getName()))
	        {
	            return false;
	        }
	        if (getReferredObject() != null && getReferredObject().equals(refObj.getReferredObject()))
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
	        result = 31 * result + (getReferredObject() != null ? getReferredObject().hashCode() : 0);
	        result = 31 * result + (getReferringObject() != null ? getReferringObject().hashCode() : 0);
	        return result;
	    }		
		
	}
}
