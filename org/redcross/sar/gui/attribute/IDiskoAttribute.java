package org.redcross.sar.gui.attribute;

import java.awt.Dimension;

import org.redcross.sar.mso.data.IAttributeIf;

public interface IDiskoAttribute {

	public Dimension getAttributeSize();

	public void setAttributeSize(Dimension size);
	
	public String getCaption();
	
	public void setCaption(String text);
	
	public double getCaptionWidth();
	
	public void setCaptionWidth(double width);
	
	public void setEditable(boolean isEditable);
	
	public boolean isEditable();
	
	public String getName();
	
	public Object getValue();		
	
	public boolean setValue(Object value);
	
	public boolean load();
	
	public boolean save();
	
	public boolean isMsoAttribute();
	
	public IAttributeIf getMsoAttribute();
	
	public boolean setMsoAttribute(IAttributeIf attribute);
	
}
