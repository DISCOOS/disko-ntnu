package org.redcross.sar.wp.tactics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.AssignmentTable;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;

public class DraftListDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private IDiskoWpModule wp = null;
	private DefaultDiskoPanel contentPanel = null;
	private JButton makeReadyButton = null;
	private AssignmentTable assignmentTable = null;
	private IDiskoApplication app = null;
	private int changeCount = 0;
	private boolean isCancel = false;
	
	public DraftListDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		this.app = wp.getApplication();
		// is modal to main frame when visible
		setModal(true);
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(600, 600));
            this.setContentPane(getContentPanel());
			this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultDiskoPanel("Velg oppdrag som er klare",false,true);
				contentPanel.setBodyComponent(getAssignmentTable());
				contentPanel.getScrollPane().getViewport().setBackground(Color.white);
				contentPanel.insertButton("finish",getMakeReadyButton(),"change");
				contentPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						// canceled?
						if("cancel".equalsIgnoreCase(cmd)) 
							cancel();
						else if("change".equalsIgnoreCase(cmd)) 
							change();
						
					}
					
				});
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}
	
	/**
	 * This method initializes makeReadyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMakeReadyButton() {
		if (makeReadyButton == null) {
			try {
				makeReadyButton = DiskoButtonFactory.createButton("STATUS.READY",ButtonSize.NORMAL);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return makeReadyButton;
	}

	/**
	 * This method initializes assignmentTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private AssignmentTable getAssignmentTable() {
		if (assignmentTable == null) {
			try {
				assignmentTable = new AssignmentTable(wp.getMsoModel());
				assignmentTable.showOnly(AssignmentStatus.DRAFT);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return assignmentTable;
	}	
	
	public int prompt() {
		// reset flag
		isCancel = false;
		// initialize
		changeCount = 0;		
		// anything to show?
		if(getAssignmentTable().getRowSorter().getViewRowCount()>0) {
			// show me
			setVisible(true);
		}
		// return state
		return isCancel ? -1 : changeCount;		
	}
	
	private void cancel() {
		// set flag
		isCancel = true;
		// hide me
		setVisible(false);		
	}	
	
	private void change() {
		try {
			// initialize
			changeCount=0;
			// suspend for faster update
			app.getMsoModel().suspendClientUpdate();
			// change status
			JTable table = getAssignmentTable();
			for (int i = 0; i < table.getRowCount(); i++) {
				// selected?
				if ((Boolean)table.getValueAt(i,0)) {
					IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
					if(!assignment.getStatus().equals(IAssignmentIf.AssignmentStatus.READY)) {
						changeCount++;
						assignment.setStatus(IAssignmentIf.AssignmentStatus.READY);
						fireOnWorkChange(assignment,AssignmentStatus.READY);
					}
				}
			}
			// resume updates
			app.getMsoModel().resumeClientUpdate();
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// hide me
		setVisible(false);		
	}

}  //  @jve:decl-index=0:visual-constraint="10,2"
