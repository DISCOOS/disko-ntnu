package org.redcross.sar.mso.data;

import org.redcross.sar.data.IDataIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.committer.ICommittableIf;
import org.redcross.sar.util.except.UnknownAttributeException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface for MSO object
 */
public interface IMsoObjectIf extends IDataIf
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
    public void setCreated(Date time);
    
    /**
     * Get Object creation status
     *
     * @return True after successful commit on a created object
     */
    public boolean isCreated();    
    
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
     * Suspend notification of listeners.
     * <p/>
     * Is used when several updates of an object shall be sent to listeners as one event.
     */
    public void suspendClientUpdate();

    /**
     * Add a "listener" to MsoObject deleteObject.
     *
     * @param aHolder Listener to add.
     */
//    public void addDeleteListener(IMsoObjectHolderIf aHolder);

    /**
     * Remove a "listener" to MsoObject deleteObject.
     *
     * @param aHolder Listener to remove.
     */
//    public void removeDeleteListener(IMsoObjectHolderIf aHolder);

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
     * Get a map of the attributes for the object
     * @return The attributes
     */
    public Map<String, IAttributeIf<?>> getAttributes();

    /**
     * Get a map of the reference objects for the object
     * @return The reference objects
     */
    public Map<String, IMsoObjectIf> getReferenceObjects();

    /**
     * Get a map of the reference lists for the object
     * @return The reference lists
     */
    public Map<String,IMsoListIf<IMsoObjectIf>> getReferenceLists();

    /**
     * Get a map of the reference lists for the object containing a given type of mso object
     * 
     * @param Class c - The item class
     * @param boolean isEqual - It <code>true</code>, only lists with item classes that are equal to passed 
     * class is returned. Else, match all items that are assignable onto passed item class. 
     * 
     * @return The reference lists that match passed arguments
     */
    public Map<String,IMsoListIf<IMsoObjectIf>> getReferenceLists(Class<?> c, boolean isEqual);
    
    /**
     * Add a reference to an IMsoObjectIf object.
     *
     * The type of object (class) determines which list to use
     * @param anObject The object to add
     * @param aReferenceName
     * @return <code>true<code/> if the object has been successfully added, <code>false<code/> otherwise.
     */
    public boolean addObjectReference(IMsoObjectIf anObject, String aReferenceName);

    /**
     * Remove a reference to an IMsoObjectIf object.
     *
     * The type of object (class) determines which list to use
     * @param anObject The object to remove
     * @param aReferenceName
     * @return <code>true<code/> if the object has been successfully removed, <code>false<code/> otherwise.
     */
    public boolean removeObjectReference(IMsoObjectIf anObject, String aReferenceName);


    /**
     * Resume notification of listeners.
     * <p/>
     * Is used if notification has been suspended by {@link #suspendClientUpdate()}.
     */
    public void resumeClientUpdate();

    /**
     * Resume notification of listeners in all lists.
     * <p/>
     * Calls {@link MsoListImpl#resumeClientUpdates} for all defined lists.
     */
    public void resumeClientUpdates();

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

//    public void registerAddedReference();

//    public void registerRemovedReference();

//    public void registerRemovedReference(boolean updateServer);

    /**
     * Register modified reference.
     * Can fire a {@link org.redcross.sar.mso.event.MsoEvent}
     */
//    public void registerModifiedReference();

    /**
     * Register modified reference.
     * Can fire a {@link org.redcross.sar.mso.event.MsoEvent}
     */
//    public void registerModifiedReference(boolean updateServer);

//    public void registerCreatedObject();

//    public void registerDeletedObject();

    /**
     * Register modified data.
     * Can fire a {@link org.redcross.sar.mso.event.MsoEvent}
     */
//    public void registerModifiedData();

    /**
     * Rollback local changes.
     */
//    public void rollback();

    /**
     * Commit local changes.
     */
//    public void postProcessCommit();

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

    public Collection<ICommittableIf.ICommitReferenceIf> getCommittableAttributeRelations();

    public Collection<ICommittableIf.ICommitReferenceIf> getCommittableListRelations();

    public interface IObjectIdIf
    {
        public String getId();
        public Calendar getCreatedTime();
        public void setCreated(Date time);
        public boolean isCreated();
    }

}
