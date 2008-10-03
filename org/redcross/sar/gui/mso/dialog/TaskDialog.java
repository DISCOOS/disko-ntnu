package org.redcross.sar.gui.mso.dialog;

import java.awt.Frame;

import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.mso.panel.TaskPanel;
import org.redcross.sar.mso.data.ITaskIf;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class TaskDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	
	private TaskPanel m_taskPanel = null;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public TaskDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
	        Utils.setFixedSize(this, 500, 435);
	        this.setContentPane(getTaskPanel());
	        this.setModal(true);
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes m_snapPanel	
	 * 	
	 * @return org.redcross.sar.gui.SnapPanel
	 */
	public TaskPanel getTaskPanel() {
		if (m_taskPanel == null) {
			try {
				// create panels
				m_taskPanel = new TaskPanel();
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_taskPanel;
	}
	
	public void setTask(ITaskIf task) {
		setMsoObject(task);
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
