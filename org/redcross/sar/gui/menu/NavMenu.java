package org.redcross.sar.gui.menu;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.EventListenerList;

import org.redcross.sar.gui.ButtonState;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.ElementCommand;
import org.redcross.sar.map.command.GotoCommand;
import org.redcross.sar.map.command.IMapCommand;
import org.redcross.sar.map.command.MapToggleCommand;
import org.redcross.sar.map.command.ScaleCommand;
import org.redcross.sar.map.command.TocCommand;
import org.redcross.sar.map.command.IMapCommand.MapCommandType;
import org.redcross.sar.map.command.IMapCommand.IDiskoCommandState;
import org.redcross.sar.map.tool.DiskoToolWrapper;
import org.redcross.sar.map.tool.DrawHostTool;
import org.redcross.sar.map.tool.EraseTool;
import org.redcross.sar.map.tool.FlankTool;
import org.redcross.sar.map.tool.FreeHandTool;
import org.redcross.sar.map.tool.IMapTool;
import org.redcross.sar.map.tool.IHostDiskoTool;
import org.redcross.sar.map.tool.IToolCollection;
import org.redcross.sar.map.tool.LineTool;
import org.redcross.sar.map.tool.POITool;
import org.redcross.sar.map.tool.PositionTool;
import org.redcross.sar.map.tool.SelectTool;
import org.redcross.sar.map.tool.SplitTool;
import org.redcross.sar.map.tool.DiskoToolWrapper.WrapAction;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.map.tool.IMapTool.IMapToolState;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.util.Utils;

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

public class NavMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	public enum ButtonPlacement {
		LEFT,
		RIGHT
    }

	private UIFactory factory;
	private JToggleButton dummyToggleButton;
	private JPanel leftPanel ;
	private JPanel rightPanel;

	/**
	 * Button that controls this menu visible state
	 */
	private JToggleButton menuToggleButton;

	/**
	 * Tool buttons
	 */
	private JToggleButton drawHostToolToggleButton;
	private JToggleButton flankToggleButton;
	private JToggleButton lineToolToggleButton;
	private JToggleButton freeHandToggleButton;
	private JToggleButton eraseToggleButton;
	private JToggleButton splitToggleButton;
	private JToggleButton poiToggleButton;
	private JToggleButton positionToggleButton;
	private JToggleButton zoomInToggleButton;
	private JToggleButton zoomOutToggleButton;
	private JToggleButton panToggleButton;
	private JToggleButton selectFeatureToggleButton;

	/**
	 * Command buttons
	 */
	private JButton zoomInFixedButton;
	private JButton zoomOutFixedButton;
	private JButton fullExtentButton;
	private JButton zoomToLastExtentForwardButton;
	private JButton zoomToLastExtentBackwardButton;
	private JButton mapToggleButton;
	private JButton tocButton;
	private JButton scaleButton;
	private JButton elementButton;
	private JButton gotoButton;
	private AbstractButton standardButton;

	private DrawHostTool drawHostTool;
	private LineTool lineTool;
	private FreeHandTool freeHandTool;
	private FlankTool flankTool;
	private SplitTool splitTool;
	private POITool puiTool;
	private PositionTool positionTool;
	private TocCommand tocCommand;
	private ElementCommand elementCommand;
	private GotoCommand gotoCommand;
	private EraseTool eraseTool;
	private ScaleCommand scaleCommand;
	private SelectTool selectFeatureTool;
	private DiskoToolWrapper zoomInTool;
	private DiskoToolWrapper zoomOutTool;
	private DiskoToolWrapper panTool;
	private DiskoToolWrapper zoomInFixedCommand;
	private DiskoToolWrapper zoomOutFixedCommand;
	private DiskoToolWrapper fullExtentCommand;
	private DiskoToolWrapper zoomToLastExtentForwardCommand;
	private DiskoToolWrapper zoomToLastExtentBackwardCommand;
	private MapToggleCommand mapToggle;

	private final ButtonGroup bgroup = new ButtonGroup();
	private final EventListenerList listeners = new EventListenerList();
	private final Hashtable<Enum<?>, ICommand> commands = new Hashtable<Enum<?>, ICommand>();
	private final Hashtable<Enum<?>, AbstractButton> buttons = new Hashtable<Enum<?>, AbstractButton>();

	public NavMenu(UIFactory factory, MainMenu mainMenu) {
		// prepare
		this.factory = factory;
		this.menuToggleButton = mainMenu.getNavToggleButton();
		// initialize GUI
		initialize();
	}

	private void initialize() {

		// defaults
		setVisible(false);

		// add to layout
		setLayout(new BorderLayout());
		add(getLeftPanel(), BorderLayout.WEST);
		add(getRightPanel(), BorderLayout.EAST);

		// Add a not visible dummy JToggleButton, used to unselect all
		// (visbible) JToggleButtons. This is a hack suggested by Java dev forum
		bgroup.add(getDummyToggleButton());

		// add all available commands
		addCommand(getDrawHostToolToggleButton(), getDrawHostTool(),
				MapToolType.DRAW_HOST_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getFreeHandToggleButton(), getFreeHandTool(),
				MapToolType.FREEHAND_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getLineToolToggleButton(), getLineTool(),
				MapToolType.LINE_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getPOIToggleButton(), getPOITool(),
				MapToolType.POI_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getPositionToggleButton(), getPositionTool(),
				MapToolType.POSITION_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getFlankToggleButton(), getFlankTool(),
				MapToolType.FLANK_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getSplitToggleButton(), getSplitTool(),
				MapToolType.SPLIT_TOOL, ButtonPlacement.LEFT,1);
		addCommand(getSelectFeatureToggleButton(), getSelectFeatureTool(),
				MapToolType.SELECT_TOOL, ButtonPlacement.LEFT,0);
		addCommand(getEraseButton(), getEraseTool(),
				MapToolType.ERASE_TOOL, ButtonPlacement.LEFT,0);
		addCommand(getElementToggleButton(), getElementCommand(),
				MapCommandType.ELEMENT_COMMAND, ButtonPlacement.LEFT,1);
		addCommand(getZoomInToggleButton(), getZoomInTool(),
				MapToolType.ZOOM_IN_TOOL, ButtonPlacement.RIGHT,0);
		addCommand(getZoomOutToggleButton(), getZoomOutTool(),
				MapToolType.ZOOM_OUT_TOOL, ButtonPlacement.RIGHT,0);
		addCommand(getPanToggleButton(), getPanTool(),
				MapToolType.PAN_TOOL, ButtonPlacement.RIGHT,0);
		addCommand(getZoomInFixedButton(), getZoomInFixedCommand(),
				MapCommandType.ZOOM_IN_FIXED_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getZoomOutFixedButton(), getZoomOutFixedCommand(),
				MapCommandType.ZOOM_OUT_FIXED_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getFullExtentButton(), getFullExtentCommand(),
				MapCommandType.ZOOM_FULL_EXTENT_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getZoomToLastExtentBackwardButton(), getZoomToLastExtentBackwardCommand(),
				MapCommandType.ZOOM_TO_LAST_EXTENT_BACKWARD_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getZoomToLastExtentForwardButton(), getZoomToLastExtentForwardCommand(),
				MapCommandType.ZOOM_TO_LAST_EXTENT_FORWARD_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getMapToggleButton(), getMapToggleCommand(),
				MapCommandType.MAP_TOGGLE_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getScaleButton(), getScaleCommand(),
				MapCommandType.SCALE_COMMAND, ButtonPlacement.RIGHT,1);
		addCommand(getTocToggleButton(), getTocTool(),
				MapCommandType.TOC_COMMAND, ButtonPlacement.RIGHT,0);
		addCommand(getGotoToggleButton(), getGotoTool(),
				MapCommandType.GOTO_COMMAND, ButtonPlacement.RIGHT,0);


		// register dialogs
		for(ICommand it : commands.values()) {
			if (it instanceof IMapTool) {
				IMapTool diskoTool = (IMapTool)it;
				if (diskoTool.getDialog() instanceof JDialog) {
					factory.register((JDialog)diskoTool.getDialog());
				}
			}
			if (it instanceof IHostDiskoTool) {
				IHostDiskoTool hostTool = (IHostDiskoTool)it;
				if (hostTool.getDialog() instanceof JDialog) {
					factory.register((JDialog)hostTool.getDialog());
				}
			}
			if (it instanceof IMapCommand) {
				IMapCommand discoCmd = (IMapCommand)it;
				if (discoCmd.getDialog() != null) {
					factory.register(discoCmd.getDialog());
				}
			}
		}

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
				lineTool = new LineTool(MsoModelImpl.getInstance(),getDrawHostTool().getDialog(),false);
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
				freeHandTool = new FreeHandTool(MsoModelImpl.getInstance(),getDrawHostTool().getDialog(),false);
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
				flankTool = new FlankTool(MsoModelImpl.getInstance(),
						(IToolCollection)getDrawHostTool().getDialog());
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
				splitTool = new SplitTool(MsoModelImpl.getInstance());
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
				puiTool = new POITool(MsoModelImpl.getInstance(),getDrawHostTool().getDialog());
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
				positionTool = new PositionTool(MsoModelImpl.getInstance(),getDrawHostTool().getDialog());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return positionTool;
	}

	public SelectTool getSelectFeatureTool() {
		if (selectFeatureTool == null) {
			try {
				selectFeatureTool = new SelectTool();
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

	private DiskoToolWrapper getZoomInTool() {
		if (zoomInTool == null) {
			try {
				zoomInTool = DiskoToolWrapper.create(
						new ControlsMapZoomInTool(),WrapAction.ONMOUSEUP,true,true);
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

	private DiskoToolWrapper getZoomOutTool() {
		if (zoomOutTool == null) {
			try {
				zoomOutTool = DiskoToolWrapper.create(
						new ControlsMapZoomOutTool(),WrapAction.ONMOUSEUP,true,true);
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

	private DiskoToolWrapper getPanTool() {
		if (panTool == null) {
			try {
				panTool = DiskoToolWrapper.create(
						new ControlsMapPanTool(),WrapAction.ONMOUSEUP,true,true);
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

	private DiskoToolWrapper getZoomInFixedCommand() {
		if (zoomInFixedCommand == null) {
			try {
				zoomInFixedCommand = DiskoToolWrapper.create(
						new ControlsMapZoomInFixedCommand(),WrapAction.ONCLICK,false,false);
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

	private DiskoToolWrapper getZoomOutFixedCommand() {
		if (zoomOutFixedCommand == null) {
			try {
				zoomOutFixedCommand = DiskoToolWrapper.create(
						new ControlsMapZoomOutFixedCommand(),WrapAction.ONCLICK,false,false);
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

	private DiskoToolWrapper getFullExtentCommand() {
		if (fullExtentCommand == null) {
			try {
				fullExtentCommand = DiskoToolWrapper.create(
						new ControlsMapFullExtentCommand(),WrapAction.ONCLICK,false,false);
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

	private DiskoToolWrapper getZoomToLastExtentForwardCommand() {
		if (zoomToLastExtentForwardCommand == null) {
			try {
				zoomToLastExtentForwardCommand = DiskoToolWrapper.create(
						new ControlsMapZoomToLastExtentForwardCommand(),WrapAction.ONCLICK,false,false);
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

	private DiskoToolWrapper getZoomToLastExtentBackwardCommand() {
		if (zoomToLastExtentBackwardCommand == null) {
			try {
				zoomToLastExtentBackwardCommand = DiskoToolWrapper.create(
						new ControlsMapZoomToLastExtentBackCommand(),WrapAction.ONCLICK,false,false);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return zoomToLastExtentBackwardCommand;
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

	private EraseTool getEraseTool() {
		if (eraseTool == null) {
			try {
				eraseTool = new EraseTool();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return eraseTool;
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
				eraseToggleButton = (JToggleButton)getEraseTool().getButton();
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
				zoomInToggleButton = (JToggleButton)getZoomInTool().getButton();
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
				zoomOutToggleButton = (JToggleButton)getZoomOutTool().getButton();
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
				panToggleButton = (JToggleButton)getPanTool().getButton();
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
				zoomInFixedButton = (JButton)getZoomInFixedCommand().getButton();
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
				zoomOutFixedButton = (JButton)getZoomOutFixedCommand().getButton();
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
				fullExtentButton = (JButton)getFullExtentCommand().getButton();
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
				zoomToLastExtentForwardButton = (JButton)getZoomToLastExtentForwardCommand().getButton();
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
				zoomToLastExtentBackwardButton = (JButton)getZoomToLastExtentBackwardCommand().getButton();
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
				eraseToggleButton = (JToggleButton)getEraseTool().getButton();
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

	public State save() {
		// forward
		return new State(this);
	}

	public void load(State state) {
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
			IDiskoMap map = Utils.getApp().getCurrentMap();
			// load
			Iterator<ICommand> commandIter = commands.values().iterator();
			Iterator<AbstractButton> buttonIter  = buttons.values().iterator();
			// loop over all keys
			while (commandIter.hasNext() && buttonIter.hasNext()) {
				// get command
				ICommand command = commandIter.next();
				if (command != null) {
					// get key and button
					AbstractButton b = buttonIter.next();
					// has current wp a map?
					if (map != null) {
						// create command
						command.onCreate(map);
						// activate button?
						if (b.isSelected()) {
							isAnySelected=true;
						}
					}
					else {
						if (command instanceof IMapTool) {
							((IMapTool)command).deactivate();
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
				standardButton.requestFocusInWindow();
			}
			for(AbstractButton b : buttons.values()) {
				// set focus?
				if(b.isSelected()) {
					b.requestFocusInWindow();
					break;
				}
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void hideDialogs() {
		Iterator<ICommand> commandIter = commands.values().iterator();
		while (commandIter.hasNext()) {
			ICommand command = commandIter.next();
			if (command instanceof IMapTool) {
				IMapTool diskoTool = (IMapTool)command;
				if (diskoTool.getDialog() != null) {
					diskoTool.getDialog().setVisible(false);
				}
			}
			if (command instanceof IHostDiskoTool) {
				IHostDiskoTool hostTool = (IHostDiskoTool)command;
				if (hostTool.getDialog() instanceof JDialog) {
					((JDialog)hostTool.getDialog()).setVisible(false);
				}
			}
			if (command instanceof IMapCommand) {
				IMapCommand discoCmd = (IMapCommand)command;
				if (discoCmd.getDialog() != null) {
					discoCmd.getDialog().setVisible(false);
				}
			}
		}
	}

	public void addActionListener(ActionListener listener) {
		listeners.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener) {
		listeners.remove(ActionListener.class, listener);
	}

	protected void fireAction(ActionEvent e) {
		ActionListener[] list = listeners.getListeners(ActionListener.class);
		for(int i=0;i<list.length;i++) {
			list[i].actionPerformed(e);
		}
	}

	@Override
	public void setVisible(boolean isVisible) {
		// hide dialogs?
		if(!isVisible) hideDialogs();
		// synchronize button with menu state
		menuToggleButton.setSelected(isVisible);
		// forward
		super.setVisible(isVisible);
	}

	public void unselectAll() {
		getDummyToggleButton().doClick(); // HACK: unselect all toggle buttons
	}

	public AbstractButton getButton(Enum<?> key) {
		return (AbstractButton)buttons.get(key);
	}

	public void addCommand(AbstractButton button, ICommand command, Enum<?> e, Enum<?> buttonPlacement, int options) {
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
			button.addActionListener(new NavActionListener(command,options));
		}
		DiskoIcon icon =
			new DiskoIcon(DiskoIconFactory.getIcon(
					DiskoEnumFactory.getIcon(e),"48x48"));
		if (icon != null) {
			button.setIcon(icon);
		}
		button.setToolTipText(DiskoEnumFactory.getText(e,"tooltip"));
	}

	public List<Enum<?>> getEnabledButtons(boolean isEnabled){
		List<Enum<?>> myInterest = Utils.getListNoneOf(MapToolType.class);
		Iterator<Enum<?>> it = buttons.keySet().iterator();
		while (it.hasNext()) {
			Enum<?> key = it.next();
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
			Enum<?> key = it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			ICommand command = (ICommand)commands.get(key);
			// is hosted?
			if(command instanceof IMapTool) {
				IMapTool tool = (IMapTool)command;
				if(tool.isHosted()) {
					((IToolCollection)tool.getDialog())
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
		List<Enum<?>> myInterest = Utils.getListNoneOf(MapToolType.class);
		Iterator<Enum<?>> it = buttons.keySet().iterator();
		while (it.hasNext()) {
			Enum<?> key = it.next();
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
			Enum<?> key = (Enum<?>)it.next();
			AbstractButton button = (AbstractButton)buttons.get(key);
			ICommand command = (ICommand)commands.get(key);
			// is hosted?
			if(command instanceof IMapTool) {
				IMapTool tool = (IMapTool)command;
				if(tool.isHosted()) {
					((IToolCollection)tool.getDialog())
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
		IHostDiskoTool host = (IHostDiskoTool)commands.get(MapToolType.DRAW_HOST_TOOL);
		// update
		((IToolCollection)host.getDialog()).setup();
	}

	public boolean activeCommand(String name, boolean setFocus) {
		return true;
	}

	class NavActionListener implements ActionListener {

		private int options = 0;
		private IMapTool tool;
		private ICommand command;

		NavActionListener(Object command, int options) {
			// prepare
			this.options = options;
			// get tool or command
			if (command instanceof DiskoToolWrapper) {
				// cast to DiskoToolWrapper
				DiskoToolWrapper w = (DiskoToolWrapper)command;
				// is command?
				if(w.isCommand())
					this.command = (ICommand)w;
				// is tool?
				if(w.isTool())
					this.tool = (IMapTool)w;
			}
			else if(command instanceof IMapTool)
				this.tool = (IMapTool)command;
			else if(command instanceof ICommand)
				this.command = (ICommand)command;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				// translate into action
				if(tool!=null) {
					IDiskoMap map = Utils.getApp().getCurrentMap();
					if(map!=null)
						map.setActiveTool(tool,options);
				}
				else if(command!=null) {
					command.onClick();
				}
			} catch (AutomationException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			} catch (IOException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			// forward
			fireAction(e);
		}
	}

	public class State {

		private Hashtable<Enum<?>, ButtonState> m_buttons;
		private Hashtable<Enum<?>, IMapToolState> m_toolStates;
		private Hashtable<Enum<?>, IDiskoCommandState> m_cmdStates;

		private State(NavMenu bar) {
			// initialize
			m_toolStates = new Hashtable<Enum<?>, IMapToolState>();
			m_cmdStates = new Hashtable<Enum<?>, IDiskoCommandState>();
			m_buttons = new Hashtable<Enum<?>,ButtonState>();
			// forward
			save(bar);
		}

		private void save(NavMenu bar) {
			// erase current
			m_toolStates.clear();
			m_cmdStates.clear();
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
				if(cmd instanceof IMapTool) {
					// get tool
					IMapTool tool = (IMapTool)cmd;
					// get state
					IMapToolState state = tool.save();
					// override isVisible?
					if(tool.isHosted()) {
						// override
						buttonState.m_isVisible =
							((IToolCollection)tool.getDialog())
								.getVisible(tool.getType());
					}
					// put to command hashtable?
					if(state!=null)
						m_toolStates.put(key, state);
				}
				// is a disko host tool?
				else if(cmd instanceof IHostDiskoTool) {
					// get host tool
					IHostDiskoTool tool = (IHostDiskoTool)cmd;
					// get state
					IMapToolState state = tool.save();
					// put to command hashtable?
					if(state!=null)
						m_toolStates.put(key, state);
				}
				// is a disko host tool?
				else if(cmd instanceof IMapCommand) {
					// get host tool
					IMapCommand diskoCmd = (IMapCommand)cmd;
					// get state
					IDiskoCommandState state = diskoCmd.save();
					// put to command hashtable?
					if(state!=null)
						m_cmdStates.put(key, state);
				}
			}
		}

		private void load(NavMenu bar) {
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
				if(cmd instanceof IMapTool) {
					// load tool
					((IMapTool)cmd).load(m_toolStates.get(key));
					// cast to IDiskoTool
					IMapTool tool = (IMapTool)cmd;
					// is a hosted tool?
					if(tool.isHosted()) {
						// transfer state to draw dialog
						((IToolCollection)tool.getDialog())
							.setVisible(tool.getType(),button.isVisible());
						// buttons of hosted tools should not
						// NEVER be shown on the navbar!
						button.setVisible(false);
					}
				}
				// is a disko host tool?
				else if(cmd instanceof IHostDiskoTool) {
					// load tool
					((IHostDiskoTool)cmd).load(m_toolStates.get(key));
				}
				// is a disko host tool?
				else if(cmd instanceof IMapCommand) {
					// load tool
					((IMapCommand)cmd).load(m_cmdStates.get(key));
				}
			}
		}

	};

}


