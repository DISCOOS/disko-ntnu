package org.redcross.sar.map.tool;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

public class BaseMsoTool extends BaseMapTool implements IMsoTool {

	private static final long serialVersionUID = 1L;
	
	// MSO objects information
	protected IMsoModelIf msoModel;
	protected IMsoObjectIf msoOwner;
	protected IMsoObjectIf msoObject;
	protected IMsoManagerIf.MsoClassCode msoCode;

	/*===============================================
	 * IMsoTool interface implementation
	 *===============================================*/

	public BaseMsoTool(IMsoModelIf model) {
		// forward
		super();
		// prepare
		msoModel = model;
	}

	/*===============================================
	 * IMsoTool interface implementation
	 *===============================================*/

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
		if(tool instanceof BaseMsoTool && tool!=this) {
			BaseMsoTool abstractTool = (BaseMsoTool)tool;
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

	public class MsoToolState extends BaseMapTool.MsoToolState {

		// MSO objects and draw information
		protected IMsoObjectIf msoOwner;
		protected IMsoObjectIf msoObject;
		protected MsoClassCode msoClassCode;

		// create state
		public MsoToolState(BaseMsoTool tool) {
			super((BaseMapTool)tool);
			save(tool);
		}

		public void save(BaseMsoTool tool) {
			this.msoClassCode = tool.msoCode;
			this.msoObject = tool.msoObject;
			this.msoOwner = tool.msoOwner;
			super.save((BaseMapTool)tool);
		}

		public void load(BaseMsoTool tool) {
			tool.msoCode = this.msoClassCode;
			tool.msoObject = this.msoObject;
			tool.msoOwner = this.msoOwner;
			super.load((BaseMapTool)tool);
		}
	}

}
