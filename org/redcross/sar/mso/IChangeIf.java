package org.redcross.sar.mso;

import java.util.List;

import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;

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
        CREATED,	// The IMsoObjectIf is created (exists only locally)
        MODIFIED,	// The IMsoObjectIf is modified (exists only remotely)
        DELETED		// The IMsoObjectIf is deleted
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
        public IMsoObjectIf getObject();
        
        /**
         * Tells if only some attributes should be updated. 
         */
        public boolean isPartial();
        
        /**
         * Returns partial list of attributes to commit
         */
		public List<IMsoAttributeIf<?>> getPartial();
        
    }

/**
 * Methods that used by the commit handler when commiting references.
 */
    public interface IChangeReferenceIf extends IChangeIf
    {
        /**
        * Get name of reference.
        */
        public String getReferenceName();

        /**
        * Get referring object.
        */
        public IMsoObjectIf getReferringObject();

        /**
        * Get referred object.
        */
        public IMsoObjectIf getReferredObject();
        
    }

}
