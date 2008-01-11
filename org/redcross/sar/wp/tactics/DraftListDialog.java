package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.AssignmentTable;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.tactics.IDiskoWpTactics.TacticsTaskType;

public class DraftListDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private IDiskoWpModule wp = null;
	private JPanel contentPanel = null;
	private JPanel buttonPanel = null;
	private JButton cancelButton = null;
	private JButton applyButton = null;
	private JLabel infoLabel = null;
	private JScrollPane tableScrollPane = null;
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
            this.setPreferredSize(new Dimension(593, 600));
            this.setContentPane(getContentPanel());
			this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}
	
	public int prompt() {
		// reset flag
		isCancel = false;
		// initialize
		changeCount = 0;		
		// anything to show?
		if(assignmentTable.getRowSorter().getViewRowCount()>0) {
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
	
	private void apply() {
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
						fireOnWorkChange(applyButton,assignment,IAssignmentIf.AssignmentStatus.READY);
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
	
	/**
	 * This method initializes contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				contentPanel.add(getTableScrollPane(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout flowLayout = new FlowLayout();
				flowLayout.setAlignment(FlowLayout.RIGHT);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(flowLayout);
				infoLabel = new JLabel("Velg oppdrag som er klare");
				buttonPanel.add(infoLabel,null);
				buttonPanel.add(getCancelButton(), null);
				buttonPanel.add(getApplyButton(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	/**
	 * This method initializes cancelButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						cancel();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	/**
	 * This method initializes applyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getApplyButton() {
		if (applyButton == null) {
			try {
				applyButton = DiskoButtonFactory.createButton("GENERAL.APPLY",ButtonSize.NORMAL);
				applyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						// forward
						apply();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return applyButton;
	}
	
	/**
	 * This method initializes tableScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getTableScrollPane() {
		if (tableScrollPane == null) {
			try {
				tableScrollPane = new JScrollPane();
				tableScrollPane.getViewport().setBackground(Color.white);
				tableScrollPane.setViewportView(getAssignmentTable());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return tableScrollPane;
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

}  //  @jve:decl-index=0:visual-constraint="10,2"
