package org.redcross.sar.util;

import java.util.Comparator;
import org.redcross.sar.map.MapSourceInfo;

public class MapInfoComparator implements Comparator {
	
	private Integer[] sortCols;
	
	public MapInfoComparator() {
    }

	public int compare(java.lang.Object obj1, java.lang.Object obj2) {
		// TODO Auto-generated method stub
		MapSourceInfo row1 = (MapSourceInfo) obj1;
		MapSourceInfo row2 = (MapSourceInfo) obj2;
        int j = 0;
        //sort primarmap
		if(Boolean.valueOf(row2.getPrimarMap()).equals(Boolean.valueOf(row1.getPrimarMap()))){
			//sort secondmap
			if(Boolean.valueOf(row2.getSecondaryMap()).equals(Boolean.valueOf(row1.getSecondaryMap()))){
				//sort status
				if (row2.getStatus().equalsIgnoreCase(row1.getStatus())){
	                return j;//equal
	            }
	            else{
	                return row2.getStatus().compareTo(row1.getStatus());
	            }
			}
			else{
				return Boolean.valueOf(row2.getSecondaryMap()).compareTo(Boolean.valueOf(row1.getSecondaryMap()));
			}		
		}
		else{
			return Boolean.valueOf(row2.getPrimarMap()).compareTo(Boolean.valueOf(row1.getPrimarMap()));
		}
	}

}
