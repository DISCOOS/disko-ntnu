package org.redcross.sar.gui.attribute;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.thread.event.DiskoWorkEvent;
import org.redcross.sar.thread.event.IDiskoWorkListener;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractDiskoAttribute extends JPanel implements IDiskoAttribute {
	
	private static final long serialVersionUID = 1L;
	
	protected static final int DEFAULT_CAPTION_WIDTH = 80;
	protected static final int DEFAULT_MAXIMUM_HEIGHT = 25;
	
	protected JLabel m_captionLabel;
	protected Component m_component;
	protected AbstractButton m_button;
	protected Component m_buttonStrut;
	
	protected String m_caption;
	
	protected IAttributeIf<?> m_attribute;
	
	protected boolean m_isDirty = false;
	protected boolean m_autoSave = false;
	protected boolean m_isEditable = false;
	
	private int m_isConsume = 0;
	
	private int m_fixedWidth;
	private int m_fixedHeight;
	
	
	private List<IDiskoWorkListener> listeners = null;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	protected AbstractDiskoAttribute(String name, String caption, boolean isEditable) {
		this(name,caption,isEditable,DEFAULT_CAPTION_WIDTH,DEFAULT_MAXIMUM_HEIGHT,null);
	}
	
	protected AbstractDiskoAttribute(String name, String caption, boolean isEditable, int width, int height, Object value) {
		// prepare
		listeners = new ArrayList<IDiskoWorkListener>();
		// initialize GUI
		initialize();
		// update
		setName(name);
		setCaption(caption);
		setValue(value);
		setEditable(isEditable);		
		setFixedCaptionWidth(width);
		setFixedHeight(height);
		//setBorder(BorderFactory.createLineBorder(Color.RED)); // USED TO DEBUG LAYOUT PROBLEMS
	}
	
	protected AbstractDiskoAttribute(IAttributeIf<?> attribute, String caption, boolean isEditable) {
		// forward
		this(attribute.getName(),caption,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	protected AbstractDiskoAttribute(IAttributeIf<?> attribute, 
			String caption, boolean isEditable, 
			int width, int height) {
		// forward
		this(attribute.getName(),caption,isEditable,width,height,null);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		load();		
	}
	
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel(m_caption);
			m_captionLabel.setVerticalAlignment(SwingConstants.TOP);
			m_captionLabel.setLabelFor(getComponent());
			m_captionLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			m_captionLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		}
		return m_captionLabel;
	}
	
	protected void setIsNotWorking() {
		if(m_isConsume>0) m_isConsume--;
	}
	
	protected void fireOnWorkChange() {
		if(m_autoSave) {
			MsoModelImpl.getInstance().suspendClientUpdate();
			if(save()) {
				fireOnWorkChange(new DiskoWorkEvent(this,getValue(),DiskoWorkEvent.EVENT_FINISH));
			}
			else {
				// notify change instead
				m_isDirty = true;
				fireOnWorkChange(new DiskoWorkEvent(this,getValue(),DiskoWorkEvent.EVENT_CHANGE));				
			}
			MsoModelImpl.getInstance().resumeClientUpdate();
		}
		else {			
			m_isDirty = true;
			fireOnWorkChange(new DiskoWorkEvent(this,getValue(),DiskoWorkEvent.EVENT_CHANGE));
		}
	}
			
	protected void fireOnWorkChange(DiskoWorkEvent e) {
		// forward
		for(IDiskoWorkListener it: listeners) {
			it.onWorkPerformed(e);
		}
	}
		
	protected IDiskoMap getInstalledMap() {
		// try to get map from current 
		IDiskoWpModule module = Utils.getApp().getCurrentRole().getCurrentDiskoWpModule();
		if(module!=null) {
			if(module.isMapInstalled())
				return module.getMap();
		}
		// no map available
		return null;
	}
	
	/**
	 * This method initializes Button	
	 * 	
	 * @return {@link AbstractButton}
	 */
	protected AbstractButton getButton() {
		if (m_button == null) {
			m_button = DiskoButtonFactory.createButton("GENERAL.EDIT", ButtonSize.SMALL);
			m_button.setVisible(false);
		}
		return m_button;
	}			
	
	/*==================================================================
	 * Private methods
	 *================================================================== 
	 */
	
	private void initialize() {
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		this.add(getCaptionLabel());
		Component c = getComponent();
		if(c instanceof JComponent) {
			((JComponent)c).setAlignmentX(JComponent.LEFT_ALIGNMENT);
			((JComponent)c).setAlignmentY(JComponent.CENTER_ALIGNMENT);
		}
		this.add(getComponent());
		this.add(getButton());
		// do not add before button is made visible
		m_buttonStrut = Box.createHorizontalStrut(5);
	}
						
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public int getVerticalAlignment() {
		return getCaptionLabel().getVerticalAlignment();
	}
	
	public int getHorizontalAlignment() {
		return getCaptionLabel().getHorizontalAlignment();
	}
	
	public void setVerticalAlignment(int alignment) {
		getCaptionLabel().setVerticalAlignment(alignment);
	}
	
	public void setHorizontalAlignment(int alignment) {
		getCaptionLabel().setHorizontalAlignment(alignment);
	}
	
	public boolean isConsume() {
		return (m_isConsume>0);
	}
	
	public void setConsume(boolean isConsume) {
		if(isConsume)
			m_isConsume++;
		else if (m_isConsume>0)
			m_isConsume--;
	}

	public boolean isDirty() {
		if(m_attribute!=null)
			return m_attribute.isUncommitted();
		else
			return m_isDirty;
	}
	
	public int getFixedCaptionWidth() {
		return m_fixedWidth;
	}

	public void setFixedCaptionWidth(int width) {
		// save width
		m_fixedWidth = width;
		// translate
		if(width==-1)
			Utils.setFixedSize(getCaptionLabel(),DEFAULT_CAPTION_WIDTH,DEFAULT_MAXIMUM_HEIGHT);
		else
			Utils.setFixedSize(getCaptionLabel(), width, Integer.MAX_VALUE);
	}	
	
	public int getFixedHeight() {
		return m_fixedHeight;
	}

	public void setFixedHeight(int height) {
		// save height
		m_fixedHeight = height;
		// constrain height to button
		height = m_button!=null && m_button.isVisible() ? Math.max(height,m_button.getPreferredSize().height) : height;
		// translate
		if(height==-1)
			Utils.setFixedHeight(this, DEFAULT_MAXIMUM_HEIGHT);
		else
			Utils.setFixedHeight(this, height);
	}	
	
	@Override
	public void setEnabled(boolean isEnabled) {
		// forward
		super.setEnabled(isEnabled);
		// update button
		getButton().setEnabled(isEnabled);
		// forward?
		if(m_component!=null) m_component.setEnabled(isEnabled);
	}
	
	public void setEditable(boolean isEditable) {
		m_isEditable = isEditable;
		getButton().setEnabled(isEditable);
	}
		
	public boolean isEditable() {
		return m_isEditable;
	}
	
	public void setAutoSave(boolean auto) {
		throw new IllegalArgumentException("AutoSave not supported");
	}
	
	public boolean getAutoSave() {
		throw new IllegalArgumentException("AutoSave not supported");
	}	
	
	public String getCaption() {
		return getCaptionLabel().getText();
	}

	public void setCaption(String text) {
		getCaptionLabel().setText(text);
	}

	public boolean fill(Object values) { return true; };
	
	public boolean load() {
		// consume?
		if(isConsume()) return false;
		// initialize
		boolean bFlag = false;
		// consume change
		setConsume(true);
		// load from mso model?
		if(isMsoAttribute()) {
			try {
				// forward
				bFlag = setValue(MsoUtils.getAttribValue(m_attribute));
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		else {
			// re-apply current value
			bFlag = setValue(getValue());
		}
		// reset flag
		m_isDirty = false;
		// resume change
		setConsume(false);
		// finished
		return bFlag;
	}
	
	public boolean save() {
		
		// consume?
		if(isConsume()) return false;
		
		// reset flag
		m_isDirty = false;
		
		// consume?
		if(!isMsoAttribute()) return false;
		
		// initialize
		boolean bFlag = false;
		
		// consume changes
		setConsume(true);
		
		try {
			// forward
			if(MsoUtils.setAttribValue(m_attribute,getValue())) {
				// success
				bFlag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// resume changes
		setConsume(false);
		
		// finished
		return bFlag;
	}


	public boolean isMsoAttribute() {
		return (m_attribute!=null);
	}
	
	public IAttributeIf<?> getMsoAttribute() {
		return m_attribute;
	}

  	public static boolean isMsoAttributeSupported(IAttributeIf<?> attribute) {
		return !(attribute instanceof AttributeImpl.MsoPolygon || 
				attribute instanceof AttributeImpl.MsoRoute || 
				attribute instanceof AttributeImpl.MsoTrack);  		
  	}

	public boolean addDiskoWorkListener(IDiskoWorkListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener) {
		return listeners.remove(listener);
		
	}
	
	public void setButton(AbstractButton button, boolean isVisible) {
		// remove current?
		if(m_button!=null) {
			if(m_button.isVisible()) this.remove(m_buttonStrut);
			this.remove(m_button);
		}
		// prepare
		m_button = button;
		// force?
		if(m_button==null) getButton();
		// update 
		m_button.setVisible(isVisible);
		m_button.setEnabled(isEnabled() && isEditable());
		m_button.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		m_button.setAlignmentY(JComponent.CENTER_ALIGNMENT);			
		if(isVisible) this.add(m_buttonStrut);
		if(button!=null) this.add(button);
		setFixedHeight(m_fixedHeight);
	}
	
	public String getButtonText() {
		return getButton().getText();
	}

	public void setButtonText(String text) {
		getButton().setText(text);		
	}

	public String getButtonTooltipText() {
		return getButton().getToolTipText();
	}

	public void setButtonTooltipText(String text) {
		getButton().setToolTipText(text);		
	}

	public boolean isButtonVisible() {
		return getButton().isVisible();
	}
	
	public void setButtonVisible(boolean isVisible) {
		if(getButton().isVisible()!=isVisible) {
			if(getButton().isVisible()) {
				this.remove(m_buttonStrut);
			}
			else {
				this.remove(getButton());
				this.add(m_buttonStrut);
				this.add(getButton());
			}			
			getButton().setVisible(isVisible);
			setFixedHeight(m_fixedHeight);			
		}
	}	
	
	public boolean isButtonEnabled() {
		return getButton().isEnabled();
	}

	public void setButtonEnabled(boolean isEnabled) {
		getButton().setEnabled(isEnabled);		
	}

	public Icon getButtonIcon() {
		return getButton().getIcon();
	}

	public void setButtonIcon(Icon icon) {
		getButton().setIcon(icon);				
	}

	public String getButtonCommand() {
		return getButton().getActionCommand();
	}

	public void setButtonCommand(String name) {
		getButton().setActionCommand(name);
		
	}
	
	public boolean addButtonActionListener(ActionListener listener) {
		if(listener!=null && m_button!=null) {
			getButton().addActionListener(listener);
			return true;
		}
		return false;		
	}

	public boolean removeButtonActionListener(ActionListener listener) {
		if(listener!=null && m_button!=null) {
			getButton().removeActionListener(listener);
			return true;
		}
		return false;		
	}
	
	/*==================================================================
	 * Abstract public methods
	 *================================================================== 
	 */


	public abstract Object getValue();
	
	public abstract boolean setValue(Object value);
	
	public abstract Component getComponent();	
	
	public abstract boolean setMsoAttribute(IAttributeIf<?> attribute);
	
	
}
