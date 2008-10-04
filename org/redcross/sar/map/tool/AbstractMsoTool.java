package org.redcross.sar.map.tool;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

public abstract class AbstractMsoTool extends AbstractMapTool implements IMsoTool {
	
	// MSO objects information
	protected IMsoObjectIf msoOwner = null;
	protected IMsoObjectIf msoObject = null;
	protected IMsoManagerIf.MsoClassCode msoCode = null;	
	
	/*===============================================
	 * IMsoTool interface implementation
	 *===============================================
	 */

	public MsoClassCode getMsoCode() {
		return msoCode;
	}

	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	public void setMsoObject(IMsoObjectIf msoObject) {
		setMsoData(msoOwner,msoObject,msoCode);
	}
	
	public IMsoObjectIf getMsoOwner() {
		return msoOwner;
	}

	public void setMsoOwner(IMsoObjectIf msoOwner) {
		setMsoData(msoOwner,msoObject,msoCode);
	}
	
	public void setMsoData(IMsoTool tool) {
		if(tool instanceof AbstractMsoTool && tool!=this) {
			AbstractMsoTool abstractTool = (AbstractMsoTool)tool;
			setMsoData(abstractTool.msoOwner,abstractTool.msoObject,abstractTool.msoCode);
		}
	}
	
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, IMsoManagerIf.MsoClassCode msoClassCode) {
		
		// is working?
		if(isWorking()) return;
		
		// set mso owner object
		this.msoOwner = msoOwner;
		// set mso object
		this.msoObject = msoObject;
		// set mso object
		this.msoCode = msoClassCode;
		
	}
	
	/* =============================================================
	 *  Inner classes
	 * ============================================================= */

	public class MsoToolState extends AbstractMapTool.MsoToolState {

		// MSO objects and draw information
		protected IMsoObjectIf msoOwner;
		protected IMsoObjectIf msoObject;
		protected MsoClassCode msoClassCode;		

		// create state
		public MsoToolState(AbstractMsoTool tool) {
			super((AbstractMapTool)tool);
			save(tool);
		}	
		
		public void save(AbstractMsoTool tool) {
			this.msoClassCode = tool.msoCode;
			this.msoObject = tool.msoObject;
			this.msoOwner = tool.msoOwner;
			super.save((AbstractMapTool)tool);
		}
		
		public void load(AbstractMsoTool tool) {
			tool.msoCode = this.msoClassCode;
			tool.msoObject = this.msoObject;
			tool.msoOwner = this.msoOwner;
			super.load((AbstractMapTool)tool);
		}
	}
	
}
