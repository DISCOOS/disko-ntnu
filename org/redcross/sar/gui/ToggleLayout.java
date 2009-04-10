package org.redcross.sar.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;

import javax.swing.BoxLayout;

import org.redcross.sar.gui.panel.ITogglePanel;

/**
 * This class honors the ITogglePanel size requirements. Use this layout manager
 * when several ITogglePanel instances are placed within the same container.
 *
 * If only one ITogglePanel instance is added, any Layout manager can
 * be used.
 *
 * @author kennetgu
 *
 */

public class ToggleLayout implements LayoutManager, LayoutManager2 {

	public static int X_AXIS = BoxLayout.X_AXIS;
	public static int Y_AXIS = BoxLayout.Y_AXIS;

	private final int direction;

	/* ==============================================================
	 * Constructors
	 * ============================================================== */

	public ToggleLayout(int direction) {
		// prepare
		this.direction = direction;
	}

	/* ==============================================================
	 * Layout manager implementation
	 * ============================================================== */

	@Override
	public void addLayoutComponent(String name, Component c) {
		/* NOP */
	}

	@Override
	public void addLayoutComponent(Component c, Object constraints) {
		/* NOP */
	}

	@Override
	public void removeLayoutComponent(Component c) {
		/* NOP */
	}

	@Override
	public float getLayoutAlignmentX(Container c) {
		/* NOP */
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(Container c) {
		/* NOP */
		return 0;
	}

	@Override
	public void layoutContainer(Container c) {
		// get insets
		Insets insets = c.getInsets();
		// get sizes
		Dimension size = c.getSize();
		Dimension min = c.isMinimumSizeSet() ? c.getMinimumSize() : null;
		Dimension pref = c.isPreferredSizeSet() ? c.getPreferredSize() : null;
		Dimension max = c.isMaximumSizeSet() ? c.getMaximumSize() : null;
		// get components
		Component[] items = c.getComponents();
		// translate direction
		if(direction==X_AXIS) {
			// loop over all items
			for(Component it : items) {
				// translate component 
				if(it instanceof ITogglePanel) {
					
				}
			}
		}
		else if(direction==Y_AXIS) {
			
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension preferredLayoutSize(Container c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dimension maximumLayoutSize(Container c) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void invalidateLayout(Container c) {
		/* NOP */
	}

}
