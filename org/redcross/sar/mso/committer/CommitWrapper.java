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
    /*
    public void add(IMsoObjectIf anObject, int aMask, List<String> attributes)
    {
    */
    	
    /**
     * Add an object to the wrapper.
     *
     * @param IUpdateHolderIf holder - The holder of the update information
     */
    public void add(IUpdateHolderIf anHolder)
    {
    	
    	// get information
    	IMsoObjectIf anObject = anHolder.getMsoObject();
    	
    	// is partial commit?
    	if(anHolder.isPartial()) {
    		// only schedule modified data
    		m_commitObjects.add(new CommittableImpl.CommitObject(anObject, 
    				CommitManager.CommitType.COMMIT_MODIFIED,anHolder.getPartial()));
    	}
    	else {    	
    		
	    	// full commit, get flags
	        boolean createdObject = anHolder.isCreated();
	        boolean deletedObject = anHolder.isDeleted();
	        boolean modifiedObject = anHolder.isModified();
	        boolean modifiedReference = anHolder.isReferenceChanged();
	
	        if (createdObject && deletedObject)
	        {
	            return;
	        }
	        if (createdObject)
	        {
	            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_CREATED,null));
	            m_commitAttributeReferences.addAll(anObject.getCommittableAttributeRelations());
	            m_commitListReferences.addAll(anObject.getCommittableListRelations());
	            return;
	        }
	        if (deletedObject)
	        {
	            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_DELETED,null));
	            return;
	        }
	        if (modifiedObject)
	        {
	            m_commitObjects.add(new CommittableImpl.CommitObject(anObject, CommitManager.CommitType.COMMIT_MODIFIED,null));
	        }
	        if (modifiedReference)
	        {
	            m_commitAttributeReferences.addAll(anObject.getCommittableAttributeRelations());
	            m_commitListReferences.addAll(anObject.getCommittableListRelations());
	        }
    	}	       
    }
}
