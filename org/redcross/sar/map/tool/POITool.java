package org.redcross.sar.map.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

import org.redcross.sar.gui.dialog.IDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.gui.panel.POIPanel;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.util.Utils;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class POITool extends BaseMsoDrawTool {

	private static final long serialVersionUID = 1L;

	// search type data
	private Enum<?> subType;

	/**
	 * Constructs the DrawTool
	 */
	public POITool(IMsoModelIf model, IToolCollection dialog) throws IOException {

		// forward
		super(model,true,FeatureType.FEATURE_POINT);

		// prepare abstract class BasicTool
		caption = DiskoEnumFactory.getText(FeatureType.FEATURE_POINT);
		category = "Commands";
		message = "Tegner en punkt";
		name = "CustomCommands_Point";
		toolTip = "Punkt";
		enabled = true;

		// set tool type
		type = MapToolType.POI_TOOL;

		// show draw frame when appropriate
		isShowDrawFrame = true;

		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseUpAction = DrawAction.ACTION_FINISH;

		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

		// create current point
		p = new Point();
		p.setX(0);
		p.setY(0);

		// create default property panel
		toolPanel = addToolPanel();

		// save dialog?
		if(dialog instanceof IDialog) {
			setDialog((IDialog)dialog);
		}

	}

	public boolean onFinish(int button, int shift, int x, int y) {

		try {

			// validate
			if(validate(msoObject==null)) {

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
		if(validate(msoObject==null)) {
			// forward
			return super.finish();
		}
		// failure
		return false;
	}

	private boolean validate(boolean bAddPOI) {

		// initialize
		boolean bDoWork = true;

		// add new point?
		if(msoObject==null) {

			if(!bAddPOI) {
				Utils.showWarning("Punktet eksisterer ikke");
				// do not add point
				bDoWork = false;
			}
			else {

				// get poi type
				POIType poiType = getPOIPanel().getPOIType();

				// get flag
				boolean isAreaPOI = IPOIIf.AREA_SET.contains(poiType);

				// dispatch type
				if (msoCode == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA) {
					// notify?
					if(isAreaPOI) {
						Utils.showWarning(DiskoEnumFactory.getText(poiType)
								+ " kan bare legges til oppdrag");
						// do not add point
						bDoWork = false;
					}
				}
				else if (msoCode == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) {
					// notify?
					if(isAreaPOI) {
						// notify
						Utils.showWarning(DiskoEnumFactory.getText(poiType)
								+ " punkter kan bare legges til oppdrag");
						// do not add point
						bDoWork = false;
					}
				}
				else {
					// check for area?
					if(isAreaPOI) {
						// add new area?
						if (msoOwner instanceof IAreaIf) {
							// check for START and STOP duplicates?
							if(poiType != IPOIIf.POIType.VIA) {
								// get poi list
								ArrayList<IPOIIf> areaPOIs = new ArrayList<IPOIIf>(((IAreaIf)msoOwner).getAreaPOIs().getItems());
								// search for duplicate
								for(int i=0;i<areaPOIs.size();i++) {
									// exists?
									if (areaPOIs.get(i).getType() == poiType) {
										// notify
										Utils.showWarning("Det finnes allerede et "
												+ DiskoEnumFactory.getText(poiType)
												+ " punkt");
										// do not add pui
										bDoWork = false;
									}
								}
							}
						}
						else if(msoOwner!=null) {
							// notify
							Utils.showWarning(DiskoEnumFactory.getText(poiType)
									+ " punkter kan bare legges til søkeoppdrag");
							// do not add point
							bDoWork = false;
						}
					}
				}
			}
		}
		else {
			if(bAddPOI) {
				Utils.showWarning("Punktet kan ikke legges til");
				// do not add point
				bDoWork = false;
			}
		}

		// return state
		return bDoWork;

	}

	/* ==================================================
	 * Public methods (override with care)
	 * ==================================================
	 */

	public POIPanel getPOIPanel() {
		return (POIPanel)toolPanel;
	}

	public IPOIIf getPOI() {
		if(msoObject instanceof IPOIIf)
			return (IPOIIf)msoObject;
		else
			return null;
	}

	public void setPOI(IPOIIf msoPOI) {
		// forward
		setMsoData(msoOwner,msoPOI,msoCode);
	}

	@Override
	public void setMsoData(IMsoObjectIf msoOwn, IMsoObjectIf msoObj, MsoClassCode msoCode) {
		try {

			// update tool point
			if(msoObj instanceof IPOIIf) {
				IPOIIf msoPOI = (IPOIIf)msoObj;
				setPoint(MapUtil.getEsriPoint(msoPOI.getPosition().getGeoPos(), map.getSpatialReference()));
			}

			// forward
			super.setMsoData(msoOwn, msoObj, msoCode);

			// forward
			getPOIPanel().setMsoObject(msoObj);

		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Object getAttribute(String attribute) {
		if("POITYPES".equalsIgnoreCase(attribute)) {
			return getPOIPanel().getPOITypes();
		}
		if("SUBTYPE".equalsIgnoreCase(attribute)) {
			return subType;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("POITYPES".equalsIgnoreCase(attribute)) {
			POIType[] types = (POIType[])value;
			Iterator<IToolPanel> it = panels.iterator();
			while(it.hasNext()) {
				// forward
				((POIPanel)it.next()).setPOITypes(types);
			}
			return;
		}
		if("SUBTYPE".equalsIgnoreCase(attribute)) {
			subType = (Enum<?>)value;
			return;
		}
	}

	@Override
	public boolean doPrepare(IMsoObjectIf msoObj, boolean isDefined) {
		// forward
		if(super.doPrepare(msoObj,isDefined)) {
			// handle this?
			if(!isDefined) {
				// get panel
				POIPanel panel = ((POIPanel)getToolPanel());
				// is poi?
				if(msoObj instanceof IPOIIf) {
					// cast to IPOIIf
					IPOIIf poi = (IPOIIf)msoObj;
					// update dialog
					poi.setType(panel.getPOIType());
					poi.setRemarks(panel.getRemarks());
					poi.setName(panel.getPOIName());
				}
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
			// finished
			return true;
		}
		// failed
		return false;
	}

	@Override
	public IToolPanel addToolPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IToolPanel>(1);
		// create panel
		IToolPanel panel = new POIPanel(this);
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
	public IMapToolState save() {
		// get new state
		return new POIToolState(this);
	}

	@Override
	public boolean load(IMapToolState state) {
		// valid state?
		if(state instanceof POIToolState) {
			((POIToolState)state).load(this);
			return true;
		}
		return false;
	}

	/* ==================================================
	 * Inner classes
	 * ==================================================
	 */

	public class POIToolState extends BaseMsoDrawTool.DrawToolState {

		private POIType type;
		private POIType[] types;
		private String name;
		private String remarks;
		private boolean isDirty;
		private Enum<?> subType;

		// create state
		public POIToolState(POITool tool) {
			super((BaseMsoDrawTool)tool);
			save(tool);
		}

		public void save(POITool tool) {
			super.save((BaseMsoDrawTool)tool);
			subType = tool.subType;
			types = tool.getPOIPanel().getPOITypes();
			type = tool.getPOIPanel().getPOIType();
			name = tool.getPOIPanel().getPOIName();
			remarks = tool.getPOIPanel().getRemarks();
			isDirty = tool.getPOIPanel().isDirty();
		}

		public void load(POITool tool) {
			tool.getPOIPanel().setChangeable(false);
			tool.subType = subType;
			tool.getPOIPanel().setPOIType(type);
			tool.getPOIPanel().setPOITypes(types);
			tool.getPOIPanel().setRemarks(remarks);
			tool.getPOIPanel().setPOIName(name);
			tool.getPOIPanel().setChangeable(true);
			tool.getPOIPanel().setDirty(isDirty);
			super.load((BaseMsoDrawTool)tool);
		}
	}
}
