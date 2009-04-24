package org.redcross.sar.map.layer;

import java.util.List;

import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.event.IMapLayerEventListener;

import com.esri.arcgis.geometry.IEnvelope;

/**
 * 
 * @author kenneth
 *
 * @param <I> - the class or interface implementing the layer
 * @param <E> - the class or interface that implements the map element
 * @param <D> - the class or interface that implements the data objects containing the geodata required to build the map element 
 * @param <G> - the class or interface that implements the geodata
 */
public interface IElementLayer<I,E,D extends IData, G extends IData> extends IMapLayer<D, IMapElement<E,D,G>> {
	
	public I getLayerImpl();

	public Enum<?> getClassCode();
	
	public Enum<?> getLayerCode();
	
	/** Create IMapElement<E,D,G> from data objects */
	public List<IMapElement<E,D,G>> loadDataObjects(List<D> dataObjs);
	
	/** Add an IMapElement<E,D,G> object to layer */
	public IMapElement<E,D,G> addDataObject(D dataObj);
	
	/** Update an IMapElement<E,D,G> object with new data*/
	public IMapElement<E,D,G> updateDataObject(D dataObj);
	
	/** Remove an IMapElement<E,D,G> object from layer */
	public IMapElement<E,D,G> removeDataObject(D dataObj);
	
	/** Remove all IMapElement<E,D,G> objects from layer */
	public List<IMapElement<E,D,G>> clearDataObjects();
	
	/** Gets list of objects that represents the geodata in the associated IMapElement */
	public List<G> getGeodataObjects(D dataObj);
	
	public boolean isVisible();
	public void setVisible(boolean isVisible);
	
	public int getRefreshRate();
	public void setRefreshRate(int inMillis);
	
	public List<D> getSelectedObjects();
	public List<IMapElement<E,D,G>> getSelectedElements();

	public int clearSelected();
	public int getSelectionCount(boolean update);
	public void setSelected(IMapElement<E,D,G> feature, boolean selected);

	public void addMapLayerEventListener(IMapLayerEventListener listener);
	public void removeMapLayerEventListener(IMapLayerEventListener listener);

	public void suspend();
	public void consume();
	public void resume();
	public boolean isSuspended();

	public IEnvelope getVisibleElementsExtent();

	public void setVisibleElements(boolean isVisible);
	public void setVisibleElements(D dataObj, boolean match, boolean others);
	public void setVisibleElements(List<D> dataObjs, boolean match, boolean others);
	public int getVisibleElementCount(boolean update);

	public int getElementCount();
	public IMapElement<E,D,G> getElement(int index);
	public IMapElement<E,D,G> getElement(Object id);
	public IMapElement<E,D,G> getElement(D dataObj);

	public Selector<D> getSelector(int id);
	public boolean addSelector(Selector<D> selector, int id);
	public boolean removeSelector(int id);

	public boolean isCaptionShown();
	public void setCaptionShown(boolean isVisible);

	public boolean isDirty();
	public IEnvelope getDirtyExtent();
	
	public boolean isSelectable();
	public void setSelectable(boolean isSelectable);	
	
}
