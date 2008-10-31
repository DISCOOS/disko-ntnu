package org.redcross.sar.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.redcross.sar.map.event.IMapDataListener;
import org.redcross.sar.map.feature.IMsoFeature;
import org.redcross.sar.map.layer.IMsoFeatureLayer;
import org.redcross.sar.map.work.IMapWork;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.data.ICmdPostIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.UpdateList;
import org.redcross.sar.thread.AbstractWork;
import org.redcross.sar.thread.IWork.WorkState;

import com.esri.arcgis.interop.AutomationException;

public class MsoDataBinder implements IMsoUpdateListenerIf {

	private static final long serialVersionUID = 1L;

	private boolean m_isActive = false;

	private FeatureWork m_work;
	private IMsoModelIf m_model;
	private IDiskoMapManager m_manager;
	private EnumSet<MsoClassCode> m_interests = EnumSet.noneOf(MsoClassCode.class);

	private final List<FeatureWork> m_exec = new ArrayList<FeatureWork>();
	private final EventListenerList m_listeners = new EventListenerList();
	private final Collection<IMsoFeatureLayer> m_layers = new Vector<IMsoFeatureLayer>(10);

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public MsoDataBinder(IDiskoMapManager manager) {
		// forward
		super();
		// prepare
		m_manager = manager;
	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	public boolean isActive() {
		return m_isActive;
	}

	public int getBufferCount() {
		return m_work!=null ? m_work.size() : 0;
	}

	public boolean activate() {
		return activate(true);
	}

	public boolean activate(boolean showProgress) {
		m_isActive = true;
		return execute(showProgress);
	}

	public boolean deactivate() {
		boolean bFlag = m_isActive;
		m_isActive = false;
		setShowProgress(false);
		return bFlag;
	}


	public boolean execute(boolean showProgress) {
		// update executing work
		setShowProgress(showProgress);
		// process buffer?
		if(m_work!=null) {
			// get map work
			IMapWork work = m_work;
			// create new work on next change
			m_work = null;
			// forward work to work loop
			m_manager.schedule(work);
			// work i schedules
			return true;
		}
		// work is not scheduled
		return false;
	}

	public void setShowProgress(boolean showProgress) {
		List<FeatureWork> remove = new ArrayList<FeatureWork>();
		for(FeatureWork it : m_exec) {
			if(it.isState(WorkState.FINISHED) || it.isState(WorkState.CANCELED)) {
				remove.add(it);
				it.setShowProgress(false);
			}
			else
				it.setShowProgress(showProgress);
		}
		m_exec.removeAll(remove);
	}

	public boolean connect(IMsoModelIf model, boolean load) {
		return connect(model,m_layers,load);
	}

	public boolean connect(IMsoModelIf model, Collection<IMsoFeatureLayer> layers, boolean load) {
		// any change?
		if(m_model!=model) {
			// forward
			disconnect();
			// connect?
			if(model!=null) {
				// prepare
				m_model = model;
				// forward
				initialize(layers, false);
				// load items?
				if(load) load();
				// success
				return true;
			}
		}
		// failure
		return false;
	}

	public boolean disconnect() {
		// clear all
		removeAll();
		// connect?
		if(m_model!=null) {
			// add listener
			m_model.getEventManager().removeClientUpdateListener(this);
			// initialize
			m_model = null;
			// success
			return true;
		}
		// failure
		return false;
	}

	public boolean load() {
		// allowed?
		if(m_model!=null && m_model.getMsoManager().operationExists()) {
			// initialize work list
			Map<IMsoFeatureLayer, Collection<IMsoFeature>> workList
				= new HashMap<IMsoFeatureLayer,Collection<IMsoFeature>>(m_layers.size());
			// get command post
			ICmdPostIf cmdPost = m_model.getMsoManager().getCmdPost();
			// initialize vector
			Collection<IMsoObjectIf> items = new ArrayList<IMsoObjectIf>();
			// loop over all layers
			for(IMsoFeatureLayer it : m_layers) {
				// get items from MSO class code
				Map<String, IMsoListIf<IMsoObjectIf>> map = cmdPost.getReferenceLists(it.getClassCode());
				// add to vector
				for(IMsoListIf<IMsoObjectIf> list : map.values()) {
					items.addAll(list.getItems());
				}
				// found items?
				if(items.size()>0) {
					workList.put(it, it.load(items));
				}
			}
			// forward
			return schedule(workList);
		}
		return false;
	}

	public void removeAll() {
		Collection<IMsoFeatureLayer> changeList = new Vector<IMsoFeatureLayer>(m_layers.size());
		// loop over all layers
		for(IMsoFeatureLayer it : m_layers) {
			// forward
			if(it.removeAll()) {
				changeList.add(it);
			}
		}
		// notify?
		if(changeList.size()>0) {
			fireDataChanged(changeList);
		}
	}

	public void addLayer(IMsoFeatureLayer layer) {
		// add?
		if(!m_layers.contains(layer)) {
			m_layers.add(layer);
			initialize(m_layers, true);
		}
	}

	public boolean setLayers(Collection<IMsoFeatureLayer> layers) {
		// add?
		if(!m_layers.containsAll(layers)) {
			initialize(layers, true);
			return true;
		}
		return false;
	}

	public Collection<IMsoFeatureLayer> getLayers() {
		return new ArrayList<IMsoFeatureLayer>(m_layers);
	}

	public void removeLayer(IMsoFeatureLayer layer) {
		// remove?
		if(m_layers.contains(layer)) {
			m_layers.remove(layer);
			initialize(m_layers, true);
		}
	}

	public void addMapDataListener(IMapDataListener listener) {
		m_listeners.add(IMapDataListener.class,listener);
	}

	public void removeMapDataListener(IMapDataListener listener) {
		m_listeners.remove(IMapDataListener.class,listener);
	}

	/* =============================================================================
	 * IMsoUpdateListenerIf implementation
	 * ============================================================================= */

	public EnumSet<MsoClassCode> getInterests() {
		return m_interests;
	}

	public void handleMsoUpdateEvent(UpdateList e) {

		// initialize work list
		Map<IMsoFeatureLayer, Collection<IMsoFeature>> workList
			= new HashMap<IMsoFeatureLayer,Collection<IMsoFeature>>(m_layers.size());

		// loop over all layers
		for(IMsoFeatureLayer it : m_layers) {
			// forward
			Collection<IMsoFeature> features = it.processMsoUpdateEvent(e);
			// has been changed?
			if(features.size()>0) {
				workList.put(it,features);
			}
		}

		// forward
		schedule(workList);

	}

	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	protected boolean schedule(Map<IMsoFeatureLayer, Collection<IMsoFeature>> workList) {
		// has work?
		if(workList.size()>0) {
			// schedule now?
			if(m_isActive) {
				// forward
				m_manager.schedule(createWork(workList));
				// was scheduled
				return true;
			}
			else if(m_work==null){
				// initialize work
				m_work = createWork(workList);
				// ass to execution loop
				m_exec.add(m_work);
			}
			else {
				// merge with existing work
				m_work.merge(workList);
			}
		}
		// was buffered
		return false;
	}

	protected void fireDataChanged(Collection<IMsoFeatureLayer> layers) {
		IMapDataListener[] list = m_listeners.getListeners(IMapDataListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(layers);
		}
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private void initialize(Collection<IMsoFeatureLayer> layers, boolean force) {
		// any change?
		if(force || !m_layers.containsAll(layers)) {
			// clear interests
			m_interests = EnumSet.noneOf(MsoClassCode.class);
			// get
			for(IMsoFeatureLayer it : layers) {
				m_interests.add(it.getClassCode());
			}
			// add listener (again) to update interests
			m_model.getEventManager().addClientUpdateListener(this);
			// prepare
			m_layers.clear();
			m_layers.addAll(layers);
		}
	}



	private FeatureWork createWork(Map<IMsoFeatureLayer, Collection<IMsoFeature>> workList) {
		try {
			return new FeatureWork(workList);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/* ===============================================================
	 * Inner classes
	 * =============================================================== */

	private class FeatureWork extends AbstractWork implements IMapWork {

		Map<IMsoFeatureLayer,Collection<IMsoFeature>> m_data;

		public FeatureWork(Map<IMsoFeatureLayer,Collection<IMsoFeature>> data) throws Exception {
			// forward
			super(true, false, ThreadType.WORK_ON_LOOP, "Bearbeider", 500, true, false);
			// prepare
			m_data = data;
		}

		public void merge(Map<IMsoFeatureLayer, Collection<IMsoFeature>> workList) {

			// merge with pending work
			for(IMsoFeatureLayer layer : workList.keySet()) {
				// get current and updated features
				Collection<IMsoFeature> list = m_data.get(layer);
				// not found?
				if(list==null) {
					// add new layer
					m_data.put(layer, workList.get(layer));
				}
				else {
					// forward
					cleanup(list);
					// add changed features
					for(IMsoFeature it : workList.get(layer)) {
						// get MSO object
						IMsoObjectIf msoObj = it.getMsoObject();
						// only add features that has a valid MSO object
						if(msoObj!=null && !msoObj.hasBeenDeleted() && !list.contains(it)) {
							list.add(it);
						}
					}
				}
			}
			// loop over all remaining layers not already visited
			for(IMsoFeatureLayer layer : m_data.keySet()) {
				if(!workList.containsKey(layer)) {
					cleanup(m_data.get(layer));
				}
			}
		}

		public void cleanup(Collection<IMsoFeature> list) {
			// initialize
			Collection<IMsoFeature> delete = new ArrayList<IMsoFeature>();
			// detect deleted features
			for(IMsoFeature it : list) {
				// get MSO object
				IMsoObjectIf msoObj = it.getMsoObject();
				// add?
				if(msoObj!=null && msoObj.hasBeenDeleted()) {
					delete.add(it);
				}
			}
			// remove all deleted features
			list.removeAll(delete);
		}

		public int size() {
			// initialize
			int size = 0;
			// loop and update
			for(Collection<IMsoFeature> list : m_data.values()) {
				size+=list.size();
			}
			return size;
		}

		@Override
		public Void doWork() {

			// initialize dirty layer list
			Collection<IMsoFeatureLayer> dirtyList = new Vector<IMsoFeatureLayer>(m_data.size());

			// do the work!
			for(IMsoFeatureLayer layer : m_data.keySet()) {
				try {
					// get features
					Collection<IMsoFeature> list = m_data.get(layer);
					// load all features
					for(IMsoFeature it : list) {
						// get current MSO object
						IMsoObjectIf msoObj = it.getMsoObject();
						// create?
						if(msoObj!=null && !msoObj.hasBeenDeleted()) it.create();
					}
					// update dirty state
					if(layer.isDirty(true)) {
						dirtyList.add(layer);
					}
				} catch (AutomationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// notify?
			if(dirtyList.size()>0) {
				fireDataChanged(dirtyList);
			}

			// finished
			return null;

		}

	}

}
