package org.redcross.sar.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

/**
 *
 */
public class DiskoScrollPanel extends JPanel implements Scrollable
{
	private static final long serialVersionUID = 1L;

	protected final JScrollPane m_surroundingScrollPane;
    protected final LayoutManager m_layoutManager;
    protected Dimension m_defaultDimension = new Dimension(10, 10);
    protected Dimension m_childDimension = new Dimension();
    protected Vector<Component> m_childrenComponents = new Vector<Component>();
    private boolean m_childSizeSet = false;
    private JLabel m_header;
    private Corner m_corner;

    protected final int m_hgap;
    protected final int m_vgap;
    protected int m_colCount = 0;

    protected boolean m_horizontalFlow = true;
    protected Insets m_componentInsets;

    public DiskoScrollPanel(JScrollPane aScrollPane, FlowLayout aLayoutManager)
    {
        this(aScrollPane, aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoScrollPanel(JScrollPane aScrollPane, GridLayout aLayoutManager)
    {
        this(aScrollPane, aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoScrollPanel(JScrollPane aScrollPane, LayoutManager aLayoutManager, int aHgap, int aVgap, boolean isHorizontalFlow)
    {
        super(aLayoutManager);
        m_surroundingScrollPane = aScrollPane;
        m_layoutManager = aLayoutManager;

        m_hgap = aHgap;
        m_vgap = aVgap;
        m_horizontalFlow = isHorizontalFlow;

        m_componentInsets = new Insets(m_vgap / 2, m_hgap / 2, m_vgap / 2, m_hgap / 2);
        m_surroundingScrollPane.setViewportView(this);

        m_header = new JLabel();
        m_surroundingScrollPane.setColumnHeaderView(m_header);

        m_corner = new DiskoScrollPanel.Corner(m_header.getBackground());
        m_surroundingScrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER, m_corner);
        m_surroundingScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

        ComponentAdapter resizeHandler = new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                resizePanel(false);
            }
        };
        addComponentListener(resizeHandler);
        getParent().addComponentListener(resizeHandler);
    }

    public void setCols(int aColumnCount)
    {
        m_colCount = Math.max(aColumnCount, 0);
    }

    /**
     * Resize the panel according to width and max label size.
     *
     * @param dataChanged Indicates that the method is called due to change of data content.
     */
    protected void resizePanel(boolean dataChanged)
    {
        int rows;
        int cols;
        Dimension myDimension = getSize();

        if (m_childrenComponents.size() > 0)
        {
            if (!m_childSizeSet)
            {
                int width = m_defaultDimension.width;
                int height = m_defaultDimension.height;
                for (Component c : m_childrenComponents)
                {
                    m_childSizeSet = c.getHeight() > 0;
                    width = Math.max(width, c.getPreferredSize().width);
                    height = Math.max(height, c.getPreferredSize().height);
                }
                m_childDimension.width = width;
                m_childDimension.height = height;
            }

            LayoutManager lm = getLayout();
            if (m_colCount > 0)
            {
                cols = m_colCount;
            } else if (lm instanceof GridLayout)
            {
                cols = ((GridLayout) lm).getColumns();
            } else
            {
                cols = myDimension.width / (m_childDimension.width + m_hgap);
            }
            cols = Math.max(cols, 1);
            rows = ((m_childrenComponents.size() - 1) / cols) + 1;
        } else
        {
            m_childSizeSet = false;
            cols = 0;
            rows = 0;
        }

        int newHeight = Math.max(rows * (m_childDimension.height + m_vgap), getParent().getHeight());

        if (!m_horizontalFlow)
        {
            rows = Math.max(newHeight / (m_childDimension.height + m_vgap), 1);
        }

        if (m_childSizeSet)
        {
            //
            if (m_layoutManager instanceof GridBagLayout)
            { // layout components manually
                layoutGridBag(rows, cols);
            } else if (m_layoutManager instanceof SpringLayout)
            { // layout components manually
                layoutSpring(rows, cols, myDimension.width + m_hgap);
            }
        }

        if (newHeight != myDimension.height)
        {
            myDimension.height = newHeight;
            setPreferredSize(myDimension);
        }
        revalidate();
        repaint();
    }

    private void layoutGridBag(int rows, int cols)
    {
        GridBagLayout layout = (GridBagLayout) m_layoutManager;
        int irow = 0;
        int icol = 0;
        for (Component c : m_childrenComponents)
        {
            GridBagConstraints gbc = layout.getConstraints(c);
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridx = icol;
            gbc.gridy = irow;
            gbc.anchor = GridBagConstraints.FIRST_LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = m_componentInsets;
            gbc.ipadx = 0;
            gbc.ipady = 0;
            layout.setConstraints(c, gbc);
            if (m_horizontalFlow)
            {
                icol++;
                if (icol == cols)
                {
                    icol = 0;
                    irow++;
                }

            } else
            {
                irow++;
                if (irow == rows)
                {
                    irow = 0;
                    icol++;
                }
            }
        }
        layout.layoutContainer(this);
    }

    private void layoutSpring(int rows, int cols, int aPanelWidth)
    {
        SpringLayout layout = (SpringLayout) m_layoutManager;
        Spring xPadSpring = Spring.constant(m_hgap);
        Spring yPadSpring = Spring.constant(m_vgap);
        Spring initialXSpring = Spring.constant(0);
        Spring initialYSpring = Spring.constant(0);

        SpringLayout.Constraints lastConstraint = null;
        SpringLayout.Constraints lastRowConstraint = null;
        SpringLayout.Constraints lastColumnConstraint = null;
        int irow = 0;
        int icol = 0;
        for (Component c : m_childrenComponents)
        {
            SpringLayout.Constraints cons = layout.getConstraints(c);
            if (irow == 0)
            {
                lastColumnConstraint = lastConstraint;
            }

            if (icol == 0)
            { //start of new row
                lastRowConstraint = lastConstraint;
                cons.setX(initialXSpring);
            } else if (m_horizontalFlow)
            { //x position depends on previous component
                cons.setX(Spring.sum(lastConstraint.getConstraint(SpringLayout.EAST), xPadSpring));
            } else
            { //x position depends on previous column
                cons.setX(Spring.sum(lastColumnConstraint.getConstraint(SpringLayout.EAST), xPadSpring));
            }

            if (m_colCount > 0)
            {
                Dimension cDim = c.getPreferredSize();
                cDim.width = ((aPanelWidth-5) / m_colCount);
                c.setPreferredSize(cDim);
            }


            if (irow == 0)
            { //first row
                lastColumnConstraint = lastConstraint;
                cons.setY(initialYSpring);
            } else if (!m_horizontalFlow)
            { //y position depends on previous component
                cons.setY(Spring.sum(lastConstraint.getConstraint(SpringLayout.SOUTH), yPadSpring));
            } else
            { //y position depends on previous row
                cons.setY(Spring.sum(lastRowConstraint.getConstraint(SpringLayout.SOUTH), yPadSpring));
            }
            lastConstraint = cons;
            if (m_horizontalFlow)
            {
                icol++;
                if (icol == cols)
                {
                    icol = 0;
                    irow++;
                }

            } else
            {
                irow++;
                if (irow == rows)
                {
                    irow = 0;
                    icol++;
                }
            }
        }
        layout.layoutContainer(this);
    }


    @Override
    public Component add(Component aComponent)
    {
        m_childrenComponents.add(aComponent);
        m_childSizeSet = false;
        return super.add(aComponent);
    }

    public Dimension getPreferredScrollableViewportSize()
    {
        return getPreferredSize();
    }

    public Dimension getPreferredSize()
    {
        return super.getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (direction == SwingConstants.HORIZONTAL)
        {
            return m_defaultDimension.width;
        } else
        {
            return m_defaultDimension.height;
        }
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        if (direction == SwingConstants.HORIZONTAL)
        {
            return m_defaultDimension.width * 5;
        } else
        {
            return m_defaultDimension.height * 5;
        }
    }

    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }

    public int getMaxNonscrollItems(Dimension aDimension)
    {
        Dimension availableDim = getParent().getSize();

        int cols;
        LayoutManager lm = getLayout();
        if (lm instanceof GridLayout)
        {
            cols = ((GridLayout) lm).getColumns();
        } else
        {
            cols = availableDim.width / (aDimension.width + m_hgap);
        }
        cols = Math.max(cols, 1);
        int rows = availableDim.height / (aDimension.height + m_vgap);
        rows = Math.max(rows, 1);
        return cols * rows;
    }

    public void removeAll()
    {
        m_childSizeSet = false;
        m_childrenComponents.clear();
        super.removeAll();
    }

    public JLabel getHeaderLabel()
    {
        return m_header;
    }

    public void setHeaderPopupHandler(AbstractPopupHandler aPopupHandler)
    {
        PopupListener listener = new PopupListener(aPopupHandler);
        m_header.addMouseListener(listener);
        m_corner.addMouseListener(listener);
    }

    public static class Corner extends JComponent
    {
        Color m_background;

        public Corner(Color aBackground)
        {
            m_background = aBackground;
        }

        public void setBackground(Color aBackground)
        {
            m_background = aBackground;
        }

        /**
         * Fill with given color.
         *
         * @param g My graphics
         */
        protected void paintComponent(Graphics g)
        {
            g.setColor(m_background);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
