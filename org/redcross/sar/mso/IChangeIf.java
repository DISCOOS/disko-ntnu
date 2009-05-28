package org.redcross.sar.mso;

import java.util.List;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoReferenceIf;

/**
 * The IChangeIf interface define methods that used by the transaction handler when 
 * committing and rolling back objects and references.
 */
@SuppressWarnings("unchecked")
public interface IChangeIf
{
    /**
     * Types of change indication what has happened to the object/relation.
     */
    public enum ChangeType
    {
    	/**
    	 * The IMsoObjectIf is created (exists only locally)
    	 */
        CREATED,	
        /**
         * The IMsoObjectIf is modified (exists remotely)
         */
        MODIFIED,
        /**
         * The IMsoObjectIf is deleted
         */
        DELETED	
    }
    
    /**
     * Get type of change
     */
    public ChangeType getType();
    
	/**
	 * Methods that used by the transaction handler when committing objects.
	 */
    public interface IChangeObjectIf extends IChangeIf
    {
        /**
        * Get the object to commit.
        */
        public IMsoObjectIf getMsoObject();
        
        /**
         * Check if only some changes should be updated. 
         */
        public boolean isFiltered();
        
        /**
         * Returns list of changed data to commit
         */
		public List<IChangeIf> getChanges();
        
        /**
         * Check if partial change exists
         */
		public boolean containsFilter(IChangeIf filter);
		
        /**
         * Check if partial change exists
         */
		public boolean containsFilter(IMsoAttributeIf<?> attribute);
		
        /**
         * Check if partial change exists
         */
		public boolean containsFilter(IMsoObjectIf reference);
		
    }
    
    public interface IChangeAttributeIf extends IChangeIf {
        
    	/**
         * Get the object that owns the attribute.
         */
         public IMsoObjectIf getOwner();
         
         /**
          * Get the attribute name
          */
         public String getName();
         
         /**
          * 
          * @return Returns a reference to the changed MSO attribute
          */
         public IMsoAttributeIf<?> getMsoAttribute();
    	
    }

	/**
	 * Methods that used by the commit handler when committing references.
	 * Note that a relation is defined by a relation name and two MSO objects.
	 * The name is needed since some classes may have several types of relations 
	 * to another class.
	 * 
	 */
    public interface IChangeReferenceIf extends IChangeIf
    {
        /**
        * Get name of reference.
        */
        public String getName();

        /**
        * Get referring object (owner).
        */
        public IMsoObjectIf getReferringObject();

        /**
        * Get referred object (referenced object).
        */
        public IMsoObjectIf getReferredObject();            
        
        /**
         * Get reference object
         * 
         * @return Returns the object representing the reference between referring and referred objects
         */
        public IMsoReferenceIf<?> getReference();
        
    }

}
