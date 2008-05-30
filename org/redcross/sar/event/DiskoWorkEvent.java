/**
 * 
 */
package org.redcross.sar.event;

import java.awt.Component;
import java.util.EventObject;

import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class DiskoWorkEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public final static int EVENT_CHANGE = 0; 
	public final static int EVENT_CANCEL = 1;
	public final static int EVENT_FINISH = 2;
	public final static int EVENT_COMMIT = 3;
	public final static int EVENT_ROLLBACK = 4;
	
	private int type = EVENT_CHANGE;
	
	private Object data = null;
	
	public DiskoWorkEvent(Object source, int type) {
		this(source,null,type);
	}
	
	public DiskoWorkEvent(Object source, Object data, int type) {
		super(source);
		this.type = type;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	
	public IMsoObjectIf getMsoObject() {
		if(isMsoSource())
			return (IMsoObjectIf)source;
		if(isMsoData())
			return (IMsoObjectIf)data;
		return null;
	}
	
	public boolean isMsoSource() {
		return (source instanceof IMsoObjectIf);
	}
	
	public boolean isMsoData() {
		return (data instanceof IMsoObjectIf);
	}
	
	public boolean isWorkDoneByAwtComponent() {
		return (getSource() instanceof Component);
	}
	
	public Object getType() {
		return type;
	}
	
	public boolean isChange() {
		return (type == EVENT_CHANGE);
	}
	
	public boolean isCancel() {
		return (type == EVENT_CANCEL);
	}
	
	public boolean isFinish() {
		return (type == EVENT_FINISH);
	}
	
	public boolean isCommit() {
		return (type == EVENT_COMMIT);		
	}
	
	public boolean isRollback() {
		return (type == EVENT_ROLLBACK);		
	}
	
}
