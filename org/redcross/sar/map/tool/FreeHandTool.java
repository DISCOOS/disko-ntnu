/**
 *
 */
package org.redcross.sar.map.tool;

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import org.redcross.sar.gui.IMsoHolder;
import org.redcross.sar.gui.dialog.IDialog;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.mso.panel.FreeHandPanel;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.geometry.Polyline;
import com.esri.arcgis.interop.AutomationException;

/**
 * Free hand drawing tool
 *
 * @author kennetgu
 *
 */
public class FreeHandTool extends AbstractMsoDrawTool {

	private static final long serialVersionUID = 1L;

	// holds draw geometry
	private Point p1;
	private Point p2;

	// search type data
	private Enum<?> subType;

	/**
	 * Constructs the FreeHandTool
	 */
	public FreeHandTool(IMsoModelIf model, IDrawToolCollection dialog, boolean isPolygon) throws IOException {

		// forward
		super(model, true,(isPolygon ? FeatureType.FEATURE_POLYGON :
			FeatureType.FEATURE_POLYLINE));

		// initialize abstract class BasicTool
		caption = "Frihånd <small style=\"color:gray\">(" + DiskoEnumFactory.getText(featureType) + ")</small>";
		category = "Commands";
		message = "Tegner en frihåndsstrek";
		name = "CustomCommands_FreeHand";
		toolTip = "Frihånd";
		enabled = true;

		// set tool type
		type = MapToolType.FREEHAND_TOOL;

		// show draw frame when appropriate
		isShowDrawFrame = true;

		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseMoveAction = DrawAction.ACTION_CHANGE;
		onMouseUpAction = DrawAction.ACTION_FINISH;

		// create initial point
		p1 = new Point();
		p1.setX(0);
		p1.setY(0);

		// create default property panel
		toolPanel = addToolPanel();

		// save dialog?
		if(dialog instanceof IDialog) {
			setDialog((IDialog)dialog);
		}

	}

	@Override
	public void onCreate(Object obj) {

		// forward
		super.onCreate(obj);

		// has map?
		if(map!=null) {

			// get property panel
			FreeHandPanel panel = (FreeHandPanel)toolPanel;

			// release current?
			if(snapAdapter!=null) {
				// add listener
				snapAdapter.removeSnapListener(panel);
			}

			// get a snapping adapter from map
			snapAdapter = map.getSnapAdapter();

			// register freehand panel?
			if(snapAdapter!=null) {
				// add listener
				snapAdapter.addSnapListener(panel);
				// update panel
				panel.onSnapToChanged();
			}
		}

	}



	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("DRAWPOLYGON".equalsIgnoreCase(attribute)) {
			if((Boolean)value)
				featureType=FeatureType.FEATURE_POLYGON;
			else
				featureType=FeatureType.FEATURE_POLYLINE;
			// update caption
			caption = "Frihånd <small style=\"color:gray\">(" + DiskoEnumFactory.getText(featureType) + ")</small>";
			// finished
			return;
		}
		if("SUBTYPE".equalsIgnoreCase(attribute)) {
			subType = (SearchSubType)value;
			return;
		}
	}

	@Override
	public Object getAttribute(String attribute) {
		if("DRAWPOLYGON".equalsIgnoreCase(attribute)) {
			return (featureType==FeatureType.FEATURE_POLYGON);
		}
		if("SUBTYPE".equalsIgnoreCase(attribute)) {
			return subType;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		// forward
		super.setMsoData(msoOwner, msoObject, msoClassCode);
		// select mso object
		((IMsoHolder)getToolPanel()).setMsoObject((msoOwner!=null ? msoOwner : msoObject));
	}

	@Override
	public boolean doPrepare(IMsoObjectIf msoObj, boolean isDefined) {
		// forward
		if(super.doPrepare(msoObj,isDefined)) {
			// handle this?
			if(!isDefined) {
				// is owner search assignment?
				if(msoObj instanceof IAreaIf) {
					// get owning assignment
					IAssignmentIf assignment = ((IAreaIf)msoObj).getOwningAssignment();
					// is search assignment?
					if(assignment instanceof ISearchIf) {
						// cast to ISearchIf
						ISearchIf search = (ISearchIf)assignment;
						// update?
						if(subType!=null && !subType.equals(search.getSubType())) {
							search.setSubType((SearchSubType)subType);
						}
					}
				}
			}
			// forward
			return true;
		}
		// failed
		return false;
	}

	public boolean onBegin(int button, int shift, int x, int y) {

		try {

			// get next point. This point is already snapped
			// by the abstract class is snapping is on.
			p2 = p;

			// initialize path geometry?
			if (geoPath == null) {
				geoPath = new Polyline();
				geoPath.addPoint(p2, null, null);
			}
			// initialize rubber geometry?
			if (geoRubber == null) {
				geoRubber = new Polyline();
				geoRubber.addPoint(p2, null, null);
			}

			// update drawn geometry
			updateGeometry();

			// revert direction?
			if(isContinued && geoPath!=null
					&& !FeatureType.FEATURE_POLYGON.equals(featureType)) {
				// get distances
				double d1 = p.returnDistance(geoPath.getFromPoint());
				double d2 = p.returnDistance(geoPath.getToPoint());
				// get start area radius
				double r = getSnapTolerance();
				// not within init distance?
				if(d1>r && d2>r) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// notify later
							Utils.showMessage("Begrensing","Du må starte med et endepunkt");
						}
					});
					return false;
				}
				// reverse direction?
				if(d1<d2) {
					geoPath.reverseOrientation();
				}
				// continue
				p1 = (Point)geoPath.getToPoint();
				// forward
				setDirty(true);
			}
			else {
				// initialize
				p1 = p2;
			}
			// reset flag
			isContinued = false;

			// update rubber band
			geoRubber.updatePoint(0,p2);

			// success
			return true;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
	}

	public boolean onChange(int button, int shift, int x, int y) {

		// only update if left mouse button is pressed
		if(button==1) {

			try {

				// get next point. This point is already snapped
				// by the abstract class if snapping is on
				p2 = p;

				// update drawn geometry
				updateGeometry();

				// update last point
				p1 = p2;

				// success
				return true;

			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		// failed
		return false;
	}


	@Override
	public boolean onCancel() {
		try {
			// reset tool
			reset();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		// always return success
		return true;
	}

	@Override
	public IToolPanel addToolPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IToolPanel>(1);
		// create panel
		IToolPanel panel = new FreeHandPanel(this);
		// try to add
		if (panels.add(panel)) {
			return panel;
		}
		return null;
	}

	public void reset() {
		// forward
		super.reset();
		// reset point
		try {
			p1 = new Point();
			p1.setX(0);
			p1.setY(0);
			p2 = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateGeometry() throws IOException, AutomationException {

		// consume?
		if (p1.returnDistance(p2) == 0 || !isDrawing()) return;

		// add current mouse point
		geoPath.addPoint(p2, null, null);

		// forward
		setDirty(true);

	}

	@Override
	public IMapToolState save() {
		// get new state
		return new FreeHandToolState(this);
	}

	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof FreeHandToolState) {
			((FreeHandToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */

	public class FreeHandToolState extends AbstractMsoDrawTool.DrawToolState {

		// holds draw geometry
		private Point p1 = null;
		private Point p2 = null;

		// search type data
		private Enum<?> subType = null;

		// create state
		public FreeHandToolState(FreeHandTool tool) {
			super((AbstractMsoDrawTool)tool);
			save(tool);
		}

		public void save(FreeHandTool tool) {
			super.save((AbstractMsoDrawTool)tool);
			this.p1 = tool.p1;
			this.p2 = tool.p2;
			this.subType = tool.subType;
		}

		public void load(FreeHandTool tool) {
			tool.p1 = this.p1;
			tool.p2 = this.p2;
			tool.subType = this.subType;
			tool.getToolPanel().update();
			super.load((AbstractMsoDrawTool)tool);
		}
	}

}
