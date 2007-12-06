/**
 * 
 */
package org.redcross.sar.thread;

/**
 * @author kennetgu
 *
 */
public interface IDiskoProgress {

	public int start(String note);	
	public int start(String note, int min, int max);
	public int start(String note, int min, int max, int progress);
	public int progress(String note, int step, boolean auto);	
	public int finish();
	public int finish(boolean force);
	public boolean isInAction();
	public int isInActionCount();
	public void addListener(IDiskoProgressListener listener);
	public void removeListener(IDiskoProgressListener listener);
	public boolean isInhibit();
	public void setInhibit(boolean inhibit);
	
	
}
