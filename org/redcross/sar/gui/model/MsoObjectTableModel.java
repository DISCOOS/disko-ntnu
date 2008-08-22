package org.redcross.sar.gui.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.redcross.sar.mso.IMsoModelIf;
import org.redcross.sar.mso.IMsoManagerIf.MsoClassCode;
import org.redcross.sar.mso.IMsoModelIf.UpdateMode;
import org.redcross.sar.mso.data.IAttributeIf;
import org.redcross.sar.mso.data.IMsoListIf;
import org.redcross.sar.mso.data.IMsoObjectIf;
import org.redcross.sar.mso.event.IMsoUpdateListenerIf;
import org.redcross.sar.mso.event.MsoEvent;
import org.redcross.sar.mso.event.MsoEvent.Update;
import org.redcross.sar.mso.util.MsoUtils;
import org.redcross.sar.util.mso.Selector;

public abstract class MsoObjectTableModel<T extends IMsoObjectIf> 
								extends AbstractTableModel  
								implements IMsoUpdateListenerIf, Selector<T> {

	private static final long serialVersionUID = 1L;

	private Map<IMsoObjectIf,Object[]> rows = null;
	private List<IMsoObjectIf> objects = null;
	private MsoClassCode code = null;
	private List<String> captions = null;
	private List<String> attributes = null;
	private EnumSet<MsoClassCode> interests = null;
	private EnumSet<MsoClassCode> coUpdateCodes = null;
	
	/* =============================================================================
	 * Constructors
	 * ============================================================================= */
	
	public MsoObjectTableModel(IMsoModelIf model, 
							   MsoClassCode code,
							   String[] attributes,
							   String[] captions) {
		// prepare
		this.rows = new HashMap<IMsoObjectIf,Object[]>();		
		this.captions = new ArrayList<String>();
		this.attributes = new ArrayList<String>();
		this.objects = new ArrayList<IMsoObjectIf>();
		this.interests = EnumSet.of(code);
		this.coUpdateCodes = EnumSet.noneOf(MsoClassCode.class);
		
		// add listener
		model.getEventManager().addClientUpdateListener(this);
		
		// forward
		install(code,attributes,captions);
		
	}
	
	/* =============================================================================
	 * Public methods
	 * ============================================================================= */
	
	public void install(MsoClassCode code, String[] attributes, String[] captions) {
		// prepare
		this.code = code;
		// reset
		this.rows.clear();
		this.captions.clear();
		this.attributes.clear();
		// add?
		if(attributes!=null) {
			for(int i=0;i<attributes.length;i++) {
				this.captions.add(captions[i]);
				this.attributes.add(attributes[i]);
			}
		}
	}
	
	public boolean addCoUpdateClass(MsoClassCode code) {
		if(!coUpdateCodes.contains(code)) {
			coUpdateCodes.add(code);
			if(!interests.contains(code))
				interests.add(code);
			return true;
		}
		return false;
	}
	
	public boolean removeCoUpdateClass(MsoClassCode code) {
		if(coUpdateCodes.contains(code)) {
			coUpdateCodes.remove(code);
			if(this.code!=code) interests.remove(code);
			return true;
		}
		return false;
	}
	
	public void load(IMsoListIf<T> list) {
		rows.clear();
		objects.clear();
		for(T it : list.selectItems(this)) {
			add(it,false);
		}
		sort();
		fireTableDataChanged();
	}
	
	public int getRow(IMsoObjectIf msoObj) {
		int i=0;
		for(IMsoObjectIf it : objects) {			
			if (it.equals(msoObj)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public IMsoObjectIf getMsoObject(int row) {
		return objects.get(row);
	}
	
	public abstract boolean select(T msoObj);
	
	public abstract void sort();
	
	/* =============================================================================
	 * Protected methods
	 * ============================================================================= */
	
	protected abstract Object getMsoValue(IMsoObjectIf msoObj, String name);
	
	protected Object getMsoAttrValue(IAttributeIf attr) {
		return MsoUtils.getAttribValue(attr);
	}

	protected boolean msoObjectCreated(IMsoObjectIf msoObj, int mask) {
		return true;
	}
	
	protected boolean msoObjectChanged(IMsoObjectIf msoObj, int mask) {
		return true;
	}

	protected boolean msoObjectDeleted(IMsoObjectIf msoObj, int mask) {
		return true;
	}
	
	protected void sort(Comparator<IMsoObjectIf> comparator) {
		Collections.sort(objects,comparator);
	}
			
	/* =============================================================================
	 * Private methods
	 * ============================================================================= */
	
	private void add(IMsoObjectIf msoObj, boolean sort) {
		int iRow = getRow(msoObj);
		if (iRow == -1) {			
			objects.add(msoObj);
			rows.put(msoObj,new Object[attributes.size()]);
		}
		update(msoObj,sort);
	}
	
	private void update(IMsoObjectIf msoObj, boolean sort) {
		Object[] row = rows.get(msoObj);
		update(msoObj,row,sort);
	}
	
	private void update(IMsoObjectIf msoObj, Object[] row, boolean sort) {
		try {
			Map attrs = msoObj.getAttributes();
			for(int i=0; i<attributes.size();i++) {
				String name = attributes.get(i);
				if(attrs.containsKey(name)) {
					row[i] = getMsoAttrValue((IAttributeIf)attrs.get(name));
				}
				else {
					row[i] = getMsoValue(msoObj,name);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		if(sort) sort();
	}
	
	private void remove(IMsoObjectIf msoObj, boolean sort) {
		int iRow = getRow(msoObj);
		if (iRow != -1) {
			rows.remove(msoObj);
			objects.remove(iRow);
		}
		if(sort) sort();
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
    	IMsoObjectIf msoObj = objects.get(iRow);
		if(msoObj==null) return null;
		Object[] row = rows.get(msoObj);
		if(row==null) return null;
		if(!(iCol>=0 && iCol<row.length)) return null;
		return row[iCol];
	}
	
	@Override
	public String getColumnName(int column) {
		return captions.get(column);
	}
	
	/* =============================================================================
	 * IMsoUpdateListenerIf implementation
	 * ============================================================================= */
	
	public boolean hasInterestIn(IMsoObjectIf aMsoObject, UpdateMode mode) {
		// consume loopback updates
		if(UpdateMode.LOOPBACK_UPDATE_MODE.equals(mode)) return false;
		// check against interests
		return interests.contains(aMsoObject.getMsoClassCode());
	}
	
	public void handleMsoUpdateEvent(Update e) {
		
		// get flags		
		int mask = e.getEventTypeMask();

		// get flag
        boolean clearAll = (mask & MsoEvent.EventType.CLEAR_ALL_EVENT.maskValue()) != 0;
		
        if(clearAll) {
        	int count = rows.size();
        	// clear?
        	if(count>0) {
        		rows.clear();
        		objects.clear();
				super.fireTableRowsDeleted(0,count-1);
        	}
        }
        else {		
        	
	        boolean createdObject  = (mask & MsoEvent.EventType.CREATED_OBJECT_EVENT.maskValue()) != 0;
	        boolean deletedObject  = (mask & MsoEvent.EventType.DELETED_OBJECT_EVENT.maskValue()) != 0;
	        boolean modifiedObject = (mask & MsoEvent.EventType.MODIFIED_DATA_EVENT.maskValue()) != 0;
	        boolean addedReference = (mask & MsoEvent.EventType.ADDED_REFERENCE_EVENT.maskValue()) != 0;
	        boolean removedReference = (mask & MsoEvent.EventType.REMOVED_REFERENCE_EVENT.maskValue()) != 0;
	        
	        // get MSO object
	        IMsoObjectIf msoObj = (IMsoObjectIf)e.getSource();
	
	        // get flag
	        boolean isCoClass = !code.equals(msoObj.getMsoClassCode());
	
	        // initialize dirty indexes
	        int iAdded = -1;
	        int iChanged = -1;
	        int iDeleted = -1;
	        
	        // is selected?
	        if(select((T)msoObj)) {
	        
		        // add object?
				if (!isCoClass && createdObject) {
					if(msoObjectCreated(msoObj,mask)) {
						add(msoObj,false);
						iAdded = getRow(msoObj);
					}
				}
				
				// is object modified?
				if (!deletedObject && (isCoClass || addedReference || removedReference || modifiedObject)) {
					if(msoObjectChanged(msoObj,mask)) {
						iChanged = getRow(msoObj);
						if(iChanged ==-1) {
							add(msoObj,false);
							iAdded = getRow(msoObj);
						}
						else {
							update(msoObj,false);
						}
					}
				}
				
				// delete object?
				if (!isCoClass && deletedObject) {
					if(msoObjectDeleted(msoObj,mask)) {
						iDeleted = getRow(msoObj);
					}
				}
		
				// sort?
				if(iAdded!=-1 || iChanged!=-1 || iDeleted!=-1) sort();
				
				// notify?
				if(iDeleted==-1 && iAdded!=-1) {
					iAdded = getRow(msoObj);
					this.fireTableRowsInserted(iAdded, iAdded);
				}
				if(iDeleted==-1 && iChanged!=-1) {
					iChanged = getRow(msoObj);
					this.fireTableRowsUpdated(iChanged, iChanged);
				}
				if(iDeleted!=-1) {
					iDeleted = getRow(msoObj);
					this.fireTableRowsDeleted(iDeleted, iDeleted);
					remove(msoObj,false);
					
				}						
	        }
        }
	}
	

}