package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class DiskoBorder implements Border {

	 /*
	  private int m_top;
	  private int m_left;
	  private int m_bottom;
	  private int m_right;
	  */
	  private Color m_color;
	  private Border m_border;

	  /* ================================================================
	   * Constructors
	   * ================================================================ */

	  public DiskoBorder() {
		  this(1,1,1,1,Color.GRAY);
	  }

	  public DiskoBorder(int top, int left, int bottom, int right) {
		  this(top,left,bottom,right,Color.GRAY);
	  }

	  public DiskoBorder(int top, int left, int bottom, int right, Color color) {
		  /*
	      this.m_top = top;
	      this.m_left = left;
	      this.m_bottom = bottom;
	      this.m_right = right;
	      */
	      this.m_color = color;
	      this.m_border = BorderFactory.createMatteBorder(top, left, bottom, right, m_color);
	  }

	  /* ================================================================
	   * Overridden methods
	   * ================================================================ */

	  @Override
	  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {

		  // forward
		  m_border.paintBorder(c, g, x, y, width, height);

		  /*
		  // get insets
	      Insets insets = getBorderInsets(c);

	      // get old color
	      Color old = g.getColor();

	      // apply color
	      //if (m_color != null)
	          g.setColor(m_color);

	      // draw borders
	      g.fillRect(0, 0, width-insets.right, insets.top); // TOP
	      g.fillRect(0, insets.top-2, insets.left, height-insets.top+2); // LEFT
	      g.fillRect(insets.left-2, height-insets.bottom, width-insets.left+2, insets.bottom); // BOTTOM
	      g.fillRect(width-insets.right, 0, insets.right, height-insets.bottom+2); // RIGHT

	      // restore state
	      g.setColor(old);
	      */
	  }

	  @Override
	  public Insets getBorderInsets(Component c) {
	      return m_border.getBorderInsets(c);
	  }

	  @Override
	  public boolean isBorderOpaque() {
	      return m_border.isBorderOpaque();
	  }

}
