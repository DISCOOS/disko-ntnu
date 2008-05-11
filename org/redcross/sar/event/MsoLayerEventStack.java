package org.redcross.sar.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.redcross.sar.event.MsoLayerEvent.MsoLayerEventType;
import org.redcross.sar.map.layer.IMsoFeatureLayer;

import com.esri.arcgis.interop.AutomationException;

public class MsoLayerEventStack {

	private ArrayList<IMsoLayerEventListener> listeners = null;
	private HashMap<MsoLayerEventType,MsoLayerEvent> stack = null;
	
	public MsoLayerEventStack() {
		// prepare
		listeners = new ArrayList<IMsoLayerEventListener>();
		stack = new HashMap<MsoLayerEventType,MsoLayerEvent>();
	}
	
	public void addMsoLayerEventListener(IMsoLayerEventListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeMsoLayerEventListener(IMsoLayerEventListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public void push(IMsoFeatureLayer layer, MsoLayerEventType type) {
		MsoLayerEvent e = null;
		// get existing event?
		if(stack.containsKey(type))
			e = stack.get(type);
		else {
			e = new MsoLayerEvent(this,type);
			stack.put(type, e);
		}
		// add layer
		e.add(layer);
	}
	
	public void fireAll() throws AutomationException, IOException{
		
		// consume?
		if(stack.size()==0) return;
		
		// start with DESELECTED_EVENT events
		MsoLayerEvent e = stack.get(MsoLayerEventType.DESELECTED_EVENT);
		
		// has event?
		if(e!=null) {
			// is final event?
			e.setFinal(!stack.containsKey(MsoLayerEventType.SELECTED_EVENT));
			// forward
			fire(e);
			// remove from stack
			stack.remove(MsoLayerEventType.DESELECTED_EVENT);
		}
		
		// continue with SELECTED_EVENT events
		e = stack.get(MsoLayerEventType.SELECTED_EVENT);
		
		// has event?
		if(e!=null) {
			// set as final event
			e.setFinal(true);
			// forward
			fire(e);
			// remove from stack
			stack.remove(MsoLayerEventType.SELECTED_EVENT);
		}
		
	}
	
	public void consumeAll() {
		stack.clear();
	}
	
	
	public void fire(IMsoFeatureLayer layer) throws AutomationException, IOException {		
		// consume?
		if(stack.size()==0) return;
		// start with DESELECTED_EVENT events
		MsoLayerEvent e = stack.get(MsoLayerEventType.DESELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// get next event
			e = stack.get(MsoLayerEventType.SELECTED_EVENT);
			// initialize flag
			boolean isFinal = false;
			// is this the final event?
			isFinal = (e==null) ? true : !e.contains(layer);
			// create new event
			e = new MsoLayerEvent(this,MsoLayerEventType.DESELECTED_EVENT);
			// set flag
			e.setFinal(isFinal);
			// add this layer only
			e.add(layer);
			// forward
			fire(e);
			// remove from stack?
			if(isEmpty)
				stack.remove(MsoLayerEventType.DESELECTED_EVENT);
		}
		// continue with SELECTED_EVENT events
		e = stack.get(MsoLayerEventType.SELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// create new event
			e = new MsoLayerEvent(this,MsoLayerEventType.SELECTED_EVENT);
			// add this layer only
			e.add(layer);
			// this is the final event
			e.setFinal(true);
			// forward
			fire(e);
			// remove from stack?
			if(isEmpty)
				stack.remove(MsoLayerEventType.SELECTED_EVENT);
		}
		
	}
	
	public void consume(IMsoFeatureLayer layer) {
		// consume?
		if(stack.size()==0) return;
		// start with DESELECTED_EVENT events
		MsoLayerEvent e = stack.get(MsoLayerEventType.DESELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// remove from stack?
			if(isEmpty)
				stack.remove(MsoLayerEventType.DESELECTED_EVENT);
		}
		// continue with SELECTED_EVENT events
		e = stack.get(MsoLayerEventType.SELECTED_EVENT);
		// has event?
		if(e!=null && e.contains(layer)) {
			// remove layer
			e.remove(layer);
			// get remove flag
			boolean isEmpty = (e.getList().size()== 1);
			// remove from stack?
			if(isEmpty)
				stack.remove(MsoLayerEventType.SELECTED_EVENT);
		}
		
	}	
	
	private void fire(MsoLayerEvent e) throws AutomationException, IOException {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onSelectionChanged(e);
		}				
	}
}
