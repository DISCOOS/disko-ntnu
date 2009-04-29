package org.redcross.sar.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.redcross.sar.data.AbstractBinder;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.IDataSource;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.map.event.IMapDataListener;
import org.redcross.sar.map.layer.IMapDataBinder;
import org.redcross.sar.map.layer.IMapLayer;
import org.redcross.sar.map.work.IMapWork;
import org.redcross.sar.work.IWork.WorkState;

/**
 * 
 * @author Administrator
 *
 * @param <S> - the class or interface that implements the data id object
 * @param <T> - the class or interface that implements the data object
 * @param <I> - the class or interface that implements the source event information object
 * @param <L> - the class or interface that implements the IMapLayer interface
 */

@SuppressWarnings("unchecked")
public abstract class AbstractMapDataBinder<S, T extends IData, I, L extends IMapLayer> 
	extends AbstractBinder<S,T,I>
	implements IMapDataBinder<S,T,I,L>, ISourceListener<I> {

	private static final long serialVersionUID = 1L;

	protected boolean m_isActive = false;

	protected IMapWork m_work;
	protected final IDiskoMapManager m_manager;

	protected final Map<Enum<?>,L> m_layers = new HashMap<Enum<?>,L>(10);
	protected final List<IMapWork> m_scheduled = new ArrayList<IMapWork>();
	protected final EventListenerList m_listeners = new EventListenerList();

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractMapDataBinder(Class<T> c, IDiskoMapManager manager) {
		// forward
		super(c);
		// prepare
		m_manager = manager;
	}

	/* =============================================================================
	 * IMapDataBinder implementation
	 * ============================================================================= */

	public boolean isActive() {
		return m_isActive;
	}

	public int getBufferCount() {
		return m_work!=null ? m_work.size() : 0;
	}

	public boolean activate(boolean wait) {
		return activate(true,wait);
	}

	public boolean activate(boolean showProgress, boolean wait) {
		m_isActive = true;
		return execute(showProgress, wait);
	}

	public boolean deactivate() {
		boolean bFlag = m_isActive;
		m_isActive = false;
		setShowProgress(false);
		return bFlag;
	}

	public boolean execute(boolean showProgress, boolean wait) {
		// initialize
		boolean bFlag = false;
		// execute on loop?
		if(!wait) {
			// process buffer?
			if(m_work!=null) {
				// get map work
				IMapWork work = m_work;
				// ensure atomic operation
				synchronized(m_scheduled) {
					// ass to execution loop
					m_scheduled.add(m_work);
				}
				// create new work on next change
				m_work = null;
				// forward work to work loop
				m_manager.schedule(work);
				// update executing work
				setShowProgress(showProgress);
				// was scheduled
				bFlag = true;
			}
		}
		else {
			// process buffer?
			if(m_work!=null) {
				// get map work
				IMapWork work = m_work;
				// create new work on next change
				m_work = null;
				// this will block thread until finished
				work.run();
			}
		}
		// finished
		return bFlag;
	}

	public void setShowProgress(boolean showProgress) {
		// initialize
		List<IMapWork> remove = new ArrayList<IMapWork>();
		// ensure synchronized operation
		synchronized(m_scheduled) {
			// evaluate scheduled work
			for(IMapWork it : m_scheduled) {
				if(it.isState(WorkState.FINISHED) || it.isState(WorkState.CANCELED)) {
					remove.add(it);
					it.setShowProgress(false);
				}
				else
					it.setShowProgress(showProgress);
			}
			// remove finished and canceled work
			m_scheduled.removeAll(remove);
		}
	}

	@Override
	public boolean connect(IDataSource<I> source) {
		return connect(source,true);
	}

	public boolean connect(IDataSource<I> source, boolean load) {
		return connect(source,m_layers.values(),load);
	}

	public boolean connect(IDataSource<I> source, Collection<L> layers, boolean load) {		
		// forward to super class first
		if(super.connect(source)) {
			// forward
			initialize(layers, false);
			// load items?
			if(load) load();
			// success
			return true;
		}
		// failure
		return false;
	}
	
	public boolean load(Collection<T> objects, boolean append) {
		// initialize
		L layer = null;
		Map<L, Collection<T>> batch
			= new HashMap<L, Collection<T>>(m_layers.size());
		Map<L, Collection<IMapData>> work
			= new HashMap<L, Collection<IMapData>>(m_layers.size());
		
		// loop over objects and create batch
		for(T it : objects) {
			layer = m_layers.get(it.getClassCode());
			Collection<T> items = batch.get(layer);
			if(items==null) {
				items = new ArrayList<T>();
				batch.put(layer, items);
			}
			items.add(it);
		}
		// get all work items
		for(L it : m_layers.values()) {
			// get items from MSO class code
			Collection<T> items = batch.get(it);
			// found items?
			if(items!=null && items.size()>0) {
				work.put(it, it.load(items));
			}
		}
		// forward
		return schedule(work);
	}

	public boolean clear() {
		List<L> changeList = new Vector<L>(m_layers.size());
		// loop over all layers
		for(L it : m_layers.values()) {
			// forward
			if(it.removeAll()) {
				changeList.add(it);
			}
		}
		// notify?
		if(changeList.size()>0) {
			fireDataChanged((List<IMapLayer>)changeList);
			return true;
		}
		return false;
	}

	public EnumSet<?> getInterests() {
		EnumSet set = EnumSet.noneOf(Enum.class);
		for(Enum it : m_layers.keySet()) {
			set.add(it);
		}
		return set;
	}
	
	public void addLayer(L layer) {
		// add?
		if(!m_layers.containsKey(layer.getClassCode())) {
			m_layers.put(layer.getClassCode(),layer);
			initialize(m_layers.values(), true);
		}
	}

	public void removeLayer(L layer) {
		// remove?
		if(m_layers.containsKey(layer.getClassCode())) {
			m_layers.remove(layer.getClassCode());
			initialize(m_layers.values(), true);
		}
	}

	public Collection<L> getLayers() {
		return new ArrayList<L>(m_layers.values());
	}

	public boolean setLayers(Collection<L> layers) {
		// add?
		if(!m_layers.values().containsAll(layers)) {
			initialize(layers, true);
			return true;
		}
		return false;
	}

	public void addMapDataListener(IMapDataListener listener) {
		m_listeners.add(IMapDataListener.class,listener);
	}

	public void removeMapDataListener(IMapDataListener listener) {
		m_listeners.remove(IMapDataListener.class,listener);
	}

	/* =============================================================================
	 * ISourceListener implementation
	 * ============================================================================= */
	
	public abstract void onSourceChanged(SourceEvent<I> e);
	
	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	protected void initialize(Collection<L> layers, boolean force) {
		// any change?
		if(force || !m_layers.values().containsAll(layers)) {
			// clear layers
			m_layers.clear();
			// add layers
			for(L it : layers) {
				m_layers.put(it.getClassCode(), it);
			}
			// add listener (again) to update interests
			getSource().addSourceListener(this);
		}
	}	
	@Override
	protected List<T> query() {
		// allowed?
		if(getSource()!=null && getSource().isAvailable()) {
			// initialize work list
			List<T> items = new ArrayList<T>(m_layers.size());
			// loop over all layers
			for(L it : m_layers.values()) {
				// get items from MSO class code
				items.addAll((List<T>)getSource().getItems(it.getClassCode()));
			}
			// forward
			return items;
		}
		return null;
	}
	
	protected boolean schedule(Map<L, Collection<IMapData>> work) {
		// has work?
		if(work.size()>0) {
			// schedule now?
			if(m_isActive) {
				// forward
				m_manager.schedule(createWork(work));
				// was scheduled
				return true;
			}
			else if(m_work==null){
				// initialize work
				m_work = createWork(work);
			}
			else {
				// merge with existing work
				m_work.merge(work);
			}
		}
		// was buffered
		return false;
	}
	
	protected abstract IMapWork createWork(Map<L, Collection<IMapData>> work);
	
	@SuppressWarnings("unchecked")
	protected void fireDataChanged(List<IMapLayer> layers) {
		IMapDataListener[] list = m_listeners.getListeners(IMapDataListener.class);
		for(int i=0; i<list.length; i++) {
			list[i].onDataChanged(layers);
		}
	}

}
