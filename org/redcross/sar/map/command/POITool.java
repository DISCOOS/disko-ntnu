package org.redcross.sar.map.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.IToolCollection;
import org.redcross.sar.gui.map.IPropertyPanel;
import org.redcross.sar.gui.map.POIPanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.IAssignmentIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class POITool extends AbstractDrawTool {

	private static final long serialVersionUID = 1L;

	// point of interest data
	private POIType[] poiTypes = null;
	
	// search type data
	private SearchSubType searchSubType = null;
	
	/**
	 * Constructs the DrawTool
	 */
	public POITool(IToolCollection dialog) throws IOException {

		// forward
		super(true,FeatureType.FEATURE_POINT);
		
		// prepare abstract class BasicTool
		caption = DiskoEnumFactory.getText(FeatureType.FEATURE_POINT); 
		category = "Commands"; 
		message = "Tegner en punkt"; 
		name = "CustomCommands_Point"; 
		toolTip = "Punkt"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.POI_TOOL;		

		// map draw operation
		onMouseDownAction = DrawAction.ACTION_BEGIN;
		onMouseUpAction = DrawAction.ACTION_FINISH;
		
		// create button
		button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);

		// create current point
		p = new Point();
		p.setX(0);
		p.setY(0);
		
		// save dialog
		this.dialog = (DiskoDialog)dialog;
				
		// create default property panel
		propertyPanel = addPropertyPanel();
		
		// registrate me in dialog
		dialog.register(this);
		
	}

	@Override
	public void onCreate(Object obj) {
		// forward
		super.onCreate(obj);
		
		try {

			// is map valid?
			if (map!=null) {
				
				// add layer listener
				IMsoFeatureLayer msoLayer =  map.getMsoLayer(IMsoFeatureLayer.LayerCode.POI_LAYER);
				Iterator<IPropertyPanel> it = panels.iterator();
				while(it.hasNext()) {
					msoLayer.addMsoLayerEventListener((POIPanel)it.next());
				}
				
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	public boolean onFinish(int button, int shift, int x, int y) {
		
		try {
			
			// validate
			if(validate(msoObject==null)) {
								
				//p.setSpatialReferenceByRef(map.getSpatialReference());
				
				// update panel
				getPOIPanel().updatePOIField(p);
				
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
	public boolean activate(boolean allow) {
		
		// forward
		boolean bflag = super.activate(allow);
		
		try {
			// update poi point
			Point p = map.getClickPoint();
			if(p.isEmpty())
				p = map.getMovePoint();
			getPOIPanel().updatePOIField(p);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// return flag
		return bflag;
		
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
				boolean isAreaPOI = (poiType == IPOIIf.POIType.START) || 
					(poiType == IPOIIf.POIType.VIA) || (poiType == IPOIIf.POIType.STOP);
				
				// dispatch type
				if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA) {
					// notify?
					if(isAreaPOI) {
						Utils.showWarning(DiskoEnumFactory.getText(poiType) 
								+ " kan bare legges til oppdrag");
						// do not add point
						bDoWork = false;
					}
				}
				else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA) {
					// notify?
					if(isAreaPOI) {
						// notify
						Utils.showWarning(DiskoEnumFactory.getText(poiType) 
								+ " punkter kan bare legges til oppdrag");
						// do not add point
						bDoWork = false;
					}
				}
				else if (msoClassCode == IMsoManagerIf.MsoClassCode.CLASSCODE_ROUTE) {
					// check for area?
					if(isAreaPOI) {
						// add new area?
						if (msoOwner != null) {
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
					}
				}
				else {
					// invalid poi?
					if(isAreaPOI) {
						// notify
						Utils.showWarning(DiskoEnumFactory.getText(poiType) 
								+ " punkter kan bare legges til søkeoppdrag");
						// do not add point
						bDoWork = false;
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

	public POIPanel getPOIPanel() {
		return (POIPanel)propertyPanel;
	}
	
	public IPOIIf getCurrentPOI() {
		if(msoObject instanceof IPOIIf)
			return (IPOIIf)msoObject;
		else
			return null;
	}
	
	public void setCurrentPOI(IPOIIf msoPOI) {
		msoObject = msoPOI;
		POIPanel panel = (POIPanel)getPropertyPanel();
		if(msoPOI!=null) {
			panel.getGotoPanel().getPositionField().setPosition(msoPOI.getPosition());
			panel.setPOIType(msoPOI.getType());
			panel.setRemarks(msoPOI.getRemarks());
		}
		else {
			panel.getGotoPanel().reset();
			panel.getTypesPanel().reset();			
			panel.setRemarks(null);
		}
	}
	
	@Override
	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		// forward
		super.setMsoDrawData(msoOwner, msoObject, msoClassCode);
		// get panel
		POIPanel panel = ((POIPanel)getPropertyPanel());
		// update comments and type
		if(msoObject instanceof IPOIIf) {
			// cast to IPOIIf
			IPOIIf poi = (IPOIIf)msoObject;
			// update panel
			panel.setPOIType(poi.getType());
			panel.setRemarks(poi.getRemarks());
		}
		else {
			// update panel
			panel.setPOIType(null);
			panel.setRemarks(null);			
		}
		// forward
		panel.setMsoObject((msoOwner!=null ? msoOwner : msoObject));
	}

	public void addPOIAt(Point p,POIType type,String remarks) {
		// update dialog
		getPOIPanel().setPOIType(type);
		getPOIPanel().setRemarks(remarks);
		// validate
		if(validate(true)) {
			try {
				// reset mso object (this forces the creation of an new POI)
				msoObject = null;
				// update point
				this.p = (Point)p.esri_clone();
				// forward
				doFinishWork();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void movePOIAt(Point p,POIType type,String remarks) {
		// update dialog
		getPOIPanel().setPOIType(type);
		getPOIPanel().setRemarks(remarks);
		// validate
		if(validate(false)) {
			try {
				// update point
				this.p = (Point)p.esri_clone();
				// forward
				doFinishWork();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public Object getAttribute(String attribute) {
		if("POITYPES".equalsIgnoreCase(attribute)) {
			return poiTypes;
		}
		if("SEARCHSUBTYPE".equalsIgnoreCase(attribute)) {
			return searchSubType;
		}
		return super.getAttribute(attribute);
	}

	@Override
	public void setAttribute(Object value, String attribute) {
		super.setAttribute(value, attribute);
		if("POITYPES".equalsIgnoreCase(attribute)) {
			poiTypes = (POIType[])value;
			Iterator<IPropertyPanel> it = panels.iterator();
			while(it.hasNext()) {
				// forward
				((POIPanel)it.next()).setTypes(poiTypes);
			}
			return;
		}
		if("SEARCHSUBTYPE".equalsIgnoreCase(attribute)) {
			searchSubType = (SearchSubType)value;
			return;
		}
	}
	
	@Override
	public boolean doPrepare() {
		// get panel
		POIPanel panel = ((POIPanel)getPropertyPanel());
		// is poi?
		if(msoObject instanceof IPOIIf) {
			// cast to IPOIIf
			IPOIIf poi = (IPOIIf)msoObject; 
			// update dialog
			poi.setType(panel.getPOIType());
			poi.setRemarks(panel.getRemarks());			
			// is owner search assignment?
			if(msoOwner instanceof IAreaIf) {
				// get owning assignment
				IAssignmentIf assignment = ((IAreaIf)msoOwner).getOwningAssignment();
				// is search assignment?
				if(assignment instanceof ISearchIf) {
					// cast to ISearchIf
					ISearchIf search = (ISearchIf)assignment; 
					// update?
					if(searchSubType!=null && !searchSubType.equals(search.getSubType()))
						search.setSubType(searchSubType);
				}
			}
		}
		// forward
		return super.doPrepare();
	}
	
	@Override
	public IPropertyPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<IPropertyPanel>(1);			
		// create panel
		IPropertyPanel panel = new POIPanel(this);
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
			
			// mark change
			setDirty();
			
		}		
	}	
	
	@Override
	public IDiskoToolState save() {
		// get new state
		return new POIToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
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
	
	public class POIToolState extends DiskoToolState {
		
		private POIType type = null;
		private POIType[] types = null;
		private String remarks = null;
		private SearchSubType searchSubType = null;
		
		// create state
		public POIToolState(POITool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		
		public void save(POITool tool) {
			super.save((AbstractDiskoTool)tool);
			types = poiTypes;
			type = tool.getPOIPanel().getPOIType();
			remarks = tool.getPOIPanel().getRemarks();
			searchSubType = tool.searchSubType;
		}
		
		public void load(POITool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.setAttribute(types,"POITYPES");
			tool.getPOIPanel().setPOIType(type);
			tool.getPOIPanel().setRemarks(remarks);
			tool.getPOIPanel().updatePOIField(p);
			tool.searchSubType = searchSubType;
		}
	}
}
