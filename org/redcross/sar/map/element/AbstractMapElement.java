package org.redcross.sar.map.element;

import java.util.List;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.IData;
import org.redcross.sar.map.event.IMapElementListener;
import org.redcross.sar.map.event.MapElementEvent;

import com.esri.arcgis.geometry.IEnvelope;

public abstract class AbstractMapElement<E,D extends IData,G extends IData> implements IMapElement<E,D,G> {

	private static final long serialVersionUID = 1L;

	protected String caption;
	protected boolean isVisible = true;
	protected boolean isSelected = false;
	protected boolean isCaptionShown = true;
	
	protected Object id;
	protected D dataObj;
	protected IGeodataCreator<D, G> creator;
	
	protected EventListenerList listeners = new EventListenerList();

	/* =======================================================
	 * Constructors
	 * ======================================================= */

	public AbstractMapElement(Object id, D dataObj, IGeodataCreator<D,G> creator) {

		// forward
		super();

		// prepare
		this.id = id;
		this.dataObj = dataObj;
		this.creator = creator;
		
	}


	/* =======================================================
	 * IMapFeature implementation
	 * ======================================================= */

	public abstract IEnvelope getExtent();
	
	public Object getID() {
		return id;
	}
	
	public D getDataObject() {
		return dataObj;
	}
	
	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		if(this.isVisible != isVisible) {
			this.isVisible = isVisible;
			fireElementChanged(MapElementEvent.STATE_EVENT);
		}
	}
	
	public boolean isSelected() {
		return isSelected;
	}
	
	public void setSelected(boolean isSelected) {
		if(this.isSelected != isSelected) {
			this.isSelected = isSelected;
			fireElementChanged(MapElementEvent.STATE_EVENT);
		}
			
	}
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		if(this.caption != caption) {
			this.caption = caption;
			fireElementChanged(MapElementEvent.STATE_EVENT);
		}
	}
	
	public boolean isCaptionShown() {
		return isCaptionShown;
	}

	public void setCaptionShown(boolean isShown) {
		isCaptionShown = isShown;
	}

	public List<G> create() {
		return creator.create(dataObj);
	}
	
	public List<G> getGeodataObjects() {
		boolean isChanged = creator.isChanged();
		List<G> list = creator.getGeodataObjects();
		if(isChanged) fireElementChanged(MapElementEvent.CREATE_EVENT);
		// notify
		return list;
	}
	
	public int getGeodataObjectCount() {
		return creator.getGeodataObjectsCount();
	}
	
	public boolean isChanged() {
		return creator.isChanged();
	}
		
	public boolean isDirty() {
		return creator.isChanged();
	}
	
	public void addMapElementListener(IMapElementListener listener) {
		listeners.add(IMapElementListener.class, listener);
	}

	public void removeMapElementListener(IMapElementListener listener) {
		listeners.remove(IMapElementListener.class, listener);
	}

	/* =======================================================
	 * Helper methods
	 * ======================================================= */

	protected void fireElementChanged(int type) {
		MapElementEvent e = new MapElementEvent(this,type);
		IMapElementListener[] list = listeners.getListeners(IMapElementListener.class);
		for(int i=0; i<list.length;i++) {
			list[i].onElementChanged(e);
		}
	}

}