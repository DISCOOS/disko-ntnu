package org.disco.io.event;

import java.util.EventObject;

import org.disco.io.IEntity;

public class EntityEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private IEntity entity;
	
	public EntityEvent(Object source, IEntity entity) {
		// forward
		super(source);
		// prepare
		this.entity = entity;
	}
	
	public IEntity getEntity() {
		return entity;
	}


}
