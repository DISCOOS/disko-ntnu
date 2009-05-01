package org.redcross.sar.map.tool;

import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.SelectMsoObjectDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.WorkPool;

import com.esri.arcgis.geodatabase.esriSpatialRelEnum;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.arcgis.geometry.IPoint;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class EraseTool extends BaseMapTool {

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

	// dialogs
	private SelectMsoObjectDialog m_selectorDialog;

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
		type = MapToolType.ERASE_TOOL;

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

	}

	public void onCreate(Object obj) {

		// is working?
		if(isWorking()) return;

		// is valid map?
		if (obj instanceof IDiskoMap) {

			// update hook
			setMap((DiskoMap)obj);

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
		List<Object[]> selected;

		// get maximum deviation from point
		double max = map.isEditSupportInstalled() ? map.getSnapAdapter().getSnapTolerance() : map.getExtent().getWidth()/SNAP_TOL_FACTOR;

		// forward
		if(isSelectByPoint)
			// only select features within maximum length of point
			selected = MapUtil.selectMsoFeaturesFromPoint(p, map, -1, max);
		else
			// select all mso features within extent
			selected = MapUtil.selectMsoFeaturesFromEnvelope(
					extent, null, map, -1, max,
					esriSpatialRelEnum.esriSpatialRelWithin);

		// found?
		if(selected!=null) {
			// only one selected?
			if(selected.size()==1) {
				// get feature
				return (IMsoFeature)selected.get(0)[0];
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
		}

		// nothing to select
		return null;

	}

	private SelectMsoObjectDialog getSelectorDialog() {
		if(m_selectorDialog==null) {
			m_selectorDialog = new SelectMsoObjectDialog(Application.getInstance());
			m_selectorDialog.getListSelectorPanel().setCaptionText("Velg objekt");
		}
		return m_selectorDialog;
	}

	@Override
	public IMapToolState save() {
		// get new state
		return new EraseToolState(this);
	}

	@Override
	public boolean load(IMapToolState state) {
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

	public class EraseToolState extends MsoToolState {

		private IPoint p = null;
		private IEnvelope extent = null;
		private boolean isSelectByPoint = true;

		// create state
		public EraseToolState(EraseTool tool) {
			super((BaseMapTool)tool);
			save(tool);
		}
		public void save(EraseTool tool) {
			super.save((BaseMapTool)tool);
			this.p = tool.p;
			this.extent = tool.extent;
			this.isSelectByPoint = tool.isSelectByPoint;
		}

		public void load(EraseTool tool) {
			super.load((BaseMapTool)tool);
			tool.p = this.p;
			tool.extent = this.extent;
			tool.isSelectByPoint = this.isSelectByPoint;
		}
	}

	private boolean doEraseWork(IMsoFeature msoFeature) {

		try {
			WorkPool.getInstance().schedule(new EraseWork(msoFeature));
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
		public void afterDone() {

			// forward
			super.afterDone();

			try {

				// get result
				boolean workDone = (Boolean)get();

				// notify?
				if(workDone)
					fireOnWorkFinish(this,null);

			}
			catch(Exception e) {
				e.printStackTrace();
			}

		}
	}
}
