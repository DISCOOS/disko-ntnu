package org.redcross.sar.wp.tactics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.AssignmentTable;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.tactics.IDiskoWpTactics.TacticsActionType;

public class ListDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private DefaultDiskoPanel contentPanel = null;
	private JButton printButton = null;
	private JButton makeReadyButton = null;
	private JButton makeDraftButton = null;
	private JPanel statusPanel = null;
	private JLabel statusLabel = null;
	private JComboBox statusComboBox = null;

	private IDiskoWpModule wp = null;
	private IDiskoApplication app = null;
	private AssignmentTable assignmentTable = null;
	
	private DiskoReportManager report = null;
	
	public ListDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		this.app = wp.getApplication();
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
	private DefaultDiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultDiskoPanel("Oppdrag",false,true);
				contentPanel.setBodyComponent(getAssignmentTable());
				contentPanel.getScrollPane().getViewport().setBackground(Color.white);
				contentPanel.insertItem("finish", getStatusPanel());
				contentPanel.insertButton("finish",getPrintButton(), "print");
				contentPanel.insertButton("finish",getMakeDraftButton(), "draft");
				contentPanel.insertButton("finish",getMakeReadyButton(), "ready");
				contentPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("cancel".equalsIgnoreCase(cmd)) 
							cancel();
						else if("print".equalsIgnoreCase(cmd)) 
							print();
						else if("draft".equalsIgnoreCase(cmd)) 
							change(AssignmentStatus.DRAFT);
						else if("draft".equalsIgnoreCase(cmd)) 
							change(AssignmentStatus.READY);
						
					}
					
				});
				
				
				
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
	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			try {
				FlowLayout flowLayout = new FlowLayout();
				flowLayout.setAlignment(FlowLayout.LEFT);
				statusLabel = new JLabel();
				statusLabel.setOpaque(false);
				statusLabel.setText("Vis status:");
				statusLabel.setForeground(Color.WHITE);
				statusPanel = new JPanel();
				statusPanel.setOpaque(false);
				statusPanel.setLayout(flowLayout);
				statusPanel.add(statusLabel);
				statusPanel.add(getStatusComboBox());
				statusPanel.setPreferredSize(new Dimension(200,30));
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return statusPanel;
	}

	/**
	 * This method initializes makeDraftButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMakeDraftButton() {
		if (makeDraftButton == null) {
			try {
				makeDraftButton = DiskoButtonFactory.createButton(TacticsActionType.CHANGE_TO_DRAFT,ButtonSize.NORMAL);
				makeDraftButton.setEnabled(false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return makeDraftButton;
	}
	
	/**
	 * This method initializes makeReadyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMakeReadyButton() {
		if (makeReadyButton == null) {
			try {
				makeReadyButton = DiskoButtonFactory.createButton(TacticsActionType.MAKE_READY,ButtonSize.NORMAL);
				makeReadyButton.setEnabled(false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return makeReadyButton;
	}
	
	
	/**
	 * This method initializes printButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getPrintButton() {
		if (printButton == null) {
			try {
				printButton = DiskoButtonFactory.createButton(TacticsActionType.PRINT_SELECTED,ButtonSize.NORMAL);
				printButton.setEnabled(false);
				report = app.getReportManager();
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return printButton;
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
				assignmentTable.getModel().addTableModelListener(new TableModelListener() {

					public void tableChanged(TableModelEvent e) {
						// forward
						enableButtons();
					}
					
				});

			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return assignmentTable;
	}

	/**
	 * This method initializes statusComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getStatusComboBox() {
		if (statusComboBox == null) {
			try {
				statusComboBox = new JComboBox();
				statusComboBox.setRenderer(new SimpleListCellRenderer());
				statusComboBox.setPreferredSize(new Dimension(125, 25));
				statusComboBox.addItem("SHOW_ALL");
				AssignmentStatus[] values = AssignmentStatus.values();
				for (int i = 0; i < values.length; i++) {
					statusComboBox.addItem(values[i]);
				}
				statusComboBox.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						assignmentTable.showOnly(statusComboBox.getSelectedItem());
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return statusComboBox;
	}

	private void change(AssignmentStatus status) {
		try {
			app.getMsoModel().suspendClientUpdate();
			JTable table = getAssignmentTable();
			for (int i = 0; i < table.getRowCount(); i++) {
				// selected?
				if ((Boolean)table.getValueAt(i,0)) {
					IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
					if(!status.equals(assignment.getStatus())) {
						assignment.setStatus(status);				
						fireOnWorkChange(assignment,status);
					}
				}
			}	
			app.getMsoModel().resumeClientUpdate();
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public boolean cancel() {
		// reset flag
		getContentPanel().setDirty(false);
		// hide me
		setVisible(false);
		// finished
		return true;
	}
	
	public boolean print() {
		// initialize
		JTable table = getAssignmentTable();
		List<IAssignmentIf> assignments = new ArrayList<IAssignmentIf>();
		// collect assignment to print
		for (int i = 0; i < table.getRowCount(); i++) {
			// selected?
			if (true == (Boolean)table.getValueAt(i,0)) {
				IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
				assignments.add(assignment);	
			}
		}		
		// forward
		report.printAssignments(assignments);
		// finished
		return true;
	}
	
	public void enableButtons() {
		// initialize
		boolean enable = false;
		// check if any is selected
		for (int i = 0; i < assignmentTable.getRowCount(); i++) {
			// selected?
			if (true == (Boolean)assignmentTable.getValueAt(i,0)) {
				enable = true; break;
			}				
		}		
		getMakeDraftButton().setEnabled(enable);
		getMakeReadyButton().setEnabled(enable);
		getPrintButton().setEnabled(enable);
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,2"
