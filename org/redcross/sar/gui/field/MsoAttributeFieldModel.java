package org.redcross.sar.gui.field;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
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
	public void setOrigin(DataOrigin origin) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void setState(DataState state) {
		throw new UnsupportedOperationException("Not supported");
	}	
	
	@Override
	public V getValue() {
		if(m_attr!=null)
		{
			return m_attr.get();
		}
		return null;
	}	
	
	@Override
	public boolean setValue(V value) {
		if(m_attr!=null)
		{
			m_attr.set(value);
			return true;
		}
		return false;
	}

	@Override
	public V getLocalValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			return m_attr.getLocalValue();
		}
		return null;
	}

	@Override
	public boolean setLocalValue(V value){
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public V getRemoteValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			return m_attr.getRemoteValue();
		}
		return null;
	}

	@Override
	public boolean setRemoteValue(V value){
		throw new UnsupportedOperationException("Not supported");
	}
	
	@Override
	public boolean acceptLocalValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			return m_attr.acceptLocalValue();
		}
		return false;
	}

	@Override
	public boolean acceptRemoteValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			return m_attr.acceptRemoteValue();
		}
		return false;
	}
	
	@Override
	public IMsoAttributeIf<V> getSource() {
		return !isOrigin(DataOrigin.NONE)?m_attr:null;
	}
	
	@Override
	public boolean isBoundTo(Object source) {
		return isEqual(m_attr.getOwnerObject(),source);
	}	
	
	public IChangeAttributeIf getChange() {
		return m_attr!=null ? m_attr.getChange() : null;
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
		if(!isOrigin(DataOrigin.NONE)) {
			try {
				
				return m_attr.commit();
				
			} catch (TransactionException e) {
				m_logger.error("Failed to commit attribute value",e);
	
			}
		}
		return false;
	}
	
	public boolean rollback() {
		if(!isOrigin(DataOrigin.NONE)) {

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
			IMsoObjectIf msoObj = m_attr.getOwnerObject();
			IMsoModelIf msoModel = msoObj.getModel();
			if(msoModel.exists())
			{
				return m_attr.getState();
			}
		}
		// attribute source does not exist
		return DataState.NONE;
	}
	
	@Override
	protected DataOrigin translateOrigin() {
		if(m_attr!=null) 
		{
			IMsoObjectIf msoObj = m_attr.getOwnerObject();
			IMsoModelIf msoModel = msoObj.getModel();
			if(msoModel.exists())
			{
				return m_attr.getOrigin();
			}
		}
		// attribute does not exist
		return DataOrigin.NONE;
	}

}
