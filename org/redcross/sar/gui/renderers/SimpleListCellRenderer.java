package org.redcross.sar.gui.renderers;

import org.redcross.sar.gui.factory.DiskoStringFactory;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class SimpleListCellRenderer extends JLabel implements ListCellRenderer
{


    private static final long serialVersionUID = 1L;

    private ResourceBundle bundle;

    public SimpleListCellRenderer()
    {
        super.setOpaque(true);
    }

    /**
     * Create a renderer that is fetching international texts from a {@link ResourceBundle} before ordinary text lookup.
     * @param aBundle The ResourceBundle to use.
     *
     */
    public SimpleListCellRenderer(ResourceBundle aBundle)
    {
        this();
        bundle = aBundle;
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus)
    {

        setText(DiskoStringFactory.translate(value,bundle));
        if (isSelected)
        {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else
        {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
