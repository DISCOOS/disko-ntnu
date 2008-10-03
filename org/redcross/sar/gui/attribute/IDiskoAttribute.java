package org.redcross.sar.gui.attribute;

import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.thread.event.IDiskoWorkListener;

public interface IDiskoAttribute { 

	public String getName();

	public boolean isDirty();
	
	public boolean isConsume();
	public void setConsume(boolean isConsume);

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
	
	public boolean load();
	public boolean save();
	
	public boolean isMsoAttribute();
	public IAttributeIf<?> getMsoAttribute();
	public boolean setMsoAttribute(IAttributeIf<?> attribute);
	
	public boolean addDiskoWorkListener(IDiskoWorkListener listener);
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener);
	
	public boolean addButtonActionListener(ActionListener listener);	
	public boolean removeButtonActionListener(ActionListener listener);
	
}
