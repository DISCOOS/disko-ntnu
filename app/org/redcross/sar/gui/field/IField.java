package org.redcross.sar.gui.field;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.event.IFieldListener;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.work.event.IFlowListener;

public interface IField<V> extends IChangeable {

	public String getName();

	public List<IChangeIf> getChanges();
	
	/**
	 * Get the first MSO attribute
	 */
	public IMsoAttributeIf<?> getMsoAttribute();
	public boolean setMsoAttribute(IMsoAttributeIf<?> attribute);
	
	public void clearModel();
	public void clearModel(Object newEditValue);
	
	/**
	 * Add a field listener
	 * @param listener - the listener
	 */
	public void addFieldListener(IFieldListener listener);

	/**
	 * Remove a field listener
	 * @param listener - the listener
	 */
	public void removeFieldListener(IFieldListener listener);	
	
	public boolean isChangeable();
	public void setChangeable(boolean isConsume);

	/**
	 * Check if buffered changes exists.
	 * 
	 * @see Overrided method {@code IChangeable:isDirty()}
	 * 
	 * @return Returns <code>true</code> if buffered changes exists.
	 */
	public boolean isDirty();
	
	/**
	 * Force a given dirty state. Note that if buffered changes exists,
	 * and the new dirty state is set to {@code false}, changes will be 
	 * lost (a rollback is performed)
	 * 
	 * @see Overrided method {@code IChangeable:setDirty(boolean isDirty)}
	 * @param isDirty - the new dirty state.
	 */	
	public void setDirty(boolean isDirty);
	
	/**
	 * Check if field is in buffer mode. </p>
	 * 
	 * If the field is in buffer mode, changes are buffered 
	 * instead of being passed to the field model. Changes are 
	 * forwarded to the model by invoking <code>finish()</code>, 
	 * or discarded by invoking <code>cancel()</code>. </p>
	 * 
	 * @return Returns <code>true</code> if in buffer mode.
	 */
	public boolean isBufferMode();

	/**
	 * Set field buffer mode. </p>
	 * 
	 * If the field is in buffer mode, changes are buffered 
	 * instead of being passed to the field model. Changes are 
	 * forwarded to the model by invoking <code>finish()</code>, 
	 * or discarded by invoking <code>cancel()</code>. </p>
	 * 
	 */
	public void setBufferMode(boolean isBufferMode);

	public String getCaptionText();
	public void setCaptionText(String text);

	public int getFixedCaptionWidth();
	public void setFixedCaptionWidth(int width);

	public int getFixedHeight();
	public void setFixedHeight(int height);

	/**
	 * Get editable state. 
	 * 
	 * @return Returns <code>true</code> as long as the internal counter is greater than zero.
	 * 
	 * @see <code>setEditable</code> and <code>clearEditable</code>
	 */	
	public boolean isEditable();
	
	/**
	 * Set field editable state. This state remembers each time the 
	 * editable state is set or reset using an internal counter. The 
	 * editable state is only reset to <code>true</code> when this 
	 * internal counter returns to zero. Hence, editable state only 
	 * change when the internal counter is less than 2 (0 -> 1 := 
	 * editable is <code>false</code>, 1 -> 0 := editable is 
	 * <code>true</code>).
	 * 
	 * @return Returns <code>false</code> as long as internal counter 
	 * is greater than zero, <code>true</code> otherwise. 
	 */	
	public void setEditable(boolean isEditable);
	
	/**
	 * Reset state to editable by resetting the internal counter to zero. 
	 * 
	 * @return the internal counter value  
	 */
	public int clearEditableCount();

	/**
	 * Get current field model value.  
	 * 
	 * @see <code>IFieldModel::getValue()</code> - The value returned by the IFieldModel depends on the 
	 * data origin and state.
	 * 
	 * @return Returns current field value
	 */
	public V getValue();
	
	/**
	 * Set current field model value.
	 * @param value - new field value 
	 * @return Returns <code>true</code> if field value was changed.
	 */
	public boolean setValue(Object value);
	
	/**
	 * Set current field model value. 
	 * @param value - new field value 
	 * @param notify - if <code>true</code>, flow listeners are notified if value was changed 
	 * @return Returns <code>true</code> if field value was changed.
	 */
	public boolean setValue(Object value, boolean notify);
	
	/**
	 * Get current field edit value. The method {@code getValue()} and this method
	 * returns the same value as long as {@code isDirty()} is {@code false}.
	 * 
	 * @see {@code isBufferMode()} and {@code isDirty}
	 * @return Returns current field edit value
	 */
	public V getEditValue();
	
	/**
	 * Set current field edit value.
	 * @param value - new field edit value 
	 * @return Returns <code>true</code> is field edit value was changed.
	 */
	public boolean setEditValue(Object value);
	
	public boolean isButtonVisible();
	public void setButtonVisible(boolean isVisible);

	public boolean isButtonEnabled();
	public void setButtonEnabled(boolean isEnabled);

	public String getButtonText();
	public void setButtonText(String text);

	public Icon getButtonIcon();
	public void setButtonIcon(Icon icon);

	public String getButtonTooltipText();
	public void setButtonTooltipText(String text);

	public String getButtonCommand();
	public void setButtonCommand(String name);

	public String getToolTipText();
	public void setToolTipText(String text);
	
	/**
	 * Get field model instance.
	 * 
	 * @return Returns a <code>IFieldModel</code> instance.
	 */
	public IFieldModel<V> getModel();
	
	/**
	 * Set the field model instance. The instance can not be <code>null</code>. 
	 * @param model - the new model instance
	 */
	public void setModel(IFieldModel<V> model);

	public void reset();
	public boolean cancel();
	public boolean finish();

	/**
	 * Parse field model for changes and update IField accordingly. If field model
	 * is changed, <code>IFieldModel::isChanged</code> is <code>true</code>, and field 
	 * is updated to reflect the changes accordingly. Otherwise, <code>parse()</code> does
	 * nothing. 
	 */
	public void parse();
	
	/** 
	 * Check if field model is changed locally or remotely since
	 * last time <code>parse()</code> was invoked.
	 * 
	 * @return Returns <code>true</code> if a <code>parse()</code> is required.
	 */
	public boolean isChanged();	
	
	public void addFlowListener(IFlowListener listener);
	public void removeFlowListener(IFlowListener listener);

	public boolean addButtonActionListener(ActionListener listener);
	public boolean removeButtonActionListener(ActionListener listener);

}
