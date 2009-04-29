package org.redcross.sar.gui.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.redcross.sar.Application;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.map.IDiskoMap;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.Utils;
import org.redcross.sar.work.event.IWorkFlowListener;
import org.redcross.sar.work.event.WorkFlowEvent;
import org.redcross.sar.wp.IDiskoWpModule;

/**
 * @author kennetgu
 *
 */
public abstract class AbstractField extends JPanel implements IDiskoField, IMsoField {

	private static final long serialVersionUID = 1L;

	protected static final int MINIMUM_COMPONENT_WIDTH = 50;
	protected static final int DEFAULT_CAPTION_WIDTH = 80;
	protected static final int DEFAULT_MAXIMUM_HEIGHT = 25;

	protected JLabel m_captionLabel;
	protected Component m_component;
	protected AbstractButton m_button;
	protected Component m_buttonStrut;

	protected String m_caption;

	protected IAttributeIf<?> m_attribute;

	protected int m_isMarked = 0;

	protected boolean m_isDirty = false;
	protected boolean m_autoSave = false;
	protected boolean m_isEditable = false;
	protected boolean m_isTrackingFocus = false;

	private int m_consumeCount = 0;

	private int m_fixedWidth;
	private int m_fixedHeight;

	private final EventListenerList listeners = new EventListenerList();

	/*==================================================================
	 * Constructors
	 *==================================================================
	 */

	protected AbstractField(String name, String caption, boolean isEditable) {
		this(name,caption,isEditable,DEFAULT_CAPTION_WIDTH,DEFAULT_MAXIMUM_HEIGHT,null);
	}

	protected AbstractField(String name, String caption, boolean isEditable, int width, int height, Object value) {
		// forward
		super();
		// initialize GUI
		initialize(width, height);
		// suspend listeners
		setChangeable(false);
		// update
		setName(name);
		setCaptionText(caption);
		setValue(value);
		setEditable(isEditable);

		// resume listeners
		setChangeable(true);
	}

	protected AbstractField(IAttributeIf<?> attribute, String caption, boolean isEditable) {
		// forward
		this(attribute.getName(),caption,isEditable);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		reset();
	}

	protected AbstractField(IAttributeIf<?> attribute,
			String caption, boolean isEditable,
			int width, int height) {
		// forward
		this(attribute.getName(),caption,isEditable,width,height,null);
		// set attribute
		if(!setMsoAttribute(attribute)) throw new IllegalArgumentException("Attribute datatype not supported");
		// get value from attribute
		reset();
	}


	/*==================================================================
	 * Protected methods
	 *==================================================================
	 */

	protected JLabel getCaption() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel(m_caption);
			m_captionLabel.setOpaque(false);
			m_captionLabel.setVerticalAlignment(SwingConstants.TOP);
			m_captionLabel.setLabelFor(getComponent());
			m_captionLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			m_captionLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
		}
		return m_captionLabel;
	}

	protected void fireOnWorkChange() {

		// consume?
		if(!isChangeable()) return;

		if(m_autoSave) {
			Application.getInstance().getMsoModel().suspendClientUpdate();
			if(finish()) {
				fireOnWorkChange(new WorkFlowEvent(this,getValue(),WorkFlowEvent.EVENT_FINISH));
			}
			else {
				// notify change instead
				m_isDirty = true;
				fireOnWorkChange(new WorkFlowEvent(this,getValue(),WorkFlowEvent.EVENT_CHANGE));
			}
			Application.getInstance().getMsoModel().resumeClientUpdate(true);
		}
		else {
			m_isDirty = true;
			fireOnWorkChange(new WorkFlowEvent(this,getValue(),WorkFlowEvent.EVENT_CHANGE));
		}
	}

	protected IDiskoMap getInstalledMap() {
		// try to get map from current
		IDiskoWpModule module = Application.getInstance().getCurrentRole().getCurrentDiskoWpModule();
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

	protected void focusChanged(boolean inFocus) {
		if(m_isTrackingFocus) {
			if(inFocus) {
				setBorder(UIFactory.createBorder(1, 1, 1, 1, Color.BLUE));
			}
			else {
				setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}

	/*==================================================================
	 * Private methods
	 *==================================================================
	 */

	private void initialize(int width, int height) {
		this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		this.add(getCaption());
		Component c = getComponent();
		if(c instanceof JComponent) {
			((JComponent)c).setAlignmentX(JComponent.LEFT_ALIGNMENT);
			((JComponent)c).setAlignmentY(JComponent.CENTER_ALIGNMENT);
		}
		this.add(c);
		this.add(getButton());
		this.setOpaque(false);
		// update fixed sizes
		setFixedCaptionWidth(width);
		setFixedHeight(height);
		// do not add before button is made visible
		m_buttonStrut = Box.createHorizontalStrut(5);
		// add focus listeners
		getComponent().addFocusListener(m_focusListener);
		getButton().addFocusListener(m_focusListener);
		// USE TO DEBUG LAYOUT PROBLEMS
		// setBorder(BorderFactory.createLineBorder(Color.RED)); 		
	}

	private void fireOnWorkChange(WorkFlowEvent e) {
		// get listeners
		IWorkFlowListener[] list = listeners.getListeners(IWorkFlowListener.class);
		// forward
		for(int i=0; i<list.length; i++) {
			list[i].onFlowPerformed(e);
		}
	}

	/*==================================================================
	 * Public methods
	 *==================================================================
	 */

	public boolean isTrackingFocus() {
		return m_isTrackingFocus;
	}

	public void setTrackingFocus(boolean isTrackingFocus) {
		m_isTrackingFocus = isTrackingFocus;
	}

	public int getVerticalAlignment() {
		return getCaption().getVerticalAlignment();
	}

	public int getHorizontalAlignment() {
		return getCaption().getHorizontalAlignment();
	}

	public void setVerticalAlignment(int alignment) {
		getCaption().setVerticalAlignment(alignment);
	}

	public void setHorizontalAlignment(int alignment) {
		getCaption().setHorizontalAlignment(alignment);
	}

	public boolean isChangeable() {
		return (m_consumeCount==0);
	}

	public void setChangeable(boolean isChangeable) {
		if(!isChangeable)
			m_consumeCount++;
		else if(m_consumeCount>0)
			m_consumeCount--;
	}

	public boolean isDirty() {
		if(m_attribute!=null)
			return m_attribute.isUncommitted();
		else
			return m_isDirty;
	}

	public void setDirty(boolean isDirty) {
		if(m_attribute==null) {
			m_isDirty = isDirty;
		}
	}

	public int isMarked() {
		return m_isMarked;
	}

	public void setMarked(int isMarked) {
		m_isMarked = isMarked;
	}

	public int getFixedCaptionWidth() {
		return m_fixedWidth;
	}

	public void setFixedCaptionWidth(int width) {
		// save width
		m_fixedWidth = width;
		// translate
		width = (width==-1 ? DEFAULT_CAPTION_WIDTH : width);
		// set fixed caption width
		Utils.setFixedWidth(getCaption(), width);
		// set minimum size
		Dimension d = getButton().getPreferredSize();
		this.setMinimumSize(new Dimension(width+MINIMUM_COMPONENT_WIDTH+d.width,d.height));
		this.setPreferredSize(new Dimension(width+MINIMUM_COMPONENT_WIDTH+d.width,d.height));
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
		height = (height==-1 ? DEFAULT_MAXIMUM_HEIGHT : height);
		// set fixed height
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

	@Override
	public void setToolTipText(String text) {
		// TODO Auto-generated method stub
		super.setToolTipText(text);
		getCaption().setToolTipText(text);
		if(getComponent() instanceof JComponent)
			((JComponent)getComponent()).setToolTipText(text);
	}

	public String getCaptionText() {
		return getCaption().getText();
	}

	public void setCaptionText(String text) {
		getCaption().setText(text);
	}

	/**
	 * This method sets the caption colors
	 *
	 */
	public void setCaptionColor(Color foreground,Color background) {
		this.setForeground(foreground);
		this.setBackground(background);
		getCaption().setForeground(foreground);
		getCaption().setBackground(background);
	}


	public boolean fill(Object values) { return true; };

	public boolean cancel() {

		// consume?
		if(!isChangeable()) return false;

		// reset flag
		m_isDirty = false;

		// consume?
		if(!isMsoField()) return false;

		// initialize
		boolean bFlag = false;

		// consume changes
		setChangeable(false);

		// forward
		bFlag = m_attribute.rollback();

		// resume changes
		setChangeable(true);

		// finished
		return bFlag;
	}

	public boolean finish() {

		// consume?
		if(!isChangeable()) return false;

		// reset flag
		m_isDirty = false;

		// consume?
		if(!isMsoField()) return false;

		// initialize
		boolean bFlag = false;

		// consume changes
		setChangeable(false);

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
		setChangeable(true);

		// finished
		return bFlag;
	}

	public void reset() {
		// consume?
		if(!isChangeable()) return;
		// consume change
		setChangeable(false);
		// load from MSO model?
		if(isMsoField()) {
			try {
				// forward
				setValue(MsoUtils.getAttribValue(m_attribute));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			// re-apply current value
			setValue(getValue());
		}
		// reset flag
		m_isDirty = false;
		// resume change
		setChangeable(true);
	}

	public boolean isMsoField() {
		return (m_attribute!=null);
	}

	public IAttributeIf<?> getMsoAttribute() {
		return m_attribute;
	}

	public IAttributeIf<?> clearMsoAttribute() {
		IAttributeIf<?> attr = m_attribute;
		m_attribute = null;
		return attr;
	}
	
	public IAttributeIf<?> clearMsoAttribute(Object setValue) {
		IAttributeIf<?> attr = clearMsoAttribute();
		setValue(setValue);
		return attr;		
	}
	
  	public static boolean isMsoAttributeSupported(IAttributeIf<?> attribute) {
		return !(attribute instanceof AttributeImpl.MsoPolygon ||
				attribute instanceof AttributeImpl.MsoRoute ||
				attribute instanceof AttributeImpl.MsoTrack);
  	}

	public void addWorkFlowListener(IWorkFlowListener listener) {
		listeners.add(IWorkFlowListener.class,listener);
	}

	public void removeWorkFlowListener(IWorkFlowListener listener) {
		listeners.remove(IWorkFlowListener.class,listener);

	}

	public void installButton(AbstractButton button, boolean isVisible) {
		// remove current?
		if(m_button!=null) {
			if(m_button.isVisible()) this.remove(m_buttonStrut);
			m_button.removeFocusListener(m_focusListener);
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
		m_button.addFocusListener(m_focusListener);
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
	 *================================================================== */


	public abstract Object getValue();

	public abstract boolean setValue(Object value);

	public abstract Component getComponent();

	public abstract boolean setMsoAttribute(IAttributeIf<?> attribute);

	/*==================================================================
	 * Anonymous classes
	 *================================================================== */

	private FocusListener m_focusListener = new FocusListener() {

		@Override
		public void focusGained(FocusEvent e) {
			focusChanged(true);
		}

		@Override
		public void focusLost(FocusEvent e) {
			focusChanged(false);
		}

	};

}
