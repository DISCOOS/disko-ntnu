package org.redcross.sar.mso;

import java.util.List;
import java.util.Set;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.CommitException;

public interface ICommitManagerIf {

    /**
     * Returns pending updates
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates();
    
    /**
     * Returns pending updates of specific class
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(MsoClassCode of);
    
    /**
     * Returns pending updates of specific classes
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(Set<MsoClassCode> of);
    
    /**
     * Returns pending update holder for specific object
     * <p/>
     */
    public IUpdateHolderIf getUpdates(IMsoObjectIf of);	
    
    /**
     * Returns pending updates of specific objects
     * <p/>
     */
    public List<IUpdateHolderIf> getUpdates(List<IMsoObjectIf> of);	
    
    /**
     * Perform commit.
     * <p/>
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.CommitException when the commit fails
     */
    public void commit() throws CommitException;

    /**
     * Perform a partial commit
     * <p/>
     * @param List<UpdateHolder> updates - holder for updates
     * @throws org.redcross.sar.util.except.CommitException when the commit fails
     */
    public void commit(List<IUpdateHolderIf> updates) throws CommitException;
    
    /**
     * Perform rollback.
     * <p/>
     * Clears all accumulated information.
     */
    public void rollback();

    /**
     * Tell if some uncommited changes exist
     *
     * @return true if uncommited changes exist
     */
    public boolean hasUncommitedChanges();
    
    public boolean hasUncommitedChanges(MsoClassCode code);
    
    public boolean hasUncommitedChanges(IMsoObjectIf msoObj);
    
}
