package org.redcross.sar.gui.attribute;

import java.awt.Dimension;

import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.mso.data.IAttributeIf;

public interface IDiskoAttribute {

	public String getName();

	public boolean isDirty();
	
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
	
	public boolean load();
	public boolean save();
	
	public boolean isMsoAttribute();
	public IAttributeIf getMsoAttribute();
	public boolean setMsoAttribute(IAttributeIf attribute);
	
	public boolean addDiskoWorkListener(IDiskoWorkListener listener);
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener);
	
	
}
