package org.redcross.sar.gui.map;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.HashMap;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.DiskoDialog;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.command.IHostDiskoTool;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.map.command.IDrawTool;
import org.redcross.sar.map.command.IDiskoTool.DiskoToolType;
import org.redcross.sar.map.command.IDrawTool.FeatureType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JSplitPane;

/**
 * @author kennetgu
 *
 */
public class DrawDialog extends DiskoDialog  implements IDrawToolCollection, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private DefaultDiskoPanel m_contentPanel = null;
	private JPanel m_buttonsPanel = null;
	private JPanel m_propertyPanels = null;
	private ButtonGroup m_buttonGroup = null;	
	private JSplitPane m_splitPane = null;
	private IHostDiskoTool m_hostTool = null;
	
	private IDiskoMap m_map = null;
	
	private IDrawTool m_selectedTool = null;
	
	private HashMap<DiskoToolType, IDrawTool> m_tools = null;
	private HashMap<DiskoToolType, IPropertyPanel> m_panels = null;
	private HashMap<DiskoToolType, JToggleButton> m_buttons = null;

	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public DrawDialog(Frame owner) {
		
		// forward
		super(owner);
		
		// prepare
		m_tools = new HashMap<DiskoToolType, IDrawTool>();
		m_panels = new HashMap<DiskoToolType, IPropertyPanel>();
		m_buttons = new HashMap<DiskoToolType, JToggleButton>();
		
		// initialize GUI
		initialize();
		
	}

	private void initialize() {
		try {
			// create button group
			m_buttonGroup = new ButtonGroup();
			// prepare dialog
	        this.setContentPane(getContentPanel());
			this.setPreferredSize(new Dimension(300,500));
	        this.pack();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method initializes m_contentPanel	
	 * 	
	 * @return {@link DefaultDiskoPanel}	
	 */
	private DefaultDiskoPanel getContentPanel() {
		if (m_contentPanel == null) {
			m_contentPanel = new DefaultDiskoPanel("Tegneverktøy",false,false);
			m_contentPanel.setBodyComponent(getSplitPane());
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
			m_buttonsPanel.setBorder(null);
			m_buttonsPanel.setLayout(fl);
			Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.NORMAL);
			m_buttonsPanel.setPreferredSize(new Dimension(300, dim.height+10));
		}
		return m_buttonsPanel;
	}

	/**
	 * This method initializes m_propertyPanels	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPropertyPanels() {
		if (m_propertyPanels == null) {
			m_propertyPanels = new JPanel();
			m_propertyPanels.setBorder(null);
			m_propertyPanels.setLayout(new CardLayout());
			m_propertyPanels.setPreferredSize(new Dimension(200, 350));
			m_propertyPanels.add(new JLabel("<html><center>&ltIngen egenskaper&gt</center></html>"),"message");
		}
		return m_propertyPanels;
	}
	
	
	/**
	 * This gets current snap dialog
	 * 	
	 * @return {@link SnapDialog}
	 */
	private SnapDialog getSnapDialog() {
		// initialize
		SnapDialog dialog = null;
		// has map?
		if (m_map!=null && m_map.isEditSupportInstalled()) {
			try {
				// get dialog
				dialog = m_map.getSnapDialog();
				// force dialog to snap east to of this dialog 
				dialog.setLocationRelativeTo(this, DiskoDialog.POS_EAST, true, false);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return dialog;
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
			m_splitPane.setBorder(null);
			m_splitPane.setTopComponent(getButtonsPanel());
			m_splitPane.setBottomComponent(getPropertyPanels());
			m_splitPane.setPreferredSize(new Dimension(200, 350));
		}
		return m_splitPane;
	}
	
	private boolean selectTool(IDrawTool tool, boolean activate, boolean force) {
		// reset old tool?
		if(m_selectedTool!=null && m_selectedTool.isDirty()) {
			// prompt user?
			if(!force && !m_selectedTool.isInterchangable(tool.getFeatureType())) {
				// prompt
				int ans = Utils.showConfirm("Bekreftelse", "Endringer vil gå tapt. Vil du fortsette?", JOptionPane.YES_NO_OPTION);
				// canceled?
				if(ans == JOptionPane.NO_OPTION) return false;
				// reset tool
				m_selectedTool.reset();
			}
		}
		// save
		m_selectedTool = tool;
		// toggle tool button on draw dialog if not selected
		JToggleButton toggle = m_buttons.get(tool.getType());
		// select active tool?
		if(!toggle.isSelected())
			toggle.setSelected(true);
		// get tool panel
		Component panel = (Component)m_panels.get(tool.getType());
		// update property panel view state
		showPropertyPanel(panel!=null ? tool.getType() : "message");
		// activate button?
		if (activate){
			tool.getButton().doClick();
		}
		// setup host tool?
		if(m_hostTool!=null) {
			// update host tool
			m_hostTool.setTool(tool,false);
			// get button
			AbstractButton button = m_hostTool.getButton();
			// update icon and tooltip text
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
			button.setSelected(activate);
			activate = activate && button.isVisible();
		}
		
		// update tool caption
		getToolCaption();
		
		// selection change
		return true;
		
	}
	
	private void showPropertyPanel(Object key) {
		// show panel from card layout
		CardLayout cl = (CardLayout)getPropertyPanels().getLayout();
		cl.show(getPropertyPanels(), key.toString());
	}	
	
	/*==========================================================
	 * IToolCollection interface
	 *========================================================== 
	 */
	
	public IHostDiskoTool getHostTool() {
		return m_hostTool;
	}

	public void setHostTool(IHostDiskoTool tool) {
		// alredy set?
		if(m_hostTool==null)
			m_hostTool = tool;
	}
	
	public IDrawTool getSelectedTool() {
		return m_selectedTool;
	}
	
	public void setSelectedTool(IDiskoTool tool, boolean activate) {
		// validate
		if(!(tool instanceof IDrawTool))
			throw new IllegalArgumentException("Only tools implementing the " +
					"IDrawTool can be selected");
		// forward
		selectTool((IDrawTool)tool,activate,true);
	}	
	
	public boolean containsToolType(DiskoToolType type) {
		return m_tools.containsKey(type);
	}
	
	public void register(IDiskoTool tool) {
		
		// validate
		if(!(tool instanceof IDrawTool))
			throw new IllegalArgumentException("Only tools implementing the " +
					"IDrawTool interface is supported");
		
		// forward
		register((IDrawTool)tool);
	}
	
	public void getToolCaption() {
		if(m_selectedTool!=null) {
			try {
				getContentPanel().setCaptionText("<html>Tegneverktøy - "
						+ m_selectedTool.getCaption() +"</hmtl>"); return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	public void setup() {
		// counters
		int enabledCount = 0;
		int visibleCount = 0;
		
		// loop over all tools and update buttons
		Iterator<IDrawTool> it = m_tools.values().iterator();
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
				if(m_selectedTool!=null) {
					selectTool(m_selectedTool,false,false);
				}
			}
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
			IDrawTool tool = m_tools.get(type);
			// has button?
			if(m_buttons.containsKey(tool.getType())) {
				// get button
				AbstractButton button = m_buttons.get(tool.getType());
				// return state
				return button.isVisible();
			}		
		}
		return false;
	}

	public void setEnabled(DiskoToolType type, boolean isEnabled) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDiskoTool tool = m_tools.get(type);
			// has button?
			if(m_buttons.containsKey(tool.getType())) {
				// get button
				AbstractButton button = m_buttons.get(tool.getType());
				// deselect?
				if(button.isSelected() && !isEnabled)
					button.setSelected(false);
				// set state
				button.setEnabled(isEnabled);
			}		
		}
	}

	public void setVisible(DiskoToolType type, boolean isVisible) {
		// exists?
		if(m_tools.containsKey(type)) {
			// get tool
			IDrawTool tool = m_tools.get(type);
			// has button?
			if(m_buttons.containsKey(tool.getType())) {
				// get button
				AbstractButton button = m_buttons.get(tool.getType());
				// set state
				button.setVisible(isVisible);
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

	public void setBatchUpdate(boolean isBatchUpdate) {
		Iterator<IDrawTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			tools.next().setBatchUpdate(isBatchUpdate);
		}		
	}
	
	
	public void setAttribute(Object value, String attribute) {
		Iterator<IDrawTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			tools.next().setAttribute(value,attribute);
		}		
	}
	
	public void setMsoDrawData(IDiskoTool source) {
		Iterator<IDrawTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			IDiskoTool tool = tools.next();
			if(tool!=source)
				tool.setMsoData(source);
		}		
	}

	public void setMsoDrawData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
		Iterator<IDrawTool> tools = m_tools.values().iterator();
		while(tools.hasNext()) {
			tools.next().setMsoData(msoOwner,msoObject,msoClassCode);
		}		
	}
	
	/*==========================================================
	 * IDrawToolCollection interface
	 *========================================================== 
	 */
	
	public void register(IDiskoMap map) throws IOException {
		// prepare
		this.m_map = map;
		// set location relative to map?
		if(map instanceof JComponent) {
			this.setLocationRelativeTo((JComponent)map, DiskoDialog.POS_WEST,true,true);
		}
	}
	

	public void register(final IDrawTool tool) {
		
		// can register?
		if(m_panels.containsKey(tool.getType())) 
			throw new IllegalArgumentException("Tool is already added");
		
		// register this tool in dialog...
		try {
			
			// add separator?
			if(m_buttonsPanel.getComponentCount()==1) {
				m_buttonsPanel.add(new JSeparator(JSeparator.HORIZONTAL),null);
			}		
			
			// create toggle button 
			JToggleButton button = DiskoButtonFactory.createToggleButton(ButtonSize.NORMAL);
			button.setIcon(tool.getButton().getIcon());
			button.setToolTipText(tool.getButton().getToolTipText());
			m_buttonsPanel.add(button,null);
			
			// add action listener
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// cancel?
					if(!selectTool(tool,true,false)) {
						// reselect old button
						m_buttons.get(m_selectedTool.getType()).setSelected(true);
					}
				}
			});
			
			// put to tool map
			m_tools.put(tool.getType(), tool);
			
			// get property panel
			IPropertyPanel panel = tool.getPropertyPanel();
			
			// register panel?
			if(panel!=null && (panel instanceof Component)) {
				// cast to component
				Component c = (Component)panel;
				// add to card layout of property panels component
				getPropertyPanels().add(c,tool.getType().toString());
				// register this
				panel.addActionListener(this);
			}
			
			// put to panel map
			m_panels.put(tool.getType(), panel);
			
			// add to button group
			m_buttonGroup.add(button);
			
			// put to button map
			m_buttons.put(tool.getType(), button);
			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void enableTools(boolean isEnabled) {
		for(IDiskoTool tool: m_tools.values()) {
			// get button
			AbstractButton b = tool.getButton();
			// enable or diable?
			b.setEnabled(isEnabled);
			m_buttons.get(tool.getType()).setEnabled(isEnabled);
		}		
		setup();
	}
	
	public void enableToolTypes(EnumSet<FeatureType> types) {
		for(IDrawTool tool: m_tools.values()) {
			// get button
			AbstractButton b = tool.getButton();
			// get flag
			boolean isEnabled = types.contains(tool.getFeatureType());
			// enable or diable?
			b.setEnabled(isEnabled);
			m_buttons.get(tool.getType()).setEnabled(isEnabled);
		}
		setup();
	}

	public void enableToolType(DiskoToolType type) {
		for(IDrawTool tool: m_tools.values()) {
			// get button
			AbstractButton b = tool.getButton();
			// get flag
			boolean isEnabled = tool.equals(tool.getType());
			// enable or diable?
			b.setEnabled(isEnabled);
			m_buttons.get(tool.getType()).setEnabled(isEnabled);
		}
		setup();
	}
	
	/*==========================================================
	 * ActionListener interface
	 *========================================================== 
	 */
		
	public void actionPerformed(ActionEvent e) {
		// parse action
		if("editsnap".equalsIgnoreCase(e.getActionCommand())) {
			getSnapDialog().setVisible(!getSnapDialog().isVisible());
		}		
	}
	
}  //  @jve:decl-index=0:visual-constraint="23,0"
