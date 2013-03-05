package org.redcross.sar.map.layer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.map.IDiskoMapManager;
import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.event.IMapElementListener;
import org.redcross.sar.map.event.IMapLayerEventListener;
import org.redcross.sar.map.event.MapElementEvent;
import org.redcross.sar.map.event.MapLayerEventStack;
import org.redcross.sar.map.event.MapLayerEvent.MapLayerEventType;

import com.esri.arcgis.geometry.IEnvelope;

/**
 * Abstract helper class for implementation of IMapLayer classes.</p>
 * 
 * 1. Extend this class with appropriate parameters.</p>
 * 
 * This include selection of a map layer class (type I) that is supported by the {@link IMap} implementation, 
 * selection of layer element class (type E) that is supported by the map layer class, and selection
 * of data class (type D) that implements the geodata structures required to create element objects.</p>
 * 
 * 2. Implement abstract methods </p>
 * 
 * The are three groups of nested methods to implement. The first group of methods (addDataObject, removeDataObject,
 * updateDataObject and clearDataObjects) implements loading, addition, update and removal of data objects 
 * (of type D). The second group of methods (addElementImpl, removeElementImpl, clearElementImpl) implements 
 * addition and removal of the objects (of type E) that implement the IMapElement interface. For example, the 
 * addDataObject
 * method calls  
 * 
 * The last group of methods </p>
 * 
 * @author kenneth
 *
 * @param <I> - The class or interface that implements the element class E. 
 * @param <E> - The class or interface that implements the IMapElement interface
 * @param <D> - The class or interface that implements the object containing the geodata used to create each IMapElement object
 * @param <D> - The class or interface that implements the geodata used to create each IMapElement object
 */

public abstract class AbstractMapLayer<I, E, D extends IData, G extends IData> implements IElementLayer<I,E,D,G> {

	private static Logger logger = Logger.getLogger(AbstractMapLayer.class);  
	
	protected int suspendedCount = 0;
	protected int selectionCount = 0;
	protected int visibleCount = 0;

	protected boolean isInterestChanged = false;
	protected boolean isSelectable = true;
	protected boolean isCaptionShown = true;
	
	protected Enum<?> classCode;
	protected Enum<?> layerCode;
	protected List<IMapElement<E,D,G>> elementList;
	protected Map<Object,IMapElement<E,D,G>> elementMap;
	
	protected IDiskoMapManager manager;
	protected MapLayerEventStack eventStack;

	protected int refreshRate;
	protected long refreshTime;
	protected IEnvelope dirtyExtent;
	
	protected final Map<Integer,Selector<D>> selectors = new HashMap<Integer,Selector<D>>();
	

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public AbstractMapLayer(String name, Enum<?> classCode, 
			Enum<?> layerCode, MapLayerEventStack eventStack) {

		// forward
		super();

		// prepare
		this.classCode = classCode;
		this.layerCode = layerCode;
		this.eventStack = eventStack;
		this.elementList = new ArrayList<IMapElement<E,D,G>>();
		this.elementMap = new HashMap<Object,IMapElement<E,D,G>>();
		
		// initialize refresh state
		this.refreshRate = -1;
		this.refreshTime = System.currentTimeMillis();
		
		// nothing to refresh
		this.dirtyExtent = null;

	}

	/* ===============================================================
	 * IMapLayer implementation
	 * =============================================================== */

	public abstract I getLayerImpl();
	public abstract List<G> getGeodataObjects(D dataObj);
	
	public Enum<?> getClassCode() {
		return classCode;
	}
	
	public Enum<?> getLayerCode() {
		return layerCode;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int inMillis) {
		refreshRate = inMillis;
	}
	
	public boolean isSelectable() {
		return isSelectable;
	}

	public void setSelectable(boolean isSelectable) {
		this.isSelectable = isSelectable;
	}
	
	public List<IMapElement<E,D,G>> loadDataObjects(List<D> dataObjs)  {

		// initialize list
		List<IMapElement<E,D,G>> elements = new Vector<IMapElement<E,D,G>>(dataObjs.size());

		try {
			// remove all elements
			setDirtyExtent(clearDataObjects());
			// add data object to layer
			for (D it : dataObjs) {
				// update refresh extent
				setDirtyExtent(addDataObject(it));
				// add element to work list
				elements.add(getElement(it));
			}
			
			// forward
			refresh();

			// finished
			return elements;
			
		} catch (Exception e) {
			logger.error("Failed to load objects");
		}

		// failed
		return null;

	}
	
	/** Add an IMapElement<E,D,G> object to layer */
	public IMapElement<E,D,G> addDataObject(D dataObj) {
		// create implementation
		IMapElement<E,D,G> element = createElementImpl(dataObj);
		// add element to layer
		IEnvelope extent = addElement(element);
		// append extent
		setDirtyExtent(extent);
		// forward
		refresh();
		// finished
		return element;
	}
	
	/** Update an IMapElement<E,D,G> object with new data*/
	public IMapElement<E,D,G> updateDataObject(D dataObj) {
		// get implementation
		IMapElement<E, D, G> element = getElement(dataObj);
		// update implementation
		IEnvelope extent = updateElement(element);
		// append extent
		setDirtyExtent(extent);
		// forward
		refresh();
		// finished
		return element;		
	}
	
	/** Remove an IMapElement<E,D,G> object from layer */
	public IMapElement<E,D,G> removeDataObject(D dataObj) {
		// get element
		IMapElement<E,D,G> element = getElement(dataObj);
		// remove implementation
		IEnvelope extent = removeElement(element);
		// append extent
		setDirtyExtent(extent);
		// forward
		refresh();
		// finished
		return element;		
	}
	
	public boolean removeAll() {
		return clearDataObjects().size()>0;
	}
	
	/** Remove all IMapElement<E,D,G> objects from layer */
	public List<IMapElement<E,D,G>> clearDataObjects() {
		
		// initialize list
		List<IMapElement<E,D,G>> elements = new Vector<IMapElement<E,D,G>>(elementList);

		try {
			// remove all elements
			for (IMapElement<E,D,G> it : elements) {
				setDirtyExtent(removeElement(it));
			}
			
			// forward
			refresh();

			// finished
			return elements;
			
		} catch (Exception e) {
			logger.error("Failed to clear objects");
		}

		// failed
		return null;
		
	}	
	
	public void refresh() {
		
		// forward
		refresh(dirtyExtent);
		
		// reset
		clearDirtyExtent();
		
	}

	public void addMapLayerEventListener(IMapLayerEventListener listener) {
		if (eventStack!=null) {
			eventStack.addMapLayerEventListener(listener);
		}
	}

	public void removeMapLayerEventListener(IMapLayerEventListener listener) {
		if (eventStack!=null) {
			eventStack.removeMapLayerEventListener(listener);
		}
	}	
	
	public IMapElement<E,D,G> getElement(int index) {
		return elementList.get(index);
	}

	public IMapElement<E,D,G> getElement(Object id) {
		return elementMap.get(id);
	}
	
	public IMapElement<E,D,G> getElement(D dataObj) {
		for(IMapElement<E,D,G> it : elementList) {
			if(it.getDataObject().equals(dataObj)) return it;
		}
		return null;
	}
	
	public int getElementCount() {
		return elementList.size();
	}
	
	public void setSelected(IMapElement<E,D,G> element, boolean selected) {
		if(element==null) return;
		if (!element.isSelected() && selected) {
			selectionCount++;
			element.setSelected(true);
			setDirtyExtent(element.getExtent());
			fireOnSelectionChanged(MapLayerEventType.SELECTED_EVENT);
		} else if (element.isSelected() && !selected) {
			selectionCount--;
			element.setSelected(false);
			setDirtyExtent(element.getExtent());
			fireOnSelectionChanged(MapLayerEventType.DESELECTED_EVENT);
		}
		refresh();
	}

	public int clearSelected() {
		int count = 0;
		for (IMapElement<E,D,G> it : elementList) {
			if (it.isSelected()) {
				selectionCount--;
				count++;
				it.setSelected(false);
				setDirtyExtent(it.getExtent());
			}
		}
		refresh();
		selectionCount = 0;
		if(count>0) {
			fireOnSelectionChanged(MapLayerEventType.DESELECTED_EVENT);
		}
		return count;
	}

	public List<IMapElement<E,D,G>> getSelectedElements() {
		ArrayList<IMapElement<E,D,G>> selection = new ArrayList<IMapElement<E,D,G>>();
		for (IMapElement<E,D,G> it : elementList) {
			if (it.isSelected()) {
				selection.add(it);
			}
		}
		return selection;
	}	
	
	public List<D> getSelectedObjects() {
		ArrayList<D> selection = new ArrayList<D>();
		for (IMapElement<E,D,G> it : elementList) {
			if (it.isSelected()) {
				selection.add(it.getDataObject());
			}
		}
		return selection;
	}	
	
	public int getSelectionCount(boolean update) {
		return getSelectedObjects().size();
	}

	public Selector<D> getSelector(int id) {
		return selectors.get(id);
	}

	public boolean addSelector(Selector<D> selector,int id) {
		// replace existing
		selectors.put(id,selector);
		// finished
		return true;
	}

	public boolean removeSelector(int id) {
		if(selectors.containsKey(id)) {
			selectors.remove(id);
			// finished
			return true;
		}
		// failure
		return false;
	}

	public boolean isCaptionShown() {
		return isCaptionShown;
	}

	public void setCaptionShown(boolean isVisible) {
		isCaptionShown = isVisible;
		// TODO: Loop over all elements
		
	}
	
	public IEnvelope getVisibleElementsExtent() {

		// initialize
		IEnvelope extent = null;

		// loop over all elements
		for (IMapElement<E,D,G> it : elementList) {
			// is visible?
			if(it.isVisible()) {
				extent = getUnion(extent,it);
			}
		}
		// finished!
		return extent;
	}

	public void setVisibleElements(boolean isVisible) {

		// loop over all elements
		for (IMapElement<E,D,G> it : elementList) {
			// make visible
			if(!it.isVisible() && isVisible) {
				visibleCount++;
				it.setVisible(true);
			} else if(it.isVisible() && !isVisible) {
				visibleCount--;
				it.setVisible(false);
			}
		}
		
	}

	public void setVisibleElements(D dataObj, boolean match, boolean others) {

		// get id
		IMapElement<E,D,G> element = getElement(dataObj);

		// loop over all elements
		for (IMapElement<E,D,G> it : elementList) {
			// make visible?
			if(it.getID().equals(element.getID())) {
				// make visible
				if(!it.isVisible() && match) {
					visibleCount++;
					it.setVisible(true);
				} else if(it.isVisible() && !match) {
					visibleCount--;
					it.setVisible(false);
				}
			}
			else {
				// make visible
				if(!it.isVisible() && others) {
					visibleCount++;
					it.setVisible(true);
				} else if(it.isVisible() && !others) {
					visibleCount--;
					it.setVisible(false);
				}
			}
		}
	}

	public void setVisibleElements(List<D> dataObjs, boolean match, boolean others) {

		// loop over all data objects
		for (D it : dataObjs) {
			// forward
			setVisibleElements(it, match, others);
		}
	}

	public int getVisibleElementCount(boolean update) {
		if(update) {
			// reset
			visibleCount = 0;
			// loop over all elements
			// loop over all elements
			for (IMapElement<E,D,G> it : elementList) {
				// visible?
				if(it.isVisible()) visibleCount++;
			}

		}
		// return count
		return visibleCount;
	}	
	
	public boolean isDirty() {
		try {
			return dirtyExtent!=null && !dirtyExtent.isEmpty();
		} catch (Exception e) {
			logger.error("Failed to get dirty flag",e);
		}
		return false;
	}
	
	public IEnvelope getDirtyExtent() {
		return dirtyExtent;
	}
	
	public boolean isSuspended() {
		return suspendedCount>0;
	}

	public void suspend() {
		// increment
		suspendedCount++;
	}

	public void resume() {
		// decrement?
		if(suspendedCount>1)
			suspendedCount--;
		// only resume on last decrement
		if(suspendedCount==1){
			try {
				// clear count
				suspendedCount = 0;
				// has event stack?
				if (eventStack!=null) {
					eventStack.fire(this);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void consume() {
		// clear stack
		eventStack.consume(this);
	}	
	
	/* ===============================================================
	 * Protected methods
	 * =============================================================== */
	
	protected abstract IMapElement<E,D,G> createElementImpl(D dataObj);

	protected abstract IEnvelope addElementImpl(IMapElement<E,D,G> element);
	protected abstract IEnvelope updateElementImpl(IMapElement<E, D, G> element, List<G> geodata);
	protected abstract IEnvelope removeElementImpl(IMapElement<E,D,G> element);
	
	protected abstract void onElementChanged(MapElementEvent e);
	
	protected abstract void refresh(IEnvelope extent);
	
	protected IEnvelope getUnion(IEnvelope extent, IMapElement<E,D,G> element) {
		if(element!=null) {
			try {
				
				if(extent==null) {
					extent = element.getExtent();
				} else {
					extent.union(element.getExtent());
				}
				
				// finished
				return extent;
				
			} catch (Exception ex) {
				logger.error("Failed to get extent of IElement object",ex);
			}
		}
		return null;		
	}
	
	protected void fireOnSelectionChanged(MapLayerEventType type) {
		if (eventStack!=null) {
			eventStack.push(this, type);
		}
	}

	protected boolean select(IMapElement<E,D,G> element) {
		if(element!=null) {
			for(Selector<D> it : selectors.values()) {
				// select?
				if(it.select(element.getDataObject())) return true;
			}
			// select element is no selectors are defined
			return selectors.isEmpty();
		}
		return false;
	}
	
	protected IEnvelope setDirtyExtent(List<IMapElement<E,D,G>> list) {
		for(IMapElement<E,D,G> it : list) {
			setDirtyExtent(it.getExtent());
		}
		return dirtyExtent;
	}
	
	protected IEnvelope setDirtyExtent(IMapElement<E,D,G> element) {		
		return setDirtyExtent(element.getExtent());
	}
		
	protected IEnvelope setDirtyExtent(IEnvelope extent) {
		try {
			if(extent!=null) {
				if(dirtyExtent == null)
					dirtyExtent = extent;
				else
					dirtyExtent.union(extent);
			}
		} catch (Exception e) {
			logger.error("Failed to set refresh extent",e);
		}
		return dirtyExtent;

	}
	
	protected IEnvelope clearDirtyExtent() {
		IEnvelope extent = dirtyExtent;
		dirtyExtent = null;
		return extent;
	}
	
	/* ===============================================================
	 * Helper methods
	 * =============================================================== */

	private IEnvelope addElement(IMapElement<E,D,G> element) {
		if(!elementList.contains(element) || elementMap.containsKey(element.getID())) {
			elementList.add(element);
			elementMap.put(element.getID(),element);
			element.addMapElementListener(listener);
			return addElementImpl(element);
		}
		return null;
	}

	private IEnvelope updateElement(IMapElement<E, D, G> element) {
		List<G> geodata = element.getGeodataObjects();
		if(element!=null) {
			return updateElementImpl(element,geodata);

		}
		return null;
	}

	private IEnvelope removeElement(IMapElement<E,D,G> element) {
		if(elementList.contains(element) && elementMap.containsKey(element.getID())) {
			elementList.remove(element);
			elementMap.remove(element.getID());
			element.removeMapElementListener(listener);
			return removeElementImpl(element);
		}
		return null;
	}
	
	/* ===============================================================
	 * Anonymous classes
	 * =============================================================== */

	IMapElementListener listener = new IMapElementListener() {

		public void onElementChanged(MapElementEvent e) {
			// forward
			AbstractMapLayer.this.onElementChanged(e);
		}

	};

}