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
    public List<IChangeRecordIf> getChanges();

    /**
     * Get a list of changed object (one-to-one) references.
     * @return The list
     */
    public List<IChangeIf.IChangeRelationIf> getObjectRelations();

    /**
     * Get a list of changed list (one-to-many) references.
     * @return The list
     */
    public List<IChangeIf.IChangeRelationIf> getListRelations();
        

}
