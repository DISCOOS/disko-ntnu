package org.redcross.sar.ds.sc;

import org.redcross.sar.ds.sc.Advisor.ID;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

public class Production extends StatusLevel<ID, IAssignmentIf, AssignmentStatus>
						implements ICue {

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Production(ID id, String name, int range, AssignmentStatus status) {
		// forward
		super(id, name, range, "oppdrag", "status", status);
	}

}
