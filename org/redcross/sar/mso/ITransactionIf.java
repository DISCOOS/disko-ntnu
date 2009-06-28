package org.redcross.sar.mso;

import java.util.List;

/**
 * The ITransactionWrapperIf interface defines methods that shall be used by the 
 * transaction handler in order to retrieve the objects and references that shall be committed and rolled back.
 */
public interface ITransactionIf
{
	
	public enum TransactionType {
		COMMIT,
		ROLLBACK
	}
	
	/**
	 * Get the transaction type
	 * 
	 * @return TransactionType object
	 */
	public TransactionType getType();
	
    /**
     * Get a list of change records.
     * @return The list
     */
    public List<IChangeRecordIf> getRecords();

    /**
     * Get a list of changed objects.
     * @return The list
     */
    public List<IChangeIf.IChangeObjectIf> getObjectChanges();

    /**
     * Get a list of changed attributes.
     * @return The list
     */
    public List<IChangeIf.IChangeAttributeIf> getAttributeChanges();
    
    /**
     * Get a list of changed object (one-to-one) references.
     * @return The list
     */
    public List<IChangeIf.IChangeRelationIf> getObjectRelationChanges();

    /**
     * Get a list of changed list (one-to-many) references.
     * @return The list
     */
    public List<IChangeIf.IChangeRelationIf> getListRelationChanges();
        

}
