package org.redcross.sar.wp.logistics;
/*
 * AssignmentDragDropLabel.java is used by the 1.4 DragLabelDemo.java example.
 */

import org.redcross.sar.gui.dnd.AssignmentTransferable;
import org.redcross.sar.gui.dnd.DiskoDragSourceAdapter;
import org.redcross.sar.gui.dnd.DiskoDropTargetAdapter;
import org.redcross.sar.gui.dnd.IDiskoDropTarget;
import org.redcross.sar.gui.dnd.IconDragGestureListener;
import org.redcross.sar.gui.renderer.IconRenderer;
import org.redcross.sar.mso.data.IAssignmentIf;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A subclass of {@link AssignmentLabel} that supports Data Transfer using drag-and-drop (DnD).
 */

public class AssignmentDragDropLabel extends AssignmentLabel
{

	private static final long serialVersionUID = 1L;
	
	private static DataFlavor m_flavor = null;
	
    public AssignmentDragDropLabel(IconRenderer.AssignmentIcon anIcon, AssignmentLabelActionHandler anActionHandler)
    {
        super(anIcon, anActionHandler);
        initialize();
    }

    public AssignmentDragDropLabel(IAssignmentIf anAssignment, AssignmentLabelActionHandler anActionHandler)
    {
        super(anAssignment,anActionHandler);
        initialize();
    }

    private void initialize()
    {
    	try {
    		// initialize
            setEnabled(true);
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
	
	class AssignmentDragDropLabelDragSourceListener extends DiskoDragSourceAdapter {
		public Component getComponent() {
			return AssignmentDragDropLabel.this;
		}
	
		public Transferable getTransferable() {
			return new AssignmentTransferable(getAssignment());
		}
		@Override
		public Icon getIcon() {
			return ((JLabel)AssignmentDragDropLabel.this).getIcon();
		}
	}

	class AssignmentDragDropLabelDropTargetListener extends DiskoDropTargetAdapter {
		
		@Override
		public void dragOver(DropTargetDragEvent e) {
			// forward
			dragEnter(e);
		}

		@Override
		public void dragEnter(DropTargetDragEvent e) {
			// always allow drag
			return;
		}

		@Override
		public void drop(DropTargetDropEvent e) {
			// forward
			transfer(e.getTransferable());			
		}
		
		public boolean transfer(Transferable data) {
			// get data
			try{
        		// get source
        		Component comp = getParent();
        		// check if component is a valid target
        		if(comp instanceof IDiskoDropTarget) {
        			// get target
        			IDiskoDropTarget target = (IDiskoDropTarget)comp;
        			// forward to source
	        		return target.transfer(data);
        		}	        		
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// can not transfer
			return false;    	
		}
		
	}
}
