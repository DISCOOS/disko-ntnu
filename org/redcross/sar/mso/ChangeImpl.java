package org.redcross.sar.mso;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

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


    public ChangeImpl(ChangeType aCommitType)
    {
        m_changeType = aCommitType;
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
    	private final List<IAttributeIf<?>> m_partial;

        public ChangeObject(IMsoObjectIf anObject, ChangeType aCommitType, List<IAttributeIf<?>> partial)
        {
            super(aCommitType);
            m_object = anObject;
            m_partial = partial!=null ? partial : new ArrayList<IAttributeIf<?>>(0);
        }

        public IMsoObjectIf getObject()
        {
            return m_object;
        }

		public List<IAttributeIf<?>> getPartial() {
			return m_partial;
		}

		public boolean isPartial() {
			return (m_partial.size()>0);
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

        public ChangeReference(String aName, IMsoObjectIf theReferringObject, IMsoObjectIf theReferredObject, ChangeType aCommitType)
        {
            super(aCommitType);
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
