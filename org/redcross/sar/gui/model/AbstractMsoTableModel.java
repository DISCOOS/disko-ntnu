package org.redcross.sar.gui.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.Selector;
import org.redcross.sar.data.IData.DataOrigin;
import org.redcross.sar.data.IData.DataState;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoBinder;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.data.IMsoRelationIf;

public abstract class AbstractMsoTableModel<T extends IMsoObjectIf>
								extends AbstractDataTableModel<T,T> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AbstractMsoTableModel.class);

	protected IMsoListIf<T> list;
	protected boolean isNameAttribute;
	protected Map<T,OriginChange> origins;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractMsoTableModel(Class<T> c,
			boolean isNameAttribute) {
		// forward
		super(c);
		// prepare
		this.isNameAttribute = isNameAttribute;
	}

	public AbstractMsoTableModel(Class<T> c,
							String[] names,
							String[] captions,
							boolean isNameAttribute) {
		// forward
		super(c,names,captions);
		// prepare
		this.isNameAttribute = isNameAttribute;

	}

	public AbstractMsoTableModel(Class<T> c,
							String[] names,
					   		String[] captions,
					   		String[] tooltips,
							boolean isNameAttribute) {
		// forward
		super(c,names,captions,tooltips);
		// prepare
		this.isNameAttribute = isNameAttribute;

	}

	public AbstractMsoTableModel(Class<T> c,
							String[] names,
							String[] captions,
							String[] tooltips,
							Boolean[] editable,
							String[] editors,
							boolean isNameAttribute) {
		// forward
		super(c,names,captions,tooltips,editable,editors);
		// prepare
		this.isNameAttribute = isNameAttribute;

	}

	/* =============================================================================
	 * Public methods
	 * ============================================================================= */

	/**
	 * If <code>true</code>, column name is mapped to attribute name in data object.
	 */
	public boolean isNameAttribute() {
		return isNameAttribute;
	}

	public MsoBinder<T> connect(IMsoModelIf source, Selector<T> selector, Comparator<T> comparator) {
		// forward
		if(isConnected(source)) {
			// get current mso binder
			MsoBinder<T> binder = getMsoBinder();
			binder.setSelector(selector);
			binder.setComparator(comparator);
			return binder;
		}
		else {
			// get binder
			MsoBinder<T> binder = createBinder(source, selector, comparator);
			// forward
			if(connect(binder)) return binder;
		}
		return null;
	}

	public MsoBinder<T> connect(IMsoModelIf source, IMsoListIf<T> list, Comparator<T> comparator) {
		// forward
		if(isConnected(source)) {
			// get current mso binder
			MsoBinder<T> binder = getMsoBinder();
			binder.setSelector(list);
			binder.setComparator(comparator);
			return binder;
		}
		else {
			// get binder
			MsoBinder<T> binder = createBinder(source, createListSelector(list), comparator);
			if(connect(binder)) return binder;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public MsoBinder<T> getMsoBinder() {
		// get MSO selector
		for(IDataBinder<T, ?, ?> it : getBinders()) {
			if(it instanceof MsoBinder) {
				// cast to MsoBinder<T>
				return (MsoBinder<T>)it;
			}
		}
		// failed
		return null;
	}

	@SuppressWarnings("unchecked")
	public boolean load(IMsoObjectIf source) {
		// initialize
		boolean bFlag = false;
		// prepare
		clear();
		// load data from list?
		if(source!=null) {
			// get lists from data class type
			Map<String,IMsoListIf<?>> map = source.getListRelations(getDataClass(), true);
			// loop over all lists and select items
			for(IMsoListIf<?> it : map.values()) {
				// is item class supported?
				if(isSupported(it.getObjectClass())) {
					load((Collection<T>)it.getObjects(),true);
				}
			}
			bFlag = true;
		}
		// finished
		return bFlag;
	}

	public void load(IMsoListIf<T> list) {
		load(list,false);
	}

	public void load(IMsoListIf<T> list, boolean append) {
		super.load(list.getObjects(),append);
	}

	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	/**
	 * This abstract method must be implemented by the extending class.
	 * 
	 * Is fired when IDataModel.update(T id, T obj, Row data) is called. 
	 * 
	 * If {@code isNameAttribute} is {@code true}, then this method 
	 * is only called if no attribute with name equal to the column
	 * name is found in the data object (T obj).
	 * 
	 * @param int row - the row index
	 * @param String column - the column key (name)
	 * @return Returns the cell value given by row and column identifiers
	 */
	@Override
	protected abstract Object getCellValue(int row, String column);

	/**
	 * Is fired when IDataModel.update(T id, T obj, Row data) is called. 
	 * 
	 * If {@code isNameAttribute} is {@code true}, then this method 
	 * is only called if no attribute with name equal to the column
	 * name is found in the data object (T obj).
	 * 
	 * @param int row - the row index
	 * @param String column - the column key (name)
	 * @return Returns the cell origin given by row and column identifiers. 
	 * Default implementation returns always DataOrigin.NONE. 
	 */
	@Override
	protected DataOrigin getCellOrigin(int row, String column) {
		return DataOrigin.NONE;
	}
	
	/**
	 * Is fired when IDataModel.update(T id, T obj, Row data) is called.
	 * 
	 * If {@code isNameAttribute} is {@code true}, then this method 
	 * is only called if no attribute with name equal to the column
	 * name is found in the data object (T obj).
	 * 
	 * @param int row - the row index
	 * @param String column - the column key (name)
	 * @return Returns the cell state given by row and column identifiers. 
	 * Default implementation returns always DataState.NONE. 
	 */
	@Override
	protected DataState getCellState(int row, String column) {
		return DataState.NONE;
	}
	
	/**
	 * This method implements the default algorithm for 
	 * updating a IDataModel instance data to the current data
	 * found in bound IMsoModelIf data sources. 
	 * 
	 * If {@code isNameAttribute} is {@code true}, then this method 
	 * tries to match each column with an IMsoAttributeIf (equal names) 
	 * in the data object (T obj). If a match is found, the associated 
	 * cell, given by the id object (S id) and matched attribute and 
	 * column name, is updated using the attribute data (value, origin 
	 * and state). If no attribute match is found, the update is 
	 * forwarded to {@code getCellValue}, {@code getCellOrigin} and 
	 * {@code getCellState}. Note that the extending class is only 
	 * required to implement the abstract method {@code getCellValue}.  
	 * 
	 * @param S id - The updated row id
	 * @param T obj - The updated data object
	 * @param IRow data - the row to update
	 * 
	 * @return Returns the update row object.
	 */
	@Override
	protected IRow update(T id, T obj, IRow data) {
		try {
			int index = findRowFromId(id);
			obj = (obj==null ? getObject(index) : obj);
			if(obj!=null && isNameAttribute) {
				Map<String,IMsoAttributeIf<?>> attrs = obj.getAttributes();
				for(int i=0; i<names.size();i++) {
					String name = names.get(i);
					ICell cell = data.getCell(i);
					if(attrs.containsKey(name)) {
						IMsoAttributeIf<?> attr = attrs.get(name);
						cell.setValue(attr.get());
						cell.setDataOrigin(attr.getOrigin());
					}
					else {
						cell.setValue(getCellValue(index,name));
						cell.setDataOrigin(getCellOrigin(index,name));
						cell.setDataState(getCellState(index,name));
					}
				}
			}
			else {
				for(int i=0; i<names.size();i++) {
					ICell cell = data.getCell(i);
					String name = names.get(i);
					cell.setValue(getCellValue(index,name));
					cell.setDataOrigin(getCellOrigin(index,name));
					cell.setDataState(getCellState(index,name));
				}
			}
			/*
			if(!obj.isRootObject()) 
			{
				setOrigin(id, obj.getMainList().getReference(obj));
			}
			*/
		}
		catch(Exception e) {
			logger.error("Failed to update table model",e);
		}
		return data;
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private void setOrigin(T id, IMsoRelationIf<? super IMsoObjectIf> refObj) 
	{
		OriginChange change = origins.get(id);
		if(change!=null)
		{
			
		}
		else
		{
			origins.put(id, new OriginChange(refObj));
		}
	}
	
	private MsoBinder<T> createBinder(IMsoModelIf source, Selector<T> selector, Comparator<T> comparator) {
		MsoBinder<T> binder = new MsoBinder<T>(getDataClass());
		binder.setSelector(selector);
		binder.setComparator(comparator);
		binder.connect(source);
		return binder;
	}

	private Selector<T> createListSelector(final IMsoListIf<T> list) {
		Selector<T> selector = new Selector<T>() {
			private IMsoListIf<T> m_list = list;
			public boolean select(T anObject) {
				return m_list.exists(anObject);
			}
		};
		return selector;
	}

	/* =============================================================================
	 * Inner classes
	 * ============================================================================= */
	
	public static class OriginChange 
	{
		DataOrigin m_origin;
		long m_remoteTimeMillis;
		
		private OriginChange(IMsoRelationIf<? super IMsoObjectIf> refObj)
		{
			m_origin = refObj.getOrigin();
			if(m_origin.equals(DataOrigin.REMOTE)) 
			{				
				m_remoteTimeMillis = System.currentTimeMillis();
			}
		}
		
		public DataOrigin getOrigin() 
		{
			return m_origin;			
		}
		
		public long getRemoteTimeMillis()
		{
			return m_remoteTimeMillis;
		}
	}
	
	
}
