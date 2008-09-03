package org.redcross.sar.modeldriver;

public interface IModelDriverListenerIf {
	public void onOperationCreated(String oprID, boolean current);
	public void onOperationFinished(String oprID, boolean current);
}
