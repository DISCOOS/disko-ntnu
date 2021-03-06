package org.redcross.sar.gui.dialog;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.HashMap;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.gui.factory.DiskoIconFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.panel.DefaultPanel;
import org.redcross.sar.gui.panel.IToolPanel;
import org.redcross.sar.gui.panel.TogglePanel;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.tool.IMsoTool;
import org.redcross.sar.map.tool.IMapTool;
import org.redcross.sar.map.tool.IDrawTool;
import org.redcross.sar.map.tool.IDrawToolCollection;
import org.redcross.sar.map.tool.IHostDiskoTool;
import org.redcross.sar.map.tool.IMsoToolCollection;
import org.redcross.sar.map.tool.IMapTool.MapToolType;
import org.redcross.sar.map.tool.IDrawTool.FeatureType;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.Utils;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;

/**
 * @author kennetgu
 *
 */
public class DrawDialog extends DefaultDialog implements IDialog, IDrawToolCollection, IMsoToolCollection, ActionListener {

    private static final long serialVersionUID = 1L;

    private TogglePanel m_contentPanel;
    private JPanel m_buttonsPanel;
    private JLabel m_messageLabel;
    private Component m_currentPanel;
    private ButtonGroup m_buttonGroup;
    private IHostDiskoTool m_hostTool;

    private IDiskoMap m_map;

    private IDrawTool m_selectedTool;

    private HashMap<MapToolType, IDrawTool> m_tools;
    private HashMap<MapToolType, IToolPanel> m_panels;
    private HashMap<MapToolType, JToggleButton> m_buttons;

    /**
     * Constructor
     *
     * @param owner
     */
    public DrawDialog(Frame owner) {

        // forward
        super(owner);

        // prepare
        m_tools = new HashMap<MapToolType, IDrawTool>();
        m_panels = new HashMap<MapToolType, IToolPanel>();
        m_buttons = new HashMap<MapToolType, JToggleButton>();

        // initialize GUI
        initialize();

    }

    /*==========================================================
     * IToolCollection interface
     *========================================================== */

    public IHostDiskoTool getHostTool() {
        return m_hostTool;
    }

    public void setHostTool(IHostDiskoTool tool) {
        // already set?
        if(m_hostTool==null)
            m_hostTool = tool;
    }

    public IDrawTool getSelectedTool() {
        return m_selectedTool;
    }

    public void setSelectedTool(IMapTool tool, boolean activate) {
        // validate
        if(!(tool instanceof IDrawTool))
            throw new IllegalArgumentException("Only tools implementing the " +
                    "IDrawTool can be selected");
        // forward
        selectTool((IDrawTool)tool,activate,true);
    }

    public boolean containsToolType(MapToolType type) {
        return m_tools.containsKey(type);
    }

    public void register(IMapTool tool) {

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
                getContentPanel().setCaptionText(m_selectedTool.getCaption()); return;
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
        for(IDrawTool it : m_tools.values()) {
            AbstractButton button = it.getButton();
            JToggleButton toggle = m_buttons.get(it.getType());
            if(toggle.getIcon()==null) {
                toggle.setIcon(getIcon(it.getType(),ButtonSize.SMALL));
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

    public IMapTool getTool(MapToolType type) {
        return m_tools.get(type);
    }

    public boolean getEnabled(MapToolType type) {
        // exists?
        if(m_tools.containsKey(type)) {
            // get tool
            IMapTool tool = m_tools.get(type);
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

    public boolean getVisible(MapToolType type) {
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

    public void setEnabled(MapToolType type, boolean isEnabled) {
        // exists?
        if(m_tools.containsKey(type)) {
            // get tool
            IMapTool tool = m_tools.get(type);
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

    public void setVisible(MapToolType type, boolean isVisible) {
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

    public Object getAttribute(MapToolType type, String attribute) {
        // exists?
        if(m_tools.containsKey(type)) {
            return m_tools.get(type).getAttribute(attribute);
        }
        return null;
    }

    public void setAttribute(MapToolType type, Object value, String attribute) {
        // exists?
        if(m_tools.containsKey(type)) {
            m_tools.get(type).setAttribute(value,attribute);
        }
    }

    public void setBatchUpdate(boolean isBatchUpdate) {
        Iterator<IDrawTool> tools = m_tools.values().iterator();
        while(tools.hasNext()) {
            IDrawTool tool =  tools.next();
            tool.setBatchUpdate(isBatchUpdate);
            tool.resetDirtyFlag();
        }
    }


    public void setAttribute(Object value, String attribute) {
        Iterator<IDrawTool> tools = m_tools.values().iterator();
        while(tools.hasNext()) {
            tools.next().setAttribute(value,attribute);
        }
    }

    public void setMsoData(IMsoTool source) {
        Iterator<IDrawTool> tools = m_tools.values().iterator();
        while(tools.hasNext()) {
            IMapTool tool = tools.next();
            if(tool!=source && tool instanceof IMsoTool)
                ((IMsoTool)tool).setMsoData(source);
        }
    }

    public void setMsoData(IMsoObjectIf msoOwner, IMsoObjectIf msoObject, MsoClassCode msoClassCode) {
        Iterator<IDrawTool> tools = m_tools.values().iterator();
        while(tools.hasNext()) {
            IDrawTool tool = tools.next();
            if(tool instanceof IMsoTool)
                ((IMsoTool)tool).setMsoData(msoOwner,msoObject,msoClassCode);
        }
    }

    /*==========================================================
     * IDrawToolCollection interface
     *========================================================== */

    public void register(IDiskoMap map) throws IOException {
        // prepare
        this.m_map = map;
        // set location relative to map?
        if(map instanceof JComponent) {
            this.setSnapToLocation((JComponent)map, DefaultDialog.POS_WEST, 0, true, false);
        }
    }


    public void register(final IDrawTool tool) {

        // can register?
        if(m_panels.containsKey(tool.getType()))
            throw new IllegalArgumentException("Tool is already added");

        // register this tool in dialog
        try {

            // add separator?
            if(m_buttonsPanel.getComponentCount()==1) {
                m_buttonsPanel.add(new JSeparator(JSeparator.HORIZONTAL),null);
            }

            // create toggle button
            JToggleButton button = DiskoButtonFactory.createToggleButton(ButtonSize.SMALL);
            button.setIcon(getIcon(tool.getType(),ButtonSize.SMALL));
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
            IToolPanel panel = tool.getToolPanel();

            // register panel?
            if(panel!=null && (panel instanceof Component)) {
                // register action listener
                panel.addActionListener(this);
                // register panel manager
                panel.setParentManager(getContentPanel(), false, false);
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
        for(IMapTool tool: m_tools.values()) {
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

    public void enableToolType(MapToolType type) {
        for(IDrawTool tool: m_tools.values()) {
            // get button
            AbstractButton b = tool.getButton();
            // get flag
            boolean isEnabled = tool.equals(tool.getType());
            // enable or disable?
            b.setEnabled(isEnabled);
            m_buttons.get(tool.getType()).setEnabled(isEnabled);
        }
        setup();
    }

    /*==========================================================
     * ActionListener interface
     *========================================================== */

    public void actionPerformed(ActionEvent e) {
        // get action command
        String cmd = e.getActionCommand();
        // parse action
        if("editsnap".equalsIgnoreCase(cmd)) {
        	getSnapDialog().setVisible(!getSnapDialog().isVisible());
        }

    }

    /*==========================================================
     * Private methods
     *========================================================== */

    private void initialize() {
        try {
            // create button group
            m_buttonGroup = new ButtonGroup();
            // prepare dialog
            setContentPane(getContentPanel());
            // show message 
            showPropertyPanel(null);
            // apply
            pack();
            // forward
            setup();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initializes m_contentPanel
     *
     * @return {@link DefaultPanel}
     */
    private TogglePanel getContentPanel() {
        if (m_contentPanel == null) {
            m_contentPanel = new TogglePanel("Tegneverkt�y",false,true,ButtonSize.SMALL);
            m_contentPanel.setNotScrollBars();
            m_contentPanel.setRequestHideOnCancel(true);
            m_contentPanel.setContainerBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            m_contentPanel.setContainerLayout(new BoxLayout(m_contentPanel.getContainer(),BoxLayout.Y_AXIS));
            m_contentPanel.addToContainer(getButtonsPanel());
            m_contentPanel.addToContainer(Box.createVerticalStrut(5));
            m_contentPanel.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {

                	// forward
                	setButtonsPanelSize();

                }

            });
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
            fl.setVgap(1);
            fl.setHgap(1);
            m_buttonsPanel = new JPanel();
            m_buttonsPanel.setBorder(
                    BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4, 4, 4, 4), UIFactory.createBorder()),
                    BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            m_buttonsPanel.setLayout(fl);
        }
        return m_buttonsPanel;
    }

    private JLabel getMessageLabel() {
        if(m_messageLabel == null) {
            m_messageLabel = new JLabel("");
        }
        return m_messageLabel;
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
                dialog.setSnapToLocation(this, DefaultDialog.POS_EAST, 0, false, false);
            } catch (java.lang.Throwable e) {
                e.printStackTrace();
            }
        }
        return dialog;
    }

    private boolean selectTool(IDrawTool tool, boolean activate, boolean force) {

    	// no change?
    	if(m_selectedTool==tool && false) return false;

        // reset old tool?
        if(m_selectedTool!=null && m_selectedTool.isDirty()) {
            // prompt user?
            if(!force && !m_selectedTool.isInterchangable(tool.getFeatureType())) {
                // prompt
                int ans = Utils.showConfirm("Bekreftelse", "Endringer vil g� tapt. Vil du fortsette?", JOptionPane.YES_NO_OPTION);
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
        if(!toggle.isSelected()) {
            toggle.setSelected(true);
        }

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
            button.requestFocusInWindow();
            activate = activate && button.isVisible();
        }

        // update tool caption
        getToolCaption();

        // selection change
        return true;

    }

    private void showPropertyPanel(Object key) {
        // remove current panel?
    	if(m_currentPanel!=null) getContentPanel().removeFromContainer(m_currentPanel);
        // get tool panel
        IToolPanel panel = m_panels.get(key);        
        // found panel?
        if(panel!=null)
        	m_currentPanel = (Component)panel;
        else
        	m_currentPanel = getMessageLabel();
        // add to container
        getContentPanel().addToContainer(m_currentPanel);
        // fit panel to buttons
        setButtonsPanelSize();
        // get preferred size
        Dimension d = getContentPanel().fitThisToPreferredContainerSize();
        // update dialog size
        setSize(d);
        setPreferredSize(d);
        validate();
    }

    private Icon getIcon(Enum<?> type, ButtonSize size) {
        return DiskoIconFactory.getIcon(
                DiskoEnumFactory.getIcon(type),
                DiskoIconFactory.getCatalog(size));
    }

    private void setButtonsPanelSize() {

    	JPanel buttons = getButtonsPanel();
    	Insets insets = buttons.getInsets();
        int max = m_contentPanel.getWidth() - (insets.left+insets.right);

        if(max>0) {

            int w = 0;
            int rows = 1;

            // loop over all tools and update buttons
            for(IDrawTool it : m_tools.values()) {
                JToggleButton b = m_buttons.get(it.getType());
                if(b.isVisible()) {
                    w += b.getWidth();
                    if(w>=max) {
                        w = 0;
                        rows++;
                    }
                }
            }

            // set button panel height
            Dimension dim = DiskoButtonFactory.getButtonSize(ButtonSize.SMALL);
            buttons.setMinimumSize(new Dimension(dim.width+16, dim.height+16));
            buttons.setPreferredSize(new Dimension(dim.width+16, (dim.height+1)*rows+15));
            buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, (dim.height+1)*rows+15));

        }

    }
}
