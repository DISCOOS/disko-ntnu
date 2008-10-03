package org.redcross.sar.gui.mso.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.redcross.sar.gui.model.DiskoTableModel;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.AbstractUnit;
import org.redcross.sar.mso.data.AssignmentImpl;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.event.IMsoEventManagerIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Selector;

public class UnitTableModel extends DiskoTableModel implements
		IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;
	
	private Selector<IUnitIf> unitSelector = null;
	
	private EnumSet<IMsoManagerIf.MsoClassCode> myInterests = null;
	private Object[] rows = null;
	private IMsoModelIf msoModel = null;

	public UnitTableModel(IMsoModelIf msoModel) {
		this(msoModel,IUnitIf.ACTIVE_RANGE);
	}
	
	public UnitTableModel(IMsoModelIf msoModel,final EnumSet<UnitStatus> status) {
		// prepare
		this.myInterests = EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT);
		this.msoModel = msoModel;
		this.unitSelector = new Selector<IUnitIf>() {
	        public boolean select(IUnitIf aUnit)
	        {
	            return (status.contains(aUnit.getStatus()));
	        }
	    };
		// add listeners
		IMsoEventManagerIf msoEventManager = msoModel.getEventManager();
		msoEventManager.addClientUpdateListener(this);
		// update table data
		update();
	}

	public void handleMsoUpdateEvent(Update e) {
		
		// update table
		update();
		
	}

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) {
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}
	
	private void update() {
		// reset data
		rows = null;
		if(msoModel.getMsoManager().operationExists()) {
			// get command post
			ICmdPostIf cmdPost = msoModel.getMsoManager().getCmdPost();
			// has command post?
			if(cmdPost!=null) {
				
				// get sorted unit list
				List<IUnitIf> units = cmdPost.getUnitList().selectItems(
						unitSelector, IUnitIf.UNIT_TYPE_AND_NUMBER_COMPARATOR);
	
				// has units?
				if(units.size()>0) {
					
					// get data
					List<Object[]> list = new ArrayList<Object[]>(units.size());
					
					// loop over all units
					for(IUnitIf unit : units) {
						// allocate memory
						Object[] row = new Object[5];
						// update row
						row[0] = unit;
						row[1] = unit.getUnitPersonnelItems().size();
						row[2] = getAllocated(unit);
						row[3] = unit.getRemarks();
						row[4] = unit.getStatus();
						// save row
						list.add(row);
					}
					// get array
					rows = list.toArray();
				}
			}
		}
		super.fireTableDataChanged();
	}
	
	private String getAllocated(IUnitIf unit) {
		// initialize
		String text = "Ingen";
		// get assignment
		List<IAssignmentIf> data = unit.getEnqueuedAssignments();
		// build string
		for(int i=0;i<data.size();i++) {
			// get name
			String name = MsoUtils.getAssignmentName(data.get(i),1).toLowerCase();
			text = (i==0 ? name : text +  "; " + name);
		}
		return text;
	}
    
	public int getColumnCount() {
		return 5;
	}

	public int getRowCount() {
		return rows!=null ? rows.length : 0;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
    	// invalid index?
    	if(!(rowIndex<rows.length)) return null;
    	// get row
		Object[] row = (Object[]) rows[rowIndex];
		if(row != null)
			return row[columnIndex];
		else
			return null;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return AbstractUnit.class; 
		case 1:
			return AssignmentImpl.class;
		case 2:
			return AssignmentImpl.class;
		case 3:
			return AssignmentImpl.class;
		case 4:
			return UnitStatus.class;
		default:
			return Object.class;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Enhet";
		case 1:
			return "Antall mannskaper";
		case 2:
			return "Oppdrag i kø";
		case 3:
			return "Merknader";
		case 4:
			return "Status";
		default:
			return null;
		}
	}	
}
