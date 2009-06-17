package org.redcross.sar.gui.field;

import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.gui.event.IFieldModelListener;

/**
 * This interface is used by IField instances to get and set the field value.
 * 
 * @author kenneth
 *
 */
public interface IFieldModel<V> { 
	
	/**
	 * Get model source if exists
	 * 
	 * @return Returns the model source
	 */
	public Object getSource();
	
	/**
	 * Checks if model is bound to given source
	 * 
	 * @param source
	 * @return Returns <code>true</code> if model is bound to given source
	 */
	public boolean isBoundTo(Object source);
	
	/** 
	 * Check if data has been changed (locally or remotely) since 
	 * last time <code>getValue()</code> was invoked. 
	 *
	 * @return boolean
	 */
	public boolean isChanged();
	
	/**
	 * Check if data has changed since given change count
	 * 
	 * @param changeCount - the change count to test for
	 * 
	 * @return Returns <code>true</code> if changed since given change count
	 */
	public boolean isChangedSince(int changeCount);
	
	/**
	 * Get field model value from data source.</p>
	 * 
	 * If data origin is LOCAL or CONFLICT, the value is retrieved 
	 * from the local source. If the data origin is REMOTE, the vale
	 * is retrieved from the remote source.</p>
	 * 
	 * <i>The mapping of data items to value is implementation dependent</i>.
	 * 
	 */
	public V getValue();
	
	/**
	 * Set local field model value.</p>
	 * 
	 * If data origin is LOCAL or CONFLICT, the value is retrieved 
	 * from the local source. If the data origin is REMOTE, the vale
	 * is retrieved from the remote source.</p>
	 * 
	 * <i>The mapping of data items to value is implementation dependent</i>.
	 * 
	 * @param value
	 */
	public void setLocalValue(V value);
	
	/**
	 * Get current data origin
	 * 
	 * @return DataOrigin
	 */
	public DataOrigin getOrigin();
	
	/**
	 * Check for given data origin
	 * 
	 * @return boolean
	 */
	public boolean isOrigin(DataOrigin origin);
	
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
	 * Get local value (from local source)
	 */
	public V getLocalValue();
	
	/**
	 * Get remote value (from remote source)
	 */
	public V getRemoteValue();
	
	/**
	 * Accepts the remote value if data state is CONFLICT  
	 * (replaces local value with remote value).
	 * @return Returns <code>true</code> if anything is changed.
	 */
	public boolean acceptRemoteValue();
	
	/**
	 * Accepts the local value if data state is CONFLICT  
	 * (replaces local value with remote value).
	 * @return Returns <code>true</code> if anything is changed.
	 */
	public boolean acceptLocalValue();
	
	/**
	 * Reset internal counters and states. This method force a 
	 * full update of the field model from source(s) and notify and 
	 * listeners of this update with all flags set.     
	 */
	public void reset();
	
	/**
	 * Analyze data source(s), update field model and notify listeners if 
	 * source is changed.
	 */
	public void parse();
	
	/**
	 * commit local value to remote source
	 * 
	 * @return Returns <code>true</code> if local changes exists.
	 */
	public boolean commit();	
	
	/**
	 * Rollback local value to remote value 
	 * 
	 * @return Returns <code>true</code> if local changes exists.
	 */
	public boolean rollback();	
	
	/**
	 * Add a IFieldModelListener to the model
	 * @param listener - the listener to add
	 */
	public void addFieldModelListener(IFieldModelListener listener);

	/**
	 * Remove a IFieldModelListener to the model
	 * @param listener - the listener to remove
	 */
	public void removeFieldModelListener(IFieldModelListener listener);
	
}
