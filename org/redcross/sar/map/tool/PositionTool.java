package org.redcross.sar.map.tool;

import java.io.IOException;
import java.util.ArrayList;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.gui.panel.PositionPanel;
import org.redcross.sar.map.MapUtil;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IUnitIf;

import com.esri.arcgis.geometry.Point;
import com.esri.arcgis.interop.AutomationException;

/**
 * A custom draw tool.
 * @author geira
 *
 */
public class PositionTool extends AbstractDrawTool {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the DrawTool
	 */
	public PositionTool(IToolCollection dialog) throws IOException {

		// forward
		super(true,FeatureType.FEATURE_POINT);
		
		// prepare abstract class BasicTool
		cursorPath = "cursors/create.cur"; 
		caption = "Posisjon"; 
		category = "Commands"; 
		message = "Setter posisjon til valgt enhet"; 
		name = "CustomCommands_Position"; 
		toolTip = "Posisjon"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.POSITION_TOOL;		

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
		this.dialog = (DefaultDialog)dialog;
		
		// create default property panel
		propertyPanel = addPropertyPanel();
		
		// registrate me in dialog
		dialog.register(this);
		
	}

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
	
	@Override
	public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoCode) {
		try {
			
			// update tool point
			if(msoObject instanceof IUnitIf) {
				IUnitIf msoUnit = (IUnitIf)msoObject;
				setPoint(MapUtil.getEsriPoint(msoUnit.getPosition(), map.getSpatialReference()));
			}
			else {
				setPoint(null);
			}
			
			// forward
			super.setMsoData(msoOwner, msoObject, msoCode);
			
			// forward
			getPositionPanel().setMsoObject(msoObject);
			
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public PositionPanel getPositionPanel() {
		return (PositionPanel)propertyPanel;
	}
	
	@Override
	public IToolPanel addPropertyPanel() {
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
	public IDiskoToolState save() {
		// get new state
		return new PositionToolState(this);
	}
	
	@Override
	public boolean load(IDiskoToolState state) {
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
	
	public class PositionToolState extends DiskoToolState {

		private IUnitIf unit = null;
		private boolean isDirty = false;
		
		// create state
		public PositionToolState(PositionTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(PositionTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.unit = tool.getPositionPanel().getUnit();
			this.isDirty = tool.getPositionPanel().isDirty();
		}
		
		public void load(PositionTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.getPositionPanel().setChangeable(false);
			tool.getPositionPanel().setUnit(this.unit);
			tool.getPositionPanel().setChangeable(true);
			tool.getPositionPanel().setDirty(isDirty);
		}
	}

}
