package org.redcross.sar.gui.dialog;
 
import java.awt.Frame;

import javax.swing.DefaultComboBoxModel;

import org.redcross.sar.gui.panel.ListSelectorPanel;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

/**
 * @author kennetgu
 *
 */
public class ListSelectorDialog extends DefaultDialog  {

	private static final long serialVersionUID = 1L;
	
	private boolean m_cancel = false;
	
	private ListSelectorPanel m_listSelectorPanel;
	
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
		addWorkFlowListener(new IWorkFlowListener() {

			@Override
			public void onFlowPerformed(WorkFlowEvent e) {
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
	
	public void prepare(String title, Object[] values) {
		// update selector
		getListSelectorPanel().setCaptionText(title);
		getListSelectorPanel().getList().setModel(new DefaultComboBoxModel(values));		
	}
	
	public Object select() {
		// prepare
		m_cancel = false;
		getListSelectorPanel().getList().setSelectedIndex(0);
		// show
		setVisible(true);
		// translate action
		if(m_cancel)
			return null;
		else
			return getListSelectorPanel().getSelected();
	}	
	
	public Object select(String title, Object[] values) {
		// prepare
		m_cancel = false;
		getListSelectorPanel().getList().setSelectedIndex(0);
		// update selector
		getListSelectorPanel().setCaptionText(title);		
		getListSelectorPanel().getList().setModel(new DefaultComboBoxModel(values));
		// show
		setVisible(true);
		// translate action
		if(m_cancel)
			return null;
		else
			return getListSelectorPanel().getSelected();
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
