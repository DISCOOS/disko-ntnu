package org.redcross.sar.modelDriver;

import no.cmr.hrs.sar.model.Fact;
import no.cmr.hrs.sar.model.Operation;
import no.cmr.hrs.sar.model.SarObjectImpl;
import no.cmr.hrs.sar.tools.ChangeObject;
import no.cmr.hrs.sar.tools.IDHelper;
import no.cmr.tools.Log;
import org.redcross.sar.app.IDiskoApplication;
import org.redcross.sar.mso.CommitManager;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.mso.committer.ICommitWrapperIf;
import org.redcross.sar.mso.committer.ICommittableIf;
import org.redcross.sar.mso.committer.ICommittableIf.ICommitObjectIf;
import org.redcross.sar.mso.data.*;
import org.redcross.sar.mso.event.IMsoCommitListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.thread.AbstractDiskoWork;
import org.redcross.sar.thread.DiskoWorkPool;
import org.redcross.sar.util.except.CommitException;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoException;
import org.rescuenorway.saraccess.api.*;
import org.rescuenorway.saraccess.except.SaraException;
import org.rescuenorway.saraccess.model.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;


/**
 * For documentation, see {@link  IModelDriverIf}
 */
public class SarModelDriver implements IModelDriverIf, IMsoCommitListenerIf, SaraChangeListener
{
	
	static int SARA_CHANGE_EVENT_BUFFER_DELAY = 2000;
	
    boolean loadingOperation = false;
    Random m_rand = new Random(89652467667623L);
    SarAccessService sarSvc;
    SarOperation sarOperation = null;
    Map<SarBaseObject, IMsoObjectIf> saraMsoMap = new HashMap<SarBaseObject, IMsoObjectIf>();
    Map<IMsoObjectIf, SarBaseObject> msoSaraMap = new HashMap<IMsoObjectIf, SarBaseObject>();

    List<IModelDriverListenerIf> listeners = new ArrayList<IModelDriverListenerIf>();
    
    long saraChangeTic = System.currentTimeMillis();
    
    boolean initiated = false;
    private IDiskoApplication diskoApp;
    
    Timer timer = new Timer();
    DelaySchedule schedule = null;
    
    DiskoWorkPool m_workPool;
    IMsoModelIf m_msoModel;

    public SarModelDriver()
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
        setUpService();
        IMsoModelIf imm = MsoModelImpl.getInstance();
        imm.getEventManager().addCommitListener(this);
        initiated = true;
        if (sarSvc.getSession().isFinishedLoading() 
        		&& sarSvc.getSession().getOperations().getOperations().size() == 0)
        {
            loadingOperation = true;
            sarSvc.getSession().createNewOperation("MSO", true);
        }
    }

    public boolean isInitiated()
    {
        if (!loadingOperation && sarSvc.getSession().isFinishedLoading() && sarSvc.getSession().getOperations().getOperations().size() == 0)
        {
            loadingOperation = true;
            sarSvc.getSession().createNewOperation("MSO", true);
        }
        return sarSvc.getSession().isFinishedLoading() && !loadingOperation;
    }

    public List<String[]> getActiveOperations()
    {
        List<String[]> ops = new ArrayList<String[]>();
        List<SarOperation> opers = sarSvc.getSession().getOperations().getOperations();
        for (SarOperation soper : opers)
        {
            String[] descr = {soper.getName(), soper.getID()};
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
        getMsoModel().suspendClientUpdate();    		    	
		
    	try {

    		// reset current operation
    		sarOperation = null;
    		
    		// get operation from SARA session
    		SarOperation soper = sarSvc.getSession().getOperation(opID);
    		
	        // try to clear current MSO model
	        if (clearMSO(false)){
	        	
		        // try to set as active operation
		        if (setActiveOperation(soper)) {
	        
			        // save operation
			        sarOperation = soper;
			        
			        // success
			        bFlag = true;
		        }
	        }
	        
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	// resume old modes
    	getMsoModel().resumeClientUpdate();
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
    		name = sarOperation.getName();
    	return name;
    }
    
    private boolean clearMSO(boolean suspend)
    {
        
        // initialize
        boolean success = false;
        
        // suspend?
        if(suspend) {
	        getWorkPool().suspend();
	        getMsoModel().suspendClientUpdate();
        }
        
        // set remote update mode
        getMsoModel().setRemoteUpdateMode();
        
        try {
    		
	    	// get manager
	        IMsoManagerIf msoManager = MsoModelImpl.getInstance().getMsoManager();
	        
	        // get operation?
	    	IOperationIf opr = msoManager.operationExists() ? msoManager.getOperation() : null;
	    	
	    	// has operation?
	    	if(opr!=null) {
	    		
	        	// delete all deleteable references
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoReferenceIf) {
	        			if(msoObj.canDelete()) {
	        				((AbstractMsoObject) msoObj).doDelete();
	        			}
	        		}
	        	}
	        	
	        	// delete all deleteable objects
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj instanceof IMsoObjectIf && !(msoObj instanceof IOperationIf)) {
	        			if(msoObj.canDelete())
	        			((AbstractMsoObject) msoObj).doDelete();
	        		}
	        	}
	
	        	// delete all undeleteable objects
	        	for(IMsoObjectIf msoObj: saraMsoMap.values()) {
	        		if(msoObj!=null && !msoObj.hasBeenDeleted() && !(msoObj instanceof IOperationIf))
	        			((AbstractMsoObject) msoObj).doDelete();
	        	}
	        	        	
		    	// delete operation?
		    	if(opr!=null)  opr.delete();
		    	
		    		
		    	// clear maps
		    	saraMsoMap.clear();	    	
		    	msoSaraMap.clear();
		    	
		    	// do garbage collection
		    	Runtime.getRuntime().gc();
		    	
	        	// notify all listeners?
		    	if(opr!=null) {
		    		MsoModelImpl.getInstance().getEventManager().notifyClearAll(opr);
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
        
    	// resume?
    	if(suspend) {
        	// resume old modes
        	getMsoModel().resumeClientUpdate();
        	getWorkPool().resume();	                                	
    	}

    	// return state
        return success;
        
    }

    private boolean setActiveOperation(SarOperation soper)
    {
        
        //CREATE MSO operation
        createMsoOperation(soper);
        
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
                updateMsoReference(so, (SarObjectImpl) entry.getValue(), entry.getKey(), SarBaseObjectImpl.ADD_NAMED_REL_FIELD);
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
                        SarObjectImpl sarBaseObject = (SarObjectImpl) entry.getValue().get(i);
                        //    SarBaseObject sarBaseObject =  entry.getValue().get(i);

                        updateMsoReference(so, sarBaseObject, entry.getKey(), SarBaseObjectImpl.ADD_REL_FIELD);
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
			m_msoModel = MsoModelImpl.getInstance();
    	}
    	return m_msoModel;
    }
    
    private DiskoWorkPool getWorkPool() {
    	if(m_workPool==null) {
    		try {
				m_workPool = DiskoWorkPool.getInstance();
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
        loadingOperation = true;
        sarSvc.getSession().createNewOperation("MSO", true);
    }

    public void merge()
    {
        //To change body of implemented methods use File | Settings | File Templates.
        //TODO
    }

    public void setDiskoApplication(IDiskoApplication aDiskoApp)
    {
        diskoApp = aDiskoApp;
        addModelDriverListener(aDiskoApp);
    }

    public void shutDown()
    {
        sarSvc.getSession().shutDown();
    }

    private void createMsoOperation(SarOperation oper)
    {
        // only one active MSO operation allowed 
        IMsoManagerIf msoManager = MsoModelImpl.getInstance().getMsoManager();
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
                // get creation date
                List periods = ((Operation)oper).getActivePeriods();
            	Calendar c = Calendar.getInstance();
                if(periods.size()>0)
                	c.setTimeInMillis(Long.valueOf(periods.get(0).toString()));
                	
                IMsoObjectIf.IObjectIdIf operid = new AbstractMsoObject.ObjectId(oper.getID(),c.getTime());
                msoManager.createOperation(prefix, number, operid);
                sarOperation = oper;
//                MsoModelImpl.getInstance().restoreUpdateMode();
                //TODO Opprett of map inn data

            }
            catch (DuplicateIdException e) // shall not happen
            {
                e.printStackTrace();
            }
        } else
        {
            //Hendelse er allerede opprettet, hva nå
        }
    }

    /*
    private Map<String, Integer> loopbackIds = Collections.synchronizedMap(new HashMap<String, Integer>());
    
    private void setLoopback(String id, boolean register) {
    	// TODO: Fix the loopback problem
    	if(register) {
    		if(loopbackIds.containsKey(id)) {
    			int count = loopbackIds.get(id);
    			loopbackIds.put(id,count+1);
    			//System.out.println("setLoopback:="+id+":"+(count+1));
    		}
    		else {
    			loopbackIds.put(id,1);
    			//System.out.println("setLoopback:="+id+":"+1);
    		}
    	}
    	else {
    		if(loopbackIds.containsKey(id)) {
    			int count = loopbackIds.get(id);
    			if(count>1) {
    				loopbackIds.put(id,count-1);
        			//System.out.println("setLoopback:="+id+":"+(count-1));
    			}
    			else {
    				loopbackIds.remove(id);
        			//System.out.println("setLoopback:="+id+":removed");
    			}
    		}
    	}
    }
    private boolean isLoopback(String id) {
    	return loopbackIds.containsKey(id);
    }
    */

    private HashMap<String, SarObject> commitObjects = new HashMap<String, SarObject>();

    public void handleMsoCommitEvent(MsoEvent.Commit e) throws CommitException
    {
    	
    	// prepare
        commitObjects.clear();
        
        //Check that operation is added
        if (sarSvc.getSession().isFinishedLoading() && sarSvc.getSession().getOperations().getOperations().size() == 0)
        {
            sarSvc.getSession().createNewOperation("MSO", true);
        }
        
        //sarSvc.getSession().beginCommit(arg0)Commit(sarOperation.getID());
        // Iterer gjennom objektene, sjekk type og oppdater sara etter typen
        ICommitWrapperIf wrapper = (ICommitWrapperIf) e.getSource();
        List<ICommitObjectIf> objectList = wrapper.getObjects();
        for (ICommitObjectIf ico : objectList)
        {
            //IF created, create SARA object
            if (ico.getType().equals(CommitManager.CommitType.COMMIT_CREATED))
            {
                SarObject so = createSaraObject(ico);
                commitObjects.put(so.getID(), so);
                so.createNewOut();
            } else if (ico.getType().equals(CommitManager.CommitType.COMMIT_MODIFIED))
            {
                // if modified, modify SaraObject.
                updateSaraObject(ico);
            }
        }

        List<ICommittableIf.ICommitReferenceIf> attrList = wrapper.getListReferences();
        for (ICommittableIf.ICommitReferenceIf ico : attrList)
        {
            msoReferenceChanged(ico, false);
        }

        List<ICommittableIf.ICommitReferenceIf> listList = wrapper.getAttributeReferences();
        for (ICommittableIf.ICommitReferenceIf ico : listList)
        {
        	// forward
            msoReferenceChanged(ico, true);
        }
        //Handle delete object last
        for (ICommitObjectIf ico : objectList)
        {
            if (ico.getType().equals(CommitManager.CommitType.COMMIT_DELETED))
            {
                // if deleted remove Sara object
                deleteSaraObject(ico);
            }
        }
        sarSvc.getSession().commit(sarOperation.getID());
        
    }

    private void msoReferenceChanged(ICommittableIf.ICommitReferenceIf ico, boolean isNamedReference)
    {
        // initialize
    	CommitManager.CommitType ct = ico.getType(); //CommitManager.CommitType.COMMIT_CREATED/CommitManager.CommitType.COMMIT_DELETED
        IMsoObjectIf owner = ico.getReferringObject();
        IMsoObjectIf ref = ico.getReferredObject();
        SarObject sourceObj = sarOperation.getSarObject(owner.getObjectId());
        SarObject relObj = sarOperation.getSarObject(ref.getObjectId());        
        String refName = ico.getReferenceName();
        if (sourceObj == null)
        {
            sourceObj = commitObjects.get(owner.getObjectId());
        }
        if (relObj == null)
        {
            relObj = commitObjects.get(ref.getObjectId());
        }
        if (sourceObj == null || relObj == null)
        {
            Log.warning("Object not found " + owner.getObjectId() + " or " + ref.getObjectId());
        } else
        {
        	// get id
        	String id = sourceObj.getId();
        	// dispatch
            if (ct.equals(CommitManager.CommitType.COMMIT_CREATED))
            {
            	// register loopback id
            	//setLoopback(id,true);
            	// add relation
                if (isNamedReference)
                {
                    sourceObj.addNamedRelation(refName, relObj);
                } else
                {
                    sourceObj.addRelation(refName, relObj);
                }
            } else if (ct.equals(CommitManager.CommitType.COMMIT_MODIFIED))
            {
                //TODO skal dette kunne skje??
                Log.warning("-------------Modify relation-----------");
            } else if (ct.equals(CommitManager.CommitType.COMMIT_DELETED))
            {
            	// register loopback id
            	//setLoopback(id,true);
            	// add relation
            	if (isNamedReference)
                {
                    sourceObj.removeNamedRelation(refName);
                } else
                {
                    sourceObj.removeRelation(refName, relObj);
                }
            }
        }
    }

    private SarObject createSaraObject(ICommitObjectIf commitObject)
    {
        IMsoObjectIf msoObj = commitObject.getObject();
        msoObj.getMsoClassCode();
        //Finn Saras mappede objekttype
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

        //TODO sett attributter og tilordne til hendelse
        sbo.setOperation(sarOperation);
        updateSaraObject(sbo, commitObject.getObject(), commitObject.getPartial(), false);
        //Opprett instans av av denne og distribuer
        return sbo;
    }

    private void updateSaraObject(ICommitObjectIf commitObject)
    {
        // get sara object
    	SarObject soi = sarOperation.getSarObject(commitObject.getObject().getObjectId());
        // ensure that this change is submitted to all listeners before any references is updated
        updateSaraObject(soi, commitObject.getObject(), commitObject.getPartial(), true);
    }

    private void updateSaraObject(SarObject sbo, IMsoObjectIf msoObj, List<IAttributeIf> partial, boolean submitChanges)
    {
    	// initialize
        SarSession sarSess = sarSvc.getSession();
        Map attrMap = msoObj.getAttributes();
        Map relMap = msoObj.getReferenceObjects();
        List<SarBaseObject> objs = sbo.getObjects();
        
        // loop over all objects in sara object 
        for (SarBaseObject so : objs)
        {
            try
            {
            	// is attribute object?
                if (so instanceof SarFact)
                {
                    // Map fact to attribute
                    String attrName = ((SarFact) so).getLabel();
                    IAttributeIf msoAttr = (IAttributeIf) attrMap.get(attrName.toLowerCase());
                    // only update fact if this is not a partial update, or if attribute 
                    // is included in the partial update
                    if(partial.size()==0 || partial.contains(msoAttr)) {
	                    // found attribute?
	                    if (msoAttr != null)
	                    {
	                        SarMsoMapper.mapMsoAttrToSarFact((SarFact) so, msoAttr, submitChanges);
	                    } else if (!attrName.equalsIgnoreCase("Objektnavn"))
	                    {
	                        Log.warning("Attribute " + attrName + " not found for " + sbo.getName());
	                    }
                    }
                // reference attributes (which is objects them selves) are
                // only updated if this is a full update operation
                } else if (partial.size()==0)
                {
                    if(so instanceof SarObject) {
	                	String objName = ((SarObject) so).getName();
	                    IMsoObjectIf refAttr = (IMsoObjectIf) relMap.get(objName);
	                    if (refAttr != null)
	                    {	                    	
	                    	// reuse partial list, it is empty anyway...
	                        updateSaraObject((SarObject) so, refAttr, partial, submitChanges);
	                    }
                    }
                }
            }
            catch (Exception e)
            {
                Log.error("Unable to map to Sara :(sarObj:" + so.getValueAsString() + ",msoObj:" + msoObj.getClass().getName() + e.getMessage());
            }
        }
    	
        // register loopback for commit
    	//setLoopback(sbo.getId(),true);
    	
        if (submitChanges)
        {
        	// commit this change
            sarSess.commit(sarOperation.getID());
        }
        
    }

    private void deleteSaraObject(ICommitObjectIf commitObject)
    {
        //Finn mappet objekt
        SarObject soi = sarOperation.getSarObject(commitObject.getObject().getObjectId());
    	// register loopback id
    	//setLoopback(soi.getId(),true);        
    	// forward
        soi.delete("DISKO");

        //Send slette melding
    }

    public void setUpService()
    {
        sarSvc = SarAccessService.getInstance();
        Credentials creds = new UserPasswordCredentials("Operatør", "Operatør");
        SarSession s = null;
        try
        {
            s = sarSvc.createSession(creds);
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
    
    //---------------SaraChangeListener-----------------------------
    public void saraChanged(final SaraChangeEvent change)
    {
        if (change.getSource() instanceof SarOperation)
        {
            if (change.getChangeType() == SaraChangeEvent.TYPE_ADD)
            {
            	boolean current = loadingOperation;
                loadingOperation = false;
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
    	// add to schedule
    	schedule.add(e);
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
//End---------------SaraChangeListener-----------------------------

    private void removeInMsoFromSara(SaraChangeEvent change)
    {

        Object co = change.getSource();
        IMsoManagerIf msoManager = MsoModelImpl.getInstance().getMsoManager();

        if(msoManager.operationExists()) {
        
	        try
	        {
	
	            if (co instanceof SarOperation)
	            {
	            	String oprId = sarOperation.getID();
	            	boolean current = (co == sarOperation);
	            	if(current) {
	            		sarOperation = null;
	            		clearMSO(true);
	            	}
	                fireOnOperationFinished(oprId,current);
	            } else
	            {
	                IMsoObjectIf source = saraMsoMap.get(co);
	                if (source != null)
	                {
	                    msoManager.remove(source);
	                }
	            }
	        }
	        catch (MsoException e)
	        {
	            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	        }
        }

//      if (co.getFactType() == Fact.FACTTYPE_RELATION)
//      {
//         SarObjectImpl so = (SarObjectImpl) change.getParent();
//         String relId = ((String[]) co.getToObject())[1];
//         String relName = ((String[]) co.getToObj())[0];
//         SarObjectImpl rel = (SarObjectImpl) sarOperation.getSarObject(relId);
//
//         IMsoObjectIf source = saraMsoMap.get(so);
//         IMsoObjectIf relObj = saraMsoMap.get(rel);
//         //Change in relations
//         //Get type relation change
//         if (co.getFieldName().equalsIgnoreCase(SarBaseObjectImpl.REM_REL_FIELD))
//         {
//            source.addObjectReference(relObj, null);
//         }
//         else if (co.getFieldName().equalsIgnoreCase(SarBaseObjectImpl.REM_NAMED_REL_FIELD))
//         {
//            IMsoReferenceIf refObj = (IMsoReferenceIf) source.getReferenceObjects().get(relName);
//            refObj.setReference(null);
//         }
//      }
//      else
//      {
//         SarBaseObject so = change.getParent();
//         if (so instanceof SarObjectImpl)
//         {
//            //Find object and remove in mso
//            IMsoManagerIf msoMgr = MsoModelImpl.getInstance().getMsoManager();
//
//            IMsoObjectIf msoObj = saraMsoMap.get(so);
//            try
//            {
//               boolean result = msoMgr.remove(msoObj);
//               //Vinjar: Hva dersom remove ikke ok??
//            }
//            catch (MsoNullPointerException e)
//            {
//               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//            //TODO avsjekk med vinjar om dette er nok
//
//         }
//         else
//         {
//            //Change of factvalue
//            Log.warning("NOT IMPLEMENTED YET deleteMsoFromSara field: " + co.getFieldType() + " ftype: " + co.getFactType());
//
//         }

//      }
    }

    private void changeMsoFromSara(SaraChangeEvent change)
    {
        SarBaseObject so = change.getParent();
        ChangeObject co = (ChangeObject) change.getSource();

        if (co.getFactType() == Fact.FACTTYPE_RELATION)
        {
        	
        	String[] toObject = (String[])co.getToObject();

        	/*
        	if(toObject.length<2)
        		System.out.println("FACTTYPE_RELATION::"+toObject[0]+".NULL");
        	else
        		System.out.println("FACTTYPE_RELATION::"+toObject[0]+"."+toObject[1]);
        	*/
        	
        	
            String relId = toObject[1];
            String relName = ((String[]) co.getToObj())[0];
            SarObjectImpl rel = (SarObjectImpl) sarOperation.getSarObject(relId);

            updateMsoReference(so, rel, relName, co.getFieldName());
            
        } else
        {
            //Change of factvalue
            //Find object containing the fact
            SarObject parentObject = getParentObject(so);
            // Use object to find msoobject
            AttributeImpl attr = null;
            if (parentObject != null)
            {
                IMsoObjectIf msoObj = saraMsoMap.get(parentObject);
                if(msoObj==null)
            		System.out.println("ERROR::"+parentObject.getName());                	
                Map attrs = msoObj.getAttributes();
                attr = (AttributeImpl) attrs.get(((SarFact) so).getLabel().toLowerCase());
                if (attr != null)
                {
                    SarMsoMapper.mapSarFactToMsoAttr(attr, (SarFact) so);
                }

            }
            //Update msoobject
            if (parentObject == null || attr == null)
            {
                Log.warning("NOT IMPLEMENTED YET changeMsoFromSara field: " + co.getFieldType() + " ftype: " + co.getFactType());
            }
        }
    }

    private void updateMsoReference(SarBaseObject so, SarObjectImpl rel, String relName, String fieldName)
    {
        IMsoObjectIf source = saraMsoMap.get(so);
        IMsoObjectIf relObj = saraMsoMap.get(rel);
        //Change in relations
        //Get type relation change
        if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.ADD_REL_FIELD))
        {
            try
            {
                source.addObjectReference(relObj, relName);
            }
            catch (DuplicateIdException die)
            {
                //Do nothing will occure when object is created by this client
            }
        } else if (fieldName.equalsIgnoreCase(SarBaseObjectImpl.ADD_NAMED_REL_FIELD))
        {

            IMsoReferenceIf refObj = (IMsoReferenceIf) source.getReferenceObjects().get(relName.toLowerCase());
            if (refObj == null)
            {
                source.getReferenceObjects().put(relName, relObj);
            } else
            {

                refObj.setReference(relObj);
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

//    private IMsoObjectIf getMsoObject(SarObject sarObject)
//    {
//        IMsoObjectIf msoObj=null;
//        //Bruker reflection og navn til  å opprtette masomanagermetode ok kall createXXX foroppretting
//        //bruk den med idparameter
//        //tilordne msoObj fra createmetode
//        String methodName="get"+sarObject.getName()+"List";
//        IMsoManagerIf msoMgr = MsoModelImpl.getInstance().getMsoManager();
//        if(sarObject.getName().equals("CmdPost"))
//        {
//            return msoMgr.getCmdPost();
//        }
//
//        Method m=null;
//        try {
//
//            m=msoMgr.getCmdPost().getClass().getMethod(methodName,IMsoObjectIf.IObjectIdIf.class);
//            m.setAccessible(true);
//            IMsoObjectIf.IObjectIdIf id=new AbstractMsoObject.ObjectId(sarObject.getID());
//            msoObj =((IMsoListIf) m.invoke(msoMgr.getCmdPost(), new Object[]{id})).getItem(sarObject.getID());
//
//            // Handle any exceptions thrown by method to be invoked.
//        }
//        catch (Exception x)
//        {
//            Log.warning(x.getMessage());
//        }
//        return msoObj;
//
//    }


    protected void addMsoObject(SarObject sarObject)
    {
        //String name = "";
        //name=sarObject.getName();
        IMsoManagerIf msoMgr = MsoModelImpl.getInstance().getMsoManager();

        IMsoObjectIf msoObj = null;
        //Bruker reflection og navn til  å opprtette masomanagermetode ok kall createXXX foroppretting
        //bruk den med idparameter
        //tilordne msoObj fra createmetode
        String methodName = "create" + sarObject.getName();
        
        //if(methodName.equals("createPersonnel"))
        //	System.out.println();
        
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
        	msoObj.setCreated(creationTime);
        }
        
        //System.out.println("getCreationDate:="+((SarObjectImpl)sarObject).getCreationDate());
        
        setMsoObjectValues(sarObject, msoObj);
        saraMsoMap.put(sarObject, msoObj);
        msoSaraMap.put(msoObj, sarObject);

    }

    public static void setMsoObjectValues(SarObject sarObject, IMsoObjectIf msoObj)
    {
        for (SarBaseObject fact : sarObject.getObjects())
        {
            if (fact instanceof SarFact)
            {
                try
                {
                	if(msoObj==null)
                		System.out.println("ERROR::setMsoObjectValues(msoObj:=null)");
                    Map attrs = msoObj.getAttributes();              
                    AttributeImpl attr = (AttributeImpl) attrs.get(((SarFact) fact).getLabel().toLowerCase());
                    if (attr != null)
                    {
                        SarMsoMapper.mapSarFactToMsoAttr(attr, (SarFact) fact);
                    }
                    //msoObj.setAttribute(((SarFact)fact).getLabel().toLowerCase(),((SarFact)fact).getValue());
                }
//               catch (UnknownAttributeException e)
//               {
//                  Log.printStackTrace("Unable to map "+sarObject.getName());
//               }
                catch (Exception npe)
                {
                    try
                    {
                        Log.warning("Attr not found " + ((SarFact) fact).getLabel() + " for msoobj " + msoObj.getMsoClassCode() + "\n" + npe.getMessage());
                    }
                    catch (Exception e)
                    {
                        Log.printStackTrace(e);
                    }
                }
                //TODO implementer
            } else
            {
                //TODO handle internal object attributes
            }
        }
        //       MsoModelImpl.getInstance().commit();
        //MsoModelImpl.getInstance().restoreUpdateMode();
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
        		// schedule on work pool
				DiskoWorkPool.getInstance().schedule(
						new SaraChangeWork(new ArrayList<SaraChangeEvent>(changes)));
				// cleanup
				changes.clear();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}    					
		}
    	
    }
    
	class SaraChangeWork extends AbstractDiskoWork<Void> {
		
		private List<SaraChangeEvent> changes = null;
		
		public SaraChangeWork(List<SaraChangeEvent> changes) throws Exception {
			// forward
			super(false,true,WorkOnThreadType.WORK_ON_SAFE,
					"Behandler endring",100,true,true,false,0);
			// save event
			this.changes = changes;
 		}
		
		protected void beforePrepare() {
        	// set remote update mode
			MsoModelImpl.getInstance().setRemoteUpdateMode();
		}
		
		/** 
		 * Worker
		 * 
		 * Notifies the model of change in worker thread (system modal)
		 */
		public Void doWork() {
        	// catch errors and log them
            try
            {
            	// loop over all changes
            	for(SaraChangeEvent change : changes) {
            		
	            	// do the update
	                if (change.getChangeType() == SaraChangeEvent.TYPE_ADD)
	                {
	                    if (change.getSource() instanceof SarOperation){
	                        createMsoOperation((SarOperation) change.getSource());
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
	                    removeInMsoFromSara(change);
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
			MsoModelImpl.getInstance().restoreUpdateMode();
		}
		
	}
	
	private void fireOnOperationCreated(final String oprId, final boolean current) {
		if (SwingUtilities.isEventDispatchThread()) {
			for (IModelDriverListenerIf it : listeners) {
				it.onOperationCreated(oprId, current);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnOperationCreated(oprId, current);
				}
			});
		}
	}
	
	private void fireOnOperationFinished(final String oprId, final boolean current) {
		if (SwingUtilities.isEventDispatchThread()) {
			for (IModelDriverListenerIf it : listeners) {
				it.onOperationFinished(oprId, current);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireOnOperationFinished(oprId, current);
				}
			});
		}
	}

	@Override
	public boolean addModelDriverListener(IModelDriverListenerIf listener) {
		if(!listeners.contains(listener)) {
			return listeners.add(listener);
		}
		return false;
	}

	@Override
	public boolean removeModelDriverListener(IModelDriverListenerIf listener) {
		if(listeners.contains(listener)) {
			return listeners.remove(listener);
		}
		return false;
	}    
}