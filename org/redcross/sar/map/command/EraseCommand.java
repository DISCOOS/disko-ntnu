package org.redcross.sar.map.command;

import java.awt.Toolkit;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.feature.MsoFeatureClass;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAreaListIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IOperationAreaListIf;
import org.redcross.sar.mso.data.IRouteIf;
import org.redcross.sar.mso.data.IRouteListIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchAreaListIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.IPOIListIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geometry.Point;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class EraseCommand extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	
	private static final int SNAP_TOL_FACTOR = 200;
	
	private Point p = null;
	
	public EraseCommand() {
		
		// prepare BaseTool
		cursorPath = "cursors/eraser.cur"; 
		caption = "Slett"; 
		category = "Commands"; 
		message = "Sletter valgt objekt"; 
		name = "CustomCommands_Erase"; 
		toolTip = "Slett"; 
		enabled = true; 
		
		// set tool type
		type = DiskoToolType.ERASE_COMMAND;

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
		
	}
	
	public void onCreate(Object obj) {
		// is working?
		if(isWorking()) return;
		// is valid map?
		if (obj instanceof IDiskoMap) {
			map = (DiskoMap)obj;
		}
		super.onCreate(obj);
	}

	@Override
	public void onMouseUp(int button, int shift, int x, int y) {

		// prevent reentry
		if(isWorking()) return;

		try {
			
			// get clicked point
			p = transform(x, y);			
			
			// run later (or else it will freeze on dialog box)
			SwingUtilities.invokeLater(new Runnable() {
				
				public void run() {
					
					try {
						
						// try to get feature
						IMsoFeature msoFeature = selectFromPoint();
						
						// found?
						if(msoFeature!=null) {
							// forward
							eraseFeature(msoFeature);
						}
						
						/*

						// get mso layers
						List layers = map.getMsoLayers();
						
						// loop over all layers and delete all selected features
						for (int i = 0; i < layers.size(); i++) {
							
							// get current layer
							IMsoFeatureLayer layer = (IMsoFeatureLayer)layers.get(i);
							
							// get selected items
							List selected = layer.getSelected();
							
							for (int j = 0; j < selected.size(); j++) {
								msoFeature = (IMsoFeature)selected.get(j);
								if (msoFeature.isSelected())  {
									// forward
									eraseFeature(msoFeature);
								}
							}
						}
						*/
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

	private boolean eraseFeature(IMsoFeature msoFeature) {
		// get mso class code
		IMsoManagerIf.MsoClassCode msoClassCode = msoFeature.getMsoObject().getMsoClassCode();
		// choose delete operations
		if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA) {
			// notfiy user
			int ans = JOptionPane.showConfirmDialog(
				Utils.getApp().getFrame(),
	            "Dette vil slette valgt operasjonsområde. Vil du fortsette?",
	            "Bekreft sletting",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute?
				return doEraseWork(1,msoFeature);
			}
		}
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) {
			// notfiy user
			int ans = JOptionPane.showConfirmDialog(
				Utils.getApp().getFrame(),
	            "Dette vil slette valgt søkeområde. Vil du fortsette?",
	            "Bekreft sletting",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(2,msoFeature);
			}
		}
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_AREA) {
			// notfiy user
			int ans = JOptionPane.showConfirmDialog(
				Utils.getApp().getFrame(),
	            "Dette vil slette valgt søketeig. Vil du fortsette?",
	            "Bekreft sletting",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(3,msoFeature);									}
		}					
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE) {
			// notfiy user
			int ans = JOptionPane.showConfirmDialog(
				Utils.getApp().getFrame(),
	            "Dette vil slette valg rute fra oppdrag. Vil du fortsette?",
	            "Bekreft slettingt",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(4,msoFeature);									}
		}					
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_POI) {
			// notfiy user
			int ans = JOptionPane.showConfirmDialog(
				Utils.getApp().getFrame(),
	            "Dette vil slette valgt veipunkt. Vil du fortsette?",
	            "Bekreft sletting",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE);
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(5,msoFeature);									}
		}					
		return false;
	}
	
	private IMsoFeature selectFromPoint() throws Exception  {
		
		// initialize
		double min = -1;
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
						// get first minimum distance
						double d = getMinimumDistance(f,p);
						// has valid distance?						
						if(d>-1) {
							// save found feature?
							if((min==-1 || (d<min)) && (d<max)) {
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
				Utils.showWarning("Du kan kun slette kartobjekter som er i endremodus");
				// clear selection
				ff = null;
				
			}
			else */
			
			if(!fl.isEnabled()) {
				// notify with beep
				Toolkit.getDefaultToolkit().beep();
				// show disable warning
				Utils.showWarning(MsoUtils.getMsoObjectName(ff.getMsoObject(), 1)
						+ " kan kan ikke slettes");
				
				// clear selection
				ff = null;

			}
		}
		else {
			// clear selection
			ff = null;
		}
		
		// return selected
		return ff;
		
	}	
	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	private boolean doEraseWork(int task,IMsoFeature msoFeature) {
		
		try {
			DiskoWorkPool.getInstance().schedule(new EraseWork(task,msoFeature));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		
	class EraseWork extends AbstractToolWork<Boolean> {

		private int m_task;
		private IMsoFeature m_msoFeature = null;
		
		EraseWork(int task,IMsoFeature msoFeature) throws Exception {
			// notify progress monitor
			super(true);
			m_task = task; 
			m_msoFeature = msoFeature;
		}
		
		@Override
		public Boolean doWork() {
			
			try {
				
				// get command post
				ICmdPostIf cmdPost = Utils.getApp().getMsoModel().getMsoManager().getCmdPost();
				
				// do task
				switch(m_task) {
				case 1: 
					// get list
					IOperationAreaListIf opAreaList = cmdPost.getOperationAreaList();
					// remove from list
					opAreaList.removeReference((IOperationAreaIf)m_msoFeature.getMsoObject());
					// finished
					return true;				
				case 2: 
					// get list
					ISearchAreaListIf searchAreaList = cmdPost.getSearchAreaList();
					// remove from list
					searchAreaList.removeReference((ISearchAreaIf)m_msoFeature.getMsoObject());
					// finished
					return true;				
				case 3:
					// get global list
					IAreaListIf areaList = cmdPost.getAreaList();
					// remove from list
					areaList.removeReference((IAreaIf)m_msoFeature.getMsoObject());
					// finished
					return true;				
				case 4:
					// get route
					IRouteIf route = (IRouteIf)m_msoFeature.getMsoObject();
					// get area
					IAreaIf area = MsoUtils.getOwningArea(route);
					// get global list
					IRouteListIf routeList = cmdPost.getRouteList();
					// remove from list
					routeList.removeReference(route);
		            // update start and stop poi
					MsoUtils.updateAreaPOIs(map,area);
					// finished
					return true;				
				case 5:
					// get global list
					IPOIListIf poiList = cmdPost.getPOIList();
					// remove from list
					poiList.removeReference((IPOIIf)m_msoFeature.getMsoObject());
					// finished
					return true;				
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
		@Override
		public void done() {		
			try {
				
				// get result
				boolean workDone = (Boolean)get();
				
				// notify disko work listeners?
				if(workDone) { 
					fireOnWorkCancel();
				}	
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
			// forward to super
			super.done();							
		}
	}
}
