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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.redcross.sar.util.mso.DTG;

public class TimeLinePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	public final int SECOND = Calendar.SECOND;
	public final int MINUTE = Calendar.MINUTE;
	public final int HOUR = Calendar.HOUR_OF_DAY;
	public final int DAY = Calendar.DAY_OF_YEAR;

	private int m_top = 30;
	private int m_gap = 30;
	private int m_min = 1;
	private int m_max = 100;
	private int m_largeTic = 10;
	private int m_smallTic = 5;
	private int m_unit;

	private Calendar m_origo;

	private boolean isDrawSmallTimeTic = false;
	private boolean isDrawLargeTimeTic = false;
	private boolean isDrawSmallTimeTicText = false;
	private boolean isDrawLargeTimeTicText = false;

	private final List<Time> m_times = new ArrayList<Time>();

	private final Stroke thinStroke = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
	private final Stroke fatStroke = new BasicStroke(2.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);

	/* ==========================================================
	 * Constructors
	 * ========================================================== */

	/**
	 * This is the default constructor
	 */
	public TimeLinePanel() {
		this(30, 30);
	}

	/**
	 * This constructor sets the gap and top parameters
	 */
	public TimeLinePanel(int gap, int top) {
		// forward
		super();
		// initialize GUI
		initialize(gap,top);
		// add mouse event listener
        this.addMouseListener(new java.awt.event.MouseAdapter() {
        	// is clicked
        	public void mouseClicked(java.awt.event.MouseEvent e) {
        		set();
        	}
        });
	}


	/* ==========================================================
	 * Public methods
	 * ========================================================== */

	public int addTime(int time, String caption, boolean isVisible, boolean isOpaque) {
		// add to list
		m_times.add(new Time(time,caption,isVisible,isOpaque));
		// sort times
		Collections.sort(m_times);
	    // force repaint
	    repaint();
	    // finished
		return m_times.size()-1;
	}

	public boolean removeTime(int index) {
		// remove from list
		boolean bFlag =(m_times.remove(index)!=null);
		// sort times
		Collections.sort(m_times);
	    // force repaint
	    repaint();
	    // finished
	    return bFlag;
	}

	public void removeAllTimes() {
		// clear all times
		m_times.clear();
	    // force repaint
	    repaint();
	}

	public void setTime(int index, int value) {
		// update
		m_times.get(index).setTime(value);
		// sort times
		Collections.sort(m_times);
	    // force repaint
	    repaint();
	}

	public int getTime(int index) {
		return m_times.get(index).getTime();
	}

	public int getTimeCount() {
		return m_times.size();
	}


	/**
	 * This method sets current time range
	 *
	 */
	public void setRange(int min, int max)
	{
		// set states
	    m_min = min;
	    m_max = max;
	    // force repaint
	    repaint();
	}

	/**
	 * This method sets current limits
	 *
	 */
	public void setSteps(int small, int large)
	{
		// set states
	    m_smallTic = small;
	    m_largeTic = large;
	    // force repaint
	    repaint();
	}

	/**
	 * This method sets the draw small time tic mode. If <code>false</code>, small time tics are not drawn.
	 */
	public void setDrawSmallTimeTic(boolean isDrawSmallTimeTic) {
		this.isDrawSmallTimeTic = isDrawSmallTimeTic;
	    // force repaint
	    repaint();
	}

	/**
	 * This method sets the draw small time tic text mode. If <code>false</code>, small time tic text is not drawn.
	 */
	public void setDrawSmallTimeTicText(boolean isDrawSmallTimeTicText) {
		this.isDrawSmallTimeTicText = isDrawSmallTimeTicText;
	    // force repaint
	    repaint();
	}

	/**
	 * This method sets the draw small time tic mode. If <code>false</code>, large time tics are not drawn.
	 */
	public void setDrawLargeTimeTic(boolean isDrawLargeTimeTic) {
		this.isDrawLargeTimeTic = isDrawLargeTimeTic;
	    // force repaint
	    repaint();
	}

	/**
	 * This method sets the draw small time tic text mode. If <code>false</code>, large time tic text is not drawn.
	 */
	public void setDrawLargeTimeTicText(boolean isDrawLargeTimeTicText) {
		this.isDrawLargeTimeTicText = isDrawLargeTimeTicText;
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
	    double d = calcDrawTicCount();
	    double dx = calcDrawTicWidth(w-30,d);
	    int tr = calcTicRatio();
        // prepare to draw rounded level rectangle
	    g2d.setColor(Color.GRAY);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    // draw the border rectangle
	    g2d.drawRoundRect(m_gap, m_top, getWidth() - m_gap * 2 - 2, h - 1, 5, 5);
    	// get middle point
    	int cy = m_top + h/2;
    	// get x and y offset
    	int x = m_gap + 30;
    	// prepare to draw
	    g2d.setFont(font);
    	g2d.setStroke(fatStroke);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	    // draw time base line
	    g2d.drawLine(x, cy, m_gap + (int)((d*dx) + 30), cy);
		// validate required fields
		if(m_origo!=null) {
		    // draw time tics?
			if(isDrawLargeTimeTic || isDrawLargeTimeTicText || isDrawSmallTimeTic || isDrawSmallTimeTicText) {
				// draw time tics
			    for(int i=0; i*dx<=w-30; i++) {
				    // get current time tic
				    int time = i*m_smallTic + m_min;
				    // draw tic
			    	if(i % tr == 0) {
			    		// draw tic?
			    		if(isDrawLargeTimeTic) {
					    	// prepare to draw main time tic's
					    	g2d.setStroke(fatStroke);
					    	g2d.setColor(time == 0 ? Color.BLUE : Color.GRAY);
					    	// draw tic
				    		drawTic(g2d,x+(int)(dx*i),cy-10,20);
			    		}
			    		// draw tic text?
			    		if(isDrawLargeTimeTicText) {
				    		// get flag
				    		boolean bFull = calcShowFullDTG(i, time);
				    		// draw DTG time text
				    		drawDTGString(g2d, time, x+(int)(dx*i), cy-15, bFull);
			    		}
			    	}
			    	else {
			    		// draw tic?
				    	if(isDrawSmallTimeTic) {
					    	// prepare to draw small time tic's
					    	g2d.setStroke(oldStroke);
					    	g2d.setColor(time == 0 ? Color.BLUE : Color.GRAY);
					    	// draw tic
				    		drawTic(g2d,x+(int)(dx*i),cy-5,10);
				    	}
			    	}
			    	// draw small time tic text?
		    		if(isDrawSmallTimeTicText) {
		    			drawTicString(g2d,time,x+(int)(dx*i),cy+10);
		    		}
			    }
			}
		}
		// draw current time?
	    if(m_times.size()>0) {
	    	// loop over events
	    	for(Time it : m_times) {
	    		// forward?
	    		if(it.isVisible()) {
	    			drawTime(g2d,it,x,m_top,w-30,h);
	    		}
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

	/**
	 * This method initialize this
	 *
	 */
	private void initialize(int gap, int top)
	{
	    m_gap = Math.max(gap,20);
	    m_top = m_gap*2 > top ? m_gap*2 : top;
	}

	private void set() {
		// set offset from top
		initialize(30,30);
		// set states
	    m_min = 0;
	    m_max = 60*5;
		// set states
	    m_smallTic = 60;
	    m_largeTic = 60;
	    // set origo
	    m_origo = Calendar.getInstance();
	    // set time
	    m_times.clear();
	    m_times.add(new Time(50,"Nå",true,false));
	    m_times.add(new Time(10,"Sol opp",true,false));
	    m_times.add(new Time(220,"Sol ned",true,false));
	    // set unit
	    m_unit = MINUTE;
	    // set background color
	    setBackground(Color.WHITE);
	    // set draw time
	    isDrawLargeTimeTic = true;
	    isDrawSmallTimeTic = true;
	    isDrawLargeTimeTicText = true;
	    isDrawSmallTimeTicText = true;
	    // force a redraw
	    repaint();

	}

	private void drawTic(Graphics2D g2d, int x, int y, int height) {
	    g2d.drawLine(x, y, x, y + height);
	}

	private void drawDTGString(Graphics2D g2d, int time, int x, int y, boolean full) {
	    // clone time
	    Calendar t = (Calendar)m_origo.clone();
	    // calculate time
	    t.add(m_unit, time);
	    // get text
	    String tic = DTG.CalToDTG(t);
	    // strip?
	    if(full) {
	    	// get full text metrics
		    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(tic, g2d);
		    // get text position
		    x -= (int)rc.getWidth()/2;
	    	// trip day
	    	String day = tic.substring(0, 2);
		    // prepare draw day text
		    g2d.setColor(Color.GRAY);
		    // draw day text
		    g2d.drawString(day, x, y);
		    // get width of day text
		    rc = g2d.getFontMetrics().getStringBounds(day, g2d);
		    // add day text width to x
		    x += (int)rc.getWidth();
		    // save current font
		    Font oldFont = g2d.getFont();
		    // create bold font
		    Font font = new Font(oldFont.getName(), Font.BOLD, oldFont.getSize());
		    // prepare draw tic text
		    g2d.setFont(font);
		    g2d.setColor(Color.BLACK);
		    // strip to time only
	    	tic = tic.substring(2, tic.length());
		    // draw tic text
		    g2d.drawString(tic, x, y);
		    // resume old font
		    g2d.setFont(oldFont);
	    }
	    else {
		    // prepare draw tic text
		    g2d.setColor(Color.BLACK);
		    // strip to time only
	    	tic = tic.substring(2, tic.length());
	    	// get text metrics
		    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(tic, g2d);
		    // get text position
		    x -= (int)rc.getWidth()/2;
		    // draw tic text
		    g2d.drawString(tic, x, y);
	    }
	}

	private void drawTicString(Graphics2D g2d, int time, int x, int y) {
	    // prepare draw small tic text
	    g2d.setColor(Color.BLACK);
	    // get text
	    String tic = Math.signum(time) >= 0 ? "+" + Math.abs(time) : "-" + Math.abs(time);
	    // get text metrics
	    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(tic, g2d);
	    // get text position
	    x -= (int)rc.getWidth()/2;
	    y += (int)rc.getHeight();
	    // draw tic text
	    g2d.drawString(tic, x, y);

	}


	private boolean calcShowFullDTG(int i,int time) {
		// always show on first tic
		if(i==0) return true;
	    // clone time
	    Calendar t = (Calendar)m_origo.clone();
	    // calculate time
	    t.add(m_unit, time);
		// check if same day
	    if(m_origo.get(Calendar.DAY_OF_MONTH)!=t.get(Calendar.DAY_OF_MONTH)) {
	    	// shift one tic back
	    	t.add(m_unit, -m_largeTic);
	    	// changed within this tic?
	    	return m_origo.get(Calendar.DAY_OF_MONTH)==t.get(Calendar.DAY_OF_MONTH);
	    }
	    // finished
		return false;
	}

	private int calcDrawWidth() {
	    return getWidth() - m_gap * 2 - 30;
	}

	private int calcDrawHeight() {
    	return getHeight() - m_top - m_gap;
	}

	private int calcDrawTimeCount() {
		return (m_max-m_min);
	}

	private double calcDrawTicCount() {
		return (m_max-m_min) / m_smallTic;
	}

	private double calcDrawTicWidth(int drawWidth, double ticCount) {
		return drawWidth / ticCount;
	}

	private int calcTicRatio() {
		return Math.max(m_largeTic/m_smallTic,1);
	}

	private void drawTime(Graphics2D g2d, Time time, int x, int y, int w, int h) {
		// calculate information
	    int d = calcDrawTimeCount();
	    double dx = calcDrawTicWidth(w,d);
	    // get time text
	    String text = time.m_caption;
    	// get text metrics
	    Rectangle2D rc = g2d.getFontMetrics().getStringBounds(text, g2d);
	    // calculate width from time text
	    w = (int)rc.getWidth()+20;
	    // calculate x from time and width
	    x += dx * (time.m_time - m_min) - w/2;
	    // is framed?
	    if(time.isFramed()) {
		    // prepare to fill upper rectangle as translucent
		    float[] comps = new float[3];
		    comps = getBackground().getRGBColorComponents(comps);
		    g2d.setColor(new Color(comps[0], comps[1], comps[2], 0.6f));
		    // fill upper rectangle
		    g2d.fillRoundRect(x, y-21, w, h/2+30, 5, 5);
		    // prepare to fill lower rectangle as opaque;
		    g2d.setColor(getBackground());
		    // fill upper rectangle
		    g2d.fillRoundRect(x, y+h/2+10, w, h/2-10, 5, 5);
	        // prepare to draw rounded level rectangle
	    	g2d.setColor(Color.GREEN);
		    g2d.setStroke(thinStroke);
		    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		    // draw the rectangle
		    g2d.drawRoundRect(x, y-21, w, h+20, 5, 5);
	    }
    	// prepare to draw tic's
    	g2d.setStroke(fatStroke);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    	// draw time tic
		drawTic(g2d,x+w/2,y+h/2-10,20);
        // prepare to draw text
    	g2d.setColor(Color.BLACK);
		// draw time text
	    g2d.drawString(text, x + 10, y-5);
		// draw tic text
	    drawTicString(g2d, time.m_time, x+w/2, y+h/2 + 10);

	}

	/* ==========================================================
	 * Inner classes
	 * ========================================================== */

	private class Time implements Comparable<Time>{

		private int m_time;
		private String m_caption;
		private boolean m_isVisible;
		private boolean m_isFramed;

		public Time(int time, String caption, boolean isVisible, boolean isFramed) {
			m_time = time;
			m_caption = caption;
			m_isVisible = isVisible;
			m_isFramed = isFramed;
		}

		public int getTime() {
			return m_time;
		}

		public void setTime(int time) {
			this.m_time = time;
		}

		public String getCaption() {
			return m_caption;
		}

		public void setCaption(String caption) {
			this.m_caption = caption;
		}

		public boolean isVisible() {
			return m_isVisible;
		}

		public void setVisible(boolean isVisible) {
			m_isVisible = isVisible;
		}

		public boolean isFramed() {
			return m_isFramed;
		}

		public void setFramed(boolean isFramed) {
			m_isFramed = isFramed;
		}

		public int compareTo(Time o) {
			return m_time - o.m_time;
		}

	}


}
