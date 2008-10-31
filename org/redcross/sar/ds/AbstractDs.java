package org.redcross.sar.ds;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileSystemView;

import org.redcross.sar.data.DataSourceImpl;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.DsChangeAdapter;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
import org.redcross.sar.thread.IWorkLoop;
import org.redcross.sar.thread.WorkLoop;
import org.redcross.sar.thread.WorkPool;
import org.redcross.sar.thread.IWorkLoop.LoopState;
import org.redcross.sar.util.Utils;

/**
 *
 * @author kennetgu
 *
 * @param <S> - ID object type. </p>
 *
 * ID is used to identify the data object (Should be type same as IDsObject.getId()).</p>
 *
 * @param <T> - Data object type. </p>
 *
 * The data object contains the DS information.</p>
 *
 * @param <U> - Update object type. </p>
 *
 * The update object is added to the queue of updates.
 * Every update may result in a change in DS data. </p>
 */
public abstract class AbstractDs<S extends IData, T extends IDsObject, U extends EventObject> implements IDs<T> {

	/* ============================================================
	 * Declaration of local lists
	 * ============================================================
	 *
	 * This solves the problem of concurrent modification of lists
	 * in MSO model. MSO is not thread safe, and MSO is updated
	 * from more then one thread. As long as all work i done on
	 * DiskoWorkPool, concurrency is ensured. DiskoWorkPool manages,
	 * both unsafe (locks application and shows progress) and safe
	 * work. Estimation is potentially a time consuming process and
	 * has real-time requirements. Hence, the user should not be aware
	 * of this task running. Thus, estimation must be done on a safe
	 * thread, managed by DiskoWorkPool, because this does not
	 * lock the application.
	 *
	 * Beware though, safe work may still be unsafe. It is the
	 * programmer that is in charge of implementing work that is
	 * thread safe, which is indicated to the DiskoWorkPool when
	 * scheduled. This property is exploited here.
	 *
	 * The algorithm is not thread safe according to the DiskoWorkPool
	 * rules. Only work that does not invoke methods on Swing and MSO
	 * model is thread safe by definition. This algorithm access MSO
	 * model during estimation. MSO objects may still be changed or
	 * deleted concurrently with access to these objects during
	 * estimation. These changes however, will be detected later and
	 * fixed through the Update Event Queue. It's really not a
	 * problem for either the system nor the algorithm, because MSO
	 * object are not written to, only read from, thus not really
	 * violating the system.
	 *
	 * ============================================================ */

	/**
	 * Local list of objects
	 */
	protected final Map<S,T> m_dsObjs = new HashMap<S,T>();

	/**
	 * Local list of ids
	 */
	protected final Map<T,S> m_idObjs = new HashMap<T,S>();

	/**
	 * Local list of added ds objects
	 */
	protected final Map<S,T> m_added =
		Collections.synchronizedMap(new HashMap<S,T>());

	/**
	 * Local list of archived ds objects
	 */
	protected final Map<S,T> m_archived =
		Collections.synchronizedMap(new HashMap<S,T>());

	/**
	 * Update event queue. Buffer between received update and
	 * handling thread. Only updates that is interesting will
	 * be added.
	 */
	protected final ConcurrentLinkedQueue<U> m_queue =
		new ConcurrentLinkedQueue<U>();

	/**
	 * Local list of cost that requires a full calculation
	 *
	 */
	protected final List<T> m_heavySet =  new ArrayList<T>();

	/**
	 * Update event queue. Buffer between received update and
	 * handling thread. Only updates that is interesting will
	 * be added.
	 */
	protected final List<T> m_residueSet = new ArrayList<T>();

	/**
	 * Operation id
	 */
	protected final String m_oprID;

	/**
	 * List of update listeners.
	 */
	protected final EventListenerList m_listeners = new EventListenerList();

	/**
	 * The data class
	 */
	protected final Class<T> m_dataClass;

	/**
	 * The work pool
	 */
	protected final WorkPool m_workPool;

	/**
	 * The work loop
	 */
	protected final WorkLoop m_workLoop;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractDs(Class<T> dataClass, String oprID,
			int dutyCycle, int timeOut) throws Exception {

		// prepare
		m_oprID = oprID;
		m_dataClass = dataClass;
		m_workPool = WorkPool.getInstance();
		m_workLoop = new WorkLoop(dutyCycle,timeOut);

        // connect repeater to client update events
        addChangeListener(m_dsUpdateRepeater);

	}

	/* ============================================================
	 * IDataSourceIf implementation
	 * ============================================================ */

	public Class<T> getDataClass() {
		return m_dataClass;
	}

	public boolean isSupported(Class<?> dataClass) {
		return m_dataClass.equals(dataClass);
	}

	/* ============================================================
	 * Required methods
	 * ============================================================ */

	public abstract boolean load();

	protected abstract void execute(List<T> changed, long tic, long timeOut);

	/* ============================================================
	 * IDs implementation
	 * ============================================================ */

	public String getOprID() {
		return m_oprID;
	}

	public List<T> getItems() {
		return getDsObjects();
	}


	public IWorkLoop getWorkLoop() {
		return m_workLoop;
	}

	public LoopState getLoopState() {
		return m_workLoop.getState();
	}

	public boolean isLoopState(LoopState state) {
		return m_workLoop.isState(state);
	}

	public boolean start() {

		// allowed?
		if(m_workLoop.getID()==0) {
			// add work loop to work pool
			if((m_workPool.add(m_workLoop)>0)) {
				fireExecuteEvent(new DsEvent.Execute(this,0));
				return true;
			}
		}
		return false;
	}

	public boolean resume() {
		if(m_workLoop.resume()) {
			fireExecuteEvent(new DsEvent.Execute(this,1));
			return true;
		}
		return false;
	}

	public boolean suspend() {
		if(m_workLoop.suspend()) {
			fireExecuteEvent(new DsEvent.Execute(this,2));
			return true;
		}
		return false;
	}

	public boolean stop() {
		// allowed?
		if(m_workLoop.getID()>0) {
			// remove work loop from work pool
			if(m_workPool.remove(m_workLoop.getID())) {
				fireExecuteEvent(new DsEvent.Execute(this,3));
				return true;
			}
		}
		return false;
	}

	/* ============================================================
	 * Public methods
	 * ============================================================ */

	public synchronized boolean importSamples(String file) {
		if(isFileImportable(file)) {
			File f = new File(file);
			if(f.exists()) {
				// initialize
				String id = "";
				int index = -1;
				T dsObj = null;
				Object[][] samples = null;
				// get data
				String matrix[][] = Utils.importText(file, "\t");
				// get upper bound
				int iCount = matrix.length;
				// loop over all objects
				for(int i=0;i<iCount;i++) {
					// get samples
					String data[] = matrix[i];
					// get next object?
					if(id!=data[i]) {
						// get object
						dsObj = getDsObject(data[i]);
						// does the object not exist?
						if(dsObj==null) continue;
						// update id
						id = data[i];
						// allocate memory
						samples = new Object[dsObj.getAttrCount()][data.length-2];
					}
					// get upper sample bound
					int jCount = data.length;
					// get attribute
					index = Integer.valueOf(data[1]);
					// get attribute class
					Class<?> c = dsObj.getAttrClass(index);
					// get over samples
					for(int j=2;i<jCount;j++) {
						samples[index][j] = Utils.valueOf(data[j],c);
					}

				}
			}
		}
		return false;
	}

	public synchronized String exportSamples(String path) {
		String stamp = Utils.toString(Calendar.getInstance());
		stamp = stamp.replace(":", "").replace(".","").replace("+", "").replace(" ", "-");
		exportSamplesToFile(path + getOprID() + "-" +  stamp + ".dss");
		return stamp;
	}

	public synchronized void exportSamplesToFile(String file) {
		// get matrix row count
		int row = 0;
		int size = 0;
		// loop over all objects
		for(IDsObject it : m_dsObjs.values()) {
			// get attribute count
			size += it.getAttrCount();
		}
		// has data?
		if(size>0) {
			// allocate memory
			String[][] matrix = new String[size][];
			// loop over all objects
			for(IDsObject it : m_dsObjs.values()) {
				// initialize
				String id = toString(it);
				// get attribute count
				int iCount = it.getAttrCount();
				// loop over all attributes
				for(int i=0;i<iCount;i++) {
					// get sample count
					int jCount = it.getSampleCount();
					// allocate memory
					String[] samples = new String[jCount+2];
					// set id and attribute index
					samples[0] = id;
					samples[1] = String.valueOf(i);
					// loop over all samples
					for(int j=0;j<jCount;j++) {
						samples[j+2] = Utils.toString(it.getAttrValue(i,j));
					}
					// update matrix
					matrix[row] = samples;
					// increment row
					row++;
				}
			}
			Utils.exportText(matrix, file, "\t");
		}
	}

	public List<String> getImportFilesInCatalog(String path) {

		// initialize
		List<String> list = new ArrayList<String>();

		// load available mxd document file paths
		File f = FileSystemView.getFileSystemView().createFileObject(path);
		File[] files = FileSystemView.getFileSystemView().getFiles(f, true);
		for (int i=0; i < files.length; i++){
			String filename = files[i].getName();
			if(isFileImportable(files[i].getName())) {
				list.add(path + "\\" + filename);
			}
		}

		// finished
		return list;

	}

	public boolean isFileImportable(String file) {
		if (file.contains(".")){
			if (file.substring(file.lastIndexOf(".")).equalsIgnoreCase(".dss")){
				String[] split = file.split(" ");
				// only load samples from current operation
				if(split.length>0 && split[0]==getOprID()) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void clear() {
		// notify
		fireRemoved(new ArrayList<T>(m_dsObjs.values()));
		// cleanup
		m_dsObjs.clear();
		m_idObjs.clear();
		m_archived.clear();
		m_heavySet.clear();
		m_residueSet.clear();
		m_queue.clear();
	}

	public void addChangeListener(IDsChangeListener listener) {
		m_listeners.add(IDsChangeListener.class,listener);
	}

	public void removeChangeListener(IDsChangeListener listener) {
		m_listeners.remove(IDsChangeListener.class,listener);
	}

	/* ===========================================
	 * IDataSourceIf implementation
	 * =========================================== */

	public Collection<?> getItems(Class<?> c) {
		return m_source.getItems(c);
	}

	public void addSourceListener(ISourceListener<DsEvent.Update> listener) {
		m_source.addSourceListener(listener);
	}

	public void removeSourceListener(ISourceListener<DsEvent.Update> listener) {
		m_source.removeSourceListener(listener);
	}

	/* ============================================================
	 * Protected methods
	 * ============================================================ */

	protected abstract void schedule(Map<S,Object[]> changes);

	protected T getDsObject(Object id) {
		if(id instanceof String) {
			String sId = (String)id;
			// search for object
			for(T it : m_dsObjs.values()) {
				if(sId.equals(toString(it)))
					return it;
			}
		}
		return m_dsObjs.get(id);
	}

	protected List<T> getDsObjects() {
		return new ArrayList<T>(m_dsObjs.values());
	}

	protected Map<S,T> getDsMap() {
		return m_dsObjs;
	}

	protected void addToResidue(T object) {
		if(object!=null && !m_residueSet.contains(object)) {
			m_residueSet.add(object);
		}
	}

	protected void addToResidue(List<T> list) {
		m_residueSet.addAll(list);
	}

	protected void setHeavySet(List<T> list) {
		m_heavySet.clear();
		m_heavySet.addAll(list);
	}

	protected U getExisting(U e) {
		Object s = e.getSource();
		for(U it : m_queue) {
			if(it.getSource().equals(s)) {
				return it;
			}
		}
		return null;
	}

	protected void fireAdded() {
		// notify?
		if(m_added.size()>0) {
			// create array
			IDsObject[] data = new IDsObject[m_added.size()];
			m_added.values().toArray(data);
			// forward
			fireUpdateEvent(new DsEvent.Update(this,DsEventType.ADDED_EVENT,0,data));
			// clear items
			m_added.clear();
		}
	}

	protected void fireModified(T object) {
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.MODIFIED_EVENT,0,new IDsObject[]{object}));
	}

	protected void fireModified(Collection<T> objects, int flags) {
		IDsObject[] data = new IDsObject[objects.size()];
		objects.toArray(data);
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.MODIFIED_EVENT,flags,data));
	}

	protected void fireArchived() {
		// notify?
		if(m_archived.size()>0) {
			// forward
			fireModified(new ArrayList<T>(m_archived.values()), 0);
			// clear items
			m_archived.clear();
		}
	}

	protected void fireRemoved(T object) {
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.REMOVED_EVENT,0,new IDsObject[]{object}));
	}

	protected void fireRemoved(Collection<T> objects) {
		IDsObject[] data = new IDsObject[objects.size()];
		objects.toArray(data);
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.REMOVED_EVENT,0,data));
	}

	protected void fireUpdateEvent(DsEvent.Update e) {
		if(e.getData().length>0) {
			IDsChangeListener[] list = m_listeners.getListeners(IDsChangeListener.class);
	 		for(int i=0;i<list.length; i++) {
				list[i].handleUpdateEvent(e);
			}
		}
	}

	/* ============================================================
	 * Helper methods
	 * ============================================================ */

	private String toString(IDsObject obj) {
		return obj.getId().toString();
	}

	private void fireExecuteEvent(DsEvent.Execute e) {
		IDsChangeListener[] list = m_listeners.getListeners(IDsChangeListener.class);
 		for(int i=0;i<list.length; i++) {
			list[i].handleExecuteEvent(e);
		}
	}


	/* =========================================================================
	 * Anonymous classes
	 * ========================================================================= */

	/**
	 * IDataSourceIf implementation
	 */
	protected final DataSourceImpl<DsEvent.Update> m_source = new DataSourceImpl<DsEvent.Update>() {

		@SuppressWarnings("unchecked")
		public Collection<?> getItems(Class<?> c) {
			// only one type to return
			return AbstractDs.this.getItems();
		}

	};

	/**
	 * IDsUpdateListener repeater implementation
	 */
	protected final IDsChangeListener m_dsUpdateRepeater = new DsChangeAdapter() {

		@Override
		@SuppressWarnings("unchecked")
		public void handleUpdateEvent(DsEvent.Update e) {
			// forward
			m_source.fireSourceChanged(new SourceEvent<DsEvent.Update>(AbstractDs.this,e));
		}

    };


}
