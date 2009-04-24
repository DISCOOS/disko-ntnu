package org.redcross.sar.map.layer;

import java.util.List;

import org.apache.log4j.Logger;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.element.AbstractGroupElement;
import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.event.MapElementEvent;
import org.redcross.sar.map.event.MapLayerEventStack;

import com.esri.arcgis.carto.CompositeGraphicsLayer;
import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IElementCollection;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.carto.IGroupElement;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;

public abstract class AbstractCompositeGraphicsLayer<D extends IData,G extends IData> 
	extends AbstractMapLayer<CompositeGraphicsLayer,IElement,D,G> {

	protected boolean isActive;
	
	protected ISpatialReference srs;
	protected IActiveView activeView;
	protected CompositeGraphicsLayer layerImpl;

	private static Logger logger = Logger.getLogger(AbstractCompositeGraphicsLayer.class);  

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public AbstractCompositeGraphicsLayer(
			String name, Enum<?> classCode, Enum<?> layerCode, 
			ISpatialReference srs, MapLayerEventStack eventStack){

		// forward
		super(name,classCode,layerCode,eventStack);

		try {
			// prepare
			this.layerImpl = new CompositeGraphicsLayer();
			this.isActive = false;
			this.srs = srs;
			
			// set name
			layerImpl.setName(name);
		} catch (Exception e) {
			logger.error("Failed create instance of class",e);
		}
		
	}

	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	public IActiveView getActiveView() {
		return activeView;
	}

	public IScreenDisplay getDisplay() {
		try {
			return getActiveView().getScreenDisplay();
		} catch (Exception e) {
			logger.error("Failed to get screen display", e);
		}
		return null;
	}
	
	public boolean isActive() {
		return isActive;
	}

	@SuppressWarnings("unchecked")
	public void activate(IActiveView activeView) {
		// any change?
		if(!isActive) {
			try {
				// prepare
				this.activeView = activeView;
				// forward
				for(IMapElement<IElement,D,G> it : elementList) {
					if(it.isVisible()) {
						setDirtyExtent(activate(it));
					}
				}
				// forward
				layerImpl.activate(getDisplay());
				layerImpl.setVisible(true);
				refresh();
				// set flag
				isActive = true;
			} catch (Exception e) {
				logger.error("Failed to activate layer",e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void deactivate() {
		// any change?
		if(!isActive) {
			try {
				// forward
				for(IMapElement<IElement,D,G> it : elementList) {
					deactivate(it);
				}
				// forward
				layerImpl.deactivate();
				layerImpl.setVisible(false);
				// cleanup
				activeView = null;
				isActive = false;
			} catch (Exception e) {
				logger.error("Failed to deactivate layer",e);
			}
		}
	}

	public ISpatialReference getSpatialReference() {
		return srs;
	}

	public void setSpatialReferenceByRef(ISpatialReference srs) {
		try {
			// forward
			layerImpl.setSpatialReferenceByRef(srs);
			// prepare
			this.srs = srs;
		} catch (Exception e) {
			logger.error("Failed to set spatial reference",e);
		}
	}

	/* ===============================================================
	 * IMapLayer implementation
	 * =============================================================== */

	public CompositeGraphicsLayer getLayerImpl() {
		return layerImpl;
	}

	public boolean isVisible() {
		try {
			return getLayerImpl().isVisible();
		} catch (Exception e) {
			logger.error("Failed to get visible state",e);
		}
		return false;
	}

	public void setVisible(boolean isVisible) {
		try {
			getLayerImpl().setVisible(isVisible);
		} catch (Exception e) {
			logger.error("Failed to set visible state",e);
		}
	}
	
		
	/* ===============================================================
	 * Protected methods
	 * =============================================================== */

	protected abstract IMapElement<IElement,D,G> createElementImpl(D dataObj);
	protected abstract IEnvelope updateElementImpl(IMapElement<IElement, D, G> element, List<G> geodata);

	protected IEnvelope addElementImpl(IMapElement<IElement,D,G> element) {
		return addElementImpl(element,element.isVisible());
	}
	
	protected IEnvelope addElementImpl(IMapElement<IElement,D,G> element, boolean isVisible) {

		// initialize
		IEnvelope extent = null;
		
		try {
			
			// get IElement implementation
			IElement impl = element.getElementImpl();
			
			// invisible elements are added as overflow elements
			if(isVisible) {				
				layerImpl.addElement(impl, 0);
				if(impl instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						layerImpl.addElement(it,0);
					}
				}
				if(isActive) {
					extent = activate(element);
				} 
			}
			else {
				layerImpl.addOverflowElement(impl);
				if(impl instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)impl).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						layerImpl.addOverflowElement(it);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Failed to add IElement implementation",ex);
		}
		// finished
		return extent;

	}
	
	@SuppressWarnings("unchecked")
	protected IEnvelope activate(IMapElement<IElement, D, G> element) {
		
		try {
			if(element instanceof AbstractGroupElement) {
				((AbstractGroupElement)element).activate(getDisplay());
				
			} else {
				element.getElementImpl().activate(getDisplay());
			}
		} catch (Exception e) {
			logger.error("Failed to activate IMapElement object",e);
		}
		
		// finished
		return getExtent(element.getElementImpl());
		
	}	

	protected IEnvelope removeElementImpl(IMapElement<IElement,D,G> element) {
		return removeElementImpl(element,element.isVisible());
	}
	
	protected IEnvelope removeElementImpl(IMapElement<IElement,D,G> element, boolean isVisible) {
			
		// initialize
		IEnvelope extent = null;
		
		try {
			
			// get IElement implementation
			IElement impl = element.getElementImpl();
			
			// invisible elements are removed from overflow elements
			if(isVisible()) {
				extent = getExtent(impl);
				layerImpl.deleteElement(impl);
				if(impl instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)impl).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						layerImpl.deleteElement(it);
					}
				}
				deactivate(element);
			}
			else {
				layerImpl.deleteOverflowElement(impl);
				if(impl instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)impl).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						layerImpl.deleteOverflowElement(it);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Failed to remove IElement implementation",ex);
		}
		
		// finished
		return extent;

	}
		
	@SuppressWarnings("unchecked")
	protected IEnvelope deactivate(IMapElement<IElement, D, G> element) {
		
		// initialize
		IEnvelope extent = null;
		
		try {
			if(element instanceof AbstractGroupElement) {
				((AbstractGroupElement)element).deactivate();
				
			} else {
				element.getElementImpl().deactivate();
			}
		} catch (Exception e) {
			logger.error("Failed to deactivate IMapElement object",e);
		}
		
		// finished
		return extent;
		
	}
	
	@SuppressWarnings("unchecked")
	protected void onElementChanged(MapElementEvent e) {
		// supported?
		if(e.getSource() instanceof IMapElement) {
			IMapElement<IElement,D,G> element = (IMapElement<IElement,D,G>)e.getSource();
			if(e.isStateEvent()) {
				IElement impl = element.getElementImpl();
				if(element.isVisible() && exists(impl,true)) {
					removeElementImpl(element,false);
					addElementImpl(element,true);
				}
				else if(!element.isVisible() && exists(impl,false)) {
					removeElementImpl(element,true);
					addElementImpl(element,false);
				}
			}
			else if(e.isCreateEvent()){
				// forward
				updateDataObject(element.getDataObject());
			}
		}
	}
	
	
	protected void refresh(IEnvelope extent) {
		try {
			long tic = System.currentTimeMillis();
			if(refreshRate==-1 || tic-refreshTime>refreshRate) {
				if(extent!=null) {
					getActiveView().partialRefresh((short)esriViewDrawPhase.esriViewGeography,getLayerImpl(),extent);
					clearDirtyExtent();
				}
				refreshTime = tic;
			}
			else {
				setDirtyExtent(extent);
			}
		} catch (Exception e) {
			logger.error("Failed to refresh display",e);
		}
	}	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

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
	
	private boolean exists(IElement element, boolean inOverflow) {
		try {
			IElementCollection container = (inOverflow ? layerImpl.getOverflowElements() : (IElementCollection)layerImpl);
			int count = container.getCount();
			IElement[] it = new IElement[1];
			for(int i=0;i<count;i++) {
				container.queryItem(i, it, null);
				if(it[0]==element) return true;
			}
		} catch (Exception ex) {
			logger.error("Failed to check for existance",ex);
		}
		// not found
		return false;
	}

}