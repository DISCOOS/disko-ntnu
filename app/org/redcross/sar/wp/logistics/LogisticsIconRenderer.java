package org.redcross.sar.wp.logistics;

import java.awt.Component;

import org.redcross.sar.gui.DiskoRoundBorder;
import org.redcross.sar.gui.renderer.ObjectIcon;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 */
public class LogisticsIconRenderer extends JLabel implements TableCellRenderer
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Border firstCellBorder = new DiskoRoundBorder(2, 8, true, true, true, false);
    private static final Border middleCellBorder = new DiskoRoundBorder(2, 8, true, false, true, false);
    private static final Border lastCellBorder = new DiskoRoundBorder(2, 8, true, false, true, true);

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        if (value instanceof ObjectIcon)
        {
            ObjectIcon iconValue = (ObjectIcon) value;
            if (isSelected && !iconValue.isSelectable())
            {
                isSelected = false;
                table.getSelectionModel().clearSelection();
            }
            if (row == 0)
            {
                TableColumn col = table.getColumnModel().getColumn(column);
                int oldWidth = col.getWidth();
                int iconWidth = iconValue.getIconWidth();
                if (oldWidth < iconWidth + 4)
                {
                    col.setMaxWidth(iconWidth + 16);
                    col.setMinWidth(iconWidth);
                    col.setPreferredWidth(iconWidth + 16);
                }
                if (column == 0)
                {
                    // Only change if different heights. Otherwise the Drag and Drop Icon apperance will not work for anything (Java 1.6.0).
                    // Just try if you don't believe it.
                    int oldHeight = table.getRowHeight();
                    int newHeight = iconValue.getIconHeight() + 16;
                    if (oldHeight != newHeight)
                    {
                        table.setRowHeight(iconValue.getIconHeight() + 16);
                    }
                }
            }
            iconValue.setSelected(isSelected);// && hasFocus);
            setText("");
            setIcon(iconValue);
        } else
        {
            setText(value!=null ? value.toString() : "");
            setIcon(null);
        }
        if (column == 0)
        {
            setBorder(firstCellBorder);
        } else if (column == table.getColumnCount() - 1)
        {
            setBorder(lastCellBorder);
        } else
        {
            setBorder(middleCellBorder);
        }
        setHorizontalAlignment(SwingConstants.CENTER);
        return this;
    }

    public static class InfoIconRenderer extends LogisticsIconRenderer
    {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}


