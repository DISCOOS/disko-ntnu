package org.redcross.sar.mso;

public interface IDispatcherListenerIf {
	public void onOperationActivated(String oprID);
	public void onOperationDeactivated(String oprID);
	public void onOperationCreated(String oprID, boolean isLoopback);
	public void onOperationFinished(String oprID, boolean isLoopback);
}
