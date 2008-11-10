package org.redcross.sar.app;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.IWorkLoop.LoopState;

public interface IService {

	public String getOprID();

	public boolean connect(IMsoModelIf model);
	public boolean disconnect();

	public boolean load();
	public boolean start();
	public boolean resume();
	public boolean suspend();
	public boolean stop();

	public LoopState getLoopState();
	public boolean isLoopState(LoopState state);

	public IWorkLoop getWorkLoop();


}
