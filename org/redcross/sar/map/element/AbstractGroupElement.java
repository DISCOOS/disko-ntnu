package org.redcross.sar.map.element;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData;

import com.esri.arcgis.carto.GroupElement;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.carto.IGroupElement;
import com.esri.arcgis.display.IDisplay;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;

public abstract class AbstractGroupElement<D extends IData, G extends IData> extends AbstractMapElement<IElement,D,G> {

	private static final long serialVersionUID = 1L;

	protected boolean isActive;
	protected IDisplay display;
	protected GroupElement elementImpl;
	
	protected static Logger logger = Logger.getLogger(AbstractGroupElement.class);
	
	/* =======================================================
	 * Constructors
	 * ======================================================= */

	public AbstractGroupElement(Object id, D dataObj, IGeodataCreator<D,G> creator) {

		// forward
		super(id,dataObj,creator);

		try {
			// prepare
			this.elementImpl = new GroupElement();
		} catch (Exception e) {
			logger.error("Failed to create GroupElement",e);
		}
		
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

	public void activate(IDisplay display) {
		try {
			// forward?
			if(isVisible) {
				// loop over elements
				IEnumElement items = elementImpl.getElements();
				IElement it = items.next();
				while(it!=null) {
					it.activate(display);
					it = items.next();
				}
				// forward
				elementImpl.activate(display);
			}
			// prepare
			this.display = display;
			// set flag
			isActive = true;
		} catch (Exception e) {
			logger.error("Failed to active AbstractGroupElement",e);
		}
	}

	public void deactivate() {
		try {
			// any change?
			if(isActive) {
				// loop over elements
				IEnumElement items = elementImpl.getElements();
				IElement it = items.next();
				while(it!=null) {
					it.deactivate();
					it = items.next();
				}
				// forward
				elementImpl.deactivate();
				// reset flag
				isActive = false;
			}
		} catch (Exception e) {
			logger.error("Failed to deactive AbstractGroupElement",e);
		}
	}
	
	

	/* =======================================================
	 * IMapFeature implementation
	 * ======================================================= */

	public GroupElement getElementImpl() {
		return elementImpl;
	}
	
	public boolean isVisible() {
		return super.isVisible() && isActive;
	}
	
	public IEnvelope getExtent() {
		return getExtent(elementImpl);
	}

	/* =======================================================
	 * Helper methods
	 * ======================================================= */
	
	protected static IEnvelope getExtent(IElement element) {
		return getUnion(null,element);
	}
	
	protected static IEnvelope getUnion(IEnvelope extent, IElement element) {
		if(element!=null) {
			try {
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					IElement it = items.next();
					while(it!=null) {
						if(extent==null)
							extent = getUnion(extent,it);
						else
							extent.union(getUnion(extent,it));
						// get next
						it = items.next();
					}
					return extent;
				}
				else {
					IGeometry geom = element.getGeometry();
					return geom!=null ? geom.getEnvelope() : null;
				}
			} catch (Exception ex) {
				logger.error("Failed to get extent of IElement object",ex);
			}
		}
		return null;
	}
	

}