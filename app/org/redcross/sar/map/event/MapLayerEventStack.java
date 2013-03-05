package org.redcross.sar.map.event;

import java.util.ArrayList;
import java.util.HashMap;

import org.redcross.sar.map.event.MapLayerEvent.MapLayerEventType;
import org.redcross.sar.map.layer.IElementLayer;

@SuppressWarnings("unchecked")
public class MapLayerEventStack {

	private ArrayList<IMapLayerEventListener> listeners = null;
	private HashMap<MapLayerEventType,MapLayerEvent> stack = null;

	public MapLayerEventStack() {
		// prepare
		listeners = new ArrayList<IMapLayerEventListener>();
		stack = new HashMap<MapLayerEventType,MapLayerEvent>();
	}

	public void addMapLayerEventListener(IMapLayerEventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeMapLayerEventListener(IMapLayerEventListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void push(IElementLayer layer, MapLayerEventType type) {
		MapLayerEvent e = null;
		// get existing event?
		if(stack.containsKey(type))
			e = stack.get(type);
		else {
			e = new MapLayerEvent(this,type);
			stack.put(type, e);
		}
		// add layer
		e.add(layer);
	}

	public void fireAll() {

		// consume?
		if(stack.size()==0) return;

		// start with DESELECTED_EVENT events
		MapLayerEvent e = stack.get(MapLayerEventType.DESELECTED_EVENT);

		// has event?
		if(e!=null) {
			// is final event?
			e.setFinal(!stack.containsKey(MapLayerEventType.SELECTED_EVENT));
			// forward
			fire(e);
			// remove from stack
			stack.remove(MapLayerEventType.DESELECTED_EVENT);
		}

		// continue with SELECTED_EVENT events
		e = stack.get(MapLayerEventType.SELECTED_EVENT);

		// has event?
		if(e!=null) {
			// set as final event
			e.setFinal(true);
			// forward
			fire(e);
			// remove from stack
			stack.remove(MapLayerEventType.SELECTED_EVENT);
		}

	}

	public void consumeAll() {
		stack.clear();
	}


	public void fire(IElementLayer layer) {
		// consume?
		if(stack.size()==0) return;
		// start with DESELECTED_EVENT events
		MapLayerEvent e = stack.get(MapLayerEventType.DESELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// get next event
			e = stack.get(MapLayerEventType.SELECTED_EVENT);
			// initialize flag
			boolean isFinal = false;
			// is this the final event?
			isFinal = (e==null) ? true : !e.contains(layer);
			// create new event
			e = new MapLayerEvent(this,MapLayerEventType.DESELECTED_EVENT);
			// set flag
			e.setFinal(isFinal);
			// add this layer only
			e.add(layer);
			// forward
			fire(e);
			// remove from stack?
			if(isEmpty)
				stack.remove(MapLayerEventType.DESELECTED_EVENT);
		}
		// continue with SELECTED_EVENT events
		e = stack.get(MapLayerEventType.SELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// create new event
			e = new MapLayerEvent(this,MapLayerEventType.SELECTED_EVENT);
			// add this layer only
			e.add(layer);
			// this is the final event
			e.setFinal(true);
			// forward
			fire(e);
			// remove from stack?
			if(isEmpty)
				stack.remove(MapLayerEventType.SELECTED_EVENT);
		}

	}

	public void consume(IElementLayer layer) {
		// consume?
		if(stack.size()==0) return;
		// start with DESELECTED_EVENT events
		MapLayerEvent e = stack.get(MapLayerEventType.DESELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// remove from stack?
			if(isEmpty)
				stack.remove(MapLayerEventType.DESELECTED_EVENT);
		}
		// continue with SELECTED_EVENT events
		e = stack.get(MapLayerEventType.SELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// remove from stack?
			if(isEmpty)
				stack.remove(MapLayerEventType.SELECTED_EVENT);
		}

	}

	private void fire(MapLayerEvent e) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onSelectionChanged(e);
		}
	}
}
