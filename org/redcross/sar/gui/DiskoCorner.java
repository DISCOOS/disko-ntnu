package org.redcross.sar.gui;

import java.awt.Color;

import javax.swing.JLabel;

import org.redcross.sar.gui.factory.UIFactory;

public class DiskoCorner extends JLabel {

	private static final long serialVersionUID = 1L;
	
	/* ==========================================================
	 * Constructors
	 * ========================================================== */

	public DiskoCorner() {
		super();
		setOpaque(true);
		setBackground(Color.LIGHT_GRAY);
		setBorder(UIFactory.createBorder(0,0,1,0));
	}	
	
}
