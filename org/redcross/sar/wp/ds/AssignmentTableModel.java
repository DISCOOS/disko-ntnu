package org.redcross.sar.wp.ds;

import java.util.Comparator;

import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.ds.IDsObjectIf;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.gui.model.DsObjectTableModel;
import org.redcross.sar.mso.data.IAssignmentIf;

public class AssignmentTableModel extends DsObjectTableModel<RouteCost> {

	private static final long serialVersionUID = 1L;
	private static final String NAME = "name";
	private static final String STATUS = "status";
	private static final String ETA = "eta";
	private static final String ETE = "ete";	
	private static final String EDE = "ede";
	private static final String EAS = "eas";	
	
	private static final String[] ATTRIBUTES = new String[]{NAME, STATUS, 
		ETA,ETE,EDE,EAS};
	
	private static final String[] CAPTIONS = new String[]{"Oppdrag",
		"Status","ETA","ETE","EDE","Gj.hast"};
	
	public AssignmentTableModel(IDsIf<RouteCost> ds) {

		// forward
		super(ds, ATTRIBUTES, CAPTIONS);
		
	}
	
	protected Object getDsAttrValue(IDsObjectIf dsObj, String name) {
		if(NAME.equals(name)) {
			return (IAssignmentIf)dsObj.getId();
		}
		else if(STATUS.equals(name)) {
			return ((IAssignmentIf)dsObj.getId()).getStatus();
		}
		// failed
		return null;
	}
	
	@Override
	public boolean select(RouteCost dsObj) {
		return true;	
	}

	/*
	@Override
	public void sort() {
		sort(COMPERATOR);
	}
	*/

}
