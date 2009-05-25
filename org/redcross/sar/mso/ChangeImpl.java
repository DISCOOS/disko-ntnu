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
		public List<IChangeIf> getPartial() {
			return m_partial;
		}

		@Override
		public boolean isPartial() {
			return (m_partial.size()>0);
		}

		@Override
		public boolean containsPartial(IChangeIf partial) {
			if(isPartial()) {
				return m_partial.contains(partial);
			}
			return false;
		}

		@Override
		public boolean containsPartial(IMsoAttributeIf<?> attribute) {
			if(isPartial()) {
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
		public boolean containsPartial(IMsoObjectIf reference) {
			if(isPartial()) {
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
	 * Class for changed relations.
	 * A relation is defined by a relation name and two MsoObjects.
	 * The name is needed since some classes may have several types of relations to another class.
	 */
	public static class ChangeReference extends ChangeImpl implements IChangeReferenceIf
	{
		private final String m_name;
		private final IMsoObjectIf m_referringObject;
		private final IMsoObjectIf m_referredObject;

		public ChangeReference(IMsoReferenceIf<?> theReference, ChangeType aChangeType)
		{
			this(theReference.getName(),theReference.getOwner(),theReference.getReference(),aChangeType);
		}
		
		public ChangeReference(String aName, IMsoObjectIf theReferringObject, IMsoObjectIf theReferredObject, ChangeType aChangeType)
		{
			super(aChangeType);
			m_name = aName;
			m_referringObject = theReferringObject;
			m_referredObject = theReferredObject;
		}

		public String getReferenceName()
		{
			return m_name;
		}

		public IMsoObjectIf getReferringObject()
		{
			return m_referringObject;
		}

		public IMsoObjectIf getReferredObject()
		{
			return m_referredObject;
		}

	}
}
