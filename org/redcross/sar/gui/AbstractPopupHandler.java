package org.redcross.sar.gui;

import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

/**
 *  Generic handler for popup events
 */
public abstract class AbstractPopupHandler
{
    /**
     * Get the related popup menu
     * @param e The event that is triggered by the popup action
     * @return The actual menu, <code>null<code/> if no menu exists.
     */
    protected abstract JPopupMenu getMenu(MouseEvent e);

    /**
     * Show the popup menu.
     * @param e The event that is triggered by the popup action
     */
    public void showPopup(MouseEvent e)
    {
        JPopupMenu menu = getMenu(e);
        if (menu != null)
        {
            menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
}
