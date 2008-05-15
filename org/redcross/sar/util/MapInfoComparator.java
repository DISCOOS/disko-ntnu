package org.redcross.sar.util;

import java.util.Comparator;
import org.redcross.sar.map.MapSourceInfo;

public class MapInfoComparator implements Comparator {
	
	public int compare(java.lang.Object obj1, java.lang.Object obj2) {
		// get rows
		MapSourceInfo row1 = (MapSourceInfo) obj1;
		MapSourceInfo row2 = (MapSourceInfo) obj2;
		// compare selection
		int compare = (row1.isCurrent()==row2.isCurrent())? 0 : (row1.isCurrent() ? 1 : -1);
        // sort on status?
		if(compare==0){
            return row2.getStatus().compareTo(row1.getStatus());
		}
		else return compare;
	}

}
