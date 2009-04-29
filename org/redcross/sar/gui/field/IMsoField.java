package org.redcross.sar.gui.field;

import org.redcross.sar.mso.data.IAttributeIf;

public interface IMsoField { 

	public IAttributeIf<?> getMsoAttribute();
	public IAttributeIf<?> clearMsoAttribute();
	public boolean setMsoAttribute(IAttributeIf<?> attribute);
	
}
