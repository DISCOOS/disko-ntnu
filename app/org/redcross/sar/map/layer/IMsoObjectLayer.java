package org.redcross.sar.map.layer;

import java.util.List;

import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoChangeListenerIf;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;

public interface IMsoObjectLayer<I,E> extends IElementLayer<I,E,IMsoObjectIf,IMsoObjectIf>, IMsoChangeListenerIf {

	public enum LayerCode {
		UNIT_LAYER
    }
	
	public LayerCode getLayerCode();
	public MsoClassCode getClassCode();

	public List<IMapElement<E, IMsoObjectIf, IMsoObjectIf>> processMsoUpdateEvent(ChangeList events);

}
