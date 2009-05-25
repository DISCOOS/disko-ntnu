package org.redcross.sar.gui.panel;

import java.awt.Color;
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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IFlowListener;
import org.redcross.sar.work.event.FlowEvent;

public class HeaderPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private int m_x = -1;
	private int m_y = -1;

	private Insets m_insets;
	private boolean m_requestMoveTo = false;
	private boolean m_isBorderVisible = true;
	private Color m_borderColor = Color.GRAY;
	private int m_buttonDirection = SwingConstants.RIGHT;
	private ButtonSize m_buttonSize = ButtonSize.NORMAL;

	private JPanel m_captionPanel;
	private JLabel m_iconLabel;
	private JLabel m_captionLabel;
	private ButtonsPanel m_buttons;

	private IPanelManager m_manager;

	private Map<String,ActionEvent> m_actions;
	private List<ActionListener> m_actionListeners;
	private List<IFlowListener> m_workListeners;

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	public HeaderPanel() {
		this("",ButtonSize.NORMAL);
	}

	public HeaderPanel(String caption, ButtonSize buttonSize) {
		this(caption,buttonSize,SwingConstants.RIGHT);
	}

	public HeaderPanel(String caption, ButtonSize buttonSize, int buttonDirection) {
		// prepare
		m_buttonSize = buttonSize;
		m_buttonDirection = buttonDirection;
		m_insets = new Insets(1,1,1,1);
		m_actions = new HashMap<String, ActionEvent>();
		m_actionListeners = new ArrayList<ActionListener>();
		m_workListeners = new ArrayList<IFlowListener>();
		// initialize GUI
		initialize();
		// set caption
		this.setCaptionText(caption);
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
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		if(m_buttonDirection==SwingConstants.RIGHT) {
			this.add(getCaptionPanel());
			this.add(Box.createHorizontalStrut(5));
			this.add(getButtonsPanel());
		}
		else {
			this.add(getButtonsPanel());
			this.add(Box.createHorizontalStrut(5));
			this.add(getCaptionPanel());
		}
		this.setBorderColor(m_borderColor);
		this.setCaptionColor(Color.WHITE,Color.LIGHT_GRAY);
		// limit height, allow any width
		DiskoButtonFactory.setFixedHeight(this, m_buttonSize);

	}

	private JPanel getCaptionPanel() {
		if (m_captionPanel == null) {
			try {
				m_captionPanel = new JPanel();
				m_captionPanel.setOpaque(false);
				m_captionPanel.setBorder(null);
				m_captionPanel.setLayout(new BoxLayout(m_captionPanel,BoxLayout.X_AXIS));
				m_captionPanel.add(getIconLabel());
				m_captionPanel.add(getCaptionLabel());
				// align {left,center} in parent container
				m_captionPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
				m_captionPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
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
			return new DiskoBorder(m_insets.top, m_insets.left,
					m_insets.bottom, m_insets.right, m_borderColor);
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
			// align {left,center} in parent container
			m_iconLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			m_iconLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		}
		return m_iconLabel;
	}

	private JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel();
			m_captionLabel.setOpaque(true);
			m_captionLabel.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
			// align {left,center} in parent container
			m_captionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			m_captionLabel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
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
		caption = Utils.trimHtml(caption);
		getCaptionLabel().setText("<html>"+caption+"</html>");
		setCaptionVisible();
	}

	public void setCaptionAlignment(int align) {
		getCaptionLabel().setHorizontalAlignment(align);
	}

	public void getCaptionAlignment() {
		getCaptionLabel().getHorizontalAlignment();
	}

	/**
	 * This method sets the border color
	 *
	 */
	public Color setBorderColor(Color color) {
		Color old = m_borderColor;
		m_borderColor = color;
		this.setBorder(createBorder());
		return old;
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

	public void setInsets(int t, int l, int b, int r) {
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
				m_buttons = new ButtonsPanel(m_buttonDirection,m_buttonSize);
				m_buttons.setOpaque(true);
				// align {left,center} in parent container
				m_buttons.setAlignmentX(JPanel.LEFT_ALIGNMENT);
				m_buttons.setAlignmentY(JPanel.CENTER_ALIGNMENT);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_buttons;
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

	public void addWorkEventListener(IFlowListener listener) {
		m_workListeners.add(listener);
	}

	public void removeWorkEventListener(IFlowListener listener) {
		m_workListeners.remove(listener);
	}

	public boolean isRootHeader() {
		return m_requestMoveTo;
	}

	public IPanelManager getManager() {
		return m_manager;
	}

	public void setManager(IPanelManager manager, boolean requestMoveTo) {
		// unregister?
		if(this.m_manager!=null && m_requestMoveTo) {
			removeMouseListener(m_mouseAdapter);
			removeMouseMotionListener(m_mouseAdapter);
			m_requestMoveTo = false;
			this.m_manager = null;
		}
		// prepare
		this.m_manager = manager;
		this.m_requestMoveTo = requestMoveTo;
		// register?
		if(this.m_manager!=null && requestMoveTo) {
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
		FlowEvent e = new FlowEvent(source,data,FlowEvent.EVENT_FINISH);
	   	// forward
    	fireOnWorkPerformed(e);
    }

	protected void fireOnWorkCancel(Object source, Object data) {
		// create event
		FlowEvent e = new FlowEvent(source,data,FlowEvent.EVENT_CANCEL);
    	// forward
		fireOnWorkPerformed(e);
    }

	protected void fireOnWorkChange(Object source, Object data) {
		// create event
		FlowEvent e = new FlowEvent(source,data,FlowEvent.EVENT_CHANGE);
		// forward
		fireOnWorkPerformed(e);
	}

	protected void fireOnWorkPerformed(FlowEvent e)
    {
		// notify listeners
		for (IFlowListener it : m_workListeners)
			it.onFlowPerformed(e);
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
			if(m_manager!=null && e.getButton()==0)
				m_manager.requestMoveTo(e.getX()-m_x, e.getY()-m_y, true);

		}

	};

}  //  @jve:decl-index=0:visual-constraint="10,10"
