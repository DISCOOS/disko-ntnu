package org.redcross.sar.gui.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.redcross.sar.gui.AWTUtilitiesWrapper;
import org.redcross.sar.gui.DiskoGlassPane;
import org.redcross.sar.gui.DiskoGlassPaneUtils;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.IMsoHolder;
import org.redcross.sar.gui.event.GlassPaneEvent;
import org.redcross.sar.gui.event.IGlassPaneListener;
import org.redcross.sar.gui.panel.AbstractPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.BaseToolPanel;
import org.redcross.sar.gui.panel.IPanel;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.AppProps;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;

public class DefaultDialog extends JDialog implements IDialog {

    private static final long serialVersionUID = 1L;

    private static int PAUSE_MILLIS = 100;
    private static int MILLIS_TO_SHOW = 1000;

    public static final int POS_WEST   = 1;
    public static final int POS_NORTH  = 2;
    public static final int POS_EAST   = 3;
    public static final int POS_SOUTH  = 4;
    public static final int POS_CENTER = 5;

    public static final int TRANSLUCENT_MANUAL = 0;
    public static final int TRANSLUCENT_ONFOCUS = 1;
    public static final int TRANSLUCENT_ONMOUSE = 2;

    public static final int MARKED_MANUAL = TRANSLUCENT_MANUAL;
    public static final int MARKED_ONFOCUS = TRANSLUCENT_ONFOCUS;
    public static final int MARKED_ONMOUSE = TRANSLUCENT_ONMOUSE;

    public static final int SIZE_TO_OFF = 0;
    public static final int SIZE_TO_FIT = 1;
    public static final int SIZE_TO_LIMIT = 2;

    private int position = POS_CENTER;			// position relative to snapToComponent
    private int sizeTo = SIZE_TO_OFF;			// resize behavior when snapping
    private boolean snapToInside = true;		// if true, snap inside the visible rectangle of snapToComponent

    private int width  = -1;
    private int height = -1;

    private Component snapToComponent;

    private float opacity = 1.0f;

    private boolean isMoveable = true;
    private boolean isEscapeable = true;
    private boolean isTranslucent = false;

    private boolean isOpacitySupported = false;
    private boolean isTranslucencySupported = false;

    private int isMarked = 0;

    private int isTranslucentOn = TRANSLUCENT_MANUAL;
    private int isMarkedOn = MARKED_MANUAL;

    private final Adapter m_adapter = new Adapter();
    private final DialogWorker m_worker = new DialogWorker(MILLIS_TO_SHOW);

    /* ==========================================================
     *  Constructors
     * ========================================================== */

    public DefaultDialog() {
        this(Utils.getApp().getFrame());
    }

    public DefaultDialog(Window owner) {

        // forward
        super(owner);

        // initialize GUI
        initialize();

        // set default location
        setSnapTo(owner,POS_CENTER,0,false);

        // add global key-event listener
        Utils.getApp().getKeyEventDispatcher().addKeyListener(
                KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE, new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                // can process event?
                if(isEscapeable() && getFocusOwner()!=null) {
                    e.consume();
                    cancel();
                }

            }
        });

        // add component listener
        addComponentListener(m_adapter);

        // add glass pane listener
        getGlassPane().addGlassPaneListener(m_adapter);

        // test for translucency support
        validateTranslucencySupport();

    }

    /* ==========================================================
     *  Public methods
     * ========================================================== */

    @Override
    public void setContentPane(Container c) {
        // get current
        Container old = super.getContentPane();
        // is old content instance of IPanel?
        if(old instanceof IPanel) {
            IPanel p = (IPanel)old;
            p.setManager(null, false);
        }
        // forward
        super.setContentPane(c);
        // is instance of IPanel?
        if(c instanceof IPanel) {
            IPanel p = (IPanel)c;
            p.setManager(this, true);
        }

    }

    @Override
    public void setVisible(boolean isVisible) {
        // update?
        if(isVisible) {
            if(getContentPane() instanceof IPanel) {
                ((AbstractPanel)getContentPane()).update();
            }
            // forward
            snapTo(false,false);
        }
        // forward
        super.setVisible(isVisible);
    }

    @Override
    public void setSize(int w, int h) {
    	width = w;
    	height = h;
    	super.setSize(w, h);
    }

    @Override
    public void setSize(Dimension d) {
    	width = d.width;
    	height = d.height;
    	super.setSize(d);
    }

    /* ==========================================================
     *  IDialog interface implementation
     * ========================================================== */

    public void snapTo() {
        // forward
        snapTo(true,false);
    }

    public boolean isMoveable() {
        return isMoveable;
    }

    public boolean isTranslucent() {
        return isTranslucent;
    }

    public boolean setTrancluent(boolean isTranslucent) {

        /* =========================================================
         *  Set translucency if possible
         * =========================================================
         *  Translucency requires release 6u10 or later.
         * ========================================================= */

        // can set translucency?
        if(isTranslucencySupported && this.isTranslucent != isTranslucent) {
            this.isTranslucent = isTranslucent;
            if(isShowing()) AWTUtilitiesWrapper.setWindowOpaque(this, !isTranslucent);
        }

        // set flag
        this.isTranslucent = isTranslucent && isTranslucencySupported;

        // finished
        return this.isTranslucent;

    }

    public float getOpacity() {
        return opacity;
    }

    public float setOpacity(float opacity) {

        /* =========================================================
         *  Set opacity if possible
         * =========================================================
         *  opacity requires JRE release 6u10 or later.
         * ========================================================= */

        // can set opacity?
        if(isOpacitySupported) {
            this.opacity = opacity;
            if(isShowing()) AWTUtilitiesWrapper.setWindowOpacity(this, opacity);
        }

        // finished
        return this.opacity;
    }

    public int isMarkedOn() {
        return isMarkedOn;
    }

    @Override
    public int setMarkedOn(int isMarkedOn) {
        int old = this.isMarkedOn;
        if(old!=isMarkedOn) {
            this.isMarkedOn = isMarkedOn;
            onAutoLayout();
        }
        return old;
    }

    public int isMarked() {
        return isMarked;
    }

    public void setMarked(int isMarked) {
        if(this.isMarked!=isMarked) {
            this.isMarked = isMarked;
            if(isWorkSupported()) {
                IChangeable changeable = (IChangeable)getContentPane();
                changeable.setMarked(isMarked);
            }
        }
    }

    @Override
    public int isTranslucentOn() {
        return isMarkedOn;
    }

    @Override
    public int setTrancluentOn(int isTranslucentOn) {
        int old = this.isTranslucentOn;
        if(old!=isTranslucentOn) {
            this.isTranslucentOn = isTranslucentOn;
            onAutoLayout();
        }
        return old;
    }

    public void setEscapeable(boolean isEscapeable) {
        this.isEscapeable = isEscapeable;
    }

    public boolean isEscapeable() {
        if(isEscapeable && isWorkSupported()) {
            if(getContentPane() instanceof BasePanel) {
                return ((BasePanel)getContentPane()).isButtonVisible("cancel");
            }
            if(getContentPane() instanceof BaseToolPanel) {
                return ((BaseToolPanel)getContentPane()).isButtonVisible("cancel");
            }
        }
        return false;
    }

    public void setMoveable(boolean isMoveable) {
        this.isMoveable = isMoveable;
    }

    public void cancelSetVisible() {
        m_worker.cancel();
    }

    public void delayedSetVisible(boolean isVisible, int millisToShow) {
        // any change?
        if(isVisible!=this.isVisible()) {
            // forward
            m_worker.start(isVisible,millisToShow);
        }
    }

    @Override
    public void setLocationByPlatform(boolean locationByPlatform) {
        // NOT ALLOWED
    }

    @Override
    public void setLocation(int x, int y) {
        // reset
        unregisterSnapToComponent();
        // forward
        super.setLocation(x, y);
    }

    @Override
    public void setLocation(Point p) {
        // reset
        unregisterSnapToComponent();
        // forward
        super.setLocation(p);
    }

    @Override
    public void setLocationRelativeTo(Component c) {
        // reset
        unregisterSnapToComponent();
        // forward
        super.setLocationRelativeTo(c);
    }

    /**
     * Set location relative to component </p>
     *
     * @param Component snapTo - Locate and resize relative to snapTo component
     * @param int position - Relative position
     * @param int sizeTo - Resizing behavior when snapping
     * @param boolean snapToInside - If <code>true</code>, snap to inside of the visible rectangle of component
     */
    public void setSnapTo(Component snapTo, int position, int sizeTo, boolean snapToInside) {

        // unregister
        unregisterSnapToComponent();

        // prepare
        this.position = position;
        this.sizeTo = sizeTo;
        this.snapToInside = snapToInside;

        // forward
        registerSnapToComponent(snapTo);
    }

    public boolean isWorkSupported() {
        return getContentPane() instanceof IChangeable;
    }

    /* ==========================================================
     *  IDialog interface implementation
     * ========================================================== */

    public boolean isDirty() {
        if(isWorkSupported()) {
            return ((IPanel)getContentPane()).isDirty();
        }
        return false;

    }

    public void setDirty(boolean isDirty) {
        if(isWorkSupported())
            ((IPanel)getContentPane()).setDirty(isDirty);
    }

    public boolean isChangeable() {
        if(isWorkSupported()) {
            return ((IPanel)getContentPane()).isChangeable();
        }
        return false;

    }

    public void setChangeable(boolean isChangeable) {
        if(isWorkSupported())
            ((IPanel)getContentPane()).setChangeable(isChangeable);
    }

    public IMsoObjectIf getMsoObject() {
        if(isWorkSupported()) {
            return ((IMsoHolder)getContentPane()).getMsoObject();
        }
        return null;
    }

    public void setMsoObject(IMsoObjectIf msoObj) {
        if(isWorkSupported())
            ((IMsoHolder)getContentPane()).setMsoObject(msoObj);
    }

    public void reset() {
        if(isWorkSupported())
            ((IPanel)getContentPane()).reset();
    }

    public boolean finish() {
        if(isWorkSupported()) {
            return ((IPanel)getContentPane()).finish();
        }
        return false;

    }

    public boolean cancel() {
        if(isWorkSupported()) {
            return ((IPanel)getContentPane()).cancel();
        }
        return false;
    }

    public void addWorkFlowListener(IWorkFlowListener listener) {
        if(isWorkSupported())
            ((IPanel)getContentPane()).addWorkFlowListener(listener);
    }

    public void removeWorkFlowListener(IWorkFlowListener listener) {
        if(isWorkSupported())
            ((IPanel)getContentPane()).removeWorkFlowListener(listener);
    }

    /* ==========================================================
     *  IPanelManager interface implementation
     * ========================================================== */

    public boolean requestMoveTo(int x, int y, boolean isRelative) {
        if(isMoveable()) {
            if(isRelative) {
                super.setLocation(getLocation().x+x,getLocation().y+y);
            } else {
                super.setLocation(x,y);
            }
            return true;
        }
        return false;
    }

    public boolean requestResize(int w, int h, boolean isRelative) {
        if(isResizable() && sizeTo==0) {
            if(isRelative) {
                setSize(width+w,height+h);
            } else {
                setSize(w, h);
            }
            snapTo();
            pack();
            return true;
        }
        return false;

    }

    public boolean requestFitToContent() { return false; }

    public boolean requestShow() {
        if(isDisplayable()) {
            setVisible(true);
            return true;
        }
        return false;
    }

    public boolean requestHide() {
        // important!
        setVisible(false);
        // finished
        return !isVisible();
    }

    @Override
    public DiskoGlassPane getGlassPane() {
        return (DiskoGlassPane)super.getGlassPane();
    }

    /* ==========================================================
     *  Private methods
     * ========================================================== */

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        // prepare
        this.setUndecorated(true);
        this.setGlassPane(DiskoGlassPaneUtils.createGlassPane(this));
    }


    private void snapTo(boolean update, boolean resizeOnly) {

        try {
            // position not defined?
            if (snapToComponent == null ||
                    !(snapToComponent.isDisplayable()
                            && snapToComponent.isShowing())) return;

            // initialize size?
            if (update || width == -1 || height == -1) {
                width  = getWidth() !=0 ? getWidth() : -1;
                height = getHeight() !=0 ? getHeight() : -1;
            }

            // initialize
            int offset = 2;
            int bx = snapToComponent.getLocationOnScreen().x;
            int by = snapToComponent.getLocationOnScreen().y;
            int bw = snapToComponent.getWidth();
            int bh = snapToComponent.getHeight();
            int w = 0;
            int h = 0;
            int x = bx;
            int y = by;
            // get position data
            switch (position) {
                case POS_WEST:
                    w = snapToInside ? (width > bw - 2*offset ? bw - 2*offset : width) : bw;
                    h = snapToInside ? h = bh - 2*offset : bh;
                    x += snapToInside ? offset : - w - offset;
                    y += snapToInside ? offset : 0;
                    break;
                case POS_EAST:
                    w = snapToInside ? (width > bw - 2*offset ? bw - 2*offset : width) : bw;
                    h = snapToInside ? bh - 2*offset : bh;
                    x += snapToInside ? (bw - w - offset) : (bw + offset);
                    y += snapToInside ? offset : 0;
                    break;
                case POS_NORTH:
                    w = snapToInside ? bw - 2*offset : bw;
                    h = snapToInside ? (height > bh - 2*offset ? bh - 2*offset : height) : bh;
                    x += snapToInside ? offset : 0;
                    y += snapToInside ? offset : - h - offset;
                    break;
                case POS_SOUTH:
                    w = snapToInside ? bw - 2*offset : bw;
                    h = snapToInside ? (height > bh - 2*offset ? bh - 2*offset : height) : bh;
                    x += snapToInside ? offset : 0;
                    y += snapToInside ? (bh - h - offset) : (bh + offset);
                    break;
                case POS_CENTER:
                    w = (width > bw - 2*offset) ? bw - 2*offset : width;
                    h = (height > bh - 2*offset) ? bh - 2*offset : height;
                    x += (bw - w) / 2;
                    y += (bh - h) / 2;
                    break;
            }

            // size to fit?
            switch(sizeTo) {
            case SIZE_TO_FIT:
                if (w > 0 && h > 0) {
                    Utils.setAnySize(this, w, h);
                    this.pack();
                }
                break;
            case SIZE_TO_LIMIT:
                if (snapToInside && width > 0 && height > 0) {
                    width += (bx + bw < x + width + offset ? bx + bw - (x + width + offset) : 0);
                    height += (by + bh < y + height + offset ? by + bh - (y + height + offset) : 0);
                    w = width;
                    h = height;
                    Utils.setAnySize(this, width, height);
                    this.pack();
                }
                break;
            }

            // snap to position?
            if(!resizeOnly) {
                // get screen size
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                // ensure visible in both directions
                x = (x + w > screen.width) ? screen.width - w : x;
                y = (y + h > screen.height) ? screen.height - h : y;
                // update location
                super.setLocation(x, y);
            }
            // apply location change
            this.validate();
        } catch (Exception ex) {
            // Consume
            //ex.printStackTrace();
        }

    }

    private void registerSnapToComponent(Component c) {
        // register?
        if(c!=null) {
            // prepare
            snapToComponent = c;
            // add listener
            c.addComponentListener(m_adapter);
            // get root component
            c = SwingUtilities.getRoot(snapToComponent);
            if(c!=null && c!=snapToComponent) c.addComponentListener(m_adapter);
            // forward?
            if(isVisible()) snapTo(true,false);
        }
    }

    private void unregisterSnapToComponent() {
        // unregister?
        if(snapToComponent!=null) {
            // remove listener
            snapToComponent.removeComponentListener(m_adapter);
            Component c = SwingUtilities.getRoot(snapToComponent);
            if(c!=null && c!=snapToComponent) c.removeComponentListener(m_adapter);
        }
        snapToComponent = null;
    }

    private boolean onAutoLayout() {

        /* ===============================================================
         *  BUG FIX: AWT-WINDOWS Deamon thread works indefinitely
         * ===============================================================
         *
         *  If opacity is set to any other value than 1.0f before
         *  JDialog.pack() is called, the AWT-WINDOWS thread will start
         *  to work indefinitely.
         *
         * =============================================================== */

        if (isShowing()) {

            // cast to glass pane to DiskoGlassPane
            DiskoGlassPane glassPane = (DiskoGlassPane) getGlassPane();
            // is dialog marked?
            if ((isMarkedOn & MARKED_ONFOCUS) == MARKED_ONFOCUS) {
                setMarked(glassPane.isFocusInWindow() ? 1 : 0);
            }
            if ((isMarkedOn & MARKED_ONMOUSE) == MARKED_ONMOUSE) {
                setMarked(glassPane.isMouseInWindow() ? 1 : 0);
            }
            // is dialog translucent?
            if ((isTranslucentOn & TRANSLUCENT_ONFOCUS) == TRANSLUCENT_ONFOCUS) {
                setTrancluent(!glassPane.isFocusInWindow());
            }
            if ((isTranslucentOn & TRANSLUCENT_ONMOUSE) == TRANSLUCENT_ONMOUSE) {
                float opacity = getOpacity();
                if (glassPane.isMouseInWindow()) {
                    setOpacity(new Float(AppProps.getText("GUI.LAYOUT.OPAQUE.SHOW")));
                } else {
                    setOpacity(new Float(AppProps.getText("GUI.LAYOUT.OPAQUE.HIDE")));
                }
                return getOpacity()!=opacity;

            }
        }
        return false;
    }

    private void validateTranslucencySupport() {

        /* =========================================================
         *  Translucency requires release 6u10 or later.
         * ========================================================= */

        isTranslucencySupported = AWTUtilitiesWrapper.isTranslucencySupported(AWTUtilitiesWrapper.PERPIXEL_TRANSLUCENT);
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsConfiguration translucencyCapableGC = env.getDefaultScreenDevice().getDefaultConfiguration();
        // is default not capable?
        if (!AWTUtilitiesWrapper.isTranslucencyCapable(translucencyCapableGC)) {

            // try one of the other devices, if they exists
            translucencyCapableGC = null;
            GraphicsDevice[] devices = env.getScreenDevices();

            for (int i = 0; i < devices.length && translucencyCapableGC == null; i++) {
                GraphicsConfiguration[] configs = devices[i].getConfigurations();
                for (int j = 0; j < configs.length && translucencyCapableGC == null; j++) {
                    if (AWTUtilitiesWrapper.isTranslucencyCapable(configs[j])) {
                        translucencyCapableGC = configs[j];
                    }
                }
            }
            if (translucencyCapableGC == null) {
                isTranslucencySupported = false;
            }
        }

        isOpacitySupported = AWTUtilitiesWrapper.isTranslucencySupported(AWTUtilitiesWrapper.TRANSLUCENT);

    }

    private boolean handleComponentEvent(Component source, boolean isMove) {
        if(source==this || source==snapToComponent) {
            return true;
        }
        else if(snapToComponent!=null && source==SwingUtilities.getRoot(snapToComponent)) {
            return isMove;
        }
        return false;
    }

    /* ===========================================
     * Protected methods
     * =========================================== */

    protected void fireOnWorkFinish(Object source, Object data) {
        fireOnWorkPerformed(new WorkFlowEvent(source,data,WorkFlowEvent.EVENT_FINISH));
    }

    protected void fireOnWorkCancel(Object source, Object data) {
        fireOnWorkPerformed(new WorkFlowEvent(source,data,WorkFlowEvent.EVENT_CANCEL));
    }

    protected void fireOnWorkChange(Object data) {
        fireOnWorkPerformed(new WorkFlowEvent(this,data,WorkFlowEvent.EVENT_CHANGE));
    }

    protected void fireOnWorkChange(Object source, Object data) {
        fireOnWorkPerformed(new WorkFlowEvent(source,data,WorkFlowEvent.EVENT_CHANGE));
    }

    protected void fireOnWorkPerformed(WorkFlowEvent e) {
        if(isWorkSupported())
            ((IPanel)getContentPane()).onFlowPerformed(e);
    }

    /*========================================================
     * Inner classes
     *======================================================== */

    private class Adapter implements ComponentListener, IGlassPaneListener {

        /*========================================================
           * ComponentListener implementation
           *======================================================== */

        public void componentShown(ComponentEvent e) {
            // cast source to component
            Component c = (Component)e.getSource();
            // handle?
            if(handleComponentEvent(c,false)) {
                requestFitToContent();
                snapTo(false,(c!=snapToComponent));
                onAutoLayout();
                setWindowState(true);
            }
        }

        public void componentHidden(ComponentEvent e) {
            // cast source to component
            Component c = (Component)e.getSource();
            // handle?
            if(handleComponentEvent(c,false)) {
                setVisible(false);
                setWindowState(true);
            }
        }

        public void componentMoved(ComponentEvent e) {
            // cast source to component
            Component c = (Component)e.getSource();
            // handle?
            if(handleComponentEvent(c,true)) {
                snapTo(false,(c!=snapToComponent));
            }
        }

        public void componentResized(ComponentEvent e) {
            // cast source to component
            Component c = (Component)e.getSource();
            // handle?
            if(handleComponentEvent(c,false)) {
                snapTo(false,(c!=snapToComponent));
            }
        }

        /*========================================================
           * IGlassPaneListener implementation
           *======================================================== */

        public void onGlassPaneChanged(GlassPaneEvent e) {
            if(!getGlassPane().isLocked()) {
                if(e.getType() == GlassPaneEvent.MOUSE_CHANGED) {
                    onAutoLayout();
                    /*
                    if(onAutoLayout()) {
                        System.out.println("onAutoLayout:="+System.currentTimeMillis() + ", thread:="+Thread.currentThread().getId());
                    }
                    */
                }
                else if(e.getType() == GlassPaneEvent.FOCUS_CHANGED) {
                    onAutoLayout();
                }
                else if(e.getType() == GlassPaneEvent.LOCK_CHANGED) {
                    onAutoLayout();
                }
            }
        }

        private void setWindowState(boolean isShowing) {

            // can set translucency?
            if(isTranslucencySupported) {
                AWTUtilitiesWrapper.setWindowOpaque(DefaultDialog.this, isShowing ? !isTranslucent : true);
            }

            // can set opacity?
            if(isOpacitySupported) {
                AWTUtilitiesWrapper.setWindowOpacity(DefaultDialog.this, isShowing ? opacity : 1.0f);
            }

        }


    }

    private class DialogWorker implements ActionListener {

        private long m_start = 0;
        private long m_millisToShow = 0;
        private Timer m_timer = null;
        private boolean m_isVisible = false;
        private boolean m_isCancelled = false;

        public DialogWorker(long millisToShow) {
            // save decision delay
            m_millisToShow = millisToShow;
            // create timer
            m_timer = new Timer(PAUSE_MILLIS, this);
        }

        public boolean start(boolean isVisible, int millisToShow) {
            // is not running?
            if(!m_timer.isRunning()) {
                // save
                m_isVisible = isVisible;
                m_millisToShow = millisToShow;
                // on construction, set time in milli seconds
                m_start = System.currentTimeMillis();
                // start timer
                m_timer.start();
                // reset flag
                m_isCancelled = false;
                // success
                return true;
            }
            // invalid
            return false;
        }

        public boolean cancel() {
            // is running?
            if(m_timer.isRunning()) {
                // reset flag
                m_isCancelled = true;
                // stop timer
                m_timer.stop();
                // success
                return true;
            }
            // invalid
            return false;
        }

        public boolean isRunning() {
            return m_timer.isRunning();
        }

        /**
         * Worker
         *
         * Executed on the Event Dispatch Thread
         *
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // has no progress?
            if(!m_isCancelled && System.currentTimeMillis()- m_start > m_millisToShow) {
                // stop timer
                m_timer.stop();
                // show me!
                setVisible(m_isVisible);
            }
        }
    }
}
