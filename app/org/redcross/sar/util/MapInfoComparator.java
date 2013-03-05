package org.redcross.sar.util;

import java.util.Comparator;
import org.redcross.sar.map.MapSourceInfo;

public class MapInfoComparator implements Comparator<MapSourceInfo> {
	
	public int compare(MapSourceInfo obj1, MapSourceInfo obj2) {
		// compare selection
		int compare = obj1.getMxdDoc().compareTo(obj2.getMxdDoc());
        // sort on status?
		if(compare==0){
            return obj2.getStatus().compareTo(obj1.getStatus());
		}
		else return compare;
	}

}
