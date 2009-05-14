package org.redcross.sar.gui.renderer;

import org.redcross.sar.gui.IStringConverter;
import org.redcross.sar.gui.factory.DiskoStringFactory;

import java.awt.Component;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class BundleListCellRenderer extends DefaultListCellRenderer implements IStringConverter
{

    private static final long serialVersionUID = 1L;

    private ResourceBundle bundle;

    public BundleListCellRenderer()
    {
        super.setOpaque(true);
    }

    /**
     * Create a renderer that is fetching international texts from a {@link ResourceBundle} before ordinary text lookup.
     * @param aBundle The ResourceBundle to use.
     *
     */
    public BundleListCellRenderer(ResourceBundle aBundle)
    {
        this();
        bundle = aBundle;
    }

    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus)
    {

    	setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        setText(toString(value));
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

	@Override
	public String toString(Object value) {
		return DiskoStringFactory.translate(value,bundle);
	}
        
}
