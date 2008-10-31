package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.redcross.sar.gui.AbstractPopupHandler;
import org.redcross.sar.gui.PopupAdapter;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.util.SpringUtilities;

public class DiskoTilesPanel extends BasePanel
{
	private static final long serialVersionUID = 1L;

    protected final int m_hgap;
    protected final int m_vgap;

    protected final Insets m_tileInsets;

    protected final Dimension m_tileSize = new Dimension();
    protected final Dimension m_defaultChildSize = new Dimension(10, 10);

    protected final Vector<Component> m_tiles = new Vector<Component>();

	protected TileList m_tileList;
    protected LayoutManager m_layout;

    protected int m_colCount = 0;
    protected boolean m_tileSizeSet = false;
    protected boolean m_horizontalFlow = true;

    /* ============================================================
     * Constructors
     * ============================================================*/

    public DiskoTilesPanel(FlowLayout aLayoutManager)
    {
        this(aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoTilesPanel(String caption, FlowLayout aLayoutManager)
    {
        this(caption,aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoTilesPanel(GridLayout aLayoutManager)
    {
        this(aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoTilesPanel(String caption, GridLayout aLayoutManager)
    {
        this(caption, aLayoutManager, aLayoutManager.getHgap(), aLayoutManager.getVgap(), true);
    }

    public DiskoTilesPanel(LayoutManager aLayoutManager, int aHgap, int aVgap, boolean isHorizontalFlow)
    {
    	this("", aLayoutManager, aHgap, aVgap, isHorizontalFlow);
    }

    public DiskoTilesPanel(String caption, LayoutManager aLayoutManager, int aHgap, int aVgap, boolean isHorizontalFlow)
    {
    	// forward
        super(caption);

        // prepare
        m_hgap = aHgap;
        m_vgap = aVgap;
        m_layout = aLayoutManager;
        m_horizontalFlow = isHorizontalFlow;
        m_tileInsets = new Insets(m_vgap, m_hgap, m_vgap, m_hgap);

        // forward
        initialize();
    }

    private void initialize()
    {

        // set body component
        setBodyComponent(getTileList());

    	// forward
        getScrollPane().setCorner(JScrollPane.UPPER_LEFT_CORNER, UIFactory.createCorner());
        getScrollPane().setCorner(JScrollPane.UPPER_RIGHT_CORNER, UIFactory.createCorner());

        // handle to changes in size
        ComponentAdapter resizeHandler = new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                resizePanel(false);
            }
        };
        getBodyComponent().addComponentListener(resizeHandler);

    }

    /* ============================================================
     * Public methods
     * ============================================================*/

    public void setCols(int aColumnCount)
    {
        m_colCount = Math.max(aColumnCount, 0);
    }

    public Component getTile(int index) {
    	return m_tiles.get(index);
    }

    public Component addTile(Component aComponent)
    {
        m_tiles.add(aComponent);
        m_tileSizeSet = false;
        return getTileList().add(aComponent);
    }

    public void removeTile(Component aComponent)
    {
        m_tiles.remove(aComponent);
        m_tileSizeSet = false;
        getTileList().remove(aComponent);
    }

    public void removeAllTiles()
    {
        m_tileSizeSet = false;
        m_tiles.clear();
        getTileList().removeAll();
    }

    public int getTileCount() {
    	return m_tiles.size();
    }

    public int getMaxNonScrollItems(Dimension aDimension)
    {
        Dimension availableDim = getSize();

        int cols;
        if (m_layout instanceof GridLayout)
        {
            cols = ((GridLayout) m_layout).getColumns();
        } else
        {
            cols = availableDim.width / (aDimension.width + m_hgap);
        }
        cols = Math.max(cols, 1);
        int rows = availableDim.height / (aDimension.height + m_vgap) - 1;
        rows = Math.max(rows, 1);
        return cols * rows;
    }

    public void setHeaderPopupHandler(AbstractPopupHandler aPopupHandler)
    {
        PopupAdapter listener = new PopupAdapter(aPopupHandler);
        getHeaderPanel().addMouseListener(listener);
        getScrollPane().getCorner(JScrollPane.UPPER_LEFT_CORNER).addMouseListener(listener);
        getScrollPane().getCorner(JScrollPane.UPPER_RIGHT_CORNER).addMouseListener(listener);
    }

    /* ============================================================
     * Protected methods
     * ============================================================*/

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

        if (m_tiles.size() > 0)
        {
            if (!m_tileSizeSet)
            {
                int width = m_defaultChildSize.width;
                int height = m_defaultChildSize.height;
                for (Component c : m_tiles)
                {
                    m_tileSizeSet = c.getHeight() > 0;
                    width = Math.max(width, c.getPreferredSize().width);
                    height = Math.max(height, c.getPreferredSize().height);
                }
                m_tileSize.width = width;
                m_tileSize.height = height;
            }

            if (m_colCount > 0)
            {
                cols = m_colCount;
            } else if (m_layout instanceof GridLayout)
            {
                cols = ((GridLayout) m_layout).getColumns();
            } else
            {
                cols = myDimension.width / (m_tileSize.width + m_hgap);
            }
            cols = Math.max(cols, 1);
            rows = ((m_tiles.size() - 1) / cols) + 1;
        } else
        {
            m_tileSizeSet = false;
            cols = 0;
            rows = 0;
        }

        int newHeight = Math.max(rows * (m_tileSize.height + m_vgap), getHeight());

        /*
        if (!m_horizontalFlow)
        {
            rows = Math.max(newHeight / (m_tileSize.height + m_vgap), 1);
        }
        */

        if (m_tileSizeSet)
        {
            if (m_layout instanceof GridBagLayout)
            { // layout components manually
                layoutGridBag(rows, cols);
            } else if (m_layout instanceof SpringLayout)
            { // layout components manually
                layoutSpring(rows, cols, myDimension.width + m_hgap);
            }
            else {
            	m_layout.layoutContainer(getTileList());
            }
        }

        if (newHeight != myDimension.height)
        {
            myDimension.height = newHeight;
            setPreferredSize(myDimension);
        }
        //revalidate();
        repaint();
    }

    protected TileList getTileList() {
    	if(m_tileList==null) {
    		m_tileList = new TileList(m_layout);
    	}
    	return m_tileList;
    }

    /* ============================================================
     * Helper methods
     * ============================================================*/

    private void layoutGridBag(int rows, int cols)
    {
        GridBagLayout layout = (GridBagLayout) m_layout;
        int irow = 0;
        int icol = 0;
        for (Component c : m_tiles)
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
            gbc.insets = m_tileInsets;
            gbc.ipadx = m_hgap;
            gbc.ipady = m_vgap;
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
        layout.layoutContainer(getTileList());
    }

    private void layoutSpring(int rows, int cols, int aPanelWidth)
    {
    	// forward
    	SpringUtilities.makeCompactGrid(getTileList(), rows, cols, m_hgap, m_vgap, m_hgap, m_vgap);

    	/*
    	if(false) {
	        SpringLayout layout = (SpringLayout) m_layout;
	        Spring xPadSpring = Spring.constant(m_hgap);
	        Spring yPadSpring = Spring.constant(m_vgap);
	        Spring initialXSpring = Spring.constant(0);
	        Spring initialYSpring = Spring.constant(0);

	        SpringLayout.Constraints lastConstraint = null;
	        SpringLayout.Constraints lastRowConstraint = null;
	        SpringLayout.Constraints lastColumnConstraint = null;
	        int irow = 0;
	        int icol = 0;
	        for (Component c : m_tiles)
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
	        layout.layoutContainer(getTileList());
    	}
    	*/
    }

    /* ============================================================
     * Inner classes
     * ============================================================*/

    private class TileList extends JComponent implements Scrollable
    {

		private static final long serialVersionUID = 1L;

        /* ============================================================
         * Constructors
         * ============================================================*/

		public TileList(LayoutManager manager) {
    		super();
    		setLayout(manager);
    	}

        /* ============================================================
         * Scrollable implementation
         * ============================================================*/

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
                return m_defaultChildSize.width;
            } else
            {
                return m_defaultChildSize.height;
            }
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
        {
            if (direction == SwingConstants.HORIZONTAL)
            {
                return m_defaultChildSize.width * 5;
            } else
            {
                return m_defaultChildSize.height * 5;
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

    }

}
