package org.redcross.sar.map.command;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.List;

import javax.swing.SwingUtilities;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.thread.DiskoWorkPool;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.esriGeometryType;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class SelectFeatureTool extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	private static final int SNAP_TOL_FACTOR = 100;

	private Point p = null;
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
		
			// transform to map coordinates
			p.setX(x);
			p.setY(y); 
			transform(p);
			
			// invoke later
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						// forward
						selectFromPoint();
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
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
			if(l.isSelectable()) {
				// get features in search extent
				MsoFeatureClass fc = (MsoFeatureClass)l.getFeatureClass();
				IFeatureCursor c = search(fc, p,max);
				IFeature f = c.nextFeature();
				// loop over all features in search extent
				while(f!=null) {
					// is mso feature?
					if (f instanceof IMsoFeature) {
						
						// selection allowed?
						//if(ff == null & (l.isEnabled() || l.isEditing())) {
						
						// only features on enabled or editing layers are selectable
						//if(l.isEnabled() || l.isEditing()) {
						
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
						//}
					}
					// get next feature
					f = c.nextFeature();
				}
			}
		}
		// found?
		if(ff!=null && fl!=null) {
			/*
			// is map in edit mode?
			boolean isEditing = map.isEditing();
			//valid feature?
			if(!ff.isEditing() && isEditing) {
				// notify with beep
				Toolkit.getDefaultToolkit().beep();
				// show in edit warning
				//Utils.showWarning("Du kan kun velge kartobjekter som er i endremodus");

				doWork = false;
				
			}
			else
			*/ 
			if(!fl.isEnabled()) {
				// notify with beep
				Toolkit.getDefaultToolkit().beep();
				// show disable warning
				//Utils.showWarning(MsoUtils.getMsoObjectName(ff.getMsoObject(), 1)
				//		+ " kan ikke velges fordi kartlaget ikket er valgbart");
				
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
		private IFeature currentFeature = null;
		
		// create state
		public SelectFeatureToolState(SelectFeatureTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(SelectFeatureTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p = tool.p;
			this.currentFeature = tool.currentFeature;
		}
		
		public void load(SelectFeatureTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p = this.p;
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
			super(false);
			this.map = map;
			this.msoFeature = msoFeature;
		}
				
		@Override
		public Boolean doWork() {
		
			try {
				boolean isDirty = false;
				// forward to map
				if(msoFeature==null) {
					if(map.getSelectionCount(true)>0) {
						map.clearSelected();
						isDirty = true;
					}
				}
				else {
					if(map.getSelectionCount(false)>0) {
						map.clearSelected();			
						isDirty = true;
					}
					if(map.isSelected(msoFeature.getMsoObject())==0) {
						map.setSelected(msoFeature.getMsoObject(),true);
						isDirty = true;
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
