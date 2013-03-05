package org.disco.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.zip.CRC32;

public class Packet implements IPacket {

	protected String type;
	protected String message;
	protected boolean isUnknown;
	protected boolean isIncomplete;
	protected boolean isUpdate;
	protected boolean isDuplicate;
	protected boolean autoReplyExists;
	protected Long time;
	protected int changeCount = 0;
	
	protected long checkSumTime;
	
	protected final CRC32 checkSum = new CRC32();
	
	public Packet(String type, String message, boolean isUnknown, boolean autoReplyExists) {
		this(type,message,isUnknown,autoReplyExists,false);
	}
	
	public Packet(String type, String message, boolean isUnknown, boolean autoReplyExists, boolean isIncomplete) {
		// prepare
		this.type = type;
		this.message = message;
		this.isUnknown = isUnknown;
		this.autoReplyExists = autoReplyExists;
		// set default flags
		isUpdate = false;
		isDuplicate = false;
	}	
	
	public byte[] getBytes() {
		return message.getBytes();
	}

	public String getMessage() {
		return message;
	}
	
	public boolean isUnknown() {
		return isUnknown;
	}
	
	public boolean isIncomplete() {
		return isIncomplete;
	}
	
	public boolean isUpdate() {
		return isUpdate;
	}
	
	public boolean isDuplicate() {
		return isDuplicate;
	}

	public boolean autoReplyExists() {
		return autoReplyExists;
	}
	
	public String getType() {
		return type;
	}
		
	public String getAttrName(int index) {
		return null;
	}

	public int getAttrIndex(String name) {
		return -1;
	}

	public int getAttrCount() {
		return 0;
	}

	public Class<?> getAttrClass(int index) {
		return null;
	}	
	
	public boolean isAttrSet(Object key) {
		return getAttrValue(key)!=null;
	}
	
	public Object getAttrValue(Object key) {
		
		Object value = null;
		// current value?
		String name = null;
		if(key instanceof Integer) {
			name = getAttrName((Integer)key);
		}
		else if (key instanceof String) {
			name = (String) key;
		}
		name = getMethodName(name,true);
		if(name!=null) {
			Class<?> c = this.getClass();
			try {
				Method method =  c.getMethod(name);
				value = method.invoke(this);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// finished
		return value;
	}
	
	public void setAttrValue(Object key, Object value) {
		// current value?
		String name = null;
		if(key instanceof Integer) {
			name = getAttrName((Integer)key);
		}
		else if (key instanceof String) {
			name = (String) key;
		}
		String member = getMethodName(name,false);
		if(member!=null) {
			Class<?> c = this.getClass();
			try {
				Method method =  c.getMethod(member,getAttrClass(getAttrIndex(name)));
				method.invoke(this,value);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	protected String getMethodName(String attrName, boolean isGetter) {
		return isGetter ? "get"+attrName : "set"+attrName;
	}

	public Calendar getTime() {
		if(time!=null) {
			Calendar t = Calendar.getInstance();
			t.setTimeInMillis(time);
			return t;
		}
		return null;
	}
	
	public void setTime(Calendar t) {
		time = t.getTimeInMillis();
	}	

	public void setTimeInMillis(Long time) {
		this.time = time;
	}
	
	public Long getTimeInMillis() {
		return time;
	}

	public void setIsComplete() {
		isIncomplete = false;
	}
	
	public void setIsDuplicate() {
		isIncomplete = false;
	}
	
	public void union(IPacket packet, boolean overwrite) throws IllegalArgumentException {

		// is compatible?
		if(this.getClass().isInstance(packet)) {
		
			// union of this
			this.isUnknown |= packet.isUnknown();
			this.isIncomplete |= packet.isIncomplete();
			this.isIncomplete |= packet.isIncomplete();
			this.changeCount = packet.getMessage().length();
			this.message = this.message.concat(packet.getMessage());
			this.type = this.type.concat("-"+packet.getType());
			
			// union of extending 
			int count = getAttrCount();
			for(int i = 0;i<count; i++) {
				if(!isAttrSet(i) || overwrite && packet.isAttrSet(i)) 
					setAttrValue(i, packet.getAttrValue(i));
			}
		}	
		else if(packet!=null){
			throw new IllegalArgumentException("Different class instances");
		}
	}
	
	/**
	 * Create and sets a CRC-32 checksum of the supplied tag 
	 * @return a long representing the CRC-32 checksum of the supplied tag
	 */	
	public long createCheckSum(String uniqueTag) {
		checkSum.update(uniqueTag.getBytes());
		checkSumTime = System.currentTimeMillis();
		return getCheckSum();
	}
	
	public long getCheckSum() {
		return checkSum.getValue();
	}
	
	public long getCheckSumAge() {
		return checkSumTime==0? -1 : System.currentTimeMillis() - checkSumTime;
	}
}
