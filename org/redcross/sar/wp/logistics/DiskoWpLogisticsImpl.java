package org.redcross.sar.wp.logistics;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import java.lang.instrument.IllegalClassFormatException;
import java.text.MessageFormat;
import java.util.EnumSet;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 */
public class DiskoWpLogisticsImpl extends AbstractDiskoWpModule implements IDiskoWpLogistics
{
    LogisticsPanel m_logisticsPanel;

    public DiskoWpLogisticsImpl(IDiskoRole role) throws IllegalClassFormatException
    {
        super(role);
        initialize();
        if (false)
        {
//        	BuildTestData.createUnits(getMsoModel());
//            BuildTestData.createUnitsAndAssignments(getMsoModel());
            //BuildTestData.createMessages(getMsoModel());
        }
    }

    private void initialize()
    {
        loadProperties("properties");
        assignWpBundle(IDiskoWpLogistics.class);

        m_logisticsPanel = new LogisticsPanel(this);
        layoutComponent(m_logisticsPanel.getPanel());

        // ensure that spesific layers are selectable
        m_logisticsPanel.setLayersSelectable();
        
    }

    public void activated()
    {
    	// forward
        super.activated();

        // setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        EnumSet<DiskoToolType> myTools =
	                EnumSet.of(DiskoToolType.SELECT_FEATURE_TOOL);
	        myTools.add(DiskoToolType.ZOOM_IN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myTools.add(DiskoToolType.PAN_TOOL);
	        myTools.add(DiskoToolType.ZOOM_FULL_EXTENT_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myTools.add(DiskoToolType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myTools.add(DiskoToolType.MAP_TOGGLE_COMMAND);
	        myTools.add(DiskoToolType.SCALE_COMMAND);
	        myTools.add(DiskoToolType.TOC_COMMAND);
	        myTools.add(DiskoToolType.GOTO_COMMAND);
			// forward
			setupNavBar(myTools,true);
		}

		// show map
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DiskoMap map = (DiskoMap) getMap();
				map.setVisible(true);
			}			
		});
				
    }

    public void deactivated()
    {
		// hide map
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(false);
		
    	// forward
        super.deactivated();

    }
    
	public String getCaption() {
		return getText("Caption");
	}
	
    /* (non-Javadoc)
     * @see com.geodata.engine.disko.task.DiskoAp#cancel()
     */
    public void cancel()
    {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.geodata.engine.disko.task.DiskoAp#finish()
     */
    public void finish()
    {
        // TODO Auto-generated method stub
    }


    private String[] options = null;

    public boolean confirmTransfer(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus aTargetStatus, IUnitIf aTargetUnit)
    {
        if (options == null)
        {
            options = new String[]{getText("yes.text"), "Nei"};
        }
        IUnitIf owningUnit = anAssignment.getOwningUnit();
        IAssignmentIf.AssignmentStatus sourceStatus = anAssignment.getStatus();

        String question;
        if (owningUnit == aTargetUnit)
        {
            if (aTargetStatus == IAssignmentIf.AssignmentStatus.ALLOCATED && sourceStatus == aTargetStatus)
            {
                question = "confirm_assignmentTransfer_q4.text";
            } else
            {
                question = "confirm_assignmentTransfer_q3.text";
            }
        } else if (aTargetUnit != null)
        {
            question = "confirm_assignmentTransfer_q2.text";
        } else
        {
            question = "confirm_assignmentTransfer_q1.text";
        }

        String unitNumber = aTargetUnit != null ? aTargetUnit.getUnitNumber() : "";
        question = getText(question);

        int n = JOptionPane.showOptionDialog(m_logisticsPanel.getPanel(),
                MessageFormat.format(question, anAssignment.getNumber(), Internationalization.translate(aTargetStatus), unitNumber),
                getText("confirm_assignmentTransfer.header"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        return n == 0;
    }

    public void showTransferWarning()
    {
        showWarning(getText("transfer_warning.text"));
    }

	public void afterOperationChange()
	{
		super.afterOperationChange();
		m_logisticsPanel.reInitPanel();
	}

	public boolean transfer(IAssignmentIf anAssignment, AssignmentStatus aStatus, IUnitIf aUnit) {
        
		// initialize 
        boolean bSuccess = false;
        AssignmentStatus oldStatus = anAssignment.getStatus();
        
        try {
        	
            // Ask for confirmation and perform transfer
            if (confirmTransfer(anAssignment, aStatus, aUnit)) {
            
		        // suspend for faster transfer
				getMsoModel().suspendClientUpdate();
				
				// allocate assignment to unit?
		        if (aStatus == AssignmentStatus.ALLOCATED){
		        	bSuccess = aUnit.addAllocatedAssignment(anAssignment, null);
		        } 
		        else {
		        	anAssignment.setStatusAndOwner(aStatus, aUnit);
		        	bSuccess = true;
		        }
	
		        // transfer OK?
		        if (bSuccess){
		        	
		        	// update transfer in message log 
		            AssignmentTransferUtilities.createAssignmentChangeMessage(
		            		getMsoManager(), aUnit, anAssignment, oldStatus);
	
		            // comitt changes
		            getMsoModel().commit();
		            /*if (unitTableModel != null)
		            {
		                unitTableModel.scrollToTableCellPosition(tableDropRow);
		            }*/
		        } 
		        else {
		            getMsoModel().rollback();
		            showTransferWarning();
		        }
		        
		        // resume update
				getMsoModel().resumeClientUpdate();
				
            }
        }
        catch (IllegalOperationException e){
        	e.printStackTrace();
        }
        
        // return state
        return bSuccess;
	}

}
