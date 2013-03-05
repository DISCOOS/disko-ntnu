package org.redcross.sar.gui.dnd;

import org.redcross.sar.gui.renderer.ObjectIcon;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.util.MsoUtils;

import java.text.MessageFormat;

/**
 * A subclass of {@link MsoDragAndDropLabel} that supports assignment transfer using drag-and-drop (DnD).
 * 
 * @author kenneth
 */

public class AssignmentDragAndDropLabel extends MsoDragAndDropLabel<IAssignmentIf>
{

	private static final long serialVersionUID = 1L;

	//private static DataFlavor m_flavor;

    public AssignmentDragAndDropLabel(ObjectIcon.AssignmentIcon anIcon, MsoLabelActionHandler<IAssignmentIf> anActionHandler)
    {
        super(anIcon, anActionHandler);
        //initialize();
    }

    public AssignmentDragAndDropLabel(IAssignmentIf anAssignment, MsoLabelActionHandler<IAssignmentIf> anActionHandler)
    {
        super(anAssignment,anActionHandler);
        //initialize();
    }
    
    
    
    
    /*
    private void initialize()
    {
		// initialize
        setEnabled(true);
    	try {
	    	// create flavor
	    	m_flavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=org.redcross.sar.mso.data.IAssignmentIf");
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    	// create gesture recognizer
    	DragSource ds = DragSource.getDefaultDragSource();
    	ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE,
    			new IconDragGestureListener(new AssignmentDragDropLabelDragSourceListener()));

    	// create drop target
    	setDropTarget(new DropTarget(this, new AssignmentDragDropLabelDropTargetListener()));

    }
    */
	
	@Override
    protected String getObjectText() {
    	String text = null;
    	IAssignmentIf anObject = getMsoObject();
    	if(anObject!=null) 
    	{
	        if (anObject.getStatus() == IAssignmentIf.AssignmentStatus.QUEUED)
	        {
	            text = MessageFormat.format("{0}: {1}", anObject.getPrioritySequence(), MsoUtils.getAssignmentName(anObject,1));
	        } else
	        {
	        	text = MsoUtils.getAssignmentName(anObject,1);
	        }
    	}
    	return text;
    }
}
