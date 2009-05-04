/**
 *
 */
package org.redcross.sar.work.event;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.redcross.sar.gui.field.IDiskoField;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class WorkFlowEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public final static int EVENT_CHANGE = 0;
	public final static int EVENT_CANCEL = 1;
	public final static int EVENT_FINISH = 2;
	public final static int EVENT_COMMIT = 3;
	public final static int EVENT_ROLLBACK = 4;

	private int type = EVENT_CHANGE;

	private Object data;
	private UndoableEdit edit;

	public WorkFlowEvent(Object source, Object data, int type) {
		this(source, data, null, type);
	}
	
	public WorkFlowEvent(Object source, Object data, UndoableEdit edit, int type) {
		super(source);
		this.type = type;
		this.data = data;
		this.edit = edit;
	}

	public Object getData() {
		return data;
	}

	public UndoableEdit getEdit() {
		return edit;
	}
	
	public IMsoObjectIf getMsoObject() {
		if(isWorkDoneByMsoObject())
			return (IMsoObjectIf)source;
		if(isMsoData())
			return (IMsoObjectIf)data;
		if(isWorkDoneByDiskoField()) {
			IDiskoField field = (IDiskoField)source;
			if(field.isMsoField()) {
				return field.getMsoAttribute().getOwner();
			}
		}
		return null;
	}
	
	public IAttributeIf<?> getMsoAttribute() {
		if(isWorkDoneByDiskoField()) {
			IDiskoField field = (IDiskoField)source;
			if(field.isMsoField()) {
				return field.getMsoAttribute();
			}			
		}
		return null;
	}
	
	public IDiskoField getDiskoField() {
		if(isWorkDoneByDiskoField())
			return (IDiskoField)source;
		
		return null; 
	}

	public boolean isMsoData() {
		return (data instanceof IMsoObjectIf);
	}
	
	public boolean isWorkDoneByMsoObject() {
		return (getSource() instanceof IMsoObjectIf);
	}

	public boolean isWorkDoneByDiskoField() {
		return (getSource() instanceof IDiskoField);
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
	
	public boolean isUndoCreateable() {
		return (edit!=null);
	}
	
	public UndoableEditEvent createUndoableEditEvent() {
		return (isUndoCreateable()?new UndoableEditEvent(getSource(),edit):null);
	}

}
