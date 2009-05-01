package org.redcross.sar.map.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import org.redcross.sar.gui.dialog.IDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.gui.panel.PositionPanel;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.mso.TimePos;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

/**
 * A unit position tool.
 * @author kennetgu
 *
 */
public class PositionTool extends BaseMsoDrawTool {

	private static final long serialVersionUID = 1L;

	/**
	 * If true, geoPos will be logged to Track
	 * If track is <code>null</code>, a track will be created
	 */
	private boolean logPosition = false;

	/**
	 * If a TimePos is given, geoPos will update the
	 * TimePos position. Time stamp is unchanged.
	 * If track is <code>null</code>, a track will be created
	 */
	private TimePos updateTrackPosition = null;

	/**
	 * If a Calendar value is given, this will be used when
	 * logging a position, else, current time will be used
	 */
	private Calendar logTimeStamp = null;

	/**
	 * Constructs the DrawTool
	 */
	public PositionTool(IMsoModelIf model, IToolCollection dialog) throws IOException {

		// forward
		super(model, true,FeatureType.FEATURE_POINT);

		// prepare abstract class BasicTool
		cursorPath = "cursors/create.cur";
		caption = "Posisjon";
		category = "Commands";
		message = "Setter posisjon til valgt enhet";
		name = "CustomCommands_Position";
		toolTip = "Posisjon";
		enabled = true;

		// set tool type
		type = MapToolType.POSITION_TOOL;

		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseUpAction = DrawAction.ACTION_FINISH;

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

		// create current point
		p = new Point();
		p.setX(0);
		p.setY(0);

		// save dialog?
		if(dialog instanceof IDialog) {
			setDialog((IDialog)dialog);
		}

		// create default property panel
		toolPanel = addToolPanel();

	}

	@Override
	public boolean onFinish(int button, int shift, int x, int y) {

		try {

			// validate
			if(validate()) {

				// forward
				updateGeometry();

				// finished
				return true;
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
	}

	@Override
	public boolean finish() {
		// validate
		if(validate()) {
			// forward
			return super.finish();
		}
		// failure
		return false;
	}

	private boolean validate() {

		// initialize
		boolean bDoWork = true;

		// add new point?
		if(msoObject==null) {
			//
			Utils.showWarning("Enhet er ikke oppgitt");
			// do not add point
			bDoWork = false;
		}
		else {

			// dispatch type
			if (msoCode != IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT) {
				Utils.showWarning("Kun enhet kan endre posisjon");
				// do not add point
				bDoWork = false;
			}
		}

		// return state
		return bDoWork;

	}

	public IUnitIf getUnit() {
		if(msoObject instanceof IUnitIf)
			return (IUnitIf)msoObject;
		else
			return null;
	}

	public void setUnit(IUnitIf msoUnit) {
		// forward
		setMsoData(msoOwner,msoUnit,msoCode);
	}

	public Calendar getLogTimeStamp() {
		return (Calendar)getAttribute("LOGTIMESTAMP");
	}

	public void setLogTimeStamp(Calendar time) {
		setAttribute(time,"LOGTIMESTAMP");
	}

	@Override
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoCode) {

		// forward
		super.setMsoData(msoOwner, msoObject, msoCode);

		// forward
		getPositionPanel().setMsoObject(msoObject);

	}

	public PositionPanel getPositionPanel() {
		return (PositionPanel)toolPanel;
	}

	@Override
	public IToolPanel addToolPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IToolPanel>(1);
		// create panel
		IToolPanel panel = new PositionPanel(this);
		// try to add
		if (panels.add(panel)) {
			return panel;
		}
		return null;
	}

	private void updateGeometry() throws IOException, AutomationException {

		// has new line been found?
		if (p!=null && isDrawing()) {

			// update
			geoPoint = p;

			// forward
			setDirty(true);
		}
	}

	@Override
	public Object getAttribute(String attribute) {
		if("LOGTIMESTAMP".equalsIgnoreCase(attribute)) {
			return logTimeStamp;
		}
		if("LOGPOSITION".equalsIgnoreCase(attribute)) {
			return logPosition;
		}
		if("UPDATETRACKPOSITION".equalsIgnoreCase(attribute)) {
			return updateTrackPosition;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("LOGTIMESTAMP".equalsIgnoreCase(attribute)) {
			logTimeStamp = (Calendar)value;
			return;
		}
		if("LOGPOSITION".equalsIgnoreCase(attribute)) {
			logPosition = (Boolean)value;
			return;
		}
		if("UPDATETRACKPOSITION".equalsIgnoreCase(attribute)) {
			updateTrackPosition = (TimePos)value;
			return;
		}
	}

	@Override
	public IMapToolState save() {
		// get new state
		return new PositionToolState(this);
	}

	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof PositionToolState) {
			((PositionToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */

	public class PositionToolState extends BaseMsoDrawTool.DrawToolState {

		private IUnitIf unit = null;
		private boolean isDirty = false;
		private boolean logPosition = false;
		private Calendar logTimeStamp = null;
		private TimePos updateTrackPosition = null;

		// create state
		public PositionToolState(PositionTool tool) {
			super((BaseMsoDrawTool)tool);
			save(tool);
		}
		public void save(PositionTool tool) {
			super.save((BaseMapTool)tool);
			this.logPosition = tool.logPosition;
			this.logTimeStamp = tool.logTimeStamp;
			this.updateTrackPosition = tool.updateTrackPosition;
			this.unit = tool.getPositionPanel().getUnit();
			this.isDirty = tool.getPositionPanel().isDirty();
		}

		public void load(PositionTool tool) {
			tool.logPosition = this.logPosition;
			tool.logTimeStamp = this.logTimeStamp;
			tool.updateTrackPosition = this.updateTrackPosition;
			tool.getPositionPanel().setChangeable(false);
			tool.getPositionPanel().setUnit(this.unit);
			tool.getPositionPanel().setChangeable(true);
			tool.getPositionPanel().setDirty(isDirty);
			super.load((BaseMsoDrawTool)tool);
		}
	}

}
