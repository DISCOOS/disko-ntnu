package org.redcross.sar.map.tool;

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
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.DiskoWorkPool;

import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;
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
	private IPoint p = null;
	private IEnvelope extent = null;
	
	// flags
	private boolean isSelectByPoint = true;
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
					try {
						// get selected elements
						List<IMsoFeature> list = map.getMsoSelection();
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
					} catch (AutomationException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
				
			}
		});
		
		
		
	}
	
	public void onCreate(Object obj) {
		
		// is working?
		if(isWorking()) return;
		
		// is valid map?
		if (obj instanceof IDiskoMap) {
			
			// update hook
			map = (DiskoMap)obj;
			
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
	public void onMouseDown(int button, int shift, int x, int y) {

		// prevent reentry
		if(isWorking()) return;

		try {

			// get selection rectangle
			extent = map.trackRectangle();
			
			// transform to map coordinates
			p = toMapPoint(x, y);			
			
			// no selection by rectangle?
			isSelectByPoint = (extent==null || extent.isEmpty());
			
			// forward to draw adapter?
			if(!map.isEditSupportInstalled() ||  !map.getDrawAdapter().onMouseDown(button,shift,p)) {			
				
				// run later (or else it will freeze on dialog box)
				SwingUtilities.invokeLater(new Runnable() {
					
					public void run() {
						
						try {
							
							// try to get feature
							IMsoFeature msoFeature = selectFeature();
							
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
		// get mso object
		IMsoObjectIf msoObj = msoFeature.getMsoObject();
		// get default value
		String message = MsoUtils.getDeleteMessage(msoObj);		
		// allowed to delete this?
		if(MsoUtils.isDeleteable(msoObj)) {
			// forward
			int ans =  Utils.showConfirm("Bekreft sletting",message,JOptionPane.YES_NO_OPTION);
			// delete?
			if(ans == JOptionPane.YES_OPTION) { 
				// create erase worker task and execute
				return doEraseWork(msoFeature);									
			}
		}
		else {			
			Utils.showWarning("Begrensning", message);
		}
		// did not delete
		return false;

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
						+ " kan ikke slettes");
				// failed
				f = null;
			}
		}
		// failed
		return f;
	}	

	@Override
	public IDiskoToolState save() {
		// get new state
		return new EraseToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
		// valid state?
		if(state instanceof EraseToolState) {
			((EraseToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */
	
	public class EraseToolState extends DiskoToolState {

		private IPoint p = null;
		private IEnvelope extent = null;
		private boolean isSelectByPoint = true;
		
		// create state
		public EraseToolState(EraseTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(EraseTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.p = tool.p;
			this.extent = tool.extent;
			this.isSelectByPoint = tool.isSelectByPoint;
		}
		
		public void load(EraseTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.p = this.p;
			tool.extent = this.extent;
			tool.isSelectByPoint = this.isSelectByPoint;
		}
	}			

	private boolean doEraseWork(IMsoFeature msoFeature) {
		
		try {
			DiskoWorkPool.getInstance().schedule(new EraseWork(msoFeature));
			return true;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
		
	class EraseWork extends AbstractToolWork<Boolean> {

		//private int m_task;
		private IMsoFeature m_msoFeature = null;
		
		EraseWork(IMsoFeature msoFeature) throws Exception {
			// notify progress monitor
			super(true);
			//m_task = task; 
			m_msoFeature = msoFeature;
		}
		
		@Override
		public Boolean doWork() {
			
			try {
				
				// get mso object
				IMsoObjectIf msoObj = m_msoFeature.getMsoObject();
				
				// get options
				int options = (msoObj instanceof IAreaIf ? 1 : 0);
				
				// try to delete
				if(MsoUtils.delete(msoObj, options))
					return true;
				else 
					Utils.showError("Sletting kunne ikke utføres");					

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
				if(workDone)
					fireOnWorkFinish(this,null);
				
			}
			catch(Exception e) {
				e.printStackTrace();
			}			
			// forward to super
			super.done();							
		}
	}
}
