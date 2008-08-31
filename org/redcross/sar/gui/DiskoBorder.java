package org.redcross.sar.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

public class DiskoBorder implements Border {

	  private int m_top;  
	  private int m_left;   
	  private int m_bottom;   
	  private int m_right;  
	  private Color m_color = null;   

	  public DiskoBorder() {  
		  this(1,1,1,1,Color.GRAY);   
	  }   
	  
	  public DiskoBorder(int top, int left, int bottom, int right, Color color) {  
	      this.m_top = top;   
	      this.m_left = left;   
	      this.m_bottom = bottom;   
	      this.m_right = right;   
	      this.m_color = color;   
	  }   

	  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {


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
	  }   

	  public Insets getBorderInsets(Component c) {  
	      return new Insets(m_top, m_left, m_bottom, m_right);  
	  }   

	  public boolean isBorderOpaque() {   
	      return true;  
	  }   

}
