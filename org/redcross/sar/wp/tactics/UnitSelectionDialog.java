package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JOptionPane;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DiskoPanel;
import org.redcross.sar.gui.UnitTable;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.MsoUtils;
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
		MESSAGE_CLEAR,
		MESSAGE_NOTHING_ALLOCATED
	}
	
	private static final long serialVersionUID = 1L;
	private IMsoModelIf msoModel = null;
	private DiskoPanel contentPanel = null;
	private UnitTable unitTable = null;

	private IAssignmentIf currentAssignment = null;
	private JButton allocateButton = null;
	private JButton reclaimButton = null;

	public UnitSelectionDialog(IDiskoWpModule wp) {
		
		// forward
		super(wp.getApplication().getFrame(),wp.getMap(),getMyInterest(),getMyLayers());
		
		// prepare objects
		msoModel = wp.getMsoModel();
		
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
            this.setPreferredSize(new Dimension(600, 400));
            this.setContentPane(getContentPanel());
			this.pack();
		}
		catch (java.lang.Throwable e) {
			e.printStackTrace();
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
				IUnitIf unit = (IUnitIf)table.getValueAt(row, 0);
				if (unit != null) {
					Collection assignments = unit.getAssignedAssignments();
					if (assignments != null && assignments.contains(assignment)) {
						table.setRowSelectionInterval(row, row);
						return;
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
	private DiskoPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DiskoPanel();
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
				AbstractButton button = DiskoButtonFactory.createButton("IconEnum.ALLOCATED",ButtonSize.NORMAL);
				button.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						allocate();
					}
				});
				contentPanel.addButton(button, "allocated");
				button = DiskoButtonFactory.createButton("IconEnum.CANCELED",ButtonSize.NORMAL);
				button.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						clear();
					}
				});
				contentPanel.addButton(button, "canceled");				
				button = DiskoButtonFactory.createButton("GENERAL.CANCEL", ButtonSize.NORMAL);
				button.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// hide me!
						setVisible(false);						
					}					
				});
				contentPanel.addButton(button,"cancel");
				contentPanel.setBodyComponent(getUnitTable());
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return contentPanel;
	}

	
	/**
	 * This method initializes allocateButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getAllocateButton() {
		if (allocateButton == null) {
			try {
				allocateButton = DiskoButtonFactory.createButton("IconEnum.ALLOCATED",ButtonSize.NORMAL);
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
		if (reclaimButton == null) {
			try {
				reclaimButton = DiskoButtonFactory.createButton("IconEnum.CANCELED",ButtonSize.NORMAL);
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
	 * This method initializes unitTable
	 *
	 * @return javax.swing.JTable
	 */
	private UnitTable getUnitTable() {
		if (unitTable == null) {
			try {
				unitTable = new UnitTable(msoModel,"48x48");
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

		// get selected row
		int row = unitTable.convertRowIndexToModel(unitTable.getSelectedRow());

		// get unit
		IUnitIf unit = (row>-1 ? (IUnitIf)unitTable.getModel().getValueAt(row, 0) : null); 
		
		// a unit was selected?
		if(unit!=null) {

			// get count
			int count = unit.getAllocatedAssignments().size();
			
			// has assignments?
			if(count>0) {
				
				// last assignment in list
				IAssignmentIf assignment = unit.getAllocatedAssignments().get(count-1);
				
				
				// initialize
				int ans = JOptionPane.NO_OPTION;
				
				// check if assignment is allocated
				if(assignment.getStatus()==AssignmentStatus.ALLOCATED) {
					
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
		            		assignment.setStatusAndOwner(
		                			AssignmentStatus.READY, null);	
							// notify
							fireOnWorkChange(getReclaimButton(),
									assignment,AssignmentStatus.READY);
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
			else {
				// prompt user
				prompt(MessageBoxType.MESSAGE_NOTHING_ALLOCATED);
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
                "Dette vil legge oppdraget til bakerst i køen av oppdrag for valgt enhet. Vil du fortsette?",
                "Bekreft valgt enhet", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		if(type == MessageBoxType.MESSAGE_REALLOCATE) {
			// notfiy user
			ans = JOptionPane.showConfirmDialog(getOwner(),
                "Dette vil flytte oppdraget fra nåværende enhet til valg enhet. Vil du fortsette?",
                "Bekreft flytting", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		else if(type == MessageBoxType.MESSAGE_CLEAR) {
			// notfiy user
			ans = JOptionPane.showConfirmDialog(getOwner(),
                "Dette vil legge fjerne bakerste oppdrag fra køen til valgt enhet. Vil du fortsette?",
                "Bekreft fjerning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);								
		}
		else if(type == MessageBoxType.MESSAGE_INVALID_STATUS) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Oppdrag som er tomme, tildelt, startet eller rapportert kan ikke legges i kø",
                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_SAME_UNIT) {
			// notfiy user
			JOptionPane.showMessageDialog(getOwner(),
                "Oppdrag er allerede lagt til enhetens kø av oppdrag",
                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_ASSIGNMENT_MISSING) {			
			JOptionPane.showMessageDialog(getOwner(),
	                "Du må først velge et oppdrag",
	                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_UNIT_MISSING) {			
			JOptionPane.showMessageDialog(getOwner(),
	                "Du må først velge en enhet i listen",
	                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_NOTHING_ALLOCATED) {			
			JOptionPane.showMessageDialog(getOwner(),
	                "Enheten har ingen oppdrag i kø",
	                "Ulovlig handling", JOptionPane.INFORMATION_MESSAGE);

		}
		return ans;
	}
	
	private IUnitIf getSelectedUnit() {
		JTable table = getUnitTable();
		if(table != null) {
			int row = table.getSelectedRow();
			if (row > -1) {
				return (IUnitIf)table.getValueAt(row, 0);
			}
		}
		return null;
	}
	
	@Override
	public int setMsoObject(IMsoObjectIf msoObj) {
		int state = 0;
		if(isWorking()) return state;
		// get assignment
		if(msoObj instanceof IAssignmentIf) {
			currentAssignment = (IAssignmentIf)msoObj;
		}
		else {
			IAreaIf area = MsoUtils.getOwningArea(msoObj);
			if(area!=null) {
				currentAssignment = area.getOwningAssignment();
			}
			else {
				// set to nothing
				currentAssignment = null;
			}
		}
		// forward
		setup();
		// success
		return currentAssignment == null ? -1 : 1;
	}	
	
	private void selectAssignedUnit() {
		if(currentAssignment!=null) {
			// get table
			JTable table = getUnitTable();
			// has table?
			if(table != null) {
				// find unit allocated to current assignment 
				for (int row = 0; row < table.getRowCount(); row++) {
					IUnitIf unit = (IUnitIf)table.getValueAt(row, 0);
					if (unit != null) {
						List list = unit.getAllocatedAssignments();
						if (list!=null && list.contains(currentAssignment)) {
							// select in table
							table.setRowSelectionInterval(row, row);
							// finished
							return;
						}
					}
				}
			}
		}
		// clear current selection
		getUnitTable().clearSelection();
	}
	
	private void setup() {
		// update icon
		if(currentAssignment!=null) {
			Enum e = MsoUtils.getType(currentAssignment,true);
			getContentPanel().setCaptionIcon(Utils.getEnumIcon(e,"48x48"));
			getContentPanel().setCaptionText("<html>Legg <b>" + 
					MsoUtils.getAssignmentName(currentAssignment, 1).toLowerCase() + 
					"</b> i kø til en enhet i listen" + (currentAssignment instanceof ISearchIf ? 
							"    (<i>mannskapsbehov</i>: <b>" + ((ISearchIf)currentAssignment).getPlannedPersonnel() + "</b>)</html>" : "</html>"));
			getAllocateButton().setEnabled(true);
			getReclaimButton().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må velge et oppdrag før du kan legge det til køen til enhet");
			getAllocateButton().setEnabled(false);
			getReclaimButton().setEnabled(false);
		}		
		// get current assigned unit
		selectAssignedUnit();
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
			// forward
			setup();
		}
	}		
	
}  //  @jve:decl-index=0:visual-constraint="10,2"
