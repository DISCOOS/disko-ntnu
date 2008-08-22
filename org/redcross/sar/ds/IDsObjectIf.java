package org.redcross.sar.ds;

public interface IDsObjectIf {
	
	public Object getId();
	
	public Object getAttrValue(Object key);
	public String getAttrName(int index);
	public int getAttrIndex(String name);
	public int getAttrCount();
	
	
}
