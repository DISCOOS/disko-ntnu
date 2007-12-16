package org.redcross.sar.gui.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.redcross.sar.app.Utils;
import org.redcross.sar.gui.renderers.IconListCellRenderer;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IAreaIf;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IOperationAreaIf;
import org.redcross.sar.mso.data.IPOIIf;
import org.redcross.sar.mso.data.ISearchAreaIf;
import org.redcross.sar.mso.data.ISearchIf;
import org.redcross.sar.mso.data.IPOIIf.POIType;
import org.redcross.sar.mso.data.ISearchIf.SearchSubType;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Selector;

public class ElementPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	public enum ElementEventType {
		SELECTED, 
		CENTER_AT,
		EDIT,
		DELETE
	}
	
	public enum ElementType {
		CLASS,
		OBJECT,
		PART
	}
	
	private boolean listsAreChangeing = false;
	
	private IMsoModelIf msoModel = null;
	
	private JScrollPane typeScrollPane = null;
	private JScrollPane objectScrollPane = null;
	private JScrollPane partScrollPane = null;

	private JPanel listsPanel = null;
	private JPanel typePanel = null;
	private JPanel objectPanel = null;
	private JPanel partPanel = null;
	
	private JPanel objectButtonsPanel = null;
	private JPanel partButtonsPanel = null;
	
	private JList typeList = null;
	private JList objectList = null;
	private JList partList = null;
	
	private List<IElementEventListener> listeners = null;
	
	private List<IMsoObjectIf> objects = null;
	private List<IMsoObjectIf> parts = null;
	
	public ElementPanel() {
		
		// prepare
		this.msoModel = Utils.getApp().getMsoModel();
		this.listeners = new ArrayList<IElementEventListener>();
		this.objects = new ArrayList<IMsoObjectIf>();
		this.parts = new ArrayList<IMsoObjectIf>();
		
		// initialize gui
		initialize();
		
		// load units
		loadTypes();
		
	}

	private void initialize() {
		BorderLayout bl = new BorderLayout();
		setLayout(bl);
		add(getListsPanel(), BorderLayout.CENTER);		
	}
	
	/**
	 * This method initializes listsPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getListsPanel() {
		if (listsPanel == null) {
			try {
				FlowLayout fl = new FlowLayout();
				fl.setHgap(1);
				fl.setVgap(0);
				fl.setAlignment(FlowLayout.LEFT);
				listsPanel = new JPanel();
				listsPanel.setLayout(fl);
				listsPanel.add(getPartPanel(), null);		
				listsPanel.add(getObjectPanel(), null);		
				listsPanel.add(getTypePanel(), null);		
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return listsPanel;
	}	
	
	/**
	 * This method initializes typeScrollPane
	 * 	
	 * @return javax.swing.JScrollPane
	 */
	public JScrollPane getTypeScrollPane() {
		if (typeScrollPane == null) {
			try {
				typeScrollPane = new JScrollPane(getTypeList());
				typeScrollPane.setWheelScrollingEnabled(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return typeScrollPane;
	}
	
	/**
	 * This method initializes typePanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getTypePanel() {
		if (typePanel == null) {
			try {
				BorderLayout bl = new BorderLayout();
				typePanel = new JPanel();
				typePanel.setLayout(bl);
				typePanel.setBorder(null);
				typePanel.setPreferredSize(new Dimension(200, 500));
				typePanel.add(new JLabel("Velg type"),BorderLayout.NORTH);
				typePanel.add(getTypeScrollPane(),BorderLayout.CENTER);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return typePanel;
	}
	
	/**
	 * This method initializes typeList	
	 * 	
	 * @return javax.swing.JList	
	 */
	public JList getTypeList() {
		if (typeList == null) {
			try {
				typeList = new JList();
				typeList.setCellRenderer(new IconListCellRenderer(0));
				typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				//typeList.setPreferredSize(new Dimension(180, 480));
				typeList.addListSelectionListener(new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						
						// is value adjusting?
						if(e.getValueIsAdjusting() || listsAreChangeing) return;
						
						// notify
						fireOnElementSelected(new ElementEvent(typeList,
								typeList.getSelectedValue(),ElementEventType.SELECTED,ElementType.CLASS));
						
						// load objects of spesified type
						loadObjects((Enum)typeList.getSelectedValue());
							
					}
					
				});
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return typeList;
	}
	
	/**
	 * This method initializes typeScrollPane
	 * 	
	 * @return javax.swing.JScrollPane
	 */
	public JScrollPane getObjectScrollPane() {
		if (objectScrollPane == null) {
			try {
				objectScrollPane = new JScrollPane(getObjectList());
				objectScrollPane.setWheelScrollingEnabled(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return objectScrollPane;
	}
	
	/**
	 * This method initializes objectPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getObjectPanel() {
		if (objectPanel == null) {
			try {
				BorderLayout bl = new BorderLayout();
				objectPanel = new JPanel();
				objectPanel.setLayout(bl);
				objectPanel.setBorder(null);
				objectPanel.setPreferredSize(new Dimension(200, 500));
				objectPanel.add(new JLabel("Velg objekt"),BorderLayout.NORTH);
				objectPanel.add(getObjectScrollPane(),BorderLayout.CENTER);
				objectPanel.add(getObjectButtonsPanel(),BorderLayout.SOUTH);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return objectPanel;
	}
	
	/**
	 * This method initializes objectList	
	 * 	
	 * @return javax.swing.JList
	 */
	public JList getObjectList() {
		if (objectList == null) {
			// create list
            objectList = new JList();
            objectList.setCellRenderer(new IconListCellRenderer(1));
			//objectList.setVisibleRowCount(1);
            /*Dimension dim = new Dimension(180, 430);
			objectList.setMinimumSize(dim);
			objectList.setPreferredSize(dim);
			objectList.setMaximumSize(dim);*/
            objectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            // add selection listener
            objectList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					
					// is value adjusting?
					if(e.getValueIsAdjusting()) return;
					
					// show buttons panel
					getObjectButtonsPanel().setVisible(true);
					
					// consume?
					if(listsAreChangeing) return;
					
					// load parts for this object
					loadParts();
					
					// get mso object
					IMsoObjectIf msoObject = (IMsoObjectIf)objectList.getSelectedValue();
					
					// notify
					fireOnElementSelected(new ElementEvent(objectList,
							msoObject,ElementEventType.SELECTED,ElementType.OBJECT));
					
					
				}
				
			});
            // add mouse listener
            objectList.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					// clear selected part
					getPartList().clearSelection();
				}
            	
            });
            
		}
		return objectList;
	}
	
	/**
	 * This method initializes partScrollPane
	 * 	
	 * @return javax.swing.JScrollPane
	 */
	public JScrollPane getPartScrollPane() {
		if (partScrollPane == null) {
			try {
				partScrollPane = new JScrollPane(getPartList());
				partScrollPane.setWheelScrollingEnabled(true);
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return partScrollPane;
	}
	
	/**
	 * This method initializes partPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getPartPanel() {
		if (partPanel == null) {
			try {
				BorderLayout bl = new BorderLayout();
				partPanel = new JPanel();
				partPanel.setLayout(bl);
				partPanel.setBorder(null);
				partPanel.setPreferredSize(new Dimension(200, 500));
				partPanel.add(new JLabel("Velg planlagt del"),BorderLayout.NORTH);
				partPanel.add(getPartScrollPane(),BorderLayout.CENTER);
				partPanel.add(getPartButtonsPanel(),BorderLayout.SOUTH);
				
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return partPanel;
	}
	
	/**
	 * This method initializes partList	
	 * 	
	 * @return javax.swing.JList
	 */
	public JList getPartList() {
		if (partList == null) {
			// create list
            partList = new JList();
            partList.setCellRenderer(new IconListCellRenderer(1));
            partList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
            //partList.setPreferredSize(new Dimension(180, 430));
            // add listener
            partList.addListSelectionListener(new ListSelectionListener() {

				public void valueChanged(ListSelectionEvent e) {
					
					// is value adjusting?
					if(e.getValueIsAdjusting()) return;
					
					// show buttons panel
					getPartButtonsPanel().setVisible(true);
					
					// consume?
					if(listsAreChangeing) return;
					
					// get mso object
					IMsoObjectIf msoObject = (IMsoObjectIf)partList.getSelectedValue();
					
					// notify
					fireOnElementSelected(new ElementEvent(partList,
							msoObject,ElementEventType.SELECTED,ElementType.PART));
					
					
				}
				
			});            
		}
		return partList;
	}
	
	/**
	 * This method initializes objectButtonsPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getObjectButtonsPanel() {
		if (objectButtonsPanel == null) {
			try {
				objectButtonsPanel = new ButtonPanel(getObjectList(),ElementType.OBJECT);		
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return objectButtonsPanel;
	}	
		
	/**
	 * This method initializes partButtonsPanel
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getPartButtonsPanel() {
		if (partButtonsPanel == null) {
			try {
				partButtonsPanel = new ButtonPanel(getPartList(),ElementType.PART);		
			} catch (java.lang.Throwable e) {
				e.printStackTrace();
			}
		}
		return partButtonsPanel;
	}	
		
	public void addElementListener(IElementEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeElementListener(IElementEventListener listener) {
		listeners.remove(listener);
	}

	private void fireOnElementSelected(ElementEvent e)
    {
		int j = 0;
    	// notify listeners
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onElementSelected(e);
        }
    }

	private void fireOnElementEdit(ElementEvent e)
    {
    	// notify listeners
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onElementEdit(e);
        }
    }
	
	private void fireOnElementCenterAt(ElementEvent e)
    {
    	// notify listeners
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onElementCenterAt(e);
        }
    }
    
	private void fireOnElementDelete(ElementEvent e)
    {
    	// notify listeners
        for (int i = 0; i < listeners.size(); i++){
            listeners.get(i).onElementDelete(e);
        }
    }
	
	
	private void reset() {
		// set flag
		listsAreChangeing = true; 
		// clear selection
		getTypeList().clearSelection();
		getObjectList().clearSelection();
		getPartList().clearSelection();
		// set flag
		listsAreChangeing = false; 		
	}
	
	private void loadTypes() {
		// get values
		ISearchIf.SearchSubType[] values = ISearchIf.SearchSubType.values();
		// allocate memory
		Object[] listData = new Object[values.length + 3];
		// fill list data
		listData[0] = IMsoManagerIf.MsoClassCode.CLASSCODE_OPERATIONAREA;
		listData[1] = IMsoManagerIf.MsoClassCode.CLASSCODE_SEARCHAREA;
		listData[2] = IMsoManagerIf.MsoClassCode.CLASSCODE_POI;
		for (int i = 0; i < values.length; i++) {
			listData[i + 3] = values[i];
		}
		getTypeList().setListData(listData);
	}
		
	private void loadObjects(Enum e) {
		
		// initialize data
		Object[] data = null;
		
		// clear buffer
		objects.clear();
		
		// get command post
		ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();

		// has command post?
		if(cp!=null) {
			// set current type
			if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(e)) {
				// get operation areas
				Collection<IOperationAreaIf> c = cp.getOperationAreaListItems();
				// get data?
				if(!c.isEmpty()) {
					data = c.toArray();
					objects.addAll(c);
				}

			}	
			else if(MsoClassCode.CLASSCODE_SEARCHAREA.equals(e)) {
				// get search areas
				Collection<ISearchAreaIf> c = cp.getSearchAreaListItems();
				// get data?
				if(!c.isEmpty()) {
					data = c.toArray();
					objects.addAll(c);
				}

			}
			else if(e instanceof SearchSubType ||
					MsoClassCode.CLASSCODE_AREA.equals(e) ) {
				// get areas
				Collection<IAreaIf> c = cp.getAreaList().selectItems(
						getAreaSelector((SearchSubType)e), null);				// get data?
				if(!c.isEmpty()) {
					data = c.toArray();
					objects.addAll(c);
				}
			}
			else if(MsoClassCode.CLASSCODE_POI.equals(e)) {
				// get areas
				Collection<IPOIIf> c = cp.getPOIList().selectItems(getPOISelector(),null);
				// get data?
				if(!c.isEmpty()) {
					data = c.toArray();
					objects.addAll(c);
				}
			}		
		}
		// set flag
		listsAreChangeing = true;
		// update model
		if(data==null)
			getObjectList().setModel(new DefaultListModel());
		else
			getObjectList().setListData(data);
		// reset flag
		listsAreChangeing = false;
		// hide buttons panel
		getObjectButtonsPanel().setVisible(false);
		// forward
		loadParts();
	}
	 
	private void loadParts() {
		
		// initialize data
		Object[] data = null;
		
		// clear buffer
		parts.clear();
		
		// get selected object
		IMsoObjectIf msoObject = (IMsoObjectIf)getObjectList().getSelectedValue();
		
		// has selected item?
		if(msoObject!=null) {
		
			// get command post
			ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();
	
			// has command post?
			if(cp!=null) {
				// set current type
				if(msoObject instanceof IOperationAreaIf || 
						msoObject instanceof ISearchAreaIf ||
						msoObject instanceof IPOIIf) {
					// has no parts
					getPartList().setEnabled(false);
				}	
				else if(msoObject instanceof IAreaIf) {
					// get area
					IAreaIf area = (IAreaIf)msoObject;
					// get geodata items
					Collection<IMsoObjectIf> c = area.getAreaGeodataItems();
		        	// add area POIs
					c.addAll(area.getAreaPOIsItems());
					// get data?
					if(!c.isEmpty()) {
						data = c.toArray();
						parts.addAll(c);
					}
					// parts
					getPartList().setEnabled(true);
				}
			}
		}
		else {
			// has no parts
			getPartList().setEnabled(false);			
		}	
		// set flag
		listsAreChangeing = true;
		// update model
		if(data==null) {
			getPartList().setModel(new DefaultListModel());
		}
		else {
			getPartList().setListData(data);
		}
		// reset flag
		listsAreChangeing = false;
		// hide buttons panel
		getPartButtonsPanel().setVisible(false);
	}
	
	private Selector<IAreaIf> getAreaSelector(final SearchSubType e) {
		// create selector
		return new Selector<IAreaIf>()
	    {
	        public boolean select(IAreaIf anArea)
	        {
	            // get sub type
				Enum subType = MsoUtils.getType(anArea,true);
				// return true if same
				return e!=null && e.equals(subType);
	        }
	    };
	}
	
	private Selector<IPOIIf> getPOISelector() {
		// create selector
		return new Selector<IPOIIf>()
	    {
	        public boolean select(IPOIIf anPOI)
	        {
				// get type
				POIType poiType = anPOI.getType();
				// is area poi?
				return !(poiType.equals(POIType.START) ||
						poiType.equals(POIType.VIA) || 
						poiType.equals(POIType.STOP));
	        }
	    };
	}
		
	private JList getList(IMsoObjectIf msoObject, boolean exists) {
		// get class code
		MsoClassCode code = msoObject.getMsoClassCode();
		// dispatch class code
		if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code) ||
				MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
			// get selected element class
			Enum e = (Enum)typeList.getSelectedValue();
			// belongs object to selected class?
			if(e!=null & e.equals(code)) {
				// return list
				if(exists)
					return objects.contains(msoObject) ? getObjectList() : null;
				else
					return getObjectList();
			}
		}
		else if(MsoClassCode.CLASSCODE_AREA.equals(code)) {
			// get search sub type
			Enum subType = MsoUtils.getType(msoObject,true);
			// get selected element class
			Enum e = (Enum)typeList.getSelectedValue();
			// belongs object to selected class?
			if(e!=null & e.equals(subType)) {
				// return list
				if(exists)
					return objects.contains(msoObject) ? getObjectList() : null;
				else
					return getObjectList();				
			}
		}
		else if(MsoClassCode.CLASSCODE_ROUTE.equals(code) ||
				MsoClassCode.CLASSCODE_TRACK.equals(code)) {
			// get owning area
			IAreaIf area = MsoUtils.getOwningArea(msoObject);
			// has area?
			if(area!=null) {
				// is selected?
				if(getObjectList().getSelectedValue()==area) {
					// validate
					if(exists)
						return parts.contains(msoObject) ? getPartList() : null;
					else
						return getPartList();				
				}
			}
		}
		else if(MsoClassCode.CLASSCODE_POI.equals(code)) {
			// get type
			POIType poiType = ((IPOIIf)msoObject).getType();
			// is area poi?
			boolean isAreaPOI = (poiType.equals(POIType.START) ||
					poiType.equals(POIType.VIA) || 
					poiType.equals(POIType.STOP));
			// is standalone poi?
			if(isAreaPOI) {
				// get owning area
				IAreaIf area = MsoUtils.getOwningArea(msoObject);
				// has area?
				if(area!=null) {
					// is selected?
					if(getObjectList().getSelectedValue()==area) {
						// validate
						if(exists)
							return parts.contains(msoObject) ? getPartList() : null;
						else
							return getPartList();				
					}
				}
			}
			else {
				// get selected element class
				Enum e = (Enum)typeList.getSelectedValue();
				// is pui class selected?
				if(MsoClassCode.CLASSCODE_POI.equals(e)) {
					// validate
					if(exists)
						return objects.contains(msoObject) ? getObjectList() : null;
					else
						return getObjectList();				
				}
			}
		}	

		// did not identiy the type, try local lookup?
		if (exists) {
			if(objects.contains(msoObject)) {
				return getObjectList();
			}
			else if(parts.contains(msoObject)) {
				return getPartList();
			}
		}
		
		// failed
		return null;
	}
	
	private List<IMsoObjectIf> getData(IMsoObjectIf msoObject) {
		// initialize
		List<IMsoObjectIf> list = null;
		// get class code
		MsoClassCode code = msoObject.getMsoClassCode();
		// dispatch class code
		if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code) ||
				MsoClassCode.CLASSCODE_SEARCHAREA.equals(code) ||
				MsoClassCode.CLASSCODE_AREA.equals(code)) {
			// get list
			list = objects;
		}
		else if(MsoClassCode.CLASSCODE_ROUTE.equals(code) ||
				MsoClassCode.CLASSCODE_TRACK.equals(code)) {
			// get list
			list = parts;
		}
		else if(MsoClassCode.CLASSCODE_POI.equals(code)) {
			// get type
			POIType poiType = ((IPOIIf)msoObject).getType();
			// is area poi?
			boolean isAreaPOI = (poiType.equals(POIType.START) ||
					poiType.equals(POIType.VIA) || 
					poiType.equals(POIType.STOP));
			// is standalone poi?
			if(isAreaPOI) {
				// get list
				list = parts;
			}
			else {
				// get list
				list = objects;
			}
		}	
		return list;
	}
	
	public void msoObjectCreated(IMsoObjectIf msoObject, int mask) {
		// get list
		JList list = getList(msoObject,false);		
		// has list?
		if(list!=null) {
			// get current data
			List<IMsoObjectIf> data = getData(msoObject);
			// clear data
			data.clear();
			// get model
			ListModel model = list.getModel();
			// find
			for(int i = 0;i<model.getSize();i++) {
				IMsoObjectIf it = (IMsoObjectIf)model.getElementAt(i);
				if (it!=msoObject)
					data.add(it);
			}
			// add new object
			data.add(msoObject);
			// set flag
			listsAreChangeing = true;
			// set data
			list.setListData(data.toArray());
			// reset flag
			listsAreChangeing = false;
			// select
			list.setSelectedValue(msoObject, true);
		}
	}
	
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// Update lists?
		if(isVisible()) {
			getTypeList().repaint();
			getObjectList().repaint();
			getPartList().repaint();
		}
	}

	public void msoObjectDeleted(IMsoObjectIf msoObject, int mask) {
		// get list
		JList list = getList(msoObject,true);		
		// has list?
		if(list!=null) {
			// get data
			List<IMsoObjectIf> data = getData(msoObject);
			// get selected
			IMsoObjectIf msoSelected = (IMsoObjectIf)list.getSelectedValue();
			// get model
			ListModel model = list.getModel();
			// find
			for(int i = 0;i<model.getSize();i++) {
				IMsoObjectIf it = (IMsoObjectIf)model.getElementAt(i);
				if (it==msoObject)
					data.remove(it);
			}
			// set flag
			listsAreChangeing = true;
			// set data
			list.setListData(data.toArray());
			// reset flag
			listsAreChangeing = false;
			// reselect?
			if(msoSelected!=null && msoObject!=msoSelected)
				list.setSelectedValue(msoSelected, true);
			else if(data.size()>0) { 
				// select first in list
				list.setSelectedIndex(0);
			}
				
		}
	}	
	
	/*
	public void onSelectionChanged(MsoLayerEvent e) throws IOException, AutomationException {
		IMsoFeatureLayer msoLayer = (IMsoFeatureLayer)e.getSource();
		List selection = msoLayer.getSelected();
		if (selection != null && selection.size() > 0) {
			// initialize
			JList list = null;
			ElementType type = null;
			// set flag
			listsAreChangeing = true; 
			// get mso object
			IMsoFeature msoFeature = (IMsoFeature)selection.get(0);
			IMsoObjectIf msoObject = msoFeature.getMsoObject();
			// select class
			if(msoObject!=null) {
				// get list and element type
				type = ElementType.CLASS;
				list = getTypeList();
				// get class code
				MsoClassCode code = msoObject.getMsoClassCode();
				// dispatch class code
				if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code) ||
						MsoClassCode.CLASSCODE_SEARCHAREA.equals(code)) {
					// selected element class
					typeList.setSelectedValue(code,true);
				}
				else if(MsoClassCode.CLASSCODE_AREA.equals(code) ||
						MsoClassCode.CLASSCODE_ROUTE.equals(code) ||
						MsoClassCode.CLASSCODE_TRACK.equals(code)) {
					// get search sub type
					Enum subType = MsoUtils.getType(msoObject,true);
					// selected element class
					if(subType==null)
						typeList.setSelectedValue(SearchSubType.PATROL,true);
					else
						typeList.setSelectedValue(subType,true);
				}
				else if(MsoClassCode.CLASSCODE_POI.equals(code)) {
					// get type
					POIType poiType = ((IPOIIf)msoObject).getType();
					// is area poi?
					boolean isAreaPOI = (poiType.equals(POIType.START) ||
							poiType.equals(POIType.VIA) || 
							poiType.equals(POIType.STOP));
					// is standalone poi?
					if(isAreaPOI) {
						// get search sub type
						Enum subType = MsoUtils.getType(msoObject,true);
						// selected element class
						if(subType==null)
							typeList.setSelectedValue(SearchSubType.PATROL,true);
						else
							typeList.setSelectedValue(subType,true);
					}
					else {
						typeList.setSelectedValue(MsoClassCode.CLASSCODE_POI,true);
					}
				}					
			}
			// contained in objects?
			if(objects.contains(msoObject)) {
				// get list and element type
				type = ElementType.OBJECT;
				list = getObjectList();		
				list.setSelectedValue(msoObject, true);
			}
			else {
				// get owner
				IMsoObjectIf msoOwner = MsoUtils.getOwningArea(msoObject); 
				// owner contained in objects?
				if(objects.contains(msoOwner)) {
					// get list and element type
					type = ElementType.OBJECT;
					list = getObjectList();		
					list.setSelectedValue(msoOwner, true);
					// object contained in parts?
					if(parts.contains(msoObject)) {
						// get list and element type
						type = ElementType.PART;
						list = getPartList();		
						list.setSelectedValue(msoObject, true);
					}
				}
			}
			// reset flag
			listsAreChangeing = false; 
			// notify
			fireOnElementSelected(new ElementEvent(list,
					msoObject,ElementEventType.SELECTED,type));
		}
		else {
			reset();
		}
	}
	*/
	
	private class ButtonPanel extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		private JList list = null;
		private ElementType type = null;
		private JButton editButton = null;
		private JButton deleteButton = null;
		private JButton centerAtButton = null;

		ButtonPanel(JList list, ElementType type) {
			// prepare
			this.list = list;
			this.type = type;
			// initialize gui
			initialize();
		}
		
		private void initialize() {
			FlowLayout fl = new FlowLayout();
			fl.setHgap(1);
			fl.setVgap(0);
			fl.setAlignment(FlowLayout.RIGHT);
			setLayout(fl);
			add(getEditButton(), null);		
			add(getDeleteButton(), null);		
			add(getCenterAtButton(), null);		
		}
		
		/**
		 * This method initializes editButton
		 * 	
		 * @return javax.swing.JButton
		 */
		public JButton getEditButton() {
			if (editButton == null) {
				try {
					editButton = new JButton();
					editButton.setIcon(Utils.getIcon("IconEnum.EDIT.icon"));
					editButton.setToolTipText(Utils.getProperty("IconEnum.EDIT.text"));
					Dimension size = Utils.getApp().getUIFactory().getSmallButtonSize();
					editButton.setPreferredSize(size);
					editButton.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							
							// get selected item
							IMsoObjectIf msoObject = (IMsoObjectIf)list.getSelectedValue();

							// notify
							fireOnElementEdit(new ElementEvent(editButton,
									msoObject,ElementEventType.EDIT,type));
							
						}
						
					});
				} catch (java.lang.Throwable e) {
					e.printStackTrace();
				}
			}
			return editButton;
		}	
		
		/**
		 * This method initializes deleteButton
		 * 	
		 * @return javax.swing.JButton
		 */
		public JButton getDeleteButton() {
			if (deleteButton == null) {
				try {
					deleteButton = new JButton();
					deleteButton.setIcon(Utils.getIcon("IconEnum.DELETE.icon"));
					deleteButton.setToolTipText(Utils.getProperty("IconEnum.DELETE.text"));
					Dimension size = Utils.getApp().getUIFactory().getSmallButtonSize();
					deleteButton.setPreferredSize(size);
					deleteButton.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
						
							// get selected item
							IMsoObjectIf msoObject = (IMsoObjectIf)list.getSelectedValue();

							// notify
							fireOnElementDelete(new ElementEvent(deleteButton,
									msoObject,ElementEventType.DELETE,type));
							
						}
						
					});
				} catch (java.lang.Throwable e) {
					e.printStackTrace();
				}
			}
			return deleteButton;
		}	
		
		/**
		 * This method initializes centerAtButton
		 * 	
		 * @return javax.swing.JButton
		 */
		public JButton getCenterAtButton() {
			if (centerAtButton == null) {
				try {
					centerAtButton = new JButton();
					centerAtButton.setIcon(Utils.getIcon("IconEnum.CENTERAT.icon"));
					centerAtButton.setToolTipText(Utils.getProperty("IconEnum.CENTERAT.text"));
					Dimension size = Utils.getApp().getUIFactory().getSmallButtonSize();
					centerAtButton.setPreferredSize(size);
					centerAtButton.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							
							// get selected item
							IMsoObjectIf msoObject = (IMsoObjectIf)list.getSelectedValue();

							// notify
							fireOnElementCenterAt(new ElementEvent(centerAtButton,
									msoObject,ElementEventType.CENTER_AT,type));
							
						}
						
					});
				} catch (java.lang.Throwable e) {
					e.printStackTrace();
				}
			}
			return centerAtButton;
		}	
		
	}
	
	public class ElementEvent extends EventObject {

		private static final long serialVersionUID = 1L;
		
		private ElementEventType event = null;
		private Object element = null;
		private ElementType type = null;
		
		public ElementEvent(Object source, 
				Object element, ElementEventType event, ElementType type) {
			super(source);
			this.event = event;
			this.type = type;
			this.element = element;
		}
		
		public Object getElement() {
			return element;
		}
		
		public ElementEventType getEventType() {
			return event;
		}
		
		public ElementType getType() {
			return type;
		}
		
		public boolean isSelectedEvent() {
			return (event == ElementEventType.SELECTED);
		}
		
		public boolean isCenterAtEvent() {
			return (event== ElementEventType.CENTER_AT);
		}
		
		public boolean isDeleteEvent() {
			return (event == ElementEventType.DELETE);
		}
		
		public boolean isEditEvent() {
			return (event == ElementEventType.EDIT);
		}
		
		public boolean isClassElement() {
			return (type == ElementType.CLASS);
		}
		
		public boolean isObjectElement() {
			return (type == ElementType.OBJECT);
		}
		
		public boolean isPartElement() {
			return (type == ElementType.PART);
		}
	}
	
	public interface IElementEventListener extends EventListener {
		
		public void onElementSelected(ElementEvent e);
		
		public void onElementEdit(ElementEvent e);

		public void onElementCenterAt(ElementEvent e);

		public void onElementDelete(ElementEvent e);
		
	}

	
}