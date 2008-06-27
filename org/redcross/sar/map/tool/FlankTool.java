package org.redcross.sar.map.tool;

import java.io.IOException;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geometry.GeometryBag;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.FlankPanel;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.FlankFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.mso.Route;

/**
 * A custom flank draw tool
 * 
 * @author geira
 *
 */
public class FlankTool extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	private static final int SNAP_TOL_FACTOR = 200;

	// current mouse position
	private Point p = null;

	// free hand properties panel 
	private FlankPanel flankPanel = null; 

	/**
	 * Constructs the FlankTool
	 */
	public FlankTool(IToolCollection dialog) throws IOException, AutomationException {

		// forward
		super();
		
		// set tool type
		type = DiskoToolType.FLANK_TOOL;
		
		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
		
		// save dialog
		this.dialog = (DefaultDialog)dialog;
		
		// create flank panel
		flankPanel = new FlankPanel(Utils.getApp(), this);
		
		// registrate me in dialog
		//dialog.register((IDrawTool)this, (JPanel)flankPanel);
		
		// create point
		p = new Point();
		p.setX(0);
		p.setY(0);
		
	}

	public void onCreate(Object obj) {
		
		// is working?
		if(isWorking()) return;

		try {
			// is valid map?
			if (obj instanceof IDiskoMap) {
				map = (DiskoMap)obj;
				flankPanel.onLoad(map);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		// forward
		super.onCreate(obj);
	}

	public void onMouseDown(int button, int shift, int x, int y) {
		
		// is working?
		if(isWorking()) return;

		try {
			
			// get point in map coordinates
			p.setX(x);
			p.setY(y);
			transform(p);
			
			// schedule work in disko work pool
			doFlankWork();
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public IDiskoToolState save() {
		// get new state
		return new FlankToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof FlankToolState) {
			((FlankToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
		
	public class FlankToolState extends DiskoToolState {
		private Point p = null;
		
		// create state
		public FlankToolState(FlankTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(FlankTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p = tool.p;
		}
		
		public void load(FlankTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p = this.p; 			
		}
	}
	
	private boolean doFlankWork() {
		
		try {
			DiskoWorkPool.getInstance().schedule(new FlankWork(map,p));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class FlankWork extends AbstractToolWork<Boolean> {

		private Point p = null;
		private DiskoMap map = null;
		
		FlankWork(DiskoMap map,Point p) 
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
				// apply flanks
				IMsoFeatureLayer editLayer = map.getMsoLayer(IMsoFeatureLayer.LayerCode.FLANK_LAYER);
				MsoFeatureClass featureClass = (MsoFeatureClass)editLayer.getFeatureClass();
				IFeatureCursor c =  MapUtil.search(featureClass, p,max);
				IFeature feature = c.nextFeature();
				if (feature != null && feature instanceof FlankFeature) {
					FlankFeature flankFeature = (FlankFeature)feature;
					GeometryBag geomBag = (GeometryBag)flankFeature.getShape();
					int index = getGeomIndex(geomBag, p);
					if (index > -1) {
						IMsoObjectIf msoObj = flankFeature.getMsoObject();
						IAreaIf area = MsoUtils.getOwningArea(msoObj);
						if(area!=null) {
							IMsoObjectIf msoObject = area.getGeodataAt(index);         // todo sjekk etter endring av GeoCollection
			                if (msoObject != null && msoObject instanceof IRouteIf)
			                {
			                    Route route = ((IRouteIf)msoObject).getGeodata();
			                    if (route != null) {
				    				route.setLayout(getLayout());
					    			flankFeature.msoGeometryChanged();		
							    }
			                }
						}
		            }
				}				
				// finished
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			// failed
			return false;
		}
		
		private String getLayout() throws AutomationException, IOException {
			String layout = "";
			if (flankPanel.getLeftCheckBox().isSelected()) {
				layout += "LeftDist="+flankPanel.getLeftSpinner().getValue()+"&";
			}
			if (flankPanel.getRightCheckBox().isSelected()) {
				layout += "RightDist="+flankPanel.getRightSpinner().getValue()+"&";
			}
			/*List clipLayers = flankPanel.getClipModel().getSelected();
			if (clipLayers.size() > 0) {
				layout += "ClipFeatures=";
				for (int i = 0; i < clipLayers.size(); i++) {
					IFeatureLayer flayer = (IFeatureLayer)clipLayers.get(i);
					layout += flayer.getFeatureClass().getAliasName()+",";
				}
			}*/
			return layout;
		}
		
		/**
		 * done 
		 * 
		 * Executed on the Event Dispatch Thread
		 * 
		 */
		@Override
		public void beforeDone() {		
			try {
				
				// get result
				boolean workDone = (Boolean)get();
				
				// notify disko work listeners?
				if(workDone)
					fireOnWorkFinish(this,msoObject);
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
		}
	}
	
}
