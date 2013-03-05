package org.redcross.sar.math;

import java.util.Calendar;
import java.util.Queue;

public interface IAverage<D extends Number> {

	public int getMaxSize();
	public void setMaxSize(int size);

	public void clear();
	public boolean sample(IVariable<D> var);

	public int size();
	public D getValue(int sample);
	public Calendar getTime(int sample);

	public Queue<Sample<D>> getSamples();

	public D calculate();

	public long getDuration();
	public long getDuration(Calendar t);

	public double getRate();


}
