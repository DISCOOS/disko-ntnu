package org.redcross.sar.map.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.layer.IElementLayer;

@SuppressWarnings("unchecked")
public class MapLayerEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum MapLayerEventType {
		SELECTED_EVENT,
		DESELECTED_EVENT
	}

	private boolean isFinal;
	private MapLayerEventType eventType;
	private List<IElementLayer> layers;

	public MapLayerEvent(Object source, MapLayerEventType type) {
		super(source);
		this.isFinal = true;
		this.eventType = type;
		this.layers = new ArrayList<IElementLayer>();
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public MapLayerEventType getEventType() {
		return eventType;
	}

	public List<IElementLayer> getList() {
		return layers;
	}

	public boolean add(IElementLayer layer) {
		if(layer!=null) {
			if(!layers.contains(layer))
					return layers.add(layer);
		}
		return false;
	}

	public boolean contains(IElementLayer layer) {
		return layers.contains(layer);
	}

	public boolean contains(Enum<?> code) {
		for(IElementLayer it: layers) {
			if(it.getLayerCode().equals(code)) return true;
		}
		return false;
	}

	public boolean remove(IElementLayer layer) {
		if(layer!=null) {
			if(layers.contains(layer))
					return layers.remove(layer);
		}
		return false;
	}

	public List<IMapElement> getSelected() {
		List<IMapElement> list = null;
		// loop over all layers
		for(IElementLayer it: layers) {
			// initialize?
			if(list==null)
				list = it.getSelectedElements();
			else
				list.addAll(it.getSelectedElements());
		}
		// finished
		return list;
	}

	public List<?> getSelectedMsoObjects() {
		List<?> list = null;
		// loop over all layers
		for(IElementLayer it: layers) {
			// initialize?
			if(list==null)
				list = it.getSelectedObjects();
			else
				list.addAll(it.getSelectedObjects());
		}
		// finished
		return list;

	}

}
