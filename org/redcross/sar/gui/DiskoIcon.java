package org.redcross.sar.gui;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;

/**
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * @author Santhosh Kumar T
 */
public class DiskoIcon implements Icon {
    private Icon delegate;

    public DiskoIcon(Icon delegate){
        this(delegate, UIManager.getColor("textHighlight"), 0.5F);
    }

    public DiskoIcon(Icon delegate, Color color){
        this(delegate, color, 0.5F);
    }

    public DiskoIcon(Icon delegate, Color color, float alpha){
        if(delegate==null) delegate = new ImageIcon(new BufferedImage(48,48,BufferedImage.TYPE_INT_RGB));
        this.delegate = delegate;
        createColorMask(color, alpha);
        createMarkMask();
    }

    /*--------------------------------[ Color mask ]--------------------------------*/

    private BufferedImage colorMask;

    private void createColorMask(Color color, float alpha){
        colorMask = new BufferedImage(delegate.getIconWidth(), delegate.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gbi = (Graphics2D)colorMask.getGraphics();
        delegate.paintIcon(new JLabel(), gbi, 0, 0);
        gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        gbi.setColor(color);
        gbi.fillRect(0, 0, colorMask.getWidth()-1, colorMask.getHeight()-1);
    }

    /*----------------------------------[ Color Painted ]-----------------------------------*/
    
    private boolean isColored = false;

    public boolean isColored(){
        return isColored;
    }

    public void setColored(boolean isColored){
        this.isColored = isColored;
    }

    /*--------------------------------[ Mark mask ]--------------------------------*/

    private BufferedImage markMask;

    private void createMarkMask(){
    	markMask = new BufferedImage(delegate.getIconWidth(), delegate.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D gbi = (Graphics2D)markMask.getGraphics();
        delegate.paintIcon(new JLabel(), gbi, 0, 0);
        //gbi.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.5F));
        gbi.setColor(Color.BLACK);
        int x = markMask.getWidth();
        int y = markMask.getHeight();
        int d = 0;
        gbi.drawLine(x, y, x, y-d);
        gbi.drawLine(x, y-d, x-d, y);
        gbi.drawLine(x-d, y, x, y);
        
    }
    
    /*----------------------------------[ Mark Painted ]-----------------------------------*/
    
    private boolean isMarked = false;

    public boolean isMarked(){
        return isMarked;
    }

    public void setMarked(boolean isMarked){
        this.isMarked = isMarked;
    }
    
    /*-------------------------------------------------[ Icon Interface ]---------------------------------------------------*/
    
    public void paintIcon(Component c, Graphics g, int x, int y){
        delegate.paintIcon(c, g, x, y);
        if(isMarked)
            g.drawImage(markMask, x, y, c);
        if(isColored)
            g.drawImage(colorMask, x, y, c);
    }

    public int getIconWidth(){
        return delegate.getIconWidth();
    }

    public int getIconHeight(){
        return delegate.getIconHeight();
    }
}