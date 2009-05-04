package org.redcross.sar.gui.field;

import org.redcross.sar.gui.event.IMsoFieldListener;
import org.redcross.sar.mso.data.IAttributeIf;

public interface IMsoField { 

	public boolean isMsoField();
	public IAttributeIf<?> getMsoAttribute();
	public IAttributeIf<?> clearMsoAttribute();
	public boolean setMsoAttribute(IAttributeIf<?> attribute);
	
	public void addMsoFieldListener(IMsoFieldListener listener);
	public void removeMsoFieldListener(IMsoFieldListener listener);

}
