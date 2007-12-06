package org.redcross.sar.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *
 */
public class PopupListener extends MouseAdapter
{
    AbstractPopupHandler m_handler;

    public PopupListener(AbstractPopupHandler aPopupHandler)
    {
        m_handler = aPopupHandler;
    }

    public void mousePressed(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e)
    {
        maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            m_handler.showPopup(e);
        }

    }
}
