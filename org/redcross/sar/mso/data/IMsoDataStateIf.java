package org.redcross.sar.mso.data;

public interface IMsoDataStateIf {

    /**
     * Check for local data update state 
     *
     * @return <code>true</code> if all data are in LOCAL state. 
     */
    public boolean isLocalState();
    
    /**
     * Test for remote data update state
     *
     * @return <code>true</code> if all data are in REMOTE state. 
     */
    public boolean isRemoteState();
    
    /**
     * Check for conflict data update state
     *
     * @return <code>true</code> if all data are in CONFLICT mode. 
     */
    public boolean isConflictState();
    
    /**
     * Check for loopback data update state
     *
     * @return <code>true</code> if all data are in LOOPBACK state. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Check for rollback data update state
     *
     * @return <code>true</code> if all data are in ROLLBACK state. 
     */
    public boolean isRollbackMode();    
    
    /**
     * Check for mixed data update state
     *
     * @return <code>true</code> if data has more than one update mode 
     */
    public boolean isMixedState();
    
    /**
     * Get change status
     *
     * @return <code>true</code> if local changes exists (data is in LOCAL or CONFLICT state). 
     */
    public boolean isChanged();
    
    /**
     * Get change status
     *
     * @return <code>true</code> if any change has occurred 
     */
    public boolean isChangedSince(int changeCount);
    
    /**
     * Get change count </p>
     * 
     * Use this counter when tracking changes executed on a object. 
     *
     * @return The number of changes performed on the object since it's construction.
     */
    public int getChangeCount();    
	
}
