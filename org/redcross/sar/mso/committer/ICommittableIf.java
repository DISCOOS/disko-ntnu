package org.redcross.sar.mso.committer;

import java.util.List;

import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * The ICommittableIf and subinterfaces define methods that used by the commit handler when commiting objects and references.
 */
public interface ICommittableIf
{
    /**
     * Get type of commit
     */
    public CommitManager.CommitType getType();
    
/**
 * Methods that used by the commit handler when commiting objects.
 */
    public interface ICommitObjectIf extends ICommittableIf
    {
        /**
        * Get the object to commit.
        */
        public IMsoObjectIf getObject();
        
        /**
         * Tells if only some attributes should be updated. Only 
         * possible if getType() is COMMIT_MODIFIED 
         */
        public boolean isPartial();
        
        /**
         * Returns partial list of attributes to comnitt Only 
         * possible if getType() is COMMIT_MODIFIED and isPartial()
         * is true 
         */
        public List<IAttributeIf> getPartial();
        
    }

/**
 * Methods that used by the commit handler when commiting references.
 */
    public interface ICommitReferenceIf extends ICommittableIf
    {
        /**
        * Get name of reference.
        */
        public String getReferenceName();

        /**
        * Get referring object.
        */
        public IMsoObjectIf getReferringObject();

        /**
        * Get referred object.
        */
        public IMsoObjectIf getReferredObject();
    }

}
