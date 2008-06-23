package org.redcross.sar.gui;

import javax.swing.JPanel;

import org.redcross.sar.app.Utils;

import java.awt.*;
import java.awt.geom.*;

/** 
 *
 * @author  kennetgu
 * 
 */

public class CompassPanel extends JPanel {

	/** Creates new Compass */
	private int bearing;
	private final int OFFSET = 90;
	private final int START_POSITION = 15;
	private final double LINE_WIDTH = 1.0;
	
	private int radius = 20;

	/**
	 * Compass constructor
	 */

	public CompassPanel(int direction, int radius) {
		this.radius = radius;
		this.bearing = OFFSET - direction;
		this.setBackground(Color.WHITE);
    	Utils.setFixedSize(this,(START_POSITION + radius)*2,(START_POSITION + radius)*2);
	}


	/**
	 * paintComponent paints the panel holding the compass
	 * @param Graphics g is the graphics context
	 * @return void
	 */

	public void paintComponent ( Graphics g )  {

		// forward
		super.paintComponent(g);

		// cast to Graphics2D
		Graphics2D g2 = (Graphics2D)g;

		// calculate diameter
		int diameter = 2 * radius;

		// draw outline of compass 
		g2.setColor(Color.BLACK);
		g2.drawOval(START_POSITION,START_POSITION,diameter,diameter);

		// set font
		g.setFont(g.getFont().deriveFont(Font.BOLD,(float)10.0));

		// get for text widths
		Rectangle2D[] tfm = {null,null,null,null};
		FontMetrics fm = g.getFontMetrics();
		tfm[0] = fm.getStringBounds("N", g);
		tfm[1] = fm.getStringBounds("E", g);
		tfm[2] = fm.getStringBounds("S", g);
		tfm[3] = fm.getStringBounds("W", g);      

		// draw compass directions
		g2.drawString ( "N", (int)(START_POSITION + (diameter - tfm[0].getWidth()) / 2 + 1), START_POSITION - 3); 
		g2.drawString ( "E", START_POSITION + diameter + 3, (int)(START_POSITION + (diameter + tfm[1].getHeight() / 2) / 2)); 
		g2.drawString ( "S", (int)(START_POSITION + (diameter - tfm[0].getWidth()) / 2 + 1), (int)(START_POSITION + diameter + tfm[2].getHeight() - 3)); 
		g2.drawString ( "W", (int)(START_POSITION - tfm[3].getWidth()), (int)(START_POSITION + (diameter + tfm[1].getHeight() / 2) / 2));

		// draw arrow in current direction
		g.setColor(Color.RED);
		double r = radius-5;
		g2.setStroke(new BasicStroke((float)LINE_WIDTH*3));
		double x0 = START_POSITION + radius + r * Math.cos(Math.toRadians((bearing+180)));
		double y0 = START_POSITION + radius + r * Math.sin(Math.toRadians(bearing+180));
		double x1 = START_POSITION + radius + r * Math.cos(Math.toRadians(bearing));
		double y1 = START_POSITION + radius + r * Math.sin(Math.toRadians(bearing));
		paintArrow(g, x0, y0, x1, y1);

	} 

	private void paintArrow(Graphics g, double x0, double y0, double x1,double y1){

		double deltaX = x1 - x0;
		double deltaY = y1 - y0;
		double frac = 0.1;

		g.drawLine((int)x0,(int)y0,(int)x1,(int)y1);
		g.drawLine((int)(x0 + (1-frac)*deltaX + frac*deltaY), (int)(y0 + (1-frac)*deltaY - frac*deltaX), (int)x1, (int)y1);
		g.drawLine((int)(x0 + (1-frac)*deltaX - frac*deltaY), (int)(y0 + (1-frac)*deltaY + frac*deltaX), (int)x1, (int)y1);

	}


	public int getBearing() {
		return bearing;
	}
	
	/**
	 * setCompassSetting sets the new direction as a compass position
	 * @param int newSetting is a new heading angle in degrees
	 * @return void
	 */
	public void setBearing ( int bearing )  {
		this.bearing = bearing - OFFSET;
		repaint();
	}  

}  