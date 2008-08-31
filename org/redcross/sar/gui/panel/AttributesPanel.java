package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.GridLayout;
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

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.attribute.AbstractDiskoAttribute;
import org.redcross.sar.gui.attribute.CheckBoxAttribute;
import org.redcross.sar.gui.attribute.DTGAttribute;
import org.redcross.sar.gui.attribute.EnumAttribute;
import org.redcross.sar.gui.attribute.IDiskoAttribute;
import org.redcross.sar.gui.attribute.NumericAttribute;
import org.redcross.sar.gui.attribute.PositionAttribute;
import org.redcross.sar.gui.attribute.TextAreaAttribute;
import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.mso.data.AttributeImpl;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoBoolean;
import org.redcross.sar.mso.data.AttributeImpl.MsoCalendar;
import org.redcross.sar.mso.data.AttributeImpl.MsoDouble;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.mso.data.AttributeImpl.MsoInteger;
import org.redcross.sar.mso.data.AttributeImpl.MsoPosition;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;
import org.redcross.sar.mso.data.AttributeImpl.MsoTimePos;

/**
 * @author kennetgu
 *
 */
public class AttributesPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private List<String> m_names;
	private Map<String,IDiskoAttribute> m_attributes;
	
	private JLabel m_messageLabel;
	
	private Component glue;
	
	private float m_attribAlignX = Component.LEFT_ALIGNMENT;
	private float m_attribAlignY = Component.CENTER_ALIGNMENT;
	
	private boolean m_isMessageVisible = false;
	
	public AttributesPanel() {
		this("Egenskaper","Ingen egenskaper funnet",true,true);
	}
	
	public AttributesPanel(String caption, String message, boolean finish, boolean cancel) {
		this(caption,message,finish,cancel,ButtonSize.SMALL);
	}

	public AttributesPanel(String caption, String message, boolean finish, boolean cancel, ButtonSize buttonSize) {
		// forward
		super(caption,finish,cancel,buttonSize);
		// prepare
		m_names = new ArrayList<String>();
		m_attributes = new HashMap<String, IDiskoAttribute>();		
		// initialize GUI
		initialize(message);
	}
	
	private void initialize(String message) {
		// get body panel
		JPanel panel = (JPanel)getBodyComponent();
		// get body panel border
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// set layout
		//panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		// prepare message label
		getMessageLabel().setText(message);		
		// show message
		setMessageVisible(true);
	}
	
	public boolean isMessageVisible() {
		return m_isMessageVisible;
	}
	
	public void setMessageVisible(boolean isVisible)  {
		// any change?
		if(m_isMessageVisible!=isVisible) {			
			m_isMessageVisible = isVisible;
			JPanel panel = (JPanel)getBodyComponent();
			panel.removeAll();
			if(isVisible) {
				panel.setLayout(new GridLayout(5,5));
				panel.add(getMessageLabel());
			}
			else {
				panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
				rebuild();
			}
		}
	}
	
	private JLabel getMessageLabel() {
		if(m_messageLabel==null) {
			m_messageLabel = new JLabel();
			m_messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			m_messageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		}
		return m_messageLabel;
	}
	
	public void setMessage(String text) {
		getMessageLabel().setText("<html>"+Utils.stripHtml(text)+"</html>");
	}
	
	public String getMessage() {
		return Utils.stripHtml(getMessageLabel().getText());
	}
		
	public void create(IMsoObjectIf msoObject, boolean isEditable) {
		// invalid argument?
		if(msoObject==null) throw new NullPointerException("MsoObject can not be null");
		// get attributes
		List<String> attributes = new ArrayList<String>(msoObject.getAttributes().size());
		Iterator<String> it = msoObject.getAttributes().keySet().iterator();
		while(it.hasNext()) {
			attributes.add(it.next());
		}
		// forward
		create(msoObject,attributes,attributes,isEditable,80,25,true);
	}
	
	public void create(IMsoObjectIf msoObject, boolean isEditable, int width, int height) {
		// invalid argument?
		if(msoObject==null) throw new NullPointerException("MsoObject can not be null");
		// get attributes
		List<String> attributes = new ArrayList<String>(msoObject.getAttributes().size());
		Iterator<String> it = msoObject.getAttributes().keySet().iterator();
		while(it.hasNext()) {
			attributes.add(it.next());
		}
		// forward
		create(msoObject,attributes,attributes,isEditable,width,height,true);
	}
	
	public int create(IMsoObjectIf msoObject, String[] attributes, String[] captions, boolean isEditable, int width, int height, boolean include) {
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
		return create(msoObject, attrList, capsList, isEditable, width, height, include);
	}
	
	public int create(IMsoObjectIf msoObject, List<String> attributes, List<String> captions, boolean isEditable, int width, int height, boolean include) {

		// initialize
		int added = 0;
		// remove old panels
		clearAttributes();
		// get all attributes
		Map<String,IAttributeIf<?>> map = msoObject.getAttributes();
		// select decision method
		if(include) {
			// insert only passed attributes 
			for(int i=0;i<attributes.size();i++) {
				String it = attributes.get(i);
				// add to panel?
				if(map.containsKey(it)) {
					// get attribute
					IAttributeIf<?> attr = map.get(it);
					// is supported?
					if(AbstractDiskoAttribute.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addAttribute(attr,captions.get(i),isEditable,width,height)!=null ? 1 : 0);
					}
				}
			}		
		}
		else {
			// insert all attributes except passed attribues
			for(String it: map.keySet()) {
				// add to panel?
				if(!attributes.contains(it)) {
					// get attribute
					IAttributeIf<?> attr = map.get(it);
					// is supported?
					if(AbstractDiskoAttribute.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addAttribute(attr,attr.getName(),isEditable,width,height)!=null ? 1 : 0);
					}
				}
			}					
		}
		// finished
		return added;
	}
	
	public int getAttributeCount() {
		return (m_names!=null ? m_names.size(): 0);
	}
	
	public void load() {
		for(IDiskoAttribute it: m_attributes.values()) {
			it.load();
		}
	}
	
	public boolean doWork() {
		return (save()>0);
	}
	
	public int save() {
		int count = 0;
		for(IDiskoAttribute it: m_attributes.values()) {
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
	
	public IDiskoAttribute addAttribute(IAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height)  {
		// string get name
		String name = attribute.getName();
		// does not exist?
		if(!m_names.contains(name)) {
			// forward
			IDiskoAttribute attr = createAttribute(attribute,caption,isEditable,width,height);
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
		// initialize flag
		boolean bFlag = false;
		// string get name
		String name = attribute.getName();
		// valid attribute?
		if(attribute instanceof JComponent && !m_names.contains(name)) {
			// get component
			JComponent c = ((JComponent)attribute);
			// get list panel
			JPanel list = (JPanel)getBodyComponent();
			// apply current alignment
			c.setAlignmentX(m_attribAlignX);
			c.setAlignmentY(m_attribAlignY);
			// add strut?
			if(m_names.size()>0) {
				list.remove(glue);
				list.add(Box.createVerticalStrut(5));
			}
			else glue = Box.createVerticalGlue();
			list.add((Component)attribute);
			list.add(glue);
			// add to list
			m_names.add(name);			
			m_attributes.put(name,attribute);			
			// add listener
			attribute.addDiskoWorkListener(this);
			// success
			bFlag = true;
		}
  		// show message?
		setMessageVisible(getAttributeCount()==0);					
		// failure
		return bFlag;
	}
	
	public IDiskoAttribute getAttribute(String name) {
		// has mso object?
		if(m_names!=null) {
			// has attribute
			if (m_names.contains(name)) {
				// return panel
				return m_attributes.get(name);
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
			m_attributes.remove(name);
			m_names.remove(name);
			// forward
			rebuild();
		}
		update();
		// failure
		return false;
	}

	private void rebuild() {
		// get list panel
		JPanel list = (JPanel)getBodyComponent();
		list.removeAll();
		boolean isFirst = true;
		for(IDiskoAttribute it : m_attributes.values()) {
			if(!isFirst)
				list.add(Box.createVerticalStrut(5));
			else
				isFirst = false;
			list.add((JComponent)it);
		}		
	}
	
	public void clearAttributes()  {
		// remove old panels?
		if(m_attributes!=null) {
			m_attributes.clear();
			m_names.clear();
			// get list panel
			JPanel list = (JPanel)getBodyComponent();
			list.removeAll();			
		}		
	}
	
	public double getCaptionWidth(String name) {
		return m_attributes.get(name).getFixedCaptionWidth();
	}

	public void setCaptionWidth(int width) {
		for(IDiskoAttribute it: m_attributes.values())
			it.setFixedCaptionWidth(width);		
	}	
	
	public void setCaptionWidth(String name, int width) {
		m_attributes.get(name).setFixedCaptionWidth(width);		
	}	
	
  	public static IDiskoAttribute createAttribute(IAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height) {
  		// initialize component
  		IDiskoAttribute component = null;
		try {
			// dispatch attribute type
			if (attribute instanceof MsoBoolean) {
				// get checkbox attribute
			    component = new CheckBoxAttribute(
			    		(MsoBoolean)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoInteger) {
				// get numeric attribute
			    component = new NumericAttribute(
			    		(MsoInteger)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoDouble) {
				// get numeric attribute
			    component = new NumericAttribute(
			    		(MsoDouble)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoString) {
				// get text attribute
			    component = new TextAreaAttribute(
			    		(MsoString)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoCalendar) {
				// get DTG attribute
			    component = new DTGAttribute(
			    		(MsoCalendar)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoPosition) {
				// get position attribute
			    component = new PositionAttribute(
			    		(MsoPosition)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoTimePos) {
				// get position attribute
			    component = new PositionAttribute(
			    		(MsoTimePos)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof AttributeImpl.MsoPolygon) {
			    //AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) attribute;
			    //throw new IllegalArgumentException("MsoPolygon is not supported");
			}
			else if (attribute instanceof AttributeImpl.MsoRoute) {
			    //AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) attribute;
			    //throw new IllegalArgumentException("MsoRoute is not supported");
			}
			else if (attribute instanceof AttributeImpl.MsoTrack) {
			    //AttributeImpl.MsoTrack lAttr = (AttributeImpl.MsoTrack) attribute;
			    //throw new IllegalArgumentException("MsoTrack is not supported");
			}
			else if (attribute instanceof MsoEnum<?>) {
				// get enum attribute
			    component = new EnumAttribute((MsoEnum<?>)attribute,caption,
			    		width,height,isEditable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return component;
  	}
  	
  	public void update() {
  		
  		// calculate dirty bit
  		for(IDiskoAttribute it : m_attributes.values()) {
  			if(it.isDirty()) {
  				setDirty(true,false); break;
  			}
  		}

  		// forward 
  		super.update();
  	}

  	public void setAutoSave(boolean autoSave) {
  		for(IDiskoAttribute it : m_attributes.values()) {
			it.setAutoSave(autoSave);
  		}
  	}
  	
  	public int getAutoSave() {
  		int count = 0;
  		for(IDiskoAttribute it : m_attributes.values()) {
			if(it.getAutoSave()) count++;
  		}
  		return count;
  	}
  	
  	public void setEditable(boolean isEditable) {
  		for(IDiskoAttribute it : m_attributes.values()) {
			it.setEditable(isEditable);
  		}
  	}
  	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		super.setEnabled(isEnabled);
  		for(IDiskoAttribute it : m_attributes.values()) {
  			if(it instanceof Component)
  				((Component)it).setEnabled(isEnabled);
  		}
  	}
  	
  	@Override
	public void setChangeable(boolean isChangeable) {
  		// forward
  		super.setChangeable(isChangeable);
  		// loop over all attributes
  		for(IDiskoAttribute it : m_attributes.values()) {
  			it.setConsume(!isChangeable);
  		}
	}
  	  	  	
	public void setAttributeAlignmentX(float position) {
		// prepare
		m_attribAlignX = position;
  		// loop over all attributes
  		for(IDiskoAttribute it : m_attributes.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}
  	

	@Override
	protected boolean beforeCancel() {
		// forward
		load();
		// success
		return true; 
	}

	@Override
	protected boolean beforeFinish() {
		// forward
		save();
		// success
		return true;
	}

	public void setAttributeAlignmentY(float position) {
		// prepare
		m_attribAlignY = position;
  		// loop over all attributes
  		for(IDiskoAttribute it : m_attributes.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}
	
	@Override
	protected void msoObjectCreated(IMsoObjectIf msoObj, int mask) {
		super.msoObjectCreated(msoObject, mask);
		if(this.msoObject == msoObj) {
			Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoAttribute it: m_attributes.values()) {
				if(it.isMsoAttribute()) {
					if(map.containsValue(it.getMsoAttribute())) {
						it.load();
					}
				}
			}
		}
	}
	

	protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		super.msoObjectChanged(msoObject, mask);
		if(this.msoObject == msoObj) {
			Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoAttribute it: m_attributes.values()) {
				if(it.isMsoAttribute()) {
					if(map.containsValue(it.getMsoAttribute())) {
						it.load();
					}
				}
			}
		}
	}

	@Override
	protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		super.msoObjectDeleted(msoObject, mask);
		if(this.msoObject == msoObj) {
			// TODO: Implement deleted attribute indication in GUI 
			Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoAttribute it: m_attributes.values()) {
				if(it.isMsoAttribute()) {
					if(map.containsValue(it.getMsoAttribute())) {
						it.setMsoAttribute(null);
						it.load();
					}
				}
			}
		}
	}
	
	@Override
	protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
		// forward
		super.msoObjectClearAll(msoObject, mask);
		// TODO: Implement deleted attribute indication in GUI 
		//Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
		// loop over attributes
		for(IDiskoAttribute it: m_attributes.values()) {
			if(it.isMsoAttribute()) {
				it.setMsoAttribute(null);
				it.load();
			}
		}
	}	
	
}
