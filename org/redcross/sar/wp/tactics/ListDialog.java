package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.AssignmentTable;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.renderers.EditActionTableCellEditor;
import org.redcross.sar.gui.renderers.SimpleListCellRenderer;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.output.DiskoReport;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.tactics.IDiskoWpTactics.TacticsTaskType;

import com.esri.arcgis.interop.AutomationException;

public class ListDialog extends DiskoDialog {

	private static final long serialVersionUID = 1L;
	private IDiskoWpModule wp = null;
	private JPanel contentPanel = null;
	private JPanel buttonPanel = null;
	private JButton printButton = null;
	private JButton makeReadyButton = null;
	private JScrollPane tableScrollPane = null;
	private AssignmentTable assignmentTable = null;
	private JLabel statusLabel = null;
	private JComboBox statusComboBox = null;
	private DiskoReport report = null;
	private IDiskoApplication app = null;
	
	public ListDialog(IDiskoWpModule wp) {
		super(wp.getApplication().getFrame());
		this.wp = wp;
		app = wp.getApplication();
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
	
	private void makeReady() {
		try {
			app.getMsoModel().suspendClientUpdate();
			JTable table = getAssignmentTable();
			for (int i = 0; i < table.getRowCount(); i++) {
				// selected?
				if ((Boolean)table.getValueAt(i,0)) {
					IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
					if(!assignment.getStatus().equals(IAssignmentIf.AssignmentStatus.READY)) {
						assignment.setStatus(IAssignmentIf.AssignmentStatus.READY);				
						fireOnWorkChange(makeReadyButton,assignment,IAssignmentIf.AssignmentStatus.READY);
					}
				}
			}	
			app.getMsoModel().resumeClientUpdate();
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendToPrint() {
		//System.out.println("sendToPrint");
		List<IAssignmentIf> assignments = new ArrayList<IAssignmentIf>();
		JTable table = getAssignmentTable();
		wp.getMap();
		for (int i = 0; i < table.getRowCount(); i++) {
			// selected?
			if (true == (Boolean)table.getValueAt(i,0)) {
				IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
				assignments.add(assignment);	
			}
		}		
		report.printAssignments(assignments);
	}
	
	public void enableButtons() {
		int[] selection = assignmentTable.getSelectedRows();
		boolean enable = selection != null && selection.length > 0;
		getMakeReadyButton().setEnabled(enable);
		getPrintButton().setEnabled(enable);
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
				flowLayout.setAlignment(FlowLayout.LEFT);
				statusLabel = new JLabel();
				statusLabel.setText("Vis status:");
				buttonPanel = new JPanel();
				buttonPanel.setLayout(flowLayout);
				buttonPanel.add(getPrintButton(), null);
				buttonPanel.add(getMakeReadyButton(), null);
				buttonPanel.add(statusLabel, null);
				buttonPanel.add(getStatusComboBox(), null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}

	/**
	 * This method initializes makeReadyButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getMakeReadyButton() {
		if (makeReadyButton == null) {
			try {
				makeReadyButton = DiskoButtonFactory.createButton(TacticsTaskType.MAKE_READY_TASK,ButtonSize.NORMAL);
				makeReadyButton.setEnabled(false);
				makeReadyButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						makeReady();
					}
				});
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
				printButton = DiskoButtonFactory.createButton(TacticsTaskType.PRINT_TASK,ButtonSize.NORMAL);
				printButton.setEnabled(false);
				report = app.getDiskoReport();
				printButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {						
						sendToPrint();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return printButton;
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
				
				EditActionTableCellEditor editor = new EditActionTableCellEditor();
				editor.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						String action = e.getActionCommand();
						int[] rows = assignmentTable.getSelectedRows();
						if (rows != null && rows.length == 1) {
							IAssignmentIf assignment = (IAssignmentIf)assignmentTable.getValueAt(rows[0],1);
							if (action.equals("EDIT")) {
								try {
									IDiskoMap map = wp.getMap();
									map.suspendNotify();
									map.setSelected(assignment, true);
									map.zoomToMsoObject(assignment);
									map.resumeNotify();
								} catch (AutomationException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							else if (action.equals("COPY")) {
								// copy not yest implemented
							}
							setVisible(false);
						}
					}
				});
				//assignmentTable.setEditActionEditor(editor);
				//selection listener
				assignmentTable.getSelectionModel().addListSelectionListener(
						new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						//Ignore extra messages.
	                    if (e.getValueIsAdjusting()) return;
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
				statusComboBox.setPreferredSize(new Dimension(125, 20));
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


}  //  @jve:decl-index=0:visual-constraint="10,2"
