package org.redcross.sar.wp.tactics;

import org.redcross.sar.wp.IDiskoWp;

public interface IDiskoWpTactics extends IDiskoWp {
	
    public final static String bundleName = "org.redcross.sar.wp.tactics.Tactics";
    
	public enum TacticsActionType {
		MANAGE_ELEMENTS,
		SHOW_ASSIGNMENT_LIST,
		SHOW_MISSION,
		SET_HYPOTHESIS,
		SET_PRIORITY,
		SET_REQUIREMENT,
		SHOW_ESTIMATES,
		SHOW_DESCRIPTION,
		ENQUEUE_TO_UNIT,
		MAKE_READY,
		CHANGE_TO_DRAFT,
		PRINT_SELECTED
    }
}