/**
 * 
 */
package org.redcross.sar.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.redcross.sar.event.DiskoWorkEvent;
import org.redcross.sar.event.IDiskoWorkListener;
import org.redcross.sar.gui.DefaultDiskoPanel;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

import com.borland.jbcl.layout.VerticalFlowLayout;

/**
 * @author kennetgu
 *
 */
public class AttributesPanel extends DefaultDiskoPanel {

	private static final long serialVersionUID = 1L;
	
	private List<String> m_attributes = null;
	private Map<String,IDiskoAttribute> m_panels = null;
	
	private JPanel m_contentPanel = null;
	private JLabel m_emptyLabel = null;
	
	public AttributesPanel() {
		this("No attributtes");
	}
	
	public AttributesPanel(String message) {
		// prepare
		// initialize
		m_attributes = new ArrayList<String>();
		m_panels = new HashMap<String, IDiskoAttribute>();		
		// initialize GUI
		initialize(message);
	}

	private void initialize(String message) {
		BorderLayout bl = new BorderLayout();
		bl.setHgap(5);
		bl.setVgap(5);
		this.setLayout(bl);
		JLabel label = getEmptyLabel();
		label.setText(message);
		this.add(label,BorderLayout.CENTER);
	}
	
	private JPanel getContentPanel() {
		if(m_contentPanel==null) {
			m_contentPanel = new JPanel();
			VerticalFlowLayout vfl = new VerticalFlowLayout();
			vfl.setHgap(0);
			vfl.setVgap(5);
			vfl.setAlignment(VerticalFlowLayout.LEFT);
			m_contentPanel.setLayout(vfl);
		}
		return m_contentPanel;
	}
	
	private JLabel getEmptyLabel() {
		if(m_emptyLabel==null) {
			m_emptyLabel = new JLabel();
		}
		return m_emptyLabel;
	}
	
	public void setMessage(String text) {
		getEmptyLabel().setText(text);
	}
	
	public String getMessage() {
		return getEmptyLabel().getText();
	}
		
	public void create(IMsoObjectIf msoObject, int width, boolean isEditable) {
		// invalid argument?
		if(msoObject==null) throw new NullPointerException("MsoObject can not be null");
		// get attributes
		List<String> attributes = new ArrayList<String>(msoObject.getAttributes().size());
		Iterator<String> it = msoObject.getAttributes().keySet().iterator();
		while(it.hasNext()) {
			attributes.add(it.next());
		}
		// forward
		create(msoObject,attributes,width,true,isEditable);
	}
	
	public int create(IMsoObjectIf msoObject, List<String> attributes, int width, boolean include, boolean isEditable) {
		// initialize
		int added = 0;
		// remove old panels
		clearAttributes();
		// get all attributes
		Map<String,IAttributeIf> map = msoObject.getAttributes();
		// select decision method
		if(include) {
			// insert only passed attribues 
			for(String it:attributes) {
				// add to panel?
				if(map.containsKey(it)) {
					// get attribute
					IAttributeIf attr = map.get(it);
					// is supported?
					if(AbstractDiskoAttribute.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addAttribute(attr,attr.getName(),width,isEditable)!=null ? 1 : 0);
					}
				}
			}		
		}
		else {
			// insert all attribues except passed attribues
			for(String it: map.keySet()) {
				// add to panel?
				if(!attributes.contains(it)) {
					// get attribute
					IAttributeIf attr = map.get(it);
					// is supported?
					if(AbstractDiskoAttribute.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addAttribute(attr,attr.getName(),width,isEditable)!=null ? 1 : 0);
					}
				}
			}					
		}
		// retrun counter
		return added;
	}
	
	public int getAttributeCount() {
		return (m_attributes!=null ? m_attributes.size(): 0);
	}
	
	public void load() {
		for(IDiskoAttribute it: m_panels.values()) {
			it.load();
		}
	}
	
	public int save() {
		int count = 0;
		for(IDiskoAttribute it: m_panels.values()) {
			count += (it.save() ? 1 : 0);
		}
		setDirty(false);
		return count;
	}
	
	@Override
	public boolean finish() {
		if(super.finish()) {
			return (save()>0);
		}
		return false;
	}
	
	@Override
	public boolean cancel() {
		if(super.cancel()) {
			load();
			return true;
		}
		return false;
	}
	
	public boolean addAttribute(IDiskoAttribute attribute)  {
		// string get name
		String name = attribute.getName();
		// valid attribute?
		if(attribute instanceof Component && !m_attributes.contains(name)) {
			// add to list
			m_panels.put(name,attribute);			
			m_attributes.add(name);			
			// add to this
			getContentPanel().add((Component)attribute,null);
			// success
			return true;
		}
		// failure
		return false;
	}
	
	public IDiskoAttribute addAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable)  {
		// string get name
		String name = attribute.getName();
		// does not exist?
		if(!m_attributes.contains(name)) {
			// forward
			IDiskoAttribute attr = createAttribute(attribute,caption,width,isEditable);
			// add to list
			m_panels.put(name,attr);			
			m_attributes.add(name);			
			// add to content panel
			getContentPanel().add((Component)attr,null);
			// add listener
			attr.addDiskoWorkListener(new IDiskoWorkListener() {

				public void onWorkCancel(DiskoWorkEvent e) {
					update();					
				}

				public void onWorkChange(DiskoWorkEvent e) {
					// TODO Auto-generated method stub
					
				}

				public void onWorkFinish(DiskoWorkEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			});
			// forward
			update();
			// return panel
			return attr;
		}
		return null;
	}
	
	public IDiskoAttribute getAttribute(String name) {
		// has mso object?
		if(m_attributes!=null) {
			// has attribute
			if (m_attributes.contains(name)) {
				// return panel
				return m_panels.get(name);
			}
		}
		// failure
		return null;
	}

	public boolean removeAttribute(String name)  {
		// get panel
		Component it = (Component)getAttribute(name);
		// has panel?
		if(it!=null) {
			// remove
			m_panels.remove(name);
			m_attributes.remove(name);
			getContentPanel().remove(it);
		}
		update();
		// failure
		return false;
	}
	
	public void clearAttributes()  {
		// remove old panels?
		if(m_panels!=null) {
			m_panels.clear();
			m_attributes.clear();
			getContentPanel().removeAll();			
		}		
	}
	
	public void doLayout() {
		// add empty label?
		if(getAttributeCount()==0) {
			// remove content panel
			remove(getContentPanel());
			// add empty label
			this.add(getEmptyLabel(),BorderLayout.CENTER);
		}
		else {
			// remove label
			remove(getEmptyLabel());
			// add content panel
			this.add(getContentPanel(),BorderLayout.CENTER);
		}
		// forward
		super.doLayout();
	}
	
	public Dimension getAttributeSize(String name) {
		return m_panels.get(name).getAttributeSize();
	}

	public void setAttributeSize(Dimension size) {
		for(IDiskoAttribute it: m_panels.values())
			it.setAttributeSize(size);		
	}
	
	public void setAttributeSize(String name,Dimension size) {
		m_panels.get(name).setAttributeSize(size);		
	}
	
	public double getCaptionWidth(String name) {
		return m_panels.get(name).getCaptionWidth();
	}

	public void setCaptionWidth(double width) {
		for(IDiskoAttribute it: m_panels.values())
			it.setCaptionWidth(width);		
	}	
	
	public void setCaptionWidth(String name, double width) {
		m_panels.get(name).setCaptionWidth(width);		
	}	
	
  	public static IDiskoAttribute createAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable) {
  		// initialize component
  		IDiskoAttribute component = null;
		try {
			// dispatch attribute type
			if (attribute instanceof AttributeImpl.MsoBoolean) {
				// get checkbox attribute
			    component = new CheckBoxAttribute(
			    		(AttributeImpl.MsoBoolean)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoInteger) {
				// get numeric attribute
			    component = new NumericAttribute(
			    		(AttributeImpl.MsoInteger)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoDouble) {
				// get numeric attribute
			    component = new NumericAttribute(
			    		(AttributeImpl.MsoDouble)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoString) {
				// get text attribute
			    component = new TextFieldAttribute(
			    		(AttributeImpl.MsoString)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoCalendar) {
				// get DTG attribute
			    component = new DTGAttribute(
			    		(AttributeImpl.MsoCalendar)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoPosition) {
				// get position attribute
			    component = new PositionAttribute(
			    		(AttributeImpl.MsoPosition)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoTimePos) {
				// get position attribute
			    component = new PositionAttribute(
			    		(AttributeImpl.MsoTimePos)attribute,caption,width,isEditable);
			}
			else if (attribute instanceof AttributeImpl.MsoPolygon) {
			    //AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) attribute;
			    throw new IllegalArgumentException("MsoPolygon is not supported");
			}
			else if (attribute instanceof AttributeImpl.MsoRoute) {
			    //AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) attribute;
			    throw new IllegalArgumentException("MsoRoute is not supported");
			}
			else if (attribute instanceof AttributeImpl.MsoTrack) {
			    //AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) attribute;
			    throw new IllegalArgumentException("MsoTrack is not supported");
			}
			else if (attribute instanceof AttributeImpl.MsoEnum) {
				// get enum attribute
			    component = new EnumAttribute(
			    		(AttributeImpl.MsoEnum)attribute,caption,width,isEditable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return component;
  	}
  	
  	public void update() {
  		// calculate dirty bit
  		for(IDiskoAttribute it : m_panels.values()) {
  			if(it.isDirty()) {
  				setDirty(true);
  				break;
  			}
  		}
  		// forward 
  		super.update();
  	}
}
