package org.redcross.sar.gui.field;

import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.thread.event.IWorkListener;

public interface IDiskoField extends IChangeable { 

	public String getName();

	public boolean isDirty();
	
	public boolean isChangeable();
	public void setChangeable(boolean isConsume);

	public boolean getAutoSave();
	public void setAutoSave(boolean autoSave);	
	
	public String getCaptionText();	
	public void setCaptionText(String text);
	
	public int getFixedCaptionWidth();
	public void setFixedCaptionWidth(int width);
	
	public int getFixedHeight();
	public void setFixedHeight(int height);
	
	public boolean isEditable();
	public void setEditable(boolean isEditable);
	
	public Object getValue();		
	public boolean setValue(Object value);
	
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
	public boolean cancel();
	public boolean finish();
	
	public void addWorkListener(IWorkListener listener);
	public void removeWorkListener(IWorkListener listener);
	
	public boolean addButtonActionListener(ActionListener listener);	
	public boolean removeButtonActionListener(ActionListener listener);
	
}
