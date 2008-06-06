/**
 * 
 */
package org.redcross.sar.map.tool;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.redcross.sar.gui.dialog.SnapDialog;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.index.IndexedGeometry;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IIdentify;
import com.esri.arcgis.carto.esriViewDrawPhase;
import com.esri.arcgis.controls.IMapControlEvents2Adapter;
import com.esri.arcgis.controls.IMapControlEvents2OnAfterDrawEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnFullExtentUpdatedEvent;
import com.esri.arcgis.controls.IMapControlEvents2OnMapReplacedEvent;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.geometry.esriSegmentExtension;
import com.esri.arcgis.interop.AutomationException;

/**
 * @author kennetgu
 *
 */
public class SnapAdapter {

	private static final long serialVersionUID = 1L;
	
	private static final int SNAP_TOL_FACTOR = 100;
	
	// flags
	protected boolean isDirty = false;
	protected boolean isSnapToMode = false;
	
	// holds snapping geometries
	private Envelope currentExtent = null;
	private Envelope searchEnvelope = null;
	
	// indexing
	private IndexedGeometry indexedGeometry = null;
	
	// map control listener
	private MapControlAdapter mapAdapter = null;
	
	// listeners
	private ArrayList<SnapListener> listeners = null;
	
	// components
	private DiskoMap map = null;
	private SnapDialog snapDialog = null;
	
	// snapping
	private IGeometry snapGeometry = null;
	private List<IFeatureLayer> snapTo = null;
	private List<IFeatureLayer> snappable = null;
	
	// control 
	public int workCount = 0;
	
	public SnapAdapter() {
		// prepare
		this.mapAdapter = new MapControlAdapter();
		this.listeners = new ArrayList<SnapListener>();
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
	
	public void addSnapListener(SnapListener listener) {
		listeners.add(listener);
	}
	
	public void removeSnapListener(SnapListener listener) {
		listeners.remove(listener);
	}
	
	public DiskoMap getMap() {
		return map;
	}
	
	public void register(DiskoMap map) throws IOException, AutomationException {
		
		// is not supporting this?
		if(!map.isEditSupportInstalled()) return;
		
		// remove old listener?
		if(this.map!=null) {
			this.map.removeIMapControlEvents2Listener(mapAdapter);
		}
		// register
		this.map = map;
		// add listener to get snapping change events
		map.addIMapControlEvents2Listener(mapAdapter);
		// set dirty
		isDirty = true;
		// update
		updateSnappable();
		// forward
		register(map.getSnapDialog());
	}
	
	private void register(SnapDialog dialog) {
		// unregister?
		if(this.snapDialog!=null) {
			this.snapDialog.getSnapPanel().setSnapAdapter(null);
		}
		// register?
		if(dialog!=null)
			dialog.getSnapPanel().setSnapAdapter(this);
		// save dialog
		this.snapDialog = dialog;
		
	}
	
	public SnapDialog getSnapDialog() {
		return snapDialog;
	}
	
	public void setSnapTolerance(double tolerance) throws IOException, AutomationException {
		searchEnvelope.setHeight(tolerance);
		searchEnvelope.setWidth(tolerance);
	}

	public double getSnapTolerance() throws IOException {
		return searchEnvelope.getHeight();
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
		// set flag
		this.isSnapToMode = isSnapToMode;
		// update indexing?
		if(isSnapToMode)
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
	
	public List<IFeatureLayer> getSnapableLayers() {
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
			for(int i = 0;i<this.snapTo.size();i++) {
				if(!snapTo.contains(this.snapTo.get(i))) {
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
			fireSnapToChanged();
		}
		
		return snapTo!=null && this.snapTo==null;
		
	}
	
	public IndexedGeometry getIndexing() {
		return indexedGeometry;
	}
	
	public IGeometry getSnapGeometry() {
		return snapGeometry;
	}
	
	public void update(boolean force) {
		
		// update flag
		isDirty = isDirty || snappable.size()==0 || force;
		
		// update?
		try {
			updateSnappable();
			updateIndexing();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public double getMaxSnapScale() {
		return map!=null ? map.getMaxSnapScale() : -1;
	}
	
	public boolean isSnappingAllowed() {
		try {
			return map!=null ? map.isSnapAllowed() : false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private void updateSnappable() throws IOException, AutomationException {
		// is snapping allowed?
		if(map.isSnapAllowed()) {
			// set snap tolerance of tool
			setSnapTolerance(map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR);
		}
		// get available snappable layers from map
		snappable = map.getSnappableLayers();	
		// notify
		fireSnapableChanged();
	}

	private void updateIndexing() throws IOException, AutomationException {
		// validate update
		if(isDirty && isSnapToMode && isSnappingAllowed() && !isWorking()) {
			// indexed geometry and SnapTo available?
			if (indexedGeometry != null && snapTo!=null) {
				// any selected?
				if(snapTo.size()>0) {
					// get current extent
					Envelope extent = (Envelope)map.getActiveView().getExtent();
					// forward
					scheduleSnapWork(extent);
				}				
			}
			// not dirty
			isDirty = false;
		}
	}
	
	private void fireSnapableChanged(){
		for(int i=0;i<listeners.size();i++)
			listeners.get(i).onSnapableChanged();
	}
	
	private void fireSnapToChanged(){
		for(int i=0;i<listeners.size();i++)
			listeners.get(i).onSnapToChanged();
	}

	private boolean isWorking() {
		return (workCount>0);
	}
	
	private int setIsWorking() {
		workCount++;
		return workCount; 
	}
	
	private int setIsNotWorking() {
		if(workCount>0) {
			workCount--;
		}
		return workCount; 
	}
	
	private void suspendUpdate() {
		if(map!=null) {
			try {
				map.suspendNotify();
				map.setSupressDrawing(true);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	private void resumeUpdate() {
		if(map!=null) {
			try {
				map.setSupressDrawing(false);
				map.refreshMsoLayers();
				map.resumeNotify();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	/*=============================================================
	 * Inner classes
	 *============================================================= 
	 */
	
	
	/**
	 * Class implementing the IMapControlEvents2Adapter intefaces
	 * 
	 * Is used to catch events that is used to draw the tool 
	 * geometries on the map
	 * 
	 */
	class MapControlAdapter extends IMapControlEvents2Adapter {

		private static final long serialVersionUID = 1L;

		@Override
		public void onAfterDraw(final IMapControlEvents2OnAfterDrawEvent e) throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// forward
					if (e.getViewDrawPhase() == esriViewDrawPhase.esriViewGraphics)
						update(false);
				}
			});			
		}

		@Override
		public void onFullExtentUpdated(IMapControlEvents2OnFullExtentUpdatedEvent e) throws IOException, AutomationException {			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// update
					update(true);
				}
			});			
		}
		
		@Override
		public void onExtentUpdated(IMapControlEvents2OnExtentUpdatedEvent e)
				throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// update?
					update(true);
				}
			});			
		}

		@Override
		public void onMapReplaced(IMapControlEvents2OnMapReplacedEvent arg0)
				throws IOException, AutomationException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// update
					update(true);
				}
			});			
		}
		
	}	
	
	private boolean scheduleSnapWork(Envelope extent) {
		// schedule on work pool thread
		try {
			// update indexes?
			if(currentExtent==null || !extent.within(currentExtent)) {
				// update current extent
				currentExtent = extent;
				// schedule
				DiskoWorkPool.getInstance().schedule(
						new SnapWork(extent,snapTo,true));
				// finished
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		// no work scheduled
		return false;
	}
	
	class SnapWork extends AbstractDiskoWork<Void> {
		
		Envelope extent = null;
		List<IFeatureLayer> snapTo = null;
		
		public SnapWork(Envelope extent, List<IFeatureLayer> snapTo, boolean notify) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Oppdaterer snapping buffer",100,notify,false);
			// prepare
			this.extent = extent;
			this.snapTo = snapTo;			
		}

		@Override
		public Void doWork() {
			// update indexing
			try {
				indexedGeometry.update(extent, snapTo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// finished
			return null;
		}

		@Override
		public void run() {
			// set flag to prevent reentry
			setIsWorking();
			// suspend for faster execution¨
			suspendUpdate();			
			// forward
			super.run();
			// is on event dispatch thread?
			if(SwingUtilities.isEventDispatchThread())
				done();
		}

		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread.
		 * 
		 */
		@Override
		public void done() {
			try {
				// resume update
		        resumeUpdate();
				// reset flag
		        setIsNotWorking();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	        // forward
	        super.done();
		}
	}	
	
	/*=============================================================
	 * Public interfaces
	 *============================================================= 
	 */
	public interface SnapListener {
		public void onSnapableChanged();
		public void onSnapToChanged();
	}
	
}
