package org.redcross.sar.gui;

import java.awt.Window;

import javax.swing.RootPaneContainer;

public class DiskoGlassPaneUtils {


	/*========================================================
	 * Public static methods
	 *======================================================== */

	public static DiskoGlassPane createGlassPane(Window window) {
		return new DiskoGlassPane(window);
	}

	public static int isLocked() {
		int count = 0;
		for(Window it : Window.getWindows()) {
			if(it instanceof RootPaneContainer) {
				RootPaneContainer c = (RootPaneContainer)it;
				if(c.getGlassPane() instanceof DiskoGlassPane) {
					DiskoGlassPane g = (DiskoGlassPane)c.getGlassPane();
					if(g.isLocked()) count++;
				}
			}
		}
		return count;
	}

	public static int setLocked(boolean isLocked) {
		int count = 0;
		for(Window it : Window.getWindows()) {
			if(it instanceof RootPaneContainer) {
				RootPaneContainer c = (RootPaneContainer)it;
				if(c.getGlassPane() instanceof DiskoGlassPane) {
					DiskoGlassPane g = (DiskoGlassPane)c.getGlassPane();
					if(g.setLocked(isLocked)) {
						count++;
					}
				}
			}
		}
		return count;
	}


}
