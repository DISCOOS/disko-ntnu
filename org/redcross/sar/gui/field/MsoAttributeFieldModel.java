package org.redcross.sar.gui.field;

import org.apache.log4j.Logger;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.TransactionException;

/**
 * This IFieldModel implementation wraps a single IMsoAttributeIf instance </p>
 *  
 * @author kenneth
 *
 * @param <V> - IMsoAttribute value data type
 */
@SuppressWarnings("unchecked")
public class MsoAttributeFieldModel<V> extends AbstractFieldModel<V> {

	private static final long serialVersionUID = 1L;	

	protected static final Logger m_logger = Logger.getLogger(MsoAttributeFieldModel.class);
	
	private IMsoAttributeIf<V> m_attr;
	
	/* ==================================================================
	 *  Constructors
	 * ================================================================== */

	public MsoAttributeFieldModel(IMsoAttributeIf<V> attr) {
		// forward
		super();
		// set attribute
		m_attr = attr;
		// forward
		parse();
	}
	
	/* ==================================================================
	 *  Public methods
	 * ================================================================== */

	@Override
	public V getLocalValue() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			return m_attr.getLocalValue();
		}
		return null;
	}

	@Override
	public V getRemoteValue() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			return m_attr.getRemoteValue();
		}
		return null;
	}

	@Override
	public void setLocalValue(V value){
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			m_attr.set(value);
		}
	}

	@Override
	public boolean acceptLocalValue() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			return m_attr.acceptLocalValue();
		}
		return false;
	}

	@Override
	public boolean acceptRemoteValue() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			return m_attr.acceptRemoteValue();
		}
		return false;
	}
	
	@Override
	public IMsoAttributeIf<V> getSource() {
		return !isOrigin(DataOrigin.ORIGIN_NOSOURCE)?m_attr:null;
	}
	
	@Override
	public boolean isBoundTo(Object source) {
		return isEqual(m_attr.getOwner(),source);
	}	
	
	/**
	 * Get IMsoAttributeIf instance.
	 * 
	 * @return IMsoAttributeIf
	 */
	public IMsoAttributeIf<V> getMsoAttribute() {
		return m_attr;
	}
	
	public boolean commit() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {
			try {
				
				return m_attr.commit();
				
			} catch (TransactionException e) {
				m_logger.error("Failed to commit attribute value",e);
	
			}
		}
		return false;
	}
	
	public boolean rollback() {
		if(!isOrigin(DataOrigin.ORIGIN_NOSOURCE)) {

			return m_attr.rollback();
			
		}
		return false;
	}

	/* ==================================================================
	 *  Protected methods
	 * ================================================================== */

	@Override
	protected int translateChangeCount() {
		return m_attr!=null?m_attr.getChangeCount():0;
	}
	
	@Override
	protected DataState translateState() {
		if(m_attr!=null) {
			IMsoObjectIf msoObj = m_attr.getOwner();
			IMsoModelIf msoModel = msoObj.getModel();
			if(m_attr.isChanged())
				return DataState.STATE_CHANGED;
			else if(m_attr.isRemoteState() || m_attr.isLoopbackMode())
				return DataState.STATE_LOOPBACK;
			else if(m_attr.isRollbackMode())
				return DataState.STATE_ROLLBACK;
			else if(m_attr.isConflictState())
				return DataState.STATE_CONFLICT;			
			else if(msoObj.isDeleted() && msoModel.getMsoManager().operationExists())
				return DataState.STATE_DELETED;
		}
		// attribute source does not exist
		return DataState.STATE_NOSOURCE;
	}
	
	@Override
	protected DataOrigin translateOrigin() {
		if(m_attr!=null) {
			if(m_attr.isLocalState())
				return DataOrigin.ORIGIN_LOCAL;
			else if(m_attr.isRemoteState())
				return DataOrigin.ORIGIN_REMOTE;
			else if(m_attr.isConflictState())
				return DataOrigin.ORIGIN_CONFLICT;
		}
		// attribute does not exist
		return DataOrigin.ORIGIN_NOSOURCE;
	}
	
}
