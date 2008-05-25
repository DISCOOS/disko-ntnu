package org.redcross.sar.gui.map;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DiskoIcon;
import org.redcross.sar.gui.DiskoHeader;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.command.IDiskoTool;
import org.redcross.sar.mso.data.IMsoObjectIf;

public class DefaultToolPanel extends AbstractToolPanel {

	private static final long serialVersionUID = 1L;
	
	private Insets m_insets = null;
	
	private IMsoObjectIf msoObject = null;
	
	private DiskoHeader captionPanel = null;	
	private DiskoHeader actionsPanel = null;	
	private JButton finishButton = null;
	private JButton cancelButton = null;
	private DiskoIcon finishIcon = null;
	private DiskoIcon cancelIcon = null;
	private JScrollPane m_scrollPane = null;
	private Component m_bodyComponent = null;	
	
	public DefaultToolPanel(IDiskoTool tool) {
		// forward
		this(tool.getCaption(),tool);
	}
	
	public DefaultToolPanel(String caption, IDiskoTool tool) {
		
		// forward
		super(tool);

		// initialize gui
		initialize();
		
		// set caption
		getCaptionPanel().setCaptionText(caption);
		
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		// set box layout
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		// set empty border
		setInsets(5,5,5,5);
		
		// initialize body component
		JPanel panel = new JPanel();
		panel.setBorder(null);
		setBodyComponent(panel);
		
		// build default panel
		add(Box.createVerticalStrut(5));
		add(getCaptionPanel());
		add(Box.createVerticalStrut(5));
		add(getActionsPanel());
		add(Box.createVerticalStrut(5));
		add(getScrollPane());
		add(Box.createVerticalStrut(5));
		
		// ensure correct header size
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// forward
				super.componentResized(e);
				// forward
				setFixedSize();	
		    }
			@Override
			public void componentShown(ComponentEvent e) {
				// forward
				super.componentShown(e);
				// forward
				setFixedSize();	
			}			
		});		
	}

	public void setFixedSize() {
		// forward
		getCaptionPanel().setFixedSize();	
		getActionsPanel().setFixedSize();
	}
	
	private DiskoHeader getCaptionPanel() {
		if (captionPanel == null) {
			try {
				captionPanel = new DiskoHeader();	
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return captionPanel;
	}

	private DiskoHeader getActionsPanel() {
		if (actionsPanel == null) {
			try {
				actionsPanel = new DiskoHeader("Utfør");
				actionsPanel.addButton(getFinishButton(),"finish");
				actionsPanel.addButton(getCancelButton(),"cancel");
				actionsPanel.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						String cmd = e.getActionCommand();
						if("finish".equalsIgnoreCase(cmd)) finish();
						else if("cancel".equalsIgnoreCase(cmd)) cancel();
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return actionsPanel;
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			try {
				cancelButton = DiskoButtonFactory.createButton("GENERAL.CANCEL",ButtonSize.NORMAL);
				cancelIcon = new DiskoIcon(cancelButton.getIcon(),Color.RED,0.4f);
				cancelIcon.setMarked(false);
				cancelButton.setIcon(cancelIcon);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return cancelButton;
	}
	
	private JButton getFinishButton() {
		if (finishButton == null) {
			try {
				finishButton = DiskoButtonFactory.createButton("GENERAL.FINISH",ButtonSize.NORMAL);
				finishIcon = new DiskoIcon(finishButton.getIcon(),Color.GREEN,0.4f);
				finishIcon.setMarked(false);
				finishButton.setIcon(finishIcon);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return finishButton;
	}	
	
	/**
	 * This method initialized the scroll pane
	 * 
	 * @return {@link JScrollPane}
	 */
	private JScrollPane getScrollPane() {
		if(m_scrollPane==null) {
			m_scrollPane = new JScrollPane(getBodyComponent());
			m_scrollPane.setBorder(null);
			m_scrollPane.setOpaque(true);
		}
		return m_scrollPane;
	}
	
	
	/* ===========================================
	 * Public methods
	 * ===========================================
	 */
	
	public boolean isScrollBarVisible(String scrollbar) {
		if(JScrollPane.HORIZONTAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getHorizontalScrollBar().isVisible();
		}
		if(JScrollPane.VERTICAL_SCROLLBAR.equalsIgnoreCase(scrollbar)) {
			return getScrollPane().getVerticalScrollBar().isVisible();
		}
		return false;
	}
	
	public void setScrollBarPolicies(int vert, int horz) {
		getScrollPane().setVerticalScrollBarPolicy(vert);
		getScrollPane().setHorizontalScrollBarPolicy(horz);
	}
	
	public Component getBodyComponent() {
		return m_bodyComponent;
	}
	
	public void setPreferredBodySize(Dimension dimension) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).setPreferredSize(dimension);
	}
	
	public void setBodyLayout(LayoutManager manager) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).setLayout(manager);
	}
		
	public void setBodyBorder(Border border) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).setBorder(border);
	}
	
	public void addBodyChild(Component c) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).add(c);
	}
	
	public void addBodyChild(Component c, int property) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).add(c,property);
	}
	
	public void addBodyChild(Component c, Object property) {
		if(m_bodyComponent instanceof JComponent)
			((JComponent)m_bodyComponent).add(c,property);
	}
	
	public void setBodyComponent(Component body) {
		// update viewport
		getScrollPane().setViewportView(body);
		// update hool
		m_bodyComponent = body;
	}
	
	/**
	 * This method gets the caption icon
	 * 
	 * @return Icon
	 */
	public Icon getCaptionIcon() {
		return getCaptionPanel().getCaptionIcon();
	}	
	
	/**
	 * This method sets the caption icon
	 *
	 */
	public void setCaptionIcon(Icon icon) {
		getCaptionPanel().setCaptionIcon(icon);
	}	
	
	/**
	 * This method gets the caption text
	 *
	 * @return String
	 */	
	public String getCaptionText() {
		return getCaptionPanel().getCaptionText();
	}
	
	/**
	 * This method sets the caption text
	 *
	 */
	public void setCaptionText(String caption) {
		getCaptionPanel().setCaptionText(caption);
	}	
		
	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		getCaptionPanel().setCaptionColor(foreground,background);
	}	
	
	public void setCaptionVisible(boolean isVisible) {
		getCaptionPanel().setVisible(isVisible);
	}
	
	public boolean isCaptionVisible() {
		return getCaptionPanel().isVisible();
	}
	
	/**
	 * This method gets the actions icon
	 * 
	 * @return Icon
	 */
	public Icon getActionsIcon() {
		return getActionsPanel().getCaptionIcon();
	}	
	
	/**
	 * This method sets the actions icon
	 *
	 */
	public void setActionsIcon(Icon icon) {
		getActionsPanel().setCaptionIcon(icon);
	}	
	
	/**
	 * This method gets the actions text
	 *
	 * @return String
	 */	
	public String getActionsText() {
		return getActionsPanel().getCaptionText();
	}
	
	/**
	 * This method sets the actions text
	 *
	 */
	public void setActionsText(String caption) {
		getActionsPanel().setCaptionText(caption);
	}	
		
	/**
	 * This method sets the caption colors
	 *
	 */
	public void setActionsColor(Color foreground,Color background) {
		getActionsPanel().setCaptionColor(foreground,background);
	}	
	
	public void setActionsVisible(boolean isVisible) {
		getActionsPanel().setVisible(isVisible);
	}
	
	public boolean isActionsVisible() {
		return getActionsPanel().isVisible();
	}
	
	/**
	 * This method sets the border color
	 *
	 */
	public void setBorderColor(Color color) {
		getCaptionPanel().setBorderColor(color);
		getActionsPanel().setBorderColor(color);
	}	
	
	public Color getBorderColor() {
		return getCaptionPanel().getBorderColor();
	}

	public Insets getInsets() {
		return m_insets;
	}
	
	public void setInsets(int l, int t, int r, int b) {
		m_insets = new Insets(t,l,b,r);
		setBorder(BorderFactory.createEmptyBorder(
				m_insets.left, m_insets.top, 
				m_insets.right, m_insets.bottom));
	}	
		
	public AbstractButton insertButton(String before, AbstractButton button, String command) {
		return getActionsPanel().insertButton(before,button,command);
	}
	
	public AbstractButton insertButton(String before, String command, String caption) {
		return getActionsPanel().insertButton(before, command, caption);
	}
	
	public AbstractButton addButton(AbstractButton button, String command) {
		return getActionsPanel().addButton(button,command);
	}
	
	public AbstractButton addButton(String command, String caption) {
		return getActionsPanel().addButton(command, caption);
	}
	
	public void removeButton(String command) {
		getActionsPanel().removeButton(command);
	}
	
	public boolean addItem(JComponent item) {
		return getActionsPanel().addItem(item);
	}
	
	public boolean insertItem(String before, JComponent item) {
		return getActionsPanel().insertItem(before,item);
	}
	
	public boolean insertItem(JComponent before, JComponent item) {
		return getActionsPanel().insertItem(before,item);
	}
	
	public boolean removeItem(JComponent item) {
		return getActionsPanel().removeItem(item);
	}	
		
	public boolean containsButton(String command) {
		return getActionsPanel().containsButton(command);
	}
	
	public AbstractButton getButton(String command) {
		return getActionsPanel().getButton(command);
	}
	
	public boolean isButtonVisible(String command) {
		return getActionsPanel().isButtonVisible(command);
	}
	
	public void setButtonVisible(String command, boolean isVisible) {
		getActionsPanel().setButtonVisible(command,isVisible);
	}
	
	public boolean isButtonEnabled(String command) {
		return getActionsPanel().isButtonEnabled(command);
	}
	
	public void setButtonEnabled(String command, boolean isEnabled) {
		getActionsPanel().setButtonEnabled(command,isEnabled);
	}
	
	public void addAction(String command) {
		getActionsPanel().addAction(command);
	}
	
	public void removeAction(String command) {
		getActionsPanel().removeAction(command);
	}
	
	public void addActionListener(ActionListener listener) {
		getActionsPanel().addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		getActionsPanel().removeActionListener(listener);
	}
	
	public boolean doAction(String command) {
		return getActionsPanel().doAction(command);
	}
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		getActionsPanel().addDiskoWorkEventListener(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		getActionsPanel().removeDiskoWorkEventListener(listener);
	}	
	
	public void fireActionEvent(ActionEvent e) {
		getActionsPanel().fireActionEvent(e);		
	}
	
	public void fireOnWorkFinish() {
		getActionsPanel().fireOnWorkFinish();
    }
    
	public void fireOnWorkFinish(DiskoWorkEvent e){
    	getActionsPanel().fireOnWorkFinish(e);
	}
	
	public void fireOnWorkCancel() {
		getActionsPanel().fireOnWorkCancel();
    }
    
	public void fireOnWorkCancel(DiskoWorkEvent e){
    	getActionsPanel().fireOnWorkCancel(e);
	}
	
	public void fireOnWorkChange(Object data) {
		fireOnWorkChange(this, data);
    }
    
	public void fireOnWorkChange(Object source, Object data) {
		getActionsPanel().fireOnWorkChange(source,data);
	}
    
	public void fireOnWorkChange(DiskoWorkEvent e){
    	getActionsPanel().fireOnWorkChange(e);
	}	
	
	/* ===========================================
	 * IPropertyPanel implementation
	 * ===========================================
	 */

	public void update() {
		
		// consume reentry?
		if(!isChangeable()) return;
		
		// suspend events
		setChangeable(false);
		
		// update dirty bit without update
		setDirty(isDirty() || getTool().isDirty(),false);
		
		// update attributes
		finishIcon.setColored(isDirty());
		cancelIcon.setColored(isDirty());
		finishButton.repaint();
		cancelButton.repaint();
			
		// resume events
		setChangeable(true);
		
	}

	@Override
	public IMsoObjectIf getMsoObject() {
		return msoObject;
	}

	@Override
	public void setMsoObject(IMsoObjectIf msoObj) {
		// prepare
		msoObject = msoObj;
		// forward
		super.setMsoObject(msoObj);
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
