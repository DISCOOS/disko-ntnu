package org.redcross.sar.mso.util;

import java.util.Comparator;

import org.redcross.sar.mso.data.IMsoObjectIf;

public class MsoCompareName implements Comparator<IMsoObjectIf> {

	private int options = 0;
	
	public void setOptions(int options) {
		this.options = options;
	}
	
	public int compare(IMsoObjectIf m1, IMsoObjectIf m2) {
		// get names
		String s1 = MsoUtils.getMsoObjectName(m1, options);
		String s2 = MsoUtils.getMsoObjectName(m2, options);
		s1 = (s1==null) ? "" : s1;
		s2 = (s2==null) ? "" : s2;
		// compare and return?
		return s1.compareTo(s2);
	}

}
