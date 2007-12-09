package org.redcross.sar.map.command;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.IHostToolDialog;
import org.redcross.sar.gui.PositionPanel;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.data.IUnitIf;
import org.redcross.sar.util.mso.Position;

import com.esri.arcgis.geometry.Point;

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
	public PositionTool(IHostToolDialog dialog) throws IOException {

		// forward
		super(true,DrawFeatureType.DRAW_FEATURE_POINT);
		
		// prepare abstract class BasicTool
		cursorPath = "cursors/crosshair.cur"; 
		caption = "Posisjon"; 
		category = "Commands"; 
		message = "Setter posisjon til valgt enhet"; 
		name = "CustomCommands_Position"; 
		toolTip = "Posisjon"; 
		enabled = true;
		
		// set tool type
		type = DiskoToolType.POSITION_TOOL;		

		// map draw operation
		onMouseDownAction = DrawActionType.DRAW_BEGIN;
		onMouseUpAction = DrawActionType.DRAW_FINISH;
		
		// get current application
		IDiskoApplication app = Utils.getApp();
		
		// create button
		Dimension size = app.getUIFactory().getSmallButtonSize();
		button = new JToggleButton();
		button.setPreferredSize(size);

		// create current point
		p = new Point();
		p.setX(0);
		p.setY(0);
		
		// save dialog
		this.dialog = (DiskoDialog)dialog;
		
		// create default property panel
		propertyPanel = addPropertyPanel();
		
		// registrate me in dialog
		dialog.register((IDrawTool)this, propertyPanel);
		
	}

	@Override
	public void onCreate(Object obj) {
		
		// forward
		super.onCreate(obj);
		
		try {

			// is map valid?
			if (map!=null) {
				
				// add layer listener
				IMsoFeatureLayer msoLayer = map.getMsoLayer(IMsoFeatureLayer.LayerCode.UNIT_LAYER);
				Iterator<JPanel> it = panels.iterator();
				while(it.hasNext()) {
					msoLayer.addDiskoLayerEventListener((PositionPanel)it.next());
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
			if(validate()) {
				// update
				p = transform(x, y);
				p.setSpatialReferenceByRef(map.getSpatialReference());
				getPositionPanel().updatePosition(p);
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
			getPositionPanel().updatePosition(p);
			// hosted?
			if(isHosted()) {
				getHostTool().setTool(this);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// return flag
		return bflag;
		
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
			if (msoClassCode != IMsoManagerIf.MsoClassCode.CLASSCODE_UNIT) {
				Utils.showWarning("Kun enhet kan endre posisjon");
				// do not add point
				bDoWork = false;
			}
		}
				
		// return state
		return bDoWork;
		
	}

	public IUnitIf getCurrentUnit() {
		if(msoObject instanceof IUnitIf)
			return (IUnitIf)msoObject;
		else
			return null;
	}
	
	public void setCurrentUnit(IUnitIf unit) {
		if(msoObject!=unit) {
			msoObject = unit;
			getPositionPanel().setCurrentUnit(unit);
		}
	}
	
	public Position getPosition() {
		if(msoObject instanceof IUnitIf)
			return ((IUnitIf)msoObject).getPosition();
		else
			return null;
	}
	
	public void setPositionAt(Point p) {
		// validate
		if(validate()) {
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
		
	public PositionPanel getPositionPanel() {
		return (PositionPanel)propertyPanel;
	}
	
	@Override
	public JPanel addPropertyPanel() {
		// create panel list?
		if(panels==null)
			panels = new ArrayList<JPanel>(1);			
		// create panel
		JPanel panel = new PositionPanel(this,true);
		// try to add
		if (panels.add(panel)) {
			return panel;
		}
		return null;
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
		
		// create state
		public PositionToolState(PositionTool tool) {
			super((AbstractDiskoTool)tool);
			save(tool);
		}		
		public void save(PositionTool tool) {
			super.save((AbstractDiskoTool)tool);
			this.unit = tool.getPositionPanel().getCurrentUnit();
		}
		
		public void load(PositionTool tool) {
			super.load((AbstractDiskoTool)tool);
			tool.getPositionPanel().setCurrentUnit(this.unit);
			tool.getPositionPanel().updatePosition(p);
		}
	}

}
