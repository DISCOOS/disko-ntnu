package org.redcross.sar.gui.field;

import org.redcross.sar.gui.event.IMsoFieldListener;
import org.redcross.sar.mso.data.IMsoAttributeIf;

public interface IMsoField { 

	public boolean isMsoField();
	public IMsoAttributeIf<?> getMsoAttribute();
	public IMsoAttributeIf<?> clearMsoAttribute();
	public boolean setMsoAttribute(IMsoAttributeIf<?> attribute);
	
	public void addMsoFieldListener(IMsoFieldListener listener);
	public void removeMsoFieldListener(IMsoFieldListener listener);

}
