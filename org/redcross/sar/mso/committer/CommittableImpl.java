package org.redcross.sar.mso.committer;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.data.IMsoObjectIf;

import java.util.Calendar;

/**
 * Base class for managing committable objects
 */
public abstract class CommittableImpl implements ICommittableIf
{
    public final Calendar m_timestamp;
    public CommitManager.CommitType m_commitType;


    public CommittableImpl(CommitManager.CommitType aCommitType)
    {
        m_commitType = aCommitType;
        m_timestamp = Calendar.getInstance();
    }

    public CommitManager.CommitType getType()
    {
        return m_commitType;
    }


    /**
     * Class for committable MsoObjects.
     */
    public static class CommitObject extends CommittableImpl implements ICommitObjectIf
    {
        public final IMsoObjectIf m_object;

        public CommitObject(IMsoObjectIf anObject, CommitManager.CommitType aCommitType)
        {
            super(aCommitType);
            m_object = anObject;
        }

        public IMsoObjectIf getObject()
        {
            return m_object;
        }
    }

    /**
     * Class for committable relations.
     * A relation is defined by a relation name and two MsoObjects.
     * The name is needed since some classes may have several types of relations to another class.
     */
    public static class CommitReference extends CommittableImpl implements ICommitReferenceIf
    {
        public final String m_name;
        public final IMsoObjectIf m_referringObject;
        public final IMsoObjectIf m_referredObject;


        public CommitReference(String aName, IMsoObjectIf theReferringObject, IMsoObjectIf theReferredObject, CommitManager.CommitType aCommitType)
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
