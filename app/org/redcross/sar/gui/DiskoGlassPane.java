/**
 *
 */
package org.redcross.sar.gui;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.redcross.sar.Application;
import org.redcross.sar.gui.dialog.ProgressDialog;
import org.redcross.sar.gui.event.GlassPaneEvent;
import org.redcross.sar.gui.event.IGlassPaneListener;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.map.event.DiskoMapEvent;
import org.redcross.sar.map.event.IDiskoMapListener;
import org.redcross.sar.util.Utils;

/**
 * GlassPane tutorial
 * "A well-behaved GlassPane"
 * http://weblogs.java.net/blog/alexfromsun/
 * <p/>
 * This is the final version of the GlassPane
 * it is transparent for MouseEvents,
 * and respects underneath component's cursors by default,
 * it is also friendly for other users,
 * if someone adds a mouseListener to this GlassPane
 * or set a new cursor it will respect them
 *
 * @author Alexander Potochkin
 *
 * Edited:
 * 1. 	Kenneth Gulbrandsøy:
 * 		Added locking of frame using the LockedGlassPane
 *		by Alexander Potochkin
 *         <p/>
 *         https://swinghelper.dev.java.net/
 *         http://weblogs.java.net/blog/alexfromsun/
 *
 * 2.	Kenneth Gulbrandsøy: Added progress information capabilities
 * 3.	Kenneth Gulbrandsøy: Added mouse and focus tracking capabilities
 */
public class DiskoGlassPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Window m_window;

    private Point m_point = new Point();
    private boolean m_isLocked = false;

    private Component m_recentFocusOwner;

    private ProgressDialog m_progressDialog;

    private boolean m_isMouseInWindow = false;
    private boolean m_isFocusInWindow = false;

    private EventListenerList m_listeners = new EventListenerList();

	/*========================================================
	 * Constructors
	 *======================================================== */

    public DiskoGlassPane(Window window) {

    	// forward
        super();

        // prepare
        this.m_window = window;

        // initialize GUI
        initialize();

        // add glass pane as focus and mouse listener
        Toolkit.getDefaultToolkit().addAWTEventListener(
        		listener,
                AWTEvent.MOUSE_MOTION_EVENT_MASK |
                AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.FOCUS_EVENT_MASK );

        // add focus listener
        this.addFocusListener(new FocusAdapter() {
        	@Override
            public void focusGained(FocusEvent e) {
        		if(getProgressDialog()!=null) {
        			getProgressDialog().getProgressPanel().getCancelButton().requestFocusInWindow();
        		}
            }
        });

		/* =================================================
		 * Register map listener.
		 * -------------------------------------------------
		 * This ensures that glass panes are able to track
		 * mouse events. Glass panes uses this to identify
		 * MOUSE_EXITED from Swing to components that do
		 * not fire AWTEvent to the default toolset (ArcGIS
		 * MapBean is one such component).
		 * ================================================= */
        Application.getInstance().getMapManager().addDiskoMapListener(m_mapListener);

    }

	/*========================================================
	 * Public methods
	 *======================================================== */

    public void register(IDiskoMap map) {
    	map.addDiskoMapListener(m_mapListener);
    }

    public void unregister(IDiskoMap map) {
    	map.removeDiskoMapListener(m_mapListener);
    }

    public Point getPoint() {
        return m_point;
    }

    public boolean isMouseInWindow() {
    	return m_isMouseInWindow;
    }

    public boolean isFocusInWindow() {
    	return m_isFocusInWindow;
    }

    public ProgressDialog getProgressDialog() {
    	return m_progressDialog;
    }

    public void setProgressDialog(ProgressDialog dialog) {
    	m_progressDialog = dialog;
    	m_progressDialog.getProgressPanel().getCancelButton().setCursor(Cursor.getDefaultCursor());
    }

    /**
     * If someone adds a mouseListener to the GlassPane or set a new cursor
     * we expect that he knows what he is doing
     * and return the super.contains(x, y)
     * otherwise we return false to respect the cursors
     * for the underneath components
     */
    public boolean contains(int x, int y) {
        if (getMouseListeners().length == 0 && getMouseMotionListeners().length == 0
                && getMouseWheelListeners().length == 0
                && getCursor() == Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
            return false;
        }
        return super.contains(x, y);
    }

    public boolean isLocked() {
		return m_isLocked;
	}

	public boolean setLocked(boolean isLocked) {
		boolean bFlag = m_isLocked;
		// any change?
		if(m_isLocked!=isLocked) {
	        // get root pane
	        JRootPane rootPane = SwingUtilities.getRootPane(this);
	        // any change?
	        if (rootPane != null) {
	        	// allow to lock?
	            if (isLocked) {
	                Component focusOwner = KeyboardFocusManager.
	                        getCurrentKeyboardFocusManager().getPermanentFocusOwner();
	                if (focusOwner != null && SwingUtilities.isDescendingFrom(focusOwner, rootPane)) {
	                	// save current focus owner
	                    m_recentFocusOwner = focusOwner;
	                }
	                requestFocusInWindow();
	            } else {
	                // resume focus to last owner?
	                if (m_recentFocusOwner != null) {
	                    m_recentFocusOwner.requestFocusInWindow();
	                }
	                m_recentFocusOwner = null;
	            }
	        }
	        // update state
			m_isLocked = isLocked;
			// notify
			fireGlassPaneChanged(null,GlassPaneEvent.LOCK_CHANGED);
		}
		setVisible(isLocked);
		return bFlag;
	}

	@Override
	public void setVisible(boolean isVisible) {
		// forward
        super.setVisible(isVisible || m_isLocked);
	}

	public void addGlassPaneListener(IGlassPaneListener listener) {
		m_listeners.add(IGlassPaneListener.class, listener);
	}

	public void removeGlassPaneListener(IGlassPaneListener listener) {
		m_listeners.remove(IGlassPaneListener.class, listener);
	}

	/*========================================================
	 * Protected methods
	 *======================================================== */

	@Override
    protected void paintComponent(Graphics g) {
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            // it is important to call print() instead of paint() here
            // because print() doesn't affect the frame's double buffer
            try {
				rootPane.getLayeredPane().print(g);
			} catch (RuntimeException e) {
				// consume
			}
        }
        if(m_isLocked && false) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setColor(new Color(0, 128, 128, 64));
	        g2.fillRect(0, 0, getWidth(), getHeight());
	        g2.dispose();
        }
    }

    protected void fireGlassPaneChanged(EventObject e, int type) {
    	fireGlassPaneChanged(new GlassPaneEvent(this,e,type));
    }

    protected void fireGlassPaneChanged(GlassPaneEvent e) {
    	for(IGlassPaneListener it : m_listeners.getListeners(IGlassPaneListener.class)) {
    		it.onGlassPaneChanged(e);
    	}
    }

	/*========================================================
	 * Helper methods
	 *======================================================== */

    private void initialize() {
    	// make transparent
    	this.setOpaque(false);
    	// set glass pane cursor
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // ensures that focus is not lost when locked
        this.setInputVerifier( new InputVerifier() {
        	public boolean verify(JComponent c){
        		return !m_isLocked;
        	}
        });
    }

    private void setFocusInWindow(boolean isFocusInWindow, EventObject e) {
    	if(m_isFocusInWindow!=isFocusInWindow) {
    		m_isFocusInWindow = isFocusInWindow;
    		fireGlassPaneChanged(e,GlassPaneEvent.FOCUS_CHANGED);
    	}
    }

    private void setMouseInWindow(Point point, EventObject e) {
		m_point = point;
		m_isMouseInWindow = (m_point!=null && point.x>0 && point.y>0);
		fireGlassPaneChanged(e,GlassPaneEvent.MOUSE_CHANGED);
    }

    private void handleOnMouseMove(DiskoMapEvent e) {
    	Component c = (e.getSource() instanceof Component ? (Component)e.getSource() : null);
    	if(!(m_window==c || SwingUtilities.isDescendingFrom(c, m_window)) && e.getFlags() == 0) {
    		setMouseInWindow(null,e);
        }
    }

	/*========================================================
	 * Anonymous classes
	 *======================================================== */

    private IDiskoMapListener m_mapListener = new IDiskoMapListener() {

		@Override
		public void onMouseMove(DiskoMapEvent e) {
			handleOnMouseMove(e);
		}

		@Override
		public void onExtentChanged(DiskoMapEvent e) { /*NOP*/ }

		@Override
		public void onMapReplaced(DiskoMapEvent e) { /*NOP*/ }

		@Override
		public void onMouseClick(DiskoMapEvent e) { /*NOP*/ }

		@Override
		public void onSelectionChanged(DiskoMapEvent e) { /*NOP*/ }

    };

    private final AWTEventListener listener = new AWTEventListener() {

        public void eventDispatched(AWTEvent e) {
            if (e instanceof KeyEvent) {
            	// dispatch event
            	KeyEvent ke = (KeyEvent)e;
                Component c = ke.getComponent();
                Component root = SwingUtilities.getRoot(c);
            	// do not belong to this application?
                if (!(Utils.inApp(c) || Application.getInstance().getMapManager().isMap(root))) return;
                // consume?
                if(m_isLocked) {
                	if(Utils.isMessageDialog(root)) {
                		root.repaint();
                	}
                	else
                		ke.consume();
                }
            }
            else if (e instanceof MouseEvent) {
                MouseEvent me = (MouseEvent)e;
                Component c = me.getComponent();
                Component root = SwingUtilities.getRoot(c);
            	// do not belong to this application?
                if (!(Utils.inApp(c) || Application.getInstance().getMapManager().isMap(root))) return;
                // consume? (allow message dialog boxes)
                if(m_isLocked) {
                	if(Utils.isMessageDialog(root) || Utils.isMessageDialogShown()) {
                		root.repaint();
                	}
                	else
                		me.consume();
                }
                else {
                	if(!(m_window==c || SwingUtilities.isDescendingFrom(c, m_window))) {
                		setMouseInWindow(null,me);
    	            }
                	else if(me.getButton()!=0 && me.getClickCount()==0) {
                		setMouseInWindow(null,me);
                	}
                	else {
                		if(me.getID() != MouseEvent.MOUSE_EXITED) {
                			MouseEvent converted = null;
	    	            	if(m_window instanceof RootPaneContainer) {
	    		                converted = SwingUtilities.convertMouseEvent(c, me,
	    		                		((RootPaneContainer)m_window).getGlassPane());
	    	            	}
	    	            	else {
	    		                converted = SwingUtilities.convertMouseEvent(c, me, m_window);
	    	            	}
	    	            	setMouseInWindow(converted!=null ? converted.getPoint() : null, me);
                		}
                		/*
                		MouseEvent converted = null;
                		if(me.getID() != MouseEvent.MOUSE_EXITED) {
	    	            	if(m_window instanceof RootPaneContainer) {
	    		                converted = SwingUtilities.convertMouseEvent(c, me,
	    		                		((RootPaneContainer)m_window).getGlassPane());
	    	            	}
	    	            	else {
	    		                converted = SwingUtilities.convertMouseEvent(c, me, m_window);
	    	            	}
                		}
    	            	setMouseInWindow(converted!=null ? converted.getPoint() : null, me);
    	            	*/
    	            }
                }
            }
            else if (e instanceof FocusEvent) {
            	FocusEvent fe = (FocusEvent)e;
                Component c = fe.getComponent();
                Component root = SwingUtilities.getRoot(c);
            	// do not belong to this application?
                if (!(Utils.inApp(c) || Application.getInstance().getMapManager().isMap(root))) return;
                // set flag
                if(!(m_window==c || SwingUtilities.isDescendingFrom(c, m_window))) {
                	setFocusInWindow(false,fe);
	            }
                else {
                	if(fe.getID() == FocusEvent.FOCUS_GAINED)
                    	setFocusInWindow(true,fe);
                    else if (fe.getID() == FocusEvent.FOCUS_LOST)
                    	setFocusInWindow(false,fe);
                }
            }
            repaint();
        }

    };


}