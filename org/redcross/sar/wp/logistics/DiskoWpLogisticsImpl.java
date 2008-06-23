package org.redcross.sar.wp.logistics;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.tool.IDiskoTool.DiskoToolType;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;
import org.redcross.sar.mso.util.AssignmentTransferUtilities;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalOperationException;
import org.redcross.sar.wp.AbstractDiskoWpModule;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 */
public class DiskoWpLogisticsImpl extends AbstractDiskoWpModule implements IDiskoWpLogistics
{
    LogisticsPanel m_logisticsPanel;

    public DiskoWpLogisticsImpl() throws IllegalClassFormatException
    {
    	// forward role to abstract class
        super();
        
        // initialize GUI
        initialize();
        
    }

    private void initialize()
    {

        // attach class resource bundle
        assignWpBundle(IDiskoWpLogistics.class);

        // install map
        installMap();
        
        // get logistic panel. This panel implements the gui
        m_logisticsPanel = new LogisticsPanel(this);
        
        // add panel as main wp component
        layoutComponent(m_logisticsPanel.getPanel());

        // ensure that wp spesific layers are selectable
        m_logisticsPanel.setLayersSelectable();
              
    }

	public void activate(IDiskoRole role) {
		
		// forward
		super.activate(role);

        // setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();	  
	        myButtons.add(DiskoToolType.SELECT_FEATURE_TOOL);
	        myButtons.add(DiskoToolType.ZOOM_IN_TOOL);
	        myButtons.add(DiskoToolType.ZOOM_OUT_TOOL);
	        myButtons.add(DiskoToolType.PAN_TOOL);
	        myButtons.add(DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(DiskoCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(DiskoCommandType.SCALE_COMMAND);
	        myButtons.add(DiskoCommandType.TOC_COMMAND);
	        myButtons.add(DiskoCommandType.GOTO_COMMAND);
			// forward
			setupNavBar(myButtons,true);
		}

		// show map
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				DiskoMap map = (DiskoMap) getMap();
				map.setVisible(true);
			}			
		});
				
    }

    public void deactivate()
    {
		// hide map
		DiskoMap map = (DiskoMap) getMap();
		map.setVisible(false);
		
    	// forward
        super.deactivate();

    }
    
	public String getCaption() {
		return getBundleText("LOGISTICS");
	}
	
    private String[] options = null;

    public boolean confirmTransfer(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus aTargetStatus, IUnitIf aTargetUnit)
    {
        if (options == null)
        {
            options = new String[]{getBundleText("yes.text"), "Nei"};
        }
        IUnitIf owningUnit = anAssignment.getOwningUnit();
        IAssignmentIf.AssignmentStatus sourceStatus = anAssignment.getStatus();

        // get names
        String staName = Internationalization.translate(aTargetStatus);
        String tarName = aTargetUnit != null ? MsoUtils.getUnitName(aTargetUnit,false) : "";
        String assName = MsoUtils.getAssignmentName(anAssignment,1);

        String question;
        if (owningUnit == aTargetUnit)
        {
            if (aTargetStatus == IAssignmentIf.AssignmentStatus.QUEUED && sourceStatus == aTargetStatus)
            {
                // change assignment priority sequence in queue
                 question = String.format(getBundleText("confirm_assignmentTransfer_reorder.text"), tarName);
            	
            } else
            {
                // move assignment within same unit
                question = String.format(getBundleText("confirm_assignmentTransfer_move.text"), assName, staName);
            }
        } else if (aTargetUnit != null)
        {
            // replace units
            question = String.format(getBundleText("confirm_assignmentTransfer_replace.text"), assName, tarName, staName);
        } else
        {
            // revert assignment to status
            question = String.format(getBundleText("confirm_assignmentTransfer_revert.text"), assName, staName);
        }

        // MessageFormat.format(question, MsoUtils.getAssignmentName(anAssignment,1), Internationalization.translate(aTargetStatus), targetName)
        
        // prompt user
        int ans = JOptionPane.showOptionDialog(m_logisticsPanel.getPanel(),
                question,
                getBundleText("confirm_assignmentTransfer.header"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        return ans == 0;
    }

    public void showTransferWarning()
    {
        showWarning(getBundleText("transfer_warning.text"));
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
		        switch(aStatus) {
		        case QUEUED:
		        	bSuccess = aUnit.addAllocatedAssignment(anAssignment, null);
		        	break;
		        case ASSIGNED:
		        	AssignmentTransferUtilities.unitAssignAssignment(aUnit, anAssignment);
		        	bSuccess = true;
		        	break;
		        case EXECUTING:
		        	AssignmentTransferUtilities.unitStartAssignment(aUnit, anAssignment);
		        	bSuccess = true;
		        	break;
		        case FINISHED:
		        	AssignmentTransferUtilities.unitCompleteAssignment(aUnit, anAssignment);
		        	bSuccess = true;
		        	break;
		        }
	
		        // transfer OK?
		        if (bSuccess){
		        	
		        	// update transfer in message log 
		            AssignmentTransferUtilities.createAssignmentChangeMessage(
		            		getMsoManager(), aUnit, anAssignment, oldStatus);
	
		            // comitt changes
		            commit();
		            
		        } 
		        else {
		            rollback();
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
	
    @Override
	public boolean commit() {
		// TODO Auto-generated method stub
		return doCommitWork();
	}

	@Override
	public boolean rollback() {
		// TODO Auto-generated method stub
		return doRollbackWork();
	}

    
	private boolean doCommitWork() {
		try {
			// forward work
			DiskoWorkPool.getInstance().schedule(new LogisticsWork(1));
			// do work
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean doRollbackWork() {
		try {
			DiskoWorkPool.getInstance().schedule(new LogisticsWork(2));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}	
	
	private class LogisticsWork extends ModuleWork<Boolean> {

		private int m_task = 0;
		
		/**
		 * Constructor
		 * 
		 * @param task
		 */
		LogisticsWork(int task) throws Exception {
			super();
			// prepare
			m_task = task;
		}
		
		@Override
		public Boolean doWork() {
			try {
				// dispatch task
				switch(m_task) {
				case 1: commit(); return true;
				case 2: rollback(); return true;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return false;
		}
		

		@Override
		public void done() {
			
			try {
				// dispatch task
				switch(m_task) {
				case 1: fireOnWorkCommit(); break;
				case 2: fireOnWorkRollback(); break;
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
			// do the rest
			super.done();
		}
		
		private void commit() {
			try{
				getMsoModel().commit();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void rollback() {
			try{
				getMsoModel().rollback();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}		

}
