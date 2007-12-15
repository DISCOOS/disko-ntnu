/**
 * 
 */
package org.redcross.sar.gui;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkEventListener;
import org.redcross.sar.map.DiskoMap;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.SnappingAdapter;
import org.redcross.sar.map.command.IDiskoHostTool;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.command.IDrawTool.DrawFeatureType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;

import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.ITool;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

/**
 * @author kennetgu
 *
 */
public class DrawDialog extends DiskoDialog 
			implements IDrawDialog, IHostToolDialog, IDiskoWorkEventListener {

	private static final long serialVersionUID = 1L;
	
	private JPanel m_contentPanel = null;
	private JPanel m_buttonsPanel = null;
	private JPanel m_toolsPanel = null;
	private ButtonGroup m_buttonGroup = null;	
	private SnapPanel m_snapPanel = null;
	private JSplitPane m_splitPane = null;
	private JTabbedPane m_tabbedPane = null;
	private IDiskoHostTool m_hostTool = null;
	private IDiskoMap map = null;
	
	private IDiskoTool m_activeTool = null;
	
	private Icon m_snapIcon = null;
	private Icon m_propsIcon = null;

	private String m_snapTooltipText = null;
	private String m_propsToolTipText = null;

	private HashMap<DiskoToolType, IDiskoTool> m_tools = null;
	private HashMap<DiskoToolType, JPanel> m_panels = null;
	private HashMap<DiskoToolType, JToggleButton> m_buttons = null;

	private SnappingAdapter m_snapping = null;
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public DrawDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// prepare
		m_snapping = new SnappingAdapter();
		m_tools = new HashMap<DiskoToolType, IDiskoTool>();
		m_panels = new HashMap<DiskoToolType, JPanel>();
		m_buttons = new HashMap<DiskoToolType, JToggleButton>();
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// get properties
			m_snapIcon = Utils.getIcon("IconEnum.SNAP.icon");
			m_snapTooltipText = Utils.getProperty("IconEnum.SNAP.text");
			m_propsIcon = Utils.getIcon("IconEnum.PROPERTIES.icon");
			m_propsToolTipText = Utils.getProperty("IconEnum.PROPERTIES.text");
			// create button group
			m_buttonGroup = new ButtonGroup();
			// prepare dialog
	        this.setSize(new Dimension(292, 293));
	        this.setPreferredSize(new Dimension(300, 400));
	        this.setContentPane(getContentPanel());
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getContentPanel() {
		if (m_contentPanel == null) {
			m_contentPanel = new JPanel();
			m_contentPanel.setLayout(new BorderLayout());
			m_contentPanel.setPreferredSize(new Dimension(250, 300));
			m_contentPanel.setBorder(BorderFactory.
					createBevelBorder(BevelBorder.RAISED));
			m_contentPanel.add(getSplitPane(), BorderLayout.CENTER);
		}
		return m_contentPanel;
	}

	/**
	 * This method initializes m_buttonsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getButtonsPanel() {
		if (m_buttonsPanel == null) {
			FlowLayout fl = new FlowLayout();
			fl.setAlignment(FlowLayout.LEFT);
			fl.setVgap(0);
			fl.setHgap(0);
			m_buttonsPanel = new JPanel();
			m_buttonsPanel.setLayout(fl);
			Dimension dim = Utils.getApp().getUIFactory().getSmallButtonSize();
			m_buttonsPanel.setPreferredSize(dim);
		}
		return m_buttonsPanel;
	}

	/**
	 * This method initializes m_toolsPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getToolsPanel() {
		if (m_toolsPanel == null) {
			m_toolsPanel = new JPanel();
			m_toolsPanel.setLayout(new CardLayout());
			m_toolsPanel.setPreferredSize(new Dimension(200, 250));
		}
		return m_toolsPanel;
	}
	
	/**
	 * This method initializes m_snapPanel	
	 * 	
	 * @return org.redcross.sar.gui.SnapPanel
	 */
	private SnapPanel getSnapPanel() {
		if (m_snapPanel == null) {
			try {
				// create panels
				m_snapPanel = new SnapPanel(m_snapping);
				m_snapPanel.setName("m_snapPanel");
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_snapPanel;
	}

	/**
	 * This method initializes m_splitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getSplitPane() {
		if (m_splitPane == null) {
			m_splitPane = new JSplitPane();
			m_splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			m_splitPane.setBottomComponent(getTabbedPane());
			m_splitPane.setTopComponent(getButtonsPanel());
		}
		return m_splitPane;
	}

	/**
	 * This method initializes m_tabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getTabbedPane() {
		if (m_tabbedPane == null) {
			m_tabbedPane = new JTabbedPane();
			m_tabbedPane.addTab(null, m_propsIcon, 
					getToolsPanel(), m_propsToolTipText);
			m_tabbedPane.addTab(null, m_snapIcon, 
					getSnapPanel(), m_snapTooltipText);
		}
		return m_tabbedPane;
	}
	
	
	/*==========================================================
	 * IHostToolDialog interface
	 *========================================================== 
	 */
	
	public IDiskoHostTool getHostTool() {
		return m_hostTool;
	}

	public void setHostTool(IDiskoHostTool tool) {
		// alredy set?
		if(m_hostTool==null)
			m_hostTool = tool;
	}
	
	public IDiskoTool getActiveTool() {
		return m_activeTool;
	}
	
	public void setActiveTool(IDiskoTool tool) {
		// forward?
		if(tool instanceof IDrawTool)
			activateTool((IDrawTool)tool);
	}	
	
	public void register(IDiskoTool tool, JPanel panel) {
		
		// validate
		if(!(tool instanceof IDrawTool))
			throw new IllegalArgumentException("Only tools implementing the " +
					"IDrawTool interface is supported");			
		if(m_panels.containsKey(tool.getType())) 
			throw new IllegalArgumentException("Tool is already added");
		
		// get as final draw tool
		final IDrawTool drawTool = (IDrawTool)tool;
		
		// register in dialog...
		try {
			
			// add separator?
			if(m_buttonsPanel.getComponentCount()==1) {
				m_buttonsPanel.add(new JSeparator(JSeparator.HORIZONTAL),null);
			}		
			
			// add button
			JToggleButton button = new JToggleButton();
			Dimension dim = Utils.getApp().getUIFactory().getSmallButtonSize();
			button.setPreferredSize(dim);
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
			m_buttonsPanel.add(button,null);
			
			// add action listener
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						map.setCurrentToolByRef((ITool)drawTool,false);
						activateTool(drawTool);
					} catch (AutomationException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			// add work listener
			tool.addDiskoWorkEventListener(this);
			
			// put to tool map
			m_tools.put(tool.getType(), tool);
			
			// add panel?
			if(panel!=null)
				m_toolsPanel.add(panel,tool.getType().toString());
			
			// put to panel map
			m_panels.put(drawTool.getType(), panel);
			
			// set tool in snap panel
			getSnapPanel().setTool(drawTool);
			
			// add to button group
			m_buttonGroup.add(button);
			
			// put to button map
			m_buttons.put(drawTool.getType(), button);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// register snapping
		drawTool.setSnappingAdapter(m_snapping);
		
	}
	
	private void activateTool(IDiskoTool tool) {
		// save
		m_activeTool = tool;
		// toggle tool button on draw dialog if not selected
		JToggleButton toggle = m_buttons.get(tool.getType());
		// select active tool?
		if(!toggle.isSelected())
			toggle.setSelected(true);
		// get tool panel
		JPanel panel = m_panels.get(tool.getType());
		// remove panel tab?
		if(panel==null) {
			// remove panel tab?
			if(getTabbedPane().getTabCount()==2)
				getTabbedPane().removeTabAt(0);
			// select snapping tab
			getTabbedPane().setSelectedIndex(0);
		}
		// recreate tabs?
		else if(getTabbedPane().getTabCount()==1) {
			getTabbedPane().removeAll();
			getTabbedPane().addTab(null, m_propsIcon, 
					getToolsPanel(), m_propsToolTipText);
			getTabbedPane().addTab(null, m_snapIcon, 
					getSnapPanel(), m_snapTooltipText);
		}
		// show panel from card layout
		CardLayout cl = (CardLayout)getToolsPanel().getLayout();
		cl.show(getToolsPanel(), tool.getType().toString());		
		// update snap panel tool pointer for 
		// automatic update of snapping data in tool?
		if(tool instanceof IDrawTool)
			getSnapPanel().setTool((IDrawTool)tool);
		// setup host tool?
		if(m_hostTool!=null) {
			// update host tool
			m_hostTool.setTool(tool);
			// get button
			AbstractButton button = m_hostTool.getButton();
			// update icon and tooltip text
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
			if(button.isVisible()) {
				button.setSelected(true);
				button.doClick();
			}
		}
		// is draw tool?
		if(m_activeTool instanceof IDrawTool) {
			// cast to draw tool
			IDrawTool drawTool = ((IDrawTool)m_activeTool);
			// buffer changes if draw dialog is visible. use 
			// tool.apply() to update the mso model
			drawTool.setBuffered(isVisible());
		}
	}
	
	public void setup() {
		// counters
		int enabledCount = 0;
		int visibleCount = 0;
		
		// loop over all tools and update buttons
		Iterator<IDiskoTool> it = m_tools.values().iterator();
		while(it.hasNext()) {
			IDiskoTool tool = it.next();
			AbstractButton button = tool.getButton();
			JToggleButton toggle = m_buttons.get(tool.getType());
			if(toggle.getIcon()==null) {
				toggle.setIcon(button.getIcon());
				toggle.setToolTipText(button.getToolTipText());
			}
			enabledCount += toggle.isEnabled() ? 1 : 0;
			visibleCount += toggle.isVisible() ? 1 : 0;
		}		
		// setup host tool button?
		if(m_hostTool!=null) {
			// get button
			AbstractButton button = m_hostTool.getButton();
			// show?
			button.setVisible(visibleCount>0);
			button.setEnabled(enabledCount>0);
			// reactivate tool?
			if(visibleCount>0) {
				if(m_activeTool!=null) {
					activateTool(m_activeTool);
				}
				else if(m_tools.size()>0) {
					// activate first in array
					activateTool(m_tools.values().iterator().next());
				}	
			}
		}
	}	
	
	
	public void setVisible(boolean isVisible) {
		// forward
		super.setVisible(isVisible);
		// is draw tool?
		if(m_activeTool instanceof IDrawTool) {
			// cast to draw tool
			IDrawTool drawTool = ((IDrawTool)m_activeTool);
			// buffer changes if draw dialog is visible. 
			// if buffered, use tool.apply() to update the mso model
			drawTool.setBuffered(isVisible());
		}
	}
	
	public IDiskoTool getTool(DiskoToolType type) {
		return m_tools.get(type);
	}

	public boolean getEnabled(DiskoToolType type) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDiskoTool tool = m_tools.get(type);
			// supports the IDrawTool interface?
			if(tool instanceof IDrawTool){
				// cast to IDrawTool
				IDrawTool drawTool = (IDrawTool)tool;
				// has button?
				if(m_buttons.containsKey(drawTool.getType())) {
					// get button
					AbstractButton button = m_buttons.get(drawTool.getType());
					// return state
					return button.isEnabled();
				}		
			}
		}
		return false;
	}

	public boolean getVisible(DiskoToolType type) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDiskoTool tool = m_tools.get(type);
			// supports the IDrawTool interface?
			if(tool instanceof IDrawTool){
				// cast to IDrawTool
				IDrawTool drawTool = (IDrawTool)tool;
				// has button?
				if(m_buttons.containsKey(drawTool.getType())) {
					// get button
					AbstractButton button = m_buttons.get(drawTool.getType());
					// return state
					return button.isVisible();
				}		
			}
		}
		return false;
	}

	public void setEnabled(DiskoToolType type, boolean isEnabled) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDiskoTool tool = m_tools.get(type);
			// supports the IDrawTool interface?
			if(tool instanceof IDrawTool){
				// cast to IDrawTool
				IDrawTool drawTool = (IDrawTool)tool;
				// has button?
				if(m_buttons.containsKey(drawTool.getType())) {
					// get button
					AbstractButton button = m_buttons.get(drawTool.getType());
					// deselect?
					if(button.isSelected() && !isEnabled)
						button.setSelected(false);
					// set state
					button.setEnabled(isEnabled);
				}		
			}
		}
	}

	public void setVisible(DiskoToolType type, boolean isVisible) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDiskoTool tool = m_tools.get(type);
			// supports the IDrawTool interface?
			if(tool instanceof IDrawTool){
				// cast to IDrawTool
				IDrawTool drawTool = (IDrawTool)tool;
				// has button?
				if(m_buttons.containsKey(drawTool.getType())) {
					// get button
					AbstractButton button = m_buttons.get(drawTool.getType());
					// set state
					button.setVisible(isVisible);
				}		
			}
		}
	}
	
	public Object getAttribute(DiskoToolType type, String attribute) {
		// exists?
		if(m_tools.containsKey(type)) {
			return m_tools.get(type).getAttribute(attribute);
		}
		return null;
	}

	public void setAttribute(DiskoToolType type, Object value, String attribute) {
		// exists?
		if(m_tools.containsKey(type)) {
			m_tools.get(type).setAttribute(value,attribute);
		}
	}

	public void setAttribute(Object value, String attribute) {
		Iterator<IDiskoTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			tools.next().setAttribute(value,attribute);
		}		
	}
	
	public void setMsoDrawData(IDiskoTool source) {
		Iterator<IDiskoTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			IDiskoTool tool = tools.next();
			if(tool!=source)
				tool.setMsoDrawData(source);
		}		
	}

	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		Iterator<IDiskoTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			tools.next().setMsoDrawData(msoOwner,msoObject,msoClassCode);
		}		
	}
	
	/*==========================================================
	 * IDrawDialog interface
	 *========================================================== 
	 */
	
	public void onLoad(IDiskoMap map) throws IOException {
		this.map = map;
		IDrawTool tool = (IDrawTool)getActiveTool();
		if(tool!=null) {
			// register in snapping adapter
			m_snapping.register((DiskoMap)map);
			// update snap tolerance
			getSnapPanel().setSnapTolerance(m_snapping.getSnapTolerance());
		}
	}
	
	public void setSnapTolerance(int value) {
		getSnapPanel().setSnapTolerance(value);
	}
	
	public List getSnapToLayers() {
		return getSnapPanel().getSnapToLayers();
	}
	
	public void setSnapableLayers(List layers) {
		getSnapPanel().setSnapableLayers(layers);
	}

	public void onWorkCancel(DiskoWorkEvent e) {
		return; // not in use
	}

	public void onWorkChange(DiskoWorkEvent e) {
		// update this tool
		if(e.getSource() instanceof IDiskoTool) {
			IDiskoTool tool = (IDiskoTool)e.getSource();
			setMsoDrawData(tool);
		}
		// notify other disko work event listeners
		fireOnWorkChange(e);
	}

	public void onWorkFinish(DiskoWorkEvent e) {
		return; // not in use
	}
		
	public void setToolSet(MsoClassCode code, Object[] attributes) {
		try {
			
			// dispatch type of data
			if (MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code)) {
				// get poi types
				POIType[] poiTypes = { POIType.GENERAL, POIType.INTELLIGENCE,
						 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
				// set attibutes
				setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
				setAttribute(attributes[0],"ISUPDATEMODE");
				setAttribute(true,"DRAWPOLYGON");
				setAttribute(null,"SEARCHSUBTYPE");
				// enable all tools
				enableToolTypes(EnumSet.allOf(DrawFeatureType.class));
				// set mso draw data
				setMsoDrawData(null, (IMsoObjectIf)attributes[1], MsoClassCode.CLASSCODE_OPERATIONAREA);
			}
			else if (MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
				// get poi types
				POIType[] poiTypes = { POIType.GENERAL, POIType.INTELLIGENCE,
						 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
				// set attibutes
				setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
				setAttribute(attributes[0],"ISUPDATEMODE");
				setAttribute(true,"DRAWPOLYGON");
				setAttribute(null,"SEARCHSUBTYPE");
				// enable all tools
				enableToolTypes(EnumSet.allOf(DrawFeatureType.class));
				// set mso draw data
				setMsoDrawData(null, (IMsoObjectIf)attributes[1], MsoClassCode.CLASSCODE_SEARCHAREA);
			}
			else if (MsoClassCode.CLASSCODE_ROUTE.equals(code)) {
				// get poi types
				POIType[] poiTypes = { POIType.START, POIType.VIA, POIType.STOP };
				// set attibutes
				setAttribute(DiskoToolType.POI_TOOL,poiTypes,"POITYPES");
				setAttribute(attributes[0],"ISUPDATEMODE");
				setAttribute(attributes[1],"DRAWPOLYGON");
				setAttribute(attributes[2],"SEARCHSUBTYPE");
				// enable all tools
				enableToolTypes(EnumSet.allOf(DrawFeatureType.class));
				// set mso draw data
				setMsoDrawData((IMsoObjectIf)attributes[3], (IMsoObjectIf)attributes[4], MsoClassCode.CLASSCODE_ROUTE);
			}
			else if (MsoClassCode.CLASSCODE_POI.equals(code)) {
				// set attibutes
				setAttribute(attributes[0],"ISUPDATEMODE");
				if((Boolean)attributes[1]) {
					POIType[] t1 = { POIType.START, POIType.VIA, POIType.STOP };
					setAttribute(DiskoToolType.POI_TOOL,t1,"POITYPES");
					// enable all tools
					enableToolTypes(EnumSet.allOf(DrawFeatureType.class));
				}
				else {
					POIType[] t2 = { POIType.GENERAL, POIType.INTELLIGENCE,
							 POIType.OBSERVATION, POIType.FINDING, POIType.SILENT_WITNESS };
						setAttribute(DiskoToolType.POI_TOOL,t2,"POITYPES");
					// enable point tools only
					enableToolTypes(EnumSet.of(DrawFeatureType.DRAW_FEATURE_POINT));
				}
				setAttribute(false,"DRAWPOLYGON");
				setAttribute(null,"SEARCHSUBTYPE");
				// set mso draw data
				setMsoDrawData((IMsoObjectIf)attributes[2], (IMsoObjectIf)attributes[3], MsoClassCode.CLASSCODE_POI);
			}
			else if (MsoClassCode.CLASSCODE_UNIT.equals(code)) {
				// set attibutes
				setAttribute(attributes[0],"ISUPDATEMODE");
				setAttribute(false,"DRAWPOLYGON");
				setAttribute(null,"SEARCHSUBTYPE");
				// enable position tools only
				enableToolType(DiskoToolType.POSITION_TOOL);
				// set mso draw data
				setMsoDrawData(null, (IMsoObjectIf)attributes[1], MsoClassCode.CLASSCODE_UNIT);
			}
			else {
				// reset tools
				Iterator<IDiskoTool> tools = m_tools.values().iterator();
				while(tools.hasNext()) {
					IDiskoTool tool = tools.next();
					tool.setMsoOwner(null);
					tool.setMsoObject(null);
				}
				// diable all tools
				enableTools(false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	private void enableTools(boolean isEnabled) {
		for(IDiskoTool tool: m_tools.values()) {
			// get button
			AbstractButton b = tool.getButton();
			// enable or diable?
			b.setEnabled(isEnabled);
			m_buttons.get(tool.getType()).setEnabled(isEnabled);
		}		
	}
	
	private void enableToolTypes(EnumSet<DrawFeatureType> types) {
		for(IDiskoTool it: m_tools.values()) {
			if(it instanceof IDrawTool) {
				// cast to IDrawTool
				IDrawTool tool = (IDrawTool)it;
				// get button
				AbstractButton b = it.getButton();
				// get flag
				boolean isEnabled = types.contains(tool.getFeatureType());
				// enable or diable?
				b.setEnabled(isEnabled);
				m_buttons.get(tool.getType()).setEnabled(isEnabled);
			}
		}
	}

	private void enableToolType(DiskoToolType type) {
		for(IDiskoTool tool: m_tools.values()) {
			// get button
			AbstractButton b = tool.getButton();
			// get flag
			boolean isEnabled = tool.equals(tool.getType());
			// enable or diable?
			b.setEnabled(isEnabled);
			m_buttons.get(tool.getType()).setEnabled(isEnabled);
		}
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
