package org.redcross.sar.mso.util;

import java.util.Comparator;

import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;

public class MsoCompareRoute implements Comparator<IMsoObjectIf> {

	public int compare(IMsoObjectIf m1, IMsoObjectIf m2) {
		// cast to IRouteIf
		IRouteIf r1 = (m1 instanceof IRouteIf) ? (IRouteIf)m1: null;
		IRouteIf r2 = (m2 instanceof IRouteIf) ? (IRouteIf)m2: null;
		// compare?
		if(!(r1 == r2 || r1==null || r2==null ||
				r1.getAreaSequenceNumber() == r2.getAreaSequenceNumber())) {
			// compare
			if(r2.getAreaSequenceNumber() > r1.getAreaSequenceNumber()) return 1;
			if(r2.getAreaSequenceNumber() < r1.getAreaSequenceNumber()) return -1;
		}
		// finished
		return 0;
	}

}
