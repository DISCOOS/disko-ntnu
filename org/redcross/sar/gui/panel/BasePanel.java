package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.redcross.sar.Application;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.event.IToggleListener;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

public class BasePanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;

	public static final String HORIZONTAL_SCROLLBAR = JScrollPane.HORIZONTAL_SCROLLBAR;
	public static final String VERTICAL_SCROLLBAR = JScrollPane.VERTICAL_SCROLLBAR;

	public static final int HORIZONTAL_SCROLLBAR_ALWAYS = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	public static final int HORIZONTAL_SCROLLBAR_AS_NEEDED = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	public static final int HORIZONTAL_SCROLLBAR_NEVER = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
	public static final int VERTICAL_SCROLLBAR_ALWAYS = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
	public static final int VERTICAL_SCROLLBAR_AS_NEEDED = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
	public static final int VERTICAL_SCROLLBAR_NEVER = JScrollPane.VERTICAL_SCROLLBAR_NEVER;

	protected Insets insets;
	protected boolean isBorderVisible = true;
	protected Color borderColor = Color.GRAY;
	protected Color oldBorderColor = Color.GRAY;
	protected ButtonSize buttonSize = ButtonSize.SMALL;

	protected HeaderPanel headerPanel;
	protected JScrollPane scrollPane;
	protected Container container;

	//protected Dimension expandedSize;

	protected int minimumCollapsedHeight;
	protected int preferredExpandedHeight;
	
	// get layout mode
	protected static boolean isTouchMode = Application.getInstance().isTouchMode();


	/* ===========================================
	 * Constructors
	 * ===========================================
	 */

	public BasePanel() {
		this("",ButtonSize.SMALL);
	}

	public BasePanel(String caption) {
		this(caption,ButtonSize.SMALL);
	}

	public BasePanel(ButtonSize buttonSize) {
		this("",buttonSize);
	}

	public BasePanel(String caption,ButtonSize buttonSize) {

		// forward
		super(caption);

		// prepare
		this.buttonSize = buttonSize;
		this.insets = new Insets(1,1,1,1);

		// initialize GUI
		initialize();

		// set caption
		getHeaderPanel().setCaptionText(caption);

		// initialize minimum collapsed height
		minimumCollapsedHeight = getHeaderPanel().getPreferredSize().height + 1;

	}

	/* ===========================================
	 * Private methods
	 * ===========================================
	 */

	/**
	 * This method initializes the panel
	 *
	 */
	private void initialize() {

		// initialize container
		setContainer(new JPanel());
		setContainerLayout(new BorderLayout());
		
		// get application
		

		// prepare this
		this.setLayout(new BorderLayout());
		this.add(getHeaderPanel(),isTouchMode?BorderLayout.SOUTH:BorderLayout.NORTH);
		this.add(getScrollPane(),BorderLayout.CENTER);
		this.setBorderColor(borderColor);

		// calculate minimum collapsed height
		minimumCollapsedHeight =
			getHeaderPanel().getPreferredSize().height + 5 	// add height of header panel including gap
		  + getInsets().top + getInsets().bottom;			// add height of vertical insets

	}

	private void setChangeable(JComponent c, boolean isChangeable) {
		// loop over all children
		for(int i=0; i < c.getComponentCount(); i++){
			Component it = c.getComponent(i);
			// implements IChangeable?
			if(it instanceof IChangeable) {
				((IChangeable)c.getComponent(i)).setChangeable(isChangeable);
			}
			else if(it instanceof JComponent) {
				setChangeable((JComponent)it,isChangeable);
			}
		}
	}

	/* ===========================================
	 * Public methods
	 * ===========================================
	 */

    /**
     * Get minimum size. </p>
     *
     * If <code>minimumCollapsedHeight</code> is greater then previously set minimum size, <code>height</code> in
     * this size is overridden by <code>minimumCollapsedHeight</code>.</p>
     *
     * @see JComponent#getMinimumSize(Dimension)
     *
     * @return Dimension - the minimum size.
     */
    @Override
    public Dimension getMinimumSize() {
    	// get current minimum size
    	Dimension d = super.getMinimumSize();
        // forward
        return new Dimension(d.width,Math.max(minimumCollapsedHeight, d.height));
    }

    /**
     * Get preferred size. </p>
     *
     * If <code>preferredExpandedHeight</code> is set, then <code>Dimension(width,preferredExpandedHeight)</code>
     * is returned, where the width <code>?</code> is <code>width</code> from previously set preferred size.</p>
     *
     * @see JComponent#getPreferredSize(Dimension)
     *
     * @return Dimension - the preferred size.
     */
    @Override
    public Dimension getPreferredSize() {
    	// get current preferred size
    	Dimension d = super.getPreferredSize();
        // forward
        return (preferredExpandedHeight==0 ? d : new Dimension(d.width,preferredExpandedHeight));
    }

    /**
	 * Button size that should be used when adding and inserting buttons
	 */
	public ButtonSize getButtonSize() {
		return buttonSize;
	}

	/**
	 * This method initializes headerPanel
	 *
	 * @return {@link HeaderPanel}
	 */
	public HeaderPanel getHeaderPanel() {
		if (headerPanel == null) {
			try {
				headerPanel = new HeaderPanel("",buttonSize);
				if(isTouchMode) {
					headerPanel.setInsets(1,0,0,0);
				} else {
					headerPanel.setInsets(0,0,1,0);
				}
				headerPanel.addWorkEventListener(new IWorkFlowListener() {

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
		return headerPanel;
	}

	/**
	 * This method gets the caption icon
	 *
	 * @return Icon
	 */
	public Icon getCaptionIcon() {
		return getHeaderPanel().getCaptionIcon();
	}

	/**
	 * This method sets the caption icon
	 *
	 */
	public void setCaptionIcon(Icon icon) {
		getHeaderPanel().setCaptionIcon(icon);
	}

	/**
	 * This method gets the caption text
	 *
	 * @return String
	 */
	public String getCaptionText() {
		return getHeaderPanel().getCaptionText();
	}

	/**
	 * This method sets the caption text
	 *
	 */
	public void setCaptionText(String caption) {
		getHeaderPanel().setCaptionText(caption);
	}

	public void setHeaderVisible(boolean isVisible) {
		getHeaderPanel().setVisible(isVisible);
	}

	public boolean isHeaderVisible() {
		return getHeaderPanel().isVisible();
	}

	@Override
	public void setBorder(Border border) {
		setInsets(border);
	}

	/**
	 * This method sets the border color
	 *
	 */
	public Color setBorderColor(Color color) {
		Color old = borderColor;
		borderColor = color;
		this.setBorder(createBorder());
		this.getHeaderPanel().setBorderColor(color);
		return old;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public boolean isBorderVisible() {
		return isBorderVisible;
	}

	public void setBorderVisible(boolean isVisible) {
		isBorderVisible = isVisible;
		super.setBorder(createBorder());
		if(isVisible) {
			if(isTouchMode) {
				getHeaderPanel().setInsets(1, 0, 0, 0);
			} else {
				getHeaderPanel().setInsets(0, 0, 1, 0);
			}
		} else {
			getHeaderPanel().setInsets(1, 1, 1, 1);
		}
	}

	@Override
	public Insets getInsets() {
		return insets;
	}

	public void setInsets(int t, int l, int b, int r) {
		Insets old = insets;
		// adjust c
		if(old!=null) {
			minimumCollapsedHeight += (insets.top - old.top) + (insets.bottom - old.bottom);
		}
		insets = new Insets(t,l,b,r);
		super.setBorder(createBorder());
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		getHeaderPanel().setCaptionColor(foreground,background);
	}

	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		return getHeaderPanel().insertButton(before,button,command);
	}

	public AbstractButton insertButton(String before, String command, String caption) {
		return getHeaderPanel().insertButton(before, command, caption);
	}

	public AbstractButton addButton(AbstractButton button, String command) {
		return getHeaderPanel().addButton(button,command);
	}

	public AbstractButton addButton(String command, String caption) {
		return getHeaderPanel().addButton(command, caption);
	}

	public void removeButton(String command) {
		getHeaderPanel().removeButton(command);
	}

	public boolean addItem(JComponent item) {
		return getHeaderPanel().addItem(item);
	}

	public boolean insertItem(String before, JComponent item) {
		return getHeaderPanel().insertItem(before,item);
	}

	public boolean insertItem(JComponent before, JComponent item) {
		return getHeaderPanel().insertItem(before,item);
	}

	public boolean removeItem(JComponent item) {
		return getHeaderPanel().removeItem(item);
	}

	public boolean containsButton(String command) {
		return getHeaderPanel().containsButton(command);
	}

	public AbstractButton getButton(String command) {
		return getHeaderPanel().getButton(command);
	}

	public boolean isButtonVisible(String command) {
		return getHeaderPanel().isButtonVisible(command);
	}

	public void setButtonVisible(String command, boolean isVisible) {
		getHeaderPanel().setButtonVisible(command,isVisible);
	}

	public boolean isButtonEnabled(String command) {
		return getHeaderPanel().isButtonEnabled(command);
	}

	public void setButtonEnabled(String command, boolean isEnabled) {
		getHeaderPanel().setButtonEnabled(command,isEnabled);
	}

	public void addAction(String command) {
		getHeaderPanel().addAction(command);
	}

	public void removeAction(String command) {
		getHeaderPanel().removeAction(command);
	}

	/**
	 * This method initialized the scroll pane
	 *
	 * @return {@link JScrollPane}
	 */
	public JScrollPane getScrollPane() {
		if(scrollPane==null) {
			scrollPane = UIFactory.createScrollPane(getContainer());
		}
		return scrollPane;
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

	public Dimension getViewSize() {
		return getScrollPane().getViewport().getViewSize();
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {

		// uninstall old?
		if(getContainer() instanceof IPanel) {
			install(container, false);
		}
		else {
			((PanelManager)getManager()).removeContainer(container);
		}

		// update viewport
		getScrollPane().setViewportView(container);

		// install new?
		if(container instanceof IPanel) {
			install(container, true);
		}
		else {
			((PanelManager)getManager()).addContainer(container);
		}

		// save hook
		this.container = container;

		// update borders
		setBorderColor(borderColor);

	}

	@Override
	public void setContainerEnabled(Boolean isEnabled) {
		// update
		getScrollPane().setEnabled(isEnabled);
		// forward
		super.setContainerEnabled(isEnabled);
	}

	public boolean doAction(String command) {
		return getHeaderPanel().doAction(command);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		for(int i=0;i<getComponentCount();i++) {
			getComponent(i).setEnabled(isEnabled);
		}
		getContainer().setEnabled(isEnabled);
	}

	@Override
	public void setChangeable(boolean isChangeable) {
		// forward
		super.setChangeable(isChangeable);
		// loop over all children?
		if(getContainer() instanceof JComponent) {
			// cast to JComponent
			setChangeable((JComponent)getContainer(),isChangeable);
		}
	}

    public int getMinimumCollapsedHeight() {
        return minimumCollapsedHeight;
    }

    public int getPreferredExpandedHeight() {
        return preferredExpandedHeight;
    }

    public void setPreferredExpandedHeight(int height) {
        preferredExpandedHeight = height > 0 ? Math.max(height,getHeaderPanel().getPreferredSize().height) : 0;
    }
	
	/**
	 * Initialize toggle limits. </p>
	 *
	 * Should be called AFTER all components are added to <code>getContainer()</code>. This ensures
	 * that the preferred size of <code>BasePanel</code> is initialized property when added
	 * to a <code>BasePanel</code> implementing the interface <code>ITogglePanel</code>. </p>
	 *
	 * The protected member variable <code>minimumCollapsedHeight</code> is set equal to <code>min</code>.</p>
	 *
	 * The protected member variable <code>preferredExpandedHeight</code> is set equal to
	 * the preferred height of this panel, using the method <code>fitThisToPreferredContainerSize</code>.</p>
	 *
	 * The protected member variable <code>expandedSize</code> is set equal to <code>Dimension(width,preferredExpandedHeight)</code>.</p>
	 *
	 * @param int width - The preferred width
	 * @param boolean setPreferredSize - if <code>true</code>, calculated
	 * <code>expandedSize</code> is set as preferred size.
	 *
	 * @return Dimension - Calculated <code>expandedSize</code>
	 */
	public Dimension setToggleLimits(int width, boolean setPreferredSize) {
		return setToggleLimits(width, minimumCollapsedHeight,setPreferredSize);
	}

	/**
	 * Initialize toggle limits. </p>
	 *
	 * Should be called AFTER all components are added to <code>getContainer()</code>. This ensures
	 * that the preferred size of <code>BasePanel</code> is initialized property when added
	 * to a <code>BasePanel</code> implementing the interface <code>ITogglePanel</code>. </p>
	 *
	 * The protected member variable <code>minimumCollapsedHeight</code> is set equal to <code>min</code>.</p>
	 *
	 * The protected member variable <code>preferredExpandedHeight</code> is set equal to
	 * the preferred height of this panel, using the method <code>fitThisToPreferredContainerSize</code>.</p>
	 *
	 * The protected member variable <code>expandedSize</code> is set equal to <code>Dimension(width,preferredExpandedHeight)</code>.</p>
	 *
	 * @param int width - The preferred width
	 * @param int min - The minimum collapsed height
	 * @param boolean setPreferredSize - if <code>true</code>, calculated
	 * <code>expandedSize</code> is set as preferred size.
	 *
	 * @return Dimension - Calculated <code>expandedSize</code>
	 */
	public Dimension setToggleLimits(int width, int min, boolean setPreferredSize) {

		// initialize minimum collapsed height
		minimumCollapsedHeight = min;

		// calculate preferred layout size of container
		Dimension d = fitThisToPreferredContainerSize();

		// set preferred expanded height
		preferredExpandedHeight = d.height;

		// create preferred expanded size
		d = new Dimension(width,preferredExpandedHeight);

		// initialize preferred size
		if(setPreferredSize) setPreferredSize(d);

		// finished
		return d;

	}

	public Dimension adjustToggleLimits(int dy) {
		setPreferredExpandedHeight(getPreferredExpandedHeight()+dy);
		invalidate();
		return getPreferredSize();
	}

	public Dimension adjustPreferredSize(int min, int dy) {
		return adjustPreferredSize(getPreferredSize(),min,dy);
	}

	public Dimension adjustPreferredSize(Dimension d, int min, int dy) {
		if(d!=null) {
			int h = d.height+dy;
			h = Math.max(h, min);
			d = new Dimension(d.width,h);
			setPreferredSize(d);
			invalidate();
		}
		return d;
	}

	/* ===========================================
	 * IPanel implementation
	 * =========================================== */

	public void update() { /* Override this */ }

	@Override
	public void setParentManager(IPanelManager parent, boolean requestMoveTo, boolean setAll) {
		// register in header
		getHeaderPanel().setManager(parent,requestMoveTo);
		// forward
		super.setParentManager(parent,requestMoveTo,setAll);
	}

	public void addWorkFlowListener(IWorkFlowListener listener) {
		getHeaderPanel().addWorkEventListener(listener);
	}

	public void removeWorkFlowListener(IWorkFlowListener listener) {
		getHeaderPanel().removeWorkEventListener(listener);
	}

	public void addActionListener(ActionListener listener) {
		getHeaderPanel().addActionListener(listener);
	}

	public void removeActionListener(ActionListener listener) {
		getHeaderPanel().removeActionListener(listener);
	}

	/* ===========================================
	 * IChangeable implementation
	 * =========================================== */

	@Override
	public void setMarked(int isMarked) {
		if(isMarked() != isMarked) {
			super.setMarked(isMarked);
			oldBorderColor = setBorderColor(isMarked>0 ? Color.BLUE : oldBorderColor);
		}
	}

	/* ===========================================
	 * ActionListener implementation
	 * =========================================== */

	public void actionPerformed(ActionEvent e) {
		getHeaderPanel().actionPerformed(e);
	}

	/* ===========================================
	 * Protected methods
	 * =========================================== */

	protected Border createBorder() {
		// create?
		if(isBorderVisible) {
			// create border
			return new DiskoBorder(
					insets.top, insets.left,
					insets.bottom, insets.right,
					borderColor);
		}
		else {
			return BorderFactory.createEmptyBorder();
		}
	}

	protected void setInsets(Border border) {
		if(border!=null) {
			Insets insets = border.getBorderInsets(this);
			setInsets(insets.top,insets.left,insets.bottom,insets.right);
		}
		else {
			setInsets(0,0,0,0);
		}
	}

	protected void fireActionEvent(ActionEvent e, boolean validate) {
		if(validate)
			getHeaderPanel().actionPerformed(e);
		else
			getHeaderPanel().fireActionEvent(e);
	}

	protected void fireOnWorkFinish(Object source, Object data) {
		getHeaderPanel().fireOnWorkFinish(source,data);
	}

	protected void fireOnWorkCancel(Object source, Object data) {
		getHeaderPanel().fireOnWorkCancel(source, data);
	}

	protected void fireOnWorkChange(Object source, Object data) {
		getHeaderPanel().fireOnWorkChange(source,data);
	}

	protected void fireOnWorkPerformed(WorkFlowEvent e){
		getHeaderPanel().fireOnWorkPerformed(e);
	}

	/* ===========================================
	 * Anonymous classes
	 * =========================================== */

	/**
	 * This listener maintains the preferred and expanded size in ITogglePanel hierarchies. This is important
	 * because when panels are toggled (resized), the preferred size must be adjusted accordingly to ensure that
	 * layout in the component hierarchy conforms to the toggle size requirements.</p>
	 *
	 * The listener should be added to all container components that implements the {@link ITogglePanel}
	 * interface. A toggle state change in a container component will then adjust the preferred size of this BasePanel.
	 *
	 */

	protected IToggleListener toggleListener = new IToggleListener() {

		@Override
		public void toggleChanged(ITogglePanel panel, boolean isExpanded, int dx, int dy) {
			if(isPreferredSizeSet()) {
				int mh = panel.getMinimumCollapsedHeight();
				int ph = panel.getPreferredExpandedHeight();
				adjustPreferredSize(minimumCollapsedHeight, isExpanded ? ph - mh: mh - ph);
			}
		}

	};

}
