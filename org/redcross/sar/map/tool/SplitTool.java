package org.redcross.sar.map.tool;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geometry.*;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;

import java.awt.*;
import java.io.IOException;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class SplitTool extends AbstractMsoTool {

	private static final long serialVersionUID = 1L;
	private static final int SNAP_TOL_FACTOR = 200;
	private Point p = null;
	
	/**
	 * Constructs the DrawTool
	 */
	public SplitTool() throws IOException {
		
		// create point
		p = new Point();
		p.setX(0);
		p.setY(0);
		
		// set tool type
		type = MapToolType.SPLIT_TOOL;		
		
		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

	}

	public void onCreate(Object obj) {
		try {
			// is working?
			if(isWorking()) return;
			// is map valid?
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// forward
		super.onCreate(obj);
	}

	public void onMouseDown(int button, int shift, int x, int y) {
		
		// is working?
		if(isWorking()) return;

		try {
			// transform to map coordinates 
			p.setX(x);
			p.setY(y);
			transform(p);
	
			// schedule work in disko work pool
			doSplitWork();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	private Polyline[] split(Polyline orginal, Point nearPoint)
			throws IOException, AutomationException {
		Polyline[] result = new Polyline[2];
		boolean[] splitHappened = new boolean[2];
		int[] newPartIndex = new int[2];
		int[] newSegmentIndex = new int[2];
		orginal.splitAtPoint(nearPoint, true, true, splitHappened,
				newPartIndex, newSegmentIndex);

		// two new polylines
		result[0] = new Polyline();
		result[0].addGeometry(orginal.getGeometry(newPartIndex[0]), null, null);
		result[0].setSpatialReferenceByRef(map.getSpatialReference());
		result[1] = new Polyline();
		result[1].addGeometry(orginal.getGeometry(newPartIndex[1]), null, null);
		result[1].setSpatialReferenceByRef(map.getSpatialReference());

		Toolkit.getDefaultToolkit().beep();
		return result;
	}
	
	@Override
	public IMapToolState save() {
		// get new state
		return new SplitToolState(this);
	}
	
	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof SplitToolState) {
			((SplitToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */

	private boolean doSplitWork() {
		
		try {
			DiskoWorkPool.getInstance().schedule(new SplitWork(map,p));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class SplitWork extends AbstractToolWork<Boolean> {

		private Point p = null;
		private DiskoMap map = null;
		
		SplitWork(DiskoMap map,Point p) 
					throws Exception {
			super(true);
			this.map = map;
			this.p = p;
		}
		
		@Override
		public Boolean doWork() {

			try {
				// get maximum deviation from point
				double max = map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR;
				IMsoFeatureLayer editLayer = map.getMsoLayer(IMsoFeatureLayer.LayerCode.ROUTE_LAYER);
				MsoFeatureClass featureClass = (MsoFeatureClass)editLayer.getFeatureClass();
				IFeatureCursor c =  MapUtil.search(featureClass, p,max);
				IFeature feature = c.nextFeature();
				if (feature != null && feature instanceof IMsoFeature) {
					IMsoFeature editFeature = (IMsoFeature)feature;
					IGeometry geom = editFeature.getShape();
					if (featureClass.getShapeType() == esriGeometryType.esriGeometryBag) {
						GeometryBag geomBag = (GeometryBag)geom;
						int index = getGeomIndex(geomBag, p);
						if (index > -1) {
							IGeometry subGeom = geomBag.getGeometry(index);
							if (subGeom instanceof Polyline) {
								Polyline[] result = split((Polyline)subGeom, p);
								IMsoObjectIf msoObj = editFeature.getMsoObject();
								IAreaIf area = MsoUtils.getOwningArea(msoObj);
								if(area!=null) {
									if(MsoModelImpl.getInstance().getMsoManager().operationExists()) {
				                        ICmdPostIf cmdPost = MsoModelImpl.getInstance().getMsoManager().getCmdPost();
				                        IRouteIf route = cmdPost.getRouteList().createRoute(MapUtil.getMsoRoute(result[0]));
				                        area.setAreaGeodataItem(index,route);
				                        route = cmdPost.getRouteList().createRoute(MapUtil.getMsoRoute(result[1]));
				                        area.addAreaGeodata(route);
									}
								}
		                    }
						}
						// success
						return true;
					}
					else {
						
						//TODO: error handling
						
						// failed
						return false;
					}
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			// failed
			return false;
		}
	
		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread
		 * 
		 */
		public void afterDone() {
			
			// forward
			super.afterDone();
			
			try {
				
				// get result
				boolean workDone = (Boolean)get();
				
				// notify listeners?
				if(workDone)
					fireOnWorkFinish(this,msoObject);
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
	}
		
	public class SplitToolState extends MsoToolState {

		private Point p = null;
		
		// create state
		public SplitToolState(SplitTool tool) {
			super((AbstractMsoTool)tool);
			save(tool);
		}		
		public void save(SplitTool tool) {
			super.save((AbstractMsoTool)tool);
			this.p = tool.p;
		}
		
		public void load(SplitTool tool) {
			tool.p = this.p;
			super.load((AbstractMsoTool)tool);
		}
	}			
	
}
