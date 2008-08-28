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

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.redcross.sar.gui.DiskoBorder;
import org.redcross.sar.gui.IChangeable;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;

public class BasePanel extends AbstractPanel {

	private static final long serialVersionUID = 1L;

	public static final int HORIZONTAL_SCROLLBAR_ALWAYS = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	public static final int HORIZONTAL_SCROLLBAR_AS_NEEDED = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	public static final int HORIZONTAL_SCROLLBAR_NEVER = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
	public static final int VERTICAL_SCROLLBAR_ALWAYS = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
	public static final int VERTICAL_SCROLLBAR_AS_NEEDED = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
	public static final int VERTICAL_SCROLLBAR_NEVER = JScrollPane.VERTICAL_SCROLLBAR_NEVER;

	private Insets insets = null;
	private boolean isBorderVisible = true;
	private Color borderColor = Color.GRAY;
	private ButtonSize buttonSize = ButtonSize.NORMAL;
	
	private HeaderPanel headerPanel = null;
	private JScrollPane scrollPane = null;
	private Component bodyComponent = null;
	
	/* ===========================================
	 * Constructors
	 * ===========================================
	 */
	
	public BasePanel() {
		this("",ButtonSize.NORMAL);
	}
	
	public BasePanel(String caption) {
		this(caption,ButtonSize.NORMAL);
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
		// prepare this
		this.setLayout(new BorderLayout());		
		this.add(getHeaderPanel(),BorderLayout.NORTH);
		this.add(getScrollPane(),BorderLayout.CENTER);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// forward
				onResize();	
		    }
			@Override
			public void componentShown(ComponentEvent e) {
				// forward
				onResize();	
			}			
		});		
		this.setBorderColor(borderColor);
	}	
	
	private Border createBorder() {
		// create?
		if(isBorderVisible) {
			// create border
			return new DiskoBorder(insets.left, insets.top, insets.right, 
					insets.bottom,borderColor);		
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
	
	public void onResize() {
		getHeaderPanel().onResize();
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
				headerPanel.setInsets(0,0,0,1);
				headerPanel.addDiskoWorkEventListener(new IDiskoWorkListener() {

					public void onWorkPerformed(DiskoWorkEvent e) {
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
	public void setBorderColor(Color color) {
		borderColor = color;
		this.setBorder(createBorder());
		this.getHeaderPanel().setBorderColor(color);
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
				getHeaderPanel().setInsets(0, 0, 0, 1);
			else
				getHeaderPanel().setInsets(1, 1, 1, 1);
		}
		else
			getHeaderPanel().setBorderVisible(isVisible);

	}
	
	public Insets getInsets() {
		return insets;
	}
	
	public void setInsets(int l, int t, int r, int b) {
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
			scrollPane = new JScrollPane(getBodyComponent());
			scrollPane.setBorder(null);
			scrollPane.setViewportBorder(null);
			scrollPane.setOpaque(true);
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
	
	public void setScrollBarPolicies(int vert, int horz) {
		getScrollPane().setVerticalScrollBarPolicy(vert);
		getScrollPane().setHorizontalScrollBarPolicy(horz);
	}
	
	public Component getBodyComponent() {
		return bodyComponent;
	}
	
	public void setPreferredBodySize(Dimension dimension) {
		if(bodyComponent instanceof JComponent)
			((JComponent)bodyComponent).setPreferredSize(dimension);
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
		// update viewport
		getScrollPane().setViewportView(body);
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
	 * ===========================================
	 */
	
	public void update() { /* Override this */ }
	
	public IPanelManager getManager() {
		return getHeaderPanel().getManager();
	}
	
	public void setManager(IPanelManager manager) {
		// forward
		getHeaderPanel().setManager(manager);
	}
	
	public void addDiskoWorkListener(IDiskoWorkListener listener) {
		getHeaderPanel().addDiskoWorkEventListener(listener);
	}
	
	public void removeDiskoWorkListener(IDiskoWorkListener listener) {
		getHeaderPanel().removeDiskoWorkEventListener(listener);
	}
	
	public void addActionListener(ActionListener listener) {
		getHeaderPanel().addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		getHeaderPanel().removeActionListener(listener);
	}
	
	/* ===========================================
	 * ActionListener implementation
	 * ===========================================
	 */
	
	public void actionPerformed(ActionEvent e) {
		getHeaderPanel().actionPerformed(e);
	}
	
	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */
			
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
    
	protected void fireOnWorkPerformed(DiskoWorkEvent e){
		getHeaderPanel().fireOnWorkPerformed(e);
	}	
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
