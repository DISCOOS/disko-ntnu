package org.redcross.sar.ds;

import org.redcross.sar.ds.IDs.DsDataType;

public class DsUtils {

	public static <D extends Number> DsDataType getDataType(Class<D> dataClass) {
		// calculate average
		if(dataClass.isAssignableFrom(java.lang.Integer.class)) {
			return DsDataType.INTEGER;
		}
		else if(dataClass.isAssignableFrom(java.lang.Double.class)) {
			return DsDataType.DOUBLE;
		}
		else if(dataClass.isAssignableFrom(java.lang.Long.class)) {
			return DsDataType.LONG;
		}
		return DsDataType.NONE;
	}
	
}
