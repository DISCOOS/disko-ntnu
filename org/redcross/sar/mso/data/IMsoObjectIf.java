package org.redcross.sar.mso.data;

import org.redcross.sar.data.IData;
import org.redcross.sar.mso.IChangeIf;
import org.redcross.sar.mso.IChangeRecordIf;
import org.redcross.sar.mso.IMsoManagerIf;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.util.except.InvalidRelationException;
import org.redcross.sar.util.except.TransactionException;
import org.redcross.sar.util.except.UnknownAttributeException;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for MSO objects
 */
public interface IMsoObjectIf extends IMsoDataIf
{
    /**
     * Get Object ID
     *
     * @return The Object ID
     */
    public String getObjectId();

    /**
     * Get the setup status.
     * 
     * @return Returns {@code true} if object is 
     * properly initialized in the model, {@code false} otherwise.
     */
    public boolean isSetup();
    
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
    public boolean isOriginLocal();
    
    /**
     * Test for remote update mode
     *
     * @return <code>true</code> if all data are in REMOTE mode. 
     */
    public boolean isOriginRemote();
    
    /**
     * Test for loopback update mode
     *
     * @return <code>true</code> if all data are in LOOPBACK mode 
     * and some data are in REMOTE mode. 
     */
    public boolean isLoopbackMode();
    
    /**
     * Check for mixed update mode
     *
     * @return <code>true</code> if data objects has different origins
     */
    public boolean isOriginMixed();    
    
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
     * 2. All relation changes (object and list, LOCAL and SERVER mode change)</p>
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
    public MsoClassCode getClassCode();

    /**
     * Check if this object is a root object (is not owned by another object)
     * 
     * @return Returns <code>true</code> if this is a root object.
     */
    public boolean isRootObject();
    
    /**
     * Get object owner
     *
     * @return Relation to IMsoObjectIf object.
     */
    public IMsoObjectIf getOwnerObject();
    
    /**
     * Get the main list of which this object belongs.
     * 
     * @return Returns the main list of which this object belongs.
     */    
    public IMsoListIf<? super IMsoObjectIf> getMainList();
    
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
     * @return Returns {@code true} is a commit was performed, 
     * {@code false} otherwise.
     *  
     * @throws TransactionException
     */
    public boolean commit() throws TransactionException;
    
    /**
     * Rollback changes in this object. Generates a client update event.
     * @return Returns {@code true} is a commit was performed, 
     * {@code false} otherwise 
     */    
    public boolean rollback();
    
    /**
     * Delete this object from the data structures
     * @param deep - if <code>true</code> this object and all objects 
     * that is own is deleted (deep deletion). Otherwise, the method  
     * performs a deletion on this object only and notifies any object 
     * holders accordingly. It do not delete owned objects in lists, or 
     * relations from this object to other owned objects explicitly. 
     * <b>IMPORTANT!</b> The result of an shallow deletion is a potential 
     * memory leak, because owned objects may still point to this 
     * object (see method <code>getOwningObjects()</code> for more 
     * information). Hence, this object will not be garbage collected. 
     * In most cases, a deep deletion is the right option. Shallow 
     * deletion should only be used when the deletion process is 
     * strictly controlled such that memory leaks are prevented.
     *
     * @return <code>true</code> if object has been deleted, <code>false</code> otherwise.
     */
    public boolean delete(boolean deep);

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
    public List<IMsoObjectHolderIf> getUndeleteableObjectHolders();

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
     * Get a attribute from index
     * @return The IAttributeIf object
     */    
    public IMsoAttributeIf<?> getAttribute(int anIndex) throws UnknownAttributeException;
    
    /**
     * Get list of all objects owning this objects.
     * 
     * @return Returns a list of objects owning this object.
     */
    public Set<IMsoObjectHolderIf> getOwningObjects();
    
    /**
     * Check if a given IMsoObject is related to by this IMsoObject
     * 
     * @param msoObj - the related object to look for
     * @return Returns <code>true</code> if relation to given object is found 
     */
    public boolean contains(IMsoObjectIf msoObj);
    
    /**
     * Get the object holder for the given IMsoObject. If the relation to
     * the given object is a one-to-one relation between this object
     * and the given object, a IMsoRelationIf instance is returned. If the found 
     * relation is a one-to-many relation, a IMsoListIf instances is returned. 
     * Regardless of this, the owning object is always this object, not the
     * IMsoRelationIf or IMsoListIf instances (they only hold the object relation
     * between objects).
     * 
     * @param msoObj - the related object to look for 
     * @return Returns the object holding the relation between this and the given object. 
     */
    public IMsoObjectHolderIf getObjectHolder(IMsoObjectIf msoObj);
    
    /**
     * Get the holder of the relation between this and the given object
     * 
     * @param msoObj - the related object to look for 
     * @return Returns the holder of the relation between this and the given object. 
     * Note that if more than one relation exists to the given object, the first one 
     * found is returned (starting with one-to-one references).
     */
    public IMsoRelationIf<?> getRelation(IMsoObjectIf msoObj);
    
    /**
     * Set or reset a relation between this and another 
     * IMsoObjectIf (one-to-one relation) 
     * @param anObject - the object set a relation to, or relation to set to <code>null</code>  
     * @param aRelationName - the name of the relation
     * @throws InvalidRelationException is thrown if the relation does not 
     * exist, or if the relation is required (cardinality is greater than <code>0</code>)
     */
    public void setObjectRelation(IMsoObjectIf anObject, String aRelationName) throws InvalidRelationException;

    /**
     * Get a map of the objects related by this object (all one-to-one and one-to-many relations). 
     * The map key is the object (one-to-one) or list (one-to-many) relation name and the value is the 
     * related object(s).
     * 
     * @return Returns a map of the objects related by this object. 
     */
    public Map<String, List<IMsoObjectIf>> getObjects();
    
    /**
     * Get a copy of the object relation map (all one-to-one relations from 
     * this object to other IMsoObjectIf objects). The map key is the relation
     * name and the value is the associated IMsoRelationIf object.
     * 
     * @return Returns a copy of the object relation map. 
     */
    public Map<String, IMsoRelationIf<?>> getObjectRelations();
    
    /**
     * Add a relation to an one-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to add a relation to
     * @param aRelationListName The relation list
     * @throws InvalidRelationException is thrown if the list relation does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, 
     * or if an relation to the IMsoObjectIf object already exists, or the object is 
     * null or not properly initialized 
     */
    public void addListRelation(IMsoObjectIf anObject, String aRelationListName) throws InvalidRelationException;

    /**
     * Remove a relation from a on-to-many relation in this IMsoObjectIf object.
     * 
     * @param anObject The object to remove a relation from
     * @param aRelationListName The relation list
     * @throws InvalidRelationException is thrown if the relation does not 
     * exist, if the list object class and IMsoObjectIf object class does not 
     * match, if the list size is less or equal to the cardinality, or if 
     * an relation to IMsoObjectIf object does not exist, or is not deleteable 
     */
    public void removeListRelation(IMsoObjectIf anObject, String aRelationListName) throws InvalidRelationException;
    
    /**
     * Get a copy of the map of the relation lists for the object (one-to-many relations)
     * @return The lists containing references
     */
    public Map<String,IMsoListIf<?>> getListRelations();

    /**
     * Get a copy of the map the relation lists that contains objects of a given class
     *
     * @param Class c - The item class
     * @param boolean isEqual - It <code>true</code>, only lists with item classes that are equal to passed
     * class is returned. Else, match all items that are assignable onto passed item class.
     *
     * @return The relation lists that match passed arguments.
     */
    public Map<String,IMsoListIf<?>> getListRelations(Class<?> c, boolean isEqual);

    /**
     * Get a copy of the map of relation lists that contains objects of a given MSO class class
     *
     * @param MsoClassCode c - The MSO class code to match
     *
     * @return The relation lists that match passed arguments. This method will
     * only return lists with one or more items in it.
     */
    public Map<String,IMsoListIf<?>> getListRelations(MsoClassCode c);
    
    /**
     * Check if updates are suspended.
     *  
     * @return Returns <code>true</code> if client updates are suspended.
     */
    public boolean isUpdateSuspended();
    
    /**
     * Suspend change notifications to listeners.
     * <p/>
     * Use this method to group all change notifications into one single 
     * event. This will greatly improve the event handling process when a 
     * large number of updates is pending.
     */
    public void suspendChange();

    /**
     * Resume pending change notification to listeners. <p/>
     *
     * Use this method to group all change notifications into one single event. 
     * This will greatly improve the event handling process when a large number of
     * updates is pending. The method has memory function, which ensures 
     * that the same number invocations of {@code suspendChange()} and 
     * {@code resumeChange()} is required to return to the same state. 
     * For example, if changes are suspended by calling {@code suspendChange()}
     * four times, resuming changes requires {@code resumeChange()} to be called
     * four times. This make it possible to enable and disable changes in a
     * object hierarchy.
     * 
     * @param boolean all - if <code>true</code>, resume is also forwarded to all
     * objects related by the object. Else, only changes associated 
     * with object are resumed (changed attributes, references from and to this object).
     * 
     * @return Returns <code>true</code> if suspended updates were resumed. 
     * If no suspended changes were resumed and notified to listeners, or
     * if changes are suspended at the model level (see {@link IMsoModelIf}), 
     * this method returns <code>false</code>.
     */
    public boolean resumeChange(boolean all);

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
     * Get all local changes in object
     * @return Returns a object containing a record of all local changes
     */
    public IChangeRecordIf getChanges();
    
    /**
     * Get list of changed attributes.
     * @return Return list of attributes changed locally
     */    
    public Collection<IChangeIf.IChangeAttributeIf> getAttributeChanges();
    
    /**
     * Get the list of changed object (one-to-one) references.
     *  
     * @return Returns a list of changed object references
     */
    public Collection<IChangeIf.IChangeRelationIf> getObjectRelationChanges();

    /**
     * Get the list of changed list (one-to-many) references.
     *  
     * @return Returns a list of changed list references
     */
    public Collection<IChangeIf.IChangeRelationIf> getListRelationChanges();

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

    public int compareTo(IData anObject);

}
