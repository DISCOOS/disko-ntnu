package org.redcross.sar.ds.sc;

import org.redcross.sar.ds.AbstractDsObject;

public class Action extends AbstractDsObject {

	public Action(Clue id) {
		// forward
		super(id);
	}

	/* =============================================================
	 * IDsObject implementation
	 * ============================================================= */

	@Override
	public Clue getId() {
		return (Clue)super.getId();
	}

	public Class<?> getAttrClass(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getAttrCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getAttrIndex(String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getAttrName(int index) {
		// TODO Auto-generated method stub
		return null;
	}

}
