package org.redcross.sar.gui.model;

import java.util.Comparator;

import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.IData;
import org.redcross.sar.data.Selector;
import org.redcross.sar.ds.DsBinder;
import org.redcross.sar.ds.IDs;
import org.redcross.sar.ds.IDsObject;

public abstract class AbstractDsTableModel<S extends IData, T extends IDsObject>
								extends AbstractDataTableModel<S, T>  {

	private static final long serialVersionUID = 1L;

	protected boolean isNameAttribute;

	/* =============================================================================
	 * Constructors
	 * ============================================================================= */

	public AbstractDsTableModel(Class<T> c,
			boolean isNameAttribute) {
		// forward
		super(c);
		// prepare
		this.isNameAttribute = isNameAttribute;
	}

	public AbstractDsTableModel(Class<T> c,
							String[] names,
			   				String[] captions,
							boolean isNameAttribute) {
		// forward
		super(c,names,captions);
		// prepare
		this.isNameAttribute = isNameAttribute;

	}

	public AbstractDsTableModel(Class<T> c,
							String[] names,
			   				String[] captions,
			   				String[] tooltips,
							boolean isNameAttribute) {
		// forward
		super(c,names,captions,tooltips);
		// prepare
		this.isNameAttribute = isNameAttribute;

	}

	public AbstractDsTableModel(Class<T> c,
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

	public DsBinder<S,T> connect(IDs<T> source, Selector<T> selector, Comparator<T> comparator) {
		// get binder
		DsBinder<S,T> binder = createDsBinder(source, selector, comparator);
		// forward
		if(connect(binder)) {
			return binder;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public DsBinder<S,T> getDsBinder() {
		// get MSO selector
		for(IDataBinder<S, ?, ?> it : getBinders()) {
			if(it instanceof DsBinder) {
				// cast to DsBinder<S,T>
				return (DsBinder<S,T>)it;
			}
		}
		// failed
		return null;
	}


	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	protected abstract Object getCellValue(int row, String column);

	protected Object[] update(S id, T obj, Object[] data) {
		try {
			int index = findRowFromId(id);
			obj = (obj==null ? getObject(index) : obj);
			if(obj!=null && isNameAttribute) {
				for(int i=0; i<names.size();i++) {
					String name = names.get(i);
					if(obj.getAttrIndex(name)!=-1) {
						data[i] = obj.getAttrValue(name);
					}
					else {
						data[i] = getCellValue(index,name);
					}
				}
			}
			else {
				for(int i=0; i<names.size();i++) {
					data[i] = getCellValue(index,names.get(i));
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

	private DsBinder<S,T> createDsBinder(IDs<T> source, Selector<T> selector, Comparator<T> comparator) {
		DsBinder<S,T> binder = new DsBinder<S,T>(getDataClass());
		binder.setSelector(selector);
		binder.setComparator(comparator);
		binder.connect(source);
		return binder;
	}

}

