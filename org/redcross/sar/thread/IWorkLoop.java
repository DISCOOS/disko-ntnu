package org.redcross.sar.thread;

import org.redcross.sar.thread.IWork.ThreadType;
import org.redcross.sar.thread.event.IWorkLoopListener;

public interface IWorkLoop extends Runnable {

	public enum LoopState {
		PENDING,
		IDLE,
		EXECUTING,
		SUSPENDED,
		FINISHED,
		CANCELED
	}

	public long getID();
	public boolean setID(long id);

	public LoopState getState();
	public boolean isState(LoopState state);

	public ThreadType getThreadType();

	public long schedule(IWork work);
	public boolean cancel(IWork work);

	public boolean suspend();
	public boolean resume();

	public int doWork();

	public IWork findWork(long id);

	public boolean contains(IWork work);
	public boolean isScheduled(IWork work);
	public boolean isExecuting(IWork work);

	public long getDutyCycle();
	public long getTimeOut();
	public void setTimeOut(long time);

	public long getMaxWorkTime();
	public long getAverageWorkTime();
	public double getUtilization();

	public void logWorkTime(long delay);

	public void addWorkLoopListener(IWorkLoopListener listener);
	public void removeWorkLoopListener(IWorkLoopListener listener);


}
