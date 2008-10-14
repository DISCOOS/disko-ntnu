package org.redcross.sar.wp.ds;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractButton;

import org.redcross.sar.data.IDataIf;
import org.redcross.sar.data.Selector;
import org.redcross.sar.ds.ete.RouteCost;
import org.redcross.sar.gui.model.DsTableModel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoBinder;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.util.mso.TimePos;

public class AssignmentTableModel extends DsTableModel<IAssignmentIf,RouteCost> {

	private static final long serialVersionUID = 1L;

	public static final int NAME_INDEX = 0;
	public static final int STATUS_INDEX = 1;
	public static final int UNIT_INDEX = 2;
	public static final int ETE_INDEX = 3;
	public static final int EDE_INDEX = 4;
	public static final int ESE_INDEX = 5;
	public static final int ETA_INDEX = 6;
	public static final int EDA_INDEX = 7;
	public static final int ESA_INDEX = 8;
	public static final int ECP_INDEX = 9;
	public static final int MTE_INDEX = 10;
	public static final int MDE_INDEX = 11;
	public static final int MSE_INDEX = 12;
	public static final int MTA_INDEX = 13;
	public static final int MDA_INDEX = 14;
	public static final int MSA_INDEX = 15;
	public static final int XTE_INDEX = 16;
	public static final int XDE_INDEX = 17;
	public static final int XSE_INDEX = 18;
	public static final int XTA_INDEX = 19;
	public static final int XDA_INDEX = 20;
	public static final int XSA_INDEX = 21;
	public static final int EDIT_INDEX = 22;

	private static final String NAME = "name";
	private static final String STATUS = "status";
	private static final String UNIT = "unit";
	private static final String ETE = RouteCost.ATTRIBUTE_NAMES[1];
	private static final String ETA = RouteCost.ATTRIBUTE_NAMES[2];;
	private static final String EDE = RouteCost.ATTRIBUTE_NAMES[3];
	private static final String EDA = RouteCost.ATTRIBUTE_NAMES[4];
	private static final String ESE = RouteCost.ATTRIBUTE_NAMES[5];
	private static final String ESA = RouteCost.ATTRIBUTE_NAMES[6];
	private static final String ECP = RouteCost.ATTRIBUTE_NAMES[7];
	private static final String MTE = RouteCost.ATTRIBUTE_NAMES[8];
	private static final String MTA = RouteCost.ATTRIBUTE_NAMES[9];
	private static final String MDE = RouteCost.ATTRIBUTE_NAMES[10];
	private static final String MDA = RouteCost.ATTRIBUTE_NAMES[11];
	private static final String MSE = RouteCost.ATTRIBUTE_NAMES[12];
	private static final String MSA = RouteCost.ATTRIBUTE_NAMES[13];
	private static final String XTE = RouteCost.ATTRIBUTE_NAMES[16];
	private static final String XTA = RouteCost.ATTRIBUTE_NAMES[17];;
	private static final String XDE = RouteCost.ATTRIBUTE_NAMES[18];
	private static final String XDA = RouteCost.ATTRIBUTE_NAMES[19];
	private static final String XSE = RouteCost.ATTRIBUTE_NAMES[20];
	private static final String XSA = RouteCost.ATTRIBUTE_NAMES[21];
	private static final String EDIT = "edit";

	public static final String[] ATTRIBUTES =

		new String[]{	NAME,STATUS,UNIT,
						ETE,EDE,ESE,ETA,EDA,ESA,ECP,
						MTE,MDE,MSE,MTA,MDA,MSA,
						XTE,XDE,XSE,XTA,XDA,XSA,
						EDIT
					};

	private static final String[] CAPTIONS =

		new String[]{	"Oppdrag","Status","Ansvarlig",
						ETE.toUpperCase(),EDE.toUpperCase(),ESE.toUpperCase(),ETA.toUpperCase(),EDA.toUpperCase(),ESA.toUpperCase(),ECP.toUpperCase(),
						MTE.toUpperCase(),MDE.toUpperCase(),MSE.toUpperCase(),MTA.toUpperCase(),MDA.toUpperCase(),MSA.toUpperCase(),
						XTE.toUpperCase(),XDE.toUpperCase(),XSE.toUpperCase(),XTA.toUpperCase(),XDA.toUpperCase(),XSA.toUpperCase(),
						""
					};


	private static final String[] TOOLTIPS =

		new String[]{	"Navn på oppdrag",
						"Status til oppdrag",
						"Navn på enheten som er tildelt oppdraget",
						"Estimert tid igjen til ankomst (tt:mm:ss)",
						"Estimert avstand igjen til ankomst (meter)",
						"Estimert gjennomsnittshastighet til ankomst (km/t)",
						"Estimert ankomsttid (DTG)",
						"Estimert avstand fra start til ankomst (meter)",
						"Estimert gjennomsnittshastighet fra start (km/t)",
						"Estimert posisjon til enhet",
						"Målt tid mellom to siste punkter (tt:mm:ss)",
						"Målt avstand mellom to siste punkter (meter)",
						"Målt gjennomsnittshastighet mellom to siste punkter (km/t)",
						"Målt ankomststid (DTG)",
						"Målt avstand fra start til ankomst (meter)",
						"Målt gjennomsnittshastighet fra start til ankomst (km/t)",
						"Avvik mellom estimert og målt tid mellom to siste punkter (tt:mm:ss)",
						"Avvik mellom estimert og målt avstand mellom to siste punkter (meter)",
						"Avvik mellom estimert og målt gjennomsnittshastighet mellom to siste punkter (km/t)",
						"Avvik mellom estimert og målt ankomsttid (DTG)",
						"Avvik mellom estimert og målt avstand fra start til ankomst (meter)",
						"Avvik mellom estimert og målt gjennomsnittshastighet fra start til ankomst (km/t)",
						"Avvik mellom estimert og målt gjennomsnittshastighet fra start til ankomst (km/t)",
						"Endre tabell"

					};

	public static final Integer[] PENDING_COLUMNS =

		new Integer[]{ 	NAME_INDEX, UNIT_INDEX, STATUS_INDEX,
						ETE_INDEX, EDE_INDEX,
						EDIT_INDEX
					 };

	public static final Integer[] ACTIVE_COLUMNS =

		new Integer[]{ 	NAME_INDEX, UNIT_INDEX, STATUS_INDEX,
						ETE_INDEX, EDE_INDEX, ETA_INDEX ,
						EDIT_INDEX
					 };

	public static final Integer[] ARCHIVED_COLUMNS =

		new Integer[]{ 	NAME_INDEX, UNIT_INDEX, NAME_INDEX,
						MTE_INDEX, MDE_INDEX, MTA_INDEX,
						XTE_INDEX, XDE_INDEX, XTA_INDEX,
						EDIT_INDEX
					 };

	private IMsoModelIf mso;
	private MsoBinder<IAssignmentIf> msoBinder;

	/* ================================================================
	 * Constructors
	 * ================================================================ */

	public AssignmentTableModel() {

		// forward
		super(RouteCost.class, ATTRIBUTES, CAPTIONS, TOOLTIPS, true);

	}

	/* ================================================================
	 * DsTableModel implementation
	 * ================================================================ */

	protected Object getCellValue(int index, String name) {

		if(NAME.equals(name)) {
			return getId(index);
		}
		else if(STATUS.equals(name)) {
			return getId(index).getStatus();
		}
		else if(UNIT.equals(name)) {
			return getId(index).getOwningUnit();
		}
		// failed
		return null;
	}

	@Override
	protected IAssignmentIf[] translate(IDataIf[] data) {
		if(data!=null) {
			List<IAssignmentIf> list = new ArrayList<IAssignmentIf>(data.length);
			List<IAssignmentIf> found = new ArrayList<IAssignmentIf>(data.length);
			for(int i=0; i<data.length; i++) {
				found.clear();
				IDataIf item = data[i];
				if(item instanceof IAssignmentIf){
					found.add((IAssignmentIf)item);
				}
				else if (item instanceof IUnitIf) {
					IUnitIf unit = (IUnitIf)item;
					found.addAll(unit.getUnitAssignments().getItems());
				}
				else if (item instanceof IRouteIf) {
					// cast to IRouteIf
					IRouteIf route = (IRouteIf)item;
					// loop over all assignments
					for(IAssignmentIf it : getIds()) {
						IAreaIf area = it.getPlannedArea();
						if(area!=null && area.getAreaGeodata().exists(route))
							found.add(it);
					}
				}
				else if (item instanceof IPOIIf) {
					// cast to IPOIIf
					IPOIIf poi = (IPOIIf)item;
					// loop over all assignments
					for(IAssignmentIf it : getIds()) {
						IAreaIf area = it.getPlannedArea();
						if(area!=null && area.getAreaPOIs().exists(poi))
							found.add(it);
					}
				}
				// search for messages
				for(IAssignmentIf it : found) {
					if(findRowFromId(it)!=-1) {
						list.add(it);
					}
				}
			}
			// any found?
			if(list.size()>0) {
				IAssignmentIf[] idx = new IAssignmentIf[list.size()];
				list.toArray(idx);
				return idx;
			}
		}
		// default action
		return super.translate(data);
	}

	/* ================================================================
	 * Public methods
	 * ================================================================ */

	public IAssignmentIf getAssignment(int row) {
		return (IAssignmentIf)getValueAt(row, 0);
	}

	public IUnitIf getOwningUnit(int row) {
		return (IUnitIf)getValueAt(row, 2);
	}

	public boolean isActive(int row) {
		IAssignmentIf assignment = getId(row);
		return assignment!=null ? (assignment.hasBeenStarted() && !(assignment.hasBeenFinished() || assignment.hasBeenAborted())) : false;
	}

	public boolean isExpired(int row) {
		Object value = getValueAt(row, ETE_INDEX);
		int ete = (value instanceof Integer ? (Integer)value : 0);
		return (ete<0);
	}

	public boolean isPending(int row) {
		IUnitIf unit = (IUnitIf)getValueAt(row, UNIT_INDEX);
		return unit!=null ? UnitStatus.PENDING.equals(unit.getStatus()) : false;
	}

	public MsoBinder<IAssignmentIf> createBinder(
			Selector<IAssignmentIf> selector,
			Comparator<IAssignmentIf> comparator) {

		MsoBinder<IAssignmentIf> binder = new MsoBinder<IAssignmentIf>(IAssignmentIf.class);
		binder.setSelector(selector);
		binder.setComparator(comparator);
		binder.addCoClass(IPOIIf.class,IPOIIf.AREA_POI_SELECTOR);
		binder.addCoClass(IRouteIf.class,null);
		binder.addCoClass(IUnitIf.class,null);
		return binder;
	}

	/* ================================================================
	 * AbstractTableModel implementation
	 * ================================================================ */

	@Override
	public Class<?> getColumnClass(int column) {
		// translate
		switch(column) {
		case NAME_INDEX: return IAssignmentIf.class;
		case UNIT_INDEX: return IUnitIf.class;
		case STATUS_INDEX: return AssignmentStatus.class;
		case ETE_INDEX:
		case MTE_INDEX:
		case XTE_INDEX:
			return Integer.class;
		case ETA_INDEX:
		case MTA_INDEX:
		case XTA_INDEX:
			return Calendar.class;
		case EDE_INDEX:
		case EDA_INDEX:
		case MDE_INDEX:
		case MDA_INDEX:
		case XDE_INDEX:
		case XDA_INDEX:
		case ESE_INDEX:
		case ESA_INDEX:
		case MSE_INDEX:
		case MSA_INDEX:
		case XSE_INDEX:
		case XSA_INDEX:
			return Double.class;
		case ECP_INDEX:
			return TimePos.class;
		case EDIT_INDEX:
			return AbstractButton.class;
		}
		return Object.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return (col==EDIT_INDEX);
	}

}
