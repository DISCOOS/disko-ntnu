package org.redcross.sar.gui.panel;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.event.IMsoFieldListener;
import org.redcross.sar.gui.event.MsoFieldEvent;
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
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.AttributeImpl.MsoBoolean;
import org.redcross.sar.mso.data.AttributeImpl.MsoCalendar;
import org.redcross.sar.mso.data.AttributeImpl.MsoDouble;
import org.redcross.sar.mso.data.AttributeImpl.MsoEnum;
import org.redcross.sar.mso.data.AttributeImpl.MsoInteger;
import org.redcross.sar.mso.data.AttributeImpl.MsoPolygon;
import org.redcross.sar.mso.data.AttributeImpl.MsoPosition;
import org.redcross.sar.mso.data.AttributeImpl.MsoRoute;
import org.redcross.sar.mso.data.AttributeImpl.MsoString;
import org.redcross.sar.mso.data.AttributeImpl.MsoTimePos;
import org.redcross.sar.mso.data.AttributeImpl.MsoTrack;
import org.redcross.sar.util.Utils;

/**
 * @author kennetgu
 *
 */
public class FieldsPanel extends TogglePanel {

	private static final long serialVersionUID = 1L;

	private JLabel m_messageLabel;

	private float m_fieldAlignX = Component.LEFT_ALIGNMENT;
	private float m_fieldAlignY = Component.CENTER_ALIGNMENT;

	private boolean m_isLayoutDirty = true;
	private boolean m_isLayoutSuspended = true;
	private boolean m_isMessageVisible = false;
	private boolean m_autoResizeX = true;
	private boolean m_autoResizeY = false;
	private boolean m_isBatchMode = true;
	private boolean m_isEditable = true;

	private int m_columns;
	
	private final IMsoFieldListener m_listener;

	private final List<String> m_names = new ArrayList<String>();
	private final Map<String,IDiskoField> m_fields = new HashMap<String,IDiskoField>();
	private final Map<String,Integer[]> m_fieldSpan = new HashMap<String,Integer[]>();
	private final Map<String,IMsoObjectIf> m_msoObjects = new HashMap<String,IMsoObjectIf>();

	public FieldsPanel() {
		this("Egenskaper");
	}

	public FieldsPanel(String caption) {
		this(caption,"Ingen egenskaper funnet",true,true);
	}

	public FieldsPanel(String caption, String message, boolean finish, boolean cancel) {
		this(caption,message,finish,cancel,false,ButtonSize.SMALL,1);
	}

	public FieldsPanel(String caption, String message, boolean finish, boolean cancel, ButtonSize buttonSize) {
		this(caption,message,finish,cancel,false,buttonSize,1);
	}
	public FieldsPanel(String caption, String message, boolean finish, boolean cancel, boolean toggle, ButtonSize buttonSize, int columns) {
		// forward
		super(caption,finish,cancel,toggle,buttonSize);
		// prepare
		m_columns = columns;
		// initialize GUI
		initialize(message);
		// hide toggle button
		setButtonVisible("toggle", false);
		// listen for IAttributeIf<?> set and reset events
		m_listener = new IMsoFieldListener() {

			@Override
			public void onMsoFieldChanged(MsoFieldEvent e) {
				switch(e.getType()) {
				case MsoFieldEvent.ATTRIBUTE_SET: 
					addInterest(e.getSource().getMsoAttribute());
					break;
				case MsoFieldEvent.ATTRIBUTE_RESET:
					removeInterest(e.getSource().getMsoAttribute());
					break;
				}
				
			}
			
		};
		
	}

	protected void initialize(String message) {
		// prepare message label
		getMessageLabel().setText(message);
		// show message (layout manager is set here)
		createMessage();
	}

	/**
	 * Connect to a IMsoModelIf model
	 * @param model - the model
	 */
	public void connect(IMsoModelIf model) {
		connect(model, EnumSet.noneOf(MsoClassCode.class));
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
	
	public boolean isAutoResizeX() {
		return m_autoResizeX;
	}

	public void setAutoResizeX(boolean isAutoResizeX)  {
		// any change?
		if(m_autoResizeX!=isAutoResizeX) {
			m_autoResizeX = isAutoResizeX;
			createGrid();
		}
	}
	
	public boolean isAutoResizeY() {
		return m_autoResizeY;
	}

	public void setAutoResizeY(boolean isAutoResizeY)  {
		// any change?
		if(m_autoResizeY!=isAutoResizeY) {
			m_autoResizeY = isAutoResizeY;
			createGrid();
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
		clearFields();
		// get all attributes
		Map<String,IMsoAttributeIf<?>> map = msoObject.getAttributes();
		// select decision method
		if(include) {
			// insert only passed attributes
			for(int i=0;i<attributes.size();i++) {
				String it = attributes.get(i);
				// add to panel?
				if(map.containsKey(it)) {
					// get attribute
					IMsoAttributeIf<?> attr = map.get(it);
					// is supported?
					if(AbstractField.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addField(attr,captions.get(i),isEditable,width,height)!=null ? 1 : 0);
					}
				}
			}
		}
		else {
			// insert all attributes except passed attributes
			for(String it: map.keySet()) {
				// add to panel?
				if(!attributes.contains(it)) {
					// get attribute
					IMsoAttributeIf<?> attr = map.get(it);
					// is supported?
					if(AbstractField.isMsoAttributeSupported(attr)) {
						// add new attribute panel this
						added += (addField(attr,attr.getName(),isEditable,width,height)!=null ? 1 : 0);
					}
				}
			}
		}
		// finished
		return added;
	}

	public int getFieldCount() {
		return (m_names!=null ? m_names.size(): 0);
	}

	public boolean doWork() {
		return finish();
	}


	@Override
	protected void afterFinish() {
		for(IDiskoField it: m_fields.values()) {
			it.finish();
		}
		super.afterFinish();
	}

	@Override
	protected void afterCancel() {
		for(IDiskoField it: m_fields.values()) {
			it.cancel();
		}
		super.afterCancel();
	}

	@Override
	public void reset() {
		// forward
		super.reset();
		// forward
		for(IDiskoField it: m_fields.values()) {
			it.reset();
		}
	}

	public IDiskoField addField(IMsoAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height)  {
		// string get name
		String name = attribute.getName();
		// does not exist?
		if(!m_names.contains(name)) {
			// forward
			IDiskoField attr = createField(attribute,caption,isEditable,width,height);
			// forward
			if(addField(attr)) {
				// forward
				update();
				// return panel
				return attr;
			}
		}
		return null;
	}

	public boolean addField(IDiskoField field)  {
		// initialize flag
		boolean bFlag = false;
		// string get name
		String name = field.getName();
		// valid attribute?
		if(field instanceof JComponent && !m_names.contains(name)) {
			// get component
			JComponent c = ((JComponent)field);
			// apply current alignment
			c.setAlignmentX(m_fieldAlignX);
			c.setAlignmentY(m_fieldAlignY);
			// add to list
			m_names.add(name);
			m_fields.put(name,field);
			m_fieldSpan.put(name,new Integer[]{1,1});
			if(field.isMsoField()) {
				addInterest(field.getMsoAttribute());
			}
			field.addMsoFieldListener(m_listener);
			// add listener
			field.addWorkFlowListener(this);
			// set auto save mode
			field.setBatchMode(m_isBatchMode);
			// set layout dirty
			m_isLayoutDirty = true;
			// forward
			if(!m_isLayoutSuspended) doLayout();
			// success
			bFlag = true;
		}
		// failure
		return bFlag;
	}
	
	public IDiskoField getField(String name) {
		// has mso object?
		if(m_names!=null) {
			// has attribute
			if (m_names.contains(name)) {
				// return panel
				return m_fields.get(name);
			}
		}
		// failure
		return null;
	}
	
	public IDiskoField getField(int index) {
		// has mso object?
		if(m_names!=null) {
			// return panel
			return m_fields.get(m_names.get(index));
		}
		// failure
		return null;
	}

	public boolean removeField(String name)  {
		// get panel
		Component attr = (Component)getField(name);
		// has panel?
		if(attr!=null) {
			// remove
			IDiskoField field = m_fields.remove(name);
			if(field.isMsoField()) {
				removeInterest(field.getMsoAttribute());
			}
			field.removeMsoFieldListener(m_listener);
			m_names.remove(name);
			m_fieldSpan.remove(name);
			// set layout dirty
			m_isLayoutDirty = true;
		}
		update();
		// failure
		return false;
	}
	
	private void addInterest(IMsoAttributeIf<?> attr) {
		IMsoObjectIf msoObj = attr.getOwner();
		String objId = msoObj.getObjectId();
		m_msoObjects.put(objId,msoObj);
		m_msoInterests.add(msoObj.getMsoClassCode());
	}

	private void removeInterest(IMsoAttributeIf<?> attr) {
		IMsoObjectIf msoObj = attr.getOwner();
		String objId = msoObj.getObjectId();
		m_msoObjects.remove(objId);
		m_msoInterests.remove(msoObj.getMsoClassCode());
	}
	
	public List<IDiskoField> getFields() {
		return new ArrayList<IDiskoField>(m_fields.values());
	}

	public List<IDiskoField> getFields(String[] names) {
		List<IDiskoField> list = new ArrayList<IDiskoField>(names.length);
		for(String name : names) {
			IDiskoField field = m_fields.get(name);
			if(field!=null) list.add(field);			
		}
		return list;
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
		if(m_isLayoutDirty) {
			if(m_isMessageVisible || getFieldCount()==0)
				createMessage();
			else
				createGrid();
			m_isLayoutDirty = false;
		}
		// forward
		super.doLayout();
	}

	private void createMessage() {
		JPanel panel = (JPanel)getContainer();
		panel.removeAll();
		panel.setLayout(new GridLayout(5,5));
		panel.add(getMessageLabel());
	}

	private void createGrid() {
		// initialize
		int i = 0;
		int s = 0;
		int count = getFieldCount();
		int[] cellSpanX = new int[count];
		int[] cellSpanY = new int[count];
		// clear current
		JPanel list = (JPanel)getContainer();
		list.removeAll();
		list.setLayout(new SpringLayout());
		// add all attributes
		for(String name : m_names) {
			list.add((Component)m_fields.get(name));
			cellSpanX[i] = getFieldSpanX(name);
			s += cellSpanX[i] - 1;
			cellSpanY[i] = getFieldSpanY(name);
			s += cellSpanY[i] - 1;
			i++;
		}
		// calculate rows
		int rows = (count+s)/m_columns;
		// forward?
		if(count>0) {
			SpringUtilities.makeSpannedGrid(list, rows, m_columns, 5, 5, 5, 5, 
					cellSpanX, cellSpanY, m_autoResizeX, m_autoResizeY);
		}
	}

	public void clearFields()  {
		// remove old panels?
		if(m_fields!=null) {
			m_msoObjects.clear();
			m_fields.clear();
			m_names.clear();
			// get list panel
			JPanel list = (JPanel)getContainer();
			list.removeAll();
		}
	}

	public boolean containsField(String name) {
		return m_fields.containsKey(name);
	}

	public double getCaptionWidth(String name) {
		return m_fields.get(name).getFixedCaptionWidth();
	}

	public void setCaptionWidth(int width) {
		for(IDiskoField it: m_fields.values())
			it.setFixedCaptionWidth(width);
	}

	public void setCaptionWidth(String name, int width) {
		m_fields.get(name).setFixedCaptionWidth(width);
	}
	
	public int getFixedHeight(String name) {
		return m_fields.get(name).getFixedHeight();
	}
	
	public void setFixedHeight(int heigth) {
		for(IDiskoField it: m_fields.values())
			it.setFixedHeight(heigth);
	}
	
	public void setFixedHeight(String name, int heigth) {
		m_fields.get(name).setFixedHeight(heigth);
	}

	public int getFieldSpanX(String name) {
		return m_fieldSpan.get(name)[0];
	}
	
	public void setFieldSpanX(String name, int span) {
		m_fieldSpan.get(name)[0] = span;
	}
	
	public int getFieldSpanY(String name) {
		return m_fieldSpan.get(name)[1];
	}
	
	public void setFieldSpanY(String name, int span) {
		m_fieldSpan.get(name)[1] = span;
	}
	
	public Object getValue(String name) {
		return getField(name).getValue();
	}

	public void setValue(String name, Object value) {
		getField(name).setValue(value);
	}

  	public static IDiskoField createField(IMsoAttributeIf<?> attribute, String caption, boolean isEditable, int width, int height) {
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
			else if (attribute instanceof MsoPolygon) {
			    //AttributeImpl.MsoPolygon lAttr = (AttributeImpl.MsoPolygon) attribute;
			    //throw new IllegalArgumentException("MsoPolygon is not supported");
			}
			else if (attribute instanceof MsoRoute) {
			    //AttributeImpl.MsoRoute lAttr = (AttributeImpl.MsoRoute) attribute;
			    //throw new IllegalArgumentException("MsoRoute is not supported");
			}
			else if (attribute instanceof MsoTrack) {
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
  		for(IDiskoField it : m_fields.values()) {
  			if(it.isDirty()) {
  				setDirty(true,false); break;
  			}
  		}

  		// forward
  		super.update();
  	}

  	public void setBatchMode(boolean isBatchMode) {
  		for(IDiskoField it : m_fields.values()) {
			it.setBatchMode(isBatchMode);
  		}
  		m_isBatchMode = isBatchMode;
  	}
  	
  	public boolean isBatchMode() {
  		return m_isBatchMode;
  	}

  	public int getBatchModeCount() {
  		int count = 0;
  		for(IDiskoField it : m_fields.values()) {
			if(it.isBatchMode()) count++;
  		}
  		return count;
  	}

  	public void setEditable(boolean isEditable) {
  		if(m_isEditable != isEditable) {
	  		for(IDiskoField it : m_fields.values()) {
  				it.setEditable(isEditable);
	  		}
	  		m_isEditable = isEditable;
  		}
  	}
  	
  	public boolean isEditable() {
  		return m_isEditable;
  	}

  	public int getEditableCount() {
  		int count = 0;
  		for(IDiskoField it : m_fields.values()) {
			if(it.isEditable()) count++;
  		}
  		return count;
  	}
  	
  	@Override
  	public void setEnabled(boolean isEnabled) {
  		super.setEnabled(isEnabled);
  		for(IDiskoField it : m_fields.values()) {
  			if(it instanceof Component)
  				((Component)it).setEnabled(isEnabled);
  		}
  	}

  	@Override
	public void setChangeable(boolean isChangeable) {
  		// forward
  		super.setChangeable(isChangeable);
  		// loop over all attributes
  		for(IDiskoField it : m_fields.values()) {
  			it.setChangeable(isChangeable);
  		}
	}

	public void setFieldAlignmentX(float position) {
		// prepare
		m_fieldAlignX = position;
  		// loop over all attributes
  		for(IDiskoField it : m_fields.values()) {
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

	public void setFieldAlignmentY(float position) {
		// prepare
		m_fieldAlignY = position;
  		// loop over all attributes
  		for(IDiskoField it : m_fields.values()) {
  			if(it instanceof JComponent) {
  				((JComponent)it).setAlignmentX(position);
  			}
  		}
	}

	@Override
	protected void msoObjectCreated(IMsoObjectIf msoObj, int mask) {
		
		// forward
		super.msoObjectCreated(msoObj, mask);
		
		// get object id
		String objId = msoObj.getObjectId();
		
		// only notify attributes belonging to this object
		if((msoObj = m_msoObjects.get(objId))!=null) {
			Map<String,IMsoAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_fields.values()) {
				if(it instanceof IMsoField) {
					if(map.containsValue(((IMsoField)it).getMsoAttribute())) {
						it.reset();
					}
				}
			}
		}
	}

	@Override
	protected void msoObjectLoopback(IMsoObjectIf msoObj, int mask) {

		/*
		 *
		 * TODO: Implement server value change indication in
		 * GUI including lookup of source information
		 * functionality. For example source name (person, module) and
		 * location (IP address, master name, logical unit)
		 *
		 */

		// forward
		super.msoObjectLoopback(msoObj, mask);
		
		// get object id
		String objId = msoObj.getObjectId();
		
		// only notify attributes belonging to this object
		if((msoObj = m_msoObjects.get(objId))!=null) {
			
			Map<String,IMsoAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_fields.values()) {
				if(it instanceof IMsoField) {
					if(map.containsValue(((IMsoField)it).getMsoAttribute())) {
						it.reset();
					}
				}
			}
		}
	}

	@Override
	protected void msoObjectChanged(IMsoObjectIf msoObj, int mask) {

		/*
		 *
		 * TODO: Implement server value change indication in
		 * GUI including lookup of source information
		 * functionality. For example source name (person, module) and
		 * location (IP address, master name, logical unit)
		 *
		 */

		// forward
		super.msoObjectChanged(msoObj, mask);
		
		// get object id
		String objId = msoObj.getObjectId();
		
		// only notify attributes belonging to this object
		if((msoObj = m_msoObjects.get(objId))!=null) {
			
			Map<String,IMsoAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_fields.values()) {
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
		super.msoObjectDeleted(msoObj, mask);

		// get object id
		String objId = msoObj.getObjectId();
		
		// only notify attributes belonging to this object
		if((msoObj = m_msoObjects.get(objId))!=null) {
			// TODO: Implement deleted attribute indication in GUI
			Map<String,IMsoAttributeIf<?>> map = msoObj.getAttributes();
			// loop over attributes
			for(IDiskoField it: m_fields.values()) {
				if(it instanceof IMsoField) {
					IMsoField field = ((IMsoField)it);
					IMsoAttributeIf<?> attr = field.getMsoAttribute();
					if(map.containsValue(attr)) {
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
		super.msoObjectClearAll(msoObj, mask);
		
		// loop over attributes
		for(IDiskoField it: m_fields.values()) {
			if(it instanceof IMsoField) {
				IMsoField field = ((IMsoField)it);
				field.setMsoAttribute(null);
				it.reset();
			}
		}
	}
	
}
