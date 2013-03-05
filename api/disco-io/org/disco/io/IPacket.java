package org.disco.io;

import java.util.Calendar;

public interface IPacket {
	
	public byte[] getBytes();
	public String getMessage();

	/**
	 * Recognition flag
	 * @return <code>true</code> if recognized, <code>false</code> otherwise.  
	 */
	public boolean isUnknown();

	/**
	 * Auto reply flag
	 * @return <code>true</code> if a automatic replay 
	 * exists for the given message, <code>false</code> otherwise.  
	 */
	public boolean autoReplyExists();
	
	/**
	 * Incomplete message flag
	 * @return <code>true</code> if the given message is incomplete, 
	 * <code>false</code> otherwise.  
	 */
	public boolean isIncomplete();
	
	/**
	 * Reset the incomplete flag
	 */
	public void setIsComplete();
	
	/**
	 * Updated message flag
	 * @return <code>true</code> if the given message is a update, 
	 * <code>false</code> otherwise. A message with the update flag set,
	 * implies that the message is a concatenation of several messages. 
	 */
	public boolean isUpdate();
	
	/**
	 * Duplicate message flag
	 * @return <code>true</code> if the given message is a duplicate, 
	 * <code>false</code> otherwise. A message with the duplicate flag set,
	 * implies that the message is parsed already and should be discarded. 
	 */
	public boolean isDuplicate();
	
	/**
	 * Set the duplicate flag
	 */
	public void setIsDuplicate();
	
	/**
	 * Create a union of two packets
	 * @param message
	 * @param overwrite - if an attribute in both packet is set, overwrite current.
	 */
	public void union(IPacket packet, boolean overwrite) throws IllegalArgumentException;
	
	
	public String getType();
	
	public Long getTimeInMillis();
	public Calendar getTime();
	
	public String getAttrName(int index);
	public int getAttrIndex(String name);
	public int getAttrCount();

	public Object getAttrValue(Object key);
	public Class<?> getAttrClass(int index);
	public void setAttrValue(Object key, Object value);
	public boolean isAttrSet(Object key);
	
}
