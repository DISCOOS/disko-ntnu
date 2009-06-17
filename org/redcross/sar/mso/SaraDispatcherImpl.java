package org.redcross.sar.mso;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import no.cmr.hrs.sar.model.Fact;
import no.cmr.hrs.sar.model.Operation;
import no.cmr.hrs.sar.model.SarObjectImpl;
import no.cmr.hrs.sar.tools.ChangeObject;
import no.cmr.hrs.sar.tools.IDHelper;
import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.redcross.sar.Application;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoTransactionListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.WorkPool;
import org.rescuenorway.saraccess.api.*;
import org.rescuenorway.saraccess.except.SaraException;
import org.rescuenorway.saraccess.model.*;

/**
 * For documentation, see {@link  IDispatcherIf}
 */
public class SaraDispatcherImpl implements IDispatcherIf, IMsoTransactionListenerIf, SaraChangeListener
{

	/**
	 * Logger object for this class.
	 */
	private static final Logger logger = Logger.getLogger(SaraDispatcherImpl.class);
	
	/** 
	 * <b>SaraChangeEvent arrival buffer strategy</b></p>
	 * 
	 * Do to the SARA and MSO model implementations, events arrive in "trains".
	 * The reason is the transaction nature of the commit process. Changes in 
	 * local instances of MSO model is committed as a group. 
	 * 
	 * This property can be exploited. Since changes comes in "trains", a 
	 * arrival buffer strategy reduces the amount of model and UI updates 
	 * required locally. In this implementation, a 2 second buffer is 
	 * hard coded. The following rules and assumptions are applied
	 * 
	 * 1. Changes are added as long as it is less than X seconds 
	 *    since last time the MSO model was updated
	 * 2. The buffer will empty every X seconds, regardless of the arrivals
	 * 3. It is assumed that the buffer is of sufficiently large capacity 
	 *    with respect to the operational requirements of the system.
	 * 
	 * IMPORTANT! It is not implemented any recovery mechanisms if the buffer
	 * overruns. This vulnerability should be addressed in the future...
	 * 
	 **/
	final static int SARA_CHANGE_EVENT_BUFFER_DELAY = 1000;
	
	/**
	 * <b>Timer used to implement the arrival buffer</b></p>
	 * 
	 * For internal use.
	 */
    private Timer timer = new Timer();

    
    /**
	 * <b>Delay schedule (buffer) implementation </b></p>
	 * 
	 * Implements the arrival buffer.
	 */
    private DelaySchedule schedule = null;

    
	/**
	 * <b>The work pool instance</b></p>
	 * 
	 * For internal use.
	 */
    private WorkPool m_workPool;
    
	
	/** 
	 * <b>Initialization of SARA dispatcher </b></p> 
	 * 
	 * When the dispatcher is initialized, background threads are started. 
	 * These threads loads available operations asynchronously. Hence, 
	 * enumeration should be delayed until all operations are loaded in SARA. 
	 **/
    boolean m_isInitiated = false;
    
    
    /**
     * <b>Initialization of active operation</b></p>
     * 
     * Each time a operation opened locally, the dispatchers has to build the
     * model representing it. This take time. 
     */   
    boolean m_isLoading = false;

    
	/**
	 * <b>SARA access service</b></p>
	 * 
	 * Access to SARA is supplied through the SARA access service. This 
	 * service returns a SARA session, which all traffic is handed through. 
	 */
    SarAccessService sarSvc;
    
    
	/**
	 * <b>Sar operation </b></p>
	 * 
	 * Each sar operation is represented by the  
	 */    
    SarOperation sarOperation = null;
    
    
    /**
     * <b>SARA to MSO object mapping</b></p>
     * 
     * For internal use.
     */
    Map<SarBaseObject, IMsoObjectIf> saraMsoMap = new HashMap<SarBaseObject, IMsoObjectIf>();

    /**
     * <b>MAS to SARA object mapping</b></p>
     * 
     * For internal use.
     */
    Map<IMsoObjectIf, SarBaseObject> msoSaraMap = new HashMap<IMsoObjectIf, SarBaseObject>();

    /**
     * <b>Dispatch listeners</b>
     * 
     * Through the {@link IDispatchListenerIf} interface, the dispatcher is able to notify
     * listeners of system critical changes. If these events are not listened for and handled
     * propertly, the system may misserably fail...
     */
    List<IDispatcherListenerIf> listeners = new ArrayList<IDispatcherListenerIf>();

    
    IMsoModelIf m_msoModel;

    public SaraDispatcherImpl()
    {
        // setUpService();
    }

    public IMsoObjectIf.IObjectIdIf makeObjectId()
    {
        return new AbstractMsoObject.ObjectId(sarOperation.getID() + "." + sarSvc.getSession().createInstanceId(),null);
    }

    public SarAccessService getSarSvc()
    {
        return sarSvc;
    }

    public void initiate()
    {
    	if(!m_isInitiated) {
    		
	        setUpService();
	        IMsoModelIf imm = Application.getInstance().getMsoModel();
	        imm.getEventManager().addCommitListener(this);
	        m_isInitiated = true;
	        if (sarSvc.getSession().isFinishedLoading()
	        		&& sarSvc.getSession().getOperations().getOperations().size() == 0)
	        {
	            m_isLoading = true;
	            sarSvc.getSession().createNewOperation("MSO", true);
	        }
    	}
    }

    public boolean isInitiated()
    {
        if (!m_isLoading && sarSvc.getSession().isFinishedLoading() && sarSvc.getSession().getOperations().getOperations().size() == 0)
        {
            m_isLoading = true;
            sarSvc.getSession().createNewOperation("MSO", true);
        }
        return sarSvc.getSession().isFinishedLoading() && !m_isLoading;
    }

    public List<String[]> getActiveOperations()
    {
        List<String[]> ops = new ArrayList<String[]>();
        List<SarOperation> opers = sarSvc.getSession().getOperations().getOperations();
        for (SarOperation soper : opers)
        {
            String[] descr = {IDHelper.formatOperationID(soper.getID()), soper.getID()};
            ops.add(descr);
        }
        return ops;
    }

    public boolean setActiveOperation(String opID)
    {

    	// initialize
    	boolean bFlag = false;

        // notify
        getWorkPool().suspend();
        getMsoModel().setRemoteUpdateMode();
        getMsoModel().suspendUpdate();

    	try {

    		// notify deactivation?
    		if(sarOperation !=null) {
    			fireOnOperationDeactivated(sarOperation.getID());
    		}

    		// deactivate current operation
    		sarOperation = null;

    		// get operation from SARA session
    		SarOperation soper = sarSvc.getSession().getOperation(opID);

	        // try to clear current MSO model
	        if (clearMSO()){

		        // try to set as active operation
		        if (setActiveOperation(soper)) {

			        // save operation
			        sarOperation = soper;

			        // notify activation
			        fireOnOperationActivated(sarOperation.getID());

			        // success
			        bFlag = true;
		        }
	        }

    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    	// resume old modes
    	getMsoModel().resumeUpdate();
        getMsoModel().restoreUpdateMode();
    	getWorkPool().resume();

        // finished
    	return bFlag;

    }

    public String getActiveOperationID()
    {
    	String id = null;
    	if(sarOperation != null)
    		id = sarOperation.getID();
    	return id;
    }

    public String getActiveOperationName()
    {
    	String name = null;
    	if(sarOperation != null)
    		name = IDHelper.formatOperationID(getActiveOperationID());
    	return name;
    }

    private boolean clearMSO()
    {

        // initialize
        boolean success = false;

        // suspend work pool
        getWorkPool().suspend();

        // set remote update mode
        getMsoModel().setRemoteUpdateMode();

        try {

	    	// get manager
	        IMsoManagerIf msoManager = getMsoModel().getMsoManager();

	        // get operation?
	    	IOperationIf opr = msoManager.operationExists() ? msoManager.getOperation() : null;

	    	// has operation?
	    	if(opr!=null) {

	    		/* ==========================================================
	    		 * The MSO model is cleared by deleting all objects
	    		 * in reverse order (relations first, then objects).
	    		 *  
	    		 * Because all objects are iterated, only a shallow
	    		 * delete is required. Hence, calling delete(), which 
	    		 * performs a deep delete (recursively 
	    		 * deleting all owned objects also), should not be used. 
	    		 * Instead, shallow delete is performed by calling the
	    		 * destroy() method. 
	    		 * 
	    		 * ========================================================== */
	    			    		
	        	// delete all deleteable relations
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoRelationIf) {
	        			if(msoObj.isDeletable()) {
	        				((AbstractMsoObject) msoObj).delete(false);
	        			}
	        		}
	        	}

	        	// delete all deleteable objects
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoObjectIf && !(msoObj instanceof IOperationIf)) {
	        			if(msoObj.isDeletable())
	        			((AbstractMsoObject) msoObj).delete(false);
	        		}
	        	}

	        	// delete all undeleteable objects
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj!=null && !msoObj.isDeleted() && !(msoObj instanceof IOperationIf))
	        			((AbstractMsoObject) msoObj).delete(false);
	        	}

		    	// delete operation?
		    	if(opr!=null)  {
		    		/* calling delete is now safe because all 
		    		 * owned object are now deleted */
		    		opr.delete(true);
		    	}

		    	// clear mapping
		    	saraMsoMap.clear();
		    	msoSaraMap.clear();

		    	// do garbage collection now
		    	Runtime.getRuntime().gc();

	        	// notify all listeners?
		    	if(opr!=null) {
		    		getMsoModel().getEventManager().notifyClearAll(opr);
		    	}

	    	}

	    	// set flag
	    	success = true;

    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}

    	// resume to previous update mode
        getMsoModel().restoreUpdateMode();

    	// resume work pool
    	getWorkPool().resume();

    	// return state
        return success;

    }

    private boolean setActiveOperation(SarOperation soper)
    {

        // CREATE MSO operation
        createMsoOperation(soper,false);

        // get copy of object
        List<SarObject> objects = new ArrayList<SarObject>(soper.getObjectList());

        // ADD ALL CO
        for (SarObject so : objects)
        {
            if (so.getName().equals("CmdPost"))
            {
                addMsoObject(so);
            }
        }

        // ADD REST OF SARObjects
        for (SarObject so : objects)
        {
            if (!so.getName().equals("CmdPost"))
            {
                addMsoObject(so);
            }
        }

        //ADD RELATIONS
        for (SarObject so : objects)
        {
            // get copy of named relations
            Hashtable<String,SarBaseObject> namedRelations = new Hashtable<String,SarBaseObject>(so.getNamedRelations());

            //Add named relations
            Iterator<Map.Entry<String, SarBaseObject>> table = namedRelations.entrySet().iterator();
            while (table.hasNext())
            {
                Map.Entry<String, SarBaseObject> entry = table.next();
                updateMsoRelation(so, (SarObjectImpl) entry.getValue(), entry.getKey(), SarBaseObjectImpl.ADD_NAMED_REL_FIELD);
            }

            // get copy of named relations
            Hashtable<String,List<SarBaseObject>> relations = new Hashtable<String,List<SarBaseObject>>(so.getRelations());

            //Add rest
            Iterator<Map.Entry<String, List<SarBaseObject>>> rels = relations.entrySet().iterator();
            while (rels.hasNext())
            {
                Map.Entry<String, List<SarBaseObject>> entry = rels.next();
                for (int i = 0; i < entry.getValue().size(); i++)
                {
                    try
                    {
                        SarObjectImpl sarBaseObject = (SarObjectImpl)entry.getValue().get(i);

                        updateMsoRelation(so, sarBaseObject, entry.getKey(), SarBaseObjectImpl.ADD_REL_FIELD);
                    }
                    catch (Exception e)
                    {
                        Log.printStackTrace(e);
                    }
                }

            }

        }

        return true;

    }

    private IMsoModelIf getMsoModel() {
    	if(m_msoModel==null) {
			m_msoModel = Application.getInstance().getMsoModel();
    	}
    	return m_msoModel;
    }

    private WorkPool getWorkPool() {
    	if(m_workPool==null) {
    		try {
				m_workPool = WorkPool.getInstance();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	return m_workPool;
    }

    public void finishActiveOperation()
    {
        //If this is the only operation, shutdown and create a new one
        boolean createNew = false;
        if (sarSvc.getSession().getOperations().getOperations().size() == 1)
        {
            createNew = true;
        }

        // forward
        sarSvc.getSession().finishOperation(sarOperation.getID());

        if (createNew)
        {
            createNewOperation();
        }

    }

    public void createNewOperation()
    {
        m_isLoading = true;
        sarSvc.getSession().createNewOperation("MSO", true);
    }

    public void merge()
    {
        //To change body of implemented methods use File | Settings | File Templates.
        //TODO
    }

    public void shutdown()
    {
        sarSvc.getSession().shutDown();
    }

    private void createMsoOperation(SarOperation oper, boolean notify)
    {
        // only one active MSO operation allowed
        IMsoManagerIf msoManager = Application.getInstance().getMsoModel().getMsoManager();
        if (!msoManager.operationExists())
        {
            try
            {
                String form = IDHelper.formatOperationID(oper.getID());
                String prefix = "";
                String number = "";
                if (form.indexOf("-") > 0)
                {
                    prefix = form.substring(0, form.lastIndexOf("-"));
                    number = form.substring(form.lastIndexOf("-") + 1);
                }
                /*
                ControllerInterface c = ((Operation)oper).getController();
                if(c instanceof SaraApiController) {
                	SaraApiController sc = (SaraApiController)c;
                	SARparse p = sc.getParser();
                }
                */
                // get creation date
                List<?> periods = ((Operation)oper).getActivePeriods();
            	Calendar t = Calendar.getInstance();
                if(periods.size()>0)
                	t.setTimeInMillis(Long.valueOf(periods.get(0).toString()));

                IMsoObjectIf.IObjectIdIf operid = new AbstractMsoObject.ObjectId(oper.getID(),t.getTime());
                msoManager.createOperation(prefix, number, operid);
                sarOperation = oper;
                // notify?
                if(notify && sarOperation!=null) {
                	fireOnOperationActivated(sarOperation.getID());
                }

            }
            catch (DuplicateIdException e) // shall not happen
            {
                e.printStackTrace();
            }
        } else
        {
            // Hendelse er allerede opprettet, hva n�
        }
    }

    // global object used in the the commit operation
    private final HashMap<String, SarObject> newObjects = new HashMap<String, SarObject>();

    public void handleMsoCommitEvent(MsoEvent.Commit e) throws TransactionException
    {

    	/* =======================================================
    	 * Consecutive commit should be serialized by unsafe
    	 * thread the Work Pool mechanism. This ensures that all
    	 * commits are serialized to prevent concurrent commits.
    	 * Hence, every commit can be matched to the LOOPBACK
    	 * events from the message queue. If some updates are
    	 * still pending after a specified amount of time, a
    	 * CommitException is thrown with update residue
    	 * information.
    	 * ======================================================= */

    	// prepare
        newObjects.clear();

        // Check that operation is added
        if (sarSvc.getSession().isFinishedLoading() && sarSvc.getSession().getOperations().getOperations().size() == 0)
        {
            sarSvc.getSession().createNewOperation("MSO", true);
        }

        // get commit wrapper
        ITransactionIf transaction = (ITransactionIf) e.getSource();

        // get object list
        List<IChangeRecordIf> list = transaction.getChanges();

        // prepare changed objects
        for (IChangeRecordIf it : list)
        {
            //IF created, create SARA object
            if (it.isObjectCreated())
            {
            	// create sara object
                SarObject so = createSaraObject(it);
                newObjects.put(so.getID(), so);
                so.createNewOut();
            } else if (it.isChanged())
            {
                // modify SaraObject.
                updateSaraObject(it);
            }
        }

        // prepare object to object relations (1-to-1, or named relation)
        for (IChangeIf.IChangeRelationIf it : transaction.getObjectRelations())
        {
        	// forward
            msoRelationChanged(it, true);
        }

        // prepare list changes (1-to-N relations)
        for (IChangeIf.IChangeRelationIf it : transaction.getListRelations())
        {
            msoRelationChanged(it, false);
        }

        // Handle delete object last
        for (IChangeRecordIf it : list)
        {
            if (it.isObjectDeleted())
            {
                // if deleted remove Sara object
                deleteSaraObject(it);
            }
        }

        // forward
        sarSvc.getSession().commit(sarOperation.getID());

    }

    private void msoRelationChanged(IChangeIf.IChangeRelationIf aChange, boolean isNamedRelation)
    {
        // initialize
        IMsoObjectIf ownObj = aChange.getReferringObject();
        IMsoObjectIf refObj = aChange.getReferredObject();
        SarObject srcObj = sarOperation.getSarObject(ownObj.getObjectId());
        SarObject relObj = sarOperation.getSarObject(refObj.getObjectId());
        String refName = aChange.getName();
        // if SAR objects does not exists in SAR operation, 
        // try map of SAR objects created in this commit 
        // operations, but not committed yet.
        if (srcObj == null)
        {
            srcObj = newObjects.get(ownObj.getObjectId());
        }
        if (relObj == null)
        {
            relObj = newObjects.get(refObj.getObjectId());
        }
        // objects was still not found?
        if (ownObj == null)
        {
            Log.warning("Object not found: " + ownObj.getObjectId());
        } 
        else if (refObj == null)
        {
            Log.warning("Object not found: " + refObj.getObjectId());
        } else
        {
        	// dispatch
            if (aChange.isRelationAdded())
            {
            	// add relation
                if (isNamedRelation)
                {
                    srcObj.addNamedRelation(refName, relObj);
                } else
                {
                    srcObj.addRelation(refName, relObj);
                }
            } else if (aChange.isRelationRemoved())
            {
            	// add relation
            	if (isNamedRelation)
                {
                    srcObj.removeNamedRelation(refName);
                } else
                {
                    srcObj.removeRelation(refName, relObj);
                }
            }
        }
    }

    private SarObject createSaraObject(IChangeRecordIf aRecord)
    {
        IMsoObjectIf msoObj = aRecord.getMsoObject();
        msoObj.getClassCode();
        // get object type
        SarSession sarSess = sarSvc.getSession();
        String className = msoObj.getClass().getName();
        if (className.indexOf("Impl") > 0)
        {
            className = className.substring(26, className.indexOf("Impl"));
        }
        String objId = msoObj.getObjectId().indexOf(".") > 0 ?
                msoObj.getObjectId().substring(msoObj.getObjectId().indexOf(".") + 1) :
                msoObj.getObjectId();
        SarObject sbo = (SarObject) sarSess.createInstance(
                className, sarOperation.getID(),
                SarBaseObjectFactory.TYPE_OBJECT, objId);

        // connect to operation
        sbo.setOperation(sarOperation);
        // update relations and attributes
        updateSaraObject(sbo, aRecord.getMsoObject(), aRecord, false);
        // finished
        return sbo;
    }

    private void updateSaraObject(IChangeRecordIf aRecord)
    {
        // get sara object
    	SarObject so = sarOperation.getSarObject(aRecord.getMsoObject().getObjectId());
        // ensure that this change is submitted to all listeners before any relations is updated
        updateSaraObject(so, aRecord.getMsoObject(), aRecord, true);
    }

    private void updateSaraObject(SarObject sbo, IMsoObjectIf msoObj, IChangeRecordIf aRecord, boolean submitChanges)
    {
    	// initialize
    	Map<String, IMsoAttributeIf<?>> attrMap = msoObj.getAttributes();
        Map<String, IMsoRelationIf<?>> objRefMap = msoObj.getObjectRelations();
        List<SarBaseObject> sarObjs = sbo.getObjects();
        boolean isComplete = aRecord==null || aRecord!=null&&!aRecord.isFiltered();

        // loop over all objects in sara object
        for (SarBaseObject so : sarObjs)
        {
            try
            {
            	// is attribute object?
                if (so instanceof SarFact)
                {
                    // Map fact to attribute
                    String attrName = ((SarFact) so).getLabel();
                    IMsoAttributeIf<?> msoAttr = attrMap.get(attrName.toLowerCase());
                    // only update fact if this is a complete update, or if attribute
                    // is included in the partial update
                    if(isComplete || aRecord.containsFilter(msoAttr)) {
	                    // found attribute?
	                    if (msoAttr != null)
	                    {
	                        SaraMsoMapper.mapMsoAttrToSarFact(sbo, (SarFact) so, msoAttr, submitChanges);
	                    } else if (!attrName.equalsIgnoreCase("Objektnavn"))
	                    {
	                        Log.warning("Attribute " + attrName + " not found for " + sbo.getName());
	                    }
                    }
                /* object relations (which is objects them selves) are
                 * only updated if this is a complete commit operation 
                 * (the opposite of a partial commit). */
                } else 
                {
                    if(so instanceof SarObject) {
	                	String objName = ((SarObject) so).getName();
	                    IMsoObjectIf refObj = objRefMap.get(objName).get();
	                    // only update fact if this is not a partial update, or if object relation
	                    // is included in the partial update	                    
	                    if (refObj != null && (isComplete || aRecord.containsFilter(refObj)))
	                    {
	                    	// recurse on relation object
	                        updateSaraObject((SarObject) so, refObj, null, submitChanges);
	                    }
                    }
                }
            }
            catch (Exception e)
            {
                Log.error("Unable to map to Sara :(sarObj:" + so.getValueAsString() + ",msoObj:" + msoObj.getClass().getName() + e.getMessage());
            }
        }

        if (submitChanges)
        {
        	// commit this change
        	sarSvc.getSession().commit(sarOperation.getID());
        }

    }

    private void deleteSaraObject(IChangeRecordIf aRecord)
    {
        // get object from operation
        SarObject soi = sarOperation.getSarObject(aRecord.getMsoObject().getObjectId());

    	// forward
        soi.delete("DISKO");

    }

    public void setUpService()
    {
        sarSvc = SarAccessService.getInstance();
        Credentials creds = new UserPasswordCredentials("Operat�r", "Operat�r");
        try
        {
        	sarSvc.createSession(creds);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        sarSvc.getSession().AddChangeListener(this);

        try
        {
            sarSvc.startRecvMessages();
        }
        catch (Exception e)
        {
            Log.printStackTrace(e);
            //Log.warning(e.getMessage());
        }
    }

    protected String getSarObjectID(SaraChangeEvent change) {
		// get source
    	Object source = change.getSource();
    	// get id
        if (source instanceof SarOperation){
            return ((SarOperation)source).getID();
        }
        else if (source instanceof SarObject){
            return ((SarObject)source).getID();
        }
        else if (source instanceof ChangeObject) {
        	// get SarObject
            SarObject so = getParentObject(change.getParent());
            // return id?
            if(so!=null) {
            	return so.getID();
            }
        }
        // not found
    	return null;
    }


    /* =====================================================================
     * SaraChangeListener implementation
     * =====================================================================*/

    public void saraChanged(final SaraChangeEvent change)
    {

        if (change.getSource() instanceof SarOperation)
        {
            if (change.getChangeType() == SaraChangeEvent.TYPE_ADD)
            {
            	boolean current = m_isLoading;
                m_isLoading = false;
                fireOnOperationCreated(((SarOperation) change.getSource()).getID(), current);
            }
        }
        if (sarOperation != null && change.getSarOp() == sarOperation)
        {
        	try {

        		// get change event
        		int type = change.getChangeType();

            	// do the update
                if (   type == SaraChangeEvent.TYPE_ADD
                	|| type == SaraChangeEvent.TYPE_CHANGE
                	|| type == SaraChangeEvent.TYPE_REMOVE )
                {

                	// forward
                	scheduleSaraChangeWork(change);

                }

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
    }

    private void scheduleSaraChangeWork(SaraChangeEvent e) {
    	// get new schedule from old
    	schedule = new DelaySchedule(schedule);
    	try {
			// add to schedule
			schedule.add(e);
		} catch (RuntimeException ex) {
			logger.error("Failed to add Sara change work to schedule. This implies that " +
					"the buffer is of usufficiently large capacity with respect to the operational " +
					"requirements of the system. The buffer interval should therefore be decreased.");
		}
		// cancel schedule
		timer.cancel();
		// set new timer
		timer = new Timer();
		// schedule Sara Change Work
		timer.schedule(schedule, SARA_CHANGE_EVENT_BUFFER_DELAY);
    }

    public void saraException(SaraException sce)
    {
        //TODO videresend exception
    }

    private void removeMsoFromSara(SaraChangeEvent change)
    {

        Object co = change.getSource();
        IMsoManagerIf msoManager = Application.getInstance().getMsoModel().getMsoManager();

        if(msoManager.operationExists()) {

	        try
	        {

	            if (co instanceof SarOperation)
	            {
	            	String oprId = sarOperation.getID();
	            	boolean current = (co == sarOperation);
	            	boolean isFinished = (sarOperation instanceof Operation
	            			? ((Operation)sarOperation).isFinished() : true);
	            	if(current) {
	            		sarOperation = null;
	            		clearMSO();
	            	}
	            	// notify
	            	if(isFinished) {
	            		fireOnOperationFinished(oprId,current);
	            	}
	            	else if(current) {
	            		fireOnOperationDeactivated(oprId);
	            	}
	            } else
	            {
	                IMsoObjectIf msoObj = saraMsoMap.get(co);
	                if (msoObj != null)
	                {
	                    msoManager.remove(msoObj);
	                }
	            }
	        }
	        catch (MsoException e)
	        {
	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	        }
        }

    }

    private void changeMsoFromSara(SaraChangeEvent change)
    {
        SarBaseObject so = change.getParent();
        ChangeObject co = (ChangeObject) change.getSource();

        if (co.getFactType() == Fact.FACTTYPE_RELATION)
        {

        	String[] toObject = (String[])co.getToObject();

            String relId = toObject.length>1?toObject[1]:null;
            String relName = ((String[]) co.getToObj())[0];
            SarObjectImpl rel = (SarObjectImpl) sarOperation.getSarObject(relId);
            updateMsoRelation(so, rel, relName, co.getFieldName());

        } else
        {
        	// initialize
        	AttributeImpl<?> attr = null;
            // find object representing the SARA fact type
            SarObject parentObject = getParentObject(so);
            // Use object to find MSO object
            if (parentObject != null)
            {
                IMsoObjectIf msoObj = saraMsoMap.get(parentObject);
                Map<?,?> attrs = msoObj.getAttributes();
                attr = (AttributeImpl<?>) attrs.get(((SarFact) so).getLabel().toLowerCase());
                if (attr != null)
                {
                    SaraMsoMapper.mapSarFactToMsoAttr(attr, (SarFact) so, co.getGivenTime());
                }

            }
            // not supported?
            if (parentObject == null || attr == null)
            {
                Log.warning("NOT IMPLEMENTED YET, changeMsoFromSara field: " + co.getFieldType() + " ftype: " + co.getFactType());
            }
        }
    }

    @SuppressWarnings("unchecked")
	private void updateMsoRelation(SarBaseObject so, SarObjectImpl rel, String relName, String fieldName)
    {
    	// get relation objects
        IMsoObjectIf source = saraMsoMap.get(so);
        IMsoObjectIf relObj = saraMsoMap.get(rel);
        //Get type relation change
        if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.ADD_REL_FIELD))
        {
            try
            {
                source.addListRelation(relObj, relName);
            }
            catch (Exception e)
            {
                Log.printStackTrace("Failed to add list relation", e);
            }
        } else if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.ADD_NAMED_REL_FIELD))
        {
        	try {
				source.setObjectRelation(relObj, relName);
			} catch (Exception e) {
                Log.printStackTrace("Failed to set object relation", e);
			}
            
	    } else if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.REM_REL_FIELD))
	    {	    	
	    	try {
				source.removeListRelation(relObj, relName);
			} catch (Exception e) {
                Log.printStackTrace("Failed to remove list relation", e);
			}
            
        } else if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.REM_NAMED_REL_FIELD))
        {   
        	try {
				source.setObjectRelation(null, relName);
			} catch (Exception e) {
                Log.printStackTrace("Failed to reset object relation", e);
			}
	    }
        
    }

    private SarObject getParentObject(SarBaseObject so)
    {
        //Use id rules to find object id  (
        SarObject soi = null;
        try
        {
            String parentid = so.getID().substring(0, so.getID().lastIndexOf("."));
            String operid = so.getID().substring(0, so.getID().indexOf("."));
            SarOperation soper = sarSvc.getSession().getOperation(operid);
            soi = soper.getSarObject(parentid);
            return soi;
        }
        catch (Exception e)
        {
            Log.printStackTrace("Unable to find parent", e);
            return null;
        }

    }

    protected void addMsoObject(SarObject sarObject)
    {
        // initialize
    	IMsoObjectIf msoObj = null;
        IMsoManagerIf msoMgr = Application.getInstance().getMsoModel().getMsoManager();

        /* ======================================================
         * MSO Object creation
         * ======================================================
         * Create method name and use reflection to create object
         * in MSO model. The method has the form createXXX,
         * where XXX equals the object name. For every MSO
         * object, two createXXX methods exists in the MSO
         * Manager. One for remove creation which is used by this
         * method (object id known), and one for local creation
         * ====================================================== */

        String methodName = "create" + sarObject.getName();

        // get creation time
        Date creationTime = ((SarObjectImpl)sarObject).getCreationDate();

        Method m = null;
        try
        {
            m = msoMgr.getClass().getMethod(methodName, IMsoObjectIf.IObjectIdIf.class);
            m.setAccessible(true);
            IMsoObjectIf.IObjectIdIf id = new AbstractMsoObject.ObjectId(sarObject.getID(),creationTime);
            msoObj = (IMsoObjectIf) m.invoke(msoMgr, new Object[]{id});

            // Handle any exceptions thrown by method to be invoked.
        }
        catch (Exception x)
        {
            Log.error(x.toString());
        }

        // update creation date if mso object already exists?
        if(msoObj!=null && !msoObj.isCreated()) {
        	msoObj.setCreatedTime(creationTime);
        }

        // update MSO object values
        setMsoObjectValues(sarObject, msoObj);

        // update internal object mappings
        saraMsoMap.put(sarObject, msoObj);
        msoSaraMap.put(msoObj, sarObject);

    }

    public static void setMsoObjectValues(SarObject sarObject, IMsoObjectIf msoObj)
    {
    	// loop over all SARA fact objects
        for (SarBaseObject fact : sarObject.getObjects())
        {
            if (fact instanceof SarFact)
            {
                try
                {
                	// get attribute mapping information
                    Map<?,?> attrs = msoObj.getAttributes();
                    // get MSO attribute from SARA Fact label name
                    AttributeImpl<?> attr = (AttributeImpl<?>) attrs.get(((SarFact) fact).getLabel().toLowerCase());
                    // found attribute?
                    if (attr != null)
                    {
                    	Date date = sarObject.getCreationDate();
                        SaraMsoMapper.mapSarFactToMsoAttr(attr, (SarFact) fact, date!=null ? date.getTime() : 0);
                    }
                }
                catch (Exception ex)
                {
                    try
                    {
                        Log.warning("Attr not found " + ((SarFact) fact).getLabel() + " for msoobj " + msoObj.getClassCode() + "\n" + ex.getMessage());
                    }
                    catch (Exception e)
                    {
                        Log.printStackTrace(e);
                    }
                }
            } else
            {
                //TODO handle internal object attributes
            }
        }
    }

    /*===============================================================
     * Inner classes
     *===============================================================
     */

    class DelaySchedule extends TimerTask {

    	List<SaraChangeEvent> changes = new ArrayList<SaraChangeEvent>();

    	DelaySchedule(DelaySchedule schedule) {
    		if(schedule!=null) changes = schedule.changes;
    	}

    	public void add(SaraChangeEvent e) {
    		changes.add(e);
    	}

    	public List<SaraChangeEvent> getBuffer() {
    		return changes;
    	}

		@Override
		public void run() {
    		try {
    			// create work
    			SaraChangeWork work = new SaraChangeWork(new ArrayList<SaraChangeEvent>(changes));
        		// schedule on work pool
				WorkPool.getInstance().schedule(work);
				// cleanup
				changes.clear();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

    }

	class SaraChangeWork extends AbstractWork {

		private List<SaraChangeEvent> changes = null;

		public SaraChangeWork(List<SaraChangeEvent> changes) throws Exception {
			// forward
			super(HIGH_HIGH_PRIORITY,false,true,WorkerType.SAFE,"Bearbeider",500,true,true);
			// save event
			this.changes = changes;
 		}

		protected void beforePrepare() {
        	// set remote update mode
			Application.getInstance().getMsoModel().setRemoteUpdateMode();
		}

		/**
		 * Worker
		 *
		 * Notifies the model of change in worker thread (system modal)
		 */
		public Void doWork(IWorkLoop loop) {
			// DEBUG: Print line
			System.out.println("SaraChangeWork-"+System.currentTimeMillis());
        	// catch errors and log them
            try
            {
            	// loop over all changes
            	for(SaraChangeEvent change : changes) {

	            	// do the update
	                if (change.getChangeType() == SaraChangeEvent.TYPE_ADD)
	                {
	                    if (change.getSource() instanceof SarOperation){
	                        createMsoOperation((SarOperation) change.getSource(),true);
	                    }
	                    else if (change.getSource() instanceof SarObject){
	                        addMsoObject((SarObject) change.getSource());
	                    }
	                    else{
	                        Log.warning("SaraChange not handled for objectType " + change.getSource().getClass().getName());
	                    }
	                    //TODO implementer for de andre objekttypene fact og object
	                }
	                else if (change.getChangeType() == SaraChangeEvent.TYPE_CHANGE){
	                    changeMsoFromSara(change);

	                }
	                else if (change.getChangeType() == SaraChangeEvent.TYPE_REMOVE){
	                    removeMsoFromSara(change);
	                }

            	}
            }
            catch (Exception e)
            {
                Log.printStackTrace("Unable to update msomodel " + e.getMessage(), e);
            }
            // finished
			return null;
		}

		protected void afterDone() {
            // resume to old mode
			Application.getInstance().getMsoModel().restoreUpdateMode();
		}

	}

	private void fireOnOperationCreated(final String oprId, final boolean current) {
		for (IDispatcherListenerIf it : listeners) {
			it.onOperationCreated(oprId, current);
		}
	}

	private void fireOnOperationFinished(final String oprId, final boolean current) {
		for (IDispatcherListenerIf it : listeners) {
			it.onOperationFinished(oprId, current);
		}
	}

	private void fireOnOperationActivated(final String oprId) {
		for (IDispatcherListenerIf it : listeners) {
			it.onOperationActivated(oprId);
		}
	}

	private void fireOnOperationDeactivated(final String oprId) {
		for (IDispatcherListenerIf it : listeners) {
			it.onOperationDeactivated(oprId);
		}
	}

	@Override
	public boolean addDispatcherListener(IDispatcherListenerIf listener) {
		if(!listeners.contains(listener)) {
			return listeners.add(listener);
		}
		return false;
	}

	@Override
	public boolean removeDispatcherListener(IDispatcherListenerIf listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;
	}
}