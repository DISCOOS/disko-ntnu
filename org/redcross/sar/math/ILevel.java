package org.redcross.sar.math;

import java.util.Calendar;

public interface ILevel<I,D extends Number> {

	public String getName();
	public String getUnit();

	public IInput<I, D> getInput();
	public void setInput(IInput<I, D> input);

	public D getLevel();
	public D getIn();
	public D getOut();

	public Double getRin();
	public Double getRout();

	public Calendar getTime();

	public int calculate() throws Exception;


}
