package org.redcross.sar.gui.renderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.wp.logistics.UnitTableModel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 */
public class IconRenderer implements Icon
{
    // Fixed design parameters
    private final static int arcDiam = 8;
    private final static int innerTop = 5;
    private final static int innerLeft = 5;

    // Not so very fixed design parameter
    private final Color selectedColor = Color.PINK;

    // Parameters related to source icon
    protected Image m_iconImage;
    private boolean m_isLocatedLeft;

    // Parameters related to current design
    private int m_width = 50;
    private int m_height = 50;
    private float m_iconResizeFactor;
    private String m_iconText;
    private boolean m_isMultiple;
    private boolean m_hasBorder;

    // Selection flag
    private boolean m_isSelected;

    public IconRenderer(Image anIconImage, boolean isLocatedLeft, String anIconText, int aWidth, int aHeight, float aResizeFactor, boolean isMultiple, boolean hasBorder, boolean isSelected)
    {
        m_iconImage = anIconImage;
        m_isLocatedLeft = isLocatedLeft;
        m_iconText = anIconText;
        m_width = aWidth;
        m_height = aHeight;
        m_iconResizeFactor = aResizeFactor;
        m_isMultiple = isMultiple;
        m_hasBorder = hasBorder;
        m_isSelected = isSelected;
    }

    public void setIconImage(Image anIconImage)
    {
        m_iconImage = anIconImage;
    }


    public void setIconText(String anIconText)
    {
        m_iconText = anIconText;
    }

    public void setSelected(boolean isSelected)
    {
        m_isSelected = isSelected;
    }

    public boolean isSelected()
    {
        return m_isSelected;
    }

    public void setMultiple(boolean isMultiple)
    {
        m_isMultiple = isMultiple;
    }

    public void setLocatedLeft(boolean isLocatedLeft)
    {
        m_isLocatedLeft = isLocatedLeft;
    }

    public void setWidth(int aWidth)
    {
        m_width = aWidth;
    }

    public void setHeight(int aHeight)
    {
        m_height = aHeight;
    }

    public void setIconResizeFactor(float aResizeFactor)
    {
        m_iconResizeFactor = aResizeFactor;
    }

    public void setHasBorder(boolean hasBorder)
    {
        m_hasBorder = hasBorder;
    }

    public int getIconWidth()
    {
        return m_width;
    }

    public int getIconHeight()
    {
        return m_height;
    }

    public Dimension getIconSize()
    {
        return new Dimension(m_width,m_height);
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        int dx1, dy1, dx2, dy2;
        int sx1, sy1, sx2, sy2;
        int tx, ty;
        int sw, sh;

        // cast to Graphics2D
        Graphics2D g2d = (Graphics2D) g;

        // Determine if antialiasing is enabled
        RenderingHints rhints = g2d.getRenderingHints();
        boolean antialiasOn = rhints.containsValue(RenderingHints.VALUE_ANTIALIAS_ON);

        // Enable antialiasing for shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);

        if (m_iconImage == null)
        {
            sw = 0;
            sh = 0;
            dx1 = 0;
            dy1 = 0;
            sx1 = 0;
            sy1 = 0;
            tx = m_width / 2 - (int) (5 * m_iconResizeFactor);
            ty = m_height / 2 + (int) (10 * m_iconResizeFactor);
        } else if (m_isLocatedLeft)
        {
        	sw = 45;
            sh = 45;
            dx1 = innerLeft - 1;
            dy1 = innerTop - 2;
            sx1 = innerLeft -3;
            sy1 = innerTop -3;
            tx = m_width - dx1;
            ty = m_height - dx1;
        } else
        {
            sw = 25;
            sh = 30;
            dx1 = m_width - sw - 6;
            dy1 = innerTop + 1;
            sx1 = 0;
            sy1 = 0;
            tx = 5;
            ty = m_height - 5;
        }
        dx2 = dx1 + (int) (sw * m_iconResizeFactor) - 1;
        dy2 = dy1 + (int) (sh * m_iconResizeFactor) - 1;
        sx2 = sx1 + sw - 1;
        sy2 = sy1 + sh - 1;

        g.translate(x, y);
        Graphics2D g2 = (Graphics2D) g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(2));

        Color bgColor;
        if (m_isSelected)
        {
            bgColor = selectedColor;
        } else
        {
            bgColor = Color.WHITE;
        }

        // draw border?
        if (m_hasBorder)
        {
            int rectWidth = m_isMultiple ? m_width - 1 - innerLeft : m_width - 3;
            int rectHeight = m_isMultiple ? m_height - 1 - innerTop : m_height - 3;
            g.setColor(bgColor);
            g2.fillRoundRect(1, 1, rectWidth, rectHeight, arcDiam, arcDiam);
            g.setColor(c.getForeground());
            g2.drawRoundRect(1, 1, rectWidth, rectHeight, arcDiam, arcDiam);
            // draw multiple border?
            if (m_isMultiple)
            {
                g.setColor(bgColor);
                g2.fillRoundRect(innerLeft, innerTop, rectWidth, rectHeight, arcDiam, arcDiam);
                g.setColor(c.getForeground());
                g2.drawRoundRect(innerLeft, innerTop, rectWidth, rectHeight, arcDiam, arcDiam);
            }
        } else
        {
            g.setColor(bgColor);
            g.fillRoundRect(0, 0, m_width, m_height, arcDiam, arcDiam);
        }


        if (m_iconImage != null)
        {
            g.setColor(c.getForeground());
            if (m_hasBorder && m_isMultiple)
            	g.drawImage(m_iconImage, dx1+4, dy1+4, dx2-4, dy2-4, sx1, sy1, sx2, sy2, bgColor, null);
            else
            	g.drawImage(m_iconImage, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgColor, null);
        }

        // resume old stroke
        g2d.setStroke(oldStroke);

        Font oldFont = g.getFont();
        // Font size depends on string length an icon size
        int fontSize = (int) ((m_iconText.length() > 2 ? 15 : 18) * m_iconResizeFactor);
        g.setFont(oldFont.deriveFont(Font.BOLD, fontSize));
        Rectangle2D rc = (g.getFontMetrics()).getStringBounds(m_iconText, g);
        g.setColor(Color.WHITE);
        g.fillRoundRect(tx-(int)rc.getWidth(), ty-(int)rc.getHeight()+4, (int)rc.getWidth(), (int)rc.getHeight()-3,2,2);
        g.setColor(c.getForeground());
        g.drawString(m_iconText, tx-(int)rc.getWidth(), ty-1);
        g.setFont(oldFont);           //Restore font
        g.translate(-x, -y);   //Restore graphics object

        // resume old state
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        		antialiasOn ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public boolean isSelectable()
    {
        return false;
    }

    public void iconSelected()
    {
    }

    public static class UnitIcon extends IconRenderer
    {
        private IUnitIf m_unit;
    	private IconRenderer.IconActionHandler m_actionHandler;

        private final HashMap<IUnitIf.UnitType, Image> m_images = new LinkedHashMap<IUnitIf.UnitType, Image>();

        public UnitIcon(IUnitIf aUnit, boolean isSelected, IconActionHandler anActionHandler)
        {
        	this(aUnit, isSelected, anActionHandler, 50, 50);
        }

        public UnitIcon(IUnitIf aUnit, boolean isSelected, IconActionHandler anActionHandler, int width, int height)
        {
            super(null, true, null, width, height, 1F, false, false, isSelected);
            if (m_images.size() == 0)
            {
                initImageMap();
            }
            m_actionHandler = anActionHandler;
            setUnit(aUnit);
        }

        private void initImageMap()
        {
            IUnitIf.UnitType[] unitTypes = new IUnitIf.UnitType[]{
                    IUnitIf.UnitType.CP,
                    IUnitIf.UnitType.TEAM,
                    IUnitIf.UnitType.DOG,
                    IUnitIf.UnitType.AIRCRAFT,
                    IUnitIf.UnitType.BOAT,
                    IUnitIf.UnitType.VEHICLE
            };

            for (int i = 0; i < unitTypes.length; i++)
            {
                try
                {
                	ImageIcon icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(unitTypes[i]), "48x48");
                	if(icon!=null) {
	                    Image image = icon.getImage();
	                    m_images.put(unitTypes[i], image);
                	}
                }
                catch (Exception e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

        public void setUnit(IUnitIf aUnit)
        {
            m_unit = aUnit;
            Image iconImage = m_images.get(aUnit.getType());
            setIconImage(iconImage);
            setIconText(Integer.toString(aUnit.getNumber()));
        }

        public IUnitIf getUnit()
        {
            return m_unit;
        }

        @Override
        public boolean isSelectable()
        {
            return true;
        }

        @Override
        public void iconSelected()
        {
            if (m_actionHandler == null)
            {
                return;
            }
            m_actionHandler.handleClick(m_unit);
        }
    }

    public static class AssignmentIcon extends IconRenderer
    {

        private IUnitIf m_actUnit;
        private int m_selectorIndex;

        private IAssignmentIf m_assignment;
        private Collection<IAssignmentIf> m_assignments;
        private IconRenderer.IconActionHandler m_actionHandler;

        private final boolean m_singleAssigmentIcon;

        private final HashMap<ISearchIf.SearchSubType, Image> m_searchImages = new LinkedHashMap<ISearchIf.SearchSubType, Image>();

        public AssignmentIcon(IAssignmentIf anAssignment, boolean isSelected, IconActionHandler anActionHandler)
        {
        	this(anAssignment, isSelected, anActionHandler, 50, 50);
        }

        public AssignmentIcon(IAssignmentIf anAssignment, boolean isSelected, IconActionHandler anActionHandler, int width, int height)
        {
            super(null, true, null, width, height, 1.0F, false, true, isSelected);
            initImageMap();
            m_singleAssigmentIcon = true;
            m_actionHandler = anActionHandler;
            setAssignment(anAssignment);
        }

        public AssignmentIcon(IUnitIf aUnit, int aSelectorIndex, boolean isSelected, IconActionHandler anActionHandler)
        {
        	this(aUnit, aSelectorIndex, isSelected, anActionHandler, 50, 50);
        }

        public AssignmentIcon(IUnitIf aUnit, int aSelectorIndex, boolean isSelected, IconActionHandler anActionHandler, int width, int height)
        {
            super(null, true, null, width, height, 1.0F, false, true, isSelected);
            initImageMap();
            m_singleAssigmentIcon = false;
            m_actionHandler = anActionHandler;
            setAssignments(aUnit, aSelectorIndex);
        }

        private void initImageMap()
        {

            ISearchIf.SearchSubType[] assignmentTypes = new ISearchIf.SearchSubType[]{
                    ISearchIf.SearchSubType.LINE,
                    ISearchIf.SearchSubType.PATROL,
                    ISearchIf.SearchSubType.URBAN,
                    ISearchIf.SearchSubType.SHORELINE,
                    ISearchIf.SearchSubType.MARINE,
                    ISearchIf.SearchSubType.AIR,
                    ISearchIf.SearchSubType.DOG
            };

            for (int i = 0; i < assignmentTypes.length; i++)
            {
            	ImageIcon icon = DiskoIconFactory.getIcon(DiskoEnumFactory.getIcon(assignmentTypes[i]), "48x48");
            	if(icon!=null) {
                    Image image = icon.getImage();
                    m_searchImages.put(assignmentTypes[i], image);
                }
            }
        }

        private void setAssignmentIcon(IAssignmentIf anAssignment, boolean isMultiple)
        {
            if (anAssignment != null)
            {
                if (anAssignment instanceof ISearchIf)
                {
                    setIconImage(m_searchImages.get(((ISearchIf) anAssignment).getSubType()));
                } else
                {
                    setIconImage(m_searchImages.get(ISearchIf.SearchSubType.LINE));
                }
            } else
            {
                setIconImage(null);
            }
            setMultiple(isMultiple);
        }

        public boolean isSingleAssigmentIcon() {
        	return m_singleAssigmentIcon;
        }

        public void setAssignment(IAssignmentIf anAssignment)
        {
            m_assignment = anAssignment;
            setIconText(Integer.toString(anAssignment.getNumber()));
            setAssignmentIcon(anAssignment, false);
        }

        public void setAssignments(IUnitIf aUnit, int aSelectorIndex)
        {
            m_actUnit = aUnit;
            m_selectorIndex = aSelectorIndex;
            m_assignments = UnitTableModel.getSelectedAssignments(m_actUnit, m_selectorIndex);
            if (m_assignments.size() == 0)
            {
                setIconText("");
                setIconImage(null);
                setHasBorder(false);
            } else
            {
                Iterator<IAssignmentIf> iterator = m_assignments.iterator();
                IAssignmentIf asg = iterator.next();
                setIconText(Integer.toString(asg.getNumber()));
                setAssignmentIcon(asg, m_assignments.size() > 1);
                setHasBorder(true);
            }
        }

        public Collection<IAssignmentIf> getAssignmentList()
        {
            return m_assignments;
        }

        public boolean isSelectable()
        {
            return m_iconImage != null;
        }

        public IAssignmentIf getAssignment()
        {
            if (m_singleAssigmentIcon)
            {
                return m_assignment;
            }
            if (m_assignments != null && m_assignments.size() > 0)
            {
                return m_assignments.iterator().next();
            }
            return null;
        }

        @Override
        public void iconSelected()
        {
            if (m_actionHandler == null){
                return;
            }
            if (m_singleAssigmentIcon)            {
                m_actionHandler.handleClick(m_assignment);
            }
            else {
            	// get selected assignments
                m_assignments = UnitTableModel.getSelectedAssignments(m_actUnit, m_selectorIndex);
                // nothing selected?
                if (m_assignments.size() == 0) {
                    return;
                } else if (m_assignments.size() == 1)
                {
                    m_actionHandler.handleClick(m_assignments.iterator().next());
                } else
                {
                    m_actionHandler.handleClick(m_actUnit, m_selectorIndex);
                }
            }
        }
    }

    public static class InfoIcon extends IconRenderer
    {

        public InfoIcon(String anIconText, boolean isSelected)
        {
            this(anIconText, isSelected, 50, 50);
        }

        public InfoIcon(String anIconText, boolean isSelected, int width, int height)
        {
            super(null, true, null, width, height, 1.0F, false, false, isSelected);
            setInfo(anIconText);
        }

        public void setInfo(String anIconText)
        {
            if (anIconText.length() > 0)
            {
                setIconText("!");
            } else
            {
                setIconText("");
            }
        }

        @Override
        public boolean isSelectable()
        {
            return false;
        }
    }

    public interface IconActionHandler
    {
        public void handleClick(IUnitIf aUnit);

        public void handleClick(IAssignmentIf anAssignment);

        public void handleClick(IUnitIf aUnit, int aSelectorIndex);
    }

}