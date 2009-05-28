package org.redcross.sar.gui.dnd;

import org.redcross.sar.gui.renderer.ObjectIcon.MsoIcon;
import org.redcross.sar.mso.data.IMsoObjectIf;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * A abstract subclass of {@link MsoLabel} that supports data transfer using drag-and-drop (DnD).
 */

public abstract class MsoDragAndDropLabel<M extends IMsoObjectIf> extends MsoLabel<M>
{

	private static final long serialVersionUID = 1L;

    public MsoDragAndDropLabel(MsoIcon<M> anIcon, MsoLabelActionHandler<M> anActionHandler)
    {
        super(anIcon, anActionHandler);
        initialize();
    }

    public MsoDragAndDropLabel(M anObject, MsoLabelActionHandler<M> anActionHandler)
    {
        super(anObject,anActionHandler);
        initialize();
    }

    private void initialize()
    {
		// prepare
        setEnabled(true);

    	// create gesture recognizer
    	DragSource ds = DragSource.getDefaultDragSource();
    	ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE,
    			new IconDragGestureListener(new MsoDragDropLabelDragSourceListener()));

    	// create drop target
    	setDropTarget(new DropTarget(this, new MsoDragDropLabelDropTargetListener()));

    }
    
    /* ===================================================================
     * Inner classes
     * =================================================================== */
    
	class MsoDragDropLabelDragSourceListener extends DiskoDragSourceAdapter {

		@Override
		public Component getComponent() {
			return MsoDragAndDropLabel.this;
		}

		@Override
		public Transferable getTransferable() {
			return new TransferableMsoObject<M>(getMsoObject());
		}
		
		@Override
		public Icon getIcon() {
			return ((JLabel)MsoDragAndDropLabel.this).getIcon();
		}
		
	}

	class MsoDragDropLabelDropTargetListener extends DiskoDropTargetAdapter {

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
