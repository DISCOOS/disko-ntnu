package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

public class ScaleBar extends JComponent {

	private static final long serialVersionUID = 1L;
	private Color color = Color.red;
	private int max = 50;
	private int value = 0;

	public ScaleBar() {
	}
	
	@Override
	public void paint(Graphics g) {
		double width = ((double)getWidth()/(double)max)*value;
		g.setColor(color);
		g.fillRect(0, 0, (int)width, getHeight());
	}
	
	public void setValue(int value) {
		this.value = value;
		repaint();
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void setMaximum(int max) {
		this.max = max;
	}
}
