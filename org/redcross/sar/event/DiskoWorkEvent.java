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
	private Object worker = null;
	private IMsoObjectIf msoObj = null;
	private Object data = null;
	
	public DiskoWorkEvent(Object source, 
			DiskoWorkEventType type) {
		this(source,null,null,null,type);
	}
	
	public DiskoWorkEvent(Object source, 
			Object worker, IMsoObjectIf msoObj,
			Object data, DiskoWorkEventType type) {
		super(source);
		this.worker = worker;
		this.type = type;
		this.msoObj = msoObj;
		this.data = data;
	}
	
	public Object getData() {
		return data;
	}
	
	public IMsoObjectIf getMsoObject() {
		return msoObj;
	}
	
	public boolean isMsoData() {
		return (msoObj!=null);
	}
	
	public Object getWorker() {
		return worker;
	}
	
	public boolean isWorkDoneByAwtComponent() {
		return (worker instanceof Component);
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
