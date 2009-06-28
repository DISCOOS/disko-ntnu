package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.event.MsoEvent.MsoEventType;
import org.redcross.sar.mso.util.EnumHistory;
import org.redcross.sar.mso.ChangeImpl;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.*;

import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

/**
 * Generic class for all MSO attributes
 */
@SuppressWarnings("unchecked")
public abstract class AttributeImpl<T> implements IMsoAttributeIf<T>
{
    private final String m_name;
    private final int m_cardinality;

    private int m_indexNo;
    private int m_changeCount = 0;
    private boolean m_isRequired = false;
    private boolean m_isRollbackMode = false;
    private boolean m_isLoopbackMode = false;

	protected final Class<T> m_class;
    protected final AbstractMsoObject m_owner;
    protected final IMsoModelIf m_model;

    protected T m_localValue;
    protected T m_remoteValue;
    protected IData.DataOrigin m_origin = IData.DataOrigin.NONE;

    protected AttributeImpl(Class<T> aClass, AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, T theValue)
    {
        m_class = aClass;
        m_owner = theOwner;
    	if(m_owner==null)
    	{
    		throw new IllegalArgumentException("MsoListImpl must have a owner");
    	}
        m_model = m_owner.getModel();
        m_name = theName.toLowerCase();
        m_cardinality = theCardinality;
        m_indexNo = theIndexNo;
        if (theValue != null)
        {
            setAttrValue(theValue, true);
        }
    }

    public String getName()
    {
        return m_name;
    }

    protected T getAttrValue()
    {
        return m_origin == IData.DataOrigin.LOCAL ? m_localValue : m_remoteValue;
    }

    public IData.DataOrigin getOrigin()
    {
        return m_origin;
    }

	@Override
	public boolean isOrigin(IData.DataOrigin origin) {
		if(m_origin!=null)
		{
			return m_origin.equals(origin);
		}
		// failed to compare
		return false;
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
		return isOrigin(IData.DataOrigin.CONFLICT);
	}
		
	@Override
	public boolean isOriginMixed() {
		return false;
	}
	
	@Override
	public boolean isLoopbackMode() {
		return m_isLoopbackMode;
	}

	@Override
	public boolean isRollbackMode() {
		return m_isRollbackMode;
	}
	
	@Override
	public boolean isDeleted() {
		return m_owner.isDeleted();
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
		if(isChanged())
		{
			if(m_origin.equals(DataOrigin.CONFLICT))
			{
				state = DataState.CONFLICT;
			}
			else 
			{
				state = DataState.CHANGED;
			}
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

	@Override
	public abstract MsoDataType getDataType();

	/**
	 * Get attribute object id.
	 * 
	 * @return Returns attribute object id.
	 */
	public String getObjectId() {
		return getOwnerObject().getObjectId() + "#" + getName();
	}
	
	@Override
	public MsoClassCode getClassCode() {
		return MsoClassCode.CLASSCODE_NOCLASS;
	}

    public int compareTo(IData anObject)
    {
    	if(anObject instanceof IMsoAttributeIf)
    	{
    		if(anObject.getClass().equals(getClass()))
    		{
    			return ((IMsoAttributeIf)anObject).getIndexNo() - getIndexNo(); 
    		}
    	}
		return anObject != null ? anObject.hashCode() - hashCode(): -1;
    }

	Class getAttributeClass()
    {
        return m_class;
    }

    public boolean isChanged() {
    	return m_origin == IData.DataOrigin.LOCAL 
    		|| m_origin == IData.DataOrigin.CONFLICT;
    }

    public boolean isChangedSince(int changeCount)
    {
        return (m_changeCount>=changeCount);
    }
    
    public int getChangeCount()
    {
        return m_changeCount;
    }

    /**
     * This method increments the change counter and creates a attribute 
     * change instance that is returned and stored locally.
     * @param aMode - the model update mode when change occurred.
     * @return Returns a IChangeAttributeIf instance.
     */
    protected IChangeIf onChangeOccurred(UpdateMode aMode) {
    	// increment change count
    	m_changeCount++;
    	// create attribute change instance 
    	IChangeIf change = new ChangeImpl.ChangeAttribute(this, aMode,get(),
				MsoEventType.MODIFIED_DATA_EVENT,
				isLoopbackMode(),isRollbackMode());
    	// finished
    	return change;
    }

    public IMsoObjectIf getOwnerObject() {
    	return m_owner;
    }
    
    public T get()
    {
    	return getAttrValue();
    }
    
    public T getLocalValue()
    {
    	return m_localValue;
    }
    
    public T getRemoteValue()
    {
    	return m_remoteValue;
    }
    
    public void set(T aValue)
    {
        if (aValue!=null && !m_class.isAssignableFrom(aValue.getClass()))
        {
            throw new ClassCastException("Cannot cast " + aValue.getClass() + " to " + m_class.toString());
        }
        setAttrValue(aValue, false);
    }

    protected void setAttrValue(T aValue)
    {
        setAttrValue(aValue, false);
    }

    protected boolean isEqual(T v1, T v2)
    {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }

    protected void setAttrValue(T aValue, boolean isCreating)
    {
    	/* ========================================================
    	 * Setting a new attribute value is dependent on the
    	 * update mode of the MSO model.
    	 *  
    	 * If the model is in REMOTE_UPDATE_MODE, a change from 
    	 * the server is registered and the attribute value should 
    	 * be analyzed to detect any conflicts between local value 
    	 * (private) and the server value (shared). 
    	 * 
    	 * If the local value has been changed (isChanged() is true), 
    	 * and the model is in REMOTE_UPDATE_MODE, this update may 
    	 * be a loopback. Since loopback updates are just a ACK
    	 * from the server, the attribute value is not changed. Hence,
    	 * IMsoClientUpdateListener listeners is not required to fetch
    	 * the attribute value. However, loopback updates may be used to
    	 * indicate to the user that the commit was successful.
    	 *  
    	 * If the model is in LOCAL_UPDATE_MODE, the attribute 
    	 * value state should be updated according to the passed 
    	 * attribute value.
    	 * 
    	 * If a the local value after a change equals the remote 
    	 * value, the change is a ROLLBACK.
    	 *
    	 * Server value and local value should only be present
    	 * concurrently during a local update sequence (between two
    	 * commit or rollback transactions), or if a conflict has
    	 * occurred (remote update during a local update sequence).
    	 * Hence, if the attribute is in a LOCAL or CONFLICTING,
    	 * both values will be present. Else, only server value will
    	 * be present (REMOTE state).
    	 *
    	 * A commit() or rollback() will reset the local value
    	 * to null to ensure state consistency.
    	 * 
    	 * ======================================================== */

        UpdateMode updateMode = m_model.getUpdateMode();
        IData.DataOrigin newOrigin;
        boolean isDirty = false;
        boolean isLoopback = false;
        boolean isRollback = false;

        // translate
        switch (updateMode)
        {
            case REMOTE_UPDATE_MODE:
            {
            	/* ===========================================================
            	 * Update to server value state with conflict detection.
            	 *
            	 * If the model is in REMOTE_UPDATE_MODE, this indicates that
            	 * an external change is detected (message queue update), and the
            	 * model should be update accordingly.
            	 *
            	 * If the attribute is in local state, the local value may
            	 * be different from the server value. Local changes are made
            	 * by the user (GUI) or a local service (the application). When a
            	 * remote update occurs (a change received from the message queue),
            	 * the new attribute state depends on the new server value
            	 * and current (local) value. If these are different, a conflict has
            	 * occurred. This is indicated by setting the attribute state
            	 * to CONFLICTING. Methods for resolving conflicts are supplied
				 * by this class. If the new server value and local value
            	 * are equal, the attribute state is changed to REMOTE.
            	 * 
            	 * =========================================================== */

            	// check if a conflict has occurred?
            	boolean isConflict = (m_origin == IData.DataOrigin.LOCAL
            			|| m_origin == IData.DataOrigin.CONFLICT) ?
            			!isEqual(m_localValue, aValue) : false;

            	// get next state
                newOrigin = isConflict ?
                		  IData.DataOrigin.CONFLICT
                		: IData.DataOrigin.REMOTE;

                // any change?
                if (!isEqual(m_remoteValue, aValue))
                {
                    m_remoteValue = aValue;
                    isDirty = true;
                }

                // no conflict found?
                if(!isConflict)
                {
                    isLoopback = (isChanged() && isEqual(m_localValue, aValue));
                	m_localValue = null;
                }

                // notify change
                isDirty |= isConflict;
                
                break;
            }
            default: // LOCAL_UPDATE_MODE
            {

            	/* ===========================================================
            	 * Update attribute to the appropriate value state
            	 *
            	 * The default update mode is LOCAL_UPDATE_MODE. This mode
            	 * indicates that the change originates from a GUI (user) or
            	 * Service (application) invocation, not from an external
            	 * change made on a different model instance. If the new
            	 * value equals the server value, the attribute value state
            	 * should be set to server state. If the new value is different
            	 * from the server value, the attribute value state should be
            	 * set to local state.
            	 * =========================================================== */

            	// potential rollback occurred?
            	if (isEqual(m_remoteValue, aValue))
                {
            		// set REMOTE origin
                    newOrigin = IData.DataOrigin.REMOTE;
                    // get change flaf
                    isDirty = (m_localValue!=null || m_origin!=newOrigin);
                    // a change implies a rollback 
                    isRollback = isDirty;
                    // reset local value
                    m_localValue = null;
                } else
                {
                    newOrigin = IData.DataOrigin.LOCAL;
                    if (!isEqual(m_localValue, aValue))
                    {
                        m_localValue = aValue;
                        isDirty = true;
                    }
                }            	
            }
            break;
        }
        if (m_origin != newOrigin)
        {
            m_origin = newOrigin;
            isDirty = true;
        }
        // set loopback mode
        m_isLoopbackMode = isLoopback;
        // set rollback mode
        m_isRollbackMode = isRollback;
        // notify change
        if (!isCreating && (isDirty || isLoopback || isRollback))
        {
        	m_owner.registerModifiedData(onChangeOccurred(updateMode));
        }
    }

    /**
     * Set index without forced renumbering
     *
     * @param anIndexNo The index number
     */
    public void renumber(int anIndexNo)
    {
        m_indexNo = anIndexNo;
    }

    public void setIndexNo(int anIndexNo)
    {
        m_owner.rearrangeAttribute(this, anIndexNo);
    }

    public int getIndexNo()
    {
        return m_indexNo;
    }

    public Vector<T> getConflictingValues()
    {
        if (m_origin == IData.DataOrigin.CONFLICT)
        {
            Vector<T> retVal = new Vector<T>(2);
            retVal.add(m_remoteValue);
            retVal.add(m_localValue);
            return retVal;
        }
        return null;
    }
    
    public boolean commit() throws TransactionException
    {
    	// get dirty flag
        boolean isDirty = isChanged();
        // is changed?
        if(isDirty)
        {
        	// get change source
        	IChangeRecordIf changes = m_model.getChanges(m_owner);
        	// add this as partial commit
        	changes.addFilter(this);
	        // increment change count
	        onChangeOccurred(m_model.getUpdateMode());
	        // commit changes
        	m_owner.getModel().commit(changes);
        }
        return isDirty;
    }
    
    public boolean rollback()
    {
        if(isChanged())
        {
        	// get change update mode flag
        	boolean bFlag = !m_model.isUpdateMode(UpdateMode.LOCAL_UPDATE_MODE);
        	// ensure that model is in local update mode
        	if(bFlag) m_model.setLocalUpdateMode();
        	// reset loopback mode
            m_isLoopbackMode = false;
            // set rollback mode
            m_isRollbackMode = true;
            // reset local value
	        m_localValue = null;
	        // update state to REMOTE
	        m_origin = IData.DataOrigin.REMOTE;
	        // notify owner
            m_owner.registerModifiedData(onChangeOccurred(m_model.getUpdateMode()));
        	// restore previous update mode?
            if(bFlag) m_model.restoreUpdateMode();
            // success
            return true;
        }
        // failure
        return false;
    }
    
    public IChangeAttributeIf getChange() {
    	IChangeRecordIf rs = m_model.getChanges(m_owner);
    	if(rs!=null)
    	{
    		return rs.get(this);
    	}
		return null;
    }

    private boolean acceptConflicting(IData.DataOrigin aState)
    {
        if (m_origin == IData.DataOrigin.CONFLICT)
        {
            if (aState == IData.DataOrigin.LOCAL)
            {
            	/* ==========================================================
            	 * resolve conflict as a local value state (keep local value)
            	 *
            	 * Both server and local values must be kept to enable
            	 * future conflict detection
            	 *
            	 * ========================================================== */

            	// NOP

            } else
            {
            	/* ==========================================================
            	 * resolve conflict as a server value state (overwrite local value)
            	 *
            	 * Since server value is chosen, the local value must be
            	 * erased
            	 *
            	 * ========================================================== */

                m_localValue = null;

            }
            m_origin = aState;
            // notify owner
            m_owner.registerModifiedData(onChangeOccurred(m_model.getUpdateMode()));
            // finished
            return true;
        }
        return false;
    }

    public boolean acceptLocalValue()
    {
        return acceptConflicting(IData.DataOrigin.LOCAL);
    }

    public boolean acceptRemoteValue()
    {
        return acceptConflicting(IData.DataOrigin.REMOTE);
    }

    public boolean isUncommitted()
    {
        return m_origin == IData.DataOrigin.LOCAL;
    }

    public boolean isGisAttribute()
    {
        return false;
    }

    public void setRequired(boolean aValue)
    {
        m_isRequired = aValue;
    }

    public boolean isRequired()
    {
        return m_isRequired;
    }

    public int getCardinality()
    {
        return m_cardinality;
    }

    public boolean validate() {
    	if(m_cardinality>0) {
    		return (getAttrValue()!=null);
    	}
    	return true;
    }
    
    protected abstract T getNaNValue(); 

    public static class MsoBoolean extends AttributeImpl<Boolean> implements IMsoBooleanIf
    {
        public MsoBoolean(AbstractMsoObject theOwner, String theName)
        {
            super(Boolean.class, theOwner, theName, 0, Integer.MAX_VALUE, false);
        }

        public MsoBoolean(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Boolean.class, theOwner, theName, 0, theIndexNo, false);
        }

        public MsoBoolean(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Boolean aBool)
        {
            super(Boolean.class, theOwner, theName, theCardinality, theIndexNo, aBool);
        }

        @Override
        public void set(Boolean aValue)
        {
            super.set(aValue);
        }

        public void setValue(boolean aValue)
        {
            setAttrValue(aValue);
        }

        public void setValue(Boolean aValue)
        {
            setAttrValue(aValue);
        }

        public boolean booleanValue()
        {
            return getAttrValue();
        }

		@Override
		public MsoDataType getDataType() {
			return MsoDataType.BOOLEAN;
		}

		@Override
		protected Boolean getNaNValue() {
			return null;
		}
		
    }

    public static class MsoInteger extends AttributeImpl<Integer> implements IMsoIntegerIf
    {
        private boolean m_isSequenceNumber;

        public MsoInteger(AbstractMsoObject theOwner, String theName)
        {
            this (theOwner, theName, false);
        }

        public MsoInteger(AbstractMsoObject theOwner, String theName, boolean isSequenceNumber)
        {
            super(Integer.class, theOwner, theName, 0,Integer.MAX_VALUE, 0);
            m_isSequenceNumber = isSequenceNumber;
        }

        public MsoInteger(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            this(theOwner,theName,false,theIndexNo);
        }

        public MsoInteger(AbstractMsoObject theOwner, String theName, boolean isSequenceNumber, int theIndexNo)
        {
            super(Integer.class, theOwner, theName, 0, theIndexNo, 0);
            m_isSequenceNumber = isSequenceNumber;
        }

        public MsoInteger(AbstractMsoObject theOwner, String theName, int theIndexNo, Integer anInt)
        {
            this(theOwner, theName,false, 0, theIndexNo, anInt);
        }

        public MsoInteger(AbstractMsoObject theOwner,  String theName, boolean isSequenceNumber, int theCardinality, int theIndexNo, Integer anInt)
        {
            super(Integer.class, theOwner, theName, theCardinality, theIndexNo, anInt);
            m_isSequenceNumber = isSequenceNumber;
        }

        @Override
        public void set(Integer aValue)
        {
            super.set(aValue);
            processSequenceNumberAttribute();
        }

        public void setValue(int aValue)
        {
            setAttrValue(aValue);
            processSequenceNumberAttribute();
        }

        public void setValue(Integer aValue)
        {
            setAttrValue(aValue);
            processSequenceNumberAttribute();
        }

        private void processSequenceNumberAttribute()
        {
            if (m_isSequenceNumber)
            {
                @SuppressWarnings("unused")
				ISerialNumberedIf numberedOwner = (ISerialNumberedIf)m_owner;
            }
        }

        public int intValue()
        {
            return getAttrValue();
        }

        public long longValue()
        {
            return getAttrValue();
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.INTEGER;
		}
        
		@Override
		protected Integer getNaNValue() {
			return null;
		}
		
    }

    public static class MsoDouble extends AttributeImpl<Double> implements IMsoDoubleIf
    {
        public MsoDouble(AbstractMsoObject theOwner, String theName)
        {
            super(Double.class, theOwner, theName, 0,Integer.MAX_VALUE, (double) 0);
        }

        public MsoDouble(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Double.class, theOwner, theName, 0,theIndexNo, (double) 0);
        }

        public MsoDouble(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Double aDouble)
        {
            super(Double.class, theOwner, theName, theCardinality, theIndexNo, aDouble);
        }

        @Override
        public void set(Double aValue)
        {
            super.set(aValue);
        }

        public void setValue(long aValue)
        {
            setAttrValue((double) aValue);
        }

        public void setValue(Integer aValue)
        {
            setAttrValue(aValue.doubleValue());
        }

        public void setValue(Long aValue)
        {
            setAttrValue(aValue.doubleValue());
        }

        public void setValue(Float aValue)

        {
            setAttrValue(aValue.doubleValue());
        }

        public void setValue(Double aValue)
        {
            setAttrValue(aValue);
        }

        public int intValue()
        {
            return getAttrValue().intValue();
        }

        public long longValue()
        {
            return getAttrValue().longValue();
        }

        public double doubleValue()
        {
            return getAttrValue();
        }

        public double floatValue()
        {
            return getAttrValue();
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.DOUBLE;
		}
        
		@Override
		protected Double getNaNValue() {
			return null;
		}
    }

    public static class MsoString extends AttributeImpl<String> implements IMsoStringIf
    {
        public MsoString(AbstractMsoObject theOwner, String theName)
        {
            super(String.class, theOwner, theName, 0, Integer.MAX_VALUE, "");
        }

        public MsoString(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(String.class, theOwner, theName, 0, theIndexNo, "");
        }

        public MsoString(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, String aString)
        {
            super(String.class, theOwner, theName, theCardinality, theIndexNo, aString);
        }

        @Override
        public void set(String aValue)
        {
            super.set(aValue);
        }

        public void setValue(String aValue)
        {
            setAttrValue(aValue);
        }

        public String getString()
        {
            return getAttrValue();
        }

        @Override
		public MsoDataType getDataType() {
			return MsoDataType.STRING;
		}
        
		@Override
		protected String getNaNValue() {
			return null;
		}
		
    }

    public static class MsoCalendar extends AttributeImpl<Calendar> implements IMsoCalendarIf
    {
        public MsoCalendar(AbstractMsoObject theOwner, String theName)
        {
//            super(Calendar.class, theOwner, theName, Integer.MAX_VALUE, Calendar.getInstance());
            super(Calendar.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoCalendar(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Calendar.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoCalendar(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Calendar aCalendar)
        {
            super(Calendar.class, theOwner, theName, theCardinality, theIndexNo, aCalendar);
        }

        public void setValue(int year, int day, String aDTG) throws IllegalMsoArgumentException
        {
            Calendar cal = DTG.DTGToCal(year,day,aDTG);
            setAttrValue(cal);
        }

        public void setValue(int year, int day, Number aDTG) throws IllegalMsoArgumentException
        {
            Calendar cal = DTG.DTGToCal(year,day,aDTG.longValue());
            setAttrValue(cal);
        }

        @Override
        public void set(Calendar aDTG)
        {
            super.set(aDTG);
        }

        public Calendar getCalendar()
        {
            return getAttrValue();
        }

        public String getDTG()
        {
            return DTG.CalToDTG(getAttrValue());
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.CALENDAR;
		}
        
		@Override
		protected Calendar getNaNValue() {
			return null;
		}
    }

    public static class MsoPosition extends AttributeImpl<Position> implements IMsoPositionIf
    {
        public MsoPosition(AbstractMsoObject theOwner, String theName)
        {
            super(Position.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoPosition(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Position.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoPosition(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Position aPosition)
        {
            super(Position.class, theOwner, theName, theCardinality, theIndexNo, aPosition);
        }

        @Override
        public void set(Position aPosition)
        {
            setAttrValue(aPosition);
        }
        
        @Override
        public void set(String anId, Point2D.Double aPoint)
        {
            Position pos = new Position(anId, aPoint);
            setAttrValue(pos);
        }

		@Override
		public MsoDataType getDataType() {
			return MsoDataType.POSITION;
		}
        
		@Override
		protected Position getNaNValue() {
			return new Position(null);
		}
		
    }

    public static class MsoTimePos extends AttributeImpl<TimePos> implements IMsoTimePosIf
    {
        public MsoTimePos(AbstractMsoObject theOwner, String theName)
        {
            super(TimePos.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoTimePos(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(TimePos.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoTimePos(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, TimePos aTimePos)
        {
            super(TimePos.class, theOwner, theName, theCardinality, theIndexNo, aTimePos);
        }

        public boolean isGisAttribute()
        {
            return true;
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.TIMEPOS;
		}
        
		@Override
		protected TimePos getNaNValue() {
			return new TimePos();
		}
    }

    public static class MsoPolygon extends AttributeImpl<Polygon> implements IMsoPolygonIf
    {
        public MsoPolygon(AbstractMsoObject theOwner, String theName)
        {
            super(Polygon.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoPolygon(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Polygon.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoPolygon(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Polygon aPolygon)
        {
            super(Polygon.class, theOwner, theName, theCardinality, theIndexNo, aPolygon);
        }

        public boolean isGisAttribute()
        {
            return true;
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.POLYGON;
		}
        
		@Override
		protected Polygon getNaNValue() {
			return new Polygon(null);
		}
    }

    public static class MsoRoute extends AttributeImpl<Route> implements IMsoRouteIf
    {
        public MsoRoute(AbstractMsoObject theOwner, String theName)
        {
            super(Route.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoRoute(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Route.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoRoute(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Route aRoute)
        {
            super(Route.class, theOwner, theName, theCardinality, theIndexNo, aRoute);
        }

        public boolean isGisAttribute()
        {
            return true;
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.ROUTE;
		}
        
		@Override
		protected Route getNaNValue() {
			return new Route(null);
		}
    }

    public static class MsoTrack extends AttributeImpl<Track> implements IMsoTrackIf
    {
        public MsoTrack(AbstractMsoObject theOwner, String theName)
        {
            super(Track.class, theOwner, theName, 0, Integer.MAX_VALUE, null);
        }

        public MsoTrack(AbstractMsoObject theOwner, String theName, int theIndexNo)
        {
            super(Track.class, theOwner, theName, 0, theIndexNo, null);
        }

        public MsoTrack(AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, Track aTrack)
        {
            super(Track.class, theOwner, theName, theCardinality, theIndexNo, aTrack);
        }

        public boolean isGisAttribute()
        {
            return true;
        }
        
		@Override
		public MsoDataType getDataType() {
			return MsoDataType.TRACK;
		}
        
		@Override
		protected Track getNaNValue() {
			return new Track(null);
		}
    }

    public static class MsoEnum<E extends Enum<E>> extends AttributeImpl<E> implements IMsoEnumIf<E>
    {

    	/**
    	 * Initial enum value
    	 */
    	private final E m_initialValue;

        /**
         * List of status changes since creation
         */
        private final EnumHistory<E> m_history = new EnumHistory<E>();

        /* ====================================================
         * Constructors
         * ==================================================== */

        public MsoEnum(AbstractMsoObject theOwner, String theName, int theCardinality, E anInstance)
        {
        	// forward
            super((Class<E>)anInstance.getClass(), theOwner, theName, theCardinality, Integer.MAX_VALUE, anInstance);
            // prepare
            m_initialValue = anInstance;
        }

        /* ====================================================
         * Public methods
         * ==================================================== */

        @Override
        public void set(E anEnum)
        {
        	set(anEnum,null);
        }

        public void set(String aName)
        {
        	set(aName,null);
        }

        public void set(E anEnum, Calendar aTime)
        {
            // forward
        	addHistory(anEnum, aTime);
        	// forward
            super.setAttrValue(anEnum);
        }

        public void set(String aName, Calendar aTime)
        {
            E anEnum = enumValue(aName);
            // validate
            if (anEnum == null)
            {
                //throw new MsoException("Cannot set enum value " + aName + " to " + this); // todo sjekk !!!!!
            }
            // forward
        	addHistory(anEnum, aTime);
            // forward
            super.setAttrValue(enumValue(aName));
        }

        public String getEnumName()
        {
            return getAttrValue().name();
        }

        public String getInternationalName()
        {
            return Internationalization.translate(getAttrValue());
        }

        public E enumValue(String aName)
        {
            E retVal;
            try
            {
                retVal = (E) Enum.valueOf(m_class, aName);
            }
            catch (NullPointerException e)
            {
                return null;
            }
            return retVal;
        }

        public E getInitialValue() {
        	return m_history.getHeadValue();
        }

        public Calendar getInitialTime() {
        	return m_history.getHeadTime();
        }

		@Override
		protected E getNaNValue() {
			return m_class.getEnumConstants()[0];
		}
		
        /**
         * Get history of specified status
         *
         * @param aStatus - the status to get history of
         *
         */
        public List<Calendar> getHistory(E aStatus)
        {
        	return m_history.getHistory(aStatus);
        }

        /**
         * Get first occurrence of specified status
         *
         * @param aStatus - The status occurrence
         *
         * @return The time of status change
         */
        public Calendar getFirstTime(E aStatus)
        {
        	return m_history.getFirstTime(aStatus);
        }

        /**
         * Get last occurrence of specified status
         *
         * @param aStatus - The status occurrence
         *
         * @return The time of status change
         */
        public Calendar getLastTime(E aStatus) {
        	return m_history.getLastTime(aStatus);
        }

        /**
         * Get duration of given unit status. </p>
         *
         * @param aStatus - The status to get duration for
         * @param total - If <code>true</code> the sum of all durations for a given status
         * is returned, the duration of the last occurrence otherwise.
         *
         * @return Duration (second)
         */
    	public double getDuration(E aStatus, boolean total) {
    		return m_history.getDuration(aStatus,total);
    	}

		public double getDuration(EnumSet<E> list, boolean total) {
			return m_history.getDuration(list,total);
		}

		@Override
		public MsoDataType getDataType() {
			return MsoDataType.ENUM;
		}

        /* ====================================================
         * Protected methods
         * ==================================================== */

        /**
         * set time stamp for initial status
         *
         * @param Calendar t - the time of creation
         */
        protected void setInitialTime(Calendar t) {
        	if(m_owner.m_model.isUpdateMode(UpdateMode.REMOTE_UPDATE_MODE)) {
        		m_history.setHead(m_initialValue, t);
        	}
        }


        /**
         * Add a status change to status history.
         *
         * Only possible if MSO model is in REMOTE_UPDATE_MODE.
         *
         * @param aStatus
         * @param aTime
         */
        protected void addHistory(E aStatus, Calendar aTime)
        {
            if(aTime!=null && m_owner.m_model.isUpdateMode(UpdateMode.REMOTE_UPDATE_MODE)) {
            	m_history.add(aStatus,aTime);
            }
        }

    }

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

        AttributeImpl attribute = (AttributeImpl) o;

        if (m_indexNo != attribute.m_indexNo)
        {
            return false;
        }
        if (m_isRequired != attribute.m_isRequired)
        {
            return false;
        }
        if (m_class != null ? !m_class.equals(attribute.m_class) : attribute.m_class != null)
        {
            return false;
        }
        if (m_localValue != null ? !m_localValue.equals(attribute.m_localValue) : attribute.m_localValue != null)
        {
            return false;
        }
        if (m_name != null ? !m_name.equals(attribute.m_name) : attribute.m_name != null)
        {
            return false;
        }
        if (m_owner != null ? !m_owner.getObjectId().equals(attribute.m_owner.getObjectId()) : attribute.m_owner != null)
        {
            return false;
        }
        if (m_remoteValue != null ? !m_remoteValue.equals(attribute.m_remoteValue) : attribute.m_remoteValue != null)
        {
            return false;
        }
        if (m_origin != attribute.m_origin)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result = 7;
        result = (m_class != null ? m_class.hashCode() : 0);
        result = 31 * result + (m_owner != null ? m_owner.getObjectId().hashCode() : 0); // To avoid eternal loop
        result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
        result = 31 * result + m_indexNo;
        result = 31 * result + (m_isRequired ? 1 : 0);
        result = 31 * result + (m_localValue != null ? m_localValue.hashCode() : 0);
        result = 31 * result + (m_remoteValue != null ? m_remoteValue.hashCode() : 0);
        result = 31 * result + (m_origin != null ? m_origin.hashCode() : 0);
        return result;
    }
}
