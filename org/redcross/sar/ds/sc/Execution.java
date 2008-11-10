package org.redcross.sar.ds.sc;

import org.redcross.sar.ds.sc.Advisor.ID;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;


public class Execution extends StatusLevel<ID, IUnitIf, UnitStatus>
						implements ICue {

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Execution(ID id, String name, int range, UnitStatus status) {
		// forward
		super(id, name, range, "enhet", "status", status);
	}

}
