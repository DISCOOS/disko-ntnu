package org.redcross.sar.gui.panel;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.redcross.sar.gui.attribute.AbstractDiskoAttribute;
import org.redcross.sar.gui.attribute.CheckBoxAttribute;
import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.attribute.EnumAttribute;
import org.redcross.sar.gui.attribute.IDiskoAttribute;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.attribute.PositionAttribute;
import org.redcross.sar.gui.attribute.TextFieldAttribute;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

/**
 * @author kennetgu
 *
 */
public class AttributesPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private List<String> m_attributes = null;
	private Map<String,IDiskoAttribute> m_panels = null;
	
	private JPanel m_listPanel = null;
	private JLabel m_emptyLabel = null;
	
	private Component glue = null;
	
	private float m_attribAlignX = Component.LEFT_ALIGNMENT;
	private float m_attribAlignY = Component.CENTER_ALIGNMENT;
	
	public AttributesPanel() {
		this("Egenskaper","Ingen egenskaper funnet",true,true);
	}
	
	public AttributesPanel(String caption, String message, boolean finish, boolean cancel) {
		// forward
		super(caption,finish,cancel);
		// prepare
		m_attributes = new ArrayList<String>();
		m_panels = new HashMap<String, IDiskoAttribute>();		
		// initialize GUI
		initialize(message);
	}

	private void initialize(String message) {
		// set body layout
		setBodyLayout(new CardLayout());
		setBodyBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel label = getEmptyLabel();
		label.setText(message);
		this.addBodyChild(getEmptyLabel(),"message");
		this.addBodyChild(getListPanel(),"list");
		// show list
		showCard("message");
	}
	
	private void showCard(String name)  {
		CardLayout c = (CardLayout)getBodyLayout();
		c.show((Container)getBodyComponent(), name);
	}
	
	private JPanel getListPanel() {
		if(m_listPanel==null) {
			m_listPanel = new JPanel();
			m_listPanel.setLayout(new BoxLayout(m_listPanel,BoxLayout.Y_AXIS));
			m_listPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		return m_listPanel;
	}
	
	private JLabel getEmptyLabel() {
		if(m_emptyLabel==null) {
			m_emptyLabel = new JLabel();
			m_emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			m_emptyLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
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
		create(msoObject,attributes,attributes,width,true,isEditable);
	}
	
	public int create(IMsoObjectIf msoObject, String[] attributes, String[] captions, int width, boolean include, boolean isEditable) {
		List<String> attrList = new ArrayList<String>();
		List<String> capsList = new ArrayList<String>();
		if(attributes!=null) {
			for(int i=0;i<attributes.length;i++) {
				attrList.add(attributes[i]);
			}			
		}
		if(captions!=null) {
			for(int i=0;i<captions.length;i++) {
				capsList.add(captions[i]);
			}			
		}
		return create(msoObject, attrList, capsList, width, include, isEditable);
	}
	
	public int create(IMsoObjectIf msoObject, List<String> attributes, List<String> captions, int width, boolean include, boolean isEditable) {

		// initialize
		int added = 0;
		// remove old panels
		clearAttributes();
		// get all attributes
		Map<String,IAttributeIf> map = msoObject.getAttributes();
		// select decision method
		if(include) {
			// insert only passed attribues 
			for(int i=0;i<attributes.size();i++) {
				String it = attributes.get(i);
				// add to panel?
				if(map.containsKey(it)) {
					// get attribute
					IAttributeIf attr = map.get(it);
					// is supported?
					if(AbstractDiskoAttribute.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addAttribute(attr,captions.get(i),width,isEditable)!=null ? 1 : 0);
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
	
	public boolean doWork() {
		return (save()>0);
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
	public boolean cancel() {
		if(super.cancel()) {
			load();
			return true;
		}
		return false;
	}
	
	public IDiskoAttribute addAttribute(IAttributeIf attribute, String caption, int width, boolean isEditable)  {
		// string get name
		String name = attribute.getName();
		// does not exist?
		if(!m_attributes.contains(name)) {
			// forward
			IDiskoAttribute attr = createAttribute(attribute,caption,width,isEditable);
			// forward
			if(addAttribute(attr)) {
				// forward
				update();
				// return panel
				return attr;
			}
		}
		return null;
	}
	
	public boolean addAttribute(IDiskoAttribute attribute)  {
		// string get name
		String name = attribute.getName();
		// valid attribute?
		if(attribute instanceof JComponent && !m_attributes.contains(name)) {
			// apply current alignment
			((JComponent)attribute).setAlignmentX(m_attribAlignX);
			((JComponent)attribute).setAlignmentY(m_attribAlignY);
			// add strut?
			if(m_attributes.size()>0) {
				getListPanel().remove(glue);
				getListPanel().add(Box.createVerticalStrut(5));
			}
			else glue = Box.createVerticalGlue();
			getListPanel().add((Component)attribute);
			getListPanel().add(glue);
			// add to list
			m_attributes.add(name);			
			m_panels.put(name,attribute);			
			// add listener
			attribute.addDiskoWorkListener(this);
			// success
			return true;
		}
		// failure
		return false;
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
		Component attr = (Component)getAttribute(name);
		// has panel?
		if(attr!=null) {
			// remove
			m_panels.remove(name);
			m_attributes.remove(name);
			// rebuild
			getListPanel().removeAll();
			boolean isFirst = true;
			for(IDiskoAttribute it : m_panels.values()) {
				if(!isFirst)
					getListPanel().add(Box.createVerticalStrut(5));
				else
					isFirst = false;
				getListPanel().add((JComponent)it);
			}
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
			getListPanel().removeAll();			
		}		
	}
	
	public double getCaptionWidth(String name) {
		return m_panels.get(name).getCaptionWidth();
	}

	public void setCaptionWidth(int width) {
		for(IDiskoAttribute it: m_panels.values())
			it.setCaptionWidth(width);		
	}	
	
	public void setCaptionWidth(String name, int width) {
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
  		
  		int max = 0;
  		
  		// calculate dirty bit
  		for(IDiskoAttribute it : m_panels.values()) {
  			if(it.isDirty()) {
  				setDirty(true,false); break;
  			}
  		}

  		// select card
		if(getAttributeCount()==0) {
			showCard("message");
		}
		else {
			showCard("list");
		}
  		
  		// forward 
  		super.update();
  	}

  	public void setAutoSave(boolean autoSave) {
  		for(IDiskoAttribute it : m_panels.values()) {
			it.setAutoSave(autoSave);
  		}
  	}
  	
  	public int getAutoSave() {
  		int count = 0;
  		for(IDiskoAttribute it : m_panels.values()) {
			if(it.getAutoSave()) count++;
  		}
  		return count;
  	}
  	
  	public void setEditable(boolean isEditable) {
  		for(IDiskoAttribute it : m_panels.values()) {
			it.setEditable(isEditable);
  		}
  	}
  	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		super.setEnabled(isEnabled);
  		for(IDiskoAttribute it : m_panels.values()) {
  			if(it instanceof Component)
  				((Component)it).setEnabled(isEnabled);
  		}
  	}
  	
  	@Override
	public void setChangeable(boolean isChangeable) {
  		// forward
  		super.setChangeable(isChangeable);
  		// loop over all attributes
  		for(IDiskoAttribute it : m_panels.values()) {
  			it.setConsume(!isChangeable);
  		}
	}
  	  	  	
	public void setAttributeAlignmentX(float position) {
		// prepare
		m_attribAlignX = position;
  		// loop over all attributes
  		for(IDiskoAttribute it : m_panels.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}
  	
	public void setAttributeAlignmentY(float position) {
		// prepare
		m_attribAlignY = position;
  		// loop over all attributes
  		for(IDiskoAttribute it : m_panels.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}
	
}
