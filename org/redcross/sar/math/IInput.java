package org.redcross.sar.math;

import java.util.Collection;

import org.redcross.sar.ds.advisor.event.IInputListener;

public interface IInput<I,C extends Number> {

	public boolean isDirty();

	public void schedule(I change);
	public void schedule(Collection<I> changes);

	public Change<C> collect();
	public Change<C> peek();

	public void addInputListener(IInputListener listener);
	public void removeInputListener(IInputListener listener);

}
