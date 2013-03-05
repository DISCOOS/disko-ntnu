package org.redcross.sar.map.event;

import java.util.Collection;
import java.util.EventObject;

import org.redcross.sar.map.layer.IMsoFeatureLayer;

public class MsoDataEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private Collection<IMsoFeatureLayer> m_layers;
	
	public MsoDataEvent(Object source, Collection<IMsoFeatureLayer> layers) {
		// forward
		super(source);
		// prepare
		m_layers = layers;
	}
	
	public Collection<IMsoFeatureLayer> getLayers() {
		return m_layers;
	}

}
