package org.redcross.sar.output;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IPersonnelIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IUnitListIf;
import org.redcross.sar.mso.util.MsoUtils;

public class UnitlogReportParams {
	
	public final static String UNIT_TYPE_NUMBER	= "UnitTypeAndNumber";
	
	public final static String LEADER_NAME	= "LeaderName";
	public final static String LEADER_TELEPHONE	= "LeaderTelephone";
	public final static String CO_LEADER_NAME = "CoLeaderName";
	public final static String CO_LEADER_TELEPHONE = "CoLeaderTelephone";
	
	public final static String PERSONELL_SHOW_ASSIGNMENT_LIST = "PersonellList";//not in use. Made for sending an array to printReport
	public final static String KEY_PERSON_NAME_PREFIX = "PersonName";
	public final static String KEY_PERSON_TELE_PREFIX = "PersonTele";
	public int maxPersonsInPrint = 10;
	
	public final static String CALLSIGNS = "Callsigns";//not in use
	public final static String KEY_CALLSIGN_PREFIX = "Callsign";
	public final static String KEY_UNIT_PREFIX = "Unit";
	public int maxCallSignsInPrint = 8;
	
	public final static String ASSIGNMENTS_SHOW_ASSIGNMENT_LIST = "AssignmentsList";//not in use. Made for sending an array to printReport
	public final static String KEY_ASSIGNMENT_PREFIX = "Assignment";
	public final static String KEY_ASSIGNMENT_STATUS_PREFIX = "AssignmentStatus";
	public int maxAssignmentsInPrint = 8;
	
	public final static String PERSONELL_RELEASED_SHOW_ASSIGNMENT_LIST = "PersonellReleasedList";//not in use. Made for sending an array to printReport
	public final static String KEY_RELEASED_PREFIX = "Released";
	public int maxReleasedInPrint = 8;
	
	public final static String REMARKS = "Remarks";
	public final static String EQUIPMENT = "Equipment";
	//Utstyr??
	
	private Map<String, Object> unitParams = new HashMap<String, Object>() ;
	private String[][] personell = new String[maxPersonsInPrint][2];
	private String[][] callSigns = new String[maxCallSignsInPrint][2];
	private String[][] assignments = new String[maxAssignmentsInPrint][2];
	private String[] released = new String[maxReleasedInPrint];
		
	public UnitlogReportParams(){
	}

	private void extractParams(IUnitListIf unitList, IUnitIf unit){
		
		if(unitList != null){
			if(unit != null){
				unitParams.put(UNIT_TYPE_NUMBER, MsoUtils.getUnitName(unit,false));
				IPersonnelIf leader = unit.getUnitLeader();
				if(leader != null){
					unitParams.put(LEADER_NAME, leader.getFirstName() + " " + leader.getLastName());
					unitParams.put(LEADER_TELEPHONE, leader.getTelephone1());
				}
				else{
					unitParams.put(LEADER_NAME, "Ikke kjent");
					unitParams.put(LEADER_TELEPHONE, "");
				}
				//co-leader mangler
				//todo
				unitParams.put(CO_LEADER_NAME, "Ikke kjent");
				unitParams.put(CO_LEADER_TELEPHONE, "");
				
				makePersonelList(unit);
				
				makeCallSignsList(unitList, unit);
				
				makeAssignmentList(unit);		
				
				//remarks				
				unitParams.put(REMARKS, unit.getRemarks());				
				
				//equipment
				//todo		
				
				//fjerner alle null verdier
				putNotNullValue(unitParams);
			}		
			else 
				System.out.println("Unit er tom");
		}
		else 
			System.out.println("Unitliste er tom.");
	}
	
	public Map getUnitlogReportParams(IUnitListIf unitList, IUnitIf unit){		
		extractParams(unitList, unit);
		return unitParams;
	}

	private void makePersonelList(IUnitIf unit){
		//Personell liste
		Collection <IPersonnelIf> personelList = unit.getUnitPersonnelItems();
		int personsCount = personelList.size();
		String personInfo = new String();		
		Iterator personIter = personelList.iterator();
		int personCount = 0;
		int releasedCount = 0;
		String keyPersonName = new String();
		String keyPersonTele = new String();
		String keyReleased = new String();
		while (personIter.hasNext()) {			
			IPersonnelIf person = (IPersonnelIf)personIter.next();
			personInfo = person.getFirstName() + " " + person.getLastName();
			personell[personCount][0] = personInfo;
			personell[personCount][1] = person.getTelephone1();
			keyPersonName = KEY_PERSON_NAME_PREFIX + Integer.toString(personCount+1);
			keyPersonTele = KEY_PERSON_TELE_PREFIX + Integer.toString(personCount+1);
			unitParams.put(keyPersonName, personInfo);
			unitParams.put(keyPersonTele, person.getTelephone1());
			if(person.getStatus() == person.getStatus().RELEASED){				
				if (released == null){
					released[0] = person.getFirstName()+" "+person.getLastName();
					keyReleased = KEY_RELEASED_PREFIX + Integer.toString(released.length+1);
					unitParams.put(keyReleased, released[0]);
					releasedCount++;
				}
				else{
					released[released.length] = person.getFirstName()+" "+person.getLastName();					
					keyReleased = KEY_RELEASED_PREFIX + Integer.toString(released.length+1);
					unitParams.put(keyReleased , released[released.length]);
					releasedCount++;
				}
			}		
			personCount++;
		}
		for (int j = 0; j < (maxPersonsInPrint-personsCount); j++){
			keyPersonName = KEY_PERSON_NAME_PREFIX + Integer.toString((personsCount+1+j));
			keyPersonTele = KEY_PERSON_TELE_PREFIX + Integer.toString((personsCount+1+j));
			unitParams.put(keyPersonName, "");
			unitParams.put(keyPersonTele, "");
		}	
		
		for (int j = 0; j < (maxReleasedInPrint-releasedCount); j++){
			keyReleased = KEY_RELEASED_PREFIX + Integer.toString((releasedCount+1+j));
			unitParams.put(keyReleased, "");
		}
		personCount++;
	}
	
	private void makeCallSignsList(IUnitListIf unitList, IUnitIf unit){
		//callsigns
		callSigns[0][1] = unit.getCallSign();
		callSigns[0][0] = unit.getCommunicatorShortName();
		int unitCount = unitList.size();
		Collection <IUnitIf> unitCollection = unitList.getObjects();
		Iterator unitIter = unitCollection.iterator();
		int iter2 = 1;
		String keyCallSign = KEY_CALLSIGN_PREFIX + Integer.toString(iter2);
		String keyUnit = KEY_UNIT_PREFIX + Integer.toString(iter2);
		unitParams.put(keyCallSign, unit.getCallSign());
		unitParams.put(keyUnit, unit.getShortName());
		while (unitIter.hasNext() && iter2 < maxCallSignsInPrint){
			IUnitIf unit2 = (IUnitIf)unitIter.next();
			if (!callSigns[0][0].equalsIgnoreCase(unit2.getShortName())){
				String unitName = MsoUtils.getUnitName(unit2,false);
				callSigns[iter2][0] = unitName;
				callSigns[iter2][1] = unit2.getCallSign();
				keyCallSign = KEY_CALLSIGN_PREFIX + Integer.toString(iter2);
				keyUnit = KEY_UNIT_PREFIX + Integer.toString(iter2);
				unitParams.put(keyCallSign, unitName);
				unitParams.put(keyUnit, unitName);
			}
			iter2++;
		}		
		for (int j = 0; j < (maxCallSignsInPrint-unitCount); j++){
			keyCallSign = KEY_CALLSIGN_PREFIX + Integer.toString((unitCount+1+j));
			keyUnit = KEY_UNIT_PREFIX + Integer.toString((unitCount+1+j));
			unitParams.put(keyCallSign, "");
			unitParams.put(keyUnit, "");
		}		
	}
	
	private void makeAssignmentList(IUnitIf unit){
		//assignments
		//IAssignmentListIf assignments = unit.getUnitAssignments();
		Collection<IAssignmentIf> assignmentsList = unit.getUnitAssignmentsItems();
		int assignmentCount = assignmentsList.size();
		Iterator<IAssignmentIf> assignmentIter = assignmentsList.iterator();
		String keyAssignmentNr = new String();
		String keyAssignmentStatus = new String();
		int iter3 = 1;
		while (assignmentIter.hasNext()){
			if(iter3>=maxAssignmentsInPrint) break;
			IAssignmentIf assignment = assignmentIter.next();
			assignments[iter3][0] = assignment.getDefaultName();
			assignments[iter3][1] = assignment.getStatusText();
			keyAssignmentNr = "KEY_ASSIGNMENT_PREFIX" + Integer.toString(iter3);
			keyAssignmentStatus = "KEY_ASSIGNMENT_STATUS_PREFIX" + Integer.toString(iter3);
			unitParams.put(keyAssignmentNr, assignment.getDefaultName());
			unitParams.put(keyAssignmentStatus, assignment.getStatusText());
			iter3++;
		}		
		for (int j = 0; j < (maxAssignmentsInPrint-assignmentCount); j++){
			keyAssignmentNr = KEY_ASSIGNMENT_PREFIX + Integer.toString((assignmentCount+1+j));
			keyAssignmentStatus = KEY_ASSIGNMENT_STATUS_PREFIX + Integer.toString((assignmentCount+1+j));
			unitParams.put(keyAssignmentNr, "");
			unitParams.put(keyAssignmentStatus, "");
		}
	}

	private void putNotNullValue(Map<String, Object> printParams){		
		Set set = printParams.keySet();
		Iterator iterator = set.iterator();
		while(iterator.hasNext()){
			String key = (String) iterator.next();
			String value = (String) printParams.get(key);			
			if (value == null){
				//printParams.put(key, "");//
				printParams.put(key, "");//testkommentar
			}
			else
				printParams.put(key, value);
		}
	}
	
}


