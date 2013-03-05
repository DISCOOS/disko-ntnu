package org.redcross.sar.mso.util;

import java.util.Comparator;

import org.redcross.sar.mso.data.IMsoObjectIf;

public class MsoCompareName implements Comparator<IMsoObjectIf> {

	private int options = 0;
	
	public void setOptions(int options) {
		this.options = options;
	}
	
	public int compare(IMsoObjectIf m1, IMsoObjectIf m2) {
		return MsoUtils.compare(m1,m2,options);
	}

}
