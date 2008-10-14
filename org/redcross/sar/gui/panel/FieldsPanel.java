package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.redcross.sar.gui.factory.DiskoButtonFactory.ButtonSize;
import org.redcross.sar.gui.field.AbstractField;
import org.redcross.sar.gui.field.CheckBoxField;
import org.redcross.sar.gui.field.DTGField;
import org.redcross.sar.gui.field.EnumField;
import org.redcross.sar.gui.field.IDiskoField;
import org.redcross.sar.gui.field.IMsoField;
import org.redcross.sar.gui.field.NumericField;
import org.redcross.sar.gui.field.PositionField;
import org.redcross.sar.gui.field.TextAreaField;
import org.redcross.sar.gui.util.SpringUtilities;
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
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class FieldsPanel extends DefaultPanel {

	private static final long serialVersionUID = 1L;
	
	private List<String> m_names;
	private Map<String,IDiskoField> m_attributes;
	
	private JLabel m_messageLabel;
	
	private float m_attribAlignX = Component.LEFT_ALIGNMENT;
	private float m_attribAlignY = Component.CENTER_ALIGNMENT;
	
	private boolean m_isLayoutDirty = true;
	private boolean m_isLayoutSuspended = true;
	private boolean m_isMessageVisible = false;
	
	private int m_columns; 
	
	public FieldsPanel() {
		this("Egenskaper");
	}
	
	public FieldsPanel(String caption) {
		this(caption,"Ingen egenskaper funnet",true,true);
	}
	
	public FieldsPanel(String caption, String message, boolean finish, boolean cancel) {
		this(caption,message,finish,cancel,ButtonSize.SMALL,1);
	}

	public FieldsPanel(String caption, String message, boolean finish, boolean cancel, ButtonSize buttonSize) {
		this(caption,message,finish,cancel,buttonSize,1);
	}
	public FieldsPanel(String caption, String message, boolean finish, boolean cancel, ButtonSize buttonSize, int columns) {
		// forward
		super(caption,finish,cancel,buttonSize);
		// prepare
		m_columns = columns;
		m_names = new ArrayList<String>();
		m_attributes = new HashMap<String, IDiskoField>();		
		// initialize GUI
		initialize(message);
	}
	
	private void initialize(String message) {
		// prepare message label
		getMessageLabel().setText(message);		
		// show message (layout manager is set here)
		createMessage();
	}
	
	public int getColumns() {
		return m_columns;
	}
	
	public void setColumns(int columns) {
		if(m_columns!=columns) {
			m_columns = columns;
			if(!m_isMessageVisible) {
				createGrid();
			}
		}
	}
	
	public boolean isMessageVisible() {
		return m_isMessageVisible;
	}
	
	public void setMessageVisible(boolean isVisible)  {
		// any change?
		if(m_isMessageVisible!=isVisible) {			
			m_isMessageVisible = isVisible;
			validate();
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
		getMessageLabel().setText("<html>"+Utils.trimHtml(text)+"</html>");
	}
	
	public String getMessage() {
		return Utils.trimHtml(getMessageLabel().getText());
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
					if(AbstractField.isMsoAttributeSupported(attr)) {
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
					if(AbstractField.isMsoAttributeSupported(attr)) {
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
	
	public boolean doWork() {
		return finish();
	}	
	
	
	@Override
	protected void afterFinish() {
		for(IDiskoField it: m_attributes.values()) {
			it.finish();
		}
		super.afterFinish();		
	}
		
	@Override
	protected void afterCancel() {
		for(IDiskoField it: m_attributes.values()) {
			it.cancel();
		}
		super.afterCancel();		
	}
	
	@Override
	public void reset() {
		// forward
		super.reset();
		// forward
		for(IDiskoField it: m_attributes.values()) {
			it.reset();
		}
	}
	
	public IDiskoField addAttribute(IAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height)  {
		// string get name
		String name = attribute.getName();
		// does not exist?
		if(!m_names.contains(name)) {
			// forward
			IDiskoField attr = createAttribute(attribute,caption,isEditable,width,height);
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
	
	public boolean addAttribute(IDiskoField attribute)  {
		// initialize flag
		boolean bFlag = false;
		// string get name
		String name = attribute.getName();
		// valid attribute?
		if(attribute instanceof JComponent && !m_names.contains(name)) {
			// get component
			JComponent c = ((JComponent)attribute);
			// apply current alignment
			c.setAlignmentX(m_attribAlignX);
			c.setAlignmentY(m_attribAlignY);
			// add to list
			m_names.add(name);			
			m_attributes.put(name,attribute);			
			// add listener
			attribute.addDiskoWorkListener(this);
			// set layout dirty
			m_isLayoutDirty = true;
			if(!m_isLayoutSuspended) doLayout();
			// success
			bFlag = true;
		}
		// failure
		return bFlag;
	}
	
	public IDiskoField getAttribute(String name) {
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
			// set layout dirty
			m_isLayoutDirty = true;			
		}
		update();
		// failure
		return false;
	}
	
	public boolean isLayoutDirty() {
		return m_isLayoutDirty;
	}

	public boolean isLayoutSuspended() {
		return m_isLayoutSuspended;
	}
	
	public boolean suspendLayout() {
		boolean bFlag = m_isLayoutSuspended;
		m_isLayoutSuspended = true;
		return bFlag;
	}
	
	public boolean resumeLayout() {
		boolean bFlag = m_isLayoutSuspended;
		m_isLayoutSuspended = false;
		if(bFlag && m_isLayoutDirty) doLayout();
		return bFlag;
	}	
	
	@Override
	public void doLayout() {
		// need to update view?
		if(m_isLayoutDirty || true) {
			if(m_isMessageVisible || getAttributeCount()==0)
				createMessage();
			else			
				createGrid();
			m_isLayoutDirty = false;
		}
		// forward
		super.doLayout();
	}
	
	private void createMessage() {
		JPanel panel = (JPanel)getBodyComponent();
		panel.removeAll();
		panel.setLayout(new GridLayout(5,5));
		panel.add(getMessageLabel());		
	}
	
	private void createGrid() {
		// clear current 
		JPanel list = (JPanel)getBodyComponent();
		list.removeAll();
		list.setLayout(new SpringLayout());		
		// add all attributes
		for(String name : m_names) {
			list.add((Component)m_attributes.get(name));
		}
		// get number of attributes 
		int count = m_names.size();
		// calculate rows
		int rows = count/m_columns;		
		// forward
		SpringUtilities.makeCompactGrid(list, rows, m_columns, 5, 5, 5, 5);
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
	
	public boolean containsAttribute(String name) {
		return m_attributes.containsKey(name);
	}
	
	public double getCaptionWidth(String name) {
		return m_attributes.get(name).getFixedCaptionWidth();
	}

	public void setCaptionWidth(int width) {
		for(IDiskoField it: m_attributes.values())
			it.setFixedCaptionWidth(width);		
	}	
	
	public void setCaptionWidth(String name, int width) {
		m_attributes.get(name).setFixedCaptionWidth(width);		
	}	
	
	public Object getValue(String name) {
		return getAttribute(name).getValue();
	}
	
	public void setValue(String name, Object value) {
		getAttribute(name).setValue(value);
	}	
	
  	public static IDiskoField createAttribute(IAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height) {
  		// initialize component
  		IDiskoField component = null;
		try {
			// dispatch attribute type
			if (attribute instanceof MsoBoolean) {
				// get checkbox attribute
			    component = new CheckBoxField(
			    		(MsoBoolean)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoInteger) {
				// get numeric attribute
			    component = new NumericField(
			    		(MsoInteger)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoDouble) {
				// get numeric attribute
			    component = new NumericField(
			    		(MsoDouble)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoString) {
				// get text attribute
			    component = new TextAreaField(
			    		(MsoString)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoCalendar) {
				// get DTG attribute
			    component = new DTGField(
			    		(MsoCalendar)attribute,caption,isEditable,width,height,Calendar.getInstance());
			}
			else if (attribute instanceof MsoPosition) {
				// get position attribute
			    component = new PositionField(
			    		(MsoPosition)attribute,caption,isEditable,width,height);
			}
			else if (attribute instanceof MsoTimePos) {
				// get position attribute
			    component = new PositionField(
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
			    component = new EnumField((MsoEnum<?>)attribute,caption,isEditable,width,height);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return component;
  	}
  	
  	public void update() {
  		
  		// calculate dirty bit
  		for(IDiskoField it : m_attributes.values()) {
  			if(it.isDirty()) {
  				setDirty(true,false); break;
  			}
  		}

  		// forward 
  		super.update();
  	}

  	public void setAutoSave(boolean autoSave) {
  		for(IDiskoField it : m_attributes.values()) {
			it.setAutoSave(autoSave);
  		}
  	}
  	
  	public int getAutoSave() {
  		int count = 0;
  		for(IDiskoField it : m_attributes.values()) {
			if(it.getAutoSave()) count++;
  		}
  		return count;
  	}
  	
  	public void setEditable(boolean isEditable) {
  		for(IDiskoField it : m_attributes.values()) {
			it.setEditable(isEditable);
  		}
  	}
  	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		super.setEnabled(isEnabled);
  		for(IDiskoField it : m_attributes.values()) {
  			if(it instanceof Component)
  				((Component)it).setEnabled(isEnabled);
  		}
  	}
  	
  	@Override
	public void setChangeable(boolean isChangeable) {
  		// forward
  		super.setChangeable(isChangeable);
  		// loop over all attributes
  		for(IDiskoField it : m_attributes.values()) {
  			it.setChangeable(isChangeable);
  		}
	}
  	  	  	
	public void setAttributeAlignmentX(float position) {
		// prepare
		m_attribAlignX = position;
  		// loop over all attributes
  		for(IDiskoField it : m_attributes.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}
  	

	@Override
	protected boolean beforeCancel() {
		// forward
		reset();
		// success
		return true; 
	}

	public void setAttributeAlignmentY(float position) {
		// prepare
		m_attribAlignY = position;
  		// loop over all attributes
  		for(IDiskoField it : m_attributes.values()) {
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
			for(IDiskoField it: m_attributes.values()) {				
				if(it instanceof IMsoField) {
					if(map.containsValue(((IMsoField)it).getMsoAttribute())) {
						it.reset();
					}
				}
			}
		}
	}
	

	protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		
		/* 
		 * 
		 * TODO: Implement server value change indication in
		 * GUI including lookup of source information 
		 * functionality. For example source name (person, module) and 
		 * location (IP address, master name, logical unit) 
		 * 
		 * TODO: Implement server/local value conflict indication in 
		 * GUI and functionality for resolving this conflict action 
		 *
		 */ 
		
		super.msoObjectChanged(msoObject, mask);
		if(this.msoObject == msoObj) {
			Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_attributes.values()) {
				if(it instanceof IMsoField) {
					if(map.containsValue(((IMsoField)it).getMsoAttribute())) {
						it.reset();
					}
				}
			}
		}
	}

	@Override
	protected void msoObjectDeleted(IMsoObjectIf msoObj, int mask) {

		// TODO: Implement deleted attribute indication in GUI
		
		// forward
		super.msoObjectDeleted(msoObject, mask);
		
		// only notify attributes belonging to this object
		if(this.msoObject == msoObj) {
			// TODO: Implement deleted attribute indication in GUI 
			Map<String,IAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_attributes.values()) {
				if(it instanceof IMsoField) {
					IMsoField field = ((IMsoField)it);
					if(map.containsValue(field.getMsoAttribute())) {
						field.setMsoAttribute(null);
						it.reset();
					}
				}
			}
		}
	}
	
	@Override
	protected void msoObjectClearAll(IMsoObjectIf msoObj, int mask) {
		
		// TODO: Implement deleted attribute indication in GUI 

		// forward
		super.msoObjectClearAll(msoObject, mask);
		// loop over attributes
		for(IDiskoField it: m_attributes.values()) {
			if(it instanceof IMsoField) {
				IMsoField field = ((IMsoField)it);
				field.setMsoAttribute(null);
				it.reset();
			}
		}
	}	
	
}
