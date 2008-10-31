package org.redcross.sar.gui.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.Timer;

import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.IMsoHolder;
import org.redcross.sar.gui.panel.AbstractPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.BaseToolPanel;
import org.redcross.sar.gui.panel.IPanel;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.event.WorkEvent;
import org.redcross.sar.thread.event.IWorkListener;
import org.redcross.sar.util.Utils;

public class DefaultDialog extends JDialog implements IDialog {

	private static final long serialVersionUID = 1L;

	private static int PAUSE_MILLIS = 100;
	private static int MILLIS_TO_SHOW = 1000;

	public static final int POS_WEST   = 1;
	public static final int POS_NORTH  = 2;
	public static final int POS_EAST   = 3;
	public static final int POS_SOUTH  = 4;
	public static final int POS_CENTER = 5;

	private int policy = POS_CENTER;
	private boolean sizeToFit = false;
	private boolean snapToInside = true;

	private int width  = -1;
	private int height = -1;

	private Component snapToComponent;

	private boolean isMoveable = true;
	private boolean isEscapeable = true;

	private final DialogWorker m_worker = new DialogWorker(MILLIS_TO_SHOW);

	final private ComponentListener listener = new ComponentListener() {
		public void componentHidden(ComponentEvent e) {
			setVisible(false);
		}
		public void componentMoved(ComponentEvent e) {
			snapTo(false);
		}
		public void componentResized(ComponentEvent e) {
			snapTo(true);
		}
		public void componentShown(ComponentEvent e) {
			snapTo(false);
		}
	};

	/* ==========================================================
	 *  Constructors
	 * ========================================================== */

	public DefaultDialog() {
		this(Utils.getApp().getFrame());
	}

	public DefaultDialog(Frame owner) {

		// forward
		super(owner);

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

		// initialize GUI
		initialize();

		// set default location
		setLocationRelativeTo(owner,POS_CENTER,false,false);

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
	}


	private void snapTo(boolean update) {

		// position not defined?
		if (snapToComponent == null || !snapToComponent.isShowing()) return;

		// initialize size?
		if (update || width == -1 || height == -1) {
			width  = getWidth() !=0 ? getWidth() : -1;
			height = getHeight() !=0 ? getHeight() : -1;
		}

		// initialize
		int offset = 2;
		int x = snapToComponent.getLocationOnScreen().x;
		int y = snapToComponent.getLocationOnScreen().y;
		int w = 0;
		int h = 0;
		int bw = snapToComponent.getWidth();
		int bh = snapToComponent.getHeight();
		// get position data
		switch (policy) {
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
		if (sizeToFit && w > 0 && h > 0) {
			Utils.setFixedSize(this, w, h);
			this.pack();
		}
		// get screen size
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		// ensure visible in both directions
		x = (x + w > screen.width) ? screen.width - w : x;
		y = (y + h > screen.height) ? screen.height - h : y;
		// update location
		super.setLocation(x, y);
		// apply location change
		this.validate();
	}

	private void registerSnapToComponent(Component c) {
		// register?
		if(c!=null) {
			// prepare
			snapToComponent = c;
			// add listener
			c.addComponentListener(listener);
			// forward?
			if(isVisible()) snapTo(true);
		}
	}

	private void unregisterSnapToComponent() {
		// unregister?
		if(snapToComponent!=null) {
			// remove listener
			snapToComponent.removeComponentListener(listener);
		}
		snapToComponent = null;
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
			p.setManager(null);
		}
		// forward
		super.setContentPane(c);
		// is instance of IPanel?
		if(c instanceof IPanel) {
			IPanel p = (IPanel)c;
			p.setManager(this);
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
			snapTo(false);
		}
		// forward
		super.setVisible(isVisible);
	}

	/* ==========================================================
	 *  IDialog interface implementation
	 * ========================================================== */

	public void snapTo() {
		// forward
		snapTo(true);
	}

	public boolean isMoveable() {
		return isMoveable;
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

	public void setLocationRelativeTo(Component c, int policy, boolean sizeToFit, boolean snapToInside) {

		// unregister
		unregisterSnapToComponent();

		// prepare
		this.policy = policy;
		this.sizeToFit = sizeToFit;
		this.snapToInside = snapToInside;

		// forward
		registerSnapToComponent(c);
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

	public void addWorkListener(IWorkListener listener) {
		if(isWorkSupported())
			((IPanel)getContentPane()).addWorkListener(listener);
	}

	public void removeWorkListener(IWorkListener listener) {
		if(isWorkSupported())
			((IPanel)getContentPane()).removeWorkListener(listener);
	}

	/* ==========================================================
	 *  IPanelManager interface implementation
	 * ========================================================== */

	public boolean requestMoveTo(int dx, int dy) {
		if(isMoveable()) {
			int x = getLocation().x;
	        int y = getLocation().y;
	        super.setLocation(x+dx,y+dy);
	        return true;
		}
		return false;
	}

	public boolean requestResize(int w, int h) {
		if(isResizable() && !sizeToFit) {
			setSize(w, h);
			snapTo();
			pack();
			return true;
		}
		return false;

	}

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

	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */

	protected void fireOnWorkFinish(Object source, Object data) {
		fireOnWorkPerformed(new WorkEvent(source,data,WorkEvent.EVENT_FINISH));
    }

	protected void fireOnWorkCancel(Object source, Object data) {
		fireOnWorkPerformed(new WorkEvent(source,data,WorkEvent.EVENT_CANCEL));
    }

	protected void fireOnWorkChange(Object data) {
		fireOnWorkPerformed(new WorkEvent(this,data,WorkEvent.EVENT_CHANGE));
    }

	protected void fireOnWorkChange(Object source, Object data) {
		fireOnWorkPerformed(new WorkEvent(source,data,WorkEvent.EVENT_CHANGE));
	}

	protected void fireOnWorkPerformed(WorkEvent e) {
		if(isWorkSupported())
			((IPanel)getContentPane()).onWorkPerformed(e);
    }

	/*========================================================
  	 * Inner classes
  	 *========================================================
  	 */

	class DialogWorker implements ActionListener {

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
