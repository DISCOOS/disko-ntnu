package org.redcross.sar.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.tool.IMapTool;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

public class BaseToolPanel extends AbstractToolPanel {

	private static final long serialVersionUID = 1L;

	public static final int HORIZONTAL_SCROLLBAR_ALWAYS = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	public static final int HORIZONTAL_SCROLLBAR_AS_NEEDED = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	public static final int HORIZONTAL_SCROLLBAR_NEVER = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
	public static final int VERTICAL_SCROLLBAR_ALWAYS = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
	public static final int VERTICAL_SCROLLBAR_AS_NEEDED = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
	public static final int VERTICAL_SCROLLBAR_NEVER = JScrollPane.VERTICAL_SCROLLBAR_NEVER;
	private int m_isMarked = 0;

	private Insets m_insets;

	private Color oldBorderColor;
	private ButtonSize buttonSize = ButtonSize.SMALL;

	private HeaderPanel captionPanel;
	private HeaderPanel actionsPanel;

	private JScrollPane scrollPane;
	private Component bodyComponent;

	private boolean isFitBodyOnResize = false;

	private ComponentListener componentListener;

	/* ===========================================
	 * Constructors
	 * =========================================== */

	public BaseToolPanel(String caption, IMapTool tool) {
		this(caption,tool,ButtonSize.SMALL);
	}

	public BaseToolPanel(IMapTool tool, ButtonSize buttonSize) {
		this("",tool,buttonSize);
	}

	public BaseToolPanel(IMapTool tool) {
		// forward
		this(tool.getCaption(),tool);
	}

	public BaseToolPanel(String caption, IMapTool tool, ButtonSize buttonSize) {

		// forward
		super(caption,tool);

		// prepare
		this.buttonSize = buttonSize;

		// initialize GUI
		initialize();

		// set caption
		getCaptionPanel().setCaptionText(caption);

	}

	/* ===========================================
	 * Private methods
	 * =========================================== */

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {

		// prepare
		setBorder(null);

		// set box layout
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		// set empty border
		setInsets(5,5,5,5);

		// initialize body component
		JPanel panel = new JPanel();
		panel.setBorder(null);
		setBodyComponent(panel);

		// build default panel
		add(Box.createVerticalStrut(5));
		add(getCaptionPanel());
		add(Box.createVerticalStrut(5));
		add(getActionsPanel());
		add(Box.createVerticalStrut(5));
		add(getScrollPane());
		add(Box.createVerticalStrut(5));
	}

	/**
	 * This method initialized the scroll pane
	 *
	 * @return {@link JScrollPane}
	 */
	public JScrollPane getScrollPane() {
		if(scrollPane==null) {
			scrollPane = new JScrollPane(getBodyComponent());
			scrollPane.setOpaque(true);
			scrollPane.setBorder(null);
			scrollPane.setViewportBorder(null);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, UIFactory.createCorner());
			scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, UIFactory.createCorner());
		}
		return scrollPane;
	}

	private HeaderPanel getCaptionPanel() {
		if (captionPanel == null) {
			try {
				captionPanel = new HeaderPanel("",ButtonSize.SMALL);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return captionPanel;
	}

	private HeaderPanel getActionsPanel() {
		if (actionsPanel == null) {
			try {
				actionsPanel = new HeaderPanel("Utfør",ButtonSize.SMALL);
				actionsPanel.addWorkEventListener(new IWorkFlowListener() {

					public void onFlowPerformed(WorkFlowEvent e) {
						// is dirty?
						if(e.isChange())
							setDirty(true);
						else
							setDirty(false);
					}

				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return actionsPanel;
	}

	/* ===========================================
	 * Public methods
	 * =========================================== */

	/**
	 * Button size that should be used when adding and inserting buttons
	 */
	public ButtonSize getButtonSize() {
		return buttonSize;
	}

	public boolean isScrollBarVisible(String scrollbar) {
		if(JScrollPane.HORIZONTAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getHorizontalScrollBar().isVisible();
		}
		if(JScrollPane.VERTICAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getVerticalScrollBar().isVisible();
		}
		return false;
	}

	public void setNotScrollBars() {
		getScrollPane().setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);
		getScrollPane().setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
	}

	public void setDefaultScrollBarPolicies() {
		getScrollPane().setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		getScrollPane().setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	public void setScrollBarPolicies(int vert, int horz) {
		getScrollPane().setVerticalScrollBarPolicy(vert);
		getScrollPane().setHorizontalScrollBarPolicy(horz);
	}

	public Component getBodyComponent() {
		return bodyComponent;
	}

	public boolean isFitBodyOnResize() {
		return isFitBodyOnResize;
	}

	public void setFitBodyOnResize(boolean isFitBodyOnResize) {
		if(this.isFitBodyOnResize != isFitBodyOnResize) {
			if(componentListener!=null) {
				componentListener = null;
				removeComponentListener(componentListener);
			}
			this.isFitBodyOnResize = isFitBodyOnResize;
			if(isFitBodyOnResize) {

				componentListener = new ComponentAdapter() {

					public void componentResized(ComponentEvent e) {

                    	/* ===================================================
                    	 * Reentry prevention
                    	 * ---------------------------------------------------
                    	 * This event is not fired in loop with the
                    	 * code that invoked the resize. Thus, any changes
                    	 * made to this component size by descendants
                    	 * (children) will result in a infinite loop of
                    	 * componentResized events. This occurs if any
                    	 * of the requestFitXxxx methods are called and
                    	 * isFitBodyOnResize is true. requestFitXxxx methods
                    	 * invoke either setSize, setPreferred, setMinimum
                    	 * or setMaximum, or all of these on on this component,
                    	 * which in turn will fire componentResized
                    	 * asynchronously.
                    	 *
                    	 * By checking if the resize source is a descendant,
                    	 * this loop can be broken.
                    	 *
                    	 * =================================================== */

                        if(SwingUtilities.isDescendingFrom((Component)e.getSource(), BaseToolPanel.this)) {
                            return;
                        }
                        
                        // 
					}
				};
				addComponentListener(componentListener);
			}
			if(isFitBodyOnResize) {
				setNotScrollBars();
				fitBodyToView();
			}
			else {
				setDefaultScrollBarPolicies();
				fitBodyToAny();
			}
		}
	}

	public void setPreferredBodySize(Dimension size) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).setPreferredSize(size);
	}

	public Dimension getMinimumBodySize() {
		if(bodyComponent instanceof JComponent)
			return ((JComponent)bodyComponent).getMinimumSize();
		return null;
	}

	public void setBodyLayout(LayoutManager manager) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).setLayout(manager);
	}

	public void setBodyBorder(Border border) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).setBorder(border);
	}

	public void addBodyChild(Component c) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).add(c);
	}

	public void addBodyChild(Component c, int property) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).add(c,property);
	}

	public void addBodyChild(Component c, Object property) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).add(c,property);
	}

	public void setBodyComponent(Component body) {
		// update viewport
		getScrollPane().setViewportView(body);
		// update hool
		bodyComponent = body;
	}

	/**
	 * This method gets the caption icon
	 *
	 * @return Icon
	 */
	public Icon getCaptionIcon() {
		return getCaptionPanel().getCaptionIcon();
	}

	/**
	 * This method sets the caption icon
	 *
	 */
	public void setCaptionIcon(Icon icon) {
		getCaptionPanel().setCaptionIcon(icon);
	}

	/**
	 * This method gets the caption text
	 *
	 * @return String
	 */
	public String getCaptionText() {
		return getCaptionPanel().getCaptionText();
	}

	/**
	 * This method sets the caption text
	 *
	 */
	public void setCaptionText(String caption) {
		getCaptionPanel().setCaptionText(caption);
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		getCaptionPanel().setCaptionColor(foreground,background);
	}

	public void setCaptionVisible(boolean isVisible) {
		getCaptionPanel().setVisible(isVisible);
	}

	public boolean isCaptionVisible() {
		return getCaptionPanel().isVisible();
	}

	/**
	 * This method gets the actions icon
	 *
	 * @return Icon
	 */
	public Icon getActionsIcon() {
		return getActionsPanel().getCaptionIcon();
	}

	/**
	 * This method sets the actions icon
	 *
	 */
	public void setActionsIcon(Icon icon) {
		getActionsPanel().setCaptionIcon(icon);
	}

	/**
	 * This method gets the actions text
	 *
	 * @return String
	 */
	public String getActionsText() {
		return getActionsPanel().getCaptionText();
	}

	/**
	 * This method sets the actions text
	 *
	 */
	public void setActionsText(String caption) {
		getActionsPanel().setCaptionText(caption);
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setActionsColor(Color foreground,Color background) {
		getActionsPanel().setCaptionColor(foreground,background);
	}

	public void setActionsVisible(boolean isVisible) {
		getActionsPanel().setVisible(isVisible);
	}

	public boolean isActionsVisible() {
		return getActionsPanel().isVisible();
	}

	/**
	 * This method sets the border color
	 *
	 */
	public Color setBorderColor(Color color) {
		Color old = getBorderColor();
		getCaptionPanel().setBorderColor(color);
		getActionsPanel().setBorderColor(color);
		return old;
	}

	public Color getBorderColor() {
		return getCaptionPanel().getBorderColor();
	}

	public Insets getInsets() {
		return m_insets;
	}

	public void setInsets(int l, int t, int r, int b) {
		m_insets = new Insets(t,l,b,r);
		setBorder(BorderFactory.createEmptyBorder(
				m_insets.left, m_insets.top,
				m_insets.right, m_insets.bottom));
	}

	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		return getActionsPanel().insertButton(before,button,command);
	}

	public AbstractButton insertButton(String before, String command, String caption) {
		return getActionsPanel().insertButton(before, command, caption);
	}

	public AbstractButton addButton(AbstractButton button, String command) {
		return getActionsPanel().addButton(button,command);
	}

	public AbstractButton addButton(String command, String caption) {
		return getActionsPanel().addButton(command, caption);
	}

	public void removeButton(String command) {
		getActionsPanel().removeButton(command);
	}

	public boolean addItem(JComponent item) {
		return getActionsPanel().addItem(item);
	}

	public boolean insertItem(String before, JComponent item) {
		return getActionsPanel().insertItem(before,item);
	}

	public boolean insertItem(JComponent before, JComponent item) {
		return getActionsPanel().insertItem(before,item);
	}

	public boolean removeItem(JComponent item) {
		return getActionsPanel().removeItem(item);
	}

	public boolean containsButton(String command) {
		return getActionsPanel().containsButton(command);
	}

	public AbstractButton getButton(String command) {
		return getActionsPanel().getButton(command);
	}

	public boolean isButtonVisible(String command) {
		return getActionsPanel().isButtonVisible(command);
	}

	public void setButtonVisible(String command, boolean isVisible) {
		getActionsPanel().setButtonVisible(command,isVisible);
	}

	public boolean isButtonEnabled(String command) {
		return getActionsPanel().isButtonEnabled(command);
	}

	public void setButtonEnabled(String command, boolean isEnabled) {
		getActionsPanel().setButtonEnabled(command,isEnabled);
	}

	public void addAction(String command) {
		getActionsPanel().addAction(command);
	}

	public void removeAction(String command) {
		getActionsPanel().removeAction(command);
	}

	public boolean doAction(String command) {
		return getActionsPanel().doAction(command);
	}

  	@Override
	public void setChangeable(boolean isChangeable) {
  		// forward
  		super.setChangeable(isChangeable);
  		// loop over all children?
  		if(getBodyComponent() instanceof JComponent) {
  			// cast to JComponent
  			JComponent c = (JComponent)getBodyComponent();
  			// loop over all children
  	  		for(int i=0; i < c.getComponentCount(); i++)
  	  			// implements IChangeable?
  	  			if(c.getComponent(i) instanceof IChangeable) {
  	  				((IChangeable)c.getComponent(i)).setChangeable(isChangeable);
  	  			}
  		}
	}

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		if(m_isMarked != isMarked) {
			m_isMarked = isMarked;
			oldBorderColor = setBorderColor(isMarked>0 ? Color.BLUE : oldBorderColor);
		}
	}

	/* ===========================================
	 * IPanel implementation
	 * =========================================== */

	public void update() { /* Override this */ }

	public IPanelManager getManager() {
		return getActionsPanel().getManager();
	}

	public void setManager(IPanelManager manager, boolean isMainPanel) {
		getActionsPanel().setManager(manager, isMainPanel);
        // update all IPanels in body
        if(getBodyComponent() instanceof JComponent) {
        	JComponent c = (JComponent)getBodyComponent();
        	for(Component it : c.getComponents()) {
        		if(it instanceof IPanel) {
        			((IPanel)it).setManager(manager, false);
        		}
        	}
        }
	}

	public void addActionListener(ActionListener listener) {
		getActionsPanel().addActionListener(listener);
	}

	public void removeActionListener(ActionListener listener) {
		getActionsPanel().removeActionListener(listener);
	}

	public void addWorkFlowListener(IWorkFlowListener listener) {
		getActionsPanel().addWorkEventListener(listener);
	}

	public void removeWorkFlowListener(IWorkFlowListener listener) {
		getActionsPanel().removeWorkEventListener(listener);
	}

	/* ===========================================
	 * IToolPanel implementation
	 * =========================================== */

	public IMapTool getTool() {
		return super.getTool();
	}

	/* ===========================================
	 * ActionListener implementation
	 * =========================================== */

	public void actionPerformed(ActionEvent e) {
		getActionsPanel().actionPerformed(e);
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected Dimension fitBodyToView() {
		Dimension d = getScrollPane().getViewport().getSize();
		Utils.setFixedSize(getBodyComponent(), d.width, d.height);
		return d;
	}

    protected Dimension fitBodyToPreferredLayoutSize() {
    	Dimension d = null;
    	if(getBodyComponent() instanceof JComponent) {
    		JComponent c = (JComponent)getBodyComponent();
    		d = c.getLayout().preferredLayoutSize(c);
    		c.setSize(d);
    		c.setPreferredSize(d);
    	}
    	return d;
    }

    protected Dimension fitBodyToMinimumLayoutSize() {
    	Dimension d = null;
    	if(getBodyComponent() instanceof JComponent) {
    		JComponent c = (JComponent)getBodyComponent();
    		d = c.getLayout().minimumLayoutSize(c);
    		c.setSize(d);
    		c.setPreferredSize(d);
    	}
    	return d;
    }

    protected Dimension fitBodyToAny() {
		Dimension d = getScrollPane().getViewport().getSize();
		Utils.setAnySize(getBodyComponent(), d.width, d.height);
		return d;
	}

    protected Dimension fitThisToPreferredBodySize() {
        Dimension d = getBodyComponent().getPreferredSize();
        int w = d.width;
        int h = d.height + 10 +
        	getCaptionPanel().getPreferredSize().height +
        	getActionsPanel().getPreferredSize().height +
        	(isScrollBarVisible(JScrollPane.HORIZONTAL_SCROLLBAR)
					? getScrollPane().getHorizontalScrollBar().getHeight() : 0);

        setSize(new Dimension(w,h));
        setPreferredSize(new Dimension(w,h));
        return d;
    }

    protected Dimension fitThisToMinimumBodySize() {
        Dimension d = getBodyComponent().getMinimumSize();
        int w = d.width;
        int h = d.height + 10 +
        	getCaptionPanel().getMinimumSize().height +
        	getActionsPanel().getMinimumSize().height +
        	(isScrollBarVisible(JScrollPane.HORIZONTAL_SCROLLBAR)
					? getScrollPane().getHorizontalScrollBar().getHeight() : 0);

        setSize(new Dimension(w,h));
        setPreferredSize(new Dimension(w,h));
        return d;
    }

	protected boolean requestFitToContent(boolean fitThisToBody) {
    	if(fitThisToBody) fitThisToPreferredBodySize();
    	return super.requestFitToContent();
	}

	protected void fireActionEvent(ActionEvent e) {
		getActionsPanel().actionPerformed(e);
	}

	protected void fireOnWorkFinish(Object source, Object data) {
		getActionsPanel().fireOnWorkFinish(source, data);
    }

	protected void fireOnWorkCancel(Object source, Object data) {
		getActionsPanel().fireOnWorkCancel(source, data);
    }

	protected void fireOnWorkChange(Object source, Object data) {
		getActionsPanel().fireOnWorkChange(source,data);
	}

	protected void fireOnWorkPerformed(WorkFlowEvent e){
    	getActionsPanel().fireOnWorkPerformed(e);
	}


}  //  @jve:decl-index=0:visual-constraint="10,10"
