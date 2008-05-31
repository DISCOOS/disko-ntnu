/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.app.Utils;
import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.util.mso.Polygon;
import org.redcross.sar.util.mso.Position;
import org.redcross.sar.util.mso.Route;
import org.redcross.sar.util.mso.TimePos;
import org.redcross.sar.util.mso.Track;

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

	protected IAttributeIf m_attribute = null;
	
	protected boolean m_isDirty = false;
	protected boolean m_autoSave = false;
	protected boolean m_isEditable = false;
	
	protected int m_captionWidth = 80;
	protected int m_maximumHeight = 25;
	
	private int m_isWorking = 0;
	
	
	private List<IDiskoWorkListener> listeners = null;
	
	/*==================================================================
	 * Protected methods
	 *================================================================== 
	 */
	
	protected AbstractDiskoAttribute(String name, String caption, int width, Object value, boolean isEditable) {
		// prepare
		m_caption = caption;
		m_isEditable = isEditable;
		listeners = new ArrayList<IDiskoWorkListener>(); 
		// initialize GUI
		initialize();
		// forward
		setCaptionWidth(width);
		// set component name
		setName(name);
		// set value
		setValue(value);
	}
	
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
	
	protected JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel(m_caption);
			m_captionLabel.setVerticalAlignment(SwingConstants.TOP);
			m_captionLabel.setLabelFor(getComponent());
		}
		return m_captionLabel;
	}
	
	protected boolean isWorking() {
		return (m_isWorking>0);
	}
	
	protected void setIsWorking() {
		m_isWorking++;
	}
	
	protected void setIsNotWorking() {
		if(m_isWorking>0) m_isWorking--;
	}
	
	protected void fireOnWorkChange() {
		fireOnWorkChange(new DiskoWorkEvent(m_component,getValue(),DiskoWorkEvent.EVENT_CHANGE));
	}
			
	protected void fireOnWorkChange(DiskoWorkEvent e) {
		// forward
		for(IDiskoWorkListener it: listeners) {
			it.onWorkPerformed(e);
		}
	}
	
	public abstract Component getComponent();
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public boolean isDirty() {
		if(m_attribute!=null)
			return m_attribute.isUncommitted();
		else
			return m_isDirty;
	}
	
	/*
	public Dimension getFixedSize() {
		return m_fixedSize;
	}
	public void setFixedSize(Dimension size) {
		// save
		m_fixedSize = size;
		// constain to size
		Utils.setFixedSize(this, size.width,size.height);
		// constrain caption width
		setCaptionWidth(getCaptionWidth());
		
	}
	*/
	
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
		Utils.setFixedSize(getCaptionLabel(), cw,mh);
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

	/**
	 * Updates the attribute value
	 * 
	 * If overridden by extending class, remember
	 * to forward the call to this super class.
	 */
	
	public boolean setValue(Object value) {
		// update dirty bit?
		if(!isWorking()) m_isDirty = true;
		// failure, is not implemented
		return false;
	}

	public boolean fill(Object values) { return true; };
	
	public boolean load() {
		// consume?
		if(isWorking()) return false;
		// initialise
		boolean bFlag = false;
		// consume change
		setIsWorking();
		// load from mso model?
		if(isMsoAttribute()) {
			try {
				// forward
				bFlag = setValue(getAttribValue(m_attribute));
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		else {
			// reapply current value
			bFlag = setValue(getValue());
		}
		// resume change
		setIsNotWorking();
		// finished
		return bFlag;
	}
	
	public boolean save() {
		// allowed?
		if(isWorking() || !isMsoAttribute() || ! m_isEditable) return false;
		
		// initialize
		boolean bFlag = false;
		
		// consume changes
		setIsWorking();
		
		try {
			// forward
			if(setAttribValue(m_attribute,getValue())) {
				// success
				bFlag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// resume changes
		setIsNotWorking();
		
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
		m_button.setVisible(isVisible);
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
	
	public abstract boolean setMsoAttribute(IAttributeIf attribute);
	
	
	/*==================================================================
	 * Static protected methods
	 *================================================================== 
	 */
	
	protected static Object getAttribValue(IAttributeIf attribute) {
		// dispatch attribute type
		if (attribute instanceof AttributeImpl.MsoBoolean) {
		    AttributeImpl.MsoBoolean lAttr = (AttributeImpl.MsoBoolean) attribute;
		    return lAttr.booleanValue();
		}
		else if (attribute instanceof AttributeImpl.MsoInteger) {
		    AttributeImpl.MsoInteger lAttr = (AttributeImpl.MsoInteger) attribute;
		    return lAttr.intValue();
		}
		else if (attribute instanceof AttributeImpl.MsoDouble) {
		    AttributeImpl.MsoDouble lAttr = (AttributeImpl.MsoDouble) attribute;
		    return lAttr.doubleValue();
		}
		else if (attribute instanceof AttributeImpl.MsoString) {
		    AttributeImpl.MsoString lAttr = (AttributeImpl.MsoString) attribute;
		    return lAttr.getString();
		}
		else if (attribute instanceof AttributeImpl.MsoCalendar) {
		    AttributeImpl.MsoCalendar lAttr = (AttributeImpl.MsoCalendar) attribute;
		    return lAttr.getCalendar();
		}
		else if (attribute instanceof AttributeImpl.MsoPosition) {
		    AttributeImpl.MsoPosition lAttr = (AttributeImpl.MsoPosition) attribute;
		    return lAttr.getPosition();
		}
		else if (attribute instanceof AttributeImpl.MsoTimePos) {
		    AttributeImpl.MsoTimePos lAttr = (AttributeImpl.MsoTimePos) attribute;
		    return lAttr.getTimePos().getPosition();
		}
		else if (attribute instanceof AttributeImpl.MsoPolygon) {
		    AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) attribute;
		    return lAttr.getPolygon();
		}
		else if (attribute instanceof AttributeImpl.MsoRoute) {
		    AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) attribute;
		    return lAttr.getRoute();
		}
		else if (attribute instanceof AttributeImpl.MsoTrack) {
		    AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) attribute;
		    return lAttr.getTrack();
		}
		else if (attribute instanceof AttributeImpl.MsoEnum) {
		    AttributeImpl.MsoEnum lAttr = (AttributeImpl.MsoEnum) attribute;
		    return lAttr.getValue();
		}
		// failed
		return null;
	}
	
	protected static boolean setAttribValue(IAttributeIf attribute, Object value) {
		// dispatch attribute type
		if (attribute instanceof AttributeImpl.MsoBoolean) {
		    AttributeImpl.MsoBoolean lAttr = (AttributeImpl.MsoBoolean) attribute;
		    if(value instanceof Boolean) {
		    	lAttr.set((Boolean)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoInteger) {
		    AttributeImpl.MsoInteger lAttr = (AttributeImpl.MsoInteger) attribute;
		    if(value instanceof Integer) {
		    	lAttr.set((Integer)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoDouble) {
		    AttributeImpl.MsoDouble lAttr = (AttributeImpl.MsoDouble) attribute;
		    if(value instanceof Double) {
		    	lAttr.set((Double)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoString) {
		    AttributeImpl.MsoString lAttr = (AttributeImpl.MsoString) attribute;
		    if(value instanceof String) {
		    	lAttr.set((String)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoCalendar) {
		    AttributeImpl.MsoCalendar lAttr = (AttributeImpl.MsoCalendar) attribute;
		    if(value instanceof Calendar) {
		    	lAttr.set((Calendar)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoPosition) {
		    AttributeImpl.MsoPosition lAttr = (AttributeImpl.MsoPosition) attribute;
		    if(value instanceof Position) {
		    	lAttr.set((Position)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoTimePos) {
		    AttributeImpl.MsoTimePos lAttr = (AttributeImpl.MsoTimePos) attribute;
		    if(value instanceof TimePos) {
		    	lAttr.set((TimePos)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoPolygon) {
		    AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) attribute;
		    if(value instanceof Polygon) {
		    	lAttr.set((Polygon)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoRoute) {
		    AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) attribute;
		    if(value instanceof Route) {
		    	lAttr.set((Route)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoTrack) {
		    AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) attribute;
		    if(value instanceof Track) {
		    	lAttr.set((Track)value); return true;
		    }
		}
		else if (attribute instanceof AttributeImpl.MsoEnum) {
		    AttributeImpl.MsoEnum lAttr = (AttributeImpl.MsoEnum) attribute;
		    if(value instanceof Enum) {
		    	lAttr.set((Enum)value); return true;
		    }
		}
		// failed
		return false;
	}		
	
}
