package org.redcross.sar.ds;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.filechooser.FileSystemView;

import org.redcross.sar.ds.event.DsEvent;
import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.ds.event.DsEvent.DsEventType;
import org.redcross.sar.modeldriver.IModelDriverIf;
import org.redcross.sar.mso.ICommitManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.committer.IUpdateHolderIf;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.thread.IDiskoWork;
import org.redcross.sar.util.Utils;
import org.redcross.sar.util.except.CommitException;

public abstract class AbstractMsoDs<M extends IMsoObjectIf, T extends IDsObjectIf> 	extends AbstractDiskoWork<Boolean> 
																			implements IDsIf<T>, IMsoUpdateListenerIf {
	
	/**
	 * Try to invoke every Duty Cycle Time
	 */
	protected final long m_dutyCycleTime; 
	
	/**
	 * Listen for Updates of assignments, routes and units
	 */
	protected final EnumSet<MsoClassCode> m_msoInterests;
	
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
	protected final Map<M,T> m_dsObjs = new HashMap<M,T>();
	
	/**
	 * Local list of ids
	 */
	protected final Map<T,M> m_idObjs = new HashMap<T,M>();
	
	/**
	 * Local list of added ds objects
	 */
	protected final Map<M,T> m_added = 
		Collections.synchronizedMap(new HashMap<M,T>());
	
	/**
	 * Local list of archived ds objects
	 */
	protected final Map<M,T> m_archived = 
		Collections.synchronizedMap(new HashMap<M,T>());
	
	/**
	 * Update event queue. Buffer between received update and
	 * handling thread. Only updates that is interesting will
	 * be added.
	 */
	protected final ConcurrentLinkedQueue<Update> m_queue = 
		new ConcurrentLinkedQueue<Update>();
	
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
	 * Estimator id
	 */
	protected final String m_oprID;
	
	/**
	 * Model driver
	 */
	protected final IModelDriverIf m_driver;
	
	/**
	 * MSO model 
	 */
	protected final IMsoModelIf m_model;
	
	/**
	 * Commit manager
	 */
	protected final ICommitManagerIf m_comitter;
	
	/**
	 * Work pool hook
	 */
	protected DiskoWorkPool m_workPool = null;
	
	/**
	 * List of update listeners.
	 */
	protected final List<IDsUpdateListenerIf> m_listeners = 
		new ArrayList<IDsUpdateListenerIf>();	
	
	/**
	 * List of attribute to update 
	 */
	protected final List<String> m_attributes;
	
	/* ============================================================
	 * Constructors
	 * ============================================================ */
	
	public AbstractMsoDs(String oprID, EnumSet<MsoClassCode> msoInterests, int dutyCycleTime, List<String> attributes) throws Exception {
		
		// forward
		super(true,false,WorkOnThreadType.WORK_ON_NEW,"Estimerer",0,false,false,true,dutyCycleTime);
		
		// prepare
		m_oprID = oprID;
		m_msoInterests = msoInterests;
		m_attributes = attributes;
		m_dutyCycleTime = dutyCycleTime;
		m_workPool = DiskoWorkPool.getInstance();
		m_model = MsoModelImpl.getInstance();
		m_comitter = (ICommitManagerIf)m_model;
		m_driver = m_model.getModelDriver();
		
		// add listener
		MsoModelImpl.getInstance().getEventManager().addClientUpdateListener(this);
		
	}	
	
	/* ============================================================
	 * Required methods
	 * ============================================================ */
	
	public abstract boolean load();
	
	protected abstract T msoObjectCreated(IMsoObjectIf msoObj, Update e);
	
	protected abstract T msoObjectChanged(IMsoObjectIf msoObj, Update e);

	protected abstract T msoObjectDeleted(IMsoObjectIf msoObj, Update e);
	
	protected abstract void execute(List<T> changed, long tic);
	
	/* ============================================================
	 * Public methods
	 * ============================================================ */
	
	public String getOprID() {
		return m_oprID;
	}
	
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
		String name = getOprID() + "-" + stamp.replace(":", "").replace(".","").replace("+", "").replace(" ", "-");
		exportSamplesToFile(path + "\\" + name + ".dss");
		return name;
	}
	
	public synchronized void exportSamplesToFile(String file) {
		// get matrix row count
		int row = 0;
		int size = 0;
		// loop over all objects
		for(IDsObjectIf it : m_dsObjs.values()) {
			// get attribute count
			size += it.getAttrCount();
		}		
		// has data?
		if(size>0) {
			// allocate memory
			String[][] matrix = new String[size][];
			// loop over all objects
			for(IDsObjectIf it : m_dsObjs.values()) {
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
	
	public boolean addUpdateListener(IDsUpdateListenerIf listener) {
		if(!m_listeners.contains(listener))
			return m_listeners.add(listener);
		return false;
	}

	public boolean removeUpdateListener(IDsUpdateListenerIf listener) {
		if(m_listeners.contains(listener))
			return m_listeners.remove(listener);
		return false;
	}
	
	/* ===========================================
	 * IMsoUpdateListenerIf implementation
	 * ===========================================
	 */

	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) {
		// consume?
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)
				|| !m_oprID.equals(m_driver.getActiveOperationID())
				|| m_dsObjs.isEmpty()) return false;		
		// check against interests
		return m_msoInterests.contains(aMsoObject.getMsoClassCode());
	}	

	public void handleMsoUpdateEvent(Update e) {
			
		// not a clear all event?
		if(!e.isClearAllEvent()) {
		
			// get flags
	        boolean deletedObject  = e.isDeleteObjectEvent();
	        boolean modifiedObject = e.isModifyObjectEvent();
	        boolean modifiedReference = e.isChangeReferenceEvent();
	        boolean resume = false;
	        
			// is object modified?
			if (deletedObject || modifiedObject || modifiedReference) {
				// forward
				Update existing = getExisting(e);
				// add to queue?
				if(existing==null)
					resume = m_queue.add(e);
				else {
					// make union
					resume = existing.union(e);
				}
			}
			
			// resume work? 
			if(resume && isSuspended()) {
				// this ensures faster service if work is suspended
				m_workPool.resume(this);
			}
			
		}
		
	}	

	/* ============================================================
	 * Protected methods
	 * ============================================================ */
	
	protected T getDsObject(Object id) {
		if(id instanceof IMsoObjectIf)
			return getDsObject((IMsoObjectIf)id);
		if(id!=null) {
			String sId = id.toString();
			// search for object
			for(T it : m_dsObjs.values()) {
				if(sId == toString(it))
					return it;
			}
		}
		return null;
	}
	
	protected T getDsObject(M msoObj) {
		return m_dsObjs.get(msoObj);
	}
	
	protected List<T> getDsObjects() {
		return new ArrayList<T>(m_dsObjs.values());
	}
	
	protected Map<M,T> getDsMap() {
		return m_dsObjs;
	}
	
	protected void addToResidue(T object) {
		if(object!=null && !m_residueSet.contains(object)) {
			m_residueSet.add(object);
		}
	}
	
	protected void setWorkResidue(List<T> list) {
		m_residueSet.clear();
		m_residueSet.addAll(list);
	}
	
	protected void setHeavySet(List<T> list) {
		m_heavySet.clear();
		m_heavySet.addAll(list);
	}

	protected void fireAdded() {
		// notify?
		if(m_added.size()>0) {
			// create array
			IDsObjectIf[] data = new IDsObjectIf[m_added.size()]; 
			m_added.values().toArray(data);
			// forward
			fireUpdateEvent(new DsEvent.Update(this,DsEventType.ADDED_EVENT,0,data));
			// clear items
			m_added.clear();
		}
	}
	
	protected void fireModified(List<T> objects, int flags) {
		IDsObjectIf[] data = new IDsObjectIf[objects.size()]; 
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
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.REMOVED_EVENT,0,new IDsObjectIf[]{object}));
	}

	protected void fireRemoved(List<T> objects) {
		IDsObjectIf[] data = new IDsObjectIf[objects.size()]; 
		objects.toArray(data);
		fireUpdateEvent(new DsEvent.Update(this,DsEventType.REMOVED_EVENT,0,data));
	}
	
	protected void fireUpdateEvent(DsEvent.Update e) {
		if(e.getData().length>0) {
	 		for(IDsUpdateListenerIf it : m_listeners) {
				it.handleDsUpdateEvent(e);
			}
		}
	}
	protected Update getExisting(Update e) {
		Object s = e.getSource();
		for(Update it : m_queue) {
			if(it.getSource().equals(s)) {
				return it;
			}
		}
		return null;
	}
	
	protected void commit(Map<M,Object[]> changes) {
		// any data to submit?
		if(changes.size()>0) {
			try {
				// create unsafe work
				IDiskoWork<Void> work = new UpdateWork(changes,m_attributes);
				// schedule work
				DiskoWorkPool.getInstance().schedule(work);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/* ============================================================
	 * IDiskoWork implementation
	 * ============================================================ */
	
	@Override
	public Boolean doWork() {
		
		/* =============================================================
		 * DESCRIPTION: This method is listening for updates made in
		 * IAssignmentIf and IRouteIf MSO objects. Only one Update is
		 * handled in each work cycle if spatial changes in routes are 
		 * made. This ensures that the system remains responsive since 
		 * spatial route estimation is time consuming (ArcGIS geodata
		 * lookup). A concurrent FIFO queue is used to store new 
		 * MSO Update events between work cycles.

		 * ALGORITHM: The following algorithm is implemented
		 * 1. Get next Update if exists. Update if new route is 
		 *    created or existing is changed spatially. 
		 * 2. Update all route cost estimates (fast if no spatial changes)
		 * 3. Update all estimated current positions of costs which is not changed
		 * 
		 * DUTY CYCLE MANAGEMENT: MAX_WORK_TIME is the cutoff work duty 
		 * cycle time. The actual duty cycle time may become longer, 
		 * but not longer than a started update or estimate step. 
		 * update() is only allowed to exceed MAX_WORK_TIME/2. The 
		 * remaining time is given to estimate(). This ensures that 
		 * update() will not starve estimate() during long update 
		 * sequences.
		 * 
		 * ============================================================= */
				
		// get start tic
		long tic = System.currentTimeMillis();
		
		// notify changes
		fireAdded();
		fireArchived();
		
		// handle updates in queue
		List<T> changed = update(tic);
		
		// forward
		execute(changed,tic);
		
		// finished
		return true;
		
	}
	
	/* ============================================================
	 * Helper methods
	 * ============================================================ */
	
	private List<T> update(long tic) {

		// initialize
		int count = 0;
		List<T> workSet = new ArrayList<T>(m_queue.size());
		
		// get available work time
		long maxTime = m_availableTime/2;
		
		// loop over all updates
		while(m_queue.peek()!=null) {
								
			// ensure that half MAX_WORK_TIME is only exceeded once
			if(System.currentTimeMillis()-tic>maxTime)
				break;
			
			// get next update event
			Update e = m_queue.poll();
			
			// increment update counter
			count++;
			
			// get flags
			boolean createdObject  = e.isCreateObjectEvent();
	        boolean deletedObject  = e.isDeleteObjectEvent();
	        boolean modifiedObject =   e.isModifyObjectEvent() 
        							|| e.isAddedReferenceEvent() 
        							|| e.isChangeReferenceEvent() 
        							|| e.isDeleteObjectEvent();
	        
	        // get MSO object
	        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
			
	        // initialize cost
			T object = null;
			
			// is object created?
			if(!deletedObject && createdObject) {
				object = msoObjectCreated(msoObj,e);
			}
			
			// is object modified?
			if (!deletedObject && modifiedObject) {
				object = msoObjectChanged(msoObj,e);
			}
			
			// delete object?
			if (deletedObject) {
				object = msoObjectDeleted(msoObj,e);
			}
			
			// add to work set?
			if(object!=null && !workSet.contains(object)) {
				workSet.add(object);				
			}
					
		}
		
		// finished
		return workSet;
		
	}		
	
	private String toString(IDsObjectIf obj) {
		// is MSO object?
		if(obj.getId() instanceof IMsoObjectIf)
			return ((IMsoObjectIf)obj.getId()).getObjectId();
		// default 
		return obj.getId().toString();		
	}
	
	/* =========================================================================
	 * Inner classes
	 * ========================================================================= */
	
    public class UpdateWork extends AbstractDiskoWork<Void> {
		
    	final Map<M,Object[]> m_changes;
    	final List<String> m_attributes;
    	
		public UpdateWork(Map<M,Object[]> changes, List<String> attributes) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					null,0,false,true,false,0);
			// prepare
			m_changes = changes;
			m_attributes = attributes;
		}
		
		@SuppressWarnings("unchecked")
		public Void doWork()  {
			
			/* ========================================================
			 * IMPORTANT:  An IMsoObjectIf should only be added to 
			 * updates for commit if and only if the IMsoObjectIf is 
			 * created (committed to SARA). Furthermore, only the 
			 * changed attribute should be committed. Concurrent work 
			 * sets (potentially at least one per work process) should 
			 * be affected in the same manner as a update event from
			 * the server would do.
			 * 
			 * REASON: This ensures that concurrent work sets is not 
			 * changed in a way that is unpredicted by the user that
			 * is working on these work sets. 
			 * 
			 * For instance, lets look at an example where an IMsoObjectIf 
			 * is created locally and thus, not committed to SARA yet. 
			 * If this thread performs a full commit on all changed objects,
			 * the concurrent work sets are also committed. This may not
			 * be what the user expects to happen, the user may instead
			 * want to rollback the changes. This will not be possible 
			 * any longer because this thread already has committed all 
			 * the changes. Hence, these precautions should be is in the 
			 * concurrent work sets best interest, an the least invasive 
			 * ones. 
			 * ======================================================== */
			
			// initialize local lists
			List<T> objects = new ArrayList<T>(m_changes.size());
			List<IUpdateHolderIf> updates = new ArrayList<IUpdateHolderIf>(m_changes.size());				
			
			// loop over all assignments
			for(M it : m_changes.keySet()) {
			
				// add object to list
				objects.add(getDsObject(it));
				
				// get values to update
				Object[] values = m_changes.get(it); 
				
				// update attributes
				for(int i=0;i<m_attributes.size();i++) {
				
					// get attribute name
					String name = m_attributes.get(i);
					
					// get attribute
					IAttributeIf attr = it.getAttributes().get(name);					
					
					// update attribute
					attr.set(values[i]);				
					
					// add to updates?
					if(it.isCreated()) {
						// get update holder set
						IUpdateHolderIf holder = m_comitter.getUpdates(it);
						// has updates?
						if(holder!=null) {
							// is modified?
							if(!holder.isCreated() && holder.isModified()) {
								// set partial update
								holder.setPartial(name);
								// add to updates?										
								updates.add(holder);
							}
						}
					}
				}
			}
			
			// notify listeners
			fireModified(objects,0);						
			
			try {
				// commit changes?
				if(updates.size()>0) {
					m_comitter.commit(updates);
				}
			} catch (CommitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			
			// finished
			return null;
			
		}
		
	}
}
