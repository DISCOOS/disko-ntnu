package org.redcross.sar.gui.renderer;

import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IAssignmentIf.AssignmentStatus;

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
public class ObjectIcon implements Icon
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

    public ObjectIcon(Image anIconImage, boolean isLocatedLeft, String anIconText, int aWidth, int aHeight, float aResizeFactor, boolean isMultiple, boolean hasBorder, boolean isSelected)
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
    
    public abstract static class MsoIcon<M extends IMsoObjectIf> extends ObjectIcon 
    {
		protected M m_msoObject;
		protected ObjectIcon.MsoIconActionHandler m_actionHandler;
        
        protected MsoIcon(M msoObject, ObjectIcon.MsoIconActionHandler actionHandler, 
        		Image anIconImage, boolean isLocatedLeft,
				String anIconText, int width, int height, float resizeFactor,
				boolean isMultiple, boolean hasBorder, boolean isSelected) {
        	// forward
			super(anIconImage, isLocatedLeft, anIconText, width, height, resizeFactor,
					isMultiple, hasBorder, isSelected);
			// prepare
			m_msoObject = msoObject;
			m_actionHandler = actionHandler;
		}
        
        public M getMsoObject() {
        	return m_msoObject;
        }        
        
        public void setMsoObject(M msoObject)
        {
            m_msoObject = msoObject;
            setIconImage(getIconImage());
            setIconText(getIconText());
        }
        
        protected abstract String getIconText(); 
        protected abstract Image getIconImage();         
    	
    }

    public static class UnitIcon extends MsoIcon<IUnitIf>
    {

        private static final HashMap<IUnitIf.UnitType, Image> m_images = new LinkedHashMap<IUnitIf.UnitType, Image>();

        static {
        	initImageMap();
        }
        
        private static void initImageMap()
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
        
        public UnitIcon(IUnitIf aUnit, MsoIconActionHandler anActionHandler, boolean isSelected)
        {
        	this(aUnit, anActionHandler, isSelected, 50, 50);
        }

        public UnitIcon(IUnitIf aUnit, MsoIconActionHandler anActionHandler, boolean isSelected, int width, int height)
        {
        	// forward
            super(aUnit, anActionHandler, null, true, null, width, height, 1F, false, false, isSelected);
            
            // prepare handler
            m_actionHandler = anActionHandler;
            
            // set object
            setMsoObject(aUnit);
            
        }

        protected String getIconText() 
        {
        	return Integer.toString(getMsoObject().getNumber());
        }
        
        protected Image getIconImage()
        {
        	return m_images.get(getMsoObject().getType());
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
            m_actionHandler.handleClick(m_msoObject);
        }
    }
    
    public static class PersonnelIcon extends MsoIcon<IPersonnelIf>
    {

        private static final Image m_image = DiskoIconFactory.getIcon("GENERAL.PERSONNEL", "48x48").getImage();
        
        public PersonnelIcon(IPersonnelIf aPersonnel, MsoIconActionHandler anActionHandler, boolean isSelected)
        {
        	this(aPersonnel, anActionHandler, isSelected, 50, 50);
        }

        public PersonnelIcon(IPersonnelIf aPersonnel, MsoIconActionHandler anActionHandler, boolean isSelected, int width, int height)
        {
        	// forward
            super(aPersonnel, anActionHandler, null, true, null, width, height, 1F, false, false, isSelected);
            
            // prepare handler
            m_actionHandler = anActionHandler;
            
            // set object
            setMsoObject(aPersonnel);
            
        }

        protected String getIconText() 
        {
        	return "";
        }
        
        protected Image getIconImage()
        {
        	return m_image;
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
            m_actionHandler.handleClick(m_msoObject);
        }
    }    

    public static class AssignmentIcon extends MsoIcon<IAssignmentIf>
    {

        private static final HashMap<ISearchIf.SearchSubType, Image> m_searchImages = new LinkedHashMap<ISearchIf.SearchSubType, Image>();

        static {
        	initImageMap();
        }
        
        private static void initImageMap()
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
        
        private IUnitIf m_owningUnit;
        private int m_aSelectorIndex;

        private final boolean m_singleAssigmentIcon;
        
        private Collection<IAssignmentIf> m_assignments;
        
        public AssignmentIcon(IAssignmentIf anAssignment, MsoIconActionHandler anActionHandler, boolean isSelected)
        {
        	this(anAssignment, anActionHandler, isSelected, 50, 50);
        }

        public AssignmentIcon(IAssignmentIf anAssignment, MsoIconActionHandler anActionHandler, boolean isSelected, int width, int height)
        {
        	// forward
            super(anAssignment, anActionHandler, null, true, null, width, height, 1.0F, false, true, isSelected);
            
            // prepare
            m_singleAssigmentIcon = true;

            // set object
            setMsoObject(anAssignment);
            
        }

        public AssignmentIcon(IUnitIf aUnit, MsoIconActionHandler anActionHandler, int aSelectorIndex, boolean isSelected)
        {
        	this(aUnit, anActionHandler, aSelectorIndex, isSelected, 50, 50);
        }

        public AssignmentIcon(IUnitIf aUnit, MsoIconActionHandler anActionHandler, int aSelectorIndex, boolean isSelected, int width, int height)
        {
        	// forward
            super(null, anActionHandler, null, true, null, width, height, 1.0F, false, true, isSelected);
            // prepare
            m_singleAssigmentIcon = false;
            m_actionHandler = anActionHandler;
            // set objects
            setAssignments(aUnit, aSelectorIndex);
        }

        private Image getAssignmentIcon(IAssignmentIf anAssignment)
        {
        	Image image = null;
            if (anAssignment != null)
            {
                if (anAssignment instanceof ISearchIf)
                {
                	image = m_searchImages.get(((ISearchIf) anAssignment).getSubType());
                } else
                {
                	image = m_searchImages.get(ISearchIf.SearchSubType.LINE);
                }
            }
            return image;
        }

        public boolean isSingleAssigmentIcon() {
        	return m_singleAssigmentIcon;
        }
        
        @Override
        public IAssignmentIf getMsoObject()
        {
            if (m_singleAssigmentIcon)
            {
                return m_msoObject;
            }
            if (m_assignments != null && m_assignments.size() > 0)
            {
                return m_assignments.iterator().next();
            }
            return null;
        }

        public void setMsoObject(IAssignmentIf anAssignment)
        {
            m_msoObject = anAssignment;
            setIconText(Integer.toString(anAssignment.getNumber()));
            setIconImage(getAssignmentIcon(anAssignment));
            setMultiple(false);
        }
        
        @Override
		protected Image getIconImage() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected String getIconText() {
			return Integer.toString(getMsoObject().getNumber());
		}

		public void setAssignments(IUnitIf aUnit, int aSelectorIndex)
        {
            m_owningUnit = aUnit;
            m_aSelectorIndex = aSelectorIndex;
            m_assignments = m_owningUnit.getAssignments(getAssignmentStatus());
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
                setIconImage(getAssignmentIcon(asg));
                setMultiple(m_assignments.size() > 1);
                setHasBorder(true);
            }
        }
		
		private AssignmentStatus getAssignmentStatus() {
			switch(m_aSelectorIndex) {
		    	case 0: return AssignmentStatus.QUEUED;
		    	case 1: return AssignmentStatus.ALLOCATED;
		    	case 2: return AssignmentStatus.EXECUTING;
		    	default: return AssignmentStatus.FINISHED;
			}
		}

        public Collection<IAssignmentIf> getAssignments()
        {
            return m_assignments;
        }

        public boolean isSelectable()
        {
            return m_iconImage != null;
        }

        @Override
        public void iconSelected()
        {
            if (m_actionHandler == null){
                return;
            }
            if (m_singleAssigmentIcon)            
            {
                m_actionHandler.handleClick(m_msoObject);
            }
            else {
            	// get selected assignments
                m_assignments = m_owningUnit.getAssignments(getAssignmentStatus());
                // nothing selected?
                if (m_assignments.size() == 0) {
                    return;
                } else if (m_assignments.size() == 1)
                {
                    m_actionHandler.handleClick(m_assignments.iterator().next());
                } else
                {
                    m_actionHandler.handleClick(m_owningUnit, m_aSelectorIndex);
                }
            }
        }
    }

    public static class InfoIcon extends ObjectIcon
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

    public interface MsoIconActionHandler
    {
        public void handleClick(IMsoObjectIf anObject);

        public void handleClick(IMsoObjectIf anObject, int anIndex);
    }

}
