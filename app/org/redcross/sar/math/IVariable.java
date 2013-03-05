package org.redcross.sar.math;

import java.util.Calendar;

public interface IVariable<D extends Number> {

	public String getName();

	public boolean isEmpty();

	public D getValue();
	public D setValue(D value);
	public Calendar getTime();
	public Calendar setTime(Calendar time);
	public Sample<D> get();
	public Sample<D> set(Sample<D> sample);
	public Sample<D> set(D value,Calendar time);
	public String getUnit();
	public Class<D> getDataClass();

}
