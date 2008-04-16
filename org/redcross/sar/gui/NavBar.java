package org.redcross.sar.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.map.IHostToolDialog;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.DrawHostTool;
import org.redcross.sar.map.command.ElementCommand;
import org.redcross.sar.map.command.EraseTool;
import org.redcross.sar.map.command.FlankTool;
import org.redcross.sar.map.command.FreeHandTool;
import org.redcross.sar.map.command.GotoCommand;
import org.redcross.sar.map.command.IHostDiskoTool;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.LineTool;
import org.redcross.sar.map.command.MapToggleCommand;
import org.redcross.sar.map.command.POITool;
import org.redcross.sar.map.command.PositionTool;
import org.redcross.sar.map.command.ScaleCommand;
import org.redcross.sar.map.command.SelectFeatureTool;
import org.redcross.sar.map.command.SplitTool;
import org.redcross.sar.map.command.TocCommand;
import org.redcross.sar.map.command.IDiskoCommand.DiskoCommandType;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.command.IDiskoTool.IDiskoToolState;

import com.esri.arcgis.controls.ControlsMapFullExtentCommand;
import com.esri.arcgis.controls.ControlsMapPanTool;
import com.esri.arcgis.controls.ControlsMapZoomInFixedCommand;
import com.esri.arcgis.controls.ControlsMapZoomInTool;
import com.esri.arcgis.controls.ControlsMapZoomOutFixedCommand;
import com.esri.arcgis.controls.ControlsMapZoomOutTool;
import com.esri.arcgis.controls.ControlsMapZoomToLastExtentBackCommand;
import com.esri.arcgis.controls.ControlsMapZoomToLastExtentForwardCommand;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ICommand;
import com.esri.arcgis.systemUI.ITool;

public class NavBar extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	public enum ButtonPlacement {
		LEFT,
		RIGHT
    }
	
	private IDiskoApplication app = null;
	private ButtonGroup bgroup  = null;
	private JToggleButton dummyToggleButton = null;
	private Hashtable<Enum<?>, ICommand> commands  = null;
	private Hashtable<Enum<?>, AbstractButton> buttons  = null;
	private JPanel leftPanel  = null;
	private JPanel rightPanel = null;
	private JToggleButton drawHostToolToggleButton = null;
	private JToggleButton flankToggleButton = null;
	private JToggleButton lineToolToggleButton = null;
	private JToggleButton freeHandToggleButton = null;
	private JToggleButton eraseToggleButton = null;
	private JToggleButton splitToggleButton = null;
	private JToggleButton poiToggleButton = null;	
	private JToggleButton positionToggleButton = null;	
	private JToggleButton zoomInToggleButton = null;
	private JToggleButton zoomOutToggleButton = null;
	private JToggleButton panToggleButton = null;
	private JToggleButton selectFeatureToggleButton = null;
	private JButton zoomInFixedButton = null;
	private JButton zoomOutFixedButton = null;
	private JButton fullExtentButton = null;
	private JButton zoomToLastExtentForwardButton = null;
	private JButton zoomToLastExtentBackwardButton = null;	
	private JButton mapToggleButton = null;
	private JButton tocButton = null;
	private JButton scaleButton = null;
	private JButton elementButton = null;
	private JButton gotoButton = null;
	private AbstractButton standardButton = null;
	
	private DrawHostTool drawHostTool = null;
	private LineTool lineTool = null;
	private FreeHandTool freeHandTool = null;
	private FlankTool flankTool = null;
	private SplitTool splitTool = null;
	private POITool puiTool = null;
	private PositionTool positionTool = null;
	private TocCommand tocCommand = null;
	private ElementCommand elementCommand = null;
	private GotoCommand gotoCommand = null;	
	private EraseTool eraseCommand = null;
	private ScaleCommand scaleCommand = null;
	private SelectFeatureTool selectFeatureTool = null;
	private ControlsMapZoomInTool zoomInTool = null;
	private ControlsMapZoomOutTool zoomOutTool = null;
	private ControlsMapPanTool panTool = null;
	private ControlsMapZoomInFixedCommand zoomInFixedCommand = null;
	private ControlsMapZoomOutFixedCommand zoomOutFixedCommand = null;
	private ControlsMapFullExtentCommand fullExtentCommand = null;
	private ControlsMapZoomToLastExtentForwardCommand zoomToLastExtentForwardCommand = null;
	private ControlsMapZoomToLastExtentBackCommand zoomToLastExtentBackCommand = null;
	private MapToggleCommand mapToggle = null;

	public NavBar() {
		this(null);
	}
	
	public NavBar(IDiskoApplication app) {
		this.app = app;
		initialize();
	}
	
	private void initialize() {
		commands = new Hashtable<Enum<?>, ICommand>();
		buttons = new Hashtable<Enum<?>, AbstractButton>();
		bgroup = new ButtonGroup();
			
		setLayout(new BorderLayout());
		add(getLeftPanel(), BorderLayout.WEST);
		add(getRightPanel(), BorderLayout.EAST);
			
		// Add a not visible dummy JToggleButton, used to unselect all
		// (visbible) JToggleButtons. This is a hack suggested by Java dev forum
		bgroup.add(getDummyToggleButton());
			
		addCommand(getDrawHostToolToggleButton(), getDrawHostTool(), 
				DiskoToolType.DRAW_HOST_TOOL, ButtonPlacement.LEFT);
		addCommand(getFreeHandToggleButton(), getFreeHandTool(), 
				DiskoToolType.FREEHAND_TOOL, ButtonPlacement.LEFT);
		addCommand(getLineToolToggleButton(), getLineTool(), 
				DiskoToolType.LINE_TOOL, ButtonPlacement.LEFT);
		addCommand(getPOIToggleButton(), getPOITool(), 
				DiskoToolType.POI_TOOL, ButtonPlacement.LEFT);
		addCommand(getPositionToggleButton(), getPositionTool(), 
				DiskoToolType.POSITION_TOOL, ButtonPlacement.LEFT);
		addCommand(getFlankToggleButton(), getFlankTool(), 
				DiskoToolType.FLANK_TOOL, ButtonPlacement.LEFT);
		addCommand(getSplitToggleButton(), getSplitTool(), 
				DiskoToolType.SPLIT_TOOL, ButtonPlacement.LEFT);
		addCommand(getSelectFeatureToggleButton(), getSelectFeatureTool(), 
				DiskoToolType.SELECT_FEATURE_TOOL, ButtonPlacement.LEFT);
		addCommand(getEraseButton(), getEraseCommand(), 
				DiskoToolType.ERASE_TOOL, ButtonPlacement.LEFT);
		addCommand(getElementToggleButton(), getElementCommand(), 
				DiskoCommandType.ELEMENT_COMMAND, ButtonPlacement.LEFT);
		addCommand(getZoomInToggleButton(), getZoomInTool(), 
				DiskoToolType.ZOOM_IN_TOOL, ButtonPlacement.RIGHT);
		addCommand(getZoomOutToggleButton(), getZoomOutTool(), 
				DiskoToolType.ZOOM_OUT_TOOL, ButtonPlacement.RIGHT);
		addCommand(getPanToggleButton(), getPanTool(), 
				DiskoToolType.PAN_TOOL, ButtonPlacement.RIGHT);
		addCommand(getZoomInFixedButton(), getZoomInFixedCommand(),
				DiskoCommandType.ZOOM_IN_FIXED_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getZoomOutFixedButton(), getZoomOutFixedCommand(),
				DiskoCommandType.ZOOM_OUT_FIXED_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getFullExtentButton(), getFullExtentCommand(), 
				DiskoCommandType.ZOOM_FULL_EXTENT_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getZoomToLastExtentBackwardButton(), getZoomToLastExtentBackCommand(), 
				DiskoCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND, ButtonPlacement.RIGHT);	
		addCommand(getZoomToLastExtentForwardButton(), getZoomToLastExtentForwardCommand(), 
				DiskoCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getMapToggleButton(), getMapToggleCommand(), 
				DiskoCommandType.MAP_TOGGLE_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getScaleButton(), getScaleCommand(), 
				DiskoCommandType.SCALE_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getTocToggleButton(), getTocTool(), 
				DiskoCommandType.TOC_COMMAND, ButtonPlacement.RIGHT);
		addCommand(getGotoToggleButton(), getGotoTool(), 
				DiskoCommandType.GOTO_COMMAND, ButtonPlacement.RIGHT);
	}
	
	private JPanel getLeftPanel() {
		if (leftPanel == null) {
			try {
				leftPanel = new JPanel();
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.LEFT);
				leftPanel.setLayout(fl);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return leftPanel;
	}
	
	private JPanel getRightPanel() {
		if (rightPanel == null) {
			try {
				rightPanel = new JPanel();
				FlowLayout fl = new FlowLayout();
				fl.setHgap(0);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.RIGHT);
				rightPanel.setLayout(fl);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return rightPanel;
	}
	
	
	public LineTool getLineTool() {
		if (lineTool == null) {
			try {
				lineTool = new LineTool((IHostToolDialog)
						getDrawHostTool().getDialog(),false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lineTool;
	}
	
	public FreeHandTool getFreeHandTool() {
		if (freeHandTool == null) {
			try {
				freeHandTool = new FreeHandTool((IHostToolDialog)
						getDrawHostTool().getDialog(),false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return freeHandTool;
	}
	
	public DrawHostTool getDrawHostTool() {
		if (drawHostTool == null) {
			try {
				drawHostTool = new DrawHostTool();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return drawHostTool;
	}
	
	public FlankTool getFlankTool() {
		if (flankTool == null) {
			try {
				flankTool = new FlankTool((IHostToolDialog)
						getDrawHostTool().getDialog());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flankTool;
	}
	
	public TocCommand getTocTool() {
		if (tocCommand == null) {
			try {
				tocCommand = new TocCommand();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tocCommand;
	}	
	
	public ElementCommand getElementCommand() {
		if (elementCommand == null) {
			try {
				elementCommand = new ElementCommand();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return elementCommand;
	}	
	
	public GotoCommand getGotoTool() {
		if (gotoCommand == null) {
			try {
				gotoCommand = new GotoCommand();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return gotoCommand;
	}	
	
	public SplitTool getSplitTool() {
		if (splitTool == null) {
			try {
				splitTool = new SplitTool();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return splitTool;
	}
	
	public POITool getPOITool() {
		if (puiTool == null) {
			try {
				puiTool = new POITool((IHostToolDialog)
						getDrawHostTool().getDialog());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return puiTool;
	}
	
	public PositionTool getPositionTool() {
		if (positionTool == null) {
			try {
				positionTool = new PositionTool((IHostToolDialog)
						getDrawHostTool().getDialog());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return positionTool;
	}
	
	public SelectFeatureTool getSelectFeatureTool() {
		if (selectFeatureTool == null) {
			try {
				selectFeatureTool = new SelectFeatureTool();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return selectFeatureTool;
	}
	
	private ControlsMapZoomInTool getZoomInTool() {
		if (zoomInTool == null) {
			try {
				zoomInTool = new ControlsMapZoomInTool();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomInTool;
	}
	
	private ControlsMapZoomOutTool getZoomOutTool() {
		if (zoomOutTool == null) {
			try {
				zoomOutTool = new ControlsMapZoomOutTool();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomOutTool;
	}
	
	private ControlsMapPanTool getPanTool() {
		if (panTool == null) {
			try {
				panTool = new ControlsMapPanTool();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return panTool;
	}
		
	private ControlsMapZoomInFixedCommand getZoomInFixedCommand() {
		if (zoomInFixedCommand == null) {
			try {
				zoomInFixedCommand = new ControlsMapZoomInFixedCommand();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomInFixedCommand;
	}
	
	private ControlsMapZoomOutFixedCommand getZoomOutFixedCommand() {
		if (zoomOutFixedCommand == null) {
			try {
				zoomOutFixedCommand = new ControlsMapZoomOutFixedCommand();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomOutFixedCommand;
		
	}
	
	private ControlsMapFullExtentCommand getFullExtentCommand() {
		if (fullExtentCommand == null) {
			try {
				fullExtentCommand = new ControlsMapFullExtentCommand();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fullExtentCommand;
		
	}
	
	private ControlsMapZoomToLastExtentForwardCommand getZoomToLastExtentForwardCommand() {
		if (zoomToLastExtentForwardCommand == null) {
			try {
				zoomToLastExtentForwardCommand = new ControlsMapZoomToLastExtentForwardCommand();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomToLastExtentForwardCommand;
		
	}
	
	private ControlsMapZoomToLastExtentBackCommand getZoomToLastExtentBackCommand() {
		if (zoomToLastExtentBackCommand == null) {
			try {
				zoomToLastExtentBackCommand = new ControlsMapZoomToLastExtentBackCommand();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomToLastExtentBackCommand;
	}
	
	
	private MapToggleCommand getMapToggleCommand() {		
		if (mapToggle == null) {
			try {
				mapToggle = new MapToggleCommand();			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mapToggle;
	}
	
	private EraseTool getEraseCommand() {		
		if (eraseCommand == null) {
			try {
				eraseCommand = new EraseTool();			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return eraseCommand;
	}
	
	private ScaleCommand getScaleCommand() {		
		if (scaleCommand == null) {
			try {
				scaleCommand = new ScaleCommand();			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return scaleCommand;
	}
	
	private JToggleButton getDummyToggleButton() {
		if (dummyToggleButton == null) {
			dummyToggleButton = new JToggleButton();
			dummyToggleButton.setVisible(false);
		}
		return dummyToggleButton;
	}
	
	public JToggleButton getDrawHostToolToggleButton() {
		if (drawHostToolToggleButton == null) {
			try {
				drawHostToolToggleButton = (JToggleButton)getDrawHostTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return drawHostToolToggleButton;
	}
	
	public JToggleButton getFlankToggleButton() {
		if (flankToggleButton == null) {
			try {
				flankToggleButton = (JToggleButton)getFlankTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return flankToggleButton;
	}
	
	public JToggleButton getSelectFeatureToggleButton() {
		if (selectFeatureToggleButton == null)  {
			try {
				selectFeatureToggleButton = (JToggleButton)getSelectFeatureTool().getButton();
				standardButton = selectFeatureToggleButton;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return selectFeatureToggleButton;
	}
	
	public JToggleButton getLineToolToggleButton() {
		if (lineToolToggleButton == null) {
			try {
				lineToolToggleButton = (JToggleButton)getLineTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return lineToolToggleButton;
	}
	
	
	public JToggleButton getFreeHandToggleButton() {
		if (freeHandToggleButton == null) {
			try {
				freeHandToggleButton = (JToggleButton)getFreeHandTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return freeHandToggleButton;
	}
	
	public JToggleButton getSplitToggleButton() {
		if (splitToggleButton == null) {
			try {
				splitToggleButton = (JToggleButton)getSplitTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return splitToggleButton;
	}
	
	public JToggleButton getEraseToggleButton() {
		if (eraseToggleButton == null) {
			try {
				eraseToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return eraseToggleButton;
	}
	
	public JToggleButton getPOIToggleButton() {
		if (poiToggleButton == null) {
			try {
				poiToggleButton = (JToggleButton)getPOITool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return poiToggleButton;
	}
	
	public JToggleButton getPositionToggleButton() {
		if (positionToggleButton == null) {
			try {
				positionToggleButton = (JToggleButton)getPositionTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return positionToggleButton;
	}
	
	public JToggleButton getZoomInToggleButton() {
		if (zoomInToggleButton == null) {
			try {
				zoomInToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomInToggleButton;
	}
	
	public JToggleButton getZoomOutToggleButton() {
		if (zoomOutToggleButton == null) {
			try {			
				zoomOutToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomOutToggleButton;
	}
	
	public JToggleButton getPanToggleButton() {
		if (panToggleButton == null)  {
			try {
				panToggleButton = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return panToggleButton;
	}
	
	public JButton getMapToggleButton(){
		if (mapToggleButton == null) {
			try {
				mapToggleButton = (JButton)getMapToggleCommand().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return mapToggleButton;
	}
	
	public JButton getTocToggleButton(){
		if (tocButton == null) {
			try {
				tocButton = (JButton)getTocTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return tocButton;
	}
	
	public JButton getElementToggleButton(){
		if (elementButton == null) {
			try {
				elementButton = (JButton)getElementCommand().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return elementButton;
	}
	
	public JButton getGotoToggleButton(){
		if (gotoButton == null) {
			try {
				gotoButton = (JButton)getGotoTool().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return gotoButton;
	}
	
	public JButton getZoomInFixedButton() {
		if (zoomInFixedButton == null) {
			try {
				zoomInFixedButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomInFixedButton;
	}
	
	public JButton getZoomOutFixedButton() {
		if (zoomOutFixedButton == null) {
			try {
				zoomOutFixedButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomOutFixedButton;
	}
	
	
	public JButton getFullExtentButton() {
		if (fullExtentButton == null) {
			try {
				fullExtentButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fullExtentButton;
	}
	
	public JButton getZoomToLastExtentForwardButton() {
		if (zoomToLastExtentForwardButton == null) {
			try {
				zoomToLastExtentForwardButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomToLastExtentForwardButton;
	}
	
	public JButton getZoomToLastExtentBackwardButton() {
		if (zoomToLastExtentBackwardButton == null) {
			try {
				zoomToLastExtentBackwardButton = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomToLastExtentBackwardButton;
	}
	
	public JToggleButton getEraseButton(){
		if (eraseToggleButton == null) {
			try {
				eraseToggleButton = (JToggleButton)getEraseCommand().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return eraseToggleButton;
	}
		
	public JButton getScaleButton(){
		if (scaleButton == null) {
			try {
				scaleButton = (JButton)getScaleCommand().getButton();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		return scaleButton;
	}
	
	public NavState save() {
		// forward
		return new NavState(this);
	}
	
	public void load(NavState state) {
		if(state!=null) {
			state.load(this);
			setup();
		}
	}
	
	public void setup() {	
		try {
			// initialize
			boolean isAnySelected = false;
			// get map
			IDiskoMap map = app.getCurrentMap();
			// load
			Iterator commandIter = commands.values().iterator();
			Iterator buttonIter  = buttons.values().iterator();
			// loop over all keys
			while (commandIter.hasNext() && buttonIter.hasNext()) {
				// get command
				ICommand command = (ICommand)commandIter.next();
				if (command != null) {
					// get key and button
					AbstractButton b = (AbstractButton)buttonIter.next();
					// has current wp a map?
					if (map != null) {
						// create command
						command.onCreate(map);
						// activate button?
						if (b.isSelected()) {
							//b.doClick();
							isAnySelected=true;
						}
					}
					else {
						if (command instanceof IDiskoTool) {
							((IDiskoTool)command).deactivate();
						}
						else if (command instanceof IHostDiskoTool) {
							((IHostDiskoTool)command).deactivate();
						}
					}
				}
			}
			// setup host tools
			setupHostTools();
			// select default?
			if(!isAnySelected) {
				standardButton.doClick();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void hideDialogs() {
		Iterator commandIter = commands.values().iterator();
		while (commandIter.hasNext()) {
			ICommand command = (ICommand)commandIter.next();
			if (command instanceof IDiskoTool) {
				IDiskoTool diskoTool = (IDiskoTool)command;
				if (diskoTool.getDialog() != null) {
					diskoTool.getDialog().setVisible(false);
				}
			}
			if (command instanceof IHostDiskoTool) {
				IHostDiskoTool hostTool = (IHostDiskoTool)command;
				if (hostTool.getDialog() != null) {
					hostTool.getDialog().setVisible(false);
				}				
			}
		}
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		if(!isVisible)
			hideDialogs();
		super.setVisible(isVisible);
	}
	
	public void unselectAll() {
		getDummyToggleButton().doClick(); // HACK: unselect all toggle buttons
	}
	
	public AbstractButton getButton(Enum key) {
		return (AbstractButton)buttons.get(key);
	}
	
	public void addCommand(AbstractButton button, ICommand command, Enum e, Enum buttonPlacement) {
		if (buttonPlacement == ButtonPlacement.LEFT) {
			getLeftPanel().add(button);
		} else {
			getRightPanel().add(button);
		}
		if (button instanceof JToggleButton) {
			bgroup.add(button);
		}
		buttons.put(e, button);
		commands.put(e, command);
		if (command != null) {
			button.addActionListener(new NavActionListener(command));
		}
		DiskoCustomIcon icon = new DiskoCustomIcon(Utils.getIcon(e,"48x48"));
		if (icon != null) {
			button.setIcon(icon);
		}
		button.setToolTipText(Utils.getIconText(e));
	}
	
	public List<Enum<?>> getEnabledButtons(boolean isEnabled){
		List<Enum<?>> myInterest = Utils.getListNoneOf(DiskoToolType.class);
		Iterator<Enum<?>> it = buttons.keySet().iterator();
		while (it.hasNext()) {
			Enum key = it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			if(button!=null && button.isEnabled()==isEnabled) {
				myInterest.add(key);
			}
		}
		return myInterest;
	}
	
	public void setEnabledButtons(List<Enum<?>> myInterests,
			boolean isEnabled, boolean append) {
		// disable all?
		if(!append)
			setEnabledButtons(buttons.keySet().iterator(),!isEnabled);
		// enable selected
		setEnabledButtons(myInterests.iterator(),isEnabled);
		// update hosts
		setupHostTools();
	}
	
	private void setEnabledButtons(Iterator<Enum<?>> it, boolean isEnabled) {
		while (it.hasNext()) {
			Enum key = it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			ICommand command = (ICommand)commands.get(key);
			// is hosted?
			if(command instanceof IDiskoTool) {
				IDiskoTool tool = (IDiskoTool)command;
				if(tool.isHosted()) {
					((IHostToolDialog)tool.getDialog())
						.setEnabled(tool.getType(), isEnabled);
					// buttons of hosted tools should not 
					// NEVER be shown on the navbar!
					if (button != null)
						button.setVisible(false);
				}
			} 
			else if (button != null) {
				button.setEnabled(isEnabled);
			}
		}		
	}
	
	public List<Enum<?>> getVisibleButtons(boolean isVisible){
		List<Enum<?>> myInterest = Utils.getListNoneOf(DiskoToolType.class);
		Iterator<Enum<?>> it = buttons.keySet().iterator();
		while (it.hasNext()) {
			Enum key = it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			if(button!=null && button.isVisible()==isVisible) {
				myInterest.add(key);
			}
		}
		return myInterest;
	}
	
	public void setVisibleButtons(List<Enum<?>> buttons, 
			boolean isVisible, boolean append) {
		// hide all?
		if(!append)
			setVisibleButtons(this.buttons.keySet().iterator(),!isVisible);
		// show selected
		setVisibleButtons(buttons.iterator(),isVisible);
		// update hosts
		setupHostTools();
	}
	
	private void setVisibleButtons(Iterator<Enum<?>> it, boolean isVisible) {
		while (it.hasNext()) {
			// initialize
			boolean bIsVisible = isVisible;
			Enum key = (Enum)it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			ICommand command = (ICommand)commands.get(key);
			// is hosted?
			if(command instanceof IDiskoTool) {
				IDiskoTool tool = (IDiskoTool)command;
				if(tool.isHosted()) {
					((IHostToolDialog)tool.getDialog())
						.setVisible(tool.getType(), isVisible);
					// buttons of hosted tools should not 
					// NEVER be shown on the navbar!
					bIsVisible = false;
				}
			} 
			if (button != null)
				button.setVisible(bIsVisible);
		}
	}	
	
	private void setupHostTools() {
		// get draw host
		IHostDiskoTool host = (IHostDiskoTool)commands.get(DiskoToolType.DRAW_HOST_TOOL);
		// update
		((IHostToolDialog)host.getDialog()).setup();
	}
	
	public void switchIcon(String command, int index){
		if(command.equalsIgnoreCase("maptoggle")){
			//System.out.println("spennende...");
			AbstractButton ab = this.getButton(DiskoCommandType.MAP_TOGGLE_COMMAND);
			ImageIcon icon;
			if(index==0)
				icon = new ImageIcon(app.getProperty("DiskoCommandType.MAP_TOGGLE_COMMAND.icon"));
			else 
				icon = new ImageIcon(app.getProperty("DiskoCommandType.MAP_TOGGLE_COMMAND_2.icon"));
			ab.setIcon(icon);			
		}
	}
	
	class NavActionListener implements ActionListener {
		
		private ICommand command = null;
		
		NavActionListener(ICommand command) {
			this.command = command;
		}
		
		public void actionPerformed(java.awt.event.ActionEvent e) {
			try {
				if (command instanceof ITool) {
					IDiskoMap map = app.getCurrentMap();
					if(map!=null)
						map.setActiveTool((ITool)command,true);
				}
				else {
					command.onClick();
				}
			} catch (AutomationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public class NavState {

		private ButtonState m_navButton = null;
		private Hashtable<Enum<?>, ButtonState> m_buttons = null;
		private Hashtable<Enum<?>, IDiskoToolState> m_tools = null;
		
		public NavState(NavBar bar) {
			// initialize
			m_tools = new Hashtable<Enum<?>, IDiskoToolState>();
			m_buttons = new Hashtable<Enum<?>,ButtonState>();
			// forward
			save(bar);
		}
		
		public void save(NavBar bar) {
			// get nav button
			UIFactory uiFactory = app.getUIFactory();
			AbstractButton navButton = uiFactory.getMainMenuPanel().getNavToggleButton();
			// save state
			m_navButton = new ButtonState("NavButton",navButton); 
			// erase current
			m_tools.clear();
			m_buttons.clear();
			// loop over all tools and buttons and save states
			Enumeration<Enum<?>> keys  = bar.buttons.keys();
			Iterator<ICommand> it = commands.values().iterator();
			// loop over all keys
			while (keys.hasMoreElements() && it.hasNext()) {
				// get enum key
				Enum<?> key = keys.nextElement();
				// get button state
				ButtonState buttonState = new ButtonState(key,bar.buttons.get(key));
				// put to button hashtable
				m_buttons.put(key, buttonState);
				// get command
				ICommand cmd = it.next();
				// is a disko tool?
				if(cmd instanceof IDiskoTool) {
					// get tool
					IDiskoTool tool = (IDiskoTool)cmd;
					// get state
					IDiskoToolState state = tool.save();
					// override isVisible?
					if(tool.isHosted()) {
						// override
						buttonState.m_isVisible = 
							((IHostToolDialog)tool.getDialog())
								.getVisible(tool.getType());
					}
					// put to command hashtable?
					if(state!=null)
						m_tools.put(key, state);					
				}
			}			
		}
		
		public void load(NavBar bar) {
			// loop over all buttons and commands and load state
			Enumeration<Enum<?>> keys  = bar.buttons.keys();
			Iterator<ICommand> it = commands.values().iterator();
			// loop over all keys
			while (keys.hasMoreElements() && it.hasNext()) {
				// get enum key
				Enum<?> key = keys.nextElement();
				// put to hashtable
				ButtonState state = m_buttons.get(key);
				// get button
				AbstractButton button = bar.buttons.get(key);
				// load button
				state.load(button);
				// get command
				ICommand cmd = it.next();
				// is a disko tool?
				if(cmd instanceof IDiskoTool) {	
					// load tool
					((IDiskoTool)cmd).load(m_tools.get(key));
					// cast to IDiskoTool
					IDiskoTool tool = (IDiskoTool)cmd;
					// is a hosted tool?
					if(tool.isHosted()) {
						// transfer state to draw dialog
						((IHostToolDialog)tool.getDialog())
							.setVisible(tool.getType(),button.isVisible());
						// buttons of hosted tools should not 
						// NEVER be shown on the navbar!
						button.setVisible(false);
					}
				}
			}						
			// get nav button
			UIFactory uiFactory = app.getUIFactory();
			AbstractButton navButton = uiFactory.getMainMenuPanel().getNavToggleButton();
			// load state
			m_navButton.load(navButton);
			// hide me?
		}
		
		public class ButtonState {
			private Object m_key = null;
			private boolean m_isVisible = false;
			private boolean m_isEnabled = false;
			private boolean m_isSelected = false;
			
			public ButtonState(Object key, AbstractButton button) {
				m_key = key;
				// forward
				save(button);
			}
			
			public Object getKey() {
				return m_key;
			}
			
			public void save(AbstractButton button) {
				m_isVisible = button.isVisible();
				m_isEnabled = button.isEnabled();
				m_isSelected = button.isSelected();
			}
			
			public void load(AbstractButton button) {
				// update properties
				button.setVisible(m_isVisible);
				button.setEnabled(m_isEnabled);
				// fire click?
				if (m_isSelected != button.isSelected()) {
					button.doClick();
				}
			}
		};
	};
	
}


