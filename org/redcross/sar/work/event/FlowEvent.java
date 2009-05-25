/**
 *
 */
package org.redcross.sar.work.event;

import java.awt.Component;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoableEdit;

import org.redcross.sar.gui.field.IField;
import org.redcross.sar.mso.IChangeSourceIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class FlowEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public final static int EVENT_CHANGE = 0;
	public final static int EVENT_CANCEL = 1;
	public final static int EVENT_FINISH = 2;
	public final static int EVENT_COMMIT = 3;
	public final static int EVENT_ROLLBACK = 4;

	private int type = EVENT_CHANGE;

	private Object data;
	private UndoableEdit edit;

	public FlowEvent(Object source, Object data, int type) {
		this(source, data, null, type);
	}
	
	public FlowEvent(Object source, Object data, UndoableEdit edit, int type) {
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

	public boolean isDataMsoObject() {
		return (data instanceof IMsoObjectIf);
	}
	
	public boolean isSourceMsoObject() {
		return (getSource() instanceof IMsoObjectIf);
	}

	public boolean isSourceField() {
		return (getSource() instanceof IField);
	}
	
	public boolean isSourceComponent() {
		return (getSource() instanceof Component);
	}

	public IMsoObjectIf getSourceAsMsoObject() {
		if(isSourceMsoObject())
			return (IMsoObjectIf)source;
		
		return null; 
	}
	
	public IField<?> getSourceAsField() {
		if(isSourceField())
			return (IField<?>)source;
		
		return null; 
	}

	public IMsoObjectIf getDataAsMsoObject() {
		if(isDataMsoObject())
			return (IMsoObjectIf)data;
		
		return null; 
	}
	
	public List<IMsoObjectIf> getMsoObjects() {
		List<IMsoObjectIf> list = new Vector<IMsoObjectIf>();
		if(isSourceMsoObject())
			list.add((IMsoObjectIf)source);
		if(isDataMsoObject())
			list.add((IMsoObjectIf)data);
		if(isSourceField()) {
			IField<?> field = (IField<?>)source;
			IMsoAttributeIf<?> attr = field.getMsoAttribute();
			if(attr!=null) {
				list.add(attr.getOwner());
			}
		}
		return list;
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
	
	public boolean isUndoableEventCreateable() {
		return (edit!=null);
	}
	
	public UndoableEditEvent createUndoableEditEvent() {
		return (isUndoableEventCreateable()?new UndoableEditEvent(getSource(),edit):null);
	}
	
	public Map<IMsoModelIf,List<IChangeSourceIf>> getUncommittedChanges() {
		Map<IMsoModelIf,List<IChangeSourceIf>> 
			changes = new HashMap<IMsoModelIf, List<IChangeSourceIf>>();
		// add source
		if(isSourceMsoObject()) {
			addChangeSource(getSourceAsMsoObject(),changes);			
		}
		if(isSourceField()) {
			IField<?> field = (IField<?>)source;
			IMsoAttributeIf<?> attr = field.getMsoAttribute();
			if(attr!=null) {
				addChangeSource(attr,changes);			
			}
		}
		// add data?
		if(isDataMsoObject()) addChangeSource(getDataAsMsoObject(),changes);
		
		// finished
		return changes;
		
	}
	
	private void addChangeSource(IMsoObjectIf msoObj, Map<IMsoModelIf,List<IChangeSourceIf>> changes) {
		if(msoObj.isChanged()) {
			IMsoModelIf model = msoObj.getModel();
			List<IChangeSourceIf> list = changes.get(model);
			if(list==null) {
				list = new ArrayList<IChangeSourceIf>();
				changes.put(model, list);
			}
			addChangeSource(msoObj,list,true);
		}
    }
	
	private void addChangeSource(IMsoAttributeIf<?> msoAttr, Map<IMsoModelIf,List<IChangeSourceIf>> changes) {
		if(msoAttr.isChanged()) {
			IMsoObjectIf msoObj = msoAttr.getOwner();
			IMsoModelIf model = msoObj.getModel();
			List<IChangeSourceIf> list = changes.get(model);
			if(list==null) {
				list = new ArrayList<IChangeSourceIf>();
				changes.put(model, list);
			}			
			// get flags
			boolean bFlag = msoObj.isCreated();
			// add change source to list
			IChangeSourceIf change = addChangeSource(msoObj,list,!bFlag);
			// add as partial?
			if(bFlag) change.addPartial(msoAttr.getName());
		}
    	
    }	
	
	private IChangeSourceIf addChangeSource(IMsoObjectIf msoObj, List<IChangeSourceIf> changes, boolean clearPartial) {
		// do not allow duplicates
		for(IChangeSourceIf it : changes) {
			if(it.getMsoObject()==msoObj) {
				if(clearPartial) it.clearPartial();
				return it;
			}
		}
		// no duplicate found, add to list
		IChangeSourceIf change = msoObj.getModel().getChanges(msoObj);
		changes.add(change);
		// finished
		return change;
	}

}
