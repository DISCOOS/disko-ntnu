package org.redcross.sar.map.command;

import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Calendar;
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
import org.redcross.sar.mso.data.IMsoObjectIf;
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
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.AutomationException;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class EraseTool extends AbstractDiskoTool {

	private static final long serialVersionUID = 1L;
	
	private static final int SNAP_TOL_FACTOR = 100;
	
	// geometries
	private Point p = null;
	
	// flags
	private boolean isMouseOverIcon = false;

	// counters
	protected long previous = 0;
	
	public EraseTool() {
		
		// prepare BaseTool
		cursorPath = "cursors/erase.cur"; 
		caption = "Slett"; 
		category = "Commands"; 
		message = "Sletter valgt objekt"; 
		name = "CustomCommands_Erase"; 
		toolTip = "Slett"; 
		enabled = true; 
		
		// set tool type
		type = DiskoToolType.ERASE_TOOL;

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
	
		// add global keyevent listener
		Utils.getApp().getKeyEventDispatcher().addKeyListener(
				KeyEvent.KEY_PRESSED, KeyEvent.VK_DELETE, new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// can process event?
				if(map!=null && map.isVisible()) {
					// get selected elements
					List<IMsoFeature> list = null;
					try {
						list = map.getMsoSelection();
					} catch (AutomationException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// any selections?
					if(list.size()>0)  {
						// get feature
						final IMsoFeature msoFeature = list.get(0);
						// run later (or else it will freeze on the message box)
						SwingUtilities.invokeLater(new Runnable() {							
							public void run() {
								
								try {
									
									// try to get feature
									eraseFeature(msoFeature);
									
								}
								catch(Exception e) {
									e.printStackTrace();
								}
							}
						});
						// consume event
						e.consume();
					}
				}
				
			}
			// has 
		});
		
		
		
	}
	
	public void onCreate(Object obj) {
		// is working?
		if(isWorking()) return;
		// is valid map?
		if (obj instanceof IDiskoMap) {
			
			// unregister?
			if(map!=null) {
				removeDiskoWorkEventListener(map);
			}
			
			// update hook
			map = (DiskoMap)obj;
			
			// add map as work listener
			addDiskoWorkEventListener(map);
					
		}
		// forward
		super.onCreate(obj);
	}
	
	@Override
	public void onMouseMove(int button, int shift, int x, int y) {

		// get tic
		long tic = Calendar.getInstance().getTimeInMillis();
		
		// consume?
		if(tic-previous<250) return;
		
		// update tic
		previous = tic;
		
		try {	
			// transform to map coordinates
			p = toMapPoint(x, y);			
			// get flag
			isMouseOverIcon = (map.isEditSupportInstalled()) ? map.getDrawFrame().hitIcon(p.getX(), p.getY(), 1)!=null : false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}	
	
	@Override
	public void onMouseUp(int button, int shift, int x, int y) {

		// prevent reentry
		if(isWorking()) return;

		try {
			
			// transform to map coordinates
			p = toMapPoint(x, y);			
			
			// forward to draw adapter?
			if(!map.isEditSupportInstalled() ||  !map.getDrawAdapter().onMouseUp(button,shift,p)) {			
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
							
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int getCursor() {
		// show default?
		if(isMouseOverIcon)
			return 0;
		else {
			return super.getCursorFromLocation("cursors/erase.cur");
		}	
	}

	private boolean eraseFeature(IMsoFeature msoFeature) {
		// initialize
		int index = 0;
		// get mso object
		IMsoObjectIf msoObj = msoFeature.getMsoObject();
		// get default value
		String message = "Objektet <" + MsoUtils.getMsoObjectName(msoObj, 1) + "> kan ikke slettes";		
		// get mso class code
		IMsoManagerIf.MsoClassCode msoClassCode = msoObj.getMsoClassCode();
		// choose delete operations
		if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA) {
			// set work index
			index = 1;
			// get message
			message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
		}
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) {
			// set work index
			index = 2;
			// get message
			message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
		}
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_AREA) {
			// set work index
			index = 3;
			// get message
			message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
		}					
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE) {
			// set work index
			index = 3;
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea(msoObj);
			// get message
			message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + " fra " 
	            + MsoUtils.getMsoObjectName(area, 1) + ". Vil du fortsette?";
		}					
		else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_POI) {
			// set work index
			index = 4;
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea(msoObj);
			// has area?
			if(area!=null)
				// get message
				message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + " fra " 
		            + MsoUtils.getMsoObjectName(area, 1) + ". Vil du fortsette?";
			else
				// get message
				message = "Dette vil slette " + MsoUtils.getMsoObjectName(msoObj, 1) + ". Vil du fortsette?";
			
		}					
		// notfiy user
		if(index>0) {
			int ans = JOptionPane.showConfirmDialog( Utils.getApp().getFrame(),
	            message, "Bekreft sletting", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			// delete?
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(index,msoFeature);									
			}
		}
		else {			
			JOptionPane.showMessageDialog(Utils.getApp().getFrame(),
		            message, "Begrensning", JOptionPane.QUESTION_MESSAGE);
		}
		// do not delete
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
					fireOnWorkChange();
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
