package org.redcross.sar.mso.committer;

import org.redcross.sar.mso.CommitManager;
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
