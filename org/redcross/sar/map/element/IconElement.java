package org.redcross.sar.map.element;

import java.io.IOException;
import java.net.UnknownHostException;

import com.esri.arcgis.carto.PngPictureElement;

public class IconElement extends PngPictureElement {

	private static final long serialVersionUID = 1L;

	private boolean isVisible = true; 
	
	public IconElement() throws IOException, UnknownHostException {
		// forward
		super();
	}

	public boolean isVisible() {
		return this.isVisible;
	}
	
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}
	
}
