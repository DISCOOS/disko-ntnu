package org.redcross.sar.map.element;

import java.io.IOException;
import java.net.UnknownHostException;

import org.redcross.sar.map.feature.IDiskoFeature;

import com.esri.arcgis.carto.GroupElement;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.interop.AutomationException;

public class AbstractDiskoFeature extends GroupElement implements IDiskoFeature {

	private static final long serialVersionUID = 1L;

	protected boolean isActive;
	protected IDisplay display;
	protected boolean isVisible = true;
	
	public AbstractDiskoFeature() throws IOException, UnknownHostException {

		// forward
		super();
		
	}

	public IDisplay display() {
		return display;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void activate(IDisplay display) throws IOException, AutomationException {
		// prepare
		this.display = display;
		// set flag
		isActive = true;
		// forward?
		if(isVisible) {
			// loop over elements
			IEnumElement items = getElements();
			IElement it = items.next();
			while(it!=null) {
				it.activate(display);
				it = items.next();
			}
			// forward
			super.activate(display);
		}
	}

	public void deactivate() throws IOException, AutomationException {
		// any change?
		if(isActive) {
			// reset flag
			isActive = false;
			// loop over elements
			IEnumElement items = getElements();
			IElement it = items.next();
			while(it!=null) {
				it.deactivate();
				it = items.next();
			}
			// forward
			super.deactivate();
		}
	}
		
	public boolean isVisible() {
		return isVisible && isActive;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		try {
			if(!isVisible)
				deactivate();
			else if(display!=null)
				activate(display);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}