package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.except.InvalidReferenceException;
import org.redcross.sar.util.except.UnknownAttributeException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface for MSO objects
 */
public interface IMsoObjectIf extends IData
{
    /**
     * Get Object ID
     *
     * @return The Object ID
     */
    public String getObjectId();

    /**
     * Get Object creation time
     *
     * @return The Object ID
     */
    public Calendar getCreatedTime();

    /**
     * Sets created state
     *
     */
    public void setCreatedTime(Date time);

    /**
     * Get Object creation status
     *
     * @return True after successful commit on a created object
     */
    public boolean isCreated();
    
    /**
     * Get Object change status
     *
     * @return True if local changes exists 
     */
    public boolean isChanged();
    

    /**
     * Gets change count since construction. Use this counter when tracking
     * changes executed on a object. The following changes are tracked, and
     * thus will increment the change counter<p>
     * 1. Attribute changes<br>
     * 2. Relation changes<p>
     * This property enables MSO Update listeners to track changes
     * without the need for local buffering of object states (attributes,
     * relations and so on).
     *
     * @return The number of changes performed on the object since the construction.
     */
    public int getChangeCount();

    /**
     * Get short descriptor of object.
     * @return Short description, default = toString(), can be overridden.
     */
    public String shortDescriptor();

    /**
     * Get classcode enumerator for the object.
     * @return The {@link IMsoManagerIf.MsoClassCode} of the object.
     */
    public IMsoManagerIf.MsoClassCode getMsoClassCode();

    /**
     * Set value to an attribute with a given name
     *
     * @param aName  The name
     * @param aValue Value to set
     * @throws UnknownAttributeException If attribute of the given type does not exist
     */
    public void setAttribute(String aName, Object aValue) throws UnknownAttributeException;

    /**
     * @param anIndex
     * @param aValue
     * @throws UnknownAttributeException
     */
    public void setAttribute(int anIndex, Object aValue) throws UnknownAttributeException;

    /**
     * Rollback changes in this object. Generates a client update event.
     */    
    public void rollback();
    
    /**
     * Rollback changes in supplied attributes in this object. Generates a client update event.
     * @param attrs - the attributes to rollback changes in
     */    
    public void rollback(List<IAttributeIf<?>> attrs);
    
    /**
     * Delete this object from the data structures
     *
     * @return <code>true</code> if object has been deleted, <code>false</code> otherwise.
     */
    public boolean delete();

    /**
     * Use this method to check if an object is deleteable. If the MSO model is in
     * REMOTE_UPDATE_MODE or in LOOPBACK_UPDATE_MODE, this method returns
     * <code>true</code> by default. Otherwise, it will check if the Object is in an required
     * relation with another object or list. If so, the method will return <code>false</code>
     * which indicates that <code>delete()</code> will not succeed, preventing the object from
     * being deleted.<p>
     *
     * @return <code>true</code> if object can be deleted, <code>false</code> otherwise.
     */
    public boolean canDelete();

    /**
     * Use this method to get list over <code>IMsoObjectHolderIf</code>
     * that prevents a deletion.<p>
     *
     * @return List of <code>IMsoObjectHolderIf</code> that prevents deletion
     */
    public List<IMsoObjectHolderIf<IMsoObjectIf>> deletePreventedBy();

    /**
     * Get a copy of map of the attributes for the object
     * @return The attributes
     */
    public Map<String, IAttributeIf<?>> getAttributes();

    /**
     * Get a attribute from name
     * @return The IAttributeIf object
     */
    public IAttributeIf<?> getAttribute(String aAttributeName) throws UnknownAttributeException;
    
    /**
     * Get a copy of the map of the reference objects for the object (one-to-one relations)
     * 
     * @return The reference objects
     */
    public Map<String, IMsoObjectIf> getObjectReferences();
    
    /**
     * Set or reset a relation between this and another 
     * IMsoObjectIf (one-to-one relation) 
     * @param anObject - the object set a relation to, or relation to set to <code>null</code>  
     * @param aReferenceName - the name of the relation
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, or if the relation is required (cardinality or 1) 
     */
    public void setObjectReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException;

    /**
     * Get a copy of the map of the reference lists for the object (one-to-many relations)
     * @return The lists containing references
     */
    public Map<String,IMsoListIf<IMsoObjectIf>> getListReferences();

    /**
     * Get a copy of the map the reference lists that contains objects of a given class
     *
     * @param Class c - The item class
     * @param boolean isEqual - It <code>true</code>, only lists with item classes that are equal to passed
     * class is returned. Else, match all items that are assignable onto passed item class.
     *
     * @return The reference lists that match passed arguments.
     */
    public Map<String,IMsoListIf<IMsoObjectIf>> getListReferences(Class<?> c, boolean isEqual);

    /**
     * Get a copy of the map of reference lists that contains objects of a given MSO class class
     *
     * @param MsoClassCode c - The MSO class code to match
     *
     * @return The reference lists that match passed arguments. This method will
     * only return lists with one or more items in it.
     */
    public Map<String,IMsoListIf<IMsoObjectIf>> getListReferences(MsoClassCode c);

    /**
     * Add a reference to an one-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to add a reference to
     * @param aReferenceName The reference list
     * @throws InvalidReferenceException is thrown if the list reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, 
     * or if an reference to the IMsoObjectIf object already exists, or the object is 
     * null or not properly initialized 
     */
    public void addListReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException;

    /**
     * Remove a reference from a on-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to remove a reference from
     * @param aReferenceName The reference list
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, or if 
     * an reference to IMsoObjectIf object does not exist, or is not deleteable 
     */
    public void removeListReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException;

    /**
     * Suspend update notifications to listeners.
     * <p/>
     * Use this method to group all update notifications into one single event. This
     * will greatly improve the event handling process when a large number of
     * updates is pending.
     */
    public void suspendClientUpdate();

    /**
     * Resume pending update notification to listeners. <p/>
     *
     * @param boolean all - if <code>true</code>, resume is also forwarded to all
     * referenced objects. Else, only this object is resumed.
     */
    public void resumeClientUpdate(boolean all);

    /**
     * Validates object states (cardinality of attributes and relations)
     * <p/>
     * @return  <code>true<code> if the object state is valid, invalid
     * IMsoObjectIf, IAttributeIf or IRelationIf otherwise.
     */
    public Object validate();

    /**
     * Tell if the object is to be deleted from the model.
     *
     * Is used when committing changes to tell that the object will be deleted.
     *
     * @return  <code>true<code> if the object has been deleted.
     */
    public boolean hasBeenDeleted();

    /**
     * Get a Boolean attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoBooleanIf getBooleanAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Boolean attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoBooleanIf getBooleanAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get an Integer attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoIntegerIf getIntegerAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get an Integer attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoIntegerIf getIntegerAttribute(String aName) throws UnknownAttributeException;

//    /**
//     * Get a Long attribute with the given index.
//     *
//     * @param anIndex Attribute index.
//     * @return The attribute, if it exists a and is of the right type, otherwise null.
//     * @throws org.redcross.sar.util.except.UnknownAttributeException
//     *          If attribute of the given type does not exist.
//     */
//    public IAttributeIf.IMsoLongIf getLongAttribute(int anIndex) throws UnknownAttributeException;
//
//    /**
//     * Get a Long attribute with the given name.
//     *
//     * @param aName Attribute name.
//     * @return The attribute, if it exists a and is of the right type, otherwise null.
//     * @throws org.redcross.sar.util.except.UnknownAttributeException
//     *          If attribute of the given type does not exist.
//     */
//    public IAttributeIf.IMsoLongIf getLongAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a Double attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoDoubleIf getDoubleAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Double attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoDoubleIf getDoubleAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a String attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoStringIf getStringAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a String attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoStringIf getStringAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a Calendar attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoCalendarIf getCalendarAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Calendar attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoCalendarIf getCalendarAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPositionIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoPositionIf getPositionAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPositionIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoPositionIf getPositionAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoTimePosIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoTimePosIf getTimePosAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoTimePosIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoTimePosIf getTimePosAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoTrackIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoTrackIf getTrackAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoTrackIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoTrackIf getTrackAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoRouteIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoRouteIf getRouteAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoRouteIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoRouteIf getRouteAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPolygonIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoPolygonIf getPolygonAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPolygonIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoPolygonIf getPolygonAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPolygonIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoEnumIf<?> getEnumAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPolygonIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoEnumIf<?> getEnumAttribute(String aName) throws UnknownAttributeException;

    public Collection<IChangeIf.IChangeReferenceIf> getCommittableAttributeRelations();

    public Collection<IChangeIf.IChangeReferenceIf> getCommittableListRelations();

    public interface IObjectIdIf
    {
        public String getId();
        public Calendar getCreatedTime();
        public void setCreatedTime(Date time);
        public boolean isCreated();
    }

    public IMsoModelIf getModel();

    public int compareTo(IData data);

}
