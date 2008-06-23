package org.redcross.sar.modelDriver;

public interface IModelDriverListenerIf {
	public void onOperationCreated(String oprID, boolean current);
	public void onOperationFinished(String oprID, boolean current);
}
