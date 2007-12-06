package org.redcross.sar.mso.committer;

import java.util.List;

/**
 * The ICommitWrapperIf interface defines methods that shall be used by the commit handler in order to retrieve the objects
 * and references that shall be committed.
 */
public interface ICommitWrapperIf
{
    /**
     * Get a list of committable objects.
     * @return The list
     */
    public List<ICommittableIf.ICommitObjectIf> getObjects();

    /**
     * Get a list of committable one-to-one references.
     * @return The list
     */
    public List<ICommittableIf.ICommitReferenceIf> getAttributeReferences();

    /**
     * Get a list of committable one-to-many references.
     * @return The list
     */
    public List<ICommittableIf.ICommitReferenceIf> getListReferences();

}
