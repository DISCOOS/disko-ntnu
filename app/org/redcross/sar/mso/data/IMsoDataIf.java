package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;

public interface IMsoDataIf extends IData {

    public enum MsoDataType {
		/**
		 * Implements the primitive Boolean data type
		 */
		BOOLEAN,
		/**
		 * Implements the primitive Integer data type
		 */ 
		INTEGER,
		/**
		 * Implements the primitive Double data type
		 */
		DOUBLE,
		/**
		 * Implements the primitive String data type
		 */
		STRING,
		/**
		 * Implements the primitive Calendar data type
		 */
		CALENDAR,
		/**
		 * Implements the any enum data type
		 */
		ENUM,
		/**
		 * Implements the IGeoListIf data type
		 */
		GEOLIST,
		/**
		 * Implements the IGeodataIf data type
		 */
		POSITION,
		/**
		 * Implements the IGeodataIf data type
		 */
		TIMEPOS,
		/**
		 * Implements the IGeodataIf data type
		 */
		POLYGON,
		/**
		 * Implements the IGeodataIf data type
		 */
		ROUTE,
		/**
		 * Implements the IGeodataIf data type
		 */
		TRACK,
		/**
		 * Implements a one-to-one relation between two IMsoObjectIf data types
		 */
		ONTO_RELATION,
		/**
		 * Implements a one-to-many relation between IMsoObjectIf data types
		 */
		MANY_RELATION,
		/**
		 * Implements a IMsoObjectIf data type
		 */
		OBJECT
	}

	/**
     * Check if all data is in a LOCAL origin 
     *
     * @return Returns <code>true</code> if all data are in a LOCAL origin. 
     */
    public boolean isOriginLocal();
    
    /**
     * CCheck if all data is in a REMOTE origin
     *
     * @return Returns <code>true</code> if all data are in a REMOTE origin. 
     */
    public boolean isOriginRemote();
    
    /**
     * Check if data is in conflict (same data has two origins)
     *
     * @return Returns <code>true</code> if all data are in CONFLICT. 
     */
    public boolean isOriginConflict();
    
    /**
     * Check if data is in more than one origin
     *
     * @return Returns <code>true</code> if data is in more than one origin 
     */
    public boolean isOriginMixed();
    
        
    /**
     * Get current data origin
     *  
     * @return Returns current data origin.
     */    
    public IData.DataOrigin getOrigin();
    
    /**
     * Check for given data origin 
     * @param origin - the origin to match
     * @return Returns <code>true</code> if origins match.
     */
    public boolean isOrigin(IData.DataOrigin origin);
    
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
    
    /**
     * Check the data loopback mode. </p>
     * 
     * <i>This flag is only true if all data objects are in
     * LOOPBACK mode</i>.</p>
     *
     * @return Returns <code>true</code> if all data objects are 
     * in LOOPBACK state. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Check the data rollback mode. </p>
     * 
     * <i>This flag is only true if all data objects are in
     * ROLLBACK mode</i>.</p>
     *
     * @return Returns <code>true</code> if all data objects are 
     * in ROLLBACK state. 
     */
    public boolean isRollbackMode();    
        
    /**
     * Check if data is deleted from the model.
     *
     * @return  <code>true<code> if the object has been deleted.
     */
    public boolean isDeleted();    
    
	/**
	 * Get current data state
	 * 
	 * @return DataState
	 */
	public DataState getState();
	
	/**
	 * Check for given data state
	 * 
	 * @return boolean
	 */
	public boolean isState(DataState state);
	
	/**
	 * Get the data type .
	 * 
	 * @return Returns enumeration value identifying the data
	 */
	public MsoDataType getDataType();
	
	
	/**
	 * Get data object id.
	 * 
	 * @return Returns data object id.
	 */
	public String getObjectId();
	

}
