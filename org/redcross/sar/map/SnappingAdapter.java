/**
 * 
 */
package org.redcross.sar.map;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.redcross.sar.gui.IDrawDialog;
import org.redcross.sar.map.index.IndexedGeometry;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IIdentify;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnFullExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriSegmentExtension;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public class SnappingAdapter {

	private static final long serialVersionUID = 1L;
	
	private static final int SNAP_TOL_FACTOR = 200;
	
	// flags
	protected boolean isDirty = false;
	protected boolean isSnapToMode = false;
	
	// holds snapping geometry
	private Envelope searchEnvelope = null;
	
	// indexing
	private IndexedGeometry indexedGeometry = null;
	
	// map control listener
	private MapControlAdapter listener = null;
	
	// listeners
	private ArrayList<SnappingListener> listeners = null;
	
	// registered map
	private DiskoMap map = null;
	
	// snapping
	private IGeometry snapGeometry = null;
	private List<DiskoMap> maps = null;
	private List<IFeatureLayer> snapTo = null;
	private List<IFeatureLayer> snappable = null;
	
	public SnappingAdapter() {
		// prepare
		this.listener = new MapControlAdapter();
		this.listeners = new ArrayList<SnappingListener>();
		this.indexedGeometry = new IndexedGeometry();
		try {
			// create the search envelope
			searchEnvelope = new Envelope();
			searchEnvelope.putCoords(0, 0, 0, 0);
			searchEnvelope.setHeight(100);
			searchEnvelope.setWidth(100);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSnappingListener(SnappingListener listener) {
		listeners.add(listener);
	}
	
	public void removeSnappingListener(SnappingListener listener) {
		listeners.remove(listener);
	}
	
	public DiskoMap getMap() {
		return map;
	}
	
	public void register(DiskoMap map) throws IOException, AutomationException {
		// remove old listener?
		if(this.map!=null) {
			this.map.removeIMapControlEvents2Listener(listener);
		}
		// register
		this.map = map;
		// add listener to get snapping change events
		map.addIMapControlEvents2Listener(listener);
		// set dirty
		isDirty = true;
		// update
		updateSnappable();
		updateIndexing();
	}
	
	public void setSnapTolerance(double tolerance) throws IOException, AutomationException {
		searchEnvelope.setHeight(tolerance);
		searchEnvelope.setWidth(tolerance);
	}

	public int getSnapTolerance() throws IOException {
		return (int)searchEnvelope.getHeight();
	}
	
	public boolean isSnapReady() {
		return snapTo!=null && snapTo.size()>0;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public boolean isSnapToMode() {
		return isSnapToMode;
	}
	
	public void setSnapToMode(boolean isSnapToMode) 
	throws IOException, AutomationException {
		// set dirty flag
		isDirty = this.isSnapToMode != isSnapToMode && isSnapToMode;
		// update flag
		this.isSnapToMode = isSnapToMode;
		// update indexing
		updateIndexing();
	}
	
	/**
	 * Snap to point
	 * 
	 * @throws IOException
	 * @throws AutomationException
	 */	
	public Point doSnapTo(Point p) throws IOException, AutomationException {
		
		// is in mode?
		if(isSnapToMode) {
			// try to snap
			searchEnvelope.centerAt(p);
			Polyline polyline = (Polyline)indexedGeometry.search(searchEnvelope);
			// found geometry?
			if (polyline != null) {
				// prepare to get point
				polyline.densify(getSnapTolerance(), -1);
				p = getNearestPoint(polyline, p);
			}
	
			// update snap geometry
			snapGeometry = polyline;
			
		}
		
		// return point
		return p;
		
	}

	public Polyline doSnapTo(Polyline polyline) throws IOException, AutomationException {
		
		// snapping?
		if (snapTo.size()>0) {
			
			// cast to IIdentify
			ArrayList<IIdentify> l = new ArrayList<IIdentify>(snapTo.size()); 
			for (int i=0;i<snapTo.size();i++) {
				l.add((IIdentify)snapTo.get(i));
			}
			// clone
			Polyline snapped = (Polyline)polyline.esri_clone();
			// do the snapping
			MapUtil.snapPolyLineTo(snapped,l,getSnapTolerance());
			// return result
			return snapped;
		}		
		
		// return line
		return polyline;
	}
	
	private Point getNearestPoint(Polyline pline, Point point)
		throws IOException, AutomationException {
		// forward
		return (Point)pline.returnNearestPoint(point, esriSegmentExtension.esriNoExtension);
	}
	
	public List<IFeatureLayer> getSnappableLayers() {
		return snappable;		
	}
	
	public List<IFeatureLayer> getSnapToLayers() {
		return snapTo;
	}
	
	public boolean setSnapToLayers(List<IFeatureLayer> snapTo) 
		throws IOException, AutomationException {
		// reset flag
		isDirty = snapTo!=null && this.snapTo==null;
		// valid?
		if(snapTo!=null && this.snapTo!=null) {
			// check if dirty
			for(int i = 0;i<snapTo.size();i++) {
				if(!this.snapTo.contains(snapTo.get(i))) {
					// set dirty
					isDirty = true;
					// finished
					break;
				}
			}
		}
		// initialize?
		if(isDirty) {
			// save layers
			this.snapTo = snapTo;
			// forward
			updateIndexing();
			// notify
			fireSnapToChange();
		}
		
		return snapTo!=null && this.snapTo==null;
	}
	
	public IndexedGeometry getIndexing() {
		return indexedGeometry;
	}
	
	public IGeometry getSnapGeometry() {
		return snapGeometry;
	}
	
	private void updateSnappable() throws IOException, AutomationException {
		// set snap tolerance of tool
		setSnapTolerance(map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR);
		// get available snappable layers from map and update draw dialg
		snappable = map.getSnappableLayers();	
		// notify
		fireSnappableChange();
	}

	private void updateIndexing() throws IOException, AutomationException {
		// is dirty?
		if(isDirty) {
			// is in mode and is dirty?
			if(isSnapToMode) {
				// indexed geometry and SnapTo available?
				if (indexedGeometry != null && snapTo!=null) {
					// any selected?
					if(snapTo.size()>0) {
						// get current extent
						Envelope extent = (Envelope)map.getActiveView().getExtent();
						// update indexing
						indexedGeometry.update(extent, snapTo);
					}				
				}
			}
			// not dirty
			isDirty = false;
		}
	}
	
	private void fireSnappableChange(){
		for(int i=0;i<listeners.size();i++)
			listeners.get(i).onSnappableChange();
	}
	
	private void fireSnapToChange(){
		for(int i=0;i<listeners.size();i++)
			listeners.get(i).onSnapToChange();
	}

	/**
	 * Class implementing the IMapControlEvents2Adapter intefaces
	 * 
	 * Is used to catch events that is used to draw the tool 
	 * geometries on the map
	 * 
	 *
	 */
	class MapControlAdapter extends IMapControlEvents2Adapter {


		private static final long serialVersionUID = 1L;

		@Override
		public void onFullExtentUpdated(IMapControlEvents2OnFullExtentUpdatedEvent arg0) throws IOException, AutomationException {
			// set dirty
			isDirty = true;
			// update
			updateSnappable();
			updateIndexing();
		}
		
		@Override
		public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent arg0)
				throws IOException, AutomationException {
			// set dirty
			isDirty = true;
			// update
			updateSnappable();
			updateIndexing();

		}

		@Override
		public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent arg0)
				throws IOException, AutomationException {
			// set dirty
			isDirty = true;
			// update
			updateSnappable();
			updateIndexing();
		}
		
	}	
	
	public interface SnappingListener {
		public void onSnappableChange();
		public void onSnapToChange();
	}
	
}
