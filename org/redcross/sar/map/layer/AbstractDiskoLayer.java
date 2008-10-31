package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.map.feature.IDiskoFeature;

import com.esri.arcgis.carto.CompositeGraphicsLayer;
import com.esri.arcgis.carto.IElement;
import com.esri.arcgis.carto.IEnumElement;
import com.esri.arcgis.carto.IGroupElement;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.interop.AutomationException;

public abstract class AbstractDiskoLayer extends
					CompositeGraphicsLayer implements IDiskoLayer {

	protected boolean isActive;
	protected LayerCode layerCode;
	protected ISpatialReference srs;
	protected IScreenDisplay display;
	protected List<IDiskoFeature> features;

	private int refreshRate;
	private long refreshTime;
	private IEnvelope refreshExtent;


	public AbstractDiskoLayer(String name, LayerCode layerCode,
			ISpatialReference srs) throws UnknownHostException, IOException {

		// forward
		super();

		// prepare
		this.isActive = false;
		this.layerCode = layerCode;
		this.srs = srs;
		this.features = new ArrayList<IDiskoFeature>();
		this.refreshRate = -1;
		this.refreshTime = System.currentTimeMillis();
		this.refreshExtent = null;

		// forward
		setName(name);

	}

	public LayerCode getLayerCode() {
		return layerCode;
	}

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
			for(IDiskoFeature it : features) {
				if(it instanceof IElement) {
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
			for(IDiskoFeature it : features) {
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

	protected void addFeature(IDiskoFeature feature) {
		features.add(feature);
		try {
			if(feature instanceof IElement) {
				IElement element = (IElement)feature;
				addElement(element, 0);
				if(feature instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					IElement it = items.next();
					while(it!=null) {
						addElement(it,0);
						it = items.next();
					}
				}
				if(isActive) element.activate(display());
			}
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void removeFeature(IDiskoFeature feature) {
		features.remove(feature);
		try {
			if(feature instanceof IElement) {
				IElement element = (IElement)feature;
				deleteElement(element);
				if(feature instanceof IGroupElement) {
					IEnumElement items = ((IGroupElement)element).getElements();
					IElement it = items.next();
					while(it!=null) {
						deleteElement(it);
						it = items.next();
					}
				}
				element.deactivate();
			}

		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int inMillis) {
		refreshRate = inMillis;
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


}