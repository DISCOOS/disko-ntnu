package org.redcross.sar.gui.dialog;

import java.awt.Frame;

import org.redcross.sar.gui.panel.ListSelectorPanel;
import org.redcross.sar.thread.event.WorkEvent;
import org.redcross.sar.thread.event.IWorkListener;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class ListSelectorDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	
	private boolean m_cancel = false;
	
	private ListSelectorPanel m_listSelectorPanel = null;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public ListSelectorDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// initialize GUI
		initialize();
		
		// add listener
		addWorkListener(new IWorkListener() {

			@Override
			public void onWorkPerformed(WorkEvent e) {
				if(e.isCancel()) m_cancel = true;				
			}
			
		});
		
	}

	private void initialize() {
		try {
	        Utils.setFixedSize(this, 250, 200);
	        this.setContentPane(getListSelectorPanel());
	        this.setModal(true);
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes listSelectorPanel
	 * 	
	 * @return {@link ListSelectorPanel}
	 */
	public ListSelectorPanel getListSelectorPanel() {
		if (m_listSelectorPanel == null) {
			try {
				// create panels
				m_listSelectorPanel = new ListSelectorPanel();
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_listSelectorPanel;
	}
	
	public Object select() {
		// reset
		m_cancel = false;
		// show
		setVisible(true);
		// translate action
		if(m_cancel)
			return null;
		else
			return getListSelectorPanel().getSelected();
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
