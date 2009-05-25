package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.except.InvalidReferenceException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.UnknownAttributeException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface for MSO objects
 */
public interface IMsoObjectIf extends IData, IMsoDataStateIf
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
     * Test for local update mode
     *
     * @return <code>true</code> if all data are in LOCAL mode. 
     */
    public boolean isLocalState();
    
    /**
     * Test for remote update mode
     *
     * @return <code>true</code> if all data are in REMOTE mode. 
     */
    public boolean isRemoteState();
    
    /**
     * Test for loopback update mode
     *
     * @return <code>true</code> if all data are in LOOPBACK mode 
     * and some data are in REMOTE mode. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Test for mixed update mode
     *
     * @return <code>true</code> if data has more than one update mode 
     */
    public boolean isMixedState();    
    
    /**
     * Get change status
     *
     * @return <code>true</code> if local changes exists. 
     */
    public boolean isChanged();
    
    /**
     * Get change status
     *
     * @return <code>true</code> if any change has occurred 
     */
    public boolean isChangedSince(int changeCount);
    
    /**
     * Gets change count since construction. Use this counter when tracking
     * changes executed on a object. </p>
     * 
     * The following changes are tracked:
     * 1. All attribute changes (LOCAL and SERVER mode change)<br>
     * 2. All reference changes (object and list, LOCAL and SERVER mode change)</p>
     * 
     * This property enables IMsoClientUpdateListeners to track incremental 
     * changes without the need for local buffering of object states 
     * (attributes, relations and so on).
     *
     * @return The number of changes performed on the object since it's construction.
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
     * 
     * Perform commit on the object. Generates a client update event.
     *
     * @return True if something has been done.
     * @throws TransactionException
     */
    public boolean commit() throws TransactionException;
    
    /**
     * Rollback changes in this object. Generates a client update event.
     */    
    public void rollback();
    
    /**
     * Rollback changes in supplied objects owned by this object. 
     * Generates a client update event it a rollback occurs
     * @param objects - the objects to rollback changes in
     */    
    public void rollback(List<IChangeIf> objects);
    
    /**
     * Delete this object from the data structures
     *
     * @return <code>true</code> if object has been deleted, <code>false</code> otherwise.
     */
    public boolean delete();

    /**
     * Use this method to check if an object is deleteable. If the MSO model is in
     * REMOTE_UPDATE_MODE, this method returns <code>true</code> by default. Otherwise, 
     * it will check if the Object is in an required relation with another object or list. 
     * If so, the method will return <code>false</code> which indicates that 
     * <code>delete()</code> will not succeed, preventing the object from being deleted.<p>
     *
     * @return <code>true</code> if object can be deleted, <code>false</code> otherwise.
     */
    public boolean isDeletable();

    /**
     * Use this method to get list over <code>IMsoObjectHolderIf</code>
     * that prevents a deletion.<p>
     *
     * @return List of <code>IMsoObjectHolderIf</code> that prevents deletion
     */
    public List<IMsoObjectHolderIf<IMsoObjectIf>> getUndeleteableReferenceHolders();

    /**
     * Get a copy of map of the attributes for the object
     * @return The attributes
     */
    public Map<String, IMsoAttributeIf<?>> getAttributes();

    /**
     * Get a attribute from name
     * @return The IAttributeIf object
     */
    public IMsoAttributeIf<?> getAttribute(String aAttributeName) throws UnknownAttributeException;
    
    
    /**
     * Check if a given IMsoObject is referenced to by this IMsoObject
     * 
     * @param msoObj - the referenced object to look for
     * @return Returns <code>true</code> if reference to given object is found 
     */
    public boolean contains(IMsoObjectIf msoObj);
    
    /**
     * Get the object holder for the given IMsoObject. It the reference to
     * the given object is a one-to-one reference between this object
     * and the given object, a IMsoReferenceIf instance is returned. If the found 
     * reference is a one-to-many reference, a IMsoListIf instances is returned. 
     * Regardless of this, the owning object is always this object, not the
     * IMsoReferenceIf or IMsoListIf instances (they only hold the object reference
     * between objects).
     * 
     * @param msoObj - the object to look for 
     * @return Returns the object holding the reference between this and the given object. 
     */
    public IMsoObjectHolderIf<?> getObjectHolder(IMsoObjectIf msoObj);
    
    /**
     * Get the holder of the reference between this and the given object
     * 
     * @param msoObj - the referenced object to look for 
     * @return Returns the holder of the reference between this and the given object
     */
    public IMsoReferenceIf<?> getReference(IMsoObjectIf msoObj);
    
    /**
     * Set or reset a relation between this and another 
     * IMsoObjectIf (one-to-one relation) 
     * @param anObject - the object set a relation to, or relation to set to <code>null</code>  
     * @param aReferenceName - the name of the relation
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, or if the relation is required (cardinality is greater than <code>0</code>)
     */
    public void setObjectReference(IMsoObjectIf anObject, String aReferenceName) throws InvalidReferenceException;

    /**
     * Get a map of the objects referenced by this object (all one-to-one and one-to-many relations). 
     * The map key is the object (one-to-one) or list (one-to-many) reference name and the value is the 
     * referenced object(s).
     * 
     * @return Returns a map of the objects referenced by this object. 
     */
    public Map<String, List<IMsoObjectIf>> getObjects();
    
    /**
     * Get a copy of the object reference map (all one-to-one relations from 
     * this object to other IMsoObjectIf objects). The map key is the reference
     * name and the value is the associated IMsoReferenceIf object.
     * 
     * @return Returns a copy of the object reference map. 
     */
    public Map<String, IMsoReferenceIf<?>> getObjectReferences();
    
    /**
     * Add a reference to an one-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to add a reference to
     * @param aReferenceListName The reference list
     * @throws InvalidReferenceException is thrown if the list reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, 
     * or if an reference to the IMsoObjectIf object already exists, or the object is 
     * null or not properly initialized 
     */
    public void addListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException;

    /**
     * Remove a reference from a on-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to remove a reference from
     * @param aReferenceListName The reference list
     * @throws InvalidReferenceException is thrown if the reference does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, or if 
     * an reference to IMsoObjectIf object does not exist, or is not deleteable 
     */
    public void removeListReference(IMsoObjectIf anObject, String aReferenceListName) throws InvalidReferenceException;
    
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
    public boolean isDeleted();

    /**
     * Get a Boolean attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoBooleanIf getBooleanAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Boolean attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoBooleanIf getBooleanAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get an Integer attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoIntegerIf getIntegerAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get an Integer attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoIntegerIf getIntegerAttribute(String aName) throws UnknownAttributeException;

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
    public IMsoAttributeIf.IMsoDoubleIf getDoubleAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Double attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoDoubleIf getDoubleAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a String attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoStringIf getStringAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a String attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoStringIf getStringAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a Calendar attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoCalendarIf getCalendarAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a Calendar attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoCalendarIf getCalendarAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPositionIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoPositionIf getPositionAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPositionIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoPositionIf getPositionAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoTimePosIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoTimePosIf getTimePosAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoTimePosIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoTimePosIf getTimePosAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoTrackIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoTrackIf getTrackAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoTrackIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoTrackIf getTrackAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoRouteIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoRouteIf getRouteAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoRouteIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoRouteIf getRouteAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPolygonIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoPolygonIf getPolygonAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPolygonIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoPolygonIf getPolygonAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPolygonIf} attribute with the given index.
     *
     * @param anIndex Attribute index.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoEnumIf<?> getEnumAttribute(int anIndex) throws UnknownAttributeException;

    /**
     * Get a {@link org.redcross.sar.mso.data.IMsoAttributeIf.IMsoPolygonIf}  attribute with the given name.
     *
     * @param aName Attribute name.
     * @return The attribute, if it exists a and is of the right type, otherwise null.
     * @throws org.redcross.sar.util.except.UnknownAttributeException
     *          If attribute of the given type does not exist.
     */
    public IMsoAttributeIf.IMsoEnumIf<?> getEnumAttribute(String aName) throws UnknownAttributeException;

    /**
     * Get list changed attributes.
     * @return Return list of attributes changed locally
     */    
    public Collection<IChangeIf.IChangeAttributeIf> getChangedAttributes();
    
    /**
     * Get sub-list of changed attributes given the partial list.
     * 
     * @return Return sub-list of attributes changed locally
     */    
    public Collection<IChangeIf.IChangeAttributeIf> getChangedAttributes(Collection<IChangeIf> partial);
    
    /**
     * Get the list of changed object (one-to-one) references.
     *  
     * @return Returns a list of changed object references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedObjectReferences();

    /**
     * Get a sub-list of changed object (one-to-one) references given the partial list.
     *  
     * @return Returns a sub-list of changed object references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedObjectReferences(Collection<IChangeIf> partial);
    
    /**
     * Get the list of changed list (one-to-many) references.
     *  
     * @return Returns a list of changed list references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedListReferences();

    /**
     * Get the sub-list of changed list (one-to-many) references given the partial list.
     *  
     * @return Returns a sub-list of changed list references
     */
    public Collection<IChangeIf.IChangeReferenceIf> getChangedListReferences(Collection<IChangeIf> partial);

    /**
     * Interface for unique identification of IMsoObjectIf instances
     * 
     * @author vinjar
     *
     */
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
