package org.redcross.sar.data;

public interface IData extends Comparable<IData> {
	
	/**
	 * Enumeration of possible data origins. </p>
	 * 
	 * The enumeration is used to codify the origin of data.</p>
	 *  
	 * @author vinjar, kenneth
	 *
	 */
	public enum DataOrigin {
		/**
		 * The NONE data origin codify that no 
		 * data source exists. 
		 */
		NONE,
		/**
		 * The LOCAL data origin codify that the last 
		 * change of data was made by a local source. 
		 */
		LOCAL,
		/**
		 * The REMOTE data origin codify that the last 
		 * change of data was made by a remote source. 
		 */
		REMOTE,
		/**
		 * The CONFLICT data origin codify that the last
		 * change made by the local source, is in conflict
		 * with a new change made by a remote source.  
		 */
		CONFLICT,
		/**
		 * The MIXED data origin codify that the object 
		 * contains data with different origins. For instance,
		 * one data object may have a LOCAL origin, an another
		 * may have a REMOTE origin.
		 */
		MIXED
	}

	/**
	 * Enumeration of possible data states.</p>
	 * 
	 * 
	 * @author kenneth
	 *
	 */
	public enum DataState {
		/**
		 * The NONE data state codify that the data 
		 * source no longer exists. Hence, no origin exists. The origin 
		 * is therefore NONE.  
		 */
		NONE,
		/**
		 * The CHANGE data state codify that the data 
		 * is changed locally. Hence, the origin 
		 * is therefore LOCAL.
		 */
		CHANGED,
		/**
		 * The LOOPBACK data state codify the 
		 * acknowledgment from a server of a commit of local data. 
		 * Hence, the data origin is REMOTE.
		 */
		LOOPBACK,
		/**
		 * The ROLLBACK data state codify that data 
		 * is rolled back to that of the the remote origin. 
		 * Hence, the data origin is REMOTE.
		 */		
		ROLLBACK,
		/**
		 * The CONFLICT data state codify that data at 
		 * the local and remote origins are in conflict.
		 * Hence, the data origin is CONFLICT.
		 */		
		CONFLICT,
		/**
		 * The DELETED data state codify that data is 
		 * deleted, locally or remotely. If data is deleted
		 * by a local source, the origin is LOCAL. Likewise,
		 * if the data is deleted by a remote source, the 
		 * origin is REMOTE. 
		 */		
		DELETED,
		/**
		 * The MIXED data state codify that the data is 
		 * in more than one state or origin. The following combinations
		 * are possible. It the MIXED data state is returned, this implies 
		 * that data is represented by more than on object. 
		 */		
		MIXED
	}
	
	/**
	 * Get the data type. Use this method to 
	 * determine the data type.
	 * 
	 * @return Returns the data type
	 */
	public Enum<?> getDataType();

	/**
	 * Get the class code. Use this method to
	 * determine the object type.
	 * 
	 * @return Returns the class code
	 */
	public Enum<?> getClassCode();
	
}
