package org.redcross.sar.ds.mso;

import org.redcross.sar.ds.mso.MsoAdvisor.ID;
import org.redcross.sar.math.MsoStatusInput;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;


public class Execution extends Level<ID, IUnitIf, Integer> implements ICue {

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Execution(ID id, String name, int range, UnitStatus status) {
		// forward
		super(Integer.class, id, name, range, "enhet");
		// install input
		setInput(new MsoStatusInput<IUnitIf, UnitStatus>("status", status));
	}

}
