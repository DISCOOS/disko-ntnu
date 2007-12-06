package org.redcross.sar.output;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.ISearchIf;

public class MissionOrderPrintParams {
	public final static String PRIORITY_TEXT = "Priority text";
	public final static String ENTITY_NAME = "Entity name";
	public final static String PLANNED_ACCURACY = "Planned accuracy";
	public final static String N50_MAP_NAME = "N50 mapname";
	public final static String DESCRIPTION_OF_ROUTE = "Description of Route";
	public final static String REMARKS = "Remarks";
	public final static String DTG	= "dtg";
	public final static String ROLE	= "role";
	
	public final static String MAP = "map_path";
	
	private Map<String, Object> printParams = null;
	
	public MissionOrderPrintParams(){
		printParams = new HashMap<String, Object>(7);
	}
	
	public Map setPrintParams(List<IAssignmentIf> assignments, String exportMapPath){
				
		
		IAssignmentIf assignment = assignments.get(0);		
		if (assignment instanceof ISearchIf) {
			ISearchIf search = (ISearchIf)assignment;
			printParams.put(PRIORITY_TEXT, search.getPriorityText());
			printParams.put(ENTITY_NAME, null);
			printParams.put(PLANNED_ACCURACY, Integer.toString(search.getPlannedAccuracy())+" %");
			printParams.put(N50_MAP_NAME, null);
			printParams.put(DESCRIPTION_OF_ROUTE, null);
			printParams.put(REMARKS, search.getRemarks());
			printParams.put(MAP, exportMapPath);
						
			System.out.println("search.getPlannedAccuracy(): " + search.getPlannedAccuracy());
			System.out.println("search.getStatusText(): " + search.getStatusText());
			System.out.println("search.getRemarks(): " + search.getRemarks());
			System.out.println("search.getOwningUnit(): " + search.getOwningUnit());
			System.out.println("search.getOwningUnit(): " + search.getOwningUnit());
			System.out.println("search.getPriorityText(): " + search.getPriorityText());
			System.out.println("search.getPlannedArea().getAreaPOIs().getName(): " + search.getPlannedArea().getAreaPOIs().getName());
			System.out.println("search.getAssignmentFindings().size(): " + assignment.getAssignmentFindings().size());
			System.out.println("search.getAssignmentFindings().getName(): " + assignment.getAssignmentFindings().getName());
			
		}
		
		return printParams;
	}
	
}
