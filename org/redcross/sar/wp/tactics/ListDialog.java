package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.renderer.BundleListCellRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.output.DiskoReportManager;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.tactics.IDiskoWpTactics.TacticsActionType;

public class ListDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private DefaultPanel contentPanel;
	private JButton printButton;
	private JButton makeReadyButton;
	private JButton makeDraftButton;
	private JLabel statusLabel;
	private JComboBox statusComboBox;
	private JLabel scaleLabel;
	private JComboBox scaleComboBox;
	private JPanel centerPanel;
	private JPanel optionsPanel;

	private IDiskoWpModule wp;
	private IDiskoApplication app;
	private AssignmentTable assignmentTable;

	private DiskoReportManager report;

	public ListDialog(IDiskoWpModule wp) {
		// forward
		super(wp.getApplication().getFrame());
		// prepare
		this.wp = wp;
		this.app = wp.getApplication();
		this.report = app.getReportManager();
		// initialize gui
		initialize();
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(400, 400));
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
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel("Oppdrag",false,true);
				contentPanel.setBodyComponent(getCenterPanel());
				contentPanel.setPreferredBodySize(new Dimension(400, 350));
				contentPanel.setNotScrollBars();
				contentPanel.setFitBodyOnResize(true);
				contentPanel.getScrollPane().getViewport().setBackground(Color.white);
				contentPanel.insertButton("finish",getPrintButton(), "print");
				contentPanel.insertButton("finish",getMakeDraftButton(), "draft");
				contentPanel.insertButton("finish",getMakeReadyButton(), "ready");
				contentPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("print".equalsIgnoreCase(cmd))
							print();
						else if("draft".equalsIgnoreCase(cmd))
							change(AssignmentStatus.DRAFT);
						else if("ready".equalsIgnoreCase(cmd))
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
	 * This method initializes makeDraftButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getMakeDraftButton() {
		if (makeDraftButton == null) {
			try {
				makeDraftButton = DiskoButtonFactory.createButton(
						TacticsActionType.CHANGE_TO_DRAFT,
						getContentPanel().getButtonSize());
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
				makeReadyButton = DiskoButtonFactory.createButton(
						TacticsActionType.MAKE_READY,
						getContentPanel().getButtonSize());
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
				printButton = DiskoButtonFactory.createButton(
						TacticsActionType.PRINT_SELECTED,
						getContentPanel().getButtonSize());
				printButton.setEnabled(false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return printButton;
	}

	private JPanel getCenterPanel() {
		if(centerPanel==null) {
			BorderLayout bl = new BorderLayout();
			centerPanel = new JPanel(bl);
			centerPanel.add(getOptionsPanel(),BorderLayout.NORTH);
			JScrollPane pane = UIFactory.createScrollPane(
					getAssignmentTable(),false,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			centerPanel.add(pane,BorderLayout.CENTER);
		}
		return centerPanel;
	}

	private JPanel getOptionsPanel() {
		if(optionsPanel ==null) {
			optionsPanel = new JPanel();
			optionsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			BoxLayout bl = new BoxLayout(optionsPanel,BoxLayout.X_AXIS);
			optionsPanel.setLayout(bl);
			optionsPanel.setOpaque(false);
			statusLabel = new JLabel();
			statusLabel.setOpaque(false);
			statusLabel.setText("Vis oppdrag");
			optionsPanel.add(statusLabel);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(getStatusComboBox());
			optionsPanel.add(Box.createHorizontalStrut(5));
			scaleLabel = new JLabel();
			scaleLabel.setOpaque(false);
			scaleLabel.setText("Skala kart");
			optionsPanel.add(scaleLabel);
			optionsPanel.add(Box.createHorizontalStrut(5));
			optionsPanel.add(getScaleComboBox());
			optionsPanel.add(Box.createHorizontalGlue());
		}
		return optionsPanel;
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
				assignmentTable.setPreferredSize(new Dimension(100,10));
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
				statusComboBox.setRenderer(new BundleListCellRenderer());
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

	/**
	 * This method initializes scaleComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getScaleComboBox() {
		if (scaleComboBox == null) {
			try {
				scaleComboBox = new JComboBox();
				scaleComboBox.setPreferredSize(new Dimension(125, 25));
				scaleComboBox.addItem("1: 5000");
				scaleComboBox.addItem("1:10000");
				scaleComboBox.addItem("1:15000");
				scaleComboBox.addItem("1:25000");
				scaleComboBox.addItem("1:50000");
				setUserScale(report.getMapScale());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return scaleComboBox;
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
						fireOnWorkChange(assignment);
					}
				}
			}
			setDirty(false);
			app.getMsoModel().resumeClientUpdate();
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		report.printAssignments(assignments,getUserScale(),false);
		// finished
		return true;
	}

	public void setUserScale(double scale) {
		if(scale>50000) {
			scale = 50000;
		}
		DecimalFormat format = new DecimalFormat("00000");
		getScaleComboBox().setSelectedItem("1:"+format.format(Math.round(scale)));
	}

	public double getUserScale() {
		String value = (String)getScaleComboBox().getSelectedItem();
		if(value!=null && !value.isEmpty()) {
			try {
				value = value.replaceAll("1:", "");
				return Double.valueOf(value.trim());
			} catch (NumberFormatException e) { }
		}
		return -1;
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
