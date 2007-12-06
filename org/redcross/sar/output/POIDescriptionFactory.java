package org.redcross.sar.output;

import java.util.Collection;
import java.util.Arrays;


import org.redcross.sar.mso.data.IPOIListIf;

public class POIDescriptionFactory {

	private POIDescription[] data = null;
	
	public POIDescriptionFactory(IPOIListIf poiList){
		int count = poiList.size();
		data = new POIDescription[count];
		for(int i = 0; i<count; i++){
			String id = Integer.toString(i);
			POIDescription poiDesc = new POIDescription(id, poiList.getName(), "..coord", "...beskrivelse");
			data[i] = poiDesc;
		}
		System.out.println("ant POIs = " + count);
	}
	
	public Object[] getPOIArray()
	{		
		return data;
	}
	
	public Collection getPOICollection()
	{		
		return Arrays.asList(data);
	}
	
}
