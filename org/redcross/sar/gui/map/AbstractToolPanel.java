package org.redcross.sar.gui.map;

import java.util.EnumSet;

import javax.swing.JPanel;

import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

public abstract class AbstractToolPanel extends JPanel implements IPropertyPanel {

	private static final long serialVersionUID = 1L;
	
	private IDiskoTool tool = null;	
	
	private boolean isDirty = false;	
	private int consumeCount = 0;
	
	public EnumSet<MsoClassCode> myInterests = null;
	
	public AbstractToolPanel(IDiskoTool tool) {
		
		// forward
		super();
		
		// prepare
		this.tool = tool;
		
		// initialize GUI
		initialize();
		
	}
	
	private void initialize() {
		this.setBorder(null);
	}

	protected void setInterestedIn(EnumSet<MsoClassCode> list) {
		myInterests = list;
	}
	
	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	public abstract void update();
	
	public abstract IMsoObjectIf getMsoObject();
	
	public void setMsoObject(IMsoObjectIf msoObj) {
		// forward
		update();
	}
		
	public void reset() {
		// reset flag?
		if(isDirty) setDirty(false);
	}
	
	public boolean finish() {
		// consume change events
		setChangeable(false);
		// forward
		boolean bFlag = getTool().finish();
		// resume changes
		setChangeable(true);
		// reset flag?
		if(bFlag) setDirty(false);
		// finished
		return bFlag;
	}
	
	public boolean cancel() {
		// consume change events
		setChangeable(false);
		// forward
		reset();
		// forward
		boolean bFlag =getTool().cancel();
		// resume change events
		setChangeable(true);
		// reset flag?
		if(bFlag) setDirty(false);
		// finished
		return bFlag;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public void setDirty(boolean isDirty) {
		setDirty(isDirty,true);
	}
	
	public boolean isChangeable() {
		return (consumeCount==0);
	}
	
	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			consumeCount++;
		else if(consumeCount>0)
			consumeCount--;
	}
	
	public IDiskoTool getTool() {
		return tool;
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
		// get flags
		int mask = e.getEventTypeMask();
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // add object?
		if (createdObject) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		
		}
	}

	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */
	
	protected void setDirty(boolean isDirty, boolean update) {
		this.isDirty = isDirty;
		if(update) update();		
	}
	
	protected void msoObjectCreated(IMsoObjectIf msoObject, int mask) { /*NOP*/ }
	
	protected void msoObjectChanged(IMsoObjectIf msoObject, int mask) { /*NOP*/ }

	protected void msoObjectDeleted(IMsoObjectIf msoObject, int mask) { /*NOP*/ }
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
