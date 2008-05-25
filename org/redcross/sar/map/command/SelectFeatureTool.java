package org.redcross.sar.map.command;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.Point;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class SelectFeatureTool extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	private static final int SNAP_TOL_FACTOR = 100;

	// geometries
	private Point p = null;
	private IEnvelope extent = null;
	
	// flags
	private boolean isSelectByPoint = true;
	
	// features
	private IFeature currentFeature = null;
	
	/**
	 * Constructs the DrawTool
	 */
	public SelectFeatureTool() throws IOException {
		
		// create point
		p = new Point();
		p.setX(0);
		p.setY(0);

		// set tool type
		type = DiskoToolType.SELECT_FEATURE_TOOL;		

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
		
		// add global keyevent listener
		Utils.getApp().getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// can process event?
				if(map!=null && map.isVisible() && isActive()) {
					// forward
					doSelectFeatureWork(null);
					// consume event
					e.consume();
				}
				
			}
		});		
		
	}

	public void onCreate(Object obj) {
		// is working?
		if(isWorking()) return;
		// is map valid?
		if (obj instanceof IDiskoMap) {
			// save map
			map = (DiskoMap)obj;
		}
		// forward
		super.onCreate(obj);
	}

	public void onMouseDown(int button, int shift, int x, int y){

		// is working?
		if(isWorking()) return;
		
		try {
		
			// get selection rectangle
			extent = map.trackRectangle();
			
			// transform to map coordinates
			p = toMapPoint(x, y);			
			
			// no selection by rectangle?
			isSelectByPoint = (extent==null || extent.isEmpty());
			
			// forward to draw adapter?
			if(!map.isEditSupportInstalled() || !map.getDrawAdapter().onMouseDown(button,shift,p)){
			
				// invoke later
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							// forward
							doSelectFeatureWork(selectFeature());
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				
			}
			else if(isSelectByPoint) {
				// forward onMouseUp() consumed by map.trackRectangle()
				onMouseUp(button, shift, x, y);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	@Override
	public void onMouseUp(int button, int shift, int x, int y) {
		
		// is working?
		if(isWorking()) return;
		
		try {
		
			// get position in map units
			p = toMapPoint(x,y);
			
			// forward to draw adapter?
			if(map.isEditSupportInstalled())
				map.getDrawAdapter().onMouseUp(button,shift,p);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private IMsoFeature selectFeature() throws Exception  {
		
		// initialize
		Object[] selected;
		IMsoFeature f = null;
		
		// get maximum deviation from point
		double max = map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR;
		
		// forward
		if(isSelectByPoint) 
			selected = MapUtil.selectMsoFeatureFromPoint(p, map, -1, max);
		else 
			selected = MapUtil.selectMsoFeatureFromEnvelope(extent, map, -1, max, 
					esriSpatialRelEnum.esriSpatialRelContains);
			
		// found?
		if(selected!=null) {
			// get feature and layer
			f = (IMsoFeature)selected[0];
			IMsoFeatureLayer l = (IMsoFeatureLayer)selected[1];
			// not allowed?
			if(!l.isEnabled()) {
				// notify with beep
				Toolkit.getDefaultToolkit().beep();
				// show disable warning
				Utils.showWarning(
						MsoUtils.getMsoObjectName(f.getMsoObject(), 1)
						+ " kan ikke velges");
				// failed
				f = null;
			}
		}
		// failed
		return f;
	}		
	

	/*
	private void selectFromPoint() throws Exception  {
		
		// initialize
		double min = -1;
		boolean doWork = true;
		IMsoFeature ff = null;
		IMsoFeatureLayer fl = null;
		
		// get maximum deviation from point
		double max = map.getActiveView().getExtent().getWidth()/SNAP_TOL_FACTOR;
		
		// get elements
		List layers = map.getMsoLayers();
				
		// search for feature
		for (int i = 0; i < layers.size(); i++) {
			IMsoFeatureLayer l = (IMsoFeatureLayer)layers.get(i);
			if(l.isSelectable() && l.isVisible()) {
				// get features in search extent
				MsoFeatureClass fc = (MsoFeatureClass)l.getFeatureClass();
				IFeatureCursor c = search(fc, p,max);
				IFeature f = c.nextFeature();
				// loop over all features in search extent
				while(f!=null) {
					// is mso feature?
					if (f instanceof IMsoFeature) {						
						// get first minimum distance
						double d = getMinimumDistance(f,p);
						// has valid distance?						
						if(d>-1) {
							int shapeType = f.getFeatureType();
							// save found feature?
							if((min==-1 || (d<min) || shapeType==esriGeometryType.esriGeometryPoint) && (d<max)) {
								// initialize
								min = d;
								ff = (IMsoFeature)f; fl = l;							
							}
						}
						else {
							// save found feature
							ff = (IMsoFeature)f; fl = l;
						}
					}
					// get next feature
					f = c.nextFeature();
				}
			}
		}
		// anything found?
		if(ff!=null && fl!=null) {
			// is layer not enabled?
			if(!fl.isEnabled()) {
				// notify with beep
				Toolkit.getDefaultToolkit().beep();
				// reset flag
				doWork = false;
			}
		}
		else {
			// clear selection
			ff = null;
		}
		
		// do work?
		if(doWork)
			doSelectFeatureWork(ff);
		
	}
	*/
	
	@Override
	public IDiskoToolState save() {
		// get new state
		return new SelectFeatureToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof SelectFeatureToolState) {
			((SelectFeatureToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	public class SelectFeatureToolState extends DiskoToolState {

		private Point p = null;
		private IEnvelope extent = null;
		private boolean isSelectByPoint = true;
		private IFeature currentFeature = null;
		
		// create state
		public SelectFeatureToolState(SelectFeatureTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(SelectFeatureTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p = tool.p;
			this.extent = tool.extent;
			this.isSelectByPoint = tool.isSelectByPoint;
			this.currentFeature = tool.currentFeature;
		}
		
		public void load(SelectFeatureTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p = this.p;
			tool.extent = this.extent;
			tool.isSelectByPoint = this.isSelectByPoint;
			tool.currentFeature = this.currentFeature;
		}
	}			
	
	private boolean doSelectFeatureWork(IMsoFeature msoFeature) {
		try {
			DiskoWorkPool.getInstance().schedule(
					new SelectFeatureWork(map,msoFeature));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	class SelectFeatureWork extends AbstractToolWork<Boolean> {

		DiskoMap map = null;
		IMsoFeature msoFeature = null;
		
		SelectFeatureWork(DiskoMap map,IMsoFeature msoFeature) 
									throws Exception {
			super(true);
			this.map = map;
			this.msoFeature = msoFeature;
		}
				
		@Override
		public Boolean doWork() {
		
			try {
				// forward to map
				if(msoFeature==null) {
					if(map.getSelectionCount(true)>0) {
						map.clearSelected();
					}
				}
				else {
					if(map.getSelectionCount(false)>0) {
						map.clearSelected();			
					}
					if(map.isSelected(msoFeature.getMsoObject())==0) {
						map.setSelected(msoFeature.getMsoObject(),true);
					}
				}
				// success
				return true;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			// failed
			return false;
		}
		
		@Override
		public void done() {

			// update selected feature
			currentFeature = msoFeature;

			// forward
			super.done();
		}		
	}		
}
