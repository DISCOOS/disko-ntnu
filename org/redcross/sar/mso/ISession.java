package org.redcross.sar.mso;

public interface ISession {
	
	/**
	 * Get mso model from operation id
	 *  
	 * @param String oprID - the operation id
	 * @return A IMsoModelIf instance if exist, <code>null</code> otherwise
	 */
	public IMsoModelIf getMsoModel(String oprID);
	
	

}
