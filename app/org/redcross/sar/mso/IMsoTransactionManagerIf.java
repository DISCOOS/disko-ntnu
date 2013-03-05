package org.redcross.sar.mso;

import java.util.List;
import java.util.Set;

import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.TransactionException;

public interface IMsoTransactionManagerIf {

    /**
     * Returns pending changes
     * <p/>
     */
    public List<IChangeRecordIf> getChanges();
    
    /**
     * Returns pending updates of specific class
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(MsoClassCode of);
    
    /**
     * Returns pending updates of specific classes
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(Set<MsoClassCode> of);
    
    /**
     * Returns pending update holder for specific object
     * <p/>
     */
    public IChangeRecordIf getChanges(IMsoObjectIf of);	
    
    /**
     * Returns pending updates of specific objects
     * <p/>
     */
    public List<IChangeRecordIf> getChanges(List<IMsoObjectIf> of);	
    
    /**
     * Perform a commit of all changes.
     * <p/>
     * Generates a {@link org.redcross.sar.mso.event.MsoEvent.Commit} event.
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit() throws TransactionException;

    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeRecordIf} for more information.
     * 
     * @param UpdateHolder updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit(IChangeRecordIf changes) throws TransactionException;
    
    /**
     * Perform a commit on a subset of all changes<p/>
     * 
     * Note that partial commits (attributes only) is only possible to perform on objects 
     * that exists remotely (modified). If a IChangeSourceIf is marked for partial commit, object references 
     * and list references are not affected, only the marked attributes. See 
     * {@link org.redcross.sar.mso.IChangeRecordIf} for more information.
     * 
     * @param List<UpdateHolder> updates - list of holders of updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void commit(List<IChangeRecordIf> changes) throws TransactionException;
    
    /**
     * Performs a rollback of all changes. <p/>
     * 
     * Clears all accumulated information.
     */
    public void rollback() throws TransactionException;
    
    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param UpdateHolder updates - holder for updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(IChangeRecordIf changes) throws TransactionException;    

    /**
     * Perform a rollback on a subset of all changes<p/>
     * 
     * @param List<UpdateHolder> updates - list of holders of updates
     * @throws org.redcross.sar.util.except.TransactionException when the commit fails
     */
    public void rollback(List<IChangeRecordIf> changes) throws TransactionException;    
    
    /**
     * Tell if some uncommitted changes exist
     *
     * @return true if uncommitted changes exist
     */
    public boolean isChanged();
    
    /**
     * Tell if some uncommitted changes exist for the given class code
     *
     * @return true if uncommitted changes exist
     */
    public boolean isChanged(MsoClassCode code);
    
    /**
     * Tell if some uncommitted changes exist for the given object
     *
     * @return true if uncommitted changes exist
     */
    public boolean isChanged(IMsoObjectIf msoObj);
    
}
