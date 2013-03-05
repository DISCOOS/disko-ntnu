package org.redcross.sar.mso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IChangeIf.IChangeObjectIf;
import org.redcross.sar.mso.IChangeIf.IChangeRelationIf;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;

public class ChangeRecordImpl implements IChangeRecordIf
{

	private int m_mask;
	private long m_seqNo = -1;
	private UpdateMode m_mode;
	private boolean m_isDirty;
	private boolean m_isLoopbackMode;
	private boolean m_isRollbackMode;
	private boolean m_isSorted = false;
	private List<IChangeIf> m_changes = new ArrayList<IChangeIf>();
	private List<IChangeIf> m_filters = new ArrayList<IChangeIf>();
	private Map<String,List<IChangeIf>> m_map = new HashMap<String, List<IChangeIf>>();
	
	private final IMsoObjectIf m_msoObj;
	
	private final ISeqNoGenIf m_nextSeqNoGen;
	
	private final Logger m_logger = Logger.getLogger(getClass());

    /* ===============================================
     * Constructors
     * =============================================== */
    
    public ChangeRecordImpl(ChangeRecordImpl rs)
    {
    	// forward
    	this(rs.getMsoObject(),rs.getUpdateMode(),getType(rs.getMask()),rs.getNextSeqNo());
    	// clone lists
    	m_filters = new ArrayList<IChangeIf>(rs.m_filters);
    	m_changes = new ArrayList<IChangeIf>(rs.m_changes);
    	m_map = new HashMap<String, List<IChangeIf>>(rs.m_map);
    	// set dirty flag
    	m_isDirty = true;
    }
    
    public ChangeRecordImpl(IMsoObjectIf anObject, UpdateMode mode)
    {
    	this(anObject,mode,MsoEventType.EMPTY_EVENT,0);
    }
    
    public ChangeRecordImpl(IMsoObjectIf anObject, UpdateMode mode, ISeqNoGenIf generator)
    {
    	this(anObject, mode, MsoEventType.EMPTY_EVENT, generator);
    }    
    
    public ChangeRecordImpl(IMsoObjectIf anObject, UpdateMode aMode, MsoEventType aMask)
    {
    	this(anObject, aMode, aMask, new SeqNoGen(0));
    }
    
    public ChangeRecordImpl(IMsoObjectIf anObject, UpdateMode aMode, MsoEventType aMask, long nextSeqNo)
    {
    	this(anObject, aMode, aMask, new SeqNoGen(nextSeqNo));
    }
    
    public ChangeRecordImpl(IMsoObjectIf anObject, UpdateMode aMode, MsoEventType aMask, ISeqNoGenIf generator)
    {
    
    	// prepare
        m_mask = aMask.maskValue();
        m_mode = aMode;
        m_msoObj = anObject;
        m_nextSeqNoGen = generator;
    }
    
    /* ===============================================
     * Public methods
     * =============================================== */
    
	@Override
	public long getSeqNo() {
		if(m_isDirty)
		{
			calculate();
		}
		return m_seqNo;
	}
    
	@Override
	public long getNextSeqNo() {
		return m_nextSeqNoGen.getNextSeqNo();
	}
			
	@Override
	public boolean isSeqNoEnabled() {
		return m_nextSeqNoGen.isSeqNoEnabled();
	}

	@Override
	public void setSeqNoEnabled(boolean isEnabled) {
		m_nextSeqNoGen.setSeqNoEnabled(isEnabled);	
	}

	@Override
	public boolean isSorted() {
		return m_isSorted;
	}

	@Override
	public void setSorted(boolean isSorted) {
		m_isSorted = isSorted;
	}

	@Override
	public IMsoObjectIf getMsoObject() {
		return m_msoObj;
	}
	
	@Override 
	public UpdateMode getUpdateMode()
	{
		return m_mode;
	}

	@Override
	public int getMask()
	{
		if(m_isDirty)
		{
			calculate();
		}
		return m_mask;
	}
	
	@Override
	public boolean isFlagSet(int flag) {
		return isFlagSet(flag, getMask());
	}

    @Override
	public boolean isChanged() {
		return getMask()>0 && !(isObjectCreated() && isObjectDeleted());
	}
    
	@Override
    public boolean isObjectCreated()
    {
    	return isFlagSet(MsoEventType.CREATED_OBJECT_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isObjectDeleted()
    {
    	return isFlagSet(MsoEventType.DELETED_OBJECT_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isObjectModified()
    {
    	return isFlagSet(MsoEventType.MODIFIED_DATA_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationAdded()
    {
    	return isFlagSet(MsoEventType.ADDED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationRemoved()
    {
    	return isFlagSet(MsoEventType.REMOVED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isRelationModified()
    {
    	return isFlagSet(MsoEventType.ADDED_RELATION_EVENT.maskValue(),getMask())
    	    || isFlagSet(MsoEventType.REMOVED_RELATION_EVENT.maskValue(),getMask());
    }

	@Override
    public boolean isAllDataCleared()
    {
    	return isFlagSet(MsoEventType.CLEAR_ALL_EVENT.maskValue(),getMask());
    }
    
	@Override
	public boolean isLoopbackMode() 
	{
		if(m_isDirty)
		{
			calculate();
		}
		return m_isLoopbackMode;
	}

	@Override
	public boolean isRollbackMode() 
	{
		if(m_isDirty)
		{
			calculate();
		}
		return m_isRollbackMode;
	}

    @Override
    public boolean isFiltered() {
    	return m_filters!=null && m_filters.size()>0;
    }

    @Override
    public boolean addFilter(String anAttribute) {

    	String name = anAttribute.toLowerCase();
    	if(getMsoObject().getAttributes().containsKey(name)) {
    		return addFilter(getMsoObject().getAttributes().get(name));
    	}
    	return false;
    }
    
    @Override
    public boolean addFilter(IMsoAttributeIf<?> anAttribute) {

    	/* =======================================
    	 * Only allowed for an object that is
    	 * 	A) already created
    	 * 	B) is modified 
    	 *  C) attribute is changed (locally)
    	 *  D) attribute not already added as partial update
    	 *  E) attribute exists in object
    	 * ======================================= */

    	if(isChanged() && getMsoObject().getAttributes().containsValue(anAttribute)) 
    	{
    		if(!containsFilter(anAttribute)) 
    		{
	    		// get change
	    		IChangeAttributeIf it = get(anAttribute);
	    		if(it!=null) 
	    		{
	        		// add attribute change to filter
	    			return m_filters.add(it);
	    		}
    		}
    		
    	}
    	// failure
    	return false;
    }        
    
    @Override
    public boolean addFilter(IMsoObjectIf aReference) {
		return addFilter(getMsoObject().getRelation(aReference));        	
    }
    	
    @Override
    public boolean addFilter(IMsoRelationIf<?> aReference) {
    	
    	/* =======================================
    	 * Only allowed for an object that is
    	 * 	A) already created
    	 * 	B) is modified 
    	 *  C) the reference is changed (locally)
    	 *  D) the reference not already added 
    	 *  as partial update
    	 * ======================================= */

    	if(isChanged()) 
    	{
    		// has reference?
    		if(aReference!=null) 
    		{
    			// is not filtered already?
    			List<IChangeRelationIf> list = getFilter(aReference);
    			// remote old filter
				m_filters.removeAll(list);
				// add new filter
				m_filters.addAll(get(aReference));
    		}
    	}
    	// failure
    	return false;
    	
    }        
    
    @Override
    public boolean removeFilter(String anAttribute) {
    	String name = anAttribute.toLowerCase();
    	IChangeAttributeIf found = null;
    	for(IChangeIf it : m_filters) {
    		if(it instanceof IChangeAttributeIf) {
    			
    			IChangeAttributeIf attr = (IChangeAttributeIf)it;
        		if(attr.getName().equals(name)) {
        			found = attr;
        			break;
        		}
    		}
    	}
    	if(found!=null) {
    		return m_filters.remove(found);
    	}
    	return false;
    }
    
    @Override
    public boolean removeFilter(IMsoAttributeIf<?> anAttribute) {
    	return removeFilter(anAttribute.getName().toLowerCase());
    }
    
	@Override
	public boolean removeFilter(IMsoObjectIf aReference) {
		return removeFilter(getMsoObject().getRelation(aReference));        	
	}
	
	@Override
	public boolean removeFilter(IMsoRelationIf<?> aReference) {
		List<IChangeRelationIf> list = get(aReference);		
    	return m_filters.removeAll(list);
	}
    
    @Override
    public List<IChangeIf> getFilters() {
    	return clone(m_filters);
    }
    
    @Override
    public void clearFilters() {
    	m_filters.clear();
    }

    @Override
    public void clearFilters(IChangeRecordIf rs) {
    	for(IChangeIf it : rs.getFilters()) 
    	{
    		if(m_filters.contains(it)) 
    		{
    			m_filters.remove(it);
    		}
    		else if(it instanceof IChangeAttributeIf)
    		{
    			removeFilter(((IChangeAttributeIf)it).getMsoAttribute());
    		}
    		else if (it instanceof IChangeRelationIf)
    		{
    			removeFilter(((IChangeRelationIf)it).getMsoRelation());        			
    		}
    	}
    }
    
    @Override
    public boolean setFilter(String anAttribute)
    {
    	clearFilters();
    	return addFilter(anAttribute);
    }
    
	@Override
	public boolean setFilter(IMsoAttributeIf<?> anAttribute) {
    	clearFilters();
    	return addFilter(anAttribute);
	}

	@Override
	public boolean setFilter(IMsoObjectIf aReference) {
    	clearFilters();
    	return addFilter(aReference);
	}

	@Override
	public boolean setFilter(IMsoRelationIf<?> aReference) {
    	clearFilters();
    	return addFilter(aReference);
	}

	@Override
	public boolean containsFilter(IMsoAttributeIf<?> anAttribute) {
		return getFilter(anAttribute)!=null;
	}

	@Override
	public boolean containsFilter(IMsoObjectIf aReference) {
		return getFilter(getMsoObject().getRelation(aReference))!=null;
	}

	@Override
	public boolean containsFilter(IMsoRelationIf<?> aReference) {
		return getFilter(aReference)!=null;
	}

	@Override
	public boolean containsFilter(String anAttribute) {
    	String name = anAttribute.toLowerCase();
    	if(getMsoObject().getAttributes().containsKey(name)) {
    		return getFilter(getMsoObject().getAttributes().get(name))!=null;
    	}
		return false;
	}

	@Override
	public List<IChangeIf> get(String objectId) {
		return m_map.get(objectId);
	}
		
	@Override
	public List<IChangeObjectIf> get(IMsoObjectIf anObject) {
		Vector<IChangeObjectIf> changes = new Vector<IChangeObjectIf>();
		List<IChangeIf> list = m_map.get(anObject.getObjectId());
		if(list!=null)
		{
			for(IChangeIf it : list)
			{
				changes.add((IChangeObjectIf)it);
			}
		}
		return changes;
	}

	@Override
	public IChangeAttributeIf get(IMsoAttributeIf<?> anAttribute) {
		List<IChangeIf> list = m_map.get(anAttribute.getObjectId());
		if(list!=null)
		{
			return (IChangeAttributeIf)list.get(0);
		}
		return null;
	}
	
	@Override
	public List<IChangeRelationIf> get(IMsoRelationIf<?> aReference) {
		Vector<IChangeRelationIf> changes = new Vector<IChangeRelationIf>();
		List<IChangeIf> list = m_map.get(aReference.getObjectId());
		if(list!=null)
		{
			for(IChangeIf it : list)
			{
				changes.add((IChangeRelationIf)it);
			}
		}
		return changes;
	}

	@Override
	public boolean record(IChangeIf aChange, boolean checkout)
	{
		try {
			// valid change type?
			if(isValid(aChange,checkout))
			{			
				
				// clone change
				aChange = aChange.clone();
				
				// set sequence number
				aChange.setSeqNo(m_nextSeqNoGen.createSeqNo());
				
				// translate
				switch(aChange.getUpdateMode())
				{
				case LOCAL_UPDATE_MODE:
					return record(aChange,checkout,aChange.isRollbackMode());
				case REMOTE_UPDATE_MODE:
					return record(aChange,checkout,aChange.isLoopbackMode());
				}
			}
		} catch (CloneNotSupportedException e) {
			m_logger.error("Failed to clone IChangeIf instance",e);
		}
		
		// invalid change type, or clone error
		return false;
	}

	@Override
	public boolean remove(IChangeIf aChange)
	{
		// set remove flag
		boolean bFlag = false;
		
		// get list
		List<IChangeIf> list = m_map.get(aChange.getObjectId());
		
		// found list of changes registered?
		if(list!=null)
		{
			// initialize
			int index = -1;
			int size = list.size();
			int mask = aChange.getMask();
			// remove change with same mask
			for(int i=0;i<size;i++)
			{
				if(list.get(i).getMask()==mask) 
				{
					index = i;
					break;
				}
			}
			// set remove flag
			bFlag = (index != -1);
			
			// remove?
			if(bFlag)
			{				
				list.remove(index);
			}
			
			// set dirty flag
			m_isDirty |= bFlag;
			
		}
		
		// finished
		return bFlag;
		
	}
	
    @Override
	public void clear() {
    	m_map.clear();
    	m_changes.clear();
    	m_isDirty = true;
	}

    @Override
    public boolean union(IChangeRecordIf aRs, boolean checkout) {
    	// initialize dirty flag
    	boolean bFlag = false;
    	// is union possible?
    	if(   aRs != null && aRs != this && aRs.getMsoObject() == getMsoObject())
    	{
	    	// get list
	    	List<IChangeIf> changes = aRs.getChanges();
	    	// set flag
	    	bFlag = (changes.size()>0);
	    	// get union mask
	    	for(IChangeIf it : changes)
	    	{
	    		record(it,checkout);
			}
    	}
		// finished
		return bFlag;
    }
    
    @Override
    public boolean difference(IChangeRecordIf aRs) {
    	// initialize dirty flag
    	boolean bFlag = false;
    	// is difference possible?
    	if(   aRs != null 
    	   && aRs != this 
    	   && aRs.getMsoObject() == getMsoObject())
    	{
	    	// get list
	    	List<IChangeIf> changes = aRs.getChanges();
	    	// get union mask
	    	for(IChangeIf it : changes)
	    	{
	    		bFlag |= remove(it);
			}
    	}
		// finished
		return bFlag;
    }

    @Override
    public List<IChangeIf> getChanges() 
    {
    	return (isFiltered() ? sort(m_filters) : clone(m_changes));
    }
                
    @Override
	public List<IChangeObjectIf> getObjectChanges()
	{    	
    	// initialize 
    	List<IChangeObjectIf> changes = new Vector<IChangeObjectIf>();
    	
    	// get flags
        boolean createdObject = isObjectCreated();
        boolean deletedObject = isObjectDeleted();

        // a object that is both CREATED and DELETED equals NO CHANGE
        if (!(createdObject && deletedObject))
        {        
	    	// get list for changes
	    	List<IChangeIf> list = (isFiltered() ? m_filters : m_map.get(m_msoObj.getObjectId()));
	    	
	    	// has changes?
	    	if(list!=null)
	    	{
		    	// get change objects
		        if (createdObject)
		        {	        	
		        	// get CREATED_OBJECT change
		        	add(changes,get(list,MsoEventType.CREATED_OBJECT_EVENT.maskValue()));
		        	// finished
		        	return sort(changes);
		        }
		        if (deletedObject)
		        {
		        	// get DELETED_OBJECT change
		        	add(changes,get(list,MsoEventType.DELETED_OBJECT_EVENT.maskValue()));	        
		        	// finished
		        	return sort(changes);
				} 
		        
		    	if (isObjectModified())
		        {
		    		add(changes,get(list,MsoEventType.MODIFIED_DATA_EVENT.maskValue()));
		        }
		        if (isRelationAdded())
		        {
		        	add(changes,get(list,MsoEventType.ADDED_RELATION_EVENT.maskValue()));
		        }
		        if (isRelationRemoved())
		        {
		        	add(changes,get(list,MsoEventType.REMOVED_RELATION_EVENT.maskValue()));
		        }
	    	}	        
        }
        // finished
    	return sort(changes);
	}
	
    @Override
	public Collection<IChangeAttributeIf> getAttributeChanges() {

    	// initialize 
    	List<IChangeAttributeIf> changes = new Vector<IChangeAttributeIf>();
    	
        // a object that is both CREATED and DELETED equals NO CHANGE
        if (isChanged())
        {        
	    	// get list for changes
	    	List<IChangeIf> list = (isFiltered() ? m_filters : m_changes);

    		for(IChangeIf it : get(list,MsoEventType.MODIFIED_DATA_EVENT.maskValue(),false,false))
    		{
	    		changes.add((IChangeAttributeIf)it);	    			
    		}
	    	
        }
        
        // finished
        return sort(changes);
        
    }

	@Override
	public Collection<IChangeRelationIf> getRelationChanges() 
	{
    	// initialize 
    	List<IChangeRelationIf> changes = new Vector<IChangeRelationIf>();
    	
        // a object that is both CREATED and DELETED equals NO CHANGE
        if (isChanged())
        {        
	    	// get list for changes
	    	List<IChangeIf> list = (isFiltered() ? m_filters : m_changes);

    		for(IChangeIf it : get(list,MsoEventType.REMOVED_RELATION_EVENT.maskValue(),false,false))
    		{
    			changes.add((IChangeRelationIf)it);
    		}
    		
    		for(IChangeIf it : get(list,MsoEventType.ADDED_RELATION_EVENT.maskValue(),false,false))
    		{
    			changes.add((IChangeRelationIf)it);
    		}
	    	
        }
        
        // finished
        return sort(changes);		
	}
	
	@Override
	public Collection<IChangeRelationIf> getObjectReferenceChanges() {

    	// initialize 
    	List<IChangeRelationIf> changes = new Vector<IChangeRelationIf>();
    	
        // a object that is both CREATED and DELETED equals NO CHANGE
        if (isChanged())
        {        
	    	// get list for changes
	    	List<IChangeIf> list = (isFiltered() ? m_filters : m_changes);

    		for(IChangeIf it : get(list,MsoEventType.REMOVED_RELATION_EVENT.maskValue(),false,false))
    		{
    			IChangeRelationIf aChange = (IChangeRelationIf)it;
    			if(!aChange.isInList())
    			{
		    		changes.add(aChange);	    				    				
    			}
    		}
    		
    		for(IChangeIf it : get(list,MsoEventType.ADDED_RELATION_EVENT.maskValue(),false,false))
    		{
    			IChangeRelationIf aChange = (IChangeRelationIf)it;
    			if(!aChange.isInList())
    			{
		    		changes.add(aChange);	    				    				
    			}
    		}
	    	
        }
        
        // finished
        return sort(changes);
    	
	}

	@Override
	public Collection<IChangeRelationIf> getListReferenceChanges() {

    	// initialize 
    	List<IChangeRelationIf> changes = new Vector<IChangeRelationIf>();
    	
        // a object that is both CREATED and DELETED equals NO CHANGE
        if (isChanged())
        {        
	    	// get list for changes
	    	List<IChangeIf> list = (isFiltered() ? m_filters : m_changes);

    		for(IChangeIf it : get(list,MsoEventType.REMOVED_RELATION_EVENT.maskValue(),false,false))
    		{
    			IChangeRelationIf aChange = (IChangeRelationIf)it;
    			if(aChange.isInList())
    			{
		    		changes.add(aChange);	    				    				
    			}
    		}
    		
    		for(IChangeIf it : get(list,MsoEventType.ADDED_RELATION_EVENT.maskValue(),false,false))
    		{
    			IChangeRelationIf aChange = (IChangeRelationIf)it;
    			if(aChange.isInList())
    			{
		    		changes.add(aChange);	    				    				
    			}
    		}
	    	
        }
        
        // finished
        return sort(changes);
        
    }

    @Override
	public int compareTo(IChangeRecordIf rs) {
		return (int)(getSeqNo() - rs.getSeqNo());
	}
    
    public static boolean isFlagSet(int flag, int mask)
    {
    	if(mask>=flag)
    	{
    		return (mask & flag) == flag;
    	}
    	else
    	{
    		return (mask & flag) == mask;    		
    	}
    }
    
    /* ===============================================
     * Private methods
     * =============================================== */
    
	private boolean isValid(IChangeIf aChange, boolean checkout)
	{
		// initialize flag
		boolean bFlag = false;
		
		// exists?
		if(aChange!=null)
		{
			// check that the change belongs to the MSO object of this record
			if(aChange instanceof IChangeObjectIf)
			{
				bFlag = (getMsoObject()==((IChangeObjectIf)aChange).getMsoObject());
			}
			else if(aChange instanceof IChangeAttributeIf)
			{
				bFlag = (getMsoObject()==((IChangeAttributeIf)aChange).getOwnerObject());			
			}
			else if(aChange instanceof IChangeRelationIf)
			{
				bFlag = (getMsoObject()==((IChangeRelationIf)aChange).getRelatingObject());						
			}
			// same MSO object owner?
			if(bFlag) 
			{
				// if checkout of old changes should be evaluated 
				bFlag = !checkout || (aChange.getUpdateMode()==getUpdateMode());
				
				// always allow loopbacks and rollbacks
				bFlag |= aChange.isLoopbackMode() || aChange.isRollbackMode();
				
			}
		}
		return bFlag;
	}
	
	private boolean record(IChangeIf aChange, boolean checkout, boolean undo)
	{
		// initialize flag
		boolean bFlag = false;
		
		// get list
		List<IChangeIf> list = m_map.get(aChange.getObjectId());
		
		/*
		for(String key : m_map.keySet())
		{
			if(key.equals(aChange.getObjectId())) {
				System.out.println("Exists");
			}
		}
		*/
		
		// a rollback or loopback has occurred and changes should be checked out?
		if(checkout && undo)
		{
			// found recorded changes?
			if(list!=null)
			{	        	
				/* a rollback or loopback mode means that 
	        	 * changes made in the changed object so far 
	        	 * are discarded. Hence, current list should 
	        	 * be cleared */
				m_map.remove(aChange.getObjectId());
				m_changes.removeAll(list);
				
				// set dirty flag
				bFlag = true;
				
			} 
			else
			{
				// found no recorded changes, notify
				m_logger.debug("Did not find any recorded changes for " + aChange.getObject());
			}			
		} 
		else
		{
			// add list to changes?
			if(list==null)
			{
				list = new ArrayList<IChangeIf>();
				m_map.put(aChange.getObjectId(), list);				
			}
			// initialize
			int mask = aChange.getMask();
			
			// find first index in mapped (short) list (maximum one occurrence exist)
			int index = find(list, 0, mask);
			
			// add or replace?
			if(index==-1)
			{
				// add
				list.add(aChange);				
			}
			else
			{
				// replace
				list.set(index, aChange);
			}
			
			
			// find first occurrences of mask in list
			index = find(m_changes, 0, mask);
			// find all equal masks 
			while(index>-1){
				// get change
				IChangeIf it = m_changes.get(index);
				// is same data object as recorded change?
				if(it.getObject() == aChange.getObject())
				{
					// replace
					m_changes.remove(index);
					m_changes.add(aChange);

					// set dirty flag
					bFlag = true;
					
				}
				// search again, starting at next index
				// find all occurrences of mask in list
				index++;
				index = find(m_changes, index, mask);
			}
			
			// add?
			if(!bFlag)
			{
				// add
				m_changes.add(aChange);				

				// set dirty flag
				bFlag = true;
				
			}
			
		}		
		
		// append to dirty flag
		m_isDirty |= bFlag;
		
		// finished
		return bFlag;
		
	}
	
	private boolean add(List<IChangeObjectIf> list, IChangeIf aChange)
	{
		
		if(aChange instanceof IChangeObjectIf && !list.contains(aChange))
		{
			return list.add((IChangeObjectIf)aChange);
		}
		return false;
	}

	
	/**
	 * Get first change with given flags set.
	 * 
	 * @param list - the list with changes.
	 * @param flags - the flags to match.
	 * @return Returns first change with given flags set.
	 */
	private IChangeIf get(List<IChangeIf> list, int flags)
	{
		list = get(list,flags,false,true);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * Get a list of matching changes.
	 * 
	 * @param list - the list to search
	 * @param mask - the mask to match
	 * @param exact - math the mask exactly (equal), or partly (bitwise and)
	 * @param head - return first found only
	 * @return Returns a list of matching changes
	 */
	private List<IChangeIf> get(List<IChangeIf> list, int mask, boolean exact, boolean head)
	{
		// initialize
		List<IChangeIf> found = new Vector<IChangeIf>();
		// add list to changes?
		if(list!=null)
		{
			// search for mask
			for(IChangeIf it : list)
			{
				if(exact)
				{
					if(it.getMask()==mask) 
					{
						found.add(it);
						if(head) break;
					}
				}
				else
				{
					if(isFlagSet(mask,it.getMask())) 
					{
						found.add(it);
						if(head) break;
					}					
				}
			}
		}		
		// finished
		return found;		
	}
	
	/**
	 * Get index of first change with mask equal to flags (exact match).
	 * 
	 * @param list - the list with changes.
	 * @param flags - the flags to match.
	 * @return Returns index of the first change with given flags set.
	 */
	private int find(List<IChangeIf> list, int offset, int flags)
	{
		List<Integer> idx = find(list,offset,flags,true,true);
		return idx.size() > 0 ? idx.get(0) : -1;
	}
	
	/**
	 * Get the index of matching changes.
	 * 
	 * @param list - the list to search
	 * @param mask - the mask to match
	 * @param exact - math the mask exactly (equal), or partly (bitwise and)
	 * @param head - return first found only
	 * @return Returns a list of matching changes
	 */
	private List<Integer> find(List<IChangeIf> list, int offset, int mask, boolean exact, boolean head)
	{
		// initialize
		List<Integer> found = new Vector<Integer>();
		// add list to changes?
		if(list!=null)
		{
			// get size
			int size = list.size();
			// search for mask
			for(int i=offset;i<size;i++)
			{
				if(exact)
				{
					if(list.get(i).getMask()==mask) 
					{
						found.add(i);
						if(head) break;
					}
				}
				else
				{
					if(isFlagSet(mask,list.get(i).getMask())) 
					{
						found.add(i);
						if(head) break;
					}					
				}
			}
		}		
		// finished
		return found;		
	}
	
    private IChangeAttributeIf getFilter(IMsoAttributeIf<?> attr) {
    	for(IChangeIf it : m_filters) {
    		if(it instanceof IChangeAttributeIf) {
    			if(((IChangeAttributeIf)it).getMsoAttribute() == attr) return (IChangeAttributeIf)it;
    		}
    	}
    	return null;
    }

    private List<IChangeRelationIf> getFilter(IMsoRelationIf<?> refObj) {
    	List<IChangeRelationIf> list = new Vector<IChangeRelationIf>(2);
    	for(IChangeIf it : m_filters) {
    		if(it instanceof IChangeRelationIf) {
    			if(((IChangeRelationIf)it).equals(refObj)) list.add((IChangeRelationIf)it);
    		}
    	}
    	return list;
    }

    /**
     * This method calculates the change object mask,
     * update, loopback and rollback modes from changes
     */
    private void calculate()
    {
    	
    	// reset current values
		m_mask = 0;
		m_seqNo = -1;
		m_isLoopbackMode = false;
		m_isRollbackMode = false;
		
		// loop over all changes
		for(IChangeIf it : m_changes)
		{

			// search for lowest and highest sequence number 
			long seqNo = it.getSeqNo();
			m_seqNo = (m_seqNo==-1 ? seqNo : Math.min(seqNo,m_seqNo));
			
			// append masks
			m_mask |= it.getMask();
	        /* =========================================
	         * Get union of update loopback flags. The
	         * change object is a loopback only as long 
	         * as all changes are loopbacks. 
	         * ========================================= */				
			// found non-dominant loopback flag?
			if(m_isLoopbackMode)
			{
				// continue to replace flag until false of finished
				m_isLoopbackMode = it.isLoopbackMode();
			}
	        /* =========================================
	         * Get union of update rollback flags. The
	         * change object is a rollback only as long 
	         * as all changes are rollbacks. 
	         * ========================================= */				
			// found non-dominant loopback flag?
			if(m_isRollbackMode)
			{
				// continue to replace flag until false of finished
				m_isRollbackMode = it.isRollbackMode();
			}
		}
		// reset dirty flag
		m_isDirty = false;
    }
    
    private static MsoEventType getType(int mask)
    {
    	for(MsoEventType it : MsoEventType.values())
    	{
    		if(it.ordinal()==mask) return it;
    	}
    	return MsoEventType.EMPTY_EVENT;
    }
    
    /**
     * Create copy of list, sort if sorting is enabled, and return it.
     * @param <T> - the list data type
     * @param list - the list to create a copy of and sort
     * @return Returns a sorted copy of given list.
     */
    private <T extends Comparable<? super T>> List<T> sort(List<T> list) 
    {
    	list = clone(list);
    	if(m_isSorted) Collections.sort(list);
    	return list;
    }

    /**
     * Clone given list
     * @param <T> - the list data type
     * @param list - the list to clone
     * @return Returns a copy of the given list
     */
    private static <T extends Comparable<? super T>> List<T> clone(List<T> list) 
    {
    	return new Vector<T>(list);
    }
    
    public static class SeqNoGen implements ISeqNoGenIf 
    {
    	private long m_nextSeqNo = 0;
    	private boolean m_isSeqNoEnabled = true;
    	
    	public SeqNoGen(long nextSeqNo)
    	{
    		m_nextSeqNo = nextSeqNo;
    	}
    	
        /**
         * Create a new sequence number 
         * @return Returns next sequence number or -1 is sequence number generation is off.
         */
        public long createSeqNo()
        {
        	long seqNo = -1;
        	if(m_isSeqNoEnabled)
        	{
	        	// get sequence number
	        	seqNo = m_nextSeqNo;
	        	// prepare next sequence number
	        	if(m_nextSeqNo==Long.MAX_VALUE)
	        	{
	        		m_nextSeqNo = 0;
	        	}
	        	else 
	        	{
	        		m_nextSeqNo++;
	        	}
        	}
        	return seqNo;
        	
        }

		@Override
		public long getNextSeqNo() {
			return m_isSeqNoEnabled ? m_nextSeqNo : -1;
		}

		@Override
		public boolean isSeqNoEnabled() {
			return m_isSeqNoEnabled;
		}

		@Override
		public void setSeqNoEnabled(boolean isEnabled) {
			m_isSeqNoEnabled = isEnabled;
		}
    	
    }
    
}
