package org.redcross.sar.mso.data;

import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.committer.ICommittableIf;
import org.redcross.sar.util.except.UnknownAttributeException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Interface for MSO object
 */
public interface IMsoObjectIf
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
     *                                   <p/>
     *                                   .
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
    public boolean deleteObject();

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
    public IAttributeIf.IMsoEnumIf getEnumAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IAttributeIf.IMsoPolygonIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IAttributeIf.IMsoEnumIf getEnumAttribute(String aName) throws UnknownAttributeException;

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
