package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;

import org.redcross.sar.map.event.IMapFeatureListener;
import org.redcross.sar.map.feature.IMapFeature;

import com.esri.arcgis.carto.CompositeGraphicsLayer;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IElementCollection;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.carto.IGroupElement;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractMapLayer extends
					CompositeGraphicsLayer implements IMapLayer {

	protected boolean isActive;
	protected LayerCode layerCode;
	protected ISpatialReference srs;
	protected IScreenDisplay display;
	protected List<IMapFeature> features;

	private int refreshRate;
	private long refreshTime;
	private IEnvelope refreshExtent;

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public AbstractMapLayer(String name, LayerCode layerCode,
			ISpatialReference srs) throws UnknownHostException, IOException {

		// forward
		super();

		// prepare
		this.isActive = false;
		this.layerCode = layerCode;
		this.srs = srs;
		this.features = new ArrayList<IMapFeature>();
		this.refreshRate = -1;
		this.refreshTime = System.currentTimeMillis();
		this.refreshExtent = null;

		// forward
		setName(name);

	}

	/* ===============================================================
	 * Public methods
	 * =============================================================== */

	public IScreenDisplay display() {
		return display;
	}

	public boolean isActive() {
		return isActive;
	}

	@Override
	public void activate(IScreenDisplay display) throws IOException,
			AutomationException {
		// any change?
		if(!isActive) {
			// prepare
			this.display = display;
			// forward
			for(IMapFeature it : features) {
				if(it.isVisible() && it instanceof IElement) {
					((IElement)it).activate(display);
				}
			}
			// set flag
			isActive = true;
			// forward
			super.activate(display);
			super.setVisible(true);
		}
	}

	@Override
	public void deactivate() throws IOException, AutomationException {
		// any change?
		if(!isActive) {
			// forward
			for(IMapFeature it : features) {
				if(it instanceof IElement) {
					((IElement)it).deactivate();
				}
			}
			// reset flag
			isActive = false;
			// forward
			super.deactivate();
			super.setVisible(false);
		}
	}

	@Override
	public ISpatialReference getSpatialReference() throws IOException,
			AutomationException {
		return srs;
	}

	@Override
	public void setSpatialReferenceByRef(ISpatialReference srs)
			throws IOException, AutomationException {
		// prepare
		this.srs = srs;
		// forward
		super.setSpatialReferenceByRef(srs);
	}

	/* ===============================================================
	 * IMapLayer implementation
	 * =============================================================== */

	public LayerCode getLayerCode() {
		return layerCode;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int inMillis) {
		refreshRate = inMillis;
	}

	/* ===============================================================
	 * Protected methods
	 * =============================================================== */

	protected void addFeature(IMapFeature feature) {
		if(feature instanceof IElement) {
			if(!features.contains(feature) && features.add(feature)) {
				addElement((IElement)feature, feature.isVisible());
			}
		}
	}

	protected void addElement(IElement element, boolean isVisible) {
		try {
			if(isVisible) {
				addElement(element, 0);
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						addElement(it,0);
					}
				}
				if(isActive) element.activate(display());
			}
			else {
				addOverflowElement(element);
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						addOverflowElement(it);
					}
				}
			}
		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}

	protected void removeFeature(IMapFeature feature) {
		if(feature instanceof IElement) {
			if(features.contains(feature) && features.remove(feature)) {
				removeElement((IElement)feature, feature.isVisible());
			}
		}
	}

	protected void removeElement(IElement element, boolean isVisible) {
		try {
			if(isVisible) {
				deleteElement(element);
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						deleteElement(it);
					}
				}
				element.deactivate();
			}
			else {
				deleteOverflowElement(element);
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					items.reset();
					IElement it = null;
					while((it = items.next())!=null) {
						deleteOverflowElement(it);
					}
				}
			}
		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

	}

	protected static IEnvelope getExtent(IElement element) {
		if(element!=null) {
			try {
				if(element instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					IElement it = items.next();
					IEnvelope extent = null;
					while(it!=null) {
						if(extent==null)
							extent = getExtent(it);
						else
							extent.union(getExtent(it));
						// get next
						it = items.next();
					}
					return extent;
				}
				else {
					IGeometry geom = element.getGeometry();
					return geom!=null ? geom.getEnvelope() : null;
				}
			} catch (AutomationException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
		return null;
	}

	protected void refresh(IEnvelope extent) {
		try {
			long tic = System.currentTimeMillis();
			if(refreshRate==-1 || tic-refreshTime>refreshRate) {
				if(extent!=null) {
					display().invalidate(setRefreshExtent(extent), false, (short)esriViewDrawPhase.esriViewGeography);
					setRefreshExtent(null);

				}
				refreshTime = tic;
			}
			else {
				setRefreshExtent(extent);
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	private IEnvelope setRefreshExtent(IEnvelope extent) {
		try {
			if(extent==null)
				refreshExtent = null;
			else if(refreshExtent == null)
				refreshExtent = extent;
			else
				refreshExtent.union(extent);
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return refreshExtent;

	}

	private boolean exists(IElement element, boolean inOverflow) {
		try {
			IElementCollection container = (inOverflow ? getOverflowElements() : (IElementCollection)this);
			int count = container.getCount();
			IElement[] it = new IElement[1];
			for(int i=0;i<count;i++) {
				container.queryItem(i, it, null);
				if(it[0]==element) return true;
			}
		} catch (AutomationException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		// not found
		return false;
	}

	/* ===============================================================
	 * Anonymous classes
	 * =============================================================== */

	IMapFeatureListener listener = new IMapFeatureListener() {

		public void onFeatureChanged(ChangeEvent e) {
			// get element
			IElement it = (IElement)e.getSource();
			// supported?
			if(it instanceof IMapFeature) {
				IMapFeature feature = (IMapFeature)it;
				if(feature.isVisible() && exists(it,true)) {
					removeElement(it,false);
					addElement(it,true);
				}
				else if(!feature.isVisible() && exists(it,false)) {
					removeElement(it,true);
					addElement(it,false);
				}
			}
		}

	};

}