/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.event.DiskoWorkEvent.DiskoWorkEventType;
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
	
	protected String m_caption = null;
	protected boolean m_isEditable = false;
	
	protected Dimension m_absolute = null;

	protected IAttributeIf m_attribute = null;
	
	protected boolean m_autoSave = false;
	
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
		this.setAttributeSize(new Dimension(250,25));
		this.add(getCaptionLabel(),BorderLayout.WEST);
		this.add(getComponent(),BorderLayout.CENTER);
		// add update manager
		getComponent().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {						
				// forward
				super.focusLost(e);
				// save?
				if(isWorking() || !m_autoSave) return;
				// forward
				save();				
			}
			
		});
		
	}
					
	protected JLabel getCaptionLabel() {
		if(m_captionLabel==null) {
			m_captionLabel = new JLabel(m_caption);
			m_captionLabel.setVerticalAlignment(SwingConstants.TOP);
			m_captionLabel.setLabelFor(getComponent());
		}
		return m_captionLabel;
	}
	
	private void setAbsoluteSize(Component c, Dimension size) {
		c.setMinimumSize(size);
		c.setPreferredSize(size);
		c.setMaximumSize(size);
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
		fireOnWorkChange(new DiskoWorkEvent(this,m_component,null,getValue(),DiskoWorkEventType.TYPE_CHANGE));
	}
			
	protected void fireOnWorkChange(DiskoWorkEvent e) {
		for(IDiskoWorkListener it: listeners) {
			it.onWorkChange(e);
		}
	}
	
	/*==================================================================
	 * Abstract protected methods
	 *================================================================== 
	 */
	
	protected abstract Component getComponent();
	
	/*==================================================================
	 * Public methods
	 *================================================================== 
	 */
	
	public Dimension getAttributeSize() {
		return m_absolute;
	}

	public void setAttributeSize(Dimension size) {
		// save
		m_absolute = size;
		// constain to size
		setAbsoluteSize(this, size);
		// constrain caption width
		setCaptionWidth(getCaptionWidth());
		
	}
	
	public double getCaptionWidth() {
		return getCaptionLabel().getPreferredSize().getWidth();
	}

	public void setCaptionWidth(double width) {
		// get contraint width
		double cw = m_absolute.getWidth()/3;		
		// get size
		Dimension size = new Dimension();
		size.setSize(Math.max(width,cw),m_absolute.getHeight());
		// set as absolute size
		setAbsoluteSize(getCaptionLabel(), size);				
	}	
	
	public void setEditable(boolean isEditable) {
		m_isEditable = isEditable;
		m_component.setEnabled(isEditable);
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
		// load from mso model?
		if(isMsoAttribute()) {
			try {
				// forward
				return setValue(getAttribValue(m_attribute));
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		else {
			// reapply current value
			return setValue(getValue());
		}
		// failed
		return false;
	}
	
	public boolean save() {
		// allowed?
		if(!isMsoAttribute() || ! m_isEditable) return false;
		try {
			// forward
			if(setAttribValue(m_attribute,getValue())) {
				// notify				
				fireOnWorkChange();
				// success
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// failed
		return false;
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

	@Override
	public void setEnabled(boolean isEnabled) {
		// forward
		super.setEnabled(isEnabled);
		// forward?
		if(m_component!=null) m_component.setEnabled(isEnabled);
	}  	
	
	public boolean addDiskoWorkListener(IDiskoWorkListener listener) {
		return listeners.add(listener);
	}
	
	public boolean removeDiskoWorkListener(IDiskoWorkListener listener) {
		return listeners.remove(listener);
		
	}
	
	public void addFocusListener(FocusListener listener) {
		getComponent().addFocusListener(listener);
	}
	
	public void removeFocusListener(FocusListener listener) {
		getComponent().removeFocusListener(listener);
	}
	
	/*==================================================================
	 * Abstract public methods
	 *================================================================== 
	 */

	public abstract Object getValue();
	
	public abstract boolean setValue(Object value);

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
