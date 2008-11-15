package org.redcross.sar.math;

public interface IMath<D extends Number> {

	public D add(Object value);
	public D subtract(Object value);
	public D multiply(Object value);
	public D divide(Object value);
	public D mod(Object value);

}
