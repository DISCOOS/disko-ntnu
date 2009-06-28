package org.redcross.sar.wp.tactics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.redcross.sar.IApplication;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;
import org.redcross.sar.wp.tactics.IDiskoWpTactics.TacticsActionType;

public class PromptDialog extends DefaultDialog {

	private static final long serialVersionUID = 1L;

	private IDiskoWpModule wp;
	private DefaultPanel contentPanel;
	private JButton makeReadyButton;
	private AssignmentTable assignmentTable;

	private IApplication app;

	private int changeCount = 0;
	private boolean isCancel = false;

	public PromptDialog(IDiskoWpModule wp) {
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
				contentPanel = new DefaultPanel("Velg oppdrag som er klare",false,true) {

					private static final long serialVersionUID = 1L;

					@Override
					protected boolean beforeCancel() {
						// set flag
						isCancel = true;
						// success
						return true;
					}

				};
				contentPanel.setContainer(getAssignmentTable());
				contentPanel.setScrollBarPolicies(
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				contentPanel.getScrollPane().getViewport().setBackground(Color.white);
				contentPanel.insertButton("finish",getMakeReadyButton(),"change");
				contentPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						// translate
						if("change".equalsIgnoreCase(cmd))
							change();
					}

				});
				contentPanel.addFlowListener((IFlowListener)wp);


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
				makeReadyButton = DiskoButtonFactory.createButton(
						TacticsActionType.MAKE_READY,
						getContentPanel().getButtonSize());
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
				assignmentTable.setPreferredSize(new Dimension(400,300));
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

	private void change() {
		try {
			// initialize
			changeCount=0;
			// suspend for faster update
			app.getMsoModel().suspendChange();
			// change status
			JTable table = getAssignmentTable();
			for (int i = 0; i < table.getRowCount(); i++) {
				// selected?
				if ((Boolean)table.getValueAt(i,0)) {
					IAssignmentIf assignment = (IAssignmentIf)table.getValueAt(i,1);
					if(!assignment.getStatus().equals(IAssignmentIf.AssignmentStatus.READY)) {
						changeCount++;
						assignment.setStatus(IAssignmentIf.AssignmentStatus.READY);
						FlowEvent e = new FlowEvent(assignment,AssignmentStatus.READY,FlowEvent.EVENT_CHANGE);
						getContentPanel().onFlowPerformed(e);
					}
				}
			}
			// resume updates
			app.getMsoModel().resumeUpdate();
		} catch (IllegalOperationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// hide me
		setVisible(false);
	}

}  //  @jve:decl-index=0:visual-constraint="10,2"
