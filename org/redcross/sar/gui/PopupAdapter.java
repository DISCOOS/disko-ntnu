package org.redcross.sar.gui;

import java.awt.event.MouseEvent;

import org.redcross.sar.gui.event.DiskoMouseAdapter;

/**
 *
 */
public class PopupAdapter extends DiskoMouseAdapter
{
	final AbstractPopupHandler m_handler;

  	/*========================================================
  	 * Constructors
  	 *======================================================== */
	
    public PopupAdapter(AbstractPopupHandler aPopupHandler)
    {
        m_handler = aPopupHandler;
    }

  	/*========================================================
  	 * MouseListener implementation
  	 *======================================================== */
    
    public void mousePressed(MouseEvent e)
    {
		super.mousePressed(e);        
        maybeShowPopup(e, false);
    }

    public void mouseReleased(MouseEvent e)
    {
    	super.mouseReleased(e);
    	maybeShowPopup(e, false);
    }

  	/*========================================================
  	 * DiskoMouseListener implementation
  	 *======================================================== */
	
	public void mouseDownExpired(MouseEvent e) 
	{
        maybeShowPopup(e, true);
	}
	
  	/*========================================================
  	 * Helper methods
  	 *======================================================== */
	
    private void maybeShowPopup(MouseEvent e, boolean force)
    {
        if (force || e.isPopupTrigger())
        {
            if(m_handler.showPopup(e)) e.consume();
        }

    }
    
}
