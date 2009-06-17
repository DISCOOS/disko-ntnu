package org.redcross.sar.mso.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeRelationIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoObjectIf.IObjectIdIf;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.except.DuplicateIdException;
import org.redcross.sar.util.except.MsoRuntimeException;
import org.redcross.sar.util.except.TransactionException;

public class MsoListImpl<M extends IMsoObjectIf> implements IMsoListIf<M>, IMsoObjectHolderIf
{
	/**
	 * Logger for all MsoListImpl objects
	 */
	protected static Logger m_logger;

	/**
	 * The list name used to identify the one-to-many relation
	 */
	protected String m_name;

	/**
	 * The list owner
	 */
	protected final AbstractMsoObject m_owner;

	/**
	 * The relations existing remotely 
	 */
	protected final HashMap<String, IMsoRelationIf<M>> m_created;

	/**
	 * The relations added locally
	 */
	protected final HashMap<String, IMsoRelationIf<M>> m_added;

	/**
	 * The relations deleted locally
	 */
	protected final HashMap<String, IMsoRelationIf<M>> m_deleted;

	/**
	 * The relations pending deletion locally. This list store deleted 
	 * relations until resumeClientUpdate() is called.
	 */
	protected final HashMap<IMsoObjectIf, IMsoRelationIf<M>> m_deleting;

	/**
	 * The owner to objects relation cardinality (0,1,..,n,...*)
	 */
	protected final int m_cardinality;

	/**
	 * If <code>true</code>, the list is the owner of related objects. 
	 * When relations to objects in main lists is deleted, the objects are
	 * also deleted.
	 */
	protected final boolean m_isMain;

	/**
	 * The object class. Used to collect items based on object class.
	 */
	protected final Class<M> m_objectClass;

	/**
	 * The MSO model owning the objects.
	 */
	protected final IMsoModelIf m_model;

	/**
	 * The change count since initialization
	 */
	protected int m_changeCount;

	/* =========================================================
	 * Constructors
	 * ========================================================= */

	public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner)
	{
		this(theObjectClass,anOwner, "");
	}

	public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName)
	{
		this(theObjectClass,anOwner, theName, false);
	}

	public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName, boolean isMain)
	{
		this(theObjectClass, anOwner, theName, isMain, 0, 50);
	}

	public MsoListImpl(Class<M> theObjectClass, IMsoObjectIf anOwner, String theName, boolean isMain, int cardinality, int aSize)
	{
		if(!(anOwner instanceof AbstractMsoObject))
		{
			throw new IllegalArgumentException("MsoListImpl must have a AbstractMsoObject owner");
		}

		// initialize logger?
		if(m_logger==null) m_logger = Logger.getLogger(getClass());

		// initialize object
		m_owner = (AbstractMsoObject)anOwner;
		m_name = theName;
		m_model = m_owner.getModel();
		m_isMain = isMain;
		m_cardinality = cardinality;
		m_created = new LinkedHashMap<String, IMsoRelationIf<M>>(aSize);
		m_added = new LinkedHashMap<String, IMsoRelationIf<M>>(aSize);
		m_deleted = new LinkedHashMap<String, IMsoRelationIf<M>>(aSize);
		m_deleting = new LinkedHashMap<IMsoObjectIf, IMsoRelationIf<M>>(aSize);
		m_objectClass = theObjectClass;
	}

	/* =========================================================
	 * IMsoListIf implementation
	 * ========================================================= */

	public String getName()
	{
		return m_name;
	}

	@Override
    public IMsoObjectIf getOwnerObject() 
    {
    	return m_owner;
    }
    
	@Override
	public boolean isOriginLocal() {
		return isOrigin(IData.DataOrigin.LOCAL);
	}

	@Override
	public boolean isOriginRemote() {
		return isOrigin(IData.DataOrigin.REMOTE);
	}

	@Override
	public boolean isOriginConflict() {
		return false;
	}

	public boolean isOriginMixed() {
		return isOrigin(IData.DataOrigin.MIXED);
	}    

	@Override
	public boolean isOrigin(IData.DataOrigin origin) {
		if(origin!=null)
		{
			return origin.equals(getOrigin());
		}
		return false;
	}

	@Override
	public IData.DataOrigin getOrigin() {

		/* Since CONFLICT origin for one-to-many relations
		 * are not defined (requires that each relation
		 * is defined by it's index), only LOCAL and REMOTE
		 * date origins are checked. */

		IData.DataOrigin origin = IData.DataOrigin.NONE;

		for (M it : getAllItems())
		{
			IData.DataOrigin o = IData.DataOrigin.NONE;
			if(isOriginLocal(it))
			{
				o = IData.DataOrigin.LOCAL;
			}
			else if(isOriginRemote(it)) 
			{
				o = IData.DataOrigin.REMOTE;
			}
			if(!origin.equals(o)) 
			{
				if(!origin.equals(IData.DataOrigin.NONE))
				{
					return IData.DataOrigin.MIXED;
				}
				origin = o;
			}
		}

		// all data has same origin
		return origin;

	}
	
	public boolean isAdded(IMsoObjectIf anObject)
	{
		if(anObject!=null) {
			return m_added.containsKey(anObject.getObjectId());
		}
		return false;
		
	}

	public boolean isOriginLocal(IMsoObjectIf anObject) 
	{
		return isAdded(anObject) || isDeleted(anObject);
	}

	public boolean isOriginRemote(IMsoObjectIf anObject) 
	{
		if(anObject!=null) {
			return m_created.containsKey(anObject.getObjectId());
		}
		return false;
	}

	public boolean exists(IMsoObjectIf anObject)
	{
		return (m_created.containsKey(anObject.getObjectId()) || m_added.containsKey(anObject.getObjectId()));
	}

	public boolean contains(IMsoObjectIf anObject)
	{
		return exists(anObject) || isDeleted(anObject);
	}
	
	public boolean isOrigin(M anObject, IData.DataOrigin origin) 
	{
		if(origin!=null) 
		{
			return origin.equals(getOrigin(anObject));
		}
		return false;		
	}

	public IData.DataOrigin getOrigin(M anObject)
	{
		if (isOriginRemote(anObject))
		{
			return IData.DataOrigin.REMOTE;
		}
		if (isOriginLocal(anObject))
		{
			return IData.DataOrigin.LOCAL;
		}
		return IData.DataOrigin.NONE;
	}

	public boolean isChanged() {
		return m_added.size()>0 || m_deleted.size()>0 || m_deleting.size()>0;
	}
	
	public boolean isDeleted() 
	{
		return m_owner.isDeleted();
	}

	public boolean isChangedSince(int changeCount)
	{
		return (m_changeCount>=changeCount);
	}

	public int getChangeCount()
	{
		return m_changeCount;
	}

	@Override
	public boolean isLoopbackMode() {
		for (IMsoRelationIf<M> relation : getRelations())
		{
			if(!relation.isLoopbackMode()) return false;
		}
		return true;
	}	

	@Override
	public boolean isRollbackMode() {
		for (IMsoRelationIf<M> relation : getRelations())
		{
			if(!relation.isRollbackMode()) return false;
		}
		return true;
	}		
	
	@Override
	public boolean isState(DataState state) {
		if(state!=null)
		{
			return state.equals(getState());
		}
		return false;
	}

	@Override
	public DataState getState() {
		DataState state = DataState.NONE;
		if(isOriginMixed())
		{
			return DataState.MIXED;
		}
		else if(isChanged())
		{
			state = DataState.CHANGED;			
		}
		else 
		{
			if(isRollbackMode())
			{
				state = DataState.ROLLBACK;
			}
			else if(isOriginRemote() || isLoopbackMode())
			{
				state = DataState.LOOPBACK;
			} 
		}
		// mixed state?
		if(isDeleted() && !state.equals(DataState.NONE)) 
		{
			// finished
			return DataState.MIXED;
		}
		
		// finished
		return state;
	}
	
	public IMsoObjectIf getOwner()
	{
		return m_owner;
	}

	public boolean isMain()
	{
		return m_isMain;
	}

	public int size()
	{
		return m_created.size() + m_added.size();
	}

	public M getHeadObject()
	{
		IMsoRelationIf<M> refObj = getHeadRelation();
		return refObj!=null?refObj.get():null;
	}

	public M getObject(IObjectIdIf anObjectId)
	{
		return getObject(anObjectId.getId());
	}

	public M getObject(String anObjectId)
	{
		IMsoRelationIf<M> refObj = getRelation(anObjectId);
		return refObj!=null?refObj.get():null;
	}

	public Collection<M> getObjects()
	{
		HashSet<M> retVal = new HashSet<M>(size());
		retVal.addAll(getItems(m_created.values()));
		retVal.addAll(getItems(m_added.values()));
		return retVal;
	}



	@Override
	public IMsoRelationIf<M> getHeadRelation() {
		Iterator<IMsoRelationIf<M>> iterator = getRelations().iterator();
		if (iterator.hasNext())
		{
			return iterator.next();
		}
		return null;
	}

	@Override
	public IMsoRelationIf<M> getRelation(M anObject) {
		return getRelation(anObject.getObjectId());
	}

	@Override
	public IMsoRelationIf<M> getRelation(IObjectIdIf anObjectId) {
		return getRelation(anObjectId.getId());
	}

	@Override
	public IMsoRelationIf<M> getRelation(String anObjectId) {
		IMsoRelationIf<M> refObj = m_created.get(anObjectId);
		if (refObj == null)
		{
			refObj = m_added.get(anObjectId);
		}
		return refObj;
	}

	@Override
	public Collection<IMsoRelationIf<M>> getRelations() {
		HashSet<IMsoRelationIf<M>> retVal = new HashSet<IMsoRelationIf<M>>(size());
		retVal.addAll(m_created.values());
		retVal.addAll(m_added.values());
		return retVal;
	}

	public int getCardinality()
	{
		return m_cardinality;
	}

	public Object validate() {
		if(m_cardinality>0) {
			return (size()<m_cardinality);
		}
		for(IMsoObjectIf it : getObjects()) {
			Object retVal = it.validate();
			if(!isTrue(retVal)) return retVal;
		}
		return true;
	}

	public boolean isDeleted(IMsoObjectIf anObject) {
		if(anObject!=null) {
			return m_deleted.containsKey(anObject.getObjectId()) || isDeleting(anObject);
		}
		return false;
	}

	public boolean isDeleting(IMsoObjectIf anObject) {
		if(anObject!=null) {
			return m_deleting.containsKey(anObject);
		}
		return false;
	}

	public boolean add(M anObject)
	{

		// initialize flags
		boolean isDirty = false;

		// valid?
		if (isSetup(anObject))
		{

			/* ========================================================
			 * Successfully adding a new object is dependent on the
			 * update mode of the MSO model.
			 *
			 * If the model is in REMOTE_UPDATE_MODE, any change from
			 * the server will be a new remote object that by definition
			 * can not exist locally in this list. Hence, ADD operations
			 * on list can not produce a conflict. 
			 *
			 * If a relation is added locally (isChanged() is true), 
			 * and the model is in REMOTE_UPDATE_MODE, this update may 
			 * be a loopback. Since loopback updates are just a ACK
			 * from the server, no relations are are changed. Hence,
			 * IMsoClientUpdateListener listeners are not required to fetch
			 * added relations. However, loopback updates may be used to
			 * indicate to the user that the commit was successful. 			    	  
			 *
			 * If the model is in LOCAL_UPDATE_MODE, a object is
			 * created locally and should be added as such.
			 *
			 * A commit() or rollback() will remove all locally
			 * added and deleted objects.
			 *
			 * ======================================================== */

			// update internal lists
			switch (m_model.getUpdateMode())
			{
			case REMOTE_UPDATE_MODE:
			{

				/* ===========================================================
				 * Update to SERVER state
				 *
				 * If the model is in REMOTE_UPDATE_MODE,
				 * any change from the server will be a new remote object.
				 * Consequently, this object can not exist locally. Hence, ADD
				 * operations can not produce a conflict.
				 * 
				 * ================================================================
				 * IMPORTANT 1
				 * ================================================================
				 *
				 * If the object exists locally, this directly yields that a
				 * proper LOOPBACK must have occurred. Any local existence should
				 * be removed from the local ADDED list. A new remote object can
				 * not by definition be deleted locally because any locally
				 * deleted object is already added remotely and can thus not be
				 * added once again.
				 * 
				 *	            	
				 * =========================================================== */

				// valid operation?
				if (!isOriginRemote(anObject))
				{
					// get key
					String id = anObject.getObjectId();

					// initialize 
					IMsoRelationIf<M> aRelObj = null;

					// remove local existence?
					if(isOriginLocal(anObject))
					{
						// remove local delete if exists (no action required)
						m_deleting.remove(anObject);
						m_deleted.remove(id);

						// remove local existence from lists if exists (loopback)
						aRelObj = m_added.remove(id);

						/* If a added relation was found, this is a 
						 * loopback (Server ACK).
						 * If nothing was found, a new relation is created*/
						isDirty = (aRelObj = updateRelation(id, anObject, aRelObj, false))!=null;

					}
					else {
						/* The If nothing was found, a new relation is created*/
						isDirty = (aRelObj = updateRelation(id, anObject, aRelObj, true))!=null;		                			                	
					}

					// add relation to REMOTE state list?
					if(isDirty) 
					{
						m_created.put(id,aRelObj);
					}

				}

				break;
			}
			default: // LOCAL_UPDATE_MODE
			{

				/* ===========================================================
				 * Update relation to the appropriate state
				 *
				 * The default update mode is LOCAL_UPDATE_MODE. This mode
				 * indicates that the change originates from a GUI (user)
				 * or Service (application) invocation. Local existence is
				 * registered by adding to the ADDED list
				 *
				 * ================================================================
				 * IMPORTANT
				 * ================================================================
				 *
				 * The new object can not exist neither remotely nor locally.
				 *
				 * =========================================================== */

				// valid operation?
				if (!exists(anObject))
				{
					// get key
					String id = anObject.getObjectId();

					// add to LOCAL state list
					m_added.put(id,createRelation(id, anObject));

					// is changed
					isDirty = true;

				}
			}
			}
		}

		// changed?
		if(isDirty)
		{
			incrementChangeCount();
		}

		// finished
		return isDirty;

	}

	public boolean remove(M anObject)
	{

		if (anObject == null)
		{
			throw new MsoRuntimeException(getName() + ": Cannot remove null object");
		}

		if(!exists(anObject)) 
		{
			throw new MsoRuntimeException(getName() + ": Cannot remove object the do not exist in list");
		}

		/* ========================================================
		 * Successfully removing a object is dependent on the
		 * update mode of the MSO model.
		 *
		 * If the model is in REMOTE_UPDATE_MODE, the removed object
		 * does not exist remotely and should be removed from the
		 * list completely. Hence, REMOVE operations can not 
		 * produce a conflict.
		 *
		 * A commit() or rollback() will remove all locally
		 * added and deleted objects.
		 *
		 * ======================================================== */

		// initialize
		boolean bFlag = false;

		// remove from list
		if (m_isMain)
		{
			/* 
			 * Since this list is a main list, relation objects 
			 * must be deleted by definition (in general, all 
			 * IMsoObjectIf instances must be owned by a 
			 * IMsoListIf instance). Since each related object 
			 * is owned by this list, deleteRelation is invoked
			 * from the destroy method of the related object. 
			 */
			bFlag = anObject.delete(true);
		} 
		else
		{	
			// only delete relation
			bFlag = deleteRelation(anObject);
		}

		// increment change count?
		if(bFlag) incrementChangeCount();

		// finished
		return bFlag;

	}


	/**
	 * Delete all objects </p>
	 * 
	 * Can be optimized, but has probably very little effect, 
	 * as the list normally will be quite short.
	 */
	public void removeAll()
	{
		M refObj = getHeadObject();
		while (refObj != null)
		{
			remove(refObj);
			refObj = getHeadObject();
		}
	}

	@Override
	public boolean commit() throws TransactionException
	{
		if(isChanged())
		{        	
			// get change source
			IChangeRecordIf changes = m_owner.getModel().getChanges(m_owner);

			// create partial commit
			for (IMsoRelationIf<M> it : m_added.values())
			{
				changes.addFilter(it);
			}
			for (IMsoRelationIf<M> it : m_deleted.values())
			{
				changes.addFilter(it);
			}

			// increment change count
			incrementChangeCount();

			// perform a partial commit
			m_owner.getModel().commit(changes);
			
			// success
			return true;
		}
		// failure
		return false;
	}    

	@Override
	public boolean rollback()
	{
		if(isChanged())
		{
			// suspend update
			m_model.suspendUpdate();

	    	// initialize restore flag
	    	boolean bFlag = !m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
	
	    	// ensure that model is in local update mode
	    	if(bFlag) m_model.setLocalUpdateMode();
	
			// rollback all added relations
			rollbackRelations(m_added);
	
			// rollback all removed relations
			rollbackRelations(m_deleted);
	
			// forward to remote items?
			if (m_isMain)
			{
				for (IMsoRelationIf<M> it : m_created.values())
				{
					M msoObj = it.get();
					if(msoObj!=null) 
					{
						((AbstractMsoObject) msoObj).rollback();
					}
				}
			}
	
	    	// restore previous update mode?
	        if(bFlag) m_model.restoreUpdateMode();
	        
	        // finalize
	        m_model.resumeUpdate();
	        
	        // success
	        return true;
		}
        // failure
		return false;
	}

	/**
	 * Check if relation to given object can be deleted
	 *
	 * @param anObject - the object to remove a relation from
	 * @return this method only return <code>true</code> if a relation to the object exists or is deleted locally
	 */
	public boolean isRelationDeletable(IMsoObjectIf anObject)
	{
		return exists(anObject) || isDeleted(anObject);
	}

	public boolean deleteRelation(IMsoObjectIf anObject)
	{

		// invalid object?
		if(!isRelationDeletable(anObject)) return false;

		/* ================================================================
		 * 
		 * If a relation is deleted locally (isChanged() is true), 
		 * and the model is in REMOTE_UPDATE_MODE, this update may 
		 * be a loopback. Since loopback updates are just a ACK
		 * from the server, no relations are are changed. Hence,
		 * IMsoClientUpdateListener listeners are not required to fetch
		 * added or deleted relations. However, loopback updates may 
		 * be used to indicate to the user that the commit was successful.
		 *  			    	  
		 * If the model is in LOCAL_UPDATE_MODE, a object is
		 * deleted locally and should be removed as such.
		 *
		 * If client updates are suspended, deleted relations must
		 * be kept until resumeClientUpdate() is called. If not,
		 * the client will never be notified because deleted relations
		 * are not present in m_added or m_items any more. m_deleting
		 * is used to store deleted relations until resumeClientUpdate()
		 * is called.
		 * 
		 * ================================================================ */

		// get local update flag
		boolean isLocalUpdate = m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);

		// get client update suspended flag from owner
		boolean isUpdateSuspended = m_owner.isUpdateSuspended();

		// get object id
		String id = anObject.getObjectId();

		// remove relation from items 
		IMsoRelationIf<M> refObj = m_created.remove(id);

		// exists remotely?
		if (refObj != null)
		{
			/* ================================================
			 * The relation exists remotely. If the model
			 * is in LOCAL_UPDATE_MODE, this implies that
			 * the object should be added to the list of 
			 * locally deleted objects.    
			 * ================================================ */

			// add to locally deleted items?
			if (isLocalUpdate)
			{
				// add to list
				m_deleted.put(id, refObj);

				// add to pending deletions?
				if(isUpdateSuspended)
				{
					m_deleting.put(anObject, refObj);
				}

			}

			/* remove this list as holder of  the object and reset 
			 * the relation. This will produce a client update event
			 * if client updates are not suspended */ 
			destroyRelation(refObj);

		} 
		else
		{
			/* ================================================
			 * The object does not exist remotely. Since a 
			 * locally added object can not exist remotely 
			 * (given by the concurrency properties of the 
			 * distribution strategy), this must either be a 
			 * ROLLBACK or a LOOPBACK situation.
			 * 
			 * If the object is added locally, this is a
			 * ROLLBACK situation. If the object is not added
			 * 
			 * ================================================ */

			// remove locally added relation
			refObj = m_added.remove(id);

			/* remove this list as holder of 
			 * the object and reset the relation? */ 
			if(destroyRelation(refObj)) 
			{            	        	
				/* This is a ROLLBACK. If client updates are
				 * suspended, the ROLLBACK should be buffered 
				 * until resumeClientUpdate() is called */
				if(isUpdateSuspended)
				{
					m_deleting.put(anObject, refObj);
				}
			}
			else 
			{	        
				/* This is a LOOPBACK. locally deleted relation 
				 * should be deleted */ 
				m_deleted.remove(id);
			}

		}
		// finished
		return true;
	}

	public Set<M> selectItems(Selector<M> aSelector)
	{
		return selectItemsInCollection(aSelector, getObjects());
	}

	public List<M> selectItems(Selector<M> aSelector, Comparator<M> aComparator)
	{
		return selectItemsInCollection(aSelector, aComparator, getObjects());
	}

	public M selectSingleItem(Selector<M> aSelector)
	{
		return selectSingleItem(aSelector, getObjects());
	}

	/* =========================================================
	 * public MsoListImpl methods
	 * ========================================================= */

	public void checkCreateOp()
	{
		verifyMainOperation("Cannot create object in a non-main list");
	}

	public void verifyMainOperation(String aMessage)
	{
		if (!m_isMain)
		{
			throw new MsoRuntimeException(aMessage);
		}
	}

	/**
     * Resume pending update notification to listeners to all list items. <p/>
	 *
	 * @return Returns <code>true</code> if suspended updates were resumed. 
	 * If no suspended client updates were resumed and notified to clients,
	 * this method returns <code>false</code>.
	 */
	public boolean resumeClientUpdate(boolean all)
	{
		// initialize
		boolean bFlag = false;

		// only notify existing items once in main list
		if (m_isMain)
		{
			// loop over all remote and local (added) items
			for (M object : getObjects())
			{
				// prevent endless loop
				if(object!=m_owner)
				{				
					bFlag |= object.resumeUpdate(all);
				}
			}
		}
		// notify deleted items and items pending deletion
		for(IMsoRelationIf<M> it : m_deleted.values()) {
			M msoObj = it.get();
			// prevent endless loop
			if(msoObj!=null && msoObj!=m_owner) 
			{
				bFlag |= msoObj.resumeUpdate(all);
			}
		}
		for(IMsoObjectIf it : m_deleting.keySet()) {
			// prevent endless loop
			if(it!=m_owner)
			{
				bFlag |= it.resumeUpdate(all);
			}
		}
		// reset pending deletions
		m_deleting.clear();

		// finished
		return bFlag;
	}

	public Collection<IChangeIf.IChangeRelationIf> getChanges()
	{
		// initialize collection
		Vector<IChangeIf.IChangeRelationIf> list = new Vector<IChangeIf.IChangeRelationIf>();

		// add changes
		for (IMsoRelationIf<M> it : m_added.values())
		{
			list.addAll(it.getChanges());
		}
		for (IMsoRelationIf<M> it : m_deleted.values())
		{
			list.addAll(it.getChanges());
		}

		// finished
		return list;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		MsoListImpl<M> list;
		try
		{
			list = (MsoListImpl<M>)o;
		}
		catch (Exception e)
		{
			return false;
		}

		if (m_owner != null ? !m_owner.equals(list.m_owner) : list.m_owner != null)
		{
			return false;
		}
		if (m_name != null ? !m_name.equals(list.m_name) : list.m_name != null)
		{
			return false;
		}
		if (m_isMain != list.m_isMain)
		{
			return false;
		}
		if (m_created != null ? !m_created.equals(list.m_created) : list.m_created != null)
		{
			return false;
		}
		if (m_added != null ? !m_added.equals(list.m_added) : list.m_added != null)
		{
			return false;
		}
		if (m_deleted != null ? !m_deleted.equals(list.m_deleted) : list.m_deleted != null)
		{
			return false;
		}

		return true;
	}

	/**
	 * Get the item class
	 */
	public Class<M> getObjectClass() {
		return m_objectClass;
	}
	
	@Override
	public MsoDataType getDataType() {
		return MsoDataType.MANY_RELATION;
	}

	@Override
	public MsoClassCode getClassCode() {
		return MsoClassCode.CLASSCODE_NOCLASS;
	}
	
	@Override
	public int compareTo(IData o) {
		return 0;
	}

	/* =========================================================
	 * private helper methods
	 * ========================================================= */

	/**
	 * Remove all added and deleted relations properly
	 */
	private void rollbackRelations(Map<String, IMsoRelationIf<M>> list)
	{

		// has no relations?
		if (list.size() == 0)
		{
			return;
		}

		/* Copy the list and clear the original before any
		 * events are sent around, since the events are checking
		 * the original list */
		Collection<IMsoRelationIf<M>> items = new Vector<IMsoRelationIf<M>>(list.size());
		items.addAll(list.values());

		// loop over list copy
		for(IMsoRelationIf<M> it : items)
		{
			it.rollback();
		}
	}

	/**
	 * Create a relation.
	 * 
	 * @param id - the object id
	 * @param msoObj - the object to create a relation to
	 * 
	 * @return Create IMsoRelationIf instance
	 */
	private IMsoRelationIf<M> createRelation(String id, M msoObj) {
		IMsoRelationIf<M> aRelObj = null;
		if(msoObj!=null) {
			/* The list need to manage relation objects list in a 
			 * expected manner. By the definition of the one-to-many 
			 * relation, a relation object is only "active" as long
			 * as it relation a object. Since the list has methods 
			 * that returns the relation object, the callee is able
			 * to change the related object, and to commit and 
			 * rollback changes made by the list. However, changing
			 * the These activities must 
			 * clearly be tracked by the list. 
			 * 
			 * Below, setting cardinality to 1, ensures that 
			 * related objects can not be set null.
			 * Furthermore, by passing a relation to this list, ensure
			 * that the list can track any activities executed on the
			 * relation object directly. */
			aRelObj = new MsoRelationImpl<M>(m_owner,id,1,true,this);
			((AbstractMsoObject)msoObj).addMsoObjectHolder(this);
			aRelObj.set(msoObj);
			String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
			m_logger.info("Added list relation from " + s + " to " + msoObj + " in list " + m_name);
		}
		return aRelObj;
	}

	/**
	 * Update given relation
	 * @param aRelObj - the relation to update
	 */
	private IMsoRelationIf<M> updateRelation(
			String id, M msoObj, 
			IMsoRelationIf<M> aRelObj, boolean isDataRemote) {

		// is a loopback?
		if(aRelObj!=null) 
		{
			// update relation
			aRelObj.set(msoObj);
		} 
		else if(isDataRemote)
		{
			// create a new relation
			aRelObj = createRelation(id, msoObj);		                	
		}

		// finished
		return aRelObj;
	}

	/**
	 * Destroy a given relation. This list is removed as 
	 * an object holder of the related 
	 * @param refObj - the relation to destroy
	 */
	private boolean destroyRelation(IMsoRelationIf<M> refObj) {
		if(refObj!=null) 
		{
			IMsoObjectIf msoObj = refObj.get();
			if(msoObj!=null) 
			{
				((AbstractMsoObject)msoObj).removeMsoObjectHolder(this);
				refObj.set(null);
				String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
				m_logger.info("Deleted list relation from " + s + " to " + msoObj + " in list " + m_name);
			}
			return true;
		}
		return false;
	}

	/**
	 * Remove added relation properly
	 */
	private boolean rollbackAddedRelation(IMsoRelationIf<M> aRelObj)
	{
		if (aRelObj != null)
		{
			// get IMsoObjectIf object
			M msoObj = aRelObj.get();
			// has object?
			if(msoObj!=null) {
				// same algorithm as for remove()
				if (m_isMain)
				{
					// rollback object creation
					msoObj.rollback();
					// success
					return true;
				} 
				else
				{
					// forward
					return deleteRelation(msoObj);
				}        		
			}

		}
		// failure
		return false;
	}
	
	/**
	 * Reinsert a relation in list.
	 *
	 * @param anObject The relation to undelete.
	 */
	private boolean rollbackRemovedRelation(IMsoRelationIf<M> aRelObj)
	{
		if (aRelObj != null)
		{
			// get referred object
			IMsoObjectIf msoObj = aRelObj.get();
			
			// reinsert, remove from deletion and 
			// add this list as object holder once more?    		
			if(msoObj!=null) 
			{
				// add to items
				m_created.put(aRelObj.getName(),aRelObj);
				
				// remove from deletion
				m_deleting.remove(msoObj);
				m_deleted.remove(msoObj.getObjectId());
				
				// add list as object holder of referred object
				((AbstractMsoObject)msoObj).addMsoObjectHolder(this);
				String s = this.m_owner != null ? this.m_owner.toString() : this.toString();
				m_logger.info("Rollback list relation from " + s + " to " + msoObj + " in list " + m_name);
			}
			return true;

		}
		return false;
	}        

	private boolean isTrue(Object value) {
		if(value instanceof Boolean)
			return (Boolean)value;
		return false;
	}

	private Collection<M> getAllItems() {
		Collection<M> allList = new Vector<M>();
		allList.addAll(getObjects());
		allList.addAll(getItems(m_deleted.values()));
		allList.addAll(getItems(m_deleting.values()));
		return allList;
	}

	private boolean isNumberDuplicate(M anItem) {
		if(anItem instanceof ISerialNumberedIf) {
			ISerialNumberedIf s0 = (ISerialNumberedIf)anItem;
			for(IMsoObjectIf it: getItems(m_created.values())) {
				if(it instanceof ISerialNumberedIf) {
					if(((ISerialNumberedIf)it).getNumber()==s0.getNumber())
						return true;
				}
			}
		}
		return false;
	}

	private boolean isSetup(M msoObj) {
		if(msoObj == null) return false;
		return msoObj.isSetup();	
	}

	private List<M> selectCandidates(Selector<M> aSelector)
	{
		return selectItems(aSelector, descendingNumberComparator);
	}

	// Loop through all items with a number higher than
	private List<M> renumberCandidates(List<M> candidates)
	{
		// initialize renumbered list
		List<M> renumbered = new ArrayList<M>(candidates.size());

		// loop over all candidates
		if (candidates.size() != 0)
		{

			m_model.setLocalUpdateMode();
			int nextNumber = -1;
			for (M item : candidates)
			{
				if (item instanceof ISerialNumberedIf)
				{
					ISerialNumberedIf numberedItem = (ISerialNumberedIf) item;
					if (numberedItem.getNumberState() != IData.DataOrigin.REMOTE)
					{
						if (nextNumber < 0)
						{
							nextNumber = numberedItem.getNumber() + 1;
						}
						int tmpNumber = numberedItem.getNumber();
						numberedItem.setNumber(nextNumber);
						nextNumber = tmpNumber;
						// add to list
						renumbered.add(item);
					}
				}
			}
			m_model.restoreUpdateMode();
		}

		// finished
		return renumbered;

	}

	/* =========================================================
	 * Protected methods
	 * ========================================================= */

	/**
	 * Callback method that MsoRelationImpl is required to call 
	 * each time BEFORE the relation object changes in LOCAL 
	 * UPDATE mode. This method should not be called during 
	 * REMOTE UPDATE mode, since remote changes are always 
	 * valid (they must be, since all changes are validated 
	 * before they are committed). The following rules must be
	 * true, before a relation is found changeable: </p>
	 * 
	 * <ol>
	 * 	<li>the relation object aRel is managed by this list</li>
	 * 	<li>new object is not null</li>
	 * 	<li>new object can not already be contained (added, 
	 * 		created or deleted) by this list </li>
	 * </ol>
	 * 
	 */
	protected boolean isChangeable(MsoRelationImpl<M> aRel, M oldObj, M newObj)
	{
		// validate relation object, cardinality and uniqueness
		if( getRelation(oldObj) == aRel 
			&& newObj!=null && !contains(newObj)) 
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Callback method that MsoRelationImpl is required to call 
	 * each time the relation changes. </p>
	 * 
	 * This method enables the list to track changes made in 
	 * relations managed by the list. 
	 */
	protected boolean change(MsoRelationImpl<M> aRel, M oldObj, M newObj)
	{
		// is allowed?
		if(isChangeable(aRel, oldObj, newObj))
		{
			/* ======================================================
			 * Change algorithm
			 * ========================================================
			 * 
			 * The only change allowed is REPLACE. This is given
			 * by the required cardinality of 1. Hence, when 
			 * a relation is found to be changeble, the
			 * following three cases must be covered.
			 * 
			 * 1. The old relation is local
			 * --------------------------------------------------------
			 * 
			 * In this case, the only action required is to replace
			 * the key pointing to the MsoRelationImpl object 
			 * in the map of added relations.
			 * 
			 * 2. The old relation is remote
			 * -------------------------------------------------------- 
			 * 
			 * In this case, the relation must be moved from the
			 * map of existing relations to the map of added relations
			 * 
			 * ======================================================== */
			
			if(isAdded(oldObj))
			{
				// replace old key with new key
				m_added.remove(oldObj.getObjectId());
				m_added.put(newObj.getObjectId(),aRel);
			}
			else 
			{
				// remove from map of created relations 
				m_created.remove(oldObj.getObjectId());
				m_added.put(newObj.getObjectId(),aRel);
			}

			
		}
		
		// not allowed
		return false;
	}
	
	/**
	 * Callback method that MsoRelationImpl is required to call 
	 * after rollback is performed, if it is part of a list 
	 * (one-to-many relation). </p>
	 * 
	 * This method enables the list to track changes made in 
	 * relations managed by the list. 
	 */
	protected boolean rollback(MsoRelationImpl<M> aRelObj)
	{
		M msoObj = aRelObj.get();
		if(isAdded(msoObj))
		{
			return rollbackAddedRelation(aRelObj);
		}
		else if(isDeleted(msoObj))
		{
			return rollbackRemovedRelation(aRelObj);			
		}
		return false;
	}
	
	
	/**
	 * This method ensures that a LOCAL added object is moved to
	 * REMOTE state when it is created after a commit.
	 *
	 * @param IObjectIdIf anObjectId - the item to check for loopback
	 * @return The found loopback object
	 */

	protected M getLoopback(IMsoObjectIf.IObjectIdIf anObjectId)
	{
		// check if exists
		M retVal = getObject(anObjectId.getId());
		// move to REMOTE state?
		if(isOriginLocal(retVal)) add(retVal);
		// succeeded
		return getObject(anObjectId.getId());
	}

	/**
	 * Update list name.
	 * 
	 * @param aName - the name
	 * 
	 */
	protected void setName(String aName)
	{
		m_name = aName.toLowerCase();
	}

	protected void incrementChangeCount() {
		m_changeCount++;
	}

	protected M createdItem(M anObject)
	{
		((AbstractMsoObject) anObject).setup(false);
		((AbstractMsoObject) anObject).setMainList(this);
		add(anObject);
		anObject.resumeUpdate(false);
		return anObject;
	}

	protected M createdUniqueItem(M anObject)
	{
		try
		{
			return createdItem(anObject);
		}
		catch (DuplicateIdException e)
		{
			//throw new MsoRuntimeException("Duplicate object id, should be unique: " + anObject.getObjectId());
		}
		return null;
	}

	protected IObjectIdIf makeUniqueId()
	{
		IObjectIdIf retVal;
		do
		{
			retVal = m_model.getDispatcher().makeObjectId();
		}
		while (m_created.get(retVal.getId()) != null || m_added.get(retVal.getId()) != null || m_deleted.get(retVal.getId()) != null);
		return retVal;
	}

	protected int makeSerialNumber()
	{
		int max = 0;
		for (M item : getObjects())
		{
			try
			{
				ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
				max = Math.max(max, serialItem.getNumber());
			}
			catch (ClassCastException e)
			{
				//throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
			}
		}
		return max + 1;
	}

	protected int makeSerialNumber(Enum<?> type)
	{
		int max = 0;
		for (M item : getObjects())
		{
			try
			{
				if(MsoUtils.getType(item, true)==type) {
					ISerialNumberedIf serialItem = (ISerialNumberedIf) item;
					max = Math.max(max, serialItem.getNumber());
				}
			}
			catch (ClassCastException e)
			{
				//throw new MsoRuntimeException("Object " + item + " is not implementing ISerialNumberedIf");
			}
		}
		return max + 1;
	}

	protected List<M> renumberDuplicateNumbers(M anItem)
	{
		List<M> updates = new ArrayList<M>(size());
		if(isNumberDuplicate(anItem))
			return renumberCandidates(selectCandidates(getRenumberSelector(anItem)));
		else
			return updates;
	}

	protected Collection<M> getItems(Collection<IMsoRelationIf<M>> list) {
		Collection<M> items = new Vector<M>();
		for(IMsoRelationIf<M> it : list) {
			items.add(it.get());
		}
		return items;
	}

	protected Map<String, IMsoRelationIf<M>> selectRelations(Map<String, IMsoRelationIf<M>> relations, Collection<IChangeRelationIf> objects) {
		Map<String, IMsoRelationIf<M>> map = new HashMap<String, IMsoRelationIf<M>>();
		for(IChangeRelationIf it : objects) {
			IMsoObjectIf msoObj = it.getReferredObject();
			String id = msoObj.getObjectId();
			IMsoRelationIf<M> refObj = relations.get(id);
			if(refObj!=null) map.put(id,refObj);
		}
		return map;
	}

	protected Selector<M> getRenumberSelector(M anItem)
	{
		renumberSelector.setSelectionItem(anItem);
		return renumberSelector;
	}
	
	/* =========================================================
	 * anonymous classes
	 * ========================================================= */

	private final Comparator<M> descendingNumberComparator = new Comparator<M>()
	{
		public int compare(M o1, M o2)
		{
			if (o1 instanceof ISerialNumberedIf && o2 instanceof ISerialNumberedIf)
			{
				return -(((ISerialNumberedIf) o1).getNumber() - ((ISerialNumberedIf) o2).getNumber()); // sort descending
			}
			return 0;
		}
	};

	/* =========================================================
	 * inner classes
	 * ========================================================= */

	private final RenumberSelector<M> renumberSelector = new RenumberSelector<M>();

	protected class RenumberSelector<T extends M> implements Selector<T>
	{
		protected M m_selectionItem;

		void setSelectionItem(M anItem)
		{
			m_selectionItem = anItem;
		}

		public boolean select(T anObject)
		{
			if (anObject == m_selectionItem)
			{
				return false;
			}
			if (anObject instanceof ISerialNumberedIf)
			{
				return ((ISerialNumberedIf) anObject).getNumber() >= ((ISerialNumberedIf) m_selectionItem).getNumber();
			}
			return false;
		}
	}

	/* =========================================================
	 * Static methods
	 * ========================================================= */

	/**
	 * Insert an item into a list.
	 *
	 * @param aList       The list to insert into
	 * @param anItem      The item to add
	 * @param aComparator A comparator. If null, the item is appended to the list, if not null, used as a comparator to sort the list.
	 */
	private static <T extends IMsoObjectIf> void addSorted(ArrayList<T> aList, T anItem, Comparator<? super T> aComparator)
	{
		if (aComparator == null)
		{
			aList.add(anItem);
		} else
		{
			int size = aList.size();
			int location = Collections.binarySearch(aList, anItem, aComparator);
			if (location < 0)
			{
				location = -location - 1;
			} else
			{
				while (location < size && aComparator.compare(anItem, aList.get(location)) <= 0)
				{
					location++;
				}
			}
			aList.add(location, anItem);
		}
	}

	public static <T extends IMsoObjectIf> Set<T> selectItemsInCollection(Selector<? super T> aSelector, Collection<T> theItems)
	{
		Set<T> retVal = new LinkedHashSet<T>();
		for (T item : theItems)
		{
			if (aSelector.select(item))
			{
				retVal.add(item);
			}
		}
		return retVal;
	}

	public static <T extends IMsoObjectIf> List<T> selectItemsInCollection(Selector<? super T> aSelector, Comparator<? super T> aComparator, Collection<T> theItems)
	{
		ArrayList<T> retVal = new ArrayList<T>();
		for (T item : theItems)
		{
			if (aSelector.select(item))
			{
				addSorted(retVal,item,aComparator);
			}
		}
		return retVal;
	}

	public static <T extends IMsoObjectIf> T selectSingleItem(Selector<? super T> aSelector, Collection<T> theItems)
	{
		for (T item : theItems)
		{
			if (aSelector.select(item))
			{
				return item;
			}
		}
		return null;
	}

}