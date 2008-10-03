package org.redcross.sar.wp.logistics;

import org.redcross.sar.app.IDiskoRole;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
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
        m_logisticsPanel.setSelectableLayers();
              
    }

	public void activate(IDiskoRole role) {
		
		// forward
		super.activate(role);

        // setup of navbar needed?
		if(isNavBarSetupNeeded()) {
			// get tool set 
	        List<Enum<?>> myButtons = new ArrayList<Enum<?>>();	  
	        myButtons.add(MapToolType.SELECT_TOOL);
	        myButtons.add(MapToolType.ZOOM_IN_TOOL);
	        myButtons.add(MapToolType.ZOOM_OUT_TOOL);
	        myButtons.add(MapToolType.PAN_TOOL);
	        myButtons.add(MapCommandType.ZOOM_FULL_EXTENT_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND);
	        myButtons.add(MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND);
	        myButtons.add(MapCommandType.MAP_TOGGLE_COMMAND);
	        myButtons.add(MapCommandType.SCALE_COMMAND);
	        myButtons.add(MapCommandType.TOC_COMMAND);
	        myButtons.add(MapCommandType.GOTO_COMMAND);
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

    public boolean confirmTransfer(IAssignmentIf anAssignment, IAssignmentIf.AssignmentStatus toStatus, IUnitIf toUnit)
    {
        if (options == null)
        {
            options = new String[]{getBundleText("yes.text"), "Nei"};
        }
        IUnitIf owningUnit = anAssignment.getOwningUnit();
        IAssignmentIf.AssignmentStatus sourceStatus = anAssignment.getStatus();

        // get names
        String staName = Internationalization.translate(toStatus);
        String tarName = toUnit != null ? MsoUtils.getUnitName(toUnit,false) : "";
        String assName = MsoUtils.getAssignmentName(anAssignment,1);

        String question;
        if (owningUnit == toUnit)
        {
            if (toStatus == IAssignmentIf.AssignmentStatus.QUEUED && sourceStatus == toStatus)
            {
                // change assignment priority sequence in queue
                 question = String.format(getBundleText("confirm_assignmentTransfer_reorder.text"), tarName);
            	
            } else
            {
                // move assignment within same unit
                question = String.format(getBundleText("confirm_assignmentTransfer_move.text"), assName, staName);
            }
        } else if (toUnit != null)
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

	public boolean transfer(IAssignmentIf anAssignment, AssignmentStatus toStatus, IUnitIf toUnit) {
        
		// initialize 
        boolean bCommit = false;
        AssignmentStatus oldStatus = anAssignment.getStatus();
        
        try {
        	
            // Ask for confirmation and perform transfer
            if (confirmTransfer(anAssignment, toStatus, toUnit)) {
            
		        // suspend for faster transfer
				getMsoModel().suspendClientUpdate();
				
				// initialize
		        boolean bCreate = true;
				
				// allocate assignment to unit?
		        switch(toStatus) {
		        case READY:
		        	anAssignment.setOwningUnit(AssignmentStatus.READY, null);
		        	bCommit = true;
		        	bCreate = false;
		        	break;
		        case QUEUED:
		        	bCommit = toUnit.enqueueAssignment(anAssignment, null);
		        	break;
		        case ALLOCATED:
		        	toUnit.allocateAssignment(anAssignment);
		        	bCommit = true;
		        	break;
		        case EXECUTING:
		        	toUnit.startAssignment(anAssignment);
		        	bCommit = true;
		        	break;
		        case FINISHED:
		        	toUnit.finishAssignment(anAssignment);
		        	bCommit = true;
		        	break;
		        }
	
		        // transfer OK?
		        if (bCommit){
		        	
		        	// update transfer in message log?
		        	if(bCreate) {
			            AssignmentTransferUtilities.createAssignmentChangeMessage(
			            		getMsoManager(), toUnit, anAssignment, oldStatus);
		        	}
	
		            // commit changes
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
        return bCommit;
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
		public void beforeDone() {
			
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
