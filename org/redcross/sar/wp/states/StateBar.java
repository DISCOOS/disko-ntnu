package org.redcross.sar.wp.states;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Random;

import javax.swing.JPanel;

public class StateBar extends JPanel {
    /**
	 * privare variables
	 */
	private static final long serialVersionUID = 1L;
	private int m_top = 30;
	private int m_gap = 30;
	private int m_step = 10;
	private int m_max = 100;
	private int m_count = 0;
	private int m_marginal = 0;
	private int m_optimal = 0;
	
	/**
	 * This is the default constructor
	 */
	public StateBar() {
		super();
		initialize(30, 30);
	}

	/**
	 * This constructor sets the gap and top parameters
	 */
	public StateBar(int gap, int top) {
		super();
		initialize(gap,top);
		// add mouse event listener
        this.addMouseListener(new java.awt.event.MouseAdapter() {
        	// is clicked
        	public void mouseClicked(java.awt.event.MouseEvent e) {
        	    // create math objects
        	    Random rnd = new Random();
        		// update properties
        	    m_max = java.lang.Math.max(rnd.nextInt(100),1);
        	    m_step = (getWidth() - m_gap * 2 - 30) / m_max;
        	    m_marginal = m_max/2;
        	    m_optimal = java.lang.Math.max(m_marginal + rnd.nextInt(m_max - m_marginal),1);
        	    m_count = rnd.nextInt(m_max);
        	    // force a redraw
        	    repaint();
        	}
        });
	}

	/**
	 * This method sets current limits
	 * 
	 * @return void
	 */
	public void setLimits(int max, int marginal, int optimal) 
	{
		// set states
	    m_max = max;
	    m_marginal = marginal;
	    m_optimal = optimal;
	    // force repaint
	    repaint();
	}
	
	/**
	 * This method initialize this
	 * 
	 * @return void
	 */
	private void initialize(int gap, int top) 
	{
	    m_gap = gap;
	    m_top = top;
	}

	/**
	 * This method paints the state bar
	 * 
	 * @return void
	 */	
	protected void paintComponent(Graphics g) {
		String tasks;
		// store state
		Color c = g.getColor();
		Font f = g.getFont();
		// cast to 2D
	    Graphics2D g2d = (Graphics2D)g;
		// create colors
	    Color cb = new Color(51,102,255);
	    Color co = new Color(255,102,0);
	    Color cy = new Color(255,204,0);
		// create font
	    int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
	    int fontSize = (int)Math.round(8.0 * screenRes / 72.0);
	    Font font = new Font(f.getName(), Font.PLAIN, fontSize);
	    // paint background?
        if (isOpaque()) { 
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
	    g2d.setFont(font);
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setColor(Color.BLACK);
	    int w = getWidth() - m_gap * 2 - 30;
	    m_step = w / m_max;
	    int m = m_marginal*m_step;
	    int o = m_optimal*m_step;
	    // get heigth
	    int h = 0;
	    if (m_top > m_gap)
	    	h = getHeight() - m_top - m_gap - 3;
	    else
	    	h = getHeight() - m_gap*2;
	    // limit
	    if (m > w) m = w;
	    if (o > w) o = w;
	    if (m > o) o = m;
	    // draw the rectangle
	    g2d.setColor(Color.GRAY);
	    g2d.drawRect(m_gap, m_top + 7, getWidth() - m_gap * 2 - 2, h - 1);
	    // draw the rest?
	    if(m_count > 0) {
	    	// draw limits
		    g2d.setColor(Color.BLACK);
		    g2d.drawString("Marginalt", m + m_gap + 5, m_top + 3);
		    g2d.drawString("Optimalt", o + m_gap + 5, m_top + 3);
		    g2d.drawLine(m + m_gap + 15, m_top + 7, m + m_gap + 15, m_top + h + 6);
		    g2d.drawLine(o + m_gap + 15, m_top + 7, o + m_gap + 15, m_top + h + 6);
		    int i = m_count*m_step;
		    if (i<m) {
		    	g2d.setColor(co);
		    }
		    else if (i >= m&&i < o) {
		    	g2d.setColor(cy);
		    }
		    else {
		    	g2d.setColor(cb);
		    }
		    if (m_count - m_marginal > 0)
		    	tasks = "+ " + java.lang.Math.abs(m_count - m_marginal) + " oppgaver";
		    else if (m_count - m_marginal == 0)
		    	tasks = "+ 0 oppgaver";
		    else
		    	tasks = "- " + java.lang.Math.abs(m_count - m_marginal) + " oppgaver";
		    // limit 
		    if (i > w) i = w;
		    // fill state rectangle
		    g2d.fillRect(m_gap + 15, m_top + 20, i + 1, h - 25);
		    // set text color
		    g2d.setColor(Color.BLACK);
		    // get left text position
		    if(i - 40 < m_gap + 20)
		    	i = m_gap + 20;
		    else
		    	i = i - 40;
		    // draw string
	    	g2d.drawString(tasks, i, m_top + h / 2 + 12);
	        // restore	        
	    }
        g.setColor(c);
        g.setFont(f);
    }
}