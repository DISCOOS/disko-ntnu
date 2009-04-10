package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

public class DiskoBorder implements Border {

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
