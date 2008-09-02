package org.redcross.sar.modeldriver1;

public interface IModelDriverListenerIf {
	public void onOperationCreated(String oprID, boolean current);
	public void onOperationFinished(String oprID, boolean current);
}
