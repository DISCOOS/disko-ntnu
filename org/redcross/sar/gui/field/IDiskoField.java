package org.redcross.sar.gui.field;

import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.work.event.IWorkFlowListener;

public interface IDiskoField extends IChangeable, IMsoField {

	public String getName();

	public boolean isMsoField();
	public IMsoAttributeIf<?> getMsoAttribute();
	public IMsoAttributeIf<?> clearMsoAttribute();
	public boolean setMsoAttribute(IMsoAttributeIf<?> attribute);
	
	public boolean isDirty();
	public boolean isChangeable();
	public void setChangeable(boolean isConsume);

	public boolean isBatchMode();
	public void setBatchMode(boolean isBatchMode);

	public String getCaptionText();
	public void setCaptionText(String text);

	public int getFixedCaptionWidth();
	public void setFixedCaptionWidth(int width);

	public int getFixedHeight();
	public void setFixedHeight(int height);

	/**
	 * Get editable state. This returns <code>true</code> as long as 
	 * the internal counter is greater than zero.
	 * 
	 * @return boolean
	 * @see setEditable, resetEditable
	 */	
	public boolean isEditable();
	
	/**
	 * Set editable state. This state remembers each time the 
	 * editable state is set or reset using an internal counter. The 
	 * editable state is only reset when this internal counter is zero.
	 * 
	 * @return boolean
	 */	
	public void setEditable(boolean isEditable);
	
	/**
	 * Reset state to editable by resetting the internal counter to zero. 
	 * 
	 * @return the internal counter value  
	 */
	public int resetEditable();

	public Object getValue();
	public boolean setValue(Object value);
	public boolean setValue(Object value, boolean notify);
	
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

	public void reset();
	public void update();
	public boolean cancel();
	public boolean finish();

	public void addWorkFlowListener(IWorkFlowListener listener);
	public void removeWorkFlowListener(IWorkFlowListener listener);

	public boolean addButtonActionListener(ActionListener listener);
	public boolean removeButtonActionListener(ActionListener listener);

}
