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
     * Get a list of committable objects.
     * @return The list
     */
    public List<IChangeIf.IChangeObjectIf> getObjects();

    /**
     * Get a list of committable one-to-one references.
     * @return The list
     */
    public List<IChangeIf.IChangeReferenceIf> getObjectReferences();

    /**
     * Get a list of committable one-to-many references.
     * @return The list
     */
    public List<IChangeIf.IChangeReferenceIf> getListReferences();
        

}
