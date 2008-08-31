package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.ds.IDsIf;
import org.redcross.sar.ds.IDsObjectIf;
import org.redcross.sar.ds.event.IDsUpdateListenerIf;
import org.redcross.sar.ds.event.DsEvent.Update;

public abstract class DsObjectTableModel<T extends IDsObjectIf> 
								extends AbstractTableModel  
								implements IDsUpdateListenerIf {

	private static final long serialVersionUID = 1L;

	private IDsIf<T> ds;
	private Map<IDsObjectIf,Object[]> rows;
	private List<IDsObjectIf> objects;
	private List<String> captions;
	private List<String> attributes;
	
	/* =============================================================================
	 * Constructors
	 * ============================================================================= */
	
	public DsObjectTableModel(IDsIf<T> ds, 
							   String[] attributes,
							   String[] captions) {
		// prepare
		this.rows = new HashMap<IDsObjectIf,Object[]>();		
		this.captions = new ArrayList<String>();
		this.attributes = new ArrayList<String>();
		this.objects = new ArrayList<IDsObjectIf>();
		
		// forward
		install(ds,attributes,captions);
		
	}
	
	/* =============================================================================
	 * Public methods
	 * ============================================================================= */
	
	public void install(IDsIf<T> ds) {
		install(ds,attributes.toArray(),captions.toArray());
	}
	
	public void install(IDsIf<T> ds, Object[] attributes, Object[] captions) {
		// uninstall
		this.rows.clear();
		this.objects.clear();
		this.captions.clear();
		this.attributes.clear();
		if(this.ds!=null) ds.removeUpdateListener(this);
		// prepare
		this.ds = ds;
		// add listener?
		if(ds!=null) ds.addUpdateListener(this);
		// add attributes?
		if(attributes!=null) {
			for(int i=0;i<attributes.length;i++) {
				this.captions.add(captions[i].toString());
				this.attributes.add(attributes[i].toString());
			}
		}
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
	
	public abstract boolean select(T dsObj);
	
	//public abstract void sort();
	
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
	
	/*
	protected void sort(Comparator<IDsObjectIf> comparator) {
		Collections.sort(objects,comparator);
	}
	*/
	
	/* =============================================================================
	 * Private methods
	 * ============================================================================= */
	
	private void add(IDsObjectIf dsObj) {
		int iRow = getRow(dsObj);
		if (iRow == -1) {			
			objects.add(dsObj);
			rows.put(dsObj,new Object[attributes.size()]);
		}
		update(dsObj);
	}
	
	private void update(IDsObjectIf dsObj) {
		Object[] row = rows.get(dsObj);
		update(dsObj,row);
	}
	
	private void update(IDsObjectIf dsObj, Object[] row) {
		try {
			for(int i=0; i<attributes.size();i++) {				
				String name = attributes.get(i);
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
	
	public int getColumnCount() {
		return attributes.size();
	}

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
				this.fireTableRowsInserted(idx[0], idx[1]);
				
    		}   

        	
			break;
			
        case MODIFIED_EVENT:
			
        	for(int i=0;i<count;i++) {
        		
        		T dsObj = data[i];
        		
	        	if(select(dsObj) && dsObjectChanged(dsObj)) {
					// get index of object
					int index = getRow(dsObj);
					// add or update?
					if(index ==-1) add(dsObj);
					else update(dsObj);
					items.add(dsObj);
	        	}	        	
        	}
        	
        	// get indexes
        	idx = getIndexes(items);
        	
    		// is dirty?
    		if(idx!=null) {
    			
    			// notify
				this.fireTableRowsUpdated(idx[0], idx[1]);
				
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
				this.fireTableRowsDeleted(idx[0], idx[1]);
				
				// remove
				for(T it : items) {
					remove(it, false);
				}
				
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
	
