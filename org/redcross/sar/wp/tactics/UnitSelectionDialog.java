package org.redcross.sar.wp.tactics;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JOptionPane;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.MessageDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.data.IUnitIf.UnitStatus;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.IDiskoWpModule;

import java.util.Collection;

public class UnitSelectionDialog extends DefaultDialog {

	private enum MessageBoxType {
		MESSAGE_ALLOCATE,
		MESSAGE_REALLOCATE,
		MESSAGE_INVALID_STATUS,
		MESSAGE_SAME_UNIT,
		MESSAGE_ASSIGNMENT_MISSING,
		MESSAGE_UNIT_MISSING,
		MESSAGE_UNIT_RELEASED,
		MESSAGE_CLEAR,
		MESSAGE_NOTHING_ALLOCATED
	}
	
	private static final long serialVersionUID = 1L;
	
	private DefaultPanel contentPanel = null;
	private UnitTable unitTable = null;

	private JButton assignButton = null;
	private JButton reclaimButton = null;

	private IDiskoWpModule wp = null;
	private IMsoModelIf msoModel = null;
	
	
	public UnitSelectionDialog(IDiskoWpModule wp) {
		
		// forward
		super(wp.getApplication().getFrame());
		
		// prepare
		this.wp = wp;
		this.msoModel = wp.getMsoModel();
		
		// initialize UI
		initialize();

		// initialise
		setup();

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

	/**
	 * This method initializes contentPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private DefaultPanel getContentPanel() {
		if (contentPanel == null) {
			try {
				contentPanel = new DefaultPanel("",false,true) {
					
					private static final long serialVersionUID = 1L;

					@Override
					public void setMsoObject(IMsoObjectIf msoObj) {
						
						// consume events
						setChangeable(false);
						
						// initialize
						IAssignmentIf assignment = null;
						
						// get assignment
						if(msoObj instanceof IAssignmentIf) {
							assignment = (IAssignmentIf)msoObj;
						}
						else {
							IAreaIf area = MsoUtils.getOwningArea(msoObj);
							if(area!=null) {
								assignment = area.getOwningAssignment();
							}
						}
						
						// update
						super.setMsoObject(assignment);

						// resume changes
						setChangeable(true);
						
						// update
						setDirty(false);
												
					}	
					
					@Override
					public void update() {
						super.update();
						setup();
					}
						
				};
				contentPanel.setInterests(wp.getMsoModel(),getMyInterest());
				contentPanel.setMsoLayers(wp.getMap(),getMyLayers());				
				contentPanel.setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));		
				contentPanel.insertButton("finish", getAssignButton(), "assign");
				contentPanel.insertButton("finish", getReclaimButton(), "reclaim");				
				contentPanel.setBodyComponent(getUnitTable());
				contentPanel.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("assign".equalsIgnoreCase(cmd))
							assign();
						else if("reclaim".equalsIgnoreCase(cmd))
							reclaim();
					}
					
				});
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
	private JButton getAssignButton() {
		if (assignButton == null) {
			try {
				assignButton = DiskoButtonFactory.createButton("STATUS.QUEUED",ButtonSize.NORMAL);
				assignButton.setToolTipText("Legg oppdrag i kø til valgt enhet");
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return assignButton;
	}
	
	/**
	 * This method initializes reclaimButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getReclaimButton() {
		if (reclaimButton == null) {
			try {
				reclaimButton = DiskoButtonFactory.createButton("STATUS.CANCELED",ButtonSize.NORMAL);
				reclaimButton.setToolTipText("Fjern siste oppdrag fra køen");
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
				unitTable = new UnitTable(msoModel,"32x32",IUnitIf.ACTIVE_RANGE);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return unitTable;
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
	 * reclaim allocated assignments
	 *
	 */
	public boolean reclaim() {

		// consume?
		if(!isChangeable()) return false;
		
		// consume events
		setChangeable(false);
		
		// initialize
		boolean bFlag = false;
		
		// get selected row
		int row = unitTable.getSelectedRow();
		
		// valid?
		if(row>-1) {
			
			// convert
			row = unitTable.convertRowIndexToModel(unitTable.getSelectedRow());

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
					if(assignment.getStatus()==AssignmentStatus.QUEUED) {
						
						// prompt user
						ans = prompt(MessageBoxType.MESSAGE_CLEAR);
						
						// clear?
						if(ans == JOptionPane.YES_OPTION) {
			                try
			                {
			                    // change status and owner (will raise 
			                	// illegal operation is not possible)
			            		assignment.setStatusAndOwner(AssignmentStatus.READY, null);	
								// notify
								fireOnWorkChange(assignment);
								// success!
								bFlag = true;
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
		}
		else {
			// prompt user
			prompt(MessageBoxType.MESSAGE_UNIT_MISSING);
		}

		// clear selection?
		if(bFlag) getUnitTable().clearSelection();

		// resume events
		setChangeable(true);
		
		// finished
		setDirty(false);

		// finished
		return bFlag;
	}

	public boolean assign() {
		
		// consume?
		if(!isChangeable()) return false;
		
		// consume events
		setChangeable(false);
		
		// initialize
		boolean bFlag = false;
		
		// get selected unit
		IUnitIf unit = getSelectedUnit();		

		// has unit?
		if(unit!=null) {

			// get assignment
			IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
			
			// has assignment?
			if(assignment!=null) {
				
				// initialize
				int ans = JOptionPane.NO_OPTION;
				boolean reallocate = false;
				
				// can assignment be allocated directly?
				if(AssignmentStatus.DRAFT.equals(assignment.getStatus()) ||
						AssignmentStatus.READY.equals(assignment.getStatus())) {
					// prompt user
					ans = prompt(MessageBoxType.MESSAGE_ALLOCATE);
				}				
				// check if assignment is already allocated
				else if(AssignmentStatus.QUEUED.equals(assignment.getStatus())) {
					// get flag
					if(!unit.equals(assignment.getOwningUnit())) {
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
    					// reallocate?
    					if(reallocate) {
	                        // change status and owner (will raise 
	                    	// illegal operation if not possible)
    						assignment.setStatusAndOwner(AssignmentStatus.QUEUED, unit);
    					}
    					else {
    						// has assignment to unit
    						unit.addUnitAssignment(assignment, AssignmentStatus.QUEUED);
    					}
    					// notify
    					fireOnWorkChange(assignment);
    					// success!
    					bFlag =  true;
                    }
                    catch (IllegalOperationException e) {
    					// prompt user
    					prompt(MessageBoxType.MESSAGE_INVALID_STATUS);
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
		
		// clear selection?
		if(bFlag) getUnitTable().clearSelection();

		// resume events
		setChangeable(true);
		
		// hide me?
		if(bFlag) setVisible(false);
		
		// finished
		return bFlag;
		
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
			Utils.showMessage("Begrensning",
					"Oppdrag er allerede lagt til enhetens kø av oppdrag", 
					MessageDialog.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_ASSIGNMENT_MISSING) {			
			Utils.showMessage("Begrensning",
	                "Du må først velge et oppdrag",
	                MessageDialog.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_UNIT_RELEASED) {			
			Utils.showMessage("Begrensning",
	                "Enheten du har valgt er oppløst og kan derfor ikke tildeles oppdrag",
	                MessageDialog.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_UNIT_MISSING) {			
			Utils.showMessage("Begrensning",
	                "Du må først velge en enhet i listen",
	                MessageDialog.INFORMATION_MESSAGE);

		}
		else if(type == MessageBoxType.MESSAGE_NOTHING_ALLOCATED) {			
			Utils.showMessage("Begrensning",
	                "Enheten har ingen oppdrag i kø",
	                MessageDialog.INFORMATION_MESSAGE);

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
	
	private void selectAssignedUnit() {
		
		// get assignment
		IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
		
		// assignment
		if(assignment!=null) {
			// get table
			JTable table = getUnitTable();
			// has table?
			if(table != null) {
				// find unit allocated to current assignment 
				for (int row = 0; row < table.getRowCount(); row++) {
					IUnitIf unit = (IUnitIf)table.getValueAt(row, 0);
					if (unit != null) {
						List list = unit.getAllocatedAssignments();
						if (list!=null && list.contains(assignment)) {
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
		// consume?
		if(!isChangeable()) return;
		
		// consume changes
		setChangeable(false);
		
		// try to get mso object?
		if(getMsoObject()==null) 
			getContentPanel().setSelectedMsoFeature(wp.getMap());
		
		// get assignment
		IAssignmentIf assignment = (IAssignmentIf)getMsoObject();
		
		// update icon
		if(assignment!=null) {
			Enum e = MsoUtils.getType(assignment,true);
			getContentPanel().setCaptionIcon(
					DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(e),"48x48"));
			getContentPanel().setCaptionText("<html>Legg <b>" + 
					MsoUtils.getAssignmentName(assignment, 1).toLowerCase() + 
					"</b> i kø til en enhet i listen" + (assignment instanceof ISearchIf ? 
							"    (<i>mannskapsbehov</i>: <b>" + ((ISearchIf)assignment).getPlannedPersonnel() + "</b>)</html>" : "</html>"));
			getAssignButton().setEnabled(true);
			getReclaimButton().setEnabled(true);
		}
		else {
			getContentPanel().setCaptionIcon(DiskoIconFactory.getIcon("GENERAL.EMPTY", "48x48"));
			getContentPanel().setCaptionText("Du må velge et oppdrag før du kan legge det til køen til enhet");
			getAssignButton().setEnabled(false);
			getReclaimButton().setEnabled(false);
		}		
		
		// get current assigned unit
		selectAssignedUnit();
		
		// resume changes
		setChangeable(true);
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,2"
