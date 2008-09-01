package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoModelImpl;
import org.redcross.sar.util.Internationalization;
import org.redcross.sar.util.except.IllegalMsoArgumentException;
import org.redcross.sar.util.mso.*;

import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.Vector;

/**
 * Generic class for all MSO attributes
 */
public abstract class AttributeImpl<T> implements IAttributeIf<T>, Comparable<AttributeImpl<T>>
{
    protected final Class m_class;
    protected final AbstractMsoObject m_owner;
    private final String m_name;
    private int m_indexNo;
    private boolean m_required = false;
    //private boolean m_changed = false;
    private final int m_cardinality;
    private int m_changeCount = 0;

    protected T m_localValue = null;
    protected T m_serverValue = null;
    protected IMsoModelIf.ModificationState m_state = IMsoModelIf.ModificationState.STATE_UNDEFINED;

    protected AttributeImpl(Class aClass, AbstractMsoObject theOwner, String theName, int theCardinality, int theIndexNo, T theValue)
    {
        m_class = aClass;
        m_owner = theOwner;
        m_name = theName.toLowerCase();
        m_cardinality = theCardinality;
        m_indexNo = theIndexNo;
        if (theValue != null)
        {
            setAttrValue(theValue, true);
        }
    }

    /**
     * Compare attribute indices, used for sorting.
     *
     * @param anObject Object to compare
     * @return As {@link Comparable#compareTo(Object)}
     */
    public int compareTo(AttributeImpl<T> anObject)
    {
        return getIndexNo() - anObject.getIndexNo();
    }

    public String getName()
    {
        return m_name;
    }

    protected T getAttrValue()
    {
        return m_state == IMsoModelIf.ModificationState.STATE_LOCAL ? m_localValue : m_serverValue;
    }

    public IMsoModelIf.ModificationState getState()
    {
        return m_state;
    }

    Class getAttributeClass()
    {
        return m_class;
    }

    public int getChangeCount()
    {
        return m_changeCount;
    }
    
    protected void incrementChangeCount() {
    	m_changeCount++;
    }    
    
    public void set(T aValue)
    {
        if (!m_class.isAssignableFrom(aValue.getClass()))
        {
            throw new ClassCastException("Cannot cast " + aValue.getClass() + " to " + m_class.toString());
        }
        setAttrValue(aValue, false);
    }

    protected void setAttrValue(T aValue)
    {
        setAttrValue(aValue, false);
    }

    protected boolean equal(T v1, T v2)
    {
        return v1 == v2 || (v1 != null && v1.equals(v2));
    }

    protected void setAttrValue(T aValue, boolean isCreating)
    {
    	/* ========================================================
    	 * Setting a new attribute value is dependent on the
    	 * update mode of the MSO model. If the model is in 
    	 * LOOPBACK_UPDATE_MODE, the attribute should reflect
    	 * the server value without any conflict detection. If the
    	 * model is in REMOTE_UPDATE_MODE, a change from the server
    	 * is registered and the attribute value should be analyzed
    	 * to detect any conflicts between local value (private) and 
    	 * the server value (shared). If the model is in 
    	 * LOCAL_UPDATE_MODE, the attribute value state should be 
    	 * updated according to the passed attribute value.
    	 * 
    	 * Server value and local value should only be present 
    	 * concurrently during a local update sequence (between two 
    	 * commit() or rollback() invocations), or if a conflict has 
    	 * occurred (remote update during a local update sequence).
    	 * Hence, if the attribute is in a LOCAL or CONFLICTING, 
    	 * both values will be present. Else, only server value will 
    	 * be present (REMOTE state).
    	 *  
    	 * A commit() or rollback() will reset the local value
    	 * to null to ensure state consistency. 
    	 * 
    	 * ======================================================== */    	    	
    	
        IMsoModelIf.UpdateMode updateMode = MsoModelImpl.getInstance().getUpdateMode();
        IMsoModelIf.ModificationState newState;
        boolean valueChanged = false;
        switch (updateMode)
        {
            case LOOPBACK_UPDATE_MODE:
            {
            	/* ===========================================================
            	 * Update to server value state without any conflict detection.
            	 * 
            	 * After a commit, all changes are looped back to the model
            	 * from the message queue. This ensures that only changes
            	 * that are successfully committed to the global message 
            	 * queue becomes valid. If any errors occurred, the message queue 
            	 * is given precedence over local values. 
            	 * 
            	 * Because of limitations in SaraAPI, it is not possible to 
            	 * distinguish between messages committed by this model instance 
            	 * and other model instances. Hence, any change received from 
            	 * the message queue may be a novel change. Because 
            	 * LOOPBACK_UPDATE_MODE is only a proper update mode if the
            	 * received update is a loop back from this model instance, the
            	 * mode can not be used with the current SaraAPI. If used, 
            	 * local changes will be overwritten because conflict detection 
            	 * is disabled in this mode.
            	 * 
            	 * The fix to this problem is to assume that if a commit is
            	 * executed without any exception from the message queue, then all 
            	 * changes was posted and forwarded to all listeners. Hence, the
            	 * attribute value can be put in server mode directly after a commit 
            	 * is executed. 
            	 * 
            	 * 		!!! The postProcessCommit() implements this fix !!!
            	 * 
            	 * If the source of each change could be uniquely identified 
            	 * (at the model instance level), change messages received as a 
            	 * result of a commit by this model, could be group together and 
            	 * forwarded to the model using the LOOPBACK_UPDATE_MODE. This would 
            	 * be the correct and intended usage of this mode.
            	 * 
            	 * ================================================================
            	 * IMPORTANT 
            	 * ================================================================
            	 * 
            	 * This mode is by definition a violation of the SARA Protocol 
            	 * which is based on the assumption that any local changes is only 
            	 * valid when it equals the server value. Hence, REMOTE mode should 
            	 * only be resumed if local value equals server value, or a REMOTE 
            	 * update is explicitly received.
            	 * 
            	 * The use of postProcessCommit() and LOOPBACK mode is therefore
            	 * discarded.
            	 * 
            	 * =========================================================== */
            	
                // only server value is kept
                m_localValue = null;
                
            	// set new state
                newState = IMsoModelIf.ModificationState.STATE_SERVER;
                
                // any change?
                if (!equal(m_serverValue, aValue))
                {
                    m_serverValue = aValue;
                    valueChanged = true;
                }
                
                break;
                
            }
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
            	boolean isConflict = (m_state == IMsoModelIf.ModificationState.STATE_LOCAL 
            			|| m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING) ? 
            			!equal(m_localValue, aValue) : false;
            	
            	// get next state
                newState = isConflict ? 
                		  IMsoModelIf.ModificationState.STATE_CONFLICTING 
                		: IMsoModelIf.ModificationState.STATE_SERVER;
                
                // any change?
                if (!equal(m_serverValue, aValue))
                {
                    m_serverValue = aValue;
                    valueChanged = true;
                }
                
                // no conflict?
                if(!isConflict) 
                {
                	m_localValue = null;
                }
                
                // notify change
                valueChanged |= isConflict;
                
                
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
            	
            	if (equal(m_serverValue, aValue))
                {
                    newState = IMsoModelIf.ModificationState.STATE_SERVER;
                    m_localValue = null;
                    valueChanged = true;
                } else
                {
                    newState = IMsoModelIf.ModificationState.STATE_LOCAL;
                    if (!equal(m_localValue, aValue))
                    {
                        m_localValue = aValue;
                        valueChanged = true;
                    }
                }
            }
        }
        if (m_state != newState)
        {
            m_state = newState;
            valueChanged = true;
        }
        if (valueChanged && !isCreating)
        {
        	incrementChangeCount();
            m_owner.registerModifiedData();
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
        if (m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING)
        {
            Vector<T> retVal = new Vector<T>(2);
            retVal.add(m_serverValue);
            retVal.add(m_localValue);
            return retVal;
        }
        return null;
    }

    public boolean rollback()
    {
        boolean isChanged = m_state == IMsoModelIf.ModificationState.STATE_LOCAL || m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING;
        if(isChanged)     
        {
	        m_localValue = null;
	        m_state = IMsoModelIf.ModificationState.STATE_SERVER;
        }
        return isChanged;
    }

    public boolean postProcessCommit()
    {
        boolean isChanged = m_state == IMsoModelIf.ModificationState.STATE_LOCAL || m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING;
        if (isChanged)
        {
            m_serverValue = m_localValue;
            m_localValue = null;
            m_state = IMsoModelIf.ModificationState.STATE_SERVER;
        }
       // m_changed = isChanged;
        return isChanged;
    }


    private boolean acceptConflicting(IMsoModelIf.ModificationState aState)
    {
        if (m_state == IMsoModelIf.ModificationState.STATE_CONFLICTING)
        {
            if (aState == IMsoModelIf.ModificationState.STATE_LOCAL)
            {
            	/* ==========================================================
            	 * resolve conflict as a local value state (keep local value)
            	 * 
            	 * Both server and local values must be kept to enable
            	 * future conflict detection  
            	 * 
            	 * ========================================================== */
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
            m_state = aState;
        	incrementChangeCount();
            m_owner.registerModifiedData();
            return true;
        }
        return false;
    }

    public boolean acceptLocal()
    {
        //MsoModelImpl.getInstance().setLocalUpdateMode();
        boolean retVal = acceptConflicting(IMsoModelIf.ModificationState.STATE_LOCAL);
        //MsoModelImpl.getInstance().restoreUpdateMode();
        return retVal;
    }

    public boolean acceptServer()
    {
        //MsoModelImpl.getInstance().setRemoteUpdateMode();
        boolean retVal = acceptConflicting(IMsoModelIf.ModificationState.STATE_SERVER);
        //MsoModelImpl.getInstance().restoreUpdateMode();
        return retVal;
    }

    public boolean isUncommitted()
    {
        return m_state == IMsoModelIf.ModificationState.STATE_LOCAL;
    }

    public boolean isGisAttribute()
    {
        return false;
    }

    public void setRequired(boolean aValue)
    {
        m_required = aValue;
    }

    public boolean isRequired()
    {
        return m_required;
    }

    /*
    public boolean isChanged()
    {
        return m_changed;
    }
    */
    
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
    }

    public static class MsoDouble extends AttributeImpl<Double> implements IMsoDoubleIf
    {
        public MsoDouble(AbstractMsoObject theOwner, String theName)
        {
            super(Long.class, theOwner, theName, 0,Integer.MAX_VALUE, (double) 0);
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

        public void setDTG(String aDTG) throws IllegalMsoArgumentException
        {
            setValue(aDTG);
        }

        public void setDTG(Number aDTG) throws IllegalMsoArgumentException
        {
            setValue(aDTG);
        }

        public void setValue(String aDTG) throws IllegalMsoArgumentException
        {
            Calendar cal = DTG.DTGToCal(aDTG);
            setAttrValue(cal);
        }

        public void setValue(Number aDTG) throws IllegalMsoArgumentException
        {
            Calendar cal = DTG.DTGToCal(aDTG.longValue());
            setAttrValue(cal);
        }

        @Override
        public void set(Calendar aDTG)
        {
            super.set(aDTG);
        }

        public void setValue(Calendar aDTG)
        {
            setAttrValue(aDTG);
        }

        public Calendar getCalendar()
        {
            return getAttrValue();
        }

        public String getDTG()
        {
            return DTG.CalToDTG(getAttrValue());
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
            super.set(aPosition);
        }

        public void setValue(Position aPosition)
        {
            super.setAttrValue(aPosition);
        }

        public void setValue(String anId, Point2D.Double aPoint)
        {
            Position pos = new Position(anId, aPoint);
            setAttrValue(pos);
        }

        public Position getPosition()
        {
            return getAttrValue();
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

        @Override
        public void set(TimePos aTimePos)
        {
            super.set(aTimePos);
        }

        public void setValue(TimePos aTimePos)
        {
            super.setAttrValue(aTimePos);
        }

        public TimePos getTimePos()
        {
            return getAttrValue();
        }

        public boolean isGisAttribute()
        {
            return true;
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

        @Override
        public void set(Polygon aPolygon)
        {
            super.set(aPolygon);
        }

        public void setValue(Polygon aPolygon)
        {
            super.setAttrValue(aPolygon);
        }

        public Polygon getPolygon()
        {
            return getAttrValue();
        }

        public boolean isGisAttribute()
        {
            return true;
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

        @Override
        public void set(Route aRoute)
        {
            super.set(aRoute);
        }

        public void setValue(Route aRoute)
        {
            super.setAttrValue(aRoute);
        }

        public Route getRoute()
        {
            return getAttrValue();
        }

        public boolean isGisAttribute()
        {
            return true;
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

        @Override
        public void set(Track aTrack)
        {
            super.set(aTrack);
        }

        public void setValue(Track aTrack)
        {
            super.setAttrValue(aTrack);
        }

        public Track getTrack()
        {
            return getAttrValue();
        }

        public boolean isGisAttribute()
        {
            return true;
        }
    }

    public static class MsoEnum<E extends Enum> extends AttributeImpl<E> implements IMsoEnumIf<E>
    {
        public MsoEnum(AbstractMsoObject theOwner, String theName, int theCardinality, E anInstance)
        {
            super(anInstance.getClass(), theOwner, theName, theCardinality, Integer.MAX_VALUE, anInstance);
        }

        @Override
        public void set(E anEnum)
        {
            super.set(anEnum);
        }

        public void setValue(E anEnum)
        {
            super.setAttrValue(anEnum);
        }

        public void setValue(String aName)
        {
            E anEnum = enumValue(aName);
            if (anEnum == null)
            {
                //throw new MsoException("Cannot set enum value " + aName + " to " + this); // todo sjekk !!!!!
            }
            super.setAttrValue(enumValue(aName));
        }

        public E getValue()
        {
            return getAttrValue();
        }

        public String getValueName()
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
        if (m_required != attribute.m_required)
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
        if (m_serverValue != null ? !m_serverValue.equals(attribute.m_serverValue) : attribute.m_serverValue != null)
        {
            return false;
        }
        if (m_state != attribute.m_state)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (m_class != null ? m_class.hashCode() : 0);
        result = 31 * result + (m_owner != null ? m_owner.getObjectId().hashCode() : 0); // To avoid eternal loop
        result = 31 * result + (m_name != null ? m_name.hashCode() : 0);
        result = 31 * result + m_indexNo;
        result = 31 * result + (m_required ? 1 : 0);
        result = 31 * result + (m_localValue != null ? m_localValue.hashCode() : 0);
        result = 31 * result + (m_serverValue != null ? m_serverValue.hashCode() : 0);
        result = 31 * result + (m_state != null ? m_state.hashCode() : 0);
        return result;
    }
}
