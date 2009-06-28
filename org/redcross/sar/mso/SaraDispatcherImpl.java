package org.redcross.sar.mso;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import no.cmr.hrs.sar.model.Message;
import no.cmr.hrs.sar.model.Operation;
import no.cmr.hrs.sar.model.SarObjectImpl;
import no.cmr.hrs.sar.tools.ChangeObject;
import no.cmr.hrs.sar.tools.IDHelper;
import no.cmr.tools.Log;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoException;
import org.redcross.sar.work.AbstractWork;
import org.redcross.sar.work.IWorkLoop;
import org.redcross.sar.work.IWorkPool;
import org.rescuenorway.saraccess.api.*;
import org.rescuenorway.saraccess.except.SaraException;
import org.rescuenorway.saraccess.model.*;

/**
 * For documentation, see {@link  IDispatcherIf}
 */
public class SaraDispatcherImpl implements IDispatcherIf, SaraChangeListener
{

	/**
	 * This constant identifies a operation finished occurrence.
	 */
	private static final String TO_FINISHED = "¤ AVSLUTTET";

	/**
	 * This constant identifies a internal message.
	 */
	private static final String FROM_INTERN = "¤ INTERN";
	
	/**
	 * The dispatched MSO model
	 */
    private IMsoModelIf m_msoModel;
	
	/**
	 * Logger object for this class.
	 */
	private static final Logger m_logger = Logger.getLogger(SaraDispatcherImpl.class);
	
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
    private Timer m_timer = new Timer();

    
    /**
	 * <b>Delay schedule (buffer) implementation </b></p>
	 * 
	 * Implements the arrival buffer.
	 */
    private DelaySchedule m_schedule = null;

    
	/**
	 * <b>The work pool instance</b></p>
	 * 
	 * For internal use.
	 */
    private IWorkPool m_workPool;
    
	
	/** 
	 * <b>Initialization of SARA dispatcher </b></p> 
	 * 
	 * When the dispatcher is initialized, background threads are started. 
	 * These threads loads available operations asynchronously. Hence, 
	 * enumeration should be delayed until all operations are loaded in SARA. 
	 **/
    private boolean m_isInitiated = false;
    
    /**
     * <b>Operation creation in process start time</b></p>
     * 
     * Each time a operation is requested created by the dispatcher, the 
     * dispatchers has to wait on the SARA change event that acknowledges the
     * creation. The MSO model is not created until this event is received. This
     * member variable stores the creation process start time in milliseconds. 
     */   
    private long m_creationInProgress = 0;
    
    /**
     * <b>Operation creation in process timeout</b></p>
     * 
     * Each time a operation is requested created by the dispatcher, the 
     * dispatchers has to wait on the SARA change event that acknowledges the
     * creation. The MSO model is not created until this event is received. This
     * member variable stores the timeout associated with the creation process 
     */   
    private long m_creationInProgressTimeOut = 0;

    
	/**
	 * <b>SARA access service</b></p>
	 * 
	 * Access to SARA is supplied through the SARA access service. This 
	 * service returns a SARA session, which all traffic is handed through. 
	 */
    private SarAccessService m_sarAccessService;
    
    /**
     * <b>Current SARA session</b></p>
     * 
     * The SARA session handles all traffic to an from the dispatcher 
     */
    private SarSession m_sarSession;
    
    
	/**
	 * <b>Sar operation </b></p>
	 * 
	 * Each sar operation is represented by the  
	 */    
    private SarOperation m_sarOperation = null;
    
    
    /**
     * <b>SARA to MSO object mapping</b></p>
     * 
     * For internal use.
     */
    private Map<SarBaseObject, IMsoObjectIf> m_saraMsoMap = new HashMap<SarBaseObject, IMsoObjectIf>();

    /**
     * <b>MAS to SARA object mapping</b></p>
     * 
     * For internal use.
     */
    private Map<IMsoObjectIf, SarBaseObject> m_msoSaraMap = new HashMap<IMsoObjectIf, SarBaseObject>();

    /**
     * <b>Dispatch listeners</b>
     * 
     * Through the {@link IDispatchListenerIf} interface, the dispatcher is able to notify
     * listeners of system critical changes. If these events are not listened for and handled
     * propertly, the system may misserably fail...
     */
    private List<IDispatcherListenerIf> m_listeners = new ArrayList<IDispatcherListenerIf>();

    
    private final InetAddress m_localHost;
     
    public SaraDispatcherImpl() throws UnknownHostException {
    	m_localHost = InetAddress.getLocalHost();
	}

	public IMsoObjectIf.IObjectIdIf createObjectId()
    {
        return createObjectId(m_sarOperation.getID(),null,false);
    }

    public boolean isInitiated()
    {
    	return m_sarSession!=null;
    }
    
    public boolean isReady()
    {
    	if(isInitiated())
    	{
	    	// prepare
	    	ensureOperationExists();
	    	// check initiation state
	        return getSarSession().isFinishedLoading() && !isCreationInProgress();
    	}
    	// failure
    	return false;
    }

    public boolean initiate(IWorkPool pool) 
    {
    	if(!isInitiated())
    	{
    		try {
				m_workPool = pool;
			    Credentials creds = new UserPasswordCredentials("Operatør", "Operatør");
				m_sarSession = getSarAccessService().createSession(creds);	        
				m_sarSession.AddChangeListener(this);
				getSarAccessService().startRecvMessages();
			    // success
			    return true;
			} catch (Exception e) {
				m_logger.error("Failed to initiate dispatcher",e);
			}
    	}
        // failure
	    return false;    
    }
    
    public boolean isInitiated(IMsoModelIf aMsoModel)
    {
    	return aMsoModel!=null && m_msoModel==aMsoModel;
    }

    public boolean initiate(IMsoModelIf aMsoModel)
    {
		if(isInitiated() && aMsoModel!=null  && !isInitiated(aMsoModel) ) 
		{
		    m_msoModel = aMsoModel;
		    m_msoModel.setDispatcher(this);
		    m_isInitiated = true; // TODO: Implement several mso models
		    ensureOperationExists();
		    // success
		    return true;
		}
		
    	// failure
    	return false;
    }
    
    public List<String[]> getActiveOperations()
    {
        List<String[]> ops = new ArrayList<String[]>();
        List<SarOperation> opers = getSarSession().getOperations().getOperations();
        for (SarOperation soper : opers)
        {
            String[] descr = {IDHelper.formatOperationID(soper.getID()), soper.getID()};
            ops.add(descr);
        }
        return ops;
    }

    public String getCurrentOperationID()
    {
    	String id = null;
    	if(m_sarOperation != null)
    		id = m_sarOperation.getID();
    	return id;
    }

    public String getCurrentOperationName()
    {
    	String name = null;
    	if(m_sarOperation != null)
    		name = IDHelper.formatOperationID(getCurrentOperationID());
    	return name;
    }

    public boolean setCurrentOperation(String opID)
    {
    	// no model initiated?
    	if(!m_isInitiated) return false;
    		
    	// initialize
    	boolean bFlag = false;

        // notify
        getWorkPool().suspend();
        getMsoModel().setRemoteUpdateMode();
        getMsoModel().suspendChange();

    	try {

    		// get operation from SARA session
    		SarOperation sarOpr = getSarSession().getOperation(opID);

	        // try to clear current MSO model
	        if (clearMSO()){

		        // try to set as active operation
		        bFlag = setActiveOperation(sarOpr);
		        
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

    private boolean clearMSO()
    {

    	// no model initiated?
    	if(!m_isInitiated) return false;
    	
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
	        	for(IMsoObjectIf msoObj: m_saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoRelationIf) {
	        			if(msoObj.isDeletable()) {
	        				((AbstractMsoObject) msoObj).delete(false);
	        			}
	        		}
	        	}

	        	// delete all deleteable objects
	        	for(IMsoObjectIf msoObj: m_saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoObjectIf && !(msoObj instanceof IOperationIf)) {
	        			if(msoObj.isDeletable())
	        			((AbstractMsoObject) msoObj).delete(false);
	        		}
	        	}

	        	// delete all undeleteable objects
	        	for(IMsoObjectIf msoObj: m_saraMsoMap.values()) {
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
		    	m_saraMsoMap.clear();
		    	m_msoSaraMap.clear();

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

    private boolean setActiveOperation(SarOperation sarOpr)
    {
    	
    	// does operation not exist?
    	if(sarOpr==null) return false;
        
		// notify deactivation?
		if(m_sarOperation !=null) {
			fireOnOperationDeactivated(m_sarOperation.getID());
		}

        // save operation
        m_sarOperation = sarOpr;

    	// CREATE MSO operation
        createMsoOperation(sarOpr);

        // get copy of object
        List<SarObject> objects = new ArrayList<SarObject>(sarOpr.getObjectList());

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

        // notify activation
        fireOnOperationActivated(m_sarOperation.getID());

        // success
        return true;

    }

    private IMsoModelIf getMsoModel() {
    	return m_msoModel;
    }

    private IWorkPool getWorkPool() {
    	return m_workPool;
    }

    public boolean createNewOperation(long timeOutMillis)
    {
    	// get current time in milliseconds.
    	long tic = System.currentTimeMillis();
    	
    	// allowed to start new process?
    	if(!isCreationInProgress() 
    			|| tic-m_creationInProgress>m_creationInProgressTimeOut)
    	{
    		// set start time
    		m_creationInProgress = System.currentTimeMillis();
    		// only allow one creation in process 
	        m_creationInProgressTimeOut = timeOutMillis;
	        // start creation process
	        getSarSession().createNewOperation("MSO", true);
	        // success
	        return true;
    	}
    	// failure
        return false;
    }
    
    public boolean isCreationInProgress() 
    {
    	return m_creationInProgress>0;
    }

    public boolean finishCurrentOperation()
    {
    	if(m_sarOperation != null)
    	{
	    	
	        // If this is the only operation, shutdown and create a new one
	        boolean createNew = false;
	        if (getSarSession().getOperations().getOperations().size() == 1)
	        {
	            createNew = true;
	        }
	
	        // forward
	        getSarSession().finishOperation(m_sarOperation.getID());
	
	        // create a new operation?
	        if (createNew)
	        {
	            createNewOperation(5000);
	        }
	        
	        // success
	        return true;
	        
    	}
    	// failure
    	return false;
    }

    public void merge()
    {
        //To change body of implemented methods use File | Settings | File Templates.
        //TODO
    }

    public void shutdown()
    {
        getSarSession().shutDown();
    }

    private void createMsoOperation(SarOperation oper)
    {
    	// a model is initiated and the SAR operation exists?
    	if(m_isInitiated && oper!=null) 
    	{
	        // MSO model is only allowed to be created once from SAR operation
	        if (!getMsoModel().exists())
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
	                
	                // create root object id
	                IObjectIdIf objId = createObjectId(oper.getID(),t.getTime(),true);
	                
	                // create operation
	                getMsoModel().getMsoManager().createOperation(prefix, number,  objId);
	                
	            }
	            catch (DuplicateIdException e) // shall not happen
	            {
	                m_logger.error("Failed to create operation",e);
	            }
	        } else
	        {
	            m_logger.warn("MSO model creation from SarOperation " +
	            		"attempted on already created MSO model");
	        }
    	}
    }
    
    private void finishMsoOperation(SarOperation sarOpr)
    {
    	// check if same as current
    	String oprId = m_sarOperation.getID();
    	boolean current = (sarOpr == m_sarOperation);
    	
    	/* 
    	 * TODO: Solve the isFinished() problem. For finished operations,
    	 * this method does not return true.
    	 * 
    	 * 
    	// try to validate finished state just in case
    	boolean isFinished = (m_sarOperation instanceof Operation
    			? ((Operation)m_sarOperation).isFinished() : true);
		*/
    	
    	// is current operation?
    	if(current) 
    	{
    		m_sarOperation = null;
    		clearMSO();
    	}
    	
    	// notify
		fireOnOperationFinished(oprId,current);
		
		/* 
		 * TODO: See problem above...
		 * 
		 * 
		 * 
    	// notify
    	if(isFinished) 
    	{
    		fireOnOperationFinished(oprId,current);
    	}
    	else if(current) 
    	{
    		fireOnOperationDeactivated(oprId);
    	}
    	*/
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
        if (getSarSession().isFinishedLoading() && getSarSession().getOperations().getOperations().size() == 0)
        {
            getSarSession().createNewOperation("MSO", true);
        }

        // get commit wrapper
        ITransactionIf transaction = (ITransactionIf) e.getSource();

        // get object list
        List<IChangeRecordIf> list = transaction.getRecords();

        // prepare changed objects
        for (IChangeRecordIf it : list)
        {
            //If created, create SARA object
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
        for (IChangeIf.IChangeRelationIf it : transaction.getObjectRelationChanges())
        {
        	// forward
            msoRelationChanged(it, true);
        }

        // prepare list changes (1-to-N relations)
        for (IChangeIf.IChangeRelationIf it : transaction.getListRelationChanges())
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
        getSarSession().commit(m_sarOperation.getID());

    }

    private void msoRelationChanged(IChangeIf.IChangeRelationIf aChange, boolean isNamedRelation)
    {
        // initialize
        IMsoObjectIf ownObj = aChange.getRelatingObject();
        IMsoObjectIf refObj = aChange.getRelatedObject();
        SarObject srcObj = m_sarOperation.getSarObject(ownObj.getObjectId());
        SarObject relObj = m_sarOperation.getSarObject(refObj.getObjectId());
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
        SarSession sarSess = getSarSession();
        String className = msoObj.getClass().getName();
        if (className.indexOf("Impl") > 0)
        {
            className = className.substring(26, className.indexOf("Impl"));
        }
        String objId = msoObj.getObjectId().indexOf(".") > 0 ?
                msoObj.getObjectId().substring(msoObj.getObjectId().indexOf(".") + 1) :
                msoObj.getObjectId();
        SarObject sbo = (SarObject) sarSess.createInstance(
                className, m_sarOperation.getID(),
                SarBaseObjectFactory.TYPE_OBJECT, objId);

        // connect to operation
        sbo.setOperation(m_sarOperation);
        // update relations and attributes
        updateSaraObject(sbo, aRecord.getMsoObject(), aRecord, false);
        // finished
        return sbo;
    }

    private void updateSaraObject(IChangeRecordIf aRecord)
    {
        // get sara object
    	SarObject so = m_sarOperation.getSarObject(aRecord.getMsoObject().getObjectId());
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
        	getSarSession().commit(m_sarOperation.getID());
        }

    }

    private void deleteSaraObject(IChangeRecordIf aRecord)
    {
        // get object from operation
        SarObject soi = m_sarOperation.getSarObject(aRecord.getMsoObject().getObjectId());

    	// forward
        soi.delete("DISKO");

    }
    
    protected SarAccessService getSarAccessService()
    {
    	if(m_sarAccessService==null)
    	{
            m_sarAccessService = SarAccessService.getInstance();    		
    	}
    	return m_sarAccessService;
    }
    
    public SarSession getSarSession()
    {
    	return m_sarSession;
    }
    
    protected boolean ensureOperationExists()
    {
        if (   getSarSession().isFinishedLoading() 
            && getSarSession().getOperations().getOperations().size() == 0)
        {
            // no operations exists, create one now
            return createNewOperation(5000);
        }
        
        // NOP
        return false;
    	
    }
    
    protected IMsoObjectIf.IObjectIdIf createObjectId(String oprId, Date creationTime, boolean isRoot)
    {
    	String objId = (isRoot?oprId:oprId + "." + getSarSession().createInstanceId());
        return new AbstractMsoObject.ObjectId(objId,creationTime);
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
    	// get change source
    	Object source = change.getSource();
    	
    	// dispatch on source instance type
        if (source instanceof Operation)
        {
            switch(change.getChangeType())
            {
            case SaraChangeEvent.TYPE_ADD:
            	// cast to Operation
            	Operation sarOpr = (Operation)source;
            	
            	// get in progress flag
            	boolean bFlag = isCreationInProgress();
            	
            	// is in progress?
            	if(bFlag)
            	{
            		// get host name
            		String hostName = m_localHost.getHostName();
            		
            		// check if this is created by me
            		bFlag = sarOpr.getCreator().equalsIgnoreCase(hostName);
            		
            		// operation created by me?
            		if(bFlag)
            		{
            			// created after creation prosess started?
            			bFlag = (m_creationInProgress<=sarOpr.getStartTime()); 
            		}            		
            	}
            	
            	// is creation process finished?
                m_creationInProgress = (bFlag ? 0 : m_creationInProgress);
                
                // notify
                fireOnOperationCreated(sarOpr.getID(), bFlag);
                
                // exit switch
                break;
            case SaraChangeEvent.TYPE_REMOVE:
        		// register this occurrence
        		finishMsoOperation(change.getSarOp());
        		// exit switch
        		break;
            case SaraChangeEvent.TYPE_CHANGE:
        		// exit switch
        		break;        		
            case SaraChangeEvent.TYPE_EXCEPTION:
        		// exit switch
        		break;        		
            case SaraChangeEvent.TYPE_MESSAGE:
        		// exit switch
        		break;        		
            }
        }
        /*
        else if (source instanceof Message)
        {
        	Message msg = (Message)source;
        	if(   FROM_INTERN.equalsIgnoreCase(msg.getFrom())
        	   && TO_FINISHED.equalsIgnoreCase(msg.getTo()))
			{
        		// register this occurrence
        		finishMsoOperation(change.getSarOp());
			}        	
        }
        */
        
        if (m_sarOperation != null && change.getSarOp() == m_sarOperation)
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
				m_logger.error("Failed to schedule SARA change",e);
			}

        }
    }

    private void scheduleSaraChangeWork(SaraChangeEvent e) {
    	// get new schedule from old
    	m_schedule = new DelaySchedule(m_schedule);
    	try {
			// add to schedule
			m_schedule.add(e);
		} catch (RuntimeException ex) {
			m_logger.error("Failed to add Sara change work to schedule. This implies that " +
					"the buffer is of usufficiently large capacity with respect to the operational " +
					"requirements of the system. The buffer interval should therefore be decreased.");
		}
		// cancel schedule
		m_timer.cancel();
		// set new timer
		m_timer = new Timer();
		// schedule Sara Change Work
		m_timer.schedule(m_schedule, SARA_CHANGE_EVENT_BUFFER_DELAY);
    }

    public void saraException(SaraException sce)
    {
        //TODO videresend exception
    }

    private void removeMsoFromSara(SaraChangeEvent change)
    {

        Object co = change.getSource();
        IMsoManagerIf msoManager = getMsoModel().getMsoManager();

        if(msoManager.operationExists()) {

	        try
	        {

	            if (co instanceof SarBaseObject)
	            {
	                IMsoObjectIf msoObj = m_saraMsoMap.get(co);
	                if (msoObj != null)
	                {
	                    msoManager.remove(msoObj);
	                }
	            }
	            
	        }
	        catch (MsoException e)
	        {
	            m_logger.error("Failed to remove MSO object",e);  
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
            SarObjectImpl rel = (SarObjectImpl) m_sarOperation.getSarObject(relId);
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
                IMsoObjectIf msoObj = m_saraMsoMap.get(parentObject);
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
        IMsoObjectIf source = m_saraMsoMap.get(so);
        IMsoObjectIf relObj = m_saraMsoMap.get(rel);
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
            SarOperation soper = getSarSession().getOperation(operid);
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
        IMsoManagerIf msoMgr = getMsoModel().getMsoManager();

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
        m_saraMsoMap.put(sarObject, msoObj);
        m_msoSaraMap.put(msoObj, sarObject);

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
				getWorkPool().schedule(work);
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
			getMsoModel().setRemoteUpdateMode();
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
	                	// get source
	                	Object source = change.getSource();
	                	// dispatch on source
	                    if (source instanceof SarOperation)
	                    {
	                        createMsoOperation((SarOperation) source);
	                    }
	                    else if (source instanceof SarObject)
	                    {
	                        addMsoObject((SarObject) source);
	                    }
	                    else if (source instanceof Message)
	                    {
	                    	Message msg = (Message)source;
	                    	if(   FROM_INTERN.equalsIgnoreCase(msg.getFrom())
	                    	   && TO_FINISHED.equalsIgnoreCase(msg.getTo()))
                			{
	                    		// register this occurrence
	                    		finishMsoOperation(change.getSarOp());
                			}
	                    	else
	                    	{
		                        Log.warning("SaraChange not handled for objectType " + change.getSource().getClass().getName());	                    		
	                    	}	                    	
	                    }
	                    else
	                    {
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
			getMsoModel().restoreUpdateMode();
		}

	}

	private void fireOnOperationCreated(String oprId, boolean isLoopback) {
		for (IDispatcherListenerIf it : m_listeners) {
			it.onOperationCreated(oprId, isLoopback);
		}
	}

	private void fireOnOperationFinished(String oprId, boolean isLoopback) {
		for (IDispatcherListenerIf it : m_listeners) {
			it.onOperationFinished(oprId, isLoopback);
		}
	}

	private void fireOnOperationActivated(String oprId) {
		for (IDispatcherListenerIf it : m_listeners) {
			it.onOperationActivated(oprId);
		}
	}

	private void fireOnOperationDeactivated(String oprId) {
		for (IDispatcherListenerIf it : m_listeners) {
			it.onOperationDeactivated(oprId);
		}
	}

	@Override
	public boolean addDispatcherListener(IDispatcherListenerIf listener) {
		if(!m_listeners.contains(listener)) {
			return m_listeners.add(listener);
		}
		return false;
	}

	@Override
	public boolean removeDispatcherListener(IDispatcherListenerIf listener) {
		if(m_listeners.contains(listener)) {
			return m_listeners.remove(listener);
		}
		return false;
	}
}