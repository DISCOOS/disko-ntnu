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
import java.awt.event.AWTEventListener; 
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent; 

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.dialog.DefaultDialog;
import org.redcross.sar.gui.dialog.ProgressDialog;

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
 * 2.	Kenneth Gulbrandsøy: Added progress information capabilities, Kenneth 
 */ 
public class DiskoGlassPane extends JPanel implements AWTEventListener { 

	private static final long serialVersionUID = 1L;

	private final JFrame m_frame; 
	
    private Point m_point = new Point(); 
    private Component m_recentFocusOwner;
    private boolean m_isLocked = false;
    private ProgressDialog m_progressDialog = null;
 
    public DiskoGlassPane(JFrame frame) { 
    	// forward
        super(); 
        // prepare
        this.m_frame = frame; 
        // initialize GUI
        initialize();
    } 
    
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
 
    public void setPoint(Point point) { 
        this.m_point = point;
    } 
 
    public Point getPoint() { 
        return m_point; 
    } 
    
    public ProgressDialog getProgressDialog() {
    	if(m_progressDialog==null) {
    		m_progressDialog = new ProgressDialog(m_frame,false);
    		m_progressDialog.getProgressPanel().getCancelButton().setCursor(Cursor.getDefaultCursor());
            this.addFocusListener(new FocusAdapter() {
            	@Override
                public void focusGained(FocusEvent e) {
            		// if
            		m_progressDialog.getProgressPanel().getCancelButton().requestFocusInWindow();
                }
            });    		
    	}
    	return m_progressDialog;
    }
 
    public void eventDispatched(AWTEvent e) { 
        if (e instanceof KeyEvent) { 
        	// dispatch event
        	KeyEvent ke = (KeyEvent)e; 
            Component c = ke.getComponent();
            Component root = SwingUtilities.getRoot(c);
        	// do not belong to this application?
            if (!(Utils.inApp(c) || Utils.getApp().getMapManager().isMap(root))) return; 
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
            if (!(Utils.inApp(c) || Utils.getApp().getMapManager().isMap(root))) return; 
            // consume? (allow message dialog boxes)
            if(m_isLocked) {
            	if(Utils.isMessageDialog(root) || Utils.isMessageDialogShown()) {
            		root.repaint();
            	}
            	else
            		me.consume();
            }
            else if (me.getID() == MouseEvent.MOUSE_EXITED && c == m_frame) { 
                m_point = null; 
            } else { 
                MouseEvent converted = 
                	SwingUtilities.convertMouseEvent(c, me, m_frame.getGlassPane()); 
                m_point = converted.getPoint(); 
            } 
            repaint(); 
        } 
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

    protected void paintComponent(Graphics g) {
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        if (rootPane != null) {
            // it is important to call print() instead of paint() here
            // because print() doesn't affect the frame's double buffer
            rootPane.getLayeredPane().print(g);
        }
        if(m_isLocked && false) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setColor(new Color(0, 128, 128, 64));
	        g2.fillRect(0, 0, getWidth(), getHeight());
	        g2.dispose();
        }
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
		}
		setVisible(isLocked);
		return bFlag;
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		// forward
        super.setVisible(isVisible || m_isLocked);
	}
	
	public void setProgressLocationAt(JComponent c) {
		if(c!=null)
			getProgressDialog().setLocationRelativeTo(c, DefaultDialog.POS_CENTER, false, true);
		else
			getProgressDialog().setLocationRelativeTo(m_frame.getLayeredPane(), DefaultDialog.POS_CENTER, false, true);
	}
	
}