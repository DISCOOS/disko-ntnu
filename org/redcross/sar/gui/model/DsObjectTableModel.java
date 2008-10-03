package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.ds.IDsObjectIf;
import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.ds.event.DsEvent.Update;

public abstract class DsObjectTableModel<T extends IDsObjectIf> 
								extends DiskoTableModel  
								implements IDsUpdateListenerIf  {

	private static final long serialVersionUID = 1L;

	protected IDsIf<T> ds;
	protected Map<IDsObjectIf,Object[]> rows;
	protected List<IDsObjectIf> objects;
	
	/* =============================================================================
	 * Constructors
	 * ============================================================================= */
	
	public DsObjectTableModel(IDsIf<T> ds, 
			   String[] attributes,
			   String[] captions) {
		// forward
		this(ds,attributes,captions,captions.clone(),
				defaultEditable(attributes.length),
				defaultEditors(attributes.length,"button"));
	}
	
	public DsObjectTableModel(IDsIf<T> ds, 
			   String[] attributes,
			   String[] captions,
			   String[] tooltips) {
		// forward
		this(ds,attributes,captions,tooltips,
				defaultEditable(attributes.length),
				defaultEditors(attributes.length,"button"));		
	}
	
	public DsObjectTableModel(IDsIf<T> ds, 
							   String[] attributes,
							   String[] captions,
							   String[] tooltips,
							   Boolean[] editable,
							   String[] editors) {
		// forward
		super(attributes,captions,tooltips,editable,editors);
		
		// prepare			
		this.objects = new ArrayList<IDsObjectIf>();
		this.rows = new HashMap<IDsObjectIf,Object[]>();
		
		// forward
		install(ds);
		
	}
	
	/* =============================================================================
	 * Abstract methods
	 * ============================================================================= */
	
	public abstract boolean select(T dsObj);
	
	/* =============================================================================
	 * Public methods
	 * ============================================================================= */
	
	@Override
	public void install(Object[] attributes, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors) {
		install(ds, attributes, captions, tooltips, editable, editors);
	}

	@Override
	public void install(Object[] attributes, Object[] captions) {
		install(ds,attributes, captions);
	}
	
	public void install(IDsIf<T> ds) {
		install(ds,	names.toArray(),
					captions.toArray(),
					tooltips.toArray(),
					editable.toArray(),
					editors.toArray());
	}

	public void install(IDsIf<T> ds, Object[] attributes, Object[] captions) {
		// use captions as tooltips
		install(ds, attributes, captions, captions.clone(),
				defaultEditable(attributes.length),
				defaultEditors(attributes.length,"button"));
	}
			
	public void install(IDsIf<T> ds, Object[] attributes, Object[] captions, Object[] tooltips, Object[] editable, Object[] editors) {
		// forward
		super.install(attributes, captions, tooltips, editable, editors);
		// uninstall
		this.rows.clear();
		this.objects.clear();
		if(this.ds!=null) ds.removeUpdateListener(this);
		// prepare
		this.ds = ds;
		// add listener?
		if(ds!=null) ds.addUpdateListener(this);	
		// forward
		load();
	}
	
	public boolean load() {
		if(ds!=null) {
			rows.clear();
			objects.clear();		
			for(T it : ds.getItems()) {
				if(select(it)) add(it);
			}
			//sort();
			fireTableDataChanged();
			return true;
		}
		return false;
	}
	
	public int getRow(IDsObjectIf dsObj) {
		int i=0;
		for(IDsObjectIf it : objects) {			
			if (it.equals(dsObj)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public IDsObjectIf getDsObject(int row) {
		return objects.get(row);
	}
	
	/* =============================================================================
	 * IDiskoTableModel implementation
	 * ============================================================================= */
		
	public String getHeaderTooltipText(int column) {
		return tooltips.get(column);
	}
	
	public void setHeaderTooltipText(int column, String text) {
		tooltips.set(column,text);
	}
	
	public boolean isHeaderEditable(int column) {
		return editable.get(column);
	}
	
	public void setHeaderEditable(int column, boolean isEditable) {
		editable.set(column,isEditable);
	}
	
	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */
	
	protected abstract Object getDsAttrValue(IDsObjectIf dsObj, String name);
	
	protected boolean dsObjectAdded(IDsObjectIf dsObj) {
		return true;
	}
	
	protected boolean dsObjectChanged(IDsObjectIf dsObj) {
		return true;
	}

	protected boolean dsObjectRemoved(IDsObjectIf dsObj) {
		return true;
	}
	
	/* =============================================================================
	 * Private methods
	 * ============================================================================= */
	
	private void add(IDsObjectIf dsObj) {
		int iRow = getRow(dsObj);
		if (iRow == -1) {			
			objects.add(dsObj);
			rows.put(dsObj,new Object[names.size()]);
		}
		update(dsObj);
	}
	
	private void update(IDsObjectIf dsObj) {
		Object[] row = rows.get(dsObj);
		update(dsObj,row);
	}
	
	private void update(IDsObjectIf dsObj, Object[] row) {
		try {
			for(int i=0; i<names.size();i++) {				
				String name = names.get(i);
				if(dsObj.getAttrIndex(name)!=-1) {
					row[i] = dsObj.getAttrValue(name);
				}
				else {
					row[i] = getDsAttrValue(dsObj,name);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		//if(sort) sort();
	}
	
	private void remove(IDsObjectIf dsObj, boolean sort) {
		int iRow = getRow(dsObj);
		if (iRow != -1) {
			rows.remove(dsObj);
			objects.remove(iRow);
		}
		//if(sort) sort();
	}
	
	/* =============================================================================
	 * AbstractTableModel methods
	 * ============================================================================= */
	
	public int getRowCount() {
		return rows.size();
	}

	public Object getValueAt(int iRow, int iCol) {
    	if(!(iRow>=0 && iRow<objects.size())) return null;
    	IDsObjectIf dsObj = objects.get(iRow);
		if(dsObj==null) return null;
		Object[] row = rows.get(dsObj);
		if(row==null) return null;
		if(!(iCol>=0 && iCol<row.length)) return null;
		return row[iCol];
	}
	
	@Override
	public String getColumnName(int column) {
		return captions.get(column);
	}
		
	@Override
	public int findColumn(String name) {
		for(int i=0; i<captions.size();i++) {
			if(captions.get(i).equals(name))
				return i;
		}
		return -1;
	}

	/* =============================================================================
	 * IDsUpdateListenerIf implementation
	 * ============================================================================= */
	
	@SuppressWarnings({"unchecked" })
	public void handleDsUpdateEvent(Update e) {
		
		// get data
        T[] data = (T[])e.getData();
        
        // get count
        int count = data!=null ? data.length : 0;

        // initialize
        List<T> items = new ArrayList<T>(count);
        List<T> removed = new ArrayList<T>(count);
        
        switch(e.getType()) {
        case ADDED_EVENT:
        	
        	for(int i=0;i<count;i++) {
        		
        		T dsObj = data[i];
        		
        		// add object?
	        	if(select(dsObj) && dsObjectAdded(dsObj)) {
					add(dsObj);
					items.add(dsObj);
	        	}	        	
        	}
        	
        	// get indexes
        	int[] idx = getIndexes(items);
        	
    		// is dirty?
    		if(idx!=null) {
    			
    			// notify
    			/*
    			if(getRowCount()>1)
    				this.fireTableRowsInserted(idx[0], idx[1]);
    			else
    			*/
    			this.fireTableDataChanged();
				
    		}   

        	
			break;
			
        case MODIFIED_EVENT:
			
        	for(int i=0;i<count;i++) {
        		
        		T dsObj = data[i];
        		
        		if(!select(dsObj)) {
					// get index of object
					int index = getRow(dsObj);
					// exists?
					if(index!=-1) {
						removed.add(dsObj);
					}
        		}
        		else if(dsObjectChanged(dsObj)) {
					// get index of object
					int index = getRow(dsObj);
					// add or update?
					if(index==-1) add(dsObj);
					else update(dsObj);
					items.add(dsObj);
	        	}
	        	
        	}
        	    		
        	// get removed indexes
        	idx = getIndexes(removed);
        	
    		// is dirty?
    		if(idx!=null) {
    			
    			// notify
    			/*
    			if(getRowCount()>1)
					this.fireTableRowsDeleted(idx[0], idx[1]);
    			else
    			*/
    			this.fireTableDataChanged();
				
				// remove
				for(T it : items) {
					remove(it, false);
				}
    		}   
    		
        	// get updated indexes
        	idx = getIndexes(items);
        	
    		// is dirty?
    		if(idx!=null) {
    			
    			// notify
    			/*
    			if(getRowCount()>1)
    				this.fireTableRowsUpdated(idx[0], idx[1]);
    			else
    			*/
    			this.fireTableDataChanged();    			
				
    		}   
    		
			break;
        case REMOVED_EVENT:
			
			// get removed objects
			for(int i=0;i<count;i++) {
				T dsObj = data[i];
				if(select(dsObj) && dsObjectRemoved(dsObj)) {
					items.add(dsObj);
				}
			}
			
        	// get indexes
        	idx = getIndexes(items);
        	
    		// is dirty?
    		if(idx!=null) {
    			
    			// notify
    			/*
    			if(getRowCount()>1)
    				this.fireTableRowsDeleted(idx[0], idx[1]);
    			else
    			*/
				
				// remove
				for(T it : items) {
					remove(it, false);
				}
				
    			this.fireTableDataChanged();
    			
    		}        		
        }        
    }
	
	private int[] getIndexes(List<T> items) {
    	
		// initialize
    	int count = items.size();
    	
		// is dirty?
		if(count>0) {
			
			// forward
			//sort();
			
			// initialize indexes
			int min = getRow(items.get(0));
			int max = min;
			// get index range
			for(int i=1;i<count;i++) {
				int index = getRow(items.get(i));
				min = Math.min(min, index);
				max = Math.max(max, index);
			}
			
			// finished
			return new int[]{min,max};
		}   
		
		return null;
		
	}

}
	
