package org.redcross.sar.mso.committer;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for passing committable objects and relations to the SARA modeldriver
 */
public class CommitWrapper implements ICommitWrapperIf
{
    private final ArrayList<ICommittableIf.ICommitObjectIf> m_commitObjects = new ArrayList<ICommittableIf.ICommitObjectIf>();
    private final ArrayList<ICommittableIf.ICommitReferenceIf> m_commitAttributeReferences = new ArrayList<ICommittableIf.ICommitReferenceIf>();
    private final ArrayList<ICommittableIf.ICommitReferenceIf> m_commitListReferences = new ArrayList<ICommittableIf.ICommitReferenceIf>();

    public List<ICommittableIf.ICommitObjectIf> getObjects()
    {
        return m_commitObjects;
    }

    public List<ICommittableIf.ICommitReferenceIf> getAttributeReferences()
    {
        return m_commitAttributeReferences;
    }

    public List<ICommittableIf.ICommitReferenceIf> getListReferences()
    {
        return m_commitListReferences;
    }

    /**
    * Add an object to the wrapper.
    *
    * @param anObject The modified object.
    * @param aMask A combination of {@link org.redcross.sar.mso.event.MsoEvent.EventType} values.
    */
    public void add(IMsoObjectIf anObject, int aMask)
    {
        boolean createdObject = (aMask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject = (aMask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (aMask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean modifiedReference = (aMask & (MsoEvent.EventType.MODIFIED_REFERENCE_EVENT.maskValue() |
                MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue() |
                MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue())) != 0;

        if (createdObject && deletedObject)
        {
            return;
        }
        if (createdObject)
        {
            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_CREATED));
            m_commitAttributeReferences.addAll(anObject.getCommittableAttributeRelations());
            m_commitListReferences.addAll(anObject.getCommittableListRelations());
            return;
        }
        if (deletedObject)
        {
            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_DELETED));
            return;
        }
        if (modifiedObject)
        {
            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_MODIFIED));
        }
        if (modifiedReference)
        {
            m_commitAttributeReferences.addAll(anObject.getCommittableAttributeRelations());
            m_commitListReferences.addAll(anObject.getCommittableListRelations());
        }
    }
}
