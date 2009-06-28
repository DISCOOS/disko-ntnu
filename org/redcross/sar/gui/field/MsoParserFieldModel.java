package org.redcross.sar.gui.field;

import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IChangeIf.IChangeAttributeIf;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.util.except.TransactionException;

public class MsoParserFieldModel<V extends Object> extends AbstractFieldModel<V> {

	private static final long serialVersionUID = 1L;	

	protected static final Logger m_logger = Logger.getLogger(MsoAttributeFieldModel.class);
	
	private IFieldParser<V, ? super Object[]> m_parser;

	private List<IMsoAttributeIf<?>> m_attrs = new Vector<IMsoAttributeIf<?>>();
	
	/* ==================================================================
	 *  Constructors
	 * ================================================================== */

	public MsoParserFieldModel(IFieldParser<V,? super Object[]> parser, IMsoAttributeIf<?>[] attrs) {
		// forward
		super();
		// prepare
		m_parser = parser;
		// add attributes to list
		for(IMsoAttributeIf<?> it : attrs)
		{
			m_attrs.add(it);
		}
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
		if(!isOrigin(DataOrigin.NONE)) {
			int i = 0;
			Object[] values = new Object[m_attrs.size()];
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				values[i++] = it.get();
			}
			// format values into string
			return getParser().format(values);
		}
		return null;
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean setValue(V value) {
		
		if(!isOrigin(DataOrigin.NONE)) {
			try {
				int i = 0;
				Object[] values = (Object[])getParser().parse(value);
				if(m_attrs.size()>0) {
					suspendUpdates();
					for(IMsoAttributeIf it : m_attrs)
					{
						it.set(values[i++]);
					}					
					resumeUpdates();
				}
				// format values into string
				return true;
			} catch (Exception e) {
				// Consume all exceptions
			}
		}
		return false;
	}	
	
	@Override
	public V getLocalValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			int i = 0;
			Object[] values = new Object[m_attrs.size()];
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				values[i++] = it.getLocalValue();
			}
			// format values into string
			return getParser().format(values);
		}
		return null;
	}

	@Override
	public boolean setLocalValue(V value) {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public V getRemoteValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			int i = 0;
			Object[] values = new Object[m_attrs.size()];
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				values[i++] = it.getRemoteValue();
			}
			// format values into string
			return getParser().format(values);
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
			boolean bFlag = false;
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				bFlag |= it.acceptLocalValue();
			}
			return bFlag;
		}
		return false;
	}

	@Override
	public boolean acceptRemoteValue() {
		if(!isOrigin(DataOrigin.NONE)) {
			boolean bFlag = false;
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				bFlag |= it.acceptRemoteValue();
			}
			return bFlag;
		}
		return false;
	}
	
	@Override
	public List<IMsoAttributeIf<?>> getSource() {
		return !isOrigin(DataOrigin.NONE)?m_attrs:null;
	}
	
	@Override
	public boolean isBoundTo(Object source) {
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			if(isEqual(it.getOwnerObject(),source)) 
				return true;
		}
		return false;
	}	

	
	public boolean commit() {
		if(!isOrigin(DataOrigin.NONE)) {
			try {
				
				boolean bFlag = false;
				for(IMsoAttributeIf<?> it : m_attrs)
				{
					bFlag |= it.commit(); 
				}
				return bFlag;
				
			} catch (TransactionException e) {
				m_logger.error("Failed to commit attribute value",e);
	
			}
		}
		return false;
	}
	
	public boolean rollback() {
		if(!isOrigin(DataOrigin.NONE)) {

			boolean bFlag = false;
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				bFlag |= it.rollback(); 
			}
			
		}
		return false;
	}
	
	public List<IChangeAttributeIf> getChanges() {
		List<IChangeAttributeIf> changes = new Vector<IChangeAttributeIf>();
		if(!isOrigin(DataOrigin.NONE)) {
			for(IMsoAttributeIf<?> it : m_attrs)
			{
				if(it.isChanged())
				{
					changes.add(it.getChange());
				}
			}
			
		}
		return changes;
	}
	
	
	public List<IMsoAttributeIf<?>> getMsoAttributes() {
		return new Vector<IMsoAttributeIf<?>>(m_attrs);
	}
	
	@Override
	public void parse() {
		super.parse();
	}
	
	/**
	 * Get field parser.
	 * 
	 * @return Returns field parser.
	 */
	public IFieldParser<V,? super Object[]> getParser() {
		return m_parser;
	}

	/** 
	 * Set field parser
	 * 
	 */
	public void setParser(IFieldParser<V,? super Object[]> parser) {
		m_parser = parser;
	}
	


	/* ==================================================================
	 *  Protected methods
	 * ================================================================== */

	protected void suspendUpdates()
	{
		List<IMsoModelIf> models = new Vector<IMsoModelIf>();
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			IMsoModelIf model = it.getOwnerObject().getModel();
			if(!models.contains(model))
			{
				models.add(model);
			}
		}
		for(IMsoModelIf it : models)
		{
			it.suspendChange();
		}		
	}
	
	protected void resumeUpdates()
	{
		List<IMsoModelIf> models = new Vector<IMsoModelIf>();
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			IMsoModelIf model = it.getOwnerObject().getModel();
			if(!models.contains(model))
			{
				models.add(model);
			}
		}
		for(IMsoModelIf it : models)
		{
			it.resumeUpdate();
		}
	}
	
	@Override
	protected int translateChangeCount() {
		int count = 0;
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			count += it.getChangeCount(); 
		}		
		return count;
	}
	
	protected boolean isLoopbackMode() {
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			if(it.isLoopbackMode()) 
				return true;
		}		
		return false;
	}
	
	protected boolean isRollbackMode() {
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			if(it.isRollbackMode()) 
				return true;
		}		
		return false;
	}
	
	protected boolean isDeleted() {
		for(IMsoAttributeIf<?> it : m_attrs)
		{
			if(it.isDeleted()) return true;
		}		
		return false;
	}	
	
	@Override
	protected DataState translateState() {
		DataState state = DataState.NONE;
		DataOrigin origin = translateOrigin();
		if(isChanged())
		{
			if(origin.equals(DataOrigin.CONFLICT))
			{
				state = DataState.CONFLICT;
			}
			else 
			{
				state = DataState.CHANGED;
			}
		}
		else 
		{
			if(isRollbackMode())
			{
				state = DataState.ROLLBACK;
			}
			else if(isLoopbackMode())
			{
				state = DataState.LOOPBACK;
			} 
		}
		// mixed state?
		if(isDeleted() && !state.equals(DataState.NONE)) 
		{
			// finished
			return DataState.MIXED;
		}
		
		// finished
		return state;
	}
	
	@Override
	protected DataOrigin translateOrigin() {
        for (IMsoAttributeIf<?> attr : m_attrs)
        {
			IMsoObjectIf msoObj = attr.getOwnerObject();
			IMsoModelIf msoModel = msoObj.getModel();
			if(msoModel.exists())
			{
	        	DataOrigin o = attr.getOrigin();
	        	if(!o.equals(DataOrigin.REMOTE))
	        	{
	        		return o;
	        	}
			}
			else {
				return DataOrigin.NONE;
			}
        }
		// attributes are all remote
		return DataOrigin.REMOTE;
	}

}
