package org.redcross.sar.gui;

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
import java.util.EnumSet;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;

public abstract class AbstractDiskoPanel extends JPanel implements IDiskoPanel {

	private static final long serialVersionUID = 1L;

	public static final int HORIZONTAL_SCROLLBAR_ALWAYS = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
	public static final int HORIZONTAL_SCROLLBAR_AS_NEEDED = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
	public static final int HORIZONTAL_SCROLLBAR_NEVER = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER;
	public static final int VERTICAL_SCROLLBAR_ALWAYS = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
	public static final int VERTICAL_SCROLLBAR_AS_NEEDED = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
	public static final int VERTICAL_SCROLLBAR_NEVER = JScrollPane.VERTICAL_SCROLLBAR_NEVER;

	private boolean isDirty = false;	
	private int consumeCount = 0;
	
	private Insets m_insets = null;
	private boolean m_isBorderVisible = true;
	private Color m_borderColor = Color.GRAY;
	
	private DiskoHeader m_headerPanel = null;
	private JScrollPane m_scrollPane = null;
	private Component m_bodyComponent = null;
	
	private EnumSet<MsoClassCode> myInterests = null;
	
	/* ===========================================
	 * Constructors
	 * ===========================================
	 */
	
	public AbstractDiskoPanel() {
		this("");
	}
	
	public AbstractDiskoPanel(String caption) {
		// prepare
		m_insets = new Insets(1,1,1,1);
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
				setFixedSize();	
		    }
			@Override
			public void componentShown(ComponentEvent e) {
				// forward
				setFixedSize();	
			}			
		});		
		this.setBorderColor(m_borderColor);
	}	
	
	private Border createBorder() {
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
		
	/* ===========================================
	 * Public methods
	 * ===========================================
	 */
	
	public void setFixedSize() {
		getHeaderPanel().setFixedSize();		
	}
	
	/**
	 * This method initializes headerPanel
	 *
	 * @return {@link DiskoHeader}
	 */
	public DiskoHeader getHeaderPanel() {
		if (m_headerPanel == null) {
			try {
				m_headerPanel = new DiskoHeader();
				m_headerPanel.setInsets(0,0,0,1);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return m_headerPanel;
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
		m_borderColor = color;
		this.setBorder(createBorder());
		this.getHeaderPanel().setBorderColor(color);
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
		this.setHeaderBorderVisible(isHeaderBorderVisible());
	}	

	public boolean isHeaderBorderVisible() {
		return getHeaderPanel().isBorderVisible();
	}
	
	public void setHeaderBorderVisible(boolean isVisible) {
		if(isVisible) {
			if(m_isBorderVisible)
				getHeaderPanel().setInsets(0, 0, 0, 1);
			else
				getHeaderPanel().setInsets(1, 1, 1, 1);
		}
		else
			getHeaderPanel().setBorderVisible(isVisible);

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
		if(m_scrollPane==null) {
			m_scrollPane = new JScrollPane(getBodyComponent());
			m_scrollPane.setBorder(null);
			m_scrollPane.setOpaque(true);
		}
		return m_scrollPane;
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
		// update borders
		setBorderColor(m_borderColor);
	}
	
	public boolean isBodyEnabled() {
		return m_bodyComponent.isEnabled();
	}
	
	public void setBodyEnabled(Boolean isEnabled) {
		// update
		getScrollPane().setEnabled(isEnabled);
		m_bodyComponent.setEnabled(isEnabled);
	}
	
	public void addActionListener(ActionListener listener) {
		getHeaderPanel().addActionListener(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		getHeaderPanel().removeActionListener(listener);
	}
	
	public boolean doAction(String command) {
		return getHeaderPanel().doAction(command);
	}
	
	public void fireActionEvent(ActionEvent e) {
		getHeaderPanel().fireActionEvent(e);		
	}
	
	
	public void addDiskoWorkEventListener(IDiskoWorkListener listener) {
		getHeaderPanel().addDiskoWorkEventListener(listener);
	}
	
	public void removeDiskoWorkEventListener(IDiskoWorkListener listener) {
		getHeaderPanel().removeDiskoWorkEventListener(listener);
	}
	
	public void fireOnWorkFinish() {
		getHeaderPanel().fireOnWorkFinish();
    }
    
	public void fireOnWorkFinish(DiskoWorkEvent e){
		getHeaderPanel().fireOnWorkFinish(e);
	}
	
	public void fireOnWorkCancel() {
		getHeaderPanel().fireOnWorkCancel();
    }
    
	public void fireOnWorkCancel(DiskoWorkEvent e){
		getHeaderPanel().fireOnWorkCancel(e);
	}
	
	public void fireOnWorkChange(Object data) {
		fireOnWorkChange(this, data);
    }
    
	public void fireOnWorkChange(Object source, Object data) {
		getHeaderPanel().fireOnWorkChange(source,data);
	}
    
	public void fireOnWorkChange(DiskoWorkEvent e){
		getHeaderPanel().fireOnWorkChange(e);
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject) {
		return myInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
		// get flags
		int mask = e.getEventTypeMask();
        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
		
        // get mso object
        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
        
        // add object?
		if (createdObject) {
			msoObjectCreated(msoObj,mask);
		}
		// is object modified?
		if ( (addedReference || removedReference || modifiedObject)) {
			msoObjectChanged(msoObj,mask);
		}
		// delete object?
		if (deletedObject) {
			msoObjectDeleted(msoObj,mask);		
		}
	}

	/* ===========================================
	 * IDiskoPanel implementation
	 * ===========================================
	 */
	
	public abstract void update();
	
	public abstract IMsoObjectIf getMsoObject();
	
	public void setMsoObject(IMsoObjectIf msoObj) {
		// forward
		update();
	}
	
	public void reset() { 
		// reset flag?
		if(isDirty) setDirty(false);
	}
	
	public boolean finish() {
		// forward
		boolean bFlag = isDirty();
		// reset flag?
		if(isDirty) setDirty(false);
		// finished
		return bFlag;
	}
	
	public boolean cancel() {
		// forward
		boolean bFlag = isDirty();
		// consume change events
		setChangeable(false);
		// forward
		reset();
		// resume change events
		setChangeable(true);
		// finished
		return bFlag;
	}
	
	public boolean isDirty() {
		return isDirty;
	}
	
	public void setDirty(boolean isDirty) {
		setDirty(isDirty,true);
	}
	
	public boolean isChangeable() {
		return (consumeCount==0);
	}
	
	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			consumeCount++;
		else if(consumeCount>0)
			consumeCount--;
	}
	
	
	/* ===========================================
	 * Protected methods
	 * ===========================================
	 */
	
	protected void setDirty(boolean isDirty, boolean update) {
		this.isDirty = isDirty;
		if(update) update();		
	}
	
	protected void msoObjectCreated(IMsoObjectIf msoObject, int mask) { /*NOP*/ }
	
	protected void msoObjectChanged(IMsoObjectIf msoObject, int mask) { /*NOP*/ }

	protected void msoObjectDeleted(IMsoObjectIf msoObject, int mask) { /*NOP*/ }
			
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
