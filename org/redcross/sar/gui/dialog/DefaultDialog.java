package org.redcross.sar.gui.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JDialog;
import javax.swing.Timer;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.panel.AbstractPanel;
import org.redcross.sar.gui.panel.BasePanel;
import org.redcross.sar.gui.panel.BaseToolPanel;
import org.redcross.sar.gui.panel.IPanel;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;

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

	private Component snapToBuddy = null;
	
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
			snapTo(false);
		}
		public void componentShown(ComponentEvent e) {
			snapTo(false);
		}
	};
	
	/* ==========================================================
	 *  Constructors
	 * ========================================================== */
	
	/**
	 * Constructor 
	 * 
	 * @param owner
	 */
	public DefaultDialog(Frame owner) {
		// forward
		super(owner);
        // listen to component events from frame
        owner.addComponentListener(new ComponentListener() {
			public void componentHidden(ComponentEvent e) {
				setVisible(false);
			}
			public void componentMoved(ComponentEvent e) {
				snapTo(false);
			}
			public void componentResized(ComponentEvent e) {
				snapTo(false);
			}
			public void componentShown(ComponentEvent e) {
				snapTo(false);
			}
		});        
        
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
		        
		// initialize ui
		initialize();
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
		if (snapToBuddy == null || !snapToBuddy.isShowing()) return;
		
		// initialize size?
		if (update || width == -1 || height == -1) {
			width  = getWidth() !=0 ? getWidth() : -1;
			height = getHeight() !=0 ? getHeight() : -1;
		}
		
		// initialize
		int offset = 2;
		int x = snapToBuddy.getLocationOnScreen().x;
		int y = snapToBuddy.getLocationOnScreen().y;
		int w = 0;
		int h = 0;
		int bw = snapToBuddy.getWidth();
		int bh = snapToBuddy.getHeight();
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
		if (sizeToFit && w > 0 && h > 0)
			Utils.setFixedSize(this, w, h);
		// get screen size
		Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		// ensure visible in both directions
		x = (x + w > screen.width) ? screen.width - w : x; 
		y = (y + h > screen.height) ? screen.height - h : y; 
		// update location
		this.setLocation(x, y);
		// apply location change
		this.validate();
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
	
	public void setLocationRelativeTo(Component buddy, int policy, boolean sizeToFit, boolean snapToInside) {
		
		// unregister?
		if(snapToBuddy!=null) {
			// remove listener
			snapToBuddy.removeComponentListener(listener);			
		}
		
		// prepare
		this.policy = policy;
		this.sizeToFit = sizeToFit;
		this.snapToInside = snapToInside;
		this.snapToBuddy = buddy;
	
		// register?
		if(snapToBuddy!=null) {
			// add listener
			snapToBuddy.addComponentListener(listener);
			// forward
			snapTo(false);
		}
		
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
			return ((IPanel)getContentPane()).getMsoObject();
		}
		return null;
	}
	
	public void setMsoObject(IMsoObjectIf msoObj) {
		if(isWorkSupported())
			((IPanel)getContentPane()).setMsoObject(msoObj);
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
	
	public void addDiskoWorkListener(IDiskoWorkListener listener) {
		if(isWorkSupported())
			((IPanel)getContentPane()).addDiskoWorkListener(listener);
	}

	public void removeDiskoWorkListener(IDiskoWorkListener listener) {
		if(isWorkSupported())
			((IPanel)getContentPane()).removeDiskoWorkListener(listener);
	}	
	
	/* ==========================================================
	 *  IPanelManager interface implementation
	 * ========================================================== */

	public boolean requestMoveTo(int dx, int dy) {
		if(isMoveable()) {
			int x = getLocation().x;
	        int y = getLocation().y;
	        setLocation(x+dx,y+dy);
	        return true;
		}
		return false;
	}

	public boolean requestResize(int w, int h) {
		if(isResizable()) {
			setSize(w, h);
			snapTo();
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
		fireOnWorkPerformed(new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_FINISH));
    }
    
	protected void fireOnWorkCancel(Object source, Object data) {
		fireOnWorkPerformed(new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CANCEL));
    }
    
	protected void fireOnWorkChange(Object data) {
		fireOnWorkPerformed(new DiskoWorkEvent(this,data,DiskoWorkEvent.EVENT_CHANGE));
    }
    
	protected void fireOnWorkChange(Object source, Object data) {
		fireOnWorkPerformed(new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CHANGE));
	}
    
	protected void fireOnWorkPerformed(DiskoWorkEvent e) {
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
