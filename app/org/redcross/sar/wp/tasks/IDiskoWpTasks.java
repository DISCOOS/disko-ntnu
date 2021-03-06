package org.redcross.sar.wp.tasks;

import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.wp.IDiskoWpModule;

public interface IDiskoWpTasks extends IDiskoWpModule
{
    public final static String bundleName = "org.redcross.sar.wp.tasks.tasks";

	public enum TaskActionType {
		NEW_TASK,
		EDIT_TASK,
		DELETE_TASK,
		TASK_FINISHED
    }    
	
    public void setCurrentTask(ITaskIf task);
}
