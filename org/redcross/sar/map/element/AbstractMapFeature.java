package org.redcross.sar.map.element;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import org.redcross.sar.map.event.IMapFeatureListener;
import org.redcross.sar.map.feature.IMapFeature;

import com.esri.arcgis.carto.GroupElement;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractMapFeature extends GroupElement implements IMapFeature {

	private static final long serialVersionUID = 1L;

	protected boolean isActive;
	protected IDisplay display;
	protected boolean isVisible = true;

	protected EventListenerList listeners = new EventListenerList();

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	public AbstractMapFeature() throws IOException, UnknownHostException {

		// forward
		super();

	}

	/* =======================================================
	 * Public methods
	 * ======================================================= */

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

	/* =======================================================
	 * IMapFeature implementation
	 * ======================================================= */

	public boolean isVisible() {
		return isVisible && isActive;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		// notify
		fireFeatureChanged();
		/*
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
		*/
	}

	public void addMapFeatureListener(IMapFeatureListener listener) {
		listeners.add(IMapFeatureListener.class, listener);
	}

	public void removeMapFeatureListener(IMapFeatureListener listener) {
		listeners.remove(IMapFeatureListener.class, listener);
	}

	/* =======================================================
	 * Helper methods
	 * ======================================================= */

	protected void fireFeatureChanged() {
		ChangeEvent e = new ChangeEvent(this);
		IMapFeatureListener[] list = listeners.getListeners(IMapFeatureListener.class);
		for(int i=0; i<list.length;i++) {
			list[i].onFeatureChanged(e);
		}
	}

}