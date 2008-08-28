package org.redcross.sar.gui.attribute;

import javax.swing.AbstractButton;

import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.thread.event.IDiskoWorkListener;

public interface IDiskoAttribute { 

	public String getName();

	public boolean isDirty();
	
	public boolean isConsume();
	public void setConsume(boolean isConsume);

	public boolean getAutoSave();
	public void setAutoSave(boolean autoSave);	
	
	public String getCaption();	
	public void setCaption(String text);
	
	public int getCaptionWidth();
	public void setCaptionWidth(int width);
	
	public int getMaximumHeight();
	public void setMaximumHeight(int height);
	
	public boolean isEditable();
	public void setEditable(boolean isEditable);
	
	public Object getValue();		
	public boolean setValue(Object value);
	
	public AbstractButton getButton();
	
	public boolean load();
	public boolean save();
	
	public boolean isMsoAttribute();
	public IAttributeIf<?> getMsoAttribute();
	public boolean setMsoAttribute(IAttributeIf<?> attribute);
	
	public boolean addDiskoWorkListener(IDiskoWorkListener listener);
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener);
	
}
