package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;

public class HeaderPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private int m_x = -1;
	private int m_y = -1;
	
	private Insets m_insets = null;
	private boolean m_isBorderVisible = true;
	private Color m_borderColor = Color.GRAY;
	private ButtonSize m_buttonSize = ButtonSize.NORMAL;
	
	private JPanel m_captionPanel = null;
	private JLabel m_iconLabel = null;
	private JLabel m_captionLabel = null;
	private ButtonsPanel m_buttons = null;

	private IPanelManager manager = null;
	
	private Map<String,ActionEvent> m_actions = null;
	private List<ActionListener> m_actionListeners = null;
	private List<IDiskoWorkListener> m_workListeners = null;

	/* =======================================================
	 * Constructors
	 * ======================================================= */
	
	public HeaderPanel() {
		this("",ButtonSize.NORMAL);
	}
	
	public HeaderPanel(String caption, ButtonSize buttonSize) {
		// prepare
		m_buttonSize = buttonSize;
		m_insets = new Insets(1,1,1,1);
		m_actions = new HashMap<String, ActionEvent>();
		m_actionListeners = new ArrayList<ActionListener>();
		m_workListeners = new ArrayList<IDiskoWorkListener>();
		// initialize GUI
		initialize();
		// set caption
		this.setCaptionText(caption);
		// set height
		this.onResize();
		// set caption color
		this.setCaptionColor(Color.WHITE,Color.LIGHT_GRAY);
	}
	
	/* =======================================================
	 * Private methods
	 * ======================================================= */
	
	/**
	 * This method initializes the panel
	 * 	
	 */
	private void initialize() {	
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		this.add(getCaptionPanel(),BorderLayout.CENTER);
		this.add(getButtonsPanel(),BorderLayout.EAST);		
		this.setBorderColor(m_borderColor);
	}
	
	private JPanel getCaptionPanel() {
		if (m_captionPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setAlignment(FlowLayout.LEFT);
				fl.setHgap(5);
				fl.setVgap(0);
				m_captionPanel = new JPanel();
				m_captionPanel.setOpaque(false);
				m_captionPanel.setBorder(null);
				m_captionPanel.setLayout(fl);
				m_captionPanel.add(getIconLabel(),null);
				m_captionPanel.add(getCaptionLabel(),null);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_captionPanel;		
	}
	
	
	private void setCaptionVisible() {
		getIconLabel().setVisible(getCaptionIcon() !=null);
		getCaptionLabel().setVisible(getCaptionText() !=null 
				&& getCaptionText().length()>0);
	}
	
	private DiskoBorder createBorder() {
		// create?
		if(m_isBorderVisible) {
			// create border
			return new DiskoBorder(m_insets.left, m_insets.top, m_insets.right, 
					m_insets.bottom,m_borderColor);		
		}
		else {
			return null;
		}
	}
		
	/**
	 * This method initializes iconLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getIconLabel() {
		if (m_iconLabel == null) {
			m_iconLabel = new JLabel();
			m_iconLabel.setOpaque(true);
			m_iconLabel.setVisible(false);
			m_iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return m_iconLabel;
	}
	
	private JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel("");
			m_captionLabel.setOpaque(true);
			m_captionLabel.setVerticalAlignment(SwingConstants.CENTER);
		}
		return m_captionLabel;
	}
	
	/* =======================================================
	 * Public methods
	 * ======================================================= */

	/**
	 * This method returns the button size that should be used when adding and inserting buttons 
	 */
	public ButtonSize getButtonSize() {
		return m_buttonSize;
	}
	
	/**
	 * This method gets the caption icon
	 * 
	 * @return Icon
	 */
	public Icon getCaptionIcon() {
		return getIconLabel().getIcon();
	}	
	
	/**
	 * This method sets the caption icon
	 *
	 */
	public void setCaptionIcon(Icon icon) {
		getIconLabel().setIcon(icon);
		setCaptionVisible();
		onResize();
	}	
	
	/**
	 * This method gets the caption text
	 *
	 * @return String
	 */	
	public String getCaptionText() {
		return getCaptionLabel().getText();
	}
	
	/**
	 * This method sets the caption text
	 *
	 */
	public void setCaptionText(String caption) {
		getCaptionLabel().setText(caption);
		setCaptionVisible();
		onResize();
	}	
	
	/**
	 * This method sets the border color
	 *
	 */
	public void setBorderColor(Color color) {
		m_borderColor = color;
		this.setBorder(createBorder());
	}	
	
	public Color getBorderColor() {
		return m_borderColor;
	}
	
	public boolean isBorderVisible() {
		return m_isBorderVisible;
	}
	
	public void setBorderVisible(boolean isVisible) {
		m_isBorderVisible = isVisible;
		this.setBorder(createBorder());
	}	
	
	public Insets getInsets() {
		return m_insets;
	}
	
	public void setInsets(int l, int t, int r, int b) {
		m_insets = new Insets(t,l,b,r);
		this.setBorder(createBorder());
	}	
	
	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		this.setForeground(foreground);
		this.setBackground(background);
		getIconLabel().setForeground(foreground);
		getIconLabel().setBackground(background);
		getButtonsPanel().setForeground(foreground);
		getButtonsPanel().setBackground(background);
		getCaptionLabel().setForeground(foreground);
		getCaptionLabel().setBackground(background);
	}	
		
	/**
	 * This method initializes cancelButton
	 *
	 * @return javax.swing.JButton
	 */
	public ButtonsPanel getButtonsPanel() {
		if (m_buttons == null) {
			try {
				m_buttons = new ButtonsPanel(FlowLayout.RIGHT,m_buttonSize);
				m_buttons.setOpaque(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_buttons;
	}				

	public void onResize() {
		Container parent = getParent();
		if(parent != null) {
			Insets insets = parent.getInsets();
			int w = parent.getWidth() - insets.left - insets.right;
			if(w>0) {
				Icon icon = getCaptionIcon();
				int iw = icon!=null ? icon.getIconWidth() : 0;
				int ih = icon!=null ? icon.getIconHeight() : 0;
				int bw = getButtonsPanel().getTotalButtonWidth();
				int bh = getButtonsPanel().getMaxButtonHeigth();
				int	cw = Math.max(w-bw-iw,25);
				int	h = Math.max(Math.max(bh,ih),25);
				// set fixed size of caption
				Utils.setFixedSize(getIconLabel(),iw,h);
				Utils.setFixedSize(getCaptionLabel(),cw-50,h);
				Utils.setFixedSize(getButtonsPanel(),bw,h);
				Utils.setFixedSize(this,w,h+2);
			}
		}
	}
	
	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		return getButtonsPanel().insertButton(before,button,command);
	}
	
	public AbstractButton insertButton(String before, String command, String caption) {
		return getButtonsPanel().insertButton(before, command, caption);
	}
	
	public AbstractButton addButton(AbstractButton button, String command) {
		return getButtonsPanel().addButton(button,command);
	}
	
	public AbstractButton addButton(String command, String caption) {
		return getButtonsPanel().addButton(command, caption);
	}
	
	public void removeButton(String command) {
		getButtonsPanel().removeButton(command);
	}
	
	public boolean addItem(JComponent item) {
		return getButtonsPanel().addItem(item);
	}
	
	public boolean insertItem(String before, JComponent item) {
		return getButtonsPanel().insertItem(before,item);
	}
	
	public boolean insertItem(JComponent before, JComponent item) {
		return getButtonsPanel().insertItem(before,item);
	}
	
	public boolean removeItem(JComponent item) {
		return getButtonsPanel().removeItem(item);
	}	
	
	public boolean containsButton(String command) {
		return getButtonsPanel().containsButton(command);
	}
	
	public AbstractButton getButton(String command) {
		return getButtonsPanel().getButton(command);
	}
	
	public boolean isButtonVisible(String command) {
		return getButtonsPanel().isButtonVisible(command);
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		getButtonsPanel().setButtonVisible(command,isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return getButtonsPanel().isButtonEnabled(command);
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		getButtonsPanel().setButtonEnabled(command,isEnabled);
	}
	
	public void addAction(String command) {
		m_actions.put(command,new ActionEvent(this,ActionEvent.ACTION_PERFORMED,command));
	}
	
	public void removeAction(String command) {
		m_actions.remove(command);
	}
	
	public void addActionListener(ActionListener listener) {
		m_actionListeners.add(listener);
		getButtonsPanel().addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		m_actionListeners.remove(listener);
		getButtonsPanel().removeActionListener(listener);
	}
	
	public boolean doAction(String command) {
		if(!getButtonsPanel().doAction(command)) {
			if(m_actions.containsKey(command)) {
				fireActionEvent(m_actions.get(command));
				return true;
			}
			return false;
		}
		return true;
	}
	
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(getButtonsPanel().containsButton(cmd))
			getButtonsPanel().doAction(cmd);
		else if (m_actions.containsKey(cmd))
			fireActionEvent(e);
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		m_workListeners.add(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		m_workListeners.remove(listener);
	}
	
	public IPanelManager getManager() {
		return manager;
	}
	
	public void setManager(IPanelManager manager) {
		// unregister?
		if(this.manager!=null) {
			removeMouseListener(m_mouseAdapter);
			removeMouseMotionListener(m_mouseAdapter);
		}
		// prepare
		this.manager = manager;
		// register?
		if(this.manager!=null) {
			addMouseListener(m_mouseAdapter);
			addMouseMotionListener(m_mouseAdapter);
		}
	}
	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		for(int i=0;i<getComponentCount();i++) {
  			getComponent(i).setEnabled(isEnabled);
  		}
  	}
  	
	/* =======================================================
	 * Protected methods
	 * ======================================================= */

	protected void fireActionEvent(ActionEvent e) {
		for(ActionListener it: m_actionListeners)
			it.actionPerformed(e);
	}
		
	protected void fireOnWorkFinish(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_FINISH);
	   	// forward
    	fireOnWorkPerformed(e);
    }
    
	protected void fireOnWorkCancel(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CANCEL);
    	// forward
		fireOnWorkPerformed(e);
    }
    
	protected void fireOnWorkChange(Object source, Object data) {
		// create event
		DiskoWorkEvent e = new DiskoWorkEvent(source,data,DiskoWorkEvent.EVENT_CHANGE);
		// forward
		fireOnWorkPerformed(e);    			
	}
	
	protected void fireOnWorkPerformed(DiskoWorkEvent e)
    {
		// notify listeners
		for (IDiskoWorkListener it : m_workListeners)
			it.onWorkPerformed(e);
	}
	
	/* =======================================================
	 * Anonumous classes
	 * ======================================================= */
	
	private final MouseAdapter m_mouseAdapter = new MouseAdapter() {

        public void mouseReleased(MouseEvent e) {
            m_x = e.getX();
            m_y = e.getY();
        }
        
        public void mousePressed(MouseEvent e){
        	m_x = e.getX();
        	m_y = e.getY();
        }      

		public void mouseDragged(MouseEvent e) {
			if(manager!=null && e.getButton()==0)
				manager.requestMoveTo(e.getX()-m_x, e.getY()-m_y);
				
		}

	};
		
}  //  @jve:decl-index=0:visual-constraint="10,10"
