/**
 * IconDragGestureListener is mainly fetched from HACK #65
 * Drag-and-Drop with files by Joshua Marinacci & Chris Adamson 
 * at O'Reilly.
 */
package org.redcross.sar.gui.dnd;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.image.BufferedImage;

import javax.swing.Icon;

import org.redcross.sar.gui.renderers.IconRenderer;


/**
 * @author kennetgu
 *
 */
public class IconDragGestureListener extends DragSourceAdapter implements
		DragGestureListener {

	private Icon m_icon = null;
	private Cursor m_cursor = null;
	private IIconDragSource m_source = null;
	
	/**
	 * Create
	 */
	public IconDragGestureListener(IIconDragSource source) {
		// save 
		m_source = source;
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent de) {
		try {
			
			// has sink?
			if(m_source!=null) {
			
	    		// get transferable data
	    		Transferable data = m_source.getTransferable();
	    		
	    		// has data to transfer?
	    		if(data!=null) {
			    		
					// get icon?
					m_icon = m_source.getIcon();
					
					// has icon?
			    	if(m_icon!=null) {
			    		
			    		// get icon as buffered image
				    	Toolkit tk = Toolkit.getDefaultToolkit();
				    	BufferedImage buff = new BufferedImage(m_icon.getIconWidth(),
				    			m_icon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
				    	// IconRenderer?
				    	if(m_icon instanceof IconRenderer) {
					    	IconRenderer icon = (IconRenderer)m_icon;
					    	// get flag
					    	boolean isSelected = icon.isSelected();
					    	// deselect
					    	icon.setSelected(false);
				    		// paint on buffer
				    		icon.paintIcon(m_source.getComponent(),buff.getGraphics(), 0, 0);
				    		// restore state
				    		icon.setSelected(isSelected);
				    	}
				    	else {
				    		// paint on buffer
				    		m_icon.paintIcon(m_source.getComponent(),buff.getGraphics(), 0, 0);
				    	}
				    	
				    	
				    	// select icon display method
				    	if(DragSource.isDragImageSupported()) {
				    		de.startDrag(DragSource.DefaultMoveDrop, buff, new Point(0,0), data, this);
				    	}
				    	else {
				    		// get cursor
				    		m_cursor = tk.createCustomCursor(buff, new Point(0,0), "icon");		    		
				    		de.startDrag(m_cursor, null, new Point(0,0), data, this);		    	
				    	}
		    		}
			    	else {
			    		// default drag operation (no icon is defined)
			    		de.startDrag(DragSource.DefaultMoveDrop, data, this);
			    	}
		    	}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
		
	@Override
	public void dragEnter(DragSourceDragEvent e) {
		DragSourceContext ctx = e.getDragSourceContext();
		ctx.setCursor(m_cursor);
	}

	@Override
	public void dragExit(DragSourceEvent e) {
		DragSourceContext ctx = e.getDragSourceContext();
		ctx.setCursor(DragSource.DefaultMoveNoDrop);	
	}
}
