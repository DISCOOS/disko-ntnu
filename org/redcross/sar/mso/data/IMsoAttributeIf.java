package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.mso.*;

import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Vector;

/**
 * Base interface for MSO Attributes
 */
public interface IMsoAttributeIf<T> extends IMsoDataIf
{
	
	/**
     * Get name of attribute
     *
     * @return The name
     */
    public String getName();

    /**
     * Get change status
     *
     * @return <code>true</code> if local changes exists. 
     */
    public boolean isChanged();

    /**
     * Check is value has change since given change count
     *
     * @return <code>true</code> if any change has occurred 
     */
    public boolean isChangedSince(int changeCount);
    
    /**
     * Gets change count since construction. </p>
     * 
     * Each time the attribute value is changed, the counter is incremented. 
     * Use this counter when tracking changes executed on a object. </p>
     * 
     * This property enables IMsoClientUpdateListeners to track incremental 
     * changes without the need for local buffering of attribute states.
     *
     * @return The number of changes performed on the object since the construction.
     */
    public int getChangeCount();

    /**
     * Set index number for the attribute
     * Will force renumbering of attributes.
     *
     * @param anIndexNo The index
     */
    public void setIndexNo(int anIndexNo);

    /**
     * Get index number for the attribute
     *
     * @return The index
     */
    public int getIndexNo();

    /**
     * Get current data origin 
     * @return Returns current data origin.
     */    
    public IData.DataOrigin getOrigin();

    /**
     * Check for given data origin 
     * @param origin - the origin to match
     * @return Returns <code>true</code> if origins match.
     */
    public boolean isOrigin(IData.DataOrigin origin);
    
	/**
     * Check if data is in more than one origin.</p>
     * 
	 * @returns Since attributes only have on data object, mixed origin states are not
	 * possible. This method returns therefore always <code>false</code>.
	 */
    public boolean isOriginMixed();
    
    /**
     * Get attribute value
     * 
     * @return a value of type T
     */
    public T get();
    
    /**
     * Get the local attribute value
     * 
     * @return a value of type T
     */
    public T getLocalValue();
    
    /**
     * Get the remote attribute value
     * 
     * @return a value of type T
     */
    public T getRemoteValue();
    
    
    /**
     * Set attribute value
     * 
     * @param aValue
     */
    public void set(T aValue);

    /**
     * Get conflicting values
     *
     * @return Vector containing the two conflicting values.
     */
    public Vector<T> getConflictingValues();

    /**
     * 
     * Commit changes to remote sources
     *
     * @return Returns <code>true</code> if changes was committed.
     * @throws TransactionException
     */
    public boolean commit() throws TransactionException;
    
    /**
     * Perform rollback on the attribute
     *
     * @return True if something has been done.
     */
    public boolean rollback();

    
    /**
     * Get attribute change 
     *
     * @return Returns a change object is local change exists, <code>null</code> otherwise.
     */
    public IChangeAttributeIf getChange();
    
    /**
     * Get value cardinality
     *
     *@return cardinality, if >0, then getAttrValue can not be null.
     */
    public int getCardinality();

    /**
     * Validates getAttrValue against the value cardinality
     * <p/>
     * @return  <code>true<code> if getAttrValue is not null, <code>false<code> otherwise.
     */
    public boolean validate();
    
    /**
     * Accept local (client) value in case of conflict.
     *
     * @return True if something has been done.
     */
    public boolean acceptLocalValue();

    /**
     * Accept remote value in case of conflict.
     *
     * @return True if something has been done.
     */
    public boolean acceptRemoteValue();

    /**
     * Tell if attribute has been changed and not committed.
     *
     * @return True if not committed
     */
    public boolean isUncommitted();

    /**
     * Tells if this attribute concerns GIS objects
     *
     * @return <code>true</code>  if a GIS attribute, <code>false</code> otherwise.
     */
    public boolean isGisAttribute();

    /**
     * Tells if this attribute is a required object
     *
     * @return <code>true</code>  if required (cannot be null) in order to commit the object, <code>false</code> oterhwise.
     */
    public boolean isRequired();

    /**
     * Get attribute owner
     *
     * @return @return Reference to IMsoObjectIf object.
     */
    public IMsoObjectIf getOwnerObject();
    
    /**
     * Compare attribute indices, used for sorting.
     *
     * @param anObject Object to compare
     * @return As {@link Comparable#compareTo(Object)}
     */
    public int compareTo(IData anObject);

    /**
     * Interface for {@link Boolean} attributes.
     */
    public interface IMsoBooleanIf extends IMsoAttributeIf<Boolean>
    {
        public void setValue(boolean aValue);

        public void setValue(Boolean aValue);

        public boolean booleanValue();
    }

    /**
     * Interface for {@link Integer} attributes.
     */
    public interface IMsoIntegerIf extends IMsoAttributeIf<Integer>
    {
        public void setValue(int aValue);

        public void setValue(Integer aValue);

        public int intValue();

        public long longValue();
    }

//    /**
//     * Interface for {@link Long} attributes.
//     */
//    public interface IMsoLongIf extends IAttributeIf<Long>
//    {
//        public void setValue(long aValue);
//
//        public void setValue(Integer aValue);
//
//        public void setValue(Long aValue);
//
//        public int intValue();
//
//        public long longValue();
//    }

    /**
     * Interface for {@link Double} attributes.
     */
    public interface IMsoDoubleIf extends IMsoAttributeIf<Double>
    {
        public void setValue(long aValue);

        public void setValue(Integer aValue);

        public void setValue(Long aValue);

        public void setValue(Float aValue);

        public void setValue(Double aValue);

        public int intValue();

        public long longValue();

        public double doubleValue();
    }

    /**
     * Interface for {@link String} attributes.
     */
    public interface IMsoStringIf extends IMsoAttributeIf<String>
    {
        public void setValue(String aValue);

        public String getString();
    }

    /**
     * Interface for {@link java.util.Calendar} attributes.
     */
    public interface IMsoCalendarIf extends IMsoAttributeIf<Calendar>
    {
        public void setValue(Calendar aDTG);

        public void set(Calendar aDTG);

        public Calendar getCalendar();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.Position} attributes.
     */
    public interface IMsoPositionIf extends IMsoAttributeIf<Position>
    {
        public void setValue(Position aPosition);

        public void setValue(String anId, Point2D.Double aPoint);

        public Position getPosition();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.TimePos} attributes.
     */
    public interface IMsoTimePosIf extends IMsoAttributeIf<TimePos>
    {
        public void setValue(TimePos aTimePos);

        public TimePos getTimePos();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.Polygon} attributes.
     */
    public interface IMsoPolygonIf extends IMsoAttributeIf<Polygon>
    {
        public void setValue(Polygon aPolygon);

        public Polygon getPolygon();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.Route} attributes.
     */
    public interface IMsoRouteIf extends IMsoAttributeIf<Route>
    {
        public void setValue(Route aRoute);

        public Route getRoute();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.Track} attributes.
     */
    public interface IMsoTrackIf extends IMsoAttributeIf<Track>
    {
        public void setValue(Track aTrack);

        public Track getTrack();
    }

    /**
     * Interface for {@link org.redcross.sar.util.mso.GeoList} attributes.
     */
    public interface IMsoGeoListIf extends IMsoAttributeIf<GeoList>
    {
        public void setValue(GeoList aGeoList);

        public GeoList getGeoList();
    }

    /**
     * Interface for {@link Enum} attributes.
     */
    public interface IMsoEnumIf<E extends Enum<E>> extends IMsoAttributeIf<E>
    {
        public void setValue(E anEnum);

        public void setValue(String aName);

        public E getValue();

        public String getValueName();

        public E enumValue(String aName);

        public List<Calendar> getHistory(E aStatus);

        public Calendar getFirstTime(E aStatus);

        public Calendar getLastTime(E aStatus);

        public double getDuration(E aStatus, boolean total);

        public double getDuration(EnumSet<E> aList, boolean total);

    }

}


