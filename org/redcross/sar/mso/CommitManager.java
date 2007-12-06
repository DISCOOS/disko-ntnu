package org.redcross.sar.mso;

import org.redcross.sar.mso.committer.CommitWrapper;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
import org.redcross.sar.mso.data.AbstractMsoObject;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.except.CommitException;

import java.util.Vector;

/**
 * The purpose of the commit manager is to catch Server update events, accumulate them, and when a commit is executed,
 * fire {@link org.redcross.sar.mso.event.MsoEvent.Commit} events.
 * The event provides access to MSO data structures that shall be committed  by passing a {@link org.redcross.sar.mso.committer.ICommitWrapperIf} object
 * to the listeners.
 */
public class CommitManager
{
    /**
     * Types of commit depending on what has happened to the object/relation.
     */
    public enum CommitType
    {
        COMMIT_CREATED,
        COMMIT_MODIFIED,
        COMMIT_DELETED
    }

    /**
     * Reference to the owning MsoModel
     */
    private final IMsoModelIf m_ownerModel;

    /**
     * Vector for accumulating {@link UpdateHolder} objects that tell which objects that shall be commited, and their update types.
     */
    private final Vector<UpdateHolder> m_updates = new Vector<UpdateHolder>(50);

    /**
     * @param theModel Reference to the singleton MSO model object holding the MsoModel object.
     */
    public CommitManager(IMsoModelIf theModel)
    {
        m_ownerModel = theModel;
        m_ownerModel.getEventManager().addServerUpdateListener(new IMsoUpdateListenerIf()
        {
            public void handleMsoUpdateEvent(MsoEvent.Update e)
            {
                registerUpdate((AbstractMsoObject) e.getSource(), e.getEventTypeMask());
            }

            public boolean hasInterestIn(IMsoObjectIf aSource)
            {
                return true;
            }

        });
    }

    public IMsoModelIf getMsoModel()
    {
        return m_ownerModel;
    }

    private ICommitWrapperIf createCommitWrapper()
    {
        CommitWrapper wrapper = new CommitWrapper();
        for (UpdateHolder updateItem : m_updates)
        {
            wrapper.add(updateItem.m_object, updateItem.m_mask);
        }
        m_updates.clear();
        return wrapper;
    }

    private void registerUpdate(AbstractMsoObject anObject, int aMask)
    {
        for (UpdateHolder updateItem : m_updates)
        {
            if (updateItem.m_object.getObjectId().equals(anObject.getObjectId()))
            {
                updateItem.applyMask(aMask);
                return;
            }
        }
        m_updates.add(new UpdateHolder(anObject, aMask));
    }

    /**
     * Perform commit.
     * <p/>
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.CommitException when the commit fails
     */
    public void commit() throws CommitException
    {
        m_ownerModel.getEventManager().notifyCommit(createCommitWrapper());
    }

    /**
     * Perform rollback.
     * <p/>
     * Clears all accumulated information.
     */
    public void rollback()
    {
        m_updates.clear();
    }

    /**
     * Tell if some uncommited changes exist
     *
     * @return true if uncommited changes exist
     */
    public boolean hasUncommitedChanges()
    {
        return m_updates.size() > 0;
    }

    private class UpdateHolder
    {
        final IMsoObjectIf m_object;
        int m_mask;

        UpdateHolder(IMsoObjectIf anObject, int aMask)
        {
            m_object = anObject;
            m_mask = aMask;
        }

        void applyMask(int aMask)
        {
            m_mask |= aMask;
        }
    }

}
