package org.redcross.sar.gui.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.redcross.sar.Application;
import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.factory.DiskoButtonFactory;
import org.redcross.sar.gui.factory.UIFactory;
import org.redcross.sar.gui.UIConstants.ButtonSize;
import org.redcross.sar.gui.renderer.MsoListCellRenderer;
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

public class ElementPanel extends TogglePanel {

	private static final long serialVersionUID = 1L;

	public enum ElementEventType {
		SELECTED,
		CENTERAT,
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

	private JScrollPane typeScrollPane;
	private JScrollPane objectScrollPane;
	private JScrollPane partScrollPane;

	private JPanel listsPanel;
	private JPanel typePanel;
	private JPanel objectPanel;
	private JPanel partPanel;

	private ButtonPanel objectButtonsPanel;
	private ButtonPanel partButtonsPanel;

	private JList typeList;
	private JList objectList;
	private JList partList;

	private List<IElementEventListener> listeners;

	private List<IMsoObjectIf> objects;
	private List<IMsoObjectIf> parts;
	
	private List<Enum<?>> types;
	private Map<Enum<?>,Boolean> editable;

	public ElementPanel() {

		// set caption
		super("Elementer",false,true,ButtonSize.SMALL);

		// prepare
		this.msoModel = Application.getInstance().getMsoModel();
		this.listeners = new ArrayList<IElementEventListener>();
		this.objects = new ArrayList<IMsoObjectIf>();
		this.parts = new ArrayList<IMsoObjectIf>();

		// initialize gui
		initialize();

		// load units
		loadTypes();

	}

	private void initialize() {
		// set lists panel
		setNotScrollBars();
		setContainer(getListsPanel());
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
				listsPanel.add(getPartPanel());
				listsPanel.add(getObjectPanel());
				listsPanel.add(getTypePanel());
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
				typeScrollPane = UIFactory.createScrollPane(getTypeList(),true);
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
				typePanel.setPreferredSize(new Dimension(200, 400));
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
				typeList.setCellRenderer(new MsoListCellRenderer(0,false,
						true,"32x32",MsoListCellRenderer.MAP_ICON_TO_TYPE));
				typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				typeList.addListSelectionListener(new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {

						// is value adjusting?
						if(e.getValueIsAdjusting() || listsAreChangeing) return;

						// load objects of spesified type
						loadObjects((Enum<?>)typeList.getSelectedValue());
						
						// notify
						fireOnElementSelected(new ElementEvent(typeList,
								typeList.getSelectedValue(),ElementEventType.SELECTED,ElementType.CLASS));						

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
				objectScrollPane = UIFactory.createScrollPane(getObjectList(),true);
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
				objectPanel.setPreferredSize(new Dimension(200, 400));
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
            objectList.setCellRenderer(new MsoListCellRenderer(1,false,true,
            		"32x32",MsoListCellRenderer.MAP_ICON_TO_TYPE));
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
				partScrollPane = UIFactory.createScrollPane(getPartList(),true);
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
				partPanel.setPreferredSize(new Dimension(200, 400));
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
            partList.setCellRenderer(new MsoListCellRenderer(1,false,true,
            		"32x32",MsoListCellRenderer.MAP_ICON_TO_TYPE));
            partList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
	public ButtonPanel getObjectButtonsPanel() {
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
	public ButtonPanel getPartButtonsPanel() {
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

	public List<Enum<?>> getTypes() {
		return new ArrayList<Enum<?>>(types);
	}
	
	public boolean contains(Enum<?> type) {
		return types.contains(type);
	}
	
	public boolean isEditable(Enum<?> type) {
		if(editable.containsKey(type)) {
			return editable.get(type); 
		}
		return false;
	}
	
	public void setEditable(Enum<?> type, boolean isEditable) {
		editable.put(type,isEditable);		
	}
	
	public void setEditable(EnumSet<?> types, boolean isEditable) {
		for(Enum<?> it : types)
			setEditable(it, isEditable);
	}
	
	
	private void loadTypes() {
		// get values
		ISearchIf.SearchSubType[] values = ISearchIf.SearchSubType.values();
		// allocate memory
		Enum<?>[] listData = new Enum<?>[values.length + 4];
		// fill list data
		listData[0] = MsoClassCode.CLASSCODE_OPERATIONAREA;
		listData[1] = MsoClassCode.CLASSCODE_SEARCHAREA;
		listData[2] = MsoClassCode.CLASSCODE_POI;
		for (int i = 0; i < values.length; i++) {
			listData[i + 3] = values[i];
		}
		listData[listData.length-1] = MsoClassCode.CLASSCODE_UNIT;
		types = new ArrayList<Enum<?>>();
		editable = new HashMap<Enum<?>,Boolean>();
		for(Enum<?> it : listData) {
			types.add(it);
			editable.put(it, true);
		}
		getTypeList().setListData(listData);
	}

	private void loadObjects(Enum<?> e) {

		// initialize data
		Object[] data = null;
		ArrayList<IMsoObjectIf> c = null;

		// clear buffer
		objects.clear();

		if(msoModel.getMsoManager().operationExists()) {
			
			// get command post
			ICmdPostIf cp = msoModel.getMsoManager().getCmdPost();

			// has command post?
			if(cp!=null) {
				// set current type
				if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(e)) {
					// get operation areas
					c = new ArrayList<IMsoObjectIf>(cp.getOperationAreaListItems());
					// get data?
					if(!c.isEmpty()) {
						data = c.toArray();
						objects.addAll(c);
					}
				}
				else if(MsoClassCode.CLASSCODE_SEARCHAREA.equals(e)) {
					// get search areas
					c = new ArrayList<IMsoObjectIf>(cp.getSearchAreaListItems());
					// get data?
					if(!c.isEmpty()) {
						data = c.toArray();
						objects.addAll(c);
					}
				}
				else if(e instanceof SearchSubType ||
						MsoClassCode.CLASSCODE_AREA.equals(e) ) {
					// get areas
					c = new ArrayList<IMsoObjectIf>(cp.getAreaList().selectItems(getAreaSelector((SearchSubType)e), null));
				}
				else if(MsoClassCode.CLASSCODE_POI.equals(e)) {
					// get areas
					c = new ArrayList<IMsoObjectIf>(cp.getPOIList().selectItems(getPOISelector(),null));
				}
				else if(MsoClassCode.CLASSCODE_UNIT.equals(e)) {
					// get search areas
					c = new ArrayList<IMsoObjectIf>(cp.getUnitListItems());
					// get data?
					if(!c.isEmpty()) {
						data = c.toArray();
						objects.addAll(c);
					}
				}				
			}
			
			// set flag
			listsAreChangeing = true;

			// get data?
			if(!c.isEmpty()) {
				// sort objects
				MsoUtils.sortByName(c,1);
				data = c.toArray();
				objects.addAll(c);
				// update button state
				getObjectButtonsPanel().setEditableFromType(e);
			}

			// update model
			if(data==null)
				getObjectList().setModel(new DefaultListModel());
			else
				getObjectList().setListData(data);
			// reset flag
			listsAreChangeing = false;
			// update layout
			getObjectButtonsPanel().setVisible(false);
		}
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

			if(Application.getInstance().getMsoModel().getMsoManager().operationExists()) {

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
					// update button state
					getPartButtonsPanel().setEditableFromType(msoObject.getMsoClassCode());
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
		// update layout
		getPartButtonsPanel().setVisible(false);
	}

	private Selector<IAreaIf> getAreaSelector(final SearchSubType e) {
		// create selector
		return new Selector<IAreaIf>()
	    {
	        public boolean select(IAreaIf anArea)
	        {
	            // get sub type
				Enum<?> subType = MsoUtils.getType(anArea,true);
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

				// is not an area poi?
				return !IPOIIf.AREA_SET.contains(poiType);

	        }
	    };
	}

	private JList getList(IMsoObjectIf msoObject, boolean exists) {
		if(msoObject!=null) {
			// get class code
			MsoClassCode code = msoObject.getMsoClassCode();
			// dispatch class code
			if(MsoClassCode.CLASSCODE_OPERATIONAREA.equals(code) ||
					MsoClassCode.CLASSCODE_SEARCHAREA.equals(code) ||
					MsoClassCode.CLASSCODE_UNIT.equals(code)) {
				// get selected element class
				Enum<?> e = (Enum<?>)typeList.getSelectedValue();
				// belongs object to selected class?
				if(e!=null && e.equals(code)) {
					// return list
					if(exists)
						return objects.contains(msoObject) ? getObjectList() : null;
					else
						return getObjectList();
				}
			}
			else if(MsoClassCode.CLASSCODE_AREA.equals(code)) {
				// get search sub type
				Enum<?> subType = MsoUtils.getType(msoObject,true);
				subType = MsoUtils.getType(msoObject,true);
				// get selected element class
				Enum<?> e = (Enum<?>)typeList.getSelectedValue();
				// belongs object to selected class?
				if(e!=null && e.equals(subType)) {
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
				boolean isAreaPOI = IPOIIf.AREA_SET.contains(poiType);;
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
					Enum<?> e = (Enum<?>)typeList.getSelectedValue();
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

			// did not identify the type, try local lookup?
			if (exists) {
				if(objects.contains(msoObject)) {
					return getObjectList();
				}
				else if(parts.contains(msoObject)) {
					return getPartList();
				}
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
				MsoClassCode.CLASSCODE_AREA.equals(code) ||
				MsoClassCode.CLASSCODE_UNIT.equals(code)) {
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
			boolean isAreaPOI = IPOIIf.AREA_SET.contains(poiType);
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

	@Override
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
			// get selected
			IMsoObjectIf msoSelected = (IMsoObjectIf)list.getSelectedValue();
			// set data
			list.setListData(data.toArray());
			// select?
			if(msoSelected!=null)
				list.setSelectedValue(msoSelected, true);
			// reset flag
			listsAreChangeing = false;
		}
	}

	@Override
	public void msoObjectChanged(IMsoObjectIf msoObject, int mask) {
		// Update lists?
		if(isVisible()) {
			getTypeList().repaint();
			getObjectList().repaint();
			getPartList().repaint();
		}
	}

	@Override
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
			// reselect?
			if(msoSelected!=null && msoObject!=msoSelected)
				list.setSelectedValue(msoSelected, true);
			else if(data.size()>0) {
				// select first in list
				list.setSelectedIndex(0);
			}
			// reset flag
			listsAreChangeing = false;

		}
	}

	@Override
	protected void msoObjectClearAll(IMsoObjectIf msoObject, int mask) {
		loadObjects(null);
	}

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
					editButton = DiskoButtonFactory.createButton(
							(ElementType.PART.equals(type) ? "GENERAL.EQUAL" : "GENERAL.PLUS"), ButtonSize.SMALL);
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
					deleteButton = DiskoButtonFactory.createButton("GENERAL.DELETE",ButtonSize.SMALL);
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
					centerAtButton = DiskoButtonFactory.createButton("MAP.CENTERAT",ButtonSize.SMALL);
					centerAtButton.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {

							// get selected item
							IMsoObjectIf msoObject = (IMsoObjectIf)list.getSelectedValue();

							// notify
							fireOnElementCenterAt(new ElementEvent(centerAtButton,
									msoObject,ElementEventType.CENTERAT,type));

						}

					});
				} catch (java.lang.Throwable e) {
					e.printStackTrace();
				}
			}
			return centerAtButton;
		}
		
		public void setEditableFromType(Enum<?> type) {
			boolean isEditable = isEditable(type);
			getEditButton().setEnabled(isEditable);
			getDeleteButton().setEnabled(isEditable);
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
			return (event== ElementEventType.CENTERAT);
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
