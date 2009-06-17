package org.redcross.sar.map.layer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redcross.sar.data.Selector;
import org.redcross.sar.gui.factory.DiskoEnumFactory;
import org.redcross.sar.map.element.IMapElement;
import org.redcross.sar.map.element.IMsoObjectElement;
import org.redcross.sar.map.event.MapLayerEventStack;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;

public abstract class AbstractMsoObjectLayer<I,E> 
	extends AbstractMapLayer<I,E,IMsoObjectIf,IMsoObjectIf> 
	implements IMsoObjectLayer<I,E> {
	
	protected EnumSet<MsoClassCode> interests;

	protected final Logger logger = Logger.getLogger(AbstractMsoObjectLayer.class);
	
	protected final Map<Integer,Selector<IMsoObjectIf>> selectors = new HashMap<Integer,Selector<IMsoObjectIf>>();

	/* ===============================================================
	 * Constructors
	 * =============================================================== */

	public AbstractMsoObjectLayer(
			int shapeType,
			MsoClassCode classCode,
			EnumSet<MsoClassCode> coClasses,
			LayerCode layerCode,
			MapLayerEventStack eventStack) 
	throws UnknownHostException, IOException {

		// forward
		super(DiskoEnumFactory.getText(layerCode), classCode, layerCode, eventStack);
		
		// event handling
		interests = EnumSet.of(classCode);
		interests.addAll(coClasses);

	}

	/* ===============================================================
	 * IMsoFeatureLayer implementation
	 * =============================================================== */

	@Override
	public LayerCode getLayerCode() {
		return (LayerCode)super.getLayerCode();
	}	
	
	/**
	 * Use this method to get geodata objects. </p>
	 *
	 * The default behavior is to return a list containing the passed
	 * MSO object. If other behavior is required, override this method.
	 */
	@SuppressWarnings("unchecked")
	public List<IMsoObjectIf> getGeodataObjects(IMsoObjectIf msoObject) {
		List<IMsoObjectIf> objects = new ArrayList<IMsoObjectIf>(1);
		if(msoObject!=null && msoObject.getClassCode().equals(classCode)) {
			objects.add((IMsoObjectIf)msoObject);
		}
		return objects;
	}

	public MsoClassCode getClassCode() {
		return (MsoClassCode)classCode;
	}
	
	

	/* ===============================================================
	 * IMsoUpdateListenerIf implementation
	 * =============================================================== */

	@Override
	public IMsoObjectElement<E> getElement(int index) {
		return (IMsoObjectElement<E>)super.getElement(index);
	}

	@Override
	public IMsoObjectElement<E> getElement(Object id) {
		return (IMsoObjectElement<E>)super.getElement(id);
	}

	@Override
	public IMsoObjectElement<E> getElement(IMsoObjectIf dataObj) {
		return (IMsoObjectElement<E>)super.getElement(dataObj);
	}

	public EnumSet<MsoClassCode> getInterests() {
		return interests;
	}

	public void handleMsoChangeEvent(ChangeList events) {
		processMsoUpdateEvent(events);
	}

	/**
	 * Processes MSO update events. Long operations are returned as work list.
	 * Add and change operations are long operations, remove operations are fast.
	 * Hence, add and change operations are added to the work list, remove operations
	 * are executed directly. The isDirty flag is only set for operations executed
	 * in this procedure. This disables layer redraws until the work is actually done.
	 * If only add and change operations are required, isDirty is not changed to true.
	 * If any remove operations are executed directly, the isDirty bit is set to true.
	 */
	@SuppressWarnings("unchecked")
	public List<IMapElement<E,IMsoObjectIf,IMsoObjectIf>> processMsoUpdateEvent(ChangeList events) {

		// initialize work list
		List<IMapElement<E,IMsoObjectIf,IMsoObjectIf>> workList = new ArrayList<IMapElement<E,IMsoObjectIf,IMsoObjectIf>>();

        // clear all?
        if(events.isClearAllEvent()) {
        	clearDataObjects();
        } else {

			// loop over all events
			for(MsoEvent.Change e : events.getEvents(interests)) {

				// consume loopback updates
				if(!e.isLoopbackMode()) {

					// get event flags
					int mask = e.getMask();

			        // get mso object and element class
			        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();

		        	// get flags
					boolean createdObject  = (mask & MsoEventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
					boolean deletedObject  = (mask & MsoEventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
					boolean modifiedObject = (mask & MsoEventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
					boolean addedReference = (mask & MsoEventType.ADDED_RELATION_EVENT.maskValue()) != 0;
					boolean removedReference = (mask & MsoEventType.REMOVED_RELATION_EVENT.maskValue()) != 0;

					// get list of
					List<IMsoObjectIf> msoObjs = getGeodataObjects(msoObj);

					// loop over all object
					for(IMsoObjectIf it: msoObjs) {

						// get element
						IMsoObjectElement<E> element = getElement(it);

						// get other flags
						boolean isElement = it.getClassCode().equals(classCode);

						// add object?
						if (createdObject && element == null && isElement) {
							// create element
							element = (IMsoObjectElement<E>)addDataObject(it);
							// add element to work list?
							if(element!=null) {
								// add load work?
								workList.add(element);
							}
						}
						// is object modified?
						if ( (addedReference || removedReference || modifiedObject)
								&& element != null && element.isChanged()) {
							// add load work?
							if(!workList.contains(element)) {
								workList.add(element);
							}
						}
						// delete object?
						if ((deletedObject) && element != null && isElement) {
							// remove object from layer
							removeDataObject(it);
						}
					}
				}
			}
		}

		// finished
		return workList;
		
	}

	/* ===============================================================
	 * Protected methods
	 * =============================================================== */
	
	

}