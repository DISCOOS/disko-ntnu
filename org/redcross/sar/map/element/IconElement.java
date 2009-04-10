package org.redcross.sar.map.element;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.carto.PngPictureElement;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.ITrackCancel;

public class IconElement extends PngPictureElement {

	private static final long serialVersionUID = 1L;

	private boolean isVisible = true;

	public IconElement() throws IOException, UnknownHostException {
		// forward
		super();
		// reset background
		setBackground(null);
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	@Override
	public void drawBackground(IDisplay display, ITrackCancel tracker)
			throws IOException, AutomationException {
		// Do not draw background
		System.out.println();
	}



}
