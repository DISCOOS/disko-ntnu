package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.util.Utils;
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

    private Insets insets;
    private boolean isBorderVisible = true;
    private Color borderColor = Color.GRAY;
    private Color oldBorderColor = Color.GRAY;
    private ButtonSize buttonSize = ButtonSize.SMALL;

    private HeaderPanel headerPanel;
    private JScrollPane scrollPane;
    private Component bodyComponent;
    private boolean isFitBodyOnResize = false;

    private ComponentListener componentListener;

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
        super();
        // prepare
        this.buttonSize = buttonSize;
        this.insets = new Insets(1,1,1,1);
        // initialize GUI
        initialize();
        // set caption
        getHeaderPanel().setCaptionText(caption);
        // set caption color
        getHeaderPanel().setCaptionColor(Color.WHITE,Color.LIGHT_GRAY);
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
        // initialize body component
        setBodyComponent(new JPanel());
        setBodyLayout(new BorderLayout());
        // prepare this
        this.setLayout(new BorderLayout());
        this.add(getHeaderPanel(),BorderLayout.NORTH);
        this.add(getScrollPane(),BorderLayout.CENTER);
        this.setBorderColor(borderColor);
    }

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
            return null;
        }
    }

    /* ===========================================
     * Public methods
     * ===========================================
     */

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
                headerPanel.setInsets(0,0,1,0);
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
        this.setBorder(createBorder());
        this.setHeaderBorderVisible(isHeaderBorderVisible());
    }

    public boolean isHeaderBorderVisible() {
        return getHeaderPanel().isBorderVisible();
    }

    public void setHeaderBorderVisible(boolean isVisible) {
        if(isVisible) {
            if(isBorderVisible)
                getHeaderPanel().setInsets(0, 0, 1, 0);
            else
                getHeaderPanel().setInsets(1, 1, 1, 1);
        }
        else
            getHeaderPanel().setBorderVisible(isVisible);

    }

    public Insets getInsets() {
        return insets;
    }

    public void setInsets(int t, int l, int b, int r) {
        insets = new Insets(t,l,b,r);
        this.setBorder(createBorder());
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
            scrollPane = UIFactory.createScrollPane(getBodyComponent());
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

                        if(SwingUtilities.isDescendingFrom((Component)e.getSource(), BasePanel.this)) {
                            return;
                        }

                        // forward
                        fitBodyToView();

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

    public Dimension getPreferredBodySize() {
        if(bodyComponent instanceof JComponent)
            return ((JComponent)bodyComponent).getPreferredSize();
        return new Dimension(0,0);
    }

    public void setPreferredBodySize(Dimension size) {
        if(bodyComponent instanceof JComponent)
            ((JComponent)bodyComponent).setPreferredSize(size);
    }

    public Dimension getViewSize() {
        return getScrollPane().getViewport().getViewSize();
    }

    public LayoutManager getBodyLayout() {
        if(bodyComponent instanceof JComponent)
            return ((JComponent)bodyComponent).getLayout();
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
        // update scrollpane and viewport
        getScrollPane().setViewportView(body);
        getScrollPane().setBorder(BorderFactory.createEmptyBorder());
        getScrollPane().setViewportBorder(BorderFactory.createEmptyBorder());

        // save hook
        bodyComponent = body;
        // update borders
        setBorderColor(borderColor);
    }

    public boolean isBodyEnabled() {
        return bodyComponent.isEnabled();
    }

    public void setBodyEnabled(Boolean isEnabled) {
        // update
        getScrollPane().setEnabled(isEnabled);
        bodyComponent.setEnabled(isEnabled);
    }

    public boolean doAction(String command) {
        return getHeaderPanel().doAction(command);
    }

      @Override
      public void setEnabled(boolean isEnabled) {
          for(int i=0;i<getComponentCount();i++) {
              getComponent(i).setEnabled(isEnabled);
          }
          getBodyComponent().setEnabled(isEnabled);
      }

      @Override
    public void setChangeable(boolean isChangeable) {
          // forward
          super.setChangeable(isChangeable);
          // loop over all children?
          if(getBodyComponent() instanceof JComponent) {
              // cast to JComponent
              setChangeable((JComponent)getBodyComponent(),isChangeable);
          }
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
     * IPanel implementation
     * =========================================== */

    public void update() { /* Override this */ }

    public IPanelManager getManager() {
        return getHeaderPanel().getManager();
    }

    public void setManager(IPanelManager manager, boolean isMainPanel) {
        // forward
        getHeaderPanel().setManager(manager,isMainPanel);
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
            validate();
        }
        return d;
    }

    protected Dimension fitBodyToAny() {
        Dimension d = getScrollPane().getViewport().getSize();
        Utils.setAnySize(getBodyComponent(), d.width, d.height);
        return d;
    }

    protected Dimension fitThisToPreferredBodySize() {
        // initialize
        Dimension d = getBodyComponent().getPreferredSize();
        int w = d.width;
        int h = d.height + getHeaderPanel().getPreferredSize().height + 5
            + (isScrollBarVisible(JScrollPane.HORIZONTAL_SCROLLBAR)
                    ? getScrollPane().getHorizontalScrollBar().getHeight() : 0);
        setSize(new Dimension(w,h));
        setPreferredSize(new Dimension(w,h));
        return d;
    }

    protected Dimension fitThisToMinimumBodySize() {
        Dimension d = getBodyComponent().getMinimumSize();
        int w = d.width;
        int h = d.height + getHeaderPanel().getMinimumSize().height + 5
        + (isScrollBarVisible(JScrollPane.HORIZONTAL_SCROLLBAR)
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
        getHeaderPanel().actionPerformed(e);
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

}  //  @jve:decl-index=0:visual-constraint="10,10"
