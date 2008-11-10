package org.redcross.sar.ds.sc;

import java.util.Calendar;

public interface ILevel {

	public Calendar time();
	public int level();
	public double in();
	public double out();
	public int range();
	public String unit();

}
