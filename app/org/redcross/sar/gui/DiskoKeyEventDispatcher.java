package org.redcross.sar.gui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DiskoKeyEventDispatcher implements KeyEventDispatcher {

	private List<Class> passed = null;
	private Map<String,List<KeyListener>> process = null;
	private boolean isEnabled = true;
	
	public DiskoKeyEventDispatcher() {
		// prepare
		passed = new ArrayList<Class>();		
		process = new HashMap<String,List<KeyListener>>();
	}
	
	public boolean addKeyListener(int id, int code, KeyListener listener) {
		// handles this already?
		if(!process(id,code,listener)) {
			// get key
			String key = getKey(id,code);
			// initialize
			List<KeyListener> listeners = null;
			// create listener list?
			if(!process.containsKey(key))
				listeners = new ArrayList<KeyListener>(1);
			else
				listeners = process.get(key);
			// add code?
			if(!listeners.contains(listener))
				listeners.add(listener);
			// update process map
			process.put(key, listeners);			
			// added
			return true;
		}
		// not added
		return false;
	}
	
	public boolean removeKeyListener(int id, int code, KeyListener listener) {
		// exists?
		if(process(id,code,listener)) {
			// get key
			String key = getKey(id, code);
			// get codes list
			List<KeyListener> listeners = process.get(key);
			// remove listener
			listeners.remove(listener);
			// remove listeners list?
			if(listeners.size()==0) process.remove(key);
			// removed
			return true;
		}
		// not removed
		return false;
	}
	
	public boolean dispatchKeyEvent(KeyEvent e) {
		
		// is disabled? (KeybordFocusManager takes over)
		if(!isEnabled) return false;
		
		// pass event? (KeybordFocusManager takes over)
		if(!isPassed(e.getComponent().getClass())) {
		
			// dispatch event?
			if(process(e.getID(),e.getKeyCode())) {
				// dispatch event
				switch(e.getID()) {
					case KeyEvent.KEY_PRESSED: fireKeyPressed(e); break;
					case KeyEvent.KEY_TYPED: fireKeyTyped(e); break;
					case KeyEvent.KEY_RELEASED: fireKeyReleased(e); break;
					default: return false;
				}
				// if event is consumed the KeybordFocusManager will
				// take no further action on the event
				return e.isConsumed();
			}
		}
		// pass event to KeybordFocusManager
		return false;
	}
	
	public boolean process(int id, int code) {
		// get key
		String key = getKey(id,code);
		// has id?
		return process.containsKey(key);
	}
	
	public boolean process(int id, int code, KeyListener listener) {
		// get key
		String key = getKey(id,code);
		// has
		if(process.containsKey(key)) {
			// return?
			return process.get(key).contains(listener);
		}
		// do not process
		return false;
	}
	
	public List<KeyListener> getListeners(int id, int code) {
		// get key
		String key = getKey(id,code);
		// process this?
		if(process.containsKey(key))
			return process.get(key);
		else
			return null;
	}
	
	private String getKey(int id, int code) {
		return id + "." + code;
	}
	
	public boolean isPassed(Class c) {
		return passed.contains(c);
	}
	
	public boolean addPassed(Class c) {
		// add?
		if(!passed.contains(c))
			return passed.add(c);
		else
			return false;
	}
	
	public boolean removePassed(Class c) {
		// add?
		if(passed.contains(c))
			return passed.remove(c);
		else
			return false;
	}
	
	private void fireKeyPressed(KeyEvent e) {
		// get listeners
		List<KeyListener> listeners = getListeners(e.getID(),e.getKeyCode());
		// forward
		for(KeyListener it: listeners)
			it.keyPressed(e);		
	}

	private void fireKeyTyped(KeyEvent e) {
		// get listeners
		List<KeyListener> listeners = getListeners(e.getID(),e.getKeyCode());
		// forward
		for(KeyListener it: listeners)
			it.keyTyped(e);
	}
	
	private void fireKeyReleased(KeyEvent e) {
		// get listeners
		List<KeyListener> listeners = getListeners(e.getID(),e.getKeyCode());
		// forward
		for(KeyListener it: listeners)
			it.keyReleased(e);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}	
	
}
