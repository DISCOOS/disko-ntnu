package org.redcross.sar.ds;

import org.redcross.sar.data.IDataIf;

public interface IDsObjectIf extends IDataIf {
	
	public IDataIf getId();
	
	public String getAttrName(int index);
	public int getAttrIndex(String name);
	public int getAttrCount();
	
	public Object getAttrValue(Object key);
	public Object getAttrValue(Object key, int sample);
	
	public Class<?> getAttrClass(int index);
	
	public int getSampleCount();
	public Object[][] samples();
	
	public void load(Object[][] samples);
	
	
}
