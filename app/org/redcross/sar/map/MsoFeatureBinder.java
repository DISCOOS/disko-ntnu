package org.redcross.sar.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.work.IMapWork;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.ChangeList;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;

@SuppressWarnings("unchecked")
public class MsoFeatureBinder 
	extends AbstractMapDataBinder<IMsoObjectIf, IMsoObjectIf, MsoEvent.ChangeList, IMsoFeatureLayer> {

	private static final long serialVersionUID = 1L;
	private static final Logger m_logger = Logger.getLogger(MsoFeatureBinder.class);

	private Progressor m_progressor;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public MsoFeatureBinder(Class<IMsoObjectIf> c, IDiskoMapManager manager, Progressor progressor) {
		// forward
		super(c,manager);
		// prepare
		m_progressor = progressor;
	}

	/* =============================================================================
	 * IMapDataBinder implementation
	 * ============================================================================= */

	public void onSourceChanged(SourceEvent<ChangeList> e) {
		
		// get update list
		ChangeList list = e.getData();
		
		// initialize work list
		Map<IMsoFeatureLayer, Collection<IMapData>> workList
			= new HashMap<IMsoFeatureLayer,Collection<IMapData>>(m_layers.size());

		// loop over all layers
		for(IMsoFeatureLayer it : m_layers.values()) {
			// forward
			Collection<IMapData> items = new ArrayList<IMapData>();
			Collection<IMsoFeature> features = it.processMsoUpdateEvent(list);
			// has been changed?
			if(features.size()>0) {
				// collect work items
				for(IMapData data : features) {
					items.add(data);
				}
				// add work items to work list
				workList.put(it,items);
			}
		}
		// forward
		schedule(workList);
	}	
	
	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	@Override
	protected IMapWork createWork(
			Map<IMsoFeatureLayer, Collection<IMapData>> work) {
		
		try {
			return new FeatureWork(work);
		} catch (Exception e) {
			m_logger.error("Failed to create FeatureWork instance",e);
		}
		return null;
	}

	/* ===============================================================
	 * Inner classes
	 * =============================================================== */

	private class FeatureWork extends AbstractWork implements IMapWork {

		Map<IMsoFeatureLayer, Collection<IMapData>> m_data;

		public FeatureWork(Map<IMsoFeatureLayer, Collection<IMapData>> data) throws Exception {
			// forward
			super(m_isActive?HIGH_PRIORITY:NORMAL_PRIORITY,true,false,WorkerType.UNSAFE,"",0,false,false);
			// prepare
			m_data = data;
		}

		public void merge(Map work) {

			// merge with pending work
			for(Object layer : work.keySet()) {
				// validate implementation 
				if(layer instanceof IMsoFeatureLayer) {
					// get current and updated features
					Collection<IMapData> list = m_data.get((IMsoFeatureLayer)layer);
					// not found?
					if(list==null) {
						// add new layer
						m_data.put((IMsoFeatureLayer)layer, (Collection<IMapData>)work.get(layer));
					}
					else {
						// forward
						cleanup(list);
						// get list
						Collection<IMapData> items = (Collection<IMapData>)work.get(layer);
						// add changed features
						for(Object it : items) {
							// validate implementation 
							if(it instanceof IMsoFeature) {
								// get MSO object
								IMsoObjectIf msoObj = ((IMsoFeature)it).getMsoObject();
								// only add features that has a valid MSO object
								if(msoObj!=null && !msoObj.isDeleted() && !list.contains(it)) {
									list.add((IMsoFeature)it);
								}
							}
						}
					}
				}
			}
			// loop over all remaining layers not already visited
			for(IMsoFeatureLayer layer : m_data.keySet()) {
				if(!work.containsKey(layer)) {
					cleanup(m_data.get(layer));
				}
			}
		}

		public void cleanup(Collection<IMapData> list) {
			// initialize
			List<IMsoFeature> delete = new ArrayList<IMsoFeature>();
			// detect deleted features
			for(IMapData it : list) {
				// validate implementation 
				if(it instanceof IMsoFeature) {
					// get MSO object
					IMsoObjectIf msoObj = ((IMsoFeature)it).getMsoObject();
					// add?
					if(msoObj!=null && msoObj.isDeleted()) {
						delete.add(((IMsoFeature)it));
					}
				}
			}
			// remove all deleted features
			list.removeAll(delete);
		}

		public int size() {
			// initialize
			int size = 0;
			// loop and update
			for(Collection<IMapData> list : m_data.values()) {
				size+=list.size();
			}
			return size;
		}

		@Override
		public Void doWork(IWorkLoop loop) {

			// initialize dirty layer list
			List<IMapLayer> dirtyList = new Vector<IMapLayer>(m_data.size());

			// do the work!
			for(IMsoFeatureLayer layer : m_data.keySet()) {
				try {
					// get features
					Collection<IMapData> list = m_data.get(layer);
					// load all features
					for(IMapData it : list) {
						// validate implementation 
						if(it instanceof IMsoFeature) {
							// get current MSO object
							IMsoObjectIf msoObj = ((IMsoFeature)it).getMsoObject();
							// create?
							if(msoObj!=null && !msoObj.isDeleted()) ((IMsoFeature)it).create();
						}
					}
					// update dirty state
					if(layer.isDirty(true)) {
						dirtyList.add(layer);
					}
				} catch (Exception e) {
					m_logger.error("Failed to execute feature work ",e);
				}
			}

			// notify?
			if(dirtyList.size()>0) {
				fireDataChanged(dirtyList);
			}

			// finished
			return null;

		}

		@Override
		public void showProgress() {
			if(m_showProgress && !m_isNotified && canShowProgess()) {
				m_isNotified = true;
				m_progressor.show();
			}
		}

		@Override
		public void hideProgress() {
			if(m_isNotified) {
				m_progressor.hide();
				m_isNotified = false;
			}
		}


	}

}
