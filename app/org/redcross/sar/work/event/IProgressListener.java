/**
 *
 */
package org.redcross.sar.work.event;

import java.util.EventListener;

/**
 * @author kennetgu
 *
 */
public interface IProgressListener extends EventListener {

	public void changeProgress(ProgressEvent e);

}
