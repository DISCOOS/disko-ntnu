package org.redcross.sar.gui.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.redcross.sar.data.IDataBinder;
import org.redcross.sar.data.Selector;
import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.MsoBinder;
import org.redcross.sar.mso.data.IMsoAttributeIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.util.MsoUtils;

public abstract class AbstractMsoTableModel<T extends IMsoObjectIf>
								extends AbstractDataTableModel<T,T> {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(AbstractMsoTableModel.class);

	protected IMsoListIf<T> list;
	protected boolean isNameAttribute;

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
			Map<String,IMsoListIf<IMsoObjectIf>> map = source.getListReferences(getDataClass(), true);
			// loop over all lists and select items
			for(IMsoListIf<IMsoObjectIf> it : map.values()) {
				// is item class supported?
				if(isSupported(it.getObjectClass())) {
					load((Collection<T>)it.getItems(),true);
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
		super.load(list.getItems(),append);
	}

	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */

	/**
	 *  Is fired when IDataModel.update(T id, T obj) is called.
	 */
	protected abstract Object getCellValue(int row, String column);

	protected Object[] update(T id, T obj, Object[] data) {
		try {
			int index = findRowFromId(id);
			obj = (obj==null ? getObject(index) : obj);
			if(obj!=null && isNameAttribute) {
				Map<String,IMsoAttributeIf<?>> attrs = obj.getAttributes();
				for(int i=0; i<names.size();i++) {
					String name = names.get(i);
					if(attrs.containsKey(name)) {
						data[i] = MsoUtils.getAttribValue(attrs.get(name));
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
			logger.error("Failed to update table model",e);
		}
		return data;
	}

	/* =============================================================================
	 * Helper methods
	 * ============================================================================= */

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

}
