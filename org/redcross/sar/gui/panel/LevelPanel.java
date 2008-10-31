package org.redcross.sar.gui.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.redcross.sar.gui.factory.DiskoIconFactory;

public class LevelPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private int m_top = 30;
	private int m_gap = 30;
	private int m_min = 0;
	private int m_max = 100;
	private int m_marginalLimit;

	private String m_unitName;

	private boolean m_isDrawLevelLabel = false;

	private final List<Level> m_levels = new ArrayList<Level>();
	private final List<Limit> m_limits = new ArrayList<Limit>();

	private final Stroke m_fatStroke = new BasicStroke(3.0f,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_BEVEL);

	/* ==========================================================
	 * Constructors
	 * ========================================================== */

	/**
	 * This is the default constructor
	 */
	public LevelPanel() {
		this(30, 30, 0);
	}

	/**
	 * This constructor sets the gap and top parameters
	 */
	public LevelPanel(int gap, int top, final int test) {
		// forward
		super();
		// initialize GUI
		initialize(gap,top);
		// add mouse event listener
        this.addMouseListener(new java.awt.event.MouseAdapter() {
        	// is clicked
        	public void mouseClicked(java.awt.event.MouseEvent e) {
        		switch(test) {
        		case 0 : test1(); break;
        		case 1 : test2(); break;
        		case 2 : test3(); break;
        		}
        	}
        });
	}

	/* ==========================================================
	 * Public methods
	 * ========================================================== */

	public boolean isDrawLevelLabel() {
		return m_isDrawLevelLabel;
	}

	public void setDrawLevelLabel(boolean isDrawLevelLabel) {
		// update
		m_isDrawLevelLabel = isDrawLevelLabel;
	    // force repaint
	    repaint();
	}

	public String getUnitName() {
		return m_unitName;
	}

	public void setUnitName(String name) {
		// update
		m_unitName = name;
	    // force repaint
	    repaint();
	}

	public int addLevel(String caption, Icon icon, int value) {
		// add to list
		m_levels.add(new Level(value, caption, icon));
		// sort levels
		Collections.sort(m_levels);
	    // force repaint
	    repaint();
		return m_levels.size()-1;
	}

	public boolean removeLevel(int index) {
		// remove from list
		boolean bFlag =(m_levels.remove(index)!=null);
		// sort levels
		Collections.sort(m_levels);
	    // force repaint
	    repaint();
	    return bFlag;
	}

	public void removeAllLevels() {
		// clear all levels
		m_levels.clear();
	    // force repaint
	    repaint();
	}

	public void setLevel(int index, int value) {
		// update
		m_levels.get(index).setLevel(value);
		// sort levels
		Collections.sort(m_levels);
	    // force repaint
	    repaint();
	}

	public int getLevel(int index) {
		return m_levels.get(index).getLevel();
	}

	public int getLevelCount() {
		return m_levels.size();
	}

	public int addLimit(int value, String caption, Color color, boolean isMarginal) {
		// add to list
		m_limits.add(new Limit(value,caption,color));
		// update marginal limit index?
		m_marginalLimit = isMarginal ? m_limits.size()-1 : m_marginalLimit;
		// sort limits
		Collections.sort(m_limits);
	    // force repaint
	    repaint();
	    // finished
		return m_limits.size()-1;
	}

	public boolean removeLimit(int index) {
		// remove from list
		boolean bFlag = (m_limits.remove(index)!=null);
		// update marginal limit index?
		m_marginalLimit = m_marginalLimit>=m_limits.size() ? m_limits.size()-1 : m_marginalLimit;
		// sort limits
		Collections.sort(m_limits);
	    // force repaint
	    repaint();
	    // finished
	    return bFlag;
	}

	public void removeAllLimits() {
		m_limits.clear();
		m_marginalLimit = -1;
	    // force repaint
	    repaint();
	}

	public int getMarginalLimit(int index) {
		return m_marginalLimit;
	}

	public void setMarginalLimit(int index) {
		m_marginalLimit = index<m_limits.size() ? index : m_marginalLimit;
	}

	public void setLimit(int index, int value) {
		// update
		m_limits.get(index).setLimit(value);
		// sort limits
		Collections.sort(m_limits);
	    // force repaint
	    repaint();
	}

	public int getLimit(int index) {
		return m_limits.get(index).getLimit();
	}

	public int getLimitCount() {
		return m_limits.size();
	}

	/**
	 * This method sets value range
	 *
	 */
	public void setRange(int min, int max)
	{
		// get change
		int d = min - m_min;
		// save
	    m_min = min;
	    m_max = Math.max(min, max);
	    // adjust limits
	    for(Limit it : m_limits) {
	    	it.setLimit(it.getLimit() + d);
	    }
		// sort limits
		Collections.sort(m_limits);
	    // force repaint
	    repaint();
	}

	/* ==========================================================
	 * Overridden methods
	 * ========================================================== */

	/**
	 * This method paints the state bar
	 *
	 * @return void
	 */
	protected void paintComponent(Graphics g) {
		// save graphics state
		Color oldColor = g.getColor();
		Font oldFont = g.getFont();
		// cast to 2D
	    Graphics2D g2d = (Graphics2D)g;
	    // save Graphics2D state
	    Stroke oldStroke = g2d.getStroke();
	    RenderingHints oldHints = g2d.getRenderingHints();
		// create font
	    int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
	    int fontSize = (int)Math.round(8.0 * screenRes / 72.0);
	    Font font = new Font(oldFont.getName(), Font.PLAIN, fontSize);
	    // paint background?
        if (isOpaque() || true) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        // calculate draw information
	    int w = calcDrawWidth();
	    int h = calcDrawHeight();
	    int dx = calcDrawStep(w);
	    int y = calcDrawLevelOffsetY();
	    int lh = calcDrawLevelHeight(h-25);
        // prepare to draw border rectangle
	    g2d.setColor(Color.GRAY);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    // draw the border rectangle
	    g2d.drawRoundRect(m_gap, m_top, getWidth() - m_gap * 2 - 2, h - 1, 5, 5);
	    // draw the limits?
	    if(m_limits.size() > 0) {
	    	// prepare to draw limits
		    g2d.setFont(font);
	    	g2d.setStroke(m_fatStroke);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		    // loop over all levels
		    for(int i=1; i<m_limits.size(); i++) {
		    	// forward
		    	drawLimit(g2d, i, w, h, dx);
		    }
	    }
	    // draw the levels?
	    if(m_levels.size() > 0) {
		    // prepare to draw
		    g2d.setStroke(oldStroke);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    // loop over all levels
		    for(Level it: m_levels) {
		    	// forward
		    	y = drawLevel(g2d,it,y,w,lh,dx);
		    }
	    }
	    // restore Graphics state
        g.setColor(oldColor);
        g.setFont(oldFont);
        // restore Graphics2D
        g2d.setStroke(oldStroke);
        g2d.setRenderingHints(oldHints);
    }

	/* ==========================================================
	 * Helper methods
	 * ========================================================== */

	private void test1() {
		// set top offset
		m_top = 50;
		// set unit name
		m_unitName = "min";
	    // create math objects
	    Random rnd = new Random();
		// update properties
	    m_max = java.lang.Math.max(rnd.nextInt(100),1);
	    // add levels
	    m_levels.clear();
	    for(int i = 0; i<7; i++)
	    	m_levels.add(new Level(rnd.nextInt(m_max), "P"+(i+1),DiskoIconFactory.getIcon("GENERAL.UNIT","48x48")));
	    /*
		Color uf = new Color(51,102,255);
		Color op = new Color(255,102,0);
		Color of = new Color(255,204,0);
		*/
	    // add limits
	    m_limits.clear();
    	m_limits.add(new Limit(m_min,"",new Color(51,102,255)));
	    int marginal = m_max/2;
	    int optimal = Math.max(marginal + rnd.nextInt(m_max - marginal),1);
    	m_limits.add(new Limit(marginal,"Snart ledig",new Color(255,204,0)));
    	m_limits.add(new Limit(optimal,"Ledig",new Color(255,102,0)));
    	m_marginalLimit = 2;
		// sort
		Collections.sort(m_levels);
		Collections.sort(m_limits);

	    // force a redraw
	    repaint();
	}

	private void test2() {
		// set top gap
		m_top = 40;
	    // create math objects
	    Random rnd = new Random();
	    // add levels
	    m_levels.clear();
	    for(int i = 0; i<1; i++)
	    	m_levels.add(new Level(rnd.nextInt(m_max), "P"+(i+1),DiskoIconFactory.getIcon("GENERAL.UNIT","48x48")));
	    // force a redraw
	    repaint();
	}

	private void test3() {
		// set top gap
		m_top = 40;
	    // create math objects
	    Random rnd = new Random();
	    // add levels
	    m_levels.clear();
	    for(int i = 0; i<7; i++)
	    	m_levels.add(new Level(rnd.nextInt(m_max), "P"+(i+1),DiskoIconFactory.getIcon("GENERAL.UNIT","48x48")));
		// sort
		Collections.sort(m_levels);
	    // force a redraw
	    repaint();
	}

	private void initialize(int gap, int top)
	{
	    m_gap = gap;
	    m_top = top;
	}

	private Color getColor(int level) {
		// has limits?
		if(m_limits.size()>0) {
			// initialize limits
			Limit min = m_limits.get(0);
			Limit max = null;
			// loop over limits
			for(int i=1;i<m_limits.size();i++) {
				// get next max value
				max = m_limits.get(i);
				// within current limit?
			    if (level>=min.getLimit() && level < max.getLimit()) {
			    	return min.getColor();
			    }
			    // shift upwards
			    min = max;
			}
			// finished
			return max!=null ? max.getColor() : min.getColor();
		}
		// no limits
		return Color.BLUE;
	}

	private String getMessage(int level) {
		int d = level;
		if(m_marginalLimit>0) {
			d = level - m_limits.get(m_marginalLimit).getLimit();
		}
	    // finished
	    return (Math.signum(d) >= 0 ? "+ " + Math.abs(d) : "- " + Math.abs(d)) + " " + m_unitName;
	}

	private int calcDrawWidth() {
	    return getWidth() - m_gap * 2 - 30;
	}

	private int calcDrawHeight() {
    	return getHeight() - m_top - m_gap;
	}

	private int calcDrawStep(int drawWidth) {
		return drawWidth / m_max;
	}

	private int calcDrawLimitX(int index, int drawWidth, int drawStep) {
		int limit = m_limits.get(index).getLimit();
		int limitX = limit*drawStep;
	    if (limitX > drawWidth) limitX = drawWidth;
	    return limitX;
	}

	private int calcDrawLevelX(int level, int drawWidth, int drawStep) {
		// calculate
		int levelX = level * drawStep;
	    // limit to maximum draw width
	    if (levelX > drawWidth) levelX = drawWidth;
	    // finished
		return levelX;
	}

	private int calcDrawLevelOffsetY() {
		return m_top + 15;
	}

	private int calcDrawMessageX(int levelX, int labelWidth, int textWidth) {
	    // limit x-position
	    if(levelX - textWidth < m_gap + 30 + labelWidth)
	    	levelX = m_gap + 30 + labelWidth;
	    else
	    	levelX = levelX - textWidth;
	    // finished
	    return levelX;
	}

	private int calcDrawLevelHeight(int drawHeight) {
		return m_levels.size()>0 ? Math.max(20,drawHeight/m_levels.size() - 5) : 0;
	}

	private void drawLimit(Graphics2D g2d, int index, int drawWidth, int drawHeight, int drawStep) {
    	// prepare to draw line
	    g2d.setColor(Color.GRAY);
	    // get x-position
	    int x = calcDrawLimitX(index, drawWidth, drawStep);
    	// draw limit line
	    g2d.drawLine(x + m_gap + 15, m_top + 2, x + m_gap + 15, m_top + drawHeight - 2);
	    // get limit
	    Limit it = m_limits.get(index);
	    // get text
	    String caption = it.getCaption();
	    // get caption text metrics
	    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(caption, g2d);
	    // prepare draw limit text
	    g2d.setColor(Color.BLACK);
	    // draw limit texts
	    g2d.drawString(caption, x + m_gap + 15 - (int)(rc.getWidth()/2), m_top - 3);

	}

	private int drawLevel(Graphics2D g2d, Level level, int y, int drawWidth, int drawHeight, int drawStep) {
		// initialize x-position
		int x = m_gap + 15;
		// draw level label?
		if(m_isDrawLevelLabel) {
			x = drawLevelCaption(g2d, level, x, y, drawHeight);
		}
		// get level
		int l = level.m_level;
	    // calculate current level
	    int lx = calcDrawLevelX(l,drawWidth,drawStep);
	    // prepare to draw filled rounded level rectangle
	    g2d.setColor(getColor(l));
	    // fill state rectangle
	    g2d.fillRoundRect(x, y, lx + 1, drawHeight, 5, 5);
	    // prepare to draw rounded level rectangle
	    g2d.setColor(Color.GRAY);
	    // fill state rectangle
	    g2d.drawRoundRect(x, y, lx + 1, drawHeight, 5, 5);
	    // prepare to draw message text
	    g2d.setColor(Color.BLACK);
	    // get message text
	    String msg = getMessage(l);
	    // calculate message text metrics
	    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(msg, g2d);
	    // calculate message x-position
	    lx = calcDrawMessageX(lx, m_isDrawLevelLabel ? drawHeight : 0, (int)rc.getWidth());
	    // draw message
    	g2d.drawString(msg, lx, y + (int)(drawHeight + rc.getHeight())/2);
    	// increment y-position
    	return y + drawHeight + 5;
	}

	private int drawLevelCaption(Graphics2D g2d, Level level, int x, int y, int drawSize) {
	    // prepare to draw filled rounded level rectangle
	    g2d.setColor(Color.DARK_GRAY);
	    // fill state rectangle
	    g2d.fillRoundRect(x, y, drawSize, drawSize, 5, 5);
	    // prepare to draw rounded level rectangle
	    g2d.setColor(Color.GRAY);
	    // fill state rectangle
	    g2d.drawRoundRect(x, y, drawSize, drawSize, 5, 5);
	    // get caption text
	    String caption = level.m_caption;
	    // calculate caption text metrics
	    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(caption, g2d);
	    // prepare to draw message text
	    g2d.setColor(Color.GRAY);
	    // draw message
    	g2d.drawString(caption, x + (int)(drawSize - rc.getWidth())/2, y + drawSize - (int)rc.getHeight());
		// increment x-position
		return x + drawSize + 5;
	}

	/* ==========================================================
	 * Inner classes
	 * ========================================================== */

	private static class Level implements Comparable<Level> {

		private int m_level;
		private String m_caption;
		private Icon m_icon;

		public Level(int value, String caption, Icon icon) {
			m_caption = caption;
			m_icon = icon;
			m_level = value;
		}

		public int getLevel() {
			return m_level;
		}

		public void setLevel(int value) {
			m_level = value;
		}

		public String getCaption() {
			return m_caption;
		}

		public void setCaption(String caption) {
			m_caption = caption;
		}

		public Icon getIcom() {
			return m_icon;
		}

		public void setIcon(Icon icon) {
			m_icon = icon;
		}

		public int compareTo(Level o) {
			return o.m_level - m_level;
		}

	}

	private static class Limit implements Comparable<Limit> {

		private int m_limit;
		private String m_caption;
		private Color m_color;

		public Limit(int value, String caption, Color color) {
			m_limit= value;
			m_caption = caption;
			m_color = color;
		}

		public int getLimit() {
			return m_limit;
		}

		public void setLimit(int value) {
			m_limit = value;
		}

		public String getCaption() {
			return m_caption;
		}

		public void setCaption(String caption) {
			m_caption = caption;
		}

		public Color getColor() {
			return m_color;
		}

		public void setColor(Color color) {
			m_color = color;
		}

		public int compareTo(Limit o) {
			return m_limit - o.m_limit;
		}

	}

}