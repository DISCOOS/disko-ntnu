package org.redcross.sar.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
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
	
	protected JLabel m_captionLabel = null;
	protected Component m_component = null;
	protected AbstractButton m_button = null;
	
	protected String m_caption = null;
	
	protected Dimension m_fixedSize = null;

	protected IAttributeIf<?> m_attribute = null;
	
	protected boolean m_isDirty = false;
	protected boolean m_autoSave = false;
	protected boolean m_isEditable = false;
	
	protected int m_captionWidth = 80;
	protected int m_maximumHeight = 25;
	
	private int m_isConsume = 0;
	
	
	private List<IDiskoWorkListener> listeners = null;
	
	/*==================================================================
	 * Constructors
	 *================================================================== 
	 */
	
	protected AbstractDiskoAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// prepare
		listeners = new ArrayList<IDiskoWorkListener>();
		// initialize GUI
		initialize();
		// update
		setName(name);
		setCaption(caption);
		setValue(value);
		setEditable(isEditable);		
		setCaptionWidth(width);
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
	
	
	/*==================================================================
	 * Private methods
	 *================================================================== 
	 */
	
	private void initialize() {
		BorderLayout bl = new BorderLayout();
		bl.setHgap(5);
		bl.setVgap(5);
		this.setLayout(bl);
		this.add(getCaptionLabel(),BorderLayout.WEST);
		this.add(getComponent(),BorderLayout.CENTER);
		this.add(getButton(), BorderLayout.EAST);
		addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				setSizes();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				setSizes();
			}
			
		});
		
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
	
	public int getCaptionWidth() {
		return m_captionWidth ==-1 ? getCaptionLabel().getWidth() : m_captionWidth ;
	}

	public void setCaptionWidth(int width) {
		// update
		m_captionWidth = width;
		// apply
		setSizes();
	}	
	
	public int getMaximumHeight() {
		return m_maximumHeight ==-1 ? getHeight() : m_maximumHeight ;
	}

	public void setMaximumHeight(int height) {
		// update
		m_maximumHeight = height;
		// apply
		setSizes();
	}	
	
	private void setSizes() {
		// get sizes
		int cw = getCaptionWidth();
		int mh = Math.max(getMaximumHeight(), getHeight());
		// set as absolute caption size
		Utils.setFixedSize(getCaptionLabel(),cw,mh);
		// set width
		int mw = Math.max(getWidth() - getCaptionWidth() - getButton().getWidth() - 10, 75);
		// set absolute component size
		Utils.setFixedSize(getComponent(), mw, mh);			
		
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
		// initialise
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
			// reapply current value
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
	
	public IAttributeIf getMsoAttribute() {
		return m_attribute;
	}

  	public static boolean isMsoAttributeSupported(IAttributeIf attribute) {
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
		if(m_button!=null) this.remove(m_button);
		// add new?
		if(button!=null) this.add(button, BorderLayout.EAST);
		// prepare
		m_button = button;
		// update?
		if(m_button!=null) {
			// update 
			m_button.setVisible(isVisible);
			m_button.setEnabled(isEnabled() && isEditable());
		}
	}
	
	/**
	 * This method initializes Button	
	 * 	
	 * @return {@link AbstractButton}
	 */
	public AbstractButton getButton() {
		if (m_button == null) {
			m_button = DiskoButtonFactory.createButton(ButtonSize.NORMAL);
			m_button.setVisible(false);
		}
		return m_button;
	}
		
	/*==================================================================
	 * Abstract public methods
	 *================================================================== 
	 */
	
	public abstract Object getValue();
	
	public abstract boolean setValue(Object value);
	
	public abstract Component getComponent();	
	
	public abstract boolean setMsoAttribute(IAttributeIf attribute);
	
	
}
