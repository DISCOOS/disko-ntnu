package org.redcross.sar.map.tool;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.SelectMsoObjectDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.data.IMsoObjectIf;
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
public class SelectTool extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	private static final int SNAP_TOL_FACTOR = 100;

	// geometries
	private Point p;
	private Point2D screen;
	private IEnvelope extent;
	private SelectMsoObjectDialog m_selectorDialog;
	
	
	// flags
	private boolean isSelectByPoint = true;
	
	// features
	private IFeature currentFeature = null;
	
	/**
	 * Constructs the DrawTool
	 */
	public SelectTool() throws IOException {
		
		// create points
		p = new Point();
		p.setX(0);
		p.setY(0);
		screen = new Point2D.Double();
		screen.setLocation(0, 0);

		// set tool type
		type = DiskoToolType.SELECT_TOOL;		

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
			
			// save screen coordinates
			screen.setLocation(x, y);
			
			// transform to map coordinates
			p = toMapPoint(x, y);			
			
			// get tolerance
			double min = map.isEditSupportInstalled() ? map.getSnapAdapter().getSnapTolerance() : map.getExtent().getWidth()/SNAP_TOL_FACTOR;
			
			// no selection by rectangle?
			isSelectByPoint = (extent==null || extent.isEmpty() || extent.getWidth()<min);
			
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
				// forward onMouseUp() that was consumed by map.trackRectangle()
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
		List<Object[]> selected = new ArrayList<Object[]>();
		
		// get maximum deviation from point
		double max = map.getExtent().getWidth()/SNAP_TOL_FACTOR;
		
		// forward
		if(isSelectByPoint)
			// only select features within maximum length of point
			selected = MapUtil.selectMsoFeaturesFromPoint(p, map, -1, max);
		else {
			// select all mso features that is contained by extent
			selected.addAll(MapUtil.selectMsoFeaturesFromEnvelope(
					extent, null, map, -1, max, 
					esriSpatialRelEnum.esriSpatialRelContains));					
			// select all mso features that overlaps with extent
			selected.addAll(MapUtil.selectMsoFeaturesFromEnvelope(
					extent, null, map, -1, max, 
					esriSpatialRelEnum.esriSpatialRelOverlaps));
			// select all mso features that overlaps with extent
			selected.addAll(MapUtil.selectMsoFeaturesFromEnvelope(
					extent, null, map, -1, max, 
					esriSpatialRelEnum.esriSpatialRelCrosses));
		}
			
		// only one selected?
		if(selected.size()==1) {
			// get feature
			IMsoFeature f = (IMsoFeature)selected.get(0)[0];
			// finished
			return f;
		}
		else if(selected.size()>1){
			// user decision is required
			IMsoObjectIf objs[] = new IMsoObjectIf[selected.size()];
			// get mso objects
			for(int i=0; i<selected.size(); i++) {
				// get feature
				IMsoFeature f = (IMsoFeature)selected.get(i)[0];
				// get mso object
				objs[i]=f.getMsoObject();
			}
			// load into selection dialog
			getSelectorDialog().load(objs);
			// locate dialog
			getSelectorDialog().setLocation((int)screen.getX()+map.getLocationOnScreen().x, (int)screen.getY()+map.getLocationOnScreen().y);
			// prompt user
			IMsoObjectIf ans = getSelectorDialog().select();
			// get feature layer
			for(int i=0; i<selected.size(); i++) {
				// get feature
				IMsoFeature f = (IMsoFeature)selected.get(i)[0];
				// is mso object?
				if(ans==f.getMsoObject()) {
					return f;
				}				
			}
		}
		// nothing to select
		return null;
	}		
	
	private SelectMsoObjectDialog getSelectorDialog() {
		if(m_selectorDialog==null) {
			m_selectorDialog = new SelectMsoObjectDialog(Utils.getApp().getFrame());
			m_selectorDialog.getListSelectorPanel().setCaptionText("Velg objekt");
		}
		return m_selectorDialog;
	}
	
	
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
		public SelectFeatureToolState(SelectTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(SelectTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p = tool.p;
			this.extent = tool.extent;
			this.isSelectByPoint = tool.isSelectByPoint;
			this.currentFeature = tool.currentFeature;
		}
		
		public void load(SelectTool tool) {
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
		public void beforeDone() {

			// update selected feature
			currentFeature = msoFeature;

		}		
	}		
}
