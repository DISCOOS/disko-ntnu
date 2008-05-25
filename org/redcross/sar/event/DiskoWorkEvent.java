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

	public enum DiskoWorkEventType {
		TYPE_CHANGE, 
		TYPE_CANCEL,
		TYPE_FINISH
	}
	
	private DiskoWorkEventType type = null;
	private Object data = null;
	
	public DiskoWorkEvent(Object source, 
			DiskoWorkEventType type) {
		this(source,null,type);
	}
	
	public DiskoWorkEvent(Object source, Object data, DiskoWorkEventType type) {
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
		return (type == DiskoWorkEventType.TYPE_CHANGE);
	}
	
	public boolean isCancel() {
		return (type == DiskoWorkEventType.TYPE_CANCEL);
	}
	
	public boolean isFinish() {
		return (type == DiskoWorkEventType.TYPE_FINISH);
	}
	
}
