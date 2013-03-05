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

import javax.swing.filechooser.FileSystemView;

import org.redcross.sar.AbstractService;
import org.redcross.sar.data.AbstractDataSource;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.event.ISourceListener;
import org.redcross.sar.data.event.SourceEvent;
import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.DsChangeAdapter;
import org.redcross.sar.ds.event.IDsChangeListener;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
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
public abstract class AbstractDs<S extends IData, T extends IDsObject, U extends EventObject>
			extends AbstractService implements IDs<T> {
	
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
	 * The data class
	 */
	protected final Class<T> m_dataClass;

	/* ============================================================
	 * Constructors
	 * ============================================================ */

	public AbstractDs(Class<T> dataClass, String oprID,
			long requestedDutyCycle, double requestedUtilization) throws Exception {

		// forward
		super(oprID, requestedDutyCycle, requestedUtilization);

		// prepare
		m_dataClass = dataClass;

        // connect repeater to client update events
        addChangeListener(m_dsUpdateRepeater);

	}

	/* ============================================================
	 * Required methods
	 * ============================================================ */

	public abstract boolean load();

	protected abstract void execute(List<T> changed, long tic, long timeOut);

	/* ============================================================
	 * IDs implementation
	 * ============================================================ */

	public List<T> getItems() {
		return getDsObjects();
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
		exportSamplesToFile(path + getID() + "-" +  stamp + ".dss");
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
				if(split.length>0 && split[0]==getID()) {
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

    public boolean isAvailable() {
    	return m_source.isAvailable();
    }
	
	public Class<T> getDataClass() {
		return m_dataClass;
	}

	public boolean isSupported(Class<?> dataClass) {
		return m_dataClass.equals(dataClass);
	}

	public Object getID() {
		return m_source.getID();
	}
	
	public Collection<?> getItems(Class<?> c) {
		return m_source.getItems(c);
	}
	
    public Collection<?> getItems(Enum<?> e) {
    	return m_source.getItems(e);
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

	protected T add(S id, T data) {
		// add to costs and assignments
		m_dsObjs.put(id, data);
		m_idObjs.put(data,id);
		// push to added?
		if(!data.isArchived()) {
			m_added.put(id, data);
			m_archived.remove(id);
		}
		// finished
		return data;
	}

	protected T remove(S id) {
		// try to remove object
		T data = m_dsObjs.remove(id);
		// remove from all?
		if(data!=null && !data.isArchived()) {
			m_idObjs.remove(data);
			m_heavySet.remove(data);
			m_residueSet.remove(data);
			m_added.remove(id);
			m_archived.remove(id);
		}
		// finished
		return data;
	}


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
		for(T it : list) {
			if(!m_residueSet.contains(it))
				m_residueSet.add(it);
		}
	}

	protected void setHeavySet(List<T> list) {
		m_heavySet.clear();
		for(T it : list) {
			if(!m_heavySet.contains(it))
				m_heavySet.add(it);
		}
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

	protected boolean include(IDsObject dsObj, List<?> list) {
		return !(list.contains(dsObj) || dsObj.isArchived() || dsObj.isSuspended());
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

	/* =========================================================================
	 * Anonymous classes
	 * ========================================================================= */

	/**
	 * IDataSourceIf implementation
	 */
	protected final AbstractDataSource<DsEvent.Update> m_source = new AbstractDataSource<DsEvent.Update>() {

		@Override
		public Object getID() {
			// TODO Auto-generated method stub
			return m_id;
		}
		
	    public boolean isAvailable() {
	    	return true;
	    }

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
