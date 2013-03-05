package org.redcross.sar.ds.mso;

import org.redcross.sar.ds.ICue;
import org.redcross.sar.ds.mso.MsoAdvisor.ID;
import org.redcross.sar.math.MsoStatusInput;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

public class Production extends Level<ID, IAssignmentIf, Integer> implements ICue {

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public Production(ID id, String name, int range, AssignmentStatus status) {
		// forward
		super(Integer.class, id, name, range, "oppdrag");
		// install input
		setInput(new MsoStatusInput<IAssignmentIf, AssignmentStatus>("status", status));
	}

}
