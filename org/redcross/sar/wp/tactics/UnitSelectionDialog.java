package org.redcross.sar.wp.tactics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.JOptionPane;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.UnitTable;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoUtils;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;

import java.util.Collection;

public class UnitSelectionDialog extends DiskoDialog {

	private enum MessageBoxType {
		MESSAGE_ALLOCATE,
		MESSAGE_REALLOCATE,
		MESSAGE_INVALID_STATUS,
		MESSAGE_SAME_UNIT,
		MESSAGE_ASSIGNMENT_MISSING,
		MESSAGE_UNIT_MISSING,
		MESSAGE_CLEAR
	}
	
	private static final long serialVersionUID = 1L;
	private IMsoModelIf msoModel = null;
	private JPanel contentPanel = null;
	private JPanel tablePanel = null;
	private JPanel buttonPanel = null;
	private JScrollPane jScrollPane = null;
	private UnitTable unitTable = null;

	private IAssignmentIf currentAssignment = null;
	private JButton allocateButton = null;
	private JLabel titleLabel = null;
	private JButton reclaimButton = null;
	private Dimension buttonSize = null;

	
	public UnitSelectionDialog(IDiskoWpModule wp) {
		
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		
		// prepare objects
		msoModel = wp.getMsoModel();
		buttonSize = wp.getApplication().getUIFactory().getLargeButtonSize();
		
		// initialize UI
		initialize();
		
		// get selected mso feature
		setSelectedMsoFeature(wp.getMap());
		
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		try {
            this.setPreferredSize(new Dimension(400, 300));
            this.setContentPane(getContentPanel());
			this.pack();
		}
		catch (java.lang.Throwable e) {
			//  Do Something
		}
	}

	private static EnumSet<IMsoManagerIf.MsoClassCode> getMyInterest() {
		EnumSet<IMsoManagerIf.MsoClassCode> myInterests 
			= EnumSet.of(IMsoManagerIf.MsoClassCode.CLASSCODE_ASSIGNMENT);
		return myInterests;
	}

	private static EnumSet<IMsoFeatureLayer.LayerCode> getMyLayers() {	
		EnumSet<IMsoFeatureLayer.LayerCode> myLayers 
			= EnumSet.of(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
		myLayers.add(IMsoFeatureLayer.LayerCode.POI_LAYER);
	    return myLayers;
	}

	public void selectedAssignedUnit(IAssignmentIf assignment) {
		JTable table = getUnitTable();
		if(table != null) {
			for (int row = 0; row < table.getRowCount(); row++) {
				for (int col = 0; col < table.getColumnCount(); col++) {
					IUnitIf unit = (IUnitIf)table.getValueAt(row, col);
					if (unit != null) {
						Collection assignments = unit.getAssignedAssignments();
						if (assignments != null && assignments.contains(assignment)) {
							table.setRowSelectionInterval(row, row);
							table.setColumnSelectionInterval(col, col);
							return;
						}
					}
				}
			}
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
				contentPanel = new JPanel();
				contentPanel.setLayout(new BorderLayout());
				contentPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				titleLabel = new JLabel("Knytt oppdrag til enhet");
				contentPanel.add(titleLabel,BorderLayout.NORTH);
				contentPanel.add(getTablePanel(), BorderLayout.CENTER);
				contentPanel.add(getButtonPanel(), BorderLayout.SOUTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	/**
	 * This method initializes tablePanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getTablePanel() {
		if (tablePanel == null) {
			try {
				tablePanel = new JPanel();
				tablePanel.setLayout(new BorderLayout());
				tablePanel.add(getJScrollPane(), BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return tablePanel;
	}
	
	/**
	 * This method initializes buttonPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
				buttonPanel = new JPanel();
				buttonPanel.setLayout(fl);
				buttonPanel.add(getAllocateButton());
				buttonPanel.add(getReclaimButton());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return buttonPanel;
	}	
	
	/**
	 * This method initializes allocateButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAllocateButton() {
		if (allocateButton == null || true) {
			try {
				allocateButton = new JButton();
				String iconName = "IconEnum.ALLOCATED.icon";
				String iconText = "IconEnum.ALLOCATED.text";
				IDiskoApplication app = Utils.getApp();
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				allocateButton.setIcon(icon);
				allocateButton.setToolTipText(app.getProperty(iconText));
				allocateButton.setPreferredSize(buttonSize);
				allocateButton.setMaximumSize(buttonSize);
				allocateButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						allocate();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return allocateButton;
	}
	
	/**
	 * This method initializes reclaimButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getReclaimButton() {
		if (reclaimButton == null || true) {
			try {
				reclaimButton = new JButton();
				String iconName = "IconEnum.CANCELED.icon";
				String iconText = "IconEnum.CANCELED.text";
				IDiskoApplication app = Utils.getApp();
				Icon icon = Utils.createImageIcon(app.getProperty(iconName),iconName);
				reclaimButton.setIcon(icon);
				reclaimButton.setToolTipText(app.getProperty(iconText));
				reclaimButton.setPreferredSize(buttonSize);
				reclaimButton.setMaximumSize(buttonSize);
				reclaimButton.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						clear();
					}
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return reclaimButton;
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			try {
				jScrollPane = new JScrollPane(getUnitTable());
				jScrollPane.getViewport().setBackground(Color.white);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return jScrollPane;
	}

	/**
	 * This method initializes unitTable
	 *
	 * @return javax.swing.JTable
	 */
	private UnitTable getUnitTable() {
		if (unitTable == null) {
			try {
				unitTable = new UnitTable(msoModel, 2);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return unitTable;
	}

	/** 
	 * clear operation
	 *
	 */
	private void clear() {

		// has assignment?
		if(currentAssignment!=null) {
			
			// initialize
			int ans = JOptionPane.NO_OPTION;
			
			// check if assignment is allocated
			if(currentAssignment.getStatus()==AssignmentStatus.ALLOCATED) {
				
				// prompt user
				ans = prompt(MessageBoxType.MESSAGE_CLEAR);
				
				// clear?
				if(ans == JOptionPane.YES_OPTION) {
	                try
	                {
	            		// notify
	            		setIsWorking();
	                    // change status and owner (will raise 
	                	// illegal operation is not possible)
	                	currentAssignment.setStatusAndOwner(
	                			AssignmentStatus.READY, null);	
						// notify
						fireOnWorkChange(getReclaimButton(),
								currentAssignment,AssignmentStatus.READY);
						// finished working
						setIsNotWorking();
						// success!
						return;
	                }
	                catch (IllegalOperationException e) {
						// prompt user
						prompt(MessageBoxType.MESSAGE_INVALID_STATUS);
	                }
				}
			}			
		}

		// is working
		setIsWorking();
		
		// clear selection
		getUnitTable().clearSelection();

		// finished working
		setIsNotWorking();
		
	}

	private boolean allocate() {
		
		// get selected unit
		IUnitIf unit = getSelectedUnit();		

		// has unit?
		if(unit!=null) {

			// has assignment?
			if(currentAssignment!=null) {
				
				// initialize
				int ans = JOptionPane.NO_OPTION;
				boolean reallocate = false;
				
				// can assignment be allocated directly?
				if(AssignmentStatus.DRAFT.equals(currentAssignment.getStatus()) ||
						AssignmentStatus.READY.equals(currentAssignment.getStatus())) {
					// prompt user
					ans = prompt(MessageBoxType.MESSAGE_ALLOCATE);
				}				
				// check if assignment is already allocated
				else if(AssignmentStatus.ALLOCATED.equals(currentAssignment.getStatus())) {
					// get flag
					if(!unit.equals(currentAssignment.getOwningUnit())) {
						// prompt user
						ans = prompt(MessageBoxType.MESSAGE_REALLOCATE);
						// set flag
						reallocate = true;
					}
					else
						prompt(MessageBoxType.MESSAGE_SAME_UNIT);
				}
				else {				
					// prompt user
					prompt(MessageBoxType.MESSAGE_INVALID_STATUS);
				}
				
				// apply change?
				if(ans == JOptionPane.YES_OPTION) { 
					
                    try
                    {
                		// is working
                		setIsWorking();
    					// reallocate?
    					if(reallocate) {
	                        // change status and owner (will raise 
	                    	// illegal operation is not possible)
	                    	currentAssignment.setStatusAndOwner(AssignmentStatus.ALLOCATED, unit);
    					}
    					else {
    						// has assignment to unit
    						unit.addUnitAssignment(currentAssignment, AssignmentStatus.ALLOCATED);
    					}
    					// notify
    					fireOnWorkChange(getAllocateButton(),currentAssignment,AssignmentStatus.ALLOCATED);
    					// finished working
    					setIsNotWorking();
    					// hide me
    					setVisible(false);
    					// success!
    					return true;
                    }
                    catch (IllegalOperationException e) {
    					// prompt user
    					prompt(MessageBoxType.MESSAGE_INVALID_STATUS);
    					// finished working
    					setIsNotWorking();
                    }										
				}
			}
			else {
				// prompt user
				prompt(MessageBoxType.MESSAGE_ASSIGNMENT_MISSING);
			}
		}	
		else { 
			// prompt user
			prompt(MessageBoxType.MESSAGE_UNIT_MISSING);
		}
		
		// is working
		setIsWorking();
		
		// clear selection
		getUnitTable().clearSelection();

		// finished working
		setIsNotWorking();
		
		// finished
		return false;
	}	
	
	private int prompt(MessageBoxType type) {
		int ans = JOptionPane.NO_OPTION;
		if(type == MessageBoxType.MESSAGE_ALLOCATE) {
			// notfiy user
			ans = JOptionPane.showConfirmDialog(getOwner(),
                "Dette vil legge oppdraget til k�en av oppdrag for valgt enhet. Vil du fortsette?",
                "Bekreft valgt enhet", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		if(type == MessageBoxType.MESSAGE_REALLOCATE) {
			// notfiy user
			ans = JOptionPane.showConfirmDialog(getOwner(),
                "Dette vil flytte oppdraget fra n�v�rende enhet til valg enhet. Vil du fortsette?",
                "Bekreft flytting", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		else if(type == MessageBoxType.MESSAGE_CLEAR) {
			// notfiy user
			ans = JOptionPane.showConfirmDialog(getOwner(),
                "Dette vil legge fjerne oppdraget fra valgt enhet. Vil du fortsette?",
                "Bekreft fjerning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		else if(type == MessageBoxType.MESSAGE_INVALID_STATUS) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Oppdrag som er tomme, tildelt, startet eller rapportert kan ikke legges i k�",
                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_SAME_UNIT) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Oppdrag er allerede lagt til enhetens k� av oppdrag",
                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_ASSIGNMENT_MISSING) {			
			JOptionPane.showMessageDialog(getOwner(),
	                "Du m� f�rst velge et oppdrag",
	                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_UNIT_MISSING) {			
			JOptionPane.showMessageDialog(getOwner(),
	                "Du m� f�rst velge en enhet i listen",
	                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		return ans;
	}
	
	private IUnitIf getSelectedUnit() {
		JTable table = getUnitTable();
		if(table != null) {
			int row = table.getSelectedRow();
			int col = table.getSelectedColumn();
			if (row > -1 && col > -1) {
				return (IUnitIf)table.getValueAt(row, col);
			}
		}
		return null;
	}

	private void selectAssignedUnit() {
		if(currentAssignment!=null) {
			// get table
			JTable table = getUnitTable();
			// has table?
			if(table != null) {
				// loop over all cells and detect if any unit is assigned to 
				// appli
				for (int row = 0; row < table.getRowCount(); row++) {
					for (int col = 0; col < table.getColumnCount(); col++) {
						IUnitIf unit = (IUnitIf)table.getValueAt(row, col);
						if (unit != null) {
							List list = unit.getAllocatedAssignments();
							if (list!=null && list.contains(currentAssignment)) {
								// select in table
								table.setRowSelectionInterval(row, row);
								table.setColumnSelectionInterval(col, col);
								// enable or diable dialog?
								if(currentAssignment.getStatus().compareTo(IAssignmentIf.AssignmentStatus.ALLOCATED)<0) {
									setEnabled(true);
								}
								else
									setEnabled(false);
								return;
							}
						}
					}
				}
			}
		}
		// forward
		clearSelection();
	}
	
	private void clearSelection() {
		getUnitTable().clearSelection();
		setEnabled(true);		
	}

	@Override
	public boolean setMsoObject(IMsoObjectIf msoObj) {
		if(isWorking()) return false;
		IAreaIf area = MsoUtils.getOwningArea(msoObj);
		if(area!=null) {
			currentAssignment = area.getOwningAssignment();
			if (currentAssignment!=null) {		
				selectAssignedUnit();
				return true;
			}
		}
		// set to nothing
		currentAssignment = null;
		// not selected
		return false;
	}	
	
	@Override
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			// select assignment
			setMsoObject(msoObject);
		}
	}

	@Override
	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		if(isWorking()) return;
		// is same as selected?
		if(msoObject == currentMsoObj) {
			// reset selection
			currentAssignment = null;
			currentMsoFeature =null;
			currentMsoObj =null;
			clearSelection();
		}
	}
	
	
}  //  @jve:decl-index=0:visual-constraint="10,2"
