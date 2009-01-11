/**
 *
 */
package org.redcross.sar.gui.panel;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.redcross.sar.gui.dialog.IDialog;

/**
 * @author kennetgu
 *
 */
public class PanelManager implements IPanelManager {

	private IPanelManager parent;
	private final Container container;
	private final Collection<Container> containers = new Vector<Container>();

    /* ==========================================================
     *  Constructors
     * ========================================================== */

	public PanelManager(Container container) {
		// prepare
		this(null,container);
	}

	public PanelManager(IPanelManager parent, Container container) {
		// prepare
		this.parent = parent;
		this.container = container;
	}

    /* ==========================================================
     *  Public methods
     * ========================================================== */

	public IPanel getPanel() {
		return getPanel(container);
	}

	public void addContainer(Container container) {
		if(!containers.contains(container)) containers.add(container);
	}

	public void removeContainer(Container container) {
		if(containers.contains(container)) containers.remove(container);
	}

    /* ==========================================================
     *  IPanelManager implementation
     * ========================================================== */

	public boolean isRootManager() {
		return parent==null;
	}

	public IPanelManager getParentManager() {
		return parent;
	}

	public IPanelManager setParentManager(IPanelManager parent) {
		IPanelManager old = this.parent;
		this.parent = parent;
		return old;
	}

    public boolean requestMoveTo(final int x, final int y, final boolean isRelative) {

		// allowed?
		if (container.isDisplayable()) {
			// ensure on EDT
	    	if (SwingUtilities.isEventDispatchThread()) {
				// forward?
				if (!isRootManager()) {

					return getParentManager().requestMoveTo(x, y, isRelative);

				} else if (isMoveable()) {

					if (isRelative) {
						container.setLocation(container.getLocation().x + x,
								container.getLocation().y + y);
					} else {
						container.setLocation(x, y);
					}
					return true;

				}
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestMoveTo(x, y, isRelative);
					}
				});
			}
		}
		return false;
    }

    public boolean requestResize(final int w, final int h, final boolean isRelative) {

    	// allowed?
        if(isResizable() && container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// get flag
				boolean isManaged = !isRootManager();
				// forward
				boolean isGranted = isManaged ? getParentManager().requestResize(w, h, isRelative) : true;
				// allowed?
				if (isGranted) {// && !isManaged) {
					// forward
					resize(w,h,isRelative || isManaged);
				}
				// finished
				return isGranted;
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestResize(w, h, isRelative);
					}
				});
			}

        }
        return false;

    }

    public boolean requestFitToMinimumContentSize(boolean pack) {

    	// allowed?
    	if(isFitable() && container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// assume failure
				boolean isGranted = false;
				// get panel
				IPanel panel = getPanel();
				// panel exists?
				if (panel instanceof Container) {

					// cast panel to Container
					Container container = (Container) panel;

					// initialize
					int w = container.getWidth();
					int h = container.getHeight();
					int dw = 0;
					int dh = 0;

					// fit to given layout size
					if(pack) panel.fitContainerToMinimumLayoutSize();
					panel.fitThisToMinimumContainerSize();

					// calculate change
					dw = container.getWidth() - w;
					dh = container.getHeight() - h;

					// forward
					isGranted = requestFit(dw, dh);

					// not allowed?
					if (!isGranted) {
						container.setSize(w, h);
					}

					// apply changes
					container.invalidate();

				}
				// finished
				return isGranted;
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFitToMinimumContentSize(false);
					}
				});
			}
    	}

    	return false;

    }

    public boolean requestFitToPreferredContentSize(boolean pack) {

    	// allowed?
    	if(isFitable() && container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// assume failure
				boolean isGranted = false;
				// get panel
				IPanel panel = getPanel();
				// panel exists?
				if (panel instanceof Container) {

					// cast panel to Container
					Container container = (Container) panel;

					// initialize
					int w = container.getWidth();
					int h = container.getHeight();
					int dw = 0;
					int dh = 0;

					// fit to given layout size
					if(pack || true) panel.fitContainerToPreferredLayoutSize();
					panel.fitThisToPreferredContainerSize();

					// calculate change
					dw = container.getWidth() - w;
					dh = container.getHeight() - h;

					// forward
					isGranted = requestFit(dw, dh);

					// not allowed?
					if (!isGranted) {
						container.setSize(w, h);
					}

					// apply changes
					container.invalidate();

				}
				// finished
				return isGranted;
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFitToPreferredContentSize(false);
					}
				});
			}
    	}

    	return false;

    }

    public boolean requestFitToMaximumContentSize(boolean pack) {

    	// allowed?
    	if(isFitable() && container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// assume failure
				boolean isGranted = false;
				// get panel
				IPanel panel = getPanel();
				// panel exists?
				if (panel instanceof Container) {

					// cast panel to Container
					Container container = (Container) panel;

					// initialize
					int w = container.getWidth();
					int h = container.getHeight();
					int dw = 0;
					int dh = 0;

					// fit to given layout size
					if(pack) panel.fitContainerToMaximumLayoutSize();
					panel.fitThisToMaximumContainerSize();

					// calculate change
					dw = container.getWidth() - w;
					dh = container.getHeight() - h;

					// forward
					isGranted = requestFit(dw, dh);

					// not allowed?
					if (!isGranted) {
						container.setSize(w, h);
					}

					// apply changes
					container.invalidate();

				}
				// finished
				return isGranted;
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFitToMaximumContentSize(false);
					}
				});
			}
    	}

    	return false;

    }

    public boolean requestShow() {

    	// allowed?
        if(container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// forward?
				if (!isRootManager()) {

					return getParentManager().requestShow();

				}
				// show is allowed
				container.setVisible(true);
				// validate
				return container.isVisible();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestShow();
					}
				});
			}

        }
        return false;
    }

    public boolean requestHide() {

       	// allowed?
        if(container.isDisplayable()) {
			// ensure on EDT
    		if (SwingUtilities.isEventDispatchThread()) {
				// forward?
				if (!isRootManager()) {

					return getParentManager().requestHide();

				}
				// hide is allowed
				container.setVisible(false);
				// validate
				return !container.isVisible();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestHide();
					}
				});
			}

        }
        return false;
    }

    /* ==========================================================
     *  Helper methods
     * ========================================================== */

    private boolean isMoveable() {

    	return (container instanceof IDialog
    			? ((IDialog)container).isMoveable() :
    				(container instanceof Frame || container instanceof Dialog
    					? true : container.getLayout()==null));

    }

    private boolean isResizable() {

    	return (container instanceof Frame
    			? ((Frame)container).isResizable() :
    				(container instanceof Dialog
    					? ((Dialog)container).isResizable() : true));
    }

    private boolean isFitable() {
    	return (isResizable() && (container instanceof IPanel || container instanceof Window));
    }

    private IPanel getPanel(Container container) {

		// fit this?
		if(container instanceof IPanel) {
			// fit to given layout size
    		return (IPanel)container;
		}
		else if(container instanceof JDialog) {
			return getPanel(((JDialog)container).getContentPane());
		}

		// not found
		return null;

    }

    private boolean requestFit(int dw, int dh) {

    	// assume failure
    	boolean isGranted = isFitable();
		// forward
    	if(container instanceof Window) {
    		if(isGranted) ((Window)container).pack();
    	}
    	else {
    		isGranted = requestResize(dw,dh,true);
    	}
    	// finished
    	return isGranted;
    }

    private void resize(int w, int h, boolean isRelative) {
    	// forward to this container
    	resize(container,w,h,isRelative);
		// forward to containers
		for(Container it : containers) {
			resize(it,w,h,isRelative);
		}
    }

    private void resize(Container c, int w, int h, boolean isRelative) {
		if (isRelative) {
			// forward
			c.setSize(c.getWidth() + w, c.getHeight()+ h);
		} else {
			c.setSize(w, h);
		}
		// validate parent?
		if(isRootManager() && !(c.getParent()==null || c.getParent() instanceof Window))
			c.getParent().validate();
		else
			c.invalidate();
    }

}
